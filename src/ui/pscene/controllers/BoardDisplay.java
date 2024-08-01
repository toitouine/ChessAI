import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import processing.core.PImage;
import processing.core.PShape;

public class BoardDisplay extends Controller<BoardDisplay> {

  private Board board;
  private int caseWidth;
  private int pov = Player.White;
  private HashMap<Integer, PImage> imgs;

  // Dessin sur le plateau
  private Integer lastSquareRightClicked = null;
  private Integer moveMarkFrom = null;
  private Integer moveMarkTo = null;
  private Arrow drawingArrow = null;
  private ArrayList<Integer> rouges = new ArrayList<Integer>();
  private ArrayList<Integer> jaunes = new ArrayList<Integer>();
  private ArrayList<Arrow> arrows = new ArrayList<Arrow>();

  // Sélection d'un coup
  private Integer squareSelected = null;
  private Move moveSelected = null;
  private boolean dragging = false;
  private boolean inPromotion = false;
  private Integer endSquarePromotion = null;
  private SyncBoolean askingMove = new SyncBoolean(false);
  private ArrayList<Move> possibleMoves = new ArrayList<Move>();
  private ArrayList<ArrayList<ImageButton>> promoButtons = new ArrayList<ArrayList<ImageButton>>();

  public BoardDisplay(SApplet sketch, float x, float y, int caseWidth) {
    this.sketch = sketch;
    this.caseWidth = caseWidth;
    this.x = (int)x;
    this.y = (int)y;
    this.w = caseWidth*8;
    this.h = caseWidth*8;

    initImages();

    for (int i = 0; i < 2; i++) {
      promoButtons.add(new ArrayList<ImageButton>());
      ArrayList<ImageButton> list = promoButtons.get(i);
      list.add(new ImageButton(sketch, x-3*caseWidth, y, 1.5f*caseWidth, 1.5f*caseWidth, imgs.get(i * Piece.Number + 1))
                 .setAction(() -> selectPromotion(MoveFlag.PromotionDame)));
      list.add(new ImageButton(sketch, x-caseWidth, y, 1.5f*caseWidth, 1.5f*caseWidth, imgs.get(i * Piece.Number + 2))
                 .setAction(() -> selectPromotion(MoveFlag.PromotionTour)));
      list.add(new ImageButton(sketch, x+caseWidth, y, 1.5f*caseWidth, 1.5f*caseWidth, imgs.get(i * Piece.Number + 3))
                 .setAction(() -> selectPromotion(MoveFlag.PromotionFou)));
      list.add(new ImageButton(sketch, x+3*caseWidth, y, 1.5f*caseWidth, 1.5f*caseWidth, imgs.get(i * Piece.Number + 4))
                 .setAction(() -> selectPromotion(MoveFlag.PromotionCavalier)));
    }
  }

  public void setBoard(Board b) {
    board = b;
    deselectAll();
  }

  public void setPov(int p) {
    pov = p;
  }

  public Move getMove() {
    moveSelected = null;
    squareSelected = null;
    endSquarePromotion = null;
    dragging = false;
    inPromotion = false;
    possibleMoves.clear();
    askingMove.set(true);
    synchronized (askingMove) {
      try {
        while (askingMove.get()) askingMove.wait();
        deselectAll();
        return moveSelected;
      } catch (Exception e) {
        Debug.error("Erreur pendant la récupération du coup d'un humain.");
        deselectAll();
        return board.getLegalMoves().get(0);
      }
    }
  }

  public void stopAskMove() {
    inPromotion = false;
    askingMove.set(false);
  }

