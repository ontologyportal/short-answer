package nlp.scripts;


import edu.stanford.nlp.util.Pair;
import nlp.data.DataReader;
import nlp.data.DataSet;
import nlp.features.ClassificationFeaturizationPipeline;
import nlp.learning.PassiveAggressiveClassifier;

import java.io.IOException;
import java.nio.file.Paths;

public class QuestionClassifierTrainingScript {

    public static void main(String[] args) throws IOException {

        double C = Double.parseDouble(args[0]);

        String modelsPath = args[0]; //"/Users/yan/scratch/qa/question-classification/models";
        String questionsDataPath = args[1]; //"/Users/yan/scratch/qa/question-classification/data";

        ClassificationFeaturizationPipeline pipeline = new ClassificationFeaturizationPipeline(modelsPath);

        DataReader reader = new DataReader(Paths.get(questionsDataPath, "train"),
                Paths.get(questionsDataPath, "test"), false, pipeline, "ISO-8859-1");

        //reader.getTrain().dataPoints.forEach(System.out::println);

        PassiveAggressiveClassifier pa = new PassiveAggressiveClassifier(C, reader.getLabels(), false);

        Pair<DataSet, DataSet> sets = reader.getTrain().split(0.9);

        int iter = 1000;
        for (int i = 0; i < iter; i++) {
            double currentLoss = pa.train(sets.first);
            System.out.println(String.format("Current loss over %d data points: %f", sets.first.dataPoints.size(), currentLoss));

            if (i % 10 == 0) {
                System.out.println(String.format("intermediate perf for %d iters, accuracy: %f", i, pa.test(sets.second)));
                pa.save(Paths.get(modelsPath, String.format("question-classifier.pa%d.ser", i)));
            }
        }

        System.out.println(String.format("Ended training after %d iters, accuracy: %f", iter, pa.test(sets.second)));
        pa.save(Paths.get(modelsPath, "question-classifier.pa.ser"));
    }
}
