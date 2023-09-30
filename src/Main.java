// TODO :
// - Coder les overlays principaux (paramètres, fens...) OK !!
// - Board (structures de données uniquement) OK!!
// - FEN : try / catch OK!!
// - UI : Board
// - Génération de coups
// - Déroulement de la partie, Game Manager, Player : Humain
// - UI : Suite
// - IA, Hacker, Book, PGN, FEN, et autre classes utiles
// - UI : Fin

import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

public class Main extends PApplet {
  private SceneManager sm;
  private GameManager game;
  private PImage notFoundImg;

  public void setup() {
    Time.init(this);
    Clipboard.init(this);
    Debug.init();

    printStartText();
    textFont(createFont("data/fonts/LucidaSans.ttf", 12));
    notFoundImg = loadImage("data/icons/notfound.png");

    game = new GameManager();
    sm = new SceneManager()
      .register(new MenuScene(this), SceneIndex.Menu)
      .register(new GameScene(this, game), SceneIndex.Game)
      .register(new EditorScene(this), SceneIndex.Editor);

    sm.setScene(SceneIndex.Menu);
  }

  public void draw() {
    game.update();
    sm.drawScene();
  }

  public static void main(String[] args) {
    String[] processingArgs = {Config.General.name};
    Main main = new Main();
    PApplet.runSketch(processingArgs, main);
  }

  /////////////////////////////////////////////////////////////////

  public void setScene(SceneIndex i) {
    sm.setScene(i);
  }

  public void startGame(Player p1, Player p2, String fen) {
    game.startGame(p1, p2, fen);
    sm.setScene(SceneIndex.Game);
  }

  public void toggleHacker() {
    game.useHacker = !game.useHacker;
  }

  public void setTitle(String title) {
    if (title.equals("")) surface.setTitle(Config.General.name);
    else surface.setTitle(Config.General.name + " - " + title);
  }

  public PSurface getSurface() {
    return surface;
  }

  @Override
  public PImage loadImage(String path) {
    PImage img = super.loadImage(path);
    if (img == null) {
      Debug.error("Image introuvable : " + path + " --> Ajout de l'image par défaut.");
      img = notFoundImg;
    }
    return img;
  }

  /////////////////////////////////////////////////////////////////

  public void keyPressed() {
    if (keyCode == ESC) key = 0;

    UserEvent event = new UserEvent(EventType.KeyPressed, keyCode);
    sm.onUserEvent(event);
  }

  public void mouseMoved() {
    UserEvent event = new UserEvent(EventType.MouseMoved, mouseX, mouseY);
    sm.onUserEvent(event);
  }

  public void mousePressed() {
    UserEvent event = new UserEvent(EventType.MousePressed, mouseX, mouseY);
    sm.onUserEvent(event);
  }

  public void mouseReleased() {
    UserEvent event = new UserEvent(EventType.MouseReleased, mouseX, mouseY);
    sm.onUserEvent(event);
  }

  public void mouseDragged() {
    UserEvent event = new UserEvent(EventType.MouseDragged, mouseX, mouseY);
    sm.onUserEvent(event);
  }

  /////////////////////////////////////////////////////////////////

  private void printStartText() {
    println();
    println("──────────────────────────");
    println(Config.General.name + ", Antoine Mechulam");
    println("(https://github.com/toitouine/ChessAI)");
    println();
    println("IAs disponibles :");
    println("- LeMaire : Très bon en ouverture et en finale");
    println("- LesMoutons : Voleur, arnaqueur, tricheur, menaces en un !!");
    println("- Loic : Plutôt mauvais, préfère pater que mater");
    println("- Antoine : Un jeu aléatoire de qualité");
    println("- Stockfish : Extrêmement difficile de perdre contre lui");
    println(" ");
    println("Voir fichier Config.java pour les options / paramètres");
    println("Appuyer sur H pour afficher l'aide (raccourcis claviers)");
    println();
    println("/!\\ La direction rejette toute responsabilité en cas de CPU détruit par ce programme ou d'ordinateur brulé.");
    println("──────────────────────────");
    println();
  }
}
