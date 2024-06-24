import java.util.ArrayList;

public class Humain extends Player {

  public Humain(SearchSettings settings) {
    name = "Humain";
    pseudo = "Humain";
    elo = "???";
    title = "";
    victoryTitle = "";
    ouvertureNumber = 0;
    isBot = false;
    this.settings = settings;
  }

  @Override
  public Move play(Board board) {
    try {
      Thread.sleep(1);
    } catch (Exception e) {
    }

    ArrayList<Move> moves = board.getLegalMoves();
    int index = (int)(Math.random() * moves.size());
    return moves.get(index);
  }

}
