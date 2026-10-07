import java.util.*;

public class AStarRouter {

    public static class NodeCoordinate {
        public final double lat;
        public final double lon;

        public NodeCoordinate(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    public static class RouteResult {
        public final List<String> path;
        public final double totalDistanceKm;
        public final double totalCost;

        public RouteResult(List<String> path, double totalDistanceKm, double totalCost) {
            this.path = path;
            this.totalDistanceKm = totalDistanceKm;
            this.totalCost = totalCost;
        }
    }

    private static class AStarNode implements Comparable<AStarNode> {
        final String nodeId;
        final double fScore;

        AStarNode(String nodeId, double gScore, double fScore) {
            this.nodeId = nodeId;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(AStarNode other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    public static RouteResult findOptimalRouteAStar(
            DisasterGraph graph,
            Map<String, NodeCoordinate> coordinates,
            String startNode,
            String targetNode) {

        if (startNode == null || targetNode == null) {
            return new RouteResult(Collections.emptyList(), 0.0, Double.MAX_VALUE);
        }

        Map<String, Double> gScore = new HashMap<>();
        Map<String, Double> fScore = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();
        Map<String, Double> actualDistanceMap = new HashMap<>();

        java.util.PriorityQueue<AStarNode> openSet = new java.util.PriorityQueue<>();
        Set<String> closedSet = new HashSet<>();

        gScore.put(startNode, 0.0);
        double initialH = calculateHaversineHeuristic(coordinates.get(startNode), coordinates.get(targetNode));
        fScore.put(startNode, initialH);
        actualDistanceMap.put(startNode, 0.0);

        openSet.add(new AStarNode(startNode, 0.0, initialH));

        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();
            String currentId = current.nodeId;

            if (currentId.equals(targetNode)) {
                return reconstructRoute(cameFrom, currentId, gScore.get(currentId), actualDistanceMap.get(currentId));
            }

            if (closedSet.contains(currentId)) continue;
            closedSet.add(currentId);

            List<RoadEdge> neighbors = graph.getNeighbors(currentId);
            if (neighbors == null) continue;

            for (RoadEdge edge : neighbors) {
                String neighborId = edge.destinationZone;
                if (closedSet.contains(neighborId)) continue;

                double edgeCost = edge.getEffectiveCost();
                double tentativeGScore = gScore.getOrDefault(currentId, Double.MAX_VALUE) + edgeCost;

                if (tentativeGScore < gScore.getOrDefault(neighborId, Double.MAX_VALUE)) {
                    cameFrom.put(neighborId, currentId);
                    gScore.put(neighborId, tentativeGScore);

                    double accumDistance = actualDistanceMap.getOrDefault(currentId, 0.0) + edge.distanceKm;
                    actualDistanceMap.put(neighborId, accumDistance);

                    double hScore = calculateHaversineHeuristic(coordinates.get(neighborId), coordinates.get(targetNode));
                    double totalFScore = tentativeGScore + hScore;

                    fScore.put(neighborId, totalFScore);
                    openSet.add(new AStarNode(neighborId, tentativeGScore, totalFScore));
                }
            }
        }

        return new RouteResult(Collections.emptyList(), 0.0, Double.MAX_VALUE);
    }

    private static double calculateHaversineHeuristic(NodeCoordinate c1, NodeCoordinate c2) {
        if (c1 == null || c2 == null) return 0.0;

        final double R = 6371.0;
        double lat1Rad = Math.toRadians(c1.lat);
        double lat2Rad = Math.toRadians(c2.lat);
        double dLat = Math.toRadians(c2.lat - c1.lat);
        double dLon = Math.toRadians(c2.lon - c1.lon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static RouteResult reconstructRoute(
            Map<String, String> cameFrom,
            String currentId,
            double totalCost,
            double totalDistanceKm) {

        LinkedList<String> path = new LinkedList<>();
        path.addFirst(currentId);

        while (cameFrom.containsKey(currentId)) {
            currentId = cameFrom.get(currentId);
            path.addFirst(currentId);
        }

        return new RouteResult(path, totalDistanceKm, totalCost);
    }
}