/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.learning;


import edu.stanford.nlp.util.Pair;
import nlp.features.SparseFeatureVector;

import java.util.List;

/**
 * Returns a list of classes and scores for a datapoint,
 * in some sense representing a ranker/classifier
 */
public interface Scorer {

    List<Pair<String, Double>> score(SparseFeatureVector dataPoint);
}
