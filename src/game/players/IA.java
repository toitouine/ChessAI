/////////////////////////////////////////////////////////////////

// IA
// Représente les joueurs qui ont une recherche de coup par
// ordinateur. Pour paramétrer la recherche, utiliser la classe
// SearchSettings.

// Il y a deux modes de recherche :
// - Recherche à profondeur fixe : l'ia recherche le meilleur coup
//   avec une profondeur donnée (algorithme en arbre type minimax).
//   Cette recherche doit être implémentée par la classe-fille.
// - Recherche par "iterative deepening" : l'ia commence par rechercher
//   avec une profondeur de 1 (en utilisant la recherche à profondeur
//   fixe décrite au-dessus), puis avec une profondeur de 2, puis de 3,
//   etc.. jusqu'à ce que le temps alloué soit écoulé. À ce moment-là,
//   renvoie le résultat de la dernière recherche complètement complétée.

// Si une ia ne fonctionne pas sur ce principe de recherche, elle peut
// réécrire la méthode play() (par exemple, voir Antoine.java qui joue
// instantanément et au hasard, quels que soient les paramètres).

// Une recherche doit pouvoir être arrêtée le plus vite possible lorsque
// la méthode stopSearch() est appelée. Cette méthode doit être implémentée
// par la classe-fille.

// La recherche renvoie un objet SearchResult avec le coup et éventuellement
// des informations complémentaires, comme l'évaluation par exemple. Cette
// évaluation doit être positive si les blancs ont l'avantage, et négative
// si les noirs ont l'avantage. Elle est comptée en centipions. L'évaluation
// permet aussi d'indiquer qu'un mat a été trouvé si elle vaut la valeur
// SearchResult.MateValue. Pour cette raison, il est recommandé que l'évaluation
// des ias ne monte pas jusqu'à cette valeur si il n'y a pas mat, pour éviter une
// fausse communication de l'évaluation calculée. Voir Evaluation.java pour plus
// de précision sur les évaluations de position.

/////////////////////////////////////////////////////////////////

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

abstract public class IA extends Player {

  private boolean searchCancelled = false;

  public abstract SearchResult search(Board board, int depth);
  protected abstract void stopSearch();
  protected void endOfMove() {}

  protected IA(SearchSettings settings) {
    super(settings);
  }

  @Override
  public boolean isBot() {
    return true;
  }

  @Override
  public SearchResult play(Board board) {
    searchCancelled = false;
    SearchResult result = null;

    if (settings.type == Search.Fixed) {
      result = search(board, settings.depth);
    }

    else if (settings.type == Search.Iterative) {
      result = iterativeDeepening(board, settings.time);
    }

    endOfMove();
    return result;
  }

  protected SearchResult iterativeDeepening(Board board, Time searchTime) {
    searchCancelled = false;
    int depth = 1;
    Time startSearchTime = Time.now();
    SearchResult lastResult = null;

    while (!searchCancelled) {
      final int currentDepth = depth;
      Time maxTime = Time.subtract(searchTime, Time.elapsed(startSearchTime));
      ExecutorService executor = Executors.newCachedThreadPool();
      Callable<SearchResult> task = () -> { return search(board, currentDepth); };
      Future<SearchResult> future = executor.submit(task);

      try {
        // Recherche à une profondeur depth avec un temps maximum maxTime
        lastResult = future.get(maxTime.millis(), TimeUnit.MILLISECONDS);
      }
      catch (Exception e) {
        // Si le temps alloué pour la recherche est écoulé ou qu'une
        // erreur survient, arrête la recherche en cours et renvoie le
        // résultat de la dernière recherche
        stopSearch();
        break;
      }
      finally {
        future.cancel(true);
        executor.shutdownNow();
      }

      depth++;

      // Si jamais la boucle s'est effectuée 200 fois, on considère
      // que la position est évidente, donc on termine la recherche
      if (depth == 200) break;
    }

    return lastResult;
  }

  @Override
  public void cancelSearch() {
    searchCancelled = true;
    stopSearch();
  }

  // Trie les coups par ordre décroissant en fonction des scores (tri par insertion)
  // Note : moves et scores doivent avoir la même taille
  protected void sortMoves(ArrayList<Move> moves, float[] scores) {
    for (int i = 1; i < moves.size(); ++i) {
      float key = scores[i];
      Move moveKey = moves.get(i);
      int j = i - 1;

      while (j >= 0 && scores[j] < key) {
        scores[j+1] = scores[j];
        moves.set(j+1, moves.get(j));
        j -= 1;
      }
      scores[j + 1] = key;
      moves.set(j+1, moveKey);
    }
  }
}
