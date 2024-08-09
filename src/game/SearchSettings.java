/////////////////////////////////////////////////////////////////

// SearchSettings
// Représente les paramètres pour la recherche de coup. Pour
// plus de détails, voir la description dans IA.java

/////////////////////////////////////////////////////////////////

public class SearchSettings {
  public final Search type;
  public final Time time;
  public final int depth;

  private SearchSettings(Time t) {
    type = Search.Iterative;
    time = t;
    depth = 0;
  }

  private SearchSettings(int d) {
    type = Search.Fixed;
    depth = d;
    time = null;
  }

  public static SearchSettings Fixed(int depth) {
    return new SearchSettings(depth);
  }

  public static SearchSettings Iterative(Time time) {
    return new SearchSettings(time);
  }

  public SearchSettings copy() {
    if (type == Search.Fixed) return new SearchSettings(depth);
    else return new SearchSettings(time);
  }
}

enum Search {
  Iterative,
  Fixed
}
