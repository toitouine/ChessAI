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

  public Case[][] grid = new Case[8][8]; // Représente l'échiquier : ligne puis colonne (ex : b8 --> grid[0][1])
  private ArrayList<Piece>[] pieces; // Pièces des blancs et des noirs
  private Piece[] rois = new Piece[2]; // Accès rapide aux rois de la partie
  private Piece[] currentEnPassantable = {null, null}; // Pions qui peuvent être pris en passant pendant ce tour

  private FenManager fen; // Gère les fens (génération, chargement de position)

  @SuppressWarnings("unchecked")
  public Board() {
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        grid[i][j] = new Case(i, j);
      }
    }

    zobrist = new Zobrist(this);
    fen = new FenManager(this);
    pieces = new ArrayList[2];
    pieces[0] = new ArrayList<Piece>(16);
    pieces[1] = new ArrayList<Piece>(16);
  }

  // Vide complètement le plateau et réinitialise les variables
  public void clear() {
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        grid[i][j].piece = null;
      }
    }
    pieces[0].clear();
    pieces[1].clear();
    rois[0] = rois[1] = null;
    currentEnPassantable[0] = currentEnPassantable[1] = null;
    zobrist.hash = 0;
    tourDeQui = Player.White;
    endGameWeight = 0;
    whitePetitRoque = false;
    whiteGrandRoque = false;
    blackPetitRoque = false;
    blackGrandRoque = false;
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

  // Ajoute la pièce sur le plateau et recalcule les données de la position (lent)
  // Uniquement fait pour des modifications de positions (type éditeur de positions)
  public void addPiece(int type, int i, int j, int c) {
    switch (type) {
      case Piece.Roi:
        Piece roi = new Roi(this, i, j, c);
        rois[c] = roi;
        pieces[c].add(roi);
        break;
      case Piece.Dame:     pieces[c].add(new Dame(this, i, j, c)); break;
      case Piece.Tour:     pieces[c].add(new Tour(this, i, j, c)); break;
      case Piece.Fou:      pieces[c].add(new Fou(this, i, j, c)); break;
      case Piece.Cavalier: pieces[c].add(new Cavalier(this, i, j, c)); break;
      case Piece.Pion:     pieces[c].add(new Pion(this, i, j, c)); break;
      default: Debug.error("Type de pièce invalide");
    }

    calculatePositionData();
  }

  public void addPiece(int index, int i, int j) {
    if (index >= 6) addPiece(index - 6, i, j, Player.Black);
    else addPiece(index, i, j, Player.White);
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
        Piece p = grid[i][j].piece;
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
