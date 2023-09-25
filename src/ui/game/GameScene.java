import processing.core.PSurface;
import processing.core.PImage;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

public class GameScene extends Scene {

  private int w = Config.UI.caseWidth;
  private float offsetX = Config.UI.offsetX;
  private float offsetY = Config.UI.offsetY;
  private int pov = Player.White;
  private boolean attach = true;
  private GameManager game;
  private BoardDisplay boardDisplay;

  // Utiliser plutôt des objets Player
  private PImage whiteImage, blackImage;

  public GameScene(Main sketch) {
    this.sketch = sketch;
    width = Math.round(offsetX + 8*w);
    height = Math.round(offsetY + 8*w);
    game = sketch.game;

    whiteImage = sketch.loadImage("data/joueurs/humainImg.jpg");
    blackImage = sketch.loadImage("data/joueurs/lemaireImg.jpg");

    init();
  }

  public void awake() {
    Debug.log("UI", "Nouvelle scène : Partie");
    PSurface surface = sketch.getSurface();
    sketch.setTitle("Humain contre LeMaire");
    // sketch.setTitle(game.getWhite().name + " contre " + game.getBlack().name);
    surface.setSize(width, height);
    surface.setLocation(sketch.displayWidth-width, 0);
    surface.setAlwaysOnTop(attach);
    surface.setVisible(true);
  }

  public void draw() {
    sketch.background(49, 46, 43);
    sketch.fill(rgb(201, 186, 155));
    sketch.noStroke();
    sketch.rectMode(sketch.CORNER);
    sketch.rect(offsetX, offsetY, 8*w, 8*w);

    float space = 0.19f * w;
    float whiteImgY = (pov == Player.White ? height - (space + w/2) : offsetY + w/2);
    float blackImgY = (pov == Player.Black ? height - (space + w/2) : offsetY + w/2);
    sketch.imageMode(sketch.CENTER);
    sketch.image(whiteImage, offsetX/2, whiteImgY, w, w);
    sketch.image(blackImage, offsetX/2, blackImgY, w, w);

    float whiteTextY = (pov == Player.White ? height - (space*2 + w) : offsetY + w + space);
    float blackTextY = (pov == Player.Black ? height - (space*2 + w) : offsetY + w + space);
    sketch.fill(255);
    sketch.textSize(12 * w / 70);
    sketch.textAlign(sketch.CENTER, sketch.CENTER);
    sketch.text("Humain (???)", offsetX/2, whiteTextY);
    sketch.text("LeMaire (3845)", offsetX/2, blackTextY);

    // float whiteEvalY = (pov == Player.White ? height - (space*3.5f + 82*w/70) : offsetY + space*2.5f + 82*w/70);
    // float blackEvalY = (pov == Player.Black ? height - (space*3.5f + 82*w/70) : offsetY + space*2.5f + 82*w/70);
    // sketch.text("Eval : MAT EN 1", offsetX/2, blackEvalY);
    // sketch.text("Eval : 1,294", offsetX/2, whiteEvalY);

    showControllers();
    showOverlay();
  }

  private void toggleAttach() {
    attach = !attach;
    sketch.getSurface().setAlwaysOnTop(attach);
    Debug.log("ui", "Fenêtre " + (attach ? "épinglée" : "désépinglée"));
  }

  private void flipPov() {
    pov = Player.opponent(pov);
    boardDisplay.setPov(pov);
  }

  private void init() {
    controllers.clear();

    Board b = sketch.game.board;
    boardDisplay = new BoardDisplay(sketch, offsetX + 4*w, offsetY + 4*w, w, b);
    controllers.add(boardDisplay);

    addUpControllers();
    addLeftControllers();
  }

