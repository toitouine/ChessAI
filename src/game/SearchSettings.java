public class SearchSettings {

  public Search type;
  public Time time;
  public int depth;

  public SearchSettings(Search type, Time time) {
    if (type != Search.Iterative) {
      Debug.error("Paramètres de recherche : type de recherche non itérative mais temps fourni");
    }
    this.type = type;
    this.time = time;
  }

  public SearchSettings(Search type, int depth) {
    if (type != Search.Fixed) {
      Debug.error("Paramètres de recherche : type de recherche non fixe mais profondeur fournie");
    }
    this.type = type;
    this.depth = depth;
  }
}

enum Search {
  Iterative,
  Fixed
}
