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
  private ArrayList<Piece>[] pieces; // Pièces des blancs et des noirs
  private Piece[] rois = new Piece[2]; // Accès rapide aux rois de la partie

  // Bitboards
  // 1 si il y a une pièce, 0 si il n'y en a pas
  // Associe chaque index de case (0 - 63) à un bit (0 pour 2^0, 1 pour 2^1, n pour 2^n)
  private long[] colorBitboard = {0, 0}; // Bitboard pour chaque couleur (blanc puis noir)

  private Integer enPassantSquare = null; // Case en passantable sur cette position
  // TODO (ne pas oublier d'ajouter dans clear, calculatePositionData...)
  // int[] materials = new int[2];

  private FenManager fen; // Gère les fens (génération, chargement de position)

  @SuppressWarnings("unchecked")
  public Board() {
    zobrist = new Zobrist(this);
    fen = new FenManager(this);
    pieces = new ArrayList[2];
    pieces[0] = new ArrayList<Piece>(16);
    pieces[1] = new ArrayList<Piece>(16);
    clear();
  }

  /////////////////////////////////////////////////////////////////

  // Crée la position à partir d'une fen
  public void loadFEN(String f) {
    fen.loadPosition(f);
    calculatePositionData();
  }

  // Génère la fen de la position
  public String generateFEN() {
    return fen.generateFEN();
  }

  // Renvoie la liste des pièces de couleur c
  public ArrayList<Piece> pieces(int c) {
    return pieces[c];
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

    Piece p = Piece.create(type, square, c);
    if (p == null) return;

    if (p.type == Piece.Roi) {
      if (rois[c] != null) Debug.error("Ajout d'un deuxième roi sur le plateau");
      rois[c] = p;
    }

    pieces[c].add(p);
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
  private float calcEndGameWeight() {
    endGameWeight = 0;

    for (int c = 0; c < 2; c++) {
      for (Piece p : pieces[c]) {
        if (p != rois[c] && p.type != Piece.Pion) endGameWeight += p.value/2;
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
      pieces[i].clear();
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
    grid[destination].move(destination);
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
    piece.move(endSquare);
    if (capture != null) pieces[capture.c].remove(capture);

    // Met la case en passantable
    if (flag == MoveFlag.DoubleAvance) enPassantSquare = startSquare + 16*piece.c - 8;

    // Capture le pion pris en passant
    else if (flag == MoveFlag.EnPassant) {
      int capturedSquare = endSquare - 16*piece.c + 8;
      pieces[Player.opponent(piece.c)].remove(grid[capturedSquare]);
      grid[capturedSquare] = null;
    }

    // Roques
    else if (flag == MoveFlag.PetitRoque) {
      petitRoque[piece.c] = false;
      grandRoque[piece.c] = false;
      movePiece(startSquare+3, startSquare+1);
    }
    else if (flag == MoveFlag.GrandRoque) {
      petitRoque[piece.c] = false;
      grandRoque[piece.c] = false;
      movePiece(startSquare-4, startSquare-1);
    }

    // Promotion (TODO: promotion humain)
    else if (MoveFlag.isPromotion(flag)) {
      pieces[piece.c].remove(piece);
      Piece promotion = Piece.promote(flag, endSquare, piece.c);
      pieces[piece.c].add(promotion);
      grid[endSquare] = promotion;
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
