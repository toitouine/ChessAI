/////////////////////////////////////////////////////////////////

// Magic Finder
// Trouve des nombres magiques qui fonctionnent (avec le moins
// de bits possibles) pour générer les coups des sliders
// (voir MoveGenerator.java pour l'explication)
//
// Note : Pour pouvoir utiliser MagicFinder, il faut avoir
// initialisé MoveGenerationData

/////////////////////////////////////////////////////////////////

import java.util.Random;

public final class MagicFinder {

  Random random;

  public MagicFinder() {
    random = new Random();
  }

  public void findAllRookMagics() {
    findAllMagics(true);
  }

  public void findAllBishopMagics() {
    findAllMagics(false);
  }

  // Trouve tous les nombres magiques de la pièce (essaye de chercher
  // les meilleurs possibles), et les affiche, ainsi que le shift correspondant
  private void findAllMagics(boolean isRook) {
    int defaultBits = 12; // Nombre de bits maximum
    Time timeout = Time.fromSeconds(60); // Temps maximum de recherche d'un magique

    // Résultats
    long[] magics = new long[64];
    int[] shifts = new int[64];

    Debug.log("magic", "Recherche de nombres magiques pour " + (isRook ? "la tour" : "le fou"));
    Debug.log("magic", "");

    // Recherche un nombre magique pour chaque case
    for (int square = 0; square < 64; square++) {
      Debug.log("magic", "Recherche pour la case " + BoardUtility.caseName(square) + " :");

      // Recherche le meilleur nombre magique possible
      // (diminue le nombre de bits au fur et à mesure)
      for (int bits = defaultBits; bits > 0; bits--) {
        Long magic = findMagic(square, bits, isRook, timeout);
        if (magic != null) {
          magics[square] = magic;
          shifts[square] = 64 - bits;
          Debug.log("magic", "  - Nouveau magique trouvé avec " + bits + " bits (shift : " + (64 - bits) + ")");
        } else break;
      }
      Debug.log("magic", "");
    }

    Debug.log("magic", "");
    printResults(magics, shifts);
  }

  private void printResults(long[] magics, int[] shifts) {
    Debug.log("magic", "Résultats : Nombres magiques et shifts");
    String magicString = "public static final long[] magics = {";
    for (int square = 0; square < 64; square++) {
      magicString += magics[square] + "L" + (square != 63 ? ", " : "};");
    }
    Debug.log(magicString);

    Debug.log("magic", "");
    String shiftString = "public static final int[] shifts = {";
    for (int square = 0; square < 64; square++) {
      shiftString += shifts[square] + (square != 63 ? ", " : "};");
    }
    Debug.log(shiftString);
  }

  // Cherche un nombre magique sur une case donnée et pour un nombre de
  // bits donné. Fonctionne pour une tour ou un fou selon le paramètre isRook.
  // Si aucun magique n'est trouvé avant timeout, renvoie null
  private Long findMagic(int square, int bits, boolean isRook, Time timeout) {
    Time before = Time.now();
    while (Time.now().millis() - before.millis() < timeout.millis()) {
      long magic = getRandomNumber(); // Récupère un nombre aléatoire
      if (isMagic(magic, bits, square, isRook)) return magic; // Vérifie si il fonctionne
    }
    return null;
  }

  // Vérifie si un nombre est magique si une case donnée, et pour un nombre de bits
  // donné. Fonctionne pour une tour ou un fou selon le paramètre isRook.
  private boolean isMagic(long magic, int bits, int square, boolean isRook) {
    // Récupère le mask
    long mask;
    if (isRook) mask = MoveGenerationData.rookMask(square);
    else mask = MoveGenerationData.bishopMask(square);

    // Tableau des coups (tableau d'objets pour avoir null, il faut utiliser .equals())
    Long[] moveTable = new Long[1 << bits];

    // Énumère tous les arrangements de bloqueurs dans le mask (voir Carry-Rippler trick)
    long blockers = 0;
    do {
      // Récupère les coups et l'index
      long moves;
      if (isRook) moves = MoveGenerationData.getSlowRookMoves(square, blockers);
      else moves = MoveGenerationData.getSlowBishopMoves(square, blockers);
      int index = Magic.index(magic, blockers, 64-bits);

      // Si il n'y a rien à l'index, où si il y a une collision qui nous
      // arrange, alors c'est bon. Sinon, le nombre n'est pas magique :(
      if (moveTable[index] == null) moveTable[index] = moves;
      else if (!moveTable[index].equals(moves)) return false;

      blockers = (blockers - mask) & mask;
    } while (blockers != 0);

    return true; // On a trouvé un nombre magique !
  }

  private long getRandomNumber() {
    return random.nextLong() & random.nextLong() & random.nextLong();
  }
}
