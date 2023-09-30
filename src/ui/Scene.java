import java.util.ArrayList;

public abstract class Scene {
  protected Main sketch;
  protected ArrayList<Controller> controllers = new ArrayList<Controller>();
  protected int width; // Largeur de la fenêtre
  protected int height; // Hauteur de la fenêtre
  protected Overlay currentOverlay = null;

  abstract void awake(); // Appelée une fois au lancement de la scène
  abstract void draw();

  protected void showControllers() {
    for (Controller c : controllers) {
      if (c.isEnabled()) c.show();
    }
  }

  protected void showOverlay() {
    if (currentOverlay !=  null) currentOverlay.draw();
  }

  protected int rgb(float r, float g, float b) {
    return sketch.color(r, g, b);
  }

  final public void onUserEvent(UserEvent e) {
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
}
