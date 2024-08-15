import java.util.ArrayList;

public class Loic extends IA {

  private final Evaluation evaluation;
  private final float infinity = SearchResult.infinity;
  private Move bestMoveFound = null;
  private boolean stopSearch = false;

  public Loic(SearchSettings settings) {
    super(settings);
    evaluation = new LoicEvaluation();
  }

  @Override
  public String elo() {
    return "-142";
  }

  @Override
  public String victoryTitle() {
    return "Tu t'es fait mater !";
  }

  @Override
  public SearchResult search(Board board, int depth) {
    int color = board.tourDeQui;
    bestMoveFound = null;
    stopSearch = false;
    board.setEvaluation(evaluation);
    float eval = minimax(board, depth, 0, -infinity, infinity);
    eval *= (color == Player.White ? 1 : -1);
    return new SearchResult(bestMoveFound, eval, depth);
  }

  private void orderMoves(Board board, ArrayList<Move> moves) {
    float[] scores = new float[moves.size()];
    for (int i = 0; i < moves.size(); i++) {
      Move move = moves.get(i);

      if (board.grid(move.endSquare()) != null) {
        Piece piece = board.grid(move.startSquare());
        if (piece.type == Piece.Roi) continue;

        Piece capture = board.grid(move.endSquare());
        scores[i] = evaluation.materialValue(capture) - evaluation.materialValue(piece);
      }
    }

    sortMoves(moves, scores);
  }

  @Override
  public void stopSearch() {
    stopSearch = true;
  }

  private float minimax(Board board, int depth, int plyFromRoot, float alpha, float beta) {
    // Si la recherche a été interrompue, on sort le plus vite possible du minimax
    if (stopSearch) return 0;

    // En arrivant en bas de l'arbre, renvoie l'évaluation relative de la position
    if (depth == 0) {
      return evaluation.evaluate(board) * (board.tourDeQui == Player.White ? 1 : -1);
    }

    ArrayList<Move> moves = board.getLegalMoves();
    orderMoves(board, moves);

    if (moves.size() == 0) {
      if (board.inCheck()) {
        // Loic aime les mats, mais préfère les pats
        float mateScore = SearchResult.mateValue/2 - plyFromRoot;
        return -mateScore;
      } else {
        // Loic adore les pats, et veut à tout prix pater son adversaire
        float patScore = SearchResult.mateValue - plyFromRoot;
        return -patScore;
      }
    }

    // Répétition
    if (board.isRepeated(board.zobrist)) {
     return 0;
    }

    float bestEval = -infinity;

    // Implémentation du négamax alpha-bêta
    for (Move move : moves) {
      board.make(move);
      float evaluation = -minimax(board, depth-1, plyFromRoot+1, -beta, -alpha);
      board.unmake(move);

      // Élaguage alpha-beta
      if (evaluation >= beta) {
        return beta;
      }

      if (plyFromRoot == 0) {
        // Nouveau meilleur coup
        if (evaluation > bestEval) {
          bestEval = evaluation;
          bestMoveFound = move;
        }
      }

      alpha = Math.max(alpha, evaluation);
    }

    return alpha;
  }
}
