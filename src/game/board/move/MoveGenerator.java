/////////////////////////////////////////////////////////////////

// Génération des coups pour un plateau donné.
// On utilise principalement les bitboards, et on s’appuie sur les données
// initialisées dans MoveGenerationData.java. On distingue deux types de pièces :

// - Roi, Cavalier, Pion :
// Ces pièces ont un mouvement suffisamment "simple" pour pouvoir générer
// les coups rapidement avec des masks des attaques ou des opératons simples.

// - Les sliders (Dame, Tour, Fou) :
// On utilise la technique des "magic bitboards". On obtient assez simplement
// un bitboard des pièces qui bloquent (bloqueurs). On effectue alors des opérations
// sur ce bitboard (faisant intervenir des "nombres magiques") pour obtenir un index
// d'un tableau (où les coups sont pré-calculés selon chaque configuration de
// bloqueurs). Au démarrage du programme, on peut donc générer un tel tableau par
// case (donc 64 tableaux pour la tour, et 64 pour le fou), et calculer en avance
// les coups sur une case donnée pour chaque arrangement de bloqueurs possible
// (voir MoveGenerationData.java).

// Magic bitboard :
// L'opération effectuée est : index = (blockers * magic) >>> (64 - n)
// où blocker correspond au bitboard des bloqueurs, magic au nombre magique de la case
// et n au nombre de bits utiles associés au nombre magique (déterminé en même
// temps que le nombre magique)
// Pour éviter une soustraction, on sauvegarde 64 - n (shift) et non n dans les tableaux.
// La liste des nombres magiques et des shifts (déterminés avec MagicFinder.java)
// se trouve dans Magic.java

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;

public class MoveGenerator {

  private Board board; // Plateau sur lequel générer les coups
  private long occupied = 0; // Bitboard avec toutes les pièces du plateau

  public MoveGenerator(Board board) {
    this.board = board;
  }

  /////////////////////////////////////////////////////////////////

  public ArrayList<Move> getLegalMoves(int color) {
    ArrayList<Move> moves = new ArrayList<Move>(45);
    occupied = board.colorBitboard[Player.White] | board.colorBitboard[Player.Black];

    addKingMoves(moves, color);
    addKnightMoves(moves, color);
    addRookMoves(moves, color);
    addBishopMoves(moves, color);
    addQueenMoves(moves, color);
    if (color == Player.White) addWhitePawnMoves(moves);
    else addBlackPawnMoves(moves);

    return moves;
  }

  /////////////////////////////////////////////////////////////////

  // Coups des dames

  private void addQueenMoves(ArrayList<Move> moves, int color) {
    long dames = board.pieceBitboard[Piece.Dame + Piece.NumberOfType*color];

    while (dames != 0) {
      // Récupère la case de départ de la dame
      int startSquare = Long.numberOfTrailingZeros(dames);

      // Récupère les coups de la tour et du fou
      long attacks = Magic.getRookAttacks(startSquare, occupied);
      attacks |= Magic.getBishopAttacks(startSquare, occupied);

      // Enlève les pièces alliées
      attacks &= ~(attacks & board.colorBitboard[color]);

      while (attacks != 0) {
        moves.add(new Move(startSquare, Long.numberOfTrailingZeros(attacks)));
        attacks &= attacks - 1;
      }

      dames &= dames - 1;
    }
  }

  // Coups des fous

  private void addBishopMoves(ArrayList<Move> moves, int color) {
    long fous = board.pieceBitboard[Piece.Fou + Piece.NumberOfType*color];

    while (fous != 0) {
      // Récupère la case de départ du fou
      int startSquare = Long.numberOfTrailingZeros(fous);

      // Récupère les attaques pré-calculées et les convertit en coups (magic bitboard)
      long attacks = Magic.getBishopAttacks(startSquare, occupied);
      attacks &= ~(attacks & board.colorBitboard[color]);

      while (attacks != 0) {
        moves.add(new Move(startSquare, Long.numberOfTrailingZeros(attacks)));
        attacks &= attacks - 1;
      }

      fous &= fous - 1;
    }
  }

  // Coups des tours

