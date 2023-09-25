import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.StringSelection;
import processing.core.PSurface;

public final class Clipboard {
  private static Main sketch;

  private Clipboard() { }

  public static void init(Main s) {
    sketch = s;
  }

  public static void copy(String f) {
    StringSelection data = new StringSelection(f);
    java.awt.datatransfer.Clipboard clipboard = getJFrame(sketch.getSurface()).getToolkit().getSystemClipboard();
    clipboard.setContents(data, data);
  }

  public static String paste() {
    String text = (String) GetFromClipboard(DataFlavor.stringFlavor);
    if (text==null) return "";

    return text;
  }

  public static Object GetFromClipboard(DataFlavor flavor) {
    java.awt.datatransfer.Clipboard clipboard = getJFrame(sketch.getSurface()).getToolkit().getSystemClipboard();

    Transferable contents = clipboard.getContents(null);
    Object object = null;

    if (contents != null && contents.isDataFlavorSupported(flavor)) {
      try {
        object = contents.getTransferData(flavor);
      }
      catch (UnsupportedFlavorException e1) {
        Debug.log("erreur", "Clipboard.GetFromClipboard() >> Unsupported flavor: " + e1);
        e1.printStackTrace();
      }
      catch (java.io.IOException e2) {
        Debug.log("erreur", "Clipboard.GetFromClipboard() >> Unavailable data: " + e2);
        e2.printStackTrace() ;
      }
    }

    return object;
  }

  private static final javax.swing.JFrame getJFrame(final PSurface surf) {
    return
      (javax.swing.JFrame)
      ((processing.awt.PSurfaceAWT.SmoothCanvas)
      surf.getNative()).getFrame();
  }
}
