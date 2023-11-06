import processing.core.PSurface;
import processing.core.PImage;
import java.util.Collections;

public class MenuScene extends Scene {

  private boolean showWhiteID = true;
  private boolean showBlackID = true;
  private boolean useHacker = false;
  private String startFEN = Config.General.defaultFEN;
  private Selector<String> whiteSelector, blackSelector;
  private TimeButton whiteTime, blackTime;
  private TextToggle whiteSearchMode, blackSearchMode;
  private Slider whiteIDSlider, blackIDSlider;
  private Slider whiteFixSlider, blackFixSlider;

  public MenuScene(SApplet sketch, int width, int height) {
    super(sketch, width, height);
    init();
  }

  protected void setup() {
    Debug.log("UI", "Nouvelle scène : Menu");
    PSurface surface = sketch.getSurface();
    sketch.setTitle("Selection des joueurs");
    java.awt.Rectangle bounds = sketch.getScreenBounds();
    surface.setLocation(bounds.x + (bounds.width-width) / 2, bounds.y);
    surface.setAlwaysOnTop(false);
    surface.setVisible(true);
  }

  protected void draw() {
    sketch.background(49, 46, 43);
    sketch.fill(255);
    sketch.textSize(30);
    sketch.textAlign(sketch.LEFT, sketch.LEFT);
    sketch.text(Config.General.name, 20, 45);
    sketch.strokeWeight(2);
    sketch.stroke(255);
    sketch.line(20, 51, 235, 51);

    sketch.fill(255);
    sketch.textAlign(sketch.LEFT, sketch.LEFT);
    sketch.textSize(15);
    sketch.text(startFEN, 10, height-10);
  }

  private void startGame() {
    SearchSettings s1, s2;
    s1 = (showWhiteID
           ? new SearchSettings(Search.Iterative, Time.fromMillis(whiteIDSlider.getValue()))
           : new SearchSettings(Search.Fixed, whiteFixSlider.getValue()));
    s2 = (showBlackID
           ? new SearchSettings(Search.Iterative, Time.fromMillis(blackIDSlider.getValue()))
           : new SearchSettings(Search.Fixed, blackFixSlider.getValue()));

    Player p1 = Player.create(whiteSelector.getValue(), s1);
    Player p2 = Player.create(blackSelector.getValue(), s2);
    Timer t1 = new Timer(whiteTime.getTime(), whiteTime.getIncrement());
    Timer t2 = new Timer(blackTime.getTime(), blackTime.getIncrement());

    Game game = new Game(p1, p2, startFEN, t1, t2, useHacker);
    ((GameScene) sketch.getScene(SceneIndex.Game)).setGame(game);
    sketch.setScene(SceneIndex.Game);
  }

  private void init() {
    controllers.clear();
    addPlayerControllers();
    addOtherControllers();
  }

  private void addOtherControllers() {
    Collections.addAll(controllers,
      // Nouvelle partie
      new TextButton(sketch, width/2, height-88, "Nouvelle partie", 30, 10)
        .setDimensions(380, 75)
        .setColors(rgb(141, 167, 90), rgb(255, 255, 255))
        .setAction( () -> startGame() ),

      // Coller FEN
      new TextButton(sketch, width-60, height-25, "Coller FEN", 17)
        .setDimensions(100, 30)
        .setAction( () -> { startFEN = Clipboard.paste();
                            Debug.log("menu", "FEN collée (" + startFEN + ")"); } ),

      // Copier FEN
      new TextButton(sketch, width-170, height-25, "Copier FEN", 17)
        .setDimensions(100, 30)
        .setAction( () -> { Clipboard.copy(startFEN);
                            Debug.log("menu", "FEN copiée (" + startFEN + ")"); } ),

      // Joueurs aléatoires
      new TextButton(sketch, width-91, height-65, "Joueurs aléatoires", 17)
        .setAction( () -> { whiteSelector.randomize(); blackSelector.randomize(); } ),

      // Preset rapide
      new ImageButton(sketch, 63, height-66, 65, 65, "data/icons/rapid.png")
        .setFullSize(false)
        .setAction( () -> { whiteTime.set(Time.fromMinutes(10), Time.fromMillis(0));
                            blackTime.set(Time.fromMinutes(10), Time.fromMillis(0));
                            whiteIDSlider.setValue(7500); blackIDSlider.setValue(7500); } ),
      // Preset blitz
      new ImageButton(sketch, 143, height-66, 65, 65, "data/icons/blitz.png")
        .setFullSize(false)
        .setAction( () -> { whiteTime.set(Time.fromMinutes(3), Time.fromSeconds(2));
                            blackTime.set(Time.fromMinutes(3), Time.fromSeconds(2));
                            whiteIDSlider.setValue(1964); blackIDSlider.setValue(1964); } ),
      // Preset bullet
      new ImageButton(sketch, 223, height-66, 65, 65, "data/icons/bullet.png")
        .setFullSize(false)
        .setAction( () -> { whiteTime.set(Time.fromMinutes(1), Time.fromMillis(0));
                            blackTime.set(Time.fromMinutes(1), Time.fromMillis(0));
                            whiteIDSlider.setValue(720); blackIDSlider.setValue(720); } ),
      // Éditeur de position
      new ImageButton(sketch, width-28, 38, 55, 55, "data/icons/chess.png")
        .setAction( () -> sketch.setScene(SceneIndex.Editor) ),

      // Bouton du hacker
      new ImageToggle(sketch, width-84, 34, 45, 45, "data/icons/background.png", "data/icons/hacker.png")
        .setAction( () -> useHacker = !useHacker )
    );
  }

