import processing.core.PSurface;
import processing.core.PImage;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

public class GameScene extends Scene<MainApplet> {

  private int w = Config.UI.caseWidth;
  private float offsetX = Config.UI.offsetX;
  private float offsetY = Config.UI.offsetY;

  private MutableBoolean attach = new MutableBoolean(true);
  private MutableBoolean isWhitePov = new MutableBoolean(true);
  private MutableBoolean showVariante = new MutableBoolean(false); // TODO
  private MutableBoolean play = new MutableBoolean(true); // TODO
  private BoardDisplay boardDisplay;
  private Player white, black;
  private PImage whiteImage, blackImage;
  private PImage errorImage;

  private Game game;

  public GameScene(MainApplet sketch, int width, int height) {
    super(sketch, width, height);
    init();
  }

  protected void setup() {
    if (game == null) {
      Debug.error("Scène de partie activée mais aucune partie fournie");
      return;
    }

    Debug.log("UI", "Nouvelle scène : Partie");
    PSurface surface = sketch.getSurface();
    sketch.setTitle(game.getWhite().pseudo + " contre " + game.getBlack().pseudo);
    java.awt.Rectangle bounds = sketch.getScreenBounds();
    surface.setLocation(bounds.x + bounds.width-width, bounds.y);
    surface.setAlwaysOnTop(attach.get());
    surface.setVisible(true);

    white = game.getWhite();
    black = game.getBlack();
    whiteImage = sketch.loadImage("data/joueurs/" + white.name.toLowerCase() + "Img.jpg");
    blackImage = sketch.loadImage("data/joueurs/" + black.name.toLowerCase() + "Img.jpg");
    boardDisplay.setBoard(game.board);
  }

  protected void draw() {
    sketch.background(49, 46, 43);

    if (game == null) {
      sketch.imageMode(sketch.CENTER);
      sketch.image(errorImage, width - 4*w, height-4*w, 8*w, 8*w);
      return;
    }

    int pov = (isWhitePov.get() ? Player.White : Player.Black);

    float space = 0.19f * w;
    float whiteImgY, whiteTextY, whiteEvalY, whiteTimeY;
    float blackImgY, blackTextY, blackEvalY, blackTimeY;
    if (pov == Player.White) {
      whiteImgY = height - (space + w/2);
      blackImgY = offsetY + w/2;
      whiteTextY = height - (space*2 + w);
      blackTextY = offsetY + w + space;
      whiteEvalY = height - (space*3.5f + 82*w/70);
      blackEvalY = offsetY + space*2.5f + 82*w/70;
      whiteTimeY = offsetY + 4*w + 0.39f*w;
      blackTimeY = offsetY + 4*w - 0.39f*w;
    } else {
      whiteImgY = offsetY + w/2;
      blackImgY = height - (space + w/2);
      whiteTextY = offsetY + w + space;
      blackTextY = height - (space*2 + w);
      whiteEvalY = offsetY + space*2.5f + 82*w/70;
      blackEvalY = height - (space*3.5f + 82*w/70);
      whiteTimeY = offsetY + 4*w - 0.39f*w;
      blackTimeY = offsetY + 4*w + 0.39f*w;
    }

    sketch.imageMode(sketch.CENTER);
    sketch.fill(255);
    sketch.textSize(12 * w / 70);
    sketch.textAlign(sketch.CENTER, sketch.CENTER);

    sketch.image(whiteImage, offsetX/2, whiteImgY, w, w);
    sketch.image(blackImage, offsetX/2, blackImgY, w, w);
    sketch.text(white.pseudo + " (" + white.elo + ")", offsetX/2, whiteTextY);
    sketch.text(black.pseudo + " (" + black.elo + ")", offsetX/2, blackTextY);
    if (white.isBot) sketch.text("Eval : 1,294", offsetX/2, whiteEvalY);
    if (black.isBot) sketch.text("Eval : MAT EN 1", offsetX/2, blackEvalY);

    if (game.useTime) {
      sketch.rectMode(sketch.CENTER);
      sketch.textSize(23*w/70);

      if (!game.paused && game.board.tourDeQui == Player.White) sketch.fill(255);
      else sketch.fill(rgb(152, 151, 149));
      sketch.rect(offsetX/2, whiteTimeY, offsetX/1.15f, 45*w/70, 4);

      if (!game.paused && game.board.tourDeQui == Player.White) sketch.fill(rgb(38, 33, 27));
      else sketch.fill(rgb(97, 94, 91));
      sketch.text(game.timers[Player.White].formattedTime(), offsetX/2, whiteTimeY);

      if (!game.paused && game.board.tourDeQui == Player.Black) sketch.fill(rgb(38, 33, 27));
      else sketch.fill(rgb(43, 39, 34));
      sketch.rect(offsetX/2, blackTimeY, offsetX/1.15f, 45*w/70, 4);

      if (!game.paused && game.board.tourDeQui == Player.Black) sketch.fill(rgb(255, 255, 255));
      else sketch.fill(rgb(130, 128, 126));
      sketch.text(game.timers[Player.Black].formattedTime(), offsetX/2, blackTimeY);
    }
  }

