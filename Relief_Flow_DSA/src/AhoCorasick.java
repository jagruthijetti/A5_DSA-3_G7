import java.util.*;

public class AhoCorasick {

    // Node structure representing Trie states
    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        Node fail = null; // Failure link used when a character match fails
        List<String> output = new ArrayList<>(); // Matched keywords ending at this state
    }

    private final Node root;

    public AhoCorasick() {
        this.root = new Node();
    }

    /**
     * Step 1: Insert keywords into the Trie.
     */
    public void insert(String keyword) {
        Node current = root;
        for (char ch : keyword.toLowerCase().toCharArray()) {
            current.children.putIfAbsent(ch, new Node());
            current = current.children.get(ch);
        }
        current.output.add(keyword);
    }

    /**
     * Step 2: Build BFS Failure Links across the Trie states.
     */
    public void buildFailureLinks() {
        Queue<Node> queue = new LinkedList<>();

        // Root's immediate children have failure links pointing to Root
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        // BFS traversal to calculate failure links for deeper nodes
        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char ch = entry.getKey();
                Node child = entry.getValue();

                Node fallback = current.fail;
                while (fallback != null && !fallback.children.containsKey(ch)) {
                    fallback = fallback.fail;
                }

                child.fail = (fallback != null) ? fallback.children.get(ch) : root;
                child.output.addAll(child.fail.output); // Inherit matching keywords
                queue.add(child);
            }
        }
    }

    /**
     * Step 3: Scan incoming disaster stream text to detect matched keywords.
     */
    public Map<String, List<Integer>> search(String text) {
        Map<String, List<Integer>> matches = new HashMap<>();
        Node current = root;
        String lowerText = text.toLowerCase();

        for (int i = 0; i < lowerText.length(); i++) {
            char ch = lowerText.charAt(i);

            while (current != null && !current.children.containsKey(ch)) {
                current = current.fail;
            }

            if (current == null) {
                current = root;
                continue;
            }

            current = current.children.get(ch);

            // Record all keyword matches ending at this index
            for (String keyword : current.output) {
                int startIndex = i - keyword.length() + 1;
                matches.putIfAbsent(keyword, new ArrayList<>());
                matches.get(keyword).add(startIndex);
            }
        }
        return matches;
    }
}

