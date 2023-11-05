final public class MainApplet extends SApplet {
  GameScene gameScene;
  MenuScene menuScene;
  EditorScene editorScene;

  public void setup() {
    textFont(createFont("data/fonts/LucidaSans.ttf", 12));

    menuScene = new MenuScene(this);
    gameScene = new GameScene(this);
    editorScene = new EditorScene(this);

    register(menuScene, SceneIndex.Menu);
    register(gameScene, SceneIndex.Game);
    register(editorScene, SceneIndex.Editor);
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
