/////////////////////////////////////////////////////////////////

// Hacker

// 1) Variables / Données
// 2) Calibration
// 3) Auto calibration
// 4) Scans
// 5) Hacker sans fin
// 6) Fonctions très utiles

/////////////////////////////////////////////////////////////////

// Pour ajouter une donnée de calibration :
// Augmenter de 1 CALIBRATION_NUMBER, ajouter l'index correspondant et une description dans calibrationDesc
// Accéder à la donnée par hackerPoints et l'index correspondant

/////////////////////////////////////////////////////////////////

final int CALIBRATION = 0; // Phase de calibration du hacker
final int INGAME = 1; // Partie en cours
final int END = 2; // Fin de partie détectée
final int WAITING_TO_RESTART = 3; // Demande de nouvelle partie effectuée, en attente d'une partie

// Index des calibrations
final int CALIBRATION_NUMBER = 3;
final int UPLEFT = 0;
final int DOWNRIGHT = 1;
final int NEWGAME = 2;

Point[] hackerPoints = new Point[CALIBRATION_NUMBER];
Point[] saveHackerPoints = new Point[CALIBRATION_NUMBER];
Color hackerWhitePieceColor, hackerBlackPieceColor;
String[] calibrationDesc = {"Coin haut-gauche", "Coin bas-droite", "Nouvelle partie"};

int hackerState = CALIBRATION;
boolean useHacker = false;
int currentHackerPOV = 0;
int timeAtLastRestartTry = 0;
int timeAtHackerEnd = 0;
int lastMoveTime = 0;
int numberOfScan = 0;
int numberOfRestartWait = 0;
Color colorOfRematch = null;
boolean isNextMoveRestranscrit = false;
Point[][] hackerCoords = new Point[8][8];
Point[][] saveHackerCoords = new Point[8][8];
ArrayList<Move> hackerMoves = new ArrayList<Move>();
ArrayList<Integer> deltaTimeHistory = new ArrayList<Integer>();

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

  println("[HACKER] Sauvegardes restaurées");
}

void forceCalibrationRestore() {
  if (noHackerSaves()) return;

  loadHackerSaves();
  prepareEngine();
  setupHacker();

  println("[HACKER] Restauration forcée des sauvegardes");
}

void calculateCalibrationData() {
  for (int i = 0; i < CALIBRATION_NUMBER; i++) {
    println("[HACKER] " + calibrationDesc[i] + " :"  , hackerPoints[i].x, hackerPoints[i].y);
  }

  int boardWidth = hackerPoints[DOWNRIGHT].x - hackerPoints[UPLEFT].x;
  int boardHeight = hackerPoints[DOWNRIGHT].y - hackerPoints[UPLEFT].y;

  for (int i = 0; i < 8; i++) {
    for (int j = 0; j < 8; j++) {
      hackerCoords[j][i].x = hackerPoints[UPLEFT].x + i*(boardWidth/7);
      hackerCoords[j][i].y = hackerPoints[UPLEFT].y + j*(boardHeight/7);
    }
  }
  currentHackerPOV = 0;

  hackerWhitePieceColor = hacker.getPixelColor(hackerCoords[7][7].x, hackerCoords[7][7].y);
  hackerBlackPieceColor = hacker.getPixelColor(hackerCoords[0][0].x, hackerCoords[0][0].y);
}

void resetCalibrationData() {
  for (int i = 0; i < CALIBRATION_NUMBER; i++) {
    hackerPoints[i] = null;
  }
  hackerWhitePieceColor = null;
  hackerBlackPieceColor = null;
  for (int i = 0; i < 8; i++) {
    for (int j = 0; j < 8; j++) {
      hackerCoords[i][j] = new Point();
    }
  }
}

void saveHackerData() {
  for (int i = 0; i < CALIBRATION_NUMBER; i++) {
    saveHackerPoints[i] = copyPoint(hackerPoints[i]);
  }
  saveHackerCoords = copyCoords(hackerCoords);
  println("[HACKER] Données du hacker sauvegardées");
}

void loadHackerSaves() {
  for (int i = 0; i < CALIBRATION_NUMBER; i++) {
    hackerPoints[i] = copyPoint(saveHackerPoints[i]);
  }
  hackerCoords = copyCoords(saveHackerCoords);
  currentHackerPOV = 0;
}

boolean noHackerSaves() {
  for (int i = 0; i < CALIBRATION_NUMBER; i++) {
    if (saveHackerPoints[i] == null) {
      alert("Sauvegarde manquante", 2500);
      return true;
    }
  }
  return false;
}

void prepareEngine() {
  if (play && !gameEnded && !rewind && isAIvsHumain() && !isHumainTurn()) {
    engineToPlay = true;
  }
}

void setupHacker() {
  clearAlert();
  hackerState = INGAME;
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

  for (int i = 0; i < CALIBRATION_NUMBER; i++) {
    if (hackerPoints[i] == null) {
      hackerPoints[i] = copyPoint(p);
      if (i == CALIBRATION_NUMBER-1) calibrerHacker();
      return;
    }
  }

  alert("Hacker déjà calibré", 2000);
  println("[HACKER] Hacker déjà calibré");
}

