class Bouton {
  float x, y, w;
  PImage i1, i2;
  int numShortcut = -1;

  Bouton(float x, float y, float w, PImage i1, PImage i2) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.i1 = i1;
    this.i2 = i2;
  }

  void setNumShortcut(int n) {
    this.numShortcut = n;
  }

  void callShortcut() {
    if (this.numShortcut == -1) { println("Erreur initialisation shortcut dans bouton"); return; }
    sc.call(this.numShortcut);
  }

  String getDescription() {
    return sc.getDescription(this.numShortcut);
  }

  void show(int c) {
    fill(0);
    imageMode(CENTER);
    if (c == 0) {
      image(this.i1, this.x+w/2, this.y+w/2, this.w/1.1, this.w/1.1);
    } else if (c == 1) {
      image(this.i2, this.x+w/2, this.y+w/2, this.w/1.1, this.w/1.1);
    }
  }

  boolean contains(int x, int y) {
    if (x >= this.x && x < this.x+w && y >= this.y && y < this.y+w) {
      return true;
    } else {
      return false;
    }
  }
}

class CircleToggle {
  float x, y, d;
  boolean state = false;

  CircleToggle(float x, float y, float d) {
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

class DragAndDrop {
  float x, y, w, h;
  int value;
  boolean lock = false;
  PImage img;

  DragAndDrop(float x, float y, float w, float h, PImage img, int value) {
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

class TextBouton {
  float x, y, w, h;
  int backColor = color(#8da75a);
  int textColor = color(#ffffff);
  String text;
  int textSize;

  TextBouton(float x, float y, float w, float h, String t, int textSize) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.text = t;
    this.textSize = textSize;
  }

  void setColors(int b, int t) {
    this.backColor = b;
    this.textColor = t;
  }

  void show() {
    noStroke();
    fill(this.backColor);
    rectMode(CORNER);
    rect(this.x, this.y, this.w, this.h, 10);

    textAlign(CENTER, CENTER);
    textSize(this.textSize);
    fill(this.textColor);
    text(this.text, this.x + this.w/2, (this.y + this.h/2) - this.textSize/5 + 2);
  }

  boolean contains(int x, int y) {
   return (x >= this.x && x < this.x+this.w && y >= this.y && y < this.y+this.h);
  }
}

class Toggle {
  float x, y, imgWidth;
  PImage img;
  boolean state = false;
  String name;

  Toggle(float x, float y, float imgWidth, PImage img, String n) {
    this.x = x;
    this.y = y;
    this.imgWidth = imgWidth;
    this.img = img;
    this.name = n;
  }

  void show() {
    imageMode(CORNER);
    if (this.state) {
      image(this.img, this.x, this.y, this.imgWidth, this.imgWidth);
      image(mark, this.x, this.y, this.imgWidth, this.imgWidth);
    } else {
      image(this.img, this.x, this.y, this.imgWidth, this.imgWidth);
    }
  }

  boolean contains(int x, int y) {
    if (x >= this.x && x < this.x+this.imgWidth && y >= this.y && y < this.y+imgWidth) {
      return true;
    } else {
      return false;
    }
  }
}

class ButtonFEN {
  float x, y, size;
  PImage img;
  String text;

  ButtonFEN(float x, float y, float size, PImage img, String text) {
    this.x = x;
    this.y = y;
    this.size = size;
    this.img = img;
    this.text = text;
  }

  void show() {
    image(this.img, this.x, this.y, this.size, this.size);
    textAlign(CENTER, CENTER);
    textSize(18 * w/75);
    fill(0);
    text(this.text, this.x, this.y + this.size/2 + 10);
  }

  boolean contains(float mx, float my) {
    return (mx > this.x - this.size/2 &&
            mx < this.x + this.size/2 &&
            my > this.y - this.size/2 &&
            my < this.y + this.size/2 + 23);
  }
}
