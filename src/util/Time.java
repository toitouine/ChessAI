import processing.core.PApplet;

public class Time {
  private static PApplet sketch;
  private int millis = 0;

  private Time(int millis) {
    this.millis = millis;
  }

  public void setMillis(int m) {
    millis = m;
  }

  public int millis() {
    return millis;
  }

  public float seconds() {
    return (float)millis/1000f;
  }

  public float minutes() {
    return (float)millis/60000f;
  }

  public void add(Time ts) {
    millis += ts.millis();
  }

  public int[] minutesSeconds() {
    int minutes = (int)millis / (int)60000;
    int seconds = (millis % 60000) / 1000; // Valeur arrondie
    int[] time = {minutes, seconds};
    return time;
  }

  static void init(PApplet s) {
    sketch = s;
  }

  static Time getMillis() {
    return new Time(sketch.millis());
  }

  static Time fromMillis(int t) {
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
}
