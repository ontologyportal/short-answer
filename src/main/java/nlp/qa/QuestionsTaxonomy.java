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

import java.util.HashMap;
import java.util.Map;

public class QuestionsTaxonomy {

    private Map<String, String> subcatMap = new HashMap<>();

    private final static String ABBREVIATION = "ABBR";
    public final static String DESCRIPTION = "DESC";
    public final static String ENTITY = "ENTY";
    public final static String HUMAN = "HUM";
    public final static String LOCATION = "LOC";
    public final static String NUMERIC = "NUM";

    public final static String _abbreviation = "abb";
    public final static String _expression = "exp";
    public final static String _definition = "def";
    public final static String _description = "desc";
    public final static String _manner = "manner";
    public final static String _reason = "reason";
    public final static String _animal = "animal";
    public final static String _body = "body";
    public final static String _color = "color";
    public final static String _creative = "cremat";
    public final static String _currency = "currency";
    public final static String _dismed = "dismed";
    public final static String _event = "event";
    public final static String _food = "food";
    public final static String _instruction = "instru";
    public final static String _lang = "lang";
    public final static String _letter = "letter";
    public final static String _plant = "plant";
    public final static String _product = "product";
    public final static String _religion = "religion";
    public final static String _sport = "sport";
    public final static String _substance = "substance";
    public final static String _symbol = "symbol";
    public final static String _technique = "techmeth";
    public final static String _term = "termeq";
    public final static String _vehicle = "veh";
    public final static String _word = "word";
    public final static String _group = "gr";
    public final static String _ind = "ind";
    public final static String _title = "title";
    public final static String _city = "city";
    public final static String _country = "country";
    public final static String _mountain = "mount";
    public final static String _other = "other";
    public final static String _state = "state";
    public final static String _code = "code";
    public final static String _count = "count";
    public final static String _date = "date";
    public final static String _distance = "dist";
    public final static String _money = "money";
    public final static String _order = "ord";
    public final static String _percent = "perc";
    public final static String _period = "period";
    public final static String _speed = "speed";
    public final static String _temp = "temp";
    public final static String _size = "volsize";
    public final static String _weight = "weight";

    public static boolean isCategoryOf(String candidate, String category) {
        return candidate.contains(category);
    }

    public static boolean isCategoryOf(String candidate, String category, String subCategory) {
        return candidate.contains(String.format("%s:%s", category, subCategory));
    }
}
