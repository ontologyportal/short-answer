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

import edu.stanford.nlp.util.Pair;
import nlp.features.SparseFeatureVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Class which represents a simple dataset of data points with their features
 */
public class DataSet {

    public Labels labels;

    public List<Pair<String, String>> rawDataPoints = new ArrayList<>();

    public List<Pair<Integer, SparseFeatureVector>> dataPoints = new ArrayList<>();

    /****************************************************************
     * @return Returns a pair of datasets as a result of splitting the
     *         original dataset into two, with the amounts of datapoints
     *         in each defined by the @part parameter.
     */
    public Pair<DataSet, DataSet> split(double part) {

        // important to shuffle the datapoints, otherwise splitting
        // might result in a not representative sample of the whole dataset
        Collections.shuffle(rawDataPoints, new Random(0));
        Collections.shuffle(dataPoints, new Random(0));

        DataSet part1 = new DataSet();
        DataSet part2 = new DataSet();

        part1.labels = labels;
        part2.labels = labels;

        int lastIndex = (int)(rawDataPoints.size()*part);
        for (int i = 0; i < rawDataPoints.size(); i++) {
            DataSet temp = i < lastIndex ? part1 : part2;
            temp.rawDataPoints.add(rawDataPoints.get(i));
            temp.dataPoints.add(dataPoints.get(i));
        }

        return new Pair<>(part1, part2);
    }
}
