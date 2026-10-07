import java.util.*;

public class App {

    // =========================================================================================================
    // AHO-CORASICK AUTOMATON (TRIE + FAILURE LINKS) FOR O(N) MULTI-PATTERN KEYWORD MATCHING
    // =========================================================================================================
    public static class AhoCorasick {

        public static class Node {
            Map<Character, Node> children = new HashMap<>();
            Node fail = null;
            List<String> output = new ArrayList<>();
        }

        private final Node root;

        public AhoCorasick(List<String> keywords) {
            root = new Node();
            buildTrie(keywords);
            buildFailureLinks();
        }

        private void buildTrie(List<String> keywords) {
            for (String keyword : keywords) {
                Node current = root;
                for (char ch : keyword.toLowerCase().toCharArray()) {
                    current.children.putIfAbsent(ch, new Node());
                    current = current.children.get(ch);
                }
                current.output.add(keyword);
            }
        }

        private void buildFailureLinks() {
            Queue<Node> queue = new LinkedList<>();

            // Depth 1 nodes fail back to root
            for (Node child : root.children.values()) {
                child.fail = root;
                queue.add(child);
            }

            // BFS construction of suffix failure pointers
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
                    child.output.addAll(child.fail.output);
                    queue.add(child);
                }
            }
        }

        public List<String> search(String text) {
            Set<String> matches = new LinkedHashSet<>();
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
                matches.addAll(current.output);
            }

            return new ArrayList<>(matches);
        }
    }

    public static class DisasterIncident implements Comparable<DisasterIncident> {
        public String id;
        public String rawText;
        public double urgencyScore;
        public int requiredSupplyWeight;
        public List<String> keywords;

        public DisasterIncident(String id, String rawText, double urgencyScore, int requiredSupplyWeight, List<String> keywords) {
            this.id = id;
            this.rawText = rawText;
            this.urgencyScore = urgencyScore;
            this.requiredSupplyWeight = requiredSupplyWeight;
            this.keywords = keywords;
        }

        @Override
        public int compareTo(DisasterIncident other) {
            return Double.compare(this.urgencyScore, other.urgencyScore);
        }
    }

    /**
     * Custom Rabin-Karp Rolling Hash Generator
     * Uses prime base = 31 and modulus = 1,000,000,007
     */
    private static long computeRabinKarpHash(String text) {
        long hash = 0;
        long primeBase = 31;
        long modulus = 1000000007L;

        for (int i = 0; i < text.length(); i++) {
            hash = (hash * primeBase + text.charAt(i)) % modulus;
        }
        return hash;
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================================================");
        System.out.println("                   DISASTER RELIEF DSA PIPELINE - COMPREHENSIVE INTEGRATION RUN");
        System.out.println("==========================================================================================================\n");

        // Initialize Aho-Corasick Dictionary with target keywords
        List<String> disasterKeywords = Arrays.asList(
            "water", "food", "hungry", "trapped", "collapse", "medical", "bleeding", "urgent", "emergency", "bridge", "road", "blocked", "supplies",
            "assistance", "rescue", "evacuation", "shelter", "aid", "injury", "casualty", "damage", "flood", "earthquake", "hurricane", "tornado", 
            "fire", "explosion", "landslide", "tsunami", "volcano", "storm", "disaster", "crisis", "relief", "evacuate", "rescue", "safety", 
            "emergency response", "first aid", "medical supplies", "water supply", "food distribution", "shelter provision", "power outage", "road closure",
            "bridge collapse", "building collapse", "flooding", "earthquake damage", "hurricane impact", "tornado damage", "fire outbreak",
            "explosion site", "landslide area", "tsunami warning", "volcanic eruption", "storm surge"
        );
        AhoCorasick ahoCorasickMatcher = new AhoCorasick(disasterKeywords);

        // -----------------------------------------------------------------------------------------------------
        // PHASE 1: RABIN-KARP TELEMETRY DEDUPLICATION & AHO-CORASICK KEYWORD EXTRACTION
        // -----------------------------------------------------------------------------------------------------
        System.out.println("==========================================================================================================");
        System.out.println("--- PHASE 1: RABIN-KARP TELEMETRY DEDUPLICATION & AHO-CORASICK KEYWORD EXTRACTION ---");
        System.out.println("==========================================================================================================");

        // Format: {Msg ID, Raw Message Text, ML Urgency Score, Required Supply Weight (kg)}
        String[][] rawMessages = {
            {"MSG_1001", "Water supply depleted, hungry families need food.", "0.4320", "15"},
            {"MSG_1002", "Emergency! 3 people trapped under building collapse!", "0.9152", "40"},
            {"MSG_1001", "Water supply depleted, hungry families need food.", "0.4320", "15"}, // Exact Duplicate
            {"MSG_1003", "Urgent medical assistance required, deep bleeding reported near Central Park.", "0.8810", "20"},
            {"MSG_1004", "Infant needs immediate food and clean drinking water.", "0.7500", "10"},
            {"MSG_1005", "Building collapse in Sector 3, people trapped inside!", "0.9540", "50"},
            {"MSG_1006", "Bridge destroyed, road blocked but no casualties reported.", "0.3100", "30"},
            {"MSG_1007", "Urgent request for trauma medical supplies and water.", "0.8200", "25"},
            {"MSG_1008", "Emergency! 3 people trapped under building collapse!", "0.9152", "40"}  // Duplicate Text, Different ID
        };

        Set<Long> seenHashes = new HashSet<>();
        List<DisasterIncident> triagedIncidents = new ArrayList<>();

        int totalIngested = rawMessages.length;
        int duplicatesDropped = 0;
        int uniquePassed = 0;

        System.out.printf("%-10s | %-12s | %-12s | %-20s | %-25s | %s\n", 
                "MSG ID", "RK HASH CODE", "DUPLICATE?", "PIPELINE STATUS", "AHO-CORASICK MATCHES", "MESSAGE CONTENT");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------");

        for (String[] raw : rawMessages) {
            String msgId = raw[0];
            String text = raw[1];
            double mlScore = Double.parseDouble(raw[2]);
            int weight = Integer.parseInt(raw[3]);

            // 1. Compute Rabin-Karp Hash for Deduplication
            long rkHash = computeRabinKarpHash(text);
            boolean isDuplicate = seenHashes.contains(rkHash);
            String dupStatus = isDuplicate ? "YES (MATCH)" : "NO (UNIQUE)";
            String pipelineStatus;

            // 2. Perform Multi-Pattern Keyword Extraction using Aho-Corasick Automaton
            List<String> matchedKeywords = ahoCorasickMatcher.search(text);

            if (isDuplicate) {
                duplicatesDropped++;
                pipelineStatus = "DROPPED [FILTERED]";
            } else {
                seenHashes.add(rkHash);
                uniquePassed++;
                pipelineStatus = "PASSED TO HEAP";
                triagedIncidents.add(new DisasterIncident(msgId, text, mlScore, weight, matchedKeywords));
            }

            System.out.printf("%-10s | %-12d | %-12s | %-20s | %-25s | \"%s\"\n", 
                    msgId, rkHash, dupStatus, pipelineStatus, matchedKeywords.toString(), text);
        }

        System.out.println("\n------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("--- RABIN-KARP & AHO-CORASICK SUMMARY ---");
        System.out.printf("  • Total Ingested Telemetry Packets : %d\n", totalIngested);
        System.out.printf("  • Duplicate Messages Filtered Out  : %d (Saved processing bandwidth)\n", duplicatesDropped);
        System.out.printf("  • Unique Valid Incidents Forwarded : %d\n", uniquePassed);
        System.out.printf("  • Aho-Corasick Dictionary Loaded   : %d target keywords\n", disasterKeywords.size());
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------\n");

        // -----------------------------------------------------------------------------------------------------
        // PHASE 2: MAX-HEAP PRIORITY TRIAGE
        // -----------------------------------------------------------------------------------------------------
        System.out.println("--- PHASE 2: MAX-HEAP PRIORITY TRIAGE (TOP INCIDENTS) ---");
        PriorityQueue<DisasterIncident> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        maxHeap.addAll(triagedIncidents);

        List<DisasterIncident> prioritizedList = new ArrayList<>();
        int rank = 1;
        while (!maxHeap.isEmpty()) {
            DisasterIncident top = maxHeap.poll();
            prioritizedList.add(top);
            if (rank <= 5) {
                System.out.printf("  Rank #%d: ID: %s | Urgency Score: %.4f | Keywords: %-22s | Content: \"%s\"\n", 
                        rank, top.id, top.urgencyScore, top.keywords.toString(), top.rawText);
            }
            rank++;
        }

        // -----------------------------------------------------------------------------------------------------
        // PHASE 3: 0/1 KNAPSACK RESOURCE ALLOCATION
        // -----------------------------------------------------------------------------------------------------
        System.out.println("\n--- PHASE 3: 0/1 KNAPSACK RESOURCE ALLOCATION ---");
        int maxTruckCapacityKg = 70; // Capacity limit
        List<DisasterIncident> selectedForTruck = solveKnapsack(prioritizedList, maxTruckCapacityKg);

        System.out.printf("Relief Truck Payload Capacity: %d kg\n", maxTruckCapacityKg);
        System.out.println("Selected Incidents for Immediate Supply Dispatch:");
        int totalWeightAllocated = 0;
        double totalUrgencyScaled = 0.0;

        for (DisasterIncident incident : selectedForTruck) {
            System.out.printf("  [DISPATCH] ID: %s | Weight: %2d kg | Urgency Impact: %5.2f | Keywords: %-20s | Content: \"%s\"\n",
                    incident.id, incident.requiredSupplyWeight, incident.urgencyScore * 100, incident.keywords.toString(), incident.rawText);
            totalWeightAllocated += incident.requiredSupplyWeight;
            totalUrgencyScaled += incident.urgencyScore * 100;
        }
        System.out.printf("Total Allocated Payload: %d / %d kg | Total Cumulative Urgency Score: %.2f\n", 
                totalWeightAllocated, maxTruckCapacityKg, totalUrgencyScaled);

        // -----------------------------------------------------------------------------------------------------
        // PHASE 4 & 5: ROAD NETWORK & A* ROUTING
        // -----------------------------------------------------------------------------------------------------
        System.out.println("\n--- PHASE 4 & 5: ROAD NETWORK & A* ROUTING ---");
        DisasterGraph graph = new DisasterGraph();
        Map<String, AStarRouter.NodeCoordinate> coordinates = new HashMap<>();

        String osmFilePath = "Relief_Flow_DSA\\data\\map.osm"; 
        OsmMapLoader.loadOsmMap(osmFilePath, graph, coordinates);

        String[] endpoints = graph.findConnectedStartAndTarget();
        if (endpoints != null) {
            String startNode = endpoints[0];
            String targetNode = endpoints[1];

            AStarRouter.RouteResult astarResult = AStarRouter.findOptimalRouteAStar(graph, coordinates, startNode, targetNode);

            System.out.printf("Routing Goal: [%s] ---> [%s]\n", startNode, targetNode);
            System.out.printf("Optimal Path (%d nodes): %s\n", astarResult.path.size(), astarResult.path);
            System.out.printf("Total Distance: %.2f km | Risk Cost Score: %.2f\n", astarResult.totalDistanceKm, astarResult.totalCost);
        } else {
            System.out.println("No valid path could be generated.");
        }

        // -----------------------------------------------------------------------------------------------------
        // PHASE 7: KRUSKAL REPAIRS
        // -----------------------------------------------------------------------------------------------------
        System.out.println("\n--- PHASE 7: KRUSKAL REPAIRS ---");
        List<RoadEdge> allEdges = graph.getAllEdges();
        allEdges.sort(Comparator.comparingDouble(RoadEdge::getEffectiveCost));

        System.out.printf("Total unique connections found in graph: %d\n", allEdges.size());
        for (int i = 0; i < Math.min(5, allEdges.size()); i++) {
            RoadEdge e = allEdges.get(i);
            System.out.printf("  - Repair Segment #%d: %s <===> %s (Cost: %.2f km)\n", (i + 1), e.sourceZone, e.destinationZone, e.getEffectiveCost());
        }

        System.out.println("\n==========================================================================================================");
        System.out.println("                                    PIPELINE EXECUTION COMPLETE");
        System.out.println("==========================================================================================================");
    }

    private static List<DisasterIncident> solveKnapsack(List<DisasterIncident> items, int capacity) {
        int n = items.size();
        int[] values = new int[n];
        int[] weights = new int[n];

        for (int i = 0; i < n; i++) {
            values[i] = (int) Math.round(items.get(i).urgencyScore * 100);
            weights[i] = items.get(i).requiredSupplyWeight;
        }

        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= capacity; w++) {
                if (weights[i - 1] <= w) {
                    dp[i][w] = Math.max(values[i - 1] + dp[i - 1][w - weights[i - 1]], dp[i - 1][w]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        List<DisasterIncident> selected = new ArrayList<>();
        int w = capacity;
        for (int i = n; i > 0 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                DisasterIncident item = items.get(i - 1);
                selected.add(item);
                w -= item.requiredSupplyWeight;
            }
        }

        Collections.reverse(selected);
        return selected;
    }
}