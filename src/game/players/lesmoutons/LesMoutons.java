import java.util.ArrayList;

public class LesMoutons extends IA {
  private int eloValue = 0;

  public LesMoutons(SearchSettings settings) {
    super(settings);
  }

  @Override
  public String pseudo() {
    return "Mouton";
  }

  @Override
  public String elo() {
    if (eloValue == 0) eloValue = (int)(1300 + Math.random() * 200);
    return String.valueOf(eloValue);
  }

  @Override
  public String victoryTitle() {
    return "YOU LOUSE";
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
