# Gold Trail: The Knight’s Path

This project implements a grid-based pathfinding system to assist a knight in collecting gold coins placed across a map. The knight must find the shortest path to each coin, considering terrain-specific travel costs and avoiding impassable obstacles.

## Overview

The knight navigates a grid of tiles representing different terrains:  
- **Grass (Type 0):** Low cost (1–5 units)  
- **Sand (Type 1):** Medium cost (8–10 units)  
- **Obstacle (Type 2):** Impassable  

The system computes the shortest path from the knight's current position to each gold coin sequentially. Movement is restricted to four directions: up, down, left, and right. Visualization of the map and the knight’s movement is done using the StdDraw graphics library.

## Input Files

The program processes three input files:
- `mapData.txt`: Contains map dimensions and tile types.
- `travelCosts.txt`: Defines travel costs between adjacent tiles.
- `objectives.txt`: Lists the knight's starting point and subsequent gold coin coordinates.

## Output

The knight's journey is recorded in `output.txt`. Each step includes the knight's new position and accumulated cost. If an objective is unreachable, the program reports it and continues with the next one.

An additional bonus mode computes the shortest possible route that visits all objectives exactly once and returns to the starting point. This route is saved in `bonus.txt`.

## Classes and Structure

- **Tile:** Represents a single tile on the map and stores its position, type, and adjacent tiles.
- **PathFinder:** Implements the core algorithm for finding shortest paths using terrain-based cost calculations.
- **ShortestRoute (Bonus):** Finds the optimal route through all objectives (similar to the Traveling Salesman Problem).

## Features

- Dynamic visualization using StdDraw
- Object-oriented design
- Terrain-aware cost-based pathfinding
- Command-line argument handling with optional `-draw` flag
- Bonus mode for optimal path computation across all objectives

## Notes

- All unreachable objectives are skipped with an appropriate message.
- Outputs include step-by-step movement and cumulative cost.
- Bonus mode must complete execution in under three seconds.
