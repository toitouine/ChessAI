/////////////////////////////////////////////////////////////////

// LeMaire

// - Recherche avec Négamax alpha-bêta et table de transposition
// - Recherche supplémentaire des captures
// - Tri des coups

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;

public class LeMaire extends IA {
  private final TranspositionTable tt;
  private final Evaluation evaluator;
  private final float Infinity = SearchResult.Infinity;
  private Move bestMoveFound = null;
  private boolean stopSearch = false;
  private final int maxQuietDepth = 30;

  public LeMaire(SearchSettings settings) {
    super(settings);
    evaluator = new LeMaireEvaluation();
    tt = new TranspositionTable(24);
  }

  @Override
  public String elo() {
    return "3845";
  }

  @Override
  public String victoryTitle() {
    return "Cmaire";
  }

  @Override
  protected void endOfMove() {
    tt.clear();
  }

  @Override
  public SearchResult search(Board board, int depth) {
    int color = board.tourDeQui;
    bestMoveFound = null;
    stopSearch = false;
    board.setEvaluation(evaluator);
    float eval = minimax(board, depth, 0, -Infinity, Infinity);
    eval *= (color == Player.White ? 1 : -1);
    return new SearchResult(bestMoveFound, eval, depth);
  }

  @Override
  public void stopSearch() {
    stopSearch = true;
  }

  private float evalRelative(Board board) {
    return evaluator.evaluate(board) * (board.tourDeQui == Player.White ? 1 : -1);
  }

  private void orderMoves(Board board, ArrayList<Move> moves) {
    float[] scores = new float[moves.size()];
    Move hashMove = tt.getBestMove(board.zobrist);
    float defaultEval = -evalRelative(board);

    for (int i = 0; i < moves.size(); i++) {
      Move move = moves.get(i);

      // Place le meilleur coup de la table de transposition en premier
      if (move.equals(hashMove)) {
        scores[i] = SearchResult.Infinity;
        continue;
      }

      // Capture
      Piece piece = board.grid(move.startSquare());
      if (piece.type != Piece.Roi && board.grid(move.endSquare()) != null) {
        Piece capture = board.grid(move.endSquare());
        scores[i] = defaultEval + evaluator.materialValue(capture) - evaluator.materialValue(piece);
        continue;
      }

      // TODO: À tester
      board.make(move);
      scores[i] = -evalRelative(board);
      board.unmake(move);
    }

    sortMoves(moves, scores);
  }

  private float minimax(Board board, int depth, int plyFromRoot, float alpha, float beta) {
    // Si la recherche a été interrompue, on sort le plus vite possible du minimax
    if (stopSearch) return 0;

    // Regarde la position dans la table de transposition et récupère la valeur (ou pas)
    Entry entry = tt.probe(board.zobrist, plyFromRoot);
    if (entry != null && entry.depth >= depth) {
      // La valeur stockée est exacte
      if (entry.nodeType == Entry.Exact) {
        if (plyFromRoot == 0) bestMoveFound = entry.bestMove;
        return entry.value;
      }

      // La valeur stockée est lowerbound donc non complète, on ajuste alpha
      else if (entry.nodeType == Entry.Lowerbound) alpha = Math.max(alpha, entry.value);

      // La valeur stockée est upperbound donc non complète, on ajuste bêta
      else if (entry.nodeType == Entry.Upperbound) beta = Math.min(beta, entry.value);

      if (alpha >= beta) {
        if (plyFromRoot == 0) bestMoveFound = entry.bestMove;
        return entry.value; // Si la valeur de la table a provoqué un élagage alpha ou bêta
      }
    }

    // Détection des répétitions
    if (board.isRepeated(board.zobrist)) {
     return 0;
    }

    // Appelle la recherche de captures si on est arrivé à la profondeur demandée
    if (depth == 0) {
      return searchCaptures(board, alpha, beta, plyFromRoot);
    }

    // Génération et classement des coups
    ArrayList<Move> moves = board.getLegalMoves();
    orderMoves(board, moves);

    // Détection des mats et pats
    if (moves.size() == 0) {
      return board.inCheck() ? plyFromRoot - SearchResult.MateValue : 0;
    }

    Move bestMoveInPosition = null;
    byte nodeType = Entry.Upperbound;

    // Algorithme Négamax
    for (Move move : moves) {
      board.make(move);
      float evaluation = -minimax(board, depth-1, plyFromRoot+1, -beta, -alpha);
      board.unmake(move);

      // Élagage alpha-beta
      if (evaluation >= beta) {
        tt.store(board.zobrist, beta, move, depth, plyFromRoot, Entry.Lowerbound);
        return beta;
      }

      // Nouveau meilleur coup
      if (evaluation > alpha) {
        nodeType = Entry.Exact;
        alpha = evaluation;
        bestMoveInPosition = move;
        if (plyFromRoot == 0) bestMoveFound = move;
      }
    }

    tt.store(board.zobrist, alpha, bestMoveInPosition, depth, plyFromRoot, nodeType);

    return alpha;
  }

  private float searchCaptures(Board board, float alpha, float beta, int plyFromRoot) {
    if (stopSearch) return 0;

    float evaluation = evalRelative(board);
    if (plyFromRoot >= maxQuietDepth) return evaluation;
    if (evaluation >= beta) return beta;
    if (evaluation > alpha) alpha = evaluation;

    ArrayList<Move> moves = board.getLegalCaptures();
    orderMoves(board, moves);

    for (Move move : moves) {
      board.make(move);
      evaluation = -searchCaptures(board, -beta, -alpha, plyFromRoot+1);
      board.unmake(move);

      if (evaluation >= beta) return beta;
      alpha = Math.max(alpha, evaluation);
    }

    return alpha;
  }
}