boolean verifyNewGameButton() {
  Color newGameColor = hacker.getPixelColor(hackerPoints[NEWGAME].x, hackerPoints[NEWGAME].y);
  if (newGameColor.getRed() > 120 && newGameColor.getGreen() > 120 && newGameColor.getBlue() > 120) {
    return false;
  }
  return true;
}

boolean verifyBoard(int tolerance, boolean confondu) {
  Color B = hackerWhitePieceColor;
  Color N = hackerBlackPieceColor;
  Color A = null;

  if (isSameColor(B, N)) return false;

  Color[][] expectedBoard = {
  {N, N, N, N, N, N, N, N},
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

boolean verifyCalibration(int tolerance, boolean confondu) {
  // Vérifie que l'on n'a pas calibré sur le + (ou pas)
  if (hackerSite == CHESSCOM && !verifyNewGameButton()) return false;

  // Tolerance représente nombre d'erreurs autorisé et confondu si on confond les deux couleurs de pièce
  return verifyBoard(tolerance, confondu);
}

void manualRestoreSaves() {
  restoreCalibrationSaves();
  if (isHumain(0) && !isHumain(1)) {
    setHackerPOV(1);
  }
}

void manualForceSaves() {
  forceCalibrationRestore();
  if (isHumain(0) && !isHumain(1)) {
    setHackerPOV(1);
  }
}

/////////////////////////////////////////////////////////////////

// Auto calibration

void autoCalibration() {
  resetCalibrationData();

  Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  Rectangle screenRectangle = new Rectangle(screenSize);
  BufferedImage image = hacker.createScreenCapture(screenRectangle);

  Color targetWhite = null, targetBlack = null;
  if (hackerSite == CHESSCOM) {
    targetWhite = expectChesscomWhitePieceColor;
    targetBlack = expectChesscomBlackPieceColor;
  }
  else if (hackerSite == LICHESS) {
    targetWhite = expectLichessWhitePieceColor;
    targetBlack = expectLichessBlackPieceColor;
  }

  Map<Integer, ArrayList<Integer>> coords = getPointsMap(image, targetBlack);

  int[] minimax = findIntervalY(coords);
  int minY = minimax[0];
  int maxY = minimax[1];

  Point[] points = findBoard(image, coords, minY, maxY, targetWhite, targetBlack);

  if (points[0] == null || points[1] ==  null) {
    println("[HACKER] Échec de l'auto calibration :(");
    alert("Échec de l'auto calibration", 1500);
  }
  else {
    println("[HACKER] Auto calibration réussie");
    hacker.mouseMove(points[0].x, points[0].y);
    delay(100);
    addPointToCalibration();
    delay(200);
    hacker.mouseMove(points[1].x, points[1].y);
    delay(100);
    addPointToCalibration();
  }
}

// Renvoie une liste des points de la couleur demandée rangés par coordonnée y
Map<Integer, ArrayList<Integer>> getPointsMap(BufferedImage image, Color targetBlack) {
  Map<Integer, ArrayList<Integer>> coords = new HashMap<Integer, ArrayList<Integer>>();

  for (int j = 0; j < displayHeight; j++) {
    for (int i = 0; i < displayWidth; i++) {

      if (!isSameColor(targetBlack, getColor(image, i, j))) continue;
      if (displayWidth-i > MINIMUM_PIXEL_DETECTION) {
        for (int n = i; n < i+MINIMUM_PIXEL_DETECTION; n++) {
          if (!isSameColor(targetBlack, getColor(image, n, j))) continue;
        }
      }

      if (coords.get(j) == null) coords.put(j, new ArrayList<Integer>());
      coords.get(j).add(i);
    }
  }

  return coords;
}

// Renvoie l'intervalle y dans lequel se trouve l'échiquier
int[] findIntervalY(Map<Integer, ArrayList<Integer>> coords) {
  // Le minimum est dans l'index 0 et le maximum dans l'index 1
  int[] minimax = {displayHeight, 0};

  for (Map.Entry<Integer, ArrayList<Integer>> set : coords.entrySet()) {
    minimax[0] = min(minimax[0], set.getKey());
    minimax[1] = max(minimax[1], set.getKey());
  }
  return minimax;
}

// Vérifie que l'échiquier (essai d'auto calibration) correspond bien à un échiquier
boolean autoVerifyBoard(int xDepart, int yDepart, int caseWidth, Color whitePiece, Color blackPiece, BufferedImage image) {
  Color B = whitePiece;
  Color N = blackPiece;
  Color A = null;

  if (isSameColor(B, N)) return false;

  Color[][] expectedBoard = {
  {N, N, N, N, N, N, N, N},
  {N, N, N, N, N, N, N, N},
  {A, A, A, A, A, A, A, A},
  {A, A, A, A, A, A, A, A},
  {A, A, A, A, A, A, A, A},
  {A, A, A, A, A, A, A, A},
  {B, B, B, B, B, B, B, B},
  {B, B, B, B, B, B, B, B}};

  Color[][] scannedBoard = new Color[8][8];
  for (int i = 0; i < 8; i++) {
    for (int j = 0; j < 8; j++) {

      int x = xDepart + i*caseWidth;
      int y = yDepart + j*caseWidth;
      if (x >= 0 && x < displayWidth && y >= 0 && y < displayHeight) scannedBoard[i][j] = getColor(image, x, y);
      else scannedBoard[i][j] = Color.black;

      if (expectedBoard[j][i] == null) {
        if (isSimilarColor(scannedBoard[i][j], whitePiece) || isSimilarColor(scannedBoard[i][j], blackPiece)) {
          return false;
        }
        continue;
      }
      if (!isSimilarColor(scannedBoard[i][j], expectedBoard[j][i])) return false;
    }
  }
  return true;
}

// Cherche l'échiquier dans l'écran
// Si il est trouvé, renvoie un couple de points correspondant aux deux points de calibration
Point[] findBoard(BufferedImage image, Map<Integer, ArrayList<Integer>> coords, int minY, int maxY, Color whiteColor, Color blackColor) {
  Point[] points = {null, null};

  for (int y = minY; y < maxY + 1; y++) {
    if (coords.get(y) == null) continue;

    ArrayList<Integer> valuesX = coords.get(y);
    int testW = (valuesX.get(getLastCoordIndex(valuesX)) - valuesX.get(0)) / 7;

    // On essaie avec (x+5, y, testW)
    if (testW != 0) {
      int index = 0;
      if (hackerSite == CHESSCOM) index = (valuesX.size() < 6 ? 0 : 5);
      if (hackerSite == LICHESS) index = (valuesX.size() < 11 ? 0 : 10);

      if (autoVerifyBoard(valuesX.get(index), y, testW, whiteColor, blackColor, image)) {
        points[0] = new Point(valuesX.get(index), y);
        points[1] = new Point(valuesX.get(index) + 7*testW, y + 7*testW);
        return points;
      }
    }
  }
  return points;
}

// Récupère l'index de la première coordonnée x de la dernière supposée pièce de la ligne
int getLastCoordIndex(ArrayList<Integer> values) {
  for (int i = values.size()-1; i >= 1; i--) {
    if (values.get(i) - values.get(i-1) > 1) return i;
  }
  return 0;
}

// Récupère la couleur d'un point dans une image
Color getColor(BufferedImage image, int x, int y) {
  int rgb = image.getRGB(x, y);
  return new Color ((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
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

  // Détection du hacker
  if (numberOfScan >= scansBetweenEndDetect) {
    numberOfScan = 0;
    if (hackerEndDetected()) {
      endOnHackerDetect();
      return;
    }
  }

  if (!isHumainTurn()) return;

  Move sm = getMoveOnBoard();
  if (sm != null) {
    isNextMoveRestranscrit = true;
    sm.play();
    if (!blockPlaying() && isAIvsHumain() && !isHumainTurn()) engineToPlay = true;
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

  hacker.mouseMove(hackerPoints[NEWGAME].x, hackerPoints[NEWGAME].y);
  delay(200);

  if (isSimilarColor(hacker.getPixelColor(hackerPoints[NEWGAME].x, hackerPoints[NEWGAME].y), endColorLichess)) {
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
  if (!hackerSansFin) return;
  if (hackerPoints[NEWGAME] == null) {
    error("hackStartGame", "Nouvelle partie non calibrée");
    return;
  }

  println("RESTARTING");

  deselectAll();

  Point mouse = MouseInfo.getPointerInfo().getLocation();
  int x = mouse.x;
  int y = mouse.y;

  colorOfRematch = hacker.getPixelColor(hackerPoints[NEWGAME].x, hackerPoints[NEWGAME].y);
  click(hackerCoords[0][0].x, hackerCoords[0][0].y);
  delay(2500);
  click(hackerPoints[NEWGAME].x, hackerPoints[NEWGAME].y);
  delay(20);
  click(x, y);
  delay(5);

  deselectAll();
  hackerState = WAITING_TO_RESTART;
  timeAtLastRestartTry = millis();
}

void handleWaitForRestart() {
  if (millis() - timeAtLastRestartTry < hackerTestRestartCooldown) return;

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
    if (isSimilarColor(colorOfRematch, hacker.getPixelColor(hackerPoints[NEWGAME].x, hackerPoints[NEWGAME].y), 5)) {
      println("PROTECTION ACTIVÉE");
      return;
    }

    println("PROTECTION PASSÉE");
  }

  boolean isGameStarted = verifyCalibration(2, true);
  if (isGameStarted) {
    int botColor;

    if (currentHackerPOV == 0) botColor = isSimilarColor(hacker.getPixelColor(hackerCoords[7][7].x, hackerCoords[7][7].y), hackerWhitePieceColor) ? 0 : 1;
    else botColor = isSimilarColor(hacker.getPixelColor(hackerCoords[0][0].x, hackerCoords[0][0].y), hackerWhitePieceColor) ? 0 : 1;

    delay(500);
    String iaType = (isHumain(0) ? joueurs.get(1).name : joueurs.get(0).name);
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
  if (hackerSite == CHESSCOM) {
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
  int delay = floor(random(80, 100));
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
  int player = (isHumain(0) ? 0 : 1);
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
