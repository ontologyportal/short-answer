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

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.util.arc.SRLArc;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.CoreMap;
import nlp.semantics.SemanticParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AnswerExtractor {

    public abstract List<IndexedWord> extract(SemanticGraph answerGraph);

    List<IndexedWord> sentenceWords(SemanticGraph graph) {

        Collection<IndexedWord> nodes = graph.getRoots();
        List<IndexedWord> decendants = nodes.stream().map(r -> graph.descendants(r)).flatMap(x -> x.stream()).collect(Collectors.toList());
        nodes.addAll(decendants);
        List<IndexedWord> sortedWords = nodes.stream().sorted(Comparator.comparingInt(n -> n.index())).collect(Collectors.toList());
        return sortedWords;
    }

    List<IndexedWord> wordsToIndexedWords(List<String> terms) {

        List<IndexedWord> words = new ArrayList<>();

        for (int i = 0; i < terms.size(); i++) {
            IndexedWord w = new IndexedWord();
            w.setIndex(i + 1);
            w.setWord(terms.get(i));
            words.add(w);
        }

        return words;
    }

    IndexedWord dn2iw(DEPNode n) {
        IndexedWord w = new IndexedWord();
        w.setWord(n.getWordForm());
        w.setIndex(n.getID());
        return w;
    }

    List<SRLArc> getAllSRLArcs(DEPTree tree) {

        List<SRLArc> results = new ArrayList<>();
        for (DEPNode n : tree.toNodeArray()) {
            results.addAll(n.getSemanticHeadArcList());
        }
        return results;
    }
}
