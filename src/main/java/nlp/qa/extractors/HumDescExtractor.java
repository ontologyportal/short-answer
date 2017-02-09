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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HumDescExtractor extends AnswerExtractor {

    private final Pattern firstPattern = Pattern.compile("/NNP ,/[^ ]+ ([^,]+),");
    private final Pattern secondPattern = Pattern.compile("([^ ]+/JJ .+?) (.+/NNP)+");
    private final Pattern thirdPattern = Pattern.compile("/NNP (was|were|is|are|[^ ]+ be)/[^ ]+ (.+)");

    private String removePOS(String withPOS) {

        StringBuilder res = new StringBuilder();
        Arrays.stream(withPOS.split(" ")).map(t -> t.split("/")[0]).forEach(w -> res.append(" " + w));

        return res.substring(1);
    }

    @Override
    public List<IndexedWord> extract(SemanticGraph answerGraph) {

        List<IndexedWord> sentenceWords = sentenceWords(answerGraph);

        String results = null;

        StringBuilder b = new StringBuilder();
        sentenceWords.stream().map(w -> String.format("%s/%s", w.word(), w.tag())).forEach(s -> b.append(" " + s));

        Matcher m1 = firstPattern.matcher(b.toString());
        if (m1.find()) {
            results = m1.group(1);
        } else {
            Matcher m2 = secondPattern.matcher(b.toString());
            if (m2.find()) {
                results = m2.group(1);
            } else {
                Matcher m3 = thirdPattern.matcher(b.toString());
                if (m3.find()) {
                    results = m3.group(2);
                }
            }
        }

        if (results == null)
            return null;

        String terms[] = removePOS(results).split(" ");
        List<IndexedWord> words = wordsToIndexedWords(Arrays.asList(terms));
        return words;
    }
}
