/////////////////////////////////////////////////////////////////

// Représente un coup (case de départ, case d'arrivée, flag) dans le format suivant :
// 6 bits pour la case de départ, 6 bits pour la case d'arrivée, 4 bits pour le flag

// Flags :
// - Promotions (dame, tour, fou, cavalier)
// - Roques
// - En passant
// - Double avance

/////////////////////////////////////////////////////////////////

public class Move {
  final public short value;

  static private int startMask = 0b0000000000111111;
  static private int endMask =   0b0000111111000000;
  static private int flagMask =  0b1111000000000000;

  public Move(int startSquare, int endSquare, int flag) {
    value = (short)(startSquare | (endSquare << 6) | (flag << 12));
  }

  public Move(int startSquare, int endSquare) {
    value = (short)(startSquare | (endSquare << 6));
  }

  // Pour facilement créer des coups et tester des choses (ex : "e2e4", MoveFlag.None)
  // (ne pas utiliser quand il faut être rapide)
  public Move(String moveString, int flag) {
    this(BoardUtility.nameToCase(moveString.substring(0, 2)),
         BoardUtility.nameToCase(moveString.substring(2, 4)),
         flag);
  }

  public Move(String moveString) {
    this(moveString, MoveFlag.None);
  }

  public int startSquare() {
    return value & startMask;
  }

  public int endSquare() {
    return (value & endMask) >> 6;
  }

  public int flag() {
    return (value & flagMask) >> 12;
  }

  public boolean equals(Move otherMove) {
    return value == otherMove.value;
  }

  @Override
  public String toString() {
    return BoardUtility.caseName(startSquare()) + BoardUtility.caseName(endSquare())
           + (flag() != 0 ? "[" + flag() + "]" : "");
  }
}

// Flag pour les coups spéciaux
// Note : il ne peut y avoir que 16 flags maximum et les
// flags de promotion doivent être placés dans l'ordre et en dernier
final class MoveFlag {
  private MoveFlag() {}

  static final public int None = 0;
  static final public int EnPassant = 1;
  static final public int DoubleAvance = 2;
  static final public int PetitRoque = 3;
  static final public int GrandRoque = 4;
  static final public int PromotionDame = 5;
  static final public int PromotionCavalier = 6;
  static final public int PromotionTour = 7;
  static final public int PromotionFou = 8;

  static final private int[] promotionTable = {Piece.Dame, Piece.Cavalier, Piece.Tour, Piece.Fou};

  static final public boolean isPromotion(int flag) {
    return flag >= PromotionDame;
  }

  static final public int getPromotionPieceType(int promotionFlag) {
    return promotionTable[promotionFlag - PromotionDame];
  }
}
