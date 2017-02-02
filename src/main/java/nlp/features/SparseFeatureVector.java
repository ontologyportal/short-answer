package nlp.features;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A sparse feature vector implementation for sparse nlp.features and weights
 */
public class SparseFeatureVector implements Serializable {

    private final Map<String, Double> features;

    public Map<String, Double> getFeatures() {
        return features;
    }

    public SparseFeatureVector(Map<String, Double> features) {
        this.features = features;
    }

    public SparseFeatureVector() {
        this.features = new HashMap<>();
    }

    public double dot(SparseFeatureVector other) {
        return this.features.keySet().stream().filter(other.features::containsKey).
                mapToDouble(k -> value(k) * other.value(k)).sum();
    }

    public Double value(String key) {
        return features.getOrDefault(key, 0.0);
    }

    public void add(String feature) {
        this.add(feature, 1.0);
    }

    public void add(String feature, Double value) {
        if (features.containsKey(feature))
            throw new RuntimeException("feature " + feature + " already exists");

        this.features.put(feature, value);
    }

    public void mergeWith(SparseFeatureVector other) {
        other.features.forEach((k, v) -> features.merge(k, v, (v1, v2) -> (v1+v2)/2.0));
    }

    public SparseFeatureVector prefix(String prefix) {
        SparseFeatureVector prefixed = new SparseFeatureVector();
        features.keySet().forEach(k -> prefixed.features.put(String.format("%s_%s", prefix, k), features.get(k)));
        return prefixed;
    }

    public SparseFeatureVector cross(SparseFeatureVector other) {
        SparseFeatureVector crossed = new SparseFeatureVector();
        this.features.forEach((k1, v1) -> other.features.forEach((k2,v2) -> crossed.add(String.format("%s_X_%s", k1, k2))));
        return crossed;
    }

    public void add(SparseFeatureVector other) {
        other.features.forEach((k, v) -> features.merge(k, v, (v1, v2) -> v1+v2));
    }

    public SparseFeatureVector mult(double w) {
        SparseFeatureVector result = new SparseFeatureVector();
        features.forEach((k, v) -> result.add(k, v*w));
        return result;
    }

    /**
     * Implements an average based merge of vectors
     * @param vectors The vectors to be averaged
     * @return An average of list of vectors
     */
    public static SparseFeatureVector merge(List<SparseFeatureVector> vectors) {
        SparseFeatureVector result = new SparseFeatureVector();
        vectors.forEach(result::add);
        result.mult(1.0 / vectors.size());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        this.features.forEach((k, v) -> b.append(String.format("%s : %s\n", k, v)));
        return b.toString();
    }
}