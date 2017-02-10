/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.scripts;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class indexed sentences in lucene
 */
public class IndexSentences {

    /****************************************************************
     * runs the indexing of all the sentences which represents the candidates for the answers
     */
    public static void main(String[] args) throws IOException {

        String corpusDir = args[0];
        String indexDir = args[1];
        indexDocs(corpusDir, indexDir);
    }

    /****************************************************************
     * indexes the sentences
     */
    private static void indexDocs(String inputDir, String indexDir) throws IOException {

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
        document.add(new TextField("sentence", line, Field.Store.YES));
        return document;
    }
}
