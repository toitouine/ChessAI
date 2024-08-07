/////////////////////////////////////////////////////////////////

// Timer
// Représente les pendules des joueurs. Pour la démarrer, appeler
// la méthode start(), pour mettre pause, appeler pause() et pour
// reprendre, resume(). L'incrément est ajouté avec la pause de
// la pendule.

/////////////////////////////////////////////////////////////////

public class Timer {
  private final Time time;
  private final Time increment;

  private Time timeAtStart = null;
  private final Time totalTimeInPause = Time.fromMillis(0);
  private final Time totalIncremented = Time.fromMillis(0);
  private Time timeAtLastPause;
  private Time remainingAtLastPause;
  private boolean inPaused = false;

  public Timer(Time time, Time increment) {
    this.time = time.copy();
    this.increment = increment.copy();
  }

  public static Timer fromMillis(long timeMs, long incMs) {
    return new Timer(Time.fromMillis(timeMs), Time.fromMillis(incMs));
  }

  public Time getTotalTime() {
    return time;
  }

  public Time getIncrement() {
    return increment;
  }

  /////////////////////////////////////////////////////////////////

  public void start() {
    timeAtStart = Time.now();
    inPaused = false;
  }

  public void resume() {
    if (inPaused) totalTimeInPause.add(Time.elapsed(timeAtLastPause));
    inPaused = false;
  }

  public void pause(boolean withInc) {
    if (inPaused) return;

    timeAtLastPause = Time.now();
    if (withInc) totalIncremented.add(increment);
    remainingAtLastPause = timeRemaining();
    inPaused = true;
  }

  public void pause() {
    pause(true);
  }

  /////////////////////////////////////////////////////////////////

  public Time timeRemaining() {
    if (timeAtStart == null) return time.copy();
    if (inPaused) return remainingAtLastPause.copy();

    Time elapsedTime = Time.subtract(Time.elapsed(timeAtStart), totalTimeInPause);
    Time remaining = Time.subtract(time, elapsedTime).add(totalIncremented);
    if (remaining.millis() <= 0) return Time.fromMillis(0);
    return remaining;
  }

  public String formattedTime() {
    Time remaining = timeRemaining();
    int[] minSec = remaining.minutesSeconds();
    String sec = (minSec[1] < 10 ? "0" : "") + String.valueOf(minSec[1]);
    String min = (minSec[0] < 10 ? "0" : "") + String.valueOf(minSec[0]);

    if (minSec[0] != 0 || minSec[1] >= 10) return min + ":" + sec;

    int seconds = minSec[1];
    long millis = remaining.millis() - seconds*1000;
    return seconds + "." + (millis/100);
  }

  public Timer copy() {
    return new Timer(time.copy(), increment.copy());
  }
}
