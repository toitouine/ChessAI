/////////////////////////////////////////////////////////////////

// Évaluation
// Indique à quel point une position est favorable à un des deux
// joueurs avec la méthode evaluate(). Cette évaluation est positive
// si les blancs ont l'avantage et négative si les noirs ont l'avantage.
// Elle est comptée en centipions.
// Pour créer une évaluation, il faut implémenter playerScore() qui
// indique l'évaluation pour un joueur (plus elle est élevée, plus le
// joueur a une bonne position), et la fonction evaluate() effectue la
// soustraction.
// Les méthodes materialValue() et positionalValue() servent à indiquer
// les valeurs matérielles et positionnelles des pièces. Ces méthodes sont
// notamment utilisées lorsqu'une évaluation est attachée à un plateau
// (voir Board.setEvaluation())

/////////////////////////////////////////////////////////////////

import java.io.Serializable;

interface Evaluation extends Serializable {
  public float materialValue(int type);
  public float positionalValue(int type, int color, int square);
  public float playerScore(Board board, int color);

  default float evaluate(Board board) {
    return playerScore(board, Player.White) - playerScore(board, Player.Black);
  }

  default float materialValue(Piece piece) {
    return materialValue(piece.type);
  }

  default float positionalValue(Piece piece, int square) {
    return positionalValue(piece.type, piece.color, square);
  }
}

class DefaultEvaluation implements Evaluation {
  public DefaultEvaluation() {
  }

  @Override
  public float materialValue(int type) {
    return 0;
  }

  @Override
  public float positionalValue(int type, int color, int square) {
    return 0;
  }

  @Override
  public float playerScore(Board board, int color) {
    return 0;
  }
}
