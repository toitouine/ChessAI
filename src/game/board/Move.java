public class Move {
  public byte fromI, fromJ; // Case de départ
  public byte i, j; // Case d'arrivée
  public Piece piece; // Pièce capturée (ou pas)
  public Piece capture; // Pièce capturée (ou pas)
  public Flag flag; // Coup spécial

  public Move(Piece piece, int i, int j, Piece capture, Flag flag) {
    this.piece = piece;
    this.i = (byte) i;
    this.j = (byte) j;
    this.capture = capture;
    this.flag = flag;
  }

  public Move(Piece piece, int i, int j, Piece capture) {
    this(piece, i, j, capture, Flag.None);
  }

  public Move(Piece piece, int i, int j, Flag flag) {
    this(piece, i, j, null, flag);
  }

  public Move(Piece piece, int i, int j) {
    this(piece, i, j, null, Flag.None);
  }
}

enum Flag {
  None,
  DoubleAvance,
  EnPassant,
  PetitRoque,
  GrandRoque,
  PromotionDame,
  PromotionCavalier,
  PromotionTour,
  PromotionFou
}
