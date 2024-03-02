public final class Bitboard {
  private Bitboard() {}

  // Colonnes et lignes utiles de l'échiquier
  static final public long Afile = 0x0101010101010101L;
  static final public long Bfile = Afile << 1;
  static final public long Gfile = Afile << 6;
  static final public long Hfile = Afile << 7;
  static final public long rank1 = 0b11111111L << 56;
  static final public long rank2 = 0b11111111L << 48;
  static final public long rank7 = 0b11111111L << 8;
  static final public long rank8 = 0b11111111L;

  public static void print(long bitboard) {
    String str = getBinaryString(bitboard);
    str = new StringBuilder(str).reverse().toString();
    for (int i = 0; i < 8; i++) {
      Debug.log(str.substring(i*8, (i+1)*8).replace("", " ").trim());
    }
    Debug.log();
  }

  public static void printBinary(long bitboard) {
    Debug.log(getBinaryString(bitboard));
  }

  private static String getBinaryString(long bitboard) {
    return String.format("%64s", Long.toBinaryString(bitboard)).replace(' ', '0');
  }
}
