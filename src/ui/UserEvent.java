public class UserEvent {
  public EventType type;
  public int x;
  public int y;
  public int key;

  public UserEvent(EventType type, int x, int y) {
    this.type = type;
    this.x = x;
    this.y = y;
  }

  public UserEvent(EventType type, int key) {
    this.type = type;
    this.key = key;
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

enum EventType {
  MousePressed,
  MouseReleased,
  MouseMoved,
  MouseDragged,
  KeyPressed
}
