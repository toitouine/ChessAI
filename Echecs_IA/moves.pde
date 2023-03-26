/////////////////////////////////////////////////////////////////

// Move class

// Constructeur : Piece, targetI, targetJ, Capture, Special

// Play et Unplay : Jouer (vraiment) le coup
// Make et Unmake : Jouer (prévisualiser) le coup
// Handle et Unhandle : Commun aux deux

// 1 = petit roque; 2 = grand roque;  3 = en passant; 4 = promotion
// 5 = dame; 6 = tour; 7 = fou; 8 = cavalier

/////////////////////////////////////////////////////////////////

int[] promoMaterials = {800, 400, 230, 220};

class Move {
  Piece piece;
  Piece capture;
  byte fromI, fromJ, i, j, special;

  // Meilleur coup après
  Move bestChild = null;

  // Probabilité bon coup
  float scoreGuess = 0;

  // Sauvegardes pour make et unmake
  int saveRoque, savePRoque, saveGRoque;
  Piece[] saveEnPassantArray = new Piece[2];
  Piece savePromo = null;
  Piece tourQuiRoque = null;

  Move(Piece piece, int i, int j, Piece capture, int special) {
    this.piece = piece;
    this.i = byte(i);
    this.j = byte(j);
    this.fromI = byte(piece.i);
    this.fromJ = byte(piece.j);
    this.capture = capture;
    this.special = byte(special);

    if (special == 1) this.tourQuiRoque = grid[this.i+1][this.j].piece;
    else if (special == 2) this.tourQuiRoque = grid[this.i-2][this.j].piece;
  }

  String stringify() {
    return str(this.fromI) + str(this.fromJ) + str(this.i) + str(this.j);
  }

  void log() {
    print(this.piece.type + "->" + grid[this.i][this.j].name + " (" + this.special + ") + " + this.scoreGuess + " | ");
  }

  boolean equals(Move m2) {
    if (m2 == null) return false;
    return (this.piece == m2.piece && this.fromI == m2.fromI && this.fromJ == m2.fromJ && this.i == m2.i && this.j == m2.j && this.capture == m2.capture && this.special == m2.special);
  }

  void savePieceData() {
    this.saveRoque = this.piece.roquable;
    this.savePRoque = this.piece.petitRoquable;
    this.saveGRoque = this.piece.grandRoquable;
    saveEnPassantArray[0] = currentEnPassantable[0];
    saveEnPassantArray[1] = currentEnPassantable[1];
    this.savePromo = null;
  }

  void handle(boolean really) {
    // Sauvegardes
    this.savePieceData();

    // En passant
    if (this.piece.pieceIndex == PION_INDEX && this.piece.j + ( (this.piece.c == 0) ? -2 : 2 ) == this.j) {
     currentEnPassantable[this.piece.c] = this.piece;
    }

    // Déplacement et capture
    this.piece.move(this);

    // Coups spéciaux
    if (really && this.special == 4) enPromotion = this.piece;
    if (this.special == 1) { tourQuiRoque.setPlace(this.i-1, this.j); }
    else if (this.special == 2) { tourQuiRoque.setPlace(this.i+1, this.j); }
    else if (this.special >= 5) {
      removePiece(this.piece);
      if (this.special == 5) this.savePromo = new Dame(this.i, this.j, this.piece.c);
      if (this.special == 6) this.savePromo = new Tour(this.i, this.j, this.piece.c);
      if (this.special == 7) this.savePromo = new Fou(this.i, this.j, this.piece.c);
      if (this.special == 8) this.savePromo = new Cavalier(this.i, this.j, this.piece.c);
      pieces[this.piece.c].add(this.savePromo);
      materials[this.piece.c] += promoMaterials[this.special-5];
    }

    // Roques
    if (this.piece.roquable != -1) this.piece.roquable = 0;
    else if (this.piece.petitRoquable != -1) this.piece.petitRoquable = this.piece.grandRoquable = 0;

    // Enlève le pion en passant de l'adversaire
    int opp = (this.piece.c == 0) ? 1 : 0;
    if (currentEnPassantable[opp] != null) {
      currentEnPassantable[opp] = null;
    }

    // Variables
    if (really) { if (enPromotion == null) tourDeQui = (tourDeQui == 0) ? 1 : 0; }
    else { tourDeQui = (tourDeQui == 0) ? 1 : 0; }
    nbTour += 0.5;
    if (this.capture != null) { calcEndGameWeight(); materials[this.capture.c] -= this.capture.maireEval; }

    zobrist.updateHash(this);
    addHashToHistory(zobrist.hash);
  }

