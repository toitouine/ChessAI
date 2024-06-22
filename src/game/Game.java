import java.util.ArrayList;
import java.util.Collections;

public final class Game extends Thread {
  public final Board board;
  public final String startFEN;
  public final boolean useHacker;
  public final boolean useTime;
  public final Timer[] timers = new Timer[2];
  private final Player[] players = new Player[2];

  public boolean ended = false;
  public GameState gameState = null;
  public final SyncBoolean paused = new SyncBoolean(true);

  private int number;
  private static int totalGames = 0;

  private ArrayList<Long> hashHistory = new ArrayList<Long>();
  private ArrayList<Move> moveHistory = new ArrayList<Move>();
  private int demicoups = 0; // Nombre de demi-coups au total
  private int fiftymoves = 0; // Demi-coups depuis la dernière capture/poussée de pion

  public Game(Player p1, Player p2, String fen, Timer t1, Timer t2, boolean hacker) {
    board = new Board(fen);
    startFEN = fen;
    players[0] = p1;
    players[1] = p2;
    timers[0] = t1;
    timers[1] = t2;
    useHacker = hacker;
    useTime = t1.getTime().millis() > 0 && t2.getTime().millis() > 0;

    totalGames++;
    number = totalGames;
  }

  public Game(Player p1, Player p2, String startFEN, Timer t1, Timer t2) {
    this(p1, p2, startFEN, t1, t2, false);
  }

  public void run() {
    if (gameState != null && gameState.state == GameState.Interrupted) {
      ended = true;
    }

    // Prépare la partie et annone le début
    board.loadFEN(startFEN);
    hashHistory.add(board.zobrist);
    announce();
    paused.set(false);
    if (!ended) gameState = GameState.Going();

    while (!ended) {
      // Si la partie a été mise en pause, on attend le redémarrage
      if (paused.get()) {
        synchronized (paused) {
          try {
            paused.wait();
          } catch (Exception e) {
            Debug.error("Erreur pendant la pause de " + this);
          }
        }
      }

      // Le joueur joue le coup
      int tourDeQui = board.tourDeQui;
      Move move = players[tourDeQui].play(board.copy());
      boolean isCapture = board.grid(move.endSquare()) != null;
      board.make(move);

      // Actualise les infos
      hashHistory.add(board.zobrist);
      moveHistory.add(move);
      demicoups++;
      if (isCapture || board.grid(move.endSquare()).type == Piece.Pion) fiftymoves = 0;
      else fiftymoves++;

      // Vérifie si c'est la fin de la partie ou non
      if (gameState.state != GameState.Interrupted) gameState = getGameState();
      ended = gameState.gameEnded;
    }

    Debug.log("game", "Partie #" + number + " terminée : " + gameState);
    GameManager.getInstance().endGame(this);
  }

  private GameState getGameState() {
    // Mat ou pat
    if (board.getLegalMoves().size() == 0) {
      if (board.inCheck()) return GameState.Mat(1-board.tourDeQui);
      else return GameState.Pat();
    }

    // Répétition
    if (Collections.frequency(hashHistory, board.zobrist) >= 3) {
      return GameState.Repetition();
    }

    // Règle des 50 coups
    if (fiftymoves >= 100) return GameState.CinquanteCoups();

    // Manque de matériel
    long pions = board.getPieces(Piece.Pion);
    long rq = board.getPieces(Piece.Tour) | board.getPieces(Piece.Dame);
    long bk = board.getPieces(Piece.Fou) | board.getPieces(Piece.Cavalier);
    if (Long.bitCount(pions) == 0 && Long.bitCount(rq) == 0) {
      // Roi contre roi
      if (Long.bitCount(bk) == 0) return GameState.Materiel();

      // Roi contre fou/cavalier
      int wbkcount = Long.bitCount(bk & board.colorBitboard[Player.White]);
      int bbkcount = Long.bitCount(bk & board.colorBitboard[Player.Black]);
      if ((wbkcount == 1 && bbkcount == 0) || (wbkcount == 0 && bbkcount == 1)) {
        return GameState.Materiel();
      }
    }

    return GameState.Going();
  }

  public void end() {
    if (!ended) gameState = GameState.Interrupted();
  }

  public Player getWhite() {
    return players[Player.White];
  }

  public Player getBlack() {
    return players[Player.Black];
  }

  private void announce() {
    String hackerText = (useHacker ? " [HACKER]" : "");
    Debug.log("game", "Nouvelle partie démarrée (#" + number +  ") : " + getWhite().name + " contre " + getBlack().name + hackerText);
  }

  @Override
  public String toString() {
    return getClass().getName() + "[#" + number + ", w=" + getWhite().name + ", b=" + getBlack().name + ", s=" + gameState + "]";
  }

  public Game copy() {
    Player p1 = players[0];
    Player p2 = players[1];
    Timer t1 = timers[0];
    Timer t2 = timers[1];
    return new Game(p1.copy(), p2.copy(), startFEN, t1.copy(), t2.copy(), useHacker);
  }
}
