package nlp.features;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class WordVecFeaturizer extends WordFeaturizer {

    private final static String baseName = "WORDVEC";

    private Map<String, Double[]> vectors = new HashMap<>();
    private int size = -1;

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

    public Double[] get(String key) {
        return vectors.get(key);
    }

    private String name(int i) {
        return String.format("%s_%d", baseName, i);
    }

    @Override
    boolean doBigrams() {
        return false;
    }

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
