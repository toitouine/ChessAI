/////////////////////////////////////////////////////////////////

// Génération des coups pour un plateau donné.
// On utilise principalement les bitboards, et on s’appuie sur les données
// initialisées dans MoveGenerationData.java. On distingue deux types de pièces :

// - Roi, Cavalier, Pion :
// Ces pièces ont un mouvement suffisamment systématique pour pouvoir
// générer les coups rapidement avec des masks des attaques (précalculés
// dans MoveGenerationData.java) ou des opératons simples.

// - Les sliders (Dame, Tour, Fou) :
// On utilise la technique des "magic bitboards". On obtient assez simplement
// un bitboard des pièces qui bloquent le déplacement (bloqueurs). On effectue
// alors une opération sur ce bitboard (faisant intervenir un "nombre magique")
// pour obtenir un index d'un tableau où les coups sont pré-calculés (selon
// chaque configuration de bloqueurs). Au démarrage du programme, on peut donc
// générer un tel tableau par case (donc 64 tableaux pour la tour, et 64 pour
// le fou), et calculer en avance les coups sur une case donnée pour chaque
// arrangement de bloqueurs possible (voir MoveGenerationData.java).

// Précisions sur les "magic bitboards" :
// L'opération effectuée est : index = (blockers * magic) >>> (64 - n)
// où blocker correspond au bitboard des bloqueurs, magic au nombre magique de
// la case, et n au nombre de bits utiles associés au nombre magique (déterminé
// en même temps que le nombre magique).
// Pour éviter une soustraction, on sauvegarde 64 - n = shift, et non n, dans
// les tableaux. La liste des nombres magiques et des shifts (déterminés avec
// MagicFinder.java) se trouve dans Magic.java

// Coups légaux :
// Pour générer des coups légaux, on ajoute quelques contraintes supplémentaires.
// - le roi ne peut aller que sur des cases non attaquées par l'adversaire
// - le roi ne peut pas roquer si il est en échec ou traverse une case attaquée
// - une pièce clouée ne peut se déplacer que dans la direction du clouage
// - dans le cas d'une prise en passant, il faut tester un clouage horizontal
// Enfin, on distingue en plus deux situations spéciales :
// 1) Le roi est en échec : le roi doit bouger, ou la pièce qui fait échec doit
//    être capturée, ou une pièce doit s'interposer entre le roi et l'attaquant
// 2) Le roi est en double échec : seul le roi peut bouger

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;

public class MoveGenerator {

  // Plateau sur lequel générer les coups
  private final Board board;

  // Couleur de celui à qui on génère les coups et son adversaire
  private int color;
  private int opponent;

  // Bitboards utiles pour générer les coups (du point de vue de celui qui génère)
  private long[] friendlyPieces = new long[Piece.NumberOfPiece];
  private long[] opponentPieces = new long[Piece.NumberOfPiece];
  private long allFriendlyPieces;
  private long allOpponentPieces;
  private long occupied = 0; // Toutes les pièces du plateau

  public MoveGenerator(Board board) {
    this.board = board;
  }

  /////////////////////////////////////////////////////////////////

  public ArrayList<Move> getLegalMoves(int c) {
    ArrayList<Move> moves = new ArrayList<Move>(45);
    color = c;
    opponent = 1-c;

    // Récupère les bitboards
    allFriendlyPieces = board.colorBitboard[color];
    allOpponentPieces = board.colorBitboard[opponent];
    occupied = allFriendlyPieces | allOpponentPieces;
    for (int i = 0; i < Piece.NumberOfPiece; i++) {
      friendlyPieces[i] = board.pieceBitboard[i + color*Piece.NumberOfPiece];
      opponentPieces[i] = board.pieceBitboard[i + opponent*Piece.NumberOfPiece];
    }

    // Génère les coups
    addKingMoves(moves);
    addKnightMoves(moves);
    addRookMoves(moves);
    addBishopMoves(moves);
    addQueenMoves(moves);
    if (color == Player.White) addWhitePawnMoves(moves);
    else addBlackPawnMoves(moves);

    return moves;
  }

  /////////////////////////////////////////////////////////////////

  // Détecte si la case square est attaquée par une pièce adverse
  public boolean isAttacked(int square) {
    long pawnAttacks = MoveGenerationData.pawnAttacks(color, square);
    if ((pawnAttacks & opponentPieces[Piece.Pion]) != 0) return true;

    long knightAttacks = MoveGenerationData.knightAttacks(square);
    if ((knightAttacks & opponentPieces[Piece.Cavalier]) != 0) return true;

    long kingAttacks = MoveGenerationData.kingAttacks(square);
    if ((kingAttacks & opponentPieces[Piece.Roi]) != 0) return true;

    long bishopQueens = opponentPieces[Piece.Dame] | opponentPieces[Piece.Fou];
    if ((bishopQueens & Magic.getBishopAttacks(square, occupied)) != 0) return true;

    long rookQueens = opponentPieces[Piece.Dame] | opponentPieces[Piece.Tour];
    if ((rookQueens & Magic.getRookAttacks(square, occupied)) != 0) return true;

    return false;
  }

