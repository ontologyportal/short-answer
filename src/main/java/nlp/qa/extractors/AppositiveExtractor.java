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
import edu.stanford.nlp.trees.GrammaticalRelation;

import java.util.List;
import java.util.stream.Collectors;

public class AppositiveExtractor implements AnswerExtractor {

    /****************************************************************
     * @return All the appositives
     */
    @Override
    public List<IndexedWord> extract(SemanticGraph answerGraph) {

        return answerGraph.typedDependencies().stream().filter(d ->
                d.reln().getShortName().equals("appos")).map(d -> d.dep()).collect(Collectors.toList());
    }
}
