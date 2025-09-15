// name surname: Devin Isler
// student ID: 2023400063
/**
 * The Tile class for the "Gold Trail: The Knight’s Path".
 * <p>
 * This class represents an individual tile on the game map, encapsulating its coordinates,
 * type (grass, sand, or obstacle), and pathfinding attributes. It supports adjacency connections
 * and is integral to the pathfinding operations.
 * </p>
 *
 * @author Devin Isler
 * @version 1.0
 * @since 2025-05-08
 */
import java.util.ArrayList;

public class Tile {
    private int column;
    private int row;
    private int type; // 0: Grass, 1: Sand, 2: Obstacle
    private ArrayList<Tile> adjacentTiles;

    // Additional properties to help with pathfinding
    private double distance; // Distance from source
    private Tile previous;   // Previous tile in the shortest path
    private boolean visited; // Whether this tile has been visited

    /**
     * Constructs a Tile object.
     * @param column Column number of the Tile
     * @param row Row number of the Tile
     * @param type Type of the Tile (0: Grass, 1: Sand, 2: Obstacle)
     */
    public Tile(int column, int row, int type) {
        this.column = column;
        this.row = row;
        this.type = type;
        this.adjacentTiles = new ArrayList<>();

        // Initialize pathfinding properties
        this.distance = Double.POSITIVE_INFINITY;
        this.previous = null;
        this.visited = false;
    }

    /**
     * Adds an adjacent tile to this tile's list of neighbors.
     * @param tile The adjacent tile to add
     */
    public void addAdjacentTile(Tile tile) {
        if (!adjacentTiles.contains(tile)) {
            adjacentTiles.add(tile);
        }
    }

    /**
     * Gets the column number of this tile.
     * @return Column number
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the row number of this tile.
     * @return Row number
     */
    public int getRow() {
        return row;
    }

    /**
     * Gets the type of this tile.
     * @return Type of tile (0: Grass, 1: Sand, 2: Obstacle)
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the list of adjacent tiles.
     * @return ArrayList of adjacent Tile objects
     */
    public ArrayList<Tile> getAdjacentTiles() {
        return adjacentTiles;
    }

    /**
     * Gets the current distance value for pathfinding.
     * @return Distance from source
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Sets the distance value for pathfinding.
     * @param distance New distance value
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Gets the previous tile in the shortest path.
     * @return Previous Tile
     */
    public Tile getPrevious() {
        return previous;
    }

    /**
     * Sets the previous tile in the shortest path.
     * @param previous Previous Tile
     */
    public void setPrevious(Tile previous) {
        this.previous = previous;
    }

    /**
     * Checks if this tile has been visited during pathfinding.
     * @return True if visited, false otherwise
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * Sets the visited status of this tile.
     * @param visited New visited status
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * Resets the pathfinding properties of this tile.
     */
    public void resetPathfinding() {
        this.distance = Double.POSITIVE_INFINITY;
        this.previous = null;
        this.visited = false;
    }

    /**
     * Returns a string representation of this tile.
     * @return String in format "(column, row)"
     */
    @Override
    public String toString() {
        return "(" + column + ", " + row + ")";
    }

    /**
     * Checks if this tile is equal to another object.
     * @param obj Object to compare with
     * @return True if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Tile other = (Tile) obj;
        return column == other.column && row == other.row;
    }
}