/////////////////////////////////////////////////////////////////

// 1) Fonctions utiles (ou pas)
// 2) Hacker
// 3) Affichages
// 4) Plateau et presets
// 5) FEN, historiques et données
// 6) Fonctions pour calculs et recherche

/////////////////////////////////////////////////////////////////

// Fonctions utiles (ou pas)

void alert(String message, int time) {
  alert = message;
  alertTime = time;
  alertStarted = millis();
}

void sendMoutonMessage(String message, float x, float y, int time) {
  messageMouton = message;
  messageMoutonTime = time;
  alertPos.x = (int)x;
  alertPos.y = (int)y;
  messageMoutonStarted = millis();
}

boolean isSameColor(Color c1, Color c2) {
  return c1.getRed() == c2.getRed() && c1.getGreen() == c2.getGreen() && c1.getBlue() == c2.getBlue();
}

boolean isSimilarColor(Color c1, Color c2) {
  return abs(c1.getRed()-c2.getRed()) <= 10 && abs(c1.getGreen()-c2.getGreen()) <= 10 && abs(c1.getBlue()-c2.getBlue()) <= 10;
}

String roundedString(float num) {
  boolean isInteger = num % 1 == 0;
  return (isInteger ? str((int)num) : nf(num, 1, 1));
}

int stringToInt(String str) {
  return Integer.parseInt(str);
}

String roundNumber(float num, int digit) {
  return nf(num, 1, digit);
}

String GetTextFromClipboard() {
  String text = (String) GetFromClipboard(DataFlavor.stringFlavor);
  if (text==null) return "";

  return text;
}

String formatInt(int value) {
  String input = str(value);
  String output = "";

  int counter = 0;
  for (int i = input.length()-1; i >= 0; i--) {
    counter++;
    output = input.charAt(i) + output;
    if (counter == 3 && i != 0) {
      output = " " + output;
      counter = 0;
    }
  }
  return output;
}

boolean isMateValue(float eval, int plyFromRoot) {
 int sign = (eval < 0) ? -1 : 1;
 float value = eval * sign;
 value += plyFromRoot;
 return (value == 50000);
}

String evalToStringMaire(float eval) {
  int sign = (eval < 0) ? -1 : 1;
  float value = eval * sign;
  if (value < 40000) return roundNumber(eval/100.0, 3);
  int ply = (int)(50000 - value);
  return "MAT EN " + ply;
}

String evalToStringLoic(float eval) {
  int sign = (eval < 0) ? -1 : 1;
  float value = eval * sign;
  if (value < 15000) return roundNumber(eval/100.0, 3);
  if (value < 37000) {
    int ply = (int)(25000 - value);
    return "MAT EN " + ply;
  }
  int ply = (int)(50000 - value);
  return "PAT EN " + ply;
}

void updateBlockPlaying() {
  blockPlaying = !play || gameEnded || rewind || (useHacker && !hackerPret);
}

Object GetFromClipboard(DataFlavor flavor) {

  Clipboard clipboard = getJFrame(getSurface()).getToolkit().getSystemClipboard();

  Transferable contents = clipboard.getContents(null);
  Object object = null; // the potential result

  if (contents != null && contents.isDataFlavorSupported(flavor)) {
    try
    {
      object = contents.getTransferData(flavor);
    }

    catch (UnsupportedFlavorException e1)
    {
      println("Clipboard.GetFromClipboard() >> Unsupported flavor: " + e1);
      e1.printStackTrace();
    }

    catch (java.io.IOException e2)
    {
      println("Clipboard.GetFromClipboard() >> Unavailable data: " + e2);
      e2.printStackTrace() ;
    }
  }

  return object;
}

static final javax.swing.JFrame getJFrame(final PSurface surf) {
  return
    (javax.swing.JFrame)
    ((processing.awt.PSurfaceAWT.SmoothCanvas)
    surf.getNative()).getFrame();
}

void error(String function, String message) {
  println();
  println(">>> !! ERREUR " + message.toUpperCase() + " DANS " + function.toUpperCase() + " !! <<<");
  println();
}

/////////////////////////////////////////////////////////////////

// Hacker

void cheat(int c, int fromI, int fromJ, int i, int j, int special) {
  deselectAll();

  // Détection du hacker sans fin sur chess.com
  if (hackerSansFin && hackerSite == CHESSCOM) {
    if (hackerEndDetected()) {
      endOnHackerDetect();
      timeAtHackerEnd = millis();
      return;
    }
  }

  // Sauvegarde les coordonnées du curseur
  Point mouse = MouseInfo.getPointerInfo().getLocation();
  int x = mouse.x;
  int y = mouse.y;
  int promoDir = (c == 0 ? 1 : -1);

  // Prend le focus de chess.com
  click(hackerCoords[fromJ][fromI].x, hackerCoords[fromJ][fromI].y);

  // Joue le coup
  click(hackerCoords[fromJ][fromI].x, hackerCoords[fromJ][fromI].y);
  int delay = (int)random(100, 300);
  delay(delay);
  click(hackerCoords[j][i].x, hackerCoords[j][i].y);

  if (special >= 5) delay(20);
  if (special == 5) click(hackerCoords[j][i].x, hackerCoords[j][i].y);
  else if (special == 6) click(hackerCoords[j][i].x, hackerCoords[j+2*promoDir][i].y);
  else if (special == 7) click(hackerCoords[j][i].x, hackerCoords[j+3*promoDir][i].y);
  else if (special == 8) click(hackerCoords[j][i].x, hackerCoords[j+promoDir][i].y);

  // Revient à la position initiale
  click(x, y);

  // Déselectionne les pièces au cas où
  deselectAll();
}

boolean chessComEndDetected() {
  Color endScreen = hacker.getPixelColor(hackerCoords[3][2].x, hackerCoords[3][2].y);
  return isSameColor(endScreen, Color.white);
}

boolean lichessEndDetected() {
  Point mouse = MouseInfo.getPointerInfo().getLocation();
  click(hackerCoords[0][0].x, hackerCoords[0][0].y);

  hacker.mouseMove(newgameLocation.x, newgameLocation.y);
  delay(200);

  if (isSimilarColor(hacker.getPixelColor(newgameLocation.x, newgameLocation.y), endColorLichess)) {
    click(mouse.x, mouse.y);
    return true;
  }

  click(mouse.x, mouse.y);
  return false;
}

