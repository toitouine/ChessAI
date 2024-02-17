import java.util.HashMap;

public final class Debug {

  private Debug() {}

  private static final String RESET = "\u001B[0m";
  private static final String BLACK = "\u001B[30m";
  private static final String RED = "\u001B[31m";
  private static final String GREEN = "\u001B[32m";
  private static final String YELLOW = "\u001B[33m";
  private static final String BLUE = "\u001B[34m";
  private static final String PURPLE = "\u001B[35m";
  private static final String CYAN = "\u001B[36m";
  private static final String WHITE = "\u001B[37m";

  private static HashMap<String, String> colorMap = new HashMap<String, String>();
  private static boolean disableLogs;
  private static String[] disableTags = {"ui"};

  static {
    colorMap.put("erreur", RED);
    colorMap.put("test", RED);
    colorMap.put("todo", PURPLE);
    colorMap.put("ui", GREEN);
    colorMap.put("game", CYAN);
    disableLogs = Config.General.disableLogs;
  }

  public static void println(Object... logs) {
    if (disableLogs) return;
    for (int i = 0; i < logs.length; i++) {
      System.out.print(logs[i] + (i == logs.length-1 ? "" : " " ) );
    }
    System.out.println();
  }

  public static void error(Object message) {
    log("erreur", message);
  }

  public static void log(Object... logs) {
    println(logs);
  }

  public static void log(Object message) {
    log("", message);
  }

  public static void log(String tag, Object message) {
    if (disableLogs) return;
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

  public static void printBinary(int binary) {
    System.out.println(Integer.toBinaryString(0xFFFF & binary));
  }

  public static void printBinary(long binary) {
    System.out.println(Long.toBinaryString(binary));
  }

  public static void printBinary(int binary, int minDigit) {
    String s = String.format("%" + minDigit + "s", Integer.toBinaryString(0xFFFF & binary)).replace(' ', '0');
    System.out.println(s);
  }

  public static void printBinary(long binary, int minDigit) {
    String s = String.format("%" + minDigit + "s", Long.toBinaryString(binary)).replace(' ', '0');
    System.out.println(s);
  }
}
