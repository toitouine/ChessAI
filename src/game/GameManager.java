/////////////////////////////////////////////////////////////////

// GameManager

// Gère les différentes parties selon 3 statuts :
// - partie en attente d'être lancée (chaque partie passe d'abord par cet état)
// - partie en cours (parties en jeu, avec un maximum défini dans Config.java)
// - partie terminée
// Pour ajouter une partie, utiliser addGame(), et la partie sera lancée dès
// qu'il y aura de la place dans les parties en cours
// Pour interrompre une partie, ou à la fin d'une partie, appeler endGame()
// Pour récupérer l'instance de la classe, appeler GameManager.getInstance()

// Une fois lancée, une partie se déroule sur un thread à part jusqu'à ce
// qu'elle se termine. Elle fait appel aux différents joueurs à tour de rôle.

/////////////////////////////////////////////////////////////////

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

  /////////////////////////////////////////////////////////////////

  public synchronized Game addGame(Game game) {
    waitingGames.add(game);
    updateWaiting();
    return game;
  }

  public synchronized Game addGame(Player p1, Player p2, String startFEN, Timer t1, Timer t2, boolean useHacker) {
    Game game = new Game(p1, p2, startFEN, t1, t2, useHacker);
    return addGame(game);
  }

  public synchronized Game addGame(Player p1, Player p2, String startFEN, Timer t1, Timer t2) {
    return addGame(p1, p2, startFEN, t1, t2, false);
  }

  public synchronized void endGame(Game game) {
    // Recherche la partie à arrêter (si elle existe)
    boolean gameFound = goingGames.remove(game);
    if (!gameFound) {
      Debug.log("erreur", "Partie à terminer introuvable : " + game);
      return;
    }

    // Confirme l'arrêt de la partie et actualise la file d'attente
    game.ended = true;
    finishedGames.add(game);
    updateWaiting();
    printFinishedGames();
  }

  private void updateWaiting() {
    // Actualise la file d'attente autant que possible
    while (goingGames.size() < maxGames) {
      if (waitingGames.size() == 0) break;
      Game game = waitingGames.remove(0);
      goingGames.add(game);
      game.start();
    }
  }

  /////////////////////////////////////////////////////////////////

  public void printWaitingGames() {
    Debug.log("Parties en attente :");
    for (int i = 0; i < waitingGames.size(); i++) {
      Debug.log("[" + i + "] " + waitingGames.get(i));
    }
  }

  public void printCurrentGames() {
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
