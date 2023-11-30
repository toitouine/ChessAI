enum EventType {
  MousePressed,
  MouseReleased,
  MouseMoved,
  MouseDragged,
  KeyPressed
}

public class UserEvent {
  private EventType type;
  public int x;
  public int y;
  public char key;
  public int keyCode;

  public UserEvent(EventType type, int x, int y) {
    this.type = type;
    this.x = x;
    this.y = y;
  }

  public UserEvent(EventType type, char key, int keyCode) {
    this.type = type;
    this.key = key;
    this.keyCode = keyCode;
  }

  public boolean mousePressed() {
    return (type == EventType.MousePressed);
  }

  public boolean mouseReleased() {
    return (type == EventType.MouseReleased);
  }

  public boolean mouseMoved() {
    return (type == EventType.MouseMoved);
  }

  public boolean mouseDragged() {
    return (type == EventType.MouseDragged);
  }

  public boolean keyPressed() {
    return (type == EventType.KeyPressed);
  }
}
