/////////////////////////////////////////////////////////////////

// Table de transposition

// Probe : Recherche la position dans la table. Si elle est trouvée, la renvoie (sinon null)
// Store : Enregistre la position dans la table, et écrase l'entrée précédente si collision

// Attention :
// La taille de la table de transposition doit être une puissance de 2
// L'index est alors calculé avec : hash & indexMax (où indexMax = size - 1)

/////////////////////////////////////////////////////////////////

public class TranspositionTable {
  private final int size; // Taille de la table
  private final int indexMax; // Index maximal de la table
  private Entry[] entries; // Tableau des entrées de la table

  public TranspositionTable(int bits) {
    size = (int)Math.pow(2, bits);
    indexMax = size-1;
    entries = new Entry[size];
  }

  public Entry probe(long hashKey, int plyFromRoot) {
    // Calcule l'index de la clé
    int ind = calculateIndex(hashKey);

    // Vérifie si l'entrée est présente dans la table
    Entry entry;
    if (entries[ind] != null) entry = entries[ind].copy();
    else return null;

    // Compare avec le hash s'y trouvant
    // (en cas de collision, évite une fausse transposition)
    if (entry.hash == hashKey) {
      entry.setValue(getMateValue(entry.value, plyFromRoot));
      return entry;
    }

    return null;
  }

  public void store(long hashKey, float value, Move move, int depth, int plyFromRoot, byte nodeType) {
    // Calcule l'index de la clé
    int ind = calculateIndex(hashKey);

    // Corrige la valeur si c'est un mat
    float evalToStore = storeMateEval(value, plyFromRoot);

    // Place l'entrée à l'endroit calculé
    entries[ind] = new Entry(hashKey, evalToStore, move, depth, nodeType);
  }

  Move getBestMove(long hash) {
    int ind = calculateIndex(hash);

    Entry entry = entries[ind];
    if (entry == null) return null;

    if (entry.hash == hash) {
      return entry.bestMove;
    }
    return null;
  }

  private int calculateIndex(long hash) {
    long index = hash & indexMax;
    return (int)index;
  }

  public void clear() {
    for (int i = entries.length-1; i >= 0; i--) {
      entries[i] = null;
    }
  }

  public float getFillState() {
    int num = 0;
    for (int i = 0; i < entries.length; i++) {
      if (entries[i] != null) num++;
    }
    return (float)num/size;
  }

  private float storeMateEval(float eval, int ply) {
    int sign = (eval < 0) ? -1 : 1;
    float value = eval * sign;
    value += ply;
    if (value == SearchResult.MateValue) return value * sign;
    else return eval;
  }

  private float getMateValue(float eval, int ply) {
    int sign = (eval < 0) ? -1 : 1;
    float value = eval * sign;
    if (value != SearchResult.MateValue) return eval;
    value -= ply;
    return value * sign;
  }
}
