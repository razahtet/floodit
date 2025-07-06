package src;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
}

class FloodItPanel extends JPanel implements ActionListener, MouseListener, KeyListener {
  // All the cells of the game
  ArrayList<Cell> board;
  int boardSize = 10;
  int colorNum = 3;
  Color lastColor;
  int clicks = 0;
  int mClicks = 0;
  Utils u = new Utils();
  int modeNum = 1;
  ArrayList<Integer> modes = new ArrayList<Integer>(Arrays.asList(9, 10, 14, 20, 22, 24, 28));
  String resultText = "";
  Color resultColor = Color.BLACK;
  ArrayList<Cell> currentF = new ArrayList<Cell>();
  ArrayList<Cell> alreadyF = new ArrayList<Cell>();
  Timer timer;
  
  // Hint system variables
  boolean showHint = false;
  Color hintColor = null;
  String hintText = "";

  FloodItPanel(int boardSize, int colorNum) {
    this.boardSize = boardSize;
    this.colorNum = colorNum;
    this.setPreferredSize(new Dimension(750, 750));
    this.addMouseListener(this);
    this.addKeyListener(this);
    this.setFocusable(true);
    this.timer = new javax.swing.Timer(10, this);
    this.initGame();
    this.timer.start();
  }

  public void initGame() {
    this.clicks = 0;
    this.boardSize = this.modes.get(this.modeNum);
    this.mClicks = ((this.boardSize * this.colorNum) / 2) - ((this.boardSize * colorNum) / 5);
    this.resultText = "";
    this.resultColor = Color.BLACK;
    this.alreadyF = new ArrayList<Cell>();
    this.currentF = new ArrayList<Cell>();
    this.board = new ArrayList<Cell>();
    for (int i = 0; i < this.boardSize; i++) {
      for (int j = 0; j < this.boardSize; j++) {
        Cell daCell = new Cell();
        daCell.x = i * 20 + 11;
        daCell.y = j * 20 + 11;
        daCell.color = this.u.getRandomColor(this.colorNum);
        if (i == 0 && j == 0) {
          daCell.flooded = true;
          this.lastColor = daCell.color;
        } else {
          daCell.flooded = false;
        }
        if (j - 1 >= 0) {
          Cell theTop = this.board.get((j - 1) + (this.boardSize * i));
          daCell.top = theTop;
          theTop.bottom = daCell;
        }
        if (i - 1 >= 0) {
          Cell theLeft = this.board.get(this.boardSize * (i - 1) + j);
          daCell.left = theLeft;
          theLeft.right = daCell;
        }
        board.add(daCell);
      }
    }
    // Update hint after game initialization
    updateHint();
  }

  public void changeSize() {
    if (this.modeNum == this.modes.size() - 1) {
      this.modeNum = 0;
    } else {
      this.modeNum++;
    }
    this.initGame();
  }

  public void changeColors() {
    if (this.colorNum == 8) {
      this.colorNum = 3;
    } else {
      this.colorNum++;
    }
    this.initGame();
  }

  // Update hint based on current board state
  public void updateHint() {
    // Only provide hints for boards with size <=10 and <=5 colors
    if (this.boardSize <= 10 && this.colorNum <= 5) {
      this.hintColor = findBestMove();
      if (this.hintColor != null) {
        this.hintText = "Hint: Click " + getColorName(this.hintColor);
      } else {
        this.hintText = "No hint available";
      }
    } else {
      this.hintColor = null;
      this.hintText = "Hints: boards \u2264 10x10, colors \u2264 5";
    }
  }

  // Find the best color to click next using a greedy approach
  public Color findBestMove() {
    if (isGameWon()) {
      return null;
    }

    Color bestColor = null;
    int maxNewCells = 0;

    // Try each available color
    ArrayList<Color> availableColors = getAvailableColors();
    for (Color testColor : availableColors) {
      if (!testColor.equals(this.lastColor)) {
        int newCells = countNewCellsIfColored(testColor);
        if (newCells > maxNewCells) {
          maxNewCells = newCells;
          bestColor = testColor;
        }
      }
    }

    return bestColor;
  }

