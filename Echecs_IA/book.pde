/////////////////////////////////////////////////////////////////

// Livre d'ouverture

// Format :
// fen:XXXX_YYY_XXXX_YYY_XXXX_YYY

// XXXX représente le coup dans le format (i1 j1 i2 j2)
// YYY représente le nombre de fois que ce coup apparait
// _ représente la séparation
// (Exception pour le roque, de la forme 88XX et 99XX)

/////////////////////////////////////////////////////////////////

class Arrow {
  int i, j, di, dj;
  float length;
  float angle = 0;
  float degrade = 0;
  PShape shape;

  Color currentColor = new Color(arrowDefaultColor.getRed(), arrowDefaultColor.getGreen(), arrowDefaultColor.getBlue());

  Arrow(int i, int j, int ti, int tj) {
    this.i = i;
    this.j = j;
    this.di = ti - i;
    this.dj = tj - j;

    createArrow();
    this.angle = (this.dj >= 0 ? 1 : -1) * acos((float)this.di*w/this.length);
  }

  void createArrow() {
    this.length = sqrt(pow((di*w), 2) + pow((dj*w), 2));

    this.shape = null;
    this.shape = createShape(GROUP);

    strokeWeight(5 * w/70);
    stroke(color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue()));
    PShape line = createShape(LINE, -this.length/2, 0, this.length/2, 0);
    PShape left = createShape(LINE, this.length/2, 0, this.length/2-(15*w/70), -(15*w/70));
    PShape right = createShape(LINE, this.length/2, 0, this.length/2-(15*w/70), (15*w/70));

    this.shape.addChild(line);
    this.shape.addChild(left);
    this.shape.addChild(right);
  }

  boolean equals(Arrow other) {
    return (this.i == other.i && this.j == other.j && this.di == other.di && this.dj == other.dj);
  }

  void setDegradeProgression(float newP) {
    this.degrade = newP;
    int red = (int)(arrowDefaultColor.getRed() * (1 - pow(this.degrade, 0.4)) + arrowFinalColor.getRed() * pow(this.degrade, 0.4));
    int green = (int)(arrowDefaultColor.getGreen() * (1 - pow(this.degrade, 0.4)) + arrowFinalColor.getGreen() * pow(this.degrade, 0.4));
    int blue = (int)(arrowDefaultColor.getBlue() * (1 - pow(this.degrade, 0.4)) + arrowFinalColor.getBlue() * pow(this.degrade, 0.4));
    this.currentColor = new Color(red, green, blue);
    createArrow();
  }

  void show() {
    float x = (grid[this.i][this.j].x + grid[this.i + this.di][this.j + this.dj].x)/2 + w/2;
    float y = (grid[this.i][this.j].y + grid[this.i + this.di][this.j + this.dj].y)/2 + w/2;

    push();
    translate(x, y);
    rotate(this.angle);
    if (!pointDeVue) rotate(PI);
    shape(this.shape);
    pop();
  }
}

// Affiche tous le livre d'ouverture (ligne par ligne)
void printBook() {
  for (int i = 0; i < book.size(); i++) {
    println("[" + i + "] " + book.get(i));
  }
}

// Affiche le nombre maximum de fois qu'un coup a été joué dans le livre d'ouverture
void printMaxEffectif() {
  int effectifMax = 0;
  String[] moves = getMoveStringFromFEN(generateFEN());

  for (int i = 1; i < moves.length; i += 2) {
    effectifMax = max(effectifMax, Integer.valueOf(moves[i]));
  }

  println(effectifMax);
}

// Indique tous les coups du livre d'ouverture dans la position
void highlightBook() {
  String[] moves = getMoveStringFromFEN(generateFEN());
  for (int i = 0; i < moves.length; i+=2) {
    int effectif = Integer.valueOf(moves[i+1]);
    int fromI = Integer.valueOf(String.valueOf(moves[i].charAt(0)));
    int fromJ = Integer.valueOf(String.valueOf(moves[i].charAt(1)));
    int targetI = Integer.valueOf(String.valueOf(moves[i].charAt(2)));
    int targetJ = Integer.valueOf(String.valueOf(moves[i].charAt(3)));

    if (fromI != 8 && fromI != 9) {
      allArrows.add(new Arrow(fromI, fromJ, targetI, targetJ));
    } else {
      // Roques
      if (fromI == 8 && targetI == 8) { allArrows.add(new Arrow(4, 7, 6, 7)); }
      else if (fromI == 8 && targetI == 9) { allArrows.add(new Arrow(4, 0, 6, 0)); }
      else if (fromI == 9 && targetI == 8) { allArrows.add(new Arrow(4, 7, 2, 7)); }
      else if (fromI == 9 && targetI == 9) { allArrows.add(new Arrow(4, 0, 2, 0)); }
    }

    // Couleur
    allArrows.get(allArrows.size()-1).setDegradeProgression(map(effectif, 1, 3497, 0, 1));
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

// Renvoie la liste des coups complètes (avec les effectifs) correspondant à la fen
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

// Sauvegarde le livre d'ouverture en écrivant les données dans le fichier book.txt
void saveBook() {
  String[] savedBook = new String[book.size()];
  for (int i = 0; i < book.size(); i++) savedBook[i] = book.get(i);
  saveStrings("data/book.txt", savedBook);
}
