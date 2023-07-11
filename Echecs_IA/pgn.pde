/////////////////////////////////////////////////////////////////

// PGN
// Contient les fonctions principales pour gérer la PGN de la partie en cours, la lecture de PGNs et la création du livre d'ouverture

/////////////////////////////////////////////////////////////////

String getPGNString(Move m) {
  String movePgn = "";
  char ambichar = ' '; //ambichar = char pour ambiguités dans la pgn

  //ambiguités
  ArrayList<Piece> doubles = detectMoveDoubles(m);
  if (doubles.size() == 1) { //pour l'instant (pour simplifier), 1 seul doublon est pris en compte
    Piece doublon = doubles.get(0);
    if (doublon.i == m.fromI) { //même colonne
      ambichar = grid[m.fromI][m.fromJ].name.charAt(1);
    } else { //même ligne ou aucun des deux
      ambichar = grid[m.fromI][m.fromJ].name.charAt(0);
    }
  }

  //encodage du coup
  if (m.special == 0) {
    if (m.piece.type != "pion") {
      if (ambichar == ' ') movePgn = movePgn + (m.piece.code.toUpperCase() + ((m.capture != null) ? "x" : "") + grid[m.i][m.j].name);
      else movePgn = movePgn + (m.piece.code.toUpperCase() + ambichar + ((m.capture != null) ? "x" : "") + grid[m.i][m.j].name);
    } else {
      movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name);
    }
  } else if (m.special == 1) {
    movePgn = movePgn + "O-O";
  } else if (m.special == 2) {
    movePgn = movePgn + "O-O-O";
  } else if (m.special == 4) { //promotion humain, complétée dans events
    movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name + "=");
  } else if (m.special == 5) {
    movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name + "=Q");
  } else if (m.special == 6) {
    movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name + "=R");
  } else if (m.special == 7) {
    movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name + "=B");
  } else if (m.special == 8) {
    movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name + "=N");
  }

  return movePgn;
}

void updatePGN(Move m) {
  String movePgn = "";
  if (tourDeQui == 1) movePgn = (int)nbTour + ".";

  movePgn = movePgn + getPGNString(m);

  movePgn = movePgn + " ";
  pgn = pgn + movePgn;
}

ArrayList detectMoveDoubles(Move m) {
  Piece p = m.piece;
  ArrayList<Piece> pieces = new ArrayList<Piece>();
  // uniquement "captures" de la même couleur que la pièce, et attention, ne marche pas pour les pions
  ArrayList<Move> friendlyMoves = getFriendlyMoves(p);

  for (int i = 0; i < friendlyMoves.size(); i++) {
    Move move = friendlyMoves.get(i);
    if (grid[move.i][move.j].piece.type == m.piece.type) {
      pieces.add(grid[move.i][move.j].piece);
    }
  }

  return pieces;
}

ArrayList getFriendlyMoves(Piece p) {
  ArrayList<Move> moves = new ArrayList<Move>();
  int oppositeColor = opponent(p.c);

  switch (p.type) {
    case "cavalier":
      moves = getQuietKnightMoves(p, oppositeColor);
    break;

    case "fou":
      moves = getQuietBishopMoves(p, oppositeColor);
    break;

    case "tour":
      moves = getQuietRookMoves(p, oppositeColor);
    break;

    case "dame":
      moves = getQuietRookMoves(p, oppositeColor);
      moves.addAll(getQuietBishopMoves(p, oppositeColor));
    break;

    case "roi":
      moves = getQuietKingMoves(p, oppositeColor);
    break;
  }

  return moves;
}

void addPgnChar(String s) {
  pgn = pgn.substring(0, pgn.length()-1); //remove last char (space)
  pgn = pgn + s + " ";
}

void addPgnCheck() {
   pgn = pgn.substring(0, pgn.length()-1);
   pgn = pgn + "+ ";
}

void addPgnMate(int c) {
   pgn = pgn.substring(0, pgn.length()-1);
   pgn = pgn + "# ";
   addPgnWin(c);
}

void addPgnWin(int c) {
  pgn = pgn + ( (c == 0) ? "1-0" : "0-1");
}

void addPgnDraw() {
  pgn = pgn + "1/2-1/2";
}

//////////////////////////////////////////////////////////////////////

boolean isAtPawnCaptureDist(Cell c1, Cell c2, int c) {
  //c1 pour départ, c2 pour arrivée

  int iDistAbs = abs(c2.i-c1.i);
  int jDist = c2.j-c1.j;

  if (c == 0) { //pion blanc
    return (iDistAbs == 1 && jDist == -1);
  } else if (c == 1) { //pion noir
    return (iDistAbs == 1 && jDist == 1);
  } else { //erreur
    return false;
  }
}

boolean isAtDiagDist(Cell c1, Cell c2) {
  int iDist = abs(c2.i-c1.i);
  int jDist = abs(c2.j-c1.j);
  return ((iDist == 1) && (jDist == 1));
}

