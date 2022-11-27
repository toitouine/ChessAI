class Arrow {
  float x, y, tx, ty;
  int i, j, ti, tj;
  int arrowSpace = 15, arrowLength = 15;
  float angle = 0;
  boolean verticalDir; //true = haut, false = bas
  boolean horizontalDir; //true = gauche, false = droit

  Arrow(int i, int j, int ti, int tj) {
    this.i = i; this.j = j; this.ti = ti; this.tj = tj;
    this.x = grid[i][j].x + w/2;
    this.y = grid[i][j].y + w/2;
    this.tx = grid[ti][tj].x + w/2;
    this.ty = grid[ti][tj].y + w/2;
    this.verticalDir = (this.tj < this.j);
    this.horizontalDir = (this.i > this.ti);

    float deltaI = abs(this.ti - this.i);
    float deltaJ = abs(this.tj - this.j);
    if (deltaJ == 0) this.angle = horizontalDir ? -PI/2.1 : PI/2.1;
    else if (this.verticalDir && this.horizontalDir) this.angle = -atan(deltaI/deltaJ);
    else if (this.verticalDir && !this.horizontalDir) this.angle = atan(deltaI/deltaJ);
    else if (!this.verticalDir && !this.horizontalDir) this.angle = PI - atan(deltaI/deltaJ);
    else if (!this.verticalDir && this.horizontalDir) this.angle = PI + atan(deltaI/deltaJ);
  }

  void show() {
    strokeWeight(5);
    stroke(255, 192, 67, 255);
    float xDraw, yDraw, txDraw, tyDraw, angleDraw;
    if (pointDeVue) {
      xDraw = this.x; yDraw = this.y; txDraw = this.tx; tyDraw = this.ty;
      angleDraw = this.angle;
    } else {
      xDraw = grid[this.i][this.j].x + w/2; yDraw = grid[this.i][this.j].y + w/2;
      txDraw = grid[this.ti][this.tj].x + w/2; tyDraw = grid[this.ti][this.tj].y + w/2;
      angleDraw = PI + this.angle;
    }
    line(xDraw, yDraw, txDraw, tyDraw);

    push();
    translate(txDraw, tyDraw);
    rotate(angleDraw);
    line(0, 0, -this.arrowSpace, this.arrowLength);
    line(0, 0, this.arrowSpace, this.arrowLength);
    pop();
  }
}

void printBook() {
  for (int i = 0; i < book.size(); i++) {
    println("[" + i + "] " + book.get(i));
  }
}

void clearBookHighlight() {
  bookArrows.clear();
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].bookFrom = false;
      grid[i][j].bookTarget = false;
      grid[i][j].moveMark = false;
    }
  }
}

void highlightBook() {

  clearBookHighlight();
  String[] moves = getMovesFromFen(generateFEN());
  for (int i = 0; i < moves.length; i++) {
    int fromI = Integer.valueOf(String.valueOf(moves[i].charAt(0)));
    int fromJ = Integer.valueOf(String.valueOf(moves[i].charAt(1)));
    int targetI = Integer.valueOf(String.valueOf(moves[i].charAt(2)));
    int targetJ = Integer.valueOf(String.valueOf(moves[i].charAt(3)));

    if (fromI != 8 && fromI != 9) {
      grid[fromI][fromJ].bookFrom = true;
      grid[targetI][targetJ].bookTarget = true;
      bookArrows.add(new Arrow(fromI, fromJ, targetI, targetJ));
    } else { //roques
      if (fromI == 8 && targetI == 8) { grid[4][7].bookFrom = true; grid[6][7].bookTarget = true; bookArrows.add(new Arrow(4, 7, 6, 7)); }
      else if (fromI == 8 && targetI == 9) { grid[4][0].bookFrom = true; grid[6][0].bookTarget = true; bookArrows.add(new Arrow(4, 0, 6, 0)); }
      else if (fromI == 9 && targetI == 8) { grid[4][7].bookFrom = true; grid[2][7].bookTarget = true; bookArrows.add(new Arrow(4, 7, 2, 7)); }
      else if (fromI == 9 && targetI == 9) { grid[4][0].bookFrom = true; grid[2][0].bookTarget = true; bookArrows.add(new Arrow(4, 0, 2, 0)); }
    }
  }
}

