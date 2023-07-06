/////////////////////////////////////////////////////////////////

// Fonctions concernant la gestion de la partie (démarrage, redémarrage, fin de partie...)

/////////////////////////////////////////////////////////////////

final int MENU = 0;
final int GAME = 1;
final int EDITOR = 2;

/////////////////////////////////////////////////////////////////

// Démarrage d'une nouvelle partie / Sélection de joueurs

void startGame() {
  joueurs.add(new Joueur(j1, 0, j1depth, j1Quiet, (j1Time == 0) ? false : true));
  joueurs.add(new Joueur(j2, 1, j2depth, j2Quiet, (j2Time == 0) ? false : true));
  surface.setSize(gameWidth, gameHeight);
  surface.setLocation(displayWidth - width, 0);
  surface.setAlwaysOnTop(attach);

  if (timeControl && (times[0][0] != 0 || times[0][1] != 0 || times[0][2] != 0) && (times[1][0] != 0 || times[1][1] != 0 || times[1][2] != 0)) {
    useTime = true;
  } else {
    useTime = false;
  }

  int iaIndex1 = getIAIndex(j1);
  int iaIndex2 = getIAIndex(j2);
  j1Img = loadImage("joueurs/" + AI_CODE[iaIndex1] + "Img.jpg");
  j1ImgEnd = loadImage("joueurs/" + AI_CODE[iaIndex1] + "ImgEnd.jpg");
  j2Img = loadImage("joueurs/" + AI_CODE[iaIndex2] + "Img.jpg");
  j2ImgEnd = loadImage("joueurs/" + AI_CODE[iaIndex2] + "ImgEnd.jpg");

  // if (attach) infos = "Épinglé";

  s1.hide();
  s2.hide();
  t1.hide();
  t2.hide();
  if (soundControl >= 2) {
    violons.stop();
    pachamama.play(); pachamama.loop();
  }
  gameState = GAME;

  println(" ");
  if (stats) {
    println("[PARTIE] Nouvelle fenêtre (" + name + ") : " + width + " x " + height);
  }
  println("[PARTIE] Nouvelle partie : " + j1 + " (aux blancs) contre " + j2 + " (aux noirs)");
  println(" ");

  pgn =       "[Event \"Chess AI\"]\n";
  if (!startFEN.equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq")) pgn = pgn + "[FEN \"" + startFEN + "\"]\n";
  pgn = pgn + "[Date \"" + year() + "." + month() + "." + day() + "\"]\n";
  pgn = pgn + "[White \"" + j1 + "\"]\n";
  pgn = pgn + "[Black \"" + j2 + "\"]\n";
  pgn = pgn + "[WhiteTitle \"" + joueurs.get(0).title + "\"]\n";
  pgn = pgn + "[BlackTitle \"" + joueurs.get(1).title + "\"]\n";
  pgn = pgn + "[WhiteElo \"" + joueurs.get(0).elo + "\"]\n";
  pgn = pgn + "[BlackElo \"" + joueurs.get(1).elo + "\"]\n\n";

  String fen = generateFEN();
  if (!fen.equals(startFEN)) {
    println("[ERREUR] INITIALISATION FEN : generateFEN() != startFEN");
    println("--> EXPECTED : " + generateFEN() + " AND GOT : " + startFEN);
    println();
    alert("Erreur d'initialisation de la fen", 2500);
  }

  addFenToHistory(fen);
  addHashToHistory(zobrist.hash);

  for (int i = 0; i < 2; i++) materials[i] = countMaireMaterial(i);

  if (soundControl >= 1) start_sound.play();
  if (useTime) {
    ta.initTimers();
    ta.show();

    // Les Moutons !
    if (isMouton(0)) { ta.timers[0].setDurationOfSecond(900); ta.timers[1].setDurationOfSecond(1100); ta.timers[0].increment = times[0][2]*1200; ta.timers[1].increment = times[1][2]*800; }
    if (isMouton(1)) { ta.timers[1].setDurationOfSecond(900); ta.timers[0].setDurationOfSecond(1100); ta.timers[0].increment = times[0][2]*800; ta.timers[1].increment = times[1][2]*1200; }
    if (isMouton(0) && isMouton(1)) { ta.timers[0].setDurationOfSecond(1000); ta.timers[1].setDurationOfSecond(1000); ta.timers[0].increment = times[0][2]*1000; ta.timers[1].increment = times[1][2]*1000; }

    if (useHacker) ta.goToHackerPosition();
  }

  sa.setTimes(j1Time, j2Time);
  showSearchController = true;
  sa.show();

  setPieces();
  checkGameState();

  delay(3);
  surface.setVisible(true);
}

void startEditor() {
  gameState = EDITOR;

  if (attach) infos = "Épinglé";

  surface.setSize(gameWidth, gameHeight);
  surface.setLocation(displayWidth - width, 0);
  surface.setAlwaysOnTop(attach);
  surface.setVisible(true);
  s1.hide();
  s2.hide();
  t1.hide();
  t2.hide();

  cursor(ARROW);

  if (soundControl >= 2) {
    violons.stop();
    pachamama.play(); pachamama.loop();
  }
}

void verifStartGame() {
  if (j1 != null && j2 != null) {
    startGame();
  } else {
    println("[MENU] Veuillez selectionner 2 joueurs");
  }
}

void rematch() {
  String savedJ1 = j1;
  String savedJ2 = j2;
  resetGame(false);
  j1 = savedJ1;
  j2 = savedJ2;
  startGame();
}

void newAIGame(int ia, String type) {
  resetGame(false);
  j1 = (ia == 0 ? type : AI_NAME[HUMAIN_INDEX]);
  j2 = (ia == 0 ? AI_NAME[HUMAIN_INDEX] : type);
  startGame();
}

void newGame() {
  resetGame(true);
}

void setRandomPlayers() {
  int num1 = floor(random(0, AI_NUMBER));
  int num2 = floor(random(0, AI_NUMBER));
  selectors.get(0).setNumber(num1);
  selectors.get(1).setNumber(num2);
}

/////////////////////////////////////////////////////////////////

// Redémarrage / Retour au menu

void resetGame(boolean menu) {
  // Reset les timers
  if (useTime) {
    ta.resetTimers();
    ta.hide();
    ta.goToDefaultPosition();
  }
  ga.hide();
  sa.hide();

  // Réinitialise les variables
  resetSettingsToDefault();

  // Resize la fenêtre
  if (menu) {
    surface.setSize(selectWidth, selectHeight);
    surface.setLocation(displayWidth/2 - width/2, 0);
    surface.setTitle(name + " - Selection des joueurs");
    surface.setAlwaysOnTop(false);
    surface.setVisible(true);
    gameState = MENU;
  }

  // Arrête la musique :(
  if (soundControl >= 2) {
    pachamama.stop();
    diagnostic.stop();
    violons.play(); violons.loop();
  }

  // Replace les pièces
  setPieces();
}

void resetSettingsToDefault() {
  // Enlève les pièces, les joueurs et bien d'autres...
  pieces[0].clear();
  pieces[1].clear();
  rois[0] = null;
  rois[1] = null;
  currentEnPassantable[0] = null;
  currentEnPassantable[1] = null;
  lastCellRightClicked = null;
  lastArrowDrawn = null;
  deltaTimeHistory.clear();
  piecesToDisplay.clear();
  positionHistory.clear();
  zobristHistory.clear();
  movesHistory.clear();
  joueurs.clear();
  hackerMoves.clear();
  tt.clear();
  sa.reset();
  allArrows.clear();
  varianteArrows.clear();
  for (ShortcutButton sb : iconButtons) sb.resetState();
  if (pointDeVue == false) flipBoard();

  // Hacker
  hackerState = CALIBRATION;
  colorOfRematch = null;
  for (int i = 0; i < CALIBRATION_NUMBER; i++) {
    hackerPoints[i] = null;
  }

  // Reset la grille
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].piece = null;
      grid[i][j].selected = false;
      grid[i][j].moveMark = false;
      grid[i][j].possibleMove = null;
      grid[i][j].red = false;
      grid[i][j].yellow = false;
    }
  }

  // Variables
  numberOfRestartWait = 0;
  numberOfScan = 0;
  lastMoveTime = 0;
  isNextMoveRestranscrit = false;
  messagesCount = 0;
  missclickCount = 0;
  appearCount = 0;
  timeCount = 0;
  showParameters = false;
  lastMissclick = 0;
  tourPourApparition = 10;
  messageMouton = "";
  messageMoutonStarted = 0;
  messageMoutonTime = 0;
  alertPos = new Point();
  missclickDragNextMove = false;
  alert = "";
  alertTime = 0;
  alertStarted = 0;
  showSavedPositions = false;
  timeAtHackerEnd = 0;
  engineToPlay = false;
  showGraph = false;
  showVariante = false;
  showSearchController = false;
  pieceSelectionne = null;
  enPromotion = null;
  attach = true;
  tourDeQui = 0;
  nbTour = 0.5;
  rewind = false;
  rewindCount = 0;
  play = true;
  gameEnded = false;
  winner = -1;
  timeAtEnd = 0;
  endReason = "";
  disableEndScreen = false;
  yEndScreen = defaultEndScreenY;
  infos = "";
  pgn = "";

  // ControlP5
  s1.show();
  s2.show();
  t1.show();
  t2.show();
}

