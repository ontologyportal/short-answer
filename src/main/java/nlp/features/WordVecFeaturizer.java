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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * A word vectors featurizer
 */
public class WordVecFeaturizer extends WordFeaturizer {

    private final static String baseName = "WORDVEC";

    private Map<String, Double[]> vectors = new HashMap<>();

    private int size = -1;

    /****************************************************************
     * @return an instance of a word vec featurizer
     */
    public WordVecFeaturizer(Path path) throws IOException {

        Files.lines(path).forEach(line -> {
            String[] vals = line.split(" ");
            // creating a new instance to generate a small footprint in RAM
            String key = new String(vals[0]);
            Double[] vector = new Double[vals.length - 1];
            IntStream.range(1, vals.length).forEach(i -> vector[i-1] = Double.parseDouble(vals[i]));
            vectors.put(key, vector);
            if (size != -1 && size != vector.length)
                throw new RuntimeException("Not consistent wordvec size");
            size = vector.length;
        });
    }

    /****************************************************************
     * @return a vector of a word given by the key @param key
     */
    public Double[] get(String word) {

        return vectors.get(word);
    }

    /****************************************************************
     * @return a feature name based on the index
     */
    private String name(int i) {

        return String.format("%s_%d", baseName, i);
    }

    /****************************************************************
     * @return Whether to create bigram features
     */
    @Override
    boolean doBigrams() {

        return false;
    }

    /****************************************************************
     * @return A feature vector representing the word
     */
    @Override
    SparseFeatureVector featurize(String word) {

        word = word.toLowerCase();

        SparseFeatureVector features = new SparseFeatureVector();

        if (!vectors.containsKey(word))
            return features;

        Double[] vector = get(word);

        IntStream.range(0, size).forEach(i -> features.add(name(i), vector[i]));
        return features;
    }
}
