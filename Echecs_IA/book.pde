/////////////////////////////////////////////////////////////////

// Livre d'ouverture

// Format :
// fen:XXXXYYY_XXXXYYY_XXXXYYY

// XXXX représente le coup dans le format (i1 j1 i2 j2)
// YYY représente le nombre de fois que ce coup apparait
// _ représente la séparation
// (Exception pour le roque, de la forme 88XX et 99XX)

/////////////////////////////////////////////////////////////////

class Arrow {
  float x, y, tx, ty;
  int i, j, ti, tj;
  int arrowSpace = 15, arrowLength = 15;
  float angle = 0;
  boolean verticalDir; //true = haut, false = bas
  boolean horizontalDir; //true = gauche, false = droit
  float progressionDegrade = 0;

  Color colorDepart = new Color(255, 192, 67);
  Color colorArrivee = new Color(255, 0, 0);
  Color currentColor = new Color(255, 192, 67);;

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

  void setDegradeProgression(float newP) {
    this.progressionDegrade = newP;
    int red = (int)(this.colorDepart.getRed() * (1 - pow(this.progressionDegrade, 0.4)) + this.colorArrivee.getRed() * pow(this.progressionDegrade, 0.4));
    int green = (int)(this.colorDepart.getGreen() * (1 - pow(this.progressionDegrade, 0.4)) + this.colorArrivee.getGreen() * pow(this.progressionDegrade, 0.4));
    int blue = (int)(this.colorDepart.getBlue() * (1 - pow(this.progressionDegrade, 0.4)) + this.colorArrivee.getBlue() * pow(this.progressionDegrade, 0.4));
    this.currentColor = new Color(red, green, blue);
  }

  void show() {
    strokeWeight(5);
    stroke(this.currentColor.getRed(), this.currentColor.getGreen(), this.currentColor.getBlue());
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

void printMaxEffectif() {
  int effectifMax = 0;
  String[] moves = getMoveStringFromFEN(generateFEN());

  for (int i = 1; i < moves.length; i += 2) {
    effectifMax = max(effectifMax, Integer.valueOf(moves[i]));
  }

  println(effectifMax);
}

void clearBookHighlight() {
  bookArrows.clear();
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].yellow = false;
      grid[i][j].red = false;
      grid[i][j].moveMark = false;
    }
  }
}

void highlightBook() {
  bookArrows.clear();

  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].yellow = false;
      grid[i][j].red = false;
      grid[i][j].moveMark = false;
    }
  }

  String[] moves = getMoveStringFromFEN(generateFEN());
  for (int i = 0; i < moves.length; i+=2) {
    int effectif = Integer.valueOf(moves[i+1]);
    int fromI = Integer.valueOf(String.valueOf(moves[i].charAt(0)));
    int fromJ = Integer.valueOf(String.valueOf(moves[i].charAt(1)));
    int targetI = Integer.valueOf(String.valueOf(moves[i].charAt(2)));
    int targetJ = Integer.valueOf(String.valueOf(moves[i].charAt(3)));

    if (fromI != 8 && fromI != 9) {
      bookArrows.add(new Arrow(fromI, fromJ, targetI, targetJ));
    } else {
      // Roques
      if (fromI == 8 && targetI == 8) { bookArrows.add(new Arrow(4, 7, 6, 7)); }
      else if (fromI == 8 && targetI == 9) { bookArrows.add(new Arrow(4, 0, 6, 0)); }
      else if (fromI == 9 && targetI == 8) { bookArrows.add(new Arrow(4, 7, 2, 7)); }
      else if (fromI == 9 && targetI == 9) { bookArrows.add(new Arrow(4, 0, 2, 0)); }
    }

    // Couleur
    bookArrows.get(bookArrows.size()-1).setDegradeProgression(map(effectif, 1, 3497, 0, 1));
  }
}

// Renvoie la fen contenue à la ligne n du livre
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

// Renvoie l'index de la fen si elle est trouvée dans le livre et renvoie -1 si elle n'est pas trouvée
int searchFenInBook(String fen) {
  for (int i = 0; i < book.size(); i++) {
    if (extractFenFromBook(i).equals(fen)) return i;
  }
  return -1;
}

