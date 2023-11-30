public final class MutableBoolean {
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
