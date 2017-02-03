/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.learning;

import edu.stanford.nlp.util.Pair;
import nlp.data.DataSet;
import nlp.data.Labels;
import nlp.features.SparseFeatureVector;

import java.io.*;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class implements the passive aggressive classification algorithm
 * from: http://u.cs.biu.ac.il/~jkeshet/papers/CrammerDeKeShSi06.pdf (Crammer 2006)
 *
 * In particular, this is an implementation of a multi-class multi-prototype single-label version.
 */
public class PassiveAggressiveClassifier implements Scorer, Serializable {

    public static final int serialVersionUID = 1;

    private final Labels labels;

    private final boolean lookAtSecondWhenTesting;

    private double C;

    // for each label
    private SparseFeatureVector[] w;

    /****************************************************************
     * @return An instance of the classifier
     * @param C The parameter that controls the effective learning rate of the optimization algorithm
     *          We have seen that setting this value to 0.1, 0.01, 0.001 and 0.0001 results in very different
     *          performances; in particular, the smaller C is, the slower (but steadier) the optimization gets.
     *          However, it then reaches better performance than with larger (more aggressive) values of C.
     *          Proper validation is needed in order to set this hyper parameter.
     */
    public PassiveAggressiveClassifier(double C, Labels labels, boolean lookAtSecondWhenTesting) {

        this.C = C;
        this.labels = labels;
        this.lookAtSecondWhenTesting = lookAtSecondWhenTesting;

        // initialization of all the feature weights (the weights are represented by sparse feature vectors)
        w = new SparseFeatureVector[labels.size()];
        for (int i = 0; i < w.length; i++) {
            w[i] = new SparseFeatureVector();
        }
    }

    /****************************************************************
     * @return the loss given the r and s indices and the data point at hand
     */
    private double loss(int r, int s, SparseFeatureVector x) {

        double margin = w[r].dot(x) - w[s].dot(x);
        if (margin >= 1.0)
            return 0.0;

        return 1.0 - margin;
    }

    /****************************************************************
     * @return index S computed from the data point at hand and the label
     */
    private int getS(SparseFeatureVector x, int y) {
        return labels.labels().filter(l -> l != y).
                mapToObj(l -> new Pair<>(l, w[l].dot(x))).
                max(Comparator.comparingDouble(Pair::second)).get().first;
    }

    private double getTau(SparseFeatureVector x, int y, double l) {

        double t = l / (2 * x.dot(x));

        // this is according to PA-I implementation (see page 555 (Crammer 2006))
        return Math.min(C, t);
    }

    /****************************************************************
     * Make one step for the datapoint x
     * @return the loss
     */
    private double step(SparseFeatureVector x, int y) {

        // r = y (as we are in single label case)
        int r = y;
        int s = getS(x, y);

        double l = loss(y, s, x);
        double tau = getTau(x, y, l);

        SparseFeatureVector rUpdate = x.mult(tau);
        SparseFeatureVector sUpdate = x.mult(-tau);

        // updating in two places r and s
        w[r].add(rUpdate);
        w[s].add(sUpdate);

        return l;
    }

    /****************************************************************
     * Perform a step for each data point in the @param train set.
     * @return the cumulative loss from all the datapoints
     */
    public double train(DataSet train) {

        return train.dataPoints.stream().mapToDouble(d -> step(d.second, d.first)).sum();
    }

    /****************************************************************
     * Run over the test set and classify all the data points
     * @return accuracy over the test set
     */
    public double test(DataSet test) {

        double score = test.dataPoints.stream().mapToDouble(d -> {
            List<Pair<String, Double>> prediction = score(d.second);
            double tempScore = labels.index(prediction.get(0).first) == d.first ? 1.0 : 0.0;

            // if the configuration is to look at the second best candidate
            if (tempScore < 1.0 && lookAtSecondWhenTesting)
                tempScore = labels.index(prediction.get(1).first) == d.first ? 1.0 : 0.0;

            return tempScore;
        }).sum();

        return score / test.dataPoints.size();
    }

    /****************************************************************
     * @return a list of labels(ranking) scored by the current weights in the model
     *         used to actually soft classify the datapoint into the categories
     */
    public List<Pair<String, Double>> score(SparseFeatureVector dataPoint) {

        List<Pair<String, Double>> results = labels.labels().mapToObj(l ->
                new Pair<>(labels.label(l), w[l].dot(dataPoint))).collect(Collectors.toList());

        results.sort(new Pair.BySecondReversePairComparator<>());
        return results;
    }

    /****************************************************************
     * Saves the current model into the @param outPath
     */
    public void save(Path outPath) throws IOException {

        FileOutputStream out = new FileOutputStream(outPath.toFile());
        ObjectOutputStream obj = new ObjectOutputStream(out);
        obj.writeObject(this);
        obj.close();
    }

    /****************************************************************
     * @return an instance of the classifier saved earlier
     */
    public static PassiveAggressiveClassifier load(Path modelPath) throws IOException, ClassNotFoundException {

        FileInputStream in = new FileInputStream(modelPath.toFile());
        ObjectInputStream obj = new ObjectInputStream(in);
        return (PassiveAggressiveClassifier) obj.readObject();
    }
}
