public class Time {
  private long millis = 0;

  private Time(long millis) {
    this.millis = millis;
  }

  public Time set(Time other) {
    millis = other.millis;
    return this;
  }

  public Time add(Time ts) {
    millis += ts.millis();
    return this;
  }

  public Time copy() {
    return new Time(millis);
  }

  public long millis() {
    return millis;
  }

  public float seconds() {
    return (float)millis/1000f;
  }

  public float minutes() {
    return (float)millis/60000f;
  }

  public int[] minutesSeconds() {
    int minutes = (int)millis / (int)60000;
    int seconds = (int)(millis % 60000) / 1000; // Valeur arrondie
    int[] time = {minutes, seconds};
    return time;
  }

  public static Time now() {
    return new Time(System.currentTimeMillis());
  }

  public static Time subtract(Time a, Time b) {
    return Time.fromMillis(a.millis() - b.millis());
  }

  public static Time elapsed(Time a) {
    return Time.subtract(Time.now(), a);
  }

  public static Time fromMillis(long t) {
    return new Time(t);
  }

  public static Time fromSeconds(float t) {
    return new Time(Math.round(1000*t));
  }

  public static Time fromMinutes(float t) {
    return new Time(Math.round(60000*t));
  }

  public static Time fromMinutesSeconds(int m, float s) {
    return new Time(60000*m + Math.round(1000*s));
  }

  @Override
  public String toString() {
    return "Time[" + millis + " ms]";
  }
}
