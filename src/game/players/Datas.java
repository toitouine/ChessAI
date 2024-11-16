/////////////////////////////////////////////////////////////////

// Datas
// Contient différentes données précalculées pour les IAs, le
// plateau, etc... On appellera plus loin "déplacement orthogonal"
// un déplacement d'une case pour une pièce comme une tour (sans
// les diagonales). Un déplacement quelconque est un déplacement
// d'une case pour un roi (déplacement orthogonal ou diagonal).

// Les données disponibles sont :
// - centerDistance : Distance d'une case à l'une des quatre cases
//                    du centre (avec déplacement diagonal autorisé)
// - manhattanDistance : Distance "manhattan" entre deux cases, soit
//                       la distance en déplacements orthogonaux entre
//                       deux cases

/////////////////////////////////////////////////////////////////

public final class Datas {

  private static int[] centerDistance = {3, 3, 3, 3, 3, 3, 3, 3,
                                         3, 2, 2, 2, 2, 2, 2, 3,
                                         3, 2, 1, 1, 1, 1, 2, 3,
                                         3, 2, 1, 0, 0, 1, 2, 3,
                                         3, 2, 1, 0, 0, 1, 2, 3,
                                         3, 2, 1, 1, 1, 1, 2, 3,
                                         3, 2, 2, 2, 2, 2, 2, 3,
                                         3, 3, 3, 3, 3, 3, 3, 3};

  private static int[][] manhattanDistance;

  private Datas() {
  }

  /////////////////////////////////////////////////////////////////

  public static int centerDistance(int square) {
    return centerDistance[square];
  }

  public static int manhattanDistance(int s1, int s2) {
    return manhattanDistance[s1][s2];
  }

  /////////////////////////////////////////////////////////////////

  static {
    initManhattanDistance();
  }

  private static void initManhattanDistance() {
    manhattanDistance = new int[64][64];

    for (int s1 = 0; s1 < 64; s1++) {
      for (int s2 = 0; s2 < 64; s2++) {
        int f1 = s1 & 7;
        int f2 = s2 & 7;
        int r1 = s1 >>> 3;
        int r2 = s2 >>> 3;
        manhattanDistance[s1][s2] = Math.abs(r2 - r1) + Math.abs(f2 - f1);
      }
    }
  }
}
