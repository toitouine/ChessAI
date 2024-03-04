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

  public ArrayList<Move> getLegalMoves(int color) {
    ArrayList<Move> moves = new ArrayList<Move>(45);

    addKnightMoves(moves, color);
    if (color == Player.White) addWhitePawnMoves(moves);
    else addBlackPawnMoves(moves);

    return moves;
  }

  /////////////////////////////////////////////////////////////////

  // Coups des cavaliers

  private long getKnightAttack(int square) {
    return MoveGenerationData.knightAttacks[square];
  }

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

  /////////////////////////////////////////////////////////////////

  // Coups des pions

  private void addWhitePawnMoves(ArrayList<Move> moves) {
    long pions = board.pieceBitboard[Piece.Pion];
    long empty = ~board.colorBitboard[0] & ~board.colorBitboard[1];

    // Avance simple des pions (et promotion)
    long onepush = (pions >> 8) & empty;
    while (onepush != 0) {
      int endSquare = Long.numberOfTrailingZeros(onepush);
      if (endSquare <= 7) addPromotionMoves(moves, endSquare+8, endSquare);
      else moves.add(new Move(endSquare + 8, endSquare));
      onepush &= onepush - 1;
    }

    // Avance double des pions
    long doublepush = (((pions & Bitboard.rank2) >> 8) & empty) >> 8 & empty;
    while (doublepush != 0) {
      int endSquare = Long.numberOfTrailingZeros(doublepush);
      moves.add(new Move(endSquare + 16, endSquare, MoveFlag.DoubleAvance));
      doublepush &= doublepush - 1;
    }

    // Capture gauche (et promotion)
    long captureLeft = ((~Bitboard.Afile & pions) >> 9) & board.colorBitboard[Player.Black];
    while (captureLeft != 0) {
      int endSquare = Long.numberOfTrailingZeros(captureLeft);
      if (endSquare <= 7) addPromotionMoves(moves, endSquare + 9, endSquare);
      else moves.add(new Move(endSquare + 9, endSquare));
      captureLeft &= captureLeft - 1;
    }

    // Capture droite (et promotion)
    long captureRight = ((~Bitboard.Hfile & pions) >> 7) & board.colorBitboard[Player.Black];
    while (captureRight != 0) {
      int endSquare = Long.numberOfTrailingZeros(captureRight);
      if (endSquare <= 7) addPromotionMoves(moves, endSquare + 7, endSquare);
      else moves.add(new Move(endSquare + 7, endSquare));
      captureRight &= captureRight - 1;
    }

    // En passant
    if (board.enPassantSquare[Player.Black] == null) return;

    int caseEnPassant = board.enPassantSquare[Player.Black];
    long mangeurs = pions & ( (1L << (caseEnPassant+7) & ~Bitboard.Hfile)
                            | (1L << (caseEnPassant+9) & ~Bitboard.Afile) );
    while (mangeurs != 0) {
      int startSquare = Long.numberOfTrailingZeros(mangeurs);
      moves.add(new Move(startSquare, caseEnPassant, MoveFlag.EnPassant));
      mangeurs &= mangeurs - 1;
    }
  }

  private void addBlackPawnMoves(ArrayList<Move> moves) {
    long pions = board.pieceBitboard[Piece.Pion + Piece.NumberOfType];
    long empty = ~board.colorBitboard[0] & ~board.colorBitboard[1];

    // Avance simple des pions (et promotion)
    long onepush = (pions << 8) & empty;
    while (onepush != 0) {
      int endSquare = Long.numberOfTrailingZeros(onepush);
      if (endSquare >= 56) addPromotionMoves(moves, endSquare-8, endSquare);
      else moves.add(new Move(endSquare - 8, endSquare));
      onepush &= onepush - 1;
    }

    // Avance double des pions
    long doublepush = (((pions & Bitboard.rank7) << 8) & empty) << 8 & empty;
    while (doublepush != 0) {
      int endSquare = Long.numberOfTrailingZeros(doublepush);
      moves.add(new Move(endSquare - 16, endSquare, MoveFlag.DoubleAvance));
      doublepush &= doublepush - 1;
    }

    // Capture droite (et promotion)
    long captureRight = ((~Bitboard.Hfile & pions) << 9) & board.colorBitboard[Player.White];
    while (captureRight != 0) {
      int endSquare = Long.numberOfTrailingZeros(captureRight);
      if (endSquare >= 56) addPromotionMoves(moves, endSquare - 9, endSquare);
      else moves.add(new Move(endSquare - 9, endSquare));
      captureRight &= captureRight - 1;
    }

    // Capture gauche (et promotion)
    long captureLeft = ((~Bitboard.Afile & pions) << 7) & board.colorBitboard[Player.White];
    while (captureLeft != 0) {
      int endSquare = Long.numberOfTrailingZeros(captureLeft);
      if (endSquare >= 56) addPromotionMoves(moves, endSquare - 7, endSquare);
      else moves.add(new Move(endSquare - 7, endSquare));
      captureLeft &= captureLeft - 1;
    }

    // En passant
    if (board.enPassantSquare[Player.White] == null) return;

    int caseEnPassant = board.enPassantSquare[Player.White];
    long mangeurs = pions & ( (1L << (caseEnPassant-7) & ~Bitboard.Afile)
                            | (1L << (caseEnPassant-9) & ~Bitboard.Hfile) );
    while (mangeurs != 0) {
      int startSquare = Long.numberOfTrailingZeros(mangeurs);
      moves.add(new Move(startSquare, caseEnPassant, MoveFlag.EnPassant));
      mangeurs &= mangeurs - 1;
    }
  }

  private void addPromotionMoves(ArrayList<Move> moves, int startSquare, int endSquare) {
    moves.add(new Move(startSquare, endSquare, MoveFlag.PromotionDame));
    moves.add(new Move(startSquare, endSquare, MoveFlag.PromotionCavalier));
    moves.add(new Move(startSquare, endSquare, MoveFlag.PromotionTour));
    moves.add(new Move(startSquare, endSquare, MoveFlag.PromotionFou));
  }
}
