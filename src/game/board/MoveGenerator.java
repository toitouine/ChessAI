/////////////////////////////////////////////////////////////////

// Classe qui s'occupe de générer les coups pour un plateau donné

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;

public class MoveGenerator {
  private Board board; // Plateau sur lequel générer les coups

  public MoveGenerator(Board board) {
    this.board = board;
  }

  public ArrayList<Move> getLegalMoves() {
    ArrayList<Move> moves = new ArrayList<Move>();
    return moves;
  }
}
