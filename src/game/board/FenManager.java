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

        int index = getIndexFromCode(c);
        if (index != -1) board.addPiece(index, 8*cursorI + cursorJ);
        cursorJ++;
      }

      // Trait
      if (fen.charAt(endOfPosition+1) == 'w') board.tourDeQui = Player.White;
      else if (fen.charAt(endOfPosition+1) == 'b') board.tourDeQui = Player.Black;
      else throw new Exception("FEN non valide : Trait");

      // Roques
      // TODO : vérifier si ils sont vraiment possibles, désactiver sinon
      for (int i = endOfPosition+3; i < fen.length(); i++) {
        char c = fen.charAt(i);
        if (c == 'K') board.petitRoque[Player.White] = true;
        else if (c == 'Q') board.grandRoque[Player.White] = true;
        else if (c == 'k') board.petitRoque[Player.Black] = true;
        else if (c == 'q') board.grandRoque[Player.Black] = true;
      }

    }
    catch (Exception e) {
      Debug.error("FEN non valide (" + fen + "). Chargement de la FEN par défaut.");
      loadPosition(Config.General.defaultFEN);
    }
  }

  public String generateFEN() {
    //Position des pièces
    String fen = "";
    int vide = 0;

    for (int i = 0; i < 64; i++) {
      Piece p = board.grid(i);
      if (p == null) {
        vide++;
      } else {
        if (vide > 0) fen += vide;
        vide = 0;
        fen += p.getCode();
      }

      if (i != 0 && (i+1) % 8 == 0) {
        if (vide > 0) fen += vide;
        vide = 0;
        fen += (i < 63 ? '/' : ' ');
      }
    }

    // Trait
    fen += (board.tourDeQui == Player.White ? "w" : "b");

    // Roques
    boolean areAnyRoques = (board.petitRoque[Player.White] || board.grandRoque[Player.White]
                         || board.petitRoque[Player.Black] || board.grandRoque[Player.Black]);
    if (areAnyRoques) {
      fen += " ";
      if (board.petitRoque[Player.White]) fen += "K";
      if (board.grandRoque[Player.White]) fen += "Q";
      if (board.petitRoque[Player.Black]) fen += "k";
      if (board.grandRoque[Player.Black]) fen += "q";
    }

    return fen;
  }
}
