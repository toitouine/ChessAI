final public class BoardUtility {
  private BoardUtility() {}

  // Renvoie le nom d'une case selon son index
  static public String caseName(int square) {
    return (char)(97+(square%8)) + String.valueOf(8 - square/8);
  }

  // Renvoie l'index d'une case à partir de son nom (e4...)
  static public int nameToCase(String name) {
    int colonne = (name.charAt(0) - '0') - 49;
    int ligne = 8 - Integer.parseInt(String.valueOf(name.charAt(1)));
    return 8 * ligne + colonne;
  }

  // Représentation d'un plateau et de ses informations
  static public String boardRepresentation(Board board) {
    StringBuilder str = new StringBuilder();
    str.append("     a   b   c   d   e   f   g   h\n");
    str.append("   ┌───┬───┬───┬───┬───┬───┬───┬───┐\n");

    for (int i = 0; i < 8; i++) {
      str.append(" " + (8-i) + " │");
      for (int j = 0; j < 8; j++) {
        Piece p = board.grid(i, j);
        char c = (p == null ? ' ' : p.getCode());
        str.append(" " + c + " │");
      }
      str.append("\n");
      if (i != 7) str.append("   ├───┼───┼───┼───┼───┼───┼───┼───┤\n");
    }
    str.append("   └───┴───┴───┴───┴───┴───┴───┴───┘\n");

    String roqueString = (board.petitRoque[Player.White] ? "K" : "")
                       + (board.grandRoque[Player.White] ? "Q" : "")
                       + (board.petitRoque[Player.Black] ? "k" : "")
                       + (board.grandRoque[Player.Black] ? "q" : "");
    if (roqueString.equals("")) roqueString = "None";

    Integer enPassantSquare = board.getEnPassantSquare();
    String enPassantString = (enPassantSquare != null ? caseName(enPassantSquare) : "None");
    str.append("\n   [Trait aux " + (board.tourDeQui == Player.White ? "blancs" : "noirs") + "]");
    str.append("\n   [Roques : " + roqueString + "]");
    str.append("\n   [En passant : " + enPassantString + "]");
    str.append("\n   [Endgame Weight : " + board.endGameWeight + "]");
    str.append("\n   [Zobrist key : " + board.zobrist + "]");
    str.append("\n   [FEN : " + board.generateFEN() + "]\n");
    return str.toString();
  }
}
