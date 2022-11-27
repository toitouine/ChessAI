class Piece {
  int i, j, c; //0 = blanc; 1 = noir
  int pieceIndex, zobristIndex = 0;
  String code, type;
  boolean dragging;

  int roquable = -1, petitRoquable = -1, grandRoquable = -1; //-1 pour undefined, 0 et 1 pour true et false
  int enPassantable = -1;
  float saveTour;

  int maireEval; //Matériel
  int loicEval;
  float mairePosEval; //Positionnel
  float loicPosEval;

  Piece(String type, int i, int j, int c) {
    this.i = i;
    this.j = j;
    this.c = c;
    this.type = type;
    this.zobristIndex = (c == 0) ? 0 : 6;

    switch(this.type) {
      case "roi":
        this.pieceIndex = ROI_INDEX;
        this.zobristIndex += pieceIndex;
        this.roquable = 1;
      break;
      case "dame":
        this.pieceIndex = DAME_INDEX;
        this.zobristIndex += pieceIndex;
      break;
      case "tour":
        this.pieceIndex = TOUR_INDEX;
        this.zobristIndex += pieceIndex;
      break;
      case "fou":
        this.pieceIndex = FOU_INDEX;
        this.zobristIndex += pieceIndex;
      break;
      case "cavalier":
        this.pieceIndex = CAVALIER_INDEX;
        this.zobristIndex += pieceIndex;
      break;
      case "pion":
        this.pieceIndex = PION_INDEX;
        this.zobristIndex += pieceIndex;
        this.enPassantable = 0;
      break;
    }

    //Données pièce
    this.maireEval = maireEvalArray[pieceIndex];
    this.loicEval = loicEvalArray[pieceIndex];
    if (this.c == 0) this.code = codeArrayB[pieceIndex];
    else this.code = codeArrayN[pieceIndex];

    grid[this.i][this.j].piece = this;

    this.updatePosEval();
  }

  void setRoques(int petit, int grand) { // Setup roques pour les tours
    this.petitRoquable = petit;
    this.grandRoquable = grand;
  }

  void show() {
    if (this.enPassantable == 1 && nbTour == this.saveTour + 1) {
      this.enPassantable = 0;
    }

    imageMode(CENTER);
    fill(0);
    noStroke();

    float posX, posY;
    if (this.dragging) {
      posX = mouseX;
      posY = mouseY;
    } else {
      if (pointDeVue) {
        posX = this.i*w + w/2 + offsetX;
        posY = this.j*w + w/2 + offsetY;
      } else {
        posX = width - (this.i*w + w/2);
        posY = height - (this.j*w + w/2);
      }
    }

    if (this.c == 0) image(imageArrayB[pieceIndex], posX, posY, pieceSize, pieceSize);
    else image(imageArrayN[pieceIndex], posX, posY, pieceSize, pieceSize);
  }

  void setRoques(int roque, int proque, int groque) {
    this.roquable = roque;
    this.petitRoquable = proque;
    this.grandRoquable = groque;
  }

  void updatePosEval() {
    if (this.c == 0) {
      this.mairePosEval = mairePosArray[pieceIndex][this.i][this.j] * (1 - endGameWeight);
      this.mairePosEval += mairePosArrayEnd[pieceIndex][this.i][this.j] * endGameWeight;

      this.loicPosEval = loicPosArray[pieceIndex][this.i][this.j];// * (1 - endGameWeight);
    } else {
      this.mairePosEval = mairePosArray[pieceIndex][7-this.i][7-this.j] * (1 - endGameWeight);
      this.mairePosEval += mairePosArrayEnd[pieceIndex][7-this.i][7-this.j] * endGameWeight;

      this.loicPosEval = loicPosArray[pieceIndex][7-this.i][7-this.j];// * (1 - endGameWeight);
    }
  }

  void setPlace(int i, int j) {
    grid[this.i][this.j].piece = null;
    this.i = i;
    this.j = j;
    grid[i][j].piece = this;

    this.updatePosEval();
  }

  void quickMove(int i, int j) {
    grid[this.i][this.j].piece = null;
    this.i = i;
    this.j = j;
    grid[i][j].piece = this;

    if (this.type == "roi") {
      if ((i == 4 && j == 0 && this.c == 1) || (i == 4 && j == 7 && this.c == 0)) this.roquable = 1;
      else this.roquable = 0;
    } else if (this.type == "tour") {
      if (this.c == 0) {
        if ((i == 0 && j == 7) || (i == 7 && j == 7)) this.roquable = 1;
        else this.roquable = 0;
      } else {
        if ((i == 0 && j == 0) || (i == 7 && j == 0)) this.roquable = 1;
        else this.roquable = 0;
      }
    }

    this.updatePosEval();
  }

  void move(Move m) {
    grid[this.i][this.j].piece = null;
    if (m.capture != null) { //captures
      removePiece(m.capture);
    }
    this.i = m.i;
    this.j = m.j;
    grid[this.i][this.j].piece = this;

    this.updatePosEval();
  }

  ArrayList generateMoves(boolean withCastle, boolean engine) {
    ArrayList<Move> moves = new ArrayList<Move>();

    switch (this.type) {
      case "pion":
        moves = getPawnMoves(this, engine);
      break;
      case "cavalier":
        moves = getKnightMoves(this);
      break;

      case "fou":
        moves = getBishopMoves(this);
      break;

      case "tour":
        moves = getRookMoves(this);
      break;

      case "dame":
        moves = getRookMoves(this);
        moves.addAll(getBishopMoves(this));
      break;

      case "roi":
        moves = getKingMoves(this, withCastle);
      break;
    }

    return moves;
  }

  ArrayList generateQuietMoves(boolean engine) {
    ArrayList<Move> moves = new ArrayList<Move>();

    switch (this.type) {
      case "pion":
        moves = getQuietPawnMoves(this, this.c, engine);
      break;
      case "cavalier":
        moves = getQuietKnightMoves(this, this.c);
      break;

      case "fou":
        moves = getQuietBishopMoves(this, this.c);
      break;

      case "tour":
        moves = getQuietRookMoves(this, this.c);
      break;

      case "dame":
        moves = getQuietRookMoves(this, this.c);
        moves.addAll(getQuietBishopMoves(this, this.c));
      break;

      case "roi":
        moves = getQuietKingMoves(this, this.c);
      break;
    }

    return moves;
  }

  ArrayList generateLegalMoves(boolean withCastle, boolean engine) {
    ArrayList<Move> pseudoMoves = this.generateMoves(withCastle, engine);
    pseudoMoves = removeIllegalMoves(this, pseudoMoves);
    return pseudoMoves;
  }

  ArrayList generateQuietLegalMoves(boolean engine) {
    ArrayList<Move> quietsMoves = this.generateQuietMoves(engine);
    quietsMoves = removeIllegalMoves(this, quietsMoves);
    return quietsMoves;
  }

  void select(boolean s) {
    if (s) { //sélection
      ArrayList<Move> moves = this.generateLegalMoves(true, false);

      //Pour chaque moves, on l'affiche
      for (int i = 0; i < moves.size(); i++) {
        grid[moves.get(i).i][moves.get(i).j].possibleMove = moves.get(i);
      }

    } else {
      this.dragging = false;
    }
  }

  void fly() {
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (grid[i][j].piece == null) grid[i][j].freeMove = true;
      }
    }
  }
}

