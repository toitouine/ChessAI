/////////////////////////////////////////////////////////////////

// Search controller (contrôle les paramètres des recherches et temps pour iterative deepening)

public class SearchApplet extends PApplet {
  int sizeW = 503, sizeH = 198;
  boolean show = true;

  // -1 : aucun, 0 : blanc, 1 : noir
  int inSearch = -1;
  int searchStartTime;
  int[] savedTimes = {0, 0};
  int[] times = {0, 0};

  String[] evals = {"0", "0"};
  String[] depths = {"0", "0"};
  String[] positions = {"0", "0"};
  String[] tris = {"0", "0"};
  String[] transpositions = {"0", "0"};
  String[] timesDisplay = {"0", "0"};

  public void settings() {
    size(sizeW, sizeH);
  }

  public void setup() {
    background(#272522);
    surface.setSize(sizeW, sizeH);
    surface.setLocation(displayWidth-sizeW, gameHeight+46);
    surface.setTitle("Search controller");
    surface.setFrameRate(30);
    surface.setAlwaysOnTop(attach);
  }

  public void draw() {

    if (this.inSearch != -1) {
      if (millis() - this.searchStartTime >= this.times[this.inSearch]) {
        this.inSearch = -1;
        stopSearch = true;
      }
    }

    if (!show) return;
    surface.setAlwaysOnTop(attach);

    background(#272522);

    // Ligne de séparation
    stroke(#524d48);
    line(width/2, 20, width/2, height-20);

    // Nom des joueurs
    fill(255);
    textSize(23);
    textAlign(CENTER, CENTER);
    if (joueurs == null || joueurs.size() < 2) return;
    if (!joueurs.get(0).name.equals("Humain")) text(joueurs.get(0).name + " (blancs)", width/4, 27);
    else text(joueurs.get(0).name + " (blancs)", width/4, height/2);

    if (joueurs == null || joueurs.size() < 2) return;
    if (!joueurs.get(1).name.equals("Humain")) text(joueurs.get(1).name + " (noirs)", (3*width)/4, 27);
    else text(joueurs.get(1).name + " (noirs)", (3*width)/4, height/2);

    // Stats
    textSize(17);
    textAlign(LEFT, CENTER);

    for (int i = 0; i < 2; i++) {
      if (joueurs != null && joueurs.size() < 2) break;
      if (!gameEnded && !joueurs.get(i).name.equals("Humain")) {
        fill(#fbd156); text("Evaluation : " + evals[i], i*width/2 + 8, 65);
        fill(#ef5a2a); text("Profondeur : " + depths[i], i*width/2 + 8, 89);
        fill(#5c8cb1); text("Positions : " + positions[i] + " (" + tris[i] + ")", i*width/2 + 8, 113);
        fill(#93b46b); text("Transpositions : " + transpositions[i], i*width/2 + 8, 137);
        fill(#abb88a); text("Temps : " + timesDisplay[i], i*width/2 + 8, 161);
      }
    }
  }

  public void startSearch(int c) {
    this.inSearch = c;
    this.searchStartTime = millis();
  }

  public void endSearch() {
    this.inSearch = -1;
    stopSearch = true;
  }

  public void setTimes(int timeForSearch1, int timeForSearch2) {
    this.times[0] = timeForSearch1;
    this.times[1] = timeForSearch2;
    this.savedTimes[0] = timeForSearch1;
    this.savedTimes[1] = timeForSearch2;
  }

  public void setTime(int c, int time) {
    this.times[c] = time;
  }

  public int getTime(int c) {
    return this.times[c];
  }

  public void setEvals(String eval, int c) {
    evals[c] = eval;
  }

  public void setDepths(String depth, int c) {
    depths[c] = depth;
  }

  public void setPositions(String pos, int c) {
    positions[c] = pos;
  }

  public void setTris(String tri, int c) {
    tris[c] = tri;
  }

  public void setTranspositions(String transpo, int c) {
    transpositions[c] = transpo;
  }

  public void setTimeDisplays(String timeDisplay, int c) {
    timesDisplay[c] = timeDisplay;
  }

  public void reset() {
    for (int i = 0; i < 2; i++) {
      evals[i] = "0";
      depths[i] = "0";
      positions[i] = "0";
      tris[i] = "0";
      transpositions[i] = "0";
    }
  }

  public void show() {
    surface.setVisible(true);
    show = true;
  }

  public void hide() {
    surface.setVisible(false);
    show = false;
  }

  PSurface initSurface() {
    PSurface pSurface = super.initSurface();
    PSurfaceAWT awtSurface = (PSurfaceAWT) surface;
    SmoothCanvas smoothCanvas = (SmoothCanvas) awtSurface.getNative();
    Frame frame = smoothCanvas.getFrame();
    frame.setUndecorated(true);
    return pSurface;
  }
}

/////////////////////////////////////////////////////////////////

// Graph applet (graphique de l'évaluation)

public class GraphApplet extends PApplet {
  int graphW = 500, graphH = 480;
  int sizeW = graphW + 50, sizeH = graphH + 60;
  boolean show = false;
  Graph g;

  public void settings() {
    size(sizeW, sizeH);
  }

  public void setup() {
    background(#272522);
    surface.setLocation(0, 23);
    surface.setTitle("Analyse");
    surface.setFrameRate(5);
  }

  public void draw() {
    background(#272522);
    if (show) {
      g.plot();
    }
  }

  public void mouseDragged() {
    surface.setFrameRate(60);
    Point mouse;
    mouse = MouseInfo.getPointerInfo().getLocation();
    surface.setLocation(mouse.x - sizeW/2, mouse.y - sizeH/2);
  }

  public void mouseReleased() {
    surface.setFrameRate(5);
  }

  PSurface initSurface() {
    PSurface pSurface = super.initSurface();
    PSurfaceAWT awtSurface = (PSurfaceAWT) surface;
    SmoothCanvas smoothCanvas = (SmoothCanvas) awtSurface.getNative();
    Frame frame = smoothCanvas.getFrame();
    frame.setUndecorated(true);
    return pSurface;
  }

  public void show() {
    surface.setVisible(true);
  }

  public void hide() {
    surface.setVisible(false);
  }

  public void disableGraph() {
    g = null;
    show = false;
    this.hide();
  }

  public void initGraph() {
    g = new Graph(10, 10);
    show = true;
    this.show();
  }

  public void clearGraph() {
    if (g != null) g.reset();
  }

  public void sendData(float[] xs, float[] ys, int c, String legende) {
    g.addValues(xs, ys, c, legende);
  }

  public class Graph {
    int x = 0, y = 0;
    int w = 500, h = 480;
    int xPas = 0, yPas = 10;
    int mateOffset = 200;
    ArrayList<float[]> allOrdonnes = new ArrayList<float[]>();
    ArrayList<float[]> allAbscisses = new ArrayList<float[]>();
    ArrayList<Integer> colors = new ArrayList<Integer>();
    ArrayList<String> legendes = new ArrayList<String>();

    Graph(int x, int y) {
      this.x = x;
      this.y = y;
    }

    void reset() {
      this.allOrdonnes.clear();
      this.allAbscisses.clear();
      this.colors.clear();
      this.legendes.clear();
    }

    void addValues(float[] xs, float[] ys, int c, String legende) {
      float[] abscisses = xs;
      float[] ordonnees = ys;
      this.allAbscisses.add(abscisses);
      this.allOrdonnes.add(ordonnees);
      this.colors.add(c);
      this.legendes.add(legende);

      int indexReference = 0;
      for (int i = 0; i < this.allAbscisses.size(); i++) {
        if (this.allAbscisses.get(i).length > this.allAbscisses.get(indexReference).length) indexReference = i;
      }
      if (this.allAbscisses.get(indexReference).length > 1) this.xPas = this.w / (this.allAbscisses.get(indexReference).length-1);
      else xPas = this.w / 2;
    }

    int isMateValue(float value) {
      // Retourne -1 si aucun des deux, 0 si pour les blancs et 1 si pour les noirs
      if (abs(500 - value) <= 1) return 0;
      if (abs(-500 - value) <= 1) return 1;
      return -1;
    }

    float getOrdonne(float val) {
      int mate = this.isMateValue(val);
      if (mate != -1) return (mate == 0) ? 24 : -24;
      else if (val >= 20) return 20;
      else if (val <= -20) return -20;
      return val;
    }

    void legende() {
      float ecartEnPlus = 30;
      float ecart = (this.w) / (this.legendes.size()+1) + ecartEnPlus;
      float lineSize = 35;
      float distanceLineText = 10;
      float yDistance = this.y + this.h + 25;

      for (int i = 0; i < this.legendes.size(); i++) {
        float totalDistance = lineSize + distanceLineText + textWidth(this.legendes.get(i));
        float centerOfPoint = this.x + ecart * (i+1) - ecartEnPlus;
        if (this.colors.size() > 0 && this.colors.size() > i) stroke(this.colors.get(i));
        strokeWeight(3);
        line(centerOfPoint - totalDistance/2, yDistance, centerOfPoint - totalDistance/2 + lineSize, yDistance);
        textAlign(LEFT, CENTER);
        textSize(14);
        if (this.legendes.size() > 0 && this.legendes.size() > i) text(this.legendes.get(i), centerOfPoint - totalDistance/2 + lineSize + distanceLineText, yDistance-2);
      }
    }

    void plot() {
      push();

      translate(this.x, this.y+this.h/2);
      noStroke();
      fill(#32302d);
      rect(0, -this.h/2, this.w, this.h);
      stroke(#383434);
      strokeWeight(3);
      line(0, 0, this.w-3, 0);

      strokeWeight(1);
      textSize(13);
      for (int i = this.mateOffset; i >= -this.mateOffset; i -= 2*yPas) {
        textAlign(CENTER, CENTER);
        fill(#706f6d);
        text(-i/yPas, this.w + 20, i-2);

        strokeWeight(1);
        stroke(#383434);
        line(0, i, this.w-1, i);
      }
      text("MAT", this.w + 20, this.mateOffset + 3.5*yPas);
      text("MAT", this.w + 20, -this.mateOffset - 3.5*yPas);

      for (int i = 0; i < this.w; i += 2*xPas) line(i, -this.h/2, i, this.h/2-1);

      for (int n = 0; n < this.allAbscisses.size(); n++) {
        float[] currentAbs = this.allAbscisses.get(n);
        float[] currentOrd = this.allOrdonnes.get(n);

        for (int i = 0; i < currentAbs.length; i++) {
          if (i != 0) {
            float prevOrd = this.getOrdonne(currentOrd[i-1]);
            float ord = this.getOrdonne(currentOrd[i]);
            strokeWeight(3);
            if (this.colors.size() > 0 && this.colors.size() > n) stroke(this.colors.get(n)); // Condition pour éviter une erreur si on est en train de clear colors dans l'applet principal
            line(currentAbs[i-1] * xPas, -prevOrd * yPas, currentAbs[i] * xPas, -ord * yPas);
          }
        }
      }

      pop();

      this.legende();
    }
  }
}

void updateGraph() {
  ga.clearGraph();
  sendValuesToGraph();
}

void activateGraph() {
  ga.initGraph();
  sendValuesToGraph();
  delay(3);
  surface.setVisible(true);
}

void sendValuesToGraph() {
  int[] colors = {#5c8cb1, #b33430};

  for (int n = 0; n < joueurs.size(); n++) {
    if (joueurs.get(n).evals.size() > 0) {
      float[] x = new float[joueurs.get(n).evals.size()];
      float[] y = new float[joueurs.get(n).evals.size()];
      for (int i = 0; i < y.length; i++) x[i] = i;
      for (int i = 0; i < y.length; i++) y[i] = joueurs.get(n).evals.get(i);
      String name = joueurs.get(n).name + " (" + (joueurs.get(n).c == 0 ? "Blancs" : "Noirs") + ")";
      ga.sendData(x, y, colors[n], name);
    }
  }
}

void disableGraph() {
  ga.disableGraph();
}

/////////////////////////////////////////////////////////////////

// Timer applet (gestion du temps des pendules)

public class TimerApplet extends PApplet {
  Timer[] timers = new Timer[2];
  boolean show = false;
  int windowWidth = 150;
  int windowHeight = 200;

  int upY = 50;
  int downY = windowHeight-upY;
  int timersX = windowWidth/2, timersTextSize = 30;

  int rate = 15;

  public void settings() {
    size(windowWidth, windowHeight);
  }

  public void setup() {
    background(49, 46, 43);
    surface.setLocation(displayWidth - (gameWidth + width), 45 + offsetY + 4*w - windowHeight/2);
    surface.setTitle("Pendules");
    surface.setAlwaysOnTop(attach);
    surface.setFrameRate(rate);

    initTimers();
  }

  public void draw() {

    if (!show) return;

    background(49, 46, 43);
    surface.setAlwaysOnTop(attach);

    for (int i = 0; i < timers.length; i++) {
      timers[i].update();
      if (pointDeVue) timers[i].show(timersX, (i == 0) ? downY : upY, timersTextSize);
      else timers[i].show(timersX, (i == 0) ? upY : downY, timersTextSize);
    }

    if (!gameEnded && play) checkTimes();
  }

  public void show() {
    show = true;
    surface.setVisible(true);
  }

  public void hide() {
    show = false;
    surface.setVisible(false);
  }

  public void goToHackerPosition() {
    surface.setLocation(displayWidth - gameWidth, displayHeight-height-51);
  }

  public void goToDefaultPosition() {
    surface.setLocation(displayWidth - (gameWidth + width), 45 + offsetY + 4*w - windowHeight/2);
  }

  public void mouseDragged() {
    // surface.setFrameRate(60);
    Point mouse;
    mouse = MouseInfo.getPointerInfo().getLocation();
    surface.setLocation(mouse.x - windowWidth/2, mouse.y - windowHeight/2);
  }

  public void mouseReleased() {
    // surface.setFrameRate(30);
  }

  public void switchTimers(int toward) {
    timers[(int)pow(toward-1, 2)].pause();
    timers[toward].resume();
  }

  public void checkTimes() {
    if (timers[0].currentTime <= 0) loseOnTime(0);
    if (timers[1].currentTime <= 1) loseOnTime(1);
  }

  public void startTimers(int startOne) {
    timers[startOne].resume();
  }

  public void pauseTimers() {
    timers[0].pause();
    timers[1].pause();
  }

  public void stopTimers() {
    timers[0].stop();
    timers[1].stop();
  }

  public void resetTimers() {
    timers[0] = null;
    timers[1] = null;
    initTimers();
  }

  public void initTimers() {
    for (int i = 0; i < timers.length; i++) {
      timers[i] = new Timer(times[i][0], times[i][1], times[i][2]);
    }
    timers[0].setColors(#ffffff, #26211b, #989795, #615e5b);
    timers[1].setColors(#26211b, #ffffff, #2b2722, #82807e);
  }

  public class Timer {
    int currentTime = 0; //temps à afficher
    int totalTime; //temps entré par l'utilisateur (ms)
    int backColorActive = #ffffff, textColorActive = #ffffff, backColor = #ffffff, textColor = #ffffff;
    int increment = 0;
    int timeOfSecond = 1000;
    boolean pause = true;

    Timer(int min, int sec, int increment) {
      this.totalTime = (min*60 + sec)*1000;
      this.currentTime = totalTime;
      this.increment = increment*1000;
    }

    void setColors(int bca, int tca, int bc, int tc) {
      this.backColorActive = bca;
      this.textColorActive = tca;
      this.backColor = bc;
      this.textColor = tc;
    }

    void setDurationOfSecond(int timeInMillis) {
      this.timeOfSecond = timeInMillis;
    }

    void addTime(int timeInMillis) {
      this.currentTime += timeInMillis;
    }

    void removeTime(int timeInMillis) {
      this.currentTime -= timeInMillis;
    }

    void update() {
      if (!this.pause) {
        this.currentTime -= timeOfSecond/rate;
      }
    }

    void pause() {
      this.pause = true;
      this.currentTime += this.increment;
    }

    void stop() {
      this.pause = true;
    }

    void resume() {
      this.pause = false;
    }

    void show(int x, int y, int size) {
      int s = (this.currentTime/1000) % 60;
      int m = ((int)this.currentTime/1000) / 60;
      String sec;
      String min;
      if (s < 10) { sec = "0" + s; } else { sec = str(s); }
      if (m < 10) { min = "0" + m; } else { min = str(m); }

      String text = min + ":" + sec;

      if (this.pause) fill(this.backColor);
      else fill(this.backColorActive);
      noStroke();
      rectMode(CENTER);
      rect(x, y, textWidth(text)*1.5, 60, 3);

      if (this.pause) fill(this.textColor);
      else fill(this.textColorActive);
      textSize(size);
      textAlign(CENTER, CENTER);
      text(text, x, y-5);
    }
  }

  PSurface initSurface() {
    PSurface pSurface = super.initSurface();
    PSurfaceAWT awtSurface = (PSurfaceAWT) surface;
    SmoothCanvas smoothCanvas = (SmoothCanvas) awtSurface.getNative();
    Frame frame = smoothCanvas.getFrame();
    frame.setUndecorated(true);
    return pSurface;
  }
}

/////////////////////////////////////////////////////////////////
