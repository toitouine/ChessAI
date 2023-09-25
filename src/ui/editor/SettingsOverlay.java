import java.util.Collections;

public class SettingsOverlay extends Overlay {

  int w = Config.UI.caseWidth;

  SettingsOverlay(Scene scene, float x, float y, int width, int height) {
    this.scene = scene;
    this.sketch = scene.sketch;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;

    init();
  }

  public void draw() {
    sketch.fill(220, 220, 220, 220);
    sketch.rectMode(sketch.CENTER);
    sketch.rect(x, y, width, height);

    showControllers();
    showOverlay();
  }

  private void init() {
    controllers.clear();
    Collections.addAll(controllers,
      // Trait
      new ImageToggle(sketch, x, y - 2.5f*w, 1.25f*w, 1.25f*w, "data/board/roi_b.png", "data/board/roi_n.png")
        .setAction( () -> Debug.log("todo", "Changer le trait") )
        .setCaptions("Trait aux blancs", "Trait aux noirs")
        .setCaptionSize(20 * w/70),

      // Petit roque blanc
      new ImageToggle(sketch, x-1.75f*w, y+0.125f*w, 2.5f*w, 1.25f*w, "data/icons/pRoqueB_off.png", "data/icons/pRoqueB.png")
        .setAction( () -> Debug.log("todo", "Petit roque blanc") )
        .setCaptionSize(16.5f * w/70)
        .setCaptions("Petit roque blanc désactivé", "Petit roque blanc activé"),

      // Petit roque noir
      new ImageToggle(sketch, x-1.75f*w, y+2.125f*w, 2.5f*w, 1.25f*w, "data/icons/pRoqueN_off.png", "data/icons/pRoqueN.png")
        .setAction( () -> Debug.log("todo", "Petit roque noir") )
        .setCaptionSize(16.5f * w/70)
        .setCaptions("Petit roque noir désactivé", "Petit roque noir activé"),

      // Grand roque blanc
      new ImageToggle(sketch, x+2*w, y+0.125f*w, 3*w, 1.25f*w, "data/icons/gRoqueB_off.png", "data/icons/gRoqueB.png")
        .setAction( () -> Debug.log("todo", "Grand roque blanc") )
        .setCaptionSize(16.5f * w/70)
        .setCaptions("Grand roque blanc désactivé", "Grand roque blanc activé"),

      // Grand roque noir
      new ImageToggle(sketch, x+2*w, y+2.125f*w, 3*w, 1.25f*w, "data/icons/gRoqueN_off.png", "data/icons/gRoqueN.png")
        .setAction( () -> Debug.log("todo", "Grand roque noir") )
        .setCaptionSize(16.5f * w/70)
        .setCaptions("Grand roque noir désactivé", "Grand roque noir activé")
    );
  }
}

class TesterOverlay extends Overlay {
  TesterOverlay(Scene scene, float x, float y, int width, int height) {
    this.scene = scene;
    this.sketch = scene.sketch;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;

    init();
  }

  public void draw() {
    sketch.fill(0);
    sketch.stroke(255);
    sketch.strokeWeight(2);
    sketch.rectMode(sketch.CENTER);
    sketch.rect(x, y, width, height);

    showControllers();
  }

  private void init() {
    controllers.clear();
    Collections.addAll(controllers,
      new TextButton(sketch, x, y, "Squicky", 17)
        .setAction( () -> Debug.log("test", "hey") )
        .setArrondi(3)
    );
  }
}
