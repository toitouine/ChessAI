// TODO :
//
// - Représentation de board en 1 dimension
// - Refaire boardDisplay (images dans la boucle)
// - Bitboards
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
    printStartMessage();

    // Démarre l'applet principal
    String[] processingArgs = {Config.General.name};
    PApplet.runSketch(processingArgs, mainApplet);
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

  public static void printHelpMenu() {
    System.out.println(" ");
    System.out.println("  Touche  │       Endroit       │       Description       ");
    System.out.println("──────────┼─────────────────────┼─────────────────────────");
    System.out.println(" B        │ En partie           │ Afficher les coups du livre d'ouverture");
    System.out.println(" C        │ En partie           │ Sauvegarder la PGN");
    System.out.println(" C        │ Éditeur             │ Copier la FEN");
    System.out.println(" D        │ En partie           │ Ouvrir Search Controller");
    System.out.println(" E        │ Menu                │ Démarrer l'éditeur de positions");
    System.out.println(" F        │ En partie / Editeur │ Informations sur la position");
    System.out.println(" G        │ En partie           │ Afficher / Masquer le graphique");
    System.out.println(" H        │ Partout             │ Raccourcis clavier");
    System.out.println(" J        │ En partie           │ Evaluation statique du maire");
    System.out.println(" K        │ En partie / Editeur │ Retourner l'échiquier");
    System.out.println(" L        │ En partie / Editeur │ Épingler/Désépingler");
    System.out.println(" P        │ En partie           │ Afficher la PGN");
    System.out.println(" P        │ Éditeur             │ Coller le HTML (chess.com) pour récupérer la position");
    System.out.println(" Q (MAJ)  │ En partie / Editeur │ Revenir au menu");
    System.out.println(" R        │ En partie + Hacker  │ Forcer la détection de fin de partie du hacker sans fin");
    System.out.println(" S        │ En partie           │ Lancer perft 5");
    System.out.println(" T        │ En partie           │ Fonction de tests");
    System.out.println(" V        │ En partie           │ Afficher les variantes");
    System.out.println(" W        │ En partie + Hacker  │ Forcer le redémarrage d'une partie du hacker sans fin");
    System.out.println(" HAUT     │ En partie           │ Augmenter délai par coups");
    System.out.println(" BAS      │ En partie           │ Diminuer délai par coups");
    System.out.println(" GAUCHE   │ En partie           │ Reculer d'un coup (uniquement en pause)");
    System.out.println(" DROITE   │ En partie           │ Avancer d'un coup (uniquement en pause)");
    System.out.println(" ESPACE   │ En partie           │ Pause/Play");
    System.out.println(" ENTRER   │ Menu                │ Démarrer la partie");
    System.out.println(" SUPPR    │ Éditeur             │ Effacer la position");
  }
}
