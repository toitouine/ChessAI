public final class GameState {
  static public final int Going = 0;
  static public final int Mat = 1;
  static public final int Pat = 2;
  static public final int Materiel = 3;
  static public final int Repetition = 4;
  static public final int CinquanteCoups = 5;
  static public final int Interrupted = 6;
  static public final int IllegalMove = 7;
  static public final int Resign = 8;

  private final boolean gameEnded;
  private final int state;
  private final int winner;

  private GameState(int s) {
    this(s, Player.None);
  }

  private GameState(int s, int w) {
    state = s;
    gameEnded = s != Going;
    winner = w;
  }

  /////////////////////////////////////////////////////////////////

  public boolean gameEnded() {
    return gameEnded;
  }

  public int state() {
    return state;
  }

  public int winner() {
    return winner;
  }

  public boolean isDraw() {
    return gameEnded && winner != Player.None;
  }

  public boolean whiteWin() {
    return gameEnded && winner == Player.White;
  }

  public boolean blackWin() {
    return gameEnded && winner == Player.Black;
  }

  /////////////////////////////////////////////////////////////////

  static public GameState Going() {
    return new GameState(Going);
  }

  static public GameState Mat(int win) {
    return new GameState(Mat, win);
  }

  static public GameState Pat() {
    return new GameState(Pat);
  }

  static public GameState CinquanteCoups() {
    return new GameState(CinquanteCoups);
  }

  static public GameState Materiel() {
    return new GameState(Materiel);
  }

  static public GameState Repetition() {
    return new GameState(Repetition);
  }

  static public GameState Interrupted() {
    return new GameState(Interrupted);
  }

  static public GameState IllegalMove(int looser) {
    return new GameState(IllegalMove, 1 - looser);
  }

  static public GameState Resign(int looser) {
    return new GameState(Resign, 1 - looser);
  }

  /////////////////////////////////////////////////////////////////

  @Override
  public String toString() {
    String wstr = (winner == Player.White ? "blancs" : "noirs");
    if (state == Going) return "Partie en cours";
    if (state == Mat) return "Victoire des " + wstr + " par échec et mat";
    if (state == Pat) return "Nulle par pat";
    if (state == Materiel) return "Nulle par manque de matériel";
    if (state == Repetition) return "Nulle par répétition";
    if (state == CinquanteCoups) return "Nulle par règle des cinquante coups";
    if (state == Interrupted) return "Partie interrompue";
    if (state == IllegalMove) return "Victoire des " + wstr + " (coup illégal)";
    if (state == Resign) return "Victoire des " + wstr + " par abandon";
    return "";
  }
}
