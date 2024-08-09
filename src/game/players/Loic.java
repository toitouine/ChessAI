import java.util.ArrayList;

public class Loic extends IA {

  public Loic(SearchSettings settings) {
    super(settings);
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
    ArrayList<Move> moves = board.getLegalMoves();
    int index = (int)(Math.random() * moves.size());

    try {
      Thread.sleep((int) (1000*(Math.pow(1.12, depth) - 1)));
    } catch (Exception e) {
    }

    return new SearchResult(moves.get(index));
  }

  @Override
  public void stopSearch() {
  }
}