/////////////////////////////////////////////////////////////////

// Genération des coups des pièces

ArrayList getKnightMoves(Piece p) {
  ArrayList<Move> moves = new ArrayList<Move>();

  int[] gi = {p.i-2, p.i-2, p.i+2, p.i+2, p.i+1, p.i+1, p.i-1, p.i-1};
  int[] gj = {p.j-1, p.j+1, p.j-1, p.j+1, p.j+2, p.j-2, p.j-2, p.j+2};

  for (int i = 0; i < gi.length; i++) {
    if (gi[i] >= 0 && gi[i] < rows && gj[i] >= 0 && gj[i] < cols) {
      if (grid[gi[i]][gj[i]].piece != null) { //si il y a une pièce
        if (grid[gi[i]][gj[i]].piece.c != p.c) { //si la pièce est adverse
          moves.add(new Move(p, gi[i], gj[i], grid[gi[i]][gj[i]].piece, 0));
        }
      } else {
        moves.add(new Move(p, gi[i], gj[i], null, 0));
      }
    }
  }

  return moves;
}

ArrayList getBishopMoves(Piece p) {
  ArrayList<Move> moves = new ArrayList<Move>();

  //Bas-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  //Haut-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  //Haut-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  //Bas-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  return moves;
}

ArrayList getRookMoves(Piece p) {
  ArrayList<Move> moves = new ArrayList<Move>();

  for (int i = p.i+1; i < cols; i++) { //Droite
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != p.c) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, i, p.j, null, 0));
  }

  for (int j = p.j+1; j < rows; j++) { //Bas
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != p.c) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, p.i, j, null, 0));
  }

  for (int i = p.i-1; i >= 0; i--) { //Gauche
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != p.c) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, i, p.j, null, 0));
  }

  for (int j = p.j-1; j >= 0; j--) { //Haut
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != p.c) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, p.i, j, null, 0));
  }

  return moves;
}

