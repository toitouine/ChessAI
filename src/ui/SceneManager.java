import java.util.HashMap;

enum SceneIndex {
  Menu,
  Game,
  Editor
}

public class SceneManager {

  private Main sketch;
  private HashMap<SceneIndex, Scene> scenes;
  private Scene currentScene;

  public SceneManager(Main sketch) {
    this.sketch = sketch;
    this.scenes = new HashMap<SceneIndex, Scene>();
  }

  public SceneManager register(Scene scene, SceneIndex index) {
    scenes.put(index, scene);
    return this;
  }

  public SceneManager setScene(SceneIndex index) {
    currentScene = scenes.get(index);
    currentScene.awake();
    return this;
  }

  public Scene getScene() {
    return currentScene;
  }

  public void drawScene() {
    if (currentScene == null) {
      Debug.log("erreur", "Aucune scène sélectionnée dans SceneManager");
      return;
    }

    currentScene.draw();
  }

  public void onUserEvent(UserEvent e) {
    currentScene.onUserEvent(e);
  }
}
