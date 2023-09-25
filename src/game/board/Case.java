public class Case {
  private int i, j;
  public String name;
  public Piece piece;

  public Case(int i, int j) {
    this.i = i;
    this.j = j;
    this.name = (char)(97+j) + String.valueOf(8 - i);
  }

}
