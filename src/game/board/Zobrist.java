/////////////////////////////////////////////////////////////////

// Zobrist hashing

// Permet d'obtenir une clé presque unique pour chaque position de manière rapide
// RNG : XOR-Shift algorithm (avec rngState)

// initZobristKeys : pour initialiser les clés correspondant à chaque données concernant une position (pas case en passant)
// calculateHash : pour recalculer complètement la clé d'une position (au départ d'une nouvelle position par exemple)
// updateHash : actualise le hash en fonction du coup qui a été joué (utile pendant la recherche car s'auto-inverse)

// TODO : case en passant

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;

final public class Zobrist {

  private Zobrist() {}

  private static long rngState = 1804289383;

  // Clés
  public static final long[][] piecesOnSquare = new long[Piece.NumberOfPiece][64];
  public static final long[] castlingRights = new long[16];
  public static final long[] enPassantSquare = new long[16];
  public static final long blackToMove;

  // Permet de représenter les droits du roque en une seule variable allant de 0 à 15 (somme des droits)
  private static int castleState = 0;
  private static final int[] petitRoque = {8, 2}; // Blanc puis noir
  private static final int[] grandRoque = {4, 1};

  // Initialisation des clés
  static {
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

    // Initialise en passant (pas utilisé pour l'instant)
    for (int i = 0; i < 16; i++) {
      enPassantSquare[i] = generateRandomNumber();
    }

    // Initialise blackToMove (permet de gérer le trait)
    blackToMove = generateRandomNumber();
  }

  static public void printKeys() {
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

  static public long calculateHash(Board board) {
    long hash = 0;

    // Pièces
    for (int i = 0; i < 64; i++) {
      if (board.grid(i) != null) hash ^= piecesOnSquare[board.grid(i).index][i];
    }

    // Roques
    castleState = 0;
    for (int i = 0; i < 2; i++) {
      if (board.petitRoque[i]) castleState += petitRoque[i];
      if (board.grandRoque[i]) castleState += grandRoque[i];
    }
    hash ^= castlingRights[castleState];

    // Tour de qui
    if (board.tourDeQui == Player.Black) hash ^= blackToMove;

    return hash;
  }

  // public long updateHash(Move m) {
  //   // XOR out DONE
  //   hash ^= piecesOnSquare[m.piece.index][m.fromI][m.fromJ];
  //   if (m.capture != null) hash ^= piecesOnSquare[m.capture.index][m.capture.i][m.capture.j];
  //
  //   // XOR in DONE
  //   hash ^= piecesOnSquare[m.piece.index][m.i][m.j];
  //
  //   // Changement de tour DONE
  //   hash ^= blackToMove;
  //
  //   // Déplacements du roque DONE
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

  // Algorithme : XOR-Shift (pour avoir les mêmes clés à chaque fois)
  static private long generateRandomNumber() {
    long number = rngState;

    number ^= number << 13;
    number ^= number >> 17;
    number ^= number << 5;

    rngState = number;

    return number;
  }
}