/////////////////////////////////////////////////////////////////

// Gestion de fin de partie

boolean checkFastRepetition(long hash) {
  int counter = 0;
  for (int i = zobristHistory.size()-1; i >= 0; i--) {
    if (zobristHistory.get(i) == hash) {
      counter++;
      if (counter >= 2) return true;
    }
  }
  return false;
}

boolean checkRepetition(long hash) {
  int counter = 0;
  for (int i = zobristHistory.size()-1; i >= 0; i--) {
    if (zobristHistory.get(i) == hash) {
      counter++;
      if (counter >= 3) return true;
    }
  }
  return false;
}

boolean manqueDeMateriel() {
  int[] materialsWithoutKing = {materials[0]-maireEvalArray[ROI_INDEX], materials[1]-maireEvalArray[ROI_INDEX]};
  int[] numPawns = {0, 0};

  for (int i = 0; i < pieces.length; i++) {
    for (int j = 0; j < pieces[i].size(); j++) {
      if (pieces[i].get(j).type == "pion") numPawns[i]++;
    }
  }

  if (numPawns[0] > 0 || numPawns[1] > 0) return false; //si il reste des pions

  if (materialsWithoutKing[0] == 0 && materialsWithoutKing[1] == 0) return true; //roi contre roi
  if (materialsWithoutKing[0] <= 330 && materialsWithoutKing[1] <= 330) return true; //roi contre fou/cavalier
  //if ((materialsWithoutKing[0] == 640 && materialsWithoutKing[1] == 0) || (materialsWithoutKing[1] == 640 && materialsWithoutKing[0] == 0)) return true; //roi contre deux cavaliers
  return false;
}

