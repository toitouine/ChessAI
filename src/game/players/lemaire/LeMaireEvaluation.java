public class LeMaireEvaluation implements Evaluation {
  private final float[] material = {0, 900, 500, 330, 320, 100};
  private final float[][] positional = new float[Piece.Number][64];

  public LeMaireEvaluation() {
    initPositional();
  }

  @Override
  public float playerScore(Board board, int color) {
    float posEval = (1 - board.phase) * board.positionalScore(color);
    return board.materialScore(color) + endGameEval(board, color) + posEval;
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

  private float endGameEval(Board board, int color) {
    if (board.materialScore(color) > board.materialScore(1-color) + 150) {
      int kingSquare = board.roi(color);
      int oppSquare = board.roi(1-color);
      float eval = 4.7f * Datas.centerDistance(oppSquare)
                 + 1.6f * (14 - Datas.manhattanDistance(kingSquare, oppSquare));
      return eval * board.phase;
    }

    return -Datas.centerDistance(board.roi(color)) * board.phase;
  }

  private void initPositional() {
    float[] kingPos = {-30, -40, -40, -50, -50, -40, -40, -30,
                       -30, -40, -40, -50, -50, -40, -40, -30,
                       -30, -40, -40, -50, -50, -40, -40, -30,
                       -30, -40, -40, -50, -50, -40, -40, -30,
                       -20, -30, -30, -40, -40, -30, -30, -20,
                       -10, -20, -20, -20, -20, -20, -20, -10,
                        20,  20,   0,   0,   0,   0,  20,  20,
                        20,  35, -10, -10, -10, -10,  35,  20};
    positional[Piece.Roi] = kingPos;

    float[] queenPos = {-20, -10, -10, -5, -5, -10, -10, -20,
                        -10,   0,   0,  0,  0,   0,   0, -10,
                        -10,   0,   5,  5,  5,   5,   0, -10,
                         -5,   0,   5,  5,  5,   5,   0,  -5,
                          0,   0,   5,  5,  5,   5,   0,   0,
                        -10,   5,   5,  5,  5,   5,   5, -10,
                        -10,   0,   5,  0,  0,   5,   0, -10,
                        -20, -10, -10, -5, -5, -10, -10, -20};
    positional[Piece.Dame] = queenPos;

    float[] bishopPos = {-20, -10, -10, -10, -10, -10, -10, -20,
                         -10,   0,   0,   0,   0,   0,   0, -10,
                         -10,   0,   5,  10,  10,   5,   0, -10,
                         -10,   5,   5,  10,  10,   5,   5, -10,
                         -10,   0,  10,  10,  10,  10,   0, -10,
                         -10,  10,  10,  10,  10,  10,  10, -10,
                         -10,   5,   0,   0,   0,   0,   5, -10,
                         -20, -10, -10, -10, -10, -10, -10, -20};
    positional[Piece.Fou] = bishopPos;

    float[] knightPos = {-50, -40, -30, -30, -30, -30, -40, -50,
                         -40, -20,   0,   0,   0,   0, -20, -40,
                         -30,   0,  10,  15,  15,  10,   0, -30,
                         -30,   5,  15,  20,  20,  15,   5, -30,
                         -30,   0,  15,  20,  20,  15,   0, -30,
                         -30,   5,  10,  15,  15,  10,   5, -30,
                         -40, -20,   0,   0,   0,   0, -20, -40,
                         -50, -40, -30, -30, -30, -30, -40, -50};
    positional[Piece.Cavalier] = knightPos;

    float[] rookPos = { 0,  0,  0,  0,  0,  0,  0,  0,
                        5, 10, 10, 10, 10, 10, 10,  5,
                       -5,  0,  0,  0,  0,  0,  0, -5,
                       -5,  0,  0,  0,  0,  0,  0, -5,
                       -5,  0,  0,  0,  0,  0,  0, -5,
                       -5,  0,  0,  0,  0,  0,  0, -5,
                       -5,  0,  0,  0,  0,  0,  0, -5,
                        0,  0,  0,  5,  5,  0,  0, 0};
    positional[Piece.Tour] = rookPos;

    float[] pawnPos = {100, 100, 100, 100, 100, 100, 100, 100,
                        50,  50,  50,  50,  50,  50,  50,  50,
                        10,  10,  20,  30,  30,  20,  10,  10,
                         5,   5,  10,  25,  25,  10,   5,   5,
                         0,   0,   0,  20,  20,   0,   0,   0,
                         5,  -5, -10,   0,   0, -10,  -5,   5,
                         5,  10,  10, -20, -20,  10,  10,   5,
                         0,   0,   0,   0,   0,   0,   0,   0};
    positional[Piece.Pion] = pawnPos;
  }
}
