import java.util.ArrayList;

public class LeMaire extends Player {

  public LeMaire(SearchSettings settings) {
    name = "LeMaire";
    pseudo = "LeMaire";
    elo = "3845";
    title = "GM";
    victoryTitle = "Cmaire";
    ouvertureNumber = 10;
    isBot = true;
    this.settings = settings;
  }

  @Override
  public Move play(Board board) {
    ArrayList<Move> moves = board.getLegalMoves();
    int index = (int)(Math.random() * moves.size());
    return moves.get(index);
  }
}
