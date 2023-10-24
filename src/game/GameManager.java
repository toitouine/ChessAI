import java.util.ArrayList;

public final class GameManager {

  private static GameManager instance;

  private ArrayList<Game> waitingGames; // Parties en attente d'être lancées
  private ArrayList<Game> goingGames; // Parties en train d'être jouées

  private GameManager() {
    waitingGames = new ArrayList<Game>();
    goingGames = new ArrayList<Game>();
  }

  public static GameManager getInstance() {
    if (instance == null) instance = new GameManager();
    return instance;
  }

  public void update() {
  }
}
