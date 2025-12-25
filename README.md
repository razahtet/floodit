# Flood-It (Java / Swing)

Flood-It is a puzzle game where the objective is to flood the entire board to a single color before running out of moves. On each turn, the player selects a color, and the flooded region expands to adjacent cells of that color.

## Controls
- Click a square to flood the board with that color
- **R** — reset the game
- **C** — change the number of colors (restarts game; 3–8)
- **S** — change the board size (restarts game; 9, 10, 14, 20, 22, 24, 28)
- **H** — toggle hints (the color will be outlined in white)

## Implementation
- The board is represented as a grid of `Cell` objects. Each cell stores its color, flooded state, and references to its adjacent neighbors.
- Flood expansion works by repeatedly checking neighboring cells and marking them as flooded when their color matches the current flood color.

### Hint System (Greedy Approach)
- When enabled, the hint system gets all of the possible next colors and chooses the one that would flood the most new cells.
- This is done by internally simulating flood expansion from the current flooded region and counting how many additional cells would be captured.
- To avoid performance issues, hints are only available for board sizes of 10 or fewer and color counts of 5 or fewer.

## How to Run

### Windows (PowerShell)
From the project root:
```powershell
java src.FloodIt
```
