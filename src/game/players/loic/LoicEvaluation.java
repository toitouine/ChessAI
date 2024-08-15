public class LoicEvaluation implements Evaluation {
  public LoicEvaluation() {
    initPositional();
  }

  @Override
  public float materialValue(int type) {
    return material[type];
  }

  @Override
  public float positionalValue(int type, int color, int square) {
    if (color == Player.White) return positional[type][square];
    else return positional[type][63-square];
  }

  @Override
  public float playerScore(Board board, int color) {
    return board.materialScore(color) + 2*board.positionalScore(color);
  }

  private final float[] material = {0, 900, 150, 300, 300, 100};
  private final float[][] positional = new float[Piece.Number][64];

  private void initPositional() {
    float[] kingPos = {-30, -40, -40, -50, -50, -40, -40, -30,
                       -30, -40, -40, -50, -50, -40, -40, -30,
                       -30, -40, -40, -50, -50, -40, -40, -30,
                       -30, -40, -40, -50, -50, -40, -40, -30,
                       -20, -30, -30, -40, -40, -30, -30, -20,
                       -10, -20, -20, -20, -20, -20, -20, -10,
                         5,   5,   0,   0,   0,   0,   5,  5,
                         0,   0,   0,  20,  20,   0,   0,  0};
    positional[Piece.Roi] = kingPos;

    float[] queenPos = {-20, -10, -10, -5, -5,  -10, -10, -20,
                        -10,   0,   0,  0,  0,   0,    0, -10,
                        -10,   0,   5,  5,  5,   5,    0, -10,
                         -5,   0,   5,  5,  5,   5,    0,  -5,
                          0,   0,   5,  5,  5,   5,    0,   0,
                        -10,   5,   5,  5,  5,   5,    5, -10,
                        -10,   0,  -5,  0,  0,  -5,    0, -10,
                        -20, -10, -10, 20, 20, -10,  -10, -20};
    positional[Piece.Dame] = queenPos;

    float[] bishopPos = {-20, -10, -10, -10, -10, -10, -10, -20,
                         -10,   0,   0,   0,   0,   0,   0, -10,
                         -10,   0, -15, -10, -10, -15,   0, -10,
                         -10, -15, -15, -10, -10, -15, -15, -10,
                         -10,   0, -10, -10, -10, -10,   0, -10,
                         -10, -10, -10, -10, -10, -10, -10, -10,
                         -10,  40, -20,   0,   0, -20,  40, -10,
                         -20,   0, -10, -10, -10, -10,   0, -20};
    positional[Piece.Fou] = bishopPos;

    float[] knightPos = {-50, -40, -30, -30, -30, -30, -40, -50,
                         -40, -20,   0,   0,   0,   0, -20, -40,
                         -30,   0,   0,   0,   0,   0,   0, -30,
                         -30,   0,   0,   0,   0,   0,   0, -30,
                         -30,   0,   0,   0,   0,   0,   0, -30,
                         -30,   5, -10,   0,   0, -10,   5, -30,
                         -40, -20,   0,  30,  30,   0, -20, -40,
                         -50, -40, -30, -30, -30, -30, -40, -50};
    positional[Piece.Cavalier] = knightPos;

    float[] rookPos = { 0, 0, 0, 0, 0, 0, 0,  0,
                        5, 0, 0, 0, 0, 0, 0,  5,
                       -5, 0, 0, 0, 0, 0, 0, -5,
                       -5, 0, 0, 0, 0, 0, 0, -5,
                       -5, 0, 0, 0, 0, 0, 0, -5,
                       -5, 0, 0, 0, 0, 0, 0, -5,
                       -5, 0, 0, 0, 0, 0, 0, -5,
                       10, 0, 0, 0, 0, 0, 0, 10};
    positional[Piece.Tour] = rookPos;

    float[] pawnPos = {100, 100, 100, 100, 100, 100, 100, 100,
                        50,  50,  50,  50,  50,  50,  50,  50,
                        10,  10,  20,  25,  25,  20,  10,  10,
                         5,   5,   5,   5,   5,   5,   5,   5,
                         0,   0,   0,   0,   0,   0,   0,   0,
                        20,  20, -10,  35,  35, -10,  20,  20,
                         5,  10,  20, -20, -20,  20,  10,   5,
                         0,   0,   0,   0,   0,   0,   0,   0};
    positional[Piece.Pion] = pawnPos;
  }
}
