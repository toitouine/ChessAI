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

  if (j1 == "Loic") { j1Img = loadImage("joueurs/loicImg.jpeg"); j1ImgEnd = loadImage("joueurs/loicImgEnd.jpeg"); }
  else if (j1 == "LesMoutons") { j1Img = loadImage("joueurs/lesmoutonsImg.jpg"); j1ImgEnd = loadImage("joueurs/lesmoutonsImgEnd.jpg"); }
  else if (j1 == "LeMaire") { j1Img = loadImage("joueurs/lemaireImg.jpg"); j1ImgEnd = loadImage("joueurs/lemaireImgEnd.jpg"); }
  else if (j1 == "Antoine") { j1Img = loadImage("joueurs/antoineImg.jpg"); j1ImgEnd = loadImage("joueurs/antoineImgEnd.jpg"); }
  else if (j1 == "Stockfish") { j1Img = loadImage("joueurs/stockfishImg.png"); j1ImgEnd = loadImage("joueurs/stockfishImgEnd.png"); }
  else if (j1 == "Humain") { j1Img = loadImage("joueurs/humanImg.png"); j1ImgEnd = loadImage("joueurs/humanImgEnd.png"); }

  if (j2 == "Loic") { j2Img = loadImage("joueurs/loicImg.jpeg"); j2ImgEnd = loadImage("joueurs/loicImgEnd.jpeg"); }
  else if (j2 == "LesMoutons") { j2Img = loadImage("joueurs/lesmoutonsImg.jpg"); j2ImgEnd = loadImage("joueurs/lesmoutonsImgEnd.jpg"); }
  else if (j2 == "LeMaire") { j2Img = loadImage("joueurs/lemaireImg.jpg"); j2ImgEnd = loadImage("joueurs/lemaireImgEnd.jpg"); }
  else if (j2 == "Antoine") { j2Img = loadImage("joueurs/antoineImg.jpg"); j2ImgEnd = loadImage("joueurs/antoineImgEnd.jpg"); }
  else if (j2 == "Stockfish") { j2Img = loadImage("joueurs/stockfishImg.png"); j2ImgEnd = loadImage("joueurs/stockfishImgEnd.png"); }
  else if (j2 == "Humain") { j2Img = loadImage("joueurs/humanImg.png"); j2ImgEnd = loadImage("joueurs/humanImgEnd.png"); }

  if (attach) infos = "??pingl??";

  s1.hide();
  s2.hide();
  if (soundControl >= 2) {
    violons.stop();
    pachamama.play(); pachamama.loop();
  }
  gameState = 1;

  println("________________________________________________________");
  println();
  if (stats) {
    println("Nouvelle fen??tre (" + name + ") : " + width + " x " + height);
  }
  println("Nouvelle partie : " + j1 + " (aux blancs) contre " + j2 + " (aux noirs)");
  println();
  println("________________________________________________________");
  println();

  pgn =       "[Event \"Chess AI Tournament\"]\n";
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
    println("ERREUR INITIALISATION FEN, generateFEN() != startFEN");
    println(">>> " + generateFEN() + " / " + startFEN);
  }

  addFenToHistory(fen);
  addHashToHistory(zobrist.hash);

  for (int i = 0; i < 2; i++) materials[i] = countMaireMaterial(i);

  if (soundControl >= 1) start_sound.play();
  if (useTime) {
    ta.initTimers();
    ta.show();

    // Les Moutons !
    if (joueurs.get(0).name == "LesMoutons") { ta.timers[0].setDurationOfSecond(900); ta.timers[1].setDurationOfSecond(1100); ta.timers[0].increment = times[0][2]*1200; ta.timers[1].increment = times[1][2]*800; }
    if (joueurs.get(1).name == "LesMoutons") { ta.timers[1].setDurationOfSecond(900); ta.timers[0].setDurationOfSecond(1100); ta.timers[0].increment = times[0][2]*800; ta.timers[1].increment = times[1][2]*1200; }
    if (joueurs.get(0).name == "LesMoutons" && joueurs.get(1).name == "LesMoutons") { ta.timers[0].setDurationOfSecond(1000); ta.timers[1].setDurationOfSecond(1000); ta.timers[0].increment = times[0][2]*1000; ta.timers[1].increment = times[1][2]*1000; }

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
  gameState = 3;

  if (attach) infos = "??pingl??";

  surface.setSize(gameWidth, gameHeight);
  surface.setLocation(displayWidth - width, 0);
  surface.setAlwaysOnTop(attach);
  surface.setVisible(true);
  s1.hide();
  s2.hide();

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
    println("Veuillez selectionner 2 joueurs");
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

void resetGame(boolean menu) {
  // reset les timers
  if (useTime) {
    ta.resetTimers();
    ta.hide();
    ta.goToDefaultPosition();
  }
  ga.hide();
  sa.hide();
  ha.reset();

  // r??initialise les variables
  resetSettingsToDefault();

  // resize la fen??tre et gameState en mode s??lection
  if (menu) {
    surface.setSize(selectWidth, selectHeight);
    surface.setLocation(displayWidth/2 - width/2, 0);
    surface.setTitle(name + " - Selection des joueurs");
    surface.setAlwaysOnTop(false);
    surface.setVisible(true);
    gameState = 0;
  }

  // arr??te la musique :(
  if (soundControl >= 2) {
    pachamama.stop();
    diagnostic.stop();
    violons.play(); violons.loop();
  }

  // replace les pi??ces
  setPieces();
}

void resetSettingsToDefault() {
  // Enl??ve les pi??ces, les joueurs et bien d'autres...
  pieces[0].clear();
  pieces[1].clear();
  rois[0] = null;
  rois[1] = null;
  piecesToDisplay.clear();
  positionHistory.clear();
  zobristHistory.clear();
  movesHistory.clear();
  joueurs.clear();
  varianteArrows.clear();
  tt.clear();
  sa.reset();

  // Reset la grille
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].piece = null;
      grid[i][j].selected = false;
      grid[i][j].moveMark = false;
      grid[i][j].possibleMove = null;
      grid[i][j].bookFrom = false;
      grid[i][j].bookTarget = false;
    }
  }

  // Reset les toggles
  for (Toggle t : toggles1) {
    t.state = false;
  }
  for (Toggle t : toggles2) {
    t.state = false;
  }

  // Variables
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
  upLeftCorner = null;
  downRightCorner = null;
  hackerPret = false;
  engineToPlay = false;
  playEngineMoveNextFrame = false;
  showGraph = false;
  showVariante = false;
  showSearchController = false;
  pieceSelectionne = null;
  enPromotion = null;
  pointDeVue = true;
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
  yEndScreen = 0;
  infos = "";
  pgn = "";
  speed = 30;
  j1 = null;
  j2 = null;
  //j1depth = 3;
  //j2depth = 3;
  //s1.setValue(j1depth);
  //s2.setValue(j2depth);

  //controlP5
  s1.show();
  s2.show();
}

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
  int[] materialsWithoutKing = {materials[0]-100000, materials[1]-100000};
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

