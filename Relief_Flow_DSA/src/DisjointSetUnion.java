import java.util.HashMap;
import java.util.Map;

public class DisjointSetUnion {

    private final Map<String, String> parent = new HashMap<>();
    private final Map<String, Integer> rank = new HashMap<>();

    /**
     * Initializes a new set for a given zone/node ID.
     */
    public void makeSet(String zoneId) {
        parent.putIfAbsent(zoneId, zoneId);
        rank.putIfAbsent(zoneId, 0);
    }

    /**
     * Finds the representative root of a set with O(α(N)) Path Compression.
     */
    public String find(String zoneId) {
        if (!parent.containsKey(zoneId)) {
            makeSet(zoneId);
            return zoneId;
        }

        if (!parent.get(zoneId).equals(zoneId)) {
            // Path Compression: Point directly to root representative
            parent.put(zoneId, find(parent.get(zoneId)));
        }
        return parent.get(zoneId);
    }

    /**
     * Merges two disjoint sets using Union by Rank.
     * @return true if merged successfully; false if already connected (cycle detected).
     */
    public boolean union(String zoneA, String zoneB) {
        String rootA = find(zoneA);
        String rootB = find(zoneB);

        if (rootA.equals(rootB)) {
            return false; // Cycle detected: both nodes are already in the same component
        }

        // Union by Rank optimization
        int rankA = rank.get(rootA);
        int rankB = rank.get(rootB);

        if (rankA < rankB) {
            parent.put(rootA, rootB);
        } else if (rankA > rankB) {
            parent.put(rootB, rootA);
        } else {
            parent.put(rootB, rootA);
            rank.put(rootA, rankA + 1);
        }

        return true;
    }
}