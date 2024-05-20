/////////////////////////////////////////////////////////////////

// Génération des coups pour un plateau donné.
// On utilise principalement les bitboards, et on s’appuie sur les données
// initialisées dans MGData.java. On distingue deux types de pièces :

// - Roi, Cavalier, Pion :
// Ces pièces ont un mouvement suffisamment systématique pour pouvoir
// générer les coups rapidement avec des masks des attaques (précalculés
// dans MGData.java) ou des opératons simples.

// - Les sliders (Dame, Tour, Fou) :
// On utilise la technique des "magic bitboards". On obtient assez simplement
// un bitboard des pièces qui bloquent le déplacement (bloqueurs). On effectue
// alors une opération sur ce bitboard (faisant intervenir un "nombre magique")
// pour obtenir un index d'un tableau où les coups sont pré-calculés (selon
// chaque configuration de bloqueurs). Au démarrage du programme, on peut donc
// générer un tel tableau par case (donc 64 tableaux pour la tour, et 64 pour
// le fou), et calculer en avance les coups sur une case donnée pour chaque
// arrangement de bloqueurs possible (voir MGData.java).

// Précisions sur les "magic bitboards" :
// L'opération effectuée est : index = (blockers * magic) >>> (64 - n)
// où blocker correspond au bitboard des bloqueurs, magic au nombre magique de
// la case, et n au nombre de bits utiles associés au nombre magique (déterminé
// en même temps que le nombre magique).
// Pour éviter une soustraction, on sauvegarde 64 - n = shift, et non n, dans
// les tableaux. La liste des nombres magiques et des shifts (déterminés avec
// MagicFinder.java) se trouve dans Magic.java