boolean isAtKingDist(Cell c1, Cell c2) {
  int iDist = abs(c2.i-c1.i);
  int jDist = abs(c2.j-c1.j);
  return ((iDist <= 1) && (jDist <= 1));
}

boolean isAtKnightDist(Cell c1, Cell c2) {
  int iDist = abs(c2.i-c1.i);
  int jDist = abs(c2.j-c1.j);
  return ((iDist == 1 && jDist == 2) || (iDist == 2 && jDist == 1));
}

boolean isAtBishopDist(Cell c1, Cell c2) {
  int iDist = abs(c2.i-c1.i);
  int jDist = abs(c2.j-c1.j);
  return (iDist == jDist);
}

boolean isAtRookDist(Cell c1, Cell c2) {
  int iDist = abs(c2.i-c1.i);
  int jDist = abs(c2.j-c1.j);
  return ((iDist == 0 && jDist > 0)|| (iDist > 0 && jDist == 0));
}

boolean isAtQueenDist(Cell c1, Cell c2) {
  return (isAtBishopDist(c1, c2) || isAtRookDist(c1, c2));
}

boolean isAtPieceDist(Cell c1, Cell c2, String type) {
  switch (type) {
    case "cavalier": return (isAtKnightDist(c1, c2));
    case "fou": return (isAtBishopDist(c1, c2));
    case "dame": return (isAtQueenDist(c1, c2));
    case "tour": return (isAtRookDist(c1, c2));
    default: error("isAtPieceDist()", "type invalide"); return false;
  }
}

boolean canBePieceMove(Piece p, int i, int j) {
  ArrayList<Move> moves = p.generateLegalMoves(false, false);
  for (int n = 0; n < moves.size(); n++) {
    if (moves.get(n).i == i && moves.get(n).j == j) return true;
  }
  return false;
}

//////////////////////////////////////////////////////////////////////

// Lecture de pgns

// Sans promotion et sans fin de partie
void playPgn(String pgn, int limit) {
  String pgnForOneMove[] = pgn.split(" ");

  if (limit == -1) limit = pgnForOneMove.length;

  String pieceType;
  int promotion = 0;
  int colonneRequise;
  int ligneRequise;
  boolean capture;
  boolean firstPly = (tourDeQui == 0) ? false : true; //premier coup du tour, aux blancs de jouer (false car on inverse après)

  for (int i = 0; i < limit; i++) {
    String word = pgnForOneMove[i];
    colonneRequise = -1;
    ligneRequise = -1;
    promotion = 0;
    capture = false;
    firstPly = !firstPly;

    if (Character.isLetter(word.charAt(0))) { //Le mot est un coup
      char c = word.charAt(0);

      //Recherche de la pièce
      if (c == 'O') { getRoquePgn(word, firstPly).play(); continue; } //roques
      else if (c == 'K') { pieceType = "roi"; word = word.substring(1, word.length()); }
      else if (c == 'Q') { pieceType = "dame"; word = word.substring(1, word.length()); }
      else if (c == 'B') { pieceType = "fou"; word = word.substring(1, word.length()); }
      else if (c == 'N') { pieceType = "cavalier"; word = word.substring(1, word.length()); }
      else if (c == 'R') { pieceType = "tour"; word = word.substring(1, word.length()); }
      else pieceType = "pion";

      //Recherche d'un prérequis
      char c1 = word.charAt(0);
      char c2 = ' ', c3 = ' ';
      if (word.length() >= 2) c2 = word.charAt(1);
      if (word.length() >= 3) c3 = word.charAt(2);

      if (isAtoH(c1) && (c2 == 'x' || isAtoH(c2))) {
        colonneRequise = letterToNum(c1);
        word = word.substring(1, word.length());
      }
      else if (is1to8(c1) && (c2 == 'x' || isAtoH(c2))) {
        ligneRequise = pgnNumToNum(c1);
        word = word.substring(1, word.length());
      }
      else if (isAtoH(c1) && is1to8(c2) && (c3 == 'x' || isAtoH(c3))) {
        colonneRequise = letterToNum(c1);
        ligneRequise = pgnNumToNum(c3);
        word = word.substring(2, word.length());
      }

      //Capture ou non
      if (word.charAt(0) == 'x') {
        capture = true;
        word = word.substring(1, word.length());
      }

      //Promotion ou non
      if (word.length() >= 4) {
        if (word.charAt(2) == '=') {
          if (word.charAt(3) == 'Q') promotion = 5;
          if (word.charAt(3) == 'R') promotion = 6;
          if (word.charAt(3) == 'B') promotion = 7;
          if (word.charAt(3) == 'N') promotion = 8;
        }
      }

      //Génération du coup
      if (pieceType != "pion") {

        int targetI = letterToNum(word.charAt(0));
        int targetJ = pgnNumToNum(word.charAt(1));
        Piece p = getPiecePgn(pieceType, ((firstPly) ? 0 : 1), targetI, targetJ, colonneRequise, ligneRequise);
        if (capture) { Move m = new Move(p, targetI, targetJ, grid[targetI][targetJ].piece, 0); m.play(); }
        else { Move m = new Move(p, targetI, targetJ, null, 0); m.play(); }

      } else {
        int targetI = letterToNum(word.charAt(0));
        int targetJ = pgnNumToNum(word.charAt(1));

        if (firstPly) { //blancs
          int fromI = (capture) ? colonneRequise : targetI;
          int fromJ = targetJ+1;
          if (targetJ == 4) {
            if (grid[fromI][5].piece != null) fromJ = 5;
            else fromJ = 6;
          }
          if (capture) {
            Piece p;
            boolean enPassant = false;
            if (grid[targetI][targetJ].piece == null) { p = grid[targetI][targetJ+1].piece; enPassant = true; }
            else p = grid[targetI][targetJ].piece;
            Move m = new Move(grid[fromI][fromJ].piece, targetI, targetJ, p, (enPassant) ? 3 : promotion); m.play(); continue;
          }
          else {
            Move m = new Move(grid[fromI][fromJ].piece, targetI, targetJ, null, promotion); m.play(); continue;
          }

        } else { //noirs
          int fromI = (capture) ? colonneRequise : targetI;
          int fromJ = targetJ-1;
          if (targetJ == 3) {
            if (grid[fromI][2].piece != null) fromJ = 2;
            else fromJ = 1;
          }
          if (capture) {
            Piece p;
            boolean enPassant = false;
            if (grid[targetI][targetJ].piece == null) { p = grid[targetI][targetJ-1].piece; enPassant = true; }
            else p = grid[targetI][targetJ].piece;
            Move m = new Move(grid[fromI][fromJ].piece, targetI, targetJ, p, (enPassant) ? 3 : promotion); m.play(); continue;
          }
          else {
            Move m = new Move(grid[fromI][fromJ].piece, targetI, targetJ, null, promotion); m.play(); continue;
          }
        }

      }
    }

  }

}

