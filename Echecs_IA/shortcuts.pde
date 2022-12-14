class Shortcut {
  Shortcut() { }

  String getDescription(int n) {
    switch(n) {
      case 0: return "Épingler / Désépingler (L)";
      case 1: return "Afficher / Masquer les variantes (V)";
      case 2: return "Afficher / Masquer l'analyse (G)";
      case 3: return "Afficher les informations (F)";
      case 4: return "Voir la PGN (P)";
      case 5: return "Sauvegarder la PGN (C)";
      case 6: return "Retourner l'échiquier (K)";
      case 7: return "Play / Pause (ESPACE)";
      case 8: return "Augmenter le délai par coups (HAUT)";
      case 9: return "Diminuer le délai par coups (BAS)";
      case 10: return "Quitter la partie";
      case 11: return "Supprimer la position (SUPPR)";
      case 12: return "Voir la FEN (F)";
      case 13: return "Copier la FEN (C)";
      case 14: return "Revenir à l'accueil";
      case 15: return "Afficher / Masquer les FENs sauvegardées";
      case 16: return "Ouvrir / Fermer search controller (D)";
      case 17: return "Afficher / Masquer les paramètres";

      default: return "";
    }
  }

  void call(int n) {
    switch (n) {
      case 0: toggleAttach(); break;
      case 1: toggleVariantes(); break;
      case 2: toggleGraph(); break;
      case 3: printInfos(); break;
      case 4: printPGN(); break;
      case 5: savePGN(); break;
      case 6: flipBoard(); break;
      case 7: playPause(); break;
      case 8: delayUp(); break;
      case 9: delayDown(); break;
      case 10: goToSelectScreen(); break;
      case 11: clearPosition(); break;
      case 12: printFEN(); break;
      case 13: copyFEN(); break;
      case 14: forceQuit(); break;
      case 15: toggleSavedPos(); break;
      case 16: toggleSearchController(); break;
      case 17: toggleParameters(); break;

      default: println(">>> Erreur dans shortcut.call()");
      break;
    }
  }
}

/////////////////////////////////////////////////////////////////

// Fonctions de shortcuts

void toggleSearchController() {
  showSearchController = !showSearchController;
  if (showSearchController) sa.show();
  else sa.hide();

  delay(3);
  surface.setVisible(true);
}

void toggleParameters() {
  showParameters =! showParameters;
  showSavedPositions = false;
}

void toggleSavedPos() {
  showSavedPositions =! showSavedPositions;
  showParameters = false;
}

void flipBoard() {
  pointDeVue = !pointDeVue;
}

void toggleVariantes() {
  showVariante =! showVariante;
}

void importSavedFEN(int number) {
  String fen = savedFENS[number];
  importFEN(fen);
  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);
}

void makeStartPos() {
  importFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);
}

void toggleGraph() {
  showGraph = !showGraph;

  if (showGraph) {
    if (joueurs.get(0).name == "LesMoutons" || joueurs.get(1).name == "LesMoutons") {
      println();
      println("!! Les Moutons !!");
      println("Arnaques au temps : " + timeCount);
      println("Missclicks : " + missclickCount);
      println("Pièces apparues : " + appearCount);
      println("Messages envoyés : " + messagesCount);
      println();
    }
    activateGraph();
  }
  else disableGraph();
}

void rewindBack() {
  if (!play || gameEnded) {
    if (movesHistory.size() == 0 || rewindCount == movesHistory.size()) return;
    rewind = true;
    rewindCount++;
    if (rewindCount > movesHistory.size()) rewindCount = movesHistory.size();
    movesHistory.get(movesHistory.size() - rewindCount).unplay();
  }
}

void rewindForward() {
  if (!play || gameEnded) {
    if (rewindCount <= 0) return;
    movesHistory.get(movesHistory.size() - rewindCount).replay();
    rewindCount--;
    if (rewindCount <= 0) { rewindCount = 0; rewind = false; }
  }
}

void forceQuit() {
  resetGame(true);
}

void clearPosition() {
  removeAllPieces();
  piecesToDisplay.clear();
}

void printFEN() {
  println("Position fen : " + generateFEN());
}

void copyFEN() {
  String f = generateFEN();
  StringSelection data = new StringSelection(f);
  Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  clipboard.setContents(data, data);

  println("Fen copiée (" + generateFEN() + ")");
}