  private void addPlayerControllers() {
    PImage[] imgs = new PImage[Config.IA.players.length];
    String[] outputs = new String[Config.IA.players.length];
    for (int i = 0; i < imgs.length; i++)  {
      imgs[i] = sketch.loadImage("data/joueurs/" + Config.IA.players[i].toLowerCase() + ".jpg");
      outputs[i] = Config.IA.players[i];
    }

    whiteSelector = new Selector<String>(sketch, 312, 163, 165, 165, outputs, imgs);
    blackSelector = new Selector<String>(sketch, width - 312, 163, 165, 165, outputs, imgs);

    whiteTime = new TimeButton(sketch, 108, height - 152, 75);

    blackTime = new TimeButton(sketch, width - 108, height - 152, 75)
      .setColors(rgb(38, 33, 27), rgb(240, 240, 240), rgb(45, 45, 42));

    whiteSearchMode = (TextToggle)new TextToggle(sketch, 312, 270, "Iterative Deepening", "Profondeur fixe", 16)
      .setAction( () -> showWhiteID = !showWhiteID )
      .setCondition( () -> true );

    blackSearchMode = (TextToggle)new TextToggle(sketch, width-312, 270, "Iterative Deepening", "Profondeur fixe", 16)
      .setAction( () -> showBlackID = !showBlackID )
      .setCondition( () -> true );

    whiteIDSlider = (Slider)new Slider(sketch, 60, 162, 40, 164)
      .setMinimax(0, 10000)
      .setValue(1000)
      .setGraduations(10)
      .setCaption("Temps (ms)", rgb(255, 255, 255))
      .setCondition( () -> showWhiteID );

    blackIDSlider = (Slider)new Slider(sketch, width-60, 162, 40, 164)
      .setMinimax(0, 10000)
      .setValue(1000)
      .setGraduations(10)
      .setCaption("Temps (ms)", rgb(255, 255, 255))
      .setCondition( () -> showBlackID );

    whiteFixSlider = (Slider)new Slider(sketch, 60, 162, 40, 164)
      .setMinimax(1, 30)
      .setValue(5)
      .setGraduations(6)
      .setColors(rgb(93, 110, 59), rgb(141, 167, 90), rgb(171, 204, 106))
      .setCaption("Profondeur", rgb(255, 255, 255))
      .setCondition( () -> !showWhiteID );

    blackFixSlider = (Slider)new Slider(sketch, width-60, 162, 40, 164)
      .setMinimax(1, 30)
      .setValue(5)
      .setGraduations(6)
      .setColors(rgb(93, 110, 59), rgb(141, 167, 90), rgb(171, 204, 106))
      .setCaption("Profondeur", rgb(255, 255, 255))
      .setCondition( () -> !showBlackID );

    Collections.addAll(controllers,
      whiteSelector, whiteTime, whiteIDSlider, whiteFixSlider, whiteSearchMode,
      blackSelector, blackTime, blackIDSlider, blackFixSlider, blackSearchMode);
  }
}
