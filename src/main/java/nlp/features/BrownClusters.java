package nlp.features;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the brown clusters loaded from a file
 */
public class BrownClusters {

    Map<String, String> map = new HashMap<>();

    public BrownClusters(Path path) throws IOException {

        Files.lines(path).forEach(line -> {
            String[] vals = line.split("\t");
            map.put(vals[1], vals[0]);
        });
    }
}