void goToSelectScreen() {
  // requestToRestart = millis();
  // println();
  // println("Quitter la partie et revenir à la sélection ? [y/n]");
  // println();
  resetGame(true);
}

void playPause() {
  play = !play;

  if (gameEnded) {
    if (play) {
      if (soundControl >= 2 && !diagnostic.isPlaying()) diagnostic.play();
    } else {
      if (soundControl >= 2 && diagnostic.isPlaying()) diagnostic.pause();
    }
  } else {
    if (play) {
      infos = "Play";
      if (soundControl >= 2 && !pachamama.isPlaying()) pachamama.play();
      if (joueurs.get(tourDeQui).name != "Humain" && !gameEnded && !rewind && (!useHacker || hackerPret)) joueurs.get(tourDeQui).play();
      if (useTime) ta.switchTimers(tourDeQui);
    } else {
      infos = "Pause";
      if (soundControl >= 2 && pachamama.isPlaying()) pachamama.pause();
      if (useTime) ta.stopTimers();
    }
  }
}

void printPGN() {
  println(pgn);
}

void printInfos() {
  printMaireEval();
  println("Endgame weight : " + endGameWeight);
  println("FEN : " + generateFEN());
  println("Zobrist hash key : " + zobrist.hash);
}

void toggleAttach() {
  attach = !attach;
  surface.setAlwaysOnTop(attach);
  if (attach) infos = "Épinglé";
  else infos = "";
}

void printMaireEval() {
  print("Evaluation statique du maire : ");
  LeMaire m = new LeMaire(1, 3, 0, false);
  println(m.Evaluation()/100); //Evaluation statique de la position selon le maire
}

void delayUp() {
  speed += 6;
  speed = constrain(speed, 0, 1200);
  printNewSpeed();
}

void delayDown() {
  speed -= 6;
  speed = constrain(speed, 0, 1200);
  printNewSpeed();
}

void printNewSpeed() {
  println("Délai par coups : " + (float)speed/60 + " s");
}

void savePGN() {
  String[] s = new String[1];
  s[0] = pgn;
  String names = joueurs.get(0).name + " vs " + joueurs.get(1).name;
  String times =  year() + "-" + month() + "-" + day() + " à " + hour() + "." + minute() + "." + second();
  String pgnTitle = names + " - " + times;
  saveStrings("pgn/" + pgnTitle + ".pgn", s);
  println("PGN sauvegardée dans pgn/" + pgnTitle + ".pgn");
}

void printHelpMenu() {
  println("------------");
  println("!! Attention à ne pas utiliser de raccourcies !!");
  //OK quitter
  println("H        Raccourcis clavier");
  println("T        Fonction de tests");
  println("P        Afficher la PGN");
  println("C        Sauvegarder la PGN");
  println("K        FlipBoard");
  println("F        EndGameWeight + FEN");
  println("S        Perft 5");
  println("J        Evaluation statique du maire");
  println("L        Épingler/Désépingler");
  println("UP       Augmenter délai par coups");
  println("DOWN     Diminuer délai par coups");
  println("ESPACE   Pause/Play");
  println("------------");
}

void runPerft() {
  perft(5);
}

void perft(int d) {
  ArrayList<Move> moves = generateAllLegalMoves(tourDeQui, true, true);
  int numPos = 0;
  int total = 0;

  for (int i = 0; i < moves.size(); i++) {
    Move m = moves.get(i);
    m.make();
    print(i+1 + ". " + grid[m.fromI][m.fromJ].name + grid[m.i][m.j].name + "  ");
    numPos += searchMoves(d - 1);
    total += numPos;
    println(numPos);
    numPos = 0;
    m.unmake();
  }
  println("Total : " + total);
}

void runCaptureSearch(int d) {
  ArrayList<Move> moves = generateAllCaptures(tourDeQui, true);
  int numPos = 0;
  int total = 0;

  for (int i = 0; i < moves.size(); i++) {
    Move m = moves.get(i);
    m.make();
    print(i+1 + ". " + grid[m.fromI][m.fromJ].name + grid[m.i][m.j].name + "  ");
    numPos += searchCaptures(d - 1);
    total += numPos;
    println(numPos);
    numPos = 0;
    m.unmake();
  }
  println("Total : " + total);
}
