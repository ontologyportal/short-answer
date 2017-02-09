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

import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocationExtractor extends AnswerExtractor {

    private final DEPTree answerParsed;

    public LocationExtractor(DEPTree answerParsed) {
        this.answerParsed = answerParsed;
    }

    private List<String> extractSemanticRoles(String rolePattern) {
        return Arrays.stream(answerParsed.toNodeArray()).map(n -> n.getSemanticHeadArcList()).flatMap(x -> x.stream()).filter(a -> {
            String l = a.getLabel().toLowerCase();
            return Pattern.compile(rolePattern).matcher(l).find();
        }).map(a -> a.getNode().getWordForm()).collect(Collectors.toList());
    }

    @Override
    public List<IndexedWord> extract(SemanticGraph answerGraph) {

        List<String> words1 = extractSemanticRoles("loc|gol|dir");

        if (!words1.isEmpty()) {

            return wordsToIndexedWords(words1);
        } else {

            List<IndexedWord> words2 = sentenceWords(answerGraph).stream().filter(n -> n.ner().toLowerCase().contains("loc")).collect(Collectors.toList());

            if (!words2.isEmpty()) {

                return words2;
            } else {

                List<String> words3 = extractSemanticRoles("a2|a4");

                if (!words3.isEmpty()) {

                    return wordsToIndexedWords(words3);
                } else {

                    List<String> words4 = extractSemanticRoles("a0|a1");
                    return wordsToIndexedWords(words4);
                }
            }
        }
    }
}
