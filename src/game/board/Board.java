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

public final class Board {

  // Plateau utilisé pour la partie ou non
  // public boolean playingBoard = false;

  public int tourDeQui = Player.White; // Joueur qui doit jouer
  public float phase = 0; // Représente l'avancement de la partie (0 = ouverture, 1 = finale)
  public long zobrist; // Hash de la position (zobrist)
  private FenManager fenManager; // Gère les fens (génération, chargement de position)

  // Représente les droits au roques (0 si non et 1 si oui, voir les masks pour l'ordre)
  public int castleState;
  private final int[] petitRoqueMask = {0b1000, 0b0010};
  private final int[] grandRoqueMask = {0b0100, 0b0001};

  private Piece[] grid = new Piece[64]; // Représente les pièces sur l'échiquier
  private Piece[] rois = new Piece[2]; // Accès rapide aux rois de la partie (TODO : peut-être uniquement la case des rois)

  // Bitboards (1 si il y a une pièce, 0 si il n'y en a pas)
  // Associe chaque index de case (0 - 63) à un bit (0 pour 2^0, 1 pour 2^1, n pour 2^n)
  private long[] colorBitboard = {0, 0}; // Bitboard pour chaque couleur (blanc puis noir)

  // Case en passantable sur cette position
  // (une pour les noirs et une pour les blancs pour faciliter l'expiration des cases)
  private Integer[] enPassantSquare = new Integer[2];

  public Board() {
    fenManager = new FenManager(this);
    clear();
  }

  /////////////////////////////////////////////////////////////////

  // Crée la position à partir d'une fen
  public void loadFEN(String f) {
    fenManager.loadPosition(f);
    calculatePositionData();
  }

  // Génère la fen de la position
  public String generateFEN() {
    return fenManager.generateFEN();
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
    calculatePositionData();
  }

  // Recalcule complètement les données dépendant de la position (hash, phase...)
  private void calculatePositionData() {
    calculatePhase();
    zobrist = Zobrist.calculateHash(this);
  }

  // Calcule le coefficient indiquant la phase de jeu (0 pour l'ouverture et 1 pour la finale)
  // TODO: compter le matériel avec les bitboards
  private float calculatePhase() {
    phase = 0;

    for (int i = 0; i < 64; i++) {
      if (grid[i] != null) phase += Config.Piece.phases[grid[i].type];
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

    // Déplacement et capture
    grid[endSquare] = grid[startSquare];
    grid[startSquare] = null;
    zobrist ^= Zobrist.piecesOnSquare[piece.index][startSquare]; // XOR out la pièce
    zobrist ^= Zobrist.piecesOnSquare[piece.index][endSquare]; // XOR in la pièce
    if (capture != null) {
      zobrist ^= Zobrist.piecesOnSquare[capture.index][endSquare]; // XOR out la capture
    }

    // Enlève tous les droits du roque du hash
    zobrist ^= Zobrist.castlingRights[castleState];

    // Met la case en passantable
    if (flag == MoveFlag.DoubleAvance) enPassantSquare[color] = startSquare + 16*color - 8;

    // Capture le pion pris en passant
    else if (flag == MoveFlag.EnPassant) {
      int capturedSquare = endSquare - 16*color + 8;
      grid[capturedSquare] = null;
    }

    // Roques
    else if (flag == MoveFlag.PetitRoque) {
      Piece tour = grid[startSquare+3];
      grid[startSquare+1] = tour;
      grid[startSquare+3] = null;
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare+3]; // Enlève la tour de la case de départ
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare+1]; // Place la tour à la case d'arrivée
    }
    else if (flag == MoveFlag.GrandRoque) {
      Piece tour = grid[startSquare-4];
      grid[startSquare-1] = tour;
      grid[startSquare-4] = null;
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare-4]; // Enlève la tour de la case de départ
      zobrist ^= Zobrist.piecesOnSquare[tour.index][startSquare-1]; // Place la tour à la case d'arrivée
    }

    // Promotion
    else if (MoveFlag.isPromotion(flag)) {
      Piece promotion = new Piece(MoveFlag.getPromotionPieceType(flag), color);
      grid[endSquare] = promotion;
      zobrist ^= Zobrist.piecesOnSquare[piece.index][endSquare]; // Retire le pion du hash
      zobrist ^= Zobrist.piecesOnSquare[promotion.index][endSquare]; // Ajoute la pièce de promotion au hash
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
    enPassantSquare[1-color] = null;

    // Changement de tour
    tourDeQui = 1 - tourDeQui;
    zobrist ^= Zobrist.blackToMove;

    // Ajoute les droits du roque du hash
    zobrist ^= Zobrist.castlingRights[castleState];

    // TODO : phase, historique de positions et bitboards
  }

  // Annule un coup sur le plateau
  // Note : doit être fait dans la position obtenue immédiatement après avoir joué le coup
  public void unmake(Move move) {
  }

  @Override
  public String toString() {
    return BoardUtility.boardRepresentation(this);
  }
}
