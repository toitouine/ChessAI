import processing.core.PImage;

public class ImageButton extends CallableWidget<ImageButton> {
  private PImage img;
  private boolean fullSize = true;
  private int arrondi = 5;
  private int backgroundColor;

  ImageButton(SApplet sketch, float x, float y, float w, float h, String imgPath) {
    this(sketch, x, y, w, h, sketch.loadImage(imgPath));
  }

  ImageButton(SApplet sketch, float x, float y, float w, float h, PImage img) {
    me = this;
    this.sketch = sketch;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.img = img;
    this.backgroundColor = sketch.color(40, 37, 34);
  }

  public void show() {
    super.show();
    sketch.imageMode(sketch.CENTER);
    if (fullSize) {
      sketch.image(img, x, y, w, h);
      return;
    }

    sketch.rectMode(sketch.CENTER);
    sketch.fill(backgroundColor);
    sketch.noStroke();
    sketch.rect(x, y, w, h, arrondi, arrondi, arrondi, arrondi);
    sketch.image(img, x, y, w/1.9f, h/1.9f);
  }

  ImageButton setBackgroundColor(int color) {
    backgroundColor = color;
    return this;
  }

  ImageButton setArrondi(int r) {
    arrondi = r;
    return this;
  }

  ImageButton setFullSize(boolean full) {
    fullSize = full;
    return this;
  }
}
