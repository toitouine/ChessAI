public class TextToggle extends CallableWidget<TextToggle> {
  private int textSize;
  private int arrondi = 8;
  private int backColor;
  private int textColor;
  private String text1, text2;
  private boolean state = false; // false = state 1, true = state 2
  private String caption1, caption2;

  TextToggle(SApplet sketch, float x, float y, String text1, String text2, int tSize) {
    me = this;
    this.sketch = sketch;
    this.x = x;
    this.y = y;
    this.text1 = text1;
    this.text2 = text2;
    this.textSize = tSize;

    backColor = sketch.color(29, 28, 26);
    textColor = sketch.color(255, 255, 255);

    sketch.textSize(textSize);
    String text = (text1.length() > text2.length() ? text1 : text2);
    this.w = sketch.textWidth(text)*1.1f;
    this.h = textSize * 1.76f;
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
    sketch.text(current(), x, y - textSize/8);
  }

  public String current() {
    return (state ? text2 : text1);
  }

  public boolean getState() {
    return state;
  }

  @Override
  public void onUserEvent(UserEvent e) {
    super.onUserEvent(e);
    if (e.mousePressed() && contains(e.x, e.y)) toggleState();
  }

  public TextToggle setCaptions(String c1, String c2) {
    caption1 = c1;
    caption2 = c2;
    if (caption1 != null && caption2 != null) setCaption( (state ? caption2 : caption1) );
    return this;
  }

  public TextToggle toggleState() {
    state = !state;
    if (caption1 != null && caption2 != null) setCaption( (state ? caption2 : caption1) );
    return this;
  }

  public TextToggle setState(boolean s) {
    state = s;
    return this;
  }

  TextToggle setDimensions(float w, float h) {
    this.w = w;
    this.h = h;
    return this;
  }

  TextToggle setColors(int b, int t) {
    this.backColor = b;
    this.textColor = t;
    return this;
  }

  TextToggle setArrondi(int a) {
    arrondi = a;
    return this;
  }
}
