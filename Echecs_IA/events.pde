void test() {
  // Move move = new Move(grid[1][7].piece, 2, 5, null, 0);
  // Move move2 = new Move(grid[4][6].piece, 4, 4, null, 0);
  // Move move = new Move(grid[4][4].piece, 3, 3, grid[3][3].piece, 0);
  // Move move = new Move(grid[5][5].piece, 5, 2, grid[5][2].piece, 0);
  // LeMaire m = new LeMaire(1, 2, 3, true);

  // long count = 0;
  // long numTest = 2000;
  // long pas = numTest/10;
  //
  // int before = millis();
  //
  // for (int i = 0; i < numTest; i++) {
  //   hackerEndDetected();
  //   count++;
  //   if (i % pas == 0) println(i);
  // }
  //
  // int temps = millis() - before;
  // println("———————————————");
  // println(formatInt((int)count) + " itérations");
  // println(temps + " ms");
  // println(formatInt((int)(1000*count/temps)) + " par seconde");

  highlightBook();
}

void mouseMoved() {
  if (MODE_SANS_AFFICHAGE && useHacker && hackerPret) return;

  if ((gameState == GAME || gameState == EDITOR) && enPromotion == null && !gameEnded && !useHacker && !showSavedPositions && !showParameters) {
    int i = getGridI();
    int j = getGridJ();

    if (i >= 0 && i < cols && j >= 0 && j < rows) {
      Piece p = grid[i][j].piece;
      if ((p != null && p.c == tourDeQui) || grid[i][j].possibleMove != null) {
        cursor(HAND);
        return;
      }
      if (p != null && gameState == EDITOR) { cursor(HAND); return; }
    }
  }

  boolean cursor = false;
  for (Button b : allButtons) {
    if (b.isEnabled() && b.contains(mouseX, mouseY)) {
      cursor(HAND);
      if (b instanceof ShortcutButton && !showSavedPositions && !showParameters) infoBox = b.getDescription();
      cursor = true;
    }
  }

  if (cursor) return;
  infoBox = "";
  cursor(ARROW);
}

void mousePressed() {
  if (MODE_SANS_AFFICHAGE && useHacker && hackerPret) return;

  for (Button b : allButtons) {
    if (b.isEnabled() && b.contains(mouseX, mouseY)) {
      b.call();
      return;
    }
  }

  if (gameState == GAME) {
    int i = getGridI();
    int j = getGridJ();

    if (i >= 0 && i < cols && j >= 0 && j < rows) {
      if (mouseButton == LEFT) {
        if (lastCellRightClicked == null && joueurs.get(tourDeQui).name == "Humain" && !blockPlaying) clickedOnBoard(i, j);
      }
      else if (mouseButton == RIGHT) lastCellRightClicked = grid[i][j];
    }
    return;
  }

  if (gameState == EDITOR && !showSavedPositions && !showParameters) {
    int i = getGridI();
    int j = getGridJ();

    if (i >= 0 && i < cols && j >= 0 && j < rows) {
      clickedOnEditorBoard(i, j);
    }
  }
}

void keyPressed() {

  if (keyCode == ESC) key = 0;
  if (key == 'h' || key == 'H') printHelpMenu();

  if (gameState == GAME) {

    if (useHacker && !hackerPret && !hackerAPImode) {
      if (keyCode == ENTER) addPointToCalibration();
      if (keyCode == BACKSPACE) manualRestoreSaves();
      if (keyCode == SHIFT) manualForceSaves();
    }

    if (key == ' ')                    playPause();
    else if (key == 'p' || key == 'P') printPGN();
    else if (key == 'k' || key == 'K') flipBoard();
    else if (key == 'f' || key == 'F') printInfos();
    else if (key == 'l' || key == 'L') toggleAttach();
    else if (key == 'g' || key == 'G') toggleGraph();
    else if (key == 's' || key == 'S') runPerft();
    else if (key == 'd' || key == 'D') toggleSearchController();
    else if (key == 'b' || key == 'B') highlightBook();
    else if (key == 'Q')               forceQuit();
    else if (key == 'v' || key == 'V') toggleVariantes();
    else if (key == 'c' || key == 'C') savePGN();
    else if (keyCode == UP)            delayUp();
    else if (keyCode == DOWN)          delayDown();
    else if (keyCode == LEFT)          rewindBack();
    else if (keyCode == RIGHT)         rewindForward();
    else if (key == 'T')               test();

    else if (useHacker && hackerPret) {
      if (key == 'r' || key == 'R') endOnHackerDetect();
      else if (key == 'w' || key == 'W') hackStartGame();
    }

  } else if (gameState == EDITOR) {

    if (key == 'l' || key == 'L') toggleAttach();
    if (key == 'f' || key == 'F') printFEN();
    if (key == 'c' || key == 'C') copyFEN();
    if (key == 'k' || key == 'K') flipBoard();
    if (key == 'p' || key == 'P') pasteHTMLtoBoard();
    if (key == BACKSPACE)         clearPosition();

  } else if (gameState == MENU) {
    if (keyCode == ENTER) verifStartGame();
  }
}

