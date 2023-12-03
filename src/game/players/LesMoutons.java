public class LesMoutons extends Player {

  public LesMoutons(SearchSettings settings) {
    name = "LesMoutons";
    pseudo = "Mouton";
    elo = String.valueOf((int)(1300 + Math.random() * 200));
    title = "Mouton";
    victoryTitle = "YOU LOUSE";
    ouvertureNumber = 5;
    this.settings = settings;
  }

}