Piece getPiecePgn(String type, int c, int targetI, int targetJ, int reqI, int reqJ) {
  ArrayList<Piece> matches = new ArrayList<Piece>();

  for (int i = 0; i < pieces[c].size(); i++) {
    if (pieces[c].get(i).type == type) matches.add(pieces[c].get(i));
  }

  for (int i = matches.size()-1; i >= 0; i--) {
    Piece p = matches.get(i);

    if (canBePieceMove(p, targetI, targetJ)) { //la pièce est à distance

      if (reqI == -1 && reqJ == -1) continue;
      else if (reqI != -1 && reqJ == -1) { //prérequis i
        if (reqI == p.i) continue;
        else matches.remove(i);
      } else if (reqI == -1 && reqJ != -1) { //prérequis j
        if (reqJ == p.j) continue;
        else matches.remove(i);
      } else { //prérequis i et j
        if (reqI == p.i && reqJ == p.j) continue;
        else matches.remove(i);
      }
    } else {
      matches.remove(i);
    }
  }

  if (matches.size() > 1) error("getPiecePgn()", "trop de matches");
  if (matches.size() == 0) return null;
  return matches.get(0);
}

String shortPgn(String pgn, int num) {
  String newPgn = "";
  String words[] = pgn.split(" ");
  for (int i = 0; i < num; i++) {
    if (i == 0) newPgn = newPgn + words[i];
    else newPgn = newPgn + " " + words[i];
  }
  return newPgn;
}

int letterToNum(char c) {
  //a to h -> 0 to 7
  int ascii = (int)c;
  return (ascii - 97);
}

int pgnNumToNum(char c) {
  int num = Integer.valueOf(String.valueOf(c));
  num = 8 - num;
  return num;
}

boolean isAtoH(char c) {
  int ascii = (int)c;
  return (ascii >= 97 && ascii <= 104);
}

boolean is1to8(char c) {
  int ascii = (int)c;
  return (ascii >= 49 && ascii <= 56);
}

Move getRoquePgn(String word, boolean firstPly) {
  if (firstPly) {
    if (word.equals("O-O") || word.equals("O-O+")) return(new Move(grid[4][7].piece, 6, 7, null, 1));
    else if (word.equals("O-O-O") || word.equals("O-O-O+")) return(new Move(grid[4][7].piece, 2, 7, null, 2));
  } else {
    if (word.equals("O-O") || word.equals("O-O+")) return(new Move(grid[4][0].piece, 6, 0, null, 1));
    else if (word.equals("O-O-O") || word.equals("O-O-O+")) return(new Move(grid[4][0].piece, 2, 0, null, 2));
  }

  return null;
}
