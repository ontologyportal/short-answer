/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.qa.extractors;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.util.arc.SRLArc;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.Pair;
import nlp.features.SparseFeatureVector;
import nlp.features.WordVecFeaturizer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NumDateExtractor extends AnswerExtractor {

    private final DEPTree depTree;
    private final WordVecFeaturizer featurizer;
    private final DEPNode verbNode;
    private final SparseFeatureVector verbNodeWV;

    public NumDateExtractor(DEPTree depTree, WordVecFeaturizer featurizer, DEPNode verbNode) {
        this.depTree = depTree;
        this.featurizer = featurizer;
        this.verbNode = verbNode;
        this.verbNodeWV = featurizer.featurize(verbNode.getWordForm());
    }

    @Override
    public List<IndexedWord> extract(SemanticGraph answerGraph) {

        Optional<SRLArc> tmp = getAllSRLArcs(depTree).stream().filter(a -> a.getLabel().toLowerCase().contains("tmp")).findFirst();
        if (tmp.isPresent()) {

            return tmp.get().getNode().getDependentList().stream().filter(d -> d.getNamedEntityTag().toLowerCase().
                    contains("date")).map(this::dn2iw).sorted(Comparator.comparingInt(IndexedWord::index)).collect(Collectors.toList());
        } else {

            List<IndexedWord> dates = Arrays.stream(depTree.toNodeArray()).filter(n -> n.getNamedEntityTag().toLowerCase().contains("date")).map(this::dn2iw).
                    sorted(Comparator.comparingInt(IndexedWord::index)).collect(Collectors.toList());

            if (!dates.isEmpty()) {
                return dates;
            } else {

                Optional<Pair<DEPNode, Double>> first = Arrays.stream(depTree.toNodeArray()).map(n -> new Pair<>(n, featurizer.featurize(n.getWordForm()).
                        similarity(verbNodeWV))).sorted(Comparator.comparingDouble(s -> s.second)).findFirst();

                if (first.isPresent()) {

                    Optional<SRLArc> tmp1 = first.get().first.getSemanticHeadArcList().stream().filter(a -> a.getLabel().toLowerCase().contains("tmp")).findFirst();
                    if (tmp1.isPresent()) {
                        List<IndexedWord> dates2 = tmp1.get().getNode().getDependentList().stream().map(this::dn2iw).collect(Collectors.toList());
                        if (!dates2.isEmpty()) {
                            return dates2;
                        }
                    }

                    List<IndexedWord> cd = first.get().first.getDependentList().stream().filter(n ->
                            n.getPOSTag().toLowerCase().contains("cd")).map(this::dn2iw).collect(Collectors.toList());
                    if (!cd.isEmpty()) {
                        return cd;
                    }
                }
            }

        }
        return null;
    }
}
