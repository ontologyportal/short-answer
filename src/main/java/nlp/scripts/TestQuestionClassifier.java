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
import nlp.features.SparseFeatureVector;
import nlp.learning.PassiveAggressiveClassifier;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * A script which runs an existing questions classifier on a test set
 * and reports the results for both gross and fine labeling.
 */
public class TestQuestionClassifier {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String modelsPath = args[0];
        String classifierFileName = args[1];
        String questionsDataPath = args[2];
        boolean gross = args[3].equals("gross");

        QCFeaturizationPipeline pipeline = new QCFeaturizationPipeline(modelsPath);

        QCDataReader reader = new QCDataReader(Paths.get(questionsDataPath, "train"),
                Paths.get(questionsDataPath, "test"), gross, pipeline, "ISO-8859-1");

        PassiveAggressiveClassifier pa = PassiveAggressiveClassifier.load(Paths.get(modelsPath, classifierFileName));

        double score = 0.0;
        for (Pair<Integer, SparseFeatureVector> d : reader.getTest().dataPoints) {

            List<Pair<String, Double>> scorePair = pa.score(d.second);
            String predicted = scorePair.get(0).first;
            String gold = reader.getLabels().label(d.first);

            if (gross) {
                predicted = predicted.split(":")[0];
                gold = gold.split(":")[0];
            }

            score += predicted.equals(gold) ? 1.0 : 0.0;
        }

        System.out.println("accuracy: " + score / reader.getTest().dataPoints.size());
    }
}
