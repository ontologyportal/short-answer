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

import com.articulate.sigma.StringUtil;
import nlp.features.QCFeaturizationPipeline;
import nlp.learning.PassiveAggressiveClassifier;
import nlp.qa.ShortAnswerExtractor;
import nlp.semantics.SemanticParser;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Demo {

    public static String corporaPath = System.getenv("CORPORA");
    public static String saPath = corporaPath + File.separator + "short-answer-data";
    public static String indexDir = saPath + File.separator + "index"; // args[0];
    public static String modelsPath = saPath + File.separator + "models"; // args[1];
    public static String classifierModel = "question-classifier.pa770.ser" ; // args[2];
    public static String questionsFilePath = null; // args.length > 3 ? args[3]: null;

    /****************************************************************
     * fetches the answer candidate to be used for the short answer extraction
     */
    private static String fetchAnswerSentence(String question, IndexReader reader) throws IOException {

        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(1, 1));

        // a special analyzer has to be used
        EnglishAnalyzer an = new EnglishAnalyzer();

        QueryParser parser = new QueryParser("sentence", an);

        // the query has to be be flexible and be able not to match all the terms
        Query q = parser.createMinShouldMatchQuery("sentence", question, 0.3f);
        TopDocs docs = searcher.search(q, 1);
        return searcher.doc(docs.scoreDocs[0].doc).get("sentence");
    }

    /****************************************************************
     * runs the short question extraction demo
     */
    public static void runTest(String indexDir, String modelsPath,
            String classifierModel, String questionsFilePath) {

        List<String> questions = new ArrayList<>();
        questions.add("What did Lincoln love?");
        questions.add("What does Kathy want?");
        questions.add("What is a car?");
        questions.add("What is the estimated population of Egypt?");
        questions.add("Who is a great pet?");
        questions.add("Where does the word otter derive from?");

        try {
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            IndexReader reader = DirectoryReader.open(dir);

            PassiveAggressiveClassifier classifier = PassiveAggressiveClassifier.load(Paths.get(modelsPath, classifierModel));
            QCFeaturizationPipeline featurizer = new QCFeaturizationPipeline(modelsPath);
            ShortAnswerExtractor extractor = new ShortAnswerExtractor(new SemanticParser(), classifier, featurizer);

        // load question from the questions path if it is not null
        if (questionsFilePath != null) {
            questions.clear();
            Files.lines(Paths.get(questionsFilePath)).forEach(questions::add);
        }

            for (String question : questions) {
                System.out.println("**************************************************");
                System.out.println("Question: " + question);
                String sentence = fetchAnswerSentence(question, reader);
                String answer = extractor.extract(question, sentence);
                System.out.println("Sentence: " + sentence);
                System.out.println("Answer: " + answer);
            }
            catch (Exception ex) {
                System.out.println("Failed on a question: " + question);
                ex.printStackTrace();
            }
        }
    }

    /****************************************************************
     * runs the short question extraction demo
     */
    public static void main(String[] args) {

        System.out.println("Demo.java running with: \n corporaPath:\t" + corporaPath);
        System.out.println("saPath:\t" + saPath);
        System.out.println("indexDir:\t" + indexDir);
        System.out.println("modelsPath:\t" + modelsPath);
        System.out.println("classifierModel:\t" + classifierModel);
        System.out.println("questionsFilePath:\t" + questionsFilePath);

        if (args != null && args.length > 0 && args[0].equals("-t")) {
            if (args.length > 1)
                indexDir = args[1];
            if (args.length > 2)
                modelsPath = args[2];
            if (args.length > 3)
                classifierModel = args[3];
            if (args.length > 4)
                questionsFilePath = args[4];
            runTest(indexDir,modelsPath,classifierModel,questionsFilePath);
        }
        if (args != null && args.length > 1 && args[0].equals("-s")) {
            String query = StringUtil.removeEnclosingQuotes(args[1]);
        }
        if (args == null || args.length < 1 || args[0].equals("-h")) {
            System.out.println("Short answer extraction");
            System.out.println("  options:");
            System.out.println("  -h - show this help screen");
            System.out.println("  -s \"<query>\"- runs one query");
            System.out.println("  -t <indexDir> <modelsPath> <classModel> <questionFile> - runs a small set of tests");
            System.out.println("  -i - runs a loop of conversions of one sentence at a time,");
        }
    }
}