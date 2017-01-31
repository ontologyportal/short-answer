import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class Labels implements Serializable {

    private Map<String, Integer> index = new HashMap<>();
    private Map<Integer, String> reverseIndex = new HashMap<>();

    public int index(String label) {
        Integer old = index.putIfAbsent(label, index.size());
        int x = index.get(label);
        if (old != null)
            reverseIndex.putIfAbsent(x, label);
        return x;
    }

    public IntStream labels() {
        return IntStream.range(0, index.size());
    }

    public String label(int index) {
        return reverseIndex.get(index);
    }

    public int size() {
        return index.size();
    }
}
