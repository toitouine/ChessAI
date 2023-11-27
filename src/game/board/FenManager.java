import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;
import static java.util.Map.Entry;

public final class FenManager {

  Board board;

  public FenManager(Board board) {
    this.board = board;
  }

  public int getIndexFromCode(char c) {
    for (int i = 0; i < Config.Piece.codes.length; i++) {
      if (Config.Piece.codes[i] == c) return i;
    }
    Debug.error("Aucun index de pièce trouvé avec le caractère : " + c);
    return -1;
  }

  public void loadPosition(String fen) {
    board.clear();

    try {
      int endOfPosition = 0;

      // Position des pièces
      int cursorI = 0, cursorJ = 0;
      for (int i = 0; i < fen.length(); i++) {
        char c = fen.charAt(i);

        if (c == ' ') {
          endOfPosition = i;
          break;
        }
        if (c == '/') {
          cursorJ = 0;
          cursorI++;
          continue;
        }
        if (Character.isDigit(c)) {
          cursorJ += Integer.valueOf(String.valueOf(c));
          continue;
        }

        board.addPiece(getIndexFromCode(c), cursorI, cursorJ);
        cursorJ++;
      }

      // Trait
      if (fen.charAt(endOfPosition+1) == 'w') board.tourDeQui = Player.White;
      else if (fen.charAt(endOfPosition+1) == 'b') board.tourDeQui = Player.Black;
      else throw new Exception("FEN invalide : Trait");

      // Roques
      for (int i = endOfPosition+3; i < fen.length(); i++) {
        char c = fen.charAt(i);
        if (c == 'K') board.whitePetitRoque = true;
        else if (c == 'Q') board.whiteGrandRoque = true;
        else if (c == 'k') board.blackPetitRoque = true;
        else if (c == 'q') board.blackGrandRoque = true;
      }

    }
    catch (Exception e) {
      Debug.error("FEN non valide (" + fen + "). Importation de la FEN par défaut.");
      loadPosition(Config.General.defaultFEN);
    }
  }

  public String generateFEN() {
    //Position des pièces
    String fen = "";
    int vide = 0;

    for (int i = 0; i < 8; i++) {

      for (int j = 0; j < 8; j++) {
        Case s = board.grid[i][j];
        if (s.piece == null) vide += 1;
        else {
          if (vide > 0) fen += vide;
          vide = 0;
          fen += Config.Piece.codes[s.piece.index];
        }
      }

      if (vide > 0) fen += vide;
      vide = 0;
      fen += (i < 7 ? '/' : ' ');
    }

    // Trait
    fen += (board.tourDeQui == Player.White ? "w" : "b");

    // Roques
    boolean areAnyRoques = (board.whitePetitRoque || board.whiteGrandRoque
                         || board.blackPetitRoque || board.blackGrandRoque);
    if (areAnyRoques) {
      fen += " ";
      if (board.whitePetitRoque) fen += "K";
      if (board.whiteGrandRoque) fen += "Q";
      if (board.blackPetitRoque) fen += "k";
      if (board.blackGrandRoque) fen += "q";
    }

    return fen;
  }
}
