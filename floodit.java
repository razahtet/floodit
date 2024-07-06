import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
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

class FloodItWorld extends World {
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
  TextImage result = new TextImage("", 25, Color.BLACK);
  ArrayList<Integer> bestMovesColorCount = new ArrayList<Integer>();
  ArrayList<Cell> currentF = new ArrayList<Cell>();
  ArrayList<Cell> alreadyF = new ArrayList<Cell>();

  FloodItWorld(int boardSize, int colorNum) {
    this.boardSize = boardSize;
    this.colorNum = colorNum;
    this.initGame();
  }

  public void initGame() {
    this.clicks = 0;
    this.boardSize = this.modes.get(this.modeNum);
    this.mClicks = ((this.boardSize * this.colorNum) / 2) - ((this.boardSize * colorNum) / 5);
    this.result = new TextImage("", 25, Color.BLACK);
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

  public WorldScene makeScene() {
    WorldScene s = new WorldScene(0, 0);
    for (int i = 0; i < board.size(); i++) {
      Cell daCell = this.board.get(i);
      RectangleImage cell = new RectangleImage(20, 20, OutlineMode.SOLID, daCell.color);
      s.placeImageXY(cell, daCell.x, daCell.y);
    }
    s.placeImageXY(new TextImage("Size: " + this.boardSize, 25, Color.BLACK), this.boardSize * 10,
        this.boardSize * 20 + 20);
    s.placeImageXY(new TextImage("Colors: " + this.colorNum, 25, Color.BLACK), this.boardSize * 10,
        this.boardSize * 20 + 50);
    s.placeImageXY(new TextImage(this.clicks + "/" + this.mClicks, 25, Color.BLACK),
        this.boardSize + (20 * (this.boardSize / 2)), this.boardSize * 20 + 80);
    s.placeImageXY(result, this.boardSize * 10, this.boardSize * 20 + 110);
    return s;
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
      this.result = new TextImage("You lose.", 25, Color.RED);
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
        this.result = new TextImage("You win!", 25, Color.GREEN);
      } else if (this.clicks == this.mClicks) {
        this.result = new TextImage("You lose.", 25, Color.RED);
      }
    }
  }

  public void onMouseClicked(Posn pos, String buttonName) {
    if (buttonName.equals("LeftButton")) {
      Cell gotClicked = null;
      for (int i = 0; i < board.size(); i++) {
        Cell daCell = this.board.get(i);
        if (pos.x >= daCell.x - 11 && pos.x < daCell.x + 9 && pos.y >= daCell.y - 11
            && pos.y < daCell.y + 9) {
          gotClicked = daCell;
          break;
        }
      }
      if (gotClicked != null) {
        Color newColor = gotClicked.color;
        if (newColor != this.lastColor) {
          for (int i = 0; i < board.size(); i++) {
            Cell daCell = this.board.get(i);
            if (daCell.flooded) {
              ArrayList<Cell> adjacent = new ArrayList<Cell>(
                  Arrays.asList(daCell.left, daCell.right, daCell.top, daCell.bottom));
              for (int j = 0; j < adjacent.size(); j++) { // write cleaner code here later
                if (adjacent.get(j) != null && !adjacent.get(j).flooded
                    && adjacent.get(j).color == newColor) {
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
  }

  public void onKeyEvent(String key) {
    if (key.equalsIgnoreCase("r")) {
      this.initGame();
    } else if (key.equalsIgnoreCase("s")) {
      this.changeSize();
    } else if (key.equalsIgnoreCase("c")) {
      this.changeColors();
    }
  }
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

class ExamplesFlood {
  void testBigBang(Tester t) {
    FloodItWorld w = new FloodItWorld(10, 3);
    w.bigBang(750, 750, 0.01);
  }
}
