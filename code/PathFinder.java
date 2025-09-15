// name surname: Devin Isler
// student ID: 2023400063
/**
 * The PathFinder class for the "Gold Trail: The Knight’s Path".
 * <p>
 * This class determines the optimal path for a knight
 * to navigate a map and collect gold coins, accounting for terrain costs
 * and obstacles. It facilitates efficient pathfinding between any two tiles on the map.
 * </p>
 *
 * @author Devin Isler
 * @version 1.0
 * @since 2025-05-08
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;

public class PathFinder {
    private Tile[][] map;
    private int columns;
    private int rows;
    private HashMap<String, Double> travelCosts;

    /**
     * Constructs a PathFinder for finding the shortest paths on the given map.
     * @param map The 2D array of tiles representing the map
     * @param columns Number of columns in the map
     * @param rows Number of rows in the map
     * @param travelCosts HashMap containing travel costs between tiles
     */
    public PathFinder(Tile[][] map, int columns, int rows, HashMap<String, Double> travelCosts) {
        this.map = map;
        this.columns = columns;
        this.rows = rows;
        this.travelCosts = travelCosts;
    }

    /**
     * Finds the shortest path from source to target using Dijkstra's algorithm.
     * @param source The starting tile
     * @param target The destination tile
     * @return ArrayList of tiles representing the shortest path, or null if no path exists
     */
    public ArrayList<Tile> findShortestPath(Tile source, Tile target) {
        // Reset all tiles' pathfinding properties
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if (map[i][j] != null) {
                    map[i][j].setDistance(Double.POSITIVE_INFINITY);
                    map[i][j].setPrevious(null);
                    map[i][j].setVisited(false);
                }
            }
        }

        // Initialize source
        source.setDistance(0);

        // Create priority queue for Dijkstra's algorithm
        PriorityQueue<Tile> queue = new PriorityQueue<>(new Comparator<Tile>() {
            @Override
            public int compare(Tile t1, Tile t2) {
                if (t1.getDistance() < t2.getDistance())
                    return -1;
                if (t1.getDistance() > t2.getDistance())
                    return 1;
                return 0;
            }
        });

        queue.add(source);

        // Process the queue
        while (!queue.isEmpty()) {
            Tile current = queue.poll();

            // If we've reached the target, we're done
            if (current.equals(target)) {
                break;
            }

            // Skip if already visited
            if (current.isVisited()) {
                continue;
            }

            current.setVisited(true);

            // Process each adjacent tile
            ArrayList<Tile> neighbors = current.getAdjacentTiles();
            for (int i = 0; i < neighbors.size(); i++) {
                Tile neighbor = neighbors.get(i);

                // Skip obstacles (type 2)
                if (neighbor.getType() == 2) {
                    continue;
                }

                // Get the travel cost between current and neighbor
                double cost = getCost(current, neighbor);
                double newDistance = current.getDistance() + cost;

                // Update distance if we've found a shorter path
                if (newDistance < neighbor.getDistance()) {
                    neighbor.setDistance(newDistance);
                    neighbor.setPrevious(current);

                    // Add to queue for processing
                    queue.add(neighbor);
                }
            }
        }

        // Check if target is reachable
        if (target.getDistance() == Double.POSITIVE_INFINITY) {
            return null; // No path exists
        }

        // Reconstruct path
        ArrayList<Tile> path = new ArrayList<>();
        Tile current = target;

        while (current != null) {
            path.add(current);
            current = current.getPrevious();
        }

        // Reverse the path to get source to target order
        ArrayList<Tile> reversedPath = new ArrayList<>();
        for (int i = path.size() - 1; i >= 0; i--) {
            reversedPath.add(path.get(i));
        }

        return reversedPath;
    }

    /**
     * Gets the travel cost between two tiles.
     * @param from The starting tile
     * @param to The destination tile
     * @return The travel cost between the two tiles
     */
    private double getCost(Tile from, Tile to) {
        String key = from.getColumn() + " " + from.getRow() + " " + to.getColumn() + " " + to.getRow();
        String reverseKey = to.getColumn() + " " + to.getRow() + " " + from.getColumn() + " " + from.getRow();

        // Check if we have a direct cost in the travelCosts map
        if (travelCosts.containsKey(key)) {
            return travelCosts.get(key);
        } else if (travelCosts.containsKey(reverseKey)) {
            return travelCosts.get(reverseKey);
        }

        // If no specific cost is defined, use default based on terrain types
        // This shouldn't happen if all costs are provided in the input file
        if (from.getType() == 0 && to.getType() == 0) { // Grass to Grass
            return 3.0; // Default mid-range of 1-5
        } else if (from.getType() == 1 && to.getType() == 1) { // Sand to Sand
            return 9.0; // Default mid-range of 8-10
        } else { // Mixed terrain
            return 8.0; // Default lower range of mixed cost
        }
    }
}