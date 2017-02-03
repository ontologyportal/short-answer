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

/**
 * This class represents the brown clusters loaded from a file
 */
public class BrownClusters {

    Map<String, String> map = new HashMap<>();

    /****************************************************************
     * Loads brown clusters into a map
     */
    public BrownClusters(Path path) throws IOException {

        Files.lines(path).forEach(line -> {
            String[] vals = line.split("\t");
            map.put(vals[1], vals[0]);
        });
    }
}
