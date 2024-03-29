public final class Config {
  private Config() {}

  static class General {
    // Nom du programme
    static final String name = "Échecs on Java";

    // Position de départ par défaut au lancement du programme (utilisée en cas d'erreurs de fen)
    static final String defaultFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq";

    // Désactiver les logs dans le terminal
    static final boolean disableLogs = false;

    // Activer (ou pas) les couleurs dans le terminal
    static final boolean terminalColor = true;

    // Nombre maximum de parties jouables simultanément
    static final int maximumGames = 1;
  }

  static class UI {
    // Taille d'une case de l'échiquier
    static final int caseWidth = 70;

    // Largeur de la barre verticale à gauche
    static final float offsetX = 95*caseWidth/70;

    // Hauteur de la barre horizontale en haut
    static final float offsetY = 50*caseWidth/70;

    // Chemin de l'image par défaut (si une image est introuvable/manquante)
    static final String defaultImage = "data/icons/notfound.png";
  }

  static class IA {
    // Liste des joueurs disponibles
    // (les classes et les images de joueurs doivent avoir le même nom)
    static final String[] players = {"Humain", "LeMaire", "LesMoutons", "Loic", "Antoine", "Stockfish"};
  }

  static class Piece {
    // Ordre : Roi, Dame, Tour, Fou, Cavalier, Pion

    // Code pour chaque pièces
    static final char[] codes = {'K', 'Q', 'R', 'B', 'N', 'P', 'k', 'q', 'r', 'b', 'n', 'p'};

    // Phase de chaque type de pièce (à quel point la pièce affecte la phase du jeu)
    // Ex : la capture d'une dame fait beaucoup avancer le jeu vers la finale, alors que la capture d'un pion non
    static final int[] phases = {0, 6, 3, 2, 2, 0};

    // Représente la somme des phases de chaque pièce au début de la partie
    static final int totalPhase = 2*phases[1] + 4*(phases[2] + phases[3] + phases[4]) + 16*phases[5];

    // Valeurs des pièces
    static final float[] maireValues = {100000, 900, 500, 330, 320, 100};
    static final float[] loicValues = {100000, 900, 150, 300, 300, 100};
  }
}
