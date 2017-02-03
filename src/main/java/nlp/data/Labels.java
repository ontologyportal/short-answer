/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * A simple class which represents bi-directional mapping between labels and their indices
 */
public class Labels implements Serializable {

    private Map<String, Integer> index = new HashMap<>();

    private Map<Integer, String> reverseIndex = new HashMap<>();

    /****************************************************************
     * @return The index of a given label
     */
    public int index(String label) {

        Integer old = index.putIfAbsent(label, index.size());
        int x = index.get(label);
        if (old != null)
            reverseIndex.putIfAbsent(x, label);
        return x;
    }

    /****************************************************************
     * @return All the indices of the labels as a stream
     */
    public IntStream labels() {

        return IntStream.range(0, index.size());
    }

    /****************************************************************
     * @return The label given the index
     */
    public String label(int index) {

        return reverseIndex.get(index);
    }

    /****************************************************************
     * @return The number of labels in the mapping
     */
    public int size() {

        return index.size();
    }
}
