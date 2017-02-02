package nlp.scripts;

import nlp.features.ClassificationFeaturizationPipeline;
import nlp.learning.PassiveAggressiveClassifier;
import nlp.qa.ShortAnswerExtractor;
import nlp.semantics.SemanticParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Demo {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        List<String> questions = new ArrayList<>();

        /***********************************************************
         */

        questions.add("What did Lincoln love?");


        /* *********************************************************
         */

        String indexDir = args[0]; //"/Users/yan/scratch/qa/indexes/qa-dataset_v1.2";
        String modelsPath = args[1]; //"/Users/yan/scratch/qa/question-classification/models";
        String classifierModel = args[2]; //"question-classifier.pa80.ser"

        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(dir);

        PassiveAggressiveClassifier classifier = PassiveAggressiveClassifier.load(Paths.get(modelsPath, classifierModel));
        ClassificationFeaturizationPipeline featurizer = new ClassificationFeaturizationPipeline(modelsPath);
        ShortAnswerExtractor extractor = new ShortAnswerExtractor(new SemanticParser(), classifier, featurizer);

        for (String question : questions) {

            // fetch the candidate
            String sentence = fetchAnswerSentence(question, reader);
            String answer = extractor.extract(question, sentence);

            System.out.println("**************************************************");
            System.out.println("Question: " + question);
            System.out.println("Sentence: " + sentence);
            System.out.println("Answer: " + answer);
        }
    }

    private static String fetchAnswerSentence(String question, IndexReader reader) throws IOException {

        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(1, 1));

        EnglishAnalyzer an = new EnglishAnalyzer();
        QueryParser parser = new QueryParser("sentence", an);
        Query q = parser.createMinShouldMatchQuery("sentence", question, 0.3f);
        TopDocs docs = searcher.search(q, 1);
        return searcher.doc(docs.scoreDocs[0].doc).get("sentence");
    }
}