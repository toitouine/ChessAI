/////////////////////////////////////////////////////////////////

// Zobrist hashing

// Permet d'obtenir une clé presque unique pour chaque position de manière rapide
// RNG : XOR-Shift algorithm (avec rngState)

// initZobristKeys : pour initialiser les clés correspondant à chaque données concernant une positin (pas pion en passant)
// calculateHash : pour calculer la clé d'une position (au départ d'une nouvelle position par exemple)
// updateHash : actualise le hash en fonction du coup qui a été joué (utile pendant la recherche car s'auto-inverse)

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;

public class Zobrist {

  private Board board;

  public long hash = 0;
  private long rngState = 1804289383;

  // Clés
  private long[][] piecesOnSquare = new long[12][64];
  private long[] castlingRights = new long[16];
  private long[] enPassantSquare = new long[16];
  private long blackToMove;

  private int castleState = 0;
  private final int whitePetitRoque = 8;
  private final int whiteGrandRoque = 4;
  private final int blackPetitRoque = 2;
  private final int blackGrandRoque = 1;

  private int[][] promoZobristIndex = new int[2][4];

  public Zobrist(Board board) {
    this.board = board;
    int[] index = {1, 2, 3, 4};

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 4; j++) {
        if (i == 0) promoZobristIndex[i][j] = index[j];
        else promoZobristIndex[i][j] = index[j] + 6;
      }
    }


    initZobristKeys();
  }

  private void initZobristKeys() {
    rngState = 1804289383;

    // Initialise les clés des pièces sur chaque case (zobristIndex, i, j);
    for (int p = 0; p < 12; p++) {
      for (int i = 0; i < 64; i++) {
        piecesOnSquare[p][i] = generateRandomNumber();
      }
    }

    // Initialise les clés du roque
    for (int i = 0; i < 16; i++) {
      castlingRights[i] = generateRandomNumber();
    }

    // Initialise en passant (pas pour l'instant)
    for (int i = 0; i < 16; i++) {
      enPassantSquare[i] = generateRandomNumber();
    }

    // Initialise blackToMove (permet de gérer le trait)
    blackToMove = generateRandomNumber();
  }

  public void exportKeys() {
    ArrayList<Long> keys = new ArrayList<Long>();

    for (int p = 0; p < 12; p++) {
      for (int i = 0; i < 64; i++) {
        keys.add(piecesOnSquare[p][i]);
      }
    }

    for (int i = 0; i < 16; i++) keys.add(castlingRights[i]);
    for (int i = 0; i < 16; i++) keys.add(enPassantSquare[i]);
    keys.add(blackToMove);
    System.out.println(keys);
  }

  public long calculateHash() {
    hash = 0;

    // Pièces
    for (int i = 0; i < 2; i++) {
      for (Piece p : board.pieces(i)) {
        hash ^= piecesOnSquare[p.index][p.square];
      }
    }

    // Roques
    castleState = 0;
    if (board.whitePetitRoque) castleState += whitePetitRoque;
    if (board.whiteGrandRoque) castleState += whiteGrandRoque;
    if (board.blackPetitRoque) castleState += blackPetitRoque;
    if (board.blackGrandRoque) castleState += blackGrandRoque;
    hash ^= castlingRights[castleState];

    // Tour de qui
    if (board.tourDeQui == Player.Black) hash ^= blackToMove;

    return this.hash;
  }

  // public long updateHash(Move m) {
  //   // XOR out
  //   hash ^= piecesOnSquare[m.piece.index][m.fromI][m.fromJ];
  //   if (m.capture != null) hash ^= piecesOnSquare[m.capture.index][m.capture.i][m.capture.j];
  //
  //   // XOR in
  //   hash ^= piecesOnSquare[m.piece.index][m.i][m.j];
  //
  //   // Changement de tour
  //   hash ^= blackToMove;
  //
  //   // Déplacements du roque
  //   if (m.flag == Flag.PetitRoque) {
  //     int jPos = (m.piece.c == 0) ? 7 : 0;
  //     int index = (m.piece.c == 0) ? Piece.Tour : (Piece.Tour + 6);
  //     hash ^= piecesOnSquare[index][7][jPos];
  //     hash ^= piecesOnSquare[index][5][jPos];
  //   } else if (m.flag == Flag.GrandRoque) {
  //     int jPos = (m.piece.c == 0) ? 7 : 0;
  //     int index = (m.piece.c == 0) ? Piece.Tour : (Piece.Tour + 6);
  //     hash ^= piecesOnSquare[index][0][jPos];
  //     hash ^= piecesOnSquare[index][3][jPos];
  //   }
  //
  //   // Roques (droits)
  //   hash ^= castlingRights[castleState]; // Retire tous les droits au roque du hash
  //
  //   castleState = 0; // Actualise la variable de droits au roque (4 bits)
  //   if (board.whitePetitRoque) castleState += whitePetitRoque;
  //   if (board.whiteGrandRoque) castleState += whiteGrandRoque;
  //   if (board.blackPetitRoque) castleState += blackPetitRoque;
  //   if (board.blackGrandRoque) castleState += blackGrandRoque;
  //
  //   hash ^= castlingRights[castleState]; // Ajoute les droits au roques au hash
  //
  //   // Promotion TODO A VOIR, VALEURS DANS ENUM ??
  //   // if (m.special >= 5) {
  //   //   // On retire le pion du hash
  //   //   hash ^= piecesOnSquare[m.piece.index][m.i][m.j];
  //   //
  //   //   // On ajoute la pièce de promotion au hash
  //   //   int index = this.promoZobristIndex[m.piece.c][m.special-5];
  //   //   this.hash ^= this.piecesOnSquare[index][m.i][m.j];
  //   // }
  //
  //   Debug.log("test", "Hash actualisé : " + this.hash);
  //
  //   return hash;
  // }

  // XOR-Shift (pour avoir les mêmes clés à chaque fois)
  private long generateRandomNumber() {
    long number = rngState;

    number ^= number << 13;
    number ^= number >> 17;
    number ^= number << 5;

    rngState = number;

    return number;
  }
}
