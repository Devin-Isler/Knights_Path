// name surname: Devin Isler
// student ID: 2023400063
/**
 * The Bonus class for the "Gold Trail: The Knight’s Path".
 * <p>
 * This class enhances the game by using the ShortestRoute class to find an optimal path
 * for the knight to visit all gold coins and return to the starting point. It features
 * advanced visualization with colored path segments and generates output for bonus requirements.
 * </p>
 *
 * @author Devin Isler
 * @version 1.0
 * @since 2025-05-08
 */
import java.io.*;
import java.util.*;
import java.awt.Color;

public class Bonus {
    private static Tile[][] map;
    private static int columns;
    private static int rows;
    private static HashMap<String, Double> travelCosts;
    private static ArrayList<int[]> objectives;
    private static int[] startingPosition;
    private static boolean drawEnabled = false;
    private static double totalCost = 0;
    private static int totalSteps = 0;
    private static final ArrayList<Tile> visitedTiles = new ArrayList<>();
    private static final List<Map.Entry<List<Tile>, Color>> pathSegments = new ArrayList<>();
    private static List<Tile> currentSegmentTiles = new ArrayList<>();
    private static final Random random = new Random();
    private static Color currentPathColor = getRandomColor(); // Random initial color

    /**
     * Generates a random color for path segments.
     * @return A random Color object
     */
    private static Color getRandomColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    /**
     * Main method to run the program.
     * @param args Command-line arguments
     * @throws IOException If an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        // Process draw flag if present
        if (args.length > 0 && args[0].equals("-draw")) {
            drawEnabled = true;
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            args = newArgs;
        }

        // Check arguments
        if (args.length < 3) {
            System.out.println("Usage: java Bonus [-draw] mapData.txt travelCosts.txt objectives.txt");
            return;
        }

        // Read all input files
        readMapData(args[0]);
        readTravelCosts(args[1]);
        readObjectives(args[2]);

        // Create output directory if needed
        File outputDir = new File("out");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        // Set up visualization if enabled
        if (drawEnabled) {
            StdDraw.pause(100);
            setupVisualization();
        }

        // Process objectives using ShortestRoute
        processObjectives();
        // System.exit(0); // Uncomment if program still hangs
    }

    /**
     * Processes objectives by finding the shortest route and writing results to bonus.txt.
     * @throws IOException If an I/O error occurs
     */
    private static void processObjectives() throws IOException {
        PrintWriter output = new PrintWriter(new FileWriter("out/bonus.txt"));

        // Create PathFinder and ShortestRoute
        PathFinder pathFinder = new PathFinder(map, columns, rows, travelCosts);
        ShortestRoute shortestRoute = new ShortestRoute(map, columns, rows, travelCosts, pathFinder);

        // Get the optimal order of objectives
        ArrayList<Tile> optimalRoute = shortestRoute.findShortestRoute(
                map[startingPosition[0]][startingPosition[1]],
                getObjectiveTiles()
        );

        // Map objectives to their original indices in objectives.txt
        HashMap<Tile, Integer> objectiveIndices = new HashMap<>();
        for (int i = 0; i < objectives.size(); i++) {
            int[] obj = objectives.get(i);
            Tile tile = map[obj[0]][obj[1]];
            objectiveIndices.put(tile, i + 1); // 1-based indexing for objectives
        }

        // Start from the initial position
        Tile currentTile = map[startingPosition[0]][startingPosition[1]];

        // Process each tile in the optimal route (excluding the final return to start)
        for (int i = 1; i < optimalRoute.size() - 1; i++) {
            Tile targetTile = optimalRoute.get(i);
            int objectiveNumber = objectiveIndices.getOrDefault(targetTile, -1);

            // Find the shortest path to the current objective
            ArrayList<Tile> path = pathFinder.findShortestPath(currentTile, targetTile);

            // If a path exists, follow it
            if (path != null && path.size() > 1) {
                currentSegmentTiles = new ArrayList<>(); // Start new segment

                // Follow the path
                for (int j = 1; j < path.size(); j++) {
                    Tile from = path.get(j - 1);
                    Tile to = path.get(j);
                    visitedTiles.add(from);
                    currentSegmentTiles.add(from);

                    // Calculate cost for this step
                    String key = from.getColumn() + " " + from.getRow() + " " + to.getColumn() + " " + to.getRow();
                    String reverseKey = to.getColumn() + " " + to.getRow() + " " + from.getColumn() + " " + from.getRow();

                    double stepCost = 0;
                    if (travelCosts.containsKey(key)) {
                        stepCost = travelCosts.get(key);
                    } else if (travelCosts.containsKey(reverseKey)) {
                        stepCost = travelCosts.get(reverseKey);
                    }

                    totalCost += stepCost;
                    totalSteps++;

                    // Write step to output file
                    output.printf("Step Count: %d, move to %s. Total Cost: %.2f.%n", totalSteps, to, totalCost);

                    // Visualize step if drawing is enabled
                    if (drawEnabled && j != path.size() - 1) {
                        StdDraw.clear();
                        drawStep(to);
                        StdDraw.pause(150);
                    }
                }

                // Save the current segment
                pathSegments.add(new AbstractMap.SimpleEntry<>(new ArrayList<>(currentSegmentTiles), currentPathColor));

                output.println("Objective " + objectiveNumber + " reached!");

                // Change path color randomly for the next segment
                if (drawEnabled) {
                    currentPathColor = getRandomColor();
                    objectives.removeIf(obj -> obj != null && obj[0] == targetTile.getColumn() && obj[1] == targetTile.getRow());
                    drawObjectiveReached(targetTile);
                    StdDraw.pause(700);
                }

                // Update current position to the reached objective
                currentTile = targetTile;
            } else {
                // Path doesn't exist
                output.println("Objective " + objectiveNumber + " cannot be reached!");
            }
        }

        // Return to the starting position
        Tile startTile = map[startingPosition[0]][startingPosition[1]];
        ArrayList<Tile> returnPath = pathFinder.findShortestPath(currentTile, startTile);

        if (returnPath != null && returnPath.size() > 1) {
            currentSegmentTiles = new ArrayList<>(); // Start return segment
            // Change path color for return path
            if (drawEnabled) {
                currentPathColor = getRandomColor();
            }

            for (int j = 1; j < returnPath.size(); j++) {
                Tile from = returnPath.get(j - 1);
                Tile to = returnPath.get(j);
                visitedTiles.add(from);
                currentSegmentTiles.add(from);

                String key = from.getColumn() + " " + from.getRow() + " " + to.getColumn() + " " + to.getRow();
                String reverseKey = to.getColumn() + " " + to.getRow() + " " + from.getColumn() + " " + from.getRow();

                double stepCost = 0;
                if (travelCosts.containsKey(key)) {
                    stepCost = travelCosts.get(key);
                } else if (travelCosts.containsKey(reverseKey)) {
                    stepCost = travelCosts.get(reverseKey);
                }

                totalCost += stepCost;
                totalSteps++;

                output.printf("Step Count: %d, move to %s. Total Cost: %.2f.%n", totalSteps, to, totalCost);

                if (drawEnabled && j != returnPath.size() - 1) {
                    StdDraw.clear();
                    drawStep(to);
                    StdDraw.pause(150);
                }
            }

            // Save the return segment
            pathSegments.add(new AbstractMap.SimpleEntry<>(new ArrayList<>(currentSegmentTiles), currentPathColor));

            if (drawEnabled) {
                drawObjectiveReached(startTile);
                StdDraw.pause(700);
            }
        }

        // Write total statistics
        output.printf("Total Step: %d, Total Cost: %.2f%n", totalSteps, totalCost);
        output.close();
    }

