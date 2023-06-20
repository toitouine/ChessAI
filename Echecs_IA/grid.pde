/////////////////////////////////////////////////////////////////

// Cases de l'échiquier

/////////////////////////////////////////////////////////////////

class Cell {
  float xNorm, yNorm;
  float x, y;
  int i, j;
  String name;
  boolean noir = false, blanc = false;
  boolean selected = false; //Pièce sur la case sélectionnée
  boolean moveMark = false; //Dernier déplacement de pièce
  boolean yellow = false; // Coloration de la case en jaune
  boolean red = false; // Coloration de la case en rouge
  boolean freeMove = false; //Mouvement n'importe où (ou presque)
  Piece piece = null;
  Move possibleMove = null;

  Cell(int i, int j, int x, int y) {
    this.i = i;
    this.j = j;
    this.xNorm = x;
    this.yNorm = y;
    this.piece = null;
    this.possibleMove = null;
    this.name = (char)(97+i) + String.valueOf(8 - j);

    if (j % 2 == 0) {
      if (i % 2 == 0) this.blanc = true;
      else this.noir = true;
    } else {
      if (i % 2 == 0) this.noir = true;
      else this.blanc = true;
    }
  }

  void show() {
    noStroke();
    if (this.blanc) fill(#f0d9b5);
    else if (this.noir) fill(#b58863);

    if (pointDeVue) {
      this.x = this.xNorm;
      this.y = this.yNorm;
    } else {
      this.x = width - (this.xNorm + w - offsetX);
      this.y = height - (this.yNorm + w - offsetY);
    }

    rectMode(CORNER);
    rect(this.x, this.y, w, w);

    if (this.red) {
      fill(224, 76, 56, 230);
      rect(this.x, this.y, w, w);
    }
    else if (this.yellow) {
      fill(237, 217, 36, 230);
      rect(this.x, this.y, w, w);
    }
    else if (this.moveMark) {
      fill(209, 206, 25, 100);
      rect(this.x, this.y, w, w);
    }
    else if (this.selected) {
      fill(189, 186, 34, 100);
      rect(this.x, this.y, w, w);
    }

    if (this.possibleMove != null && this.possibleMove.capture != null) {
      noFill();
      stroke(75, 75, 75, 100);
      strokeWeight(w/16);
      ellipse(this.x + w/2, this.y+w/2, w - w/16, w - w/16);
    } else if (this.possibleMove != null) {
      fill(75, 75, 75, 100);
      ellipse(this.x + w/2, this.y + w/2, w/4, w/4);
    }
  }

  boolean contains(int x, int y) {
    if (x >= this.x && x < this.x + w && y >= this.y && y < this.y + w) {
      return true;
    } else {
      return false;
    }
  }

  void removePiece() {
    this.piece = null;
  }

  void toggleRed() {
    this.red =! this.red;
  }

  void toggleYellow() {
    this.yellow =! this.yellow;
  }
}
