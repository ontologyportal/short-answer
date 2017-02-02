package nlp.features;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A featurizer that takes authority lists(gazetteers) and create nlp.features out of them.
 */
public class ListsFeaturizer extends WordFeaturizer {

    private final static String LIST = "LIST";
    private Map<String, String> index = new HashMap<>();

    public ListsFeaturizer(Path path) throws IOException {

        Files.list(path).forEach(file -> {
            try {
                Files.lines(file).forEach(line -> {
                    index.put(line, file.getFileName().toString());
                });
            }
            catch (IOException e) {
                throw new RuntimeException("Loading failed.");
            }
        });
    }

    @Override
    boolean doBigrams() {
        return false;
    }

    @Override
    SparseFeatureVector featurize(String word) {

        SparseFeatureVector result = new SparseFeatureVector();

        if (index.containsKey(word)) {
            result.add(String.format("%s_%s", LIST, index.get(word)));
        }
        return result;
    }
}
