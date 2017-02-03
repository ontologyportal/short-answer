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

import java.io.IOException;
import java.nio.file.Path;

/**
 * A featurizer which takes brown clusters and creates nlp features based on them.
 */
public class BrownClustersFeaturizer extends WordFeaturizer {

    private final static String name = "BROWN_CLUSTER";

    private final int prefixSize;

    private final BrownClusters clusters;

    /****************************************************************
     * Creates the brown clusters featurize with an instance of clusters and a specific addPrefix
     */
    public BrownClustersFeaturizer(BrownClusters clusters, int prefixSize) throws IOException {

        this.prefixSize = prefixSize;
        this.clusters = clusters;
    }

    /****************************************************************
     * @return Whether to create bigrams with those features
     */
    @Override
    boolean doBigrams() {

        return false;
    }

    /****************************************************************
     * @return The features vector based on a word
     */
    @Override
    SparseFeatureVector featurize(String word) {

        SparseFeatureVector features = new SparseFeatureVector();
        if (!clusters.map.containsKey(word))
            return features;

        String path = clusters.map.get(word);
        String value = clusters.map.get(word).substring(0, Math.min(prefixSize, path.length()));
        features.add(String.format("%s_%s", name, value));
        return features;
    }

    /****************************************************************
     * @return the clusters loaded from a path
     */
    public static BrownClusters loadClusters(Path path) throws IOException {

        return new BrownClusters(path);
    }
}
