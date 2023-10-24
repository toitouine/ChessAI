import processing.core.PImage;

public class ImageToggle extends CallableWidget<ImageToggle> {
  private PImage img1, img2;
  private boolean fullSize = true;
  private boolean state = false; // false = state 1, true = state 2
  private int arrondi = 5;
  private int backgroundColor;
  private String caption1, caption2;

  ImageToggle(Applet sketch, float x, float y, float w, float h, String imgPath1, String imgPath2) {
    me = this;
    this.sketch = sketch;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.img1 = sketch.loadImage(imgPath1);
    this.img2 = sketch.loadImage(imgPath2);
    this.backgroundColor = sketch.color(40, 37, 34);
  }

  public void show() {
    super.show();
    sketch.imageMode(sketch.CENTER);
    PImage img = (state ? img2 : img1);

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

  public ImageToggle setCaptions(String c1, String c2) {
    caption1 = c1;
    caption2 = c2;
    if (caption1 != null && caption2 != null) setCaption( (state ? caption2 : caption1) );
    return this;
  }

  @Override
  public void onUserEvent(UserEvent e) {
    super.onUserEvent(e);
    if (e.mousePressed() && contains(e.x, e.y)) toggleState();
  }

  public ImageToggle setState(boolean s) {
    state = s;
    return this;
  }

  public ImageToggle toggleState() {
    state = !state;
    if (caption1 != null && caption2 != null) setCaption( (state ? caption2 : caption1) );
    return this;
  }

  public ImageToggle setBackgroundColor(int color) {
    backgroundColor = color;
    return this;
  }

  public ImageToggle setArrondi(int r) {
    arrondi = r;
    return this;
  }

  public ImageToggle setFullSize(boolean full) {
    fullSize = full;
    return this;
  }
}
