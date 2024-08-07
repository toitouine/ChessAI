import java.util.ArrayList;

public class Stockfish extends Player {

  public Stockfish(SearchSettings settings) {
    super(settings);
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
