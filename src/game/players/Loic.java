import java.util.ArrayList;

public class Loic extends Player {

  public Loic(SearchSettings settings) {
    name = "Loic";
    pseudo = "Loic";
    elo = "-142";
    title = "IM";
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
