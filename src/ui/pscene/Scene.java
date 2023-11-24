import java.util.ArrayList;

// Une scène affiche à l'écran différents éléments (dans draw())
// et des controllers (avec l'arraylist controllers)
// Pour afficher une scène, utiliser show() et awake() pour la préparer avant son lancement

public abstract class Scene<T extends SApplet> {
  protected T sketch;
  protected ArrayList<Controller> controllers = new ArrayList<Controller>();
  protected int width; // Largeur de la fenêtre
  protected int height; // Hauteur de la fenêtre
  private Overlay currentOverlay = null;

  public Scene(T sketch, int width, int height) {
    this.sketch = sketch;
    this.width = width;
    this.height = height;
  }

  abstract protected void setup(); // Appelée une fois au lancement de la scène
  abstract protected void draw();  // Appelée continuellement si la scène est active

  public void awake() {
    sketch.getSurface().setSize(width, height);
    setup();
  }

  final public void show() {
    draw();
    showControllers();
    showOverlay();
  }

  private void showControllers() {
    for (Controller c : controllers) {
      if (c.isEnabled()) c.show();
    }
  }

  private void showOverlay() {
    if (currentOverlay != null) currentOverlay.show();
  }

  protected void setOverlay(Overlay overlay) {
    currentOverlay = overlay;
  }

  protected void toggleOverlay(Overlay overlay) {
    if (currentOverlay != overlay) currentOverlay = overlay;
    else currentOverlay = null;
  }

  public void onUserEvent(UserEvent e) {
    if (currentOverlay != null && currentOverlay.contains(e.x, e.y)) {
      currentOverlay.onUserEvent(e);
      return;
    }

    for (Controller c : controllers) {
      if (c.isEnabled()) {
        c.onUserEvent(e);
        if (c.contains(e.x, e.y)) return;
      }
    }

    if (e.mouseMoved()) sketch.cursor(sketch.ARROW);
  }

  protected int rgb(float r, float g, float b) {
    return sketch.color(r, g, b);
  }
}
