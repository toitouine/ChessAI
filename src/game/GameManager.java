import java.util.ArrayList;

public final class GameManager {

  private static GameManager instance;

  private ArrayList<Game> waitingGames; // Parties en attente d'être lancées
  private ArrayList<Game> goingGames; // Parties en train d'être jouées
  private ArrayList<Game> finishedGames; // Parties terminées

  private int maxGames = Config.General.maximumGames;

  private GameManager() {
    waitingGames = new ArrayList<Game>();
    goingGames = new ArrayList<Game>();
    finishedGames = new ArrayList<Game>();
  }

  public static GameManager getInstance() {
    if (instance == null) instance = new GameManager();
    return instance;
  }

  public Game addGame(Player p1, Player p2, String startFEN, Timer t1, Timer t2, boolean useHacker) {
    Game game = new Game(p1, p2, startFEN, t1, t2, useHacker);
    waitingGames.add(game);
    return game;
  }

  public void gameEnded(Game endedGame) {
    // TODO:
    // changer la partie de liste, et éventuellement en démarrer une nouvelle si en attente
  }

  public void printWaitingGames() {
    Debug.log("Parties en attente :");
    for (int i = 0; i < waitingGames.size(); i++) {
      Debug.log("[" + i + "] " + waitingGames.get(i));
    }
  }

  public void printGoingGames() {
    Debug.log("Parties en cours :");
    for (int i = 0; i < goingGames.size(); i++) {
      Debug.log("[" + i + "] " + goingGames.get(i));
    }
  }

  public void printFinishedGames() {
    Debug.log("Parties terminées :");
    for (int i = 0; i < finishedGames.size(); i++) {
      Debug.log("[" + i + "] " + finishedGames.get(i));
    }
  }
}
