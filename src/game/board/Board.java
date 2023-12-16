// Représente une position à un moment donné dans la partie
// Inclut les historiques de positions, le suivi du matériel, etc...

import java.util.ArrayList;

public final class Board {

  public int tourDeQui = Player.White; // Joueur qui doit jouer
  public Zobrist zobrist; // Hash et calculs de hash de la position
  public float endGameWeight = 0; // Représente l'avancement de la partie (0 = ouverture, 1 = finale)

  // Roques
  public boolean whitePetitRoque = false;
  public boolean whiteGrandRoque = false;
  public boolean blackPetitRoque = false;
  public boolean blackGrandRoque = false;

  private Piece[] grid = new Piece[64]; // Représente les pièces sur l'échiquier (8 * ligne + colonne)
  private ArrayList<Piece>[] pieces; // Pièces des blancs et des noirs
  private Piece[] rois = new Piece[2]; // Accès rapide aux rois de la partie

  // Bitboards
  // 1 si il y a une pièce, 0 si il n'y en a pas
  // Associe chaque index de case (0 - 63) à un bit (0 pour 2^0, 1 pour 2^1 etc...)
  private long[] colorBitboard = {0, 0}; // Bitboard pour chaque couleur (0 = blanc, 1 = noir)

  // TODO (ne pas oublier d'ajouter dans clear())
  // private int[] enPassantSquare = {null, null}; // Cases qui peuvent être pris en passant sur cette position
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

  // Vide complètement le plateau et réinitialise les variables
  public void clear() {
    for (int i = 0; i < 64; i++) {
      grid[i] = null;
    }
    pieces[0].clear();
    pieces[1].clear();
    rois[0] = null;
    rois[1] = null;
    tourDeQui = Player.White;
    whitePetitRoque = false;
    whiteGrandRoque = false;
    blackPetitRoque = false;
    blackGrandRoque = false;
    zobrist.hash = 0;
    endGameWeight = 0;
    colorBitboard[0] = 0;
    colorBitboard[1] = 0;

    calculatePositionData();
  }

  // Crée la position à partir d'une fen
  public void loadFEN(String f) {
    fen.loadPosition(f);
    calculatePositionData();
  }

  // Génère la fen de la position
  public String generateFEN() {
    return fen.generateFEN();
  }

  public ArrayList<Piece> pieces(int c) {
    return pieces[c];
  }

  public Piece grid(int index) {
    return grid[index];
  }

  public Piece grid(int i, int j) {
    return grid[8*i + j];
  }

  // Ajoute la pièce sur le plateau et recalcule les données de la position (lent)
  // Uniquement fait pour des modifications de positions (type éditeur de positions ou fen)
  public void addPiece(int type, int square, int c) {
    Piece p = null;

    switch (type) {
      case Piece.Roi:
        if (rois[c] != null) Debug.error("Ajout d'un deuxième roi sur le plateau");
        p = new Roi(square, c);
        rois[c] = p;
        break;

      case Piece.Dame: p = new Dame(square, c); break;
      case Piece.Tour: p = new Tour(square, c); break;
      case Piece.Fou: p = new Fou(square, c); break;
      case Piece.Cavalier: p = new Cavalier(square, c); break;
      case Piece.Pion: p = new Pion(square, c); break;

      default:
        Debug.error("Type de pièce invalide");
    }

    if (p != null) {
      pieces[c].add(p);
      grid[square] = p;
      colorBitboard[c] |= 1L << square;
    }

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

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("     a   b   c   d   e   f   g   h\n");
    str.append("   ┌───┬───┬───┬───┬───┬───┬───┬───┐\n");

    for (int i = 0; i < 8; i++) {
      str.append(" " + (8-i) + " │");
      for (int j = 0; j < 8; j++) {
        Piece p = grid(i, j);
        char c = (p == null ? ' ' : Config.Piece.codes[p.index]);
        str.append(" " + c + " │");
      }
      str.append("\n");
      if (i != 7) str.append("   ├───┼───┼───┼───┼───┼───┼───┼───┤\n");
    }
    str.append("   └───┴───┴───┴───┴───┴───┴───┴───┘\n");

    String roqueString = (whitePetitRoque ? "K" : "") + (whiteGrandRoque ? "Q" : "")
                       + (blackPetitRoque ? "k" : "") + (blackGrandRoque ? "q" : "");
    if (roqueString.equals("")) roqueString = "None";
    str.append("\n   [Trait aux " + (tourDeQui == Player.White ? "blancs" : "noirs") + "]");
    str.append("\n   [Roques : " + roqueString + "]");
    str.append("\n   [Endgame Weight : " + endGameWeight + "]");
    str.append("\n   [Zobrist key : " + zobrist.hash + "]");
    str.append("\n   [FEN : " + fen.generateFEN() + "]\n");
    // TODO: case en passant
    return str.toString();
  }
}
