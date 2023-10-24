public class Timer {
  private Time time;
  private Time increment;

  public Timer(Time time, Time increment) {
    this.time = time.copy();
    this.increment = increment.copy();
  }

  public Time getTime() {
    return time;
  }

  public Time getIncrement() {
    return increment;
  }

  public String formattedTime() {
    int[] minSec = time.minutesSeconds();
    String sec = (minSec[1] < 10 ? "0" : "") + String.valueOf(minSec[1]);
    String min = (minSec[0] < 10 ? "0" : "") + String.valueOf(minSec[0]);

    return min + ":" + sec;
  }
}