boolean hackerEndDetected() {
  return ( (hackerSite == CHESSCOM && chessComEndDetected()) || (hackerSite == LICHESS && lichessEndDetected()) );
}

Color[][] scanBoard(boolean debugPrint) {
  // int before = millis();
  Color[][] scannedBoard = new Color[8][8];

  for (int i = 0; i < 8; i++) {
    for (int j = 0; j < 8; j++) {
      int x = hackerCoords[i][j].x;
      int y = hackerCoords[i][j].y;
      scannedBoard[i][j] = hacker.getPixelColor(x, y);

      if (debugPrint) {
        if (isSimilarColor(scannedBoard[i][j], hackerWhitePieceColor)) print("B ");
        else if (isSimilarColor(scannedBoard[i][j], hackerBlackPieceColor)) print("N ");
        else print("/ ");
      }
    }
    if (debugPrint) { println(); }
  }

  // println("Scan completed in " + (millis()-before) + " ms");

  return scannedBoard;
}


Move getMoveOnBoard() {
  Color[][] scannedBoard = scanBoard(false);
  Color pieceColor = (tourDeQui == 0) ? hackerWhitePieceColor : hackerBlackPieceColor;

  ArrayList<Piece> piecesToCheck = new ArrayList<Piece>();
  piecesToCheck.add(rois[tourDeQui]);
  for (int i = 0; i < pieces[tourDeQui].size(); i++) {
    if (pieces[tourDeQui].get(i) == rois[tourDeQui]) continue;
    piecesToCheck.add(pieces[tourDeQui].get(i));
  }

  for (int n = 0; n < piecesToCheck.size(); n++) {
    // On regarde si les pièces sont à la bonne case (i et j sont inversés)
    Piece p = piecesToCheck.get(n);
    Color scannedColor = scannedBoard[p.j][p.i];
    if (isSimilarColor(scannedColor, pieceColor)) continue;

    // C'est cette pièce qui s'est déplacé, on génère ses coups (les coups spéciaux sont regardés en premier pour régler le problème du roque)
    ArrayList<Move> moves = p.generateLegalMoves(true, false);
    for (int k = 0; k < moves.size(); k++) {
      if (moves.get(k).special != 0) {
        if (moves.get(k).special == 4) moves.get(k).special = 5; // autopromotion en dame pour le hacker
        Move m = moves.remove(k);
        moves.add(0, m);
      }
    }

    for (int k = 0; k < moves.size(); k++) {
      Move m = moves.get(k);
      Color scannedColorMove = hacker.getPixelColor(hackerCoords[m.j][m.i].x, hackerCoords[m.j][m.i].y);
      if (isSimilarColor(scannedColorMove, pieceColor)) return m;
    }
  }

  return null;
}

void scanMoveOnBoard() {
  lastHackerScan = millis();
  numberOfScan++;

  // Détection du hacker sans fin
  if (hackerSansFin && numberOfScan >= scansBetweenEndDetect) {
    numberOfScan = 0;
    if (hackerEndDetected()) {
      endOnHackerDetect();
      timeAtHackerEnd = millis();
      return;
    }
  }

  Move sm = getMoveOnBoard();
  if (sm != null) {
    isNextMoveRestranscrit = true;
    sm.play();
    if (!blockPlaying) {
      if ((joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") || (joueurs.get(0).name != "Humain" && joueurs.get(1).name == "Humain")) {
        if (joueurs.get(tourDeQui).name != "Humain") { engineToPlay = true; }
      }
    }
  }
}

// Tolerance représente nombre d'erreurs autorisé et confondu si on confond les deux types de pièce
boolean verifyCalibration(int tolerance, boolean confondu) {
  Color B = hackerWhitePieceColor;
  Color N = hackerBlackPieceColor;
  Color A = null;

  if (isSameColor(B, N)) return false;

  Color[][] expectedBoard = {{N, N, N, N, N, N, N, N},
                             {N, N, N, N, N, N, N, N},
                             {A, A, A, A, A, A, A, A},
                             {A, A, A, A, A, A, A, A},
                             {A, A, A, A, A, A, A, A},
                             {A, A, A, A, A, A, A, A},
                             {B, B, B, B, B, B, B, B},
                             {B, B, B, B, B, B, B, B}};

  boolean debugPrint = (confondu ? false : true);
  Color[][] scannedBoard = scanBoard(debugPrint);
  int errorCount = 0;

  for (int i = 0; i < 8; i++) {
    for (int j = 0; j < 8; j++) {

      if (expectedBoard[i][j] == null) {
        if (isSimilarColor(scannedBoard[i][j], hackerWhitePieceColor) || isSimilarColor(scannedBoard[i][j], hackerBlackPieceColor)) {
          if (errorCount >= tolerance) return false;
          errorCount++;
        }
        continue;
      }

      if (!confondu) {
        if (!isSimilarColor(scannedBoard[i][j], expectedBoard[i][j])) {
          if (errorCount >= tolerance) return false;
          errorCount++;
        }
      } else {
        if ((!isSimilarColor(scannedBoard[i][j], hackerWhitePieceColor)) && (!isSimilarColor(scannedBoard[i][j], hackerBlackPieceColor))) {
          if (errorCount >= tolerance) return false;
          errorCount++;
        }
      }

    }
  }
  return true;
}

void click(int x, int y) {
  hacker.mouseMove(x, y);
  hacker.mousePress(InputEvent.BUTTON1_DOWN_MASK);
  hacker.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
}

void hackStartGame() {
  if (newgameLocation == null) {
    error("hackStartGame", "Nouvelle partie non calibrée");
    return;
  }

  deselectAll();

  Point mouse = MouseInfo.getPointerInfo().getLocation();
  int x = mouse.x;
  int y = mouse.y;

  click(hackerCoords[0][0].x, hackerCoords[0][0].y);
  delay(2);
  click(newgameLocation.x, newgameLocation.y);
  delay(2);
  click(x, y);

  deselectAll();
  hackerWaitingToRestart = true;
}

void handleWaitForRestart() {
  timeAtLastRestartTry = millis();

  boolean isGameStarted = verifyCalibration(2, true);
  scanBoard(false);
  if (isGameStarted) {
    int botColor;

    if (currentHackerPOV == 0) botColor = isSimilarColor(hacker.getPixelColor(hackerCoords[7][7].x, hackerCoords[7][7].y), hackerWhitePieceColor) ? 0 : 1;
    else botColor = isSimilarColor(hacker.getPixelColor(hackerCoords[0][0].x, hackerCoords[0][0].y), hackerWhitePieceColor) ? 0 : 1;

    delay(500);
    String iaType = ((joueurs.get(0).name == "Humain") ? joueurs.get(1).name : joueurs.get(0).name);
    setHackerPOV(botColor);
    newAIGame(botColor, iaType);
    forceCalibrationRestore();
  }
}

void restoreCalibrationSaves() {
  if (saveUpLeftCorner == null || saveDownRightCorner == null || saveNewgameLocation == null || saveWhitePieceColor == null || saveBlackPieceColor == null) {
    alert("Aucune sauvegarde", 2500);
    println("Aucune sauvegarde");
    return;
  }

  upLeftCorner = copyPoint(saveUpLeftCorner);
  downRightCorner = copyPoint(saveDownRightCorner);
  newgameLocation = copyPoint(saveNewgameLocation);
  hackerWhitePieceColor = copyColor(saveWhitePieceColor);
  hackerBlackPieceColor = copyColor(saveBlackPieceColor);
  hackerCoords = copyCoords(saveHackerCoords);
  currentHackerPOV = 0;

  if (!verifyCalibration(0, false)) {
    upLeftCorner = null;
    downRightCorner = null;
    newgameLocation = null;
    hackerWhitePieceColor = null;
    hackerBlackPieceColor = null;
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        hackerCoords[i][j] = new Point();
      }
    }
    alert("Échec de la calibration", 2500);
    return;
  }

  if (play && !gameEnded && !rewind) {
    if (joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") {
      setHackerPOV(1);
      if (joueurs.get(tourDeQui).name != "Humain") engineToPlay = true;
    }
    else if (joueurs.get(0).name != "Humain" && joueurs.get(1).name == "Humain") {
      if (joueurs.get(tourDeQui).name != "Humain") { engineToPlay = true; }
    }
  }

  hackerPret = true;
  if (MODE_SANS_AFFICHAGE) {
    surface.setSize(150, 150);
    surface.setLocation(displayWidth-150, 0);
    hacker.mouseMove(displayWidth-75, 120);
    sa.hide();
    ta.hide();
    ga.hide();
  }

  println("Sauvegardes restaurées");
}

