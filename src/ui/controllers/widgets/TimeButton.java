import java.util.Collections;
import java.util.ArrayList;

public class TimeButton extends Widget<TimeButton> {
  private float buttonHeight;
  private float singleRectWidth;
  private float espace;
  private float textSize;
  private int backgroundColor, textColor, hoveredColor;
  private float arrowEspace, arrowOffset;
  private Time time = Time.fromMillis(0);
  private Time increment = Time.fromMillis(0);
  private ArrayList<SubTimebutton> subButtons = new ArrayList<SubTimebutton>();

  TimeButton(Applet sketch, float x, float y, float h) {
    me = this;
    this.sketch = sketch;
    this.x = x;
    this.y = y;
    this.h = h;

    singleRectWidth = (h * 49) / 75;
    espace = (h * 7) / 75;
    buttonHeight = (h * 10) / 75;
    textSize = (h * 30) / 75;
    arrowEspace = (h * 5) / 75;
    arrowOffset = (h * 2) / 75;
    backgroundColor = sketch.color(240, 240, 240);
    textColor = sketch.color(38, 33, 27);
    hoveredColor = sketch.color(209, 207, 207);

    this.w = 3*singleRectWidth + espace;

    Collections.addAll(subButtons,
      new SubTimebutton(x - w/2, y - h/2, singleRectWidth, buttonHeight+1, true)
        .setArrondis(5, 0, 0, 0)
        .setTimeModifier(time, Time.fromMinutes(1), Time.fromSeconds(3599)),

      new SubTimebutton(x - w/2 + singleRectWidth, y - h/2, singleRectWidth, buttonHeight+1, true)
        .setArrondis(0, 5, 0, 0)
        .setTimeModifier(time, Time.fromSeconds(1), Time.fromSeconds(3599)),

      new SubTimebutton(x + w/2 - singleRectWidth, y - h/2, singleRectWidth, buttonHeight+1, true)
        .setArrondis(5, 5, 0, 0)
        .setTimeModifier(increment, Time.fromSeconds(1), Time.fromMinutes(1)),

      new SubTimebutton(x - w/2, y + h/2 - buttonHeight-1, singleRectWidth, buttonHeight+1, false)
        .setArrondis(0, 0, 0, 5)
        .setTimeModifier(time, Time.fromMinutes(-1), Time.fromSeconds(3599)),

      new SubTimebutton(x - w/2 + singleRectWidth, y + h/2 - buttonHeight-1, singleRectWidth, buttonHeight+1, false)
        .setArrondis(0, 0, 5, 0)
        .setTimeModifier(time, Time.fromSeconds(-1), Time.fromSeconds(3599)),

      new SubTimebutton(x + w/2 - singleRectWidth, y + h/2 - buttonHeight-1, singleRectWidth, buttonHeight+1, false)
        .setArrondis(0, 0, 5, 5)
        .setTimeModifier(increment, Time.fromSeconds(-1), Time.fromMinutes(1))
      );
  }

  public TimeButton setColors(int background, int text, int hovered) {
    backgroundColor = background;
    textColor = text;
    hoveredColor = hovered;
    return this;
  }

  public Time getTime() {
    return time;
  }

  public Time getIncrement() {
    return increment;
  }

  public void set(Time t, Time i) {
    time.setMillis(t.millis());
    increment.setMillis(i.millis());
  }

