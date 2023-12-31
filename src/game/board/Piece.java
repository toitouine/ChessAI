import java.util.ArrayList;

public abstract class Piece {

  static final int Roi = 0;
  static final int Dame = 1;
  static final int Tour = 2;
  static final int Fou = 3;
  static final int Cavalier = 4;
  static final int Pion = 5;

  public int square, c; // Position (index de la case) et couleur (0 ou 1)
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

  static public Piece create(int type, int square, int c) {
    switch (type) {
      case Piece.Roi: return new Roi(square, c);
      case Piece.Dame: return new Dame(square, c);
      case Piece.Tour: return new Tour(square, c);
      case Piece.Fou: return new Fou(square, c);
      case Piece.Cavalier: return new Cavalier(square, c);
      case Piece.Pion: return new Pion(square, c);
    }

    Debug.error("Type de pièce invalide");
    return null;
  }

  static public Piece promote(int promotionFlag, int square, int c) {
    switch (promotionFlag) {
      case MoveFlag.PromotionDame: return new Dame(square, c);
      case MoveFlag.PromotionCavalier: return new Cavalier(square, c);
      case MoveFlag.PromotionTour: return new Tour(square, c);
      case MoveFlag.PromotionFou: return new Fou(square, c);
    }

    Debug.error("Flag de promotion invalide");
    return null;
  }

  @Override
  public String toString() {
    return getClass().getName() + "("
           + (c == Player.White ? "b" : "n") + ", "
           + BoardUtility.caseName(square) + ")";
  }
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
