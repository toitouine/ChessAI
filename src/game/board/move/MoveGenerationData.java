import java.util.HashMap;

public final class MoveGenerationData {
  private MoveGenerationData() {}

  static final public long Afile = Bitboard.Afile;
  static final public long Bfile = Bitboard.Bfile;
  static final public long Gfile = Bitboard.Gfile;
  static final public long Hfile = Bitboard.Hfile;
  static final public long rank1 = Bitboard.rank1;
  static final public long rank2 = Bitboard.rank2;
  static final public long rank7 = Bitboard.rank7;
  static final public long rank8 = Bitboard.rank8;

  // Attaques de différentes pièces depuis une case sous forme de bitboard
  static public long kingAttacks[];
  static public long knightAttacks[];

  // Masks pour la génération des coups des sliders (tour, fou, dame)
  static public long rookAttackMask[];

  // Table des coups possibles pour une tour sur une case selon les arrangements de bloqueurs
  static public HashMap<Long, Long>[] rookMoves;

  // Cases des pièces entre le roi et la tour pour tester la possibilité des roques
  static final public long[] petitRoquePiecesMask = {0b1100000L << 56, 0b1100000L};
  static final public long[] grandRoquePiecesMask = {0b1110L << 56, 0b1110L};

  public static void initialize() {
    generateKingAttacks();
    generateKnightAttacks();
    generateRookMasks();
    generateRookMoveMap();
  }

  /////////////////////////////////////////////////////////////////

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
      int rank = i >> 3; // Ligne de la case
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

  // Pour chaque case, regarde tous les arrangements de bloqueurs possibles
  // et génère les coups en conséquence. Un bitboard des bloqueurs sert alors
  // de clés pour accéder aux coups.

  @SuppressWarnings("unchecked")
  private static void generateRookMoveMap() {
    rookMoves = new HashMap[64];

    for (int square = 0; square < 64; square++) {
      rookMoves[square] = new HashMap<Long, Long>();
      long mask = rookAttackMask[square];
      long blockers = 0;
      do { // Énumère tous les arrangements de bloqueurs dans le mask (voir Carry-Rippler trick)
        long moves = getRookMoves(square, blockers);
        rookMoves[square].put(blockers, moves);
        blockers = (blockers - mask) & mask;
      } while (blockers != 0);
    }
  }

  // Calcule les coups d'une tour sur une case et connaissant les blockers
  // Utilisé pour pré-calculer les coups de la tour selon les bloqueurs

  private static long getRookMoves(int square, long blockers) {
    long moves = 0;
    // Directions possibles de la tour
    int[] dirs = {-8, 1, 8, -1};
    // Biboard du côté de la direction dans dirs (pour l'arret)
    long[] edges = {rank8, Hfile, rank1, Afile};

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
