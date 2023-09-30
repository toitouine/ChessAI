import java.util.ArrayList;

public final class Board {

  public int tourDeQui = Player.White; // Joueur qui doit jouer
  public Zobrist zobrist; // Hash et calculs de hash de la position
  public float nbTour = 0.5f; // Nombre de tour depuis le début de la partie (0.5 ? à expliquer)
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

  // Crée la position à partir d'une fen
  public void loadFEN(String f) {
    fen.loadPosition(f);
    updatePositionData();
  }

  // Génère la fen de la position
  public String generateFEN() {
    return fen.generateFEN();
  }

  public ArrayList<Piece> pieces(int c) {
    return pieces[c];
  }

  // Ajoute la pièce sur le plateau et modifie le hash etc... Plus lent que par l'array
  // Uniquement fait pour des modifications de positions (type éditeur de positions)
  public void addPiece(int type, int i, int j, int c) {
    if (type == Piece.Roi) pieces[c].add(new Roi(this, i, j, c));
    else if (type == Piece.Dame) pieces[c].add(new Dame(this, i, j, c));
    else if (type == Piece.Tour) pieces[c].add(new Tour(this, i, j, c));
    else if (type == Piece.Fou) pieces[c].add(new Fou(this, i, j, c));
    else if (type == Piece.Cavalier) pieces[c].add(new Cavalier(this, i, j, c));
    else if (type == Piece.Pion) pieces[c].add(new Pion(this, i, j, c));
    else Debug.error("Type de pièce invalide");

    updatePositionData();
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
    nbTour = 0.5f;
    endGameWeight = 0;
    whitePetitRoque = false;
    whiteGrandRoque = false;
    blackPetitRoque = false;
    blackGrandRoque = false;
  }

  // Recalcule les données dépendant de la position (hash, endGameWeight...)
  private void updatePositionData() {
    calcEndGameWeight();
    zobrist.calculateHash();
  }

  // A VERIF TODO Calcule le coefficient indiquant la phase de jeu
  private float calcEndGameWeight() {
    float[] totals = {0, 0};

    for (int c = 0; c < 2; c++) {
      for (Piece p : pieces[c]) {
        // Avant : c != Piece.Pion ?!?
        if (p != rois[c] && p.type != Piece.Pion) totals[c] += p.value;
      }
    }

    float val = (totals[0] + totals[1]) / 2;
    val = 1 - (val / 3200f);

    if (val < 0) endGameWeight = 0;
    if (val > 1) endGameWeight = 1;

    return endGameWeight;
  }
}
