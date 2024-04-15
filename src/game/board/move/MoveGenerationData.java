/////////////////////////////////////////////////////////////////

// MoveGenerationData
// Contient les données pré-calculées utiles pour générer
// les coups des pièces (voir MoveGenerator.java), comme par
// exemple les masks, nombres magiques...

/////////////////////////////////////////////////////////////////

import java.util.HashMap;

public final class MoveGenerationData {
  private MoveGenerationData() {}

  // Raccourcis pour les colonnes et lignes de l'échiquier
  static final private long Afile = Bitboard.Afile;
  static final private long Bfile = Bitboard.Bfile;
  static final private long Gfile = Bitboard.Gfile;
  static final private long Hfile = Bitboard.Hfile;
  static final private long rank1 = Bitboard.rank1;
  static final private long rank2 = Bitboard.rank2;
  static final private long rank7 = Bitboard.rank7;
  static final private long rank8 = Bitboard.rank8;

  // Attaques de différentes pièces depuis une case sous forme de bitboard
  static private long kingAttacks[];
  static private long knightAttacks[];

  // Masks pour la génération des coups des sliders (tour, fou, dame)
  static private long rookAttackMask[];

  // Table des coups par case pour la génération des coups des sliders
  // (voir MoveGenerator.java)
  static private long rookMoveTable[][];


  // Cases des pièces entre le roi et la tour pour tester la possibilité des roques
  static final public long[] petitRoquePiecesMask = {0b1100000L << 56, 0b1100000L};
  static final public long[] grandRoquePiecesMask = {0b1110L << 56, 0b1110L};

  // Intialise toutes les données
  static {
    generateKingAttacks();
    generateKnightAttacks();
    generateRookMasks();
    generateRookMoveTable();
  }

  /////////////////////////////////////////////////////////////////

  // Méthodes pour récupérer les données

  public static long kingAttacks(int square) {
    return kingAttacks[square];
  }

  public static long knightAttacks(int square) {
    return knightAttacks[square];
  }

  public static long rookMask(int square) {
    return rookAttackMask[square];
  }

  public static long rookMoves(int square, int index) {
    return rookMoveTable[square][index];
  }

  /////////////////////////////////////////////////////////////////

  // Les trois méthodes suivantes génèrent les attaques (ou masks) des
  // pièces pour générer les coups (ou les masks si la pièce est un slider)

  private static void generateKnightAttacks() {
    knightAttacks = new long[64];

    for (int i = 0; i < 64; i++) {
      long attacks = 0;
      attacks |= (1L << (i-10)) & ~Gfile & ~Hfile & ~rank1;
      attacks |= (1L << (i-17)) & ~Hfile & ~rank1 & ~rank2;
      attacks |= (1L << (i-15)) & ~Afile & ~rank1 & ~rank2;
      attacks |= (1L << (i-6))  & ~Afile & ~Bfile & ~rank1;
      attacks |= (1L << (i+10)) & ~Afile & ~Bfile & ~rank8;
      attacks |= (1L << (i+17)) & ~Afile & ~rank7 & ~rank8;
      attacks |= (1L << (i+15)) & ~Hfile & ~rank7 & ~rank8;
      attacks |= (1L << (i+6))  & ~Gfile & ~Hfile & ~rank8;
      knightAttacks[i] = attacks;
    }
  }

  private static void generateKingAttacks() {
    kingAttacks = new long[64];

    for (int i = 0; i < 64; i++) {
      long attacks = 0;
      attacks |= (1L << (i-9)) & ~Hfile & ~rank1;
      attacks |= (1L << (i-8)) & ~rank1;
      attacks |= (1L << (i-7)) & ~Afile & ~rank1;
      attacks |= (1L << (i-1)) & ~Hfile;
      attacks |= (1L << (i+1)) & ~Afile;
      attacks |= (1L << (i+7)) & ~Hfile & ~rank8;
      attacks |= (1L << (i+8)) & ~rank8;
      attacks |= (1L << (i+9)) & ~Afile & ~rank8;
      kingAttacks[i] = attacks;
    }
  }

  private static void generateRookMasks() {
    rookAttackMask = new long[64];

    for (int i = 0; i < 64; i++) {
      int file = i & 7; // Colonne de la case
      int rank = i >>> 3; // Ligne de la case
      long remove = 1L << i; // Cases à retirer du mask
      if (file != 0) remove |= Afile;
      if (file != 7) remove |= Hfile;
      if (rank != 7) remove |= rank1;
      if (rank != 0) remove |= rank8;
      long mask = ~remove & ((Afile << file) | (rank8 << (8*rank)));
      rookAttackMask[i] = mask;
    }
  }

  /////////////////////////////////////////////////////////////////

  // Initialise les coups dans les tables correspondant aux nombres magiques
  // (voir MoveGenerator.java pour l'explication et MagicFinder.java pour l'algorithme)

  private static void generateRookMoveTable() {
    rookMoveTable = new long[64][];

    for (int square = 0; square < 64; square++) {
      long magic = Magic.rookMagics[square];
      int shift = Magic.rookShifts[square];
      int tableSize = 1 << (64 - shift);
      rookMoveTable[square] = new long[tableSize];

      long mask = MoveGenerationData.rookAttackMask[square];
      long blockers = 0;
      do {
        long moves = MoveGenerationData.getSlowRookMoves(square, blockers);
        int index = Magic.index(magic, blockers, shift);
        rookMoveTable[square][index] = moves;
        blockers = (blockers - mask) & mask;
      } while (blockers != 0);
    }
  }

  /////////////////////////////////////////////////////////////////

  // Renvoie le bitboard des coups pour une tour sur une case, étant
  // donné un arrangement de bloqueur
  // Note : ne doit être utilisé que pour initialiser, la méthode des nombres
  // magiques est bien plus rapide

  public static long getSlowRookMoves(int square, long blockers) {
    long moves = 0;
    int[] dirs = {-8, 1, 8, -1}; // Directions possibles de la tour
    long[] edges = {rank8, Hfile, rank1, Afile}; // Biboard opposé à la direction

    for (int i = 0; i < dirs.length; i++) {
      int dir = dirs[i];
      int currentSquare = square;
      long squareBitboard = (1L << currentSquare);

      while ((blockers & squareBitboard) == 0) {
        // Si c'est une case du bord opposé, on arrête
        if ((edges[i] & squareBitboard) != 0) break;
        moves |= squareBitboard;
        currentSquare += dir;
        squareBitboard = (1L << currentSquare);
      }

      // Ajoute le bloqueur aux coups (il peut s'agir d'une pièce alliée)
      moves |= squareBitboard;

      // Retire la case de la tour
      moves &= ~(1L << square);
    }
    return moves;
  }
}
