import processing.core.PSurface;
import processing.core.PImage;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

public class EditorScene extends Scene<MainApplet> {

  private int w = Config.UI.caseWidth;
  private float offsetX = Config.UI.offsetX;
  private float offsetY = Config.UI.offsetY;

  private MutableBoolean attach = new MutableBoolean(true);
  private MutableBoolean isWhitePov = new MutableBoolean(true);

  private Overlay settings, fens;

  public EditorScene(MainApplet sketch, int width, int height) {
    super(sketch, width, height);

    settings = new SettingsOverlay(this, offsetX + 4*w, offsetY+4*w, 8*w, 8*w);
    fens = new FenOverlay(this, offsetX + 4*w, offsetY+4*w, 8*w, 8*w);
    init();
  }

  protected void setup() {
    Debug.log("UI", "Nouvelle scène : Editeur");
    PSurface surface = sketch.getSurface();
    sketch.setTitle("Editeur de position");
    java.awt.Rectangle bounds = sketch.getScreenBounds();
    surface.setLocation(bounds.x + bounds.width-width, bounds.y);
    surface.setAlwaysOnTop(attach.get());
    surface.setVisible(true);
  }

  protected void draw() {
    sketch.background(49, 46, 43);
    sketch.fill(rgb(201, 186, 155));
    sketch.noStroke();
    sketch.rectMode(sketch.CORNER);
    sketch.rect(offsetX, offsetY, 8*w, 8*w);

    sketch.stroke(0);
    sketch.strokeWeight(1);
    for (int i = 0; i < 8; i++) {
      sketch.line(offsetX + i * w, offsetY, offsetX+i*w, sketch.height);
      sketch.line(offsetX, offsetY + i * w, sketch.width, offsetY + i * w);
    }
  }

  private void toggleAttach() {
    attach.toggle();
    sketch.getSurface().setAlwaysOnTop(attach.get());
    Debug.log("ui", "Fenêtre " + (attach.get() ? "épinglée" : "désépinglée"));
  }

  private void init() {
    controllers.clear();
    addUpControllers();
    addLeftControllers();

    addShortcut("lL", this::toggleAttach);
    addShortcut('Q', sketch::goToMenu);
    addShortcut("kK", () -> isWhitePov.toggle() );
    // TODO
    // addShortcut(sketch.BACKSPACE, () -> clearPosition() );
    // addShortcut('fF', () -> Debug.log(board) );
    // addShortcut('cC', () -> copyFEN() );
    // addShortcut('pP', () -> pasteHTMLtoBoard() );
  }

  private void addUpControllers() {
    int iconNumber = 9;
    float iconSize = 0.8f * offsetY;
    float edgeSpacing = (offsetX - w) / 2 + 1;
    float spacingBetweenIcons = (width - (edgeSpacing*2 + iconNumber*iconSize)) / (iconNumber-1);
    Function<Integer, Float> calcX = n -> iconSize/2 + edgeSpacing + n*iconSize + n*spacingBetweenIcons;

    Collections.addAll(controllers,
      new ImageToggle(sketch, calcX.apply(0), offsetY/2, iconSize, iconSize, "data/icons/pinOff.png", "data/icons/pin.png")
        .setState(true)
        .linkTo(attach)
        .setAction(this::toggleAttach),

      new ImageButton(sketch, calcX.apply(1), offsetY/2, iconSize, iconSize, "data/icons/delete.png")
        .setAction( () -> Debug.log("todo", "Supprimer la position") ),

      new ImageButton(sketch, calcX.apply(2), offsetY/2, iconSize, iconSize, "data/icons/copy.png")
        .setAction( () -> Debug.log("todo", "Copier la FEN") ),

      new ImageButton(sketch, calcX.apply(3), offsetY/2, iconSize, iconSize, "data/icons/info.png")
        .setAction( () -> Debug.log("todo", "Afficher les informations") ),

      new ImageButton(sketch, calcX.apply(4), offsetY/2, iconSize, iconSize, "data/icons/pawn.png")
        .setAction( () -> toggleOverlay(fens) ),

      new ImageButton(sketch, calcX.apply(5), offsetY/2, iconSize, iconSize, "data/icons/paste.png")
        .setAction( () -> Debug.log("todo", "HTML de la position") ),

      new ImageButton(sketch, calcX.apply(6), offsetY/2, iconSize, iconSize, "data/icons/parameter.png")
        .setAction( () -> toggleOverlay(settings) ),

      new ImageToggle(sketch, calcX.apply(7), offsetY/2, iconSize, iconSize, "data/icons/rotate1.png", "data/icons/rotate2.png")
        .linkTo(isWhitePov),

      new ImageButton(sketch, calcX.apply(8), offsetY/2, iconSize, iconSize, "data/icons/quit.png")
        .setAction(sketch::goToMenu)
    );
  }

  private void addLeftControllers() {
    Collections.addAll(controllers,
      new ImageToggle(sketch, offsetX/2, height - 0.5f*offsetX, offsetX/1.2f, offsetX/1.2f, "data/icons/switchColor1.png", "data/icons/switchColor2.png")
        .setAction( () -> Debug.log("todo", "Color switch") )
    );
  }
}