// Renvoie une liste des coups correspondant à la fen dans le même format que celui du livre (i1 j1 i2 j2)
ArrayList<String> getMovesFromFen(String fen) {
  String movesString = "";

  int index = searchFenInBook(fen);
  if (index == -1) return new ArrayList<String>();

  String b = book.get(index);

  // Index auquel les coups commencent à être indiqués
  int startMoves = fen.length()+1;

  // String de tous les coups
  movesString = b.substring(startMoves, b.length());

  ArrayList<String> moves = new ArrayList<String>();

  // Ajoute les coups à l'array list autant de fois que nécessaire (pour que l'effectif corresponde)
  String[] preMovesArray = split(movesString, "_");
  for (int i = 0; i < preMovesArray.length; i+=2) {
    int numOfMoves = Integer.valueOf(preMovesArray[i+1]);
    for (int n = 0; n < numOfMoves; n++) {
      moves.add(preMovesArray[i]);
    }
  }

  return moves;
}

// Renvoie une liste des coups correspondant à l'index de la fen dans le livre dans le même format que celui du livre (i1 j1 i2 j2)
ArrayList<String> getMovesFromIndex(int index) {
  String movesString = "";
  String b = book.get(index);
  int startMoves = 0;

  for (int i = b.length() - 1; i >= 0; i--) {
    if (b.charAt(i) == ':') startMoves = i+1;
  }

  movesString = b.substring(startMoves, b.length());

  ArrayList<String> moves = new ArrayList<String>();

  String[] preMovesArray = split(movesString, "_");
  for (int i = 0; i < preMovesArray.length; i+=2) {
    int numOfMoves = Integer.valueOf(preMovesArray[i+1]);
    for (int n = 0; n < numOfMoves; n++) {
      moves.add(preMovesArray[i]);
    }
  }

  return moves;
}

// Renvoie la liste des coups complètes (avec les effectifs) correspondant à l'index de la fen
String[] getMoveStringFromFEN(String fen) {
  String movesString = "";

  int index = searchFenInBook(fen);
  if (index == -1) return new String[0];

  String b = book.get(index);

  // Index auquel les coups commencent à être indiqués
  int startMoves = fen.length()+1;

  movesString = b.substring(startMoves, b.length());

  String[] movesArray = split(movesString, "_");
  return movesArray;
}

// Renvoie la liste des coups complètes (avec les effectifs) correspondant à l'index de la fen
String[] getMoveStringFromIndex(int index) {
  String movesString = "";
  String b = book.get(index);
  int startMoves = 0;

  for (int i = b.length() - 1; i >= 0; i--) {
    if (b.charAt(i) == ':') startMoves = i+1;
  }

  movesString = b.substring(startMoves, b.length());

  String[] movesArray = split(movesString, "_");
  return movesArray;
}

// Choisi aléatoirement un coup parmi ceux du livre qui sont passés en argument et le joue
Move playMoveFromBook(ArrayList<String> moves) {
  String moveString = moves.get(floor(random(0, moves.size())));
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

  return m;
}

// Ajoute un coup au livre d'ouverture correspondant à la position dans laquelle il doit être joué
void addMoveToBook(String fen, Move m) {
  int index = searchFenInBook(fen);
  String moveString;
  if (m.special == 1) moveString = "88" + (m.piece.c == 0 ? "88" : "99"); //petit roque
  else if (m.special == 2) moveString = "99" + (m.piece.c == 0 ? "88" : "99"); //grand roque
  else moveString = str(m.fromI) + str(m.fromJ) + str(m.i) + str(m.j);

  if (index == -1) {
    // La fen introuvable, donc on l'ajoute au livre
    book.add(fen + ":" + moveString + "_1");
    println("[BOOK] " + moveString + " ajouté + nouvelle position");
  }
  else {
    // La fen est déjà référencée (trouvée à index)
    String[] movesStringAtIndex = getMoveStringFromIndex(index);

    for (int i = 0; i < movesStringAtIndex.length; i+=2) {
      if (movesStringAtIndex[i].equals(moveString)) {
        // Le coup est déjà enregistré, on incrémente l'effectif (en reconstituant la ligne du livre)
        int effectif = Integer.valueOf(movesStringAtIndex[i+1]);
        movesStringAtIndex[i+1] = str(effectif+1);
        String bookLine = fen + ":" + join(movesStringAtIndex, "_");
        book.set(index, bookLine);
        println("[BOOK] " + moveString + " incrémenté : " + (effectif+1));
        return;
      }
    }

    // Si le coup n'est pas déjà répertorié, on l'ajoute au livre
    String b = book.get(index);
    b = b + "_" + moveString + "_1";
    book.set(index, b);
    println("[BOOK] " + moveString + " ajouté (nouveau coup, position connue)");
  }
}

// Sauvegarde le livre d'ouverture en écrivant les données dans le fichier book.txt OK
void saveBook() {
  String[] savedBook = new String[book.size()];
  for (int i = 0; i < book.size(); i++) savedBook[i] = book.get(i);
  saveStrings("data/book.txt", savedBook);
}
