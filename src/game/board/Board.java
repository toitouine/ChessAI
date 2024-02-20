/////////////////////////////////////////////////////////////////

// Représente une position à un moment donné dans la partie (pièces, informations, suivi du matériel...)
// Contient également les informations nécessaires pour passer à la position d'avant en annulant un coup
// Note : pour annuler un coup, il est nécessaire de l'annuler dans la position obtenue juste après avoir joué le coup
// Chaque case est représentée par un nombre (de 0 à 63,  8 * ligne + colonne)
// La case en haut à gauche correspond à 0, et celle en bas à droite à 63

// Un plateau peut être utilisé pour une partie ou non (selon la variable playingBoard) pour savoir
// si des sons, sauvegardes etc... doivent être effectués ou non

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
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
  private Piece[] rois = new Piece[2]; // Accès rapide aux rois de la partie (TODO : peut-être uniquement la case des rois)

  // Bitboards (1 si il y a une pièce, 0 si il n'y en a pas)
  // Associe chaque index de case (0 - 63) à un bit (0 pour 2^0, 1 pour 2^1, n pour 2^n)
  public long[] colorBitboard; // Bitboard pour chaque couleur (blanc puis noir)
  public long[] pieceBitboard; // Bitboard pour chaque index de pièce (voir piece.index)

  // Case en passantable sur cette position
  // (une pour les noirs et une pour les blancs pour faciliter l'expiration des cases)
  private Integer[] enPassantSquare = new Integer[2];

  // Sauvegardes pour pouvoir annuler correctement un coup
  public Deque<MoveSave> saves = new ArrayDeque<MoveSave>();

  // Générateur de coups
  private MoveGenerator generator;

  public Board(String fen) {
    colorBitboard = new long[2];
    pieceBitboard = new long[Piece.NumberOfPiece];
    generator = new MoveGenerator(this);
    loadFEN(fen);
  }

  public Board() {
    this(Config.General.defaultFEN);
  }

  /////////////////////////////////////////////////////////////////

  // Crée la position à partir d'une fen
  public void loadFEN(String f) {
    FenManager.loadPosition(this, f);
    calculatePositionData();
  }

  public void loadStartPosition() {
    loadFEN(Config.General.defaultFEN);
  }

  // Génère la fen de la position
  public String generateFEN() {
    return FenManager.generateFEN(this);
  }

  // Renvoie la pièce située sur une case
  public Piece grid(int index) {
    return grid[index];
  }

  // Renvoie la pièce située sur une case (ligne puis colonne)
  public Piece grid(int i, int j) {
    return grid[8*i + j];
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
      if (rois[p.color] != null) Debug.error("Ajout d'un deuxième roi sur le plateau");
      rois[p.color] = p;
    }

    grid[square] = p;
    colorBitboard[p.color] |= 1L << square;
    pieceBitboard[index] |= 1L << square;
    calculatePositionData();
  }

  // Recalcule complètement les données dépendant de la position (hash, phase...)
  private void calculatePositionData() {
    phase = calculatePhase();
    zobrist = Zobrist.calculateHash(this);
  }

  // Calcule le coefficient indiquant la phase de jeu (0 pour l'ouverture et 1 pour la finale)
  public float calculatePhase() {
    phase = 0;

    for (int i = 0; i < Piece.NumberOfType; i++) {
      long bitboard = pieceBitboard[i] | pieceBitboard[i+Piece.NumberOfType];
      phase += Long.bitCount(bitboard) * Config.Piece.phases[i];
    }

    phase = Math.clamp(1 - (phase / Config.Piece.totalPhase), 0, 1);
    return phase;
  }

  // Vide complètement le plateau et réinitialise les variables
  public void clear() {
    for (int i = 0; i < 64; i++) {
      grid[i] = null;
    }

    for (int i = 0; i < 2; i++) {
      rois[i] = null;
      colorBitboard[i] = 0;
      enPassantSquare[i] = null;
    }

    for (long bitboard : pieceBitboard) {
      bitboard = 0;
    }

    tourDeQui = Player.White;
    zobrist = 0;
    phase = 0;
    castleState = 0;

    calculatePositionData();
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
    MoveSave save = new MoveSave(capture, castleState, phase, zobrist, enPassantSquare[opponent]);
    saves.push(save);

    // Déplacement de la pièce
    grid[endSquare] = grid[startSquare];
    grid[startSquare] = null;

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
      colorBitboard[opponent] ^= (1L << capturedSquare);
      pieceBitboard[grid[capturedSquare].index] ^= (1L << capturedSquare);
      grid[capturedSquare] = null;
    }

    // Roques
    // (déplace la tour au bon endroit)
    else if (flag == MoveFlag.PetitRoque) {
      Piece tour = grid[startSquare+3];
      grid[startSquare+1] = tour;
      grid[startSquare+3] = null;
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare+3];
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare+1];
      pieceBitboard[tour.index] ^= (1L << (startSquare+3) | 1L << (startSquare+1));
      colorBitboard[color] ^= (1L << (startSquare+3) | 1L << (startSquare+1));
    }
    else if (flag == MoveFlag.GrandRoque) {
      Piece tour = grid[startSquare-4];
      grid[startSquare-1] = tour;
      grid[startSquare-4] = null;
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare-4];
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare-1];
      pieceBitboard[tour.index] ^= (1L << (startSquare-4) | 1L << (startSquare-1));
      colorBitboard[color] ^= (1L << (startSquare-4) | 1L << (startSquare-1));
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
      phase = calculatePhase();
    }
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
    phase = save.phase;
    zobrist = save.zobrist;
    castleState = save.castleState;
    enPassantSquare[opponent] = save.opponentEnPassant;
    Piece capture = save.capture;

    // Déplacement de la pièce
    // Note : si on est dans le cas d'une promotion, la pièce qui a bougé est la pièce de promotion
    grid[startSquare] = grid[endSquare];
    grid[endSquare] = capture;

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

  /////////////////////////////////////////////////////////////////

  public ArrayList<Move> getLegalMoves() {
    return generator.getLegalMoves();
  }

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
