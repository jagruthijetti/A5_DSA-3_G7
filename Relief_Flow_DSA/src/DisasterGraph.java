import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

class RoadEdge {
    public String sourceZone;
    public String destinationZone;
    public double distanceKm;
    public double riskFactor = 1.0;

    public RoadEdge(String sourceZone, String destinationZone, double distanceKm, double riskFactor) {
        this.sourceZone = (sourceZone == null) ? "" : sourceZone;
        this.destinationZone = (destinationZone == null) ? "" : destinationZone;
        this.distanceKm = (distanceKm < 0) ? 0.0 : distanceKm;
        this.riskFactor = (riskFactor <= 0) ? 1.0 : riskFactor;
    }

    public RoadEdge(String destinationZone, double distanceKm, double riskFactor) {
        this("", destinationZone, distanceKm, riskFactor);
    }

    public double getEffectiveCost() {
        double cost = distanceKm * riskFactor;
        if (cost <= 0.0 && distanceKm > 0) {
            return distanceKm;
        }
        return cost;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%.2f km, Risk: %.1f, Cost: %.2f)", 
                sourceZone.isEmpty() ? "?" : sourceZone,
                destinationZone, distanceKm, riskFactor, getEffectiveCost());
    }
}

public class DisasterGraph {

    private final Map<String, List<RoadEdge>> adjacencyList;

    public DisasterGraph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addZone(String zoneId) {
        if (zoneId != null && !zoneId.isEmpty()) {
            adjacencyList.putIfAbsent(zoneId, new ArrayList<>());
        }
    }

    public void addEdge(String source, String destination, double distanceKm, double riskFactor) {
        if (source == null || destination == null) return;
        addZone(source);
        addZone(destination);
        adjacencyList.get(source).add(new RoadEdge(source, destination, distanceKm, riskFactor));
    }

    public void addRoad(String source, String destination, double distanceKm, double riskFactor) {
        if (source == null || destination == null) return;
        addZone(source);
        addZone(destination);
        adjacencyList.get(source).add(new RoadEdge(source, destination, distanceKm, riskFactor));
        adjacencyList.get(destination).add(new RoadEdge(destination, source, distanceKm, riskFactor));
    }

    public List<RoadEdge> getNeighbors(String zoneId) {
        return adjacencyList.getOrDefault(zoneId, Collections.emptyList());
    }

    public List<String> getAllZones() {
        return new ArrayList<>(adjacencyList.keySet());
    }

    public int getNodeCount() {
        return adjacencyList.size();
    }

    public List<RoadEdge> getAllEdges() {
        List<RoadEdge> allEdges = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();

        for (String u : adjacencyList.keySet()) {
            for (RoadEdge edge : adjacencyList.get(u)) {
                String edgeKey = u.compareTo(edge.destinationZone) < 0 
                    ? u + "---" + edge.destinationZone 
                    : edge.destinationZone + "---" + u;

                if (!visitedEdges.contains(edgeKey)) {
                    visitedEdges.add(edgeKey);
                    RoadEdge canonicalEdge = new RoadEdge(u, edge.destinationZone, edge.distanceKm, edge.riskFactor);
                    allEdges.add(canonicalEdge);
                }
            }
        }
        return allEdges;
    }

    public String[] findConnectedStartAndTarget() {
        for (String start : adjacencyList.keySet()) {
            if (adjacencyList.get(start).isEmpty()) continue;

            Set<String> visited = new HashSet<>();
            Queue<String> queue = new LinkedList<>();
            queue.add(start);
            visited.add(start);

            while (!queue.isEmpty() && visited.size() < 500) {
                String current = queue.poll();
                for (RoadEdge edge : adjacencyList.getOrDefault(current, Collections.emptyList())) {
                    if (!visited.contains(edge.destinationZone)) {
                        visited.add(edge.destinationZone);
                        queue.add(edge.destinationZone);
                    }
                }
            }

            if (visited.size() >= 5) {
                List<String> componentNodes = new ArrayList<>(visited);
                String target = componentNodes.get(componentNodes.size() - 1);
                return new String[]{start, target};
            }
        }

        // Fallback for smaller graphs
        if (!adjacencyList.isEmpty()) {
            String first = adjacencyList.keySet().iterator().next();
            List<RoadEdge> edges = adjacencyList.get(first);
            if (edges != null && !edges.isEmpty()) {
                return new String[]{first, edges.get(0).destinationZone};
            }
        }
        return null;
    }
}