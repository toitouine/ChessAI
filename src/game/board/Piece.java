import java.io.Serializable;

public class Piece implements Serializable {

  // Type des pièces
  static final public int Roi = 0;
  static final public int Dame = 1;
  static final public int Tour = 2;
  static final public int Fou = 3;
  static final public int Cavalier = 4;
  static final public int Pion = 5;

  static final public int NumberOfType = 6; // Nombre total de type de pièce
  static final public int NumberOfPiece = 2*NumberOfType; // Nombre total de pièces différentes

  // Couleur des pièces
  static final public int White = 0;
  static final public int Black = 1;

  final public int color; // Blanc ou noir
  final public int type; // Type de la pièce (roi, dame, pion...)
  final public int index; // Index (contient les informations de type et de couleur)

  public Piece(int type, int color) {
    this.color = color;
    this.type = type;
    this.index = type + color*NumberOfType;
  }

  // Plus lent mais parfois pratique
  public Piece(int index) {
    this(index % NumberOfType, index/NumberOfType);
  }

  public char getCode() {
    return Config.Piece.codes[index];
  }

  @Override
  public String toString() {
    return String.valueOf(getCode());
  }
}
