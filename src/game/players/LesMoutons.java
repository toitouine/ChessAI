import java.util.ArrayList;

public class LesMoutons extends Player {

  public LesMoutons(SearchSettings settings) {
    name = "LesMoutons";
    pseudo = "Mouton";
    elo = String.valueOf((int)(1300 + Math.random() * 200));
    title = "Mouton";
    victoryTitle = "YOU LOUSE";
    ouvertureNumber = 5;
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
