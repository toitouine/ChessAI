public final class MoveGenerationData {
  private MoveGenerationData() {}

  static final public long Afile = Bitboard.Afile;
  static final public long Bfile = Bitboard.Bfile;
  static final public long Gfile = Bitboard.Gfile;
  static final public long Hfile = Bitboard.Hfile;
  static final public long rank1 = Bitboard.rank1;
  static final public long rank2 = Bitboard.rank2;
  static final public long rank7 = Bitboard.rank7;
  static final public long rank8 = Bitboard.rank8;

  // Attaques d'un roi sur une case sous forme de bitboard
  static public long kingAttacks[];

  // Attaques d'un cavalier sur une case sous forme de bitboard
  static public long knightAttacks[];

  // Cases des pièces entre le roi et la tour pour tester la possibilité des roques
  static final public long[] petitRoquePiecesMask = {0b1100000L << 56, 0b1100000L};
  static final public long[] grandRoquePiecesMask = {0b1110L << 56, 0b1110L};

  public static void initialize() {
    generateKingAttacks();
    generateKnightAttacks();
  }

  private static void generateKnightAttacks() {
    knightAttacks = new long[64];

    for (int i = 0; i < 64; i++) {
      long attacks = 0;
      attacks |= (1L << (i-10)) & ~Gfile & ~Hfile & ~rank1;
      attacks |= (1L << (i-17)) & ~Hfile & ~rank1 & ~rank2;
      attacks |= (1L << (i-15)) & ~Afile & ~rank1 & ~rank2;
      attacks |= (1L << (i-6))  & ~Afile & ~Bfile & ~rank1;
      attacks |= (1L << (i+10)) & ~Afile & ~Bfile & ~rank8;
      attacks |= (1L << (i+17)) & ~Afile & ~rank7 & ~rank8;
      attacks |= (1L << (i+15)) & ~Hfile & ~rank7 & ~rank8;
      attacks |= (1L << (i+6))  & ~Gfile & ~Hfile & ~rank8;
      knightAttacks[i] = attacks;
    }
  }

  private static void generateKingAttacks() {
    kingAttacks = new long[64];

    for (int i = 0; i < 64; i++) {
      long attacks = 0;
      attacks |= (1L << (i-9)) & ~Hfile & ~rank1;
      attacks |= (1L << (i-8)) & ~rank1;
      attacks |= (1L << (i-7)) & ~Afile & ~rank1;
      attacks |= (1L << (i-1)) & ~Hfile;
      attacks |= (1L << (i+1)) & ~Afile;
      attacks |= (1L << (i+7)) & ~Hfile & ~rank8;
      attacks |= (1L << (i+8)) & ~rank8;
      attacks |= (1L << (i+9)) & ~Afile & ~rank8;
      kingAttacks[i] = attacks;
    }
  }
}
