import processing.core.PImage;

public class Selector extends Widget<Selector> {
  private PImage[] imgs;
  private PImage leftArrow, rightArrow;
  private int index = 0;

  Selector(Main sketch, float x, float y, float w, float h, PImage[] imgs) {
    me = this;
    this.sketch = sketch;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.imgs = imgs;

    leftArrow = sketch.loadImage("data/icons/arrowLeft.png");
    rightArrow = sketch.loadImage("data/icons/arrowRight.png");
  }

  public void show() {
    super.show();
    sketch.imageMode(sketch.CENTER);
    sketch.image(imgs[index], x, y, w, h);
    sketch.image(leftArrow, x - (3*w/4), y, h/4, h/4);
    sketch.image(rightArrow, x + (3*w/4), y, h/4, h/4);
  }

  public int getIndex() {
    return index;
  }

  public void randomize() {
    index = sketch.floor(sketch.random(0, imgs.length));
  }

  public boolean contains(int mx, int my) {
    return (isLeft(mx, my) || isRight(mx, my));
  }

  private boolean isLeft(int mx, int my) {
    return (mx >= x - (7*w/8) && mx < x - (5*w/8) && my >= y - w/8 && my < y + w/8);
  }

  private boolean isRight(int mx, int my) {
    return (mx >= x + (5*w/8) && mx < x + (7*w/8) && my >= y - w/8 && my < y + w/8);
  }

  public void onUserEvent(UserEvent e) {
    super.onUserEvent(e);
    if (e.mousePressed() && contains(e.x, e.y)) {
      if (isLeft(sketch.mouseX, sketch.mouseY)) index--;
      else index++;

      if (index == -1) index = imgs.length-1;
      else if (index == imgs.length) index = 0;
    }
  }
}