void checkGameState() {

  //Manque de mat??riel
  if (manqueDeMateriel()) {
    winner = 2;
    println();
    println("Nulle par manque de mat??riel");
    println();
    addPgnDraw();
    updateScores(0.5);
    endReason = "par manque de mat??riel";

    gameEnded = true;
    timeAtEnd = millis();
    infos = "Game ended";

    if (soundControl >= 1) nulle_sound.play();
    if (useTime) ta.stopTimers();
    return;
  }

  //R??p??tition
  long hash = zobrist.hash;
  if (checkRepetition(hash)) {
    winner = 2;
    println();
    println("Nulle par r??p??tition");
    println();
    addPgnDraw();
    updateScores(0.5);
    endReason = "par r??p??tition";

    gameEnded = true;
    timeAtEnd = millis();
    infos = "Game ended";

    if (soundControl >= 1) nulle_sound.play();
    if (useTime) ta.stopTimers();
    return;
  }

  //Mats et pat
  if (generateAllLegalMoves(tourDeQui, true, false).size() == 0) { //le joueur au trait n'a aucun coup
    if (playerInCheck(tourDeQui) == tourDeQui) { //Mat, le joueur au trait est en ??chec

      if (soundControl >= 2) { pachamama.stop(); diagnostic.play(); }
      if (soundControl >= 1) { mat_sound.play(); }

      winner = (int)pow(tourDeQui-1, 2);
      println();
      println("Victoire des " + (tourDeQui == 0 ? "noirs" : "blancs") + " (" + joueurs.get(winner).name + ") par ??chec et mat");
      println();
      addPgnMate(winner);
      updateScores(winner);
      endReason = "par ??chec et mat";

    } else { //Pat, le joueur au trait n'est pas en ??chec

      if (soundControl >= 2) { pachamama.stop(); diagnostic.play(); }
      if (soundControl >= 1) { nulle_sound.play(); }

      winner = 2;
      println();
      println("Nulle par pat");
      println();
      addPgnDraw();
      updateScores(0.5);
      endReason = "par pat";
    }

    gameEnded = true;
    infos = "Game ended";
    timeAtEnd = millis();

    if (useTime) ta.stopTimers();
    return;
  }

  //Echecs
  if (playerInCheck(2) != -1) {
    addPgnCheck();
  }

}

void loseOnTime(int loser) {
  winner = (int)pow(loser-1, 2);
  println();
  println("Victoire des " + ((winner == 0) ? "blancs" : "noirs") + " au temps");
  println();
  addPgnWin(winner);
  updateScores(winner);
  endReason = "au temps";

  gameEnded = true;
  timeAtEnd = millis();
  infos = "Game ended";

  if (soundControl >= 1) nulle_sound.play();
  if (useTime) ta.stopTimers();
}

void updateScores(float num) {
  if (joueurs.get(0).name != joueurs.get(1).name) {

    if (num == 0.5) { //nulle
      joueurs.get(0).addScore(0.5);
      joueurs.get(1).addScore(0.5);
    } else { //num = gagnant
      joueurs.get((int)num).addScore(1);
    }
    joueurs.get(0).addTotalScore(1);
    joueurs.get(1).addTotalScore(1);

  } else {

    //si les deux joueurs sont les m??mes
    if (num == 0.5) {
      joueurs.get(0).addScore(0.5);
    } else {
      joueurs.get((int)num).addScore(1);
    }
    joueurs.get(0).addTotalScore(1);

  }
}
