import java.util.HashSet;
import java.util.Set;

public class RabinKarp {

    // Prime number multiplier for rolling hash
    private static final int BASE = 31;
    // Large prime modulus to minimize hash collisions
    private static final long MODULUS = 1000000007L;

    // Set storing unique message hashes already seen in the stream
    private final Set<Long> seenHashes;

    public RabinKarp() {
        this.seenHashes = new HashSet<>();
    }

    /**
     * Computes the polynomial rolling hash of a string in O(N) time.
     * Hash formula: H = (c_0 * B^(n-1) + c_1 * B^(n-2) + ... + c_(n-1)) % MOD
     */
    public long computeHash(String text) {
        long hashValue = 0;
        String normalized = text.trim().toLowerCase();

        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            hashValue = (hashValue * BASE + ch) % MODULUS;
        }
        return hashValue;
    }

    /**
     * Checks if a message is a duplicate.
     * Returns true if it's a new unique message, false if it's a duplicate.
     */
    public boolean processMessage(String messageId, String text) {
        long hash = computeHash(text);

        if (seenHashes.contains(hash)) {
            System.out.printf("  [DUPLICATE DETECTED] Msg ID: %s | Hash: %d -> Dropped!\n", messageId, hash);
            return false; // Duplicate found, drop it
        }

        seenHashes.add(hash);
        System.out.printf("  [UNIQUE MESSAGE] Msg ID: %s | Hash: %d -> Passed to Pipeline\n", messageId, hash);
        return true; // New message, keep it
    }
}