void forceCalibrationRestore() {
  if (saveUpLeftCorner == null || saveDownRightCorner == null || hackerWhitePieceColor == null || hackerBlackPieceColor == null) {
    alert("Aucune sauvegarde", 2500);
    println("Aucune sauvegarde");
    return;
  }

  upLeftCorner = copyPoint(saveUpLeftCorner);
  downRightCorner = copyPoint(saveDownRightCorner);
  newgameLocation = copyPoint(saveNewgameLocation);
  hackerWhitePieceColor = copyColor(saveWhitePieceColor);
  hackerBlackPieceColor = copyColor(saveBlackPieceColor);
  hackerCoords = copyCoords(saveHackerCoords);
  currentHackerPOV = 0;

  if (play && !gameEnded && !rewind) {
    if (joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") {
      setHackerPOV(1);
      if (joueurs.get(tourDeQui).name != "Humain") engineToPlay = true;
    }
    else if (joueurs.get(0).name != "Humain" && joueurs.get(1).name == "Humain") {
      if (joueurs.get(tourDeQui).name != "Humain") { engineToPlay = true; }
    }
  }

  hackerPret = true;
  if (MODE_SANS_AFFICHAGE) {
    surface.setSize(150, 150);
    surface.setLocation(displayWidth-150, 0);
    hacker.mouseMove(displayWidth-75, 120);
    sa.hide();
    ta.hide();
    ga.hide();
  }

  alert("Sauvegarde forcée", 1500);
  println("Restauration forcée des sauvegardes");
}

void addPointToCalibration() {
  Point p = MouseInfo.getPointerInfo().getLocation();

  if (upLeftCorner == null) {
    upLeftCorner = p;
  }
  else if (downRightCorner == null) {
    downRightCorner = p;
  }
  else if (newgameLocation == null) {
    newgameLocation = p;
    calibrerHacker();
  }
  else println(">>> Hacker déjà calibré");
}

void calibrerHacker() {
  println("Haut-gauche :", upLeftCorner.x, upLeftCorner.y);
  println("Bas-droite :", downRightCorner.x, downRightCorner.y);
  println("Nouvelle partie :", newgameLocation.x, newgameLocation.y);

  int boardWidth = downRightCorner.x - upLeftCorner.x;
  int boardHeight = downRightCorner.y - upLeftCorner.y;

  for (int i = 0; i < 8; i++) {
    for (int j = 0; j < 8; j++) {
      hackerCoords[j][i].x = upLeftCorner.x + i*(boardWidth/7);
      hackerCoords[j][i].y = upLeftCorner.y + j*(boardHeight/7);
    }
  }
  currentHackerPOV = 0;

  hackerWhitePieceColor = hacker.getPixelColor(hackerCoords[7][7].x, hackerCoords[7][7].y);
  hackerBlackPieceColor = hacker.getPixelColor(hackerCoords[0][0].x, hackerCoords[0][0].y);

  if (!verifyCalibration(0, false)) {
    upLeftCorner = null;
    downRightCorner = null;
    newgameLocation = null;
    hackerWhitePieceColor = null;
    hackerBlackPieceColor = null;
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        hackerCoords[i][j] = new Point();
      }
    }
    alert("Échec de la calibration", 2500);
    return;
  }

  saveUpLeftCorner = upLeftCorner;
  saveDownRightCorner = downRightCorner;
  saveHackerCoords = copyCoords(hackerCoords);
  saveWhitePieceColor = hackerWhitePieceColor;
  saveBlackPieceColor = hackerBlackPieceColor;
  saveNewgameLocation = newgameLocation;
  println("Données du hacker sauvegardées");

  if (play && !gameEnded && !rewind) {
    if ((joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") || (joueurs.get(0).name != "Humain" && joueurs.get(1).name == "Humain")) {
      if (joueurs.get(tourDeQui).name != "Humain") { engineToPlay = true; }
    }
  }

  hackerPret = true;
  if (MODE_SANS_AFFICHAGE) {
    surface.setSize(150, 150);
    surface.setLocation(displayWidth-150, 0);
    hacker.mouseMove(displayWidth-75, 120);
    sa.hide();
    ta.hide();
    ga.hide();
  }

  println();
  println(">>> Hacker calibré avec succès (ou pas)");
  println();
}