    /**
     * Converts objectives to a list of Tile objects.
     * @return ArrayList of Tile objects representing objectives
     */
    private static ArrayList<Tile> getObjectiveTiles() {
        ArrayList<Tile> objectiveTiles = new ArrayList<>();
        for (int[] objective : objectives) {
            objectiveTiles.add(map[objective[0]][objective[1]]);
        }
        return objectiveTiles;
    }

    /**
     * Reads the map data file and initializes the map.
     * @param filename The name of the map data file
     * @throws IOException If an I/O error occurs
     */
    private static void readMapData(String filename) throws IOException {
        Scanner scanner = new Scanner(new File(filename));

        // Read map dimensions
        String[] dimensions = scanner.nextLine().split(" ");
        columns = Integer.parseInt(dimensions[0]);
        rows = Integer.parseInt(dimensions[1]);

        // Initialize map
        map = new Tile[columns][rows];

        // Read tile data
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(" ");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int type = Integer.parseInt(parts[2]);

            map[x][y] = new Tile(x, y, type);
        }

        scanner.close();

        // Set up adjacent tiles
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                if (map[x][y] != null) {
                    if (x > 0 && map[x-1][y] != null) {
                        map[x][y].addAdjacentTile(map[x-1][y]);
                    }
                    if (x < columns-1 && map[x+1][y] != null) {
                        map[x][y].addAdjacentTile(map[x+1][y]);
                    }
                    if (y > 0 && map[x][y-1] != null) {
                        map[x][y].addAdjacentTile(map[x][y-1]);
                    }
                    if (y < rows-1 && map[x][y+1] != null) {
                        map[x][y].addAdjacentTile(map[x][y+1]);
                    }
                }
            }
        }
    }

    /**
     * Reads the travel costs file and initializes the travel costs map.
     * @param filename The name of the travel costs file
     * @throws IOException If an I/O error occurs
     */
    private static void readTravelCosts(String filename) throws IOException {
        Scanner scanner = new Scanner(new File(filename));
        travelCosts = new HashMap<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(" ");
            int x1 = Integer.parseInt(parts[0]);
            int y1 = Integer.parseInt(parts[1]);
            int x2 = Integer.parseInt(parts[2]);
            int y2 = Integer.parseInt(parts[3]);
            double cost = Double.parseDouble(parts[4]); // Fixed: parseDouble instead of parseInt

            travelCosts.put(x1 + " " + y1 + " " + x2 + " " + y2, cost);
            travelCosts.put(x2 + " " + y2 + " " + x1 + " " + y1, cost);
        }

        scanner.close();
    }

    /**
     * Reads the objectives file and initializes the objectives list.
     * @param filename The name of the objectives file
     * @throws IOException If an I/O error occurs
     */
    private static void readObjectives(String filename) throws IOException {
        Scanner scanner = new Scanner(new File(filename));
        objectives = new ArrayList<>();

        // Read starting position
        String[] startPos = scanner.nextLine().split(" ");
        startingPosition = new int[] {Integer.parseInt(startPos[0]), Integer.parseInt(startPos[1])};

        // Read objectives
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(" ");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            objectives.add(new int[] {x, y});
        }

        scanner.close();
    }

    /**
     * Sets up the StdDraw visualization.
     */
    private static void setupVisualization() {
        StdDraw.setCanvasSize(30*rows, 30*columns);
        StdDraw.setXscale(-0.5, columns - 0.5);
        StdDraw.setYscale(-0.5, rows - 0.5);
        StdDraw.enableDoubleBuffering();

        drawMap();
        drawGoldCoin();
        drawKnight(startingPosition[0], startingPosition[1]);
        StdDraw.show();
    }

    /**
     * Draws the map with appropriate tile textures.
     */
    private static void drawMap() {
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                if (map[x][y] != null) {
                    if (map[x][y].getType() == 0) {
                        StdDraw.picture(x, rows-1-y, "./misc/grassTile.jpeg", 1.0, 1.0);
                    } else if (map[x][y].getType() == 1) {
                        StdDraw.picture(x, rows-1-y, "./misc/sandTile.png", 1.0, 1.0);
                    } else {
                        StdDraw.picture(x, rows-1-y, "./misc/impassableTile.jpeg", 1.0, 1.0);
                    }
                }
            }
        }
    }

    /**
     * Draws a knight figure at the specified position.
     * @param x The column position
     * @param y The row position
     */
    private static void drawKnight(int x, int y) {
        StdDraw.picture(x, rows - 1 - y, "./misc/knight.png", 1.0, 1.0);
    }

    /**
     * Draws gold coins at objective positions.
     */
    private static void drawGoldCoin() {
        for (int[] objective : objectives) {
            if (objective != null) {
                StdDraw.picture(objective[0], rows - 1 - objective[1], "./misc/coin.png", 1.0, 1.0);
            }
        }
    }

    /**
     * Visualizes a step in the path, drawing each segment in its assigned color.
     * @param to The tile to move to
     */
    private static void drawStep(Tile to) {
        drawMap();
        drawGoldCoin();

        // Draw previous segments in their colors
        for (Map.Entry<List<Tile>, Color> segment : pathSegments) {
            StdDraw.setPenColor(segment.getValue());
            for (Tile tile : segment.getKey()) {
                StdDraw.filledCircle(tile.getColumn(), rows - 1 - tile.getRow(), 0.15);
            }
        }

        // Draw current segment in current color
        StdDraw.setPenColor(currentPathColor);
        for (Tile tile : currentSegmentTiles) {
            StdDraw.filledCircle(tile.getColumn(), rows - 1 - tile.getRow(), 0.15);
        }

        drawKnight(to.getColumn(), to.getRow());
        StdDraw.show();
    }

    /**
     * Visualizes reaching an objective, drawing each segment in its assigned color.
     * @param tile The tile where the objective is reached
     */
    private static void drawObjectiveReached(Tile tile) {
        StdDraw.clear();
        drawMap();
        drawGoldCoin();

        // Draw current segment in current color
        StdDraw.setPenColor(currentPathColor);
        for (Tile tileInPath : currentSegmentTiles) {
            StdDraw.filledCircle(tileInPath.getColumn(), rows - 1 - tileInPath.getRow(), 0.15);
        }

        // Draw previous segments in their colors
        for (Map.Entry<List<Tile>, Color> segment : pathSegments) {
            StdDraw.setPenColor(segment.getValue());
            for (Tile tileInPath : segment.getKey()) {
                StdDraw.filledCircle(tileInPath.getColumn(), rows - 1 - tileInPath.getRow(), 0.15);
            }
        }

        drawKnight(tile.getColumn(), tile.getRow()); // Fixed: tile.getRow() instead of to.getRow()
        StdDraw.show();
    }
}
