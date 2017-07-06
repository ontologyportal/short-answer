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

import java.util.List;
import java.util.Optional;

/**
 * A number count extractor
 */
public class NumCountExtractor extends AnswerExtractor {

    /****************************************************************
     * @return A list of indexed words representing numeric counts
     *         from the graph
     */
    @Override
    public List<IndexedWord> extract(SemanticGraph answerGraph) {

        List<IndexedWord> num = new RelationExtractor("num").extract(answerGraph);
        if (num != null && !num.isEmpty()) {
            return num;
        }
        else {
            num = new RelationExtractor("nummod").extract(answerGraph);
            if (num != null && !num.isEmpty())
                return num;
        }

        Optional<IndexedWord> cd = sentenceWords(answerGraph).stream().filter(w -> w.tag().toLowerCase().equals("cd")).findAny();

        return cd.map(answerGraph::getChildList).orElse(null);
    }
}
