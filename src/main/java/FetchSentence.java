import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.stream.IntStream;

public class FetchSentence {

    public static void main(String[] args) throws IOException {

        Directory dir = FSDirectory.open(Paths.get("/Users/yan/scratch/qa/indexes/qa-dataset_v1.2"));
        IndexReader reader = DirectoryReader.open(dir);

        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(1, 1));

        Analyzer an = new StandardAnalyzer();

        QueryParser parser = new QueryParser("sentence", an);

        Query q = parser.createMinShouldMatchQuery("sentence", "When did organized agriculture appear in the Nile Valley?", 0.3f);

        TopDocs docs = searcher.search(q, 20);

        IntStream.range(0, 1).forEach( i -> {
            try {
                String sentence = searcher.doc(docs.scoreDocs[i].doc).get("sentence");
                System.out.println(sentence);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}