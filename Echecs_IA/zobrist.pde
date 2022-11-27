class Zobrist {

  long hash = 0;
  long[][][] piecesOnSquare = new long[12][8][8];
  long[] castlingRights = new long[16];
  long[] enPassantSquare = new long[16];
  long blackToMove;

  int castleState = 0; // 1101 KQkq
  final int whitePetitRoque = 8;
  final int whiteGrandRoque = 4;
  final int blackPetitRoque = 2;
  final int blackGrandRoque = 1;

  int[][] promoZobristIndex = new int[2][4];

  Zobrist() {
    int[] index = {1, 2, 3, 4};

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 4; j++) {
        if (i == 0) this.promoZobristIndex[i][j] = index[j];
        else this.promoZobristIndex[i][j] = index[j] + 6;
      }
    }


    this.initZobristKeys();
  }

  void initZobristKeys() {
    rngState = 1804289383;

    // Init pieces on square (zobristIndex, i, j);
    for (int p = 0; p < 12; p++) {
      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
          this.piecesOnSquare[p][i][j] = generateRandomNumber();
        }
      }
    }

    // Init caslingRights
    for (int i = 0; i < 16; i++) {
      this.castlingRights[i] = generateRandomNumber();
    }

    // Init enPassant (pas pour l'instant)
    for (int i = 0; i < 16; i++) {
      this.enPassantSquare[i] = generateRandomNumber();
    }

    // Init blackToMove
    blackToMove = generateRandomNumber();
  }

  long initHash() {
    this.hash = 0;

    // pièces
    for (int i = 0; i < pieces.length; i++) {
      for (int j = 0; j < pieces[i].size(); j++) {
        Piece p = pieces[i].get(j);
        this.hash ^= this.piecesOnSquare[p.zobristIndex][p.i][p.j];
      }
    }

    // droits au roque
    castleState = 0;
    if (rois[0] != null && rois[0].roquable == 1) {
      if (grid[7][7].piece != null && grid[7][7].piece.petitRoquable == 1) castleState += whitePetitRoque;
      if (grid[0][7].piece != null && grid[0][7].piece.grandRoquable == 1) castleState += whiteGrandRoque;
    }
    if (rois[1] != null && rois[1].roquable == 1) {
      if (grid[7][0].piece != null && grid[7][0].piece.petitRoquable == 1) castleState += blackPetitRoque;
      if (grid[0][0].piece != null && grid[0][0].piece.grandRoquable == 1) castleState += blackGrandRoque;
    }
    this.hash ^= this.castlingRights[castleState];

    // tour de qui
    if (tourDeQui == 1) this.hash ^= this.blackToMove;

    // println("Generate from scratch zobrist key : " + this.hash);

    return this.hash;
  }

  long updateHash(Move m) {
    // xor out
    this.hash ^= this.piecesOnSquare[m.piece.zobristIndex][m.fromI][m.fromJ];
    if (m.capture != null) this.hash ^= this.piecesOnSquare[m.capture.zobristIndex][m.capture.i][m.capture.j];

    // xor in
    this.hash ^= this.piecesOnSquare[m.piece.zobristIndex][m.i][m.j];

    // changement de tour
    this.hash ^= this.blackToMove;

    // déplacements du roque
    if (m.special == 1) {
      int jPos = (m.piece.c == 0) ? 7 : 0;
      this.hash ^= this.piecesOnSquare[m.tourQuiRoque.zobristIndex][7][jPos];
      this.hash ^= this.piecesOnSquare[m.tourQuiRoque.zobristIndex][5][jPos];
    } else if (m.special == 2) {
      int jPos = (m.piece.c == 0) ? 7 : 0;
      this.hash ^= this.piecesOnSquare[m.tourQuiRoque.zobristIndex][0][jPos];
      this.hash ^= this.piecesOnSquare[m.tourQuiRoque.zobristIndex][3][jPos];
    }

    // droits au roque
    this.hash ^= this.castlingRights[castleState]; // Retire tous les droits au roque du hash

    castleState = 0; // Update la variable de droits au roque (4 bits)
    if (rois[0].roquable == 1) {
      if (grid[7][7].piece != null && grid[7][7].piece.petitRoquable == 1) castleState += whitePetitRoque;
      if (grid[0][7].piece != null && grid[0][7].piece.grandRoquable == 1) castleState += whiteGrandRoque;
    }
    if (rois[1].roquable == 1) {
      if (grid[7][0].piece != null && grid[7][0].piece.petitRoquable == 1) castleState += blackPetitRoque;
      if (grid[0][0].piece != null && grid[0][0].piece.grandRoquable == 1) castleState += blackGrandRoque;
    }
    this.hash ^= this.castlingRights[castleState]; // Ajoute les droits au roques au hash

    // promotion
    if (m.special >= 5) {
      // on retire le pion du hash
      this.hash ^= this.piecesOnSquare[m.piece.zobristIndex][m.i][m.j];

      // on ajoute la pièce de promotion au hash
      int index = this.promoZobristIndex[m.piece.c][m.special-5];
      this.hash ^= this.piecesOnSquare[index][m.i][m.j];
    }

    // println("Incrementally updated zobrist key : " + this.hash);

    return this.hash;
  }

}

// Pseudo Random Number Generator (XOR-Shift algorithm) pour avoir les mêmes clés à chaque fois
long generateRandomNumber() {
  long number = rngState;

  number ^= number << 13;
  number ^= number >> 17;
  number ^= number << 5;

  rngState = number;

  return number;
}
