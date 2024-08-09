import java.util.ArrayList;

public class Humain extends Player {

  public Humain(SearchSettings settings) {
    super(settings);
  }

  @Override
  public String elo() {
    return "???";
  }

  @Override
  public String victoryTitle() {
    return "Vous avez gagné !";
  }

  @Override
  public boolean isBot() {
    return false;
  }

  @Override
  public SearchResult play(Board board) {
    return new SearchResult(askHumanMove(board));
  }

  @Override
  public void cancelSearch() {
    stopAskHumanMove();
  }
}
