/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical
 *  Theories. AI Communications 26, pp79-97.
 */

package nlp.scripts;

import com.articulate.sigma.KBmanager;
import com.articulate.sigma.StringUtil;
import com.articulate.nlp.lucene.LuceneIR;
import com.articulate.nlp.lucene.SearchResult;
import nlp.features.QCFeaturizationPipeline;
import nlp.learning.PassiveAggressiveClassifier;
import nlp.qa.ShortAnswerExtractor;
import nlp.semantics.SemanticParser;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.articulate.nlp.lucene.LuceneIR.docScore;

public class Demo {

    private static String corporaPath = System.getenv("CORPORA");
    private static String saPath = corporaPath + File.separator + "short-answer-data";
    private static String indexDir = "/home/apease/corpora/Question_Answer_Dataset_v1.2/combined/index";
    //private static String indexDir = saPath + File.separator + "index"; // args[0];
    private static String modelsPath = saPath + File.separator + "models"; // args[1];
    private static String classifierModel = "question-classifier.pa770.ser" ; // args[2];
    private static String questionsFilePath = null; // args.length > 3 ? args[3]: null;

    // assume sentences are indexed, otherwise, index and search over documents, then search sentences
    public static boolean sentencePipeline = true;

    /****************************************************************
     */
    public static void init() {

        try {
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            IndexReader reader = DirectoryReader.open(dir);
        }
        catch (Exception ex) {
            System.out.println("Error in init(): " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /****************************************************************
     */
    public static Document fromScoreDoc(ScoreDoc sd, IndexSearcher searcher) {

        try {
            int docId = sd.doc;
            Document d = searcher.doc(docId);
            return d;
        }
        catch (Exception ex) {
            System.out.println("fromScoreDoc() fail ");
            ex.printStackTrace();
        }
        return null;
    }

    /****************************************************************
     * fetches the answer candidate to be used for the short answer extraction
     */
    public static ArrayList<String> fetchAnswerSentences(String question, IndexReader reader, int num) throws IOException {

        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(1, 1));

        // a special analyzer has to be used
        EnglishAnalyzer an = new EnglishAnalyzer();
        QueryParser parser = new QueryParser("sentence", an);

        // the query has to be be flexible and be able not to match all the terms
        Query q = parser.createMinShouldMatchQuery("sentence", question, 0.3f);
        TopDocs docs = searcher.search(q, 1);

        // find a document, then find sentences from the document
        LuceneIR.initScores(LuceneIR.docScore);
        LuceneIR.initScores(LuceneIR.sentScore);
        LuceneIR lir = new LuceneIR();
        SearchResult sr = new SearchResult();
        sr.query = question;
        parser = new QueryParser(LuceneIR.DOC_CONTENT_FIELD, an);
        q = parser.createMinShouldMatchQuery(LuceneIR.DOC_CONTENT_FIELD, question, 0.3f);
        docs = searcher.search(q, num);
        ScoreDoc[] hits = docs.scoreDocs;
        System.out.println("fetchAnswerSentence(): " + hits.length + " documents found");
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < hits.length; i++) {
            Document d = fromScoreDoc(hits[i], searcher);
            result.addAll(lir.getSentenceAnswers(d, sr, 3));
        }
        return result;
    }

    /****************************************************************
     * fetches the answer candidate to be used for the short answer extraction
     * find sentences directly from the reader
     */
    public static String fetchAnswerSentence(String question, IndexReader reader) throws IOException {

        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(1, 1));

        // a special analyzer has to be used
        EnglishAnalyzer an = new EnglishAnalyzer();
        QueryParser parser = new QueryParser(LuceneIR.SENTENCE_CONTENT_FIELD, an);

        // the query has to be be flexible and be able not to match all the terms
        Query q = parser.createMinShouldMatchQuery(LuceneIR.SENTENCE_CONTENT_FIELD, question, 0.3f);
        TopDocs docs = searcher.search(q, 1);

        // find a document, then find sentences from the document
        if (docs != null && docs.scoreDocs != null && docs.scoreDocs.length > 0)
            return searcher.doc(docs.scoreDocs[0].doc).get(LuceneIR.SENTENCE_CONTENT_FIELD);
        else
            return null;
    }

