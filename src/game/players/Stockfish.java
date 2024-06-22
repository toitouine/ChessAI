import java.util.ArrayList;

public class Stockfish extends Player {

  public Stockfish(SearchSettings settings) {
    name = "Stockfish";
    pseudo = "Stockfish";
    elo = "284";
    title = "Noob";
    victoryTitle = "??!?";
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
