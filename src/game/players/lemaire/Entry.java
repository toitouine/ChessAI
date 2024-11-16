/////////////////////////////////////////////////////////////////

// Entry

// Élément stocké dans la table de transposition.
// Voir TranspositionTable.java pour les précisions

/////////////////////////////////////////////////////////////////

public class Entry {
  public static final byte Exact = 0;
  public static final byte Lowerbound = 1;
  public static final byte Upperbound = 2;

  public final long hash; // Clé de hachage de la position
  public float value; // Evaluation de la position (potentiellement incomplète)
  public final Move bestMove; // Meilleur coup
  public final int depth; // Profondeur (nombre de coups cherchés à partir du coup)
  public final byte nodeType; // Type de noeud (Exact, Lowerbound, Upperbound)

  public Entry(long hash, float value, Move bestMove, int depthAhead, byte type) {
    this.hash = hash;
    this.value = value;
    this.bestMove = bestMove;
    this.depth = depthAhead;
    this.nodeType = type;
  }

  public void setValue(float v) {
    value = v;
  }

  public Entry copy() {
    return new Entry(hash, value, bestMove, depth, nodeType);
  }
}
