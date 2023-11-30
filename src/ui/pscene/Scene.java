// Une scène affiche à l'écran différents éléments (dans draw())
// et des controllers (avec l'arraylist controllers)
// Pour afficher une scène, utiliser show() et awake() pour la préparer avant son lancement

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Scene<T extends SApplet> {
  protected T sketch;
  protected ArrayList<Controller> controllers = new ArrayList<Controller>();
  protected int width; // Largeur de la fenêtre
  protected int height; // Hauteur de la fenêtre
  private Overlay currentOverlay = null;
  private HashMap<Character, Callback> shortcutsChar = new HashMap<Character, Callback>();
  private HashMap<Integer, Callback> shortcutsCode = new HashMap<Integer, Callback>();

  public Scene(T sketch, int width, int height) {
    this.sketch = sketch;
    this.width = width;
    this.height = height;

    addShortcut("hH", Main::printHelpMenu);
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

  protected void addShortcut(char c, Callback callback) {
    shortcutsChar.put(c, callback);
  }

  protected void addShortcut(String s, Callback callback) {
    for (int i = 0; i < s.length(); i++) {
      shortcutsChar.put(s.charAt(i), callback);
    }
  }

  protected void addShortcut(int code, Callback callback) {
    shortcutsCode.put(code, callback);
  }

  protected void setOverlay(Overlay overlay) {
    currentOverlay = overlay;
  }

  protected void toggleOverlay(Overlay overlay) {
    if (currentOverlay != overlay) currentOverlay = overlay;
    else currentOverlay = null;
  }

  public void onUserEvent(UserEvent e) {
    if (e.keyPressed()) {
      if (e.key != sketch.CODED) {
        if (shortcutsChar.get(e.key) != null) shortcutsChar.get(e.key).call();
      }
      else {
        if (shortcutsCode.get(e.keyCode) != null) shortcutsCode.get(e.keyCode).call();
      }
    }

    if (currentOverlay != null) {
      if (e.keyPressed() || currentOverlay.contains(e.x, e.y)) {
        currentOverlay.onUserEvent(e);
        return;
      }
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

@FunctionalInterface
interface Callback {
  void call();
}
