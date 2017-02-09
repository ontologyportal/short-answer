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
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FirstCapSeqExtractor extends AnswerExtractor {

    private boolean isCapitalized(String word) {
        return Character.isUpperCase(word.charAt(0)) && IntStream.range(1, word.length()).map(word::charAt).allMatch(Character::isLowerCase);
    }

    @Override
    public List<IndexedWord> extract(SemanticGraph answerGraph) {

        List<IndexedWord> results = new ArrayList<>();

        List<IndexedWord> sortedWords = sentenceWords(answerGraph);

        for (IndexedWord word: sortedWords) {
            if (isCapitalized(word.word()) && (results.isEmpty() ||
                    word.index() - 1 == results.get(results.size() - 1).index())) {
                results.add(word);
            } else if (!results.isEmpty()) return results;
        }

        return !results.isEmpty()? results : null;
    }
}
