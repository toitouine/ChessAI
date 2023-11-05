// TODO :
//
// - Lier les toggles à des variables
// - Raccourcis clavier
// - Génération de coups
// - Déroulement de la partie, Game Manager, Players, IA et Humain
// - UI : Suite
// - IA, Hacker, Book, PGN, et autre classes utiles
// - UI : Fin

import processing.core.PApplet;

public class Main {

  public static void main(String[] args) {
    // Initialisation
    GameManager gameManager = GameManager.getInstance();
    MainApplet mainApplet = new MainApplet();
    Time.init(mainApplet);
    printStartMessage();

    // Démarre l'applet principal
    String[] processingArgs = {Config.General.name};
    PApplet.runSketch(processingArgs, mainApplet);

    // Actualise GameManager pour démarrer des nouvelles parties
    // en attente, gérer les fins de parties...
    for (;;) gameManager.update();
  }

  private static void printStartMessage() {
    System.out.println();
    System.out.println("─────────────────────────────────────────────");
    System.out.println(Config.General.name + ", Antoine Mechulam");
    System.out.println("(https://github.com/toitouine/ChessAI)");
    System.out.println();
    System.out.println("IAs disponibles :");
    System.out.println("- LeMaire : Très bon en ouverture et en finale");
    System.out.println("- LesMoutons : Voleur, arnaqueur, tricheur, menaces en un !!");
    System.out.println("- Loic : Plutôt mauvais, préfère pater que mater");
    System.out.println("- Antoine : Un jeu aléatoire de qualité");
    System.out.println("- Stockfish : Extrêmement difficile de perdre contre lui");
    System.out.println();
    System.out.println("Voir fichier Config.java pour les options / paramètres");
    System.out.println("Appuyer sur H pour afficher l'aide (raccourcis claviers)");
    System.out.println();
    System.out.println("/!\\ La direction rejette toute responsabilité en cas de CPU détruit par ce programme ou d'ordinateur brulé.");
    System.out.println("─────────────────────────────────────────────");
    System.out.println();
  }
}
