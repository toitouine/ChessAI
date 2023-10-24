final public class MainApplet extends Applet {
  GameScene gameScene;

  public MainApplet() {
  }

  public void setup() {
    textFont(createFont("data/fonts/LucidaSans.ttf", 12));

    gameScene = new GameScene(this);
    register(gameScene, SceneIndex.Game);
    register(new MenuScene(this), SceneIndex.Menu);
    register(new EditorScene(this), SceneIndex.Editor);
    setScene(SceneIndex.Menu);
  }

  public void startDisplayGame(Player p1, Player p2, String fen, Timer t1, Timer t2, boolean useHacker) {
    Game game = new Game(p1, p2, fen, t1, t2, useHacker);
    gameScene.setGame(game);
    setScene(SceneIndex.Game);
  }

  public void startDisplayGame(Player p1, Player p2, String fen, Timer t1, Timer t2) {
    startDisplayGame(p1, p2, fen, t1, t2, false);
  }
}
