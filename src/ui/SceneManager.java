import java.util.HashMap;

enum SceneIndex {
  Menu,
  Game,
  Editor
}

public class SceneManager {

  private HashMap<SceneIndex, Scene> scenes;
  private Scene currentScene;

  public SceneManager() {
    scenes = new HashMap<SceneIndex, Scene>();
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
      Debug.error("Aucune scène sélectionnée dans SceneManager");
      return;
    }

    currentScene.draw();
  }

  public void onUserEvent(UserEvent e) {
    currentScene.onUserEvent(e);
  }
}