void mouseReleased() {
  if (MODE_SANS_AFFICHAGE && useHacker && hackerPret) return;

  if (gameState == MENU) {
    for (int i = 0; i < timeButtons.length; i++) {
      for (int j = 0; j < timeButtons[i].size(); j++) {
        if (timeButtons[i].get(j).pressed) timeButtons[i].get(j).release();
      }
    }
    return;
  }

  if (gameState == GAME && enPromotion == null) {
    int i = getGridI();
    int j = getGridJ();

    if (i >= 0 && i < cols && j >= 0 && j < rows) {
      if (mouseButton == LEFT && pieceSelectionne != null) {
        if (i == pieceSelectionne.i &&  j == pieceSelectionne.j) {
          pieceSelectionne.dragging = false;
          return;
        }
        if (grid[i][j].possibleMove != null) grid[i][j].possibleMove.play();
      }
      else if (mouseButton == RIGHT) {
        if (lastCellRightClicked == null || lastCellRightClicked == grid[i][j]) {
          if (keyPressed && keyCode == CONTROL) grid[i][j].toggleYellow();
          else grid[i][j].toggleRed();
        }
        else {
          allArrows.remove(lastArrowDrawn);
          drawArrow(lastCellRightClicked.i, lastCellRightClicked.j, i, j);
          lastArrowDrawn = null;
        }
        lastCellRightClicked = null;
        return;
      }

      deselectAll();
      pieceSelectionne = null;
    }

    lastCellRightClicked = null;
    return;
  }

  if (gameState == EDITOR) {

    if (enAjoutPiece != null) {
      int i = getGridI();
      int j = getGridJ();

      if (i >= 0 && i < cols && j >= 0 && j < rows) {
        if (grid[i][j].piece == null) {
          addPieceToBoardByDrop(enAjoutPiece.getValue(), i, j);
        }
      }

      enAjoutPiece.unlockMouse();
      enAjoutPiece = null;
    }

    else if (pieceSelectionne != null) {
      int i =  getGridI();
      int j =  getGridJ();

      if (i >= 0 && i < cols && j >= 0 && j < rows) {
        if (i == pieceSelectionne.i &&  j == pieceSelectionne.j) { pieceSelectionne.dragging = false; return; }
        if (grid[i][j].freeMove) pieceSelectionne.quickMove(i, j);
      }

      deselectAll();
      pieceSelectionne = null;
    }
  }
}

void mouseDragged() {
  if ((MODE_SANS_AFFICHAGE && useHacker && hackerPret) || gameState == MENU) return;

  if (gameState == EDITOR && mouseButton == RIGHT) {
    int i = getGridI();
    int j = getGridJ();

    if (i >= 0 && i < cols && j >= 0 && j < rows) {
      Piece p = grid[i][j].piece;
      if (p == null) return;
      removePieceToDisplay(p);
      removePiece(p);
    }
  }

  if (gameState == GAME && mouseButton == RIGHT) {
    int i = getGridI();
    int j = getGridJ();

    if (i >= 0 && i < cols && j >= 0 && j < rows && lastCellRightClicked != grid[i][j]) {
      if (lastArrowDrawn != null && lastArrowDrawn.ti == i && lastArrowDrawn.tj == j) return;
      updateDraggedArrow(i, j);
    }
  }

  if (pieceSelectionne != null && lastCellRightClicked == null) {
    pieceSelectionne.dragging = true;

    int i = getGridI();
    int j = getGridJ();
    if (i >= 0 && i < cols && j >= 0 && j < rows && grid[i][j].possibleMove != null) cursor(HAND);
    else cursor(ARROW);

    // Missclicks :(
    if (gameState == GAME && joueurs.get((int)pow(tourDeQui-1, 2)).name == "LesMoutons") missclick(i, j);
  }
}