void setHackerPOV(int pov) {
  if (pov == currentHackerPOV) return;
  hackerCoords = reverseCoords(hackerCoords);
  currentHackerPOV = pov;
}

Point[][] reverseCoords(Point[][] arr) {
  int l1 = arr.length;
  int l2 = arr[0].length;
  Point[][] result = new Point[l1][l2];

  for (int i = 0; i < l1; i++) {
    for (int j = 0; j < l2; j++) {
      result[i][j] = arr[l1-(i+1)][l2-(j+1)];
    }
  }
  return result;
}

Color copyColor(Color c) {
  int r = c.getRed();
  int g = c.getGreen();
  int b = c.getBlue();
  return new Color(r, g, b);
}

Point copyPoint(Point p) {
  Point result = new Point();
  result.x = p.x;
  result.y = p.y;
  return result;
}

Point[][] copyCoords(Point[][] array) {
  Point[][] result = new Point[8][8];
  for (int i = 0; i < array.length; i++) {
    for (int j = 0; j < array.length; j++) {
      result[i][j] = array[i][j];
    }
  }
  return result;
}

/////////////////////////////////////////////////////////////////

// Affichages

void displayAlert() {
  if (millis() - alertStarted >= alertTime) {
    alert = ""; alertStarted = 0; alertTime = 0;
    return;
  }

  fill(255);
  rectMode(CORNER);
  rect(offsetX + w/2, offsetY + w/2, 7*w, 1.5*w, 5, 5, 5, 5);

  imageMode(CORNER);
  image(warning, offsetX + 0.625*w, offsetY + 0.625*w, 1.25*w, 1.25*w);

  textAlign(CENTER, CENTER);
  fill(color(#b33430));
  textSize(28 * w/75);
  text(alert, offsetX + 4.5*w, offsetY + 1.25*w);
}

void displayMoutonAlert() {
  if (millis() - messageMoutonStarted >= messageMoutonTime) {
    messageMouton = ""; messageMoutonStarted = 0; messageMoutonTime = 0;
    return;
  }

  fill(255);
  rectMode(CORNER);
  rect(alertPos.x, alertPos.y, 6*w, 2*w, 5, 5, 5, 5);

  imageMode(CORNER);
  image(mouton, alertPos.x + 0.125*w, alertPos.y + 0.125*w, 1.75*w, 1.75*w);

  textAlign(CENTER, CENTER);
  textSize(28 * w/75);
  fill(color(#b33430));
  text("Nouveau message :", alertPos.x + 4*w, alertPos.y + 0.5*w);
  strokeWeight(2);
  stroke(color(#b33430));
  line(alertPos.x + 2.25*w, alertPos.y + 0.8*w, alertPos.x + 5.75*w, alertPos.y + 0.8*w);

  textAlign(LEFT, CENTER);
  textSize(23 * w/75);
  fill(color(#000000));
  text(messageMouton, alertPos.x + 2*w, alertPos.y + 1.1*w);
}

void blur(int alpha) {
  fill(220, 220, 220, alpha);
  rectMode(CORNER);
  rect(offsetX, offsetY, rows * w, cols * w);
}

void drawHackerPage() {
  blur(150);

  // Rectange
  fill(255);
  float rectX = (width-offsetX)/2 + offsetX;
  float rectY = (height-offsetY)/2 + offsetY;
  float rectW = 7*w, rectH = 3*w;
  rectMode(CENTER);
  rect(rectX, rectY, rectW, rectH);

  // Image
  imageMode(CORNER);
  image(botLarge, rectX - rectW/2 + 10*w/75, rectY - rectH/2 + 10*w/75, 90 * w/75, 90 * w/75);

  // Titre
  fill(color(#b33430));
  textAlign(CENTER, CENTER);
  textSize(35 * w/75);
  text("Hacker " + (hackerAPImode ? "API " : "") + "mode activé", rectX + (100*w/75)/2, rectY - rectH/2 + 55*w/75);

  // Texte de configuration
  if (!hackerAPImode) {
    String hackerText;
    if (upLeftCorner == null) hackerText = "Calibrer le coin haut-gauche";
    else if (downRightCorner == null) hackerText = "Calibrer le coin bas-droite";
    else hackerText = "Calibrer la nouvelle partie";
    fill(0);
    noStroke();
    textSize(27 * w/75);
    text(hackerText, (width-offsetX)/2 + offsetX, rectY + (100*w/75)/3);

    String hg = (upLeftCorner == null) ? "___" : str(upLeftCorner.x) + " ; " + str(upLeftCorner.y);
    String bd = (downRightCorner == null) ? "___" : str(downRightCorner.x) + " ; " + str(downRightCorner.y);
    textSize(20 * w/75);
    text("HG : " + hg + "     " + "BD : " + bd, (width-offsetX)/2 + offsetX, rectY + (100*w/75)/1.15);
  } else {
    fill(0);
    noStroke();
    textSize(27 * w/75);
    text("En attente de l'API...", (width-offsetX)/2 + offsetX, rectY + (100*w/75)/2);
  }
}

void drawSavedPosition() {
  blur(220);
  for (ButtonFEN b : savedFENSbuttons) b.show();
}

void drawParameters() {
  blur(220);
  fill(0);
  stroke(0);
  textSize(35 * w/75);
  textAlign(LEFT, CENTER);
  text("Trait :", offsetX + w/2, offsetY + w/2);
  line(offsetX + w/2, offsetY + 0.875*w, offsetX + w/2 + textWidth("Trait :"), offsetY + 0.875*w);

  text("Roques :", offsetX + w/2, offsetY + 2.5*w);
  line(offsetX + w/2, offsetY + 2.875*w, offsetX + w/2 + textWidth("Roques :"), offsetY + 2.875*w);
}

void drawInfoBox(String i) {
  noStroke();
  fill(pointDeVue ? 255 : color(49, 46, 43));
  rectMode(CENTER);
  rect((width-offsetX)/2 + offsetX, offsetY + w/2, 15 * w/75 * i.length(), 50 * w/75);
  fill(pointDeVue ? color(49, 46, 43) : 255);
  textAlign(CENTER, CENTER);
  textSize(25 * w/75);
  text(i, (width-offsetX)/2 + offsetX, offsetY + w/2.3);
}

void drawPlayersInfos() {
  Joueur j1 = joueurs.get(0);
  Joueur j2 = joueurs.get(1);
  int space = (int)(offsetX - w) / 2 + 1;
  imageMode(CORNER);
  textAlign(CENTER, CENTER);
  textSize(13 * w/75);
  fill(255);
  noStroke();

  if (pointDeVue) {
    image(j1Img, space, height-(space+w), w, w);
    image(j2Img, space, offsetY + ( (offsetY<=10) ? space : 0), w, w);

    text( (j1.name == "LesMoutons" ? "Mouton" : j1.name)  + " (" + j1.elo + ")", space+w/2, height-(space+w)-space-5);
    text( (j2.name == "LesMoutons" ? "Mouton" : j2.name) + " (" + j2.elo + ")", space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w);

    text(roundedString(j1.getScore()) + "/" + j1.getTotalScore(), space+w/2, height-(space+w)-space-40);
    text(roundedString(j2.getScore()) + "/" + j2.getTotalScore(), space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w+40);
    if (j1.lastEval != "") text("Eval : " + j1.lastEval, space+w/2, height-(space+w)-space-80);
    if (j2.lastEval != "") text("Eval : " + j2.lastEval, space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w+80);
  } else {
    image(j1Img, space, offsetY + ( (offsetY<=10) ? space : 0), w, w);
    image(j2Img, space, height-(space+w), w, w);

    text( (j1.name == "LesMoutons" ? "Mouton" : j1.name) + " (" + j1.elo + ")", space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w);
    text( (j2.name == "LesMoutons" ? "Mouton" : j2.name) + " (" + j2.elo + ")", space+w/2, height-(space+w)-space-5);

    text(roundedString(j1.getScore()) + "/" + j1.getTotalScore(), space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w+40);
    text(roundedString(j2.getScore()) + "/" + j2.getTotalScore(), space+w/2, height-(space+w)-space-40);
    if (j1.lastEval != "") text("Eval : " + j1.lastEval, space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w+80);
    if (j2.lastEval != "") text("Eval : " + j2.lastEval, space+w/2, height-(space+w)-space-80);
  }
}

void drawEndScreen(float y) {
  noStroke();
  int gris = color(#666463);
  int vert = color(#8da75a);
  //int rouge = color(#b33430);

  blur(150);

  // grand rectangle
  float rectX = 1.75*w + offsetX, rectY = y;
  float rectW = 4.5*w, rectH = 3*w;
  fill(255);
  rect(rectX, rectY, rectW, rectH, 5);

  // images
  float imgW = 1.2 * w;
  float space = ((rectY + rectH) - (rectY + rectH/3) - imgW)/2; //se simplifie très probablement
  float imgY = (rectY + rectH/3) + space/1.5;
  imageMode(CORNER);
  image(j1ImgEnd, rectX+space, imgY, imgW, imgW);
  image(j2ImgEnd, rectX+rectW-space-imgW, imgY, imgW, imgW);

  // vs
  fill(gris);
  textSize(20* w/75);
  text("VS", rectX+rectW/2, imgY + imgW/2);

  // raison
  fill(gris);
  textSize(15* w/75);
  text(endReason, rectX+rectW/2, rectY+rectH - space/1.5);

  // gagnant
  if (winner != 2) {
    stroke(vert);
    strokeWeight(6 * w/75);
    noFill();
    rect((winner == 0) ? (rectX+space) : (rectX+rectW-space-imgW), imgY, imgW, imgW, 5);
  }

  // petit rectange et texte
  String title = "";
  if (winner == 2) { //nulle
    fill(gris);
    title = "Nulle";
  }
  else if (joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") { //humain contre machine
    if (winner == 0) { fill(vert); title = "Vous avez gagné !"; }
    else { fill(gris); title = joueurs.get(1).victoryTitle; }
  }
  else if (joueurs.get(0).name != "Humain" && joueurs.get(1).name == "Humain") { //machine contre humain
    if (winner == 1) { fill(vert); title = "Vous avez gagné !"; }
    else { fill(gris); title = joueurs.get(0).victoryTitle; }
  }
  else {
    title = joueurs.get(winner).victoryTitle;
    fill(vert);
  }
  noStroke();
  rect(rectX, rectY, rectW, rectH/3, 5, 5, 0, 0);
  fill(255);
  textAlign(CENTER, CENTER);
  textSize(30 * w/75);
  text(title, rectX + rectW/2, rectY + rectH/6.5);
}

/////////////////////////////////////////////////////////////////

// Plateau et presets

void updateBoard() {
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].show();
    }
  }

  for (int i = 0; i < piecesToDisplay.size(); i++) {
    piecesToDisplay.get(i).show();
  }
}

void deselectAll() {
  for (int i = 0; i < pieces.length; i++) {
    for (int j = 0; j < pieces[i].size(); j++) {
      pieces[i].get(j).select(false);
    }
  }

  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].selected = false;
      grid[i][j].possibleMove = null;
      grid[i][j].freeMove = false;
    }
  }
}

void playerPromote(int numButton) {
  removePiece(enPromotion);

  if (numButton == 0) { //promotion en dame
    pieces[enPromotion.c].add(new Dame(enPromotion.i, enPromotion.c*7, enPromotion.c));
    materials[enPromotion.c] += 800;
    addPgnChar("Q");
  } else if (numButton == 1) { //en tour
    pieces[enPromotion.c].add(new Tour(enPromotion.i, enPromotion.c*7, enPromotion.c));
    materials[enPromotion.c] += 400;
    addPgnChar("R");
  } else if (numButton == 2) { //en fou
    pieces[enPromotion.c].add(new Fou(enPromotion.i, enPromotion.c*7, enPromotion.c));
    materials[enPromotion.c] += 230;
    addPgnChar("B");
  } else if (numButton == 3) { //en cavalier
    pieces[enPromotion.c].add(new Cavalier(enPromotion.i, enPromotion.c*7, enPromotion.c));
    materials[enPromotion.c] += 220;
    addPgnChar("N");
  }

  enPromotion = null;

  // On retire le hash précédent qui est faux à cause de la promotion humain et on ajoute le hash calculé à partir de 0
  removeLastFromHashHistory();
  zobrist.initHash();
  zobristHistory.add(zobrist.hash);

  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);

  if (tourDeQui == 0) tourDeQui = 1;
  else tourDeQui = 0;
}

void clickedOnBoard(int i, int j) {
  Piece p = grid[i][j].piece;
  if (stats && details) println("Case : [" + i + "][" + j + "] (" + grid[i][j].name + ")");

  if (grid[i][j].possibleMove != null) {
      grid[i][j].possibleMove.play();
      pieceSelectionne = null;
      return;
  }
  if (p == null || p.c != tourDeQui) {
    pieceSelectionne = null;
    deselectAll();
    return;
  }
  if (p.c == tourDeQui) {
    deselectAll();
    p.select(true);
    grid[p.i][p.j].selected = true;
    pieceSelectionne = p;
    return;
  }
}

void setPieces() {
  importFEN(startFEN);

  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);

  calcEndGameWeight();
  zobrist.initHash();
  // println("Hash de la position : " + zobrist.hash);
}

int getGridI() {
  if (mouseX < offsetX || mouseX > cols*w + offsetX || mouseY < offsetY || mouseY > rows*w + offsetY) return -1;

  int i;
  if (pointDeVue) i = (int)(mouseX-offsetX)/w;
  else i = 7 - (int)(mouseX-offsetX)/w;
  return i;
}

int getGridJ() {
  if (mouseX < offsetX || mouseX > cols*w + offsetX || mouseY < offsetY || mouseY > rows*w + offsetY) return -1;

  int j;
  if (pointDeVue) j = (int)(mouseY-offsetY)/w;
  else j = 7 - (int)(mouseY-offsetY)/w;
  return j;
}

void showPromoButtons() {
  for (int i = 0; i < promoButtons.size(); i++) {
    promoButtons.get(i).show(enPromotion.c);
  }
}

void removeAllPieces() {
  pieces[0].clear();
  pieces[1].clear();
  for (int i = 0; i < 8; i++) {
    for (int j = 0; j < 8; j++) {
      grid[i][j].piece = null;
    }
  }

  //while (pieces[c].size() != 1) {
  //  Piece p = pieces[c].get(0);
  //  if (p == rois[c]) p = pieces[c].get(1);
  //  removePiece(p);
  //  grid[p.i][p.j].piece = null;
  //}
}

void playSound(Move m) {
  if (soundControl < 1 || (gameEnded && !rewind)) return;

  if (m.special == 3) { enPassant.play(); }
  if (playerInCheck(2) != -1) { check_sound.play(); return; }
  if (m.special == 1 || m.special == 2) { castle_sound.play(); return; }
  if (m.capture != null) { prise_sound.play(); return; }

  move_sound.play();
}

void setMoveMarks(Cell c1, Cell c2) {
  clearMoveMarks();
  c1.moveMark = true;
  c2.moveMark = true;
}

void clearMoveMarks() {
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].moveMark = false;
    }
  }
}

boolean pieceHovered() {
  int i = getGridI();
  int j = getGridJ();
  if (i >= 0 && i < cols && j >= 0 && j < rows) {
    if (grid[i][j].piece != null) return true;
  }

  return false;
}

void addPieceToBoardByDrop(int value, int i, int j) {

  switch (value) {
    case 0: pieces[0].add(new Roi(i, j, 0)); break;
    case 1: pieces[0].add(new Dame(i, j, 0)); break;
    case 2: pieces[0].add(new Tour( i, j, 0)); break;
    case 3: pieces[0].add(new Fou(i, j, 0)); break;
    case 4: pieces[0].add(new Cavalier(i, j, 0)); break;
    case 5: pieces[0].add(new Pion(i, j, 0)); break;

    case 6: pieces[1].add(new Roi(i, j, 1)); break;
    case 7: pieces[1].add(new Dame(i, j, 1)); break;
    case 8: pieces[1].add(new Tour( i, j, 1)); break;
    case 9: pieces[1].add(new Fou(i, j, 1)); break;
    case 10: pieces[1].add(new Cavalier(i, j, 1)); break;
    case 11: pieces[1].add(new Pion(i, j, 1)); break;
  }

  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);
}

