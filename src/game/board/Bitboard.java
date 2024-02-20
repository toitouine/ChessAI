public final class Bitboard {
  private Bitboard() {}

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
