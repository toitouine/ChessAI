/////////////////////////////////////////////////////////////////

// MoveGenerationData
// Contient des données pré-calculées utiles pour générer
// les coups des pièces (voir MoveGenerator.java), comme par
// exemple les masks, les attaques...

/////////////////////////////////////////////////////////////////

public final class MGData {
  private MGData() {}

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
  static private long pawnAttacks[][];
  static private long knightAttacks[];

  // Masks pour la génération des coups des sliders (tour, fou, dame)
  static private long rookAttackMask[];
  static private long bishopAttackMask[];

  // Table des coups par case pour la génération des coups des sliders
  // (voir MoveGenerator.java)
  static private long rookMoveTable[][];
  static private long bishopMoveTable[][];

  // Bitboard des cases situées strictement entre deux cases, de manière
  // orthogonale ou diagonale (déplacement d'une tour ou d'un fou).
  // Contient 0 si les cases ne sont pas diagonales ou orthogonales.
  static private long inBetween[][];

  // Bitboard de la demi droite d'origine [origin] et dans la direction d'une
  // case [target]. La demi droite traverse la case cible, et s'arrête au bord
  // du plateau. Contient 0 si les cases ne sont pas diagonales ou orthogonales.
  // Note : la case d'origine n'est pas comprise dans le bitboard.
  static private long ray[][];

  // Cases des pièces entre le roi et la tour pour tester la possibilité des roques
  static final public long[] petitRoquePiecesMask = {0b1100000L << 56, 0b1100000L};
  static final public long[] grandRoquePiecesMask = {0b1110L << 56, 0b1110L};

  // Intialise toutes les données
  static {
    generateKingAttacks();
    generatePawnAttacks();
    generateKnightAttacks();
    generateRookMasks();
    generateBishopMasks();
    generateRookMoveTable();
    generateBishopMoveTable();
    generateInBetween();
    generateRayTable();
  }

  /////////////////////////////////////////////////////////////////

  // Méthodes pour récupérer les données

  public static long kingAttacks(int square) {
    return kingAttacks[square];
  }

  public static long pawnAttacks(int color, int square) {
    return pawnAttacks[color][square];
  }

  public static long knightAttacks(int square) {
    return knightAttacks[square];
  }

  public static long rookMask(int square) {
    return rookAttackMask[square];
  }

  public static long bishopMask(int square) {
    return bishopAttackMask[square];
  }

  public static long rookMoves(int square, int index) {
    return rookMoveTable[square][index];
  }

  public static long bishopMoves(int square, int index) {
    return bishopMoveTable[square][index];
  }

  public static long inBetween(int s1, int s2) {
    return inBetween[s1][s2];
  }

  public static long ray(int origin, int target) {
    return ray[origin][target];
  }

  /////////////////////////////////////////////////////////////////

  // Les méthodes suivantes précalculent les différentes données
  // utiles pour la génération des coups et les stockent dans les
  // tableaux

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

