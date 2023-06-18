/////////////////////////////////////////////////////////////////

// Hacker

// 1) Variables / Données
// 2) Calibration
// 3) Scans
// 4) Hacker sans fin
// 5) Fonctins très utiles

/////////////////////////////////////////////////////////////////

Point upLeftCorner, downRightCorner, newgameLocation;
Point saveUpLeftCorner, saveDownRightCorner, saveNewgameLocation;
Color hackerWhitePieceColor, hackerBlackPieceColor;
Color saveWhitePieceColor, saveBlackPieceColor;

Color colorOfRematch = null;
boolean hackerWaitingToRestart = false;
int timeAtLastRestartTry = 0;
int currentHackerPOV = 0;
int timeAtHackerEnd = 0;
int lastMoveTime = 0;
int numberOfScan = 0;
int numberOfRestartWait = 0;
boolean isNextMoveRestranscrit = false;
boolean useHacker = false;
boolean hackerPret = false;
boolean hackerAPImode = false;
Point[][] hackerCoords = new Point[8][8];
Point[][] saveHackerCoords = new Point[8][8];
ArrayList<Move> hackerMoves = new ArrayList<Move>();
ArrayList<Float> deltaTimeHistory = new ArrayList<Float>(); // en secondes

/////////////////////////////////////////////////////////////////

// Calibration

void calibrerHacker() {
  calculateCalibrationData();

  if (!verifyCalibration(0, false)) {
    resetCalibrationData();
    alert("Échec de la calibration", 1500);
    return;
  }

  saveHackerData();
  prepareEngine();
  setupHacker();

  alert("Hacker calibré avec succès", 1500);
  println("[HACKER] Hacker calibré avec succès (ou pas)");
}

void restoreCalibrationSaves() {
  if (noHackerSaves()) return;

  loadHackerSaves();

  if (!verifyCalibration(0, false)) {
    resetCalibrationData();
    alert("Échec de la calibration", 1500);
    return;
  }

  prepareEngine();
  setupHacker();

  alert("Sauvegardes restaurées", 1500);
  println("[HACKER] Sauvegardes restaurées");
}

void forceCalibrationRestore() {
  if (noHackerSaves()) return;

  loadHackerSaves();
  prepareEngine();
  setupHacker();

  alert("Calibration forcée", 1500);
  println("[HACKER] Restauration forcée des sauvegardes");
}

void calculateCalibrationData() {
  println("[HACKER] Haut-gauche :", upLeftCorner.x, upLeftCorner.y);
  println("[HACKER] Bas-droite :", downRightCorner.x, downRightCorner.y);
  println("[HACKER] Nouvelle partie :", newgameLocation.x, newgameLocation.y);

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
}

void resetCalibrationData() {
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
}

void saveHackerData() {
  saveUpLeftCorner = upLeftCorner;
  saveDownRightCorner = downRightCorner;
  saveHackerCoords = copyCoords(hackerCoords);
  saveWhitePieceColor = hackerWhitePieceColor;
  saveBlackPieceColor = hackerBlackPieceColor;
  saveNewgameLocation = newgameLocation;
  println("[HACKER] Données du hacker sauvegardées");
}

void loadHackerSaves() {
  upLeftCorner = copyPoint(saveUpLeftCorner);
  downRightCorner = copyPoint(saveDownRightCorner);
  newgameLocation = copyPoint(saveNewgameLocation);
  hackerWhitePieceColor = copyColor(saveWhitePieceColor);
  hackerBlackPieceColor = copyColor(saveBlackPieceColor);
  hackerCoords = copyCoords(saveHackerCoords);
  currentHackerPOV = 0;
}

boolean noHackerSaves() {
  if (saveUpLeftCorner == null || saveDownRightCorner == null || saveNewgameLocation == null || saveWhitePieceColor == null || saveBlackPieceColor == null) {
    alert("Aucune sauvegarde", 2500);
    return true;
  }
  return false;
}

