/////////////////////////////////////////////////////////////////

// Table de transposition

// Probe : Recherche la position dans la table. Si elle est trouvée, la renvoie (sinon null)
// Store : Enregistre la position dans la table, et écrase l'entrée précédente si collision
// L'index est calculé avec : hash & indexMax (où indexMax est size-1 et size une puissance de 2)
// (Collision : 1 sur 100 hash environ)

/////////////////////////////////////////////////////////////////

// Constantes
byte EXACT = 0;
byte LOWERBOUND = 1;
byte UPPERBOUND = 2;

class TranspositionTable {

  // Taille de la table de transposition
  int size;

  // Index maximal de la table
  int indexMax;

  // Tableau des entrées de la table
  Entry[] entries;

  TranspositionTable(int size) {
    this.size = size;
    this.indexMax = this.size-1;
    this.entries = new Entry[size];
  }

  Entry Probe(long hashKey, int plyFromRoot) {
    // calcule l'index de la clé
    int ind = this.Index(hashKey);

    Entry entry;
    if (this.entries[ind] != null) entry = this.entries[ind].copy();
    else return null;

    // compare avec le hash s'y trouvant
    if (entry.hash == hashKey) {
      entry.value = retrieveMateValue(entry.value, plyFromRoot);
      return entry;
    }

    return null;
  }

  void Store(long hashKey, float value, Move move, int depth, int plyFromRoot, byte nodeType) {
    // calcule l'index de la clé
    int ind = this.Index(hashKey);

    // corrige la valeur si c'est un mat (50000 ou -50000)
    float evalToStore = this.storeMateEval(value, plyFromRoot);

    // place l'entrée à l'index
    this.entries[ind] = new Entry(hashKey, evalToStore, move, depth, nodeType);
  }

  Move getBestMove(long hash) {
    int ind = this.Index(hash);

    Entry entry = this.entries[ind];
    if (entry == null) return null;

    if (entry.hash == hash) {
      return entry.bestMove;
    }
    return null;
  }

  int Index(long hash) {
    long index = hash & this.indexMax;
    return (int)index;
  }

  void clear() {
    for (int i = this.entries.length-1; i >= 0; i--) {
      this.entries[i] = null;
    }
  }

  int getFillState() {
    int num = 0;
    for (int i = 0; i < this.entries.length; i++) {
      if (this.entries[i] != null) num++;
    }
    return num;
  }

  float storeMateEval(float eval, int ply) {
    int sign = (eval < 0) ? -1 : 1;
    float value = eval * sign;
    value += ply;
    if (value == 50000) return value * sign;
    else return eval;
  }

  float retrieveMateValue(float eval, int ply) {
    int sign = (eval < 0) ? -1 : 1;
    float value = eval * sign;
    if (value != 50000) return eval;
    value -= ply;
    return value * sign;
  }

}

/////////////////////////////////////////////////////////////////

class Entry {

  // Clé de hachage de la position
  long hash;

  // Evaluation de la position (potentiellement incomplète)
  float value;

  // Meilleur coup
  Move bestMove;

  // Profondeur (nombre de coups cherchés à partir du coup)
  int depth;

  // Type de noeud (Exact, Lowerbound, Upperbound)
  byte nodeType;


  Entry(long hashKey, float eval, Move best, int depthAhead, byte type) {
    this.hash = hashKey;
    this.value = eval;
    this.bestMove = best;
    this.depth = depthAhead;
    this.nodeType = type;
  }

  Entry copy() {
    Entry newEntry = new Entry(this.hash, this.value, this.bestMove, this.depth, this.nodeType);
    return newEntry;
  }
}
