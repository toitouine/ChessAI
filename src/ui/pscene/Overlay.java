// Scène particulière qui s'affiche par dessus une autre scène
// Empêche les controllers inaccessibles d'être activé par des events

public abstract class Overlay extends Scene {
  protected Scene scene;
  protected float x, y;

  public boolean contains(int xmouse, int ymouse) {
    return (xmouse >= x-width/2 && xmouse < x+width/2 && ymouse >= y-height/2 && ymouse < y+height/2);
  }

  final public void awake() {
    Debug.error("Impossible d'appeler awake() sur un overlay");
  }
}