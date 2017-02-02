package nlp.learning;


import edu.stanford.nlp.util.Pair;
import nlp.features.SparseFeatureVector;

import java.util.List;

public interface Scorer {
    List<Pair<String, Double>> score(SparseFeatureVector dataPoint);
}
