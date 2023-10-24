import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;

public final class Clipboard {
  private Clipboard() { }

  public static void copy(String f) {
    StringSelection data = new StringSelection(f);
    java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(data, null);
  }

  public static String paste() {
    try {
      return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    }
    catch (Exception e) {
      Debug.error("Erreur dans le presse papier : paste");
      return "";
    }
  }
}
