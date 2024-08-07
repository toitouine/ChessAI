import java.util.ArrayList;

public class LeMaire extends Player {

  public LeMaire(SearchSettings settings) {
    super(settings);
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
