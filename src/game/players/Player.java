abstract public class Player {

  public static final int None = -1;
  public static final int White = 0;
  public static final int Black = 1;

  public final SearchSettings settings;
  private Game.PlayerInterface gameInterface;

  public abstract Move play(Board board);
  public abstract void cancelSearch();
  public abstract String elo();
  public abstract String victoryTitle();
  public abstract boolean isBot();

  public Player(SearchSettings settings) {
    this.settings = settings;
  }

  final public String name()  {
    return getClass().getSimpleName();
  }

  final public void setGameInterface(Game.PlayerInterface i) {
    gameInterface = i;
  }

  final public void resign() {
    gameInterface.resign();
  }

  final public Move askHumanMove(Board board) {
    return gameInterface.askHumanMove(board);
  }

  final public void stopAskHumanMove() {
    gameInterface.stopAskHumanMove();
  }

  public String pseudo() {
    return name();
  }

  static Player create(String name, SearchSettings s) {
    try {
      Class<?> clazz = Class.forName(name);
      Object instance = clazz.getConstructor(SearchSettings.class).newInstance(s);
      return (Player)instance;
    }
    catch (Exception e) {
      Debug.error("Classe du joueur [" + name + "] introuvable. Ajout du maire à la place.");
      return new LeMaire(s);
    }
  }

  public String description() {
    String desc = name();
    if (settings.type == Search.Iterative) {
      desc += " (Iterative Deepening, " + settings.time.millis() + " ms)";
    }
    else if (settings.type == Search.Fixed) {
      desc += " (Profondeur fixe, " + settings.depth + ")";
    }
    return desc;
  }

  @Override
  public String toString() {
    return description();
  }

  public Player copy() {
    SearchSettings s = settings.copy();
    try {
      Object instance = getClass().getConstructor(SearchSettings.class).newInstance(s);
      return (Player)instance;
    } catch (Exception e) {
      Debug.error("Impossible de copier le joueur " + this + ". Ajout du maire à la place.");
      return new LeMaire(s);
    }
  }
}
