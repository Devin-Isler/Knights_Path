// name surname: Devin Isler
// student ID: 2023400063
/**
 * The ShortestRoute class for the "Gold Trail: The Knight’s Path".
 * <p>
 * This class compute the shortest route for the knight, starting from
 * a source tile, visiting all gold coin objectives, and returning to the source.
 * It integrates with the PathFinder class to calculate distances between tile pairs.
 * </p>
 *
 * @author Devin Isler
 * @version 1.0
 * @since 2025-05-08
 */
import java.util.*;

public class ShortestRoute {
    private Tile[][] map;
    private int columns;
    private int rows;
    private HashMap<String, Double> travelCosts;
    private PathFinder pathFinder;

    /**
     * Constructs a ShortestRoute object.
     * @param map The 2D array of tiles representing the map
     * @param columns Number of columns in the map
     * @param rows Number of rows in the map
     * @param travelCosts HashMap containing travel costs between tiles
     * @param pathFinder The PathFinder instance for finding shortest paths
     */
    public ShortestRoute(Tile[][] map, int columns, int rows, HashMap<String, Double> travelCosts, PathFinder pathFinder) {
        this.map = map;
        this.columns = columns;
        this.rows = rows;
        this.travelCosts = travelCosts;
        this.pathFinder = pathFinder;
    }

    /**
     * Finds the shortest route starting from source, visiting all objectives, and returning to source.
     * @param source The starting tile
     * @param objectives List of objective tiles to visit
     * @return ArrayList of tiles representing the shortest route
     */
    public ArrayList<Tile> findShortestRoute(Tile source, ArrayList<Tile> objectives) {
        int n = objectives.size();
        if (n == 0) {
            ArrayList<Tile> route = new ArrayList<>();
            route.add(source);
            route.add(source); // Return to start
            return route;
        }

        // Precompute shortest path costs between all pairs of tiles (source + objectives)
        Tile[] nodes = new Tile[n + 1];
        nodes[0] = source;
        for (int i = 0; i < n; i++) {
            nodes[i + 1] = objectives.get(i);
        }

        double[][] costs = new double[n + 1][n + 1];
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == j) {
                    costs[i][j] = 0;
                } else {
                    ArrayList<Tile> path = pathFinder.findShortestPath(nodes[i], nodes[j]);
                    costs[i][j] = path != null ? nodes[j].getDistance() : Double.POSITIVE_INFINITY;
                }
            }
        }

        // dp[mask][last] = min cost to visit all nodes in mask ending at last
        double[][] dp = new double[1 << (n + 1)][n + 1];
        int[][] parent = new int[1 << (n + 1)][n + 1];
        for (double[] row : dp) {
            Arrays.fill(row, Double.POSITIVE_INFINITY);
        }

        // Initialize: start at source (node 0)
        dp[1][0] = 0;

        // Iterate over all subsets
        for (int mask = 1; mask < (1 << (n + 1)); mask++) {
            for (int last = 0; last <= n; last++) {
                if (dp[mask][last] == Double.POSITIVE_INFINITY) continue;
                // Try adding each unvisited node
                for (int next = 0; next <= n; next++) {
                    if ((mask & (1 << next)) == 0 && costs[last][next] != Double.POSITIVE_INFINITY) {
                        int newMask = mask | (1 << next);
                        double newCost = dp[mask][last] + costs[last][next];
                        if (newCost < dp[newMask][next]) {
                            dp[newMask][next] = newCost;
                            parent[newMask][next] = last;
                        }
                    }
                }
            }
        }

        // Find the minimum cost to complete the tour (return to source)
        double minCost = Double.POSITIVE_INFINITY;
        int lastNode = -1;
        int finalMask = (1 << (n + 1)) - 1;
        for (int last = 1; last <= n; last++) {
            if (costs[last][0] != Double.POSITIVE_INFINITY) {
                double tourCost = dp[finalMask][last] + costs[last][0];
                if (tourCost < minCost) {
                    minCost = tourCost;
                    lastNode = last;
                }
            }
        }

        // Reconstruct the route
        ArrayList<Tile> route = new ArrayList<>();
        if (lastNode == -1) {
            // No valid tour exists
            route.add(source);
            return route;
        }

        int currentMask = finalMask;
        int currentNode = lastNode;
        Stack<Integer> path = new Stack<>();
        path.push(currentNode);

        while (currentMask != 1) {
            int prevNode = parent[currentMask][currentNode];
            path.push(prevNode);
            currentMask ^= (1 << currentNode);
            currentNode = prevNode;
        }

        // Build the route in correct order
        while (!path.isEmpty()) {
            route.add(nodes[path.pop()]);
        }
        route.add(source); // Return to start

        return route;
    }
}