  public void setGame(Game g) {
    game = g;
  }

  private void toggleAttach() {
    attach.toggle();
    sketch.getSurface().setAlwaysOnTop(attach.get());
    Debug.log("ui", "Fenêtre " + (attach.get() ? "épinglée" : "désépinglée"));
  }

  private void flipPov() {
    isWhitePov.toggle();
    boardDisplay.setPov(isWhitePov.get() ? Player.White : Player.Black);
  }

  private void init() {
    controllers.clear();

    errorImage = sketch.loadImage("data/icons/notfound.png");
    boardDisplay = new BoardDisplay(sketch, offsetX + 4*w, offsetY + 4*w, w);
    controllers.add(boardDisplay);

    addUpControllers();
    addLeftControllers();

    addShortcut("kK", this::flipPov);
    addShortcut("lL", this::toggleAttach);
    addShortcut('Q', sketch::goToMenu);
    addShortcut(' ', () -> play.toggle() );
    addShortcut("fF", () -> Debug.log(game.board) );
    addShortcut("vV", () -> showVariante.toggle() );
    // TODO
    // addShortcut("pP", () -> printPGN() );
    // addShortcut("gG", () -> toggleGraph() );
    // addShortcut("sS", () -> runPerft() );
    // addShortcut("dD", () -> toggleSearchController() );
    // addShortcut("bb", () -> highlightBook() );
    // addShortcut("cC", () -> savePGN() );
    // addShortcut(sketch.UP, () -> delayUp() );
    // addShortcut(sketch.DOWN, () -> delayDown() );
    // addShortcut(sketch.LEFT, () -> rewindBack() );
    // addShortcut(sketch.RIGHT, () -> rewindForward() );
    // (+ ceux du hacker)
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
        .setAction(this::toggleAttach)
        .linkTo(attach),

      new ImageToggle(sketch, calcX.apply(1), offsetY/2, iconSize, iconSize, "data/icons/varianteOff.png", "data/icons/variante.png")
        .linkTo(showVariante),

      new ImageButton(sketch, calcX.apply(2), offsetY/2, iconSize, iconSize, "data/icons/analysis.png")
        .setAction( () -> Debug.log("todo", "Afficher l'analyse (ou pas)") ),

      new ImageButton(sketch, calcX.apply(3), offsetY/2, iconSize, iconSize, "data/icons/info.png")
        .setAction( () -> Debug.log(game.board) ),

      new ImageButton(sketch, calcX.apply(4), offsetY/2, iconSize, iconSize, "data/icons/pgn.png")
        .setAction( () -> Debug.log("todo", "Afficher la PGN") ),

      new ImageButton(sketch, calcX.apply(5), offsetY/2, iconSize, iconSize, "data/icons/save.png")
        .setAction( () -> Debug.log("todo", "Sauvegarder la PGN") ),

      new ImageToggle(sketch, calcX.apply(6), offsetY/2, iconSize, iconSize, "data/icons/rotate1.png", "data/icons/rotate2.png")
        .setAction(this::flipPov)
        .linkTo(isWhitePov),

      new ImageToggle(sketch, calcX.apply(7), offsetY/2, iconSize, iconSize, "data/icons/pause.png", "data/icons/play.png")
        .setState(true)
        .linkTo(play),

      new ImageButton(sketch, calcX.apply(8), offsetY/2, iconSize, iconSize, "data/icons/computer.png")
        .setAction( () -> Debug.log("todo", "Afficher Search Controller / Stats displayer (?)") ),

      new ImageButton(sketch, calcX.apply(9), offsetY/2, iconSize, iconSize, "data/icons/quit.png")
        .setAction(sketch::goToMenu)
    );
  }

  private void addLeftControllers() {
    float buttonSize = 38*w/70; // Taille des boutons
    float space = (offsetX - 2*buttonSize)/3; // Espacement entre les deux boutons (abandon et aide)
    float elementsSpacing = 0.19f * w; // Espacement entre les éléments de la barre verticale gauche

    Supplier<Float> whiteYPos = () -> (isWhitePov.get()
      ? height - (elementsSpacing*3.5f + w + buttonSize/2)
      : offsetY + elementsSpacing*2.5f + w + buttonSize/2);
    Supplier<Float> blackYPos = () -> (!isWhitePov.get()
      ? height - (elementsSpacing*3.5f + w + buttonSize/2)
      : offsetY + elementsSpacing*2.5f + w + buttonSize/2);

    Collections.addAll(controllers,
      new ImageButton(sketch, 0, 0, buttonSize, buttonSize, "data/icons/resign.png")
        .setFullSize(false)
        .setArrondi(10)
        .setAction( () -> Debug.log("todo", "Abandon blanc") )
        .setCondition( () -> !game.useHacker && !game.gameEnded && !white.isBot )
        .setMovablePosition(() -> space + buttonSize/2f, whiteYPos),

      new ImageButton(sketch, 0, 0, buttonSize, buttonSize, "data/icons/helpMove.png")
        .setFullSize(false)
        .setArrondi(10)
        .setAction( () -> Debug.log("todo", "Aide blanc") )
        .setCondition( () -> !game.useHacker && !game.gameEnded && !white.isBot )
        .setMovablePosition(() -> space*2 + 3*buttonSize/2f, whiteYPos),

      new ImageButton(sketch, 0, 0, buttonSize, buttonSize, "data/icons/resign.png")
        .setFullSize(false)
        .setArrondi(10)
        .setAction( () -> Debug.log("todo", "Abandon noir") )
        .setCondition( () -> !game.useHacker && !game.gameEnded && !black.isBot )
        .setMovablePosition(() -> space + buttonSize/2f, blackYPos),

      new ImageButton(sketch, 0, 0, buttonSize, buttonSize, "data/icons/helpMove.png")
        .setFullSize(false)
        .setArrondi(10)
        .setAction( () -> Debug.log("todo", "Aide noir") )
        .setCondition( () -> !game.useHacker && !game.gameEnded && !black.isBot )
        .setMovablePosition(() -> space*2 + 3*buttonSize/2f, blackYPos),

      new TextButton(sketch, offsetX/2, offsetY + 4*w - 16*w/70, "Revanche", 15 * w/70, 3)
        .setDimensions(79 * w / 70, 26 * w / 70)
        .setAction( () -> Debug.log("todo", "Revanche") )
        .setCondition( () -> game.gameEnded && !game.useHacker ),

      new TextButton(sketch, offsetX/2, offsetY + 4*w + 16*w/70, "Menu", 15 * w/70, 3)
        .setDimensions(79 * w / 70, 26 * w / 70)
        .setAction( () -> Debug.log("todo", "Menu") )
        .setCondition( () -> game.gameEnded && !game.useHacker )
    );
  }
}
