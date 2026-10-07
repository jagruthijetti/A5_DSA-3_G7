import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.File;
import java.util.*;

public class OsmMapLoader {

    public static void loadOsmMap(String filePath, DisasterGraph graph, Map<String, AStarRouter.NodeCoordinate> coordinates) {
        File osmFile = new File(filePath);

        if (!osmFile.exists()) {
            System.out.printf("  [WARNING] File '%s' not found! Falling back to sample network.\n", filePath);
            createFallbackGraph(graph, coordinates);
            return;
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                private String currentNodeId = null;
                private double currentLat = 0.0;
                private double currentLon = 0.0;

                private final List<String> currentWayNodes = new ArrayList<>();
                private boolean isHighway = false;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    // 1. Parse Node (Intersection / Point)
                    if (qName.equalsIgnoreCase("node")) {
                        currentNodeId = "Node_" + attributes.getValue("id");
                        currentLat = Double.parseDouble(attributes.getValue("lat"));
                        currentLon = Double.parseDouble(attributes.getValue("lon"));
                        coordinates.put(currentNodeId, new AStarRouter.NodeCoordinate(currentLat, currentLon));
                    }
                    // 2. Start Way (Road segment)
                    else if (qName.equalsIgnoreCase("way")) {
                        currentWayNodes.clear();
                        isHighway = false;
                    }
                    // 3. Collect Way Node References
                    else if (qName.equalsIgnoreCase("nd")) {
                        String ref = "Node_" + attributes.getValue("ref");
                        currentWayNodes.add(ref);
                    }
                    // 4. Check if Way is a walkable/driveable road
                    else if (qName.equalsIgnoreCase("tag")) {
                        String k = attributes.getValue("k");
                        if ("highway".equalsIgnoreCase(k)) {
                            isHighway = true;
                        }
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    // When closing a <way>, add road connections if it's a valid highway
                    if (qName.equalsIgnoreCase("way")) {
                        if (isHighway && currentWayNodes.size() >= 2) {
                            for (int i = 0; i < currentWayNodes.size() - 1; i++) {
                                String u = currentWayNodes.get(i);
                                String v = currentWayNodes.get(i + 1);

                                AStarRouter.NodeCoordinate c1 = coordinates.get(u);
                                AStarRouter.NodeCoordinate c2 = coordinates.get(v);

                                double distanceKm = calculateHaversine(c1, c2);
                                double riskFactor = 1.0; // Default hazard factor

                                graph.addRoad(u, v, distanceKm, riskFactor);
                            }
                        }
                    }
                }
            };

            System.out.printf("  [SAX PARSER] Parsing OpenStreetMap file: %s...\n", filePath);
            saxParser.parse(osmFile, handler);
            System.out.printf("  [SAX PARSER] Successfully loaded %d nodes into graph!\n", graph.getNodeCount());

        } catch (Exception e) {
            System.err.println("  [ERROR] Failed to parse OSM file: " + e.getMessage());
            e.printStackTrace();
            createFallbackGraph(graph, coordinates);
        }
    }

    private static double calculateHaversine(AStarRouter.NodeCoordinate c1, AStarRouter.NodeCoordinate c2) {
        if (c1 == null || c2 == null) return 0.5; // Fallback distance

        final double R = 6371.0; // Earth radius in km
        double lat1Rad = Math.toRadians(c1.lat);
        double lat2Rad = Math.toRadians(c2.lat);
        double dLat = Math.toRadians(c2.lat - c1.lat);
        double dLon = Math.toRadians(c2.lon - c1.lon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.max(0.01, R * c);
    }

    private static void createFallbackGraph(DisasterGraph graph, Map<String, AStarRouter.NodeCoordinate> coordinates) {
        graph.addRoad("Node_13382968290", "Node_Intermediate_1", 2.4, 1.1);
        graph.addRoad("Node_Intermediate_1", "Node_3579915039", 3.1, 1.2);
        graph.addRoad("Node_13382968290", "Node_3579915039", 6.0, 1.5);

        coordinates.put("Node_13382968290", new AStarRouter.NodeCoordinate(12.9716, 77.5946));
        coordinates.put("Node_Intermediate_1", new AStarRouter.NodeCoordinate(12.9750, 77.5980));
        coordinates.put("Node_3579915039", new AStarRouter.NodeCoordinate(12.9800, 77.6000));
    }
}