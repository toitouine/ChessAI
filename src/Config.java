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
    // Nombre de joueurs différents
    static final int number = 6;

    // Index des joueurs
    static final int HUMAIN_INDEX = 0;
    static final int LEMAIRE_INDEX = 1;
    static final int LESMOUTONS_INDEX = 2;
    static final int LOIC_INDEX = 3;
    static final int ANTOINE_INDEX = 4;
    static final int STOCKFISH_INDEX = 5;

    // Nom complet des joueurs
    static final String[] names = {"Humain", "LeMaire", "LesMoutons", "Loic", "Antoine", "Stockfish"};

    // Nom des joueurs en partie
    static final String[] pseudos = {"Humain", "LeMaire", "Mouton", "Loic", "Antoine", "Stockfish"};

    // Nom raccourci des joueurs (utilisé pour les images)
    // static final String[] codes = {"humain", "lemaire", "lesmoutons", "loic", "antoine", "stockfish"};

    // Élo des différentes IAs
    static final String[] elos = {"???", "3845", "1400", "-142", "100", "284"};

    // Titre des différentes IAs
    static final String[] titles = {"", "GM", "Mouton", "IM", "", "Noob"};

    // Nombre maximum de coups du livre d'ouverture
    static final int[] ouvertures = {0, 10, 5, 0, 0, 0};

    // Description de chaque IA
    static final String[] descriptions = {
      "",
      "Très bon en ouverture et en finale",
      "Voleur, arnaqueur, tricheur, menaces en un !!",
      "Plutôt mauvais, préfère pater que mater",
      "Un jeu aléatoire de qualité",
      "Extrêmement difficile de perdre contre lui"
    };

    // Texte de victoire de chaque IA
    static final String[] victories = {
      "",
      "Cmaire",
      "YOU LOUSE",
      "Tu t'es fait mater !",
      "Tu t'es fait mater !",
      "??!?"
    };
  }

  static class Eval {
    // Valeurs des pièces selon le maire ou loic
    static final float[] maireValues = {100000, 900, 500, 330, 320, 100};
    static final float[] stockfishValues = {100000, 905, 456, 300, 293, 71};
    static final float[] loicValues = {100000, 900, 150, 300, 300, 100};
  }
}
