public final class Config {
  private Config() {}

  static class General {
    // Nom du programme
    static final String name = "Échecs on Java";

    // Position de départ par défaut au lancement du programme
    static final String defaultFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq";

    // Activer (ou pas) les couleurs dans le terminal
    static final boolean terminalColor = true;
  }

  static class UI {
    // Taille d'une case de l'échiquier
    static final int caseWidth = 70;

    // Largeur de la barre verticale à gauche
    static final float offsetX = 95*caseWidth/70;

    // Hauteur de la barre horizontale en haut
    static final float offsetY = 50*caseWidth/70;
  }

  static class IA {
    // Liste des joueurs disponibles
    // (les classes et les images de joueurs doivent avoir le même nom)
    static final String[] players = {"Humain", "LeMaire", "LesMoutons", "Loic", "Antoine", "Stockfish"};
  }

  static class Eval {
    // Valeurs des pièces selon le maire ou loic
    static final float[] maireValues = {100000, 900, 500, 330, 320, 100};
    static final float[] stockfishValues = {100000, 905, 456, 300, 293, 71};
    static final float[] loicValues = {100000, 900, 150, 300, 300, 100};
  }
}
