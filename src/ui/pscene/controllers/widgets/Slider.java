public class Slider extends Widget<Slider> {
  private int backgroundColor;
  private int frontColor;
  private int hoveredColor;
  private boolean hovered;
  private int value, min, max;
  private int graduationNumber = 0;
  private boolean lock = false;

  public Slider(SApplet sketch, float x, float y, float w, float h) {
    me = this;
    this.sketch = sketch;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;

    backgroundColor = sketch.color(130, 126, 64);
    frontColor = sketch.color(189, 189, 100);
    hoveredColor = sketch.color(214, 212, 111);

    min = 0;
    max = Math.round(w);
    value = max / 2;
  }

  public void show() {
    super.show();

    sketch.strokeWeight(1);
    sketch.fill(backgroundColor);
    sketch.stroke(backgroundColor);
    sketch.rectMode(sketch.CENTER);
    sketch.rect(x, y, w-1, h-1);

    float valueRectWidth = sketch.map(value, min, max, 0, h);
    if (value > min) {
      sketch.fill( (hovered ? hoveredColor : frontColor) );
      sketch.stroke( (hovered ? hoveredColor : frontColor) );
      sketch.rectMode(sketch.CORNER);
      sketch.rect(x - w/2, y + h/2 - valueRectWidth, w-1, valueRectWidth);
    }

    if (graduationNumber > 1) {
      float ecart = h / (graduationNumber);
      sketch.stroke(255);
      sketch.strokeWeight(1);
      for (int i = 1; i < graduationNumber + 1; i++) {
        sketch.line(x - 6*w/10, y + h/2 - i*ecart, x - 58*w/80, y + h/2 - i*ecart);
      }
    }

    sketch.textAlign(sketch.LEFT, sketch.CENTER);
    float tSize = w/4;
    sketch.textSize(tSize);
    sketch.fill(255);
    sketch.text(value, x + w/2 + w/16, sketch.constrain(y + h/2 - valueRectWidth, y - h/2 + tSize/2 - 1, y + h/2 - tSize/2 + 1));
  }

  public void onUserEvent(UserEvent e) {
    super.onUserEvent(e);
    if (e.mouseMoved()) {
       hovered = contains(e.x, e.y);
    }
    else if (e.mouseDragged() && lock) {
      updateSlider(e.y);
    }
    else if (e.mouseReleased()) {
       lock = false;
    }
    else if (e.mousePressed() && contains(e.x, e.y)) {
      lock = true;
      updateSlider(e.y);
    }
  }

  private void updateSlider(int mouseY) {
    float yPos = -mouseY + y + h/2;
    if (yPos < min) {
      value = min;
      return;
    }
    if (yPos > h) {
      value = max;
      return;
    }
    value = Math.round(sketch.map(yPos, 0, h, min, max));
  }

  public int getValue() {
    return value;
  }

  public Slider setColors(int back, int front, int hovered) {
    backgroundColor = back;
    frontColor = front;
    hoveredColor = hovered;
    return this;
  }

  public Slider setValue(int value) {
    if (value > max || value < min) {
      Debug.error("Valeur du slider " + this + " en dehors des limites");
      return this;
    }
    this.value = value;
    return this;
  }

  public Slider setMinimax(int min, int max) {
    this.min = min;
    this.max = max;
    return this;
  }

  public Slider setGraduations(int num) {
    if (num <= 1) Debug.error("Graduation minimum pour les sliders : 2");
    graduationNumber = num;
    return this;
  }
}