void bulletPreset() {
  if (timeControl) {
    times[0][0] = 1; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 1; times[1][1] = 0; times[1][2] = 0;
  }
  t1.setValue(720);
  t2.setValue(720);
}

void blitzPreset() {
  if (timeControl) {
    times[0][0] = 3; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 3; times[1][1] = 0; times[1][2] = 0;
  }
  t1.setValue(2214);
  t2.setValue(2214);
}

void rapidPreset() {
  if (timeControl) {
    times[0][0] = 10; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 10; times[1][1] = 0; times[1][2] = 0;
  }
  t1.setValue(7500);
  t2.setValue(7500);
}

void noTimePreset() {
  if (timeControl) {
    times[0][0] = 0; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 0; times[1][1] = 0; times[1][2] = 0;
  }
}

/////////////////////////////////////////////////////////////////

// FEN, historiques et données

String[] parseHTMLData(String str) {
  String[] m = split(str, "<div class=\"piece ");
  String[] pieceData = new String[m.length-1];

  for (int i = 1; i < m.length; i++) {
    if (m[i].charAt(0) != 's') pieceData[i-1] = "" + m[i].charAt(0) + m[i].charAt(1) + m[i].charAt(10) + m[i].charAt(11);
    else pieceData[i-1] = "" + m[i].charAt(10) + m[i].charAt(11) + m[i].charAt(7) + m[i].charAt(8);
  }
  printArray(pieceData);
  return pieceData;
}

