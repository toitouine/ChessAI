public class StockfishEvaluation extends LeMaireEvaluation {
  public StockfishEvaluation() {
  }

  @Override
  public float playerScore(Board board, int color) {
    return board.materialScore(color) + board.positionalScore(color);
  }
}
