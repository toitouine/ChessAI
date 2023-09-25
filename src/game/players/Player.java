abstract public class Player {

  static int White = 0;
  static int Black = 1;

  static int opponent(int player) {
    return (player == White ? Black : White);
  }

  public String name;
  public String pseudo;
  public String elo;
  public String title;
  public String victoryTitle;
  public int ouvertureNumber;
}
