// TODO :
// - Coder les overlays principaux (paramètres, fens...) OK !!
// - Board (structures de données uniquement) OK!!
// - FEN : try / catch
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
  public SceneManager sm;
  public GameManager game;

  public void setup() {
    printStartText();
    textFont(createFont("data/fonts/LucidaSans.ttf", 12));

    Time.init(this);
    Clipboard.init(this);
    Debug.init();

    game = new GameManager();
    sm = new SceneManager(this)
      .register(new MenuScene(this), SceneIndex.Menu)
      .register(new GameScene(this), SceneIndex.Game)
      .register(new EditorScene(this), SceneIndex.Editor)
      .setScene(SceneIndex.Menu);
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

  public void setTitle(String title) {
    if (title.equals("")) surface.setTitle(Config.General.name);
    else surface.setTitle(Config.General.name + " - " + title);
  }

  public PSurface getSurface() {
    return surface;
  }

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

  private void printStartText() {
    Debug.log();
    Debug.log("──────────────────────────");
    Debug.log(Config.General.name + ", Antoine Mechulam");
    Debug.log("(https://github.com/toitouine/ChessAI)");
    Debug.log();
    Debug.log("IAs disponibles :");
    Debug.log("- LeMaire : Très bon en ouverture et en finale");
    Debug.log("- LesMoutons : Voleur, arnaqueur, tricheur, menaces en un !!");
    Debug.log("- Loic : Plutôt mauvais, préfère pater que mater");
    Debug.log("- Antoine : Un jeu aléatoire de qualité");
    Debug.log("- Stockfish : Extrêmement difficile de perdre contre lui");
    Debug.log(" ");
    Debug.log("Voir fichier Config.java pour les options / paramètres");
    Debug.log("Appuyer sur H pour afficher l'aide (raccourcis claviers)");
    Debug.log();
    Debug.log("/!\\ La direction rejette toute responsabilité en cas de CPU détruit par ce programme ou d'ordinateur brulé.");
    Debug.log("──────────────────────────");
    Debug.log();
  }
}
