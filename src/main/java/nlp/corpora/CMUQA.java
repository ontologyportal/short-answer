package nlp.corpora;

import com.articulate.nlp.lucene.LuceneIR;
import com.articulate.nlp.lucene.SearchResult;
import com.articulate.sigma.KBmanager;
import com.articulate.sigma.StringUtil;
import com.articulate.sigma.WordNetUtilities;
import nlp.features.QCFeaturizationPipeline;
import nlp.learning.PassiveAggressiveClassifier;
import nlp.qa.ShortAnswerExtractor;
import nlp.scripts.Demo;
import nlp.scripts.IndexDocuments;
import nlp.semantics.SemanticParser;
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
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
Read data from http://www.cs.cmu.edu/~ark/QA-data/
 Question Generation as a Competitive Undergraduate Course Project
 Noah A. Smith, Michael Heilman, and Rebecca Hwa
 In Proceedings of the NSF Workshop on the Question Generation Shared
 Task and Evaluation Challenge, Arlington, VA, September 2008.
 */
public class CMUQA {

    public enum headers {TITLE, QUESTION, ANSWER, DIFF_Q, DIFF_A, FILE};
    public static HashSet<HashMap<headers,String>> corpus = new HashSet<>();

    private static String corporaPath = System.getenv("CORPORA");
    private static String saPath = corporaPath + File.separator + "short-answer-data";
    private static String indexDir = corporaPath + File.separator + "Question_Answer_Dataset_v1.2/combined/index";
    private static String corpusDir = corporaPath + File.separator + "Question_Answer_Dataset_v1.2/combined/corpus";
    //private static String indexDir = saPath + File.separator + "index"; // args[0];
    private static String modelsPath = saPath + File.separator + "models"; // args[1];
    private static String classifierModel = "question-classifier.pa770.ser" ; // args[2];
    private static String questionsFilePath = corporaPath +
            "/Question_Answer_Dataset_v1.2/question_answer_pairs-combined.txt";

