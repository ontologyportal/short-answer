/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.qa;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.Pair;

import java.util.List;

/**
 * Represents the foci question terms (head words and other information)
 */
public class QuestionFociTerms {

    // the type of the question
    public String questionType;

    // head words with types
    public List<Pair<IndexedWord, String>> termsWithTypes;

    // the actual question word
    public String questionWord;

    public QuestionFociTerms(List<Pair<IndexedWord, String>> termsWithTypes, String questionWord, String questionType) {

        this.termsWithTypes = termsWithTypes;
        this.questionWord = questionWord;
        this.questionType = questionType;
    }
}
