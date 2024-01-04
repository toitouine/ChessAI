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
  private static final int[] petitRoqueIndex = {8, 2}; // Blanc puis noir
  private static final int[] grandRoqueIndex = {4, 1};

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
    hash ^= getCastlingKey(board.petitRoque, board.grandRoque);

    // Tour de qui
    if (board.tourDeQui == Player.Black) hash ^= blackToMove;

    return hash;
  }

  static public long getCastlingKey(boolean[] petitRoque, boolean[] grandRoque) {
    int castleState = 0;
    for (int i = 0; i < 2; i++) {
      if (petitRoque[i]) castleState += petitRoqueIndex[i];
      if (grandRoque[i]) castleState += grandRoqueIndex[i];
    }
    return castlingRights[castleState];
  }

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
