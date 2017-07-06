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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simplistic yes/no extractor based on negativity words
 */
public class YesNoExtractor extends AnswerExtractor {

    private final IndexedWord no;
    private final IndexedWord yes;
    Set<String> negationWords = new HashSet<String>();

    public YesNoExtractor() {

        negationWords.add("no");
        negationWords.add("not");
        negationWords.add("n't");

        no = new IndexedWord();
        yes = new IndexedWord();

        no.setIndex(0);
        yes.setIndex(0);

        no.setWord("No");
        yes.setWord("Yes");
    }

    @Override
    public List<IndexedWord> extract(SemanticGraph answerGraph) {

        boolean negation = sentenceWords(answerGraph).stream().anyMatch(w -> negationWords.contains(w.word().toLowerCase()));
        return Collections.singletonList(negation ? no : yes);
    }
}
