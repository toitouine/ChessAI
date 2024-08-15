/////////////////////////////////////////////////////////////////

// Board
// Représente une position à un moment donné dans la partie (pièces, informations,
// suivi du matériel...). Contient également les informations nécessaires pour
// passer à la position précédente en annulant un coup.
// Note : pour annuler un coup, il est nécessaire de l'annuler dans la position
// obtenue juste après avoir joué le coup

// La position des pièces est stockée sous forme de bitboards :
// Chaque case est représentée par un indice (de 0 à 63,  8 * ligne + colonne)
// La case en haut à gauche correspond à 0, et celle en bas à droite à 63.
// Chaque bit du bitboard correspond à une case (le bit n correspond à la case
// d'indice n) : le bit est à 1 si il y a une pièce, et 0 sinon

// La méthode setEvaluation() permet d'attacher une évaluation au plateau
// pour actualiser material et positional, qui indiquent respectivement
// les évaluations matérielles et positionnelles.
// (voir Evaluation.java pour plus d'informations sur l'évaluation)

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public final class Board implements Serializable {

  public int tourDeQui = Player.White; // Joueur qui doit jouer
  public float phase = 0; // Représente l'avancement de la partie (0 = ouverture, 1 = finale)
  public long zobrist; // Hash de la position (zobrist)

  // Représente les droits au roques (0 si non et 1 si oui, voir les masks pour l'ordre)
  public byte castleState;
  private final int[] petitRoqueMask = {0b1000, 0b0010};
  private final int[] grandRoqueMask = {0b0100, 0b0001};

  private Piece[] grid = new Piece[64]; // Représente les pièces sur l'échiquier
  private int[] rois = {-1, -1}; // Accès rapide à la case des rois de la partie

  // Bitboards (1 si il y a une pièce, 0 si il n'y en a pas)
  public long[] colorBitboard; // Bitboard pour chaque couleur (blanc puis noir)
  public long[] pieceBitboard; // Bitboard pour chaque index de pièce (voir piece.index)

  // Case en passantable sur cette position
  // (une pour les noirs et une pour les blancs pour faciliter l'expiration des cases)
  public Integer[] enPassantSquare = new Integer[2];

  // Sauvegardes pour pouvoir annuler correctement un coup
  public Deque<MoveSave> saves = new ArrayDeque<MoveSave>();

  // Positions rencontrées pour détecter les répétitions
  public HashMap<Long, Integer> hashHistory = new HashMap<Long, Integer>();

  // Générateur de coups
  private MoveGenerator generator;

  // Évaluation pour actualiser des données d'évaluation de la position
  private Evaluation evaluation = new DefaultEvaluation();
  private float[] material = new float[2];
  private float[] positional = new float[2];

  public Board(String fen) {
    colorBitboard = new long[2];
    pieceBitboard = new long[2*Piece.Number];
    generator = new MoveGenerator(this);
    loadFEN(fen);
  }

  public Board() {
    this(Config.General.defaultFEN);
  }

  /////////////////////////////////////////////////////////////////

  // Génère la position à partir d'une fen
  public void loadFEN(String f) {
    Fen.loadPosition(this, f);
    calculatePhase();
    calculateAndPushHash();
    updateEvaluation();
  }

  // Génère la fen de la position
  public String generateFEN() {
    return Fen.generateFEN(this);
  }

  // Renvoie la pièce située sur une case
  public Piece grid(int index) {
    return grid[index];
  }

  // Renvoie la pièce située sur une case (ligne puis colonne)
  public Piece grid(int i, int j) {
    return grid[8*i + j];
  }

  public void setEvaluation(Evaluation eval) {
    if (eval != null) {
      evaluation = eval;
      updateEvaluation();
    }
  }

  // Renvoie la case du roi color
  public int roi(int color) {
    return rois[color];
  }

  // Renvoie la case en passantable si il y en a une
  public Integer getEnPassantSquare() {
    if (enPassantSquare[0] != null) return enPassantSquare[0];
    return enPassantSquare[1];
  }

  // Renvoie si le joueur color a droit au petit roque ou non
  public boolean petitRoque(int color) {
    return (castleState & petitRoqueMask[color]) != 0;
  }

  // Renvoie si le joueur color a droit au grand roque ou non
  public boolean grandRoque(int color) {
    return (castleState & grandRoqueMask[color]) != 0;
  }

  // Active le petit roque chez color
  public void enablePetitRoque(int color) {
    int shift = (color == Player.White ? 3 : 1);
    castleState |= 1 << shift;
  }

  // Active le grand roque chez color
  public void enableGrandRoque(int color) {
    int shift = (color == Player.White ? 2 : 0);
    castleState |= 1 << shift;
  }

  // Renvoie le bitboard des pièces (noires et blanches) d'un certain type
  public long getPieces(int type) {
    return pieceBitboard[type] | pieceBitboard[type + Piece.Number];
  }

  public long getPieces(int type, int color) {
    return pieceBitboard[type + Piece.Number*color];
  }

  public boolean isRepeated(long hash) {
    return hashHistory.containsKey(hash) && hashHistory.get(hash) >= 3;
  }

  public float materialScore(int color) {
    return material[color];
  }

  public float positionalScore(int color) {
    return positional[color];
  }

  /////////////////////////////////////////////////////////////////

  // Ajoute la pièce sur le plateau et recalcule les données de la position (lent)
  // Uniquement fait pour des modifications de positions (type éditeur de positions ou fen)
  public void addPiece(int index, int square) {
    if (grid[square] != null) {
      Debug.error("Ajout d'une pièce par dessus une autre");
      return;
    }

    Piece p = new Piece(index);

    if (p.type == Piece.Roi) {
      if (pieceBitboard[index] != 0) Debug.error("Ajout d'un deuxième roi sur le plateau");
      rois[p.color] = square;
    }

    grid[square] = p;
    colorBitboard[p.color] |= 1L << square;
    pieceBitboard[index] |= 1L << square;
    calculatePhase();
    calculateAndPushHash();
    material[p.color] += evaluation.materialValue(p);
    positional[p.color] += evaluation.positionalValue(p, square);
  }

  // Calcule le coefficient indiquant la phase de jeu (0 pour l'ouverture et 1 pour la finale)
  public void calculatePhase() {
    phase = 0;

    for (int i = 0; i < Piece.Number; i++) {
      long bitboard = pieceBitboard[i] | pieceBitboard[i+Piece.Number];
      phase += Long.bitCount(bitboard) * Config.Piece.phases[i];
    }

    phase = Math.clamp(1 - (phase / Config.Piece.totalPhase), 0, 1);
  }

  // Calcule le hash de la position et actualise le compteur de hash
  public void calculateAndPushHash() {
    zobrist = Zobrist.calculateHash(this);
    hashHistory.merge(zobrist, 1, Integer::sum);
  }

  // Vide complètement le plateau et réinitialise les variables
  public void clear() {
    for (int i = 0; i < 64; i++) {
      grid[i] = null;
    }

    for (int i = 0; i < 2; i++) {
      rois[i] = -1;
      colorBitboard[i] = 0;
      enPassantSquare[i] = null;
    }

    for (int i = 0; i < pieceBitboard.length; i++) {
      pieceBitboard[i] = 0;
    }

    tourDeQui = Player.White;
    castleState = 0;
    hashHistory.clear();
    calculatePhase();
    calculateAndPushHash();
    updateEvaluation();
  }

  private void updateEvaluation() {
    material[Player.White] = material[Player.Black] = 0;
    positional[Player.White] = positional[Player.Black] = 0;

    for (int i = 0; i  < 64; i++) {
      if (grid[i] != null) {
        Piece p = grid[i];
        material[p.color] += evaluation.materialValue(p);
        positional[p.color] += evaluation.positionalValue(p, i);
      }
    }
  }

  /////////////////////////////////////////////////////////////////

  // Joue un coup sur le plateau
  public void make(Move move) {
    int startSquare = move.startSquare();
    int endSquare = move.endSquare();
    int flag = move.flag();
    Piece piece = grid[startSquare];
    Piece capture = grid[endSquare];
    int color = piece.color;
    int opponent = 1 - color;

    // Sauvegarde les données de la position dans l'historique
    MoveSave save = new MoveSave(capture, castleState,
                                 phase, zobrist,
                                 enPassantSquare[opponent],
                                 material, positional);
    saves.push(save);

    // Déplacement de la pièce
    grid[endSquare] = grid[startSquare];
    grid[startSquare] = null;
    positional[color] -= evaluation.positionalValue(piece, startSquare);
    positional[color] += evaluation.positionalValue(piece, endSquare);
    if (piece.type == Piece.Roi) rois[color] = endSquare;

    // Actualise la clé de la position (XOR out et in)
    zobrist ^= Zobrist.piecesOnSquare[piece.index][startSquare];
    zobrist ^= Zobrist.piecesOnSquare[piece.index][endSquare];

    // Actualise les bitboards
    long movingMask = (1L << startSquare | 1L << endSquare);
    colorBitboard[color] ^= movingMask;
    pieceBitboard[piece.index] ^= movingMask;

    // Dans le cas d'une capture
    if (capture != null) {
      zobrist ^= Zobrist.piecesOnSquare[capture.index][endSquare]; // XOR out la capture
      colorBitboard[opponent] ^= (1L << endSquare);
      pieceBitboard[capture.index] ^= (1L << endSquare);
      material[opponent] -= evaluation.materialValue(capture);
      positional[opponent] -= evaluation.positionalValue(capture, endSquare);
    }

    // Enlève tous les droits du roque du hash
    zobrist ^= Zobrist.castlingRights[castleState];

    // Met la case en passantable
    if (flag == MoveFlag.DoubleAvance) {
      enPassantSquare[color] = startSquare + (color == Player.White ? -8 : 8);
    }

    // Capture le pion pris en passant
    else if (flag == MoveFlag.EnPassant) {
      int capturedSquare = endSquare + (color == Player.White ? 8 : -8);
      Piece capturedPawn = grid[capturedSquare];
      colorBitboard[opponent] ^= (1L << capturedSquare);
      pieceBitboard[capturedPawn.index] ^= (1L << capturedSquare);
      grid[capturedSquare] = null;
      // XOR out la capture du pion en passant
      zobrist ^= Zobrist.piecesOnSquare[capturedPawn.index][capturedSquare];
      material[opponent] -= evaluation.materialValue(capturedPawn);
      positional[opponent] -= evaluation.positionalValue(capturedPawn, capturedSquare);
    }

    // Roques (déplace la tour au bon endroit)
    else if (flag == MoveFlag.PetitRoque) {
      Piece tour = grid[startSquare+3];
      grid[startSquare+1] = tour;
      grid[startSquare+3] = null;
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare+3];
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare+1];
      pieceBitboard[tour.index] ^= (1L << (startSquare+3) | 1L << (startSquare+1));
      colorBitboard[color] ^= (1L << (startSquare+3) | 1L << (startSquare+1));
      positional[color] -= evaluation.positionalValue(Piece.Tour, color, startSquare+3);
      positional[color] += evaluation.positionalValue(Piece.Tour, color, startSquare+1);
    }
    else if (flag == MoveFlag.GrandRoque) {
      Piece tour = grid[startSquare-4];
      grid[startSquare-1] = tour;
      grid[startSquare-4] = null;
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare-4];
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare-1];
      pieceBitboard[tour.index] ^= (1L << (startSquare-4) | 1L << (startSquare-1));
      colorBitboard[color] ^= (1L << (startSquare-4) | 1L << (startSquare-1));
      positional[color] -= evaluation.positionalValue(Piece.Tour, color, startSquare-4);
      positional[color] += evaluation.positionalValue(Piece.Tour, color, startSquare-1);
    }

    // Promotion
    else if (MoveFlag.isPromotion(flag)) {
      Piece promotion = new Piece(MoveFlag.getPromotionPieceType(flag), color);
      grid[endSquare] = promotion;
      zobrist ^= Zobrist.piecesOnSquare[piece.index][endSquare]; // Retire le pion du hash
      zobrist ^= Zobrist.piecesOnSquare[promotion.index][endSquare]; // Ajoute la pièce de promotion au hash
      // Ajoute la pièce de promotion au bitboard, et retire le pion
      // (colorBitboard n'est pas modifié car il y a toujours une pièce sur la case de promotion)
      pieceBitboard[piece.index] ^= (1L << endSquare);
      pieceBitboard[promotion.index] |= (1L << endSquare);
      material[color] -= evaluation.materialValue(Piece.Pion);
      material[color] += evaluation.materialValue(promotion);
      positional[color] -= evaluation.positionalValue(Piece.Pion, color, endSquare);
      positional[color] += evaluation.positionalValue(promotion, endSquare);
    }

    // Actualise les droits au roque
    if (piece.type == Piece.Roi) {
      castleState &= ~petitRoqueMask[color];
      castleState &= ~grandRoqueMask[color];
    }
    if (castleState != 0) {
      if (startSquare == 63 || endSquare == 63) castleState &= ~petitRoqueMask[Player.White];
      if (startSquare == 56 || endSquare == 56) castleState &= ~grandRoqueMask[Player.White];
      if (startSquare == 7 || endSquare == 7) castleState &= ~petitRoqueMask[Player.Black];
      if (startSquare == 0 || endSquare == 0) castleState &= ~grandRoqueMask[Player.Black];
    }

    // Expiration d'une case en passant éventuelle
    enPassantSquare[opponent] = null;

    // Changement de tour
    tourDeQui = 1 - tourDeQui;
    zobrist ^= Zobrist.blackToMove;

    // Ajoute les droits du roque du hash
    zobrist ^= Zobrist.castlingRights[castleState];

    // Actualise la phase du jeu si nécessaire
    if (capture != null || flag == MoveFlag.EnPassant || MoveFlag.isPromotion(flag)) {
      calculatePhase();
    }

    // Ajoute la position au compteur de positions
    hashHistory.merge(zobrist, 1, Integer::sum);
  }

  // Annule un coup sur le plateau
  // Note : doit être fait dans la position obtenue immédiatement après avoir joué le coup
  public void unmake(Move move) {
    // On se place du point de vue de celui qui a joué le coup move
    // C'est donc maintenant le tour de l'adversaire
    int startSquare = move.startSquare();
    int endSquare = move.endSquare();
    int flag = move.flag();
    Piece piece = grid[endSquare]; // Pièce qui a bougé (ou dans le cas d'une promotion, pièce de promotion)
    int color = piece.color;
    int opponent = tourDeQui;

    // Retour des sauvegardes
    MoveSave save = saves.pop();
    hashHistory.put(zobrist, hashHistory.get(zobrist) - 1);
    phase = save.phase;
    zobrist = save.zobrist;
    castleState = save.castleState;
    enPassantSquare[opponent] = save.opponentEnPassant;
    material[Player.White] = save.material[Player.White];
    material[Player.Black] = save.material[Player.Black];
    positional[Player.White] = save.positional[Player.White];
    positional[Player.Black] = save.positional[Player.Black];
    Piece capture = save.capture;

    // Déplacement de la pièce
    // Note : si on est dans le cas d'une promotion, la pièce qui a bougé est la pièce de promotion
    grid[startSquare] = grid[endSquare];
    grid[endSquare] = capture;
    if (piece.type == Piece.Roi) rois[color] = startSquare;

    // Actualise les bitboards
    long movingMask = (1L << startSquare | 1L << endSquare);
    colorBitboard[color] ^= movingMask;
    pieceBitboard[piece.index] ^= movingMask;

    // Si c'est une capture, ajoute la pièce retirée
    if (capture != null) {
      colorBitboard[opponent] |= (1L << endSquare);
      pieceBitboard[capture.index] |= (1L << endSquare);
    }

    // Enlève éventuellement un case en passant
    if (flag == MoveFlag.DoubleAvance) enPassantSquare[color] = null;

    // Ajoute le pion pris en passant
    else if (flag == MoveFlag.EnPassant) {
      int capturedSquare = endSquare + (color == Player.White ? 8 : -8);
      grid[capturedSquare] = new Piece(Piece.Pion, opponent);
      colorBitboard[opponent] |= (1L << capturedSquare);
      pieceBitboard[grid[capturedSquare].index] |= (1L << capturedSquare);
    }

    // Replace la tour dans le cas d'un roque
    else if (flag == MoveFlag.PetitRoque) {
      Piece tour = grid[startSquare+1];
      grid[startSquare+3] = tour;
      grid[startSquare+1] = null;
      pieceBitboard[tour.index] ^= (1L << (startSquare+3) | 1L << (startSquare+1));
      colorBitboard[color] ^= (1L << (startSquare+3) | 1L << (startSquare+1));
    }
    else if (flag == MoveFlag.GrandRoque) {
      Piece tour = grid[startSquare-1];
      grid[startSquare-4] = tour;
      grid[startSquare-1] = null;
      pieceBitboard[tour.index] ^= (1L << (startSquare-4) | 1L << (startSquare-1));
      colorBitboard[color] ^= (1L << (startSquare-4) | 1L << (startSquare-1));
    }

    // Promotion
    else if (MoveFlag.isPromotion(flag)) {
      // Dans le cas de la promotion, piece représente la pièce de promotion
      // On remplace la pièce déplacée (de la case d'arrivée à la case de départ) par un pion
      grid[startSquare] = new Piece(Piece.Pion, color);
      pieceBitboard[piece.index] ^= (1L << startSquare); // Retire la pièce de promotion de la case de départ
      pieceBitboard[grid[startSquare].index] |= (1L << startSquare); // Ajoute le pion sur la case de départ
    }

    // Changement de tour
    tourDeQui = 1 - tourDeQui;
  }

  public void make(String moveString, int flag) {
    make(new Move(moveString, flag));
  }

  public void make(String moveString) {
    make(new Move(moveString, MoveFlag.None));
  }

  /////////////////////////////////////////////////////////////////

  public ArrayList<Move> getLegalMoves(int color) {
    return generator.getLegalMoves(color);
  }

  public ArrayList<Move> getLegalMoves() {
    return getLegalMoves(tourDeQui);
  }

  public ArrayList<Move> getSquareMoves(int square) {
    ArrayList<Move> allMoves = getLegalMoves(Player.White);
    allMoves.addAll(getLegalMoves(Player.Black));
    ArrayList<Move> moves = new ArrayList<Move>();

    for (Move move : allMoves) {
      if (move.startSquare() == square) moves.add(move);
    }

    return moves;
  }

  // Renvoie si le joueur est en échec ou non
  public boolean inCheck(int color) {
    long occupancy = colorBitboard[Player.White] | colorBitboard[Player.Black];
    return generator.isAttacked(rois[color], occupancy);
  }

  public boolean inCheck() {
    return inCheck(tourDeQui);
  }

  /////////////////////////////////////////////////////////////////

  public long perft(int depth) {
    return perft(depth, true, true);
  }

  public long perft(int depth, boolean bulk, boolean print) {
    if (print) {
      Debug.log("perft", "");
      Debug.log("perft", "Démarrage de perft " + depth);
      if (bulk) Debug.log("perft", "Bulk-counting activé");
      Debug.log("perft", "");
    }

    long before = System.nanoTime();

    long total = 0;
    ArrayList<Move> moves = getLegalMoves();
    for (int i = 0; i < moves.size(); i++) {
      Move move = moves.get(i);
      make(move);
      long count = countMoves(depth-1, bulk);
      unmake(move);
      if (print) Debug.log("perft", move.notation() + ": " + count);
      total += count;
    }

    long time = System.nanoTime() - before;
    float timeMs = (float)time/1000000;
    float mps = total/(timeMs*1000);

    if (print) {
      Debug.log("perft", "");
      Debug.log("perft", "Total : " + Value.format(total));
      Debug.log("perft", "Temps : " + Value.format(timeMs) + " ms");
      Debug.log("perft", "Positions par seconde : " + Value.format(mps) + " millions/s");
      Debug.log("perft", "");
    }

    return total;
  }

  public boolean verifyPerft(String fen, int depth, long expected) {
    String previousFen = generateFEN();
    loadFEN(fen);
    long total = perft(depth, true, false);
    boolean success = total == expected;
    String str = fen + " [" + depth + "]";
    if (success) {
      Debug.log("perft", "Test réussi : " + str);
    } else {
      Debug.log("perft", "ÉCHEC : " + str + ". Expected : " + expected + ", Got : " + total);
    }
    loadFEN(previousFen);
    return success;
  }

  public long countMoves(int depth, boolean bulk) {
    if (depth == 0) return 1;

    ArrayList<Move> moves = getLegalMoves();
    if (bulk && depth == 1) {
      return moves.size();
    }

    long count = 0;
    for (int i = 0; i < moves.size(); i++) {
      Move move = moves.get(i);
      make(move);
      count += countMoves(depth-1, bulk);
      unmake(move);
    }
    return count;
  }

  /////////////////////////////////////////////////////////////////

  public Board copy() {
    try {
      ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
      ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
      objectOutput.writeObject(this);
      ByteArrayInputStream byteInput = new ByteArrayInputStream(byteOutput.toByteArray());
      ObjectInputStream objectInput = new ObjectInputStream(byteInput);
      return (Board) objectInput.readObject();
    }
    catch (Exception e) {
      Debug.error("Impossible de copier le plateau : " + e);
      return null;
    }
  }

  @Override
  public String toString() {
    return BoardUtility.boardRepresentation(this);
  }
}