ArrayList getKingMoves(Piece p, boolean withCastle) {
  ArrayList<Move> moves = new ArrayList<Move>();

  //Déplacements classiques
  for (int i = -1; i <= 1; i++) {
    if (p.i+i < 0 || p.i+i >= rows) continue;
    for (int j = -1; j <= 1; j++) {
      if (p.j+j < 0 || p.j+j >= cols) continue;
      if (i == 0 && j == 0) continue;
      Cell cell = grid[p.i+i][p.j+j];
      if (cell.piece != null) {
        if (cell.piece.c != p.c) {
          moves.add(new Move(p, cell.i, cell.j, cell.piece, 0));
        }
      } else {
        moves.add(new Move(p, cell.i, cell.j, null, 0));
      }
    }
  }

  //Roque
  if (withCastle == true) { // ??? variables globales pRoque et gRoque (b et n)

    if (p.roquable == 1 && playerInCheck(p.c) == -1) {
      for (int i = 0; i < pieces[p.c].size(); i++) {
        Piece p2 = pieces[p.c].get(i);

        if (p2.petitRoquable == 1) {
          if (grid[p.i+1][p.j].piece == null && grid[p.i+2][p.j].piece == null) {
            ArrayList<Move> MovesToTest = new ArrayList<Move>();
            MovesToTest.add(new Move(p, p.i+1, p.j, null, 0));
            MovesToTest.add(new Move(p, p.i+2, p.j, null, 0));
            if (findIllegalMoves(p, MovesToTest).size() == 0) {
              moves.add(new Move(p, p.i+2, p.j, null, 1));
            }
          }

        } else if (p2.grandRoquable == 1) {
          if (grid[p.i-1][p.j].piece == null && grid[p.i-2][p.j].piece == null && grid[p.i-3][p.j].piece == null) {
            ArrayList<Move> MovesToTest = new ArrayList<Move>();
            MovesToTest.add(new Move(p, p.i-1, p.j, null, 0));
            MovesToTest.add(new Move(p, p.i-2, p.j, null, 0));
            if (findIllegalMoves(p, MovesToTest).size() == 0) {
              moves.add(new Move(p, p.i-2, p.j, null, 2));
            }
          }
        }

      }
    }

  }
  return moves;
}