    /****************************************************************
     */
    private static void readCMUQA() {

        LineNumberReader lr = null;
        try {
            String line;
            String filename = questionsFilePath;
            File f = new File(filename);
            if (f == null) {
                System.out.println("Error in readCMUQA(): The file does not exist in " + filename);
                return;
            }
            FileReader r = new FileReader(filename);
            lr = new LineNumberReader(r);
            while ((line = lr.readLine()) != null) {
                String[] valuesAr = line.split("\t");
                HashMap<headers,String> entry = new HashMap<>();
                if (valuesAr.length == 6) {
                    entry.put(headers.TITLE,valuesAr[0]);
                    entry.put(headers.QUESTION,valuesAr[1]);
                    entry.put(headers.ANSWER,valuesAr[2]);
                    entry.put(headers.DIFF_Q,valuesAr[3]);
                    entry.put(headers.DIFF_A,valuesAr[4]);
                    entry.put(headers.FILE,valuesAr[5]);
                    if (!entry.values().contains("NULL")) // eliminate bad corpus lines
                        corpus.add(entry);
                }
                else {
                    System.out.println("Error in readCMUQA(): Bad line: " + line);
                }
            }
        }
        catch (Exception ex) {
            System.out.println("Error in readCMUQA(): " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /****************************************************************
     */
    private static ArrayList<SearchResult> runTest() {

        ArrayList<SearchResult> result = new ArrayList<SearchResult>();
        int right = 0;
        int wrong = 0;
        StandardAnalyzer sa = new StandardAnalyzer();
        String currentQuestion = null;
        try {
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            IndexReader reader = DirectoryReader.open(dir);
            PassiveAggressiveClassifier classifier = PassiveAggressiveClassifier.load(Paths.get(modelsPath, classifierModel));
            QCFeaturizationPipeline featurizer = new QCFeaturizationPipeline(modelsPath);
            ShortAnswerExtractor extractor = new ShortAnswerExtractor(new SemanticParser(), classifier, featurizer);

            for (HashMap<headers,String> entry : corpus) {
                currentQuestion = entry.get(headers.QUESTION);
                currentQuestion = currentQuestion.replace("\"","\\\""); //lucene appears confused by quotes in a query
                currentQuestion = currentQuestion.replace("?",".");
                currentQuestion = currentQuestion.replace("/","\\/");
                currentQuestion = StringUtil.replaceNonAsciiChars(currentQuestion);
                String desiredAnswer = entry.get(headers.ANSWER);
                if (!StringUtil.emptyString(currentQuestion)) {
                    System.out.println("**************************************************");
                    System.out.println("Question: " + currentQuestion);
                    IndexSearcher searcher = new IndexSearcher(reader);
                    searcher.setSimilarity(new BM25Similarity(1, 1));
                    QueryParser parser = new QueryParser(LuceneIR.DOC_CONTENT_FIELD, sa);
                    Query query = parser.parse(currentQuestion);
                    TopDocs results = searcher.search(query, 1);
                    System.out.println("Hits for " + currentQuestion + " -->" + results.totalHits);
                    //ScoreDoc[] hits = Demo.fetchDocumentHits(currentQuestion, searcher, 1);
                    ScoreDoc[] hits = results.scoreDocs;
                    if (hits != null) {
                        System.out.println("hits: " + hits.length);
                        for (int i = 0; i < hits.length; i++) {
                            Document d = Demo.fromScoreDoc(hits[i], searcher);
                            System.out.println("Doc: " + StringUtil.getFirstNChars(d.get(LuceneIR.DOC_CONTENT_FIELD),100));
                            LuceneIR lir = new LuceneIR();
                            lir.initScores(lir.docScore);
                            lir.initScores(lir.sentScore);
                            SearchResult sr = new SearchResult();
                            sr.query = currentQuestion;
                            ArrayList<String> sents = lir.getSentenceAnswers(d, sr, 1);
                            for (String sent : sents) {
                                sr.sentenceAnswers.add(sent);
                                System.out.println("Sentence: " + sent);
                                String answer = extractor.extract(currentQuestion, sent, sr);
                                sr.answers.put(d.get("path"),answer);
                                System.out.println("Answer: " + answer);
                                System.out.println("desiredAnswer: " + desiredAnswer);
                                sr.expectedAnswer = desiredAnswer;
                                if (!StringUtil.emptyString(desiredAnswer) &&
                                        !StringUtil.emptyString(answer)) {
                                    if (desiredAnswer.toLowerCase().contains(answer.toLowerCase()))
                                        right++;
                                    else
                                        wrong++;
                                }
                            }
                            result.add(sr);
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            System.out.println("Failed on a question: " + currentQuestion);
            ex.printStackTrace();
        }
        System.out.println("Total right: " + right);
        System.out.println("Total wrong: " + wrong);
        return result;
    }

    /****************************************************************
     * Test whether the UIUC-WN sense mappings cover all of the answers
     * in the CMU corpus.
     */
    public static void testSenseMaps() {

        KBmanager.getMgr().initializeOnce();
        ShortAnswerExtractor.readSenseMap();
        System.out.println("CMUQA.testSenseMaps(): index docs");
        //IndexDocuments.indexDocuments(corpusDir, indexDir);
        System.out.println("CMUQA.testSenseMaps(): Demo.init()");
        Demo.init();
        System.out.println("CMUQA.testSenseMaps(): readCMUQA()");
        readCMUQA();
        System.out.println("CMUQA.testSenseMaps(): IndexDocuments.test()");
        //IndexDocuments.test("England","/home/apease/corpora/Question_Answer_Dataset_v1.2/combined/index");
        System.out.println("CMUQA.testSenseMaps(): IndexDocuments.runTest()");
        ArrayList<SearchResult> result = runTest();
        for (SearchResult sr : result) {
            HashSet<String> synsets = ShortAnswerExtractor.getSenses(sr.UIUCtopCat,sr.UIUC2ndCat);
            for (String ans : sr.answers.values()) {
                if (synsets != null && !WordNetUtilities.isHyponymousWord(ans, synsets)) {
                    System.out.println("non-matching ans: " + ans + " synset: " + synsets);
                }
                if (synsets != null && WordNetUtilities.isHyponymousWord(ans, synsets)) {
                    System.out.println("matching ans: " + ans + " synset: " + synsets);
                }
            }
            if (synsets != null && !WordNetUtilities.isHyponymousWord(sr.expectedAnswer, synsets)) {
                System.out.println("non-matching expected ans: " + sr.expectedAnswer + " synset: " + synsets);
            }
            if (synsets != null && WordNetUtilities.isHyponymousWord(sr.expectedAnswer, synsets)) {
                System.out.println("matching expected ans: " + sr.expectedAnswer + " synset: " + synsets);
            }
        }
    }

    /****************************************************************
     * Test whether the UIUC-WN sense mappings cover all of the answers
     * in the CMU corpus.
     */
    public static void runCMUQAtest() {

        KBmanager.getMgr().initializeOnce();
        ShortAnswerExtractor.readSenseMap();
        System.out.println("CMUQA.main(): index docs");
        //IndexDocuments.indexDocuments(corpusDir, indexDir);
        System.out.println("CMUQA.main(): Demo.init()");
        Demo.init();
        System.out.println("CMUQA.main(): readCMUQA()");
        readCMUQA();
        System.out.println("CMUQA.main(): IndexDocuments.test()");
        //IndexDocuments.test("England","/home/apease/corpora/Question_Answer_Dataset_v1.2/combined/index");
        System.out.println("CMUQA.main(): IndexDocuments.runTest()");
        runTest();
    }

    /****************************************************************
     */
    public static void main(String[] args) {

        testSenseMaps();
    }
}