  private void addRookMoves(ArrayList<Move> moves, int color) {
    long tours = board.pieceBitboard[Piece.Tour + Piece.NumberOfType*color];

    while (tours != 0) {
      // Récupère la case de départ de la tour
      int startSquare = Long.numberOfTrailingZeros(tours);

      // Récupère les attaques pré-calculées et les convertit en coups (magic bitboard)
      long attacks = Magic.getRookAttacks(startSquare, occupied);
      attacks &= ~(attacks & board.colorBitboard[color]);

      while (attacks != 0) {
        moves.add(new Move(startSquare, Long.numberOfTrailingZeros(attacks)));
        attacks &= attacks - 1;
      }

      tours &= tours - 1;
    }
  }

  // Coups du roi

  private void addKingMoves(ArrayList<Move> moves, int color) {
    // On suppose qu'il n'y a qu'un seul roi de couleur color en jeu
    int startSquare = board.roi(color);

    // Récupère les attaques du roi
    long attacks = MoveGenerationData.kingAttacks(startSquare);

    // Convertit le bitboard des attaques en coups
    long endSquares = attacks & ~board.colorBitboard[color];
    while (endSquares != 0) {
      moves.add(new Move(startSquare, Long.numberOfTrailingZeros(endSquares)));
      endSquares &= endSquares - 1;
    }

    // Ajoute éventuellement les roques
    if (board.petitRoque(color)) {
      if ((MoveGenerationData.petitRoquePiecesMask[color] & occupied) == 0) {
        moves.add(new Move(startSquare, startSquare+2, MoveFlag.PetitRoque));
      }
    }
    if (board.grandRoque(color)) {
      if ((MoveGenerationData.grandRoquePiecesMask[color] & occupied) == 0) {
        moves.add(new Move(startSquare, startSquare-2, MoveFlag.GrandRoque));
      }
    }
  }

  // Coups des cavaliers

  private void addKnightMoves(ArrayList<Move> moves, int color) {
    long cavaliers = board.pieceBitboard[Piece.Cavalier + Piece.NumberOfType*color];

    while (cavaliers != 0) {
      // Récupère la case de départ du cavalier et ses attaques
      int startSquare = Long.numberOfTrailingZeros(cavaliers);
      long attacks = MoveGenerationData.knightAttacks(startSquare);

      // Convertit le bitboard des attaques en coups
      long endSquares = attacks & ~board.colorBitboard[color];
      while (endSquares != 0) {
        moves.add(new Move(startSquare, Long.numberOfTrailingZeros(endSquares)));
        endSquares &= endSquares - 1;
      }
      cavaliers &= cavaliers - 1;
    }
  }

  // Coups des pions

  private void addWhitePawnMoves(ArrayList<Move> moves) {
    long pions = board.pieceBitboard[Piece.Pion];
    long empty = ~occupied;

    // Avance simple des pions (et promotion)
    long onepush = (pions >>> 8) & empty;
    while (onepush != 0) {
      int endSquare = Long.numberOfTrailingZeros(onepush);
      if (endSquare <= 7) addPromotionMoves(moves, endSquare+8, endSquare);
      else moves.add(new Move(endSquare + 8, endSquare));
      onepush &= onepush - 1;
    }

    // Avance double des pions
    long doublepush = (((pions & Bitboard.rank2) >>> 8) & empty) >>> 8 & empty;
    while (doublepush != 0) {
      int endSquare = Long.numberOfTrailingZeros(doublepush);
      moves.add(new Move(endSquare + 16, endSquare, MoveFlag.DoubleAvance));
      doublepush &= doublepush - 1;
    }

    // Capture gauche (et promotion)
    long captureLeft = ((~Bitboard.Afile & pions) >>> 9) & board.colorBitboard[Player.Black];
    while (captureLeft != 0) {
      int endSquare = Long.numberOfTrailingZeros(captureLeft);
      if (endSquare <= 7) addPromotionMoves(moves, endSquare + 9, endSquare);
      else moves.add(new Move(endSquare + 9, endSquare));
      captureLeft &= captureLeft - 1;
    }

    // Capture droite (et promotion)
    long captureRight = ((~Bitboard.Hfile & pions) >>> 7) & board.colorBitboard[Player.Black];
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
    long empty = ~occupied;

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
