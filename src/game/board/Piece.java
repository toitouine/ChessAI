import java.util.ArrayList;

public abstract class Piece {

  static final int Roi = 0;
  static final int Dame = 1;
  static final int Tour = 2;
  static final int Fou = 3;
  static final int Cavalier = 4;
  static final int Pion = 5;

  public int square, c; // Position (square) et couleur (0 ou 1)
  public int type; // Type de pièce (roi, dame, pion...)
  public int index; // Index de la pièce (type et couleur)
  public float value, loicValue; // Valeur de la pièce en terme de matériel

  public Piece(int square, int c, int type) {
    this.square = square;
    this.c = c;
    this.type = type;
    index = type + c*6;
    value = Config.Piece.maireValues[type];
    loicValue = Config.Piece.loicValues[type];
  }

  public void move(int destination) {
    square = destination;
  }

  // public abstract ArrayList<Move> getMoves(); // Coups pseudo-légaux
  // public abstract ArrayList<Move> getLegalMoves(); // Coups légaux
  // public abstract ArrayList<Move> getLegalCaptures(); // Captures légales
}

class Roi extends Piece {
  public Roi(int square, int c) {
    super(square, c, Piece.Roi);
  }
}

class Dame extends Piece {
  public Dame(int square, int c) {
    super(square, c, Piece.Dame);
  }
}

class Tour extends Piece {
  public Tour(int square, int c) {
    super(square, c, Piece.Tour);
  }
}

class Fou extends Piece {
  public Fou(int square, int c) {
    super(square, c, Piece.Fou);
  }
}

class Cavalier extends Piece {
  public Cavalier(int square, int c) {
    super(square, c, Piece.Cavalier);
  }
}

class Pion extends Piece {
  public Pion(int square, int c) {
    super(square, c, Piece.Pion);
  }
}
