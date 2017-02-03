/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.features;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A generic implementation of a sparse vector
 * Basically just a map for holding String features with Double values,
 * and supporting functionality.
 */
public class SparseFeatureVector implements Serializable {

    private final Map<String, Double> features;

    /****************************************************************
     * @return Returns the features as a map
     */
    public Map<String, Double> getFeatures() {
        return features;
    }

    /****************************************************************
     * Creates a sparse vector from a map
     */
    public SparseFeatureVector(Map<String, Double> features) {

        this.features = features;
    }

    /****************************************************************
     * @return Creates an empty feature vector
     */
    public SparseFeatureVector() {

        this.features = new HashMap<>();
    }

    /****************************************************************
     * @return An inner product of this vector with the other vector
     */
    public double dot(SparseFeatureVector other) {

        return this.features.keySet().stream().filter(other.features::containsKey).
                mapToDouble(k -> value(k) * other.value(k)).sum();
    }

    /****************************************************************
     * @return Get the value of a feature based on its name
     */
    public Double value(String key) {

        return features.getOrDefault(key, 0.0);
    }

    /****************************************************************
     * Adds a feature with a default value of 1.0
     */
    public void add(String feature) {

        this.add(feature, 1.0);
    }

    /****************************************************************
     * @return Adds a feature with a name and a value
     */
    public void add(String feature, Double value) {

        if (features.containsKey(feature))
            throw new RuntimeException("feature " + feature + " already exists");

        this.features.put(feature, value);
    }

    /****************************************************************
     * Merge another vector into this one (by averaging the value)
     */
    public void mergeWith(SparseFeatureVector other) {

        other.features.forEach((k, v) -> features.merge(k, v, (v1, v2) -> (v1 + v2) / 2.0));
    }

    /****************************************************************
     * @return Adds a prefix to all the feature names and returns a new vector
     */
    public SparseFeatureVector addPrefix(String prefix) {

        SparseFeatureVector prefixed = new SparseFeatureVector();
        features.keySet().forEach(k -> prefixed.features.put(String.format("%s_%s", prefix, k), features.get(k)));
        return prefixed;
    }

    /****************************************************************
     * @return a cartezian product of this vector with the other to create bigram
     *         features
     */
    public SparseFeatureVector cross(SparseFeatureVector other) {

        SparseFeatureVector crossed = new SparseFeatureVector();
        this.features.forEach((k1, v1) -> other.features.forEach((k2,v2) ->
                crossed.add(String.format("%s_X_%s", k1, k2))));
        return crossed;
    }

    /****************************************************************
     * Adds the other features to this on aby adding values
     */
    public void add(SparseFeatureVector other) {
        other.features.forEach((k, v) -> features.merge(k, v, (v1, v2) -> v1 + v2));
    }

    /****************************************************************
     * @return a new vector which contains weighted values by a scalar @param w
     */
    public SparseFeatureVector mult(double w) {

        SparseFeatureVector result = new SparseFeatureVector();
        features.forEach((k, v) -> result.add(k, v*w));
        return result;
    }

    /****************************************************************
     * @return a vector which was merged from a list of @param vectors (by averaging)
     */
    public static SparseFeatureVector merge(List<SparseFeatureVector> vectors) {

        SparseFeatureVector result = new SparseFeatureVector();
        vectors.forEach(result::add);
        result.mult(1.0 / vectors.size());
        return result;
    }

    /****************************************************************
     * @return a string representation of the vector
     */
    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();
        this.features.forEach((k, v) -> b.append(String.format("%s : %s\n", k, v)));
        return b.toString();
    }
}