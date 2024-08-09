/////////////////////////////////////////////////////////////////

// Game

// Représente une partie d'échecs entre deux joueurs (Player)
// Après être créée, une partie a un numéro unique qui lui correspond.
// Pour lancer une partie, utiliser GameManager.addGame(game) et la partie
// sera lancée dès que ce sera possible.
// Pendant la partie, chaque joueur est appelé à tour de rôle pour jouer.
// Une partie peut être affichée sur un GameDisplayer qui peut être une
// interface graphique, des lignes de commandes, etc... Cette interface
// est alertée à chaque coup joué, au début et à la fin de la partie. Les
// coups d'humains sont demandés à travers cette interface.
// Les joueurs ont accès à la partie à travers une PlayerInterface pour
// pouvoir abandonner, demander un coup d'humain...

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public final class Game extends Thread {
  public final Board board;
  public final String startFEN;
  public final boolean useHacker = false;
  public final boolean useTime;
  public final Timer[] timers = new Timer[2];
  private final Player[] players = new Player[2];

  private final PlayerInterface[] playerInterfaces = new PlayerInterface[2];

  private final GameDisplayer displayer;
  private final boolean isDisplayed;

  public GameState gameState = null;
  public final SyncBoolean paused = new SyncBoolean(true);

  private final int number;
  private static int totalGames = 0;

  private final ArrayList<Long> hashHistory = new ArrayList<Long>();
  private final ArrayList<Move> moveHistory = new ArrayList<Move>();
  private int demicoups = 0; // Nombre de demi-coups au total
  private int fiftymoves = 0; // Demi-coups depuis la dernière capture/poussée de pion

  public Game(Player p1, Player p2, String fen, Timer t1, Timer t2, GameDisplayer gd) {
    board = new Board(fen);
    startFEN = fen;
    players[0] = p1;
    players[1] = p2;
    timers[0] = t1;
    timers[1] = t2;
    displayer = gd;
    useTime = t1.getTotalTime().millis() > 0 && t2.getTotalTime().millis() > 0;
    isDisplayed = gd != null;
    totalGames++;
    number = totalGames;

    playerInterfaces[0] = new PlayerInterface(p1);
    playerInterfaces[1] = new PlayerInterface(p2);

    // Pour qu'un humain puisse jouer, la partie doit être connectée à un GameDisplayer.
    // Si il n'y en a pas, l'humain jouera le premier coup à chaque fois.
    if (!isDisplayed) {
      if (p1 instanceof Humain || p2 instanceof Humain) {
        Debug.error("Humain dans la partie " + this + " mais pas de GameDisplayer.");
      }
    }

    if (isDisplayed) displayer.setGame(this);
  }

  public Game(Player p1, Player p2, String fen, Timer t1, Timer t2) {
    this(p1, p2, fen, t1, t2, null);
  }

  public Game(Player p1, Player p2, String fen) {
    this(p1, p2, fen, Timer.fromMillis(0, 0), Timer.fromMillis(0, 0), null);
  }

  public Game(Player p1, Player p2) {
    this(p1, p2, Config.General.defaultFEN);
  }

  public void run() {
    if (gameState == null) gameState = GameState.Going();

    // Prépare la partie et annone le début
    board.loadFEN(startFEN);
    hashHistory.add(board.zobrist);
    announce();
    paused.set(false);
    if (isDisplayed) displayer.onGameStart();
    if (useTime) {
      timers[Player.White].start();
      timers[Player.Black].start();
      timers[Player.Black].pause(false);
    }

    while (!ended()) {
      int tourDeQui = board.tourDeQui;

      // Si la partie a été mise en pause, on attend le redémarrage
      if (paused.get()) {
        synchronized (paused) {
          try {
            timers[Player.White].pause(false);
            timers[Player.Black].pause(false);
            while (paused.get()) paused.wait();
            timers[tourDeQui].resume();
          } catch (Exception e) {
            Debug.error("Erreur pendant la pause de " + this);
          }
        }
      }

      // Le joueur joue le coup (on ignore la suite si la partie a été interrompue)
      if (gameState != null && gameState.gameEnded()) break;
      SearchResult result = getPlayerSearch(tourDeQui);
      Move move = (result != null) ? result.move() : null;
      if (gameState != null && gameState.gameEnded()) break;

      if (!isMoveValid(board, move)) {
        gameState = GameState.IllegalMove(tourDeQui);
        break;
      }
      boolean isCapture = board.grid(move.endSquare()) != null;
      board.make(move);

      // Gère les pendules
      timers[tourDeQui].pause();
      timers[1-tourDeQui].resume();

      // Actualise les infos
      hashHistory.add(board.zobrist);
      moveHistory.add(move);
      demicoups++;
      if (isCapture || board.grid(move.endSquare()).type == Piece.Pion) fiftymoves = 0;
      else fiftymoves++;

      if (isDisplayed) displayer.onMovePlayed(move);

      // Vérifie si c'est la fin de la partie ou non
      gameState = getGameState();
    }

    if (useTime) {
      timers[Player.White].pause(false);
      timers[Player.Black].pause(false);
    }
    if (isDisplayed) displayer.onGameEnd();
    Debug.log("game", "Partie #" + number + " terminée : " + gameState);
    GameManager.endGame(this);
  }

  private SearchResult getPlayerSearch(int tourDeQui) {
    Player player = players[tourDeQui];

    // Si il n'y a pas de temps, demande simplement le coup
    if (!useTime) return player.play(board.copy());

    // Sinon, demande le coup et si il n'y a plus de temps, interrompt la recherche
    Time maxTime = timers[tourDeQui].timeRemaining();
    ExecutorService executor = Executors.newCachedThreadPool();
    Callable<SearchResult> task = () -> { return player.play(board.copy()); };
    Future<SearchResult> future = executor.submit(task);

    try {
      // Si la recherche retourne un coup à temps, le renvoie
      SearchResult result = future.get(maxTime.millis(), TimeUnit.MILLISECONDS);
      return result;
    }
    catch (TimeoutException ex) {
      // Sinon, interrompt la recherche et gère la fin de partie
      player.cancelSearch();
      if (hasSufficientMaterial(1-tourDeQui)) gameState = GameState.Timeout(tourDeQui);
      else gameState = GameState.TimeoutAndMateriel();
      return null;
    }
    catch (Exception e) {
      return null;
    }
    finally {
      future.cancel(true);
      executor.shutdownNow();
    }
  }

  private boolean isMoveValid(Board b, Move move) {
    if (move == null) return false;

    ArrayList<Move> moves = b.getLegalMoves();
    for (Move m : moves) {
      if (m.equals(move)) return true;
    }
    return false;
  }

  private GameState getGameState() {
    // Partie interrompue
    if (gameState.state() == GameState.Interrupted) return GameState.Interrupted();

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

  public boolean hasSufficientMaterial(int color) {
    // Si il reste un pion, une dame ou une tour, il y a assez de matériel
    if (board.getPieces(Piece.Pion, color) != 0 ||
        board.getPieces(Piece.Tour, color) != 0 ||
        board.getPieces(Piece.Dame, color) != 0) return true;

    // Si il reste deux cavaliers ou deux fous, il y a assez de matériel
    if (Long.bitCount(board.getPieces(Piece.Cavalier, color)) >= 2 ||
        Long.bitCount(board.getPieces(Piece.Fou, color)) >= 2) return true;

    // Si il reste un fou et un cavalier, il y a assez de matériel
    if (Long.bitCount(board.getPieces(Piece.Cavalier, color)) == 1 &&
        Long.bitCount(board.getPieces(Piece.Fou, color)) == 1) return true;

    // Sinon, il n'y a plus assez de matériel
    return false;
  }

  public void terminate() {
    if (gameState == null || !gameState.gameEnded()) {
      gameState = GameState.Interrupted();
      if (isDisplayed) displayer.stopAskMove();
    }
  }

  private void resign(int color) {
    if (gameState == null || !gameState.gameEnded()) {
      gameState = GameState.Resign(color);
      if (isDisplayed) displayer.stopAskMove();
    }
  }

  public boolean ended() {
    if (gameState == null) return false;
    return gameState.gameEnded();
  }

  public Player getWhite() {
    return players[Player.White];
  }

  public Player getBlack() {
    return players[Player.Black];
  }

  public Player getPlayer(int color) {
    if (color != Player.White && color != Player.Black) return players[Player.White];
    return players[color];
  }

  private void announce() {
    String hackerText = (useHacker ? " [HACKER]" : "");
    Debug.log("game", "Partie #" + number + " démarrée : " + getWhite().name() + " contre " + getBlack().name() + hackerText);
  }

  @Override
  public String toString() {
    return getClass().getName() + "[#" + number + ", w=" + getWhite().name() + ", b=" + getBlack().name() + ", s=" + gameState + "]";
  }

  public Game copy() {
    Player p1 = players[0];
    Player p2 = players[1];
    Timer t1 = timers[0];
    Timer t2 = timers[1];
    return new Game(p1.copy(), p2.copy(), startFEN, t1.copy(), t2.copy(), displayer);
  }

  public class PlayerInterface {
    private final Player player;

    public PlayerInterface(Player p) {
      player = p;
      player.setGameInterface(this);
    }

    public void resign() {
      if (players[Player.White] == player) Game.this.resign(Player.White);
      else if (players[Player.Black] == player) Game.this.resign(Player.Black);
    }

    public Move askHumanMove(Board b) {
      if (!isDisplayed) return b.getLegalMoves().get(0);
      return displayer.askHumanMove(b);
    }

    public void stopAskHumanMove() {
      if (isDisplayed) displayer.stopAskMove();
    }
  }
}
