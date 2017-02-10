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


import edu.stanford.nlp.util.Pair;
import nlp.data.DataSet;
import nlp.data.QCDataReader;
import nlp.features.QCFeaturizationPipeline;
import nlp.learning.PassiveAggressiveClassifier;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * A script that trains the questions classifier
 */
public class TrainQuestionClassifier {

    public static void main(String[] args) throws IOException {

        double C = Double.parseDouble(args[0]);

        String modelsPath = args[1];
        String questionsDataPath = args[2];

        QCFeaturizationPipeline pipeline = new QCFeaturizationPipeline(modelsPath);

        QCDataReader reader = new QCDataReader(Paths.get(questionsDataPath, "train"),
                Paths.get(questionsDataPath, "test"), false, pipeline, "ISO-8859-1");

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
