public final class GameManager {

  public Board board;
  public Player[] players = new Player[2];
  public boolean gameEnded = false;
  public boolean useHacker = false;

  public GameManager() {
    board = new Board();
  }

  public void update() {
    // Faire jouer les joueurs si c'est à leur tour / si c'est pas bloqué
    // Gestion du hacker
  }

  public void startGame(Player p1, Player p2, String startFEN) {
    players[0] = p1;
    players[1] = p2;
    board.loadFEN(startFEN);
  }

  public Player getWhite() {
    return players[Player.White];
  }

  public Player getBlack() {
    return players[Player.Black];
  }

}
