import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InfrastructureRecoveryEngine {

    public static class RecoveryEdge implements Comparable<RecoveryEdge> {
        public String source;
        public String destination;
        public double repairCost; // Distance weighted by clearance difficulty

        public RecoveryEdge(String source, String destination, double repairCost) {
            this.source = source;
            this.destination = destination;
            this.repairCost = repairCost;
        }

        @Override
        public int compareTo(RecoveryEdge other) {
            return Double.compare(this.repairCost, other.repairCost);
        }

        @Override
        public String toString() {
            return String.format("%s <===> %s (Clearance Cost: %.2f)", source, destination, repairCost);
        }
    }

    public static class MstResult {
        public List<RecoveryEdge> recoveryPlan = new ArrayList<>();
        public double totalClearanceCost = 0.0;
        public int connectedComponentsCount;
        public RoadEdge[] mstEdges = new RoadEdge[0];

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("CRITICAL ROAD CLEARANCE MANIFEST (%d Edges | Total Cost: %.2f):\n", 
                    recoveryPlan.size(), totalClearanceCost));
            for (RecoveryEdge edge : recoveryPlan) {
                sb.append("  [REPAIR TARGET] ").append(edge).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Executes Kruskal's Minimum Spanning Tree algorithm using Disjoint Set Union (DSU).
     * Time Complexity: O(E log E) due to edge sorting.
     */
    public static MstResult computeRecoveryPlan(DisasterGraph graph) {
        MstResult result = new MstResult();
        List<RecoveryEdge> allEdges = new ArrayList<>();
        DisjointSetUnion dsu = new DisjointSetUnion();

        // 1. Initialize DSU sets and extract unique graph edges
        for (String zone : graph.getAllZones()) {
            dsu.makeSet(zone);
            for (RoadEdge edge : graph.getNeighbors(zone)) {
                if (zone.compareTo(edge.destinationZone) < 0) { 
                    // Calculate raw traversal/clearance cost
                    double cost = edge.getEffectiveCost();
         
                    // Fix: If distance is very small (OSM micro-segments), scale to meters or enforce a minimum cost floor
                    if (cost <= 0.001) {
                        // Convert km to meters or apply a base clearance effort score
                        cost = Math.max(edge.distanceKm * 1000.0, 1.25); 
                    }
                    allEdges.add(new RecoveryEdge(zone, edge.destinationZone, cost));
                }
            }
        }
        // 2. Sort all damaged road edges in ascending order of repair cost
        Collections.sort(allEdges);

        // Track selected RoadEdge objects for compatibility with App.java
        List<RoadEdge> selectedRoadEdges = new ArrayList<>();

        // 3. Kruskal's greedy edge selection using DSU cycle detection
        for (RecoveryEdge edge : allEdges) {
            if (dsu.union(edge.source, edge.destination)) {
                result.recoveryPlan.add(edge);
                result.totalClearanceCost += edge.repairCost;

                // Populate corresponding RoadEdge object for external MST loops
                selectedRoadEdges.add(new RoadEdge(edge.source, edge.destination, edge.repairCost, 1.0));
            }
        }

        // 4. Populate mstEdges array for direct access in main pipeline
        result.mstEdges = selectedRoadEdges.toArray(new RoadEdge[0]);

        return result;
    }
}
