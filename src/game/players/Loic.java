import java.util.ArrayList;

public class Loic extends Player {

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
