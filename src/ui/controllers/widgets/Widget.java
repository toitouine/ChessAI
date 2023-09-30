/////////////////////////////////////////////////////////////////

// Un widget est un type de controller qui regroupe les éléments de base
// de l'interface comme les boutons, sliders, sélecteurs...

/////////////////////////////////////////////////////////////////

import java.util.function.Supplier;

public abstract class Widget<T> extends Controller<T> {
  protected Supplier<Float> movableX, movableY;
  protected String caption = "";
  protected int captionColor = 0;
  protected float captionSize = -1;

  public void onUserEvent(UserEvent e) {
    if (contains(e.x, e.y)) sketch.cursor(sketch.HAND);
  }

  public void show() {
    if (!caption.equals("")) showCaption();
    if (movableX != null && movableY != null) {
      x = movableX.get();
      y = movableY.get();
    }
  }

  protected void showCaption() {
    sketch.textAlign(sketch.CENTER, sketch.CENTER);
    sketch.textSize(captionSize);
    sketch.fill(captionColor);
    sketch.text(caption, x, y + h/2 + captionSize/1.5f);
  }

  final public T setCaption(String c) {
    caption = c;
    if (captionSize == -1) captionSize = (h + w) / 16;
    return me;
  }

  final public T setCaptionSize(float s) {
    if (s <= 0) {
      Debug.error("Taille de légende demandée strictement positive");
      return me;
    }
    captionSize = s;
    return me;
  }

  final public T setCaption(String c, int color) {
    setCaption(c);
    setCaptionColor(color);
    return me;
  }

  final public T setCaptionColor(int color) {
    captionColor = color;
    return me;
  }

  final public T setMovablePosition(Supplier<Float> mx, Supplier<Float> my) {
    movableX = mx;
    movableY = my;
    return me;
  }
}

/////////////////////////////////////////////////////////////////

// Un callable widget est un type de widget qui effectue une action précise
// lorsque l'on clique dessus (callback)

/////////////////////////////////////////////////////////////////

abstract class CallableWidget<T> extends Widget<T> {
  protected Callback function;

  final public T setAction(Callback f) {
    function = f;
    return me;
  }

  @Override
  public void onUserEvent(UserEvent e) {
    super.onUserEvent(e);
    if (e.mousePressed() && contains(e.x, e.y) && function != null) function.call();
  }
}

@FunctionalInterface
interface Callback {
  void call();
}
