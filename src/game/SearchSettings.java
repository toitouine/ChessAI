public class SearchSettings {

  public SearchType type;
  public Time time;
  public int depth;

  public SearchSettings(SearchType type, Time time) {
    this.type = type;
    this.time = time;
  }

  public SearchSettings(SearchType type, int depth) {
    this.type = type;
    this.depth = depth;
  }
}

enum SearchType {
  IterativeDeepening,
  Fixed
}