  // Get all colors currently used on the board
  public ArrayList<Color> getAvailableColors() {
    ArrayList<Color> colors = new ArrayList<Color>();
    for (int i = 0; i < this.colorNum; i++) {
      colors.add(this.u.colorList.get(i));
    }
    return colors;
  }

  // Count how many new cells would be flooded if we choose this color
  public int countNewCellsIfColored(Color testColor) {
    int count = 0;
    HashSet<Cell> visited = new HashSet<Cell>();
    
    // Find all currently flooded cells
    ArrayList<Cell> floodedCells = new ArrayList<Cell>();
    for (Cell cell : this.board) {
      if (cell.flooded) {
        floodedCells.add(cell);
      }
    }

    // Check adjacent cells that would become flooded
    Queue<Cell> queue = new LinkedList<Cell>();
    for (Cell floodedCell : floodedCells) {
      ArrayList<Cell> adjacent = new ArrayList<Cell>(Arrays.asList(
          floodedCell.left, floodedCell.right, floodedCell.top, floodedCell.bottom));
      for (Cell adj : adjacent) {
        if (adj != null && !adj.flooded && adj.color.equals(testColor) && !visited.contains(adj)) {
          queue.add(adj);
          visited.add(adj);
          count++;
        }
      }
    }

    // Continue flooding simulation
    while (!queue.isEmpty()) {
      Cell current = queue.poll();
      ArrayList<Cell> adjacent = new ArrayList<Cell>(Arrays.asList(
          current.left, current.right, current.top, current.bottom));
      for (Cell adj : adjacent) {
        if (adj != null && !adj.flooded && adj.color.equals(testColor) && !visited.contains(adj)) {
          queue.add(adj);
          visited.add(adj);
          count++;
        }
      }
    }

    return count;
  }

  // Check if the game is won
  public boolean isGameWon() {
    for (Cell cell : this.board) {
      if (!cell.flooded) {
        return false;
      }
    }
    return true;
  }

