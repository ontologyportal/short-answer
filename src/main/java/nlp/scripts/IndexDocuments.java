/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment
 *  for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.scripts;

import com.articulate.nlp.lucene.LuceneIR;
import com.articulate.sigma.StringUtil;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * This class indexed sentences in lucene
 */
public class IndexDocuments {

    /** **************************************************************
     * Recursively add all documents in the directory to the IndexWriter.
     * Add document contents with the keyword "contents"
     * @param filesDir is the directory path
     */
    public static void indexDocumentsRecurse(IndexWriter writer, String filesDir) {

        try {
            File fdir = new File(filesDir);
            File[] files = fdir.listFiles();
            if (files.length <= 1)
                System.out.println("Error in indexDocument(): no files in " + filesDir);
            for (File f : files) {
                if (f.isDirectory())
                    indexDocumentsRecurse(writer,f.getAbsolutePath());
                else {
                    //System.out.println("indexDocumentsRecurse: Add file: " + f);
                    //String contents = parseDoc(f.getAbsolutePath());
                    Document document = new Document();
                    String path = f.getCanonicalPath();
                    document.add(new StringField("path", path, Field.Store.YES));
                    String contents = new String(Files.readAllBytes(Paths.get(path)));
                    //System.out.println("indexDocumentsRecurse: with contents: " +
                    //        StringUtil.getFirstNChars(contents,100));
                    document.add(new TextField(LuceneIR.DOC_CONTENT_FIELD, contents,  Field.Store.YES));
                    writer.addDocument(document);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** **************************************************************
     * Recursively add all documents in the directory to the IndexWriter.  Note that
     * parseDoc() is called because each file actually contains multiple
     * documents.
     * @param filesDir is the directory path
     */
    public static void indexDocuments(String filesDir, String outputDir) {

        try {
            StandardAnalyzer sa = new StandardAnalyzer();
            Directory dir = FSDirectory.open(Paths.get(outputDir));
            IndexWriterConfig config = new IndexWriterConfig(sa);
            config.setSimilarity(new BM25Similarity((float) 1.2, (float) 0.75));
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, config);
            indexDocumentsRecurse(writer,filesDir);
            writer.close();
            //writer.optimize();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /****************************************************************
     */
    public static void indexLines(String inputDir, String indexDir) throws IOException {

        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        Directory d = new SimpleFSDirectory(Paths.get(indexDir));
        IndexWriter writer = new IndexWriter(d, config);

        Files.list(Paths.get(inputDir)).forEach(file -> {
            try {
                Files.lines(file).forEach(line -> {
                    try {
                        // index only non empty lines with content which is other than all spaces
                        if (line.length() > 0 && line.chars().filter(i -> (char) i != ' ').count() > 0)
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

    /****************************************************************
     * @return a document with the sentence field
     */
    private static Document toDocument(String line) {

        Document document = new Document();
        document.add(new TextField(LuceneIR.SENTENCE_CONTENT_FIELD, line, Field.Store.YES));
        return document;
    }

    /****************************************************************
     */
    public static void test(String testString, String indexDir) {

        StandardAnalyzer sa = new StandardAnalyzer();
        try {
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            IndexReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser(LuceneIR.DOC_CONTENT_FIELD, sa);
            Query query = parser.parse(testString);
            TopDocs results = searcher.search(query, 5);
            System.out.println("Hits for " + testString + " -->" + results.totalHits);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /****************************************************************
     * runs the indexing of all the sentences which represents the candidates for the answers
     */
    public static void main(String[] args) throws IOException {

        System.out.println("test invocation with <corpusDir> <indexDir> \"testString\"");
        String corpusDir = args[0];
        String indexDir = args[1];
        String testString = StringUtil.removeEnclosingQuotes(args[2]);
        //indexLines(corpusDir, indexDir);
        indexDocuments(corpusDir, indexDir);
        test(testString,indexDir);
    }
}