  public void show() {
    super.show();
    sketch.fill(backgroundColor);
    sketch.noStroke();
    sketch.rectMode(sketch.CORNER);
    sketch.rect(x - w/2, y - h/2 + buttonHeight, 2*singleRectWidth, h - 2*buttonHeight);
    sketch.rect(x - w/2 + 2*singleRectWidth + espace, y - h/2 + buttonHeight, singleRectWidth, h - 2*buttonHeight);

    sketch.textAlign(sketch.CENTER, sketch.CENTER);
    sketch.textSize(textSize);
    sketch.fill(textColor);
    sketch.stroke(textColor);
    sketch.text(sketch.nf(time.minutesSeconds()[0], 2), x - w/2 + singleRectWidth/2, y-2);
    sketch.text(":", x - w/2 + singleRectWidth, y-2);
    sketch.text(sketch.nf(time.minutesSeconds()[1], 2), x - w/2 + 1.5f*singleRectWidth, y-2);

    sketch.text(sketch.nf(Math.round(increment.seconds()), 2), x + w/2 - singleRectWidth/2, y-2);

    for (SubTimebutton s : subButtons) s.show();
  }

  public boolean contains(int mx, int my) {
    for (SubTimebutton s : subButtons) {
      if (s.isHovered(mx, my)) return true;
    }
    return false;
  }

  public void onUserEvent(UserEvent e) {
    super.onUserEvent(e);
    if (e.mousePressed() && contains(e.x, e.y)) {
      for (SubTimebutton s : subButtons) {
        if (s.isHovered(sketch.mouseX, sketch.mouseY)) {
          s.updateTime();
          s.pressed();
          return;
        }
      }
    }

    else if (e.mouseMoved()) {
      for (SubTimebutton s : subButtons) {
        if (s.isHovered(sketch.mouseX, sketch.mouseY)) s.hovered = true;
        else s.hovered = false;
      }
    }

    else if (e.mouseReleased()) {
      for (SubTimebutton s : subButtons) s.released();
    }
  }

  private class SubTimebutton {
    private float x, y, w, h; // x et y du coin en haut à gauche
    private int r1, r2, r3, r4;
    private boolean facingUp = true;
    private Time timeToModify, amountToModify, maximum;
    private Time pressedAt = null;
    private boolean isPressed = false;
    public boolean hovered = false;

    SubTimebutton(float x, float y, float w, float h, boolean facingUp) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.facingUp = facingUp;
    }

    SubTimebutton setArrondis(int r1, int r2, int r3, int r4) {
      this.r1 = r1;
      this.r2 = r2;
      this.r3 = r3;
      this.r4 = r4;
      return this;
    }

    SubTimebutton setTimeModifier(Time timeToModify, Time amountToModify, Time max) {
      this.timeToModify = timeToModify;
      this.amountToModify = amountToModify;
      this.maximum = max;
      return this;
    }

    void show() {
      if (!hovered) sketch.fill(backgroundColor);
      else sketch.fill(hoveredColor);
      sketch.noStroke();
      sketch.rectMode(sketch.CORNER);
      sketch.rect(x, y, w, h, r1, r2, r3, r4);

      sketch.strokeWeight(2);
      sketch.stroke(textColor);
      if (facingUp) {
        sketch.line(x + w/2, y + h/2 - arrowOffset, x + w/2 + arrowEspace, y + h/2 + arrowOffset);
        sketch.line(x + w/2, y + h/2 - arrowOffset, x + w/2 - arrowEspace, y + h/2 + arrowOffset);
      } else {
        sketch.line(x + w/2, y + h/2 + arrowOffset, x + w/2 + arrowEspace, y + h/2 - arrowOffset);
        sketch.line(x + w/2, y + h/2 + arrowOffset, x + w/2 - arrowEspace, y + h/2 - arrowOffset);
      }

      if (isPressed && sketch.millis() - pressedAt.millis() >= 500) updateTime();
    }

    void pressed() {
      pressedAt = Time.getMillis();
      isPressed = true;
    }

    void released() {
      isPressed = false;
    }

    void updateTime() {
      if ( (timeToModify.millis() + amountToModify.millis() < 0)
        || (timeToModify.millis() + amountToModify.millis() > maximum.millis()) ) return;

      timeToModify.add(amountToModify);
    }

    boolean isHovered(int mx, int my) {
      return (mx >= x && mx < x+w && my >= y && my < y+h);
    }
  }
}
