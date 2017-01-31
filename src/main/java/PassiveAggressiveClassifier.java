import edu.stanford.nlp.util.Pair;
import features.SparseFeatureVector;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PassiveAggressiveClassifier implements Serializable{

    public static final int serialVersionUID = 1;
    private final Labels labels;
    private final boolean lookAtSecondWhenTesting;
    private double C;

    // for each label
    private SparseFeatureVector[] w;

    public PassiveAggressiveClassifier(double C, Labels labels, boolean lookAtSecondWhenTesting) {
        this.C = C;
        this.labels = labels;
        this.lookAtSecondWhenTesting = lookAtSecondWhenTesting;

        w = new SparseFeatureVector[labels.size()];
        for (int i = 0; i < w.length; i++) {
            w[i] = new SparseFeatureVector();
        }
    }

    private double loss(int r, int s, SparseFeatureVector x) {

        double margin = w[r].dot(x) - w[s].dot(x);
        if (margin >= 1.0)
            return 0.0;

        return 1.0 - margin;
    }

    private int getS(SparseFeatureVector x, int y) {
        return labels.labels().filter(l -> l != y).
                mapToObj(l -> new Pair<>(l, w[l].dot(x))).
                max(Comparator.comparingDouble(Pair::second)).get().first;
    }

    private double getTau(SparseFeatureVector x, int y, double l) {

        double t = l / (2 * x.dot(x));
        return Math.min(C, t);

    }

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

    public void train(DataSet train, DataSet validate, int iter) {

        for (int i = 0; i < iter; i++) {
            double currentLoss = train.dataPoints.stream().mapToDouble(d -> step(d.second, d.first)).sum();
            System.out.println(String.format("Current loss over %d data points: %f", train.dataPoints.size(), currentLoss));

            if (i % 10 == 0) {
                System.out.println(String.format("intermediate perf for %d iters, accuracy: %f", i, test(validate)));
            }
        }

        System.out.println(String.format("Ended training after %d iters, accuracy: %f", iter, test(validate)));
    }

    public double test(DataSet test) {
        double score = test.dataPoints.stream().mapToDouble(d -> {
            List<Pair<String, Double>> prediction = predict(d.second);
            double tempScore = labels.index(prediction.get(0).first) == d.first ? 1.0 : 0.0;
            if (tempScore < 1.0 && lookAtSecondWhenTesting) tempScore = labels.index(prediction.get(1).first) == d.first ? 1.0 : 0.0;
            return tempScore;
        }).sum();

        return score / test.dataPoints.size();
    }

    public List<Pair<String, Double>> predict(SparseFeatureVector dataPoint) {

        List<Pair<String, Double>> results = labels.labels().mapToObj(l ->
                new Pair<>(labels.label(l), w[l].dot(dataPoint))).collect(Collectors.toList());

        results.sort(new Pair.BySecondReversePairComparator<>());
        return results;
    }


    public void save(Path outPath) throws IOException {

        FileOutputStream out = new FileOutputStream(outPath.toFile());
        ObjectOutputStream obj = new ObjectOutputStream(out);
        obj.writeObject(this);
        obj.close();
    }

    public static PassiveAggressiveClassifier load(Path modelPath) throws IOException, ClassNotFoundException {

        FileInputStream in = new FileInputStream(modelPath.toFile());
        ObjectInputStream obj = new ObjectInputStream(in);
        return (PassiveAggressiveClassifier) obj.readObject();
    }
}
