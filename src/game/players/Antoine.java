import java.util.ArrayList;

public class Antoine extends Player {

  public Antoine(SearchSettings settings) {
    name = "Antoine";
    pseudo = "Antoine";
    elo = "100";
    title = "";
    victoryTitle = "Tu t'es fait mater !";
    ouvertureNumber = 0;
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
