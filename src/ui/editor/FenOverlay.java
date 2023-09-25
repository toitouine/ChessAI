import java.util.Collections;

public class FenOverlay extends Overlay {

  int w = Config.UI.caseWidth;

  FenOverlay(Scene scene, float x, float y, int width, int height) {
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
    float size = 145 * w / 70;
    float offstart = 25 * w / 70;
    float offend = 30 * w / 70;
    float espx = size + ((width - 3*size - 2*offstart) / 2);
    float espy = size + ((width - 3*size - offstart - offend) / 2);
    float x = this.x-4*w;
    float y = this.y-4*w;
    EditorScene scene = (EditorScene)this.scene;

    controllers.clear();
    Collections.addAll(controllers,
      new ImageButton(sketch, x+offstart+size/2+0*espx, y+offstart+size/2+0*espy, size, size, "data/positions/position_0.png")
        .setCaptionSize(17 * w /70)
        .setCaption("Position de départ")
        .setAction( () -> scene.toggleFens() ),

      new ImageButton(sketch, x+offstart+size/2+1*espx, y+offstart+size/2+0*espy, size, size, "data/positions/position_1.png")
        .setCaptionSize(17 * w /70)
        .setCaption("Vecteur vitesse")
        .setAction( () -> scene.toggleFens() ),

      new ImageButton(sketch, x+offstart+size/2+2*espx, y+offstart+size/2+0*espy, size, size, "data/positions/position_2.png")
        .setCaptionSize(17 * w /70)
        .setCaption("Mat à l'étouffé")
        .setAction( () -> scene.toggleFens() ),

      new ImageButton(sketch, x+offstart+size/2+0*espx, y+offstart+size/2+1*espy, size, size, "data/positions/position_3.png")
        .setCaptionSize(17 * w /70)
        .setCaption("Mat roi-tour")
        .setAction( () -> scene.toggleFens() ),

      new ImageButton(sketch, x+offstart+size/2+1*espx, y+offstart+size/2+1*espy, size, size, "data/positions/position_4.png")
        .setCaptionSize(17 * w /70)
        .setCaption("Opposition")
        .setAction( () -> scene.toggleFens() ),

      new ImageButton(sketch, x+offstart+size/2+2*espx, y+offstart+size/2+1*espy, size, size, "data/positions/position_5.png")
        .setCaptionSize(17 * w /70)
        .setCaption("Check-check-check")
        .setAction( () -> scene.toggleFens() ),

      new ImageButton(sketch, x+offstart+size/2+0*espx, y+offstart+size/2+2*espy, size, size, "data/positions/position_6.png")
        .setCaptionSize(17 * w /70)
        .setCaption("Transpositions")
        .setAction( () -> scene.toggleFens() )
    );
  }
}
