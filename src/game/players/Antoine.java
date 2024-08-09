import java.util.ArrayList;

public class Antoine extends IA {

  public Antoine(SearchSettings settings) {
    super(settings);
  }

  @Override
  public String elo() {
    return "100";
  }

  @Override
  public String victoryTitle() {
    return "Tu t'es fait mater !";
  }

  @Override
  public SearchResult play(Board board) {
    return search(board, 0);
  }

  @Override
  public SearchResult search(Board board, int depth) {
    ArrayList<Move> moves = board.getLegalMoves();
    int index = (int)(Math.random() * moves.size());
    Move move = moves.get(index);
    float eval = (float)(Math.random()-0.5f)*10f;
    return new SearchResult(move, eval, (int)(Math.random()*6));
  }

  @Override
  public void stopSearch() {
  }
}
