public class Time {
  private long millis = 0;

  private Time(long millis) {
    this.millis = millis;
  }

  public void set(Time other) {
    millis = other.millis;
  }

  public void add(Time ts) {
    millis += ts.millis();
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

  static Time now() {
    return new Time(System.currentTimeMillis());
  }

  static Time fromMillis(long t) {
    return new Time(t);
  }

  static Time fromSeconds(float t) {
    return new Time(Math.round(1000*t));
  }

  static Time fromMinutes(float t) {
    return new Time(Math.round(60000*t));
  }

  static Time fromMinutesSeconds(int m, float s) {
    return new Time(60000*m + Math.round(1000*s));
  }

  @Override
  public String toString() {
    return "Time[" + millis + " ms]";
  }
}
