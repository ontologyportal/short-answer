package nlp.features;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A featurizer which takes brown clusters and creates nlp.features based on them.
 */
public class BrownClustersFeaturizer extends WordFeaturizer {

    private final static String name = "BROWN_CLUSTER";

    private final int prefixSize;
    private final BrownClusters clusters;

    public BrownClustersFeaturizer(BrownClusters clusters, int prefixSize) throws IOException {
        this.prefixSize = prefixSize;
        this.clusters = clusters;
    }

    @Override
    boolean doBigrams() {
        return false;
    }

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

    public static BrownClusters loadClusters(Path path) throws IOException {
        return new BrownClusters(path);
    }
}