ArrayList getPawnMoves(Piece p, boolean engine) {
  ArrayList<Move> moves = new ArrayList<Move>();

  if (p.c == 0) { //pion blanc

    //captures diagonales
    int tempI = p.i+1;
    int tempJ = p.j-1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != p.c) {

          if (p.j == 1) {
            //promotion
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }

        }
      }
    }
    tempI = p.i-1;
    tempJ = p.j-1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != p.c) {

          if (p.j == 1) {
            //promotion
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }

        }
      }
    }

    if (p.j == 6) {
      //première avancée
      for (int j = 1; j <= 2; j++) {
        if (p.j-j < 0) continue;
        if (grid[p.i][p.j-j].piece != null) break;
        moves.add(new Move(p, p.i, p.j-j, null, 0));
      }
    } else {
      //autre avancée
      if (p.j-1 >= 0 && grid[p.i][p.j-1].piece == null) {
        if (p.j == 1) {
          if (engine == false) {
            moves.add(new Move(p, p.i, p.j-1, null, 4));
          } else {
            moves.add(new Move(p, p.i, p.j-1, null, 5));
            moves.add(new Move(p, p.i, p.j-1, null, 6));
            moves.add(new Move(p, p.i, p.j-1, null, 7));
            moves.add(new Move(p, p.i, p.j-1, null, 8));
          }
        } else {
          moves.add(new Move(p, p.i, p.j-1, null, 0));
        }
      }
    }

    //En passant
    if (p.j == 3) {
      if (p.i - 1 >= 0) { //à gauche
        if (grid[p.i-1][p.j].piece != null) {
          if (grid[p.i-1][p.j].piece.enPassantable == 1) {
            moves.add(new Move(p, p.i-1, p.j-1, grid[p.i-1][p.j].piece, 3));
          }
        }
      }
      if (p.i + 1 < cols) { //à droite
        if (grid[p.i+1][p.j].piece != null) {
          if (grid[p.i+1][p.j].piece.enPassantable == 1) {
            moves.add(new Move(p, p.i+1, p.j-1, grid[p.i+1][p.j].piece, 3));
          }
        }
      }
    }
  } else { //pion noir

    //captures diagonales
    int tempI = p.i+1;
    int tempJ = p.j+1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != p.c) {
          if (p.j == 6) {
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }
        }
      }
    }
    tempI = p.i-1;
    tempJ = p.j+1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != p.c) {
          if (p.j == 6) {
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }
        }
      }
    }
    if (p.j == 1) { //première avancée
      for (int j = 1; j <= 2; j++) {
        if (p.j+j >= cols) continue;
        if (grid[p.i][p.j+j].piece != null) break;
        moves.add(new Move(p, p.i, p.j+j, null, 0));
      }
    } else { //autre avancée
      if (p.j+1 < cols && grid[p.i][p.j+1].piece == null) {
          if (p.j == 6) {
            if (engine == false) {
              moves.add(new Move(p, p.i, p.j+1, null, 4));
            } else {
              moves.add(new Move(p, p.i, p.j+1, null, 5));
              moves.add(new Move(p, p.i, p.j+1, null, 6));
              moves.add(new Move(p, p.i, p.j+1, null, 7));
              moves.add(new Move(p, p.i, p.j+1, null, 8));
            }
          } else {
            moves.add(new Move(p, p.i, p.j+1, null, 0));
          }
      }
    }

    //En passant
    if (p.j == 4) {
      if (p.i - 1 >= 0) { //à gauche
        if (grid[p.i-1][p.j].piece != null) {
          if (grid[p.i-1][p.j].piece.enPassantable == 1) {
            moves.add(new Move(p, p.i-1, p.j+1, grid[p.i-1][p.j].piece, 3));
          }
        }
      }
      if (p.i + 1 < cols) { //à droite
        if (grid[p.i+1][p.j].piece != null) {
          if (grid[p.i+1][p.j].piece.enPassantable == 1) {
            moves.add(new Move(p, p.i+1, p.j+1, grid[p.i+1][p.j].piece, 3));
          }
        }
      }
    }
  }

  return moves;
}

/////////////////////////////////////////////////////////////////

// Génération des coups quiets des pièces
// colorToNotDetect : Si le paramètre est de la même couleur que la pièce, renvoie les captures, sinon renvoie les coups pour pgn

ArrayList getQuietKnightMoves(Piece p, int colorToNotDetect) {
  ArrayList<Move> moves = new ArrayList<Move>();

  int[] gi = {p.i-2, p.i-2, p.i+2, p.i+2, p.i+1, p.i+1, p.i-1, p.i-1};
  int[] gj = {p.j-1, p.j+1, p.j-1, p.j+1, p.j+2, p.j-2, p.j-2, p.j+2};

  for (int i = 0; i < gi.length; i++) {
    if (gi[i] >= 0 && gi[i] < rows && gj[i] >= 0 && gj[i] < cols) {
      if (grid[gi[i]][gj[i]].piece != null) { //si il y a une pièce
        if (grid[gi[i]][gj[i]].piece.c != colorToNotDetect) { //si la pièce n'est pas de la couleur à ne pas détecter
          moves.add(new Move(p, gi[i], gj[i], grid[gi[i]][gj[i]].piece, 0));
        }
      }
    }
  }

  return moves;
}

