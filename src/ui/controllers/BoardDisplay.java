import java.util.HashMap;
import processing.core.PImage;

public class BoardDisplay extends Controller<BoardDisplay> {

  private Board board;
  private int caseWidth;
  private int pov = Player.White;
  private HashMap<Integer, PImage> imgs;

  public BoardDisplay(Main sketch, float x, float y, int caseWidth, Board board) {
    this.sketch = sketch;
    this.board = board;
    this.caseWidth = caseWidth;
    this.x = (int)x;
    this.y = (int)y;
    this.w = caseWidth*8;
    this.h = caseWidth*8;

    initImages();
  }

  public void setPov(int pov) {
    this.pov = pov;
  }

  public void show() {
    sketch.rectMode(sketch.CORNER);
    sketch.imageMode(sketch.CENTER);
    sketch.noStroke();

    // Affiche le plateau
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if ((i+j) % 2 == 0) sketch.fill(rgb(240, 217, 181)); // Case blanche
        else sketch.fill(rgb(181, 136, 99)); // Case noire

        sketch.rect(x - w/2 + i * caseWidth, y - h/2 + j * caseWidth, caseWidth, caseWidth);
      }
    }

    // Affiche les pièces
    for (int n = 0; n < 2; n++) {
      for (Piece p : board.pieces(n)) {
        if (p != null) {
          int i = (pov == Player.White ? p.i : 7-p.i);
          int j = (pov == Player.White ? p.j : 7-p.j);
          float piecex = x - w/2 + j*caseWidth + caseWidth/2;
          float piecey = y - h/2 + i*caseWidth + caseWidth/2;
          sketch.image(imgs.get(p.index), piecex, piecey, caseWidth, caseWidth);
        }
      }
    }
  }

  public void onUserEvent(UserEvent e) {
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
}
