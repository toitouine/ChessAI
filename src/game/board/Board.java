/////////////////////////////////////////////////////////////////

// Représente une position à un moment donné dans la partie
// (pièces, informations, historique de positions, suivi du matériel...)
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
  public Zobrist zobrist; // Hash et calculs de hash de la position
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

  private Integer enPassantSquare = null; // Case en passantable sur cette position
  // TODO (ne pas oublier d'ajouter dans clear, calculatePositionData...)
  // int[] materials = new int[2];

  private FenManager fenManager; // Gère les fens (génération, chargement de position)

  public Board() {
    zobrist = new Zobrist(this);
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
    return enPassantSquare;
  }

  /////////////////////////////////////////////////////////////////

  // Ajoute la pièce sur le plateau et recalcule les données de la position (lent)
  // Uniquement fait pour des modifications de positions (type éditeur de positions ou fen)
  public void addPiece(int type, int square, int c) {
    if (grid[square] != null) {
      Debug.error("Ajout d'une pièce par dessus une autre");
      return;
    }

    Piece p = new Piece(type, c);

    if (p.type == Piece.Roi) {
      if (rois[c] != null) Debug.error("Ajout d'un deuxième roi sur le plateau");
      rois[c] = p;
    }

    grid[square] = p;
    colorBitboard[c] |= 1L << square;
    calculatePositionData();
  }

  public void addPiece(int index, int square) {
    if (index >= 6) addPiece(index - 6, square, Player.Black);
    else addPiece(index, square, Player.White);
  }

  // Recalcule les données dépendant de la position (hash, endGameWeight...)
  private void calculatePositionData() {
    calcEndGameWeight();
    zobrist.calculateHash();
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
    }

    tourDeQui = Player.White;
    zobrist.hash = 0;
    endGameWeight = 0;
    enPassantSquare = null;

    calculatePositionData();
  }

  /////////////////////////////////////////////////////////////////

  // Déplace une pièce
  // Note : si on est dans le cas d'une capture, il faut gérer la capture en dehors
  private void movePiece(int pieceSquare, int destination) {
    grid[destination] = grid[pieceSquare];
    grid[pieceSquare] = null;
  }

  // Joue un coup sur le plateau
  public void make(Move move) {
    int startSquare = move.startSquare();
    int endSquare = move.endSquare();
    int flag = move.flag();
    Piece piece = grid[startSquare];
    Piece capture = grid[endSquare];

    // Déplacement et capture
    grid[startSquare] = null;
    grid[endSquare] = piece;

    // Met la case en passantable
    if (flag == MoveFlag.DoubleAvance) enPassantSquare = startSquare + 16*piece.color - 8;

    // Capture le pion pris en passant
    else if (flag == MoveFlag.EnPassant) {
      int capturedSquare = endSquare - 16*piece.color + 8;
      grid[capturedSquare] = null;
    }

    // Roques
    else if (flag == MoveFlag.PetitRoque) {
      petitRoque[piece.color] = false;
      grandRoque[piece.color] = false;
      movePiece(startSquare+3, startSquare+1);
    }
    else if (flag == MoveFlag.GrandRoque) {
      petitRoque[piece.color] = false;
      grandRoque[piece.color] = false;
      movePiece(startSquare-4, startSquare-1);
    }

    // Promotion (TODO: promotion humain)
    else if (MoveFlag.isPromotion(flag)) {
      int type = MoveFlag.getPromotionPieceType(flag);
      grid[endSquare] = new Piece(type, piece.color);
    }

    // Droits au roques
    // Expiration d'une case en passant éventuelle

    // Update : tourDeQui, nbTour, recalculer endGameWeight, materials éventuellement
    // Update : zobrist, et historique de positions
    // Bitboards
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