void HTMLtoBoard(String str) {
  removeAllPieces();
  String[] parsedData = parseHTMLData(str);

  for (int n = 0; n < parsedData.length; n++) {
    String d = parsedData[n];
    int c = (d.charAt(0) == 'w' ? 0 : 1);
    int i = stringToInt(str(d.charAt(2)))-1;
    int j = abs(stringToInt(str(d.charAt(3)))-8);
    switch (str(d.charAt(1))) {
      case "k": pieces[c].add(new Roi(i, j, c)); break;
      case "q": pieces[c].add(new Dame(i, j, c)); break;
      case "r": pieces[c].add(new Tour( i, j, c)); break;
      case "b": pieces[c].add(new Fou(i, j, c)); break;
      case "n": pieces[c].add(new Cavalier(i, j, c)); break;
      case "p": pieces[c].add(new Pion(i, j, c)); break;
    }

    if (d.charAt(1) == 'r') {
      pieces[c].get(pieces[c].size()-1).setRoques(0, 0);
    }
  }

  println(">>> Plateau importé");

  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);

  calcEndGameWeight();
  zobrist.initHash();
}

void pasteFEN() {
  startFEN = GetTextFromClipboard();
  setPieces();
  println (">>> Fen importée");
}

void addFenToHistory(String f) {
  positionHistory.add(f);
}

