import java.util.ArrayList;

public class Piece {

  // Type des pièces
  static final int Roi = 0;
  static final int Dame = 1;
  static final int Tour = 2;
  static final int Fou = 3;
  static final int Cavalier = 4;
  static final int Pion = 5;

  // Couleur des pièces
  static final int White = 0;
  static final int Black = 1;

  final public int color; // Blanc ou noir
  final public int type; // Type de la pièce (roi, dame, pion...)
  final public int index; // Index (contient les informations de type et de couleur)

  public Piece(int type, int color) {
    this.color = color;
    this.type = type;
    this.index = type + color*6;
  }

  public char getCode() {
    return Config.Piece.codes[index];
  }

  @Override
  public String toString() {
    return String.valueOf(getCode());
  }
}
