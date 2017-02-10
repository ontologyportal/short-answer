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

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A generic graph relation extractor
 */
public class RelationExtractor extends AnswerExtractor {

    // the name of the relation to extract
    private final String rel;

    public RelationExtractor(String rel) {

        this.rel = rel;
    }

    /****************************************************************
     * @return List of indexed words which are the dependents in the relation
     */
    @Override
    public List<IndexedWord> extract(SemanticGraph answerGraph) {

        List<IndexedWord> relations = answerGraph.typedDependencies().stream().filter(d ->
                d.reln().getShortName().equals(rel)).map(TypedDependency::dep).collect(Collectors.toList());

        if (!relations.isEmpty()) {
            return relations;
        }

        return null;
    }
}
