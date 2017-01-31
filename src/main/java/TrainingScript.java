import edu.stanford.nlp.util.Pair;

import java.io.IOException;
import java.nio.file.Paths;

public class TrainingScript {

    public static void main(String[] args) throws IOException {

        double C = Double.parseDouble(args[0]);

        String modelsPath = "/Users/yan/scratch/qa/question-classification/models";
        String questionsDataPath = "/Users/yan/scratch/qa/question-classification/data";

        FeaturizationPipeline pipeline = new FeaturizationPipeline(modelsPath);

        pipeline.featurize("What relative of the racoon is sometimes known as the cat-bear.");
        pipeline.featurize("What is the name of the tallest mountain in the world?,");

        DataReader reader = new DataReader(Paths.get(questionsDataPath, "train"),
                Paths.get(questionsDataPath, "test"), false, pipeline, "ISO-8859-1");

        //reader.getTrain().dataPoints.forEach(System.out::println);

        PassiveAggressiveClassifier pa = new PassiveAggressiveClassifier(C, reader.getLabels(), true);

        Pair<DataSet, DataSet> sets = reader.getTrain().split(0.9);

        pa.train(sets.first, sets.second, 1000);

        pa.save(Paths.get(modelsPath, "question-classifier.pa.ser"));
    }
}
