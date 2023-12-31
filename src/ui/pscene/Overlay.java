/////////////////////////////////////////////////////////////////

// Scène particulière qui s'affiche par dessus une autre scène
// Empêche les controllers inaccessibles d'être activé par des events

/////////////////////////////////////////////////////////////////

public abstract class Overlay<T extends SApplet> extends Scene<T> {
  protected Scene scene;
  protected float x, y;

  public Overlay(Scene<T> scene, float x, float y, int width, int height) {
    super(scene.sketch, width, height);
    this.scene = scene;
    this.x = x;
    this.y = y;
  }

  public boolean contains(int xmouse, int ymouse) {
    return (xmouse >= x-width/2 && xmouse < x+width/2 && ymouse >= y-height/2 && ymouse < y+height/2);
  }

  @Override
  final public void awake() {
    Debug.error("Impossible d'appeler awake() sur un overlay");
  }

  final protected void setup() {
    Debug.error("Impossible d'appeler setup() sur un overlay");
  }
}