  private static void generatePawnAttacks() {
    pawnAttacks = new long[2][64];

    // Pions blancs
    for (int i = 0; i < 64; i++) {
      if (i < 8) {
        pawnAttacks[Player.White][i] = 0;
        continue;
      }
      long attack1 = (1L << (i-7)) & ~Afile;
      long attack2 = (1L << (i-9)) & ~Hfile;
      pawnAttacks[Player.White][i] = attack1 | attack2;
    }

    // Pions noirs
    for (int i = 0; i < 64; i++) {
      if (i > 55) {
        pawnAttacks[Player.Black][i] = 0;
        continue;
      }
      long attack1 = 1L << (i+7) & ~Hfile;
      long attack2 = 1L << (i+9) & ~Afile;
      pawnAttacks[Player.Black][i] = attack1 | attack2;
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

  private static void generateBishopMasks() {
    bishopAttackMask = new long[64];
    int[] dirs = {-9, -7, 7, 9};
    long edges = Afile | Hfile | rank1 | rank8;

    for (int square = 0; square < 64; square++) {
      long mask = 0L;

      for (int i = 0; i < dirs.length; i++) {
        int dir = dirs[i];
        int currentSquare = square + dir;
        long squareBitboard = (1L << currentSquare);
        while ((edges & squareBitboard) == 0) {
          mask |= squareBitboard;
          currentSquare += dir;
          squareBitboard = (1L << currentSquare);
        }
      }

      bishopAttackMask[square] = mask;
    }
  }

  private static void generateInBetween() {
    inBetween = new long[64][64];

    for (int s1 = 0; s1 < 64; s1++) {
      for (int s2 = 0; s2 < 64; s2++) {
        // On passe les cases qui ne sont pas situées en diagonale / orthogonalement
        inBetween[s1][s2] = 0;
        if (s1 == s2) continue;
        long s2bb = 1L << s2;
        long orthoInter = getSlowRookMoves(s1, s2bb) & s2bb;
        long diagoInter = getSlowBishopMoves(s1, s2bb) & s2bb;
        if (orthoInter == 0 && diagoInter == 0) continue;

        int fileDiff = Math.abs((s1 & 7) - (s2 & 7));
        int rankDiff = Math.abs((s1 >>> 3) - (s2 >>> 3));
        int dir = (s2 - s1)/Math.max(fileDiff, rankDiff);
        int sq = s1 + dir;
        while (sq != s2) {
          inBetween[s1][s2] |= 1L << sq;
          sq += dir;
        }
      }
    }
  }

  private static void generateRayTable() {
    ray = new long[64][64];

    for (int s1 = 0; s1 < 64; s1++) {
      long opposingEdges = 0L;
      if ((s1 & 7) != 0) opposingEdges |= Afile;
      if ((s1 & 7) != 7) opposingEdges |= Hfile;
      if ((s1 >>> 3) != 0) opposingEdges |= rank8;
      if ((s1 >>> 3) != 7) opposingEdges |= rank1;

      for (int s2 = 0; s2 < 64; s2++) {
        ray[s1][s2] = 0;
        if (s1 == s2) continue;
        long s2bb = 1L << s2;
        long orthoInter = getSlowRookMoves(s1, s2bb) & s2bb;
        long diagoInter = getSlowBishopMoves(s1, s2bb) & s2bb;
        if (orthoInter == 0 && diagoInter == 0) continue;

        int fileDiff = Math.abs((s1 & 7) - (s2 & 7));
        int rankDiff = Math.abs((s1 >>> 3) - (s2 >>> 3));
        int dir = (s2 - s1)/Math.max(fileDiff, rankDiff);
        int sq = s1 + dir;

        while (((1L << sq) & opposingEdges) == 0) {
          ray[s1][s2] |= 1L << sq;
          sq += dir;
        }
        ray[s1][s2] |= 1L << sq;
      }
    }
  }

  /////////////////////////////////////////////////////////////////

  // Initialise les coups dans les tables correspondant aux nombres magiques
  // (voir MoveGenerator.java pour l'explication)

  private static void generateRookMoveTable() {
    rookMoveTable = new long[64][];

    for (int square = 0; square < 64; square++) {
      long magic = Magic.rookMagics[square];
      int shift = Magic.rookShifts[square];
      int tableSize = 1 << (64 - shift);
      rookMoveTable[square] = new long[tableSize];

      long mask = rookAttackMask[square];
      long blockers = 0;
      do {
        long moves = getSlowRookMoves(square, blockers);
        int index = Magic.index(magic, blockers, shift);
        rookMoveTable[square][index] = moves;
        blockers = (blockers - mask) & mask;
      } while (blockers != 0);
    }
  }

  private static void generateBishopMoveTable() {
    bishopMoveTable = new long[64][];

    for (int square = 0; square < 64; square++) {
      long magic = Magic.bishopMagics[square];
      int shift = Magic.bishopShifts[square];
      int tableSize = 1 << (64 - shift);
      bishopMoveTable[square] = new long[tableSize];

      long mask = bishopAttackMask[square];
      long blockers = 0;
      do {
        long moves = getSlowBishopMoves(square, blockers);
        int index = Magic.index(magic, blockers, shift);
        bishopMoveTable[square][index] = moves;
        blockers = (blockers - mask) & mask;
      } while (blockers != 0);
    }
  }

  /////////////////////////////////////////////////////////////////

  // Renvoie le bitboard des coups pour une tour ou un fou sur une
  // case, étant donné un arrangement de bloqueurs
  // Note : ne doit être utilisé que pour initialiser, la méthode
  // des nombres magiques est plus rapide

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

  public static long getSlowBishopMoves(int square, long blockers) {
    long moves = 0L;
    int[] dirs = {-9, -7, 7, 9};
    long edges = Afile | Hfile | rank1 | rank8;

    for (int i = 0; i < dirs.length; i++) {
      int dir = dirs[i];

      // Si c'est une case du bord, on vérifie que la direction soit bonne
      if ((1L << square & edges) != 0) {
        if ((Afile & (1L << square)) != 0 && (dir == -9 || dir == 7))  continue;
        if ((Hfile & (1L << square)) != 0 && (dir == 9  || dir == -7)) continue;
        if ((rank8 & (1L << square)) != 0 && (dir == -9 || dir == -7)) continue;
        if ((rank1 & (1L << square)) != 0 && (dir == 9  || dir == 7))  continue;
      }

      int currentSquare = square + dir;
      long squareBitboard = (1L << currentSquare);
      while ((edges & squareBitboard) == 0 && (squareBitboard & blockers) == 0) {
        moves |= squareBitboard;
        currentSquare += dir;
        squareBitboard = (1L << currentSquare);
      }
      moves |= squareBitboard;
    }

    return moves;
  }
}