void removeLastFromFenHistory() {
  positionHistory.remove(positionHistory.size() - 1);
}

void addHashToHistory(long hash) {
  zobristHistory.add(hash);
}

void removeLastFromHashHistory() {
  zobristHistory.remove(zobristHistory.size() - 1);
}

String generateFEN() {
  String fen = "";
  int vide = 0;

  //Position
  for (int j = 0; j < rows; j++) {
    for (int i = 0; i < cols; i++) {
      Cell g = grid[i][j];
      if (g.piece == null) {
        vide += 1;
      } else {
        if (vide > 0) fen = fen + str(vide);
        vide = 0;

        fen = fen + g.piece.code;
      }

      if (i == 7) {
        if (vide > 0) fen = fen + str(vide);
        vide = 0;
        fen = fen + (j < 7 ? '/' : ' ');
      }
    }
  }

  //Trait
  if (tourDeQui == 0) {
    fen = fen + "w";
  } else {
    fen = fen + "b";
  }

  //Roques
  boolean pRoques[] = {false, false}; //blancs, noirs
  boolean gRoques[] = {false, false};

  for (int i = 0; i < pieces.length; i++) {
    if (rois[i].roquable == 1) {
      for (int j = 0; j < pieces[i].size(); j++) {

        if (pieces[i].get(j).petitRoquable != -1) {
          if (pieces[i].get(j).petitRoquable == 1) {
            pRoques[i] = true;
          }
        }

        if (pieces[i].get(j).grandRoquable != -1) {
          if (pieces[i].get(j).grandRoquable == 1) {
            gRoques[i] = true;
          }
        }
      }
    }
  }

  if (pRoques[0] || pRoques[1] || gRoques[0] || gRoques[1]) {
    fen = fen + " ";
    if (pRoques[0]) fen = fen + "K";
    if (gRoques[0]) fen = fen + "Q";
    if (pRoques[1]) fen = fen + "k";
    if (gRoques[1]) fen = fen + "q";
  }

  return fen;
}

void importFEN(String f) { //fen simplifiée, sans en passant et règle des 50 coups
  removeAllPieces();

  //roques et trait
  int pRoqueB = 0, pRoqueN = 0, gRoqueB = 0, gRoqueN = 0;
  int roiRoqueB = 0; int roiRoqueN = 0;
  boolean stopEvaluatingEnd = false;

  for (int i = f.length() - 1; i >= 0; i--) {
    if (stopEvaluatingEnd) break;
    char c = f.charAt(i); //current char
    switch (c) {
      case ' ':
        break;
      case 'K': pRoqueB = 1; roiRoqueB = 1;
        break;
      case 'k': pRoqueN = 1; roiRoqueN = 1;
        break;
      case 'Q': gRoqueB = 1; roiRoqueB = 1;
        break;
      case 'q': gRoqueN = 1; roiRoqueN = 1;
        break;
      case 'w': tourDeQui = 0; stopEvaluatingEnd = true;
        break;
      case 'b': tourDeQui = 1; stopEvaluatingEnd = true;
        break;
    }
  }

  //importe la position
  int cursorI = 0, cursorJ = 0;
  boolean stopEvaluatingStart = false;

  for (int i = 0; i < f.length(); i++) {
    if (stopEvaluatingStart) break;
    char c = f.charAt(i); //current char
    int pieceColor = Character.isLowerCase(c) ? 1 : 0;

    switch (Character.toLowerCase(c)) {
      case '/':
        cursorI = 0; cursorJ++; //nouvelle ligne
      break;

      case ' ':
        stopEvaluatingStart = true;
      break;

      //pièces
      case 'k':
        rois[pieceColor] = new Roi(cursorI, cursorJ, pieceColor); pieces[pieceColor].add(rois[pieceColor]); cursorI++;
        rois[pieceColor].roquable = (pieceColor == 0 ? roiRoqueB : roiRoqueN);
      break;
      case 'q':
        pieces[pieceColor].add(new Dame(cursorI, cursorJ, pieceColor)); cursorI++;
      break;
      case 'r':
        pieces[pieceColor].add(new Tour(cursorI, cursorJ, pieceColor)); cursorI++;
      break;
      case 'b':
        pieces[pieceColor].add(new Fou(cursorI, cursorJ, pieceColor)); cursorI++;
      break;
      case 'n':
        pieces[pieceColor].add(new Cavalier(cursorI, cursorJ, pieceColor)); cursorI++;
      break;
      case 'p':
        pieces[pieceColor].add(new Pion(cursorI, cursorJ, pieceColor)); cursorI++;
      break;

      default:
      break;
    }

    if (Character.isDigit(c)) {
      String stringChar = String.valueOf(c);
      cursorI += Integer.valueOf(stringChar);
    }

  }

  //set les roques
  if (pRoqueB == 1) grid[7][7].piece.setRoques(1, 0);
  if (gRoqueB == 1) grid[0][7].piece.setRoques(0, 1);

  if (pRoqueN == 1) grid[7][0].piece.setRoques(1, 0);
  if (gRoqueN == 1) grid[0][0].piece.setRoques(0, 1);
}

