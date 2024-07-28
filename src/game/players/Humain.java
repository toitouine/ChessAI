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
    return "";
  }

  @Override
  public boolean isBot() {
    return false;
  }

  @Override
  public Move play(Board board) {
    return askHumanMove();
  }
}
