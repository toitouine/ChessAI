// hlength = 0.5f * Math.sqrt(Math.pow((deltaI*caseWidth), 2) + Math.pow((deltaJ*caseWidth), 2));

public class Case {
  public int i, j;
  public String name;
  public Piece piece;

  public Case(int i, int j) {
    this.i = i;
    this.j = j;
    this.name = (char)(97+j) + String.valueOf(8 - i);
  }

}