  public void show() {
    if (board == null) return;

    sketch.push();
    sketch.translate(x-w/2, y-h/2);

    sketch.rectMode(sketch.CORNER);
    sketch.imageMode(sketch.CENTER);

    // Affiche le plateau et les pièces
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        sketch.noStroke();
        if ((i+j) % 2 == 0) sketch.fill(rgb(240, 217, 181)); // Case blanche
        else sketch.fill(rgb(181, 136, 99)); // Case noire

        int square = 8*i + j;
        float casex, casey;
        if (pov == Player.White) {
          casex = j * caseWidth;
          casey = i * caseWidth;
        } else {
          casex = (7-j) * caseWidth;
          casey = (7-i) * caseWidth;
        }

        sketch.rect(casex, casey, caseWidth, caseWidth);

        if (rouges.contains(square)) {
          sketch.fill(224, 76, 56, 230);
          sketch.rect(casex, casey, caseWidth, caseWidth);
        }
        else if (jaunes.contains(square)) {
          sketch.fill(235, 214, 35, 230);
          sketch.rect(casex, casey, caseWidth, caseWidth);
        }
        else if (moveMarkFrom != null && moveMarkFrom == square) {
          sketch.fill(209, 206, 25, 100);
          sketch.rect(casex, casey, caseWidth, caseWidth);
        }
        else if (moveMarkTo != null && moveMarkTo == square) {
          sketch.fill(209, 206, 25, 100);
          sketch.rect(casex, casey, caseWidth, caseWidth);
        }
        else if (squareSelected != null && squareSelected == square) {
          sketch.fill(189, 186, 34, 100);
          sketch.rect(casex, casey, caseWidth, caseWidth);
        }

        Move move = getMoveFromTarget(square);
        if (move != null && move.endSquare() == square) {
          int w = caseWidth;
          if (board.grid(i, j) == null && move.flag() != MoveFlag.EnPassant) {
            sketch.fill(75, 75, 75, 100);
            sketch.ellipse(casex + w/2, casey + w/2, w/4, w/4);
          } else {
            sketch.noFill();
            sketch.stroke(75, 75, 75, 100);
            sketch.strokeWeight(w/16);
            sketch.ellipse(casex + w/2, casey + w/2, w - w/16, w - w/16);
          }
        }

        if (dragging && square == squareSelected) continue;
        if (board.grid(square) != null) {
          sketch.image(imgs.get(board.grid(square).index), casex+caseWidth/2, casey+caseWidth/2, caseWidth, caseWidth);
        }
      }
    }

    sketch.pop();

    for (Arrow arrow : arrows) arrow.show();
    if (dragging) {
      PImage img = imgs.get(board.grid(squareSelected).index);
      sketch.image(img, sketch.mouseX, sketch.mouseY, caseWidth, caseWidth);
    }

    if (inPromotion) {
      sketch.rectMode(sketch.CENTER);
      sketch.fill(220, 220, 220, 220);
      sketch.rect(x, y, w, h);

      for (ImageButton b : promoButtons.get(board.tourDeQui)) {
        b.show();
      }
    }
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
    // S'occupe des boutons de promotion
    if (inPromotion) {
      for (ImageButton b : promoButtons.get(board.tourDeQui)) {
        b.onUserEvent(e);
        if (b.contains(e.x, e.y)) return;
      }
      if (e.mouseMoved()) sketch.cursor(sketch.ARROW);
      return;
    }

    // S'assure que le plateau est bien concerné
    if (e.keyPressed() || board == null) return;
    if (!contains(e.x, e.y)) {
      if (e.mouseReleased()) {
        lastSquareRightClicked = null;
        squareSelected = null;
        dragging = false;
        possibleMoves.clear();
      }
      return;
    }

    // Récupère la case et la pièce concernée
    int i = getGridLine(e.x, e.y);
    int j = getGridColumn(e.x, e.y);
    if (i < 0 || i > 7 || j < 0 || j > 7) return;

    int square = 8*i + j;
    Piece piece = board.grid(square);

    // Si la souris bouge, actualise le curseur
    if (e.mouseMoved() && askingMove.get()) {
      if (piece != null && piece.color == board.tourDeQui) sketch.cursor(sketch.HAND);
      else sketch.cursor(sketch.ARROW);
    }

    // Dans le cas d'un clic, sélectionne une pièce ou déselectionne
    else if (e.mousePressed()) {
      if (sketch.mouseButton == sketch.LEFT) {
        if (squareSelected == null && (piece == null || piece.color != board.tourDeQui)) {
          deselectAll();
        }
        else if (askingMove.get()) {
          if (piece != null && piece.color == board.tourDeQui) {
            squareSelected = square;
            possibleMoves = getMoves(squareSelected);
          } else {
            trySelectMove(square);
          }
        }
      }
      else if (sketch.mouseButton == sketch.RIGHT) {
        lastSquareRightClicked = square;
      }
    }

    // Si la souris glisse, fait glisser une pièce (et curseur) TODO ou dessine une flèche
    else if (e.mouseDragged()) {
      if (sketch.mouseButton == sketch.LEFT && squareSelected != null) {
        if (!dragging) dragging = true;
      }
      else if (lastSquareRightClicked != null && sketch.mouseButton == sketch.RIGHT) {
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
    }

    // Si la souris est relachée, dessine une flèche ou joue un coup si la souris glissait
    else if (e.mouseReleased()) {
      if (sketch.mouseButton == sketch.LEFT && askingMove.get() && dragging) {
        trySelectMove(square);
        dragging = false;
      }
      else if (sketch.mouseButton == sketch.RIGHT) {
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

  public void setMoveMark(Integer from, Integer to) {
    moveMarkFrom = from;
    moveMarkTo = to;
  }

  private synchronized ArrayList<Move> getMoves(int square) {
    // Si le coup est une promotion, ne garde que la promotion en dame
    ArrayList<Move> moves = board.getSquareMoves(square);
    moves.removeIf(
      m -> MoveFlag.isPromotion(m.flag()) && m.flag() != MoveFlag.PromotionDame
    );
    return moves;
  }

  private synchronized Move getMoveFromTarget(int target) {
    List<Move> match = possibleMoves.stream()
      .filter(m -> m.endSquare() == target)
      .collect(Collectors.toList());
    if (match.size() != 0) return match.get(0);

    return null;
  }

  private synchronized void trySelectMove(int square) {
    Move move = getMoveFromTarget(square);
    boolean isPromotion = move != null && MoveFlag.isPromotion(move.flag());
    if (move != null) {
      sketch.cursor(sketch.ARROW);
      if (isPromotion) {
        inPromotion = true;
        endSquarePromotion = square;
      } else {
        moveSelected = move;
        askingMove.set(false);
      }
    } else if (square != squareSelected) {
      possibleMoves.clear();
      squareSelected = null;
    }
  }

  private synchronized void selectPromotion(int flag) {
    Move move = new Move(squareSelected, endSquarePromotion, flag);
    moveSelected = move;
    askingMove.set(false);
  }

  private void deselectAll() {
    rouges.clear();
    jaunes.clear();
    arrows.clear();
    lastSquareRightClicked = null;
    squareSelected = null;
    endSquarePromotion = null;
    inPromotion = false;
    dragging = false;
    possibleMoves.clear();
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

    public Arrow(int i, int j, int ti, int tj) {
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

    private void createArrow() {
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

    public void show() {
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

    public boolean equals(Arrow other) {
      return(i == other.i && j == other.j && deltaI == other.deltaI && deltaJ == other.deltaJ);
    }
  }
}