/////////////////////////////////////////////////////////////////

// Fonctions pour calculs et recherche

float calcEndGameWeight() {
  float[] totals = {0, 0};

  for (int c = 0; c < 2; c++) {
    for (int i = 0; i < pieces[c].size(); i ++) {
      Piece p = pieces[c].get(i);
      if (p != rois[c] && c != PION_INDEX) {
        totals[c] += p.maireEval;
      }
    }
  }

  float val = (totals[0] + totals[1]) / 2;
  val = 1 - (val / TOTAL_DEPART);

  endGameWeight = constrain(val, 0, 1); //Si val > totalDepart
  return endGameWeight;
}

Piece removePiece(Piece piece) {
  for (int i = pieces[piece.c].size() - 1; i >= 0; i--) {
    if (pieces[piece.c].get(i) == piece) {
      grid[piece.i][piece.j].piece = null;
      Piece p = pieces[piece.c].get(i);
      pieces[piece.c].remove(i);
      return p;
    }
  }
  return null;
}

ArrayList<Piece> copyPieceArrayList(ArrayList<Piece> p) {
  ArrayList<Piece> copy = new ArrayList<Piece>();

  for (int i = 0; i < p.size(); i++) {
    copy.add(p.get(i));
  }

  return copy;
}

int countMaireMaterial(int c) {
  int material = 0;
  for (int i = 0; i < pieces[c].size(); i++) {
    material += pieces[c].get(i).maireEval;
  }
  return material;
}

ArrayList selectionSortMoves(ArrayList<Move> arr) {
  int min;

  //start passes.
  for (int i = 0; i < arr.size(); i++) {
    //index of the smallest element to be the ith element.
    min = i;

    //Check through the rest of the array for a lesser element
    for (int j = i + 1; j < arr.size(); j++) {
      if (-arr.get(j).scoreGuess < -arr.get(min).scoreGuess) { //les moins sont là pour inverser
        min = j;
      }
    }

    //compare the indexes
    if (min != i) {
      //swap
      Move m = arr.get(min);
      arr.set(min, arr.get(i));
      arr.set(i, m);
    }
  }

  return arr;
}

// Retourne le joueur en échec si 2 est passé en argument, sinon ne regarde que le joueur passé en argument
// Retourne -1 si aucun n'est en échec ou si le joueur testé n'est pas en échec

int playerInCheck(int checkColor) {

  if (checkColor == 2) {
    for (int i = 0; i < 2; i++) {
      int opColor = (int)pow((i - 1), 2);

      //tester si le roi est en prise après un coup adverse
      for (int j = 0; j < pieces[opColor].size(); j++) { //pour chaque piece adverse

        int indexP = pieces[opColor].get(j).pieceIndex;
        if (!(pc.getDistanceTable(indexP, rois[i].i, rois[i].j, pieces[opColor].get(j).i, pieces[opColor].get(j).j))) continue;

        ArrayList<Move> moves = pieces[opColor].get(j).generateMoves(false, false);

        for (int k = 0; k < moves.size(); k++) { //pour chaque coup de la pièce
          if (moves.get(k).i == rois[i].i && moves.get(k).j == rois[i].j) {
            return i; // joueur i en échec
          }
        }
      }
    }
    return -1;
  } else if (checkColor == 0 || checkColor == 1) {
    int opColor = (int)pow((checkColor - 1), 2);
    for (int j = 0; j < pieces[opColor].size(); j++) {
      int indexP = pieces[opColor].get(j).pieceIndex;
      if (!(pc.getDistanceTable(indexP, rois[checkColor].i, rois[checkColor].j, pieces[opColor].get(j).i, pieces[opColor].get(j).j))) continue;

      ArrayList<Move> moves = pieces[opColor].get(j).generateMoves(false, false);
      for (int k = 0; k < moves.size(); k++) {
        if (moves.get(k).i == rois[checkColor].i && moves.get(k).j == rois[checkColor].j) {
          return checkColor;
        }
      }
    }

    return -1;
  }

  return -1;
}

// Reçoit en paramètres la pièce qui a généré les coups et la liste des coups qu'elle a générée
// Renvoie la liste des coups illégaux : à optimiser

ArrayList<Move> findIllegalMoves(Piece piece, ArrayList<Move> pseudoMoves) {
  ArrayList<Move> illegalMoves = new ArrayList<Move>();

  //pour chaque pseudoMoves
  for (int i = 0; i < pseudoMoves.size(); i++) {

    //prévisualise le coup
    pseudoMoves.get(i).make();

    //teste si le roi est en prise après chaque coup adverse
    if (playerInCheck(piece.c) == piece.c) {
      illegalMoves.add(pseudoMoves.get(i));
    }

    //rétablit la position de départ
    pseudoMoves.get(i).unmake();
  }

  return illegalMoves;
}

boolean isMoveLegal(Piece p, Move move) {
  boolean isLegal = true;

  move.make();
  if (playerInCheck(p.c) == p.c) {
    isLegal = false;
  }
  move.unmake();

  return isLegal;
}

ArrayList<Move> removeIllegalMoves(Piece piece, ArrayList<Move> pseudoMoves) {

  boolean removeMove = false;

  //pour chaque pseudoMoves
  for (int i = pseudoMoves.size()-1; i >= 0; i--) {
    removeMove = false;

    //prévisualise le coup
    pseudoMoves.get(i).make();

    //teste si le roi est en prise après chaque coup adverse
    if (playerInCheck(piece.c) == piece.c) {
      removeMove = true;
    }

    //rétablit la position de départ
    pseudoMoves.get(i).unmake();

    if (removeMove) pseudoMoves.remove(i);
  }

  return pseudoMoves;
}

/////////////////////////////////////////////////////////////////
