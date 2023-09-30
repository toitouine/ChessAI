abstract public class Player {

  static int White = 0;
  static int Black = 1;

  static int opponent(int player) {
    return (player == White ? Black : White);
  }

  static Player create(String name, SearchSettings s) {
    try {
      Class<?> clazz = Class.forName(name);
      Object instance = clazz.getConstructor(SearchSettings.class).newInstance(s);
      return (Player)instance;
    }
    catch (Exception e) {
      Debug.error("Classe du joueur introuvable ! Ajout d'un humain à la place.");
      return new Humain(s);
    }
  }

  public String name;
  public String pseudo;
  public String elo;
  public String title;
  public String victoryTitle;
  public int ouvertureNumber;
  public boolean isBot = true;
  protected SearchSettings settings;
}
