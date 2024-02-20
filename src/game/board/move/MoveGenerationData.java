public final class MoveGenerationData {
  private MoveGenerationData() {}

  // Colonnes et lignes utiles de l'échiquier
  static final public long Afile = 0x0101010101010101L;
  static final public long Bfile = Afile << 1;
  static final public long Gfile = Afile << 6;
  static final public long Hfile = Afile << 7;
  static final public long rank1 = 0b11111111L << 56;
  static final public long rank2 = 0b11111111L << 48;
  static final public long rank7 = 0b11111111L << 8;
  static final public long rank8 = 0b11111111L;

  // Attaques d'un cavalier sur une case sous forme de bitboard
  static public long knightAttacks[];

  static {
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


}