  // Get a readable name for a color
  public String getColorName(Color color) {
    if (color.equals(Color.CYAN)) return "CYAN";
    if (color.equals(Color.GREEN)) return "GREEN";
    if (color.equals(Color.RED)) return "RED";
    if (color.equals(Color.ORANGE)) return "ORANGE";
    if (color.equals(Color.YELLOW)) return "YELLOW";
    if (color.equals(Color.MAGENTA)) return "MAGENTA";
    if (color.equals(Color.PINK)) return "PINK";
    if (color.equals(Color.DARK_GRAY)) return "DARK_GRAY";
    return "UNKNOWN";
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    // First, draw all the cells
    for (int i = 0; i < board.size(); i++) {
      Cell daCell = this.board.get(i);
      g.setColor(daCell.color);
      g.fillRect(daCell.x - 10, daCell.y - 10, 20, 20);
    }
    
    // Then, draw hint borders on top to avoid overlap issues
    if (this.showHint && this.hintColor != null) {
      // Use a thin stroke for clean, non-overlapping borders
      Graphics2D g2d = (Graphics2D) g;
      Stroke originalStroke = g2d.getStroke();
      g2d.setStroke(new BasicStroke(1.5f));
      g2d.setColor(Color.WHITE);
      
      for (int i = 0; i < board.size(); i++) {
        Cell daCell = this.board.get(i);
        if (daCell.color.equals(this.hintColor)) {
          // Draw border inside the cell boundaries to prevent overlap
          g2d.drawRect(daCell.x - 9, daCell.y - 9, 18, 18);
        }
      }
      
      // Restore original stroke
      g2d.setStroke(originalStroke);
    }
    
    // Calculate board dimensions and center position for text alignment
    int boardWidth = this.boardSize * 20;
    int boardStartX = 1;  // Board starts at x=1 (first cell at x=11, minus 10 for cell width)
    int boardCenterX = boardStartX + (boardWidth / 2);
    int boardEndY = this.boardSize * 20 + 20;
    
    // Set up font and get font metrics for centering
    g.setColor(Color.BLACK);
    g.setFont(new Font("Arial", Font.PLAIN, 25));
    FontMetrics fm = g.getFontMetrics();
    
    // Center the main game info text relative to the board
    String sizeText = "Size: " + this.boardSize;
    String colorsText = "Colors: " + this.colorNum;
    String turnsText = this.clicks + "/" + this.mClicks;
    
    int sizeTextWidth = fm.stringWidth(sizeText);
    int colorsTextWidth = fm.stringWidth(colorsText);
    int turnsTextWidth = fm.stringWidth(turnsText);
    
    g.drawString(sizeText, boardCenterX - (sizeTextWidth / 2), boardEndY + 20);
    g.drawString(colorsText, boardCenterX - (colorsTextWidth / 2), boardEndY + 50);
    g.drawString(turnsText, boardCenterX - (turnsTextWidth / 2), boardEndY + 80);
    
    // Center the result text relative to the board
    g.setColor(this.resultColor);
    if (!this.resultText.isEmpty()) {
      int resultTextWidth = fm.stringWidth(this.resultText);
      g.drawString(this.resultText, boardCenterX - (resultTextWidth / 2), boardEndY + 110);
    }
    
    // Display hint text - fix the logic for when hints are not available
    boolean hintsAvailable = (this.boardSize <= 10 && this.colorNum <= 5);
    
    if (hintsAvailable) {
      if (this.showHint) {
        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics hintFm = g.getFontMetrics();
        int hintTextWidth = hintFm.stringWidth(this.hintText);
        g.drawString(this.hintText, boardCenterX - (hintTextWidth / 2), boardEndY + 140);
        
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics hideFm = g.getFontMetrics();
        String hideText = "Press 'H' to hide hints";
        int hideTextWidth = hideFm.stringWidth(hideText);
        g.drawString(hideText, boardCenterX - (hideTextWidth / 2), boardEndY + 165);
      } else {
        g.setColor(Color.GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics showFm = g.getFontMetrics();
        String showText = "Press 'H' to show hints";
        int showTextWidth = showFm.stringWidth(showText);
        g.drawString(showText, boardCenterX - (showTextWidth / 2), boardEndY + 140);
      }
    } else {
      // When hints are not available, show the restriction message
      g.setColor(Color.GRAY);
      g.setFont(new Font("Arial", Font.PLAIN, 16));
      FontMetrics restrictFm = g.getFontMetrics();
      String restrictText = "Hints: boards \u2264 10x10, colors \u2264 5";
      int restrictTextWidth = restrictFm.stringWidth(restrictText);
      
      // Smart positioning: keep board-centered but prevent cutoff
      int panelWidth = this.getWidth();
      int idealX = boardCenterX - (restrictTextWidth / 2);
      int textX;
      
      // Check if board-centered position would cause cutoff
      if (idealX < 5) {
        // Text would be cut off on the left, shift right just enough
        textX = 5;
      } else if (idealX + restrictTextWidth > panelWidth - 5) {
        // Text would be cut off on the right, shift left just enough
        textX = panelWidth - restrictTextWidth - 5;
      } else {
        // Text fits fine with board centering
        textX = idealX;
      }
      g.drawString(restrictText, textX, boardEndY + 140);
    }
  }

  public void flood(ArrayList<Cell> cellAr) {
    ArrayList<Cell> F2 = new ArrayList<Cell>();
    int gotColored = 0;
    for (int i = 0; i < cellAr.size(); i++) {
      if (cellAr.get(i) != null && cellAr.get(i).flooded
          && !this.alreadyF.contains(cellAr.get(i))) {
        gotColored++;
        this.alreadyF.add(cellAr.get(i));
        cellAr.get(i).color = this.lastColor;
        ArrayList<Cell> adjacent = new ArrayList<Cell>(Arrays.asList(cellAr.get(i).left,
            cellAr.get(i).right, cellAr.get(i).top, cellAr.get(i).bottom));
        F2.addAll(adjacent);
      }
    }
    if (gotColored == 0) {
      this.currentF = new ArrayList<Cell>();
      this.alreadyF = new ArrayList<Cell>();
    } else {
      this.currentF = F2;
    }
  }

  public void onTick() {
    if (this.currentF.size() > 0) {
      this.flood(this.currentF);
    }
    if (this.clicks > this.mClicks) {
      this.resultText = "You lose.";
      this.resultColor = Color.RED;
    } else {
      boolean allSame = true;
      for (int i = 0; i < board.size(); i++) {
        Cell daCell = this.board.get(i);
        if (!daCell.flooded) {
          allSame = false;
          break;
        }
      }
      if (allSame) {
        this.resultText = "You win!";
        this.resultColor = Color.GREEN;
      } else if (this.clicks == this.mClicks) {
        this.resultText = "You lose.";
        this.resultColor = Color.RED;
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    this.onTick();
    this.repaint();
  }

  public void mouseClicked(MouseEvent e) {
    int posX = e.getX();
    int posY = e.getY();
    Cell gotClicked = null;
    for (int i = 0; i < board.size(); i++) {
      Cell daCell = this.board.get(i);
      if (posX >= daCell.x - 10 && posX < daCell.x + 10 && posY >= daCell.y - 10
          && posY < daCell.y + 10) {
        gotClicked = daCell;
        break;
      }
    }
    if (gotClicked != null) {
      Color newColor = gotClicked.color;
      if (!newColor.equals(this.lastColor)) {
        for (int i = 0; i < board.size(); i++) {
          Cell daCell = this.board.get(i);
          if (daCell.flooded) {
            ArrayList<Cell> adjacent = new ArrayList<Cell>(
                Arrays.asList(daCell.left, daCell.right, daCell.top, daCell.bottom));
            for (int j = 0; j < adjacent.size(); j++) {
              if (adjacent.get(j) != null && !adjacent.get(j).flooded
                  && adjacent.get(j).color.equals(newColor)) {
                adjacent.get(j).flooded = true;
              }
            }
          }
        }
        this.lastColor = newColor;
        this.alreadyF = new ArrayList<Cell>();
        this.currentF = new ArrayList<Cell>(Arrays.asList(this.board.get(0)));
        this.clicks++;
        // Update hint after each move
        updateHint();
      }
    }
  }

  public void mousePressed(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}

  public void keyPressed(KeyEvent e) {
    char key = e.getKeyChar();
    if (key == 'r' || key == 'R') {
      this.initGame();
    } else if (key == 's' || key == 'S') {
      this.changeSize();
    } else if (key == 'c' || key == 'C') {
      this.changeColors();
    } else if (key == 'h' || key == 'H') {
      this.showHint = !this.showHint;
    }
    this.repaint();
  }

  public void keyTyped(KeyEvent e) {}
  public void keyReleased(KeyEvent e) {}

}

class Utils {
  ArrayList<Color> colorList = new ArrayList<Color>(Arrays.asList(Color.CYAN, Color.GREEN,
      Color.RED, Color.ORANGE, Color.YELLOW, Color.MAGENTA, Color.PINK, Color.DARK_GRAY));

  Utils() {
  }

  public Color getRandomColor(int num) {
    Random rand = new Random();
    int randNum = rand.nextInt(num);
    return this.colorList.get(randNum);
  }
}

class FloodIt {
  public static void main(String[] args) {
    FloodItPanel panel = new FloodItPanel(10, 3);
    JFrame frame = new JFrame("Flood It");
    frame.add(panel);
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
}