ArrayList getQuietBishopMoves(Piece p, int colorToNotDetect) {
  ArrayList<Move> moves = new ArrayList<Move>();

  //Bas-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  //Haut-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  //Haut-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  //Bas-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  return moves;
}

ArrayList getQuietRookMoves(Piece p, int colorToNotDetect) {
  ArrayList<Move> moves = new ArrayList<Move>();

  for (int i = p.i+1; i < cols; i++) { //Droite
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
  }

  for (int j = p.j+1; j < rows; j++) { //Bas
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
  }

  for (int i = p.i-1; i >= 0; i--) { //Gauche
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
  }

  for (int j = p.j-1; j >= 0; j--) { //Haut
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
  }

  return moves;
}

ArrayList getQuietKingMoves(Piece p, int colorToNotDetect) {
  ArrayList<Move> moves = new ArrayList<Move>();

  //Déplacements classiques
  for (int i = -1; i <= 1; i++) {
    if (p.i+i < 0 || p.i+i >= rows) continue;
    for (int j = -1; j <= 1; j++) {
      if (p.j+j < 0 || p.j+j >= cols) continue;
      if (i == 0 && j == 0) continue;
      Cell cell = grid[p.i+i][p.j+j];
      if (cell.piece != null) {
        if (cell.piece.c != colorToNotDetect) {
          moves.add(new Move(p, cell.i, cell.j, cell.piece, 0));
        }
      }
    }
  }
  return moves;
}

ArrayList getQuietPawnMoves(Piece p, int colorToDetect, boolean engine) {
  ArrayList<Move> moves = new ArrayList<Move>();

  if (p.c == 0) { //pion blanc

    //captures diagonales
    int tempI = p.i+1;
    int tempJ = p.j-1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != colorToDetect) {

          if (p.j == 1) {
            //promotion
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }

        }
      }
    }
    tempI = p.i-1;
    tempJ = p.j-1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != colorToDetect) {

          if (p.j == 1) {
            //promotion
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }

        }
      }
    }

    //En passant
    if (p.j == 3) {
      if (p.i - 1 >= 0) { //à gauche
        if (grid[p.i-1][p.j].piece != null) {
          if (grid[p.i-1][p.j].piece.enPassantable == 1) {
            moves.add(new Move(p, p.i-1, p.j-1, grid[p.i-1][p.j].piece, 3));
          }
        }
      }
      if (p.i + 1 < cols) { //à droite
        if (grid[p.i+1][p.j].piece != null) {
          if (grid[p.i+1][p.j].piece.enPassantable == 1) {
            moves.add(new Move(p, p.i+1, p.j-1, grid[p.i+1][p.j].piece, 3));
          }
        }
      }
    }
  } else { //pion noir

    //captures diagonales
    int tempI = p.i+1;
    int tempJ = p.j+1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != colorToDetect) {
          if (p.j == 6) {
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }
        }
      }
    }
    tempI = p.i-1;
    tempJ = p.j+1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != colorToDetect) {
          if (p.j == 6) {
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }
        }
      }
    }

    //En passant
    if (p.j == 4) {
      if (p.i - 1 >= 0) { //à gauche
        if (grid[p.i-1][p.j].piece != null) {
          if (grid[p.i-1][p.j].piece.enPassantable == 1) {
            moves.add(new Move(p, p.i-1, p.j+1, grid[p.i-1][p.j].piece, 3));
          }
        }
      }
      if (p.i + 1 < cols) { //à droite
        if (grid[p.i+1][p.j].piece != null) {
          if (grid[p.i+1][p.j].piece.enPassantable == 1) {
            moves.add(new Move(p, p.i+1, p.j+1, grid[p.i+1][p.j].piece, 3));
          }
        }
      }
    }
  }

  return moves;
}