  private void addUpControllers() {
    int iconNumber = 10;
    float iconSize = 0.8f * offsetY;
    float edgeSpacing = (offsetX - w) / 2 + 1;
    float spacingBetweenIcons = (width - (edgeSpacing*2 + iconNumber*iconSize)) / (iconNumber-1);
    Function<Integer, Float> calcX = n -> iconSize/2 + edgeSpacing + n*iconSize + n*spacingBetweenIcons;

    Collections.addAll(controllers,
      new ImageToggle(sketch, calcX.apply(0), offsetY/2, iconSize, iconSize, "data/icons/pinOff.png", "data/icons/pin.png")
        .setState(true)
        .setAction( () -> toggleAttach() ),

      new ImageToggle(sketch, calcX.apply(1), offsetY/2, iconSize, iconSize, "data/icons/varianteGris.png", "data/icons/variante.png")
        .setAction( () -> Debug.log("todo", "Afficher les variantes (ou pas)") ),

      new ImageButton(sketch, calcX.apply(2), offsetY/2, iconSize, iconSize, "data/icons/analysis.png")
        .setAction( () -> Debug.log("todo", "Afficher l'analyse (ou pas)") ),

      new ImageButton(sketch, calcX.apply(3), offsetY/2, iconSize, iconSize, "data/icons/info.png")
        .setAction( () -> Debug.log("test", game.board.generateFEN()) ),

      new ImageButton(sketch, calcX.apply(4), offsetY/2, iconSize, iconSize, "data/icons/pgn.png")
        .setAction( () -> Debug.log("todo", "Afficher la PGN") ),

      new ImageButton(sketch, calcX.apply(5), offsetY/2, iconSize, iconSize, "data/icons/save.png")
        .setAction( () -> Debug.log("todo", "Sauvegarder la PGN") ),

      new ImageToggle(sketch, calcX.apply(6), offsetY/2, iconSize, iconSize, "data/icons/rotate1.png", "data/icons/rotate2.png")
        .setAction( () -> flipPov() ),

      new ImageToggle(sketch, calcX.apply(7), offsetY/2, iconSize, iconSize, "data/icons/pause.png", "data/icons/play.png")
        .setState(true)
        .setAction( () -> Debug.log("todo", "Pause / Play") ),

      new ImageButton(sketch, calcX.apply(8), offsetY/2, iconSize, iconSize, "data/icons/computer.png")
        .setAction( () -> Debug.log("todo", "Afficher Search Controller / Stats displayer (?)") ),

      new ImageButton(sketch, calcX.apply(9), offsetY/2, iconSize, iconSize, "data/icons/quit.png")
        .setAction( () -> sketch.sm.setScene(SceneIndex.Menu) )
    );
  }

  private void addLeftControllers() {
    float buttonSize = 38*w/70; // Taille des boutons
    float space = (offsetX - 2*buttonSize)/3; // Espacement entre les deux boutons (abandon et aide)
    float elementsSpacing = 0.19f * w; // Espacement entre les éléments de la barre verticale gauche

    Supplier<Float> whiteYPos = () -> (pov == Player.White
      ? height - (elementsSpacing*3.5f + w + buttonSize/2)
      : offsetY + elementsSpacing*2.5f + w + buttonSize/2);
    Supplier<Float> blackYPos = () -> (pov == Player.Black
      ? height - (elementsSpacing*3.5f + w + buttonSize/2)
      : offsetY + elementsSpacing*2.5f + w + buttonSize/2);

    Collections.addAll(controllers,
      new ImageButton(sketch, 0, 0, buttonSize, buttonSize, "data/icons/resign.png")
        .setFullSize(false)
        .setArrondi(10)
        .setAction( () -> Debug.log("todo", "Abandon blanc") )
        .setCondition( () -> !game.useHacker )
        .setMovablePosition(() -> space + buttonSize/2f, whiteYPos),

      new ImageButton(sketch, 0, 0, buttonSize, buttonSize, "data/icons/helpMove.png")
        .setFullSize(false)
        .setArrondi(10)
        .setAction( () -> Debug.log("todo", "Aide blanc") )
        .setCondition( () -> !game.useHacker )
        .setMovablePosition(() -> space*2 + 3*buttonSize/2f, whiteYPos),

      new ImageButton(sketch, 0, 0, buttonSize, buttonSize, "data/icons/resign.png")
        .setFullSize(false)
        .setArrondi(10)
        .setAction( () -> Debug.log("todo", "Abandon noir") )
        .setCondition( () -> !game.useHacker )
        .setMovablePosition(() -> space + buttonSize/2f, blackYPos),

      new ImageButton(sketch, 0, 0, buttonSize, buttonSize, "data/icons/helpMove.png")
        .setFullSize(false)
        .setArrondi(10)
        .setAction( () -> Debug.log("todo", "Aide noir") )
        .setCondition( () -> !game.useHacker )
        .setMovablePosition(() -> space*2 + 3*buttonSize/2f, blackYPos),

      new TextButton(sketch, offsetX/2, offsetY + 4*w - 16*w/70, "Revanche", 15 * w/70, 3)
        .setDimensions(79 * w / 70, 26 * w / 70)
        .setAction( () -> Debug.log("todo", "Revanche") )
        .setCondition( () -> game.gameEnded && !game.useHacker ),

      new TextButton(sketch, offsetX/2, offsetY + 4*w + 16*w/70, "Menu", 15 * w/70, 3)
        .setDimensions(79 * w / 70, 26 * w / 70)
        .setAction( () -> Debug.log("todo", "Menu") )
        .setCondition( () -> game.gameEnded && !game.useHacker )

      // Condition : gameState == GAME && !useHacker && !gameEnded && isHumain(0)
    );
  }
}