// Coups légaux :
// Pour générer des coups légaux, on ajoute quelques contraintes supplémentaires :
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

  // Case du roi allié
  private int kingSquare;

  // Bitboards utiles pour générer les coups (du point de vue de celui qui génère)
  private long[] friendlyPieces = new long[Piece.Number];
  private long[] opponentPieces = new long[Piece.Number];
  private long allFriendlyPieces;
  private long allOpponentPieces;
  private long occupied; // Toutes les pièces du plateau
  private long pinned; // Pièces clouées au roi

  public MoveGenerator(Board board) {
    this.board = board;
  }

  /////////////////////////////////////////////////////////////////

  public ArrayList<Move> getLegalMoves(int c) {
    ArrayList<Move> moves = new ArrayList<Move>(45);
    color = c;
    opponent = 1-c;
    kingSquare = board.roi(color);

    // Récupère les bitboards
    allFriendlyPieces = board.colorBitboard[color];
    allOpponentPieces = board.colorBitboard[opponent];
    occupied = allFriendlyPieces | allOpponentPieces;
    for (int i = 0; i < Piece.Number; i++) {
      friendlyPieces[i] = board.pieceBitboard[i + color*Piece.Number];
      opponentPieces[i] = board.pieceBitboard[i + opponent*Piece.Number];
    }

    // Récupère les pièces clouées
    pinned = getPinnedPieces();

    // Génère les coups
    // addKingMoves(moves);
    // addKnightMoves(moves);
    // addRookMoves(moves);
    // addBishopMoves(moves);
    // addQueenMoves(moves);
    if (color == Player.White) addWhitePawnMoves(moves);
    else addBlackPawnMoves(moves);

    return moves;
  }

  /////////////////////////////////////////////////////////////////

  // Renvoie les cases attaquées par x-ray (derrière les pièces blockers)
  // Note : cela renvoie toutes les cases attaquées, occupées ou non.
  private long rookXray(int square, long blockers) {
    long attacks = Magic.getRookAttacks(square, occupied);
    blockers &= attacks;
    return attacks ^ Magic.getRookAttacks(square, occupied ^ blockers);
  }

  private long bishopXray(int square, long blockers) {
    long attacks = Magic.getBishopAttacks(square, occupied);
    blockers &= attacks;
    return attacks ^ Magic.getBishopAttacks(square, occupied ^ blockers);
  }

  // Renvoie les pièces clouées au roi
  private long getPinnedPieces() {
    // Récupère les pièces qui clouent (pinners)
    long orthoSliders = opponentPieces[Piece.Dame] | opponentPieces[Piece.Tour];
    long diagoSliders = opponentPieces[Piece.Dame] | opponentPieces[Piece.Fou];
    long pinners = rookXray(kingSquare, allFriendlyPieces) & orthoSliders;
    pinners |= bishopXray(kingSquare, allFriendlyPieces) & diagoSliders;

    // Récupère les pièces clouées
    long pinned = 0L;
    while (pinners != 0) {
      int square = Long.numberOfTrailingZeros(pinners);
      pinned |= MGData.inBetween(square, kingSquare) & allFriendlyPieces;
      pinners &= pinners - 1;
    }

    return pinned;
  }

  // Détecte si la case square est attaquée par une pièce adverse
  public boolean isAttacked(int square) {
    long pawnAttacks = MGData.pawnAttacks(color, square);
    if ((pawnAttacks & opponentPieces[Piece.Pion]) != 0) return true;

    long knightAttacks = MGData.knightAttacks(square);
    if ((knightAttacks & opponentPieces[Piece.Cavalier]) != 0) return true;

    long kingAttacks = MGData.kingAttacks(square);
    if ((kingAttacks & opponentPieces[Piece.Roi]) != 0) return true;

    long bishopQueens = opponentPieces[Piece.Dame] | opponentPieces[Piece.Fou];
    if ((bishopQueens & Magic.getBishopAttacks(square, occupied)) != 0) return true;

    long rookQueens = opponentPieces[Piece.Dame] | opponentPieces[Piece.Tour];
    if ((rookQueens & Magic.getRookAttacks(square, occupied)) != 0) return true;

    return false;
  }

  /////////////////////////////////////////////////////////////////

  // Génération des coups de chaque type de pièce

  // Coups des dames
  private void addQueenMoves(ArrayList<Move> moves) {
    long dames = friendlyPieces[Piece.Dame];

    while (dames != 0) {
      // Récupère la case de départ de la dame
      int startSquare = Long.numberOfTrailingZeros(dames);

      // Si la dame est clouée, récupère les cases de déplacement valides
      long possibleSquares = -1L;
      if ((dames & -dames & pinned) != 0) {
        possibleSquares = MGData.ray(kingSquare, startSquare);
      }

      // Récupère les coups de la tour et du fou
      long attacks = Magic.getRookAttacks(startSquare, occupied);
      attacks |= Magic.getBishopAttacks(startSquare, occupied);

      // Ne conserve que les attaques valides et enlève les pièces alliées
      attacks &= possibleSquares;
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

      long possibleSquares = -1L;
      if ((fous & -fous & pinned) != 0) {
        possibleSquares = MGData.ray(kingSquare, startSquare);
      }

      // Récupère les attaques pré-calculées et les convertit en coups (magic bitboard)
      long attacks = Magic.getBishopAttacks(startSquare, occupied);
      attacks &= possibleSquares;
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

      long possibleSquares = -1L;
      if ((tours & -tours & pinned) != 0) {
        possibleSquares = MGData.ray(kingSquare, startSquare);
      }

      // Récupère les attaques pré-calculées et les convertit en coups (magic bitboard)
      long attacks = Magic.getRookAttacks(startSquare, occupied);
      attacks &= possibleSquares;
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
    // Récupère les attaques du roi
    long attacks = MGData.kingAttacks(kingSquare);

    // Convertit le bitboard des attaques en coups
    long endSquares = attacks & ~allFriendlyPieces;
    while (endSquares != 0) {
      int square = Long.numberOfTrailingZeros(endSquares);
      // Ajoute le coup si la case d'arrivée n'est pas attaquée
      if (!isAttacked(square)) moves.add(new Move(kingSquare, square));
      endSquares &= endSquares - 1;
    }

    // Ajoute éventuellement les roques
    if (board.petitRoque(color)) {
      if ((MGData.petitRoquePiecesMask[color] & occupied) == 0) {
        if (!isAttacked(kingSquare+1) & !isAttacked(kingSquare+2)) {
          moves.add(new Move(kingSquare, kingSquare+2, MoveFlag.PetitRoque));
        }
      }
    }
    if (board.grandRoque(color)) {
      if ((MGData.grandRoquePiecesMask[color] & occupied) == 0) {
        if (!isAttacked(kingSquare-1) & !isAttacked(kingSquare-2)) {
          moves.add(new Move(kingSquare, kingSquare-2, MoveFlag.GrandRoque));
        }
      }
    }
  }

  // Coups des cavaliers
  private void addKnightMoves(ArrayList<Move> moves) {
    long cavaliers = friendlyPieces[Piece.Cavalier];

    while (cavaliers != 0) {
      // Si le cavalier est cloué, il ne peut pas bouger
      if ((cavaliers & -cavaliers & pinned) != 0) {
        cavaliers &= cavaliers - 1;
        continue;
      }

      // Récupère la case de départ du cavalier et ses attaques
      int startSquare = Long.numberOfTrailingZeros(cavaliers);
      long attacks = MGData.knightAttacks(startSquare);

      // Convertit le bitboard des attaques en coups
      long endSquares = attacks & ~allFriendlyPieces;
      while (endSquares != 0) {
        moves.add(new Move(startSquare, Long.numberOfTrailingZeros(endSquares)));
        endSquares &= endSquares - 1;
      }
      cavaliers &= cavaliers - 1;
    }
  }

  // Coups des pions blancs
  private void addWhitePawnMoves(ArrayList<Move> moves) {
    long pions = friendlyPieces[Piece.Pion];
    long empty = ~occupied;

    // Avance simple et double des pions, captures
    long onepush = (pions >>> 8) & empty;
    long doublepush = ((onepush & Bitboard.rank3) >>> 8) & empty;
    long captureLeft = ((~Bitboard.Afile & pions) >>> 9) & allOpponentPieces;
    long captureRight = ((~Bitboard.Hfile & pions) >>> 7) & allOpponentPieces;

    while (onepush != 0) {
      int endSquare = Long.numberOfTrailingZeros(onepush);
      int startSquare = endSquare+8;
      if (((1L << startSquare) & pinned) != 0) {
        if (!MGData.isSameFile(startSquare, kingSquare)) {
          onepush &= onepush - 1;
          continue;
        }
      }
      if (endSquare <= 7) addPromotionMoves(moves, startSquare, endSquare);
      else moves.add(new Move(startSquare, endSquare));
      onepush &= onepush - 1;
    }

    while (doublepush != 0) {
      int endSquare = Long.numberOfTrailingZeros(doublepush);
      int startSquare = endSquare + 16;
      if (((1L << startSquare) & pinned) != 0) {
        if (!MGData.isSameFile(startSquare, kingSquare)) {
          doublepush &= doublepush - 1;
          continue;
        }
      }
      moves.add(new Move(startSquare, endSquare, MoveFlag.DoubleAvance));
      doublepush &= doublepush - 1;
    }

    while (captureLeft != 0) {
      int endSquare = Long.numberOfTrailingZeros(captureLeft);
      int startSquare = endSquare + 9;
      if (((1L << startSquare) & pinned) != 0) {
        if (!MGData.isSameLeftDiag(startSquare, kingSquare)) {
          captureLeft &= captureLeft - 1;
          continue;
        }
      }
      if (endSquare <= 7) addPromotionMoves(moves, startSquare, endSquare);
      else moves.add(new Move(startSquare, endSquare));
      captureLeft &= captureLeft - 1;
    }

    while (captureRight != 0) {
      int endSquare = Long.numberOfTrailingZeros(captureRight);
      int startSquare = endSquare + 7;
      if (((1L << startSquare) & pinned) != 0) {
        if (!MGData.isSameRightDiag(startSquare, kingSquare)) {
          captureRight &= captureRight - 1;
          continue;
        }
      }
      if (endSquare <= 7) addPromotionMoves(moves, startSquare, endSquare);
      else moves.add(new Move(startSquare, endSquare));
      captureRight &= captureRight - 1;
    }

    // En passant
    if (board.enPassantSquare[opponent] == null) return;

    int caseEnPassant = board.enPassantSquare[opponent];
    long mangeurs = pions & MGData.pawnAttacks(opponent, caseEnPassant);
    while (mangeurs != 0) {
      int startSquare = Long.numberOfTrailingZeros(mangeurs);

      // On doit tester si le pion est cloué normalement ou horizontalement de manière spéciale
      if (((1L << startSquare) & pinned) != 0) {
        long possibleSquares = MGData.ray(kingSquare, startSquare);
        if ((possibleSquares & (1L << caseEnPassant)) == 0) {
          mangeurs &= mangeurs - 1;
          continue;
        }
      }
      long ennemySliders = opponentPieces[Piece.Tour] | opponentPieces[Piece.Dame];
      long kingBitboard = friendlyPieces[Piece.Roi];
      if ((Bitboard.rank5 & kingBitboard) != 0 && (Bitboard.rank5 & ennemySliders) != 0) {
        long twoPawns = (1L << startSquare) | (1L << (caseEnPassant + 8));
        long ray = MGData.ray(kingSquare, startSquare);
        if ((ray & ~twoPawns & ~ennemySliders & occupied) == 0) {
          mangeurs &= mangeurs - 1;
          continue;
        }
      }

      moves.add(new Move(startSquare, caseEnPassant, MoveFlag.EnPassant));
      mangeurs &= mangeurs - 1;
    }
  }

  // Coups des pions noirs
  private void addBlackPawnMoves(ArrayList<Move> moves) {
    long pions = friendlyPieces[Piece.Pion];
    long empty = ~occupied;

    // Avance simple et double des pions, captures
    long onepush = (pions << 8) & empty;
    long doublepush = ((onepush & Bitboard.rank6) << 8) & empty;
    long captureLeft = ((~Bitboard.Afile & pions) << 7) & allOpponentPieces;
    long captureRight = ((~Bitboard.Hfile & pions) << 9) & allOpponentPieces;

    while (onepush != 0) {
      int endSquare = Long.numberOfTrailingZeros(onepush);
      int startSquare = endSquare - 8;
      if (((1L << startSquare) & pinned) != 0) {
        if (!MGData.isSameFile(startSquare, kingSquare)) {
          onepush &= onepush - 1;
          continue;
        }
      }
      if (endSquare >= 56) addPromotionMoves(moves, startSquare, endSquare);
      else moves.add(new Move(startSquare, endSquare));
      onepush &= onepush - 1;
    }

    while (doublepush != 0) {
      int endSquare = Long.numberOfTrailingZeros(doublepush);
      int startSquare = endSquare - 16;
      if (((1L << startSquare) & pinned) != 0) {
        if (!MGData.isSameFile(startSquare, kingSquare)) {
          doublepush &= doublepush - 1;
          continue;
        }
      }
      moves.add(new Move(startSquare, endSquare, MoveFlag.DoubleAvance));
      doublepush &= doublepush - 1;
    }

    while (captureRight != 0) {
      int endSquare = Long.numberOfTrailingZeros(captureRight);
      int startSquare = endSquare - 9;
      if (((1L << startSquare) & pinned) != 0) {
        if (!MGData.isSameLeftDiag(startSquare, kingSquare)) {
          captureRight &= captureRight - 1;
          continue;
        }
      }
      if (endSquare >= 56) addPromotionMoves(moves, startSquare, endSquare);
      else moves.add(new Move(startSquare, endSquare));
      captureRight &= captureRight - 1;
    }

    while (captureLeft != 0) {
      int endSquare = Long.numberOfTrailingZeros(captureLeft);
      int startSquare = endSquare - 7;
      if (((1L << startSquare) & pinned) != 0) {
        if (!MGData.isSameRightDiag(startSquare, kingSquare)) {
          captureLeft &= captureLeft - 1;
          continue;
        }
      }
      if (endSquare >= 56) addPromotionMoves(moves, startSquare, endSquare);
      else moves.add(new Move(startSquare, endSquare));
      captureLeft &= captureLeft - 1;
    }

    // En passant
    if (board.enPassantSquare[opponent] == null) return;

    int caseEnPassant = board.enPassantSquare[opponent];
    long mangeurs = pions & MGData.pawnAttacks(opponent, caseEnPassant);
    while (mangeurs != 0) {
      int startSquare = Long.numberOfTrailingZeros(mangeurs);

      // On doit tester si le pion est cloué normalement ou horizontalement de manière spéciale
      if (((1L << startSquare) & pinned) != 0) {
        long possibleSquares = MGData.ray(kingSquare, startSquare);
        if ((possibleSquares & (1L << caseEnPassant)) == 0) {
          mangeurs &= mangeurs - 1;
          continue;
        }
      }
      long ennemySliders = opponentPieces[Piece.Tour] | opponentPieces[Piece.Dame];
      long kingBitboard = friendlyPieces[Piece.Roi];
      if ((Bitboard.rank4 & kingBitboard) != 0 && (Bitboard.rank4 & ennemySliders) != 0) {
        long twoPawns = (1L << startSquare) | (1L << (caseEnPassant - 8));
        long ray = MGData.ray(kingSquare, startSquare);
        if ((ray & ~twoPawns & ~ennemySliders & occupied) == 0) {
          mangeurs &= mangeurs - 1;
          continue;
        }
      }

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
