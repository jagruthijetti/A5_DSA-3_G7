import java.util.*;

public class MinCostMaxFlow {

    public static class FlowEdge {
        public int to;
        public int rev;
        public int capacity;
        public int flow;
        public double cost; // Cost per unit of supply flow

        public FlowEdge(int to, int rev, int capacity, double cost) {
            this.to = to;
            this.rev = rev;
            this.capacity = capacity;
            this.flow = 0;
            this.cost = cost;
        }
    }

    public static class NetworkFlowResult {
        public int maxFlow;
        public double minCost;

        public NetworkFlowResult(int maxFlow, double minCost) {
            this.maxFlow = maxFlow;
            this.minCost = minCost;
        }

        @Override
        public String toString() {
            return String.format("Max Supply Flow: %d units | Total Transit Cost: %.2f", maxFlow, minCost);
        }
    }

    private final int N;
    private final List<List<FlowEdge>> graph;
    private final Map<String, Integer> nodeToIndex = new HashMap<>();
    private final Map<Integer, String> indexToNode = new HashMap<>();
    private int nodeCounter = 0;

    public MinCostMaxFlow(int numNodes) {
        this.N = numNodes;
        this.graph = new ArrayList<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            graph.add(new ArrayList<>());
        }
    }

    public int getOrAddNode(String label) {
        if (!nodeToIndex.containsKey(label)) {
            nodeToIndex.put(label, nodeCounter);
            indexToNode.put(nodeCounter, label);
            nodeCounter++;
        }
        return nodeToIndex.get(label);
    }

    public void addEdge(String fromNode, String toNode, int capacity, double cost) {
        int u = getOrAddNode(fromNode);
        int v = getOrAddNode(toNode);

        FlowEdge a = new FlowEdge(v, graph.get(v).size(), capacity, cost);
        FlowEdge b = new FlowEdge(u, graph.get(u).size(), 0, -cost); // Residual edge with negative cost
        graph.get(u).add(a);
        graph.get(v).add(b);
    }

    /**
     * Successive Shortest Path algorithm using Shortest Path Faster Algorithm (SPFA).
     */
    public NetworkFlowResult computeMinCostMaxFlow(String sourceLabel, String sinkLabel) {
        int s = getOrAddNode(sourceLabel);
        int t = getOrAddNode(sinkLabel);

        int maxFlow = 0;
        double minCost = 0.0;

        int[] parentEdgeIndex = new int[N];
        int[] parentNode = new int[N];
        double[] dist = new double[N];
        boolean[] inQueue = new boolean[N];

        while (true) {
            Arrays.fill(dist, Double.POSITIVE_INFINITY);
            Arrays.fill(inQueue, false);

            Queue<Integer> queue = new LinkedList<>();
            dist[s] = 0.0;
            queue.add(s);
            inQueue[s] = true;

            // SPFA to find shortest path in terms of cost considering residual capacities
            while (!queue.isEmpty()) {
                int u = queue.poll();
                inQueue[u] = false;

                for (int i = 0; i < graph.get(u).size(); i++) {
                    FlowEdge edge = graph.get(u).get(i);
                    if (edge.capacity - edge.flow > 0 && dist[edge.to] > dist[u] + edge.cost) {
                        dist[edge.to] = dist[u] + edge.cost;
                        parentNode[edge.to] = u;
                        parentEdgeIndex[edge.to] = i;

                        if (!inQueue[edge.to]) {
                            queue.add(edge.to);
                            inQueue[edge.to] = true;
                        }
                    }
                }
            }

            // If sink is unreachable in residual graph, max flow reached
            if (dist[t] == Double.POSITIVE_INFINITY) break;

            // Find bottleneck capacity along augmenting path
            int pushFlow = Integer.MAX_VALUE;
            for (int v = t; v != s; v = parentNode[v]) {
                int u = parentNode[v];
                int edgeIdx = parentEdgeIndex[v];
                pushFlow = Math.min(pushFlow, graph.get(u).get(edgeIdx).capacity - graph.get(u).get(edgeIdx).flow);
            }

            // Push flow along path and update residual graph
            for (int v = t; v != s; v = parentNode[v]) {
                int u = parentNode[v];
                int edgeIdx = parentEdgeIndex[v];
                FlowEdge edge = graph.get(u).get(edgeIdx);

                edge.flow += pushFlow;
                graph.get(v).get(edge.rev).flow -= pushFlow;
                minCost += pushFlow * edge.cost;
            }

            maxFlow += pushFlow;
        }

        return new NetworkFlowResult(maxFlow, minCost);
    }
}