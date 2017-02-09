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
import java.util.Optional;
import java.util.stream.Collectors;

public class GenericNumExtractor extends AnswerExtractor {

    @Override
    public List<IndexedWord> extract(SemanticGraph answerGraph) {

        List<IndexedWord> sentenceWords = sentenceWords(answerGraph);
        List<IndexedWord> words = sentenceWords.stream().filter(n -> n.tag().toLowerCase().contains("cd")).collect(Collectors.toList());
        if (!words.isEmpty()) {
            return words;
        }
        List<TypedDependency> num = answerGraph.typedDependencies().stream().filter(d -> d.reln().getShortName().toLowerCase().
                contains("num")).collect(Collectors.toList());
        if (!num.isEmpty()) {
            return num.stream().map(t -> t.dep()).collect(Collectors.toList());
        }

        return null;
    }
}
