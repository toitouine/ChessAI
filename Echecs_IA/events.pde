void test() {
   //Move move = new Move(grid[1][7].piece, 2, 5, null, 0);
  // Move move2 = new Move(grid[4][6].piece, 4, 4, null, 0);
  // Move move = new Move(grid[4][4].piece, 3, 3, grid[3][3].piece, 0);
  // Move move = new Move(grid[5][5].piece, 5, 2, grid[5][2].piece, 0);
  // LeMaire m = new LeMaire(1, 2, 3, true);

  // 4 329 941 de coups par seconde sur la position de départ
  // 2 383 172 de coups par seconde sur vecteur vitesse

  // 4138 ms --> 1 000 000 légaux
  // 1200 ms --> 1 000 000 pseudo légaux

  // long count = 0;
  // long numTest = 1000000;
  // long pas = numTest/10;
  //
  // int before = millis();
  //
  // for (int i = 0; i < numTest; i++) {
  //   generateAllMoves(0, true, true).size();
  //   count++;
  //   if (i % pas == 0) println(i);
  // }
  //
  // int temps = millis() - before;
  // println("---------------");
  // println(formatInt((int)count));
  // long ps = (1000*count)/temps;
  // println(formatInt((int)(1000*count/temps)) + " par seconde");
  //
  // println(temps + " ms");

  endOnHackerDetect();
}

void keyPressed() {
  if (key == 'y' || key == 'Y') {
    if (requestToRestart != -1) {
      requestToRestart = -1;
      println(">>> Retour à la sélection des joueurs" + (!gameEnded ? " (partie annulée)" : "" + "\n"));
      resetGame(true);
    }
  }

  if (key == 'n' || key == 'N') {
    if (requestToRestart != -1) {
      requestToRestart = -1;
      println(">>> Annulation annulée\n");
    }
  }

  if (gameState == 1) {

    if (useHacker && !hackerPret) {
      if (keyCode == ENTER) addPointToCalibration();
      if (keyCode == BACKSPACE) restoreCalibrationSaves();
      if (keyCode == SHIFT) forceCalibrationRestore();
    }

    if (key == ' ') playPause();
    else if (key == 'p' || key == 'P') printPGN();
    else if (key == 'k' || key == 'K') flipBoard();
    else if (key == 'f' || key == 'F') printInfos();
    else if (key == 'l' || key == 'L') toggleAttach();
    else if (key == 'g' || key == 'G') toggleGraph();
    else if (key == 's' || key == 'S') runPerft();
    else if (key == 'd' || key == 'D') toggleSearchController();
    else if (keyCode == 'v' || keyCode == 'V') toggleVariantes();
    else if (keyCode == 'c' || keyCode == 'C') savePGN();
    else if (keyCode == UP) delayUp();
    else if (keyCode == DOWN) delayDown();
    else if (keyCode == LEFT) rewindBack();
    else if (keyCode == RIGHT) rewindForward();
    else if (key == 'T') test();

  } else if (gameState == 3) {

    if (key == 'l' || key == 'L') toggleAttach();
    if (key == BACKSPACE) clearPosition();
    if (key == 'f' || key == 'F') printFEN();
    if (key == 'c' || key == 'C') copyFEN();
    if (key == 'k' || key == 'K') flipBoard();
    if (key == 'p' || key == 'P') pasteHTMLtoBoard();

  } else if (gameState == 0) {
    if (keyCode == ENTER) verifStartGame();
  }

  if (keyCode == ESC) {
    key = 0;
  }
}