void prepareEngine() {
  if (play && !gameEnded && !rewind) {
    if ((joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") || (joueurs.get(0).name != "Humain" && joueurs.get(1).name == "Humain")) {
      if (joueurs.get(tourDeQui).name != "Humain") engineToPlay = true;
    }
  }
}

void setupHacker() {
  hackerPret = true;
  if (MODE_SANS_AFFICHAGE) {
    surface.setSize(150, 150);
    surface.setLocation(displayWidth-150, 0);
    hacker.mouseMove(displayWidth-75, 120);
    sa.hide();
    ta.hide();
    ga.hide();
  }
  frameRate(HACKER_RATE);
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
  else println("[HACKER] Hacker déjà calibré");
}

boolean verifyCalibration(int tolerance, boolean confondu) {
  // Tolerance représente nombre d'erreurs autorisé et confondu si on confond les deux types de pièce
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

void manualRestoreSaves() {
  restoreCalibrationSaves();
  if (joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") {
    setHackerPOV(1);
  }
}

void manualForceSaves() {
  forceCalibrationRestore();
  if (joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") {
    setHackerPOV(1);
  }
}

/////////////////////////////////////////////////////////////////

// Scans

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

  // println("[HACKER] Scan terminé en " + (millis()-before) + " ms");

  return scannedBoard;
}

boolean isMovePlayed(Move m) {
  Color coupColorWhite = null, coupColorBlack = null;
  if (hackerSite == LICHESS) {
    coupColorWhite = coupLichessWhite;
    coupColorBlack = coupLichessBlack;
  } else if (hackerSite == CHESSCOM) {
    coupColorWhite = coupChesscomWhite;
    coupColorBlack = coupChesscomBlack;
  }

  Color scannedFrom = hacker.getPixelColor(hackerCoords[m.fromJ][m.fromI].x, hackerCoords[m.fromJ][m.fromI].y);
  if (isSameColor(scannedFrom, coupColorWhite) || isSameColor(scannedFrom, coupColorBlack)) {
    Color scannedAt = hacker.getPixelColor(hackerCoords[m.j][m.i].x, hackerCoords[m.j][m.i].y);
    if (isSimilarColor(scannedAt, (tourDeQui == 0 ? hackerWhitePieceColor : hackerBlackPieceColor))) {
      return true;
    }
  }
  return false;
}

Move getMoveOnBoard() {
  if (hackerMoves.size() == 0) updateHackerMoves();

  for (int i = 0; i < hackerMoves.size(); i++) {
    Move m = hackerMoves.get(i);

    if (isMovePlayed(m)) {
      for (int n = 0; n < hackerMoves.size(); n++) { // Vérification du roque
        Move testedMove = hackerMoves.get(n);
        if (testedMove.special == 0 || i == n) continue;
        if (isMovePlayed(testedMove)) {
          hackerMoves.clear();
          return testedMove;
        }
      }

      hackerMoves.clear();
      return m;
    }
  }
  return null;
}

void scanMoveOnBoard() {
  numberOfScan++;

  // Détection du hacker sans fin
  if (hackerSansFin && numberOfScan >= scansBetweenEndDetect) {
    numberOfScan = 0;
    if (hackerEndDetected()) {
      endOnHackerDetect();
      return;
    }
  }

  if (joueurs.get(tourDeQui).name != "Humain") return;

  Move sm = getMoveOnBoard();
  if (sm != null) {
    isNextMoveRestranscrit = true;
    sm.play();
    if (!blockPlaying) {
      if ((joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") || (joueurs.get(0).name != "Humain" && joueurs.get(1).name == "Humain")) {
        if (joueurs.get(tourDeQui).name != "Humain") engineToPlay = true;
      }
    }
  }
}

/////////////////////////////////////////////////////////////////

// Hacker sans fin

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
  delay(1500);
  click(newgameLocation.x, newgameLocation.y);
  delay(190);
  click(x, y);
  delay(20);
  colorOfRematch = hacker.getPixelColor(newgameLocation.x, newgameLocation.y);

  deselectAll();
  hackerWaitingToRestart = true;
}

void handleWaitForRestart() {
  timeAtLastRestartTry = millis();
  numberOfRestartWait++;

  if (hackerSite == CHESSCOM) {
    // Protection anti-revanche
    if (numberOfRestartWait == waitsBetweenStartRetry) {
      numberOfRestartWait = 0;
      hackStartGame();
      return;
    }

    // Protection anti-annulation
    // if (isSameColor(colorOfRematch, hacker.getPixelColor(newgameLocation.x, newgameLocation.y))) return;
  }

  boolean isGameStarted = verifyCalibration(2, true);
  if (isGameStarted) {
    int botColor;

    if (currentHackerPOV == 0) botColor = isSimilarColor(hacker.getPixelColor(hackerCoords[7][7].x, hackerCoords[7][7].y), hackerWhitePieceColor) ? 0 : 1;
    else botColor = isSimilarColor(hacker.getPixelColor(hackerCoords[0][0].x, hackerCoords[0][0].y), hackerWhitePieceColor) ? 0 : 1;

    delay(500);
    String iaType = ((joueurs.get(0).name == "Humain") ? joueurs.get(1).name : joueurs.get(0).name);
    // La pov change quelque part !!! ATTENTION ANTI ANNULATION DESACTIVÉE
    newAIGame(botColor, iaType);
    forceCalibrationRestore();
    setHackerPOV(botColor);
  }
}

/////////////////////////////////////////////////////////////////

// Fonctions très utiles

void click(int x, int y) {
  hacker.mouseMove(x, y);
  hacker.mousePress(InputEvent.BUTTON1_DOWN_MASK);
  delay(5);
  hacker.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
}

void cheat(int c, int fromI, int fromJ, int i, int j, int special) {

  // Détection du hacker sans fin sur chess.com
  if (hackerSansFin && hackerSite == CHESSCOM) {
    if (hackerEndDetected()) return;
  }

  // Sauvegarde les coordonnées du curseur
  Point mouse = MouseInfo.getPointerInfo().getLocation();
  int x = mouse.x;
  int y = mouse.y;
  int promoDir = (c == 0 ? 1 : -1);

  // Prend le focus de chess.com
  delay(10);
  click(hackerCoords[fromJ][fromI].x, hackerCoords[fromJ][fromI].y);

  // Joue le coup
  click(hackerCoords[fromJ][fromI].x, hackerCoords[fromJ][fromI].y);
  int delay = (int)random(80, 100);
  delay(delay);
  click(hackerCoords[j][i].x, hackerCoords[j][i].y);

  if (special >= 5) delay(30);
  if (special == 5) click(hackerCoords[j][i].x, hackerCoords[j][i].y);
  else if (special == 6) click(hackerCoords[j][i].x, hackerCoords[j+2*promoDir][i].y);
  else if (special == 7) click(hackerCoords[j][i].x, hackerCoords[j+3*promoDir][i].y);
  else if (special == 8) click(hackerCoords[j][i].x, hackerCoords[j+promoDir][i].y);

  // Revient à la position initiale
  click(x, y);
  delay(10);

  // Déselectionne les pièces au cas où il clique dessus
  deselectAll();
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

void updateHackerMoves() {
  hackerMoves.clear();
  int player = (joueurs.get(0).name == "Humain" ? 0 : 1);
  ArrayList<Move> movesGenerated = generateAllLegalMoves(player, true, false);
  for (int i = 0; i < movesGenerated.size(); i++) {
    if (movesGenerated.get(i).special == 4) movesGenerated.get(i).special = 5;
    if (movesGenerated.get(i).special != 0) hackerMoves.add(0, movesGenerated.get(i));
    else hackerMoves.add(movesGenerated.get(i));
  }
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
