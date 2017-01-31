import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IndexSentences {

    private static void indexDocs(String wikipediaDir, String indexDir) throws IOException {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        Directory d = new SimpleFSDirectory(Paths.get(indexDir));
        IndexWriter writer = new IndexWriter(d, config);

        Files.list(Paths.get(wikipediaDir)).forEach(file -> {
            try {
                Files.lines(file).forEach(line -> {
                    try {
                        // index only non empty lines with content which is other than all spaces
                        if (line.length() > 0 && line.chars().filter(i -> (char)i != ' ').count() > 0)
                            writer.addDocument(toDocument(line));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                System.out.println("File processed.");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        writer.close();
    }

    private static Document toDocument(String line) {
        Document document = new Document();
        document.add(new TextField("sentence", line, Field.Store.YES));
        return document;
    }

    public static void main(String[] args) throws IOException {
        //String wikipediaDir = "/Users/yan/Downloads/wikipedia2014en/wikipedia.txt.dump.20140615-en.SZTAKI/segmented/1";
        String wikipediaDir = "/Users/yan/Downloads/Question_Answer_Dataset_v1.2/segmented";
        String indexDir = "/Users/yan/scratch/qa/indexes/qa-dataset_v1.2";
        indexDocs(wikipediaDir, indexDir);
    }
}