void mouseReleased() {
  if (gameState == 1) {
    if (pieceSelectionne != null && enPromotion == null) {
      int i =  getGridI();
      int j =  getGridJ();

      if (i >= 0 && i < cols && j >= 0 && j < rows) {
        if (i == pieceSelectionne.i &&  j == pieceSelectionne.j) { pieceSelectionne.dragging = false; return; }

        if (grid[i][j].possibleMove != null) {
          grid[i][j].possibleMove.play();
          pieceSelectionne = null;
        } else {
          deselectAll();
          pieceSelectionne = null;
        }
      } else {
        deselectAll();
        pieceSelectionne = null;
      }
    }

    // if (play && !gameEnded && !rewind && (!useHacker || hackerPret)) {
    if (!blockPlaying) {
      if ((joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") || (joueurs.get(0).name != "Humain" && joueurs.get(1).name == "Humain")) {
        if (joueurs.get(tourDeQui).name != "Humain") { engineToPlay = true; }
      }
    }
  }
  else if (gameState == 3) {

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

    if (pieceSelectionne != null) {
      int i =  getGridI();
      int j =  getGridJ();

      if (i >= 0 && i < cols && j >= 0 && j < rows) {
        if (i == pieceSelectionne.i &&  j == pieceSelectionne.j) { pieceSelectionne.dragging = false; return; }

        if (grid[i][j].freeMove) {
          pieceSelectionne.quickMove(i, j);
          pieceSelectionne = null;
          deselectAll();
        } else {
          deselectAll();
          pieceSelectionne = null;
        }
      } else {
        deselectAll();
        pieceSelectionne = null;
      }
    }
  }
  else if (gameState == 0) {
    for (int i = 0; i < timeButtons.length; i++) {
      for (int j = 0; j < timeButtons[i].size(); j++) {
        if (timeButtons[i].get(j).pressed) {
          timeButtons[i].get(j).release();
        }
      }
    }
  }
}

void mouseDragged() {
  if (pieceSelectionne != null) {
    pieceSelectionne.dragging = true;
    cursor(HAND);

    // À la fin de l'arraylist
    for (int i = 0; i < piecesToDisplay.size(); i++) {
      Piece p = piecesToDisplay.get(i);
      if (p == pieceSelectionne) {
        piecesToDisplay.remove(i);
        piecesToDisplay.add(pieceSelectionne);
      }
    }

    // Curseur
    int i = getGridI();
    int j = getGridJ();

    if (i >= 0 && i < cols && j >= 0 && j < rows) {
      if (grid[i][j].possibleMove != null) cursor(HAND);
      else cursor(ARROW);
    } else {
      cursor(ARROW);
    }

    // Missclicks :(
    if (joueurs.size() != 0 && (joueurs.get((int)pow(tourDeQui-1, 2)).name != "LesMoutons")) return;

    boolean castling = false;
    if (random(1) <= 0.75) {
      if (pieceSelectionne.pieceIndex == ROI_INDEX) {
        ArrayList<Move> moves = pieceSelectionne.generateLegalMoves(true, false);
        for (int n = 0; n < moves.size(); n++) {
          if (moves.get(n).special != 0) castling = true;
        }
      }
    }

    if (!castling && !missclickDragNextMove) return;
    if (i < 0 && i >= cols && j < 0 && j >= rows) return;

    if (grid[i][j].possibleMove != null) {
      grid[i][j].possibleMove.play();
      missclickCount++;
      pieceSelectionne = null;
      missclickDragNextMove = false;
    }

  }
  else if (gameState == 3 && mouseButton == RIGHT) {
    int i = getGridI();
    int j = getGridJ();

    if (i >= 0 && i < cols && j >= 0 && j < rows) {
      Piece p = grid[i][j].piece;
      if (p == null) return;
      for (int n = 0; n < piecesToDisplay.size(); n++) {
        if (piecesToDisplay.get(n) == p) { piecesToDisplay.remove(n); break; }
      }
      removePiece(p);
    }
  }
}

void mousePressed() {
  if (gameState == 1) {

    // Boutons rematch
    if (gameEnded && !useHacker && !hackerPret) {
      if (rematchButton.contains(mouseX, mouseY)) { rematch(); return; }
      if (newGameButton.contains(mouseX, mouseY)) { resetGame(true); return; }
    }

    // Barre d'outils
    for (Bouton b : iconButtons) {
      if (b.contains(mouseX, mouseY)) {
        b.callShortcut();
        return;
      }
    }

    // Échiquier
    if (joueurs.get(tourDeQui).name == "Humain" && !blockPlaying) {

      if (enPromotion == null) {
        //Select pièces et grid
        int i = getGridI();
        int j = getGridJ();

        if (i >= 0 && i < cols && j >= 0 && j < rows) {
          Piece p = grid[i][j].piece;
          if (stats && details) println("Case : [" + i + "][" + j + "] (" + grid[i][j].name + ")");

          if (grid[i][j].possibleMove != null) {
              grid[i][j].possibleMove.play();
              pieceSelectionne = null;
          } else if (p == null || p.c != tourDeQui) {
            pieceSelectionne = null;
            deselectAll();
          } else if (p.c == tourDeQui) {
            deselectAll();
            p.select(true);
            grid[p.i][p.j].selected = true;
            pieceSelectionne = p;
          }
        }

      } else {
        for (int i = 0; i < promoButtons.size(); i++) {
          if (promoButtons.get(i).contains(mouseX, mouseY)) {

            removePiece(enPromotion);

            if (i == 0) { //promotion en dame
              pieces[enPromotion.c].add(new Piece("dame", enPromotion.i, enPromotion.c*7, enPromotion.c));
              materials[enPromotion.c] += 800;
              addPgnChar("Q");
            } else if (i == 1) { //en tour
              pieces[enPromotion.c].add(new Piece("tour", enPromotion.i, enPromotion.c*7, enPromotion.c));
              materials[enPromotion.c] += 400;
              addPgnChar("R");
            } else if (i == 2) { //en fou
              pieces[enPromotion.c].add(new Piece("fou", enPromotion.i, enPromotion.c*7, enPromotion.c));
              materials[enPromotion.c] += 230;
              addPgnChar("B");
            } else if (i == 3) { //en cavalier
              pieces[enPromotion.c].add(new Piece("cavalier", enPromotion.i, enPromotion.c*7, enPromotion.c));
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

            break;
          }
        }
      }
    }
  }
  else if (gameState == 0) {
    for (int i = 0; i < timeButtons.length; i++) {
      for (int j = 0; j < timeButtons[i].size(); j++) {
        if (timeButtons[i].get(j).contains(mouseX, mouseY)) {
          timeButtons[i].get(j).click();
        }
      }
    }
    for (int i = 0; i < presetButtons.size(); i++) {
      if (presetButtons.get(i).contains(mouseX, mouseY)) {
        if (i == 0) rapidPreset();
        else if (i == 1) blitzPreset();
        else if (i == 2) bulletPreset();
      }
    }
    for (int i = 0; i < toggles1.size(); i++) {
      if (toggles1.get(i).contains(mouseX, mouseY)) {
        for (Toggle t : toggles1) t.state = false;
        toggles1.get(i).state = !toggles1.get(i).state;
        j1 = toggles1.get(i).name;
      }
    }

    for (int i = 0; i < toggles2.size(); i++) {
      if (toggles2.get(i).contains(mouseX, mouseY)) {
        for (Toggle t : toggles2) t.state = false;
        toggles2.get(i).state = !toggles2.get(i).state;
        j2 = toggles2.get(i).name;
      }
    }

    for (int i = 0; i < hubButtons.size(); i++) {
      TextBouton b = hubButtons.get(i);
      if (b.contains(mouseX, mouseY)) {
        if (i == 0) verifStartGame();
        else if (i == 1) pasteFEN();
        else if (i == 2) copyFEN();
      }
    }

    if (positionEditor.contains(mouseX, mouseY)) {
      startEditor();
    }

    if (hackerButton.contains(mouseX, mouseY)) {
      useHacker =! useHacker;
    }
  }
  else if (gameState == 3) {

    // barre d'outils
    for (Bouton b : editorIconButtons) {
      if (b.contains(mouseX, mouseY)) { b.callShortcut(); return; }
    }

    if (showSavedPositions) {
      for (int i = 0; i < savedFENSbuttons.size(); i++) {
        if (savedFENSbuttons.get(i).contains(mouseX, mouseY)) {
          importSavedFEN(i);
          toggleSavedPos();
          break;
        }
      }
      return;
    }

    if (showParameters) {
      return;
    }

    // drag and drop
    for (int i = 0; i < addPiecesButtons[addPiecesColor].size(); i++) {
      DragAndDrop d = addPiecesButtons[addPiecesColor].get(i);
      if (d.contains(mouseX, mouseY)) {
        if (enAjoutPiece == null) {
          enAjoutPiece = d;
          d.lockToMouse();
        }
      }
    }

    // changement de couleur du drag and drop
    if (addPiecesColorSwitch.contains(mouseX, mouseY)) {
      addPiecesColorSwitch.toggle();
      addPiecesColor = (addPiecesColor == 1) ? 0 : 1;
    }

    // pieces
    int i = getGridI();
    int j = getGridJ();

    if (i >= 0 && i < cols && j >= 0 && j < rows) {
      Piece p = grid[i][j].piece;

      if (grid[i][j].freeMove) {
          pieceSelectionne.quickMove(i, j);
          pieceSelectionne = null;
          deselectAll();
      } else if (p != null) {
        if (mouseButton == LEFT) {
          deselectAll();
          p.fly();
          grid[p.i][p.j].selected = true;
          pieceSelectionne = p;
        } else if (mouseButton == RIGHT) {
          // retire de piecesToDisplay
          for (int n = 0; n < piecesToDisplay.size(); n++) {
            if (piecesToDisplay.get(n) == p) { piecesToDisplay.remove(n); break; }
          }
          removePiece(p);
        }
      }
    }
  }
}

void mouseMoved() {
  if (gameState == 1) {

    // Bouton rematch
    if (gameEnded && !useHacker && !hackerPret && (rematchButton.contains(mouseX, mouseY) || newGameButton.contains(mouseX, mouseY))) {
      cursor(HAND);
      return;
    }

    // Barre d'outils
    for (int i = 0; i < iconButtons.size(); i++) {
      if (iconButtons.get(i).contains(mouseX, mouseY)) {
        cursor(HAND);
        infoBox = iconButtons.get(i).getDescription();
        return;
      }
    }
    infoBox = "";

    if (enPromotion == null) {

      if (useHacker && !hackerPret) { cursor(ARROW); return; }

      int i = getGridI();
      int j = getGridJ();

      if (i >= 0 && i < cols && j >= 0 && j < rows) {
        if (gameEnded) { cursor(ARROW); return; }
        Piece p = grid[i][j].piece;
        if ((p != null && p.c == tourDeQui) || grid[i][j].possibleMove != null) cursor(HAND);
        else cursor(ARROW);

      } else {
        cursor(ARROW);
      }

    } else {
      for (int i = 0; i < promoButtons.size(); i++) {
        if (promoButtons.get(i).contains(mouseX, mouseY)) {
          cursor(HAND);
          return;
        } else {
          cursor(ARROW);
        }
      }
    }
  }

  else if (gameState == 0) {

    for (int i = 0; i < timeButtons.length; i++) {
      for (int j = 0; j < timeButtons[i].size(); j++) {
        timeButtons[i].get(j).hovered = false;
      }
    }
    for (int i = 0; i < timeButtons.length; i++) {
      for (int j = 0; j < timeButtons[i].size(); j++) {
        if (timeButtons[i].get(j).contains(mouseX, mouseY)) {
          timeButtons[i].get(j).hovered = true;
          cursor(HAND);
          return;
        }
      }
    }
    for (int i = 0; i < presetButtons.size(); i++) {
      if (presetButtons.get(i).contains(mouseX, mouseY)) {
        cursor(HAND);
        return;
      }
    }
    for (Toggle t : toggles1) {
      if (t.contains(mouseX, mouseY)) {
        cursor(HAND);
        return;
      }
    }
    for (Toggle t2 : toggles2) {
      if (t2.contains(mouseX, mouseY)) {
        cursor(HAND);
        return;
      }
    }
    for (TextBouton b : hubButtons) {
      if (b.contains(mouseX, mouseY)) {
        cursor(HAND);
        return;
      }
    }
    if (positionEditor.contains(mouseX, mouseY) || hackerButton.contains(mouseX, mouseY)) {
      cursor(HAND);
      return;
    }

    cursor(ARROW);
  }

  else if (gameState == 3) {

    // barre d'outils
    for (int i = 0; i < editorIconButtons.size(); i++) {
      if (editorIconButtons.get(i).contains(mouseX, mouseY)) {
        cursor(HAND);
        infoBox = editorIconButtons.get(i).getDescription();
        return;
      }
    }

    if (showSavedPositions) {
      for (ButtonFEN b : savedFENSbuttons) {
        if (b.contains(mouseX, mouseY)) { cursor(HAND); return; }
      }
      cursor(ARROW);
      infoBox = "";
      return;
    }

    if (showParameters) {
      infoBox = "";
      cursor(ARROW);
      return;
    }

    // Pièces
    if (pieceHovered()) {
      cursor(HAND);
      return;
    }

    // Drag and drop
    if (enAjoutPiece == null) {
      if (addPiecesColorSwitch.contains(mouseX, mouseY)) { cursor(HAND);  return; }
      for (int i = 0; i < addPiecesButtons[addPiecesColor].size(); i++) {
        if (addPiecesButtons[addPiecesColor].get(i).contains(mouseX, mouseY)) {
          cursor(HAND);
          return;
        }
      }
    }

    infoBox = "";
    cursor(ARROW);
  }
}
