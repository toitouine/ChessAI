import java.util.HashMap;
import java.util.ArrayList;
import processing.core.PImage;
import processing.core.PShape;

public class BoardDisplay extends Controller<BoardDisplay> {

  private Board board;
  private int caseWidth;
  private int pov = Player.White;
  private HashMap<Integer, PImage> imgs;

  private ArrayList<Integer> rouges = new ArrayList<Integer>();
  private ArrayList<Integer> jaunes = new ArrayList<Integer>();
  private ArrayList<Arrow> arrows = new ArrayList<Arrow>();

  private Integer lastSquareRightClicked = null;
  private Arrow drawingArrow = null;

  public BoardDisplay(SApplet sketch, float x, float y, int caseWidth) {
    this.sketch = sketch;
    this.caseWidth = caseWidth;
    this.x = (int)x;
    this.y = (int)y;
    this.w = caseWidth*8;
    this.h = caseWidth*8;

    initImages();
  }

  public void setBoard(Board b) {
    board = b;
    deselectAll();
  }

  public void setPov(int p) {
    pov = p;
  }

  public void addArrow(Arrow arrow) {
    arrows.add(arrow);
  }

  public void show() {
    if (board == null) return;

    sketch.push();
    sketch.translate(x-w/2, y-h/2);

    sketch.rectMode(sketch.CORNER);
    sketch.imageMode(sketch.CENTER);
    sketch.noStroke();

    // Affiche le plateau
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if ((i+j) % 2 == 0) sketch.fill(rgb(240, 217, 181)); // Case blanche
        else sketch.fill(rgb(181, 136, 99)); // Case noire

        float casex, casey;
        if (pov == Player.White) {
          casex = j * caseWidth;
          casey = i * caseWidth;
        } else {
          casex = (7-j) * caseWidth;
          casey = (7-i) * caseWidth;
        }

        sketch.rect(casex, casey, caseWidth, caseWidth);

        if (rouges.contains(8*i+j)) {
          sketch.fill(224, 76, 56, 230);
          sketch.rect(casex, casey, caseWidth, caseWidth);
        }
        else if (jaunes.contains(8*i+j)) {
          sketch.fill(235, 214, 35, 230);
          sketch.rect(casex, casey, caseWidth, caseWidth);
        }
      }
    }

    // Affiche les pièces
    for (int n = 0; n < 2; n++) {
      for (Piece p : board.pieces(n)) {
        if (p != null) {
          int i = (pov == Player.White ? p.square/8 : 7-(p.square/8));
          int j = (pov == Player.White ? p.square%8 : 7-(p.square%8));
          float piecex = j*caseWidth + caseWidth/2;
          float piecey = i*caseWidth + caseWidth/2;
          sketch.image(imgs.get(p.index), piecex, piecey, caseWidth, caseWidth);
        }
      }
    }

    sketch.pop();

    for (Arrow arrow : arrows) arrow.show();
  }

  private void toggleYellow(Integer s) {
    if (rouges.contains(s)) rouges.remove(s);
    if (jaunes.contains(s)) jaunes.remove(s);
    else jaunes.add(s);
  }

  private void toggleRed(Integer s) {
    if (jaunes.contains(s)) jaunes.remove(s);
    if (rouges.contains(s)) rouges.remove(s);
    else rouges.add(s);
  }

  private int getGridLine(int mx, int my) {
    if (pov == Player.White) return 7+(int)(my-y-h/2)/caseWidth;
    else return -(int)(my-y-h/2)/caseWidth;
  }

  private int getGridColumn(int mx, int my) {
    if (pov == Player.White) return 7+(int)(mx-x-w/2)/caseWidth;
    else return -(int)(mx-x-w/2)/caseWidth;
  }

  public void onUserEvent(UserEvent e) {
    if (board == null) return;
    if (!contains(e.x, e.y)) {
      if (e.mouseReleased()) lastSquareRightClicked = null;
      return;
    }

    int i = getGridLine(e.x, e.y);
    int j = getGridColumn(e.x, e.y);
    if (i < 0 || i > 7 || j < 0 || j > 7) return;

    int square = 8*i + j;
    Piece piece = board.grid(square);

    if (e.mouseMoved()) {
      if (piece != null && board.tourDeQui == piece.c) sketch.cursor(sketch.HAND);
      else sketch.cursor(sketch.ARROW);
    }
    else if (e.mousePressed()) {
      if (sketch.mouseButton == sketch.LEFT) {
        if (piece == null || piece.c != board.tourDeQui) deselectAll();
      }
      else if (sketch.mouseButton == sketch.RIGHT) {
        lastSquareRightClicked = square;
      }
    }
    else if (e.mouseDragged()) {
      if (lastSquareRightClicked == null || sketch.mouseButton != sketch.RIGHT) return;

      if (drawingArrow == null) drawingArrow = new Arrow(i, j, i, j);
      if (lastSquareRightClicked != square) {
        if (drawingArrow.i+drawingArrow.deltaI != i || drawingArrow.j+drawingArrow.deltaJ != j) {
          Arrow arr = new Arrow(lastSquareRightClicked/8, lastSquareRightClicked%8, i, j);
          arrows.remove(drawingArrow);
          arrows.add(arr);
          drawingArrow = arr;
        }
      }
    }
    else if (e.mouseReleased()) {
      if (sketch.mouseButton == sketch.RIGHT) {
        if (lastSquareRightClicked == null || lastSquareRightClicked == square) {
          if (sketch.keyPressed && sketch.keyCode == sketch.CONTROL) toggleYellow(square);
          else toggleRed(square);
        }
        else {
          for (int n = arrows.size()-1; n >= 0; n--) {
            if (arrows.get(n).equals(drawingArrow) && drawingArrow != arrows.get(n)) {
              arrows.remove(n);
              arrows.remove(drawingArrow);
            }
          }
          drawingArrow = null;
        }
      }

      lastSquareRightClicked = null;
    }
  }

  private void deselectAll() {
    rouges.clear();
    jaunes.clear();
    arrows.clear();
    lastSquareRightClicked = null;
  }

  private void initImages() {
    imgs = new HashMap<Integer, PImage>();

    for (int i = 0; i < 2; i++) {
      String couleur = (i == 0 ? "b" : "n");
      imgs.put(0 + i*6, sketch.loadImage("data/board/roi_" + couleur + ".png"));
      imgs.put(1 + i*6, sketch.loadImage("data/board/dame_" + couleur + ".png"));
      imgs.put(2 + i*6, sketch.loadImage("data/board/tour_" + couleur + ".png"));
      imgs.put(3 + i*6, sketch.loadImage("data/board/fou_" + couleur + ".png"));
      imgs.put(4 + i*6, sketch.loadImage("data/board/cavalier_" + couleur + ".png"));
      imgs.put(5 + i*6, sketch.loadImage("data/board/pion_" + couleur + ".png"));
    }
  }

  private class Arrow {
    private int i, j, deltaI, deltaJ;
    private float length;
    private float angle = 0;
    private PShape shape;
    private int arrowColor;

    Arrow(int i, int j, int ti, int tj) {
      this.i = i;
      this.j = j;
      deltaI = ti - i;
      deltaJ = tj - j;
      arrowColor = rgb(255, 192, 67);

      length = (float)Math.sqrt(Math.pow((deltaI*caseWidth), 2) + Math.pow((deltaJ*caseWidth), 2));
      angle = (float)Math.asin((float)deltaI*caseWidth/length);
      if (deltaJ < 0) angle = sketch.PI - angle;

      createArrow();
    }

    void createArrow() {
      shape = null;
      shape = sketch.createShape(sketch.GROUP);
      sketch.strokeWeight(5 * caseWidth/70);
      sketch.stroke(arrowColor);
      PShape line = sketch.createShape(sketch.LINE, 0, 0, length, 0);
      PShape left = sketch.createShape(sketch.LINE, length, 0, length-(15*caseWidth/70), -(15*caseWidth/70));
      PShape right = sketch.createShape(sketch.LINE, length, 0, length-(15*caseWidth/70), (15*caseWidth/70));

      shape.addChild(line);
      shape.addChild(left);
      shape.addChild(right);
    }

    void show() {
      float posx, posy;
      if (pov == Player.White) {
        posx = x - w/2 + j*caseWidth + caseWidth/2;
        posy = y - h/2 + i*caseWidth + caseWidth/2;
      } else {
        posx = x - w/2 + (7-j)*caseWidth + caseWidth/2;
        posy = y - h/2 + (7-i)*caseWidth + caseWidth/2;
      }

      sketch.push();
      sketch.translate(posx, posy);
      sketch.rotate(angle);
      if (pov != Player.White) sketch.rotate(sketch.PI);
      sketch.shape(shape);
      sketch.pop();
    }

    boolean equals(Arrow other) {
      return(i == other.i && j == other.j && deltaI == other.deltaI && deltaJ == other.deltaJ);
    }
  }
}
