final public class MainApplet extends SApplet {
  private GameScene gameScene;
  private MenuScene menuScene;
  private EditorScene editorScene;

  public void setup() {
    textFont(createFont("data/fonts/LucidaSans.ttf", 12));

    int boardWindowWidth = Math.round(Config.UI.offsetX + 8*Config.UI.caseWidth);
    int boardWindowHeight = Math.round(Config.UI.offsetY + 8*Config.UI.caseWidth);

    menuScene = new MenuScene(this, 1100, 460);
    gameScene = new GameScene(this, boardWindowWidth, boardWindowHeight);
    editorScene = new EditorScene(this, boardWindowWidth, boardWindowHeight);

    register(menuScene, SceneIndex.Menu);
    register(gameScene, SceneIndex.Game);
    register(editorScene, SceneIndex.Editor);
    setScene(SceneIndex.Menu);
  }

  public void goToMenu() {
    setScene(SceneIndex.Menu);
  }

  public void goToEditor() {
    setScene(SceneIndex.Editor);
  }

  public void startDisplayGame(Player p1, Player p2, String startFEN, Timer t1, Timer t2, boolean useHacker) {
    GameManager gm = GameManager.getInstance();
    Game game = gm.addGame(p1, p2, startFEN, t1, t2, useHacker);
    gameScene.setGame(game);
    setScene(SceneIndex.Game);
  }
}
