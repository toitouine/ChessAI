abstract public class Player {

  public static int White = 0;
  public static int Black = 1;

  public String name;
  public String pseudo;
  public String elo;
  public String title;
  public String victoryTitle;
  public int ouvertureNumber;
  public boolean isBot;
  public SearchSettings settings;

  static Player create(String name, SearchSettings s) {
    try {
      Class<?> clazz = Class.forName(name);
      Object instance = clazz.getConstructor(SearchSettings.class).newInstance(s);
      return (Player)instance;
    }
    catch (Exception e) {
      Debug.error("Classe du joueur [" + name + "] introuvable. Ajout d'un humain à la place.");
      return new Humain(s);
    }
  }

  public String description() {
    String desc = name;
    if (settings.type == Search.Iterative) {
      desc += " : Iterative Deepening, " + settings.time.millis() + " ms";
    }
    else if (settings.type == Search.Fixed) {
      desc += " : Profondeur fixe, " + settings.depth;
    }
    return desc;
  }
}
