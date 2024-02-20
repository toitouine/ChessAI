/////////////////////////////////////////////////////////////////

// Classe qui s'occupe de générer les coups pour un plateau donné

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;

public class MoveGenerator {
  private Board board; // Plateau sur lequel générer les coups

  public MoveGenerator(Board board) {
    this.board = board;
  }

  /////////////////////////////////////////////////////////////////

  public ArrayList<Move> getLegalMoves() {
    return getLegalMoves(board.tourDeQui);
  }

  public ArrayList<Move> getLegalMoves(int color) {
    ArrayList<Move> moves = new ArrayList<Move>();

    addKnightMoves(moves, color);

    return moves;
  }

  /////////////////////////////////////////////////////////////////

  private long getKnightAttack(int square) {
    return MoveGenerationData.knightAttacks[square];
  }

  /////////////////////////////////////////////////////////////////

  private void addKnightMoves(ArrayList<Move> moves, int color) {
    long cavaliers = board.pieceBitboard[Piece.Cavalier + Piece.NumberOfType*color];

    while (cavaliers != 0) {
      // Récupère la case de départ du cavalier et ses attaques
      int startSquare = Long.numberOfTrailingZeros(cavaliers);
      long attacks = getKnightAttack(startSquare);

      // Convertit le bitboard des attaques en coups
      long endSquares = attacks & ~board.colorBitboard[color];
      while (endSquares != 0) {
        moves.add(new Move(startSquare, Long.numberOfTrailingZeros(endSquares)));
        endSquares &= endSquares - 1;
      }
      cavaliers &= cavaliers - 1;
    }
  }

}
