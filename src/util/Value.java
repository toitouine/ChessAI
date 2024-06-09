import java.text.DecimalFormat;

public final class Value {
  static private DecimalFormat df = new DecimalFormat("###,###.######");

  private Value() {}

  public static String format(long number) {
    return df.format(number);
  }

  public static String format(double number) {
    return df.format(number);
  }
}
