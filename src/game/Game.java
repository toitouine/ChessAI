public final class Game {
  public Board board;
  public Player[] players = new Player[2];
  public Timer[] timers = new Timer[2];
  public boolean gameEnded = false;
  public boolean useHacker;
  public boolean useTime;

  public boolean paused = true;

  public Game(Player p1, Player p2, String startFEN, Timer t1, Timer t2, boolean hacker) {
    board = new Board();
    players[0] = p1;
    players[1] = p2;
    timers[0] = t1;
    timers[1] = t2;
    useHacker = hacker;
    useTime = timers[0].getTime().millis() > 0 && timers[1].getTime().millis() > 0;
    board.loadFEN(startFEN);
    announceGame();
  }

  public Game(Player p1, Player p2, String startFEN, Timer t1, Timer t2) {
    this(p1, p2, startFEN, t1, t2, false);
  }

  public Player getWhite() {
    return players[Player.White];
  }

  public Player getBlack() {
    return players[Player.Black];
  }

  private void announceGame() {
    String hackerText = (useHacker ? " [HACKER]" : "");
    Debug.log();
    Debug.log("game", "Nouvelle partie créée : " + getWhite().name + " contre " + getBlack().name + hackerText);
    if (getWhite().isBot) Debug.log("  - " + getWhite().description());
    if (getBlack().isBot) Debug.log("  - " + getBlack().description());
    Debug.log();
  }
}
