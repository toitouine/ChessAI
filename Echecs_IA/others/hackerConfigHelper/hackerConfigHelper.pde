/////////////////////////////////////////////////////////////////

// Liste des calibrations à faire

// endColorLichess
// coupLichessWhite
// coupLichessBlack
// coupChesscomWhite
// coupChesscomBlack
// expectChesscomWhitePieceColor
// expectChesscomBlackPieceColor
// expectLichessWhitePieceColor
// expectLichessBlackPieceColor

/////////////////////////////////////////////////////////////////

import java.awt.Color;
import java.awt.Point;
import java.awt.MouseInfo;
import java.awt.Robot;

Robot hacker;

int numberOfCalibrations = 9;
int current = 0;
Calibration[] calibrations;

float textSize = 25;
float interligne = 15;

void settings() {
  size(800, 300);
}

void setup() {
  surface.setAlwaysOnTop(true);
  surface.setLocation(displayWidth-width, 0);
  surface.setTitle("Aide à la configuration du hacker");

  calibrations = new Calibration[numberOfCalibrations];
  calibrations[0] = new Calibration("coupChesscomWhite", "Couleur de surlignage des cases blanches sur chess.com [voir config helper]", loadImage("chesscomCaseWhite.png"));
  calibrations[1] = new Calibration("coupChesscomBlack", "Couleur de surlignage des cases noires sur chess.com [voir config helper]", loadImage("chesscomCaseBlack.png"));
  calibrations[2] = new Calibration("expectChesscomWhitePieceColor", "Couleur des pièces blanches de chess.com (pour l'auto calibration) [voir config helper]", loadImage("chesscomWhite.png"));
  calibrations[3] = new Calibration("expectChesscomBlackPieceColor", "Couleur des pièces noires de chess.com (pour l'auto calibration) [voir config helper]", loadImage("chesscomBlack.png"));
  calibrations[4] = new Calibration("coupLichessWhite", "Couleur de surlignage des cases blanches sur Lichess [voir config helper]", loadImage("lichessCaseWhite.png"));
  calibrations[5] = new Calibration("coupLichessBlack", "Couleur de surlignage des cases noires sur Lichess [voir config helper]", loadImage("lichessCaseBlack.png"));
  calibrations[6] = new Calibration("expectLichessWhitePieceColor", "Couleur des pièces blanches de Lichess (pour l'auto calibration) [voir config helper]", loadImage("lichessWhite.png"));
  calibrations[7] = new Calibration("expectLichessBlackPieceColor", "Couleur des pièces noires de Lichess (pour l'auto calibration) [voir config helper]", loadImage("lichessBlack.png"));
  calibrations[8] = new Calibration("endColorLichess", "Couleur du bouton de nouvelle partie de Lichess (quand la souris est dessus) [voir config helper]", loadImage("lichessEnd.png"));

  try {
    hacker = new Robot();
  } catch(Exception e) {
    e.printStackTrace();
  }
}

void draw() {
  background(49, 46, 43);
  fill(255);
  textSize(textSize);
  textAlign(CENTER, TOP);
  text(calibrations[current].description, width/2, height/2 - (interligne + textSize));
  text("[APPUYEZ SUR ESPACE]", width/2, height/2 + interligne);

  image(calibrations[current].img, 25, height-110, 85, 85);
  textSize(15);
  text("Exemple", 68, height-22);

  Point p = MouseInfo.getPointerInfo().getLocation();
  Color c = hacker.getPixelColor(p.x, p.y);
  fill(c.getRed(), c.getGreen(), c.getBlue());
  rect(width-110, height-110, 85, 85);
  fill(255);
  textSize(15);
  text("Couleur", width-68, height-22);
  textAlign(CENTER, CENTER);
  textSize(12);
  fill(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
  text(c.getRed() + ", " + c.getGreen() + ", " + c.getBlue(), width-67, height-68);
}

void keyPressed() {
  if (key == ' ') {
    Point p = MouseInfo.getPointerInfo().getLocation();
    calibrations[current].setColor(hacker.getPixelColor(p.x, p.y));
    current++;
    if (current >= numberOfCalibrations) calibrationDone();
  }
}

void calibrationDone() {
  String[] output = new String[numberOfCalibrations];
  for (int i = 0; i < numberOfCalibrations; i++) {
    output[i] = calibrations[i].getConfigLine();
  }
  printArray(output);
  saveStrings("config.txt", output);
  println("Configuration sauvegardée dans config.txt");
  exit();
}

class Calibration {
  String variableName, description, comment;
  PImage img;
  Color c;

  Calibration(String variableName, String comment, PImage img) {
    this.variableName = variableName;
    this.description = split(split(comment, '[')[0], '(')[0];
    this.comment = comment;
    this.img = img;
  }

  void setColor(Color newc) {
    this.c = newc;
  }

  String getConfigLine() {
    return ("Color " + this.variableName + " = new Color(" + this.c.getRed() + ", " + this.c.getGreen() + ", " + this.c.getBlue() + "); // " + this.comment);
  }
}
