interface GameDisplayer {
  public void setGame(Game game);
  public void onGameStart();
  public void onGameEnd();
  public void onMovePlayed(SearchResult result, int color);
  public Move askHumanMove(Board board);
  public void stopAskMove();
}
