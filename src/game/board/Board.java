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
  public float endGameWeight = 0; // Représente l'avancement de la partie (0 = ouverture, 1 = finale)

  // Roques (blanc puis noir)
  public boolean[] petitRoque = {false, false};
  public boolean[] grandRoque = {false, false};

  private Piece[] grid = new Piece[64]; // Représente les pièces sur l'échiquier
  private Piece[] rois = new Piece[2]; // Accès rapide aux rois de la partie (TODO : peut-être uniquement la case des rois)

  // Bitboards
  // 1 si il y a une pièce, 0 si il n'y en a pas
  // Associe chaque index de case (0 - 63) à un bit (0 pour 2^0, 1 pour 2^1, n pour 2^n)
  private long[] colorBitboard = {0, 0}; // Bitboard pour chaque couleur (blanc puis noir)

  public long zobrist; // Hash de la position (zobrist)

  // Case en passantable sur cette position
  // (une pour les noirs et une pour les blancs pour faciliter l'expiration des cases)
  private Integer[] enPassantSquare = new Integer[2];

  // Cases de départ des tours pour le petit et grand roque (blanc et noir)
  private final int[] petitRoqueSquare = {63, 7};
  private final int[] grandRoqueSquare = {56, 0};

  private FenManager fenManager; // Gère les fens (génération, chargement de position)

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

  // Renvoie si la case square est la case de départ de la tour de petit roque du joueur color
  private boolean isPetitRoqueSquare(int square, int color) {
    return petitRoqueSquare[color] == square;
  }

  // Renvoie si la case square est la case de départ de la tour de grand roque du joueur color
  private boolean isGrandRoqueSquare(int square, int color) {
    return grandRoqueSquare[color] == square;
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

  // Recalcule complètement les données dépendant de la position (hash, endGameWeight...)
  private void calculatePositionData() {
    calcEndGameWeight();
    zobrist = Zobrist.calculateHash(this);
  }

  // Calcule le coefficient indiquant la phase de jeu (0 pour l'ouverture et 1 pour la finale)
  // TODO: compter le matériel avec les bitboards
  private float calcEndGameWeight() {
    endGameWeight = 0;

    for (int i = 0; i < 64; i++) {
      if (grid[i] != null && grid[i].type != Piece.Pion && grid[i].type != Piece.Roi) {
        endGameWeight += Config.Piece.maireValues[grid[i].type]/2;
      }
    }

    endGameWeight = 1 - (endGameWeight / 3200f);
    if (endGameWeight < 0) endGameWeight = 0;
    else if (endGameWeight > 1) endGameWeight = 1;
    return endGameWeight;
  }

  // Vide complètement le plateau et réinitialise les variables
  public void clear() {
    for (int i = 0; i < 64; i++) {
      grid[i] = null;
    }

    for (int i = 0; i < 2; i++) {
      rois[i] = null;
      colorBitboard[i] = 0;
      petitRoque[i] = false;
      grandRoque[i] = false;
      enPassantSquare[i] = null;
    }

    tourDeQui = Player.White;
    zobrist = 0;
    endGameWeight = 0;

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
    zobrist ^= Zobrist.getCastlingKey(petitRoque, grandRoque);

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

    // Droits au roques
    if (petitRoque[color] || grandRoque[color]) {
      if (piece.type == Piece.Roi) {
        petitRoque[color] = false;
        grandRoque[color] = false;
      }
      else if (piece.type == Piece.Tour) {
        if (isPetitRoqueSquare(startSquare, color)) petitRoque[color] = false;
        else if (isGrandRoqueSquare(startSquare, color)) grandRoque[color] = false;
      }
    }

    // Expiration d'une case en passant éventuelle
    enPassantSquare[1-color] = null;

    // Changement de tour
    tourDeQui = 1 - tourDeQui;
    zobrist ^= Zobrist.blackToMove;

    // Ajoute les droits du roque du hash
    zobrist ^= Zobrist.getCastlingKey(petitRoque, grandRoque);

    // TODO : endGameWeight, zobrist, historique de positions et bitboards
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
