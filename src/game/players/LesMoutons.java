import java.util.ArrayList;

public class LesMoutons extends Player {
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
  public boolean isBot() {
    return true;
  }

  @Override
  public Move play(Board board) {
    ArrayList<Move> moves = board.getLegalMoves();
    int index = (int)(Math.random() * moves.size());
    return moves.get(index);
  }

  @Override
  public void cancelSearch() {
  }
}
