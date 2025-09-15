// name surname: Devin Isler
// student ID: 2023400063
/**
 * The Main class for the "Gold Trail: The Knight’s Path".
 * <p>
 * This class serves as the entry point, managing input file processing, map initialization,
 * and the knight’s movement to collect gold coins using the PathFinder class. It also provides
 * optional visualization of the knight’s journey and outputs the results to a file.
 * </p>
 *
 * @author Devin Isler
 * @version 1.0
 * @since 2025-05-08
 */
import java.io.*;
import java.util.*;

public class Main {
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
            System.out.println("Usage: java Main [-draw] mapData.txt travelCosts.txt objectives.txt");
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

        // Process all objectives and write results to output file
        processObjectives();
    }

    /**
     * Processes all objectives and writes results to the output file.
     * @throws IOException If an I/O error occurs
     */
    private static void processObjectives() throws IOException {
        PrintWriter output = new PrintWriter(new FileWriter("out/output.txt"));

        // Create the PathFinder
        PathFinder pathFinder = new PathFinder(map, columns, rows, travelCosts);

        // Start from the initial position
        int[] currentPos = startingPosition;
        Tile currentTile = map[currentPos[0]][currentPos[1]];

        // Process each objective
        for (int i = 0; i < objectives.size(); i++) {
            int[] objective = objectives.get(i);
            Tile targetTile = map[objective[0]][objective[1]];

            // Find the shortest path to the current objective
            ArrayList<Tile> path = pathFinder.findShortestPath(currentTile, targetTile);

            // If a path exists, follow it
            if (path != null && path.size() > 1) {
                output.println("Starting position: " + currentTile);
                double pathCost = 0;
                visitedTiles.clear();

                // Follow the path
                for (int j = 1; j < path.size(); j++) {
                    Tile from = path.get(j - 1);
                    Tile to = path.get(j);
                    visitedTiles.add(from);

                    // Calculate cost for this step
                    String key = from.getColumn() + " " + from.getRow() + " " + to.getColumn() + " " + to.getRow();
                    String reverseKey = to.getColumn() + " " + to.getRow() + " " + from.getColumn() + " " + from.getRow();

                    double stepCost = 0;
                    if (travelCosts.containsKey(key)) {
                        stepCost = travelCosts.get(key);
                    } else if (travelCosts.containsKey(reverseKey)) {
                        stepCost = travelCosts.get(reverseKey);
                    }

                    pathCost += stepCost;
                    totalCost += stepCost;
                    totalSteps++;

                    // Write step to output file
                    output.printf("Step Count: %d, move to %s. Total Cost: %.2f.%n", j, to, pathCost);

                    // Visualize step if drawing is enabled
                    if (drawEnabled && j != path.size() - 1) {
                        StdDraw.clear();
                        drawStep(to);
                        StdDraw.pause(150);
                    }
                }

                output.println("Objective " + (i + 1) + " reached!");

                // Update current position to the reached objective
                currentPos = objective;
                currentTile = targetTile;

                // Visualize reaching the objective
                if (drawEnabled) {
                    objectives.set(i, null);
                    drawObjectiveReached(targetTile);
                    StdDraw.pause(700);
                }
            } else {
                // Path doesn't exist
                output.println("Objective " + (i + 1) + " cannot be reached!");
            }
        }

        // Write total statistics
        output.printf("Total Step: %d, Total Cost: %.2f%n", totalSteps, totalCost);
        output.close();
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
                    // Add adjacent tiles in four directions (up, down, left, right)
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
            double cost = Double.parseDouble(parts[4]);

            // Store the cost in both directions
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

        // Draw the map
        drawMap();

        // Draw objectives
        drawGoldCoin();

        // Draw knight at starting position
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
                    // Set texture based on tile type
                    if (map[x][y].getType() == 0) {
                        StdDraw.picture(x, rows-1-y, "./misc/grassTile.jpeg", 1.0, 1.0); // Grass
                    } else if (map[x][y].getType() == 1) {
                        StdDraw.picture(x, rows-1-y, "./misc/sandTile.png", 1.0, 1.0); // Sand
                    } else {
                        StdDraw.picture(x, rows-1-y, "./misc/impassableTile.jpeg", 1.0, 1.0); // Obstacle
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
     * Visualizes a step in the path.
     * @param to The tile to move to
     */
    private static void drawStep(Tile to) {
        drawMap();
        drawGoldCoin();
        StdDraw.setPenColor(StdDraw.RED);
        // Draw a path dot at the previous positions
        for (Tile tile: visitedTiles) {
            StdDraw.filledCircle(tile.getColumn(), rows - 1 - tile.getRow(), 0.15);
        }
        // Draw the knight at the new position
        drawKnight(to.getColumn(), to.getRow());
        StdDraw.show();
    }

    /**
     * Visualizes reaching an objective.
     * @param tile The tile where the objective is reached
     */
    private static void drawObjectiveReached(Tile tile) {
        StdDraw.clear();
        drawMap();
        drawGoldCoin();
        drawKnight(tile.getColumn(), tile.getRow());
        StdDraw.show();
    }
}