    /****************************************************************
     * fetches the answer candidate to be used for short answer extraction
     * @param question is the question to which an answer should be found
     * @param
     */
    public static ScoreDoc[] fetchDocumentHits(String question, IndexSearcher searcher,
                                               int num) throws IOException {

        // a special analyzer has to be used
        EnglishAnalyzer an = new EnglishAnalyzer();
        QueryParser parser = new QueryParser(LuceneIR.DOC_CONTENT_FIELD, an);

        // the query has to be be flexible and be able not to match all the terms
        Query q = parser.createMinShouldMatchQuery(LuceneIR.DOC_CONTENT_FIELD, question, 0.3f);
        TopDocs docs = searcher.search(q, num);
        if (docs != null && docs.scoreDocs != null && docs.scoreDocs.length < 1)
            return docs.scoreDocs;
        return null;
    }

    /****************************************************************
     */
    public static void runOneTest(String question) {

        try {
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            IndexReader reader = DirectoryReader.open(dir);

            PassiveAggressiveClassifier classifier = PassiveAggressiveClassifier.load(Paths.get(modelsPath, classifierModel));
            QCFeaturizationPipeline featurizer = new QCFeaturizationPipeline(modelsPath);
            ShortAnswerExtractor extractor = new ShortAnswerExtractor(new SemanticParser(), classifier, featurizer);

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

    /****************************************************************
     * runs the short question extraction demo
     */
    public static void runTest() {

        String currentQuestion = null;
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
                currentQuestion = question;
                System.out.println("**************************************************");
                System.out.println("Question: " + question);
                String sentence = fetchAnswerSentence(question, reader);
                String answer = extractor.extract(question, sentence);
                System.out.println("Sentence: " + sentence);
                System.out.println("Answer: " + answer);
            }
        }
        catch (Exception ex) {
            System.out.println("Failed on a question: " + currentQuestion);
            ex.printStackTrace();
        }
    }

    /****************************************************************
     * runs the short question extraction demo
     */
    public static void interactive() {

        String input = "";
        try {
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            IndexReader reader = DirectoryReader.open(dir);

            PassiveAggressiveClassifier classifier = PassiveAggressiveClassifier.load(Paths.get(modelsPath, classifierModel));
            QCFeaturizationPipeline featurizer = new QCFeaturizationPipeline(modelsPath);
            ShortAnswerExtractor extractor = new ShortAnswerExtractor(new SemanticParser(), classifier, featurizer);
            Scanner scanner = new Scanner(System.in);
            do {
                System.out.print("Enter sentence: ");
                input = scanner.nextLine().trim();
                System.out.println("**************************************************");
                System.out.println("Question: " + input);
                String sentence = fetchAnswerSentence(input, reader);
                String answer = extractor.extract(input, sentence);
                System.out.println("Sentence: " + sentence);
                System.out.println("Answer: " + answer);
            }
            while (!input.equals("quit"));
        }
        catch (Exception ex) {
            System.out.println("Failed on a question: " + input);
            ex.printStackTrace();
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
            runTest();
        }
        if (args != null && args.length > 1 && args[0].equals("-s")) {
            String query = StringUtil.removeEnclosingQuotes(args[1]);
            runOneTest(query);
        }
        if (args != null && args.length > 0 && args[0].equals("-i")) {
            interactive();
        }
        if (args != null && args.length > 0 && args[0].equals("-r")) {
            ShortAnswerExtractor.readSenseMap();
            System.out.println(ShortAnswerExtractor.senseMap);
        }
        if (args != null && args.length > 1 && args[0].equals("-x")) {
            String corpusDir = args[1];
            IndexDocuments.indexDocuments(corpusDir, corpusDir + File.separator + "index");
        }
        if (args == null || args.length < 1 || args[0].equals("-h")) {
            System.out.println("Short answer extraction");
            System.out.println("  options:");
            System.out.println("  -h - show this help screen");
            System.out.println("  -s \"<query>\"- runs one query");
            System.out.println("  -t <indexDir> <modelsPath> <classModel> <questionFile> - runs a small set of tests");
            System.out.println("  -i <dir> - runs a loop of conversions of one sentence at a time, optional dir");
            System.out.println("  -x <dir> - indexes files in the directory");
            System.out.println("  -r - run the default test");
        }
    }
}