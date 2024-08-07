import processing.core.PImage;

public class EndOverlay extends Overlay<MainApplet> {

  private final int w = Config.UI.caseWidth;
  private int winnerColor;
  private Player[] players = new Player[2];
  private boolean nulle;
  private String reason;
  private PImage j1Img, j2Img;

  private final float targetEndScreenY;
  private final Time timeBeforeEndDisplay = Time.fromMillis(750);
  private final float startEndScreenY = -210;
  private final float endScreenEasing = 0.04f;
  private Time timeAtEndStart;
  private float currentYLevel;
  private boolean canClose;

  public EndOverlay(Scene<MainApplet> scene, float x, float y, int width, int height) {
    super(scene, x, y, width, height);
    targetEndScreenY = y - 1.5f*w;
  }

  public void draw() {
    if (Time.elapsed(timeAtEndStart).millis() > timeBeforeEndDisplay.millis()) {
      sketch.fill(220, 220, 220, 220);
      sketch.rectMode(sketch.CENTER);
      sketch.rect(x, y, width, height);

      if (currentYLevel < targetEndScreenY) {
        float dy = targetEndScreenY - currentYLevel;
        dy = Math.max(dy, 7);
        currentYLevel += dy * endScreenEasing;
      } else if (!canClose) canClose = true;
      drawEndScreen(currentYLevel);
    }
  }

  public void startEndAnimation(Game game) {
    GameState state = game.gameState;

    winnerColor = state.winner();
    nulle = state.isDraw();
    reason = state.reason();
    players[0] = game.getWhite();
    players[1] = game.getBlack();
    String whiteName = players[0].name();
    String blackName = players[1].name();
    j1Img = sketch.loadImage("data/joueurs/" + whiteName.toLowerCase() + "ImgEnd.jpg");
    j2Img = sketch.loadImage("data/joueurs/" + blackName.toLowerCase() + "ImgEnd.jpg");

    currentYLevel = startEndScreenY;
    timeAtEndStart = Time.now();
    canClose = false;
    scene.setOverlay(this);
  }

  public void drawEndScreen(float yLevel) {
    sketch.noStroke();
    sketch.rectMode(sketch.CORNER);
    int gris = rgb(102, 100, 99);
    int vert = rgb(141, 167, 90);

    // Grand rectangle
    float rectX = x - 2.25f*w;
    float rectY = yLevel;
    float rectW = 4.5f*w;
    float rectH = 3f*w;
    sketch.fill(255);
    sketch.rect(rectX, rectY, rectW, rectH, 5);

    // Images
    float imgW = 1.2f*w;
    float space = (2*rectH/3 - imgW)/2;
    float imgY = (rectY + rectH/3f) + space/1.5f;
    sketch.imageMode(sketch.CORNER);
    sketch.image(j1Img, rectX+space, imgY, imgW, imgW);
    sketch.image(j2Img, rectX+rectW-space-imgW, imgY, imgW, imgW);

    // VS
    sketch.fill(gris);
    sketch.textSize(20 * w/75);
    sketch.text("VS", rectX+rectW/2f, imgY + imgW/2f);

    // Raison
    sketch.fill(gris);
    sketch.textSize(15* w/75);
    sketch.text(reason, rectX+rectW/2f, rectY+rectH - space/1.5f);

    // Gagnant
    if (!nulle) {
      sketch.stroke(vert);
      sketch.strokeWeight(6 * w/75);
      sketch.noFill();
      sketch.rect((winnerColor == Player.White) ? (rectX+space) : (rectX+rectW-space-imgW), imgY, imgW, imgW, 5);
    }

    // Petit rectangle et texte
    int fillColor;
    if (players[0] instanceof Humain && players[1] instanceof Humain) {
      fillColor = vert;
    } else {
      fillColor = (nulle || players[1-winnerColor] instanceof Humain) ? gris : vert;
    }
    String title = nulle ? "Nulle" : players[winnerColor].victoryTitle();
    sketch.fill(fillColor);
    sketch.noStroke();
    sketch.rect(rectX, rectY, rectW, rectH/3, 5, 5, 0, 0);
    sketch.fill(255);
    sketch.textAlign(sketch.CENTER, sketch.CENTER);
    sketch.textSize(30 * w/75);
    sketch.text(title, rectX + rectW/2f, rectY + rectH/6.5f);
  }

  @Override
  public void onUserEvent(UserEvent event) {
    super.onUserEvent(event);
    if (event.mousePressed() && contains(event.x, event.y)) {
      if (canClose) scene.toggleOverlay(this);
    }
  }
}
