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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * An extractor which extracts a capitalized sequence of words
 */
public class FirstCapSeqExtractor extends AnswerExtractor {

    /****************************************************************
     * @return Wether the word is properly capitalized, like "Hello" vs. "hello" or "hEllo"
     */
    private boolean isCapitalized(String word) {

        return Character.isUpperCase(word.charAt(0)) &&
                IntStream.range(1, word.length()).map(word::charAt).allMatch(Character::isLowerCase);
    }

    /****************************************************************
     * @return A list of indexed words which represents the capitalized sequence,
     *         if exists, and null otherwise
     */
    @Override
    public List<IndexedWord> extract(SemanticGraph answerGraph) {

        List<IndexedWord> results = new ArrayList<>();

        List<IndexedWord> sortedWords = sentenceWords(answerGraph);

        for (IndexedWord word : sortedWords) {
            if (isCapitalized(word.word()) && (results.isEmpty() ||
                    word.index() - 1 == results.get(results.size() - 1).index())) {
                results.add(word);
            }
            else if (!results.isEmpty()) return results;
        }

        return !results.isEmpty() ? results : null;
    }
}
