import java.util.ArrayList;

public class Stockfish extends IA {

  private boolean stopSearch = false;
  private Move bestMoveFound = null;
  private final Evaluation evaluation;

  public Stockfish(SearchSettings settings) {
    super(settings);
    evaluation = new StockfishEvaluation();
  }

  @Override
  public String elo() {
    return "284";
  }

  @Override
  public String victoryTitle() {
    return "??!?";
  }

  @Override
  public SearchResult search(Board board, int depth) {
    int color = board.tourDeQui;
    bestMoveFound = null;
    stopSearch = false;
    board.setEvaluation(evaluation);
    float eval = minimax(board, depth, 0, -SearchResult.Infinity, SearchResult.Infinity);
    eval *= (color == Player.White ? 1 : -1);
    return new SearchResult(bestMoveFound, eval, depth);
  }

  @Override
  public void stopSearch() {
    stopSearch = true;
  }

  private float minimax(Board board, int depth, int plyFromRoot, float alpha, float beta) {
    if (stopSearch) return 0;

    if (depth == 0) {
      return evaluation.evaluate(board) * (board.tourDeQui == Player.White ? 1 : -1);
    }

    ArrayList<Move> moves = board.getLegalMoves();

    if (moves.size() == 0) {
      if (board.inCheck()) {
        float mateScore = SearchResult.MateValue - plyFromRoot;
        return -mateScore;
      } else {
        return 0;
      }
    }

    if (board.isRepeated(board.zobrist)) {
     return 0;
    }

    float worstEval = SearchResult.Infinity;

    for (Move move : moves) {
      board.make(move);
      float evaluation = -minimax(board, depth-1, plyFromRoot+1, -beta, -alpha);
      board.unmake(move);

      if (evaluation >= beta) {
        return beta;
      }

      if (plyFromRoot == 0) {
        if (evaluation < worstEval) {
          worstEval = evaluation;
          bestMoveFound = move;
        }
      } else {
        alpha = Math.max(alpha, evaluation);
      }
    }

    if (plyFromRoot == 0) return worstEval;
    return alpha;
  }
}
