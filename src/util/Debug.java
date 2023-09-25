import java.util.HashMap;

public final class Debug {
  public static final String RESET = "\u001B[0m";
  public static final String BLACK = "\u001B[30m";
  public static final String RED = "\u001B[31m";
  public static final String GREEN = "\u001B[32m";
  public static final String YELLOW = "\u001B[33m";
  public static final String BLUE = "\u001B[34m";
  public static final String PURPLE = "\u001B[35m";
  public static final String CYAN = "\u001B[36m";
  public static final String WHITE = "\u001B[37m";

  private static HashMap<String, String> colorMap = new HashMap<String, String>();
  private static boolean disableLog = false;
  private static String[] disableTags = {};

  private Debug () {}

  public static void init() {
    colorMap.put("erreur", RED);
    colorMap.put("test", RED);
    colorMap.put("todo", PURPLE);
    colorMap.put("ui", YELLOW);
    colorMap.put("menu", CYAN);
    colorMap.put("éditeur", CYAN);
  }

  public static void println(Object... logs) {
    for (int i = 0; i < logs.length; i++) {
      System.out.print(logs[i] + (i == logs.length-1 ? "" : " " ) );
    }
    System.out.println();
  }

  public static void log(Object... logs) {
    println(logs);
  }

  public static void log(Object message) {
    log("", message);
  }

  public static void log(String tag, Object message) {
    if (disableLog) return;
    for (String s : disableTags) {
      if (s.toUpperCase().equals(tag.toUpperCase())) return;
    }

    String header = "";
    if (Config.General.terminalColor && colorMap.get(tag.toLowerCase()) != null) {
      header = colorMap.get(tag.toLowerCase());
    }
    if (!tag.equals("")) header += "[" + tag.toUpperCase() + "] ";
    System.out.println(header + message + (Config.General.terminalColor ? RESET : ""));
  }
}
