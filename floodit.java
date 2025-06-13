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

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    for (int i = 0; i < board.size(); i++) {
      Cell daCell = this.board.get(i);
      g.setColor(daCell.color);
      g.fillRect(daCell.x - 10, daCell.y - 10, 20, 20);
    }
    g.setColor(Color.BLACK);
    g.setFont(new Font("Arial", Font.PLAIN, 25));
    g.drawString("Size: " + this.boardSize, this.boardSize * 10, this.boardSize * 20 + 20);
    g.drawString("Colors: " + this.colorNum, this.boardSize * 10, this.boardSize * 20 + 50);
    g.drawString(this.clicks + "/" + this.mClicks, this.boardSize + (20 * (this.boardSize / 2)), this.boardSize * 20 + 80);
    g.setColor(this.resultColor);
    g.drawString(this.resultText, this.boardSize * 10, this.boardSize * 20 + 110);
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
