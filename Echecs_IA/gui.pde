class Button {
  String function;
  Condition condition;

  Button(String f, Condition c) {
    this.function = f;
    this.condition = c;
  }

  void call() {
    if (this.function == "") return;
    method(this.function);
  }

  boolean isEnabled() {
    return this.condition.c();
  }

  void show() {}
  boolean contains(int x, int y) { error("Button.contains()", "Pas de surcharge de méthode"); return false; }
  String getDescription() { error("Button.getDescription()", "Pas de surcharge de méthode"); return ""; }
}

public interface Condition {
  public boolean c();
}

class PromotionButton extends Button {
  int promoNumber;
  float x, y, w;
  PImage i1, i2;

  PromotionButton(float x, float y, float w, PImage i1, PImage i2, int pn, Condition c) {
    super("", c);
    this.x = x;
    this.y = y;
    this.w = w;
    this.i1 = i1;
    this.i2 = i2;
    this.promoNumber = pn;
  }

  void show(int c) {
    fill(0);
    imageMode(CENTER);
    if (c == 0) image(this.i1, this.x+w/2, this.y+w/2, this.w/1.1, this.w/1.1);
    else if (c == 1) image(this.i2, this.x+w/2, this.y+w/2, this.w/1.1, this.w/1.1);
  }

  @Override
  void call() {
    playerPromote(this.promoNumber);
  }

  boolean contains(int x, int y) {
    return (x >= this.x && x < this.x+w && y >= this.y && y < this.y+w);
  }
}

class ShortcutButton extends Button {
  float x, y, w;
  PImage i1, i2;
  int numShortcut = -1;

  ShortcutButton(float x, float y, float w, PImage i1, PImage i2, Condition c) {
    super("", c);
    this.x = x;
    this.y = y;
    this.w = w;
    this.i1 = i1;
    this.i2 = i2;
  }

  void setNumShortcut(int n) {
    this.numShortcut = n;
  }

  @Override
  void call() {
    if (this.numShortcut == -1) { println("Erreur initialisation shortcut dans bouton"); return; }
    sc.call(this.numShortcut);
  }

  String getDescription() {
    return sc.getDescription(this.numShortcut);
  }

  void show(int c) {
    fill(0);
    imageMode(CENTER);
    if (c == 0) image(this.i1, this.x+w/2, this.y+w/2, this.w/1.1, this.w/1.1);
    else if (c == 1) image(this.i2, this.x+w/2, this.y+w/2, this.w/1.1, this.w/1.1);
  }

  boolean contains(int x, int y) {
    return (x >= this.x && x < this.x+w && y >= this.y && y < this.y+w);
  }
}

class TimeButton extends Button {
  float x, y, w, h;
  int r1, r2, r3, r4;
  int background, arrowColor, hoveredColor;
  boolean facingUp;
  boolean hovered, pressed;
  int cooldownFastIncrement = 500, pressedAt;
  int i, j;

  TimeButton(float x, float y, float w, float h, int r1, int r2, int r3, int r4, int background, int arrowColor, int hoveredColor, boolean facing, Condition c) {
    super("", c);
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.r1 = r1;
    this.r2 = r2;
    this.r3 = r3;
    this.r4 = r4;
    this.background = background;
    this.arrowColor = arrowColor;
    this.hoveredColor = hoveredColor;
    this.facingUp = facing;
  }

  void show() {
    if (!this.hovered) {
      fill(this.background);
      stroke(this.background);
    } else {
      fill(this.hoveredColor);
      stroke(this.hoveredColor);
    }
    strokeWeight(1);
    rect(this.x, this.y, this.w, this.h, this.r1, this.r2, this.r3, this.r4);

    strokeWeight(2);
    stroke(this.arrowColor);
    if (this.facingUp) {
      line(this.x + this.w/2, this.y + this.h/2 - 2, this.x + this.w/2 + 5, this.y + this.h/2 + 2);
      line(this.x + this.w/2, this.y + this.h/2 - 2, this.x + this.w/2 - 5, this.y + this.h/2 + 2);
    } else {
      line(this.x + this.w/2, this.y + this.h/2 + 2, this.x + this.w/2 + 5, this.y + this.h/2 - 2);
      line(this.x + this.w/2, this.y + this.h/2 + 2, this.x + this.w/2 - 5, this.y + this.h/2 - 2);
    }
  }

  @Override
  void call() {
    this.click();
  }

  void update() {
    if (this.pressed && millis() - pressedAt >= this.cooldownFastIncrement) {
      if (frameCount % 2 == 0) this.updateAssignedTimer();
    }
  }

  void setIndex(int i, int j) {
    this.i = i;
    this.j = j;
  }

  void updateAssignedTimer() {
    times[this.i][this.j] += (this.facingUp ? 1 : -1);
    times[this.i][this.j] = constrain(times[this.i][this.j], 0, 60);
  }

  void click() {
    this.pressed = true;
    this.pressedAt = millis();
    this.updateAssignedTimer();
  }

  void release() {
    this.pressed = false;
  }

  boolean contains(int x, int y) {
    this.hovered = (x >= this.x && x < this.x+this.w && y >= this.y && y < this.y+this.h);
    return this.hovered;
  }
}

class ImageButton extends Button {
  float x, y, w, h;
  int background, r;
  boolean fullImageSize, display = true;
  PImage img;

  ImageButton(float x, float y, float w, float h, int r, int background, PImage img, boolean fullSize, String f, Condition c) {
    super(f, c);
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.r = r;
    this.img = img;
    this.background = background;
    this.fullImageSize = fullSize;
  }

