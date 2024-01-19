import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Test {
  private Test() {}

  static public void measure(Callback function) {
    measure(function, 0);
  }

  static public void measureIterations(Callback function, long iterations) {
    iterations(function, iterations);
    Debug.log("test", "───────────────────");
  }

  static public void measure(Callback function, long iterations) {
    Debug.log();
    Debug.log("test", "Démarrage du test");

    ArrayList<Long> itsPerSeconds = new ArrayList<Long>();

    if (iterations > 0) itsPerSeconds.add(iterations(function, iterations));
    itsPerSeconds.add(duringTime(function, Time.fromSeconds(1)));
    itsPerSeconds.add(duringTime(function, Time.fromSeconds(5)));
    itsPerSeconds.add(duringTime(function, Time.fromSeconds(10)));

    // Calcul de la moyenne
    long moyenne = 0;
    for (Long value : itsPerSeconds) moyenne += value;
    moyenne /= itsPerSeconds.size();

    // Calcul de l'incertitude
    long incertitude = 0;
    for (Long value : itsPerSeconds) incertitude += Math.pow((value - moyenne), 2);
    incertitude /= (itsPerSeconds.size()-1);
    incertitude = (long)Math.sqrt(incertitude);
    incertitude = (long)(incertitude / Math.sqrt(itsPerSeconds.size()));

    Debug.log("test", "───────────────────");
    Debug.log("test", "Résultat : " + String.format("%,d", moyenne) + " ± " + String.format("%,d", incertitude) + " itérations par seconde");
    Debug.log("test", "(environ " + String.format("%,f", 1/(float)moyenne*1000000000L) + " nanosecondes par itération)");
    Debug.log();
  }

  // Mesure le temps d'éxecution de la fonction pendant un certain nombre d'itérations
  // Renvoie le nombre d'itérations par seconde
  private static long iterations(Callback function, long iterations) {
    Debug.log("test", "───────────────────");
    Debug.log("test", "Avec " + String.format("%,d", iterations) + " itérations :");

    long before = System.nanoTime();
    for (long i = 0; i < iterations; i++) {
      function.call();
    }
    long timeNano = System.nanoTime() - before;

    long itPerSeconds = 1000000000*iterations/timeNano;
    Debug.log("test", "Temps : " + String.format("%,f", (float)timeNano/1000000) + " ms");
    Debug.log("test", "⟶  " + String.format("%,d", itPerSeconds) + " itérations par seconde");
    return itPerSeconds;
  }

  // Mesure le nombre d'éxecutions de la fonction pendant un certain temps
  // Renvoie le nombre d'itérations par seconde
  private static long duringTime(Callback function, Time time) {
    Debug.log("test", "───────────────────");
    Debug.log("test", "En " + time.millis() + " ms :");

    long count = 0;

    AtomicBoolean go = new AtomicBoolean(true);
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.schedule(() -> go.set(false), time.millis(), TimeUnit.MILLISECONDS);

    while (go.get()) {
      function.call();
      count++;
    }

    long itPerSeconds = 1000*count / time.millis();
    Debug.log("test", "Itérations : " + String.format("%,d", count));
    Debug.log("test", "⟶  " + String.format("%,d", itPerSeconds) + " itérations par seconde");
    return itPerSeconds;
  }
}
