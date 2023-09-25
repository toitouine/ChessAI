import java.util.ArrayList;

public abstract class Piece {

  static final int Roi = 0;
  static final int Dame = 1;
  static final int Tour = 2;
  static final int Fou = 3;
  static final int Cavalier = 4;
  static final int Pion = 5;

  public Board board; // Plateau sur lequel la pièce est présente
  public int i, j, c; // Position (i, j) et couleur (0 ou 1)
  public int type; // Type de pièce (roi, dame, pion...)
  public int index; // Index de la pièce (type et couleur)
  public float value, loicValue; // Valeur de la pièce en terme de matériel

  public void init(int t) {
    board.grid[i][j].piece = this;
    type = t;
    index = t + c*6;
    value = Config.Eval.maireValues[type];
    loicValue = Config.Eval.loicValues[type];
  }

  public void move(int toI, int toJ) {
    board.grid[i][j].piece = null;
    i = toI;
    j = toJ;
    board.grid[i][j].piece = this;
  }

  // public abstract ArrayList<Move> getMoves(); // Coups pseudo-légaux
  // public abstract ArrayList<Move> getLegalMoves(); // Coups légaux
  // public abstract ArrayList<Move> getLegalCaptures(); // Captures légales
}

class Roi extends Piece {
  public Roi(Board board, int i, int j, int c) {
    this.board = board;
    this.i = i;
    this.j = j;
    this.c = c;
    init(Piece.Roi);
  }
}

class Dame extends Piece {
  public Dame(Board board, int i, int j, int c) {
    this.board = board;
    this.i = i;
    this.j = j;
    this.c = c;
    init(Piece.Dame);
  }
}

class Tour extends Piece {
  public Tour(Board board, int i, int j, int c) {
    this.board = board;
    this.i = i;
    this.j = j;
    this.c = c;
    init(Piece.Tour);
  }
}

class Fou extends Piece {
  public Fou(Board board, int i, int j, int c) {
    this.board = board;
    this.i = i;
    this.j = j;
    this.c = c;
    init(Piece.Fou);
  }
}

class Cavalier extends Piece {
  public Cavalier(Board board, int i, int j, int c) {
    this.board = board;
    this.i = i;
    this.j = j;
    this.c = c;
    init(Piece.Cavalier);
  }
}

class Pion extends Piece {
  public Pion(Board board, int i, int j, int c) {
    this.board = board;
    this.i = i;
    this.j = j;
    this.c = c;
    init(Piece.Pion);
  }
}
