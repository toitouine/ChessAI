public final class GameState {
  static public final int Going = 0;
  static public final int Mat = 1;
  static public final int Pat = 2;
  static public final int Materiel = 3;
  static public final int Repetition = 4;
  static public final int CinquanteCoups = 5;
  static public final int Interrupted = 6;

  public boolean gameEnded;
  public int state;
  public int winner = Player.None;

  private GameState(int s) {
    this(s, Player.None);
  }

  private GameState(int s, int w) {
    state = s;
    gameEnded = s != Going;
    if (gameEnded) winner = w;
  }

  static public GameState Going() {
    return new GameState(Going);
  }

  static public GameState Mat(int w) {
    return new GameState(Mat, w);
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

  @Override
  public String toString() {
    if (state == Going) return "Partie en cours";
    if (state == Mat) return "Victoire des " + (winner == Player.White ? "blancs" : "noirs") + " par échec et mat";
    if (state == Pat) return "Nulle par pat";
    if (state == Materiel) return "Nulle par manque de matériel";
    if (state == Repetition) return "Nulle par répétition";
    if (state == CinquanteCoups) return "Nulle par règle des cinquante coups";
    if (state == Interrupted) return "Partie interrompue";
    return "";
  }
}