String extractFenFromBook(int n) {
  String b = book.get(n);
  String resultFen = "";

  for (int i = 0; i < b.length(); i++) {
    char c = b.charAt(i);
    if (c == ':') break;
    resultFen = resultFen + c;
  }

  return resultFen;
}

int searchFenInBook(String fen) {
  for (int i = 0; i < book.size(); i++) {
    if (extractFenFromBook(i).equals(fen)) return i;
  }
  return -1;
}

String[] getMovesFromFen(String fen) {
  String movesString = "";

  int index = searchFenInBook(fen);
  if (index == -1) return new String[0];

  String b = book.get(index);
  int startMoves = 0;

  for (int i = b.length() - 1; i >= 0; i--) {
    if (b.charAt(i) == ':') startMoves = i+1;
  }

  movesString = b.substring(startMoves, b.length());
  int size = movesString.length()/4;
  String[] moves = new String[size];

  for (int i = 0; i < size; i++) {
    moves[i] = movesString.substring(i*4, i*4+4);
  }

  return moves;
}

String[] getMovesFromIndex(int index) {
  String movesString = "";
  String b = book.get(index);
  int startMoves = 0;

  for (int i = b.length() - 1; i >= 0; i--) {
    if (b.charAt(i) == ':') startMoves = i+1;
  }

  movesString = b.substring(startMoves, b.length());
  int size = movesString.length()/4;
  String[] moves = new String[size];

  for (int i = 0; i < size; i++) {
    moves[i] = movesString.substring(i*4, i*4+4);
  }

  return moves;
}

void playMoveFromBook(String moves[]) {
  String moveString = moves[floor(random(0, moves.length))];
  int fromI = Integer.valueOf(String.valueOf(moveString.charAt(0)));
  int fromJ = Integer.valueOf(String.valueOf(moveString.charAt(1)));
  int i = Integer.valueOf(String.valueOf(moveString.charAt(2)));
  int j = Integer.valueOf(String.valueOf(moveString.charAt(3)));
  Move m;
  if (fromI == 8) { //petit roque : 88xx
    if (i == 8) m = new Move(grid[4][7].piece, 6, 7, null, 1); //blanc 8888
    else m = new Move(grid[4][0].piece, 6, 0, null, 1); //noir 8899
  }
  else if (fromI == 9) { //grand roque 99xx
    if (i == 8) m = new Move(grid[4][7].piece, 2, 7, null, 2); //blanc 9988
    else m = new Move(grid[4][0].piece, 2, 0, null, 2); //noir 9999
  }
  else {
    m = new Move(grid[fromI][fromJ].piece, i, j, grid[i][j].piece, 0);
  }
  m.play();
}

void addMoveToBook(String fen, Move m) {
  int index = searchFenInBook(fen);
  String moveString;
  if (m.special == 1) moveString = "88" + (m.piece.c == 0 ? "88" : "99"); //petit roque
  else if (m.special == 2) moveString = "99" + (m.piece.c == 0 ? "88" : "99"); //grand roque
  else moveString = str(m.fromI) + str(m.fromJ) + str(m.i) + str(m.j);

  if (index == -1) {
    //fen introuvable
    book.add(fen + ":" + moveString);
    //println(moveString + " ajouté + nouvelle position");
  } else {
    //fen existante à index
    String[] movesAtIndex = getMovesFromIndex(index);
    for (int i = 0; i < movesAtIndex.length; i++) {
      if (movesAtIndex[i].equals(moveString)) return; //si le coup est déjà enregistré
    }
    //si le coup n'est pas déjà répertorié, on l'ajoute
    String b = book.get(index);
    b = b + moveString;
    book.set(index, b);
    //println(moveString + " ajouté");
  }
}

void saveBook() {
  String[] savedBook = new String[book.size()];
  for (int i = 0; i < book.size(); i++) savedBook[i] = book.get(i);
  saveStrings("data/book.txt", savedBook);
}
