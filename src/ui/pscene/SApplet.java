import processing.core.PApplet;
import processing.core.PImage;
import processing.awt.PSurfaceAWT;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.GraphicsDevice;
import java.util.HashMap;

enum SceneIndex {
  Menu,
  Game,
  Editor
}

public abstract class SApplet extends PApplet {

  private HashMap<SceneIndex, Scene> scenes = new HashMap<SceneIndex, Scene>();
  private Scene currentScene;

  abstract public void setup();

  public void draw() {
    if (currentScene == null) {
      Debug.error("Aucune scène sélectionnée dans l'applet " + this);
      return;
    }

    currentScene.show();
  }

  final protected void register(Scene scene, SceneIndex index) {
    scenes.put(index, scene);
  }

  final public void setScene(SceneIndex index) {
    Scene scene = scenes.get(index);
    if (scene == null) {
      Debug.error("Scène demandée introuvable (SceneIndex " + index + "). Annulation du changement de scène.");
      return;
    }

    currentScene = scene;
    currentScene.awake();
  }

  public void keyPressed() {
    if (keyCode == ESC) key = 0;

    UserEvent event = new UserEvent(EventType.KeyPressed, keyCode);
    currentScene.onUserEvent(event);
  }

  public void mouseMoved() {
    UserEvent event = new UserEvent(EventType.MouseMoved, mouseX, mouseY);
    currentScene.onUserEvent(event);
  }

  public void mousePressed() {
    UserEvent event = new UserEvent(EventType.MousePressed, mouseX, mouseY);
    currentScene.onUserEvent(event);
  }

  public void mouseReleased() {
    UserEvent event = new UserEvent(EventType.MouseReleased, mouseX, mouseY);
    currentScene.onUserEvent(event);
  }

  public void mouseDragged() {
    UserEvent event = new UserEvent(EventType.MouseDragged, mouseX, mouseY);
    currentScene.onUserEvent(event);
  }

  final public void setTitle(String title) {
    if (title.equals("")) surface.setTitle(Config.General.name);
    else surface.setTitle(Config.General.name + " - " + title);
  }

  @Override
  final public PImage loadImage(String path) {
    PImage img = super.loadImage(path);
    if (img == null) {
      Debug.error("Image introuvable : " + path + ". Ajout de l'image par défaut.");
      img = super.loadImage(Config.UI.defaultImage);
    }
    return img;
  }

  final public Frame getFrame() {
    return ( (PSurfaceAWT.SmoothCanvas) ((PSurfaceAWT)surface).getNative()).getFrame();
  }

  final public GraphicsDevice getScreen() {
    return getFrame().getGraphicsConfiguration().getDevice();
  }

  final public Rectangle getScreenBounds() {
    return getScreen().getDefaultConfiguration().getBounds();
  }
}
