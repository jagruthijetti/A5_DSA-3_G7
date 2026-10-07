import java.util.*;

public class MultiDimKnapsack {

    public static class ReliefItem {
        public String id;
        public String name;
        public double weight;
        public double volume;
        public int value; // Represents Utility Score

        public ReliefItem(String id, String name, double weight, double volume, int value) {
            this.id = id;
            this.name = name;
            this.weight = weight;
            this.volume = volume;
            this.value = value;
        }

        // Convenience constructor for integer inputs
        public ReliefItem(String id, String name, int weight, int volume, int value) {
            this(id, name, (double) weight, (double) volume, value);
        }
    }

    /**
     * Solves the 0/1 Multi-Dimensional Knapsack Problem using 3D Dynamic Programming.
     * Constraints: Maximum Weight Capacity and Maximum Volume Capacity.
     */
    public static List<ReliefItem> optimizeCargo(List<ReliefItem> items, int maxWeight, int maxVolume) {
        int n = items.size();
        int[][][] dp = new int[n + 1][maxWeight + 1][maxVolume + 1];

        // Build DP table
        for (int i = 1; i <= n; i++) {
            ReliefItem item = items.get(i - 1);
            int itemWeight = (int) Math.round(item.weight);
            int itemVolume = (int) Math.round(item.volume);
            int itemValue = item.value;

            for (int w = 0; w <= maxWeight; w++) {
                for (int v = 0; v <= maxVolume; v++) {
                    if (itemWeight <= w && itemVolume <= v) {
                        dp[i][w][v] = Math.max(
                            dp[i - 1][w][v],
                            dp[i - 1][w - itemWeight][v - itemVolume] + itemValue
                        );
                    } else {
                        dp[i][w][v] = dp[i - 1][w][v];
                    }
                }
            }
        }

        // Backtrack to find packed items
        List<ReliefItem> packedItems = new ArrayList<>();
        int w = maxWeight;
        int v = maxVolume;

        for (int i = n; i > 0; i--) {
            if (dp[i][w][v] != dp[i - 1][w][v]) {
                ReliefItem item = items.get(i - 1);
                packedItems.add(item);
                w -= (int) Math.round(item.weight);
                v -= (int) Math.round(item.volume);
            }
        }

        Collections.reverse(packedItems);
        return packedItems;
    }
}