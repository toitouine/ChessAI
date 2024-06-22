public class MutableBoolean {
  private boolean value;

  public MutableBoolean(boolean value) {
    this.value = value;
  }

  public void set(boolean value) {
    this.value = value;
  }

  public boolean get() {
    return value;
  }

  public void toggle() {
    value = !value;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[" + value + "]";
  }
}

// Variable booléenne synchronisée entre plusieurs threads
class SyncBoolean extends MutableBoolean {
  public SyncBoolean(boolean value) {
    super(value);
  }

  @Override
  public synchronized void set(boolean value) {
    super.set(value);
    notify();
  }

  @Override
  public synchronized void toggle() {
    super.toggle();
    notify();
  }
}