void updateScores(float num) {
  // Si les deux joueurs sont différents
  if (!joueurs.get(0).name.equals(joueurs.get(1).name)) {
    if (num == 0.5) {
      joueurs.get(0).addScore(0.5);
      joueurs.get(1).addScore(0.5);
    } else {
      // Num correspond au gagnant en cas de victoire
      joueurs.get((int)num).addScore(1);
    }
    joueurs.get(0).addTotalScore(1);
    joueurs.get(1).addTotalScore(1);
  }
  // Si les deux joueurs sont les mêmes
  else {
    if (num == 0.5) joueurs.get(0).addScore(0.5);
    else joueurs.get((int)num).addScore(1);

    joueurs.get(0).addTotalScore(1);
  }
}

void checkGameState() {
  if (useHacker) return;

  // Manque de matériel
  if (manqueDeMateriel()) {
    endGame(2, "par manque de matériel");
    return;
  }

  // Répétition
  if (checkRepetition(zobrist.hash)) {
    endGame(2, "par répétition");
    return;
  }

  // Mat et pat
  if (generateAllLegalMoves(tourDeQui, true, false).size() == 0) {
    if (playerInCheck(tourDeQui) == tourDeQui) {
      endGame(opponent(tourDeQui), "par échec et mat", true);
    } else {
      endGame(2, "par pat");
    }
    return;
  }

  // Échecs
  if (playerInCheck(2) != -1) addPgnCheck();
}

void loseOnTime(int loser) {
  if (useHacker) return;
  endGame(opponent(loser), "au temps");
}

void resignWhite() {
  if (useHacker) return;
  endGame(1, "par abandon");
}

void resignBlack() {
  if (useHacker) return;
  endGame(0, "par abandon");
}

void endOnHackerDetect() {
  if (!useHacker) return;

  timeAtHackerEnd = millis();
  String reason = "par détection du hacker" + (hackerSansFin ? " sans fin" : "");
  endGame(2, reason);

  hackerState = END;
}

void endGame(int winnerTag, Object... b) {
  // Tests sur b (pour argument optionnel)
  if (b.length != 1 && b.length != 2) {
    error("endGame()", "2 ou 3 arguments seulement sont attendus");
    return;
  }
  if (b.length == 1 && !(b[0] instanceof String)) {
    error("endGame()", "Argument 2 doit être de type string");
    return;
  }
  if (b.length == 2 && !(b[0] instanceof String && b[1] instanceof Boolean)) {
    error("endGame()", "Argument 3 doit être de type booléen");
    return;
  }

  endReason = (String)b[0];
  winner = winnerTag;
  boolean isMate = false;
  if (b.length == 2) isMate = (Boolean)b[1];

  // Affiche le texte de fin de partie
  println(" ");
  if (winner == 0)      print("[PARTIE] Victoire des blancs ");
  else if (winner == 1) print("[PARTIE] Victoire des noirs ");
  else if (winner == 2) print("[PARTIE] Nulle ");
  println(endReason);
  println(" ");

  // Actualise les scores
  if (winner == 2) updateScores(0.5);
  else updateScores(winner);

  // Arrête le temps
  if (useTime) ta.stopTimers();

  gameEnded = true;
  timeAtEnd = millis();
  infos = "Game ended";

  // Sons
  if (soundControl >= 1) {
    if (winner == 2) nulle_sound.play();
    else mat_sound.play();
  }
  if (soundControl >= 2) {
    pachamama.stop();
    diagnostic.play();
  }

  // PGN
  if (winner == 2) addPgnDraw();
  else if (isMate) addPgnMate(winner);
  else addPgnWin(winner);
}
