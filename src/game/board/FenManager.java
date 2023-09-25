import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;
import static java.util.Map.Entry;

public final class FenManager {

  Board board;
  Map<Integer, Character> indexToCode = Map.ofEntries(
    entry(0, 'K'), entry(6, 'k'),
    entry(1, 'Q'), entry(7, 'q'),
    entry(2, 'R'), entry(8, 'r'),
    entry(3, 'B'), entry(9, 'b'),
    entry(4, 'N'), entry(10, 'n'),
    entry(5, 'P'), entry(11, 'p')
  );

  HashMap<Character, Integer> codeToIndex = new HashMap<Character, Integer>();

  public FenManager(Board board) {
    this.board = board;
    for(Entry<Integer, Character> entry : indexToCode.entrySet()) {
      codeToIndex.put(entry.getValue(), entry.getKey());
    }
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

        int pieceColor = Character.isLowerCase(c) ? 1 : 0;
        board.addPiece(codeToIndex.get(c)-pieceColor*6, cursorI, cursorJ, pieceColor);
        cursorJ++;
      }

      // Trait
      if (fen.charAt(endOfPosition+1) == 'w') board.tourDeQui = Player.White;
      else if (fen.charAt(endOfPosition+1) == 'b') board.tourDeQui = Player.Black;
      else {
        Debug.log("erreur", "FEN invalide : Trait");
        return;
      }

      // Roques
      if (fen.charAt(endOfPosition+2) != ' ') {
        Debug.log("erreur", "FEN invalide : Séparation trait-roque");
        return;
      }
      for (int i = endOfPosition+3; i < fen.length(); i++) {
        char c = fen.charAt(i);
        if (c == 'K') board.whitePetitRoque = true;
        else if (c == 'Q') board.whiteGrandRoque = true;
        else if (c == 'k') board.blackPetitRoque = true;
        else if (c == 'q') board.blackGrandRoque = true;
      }

    }
    catch (Exception e) {
      Debug.log("erreur", "FEN non valide ! Importation de la FEN par défaut.");
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
          fen += indexToCode.get(s.piece.index);
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
