/////////////////////////////////////////////////////////////////

// GameManager

// Gère les différentes parties selon 3 statuts :
// - partie en attente d'être lancée (chaque partie passe d'abord par cet état)
// - partie en cours (parties en jeu, avec un maximum défini dans Config.java)
// - partie terminée
// Pour ajouter une partie, utiliser addGame(), et la partie sera lancée dès
// qu'il y aura de la place dans la liste des parties en cours
// À la fin d'une partie, celle-ci appelle endGame() pour valider la fin. Pour
// interrompre une partie, utiliser plutôt game.end().

// Une fois lancée, une partie se déroule sur un thread à part jusqu'à ce
// qu'elle se termine. Elle fait appel aux différents joueurs à tour de rôle.

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;

public final class GameManager {

  private static ArrayList<Game> waitingGames; // Parties en attente d'être lancées
  private static ArrayList<Game> goingGames; // Parties en train d'être jouées
  private static ArrayList<Game> finishedGames; // Parties terminées

  private final static int maxGames = Config.General.maximumGames;

  static {
    waitingGames = new ArrayList<Game>();
    goingGames = new ArrayList<Game>();
    finishedGames = new ArrayList<Game>();
  }

  /////////////////////////////////////////////////////////////////

  public static synchronized Game addGame(Game game) {
    if (waitingGames.contains(game) || goingGames.contains(game) || finishedGames.contains(game)) {
      Debug.error("Impossible d'ajouter la partie : " + game + ". Elle est déjà présente.");
      return game;
    }
    waitingGames.add(game);
    updateWaiting();
    return game;
  }

  public static synchronized Game addGame(Player p1, Player p2, String startFEN, Timer t1, Timer t2) {
    Game game = new Game(p1, p2, startFEN, t1, t2);
    return addGame(game);
  }

  public static synchronized void endGame(Game game) {
    // Si la partie est encore en cours, demande l'arrêt
    // La partie sera donc retirée de la liste quand elle sera finie
    if (!game.ended) {
      game.end();
      return;
    }

    // Recherche la partie à arrêter (si elle existe)
    boolean gameFound = goingGames.remove(game);
    if (!gameFound) {
      Debug.log("erreur", "Partie à terminer introuvable : " + game);
      return;
    }

    // Actualise les listes
    finishedGames.add(game);
    updateWaiting();
  }

  private static void updateWaiting() {
    // Actualise la file d'attente autant que possible
    while (goingGames.size() < maxGames) {
      if (waitingGames.size() == 0) break;
      Game game = waitingGames.remove(0);
      goingGames.add(game);
      game.start();
    }
  }

  /////////////////////////////////////////////////////////////////

  public static void printWaitingGames() {
    Debug.log("Parties en attente :");
    for (int i = 0; i < waitingGames.size(); i++) {
      Debug.log("[" + i + "] " + waitingGames.get(i));
    }
  }

  public static void printCurrentGames() {
    Debug.log("Parties en cours :");
    for (int i = 0; i < goingGames.size(); i++) {
      Debug.log("[" + i + "] " + goingGames.get(i));
    }
  }

  public static void printFinishedGames() {
    Debug.log("Parties terminées :");
    for (int i = 0; i < finishedGames.size(); i++) {
      Debug.log("[" + i + "] " + finishedGames.get(i));
    }
  }
}