  /////////////////////////////////////////////////////////////////

  // Coups des dames
  private void addQueenMoves(ArrayList<Move> moves) {
    long dames = friendlyPieces[Piece.Dame];

    while (dames != 0) {
      // Récupère la case de départ de la dame
      int startSquare = Long.numberOfTrailingZeros(dames);

      // Récupère les coups de la tour et du fou
      long attacks = Magic.getRookAttacks(startSquare, occupied);
      attacks |= Magic.getBishopAttacks(startSquare, occupied);

      // Enlève les pièces alliées
      attacks &= ~(attacks & allFriendlyPieces);

      while (attacks != 0) {
        moves.add(new Move(startSquare, Long.numberOfTrailingZeros(attacks)));
        attacks &= attacks - 1;
      }

      dames &= dames - 1;
    }
  }

  // Coups des fous
  private void addBishopMoves(ArrayList<Move> moves) {
    long fous = friendlyPieces[Piece.Fou];

    while (fous != 0) {
      // Récupère la case de départ du fou
      int startSquare = Long.numberOfTrailingZeros(fous);

      // Récupère les attaques pré-calculées et les convertit en coups (magic bitboard)
      long attacks = Magic.getBishopAttacks(startSquare, occupied);
      attacks &= ~(attacks & allFriendlyPieces);

      while (attacks != 0) {
        moves.add(new Move(startSquare, Long.numberOfTrailingZeros(attacks)));
        attacks &= attacks - 1;
      }

      fous &= fous - 1;
    }
  }

  // Coups des tours
  private void addRookMoves(ArrayList<Move> moves) {
    long tours = friendlyPieces[Piece.Tour];

    while (tours != 0) {
      // Récupère la case de départ de la tour
      int startSquare = Long.numberOfTrailingZeros(tours);

      // Récupère les attaques pré-calculées et les convertit en coups (magic bitboard)
      long attacks = Magic.getRookAttacks(startSquare, occupied);
      attacks &= ~(attacks & allFriendlyPieces);

      while (attacks != 0) {
        moves.add(new Move(startSquare, Long.numberOfTrailingZeros(attacks)));
        attacks &= attacks - 1;
      }

      tours &= tours - 1;
    }
  }

  // Coups du roi
  private void addKingMoves(ArrayList<Move> moves) {
    // On suppose qu'il n'y a qu'un seul roi de couleur color en jeu
    int startSquare = board.roi(color);

    // Récupère les attaques du roi
    long attacks = MoveGenerationData.kingAttacks(startSquare);

    // Convertit le bitboard des attaques en coups
    long endSquares = attacks & ~allFriendlyPieces;
    while (endSquares != 0) {
      int square = Long.numberOfTrailingZeros(endSquares);
      // Ajoute le coup si la case d'arrivée n'est pas attaquée
      if (!isAttacked(square)) moves.add(new Move(startSquare, square));
      endSquares &= endSquares - 1;
    }

    // Ajoute éventuellement les roques
    if (board.petitRoque(color)) {
      if ((MoveGenerationData.petitRoquePiecesMask[color] & occupied) == 0) {
        if (!isAttacked(startSquare+1) & !isAttacked(startSquare+2)) {
          moves.add(new Move(startSquare, startSquare+2, MoveFlag.PetitRoque));
        }
      }
    }
    if (board.grandRoque(color)) {
      if ((MoveGenerationData.grandRoquePiecesMask[color] & occupied) == 0) {
        if (!isAttacked(startSquare-1) & !isAttacked(startSquare-2)) {
          moves.add(new Move(startSquare, startSquare-2, MoveFlag.GrandRoque));
        }
      }
    }
  }

  // Coups des cavaliers
  private void addKnightMoves(ArrayList<Move> moves) {
    long cavaliers = friendlyPieces[Piece.Cavalier];

    while (cavaliers != 0) {
      // Récupère la case de départ du cavalier et ses attaques
      int startSquare = Long.numberOfTrailingZeros(cavaliers);
      long attacks = MoveGenerationData.knightAttacks(startSquare);

      // Convertit le bitboard des attaques en coups
      long endSquares = attacks & ~allFriendlyPieces;
      while (endSquares != 0) {
        moves.add(new Move(startSquare, Long.numberOfTrailingZeros(endSquares)));
        endSquares &= endSquares - 1;
      }
      cavaliers &= cavaliers - 1;
    }
  }

  // Coups des pions (blancs)
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

  // Coups des pions (noirs)
  private void addBlackPawnMoves(ArrayList<Move> moves) {
    long pions = board.pieceBitboard[Piece.Pion + Piece.NumberOfPiece];
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

  // Ajoute les coups de promotion
  private void addPromotionMoves(ArrayList<Move> moves, int startSquare, int endSquare) {
    moves.add(new Move(startSquare, endSquare, MoveFlag.PromotionDame));
    moves.add(new Move(startSquare, endSquare, MoveFlag.PromotionCavalier));
    moves.add(new Move(startSquare, endSquare, MoveFlag.PromotionTour));
    moves.add(new Move(startSquare, endSquare, MoveFlag.PromotionFou));
  }
}