  void show() {
    if (!this.display) return;

    imageMode(CENTER);
    if (this.fullImageSize) {
      image(this.img, this.x+w/2, this.y+w/2, this.w/1.1, this.w/1.1);
      return;
    }

    rectMode(CORNER);
    fill(this.background);
    noStroke();
    rect(this.x, this.y, this.w, this.h, this.r, this.r, this.r, this.r);
    image(this.img, this.x+this.w/2, this.y+this.h/2, this.w/1.9, this.h/1.9);
  }

  boolean contains(int x, int y) {
   return (x >= this.x && x < this.x+this.w && y >= this.y && y < this.y+this.h);
  }
}

class CircleToggleButton extends Button {
  float x, y, d;
  boolean state = false;

  CircleToggleButton(float x, float y, float d, String f, Condition c) {
    super(f, c);
    this.x = x;
    this.y = y;
    this.d = d;
  }

  void show() {
    stroke(0);
    if (state) fill(#e4e4e4);
    else fill(#444141);
    circle(this.x, this.y, this.d);
  }

  void toggle() {
    this.state = !this.state;
  }

  boolean contains(int x, int y) {
    return (dist(x, y, this.x, this.y) <= this.d/2);
  }
}

class DragAndDrop extends Button {
  float x, y, w, h;
  int value;
  boolean lock = false;
  PImage img;

  DragAndDrop(float x, float y, float w, float h, PImage img, int value, Condition c) {
    super("", c);
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.value = value;
    this.img = img;
  }

  void show() {
    imageMode(CENTER);
    if (this.lock) image(this.img, mouseX, mouseY, this.w, this.h);
    else image(this.img, this.x, this.y, this.w, this.h);

    //rectMode(CENTER); noFill(); stroke(0); strokeWeight(3);
    //rect(this.x, this.y, this.w, this.h);
  }

  @Override
  void call() {
    if (enAjoutPiece == null) {
      enAjoutPiece = this;
      this.lockToMouse();
    }
  }

  int getValue() {
    return this.value;
  }

  void lockToMouse() {
    this.lock = true;
  }

  void unlockMouse() {
    this.lock = false;
  }

  boolean contains(int x, int y) {
    return (x >= this.x-this.w/2 && x < this.x+this.w/2 && y >= this.y-this.h/2 && y < this.y+this.h/2);
  }
}

class TextButton extends Button {
  float x, y, w, h;
  int backColor = color(#8da75a);
  int textColor = color(#ffffff);
  String text;
  int textSize;
  int arrondi;

  TextButton(float x, float y, float w, float h, String t, int textSize, int arrondi, String f, Condition c) {
    super(f, c);
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.text = t;
    this.textSize = textSize;
    this.arrondi = arrondi;
  }

  void setColors(int b, int t) {
    this.backColor = b;
    this.textColor = t;
  }

  void show() {
    noStroke();
    fill(this.backColor);
    rectMode(CORNER);
    rect(this.x, this.y, this.w, this.h, this.arrondi);

    textAlign(CENTER, CENTER);
    textSize(this.textSize);
    fill(this.textColor);
    text(this.text, this.x + this.w/2, (this.y + this.h/2) - this.textSize/5 + 2);
  }

  boolean contains(int x, int y) {
   return (x >= this.x && x < this.x+this.w && y >= this.y && y < this.y+this.h);
  }
}

class ImageSelector extends Button {
  float x, y, w;
  int c, number;
  PImage[] images;
  String[] names;

  ImageSelector(float x, float y, float w, PImage[] imgs, String[] names, int c, Condition cond) {
    super("", cond);
    this.x = x;
    this.y = y;
    this.w = w;
    this.images = imgs;
    this.names = names;
    this.c = c;
    this.number = 0;
  }

  void show() {
    imageMode(CORNER);
    image(this.images[this.number], this.x, this.y, this.w, this.w);

    imageMode(CENTER);
    image(leftArrow, this.x - this.w/4, this.y + this.w/2, this.w/4, this.w/4);
    image(rightArrow, this.x + 5*this.w/4, this.y + this.w/2, this.w/4, this.w/4);
  }

  @Override
  void call() {
    int add;
    if (isLeft(mouseX, mouseY)) add = -1;
    else add = 1;

    this.number += add;
    if (this.number == -1) this.number = this.names.length-1;
    else if (this.number == this.names.length) this.number = 0;

    if (c == 0) j1 = this.names[this.number];
    else if (c == 1) j2 = this.names[this.number];
  }

  @Override
  boolean contains(int mx, int my) {
    return (isLeft(mx, my) || isRight(mx, my));
  }

  boolean isLeft(int mx, int my) {
    return (mx >= this.x-3*this.w/8 && mx < this.x-this.w/8 && my >= this.y + 3*this.w/8 && my < this.y + 5*this.w/8);
  }

  boolean isRight(int mx, int my) {
    return (mx >= this.x + 9*this.w/8 && mx < this.x + 11*this.w/8 && my >= this.y + 3*this.w/8 && my < this.y + 5*this.w/8);
  }
}

class ButtonFEN extends Button {
  float x, y, size;
  int numFEN;
  PImage img;
  String text;

  ButtonFEN(float x, float y, float size, PImage img, String text, int num, Condition c) {
    super("", c);
    this.x = x;
    this.y = y;
    this.size = size;
    this.img = img;
    this.text = text;
    this.numFEN = num;
  }

  void show() {
    image(this.img, this.x, this.y, this.size, this.size);
    textAlign(CENTER, CENTER);
    textSize(18 * w/75);
    fill(0);
    text(this.text, this.x, this.y + this.size/2 + 10);
  }

  @Override
  void call() {
    importSavedFEN(this.numFEN);
    toggleSavedPos();
  }

  boolean contains(int mx, int my) {
    return (mx > this.x - this.size/2 &&
            mx < this.x + this.size/2 &&
            my > this.y - this.size/2 &&
            my < this.y + this.size/2 + 23);
  }
}
