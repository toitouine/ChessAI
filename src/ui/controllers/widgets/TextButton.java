public class TextButton extends CallableWidget<TextButton> {
  private int textSize;
  private int arrondi;
  private String text;
  private int backColor;
  private int textColor;

  TextButton(Main sketch, float x, float y, String t, int tSize, int arrondi) {
    me = this;
    this.sketch = sketch;
    this.x = x;
    this.y = y;
    this.text = t;
    this.textSize = tSize;
    this.arrondi = arrondi;

    backColor = sketch.color(29, 28, 26);
    textColor = sketch.color(255, 255, 255);

    sketch.textSize(textSize);
    this.w = sketch.textWidth(text) * 1.1f;
    this.h = textSize * 1.76f;
  }

  TextButton(Main sketch, float x, float y, String t, int tSize) {
    this(sketch, x, y, t, tSize, 8);
  }

  public void show() {
    super.show();
    sketch.noStroke();
    sketch.fill(backColor);
    sketch.rectMode(sketch.CENTER);
    sketch.rect(x, y, w, h, arrondi);

    sketch.textAlign(sketch.CENTER, sketch.CENTER);
    sketch.textSize(textSize);
    sketch.fill(textColor);
    sketch.text(text, x, y - textSize/8);
  }

  TextButton setDimensions(float wi, float he) {
    w = wi;
    h = he;
    return this;
  }

  TextButton setColors(int b, int t) {
    backColor = b;
    textColor = t;
    return this;
  }

  TextButton setArrondi(int a) {
    arrondi = a;
    return this;
  }
}
