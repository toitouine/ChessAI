import processing.core.PSurface;
import processing.core.PImage;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

public class EditorScene extends Scene {

  private int w = Config.UI.caseWidth;
  private float offsetX = Config.UI.offsetX;
  private float offsetY = Config.UI.offsetY;
  private boolean attach = true;
  private int pov = Player.White;

  private Overlay settings, fens;

  public EditorScene(Main sketch) {
    this.sketch = sketch;
    width = Math.round(offsetX + 8*w);
    height = Math.round(offsetY + 8*w);

    settings = new SettingsOverlay(this, offsetX + 4*w, offsetY+4*w, 8*w, 8*w);
    fens = new FenOverlay(this, offsetX + 4*w, offsetY+4*w, 8*w, 8*w);

    init();
  }

  public void awake() {
    Debug.log("UI", "Nouvelle scène : Editeur");
    PSurface surface = sketch.getSurface();
    sketch.setTitle("Editeur de position");
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

    sketch.stroke(0);
    sketch.strokeWeight(1);
    for (int i = 0; i < 8; i++) {
      sketch.line(offsetX + i * w, offsetY, offsetX+i*w, sketch.height);
      sketch.line(offsetX, offsetY + i * w, sketch.width, offsetY + i * w);
    }

    showControllers();
    showOverlay();
  }

  private void toggleSettings() {
    if (currentOverlay != settings) currentOverlay = settings;
    else currentOverlay = null;
  }

  public void toggleFens() {
    if (currentOverlay != fens) currentOverlay = fens;
    else currentOverlay = null;
  }

  private void flipPov() {
    pov = Player.opponent(pov);
  }

  private void toggleAttach() {
    attach = !attach;
    sketch.getSurface().setAlwaysOnTop(attach);
    Debug.log("ui", "Fenêtre " + (attach ? "épinglée" : "désépinglée"));
  }

  private void init() {
    controllers.clear();
    addUpControllers();
    addLeftControllers();
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
        .setAction( () -> toggleAttach() ),

      new ImageButton(sketch, calcX.apply(1), offsetY/2, iconSize, iconSize, "data/icons/delete.png")
        .setAction( () -> Debug.log("todo", "Supprimer la position") ),

      new ImageButton(sketch, calcX.apply(2), offsetY/2, iconSize, iconSize, "data/icons/copy.png")
        .setAction( () -> Debug.log("todo", "Copier la FEN") ),

      new ImageButton(sketch, calcX.apply(3), offsetY/2, iconSize, iconSize, "data/icons/info.png")
        .setAction( () -> Debug.log("todo", "Afficher les informations") ),

      new ImageButton(sketch, calcX.apply(4), offsetY/2, iconSize, iconSize, "data/icons/pawn.png")
        .setAction( () -> toggleFens() ),

      new ImageButton(sketch, calcX.apply(5), offsetY/2, iconSize, iconSize, "data/icons/paste.png")
        .setAction( () -> Debug.log("todo", "HTML de la position") ),

      new ImageButton(sketch, calcX.apply(6), offsetY/2, iconSize, iconSize, "data/icons/parameter.png")
        .setAction( () -> toggleSettings() ),

      new ImageToggle(sketch, calcX.apply(7), offsetY/2, iconSize, iconSize, "data/icons/rotate1.png", "data/icons/rotate2.png")
        .setAction( () -> flipPov() ),

      new ImageButton(sketch, calcX.apply(8), offsetY/2, iconSize, iconSize, "data/icons/quit.png")
        .setAction( () -> sketch.sm.setScene(SceneIndex.Menu) )
    );
  }

  private void addLeftControllers() {
    Collections.addAll(controllers,
      new ImageToggle(sketch, offsetX/2, height - 0.5f*offsetX, offsetX/1.2f, offsetX/1.2f, "data/icons/switchColor1.png", "data/icons/switchColor2.png")
        .setAction( () -> Debug.log("todo", "Color switch") )
    );
  }
}