  void unhandle(boolean really) {
    //Retour des sauvegardes
    if (this.saveRoque != -1) this.piece.roquable = this.saveRoque;
    if (this.savePRoque != -1) this.piece.petitRoquable = this.savePRoque;
    if (this.saveGRoque != -1) this.piece.grandRoquable = this.saveGRoque;
    currentEnPassantable[0] = saveEnPassantArray[0];
    currentEnPassantable[1] = saveEnPassantArray[1];

    // Coups spéciaux
    if (this.special == 1) { Piece p = grid[this.i-1][this.j].piece; p.setPlace(this.i+1, this.j); }
    else if (this.special == 2) { Piece p = grid[this.i+1][this.j].piece; p.setPlace(this.i-2, this.j); }
    else if (this.special >= 5) {
      pieces[this.piece.c].add(this.piece);
      removePiece(this.savePromo);
      materials[this.piece.c] -= promoMaterials[this.special-5];
    }

    // Déplacement de la pièce
    this.piece.setPlace(this.fromI, this.fromJ);

    // Update les variables
    tourDeQui = (tourDeQui == 0) ? 1 : 0;
    nbTour -= 0.5;

    // Update d'autres variables et réssucite la pièce capturée
    if (this.capture != null) {
      pieces[this.capture.c].add(this.capture);
      grid[this.capture.i][this.capture.j].piece = this.capture;

      calcEndGameWeight();
      materials[this.capture.c] += this.capture.maireEval;
    }

    zobrist.updateHash(this);
    removeLastFromHashHistory(); //retire le dernier hash de l'historique
  }

  void play() {
    this.handle(true);

    // Update des pièces du plateau
    piecesToDisplay.clear();
    piecesToDisplay.addAll(pieces[0]);
    piecesToDisplay.addAll(pieces[1]);

    // Fonctions très utiles (ou pas)
    deselectAll();
    updatePGN(this);
    checkGameState();
    playSound(this);
    clearBookHighlight();

    // Move marks
    setMoveMarks(grid[this.fromI][this.fromJ], grid[this.i][this.j]);

    // Historiques
    addFenToHistory(generateFEN());
    movesHistory.add(this);

    // Divers et variés
    if (useTime && !gameEnded) ta.switchTimers(tourDeQui);
    if (showGraph) updateGraph();

    // Hacker
    if (useHacker && hackerPret && !hackerAPImode && !isNextMoveRestranscrit) {
      cheat(this.piece.c, this.fromI, this.fromJ, this.i, this.j, this.special);
    }
    isNextMoveRestranscrit = false;

    // Les Moutons !
    if (ENABLE_ARNAQUES && !useHacker && (joueurs.get(0).name == "LesMoutons" || joueurs.get(1).name == "LesMoutons")) {
      arnaques();
    }

    // Efface la table de transposition
    // On le fait à chaque coup (ou presque) pour éviter des conflits à propos de la table quand deux maires jouent ensemble, ou quand les moutons interviennent...
    //if (!((joueurs.get(0).name == "LeMaire" && joueurs.get(1).name == "Humain") || (joueurs.get(0).name == "Humain" && joueurs.get(1).name == "LeMaire"))) {
    tt.clear();
    //}
  }

  void replay() {
    this.handle(true);

    //Update des pièces du plateau
    piecesToDisplay.clear();
    piecesToDisplay.addAll(pieces[0]);
    piecesToDisplay.addAll(pieces[1]);

    //Fonctions très utiles (ou pas)
    deselectAll();
    playSound(this);
    clearBookHighlight();

    //Move marks
    setMoveMarks(grid[this.fromI][this.fromJ], grid[this.i][this.j]);
  }

  void unplay() {
    this.unhandle(true);

    piecesToDisplay.clear();
    piecesToDisplay.addAll(pieces[0]);
    piecesToDisplay.addAll(pieces[1]);

    deselectAll();
    playSound(this);
    clearBookHighlight();

    clearMoveMarks();
  }

  void make() {
    this.handle(false);
  }

  void unmake() {
    this.unhandle(false);
  }

}

/////////////////////////////////////////////////////////////////

// Génération des coups

ArrayList<Move> generateAllMoves(int c, boolean withCastle, boolean engine) {

  ArrayList<Move> moves = new ArrayList<Move>();
  ArrayList<Piece> piecesToCheck = copyPieceArrayList(pieces[c]); //crée une *nouvelle instance* d'arraylist avec les pièces dans l'ordre (qui ne change pas dans la suite)

  for (int i = 0; i < piecesToCheck.size(); i++) {
    moves.addAll(piecesToCheck.get(i).generateMoves(withCastle, engine));
  }

  return moves;
}

ArrayList<Move> generateAllLegalMoves(int c, boolean withCastle, boolean engine) {
  ArrayList<Move> moves = new ArrayList<Move>();
  ArrayList<Piece> piecesToCheck = copyPieceArrayList(pieces[c]);

  for (int i = 0; i < piecesToCheck.size(); i++) {
    moves.addAll(piecesToCheck.get(i).generateLegalMoves(withCastle, engine));
  }

  return moves;
}

ArrayList<Move> generateAllCaptures(int c, boolean engine) {
  ArrayList<Move> moves = new ArrayList<Move>();
  ArrayList<Piece> piecesToCheck = copyPieceArrayList(pieces[c]);

  for (int i = 0; i < piecesToCheck.size(); i++) {
    moves.addAll(piecesToCheck.get(i).generateQuietLegalMoves(engine));
  }

  return moves;
}
