final public class MainApplet extends SApplet {
  private GameScene gameScene;
  private MenuScene menuScene;
  private EditorScene editorScene;

  public void setup() {
    textFont(createFont("data/fonts/LucidaSans.ttf", 12));
    printStartMessage();

    int boardWindowWidth = Math.round(Config.UI.offsetX + 8*Config.UI.caseWidth);
    int boardWindowHeight = Math.round(Config.UI.offsetY + 8*Config.UI.caseWidth);

    menuScene = new MenuScene(this, 1100, 460);
    gameScene = new GameScene(this, boardWindowWidth, boardWindowHeight);
    editorScene = new EditorScene(this, boardWindowWidth, boardWindowHeight);

    register(menuScene, SceneIndex.Menu);
    register(gameScene, SceneIndex.Game);
    register(editorScene, SceneIndex.Editor);
    setScene(SceneIndex.Menu);
  }

  public void goToMenu() {
    setScene(SceneIndex.Menu);
  }

  public void goToEditor() {
    setScene(SceneIndex.Editor);
  }

  public void startDisplayGame(Player p1, Player p2, String startFEN, Timer t1, Timer t2, boolean useHacker) {
    GameManager gm = GameManager.getInstance();
    Game game = gm.addGame(p1, p2, startFEN, t1, t2, useHacker);
    gameScene.setGame(game);
    setScene(SceneIndex.Game);
  }

  public static void printStartMessage() {
    println();
    println("─────────────────────────────────────────────");
    println(Config.General.name + ", Antoine Mechulam");
    println("(https://github.com/toitouine/ChessAI)");
    println();
    println("IAs disponibles :");
    println("- LeMaire : Très bon en ouverture et en finale");
    println("- LesMoutons : Voleur, arnaqueur, tricheur, menaces en un !!");
    println("- Loic : Plutôt mauvais, préfère pater que mater");
    println("- Antoine : Un jeu aléatoire de qualité");
    println("- Stockfish : Extrêmement difficile de perdre contre lui");
    println();
    println("Voir fichier Config.java pour les options / paramètres");
    println("Appuyer sur H pour afficher l'aide (raccourcis claviers)");
    println();
    println("/!\\ La direction rejette toute responsabilité en cas de CPU détruit par ce programme ou d'ordinateur brulé.");
    println("─────────────────────────────────────────────");
    println();
  }

  public static void printHelpMenu() {
    println(" ");
    println("  Touche  │       Endroit       │       Description       ");
    println("──────────┼─────────────────────┼─────────────────────────");
    println(" B        │ En partie           │ Afficher les coups du livre d'ouverture");
    println(" C        │ En partie           │ Sauvegarder la PGN");
    println(" C        │ Éditeur             │ Copier la FEN");
    println(" D        │ En partie           │ Ouvrir Search Controller");
    println(" E        │ Menu                │ Démarrer l'éditeur de positions");
    println(" F        │ En partie / Editeur │ Informations sur la position");
    println(" G        │ En partie           │ Afficher / Masquer le graphique");
    println(" H        │ Partout             │ Raccourcis clavier");
    println(" J        │ En partie           │ Evaluation statique du maire");
    println(" K        │ En partie / Editeur │ Retourner l'échiquier");
    println(" L        │ En partie / Editeur │ Épingler/Désépingler");
    println(" P        │ En partie           │ Afficher la PGN");
    println(" P        │ Éditeur             │ Coller le HTML (chess.com) pour récupérer la position");
    println(" Q (MAJ)  │ En partie / Editeur │ Revenir au menu");
    println(" R        │ En partie + Hacker  │ Forcer la détection de fin de partie du hacker sans fin");
    println(" S        │ En partie           │ Lancer perft 5");
    println(" T        │ En partie           │ Fonction de tests");
    println(" V        │ En partie           │ Afficher les variantes");
    println(" W        │ En partie + Hacker  │ Forcer le redémarrage d'une partie du hacker sans fin");
    println(" HAUT     │ En partie           │ Augmenter délai par coups");
    println(" BAS      │ En partie           │ Diminuer délai par coups");
    println(" GAUCHE   │ En partie           │ Reculer d'un coup (uniquement en pause)");
    println(" DROITE   │ En partie           │ Avancer d'un coup (uniquement en pause)");
    println(" ESPACE   │ En partie           │ Pause/Play");
    println(" ENTRER   │ Menu                │ Démarrer la partie");
    println(" SUPPR    │ Éditeur             │ Effacer la position");
  }
}
