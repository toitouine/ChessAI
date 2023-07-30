/////////////////////////////////////////////////////////////////

// 1) Fonctions utiles (ou pas)
// 2) Affichages
// 3) Plateau et presets
// 4) FEN, historiques et données
// 5) Fonctions pour calculs et recherche

/////////////////////////////////////////////////////////////////

// Fonctions utiles (ou pas)

void alert(String message, int time) {
  alert = message;
  alertTime = time;
  alertStarted = millis();
}

void clearAlert() {
  alert = "";
  alertStarted = 0;
  alertTime = 0;
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

boolean isVerySimilarColor(Color c1, Color c2) {
  return abs(c1.getRed()-c2.getRed()) <= 5 && abs(c1.getGreen()-c2.getGreen()) <= 5 && abs(c1.getBlue()-c2.getBlue()) <= 5;
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

boolean blockPlaying() {
  return (!play || sa.inThreadSearch || gameEnded || rewind || (useHacker && hackerState == CALIBRATION));
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

Point getMainWindowLocation() {
  Frame mainFrame = ( (PSurfaceAWT.SmoothCanvas) ((PSurfaceAWT)surface).getNative()).getFrame();
  if (mainFrame.isShowing()) return mainFrame.getLocationOnScreen();
  return null;
}

int getIAIndex(String ia) {
  for (int i = 0; i < AI_NUMBER; i++) {
    if (ia.equals(AI_NAME[i])) {
      return i;
    }
  }

  error("getIAIndex()", "Nom d'IA non trouvée dans la configuration");
  return -1;
}

void error(String function, String message) {
  println();
  println("[ERREUR] Erreur : " + message + " dans " + function);
  println();
}

void helpMoveWhite() {
  if (sa.inThreadSearch) return;

  if (tourDeQui != 0 || useHacker) return;
  cursor(WAIT);
  LeMaire cmaire = new LeMaire(0, 7, 30, true);
  Move bestMove = cmaire.getBestMove(2000);
  allArrows.add(new Arrow(bestMove.fromI, bestMove.fromJ, bestMove.i, bestMove.j));
  cursor(HAND);
}

void helpMoveBlack() {
  if (sa.inThreadSearch) return;

  if (tourDeQui != 1 || useHacker) return;
  cursor(WAIT);
  LeMaire cmaire = new LeMaire(1, 7, 30, true);
  Move bestMove = cmaire.getBestMove(2000);
  allArrows.add(new Arrow(bestMove.fromI, bestMove.fromJ, bestMove.i, bestMove.j));
  cursor(HAND);
}

int opponent(int player) {
  return (player == 0 ? 1 : 0);
}

boolean isAIvsHumain() {
  return ( (isHumain(0) && !isHumain(1)) || (!isHumain(0) && isHumain(1)) );
}

boolean isHumain(int qui) {
  return (joueurs.get(qui).name.equals(AI_NAME[HUMAIN_INDEX]));
}

boolean isHumainTurn() {
  return isHumain(tourDeQui);
}

boolean isMouton(int qui) {
  return (joueurs.get(qui).name.equals(AI_NAME[LESMOUTONS_INDEX]));
}

/////////////////////////////////////////////////////////////////

// Affichages

void drawGameInterface() {
  // Titre
  surface.setTitle(name + " - "
                        + j1 + " (" + ((joueurs.get(0).useIterativeDeepening) ? "ID" : j1depth) + ") contre "
                        + j2 + " (" + ((joueurs.get(1).useIterativeDeepening) ? "ID" : j2depth) + ")"
                        + ((infos == "") ? "" : " - ") + infos);

  // Icones
  for (ShortcutButton b : iconButtons) b.show();
  for (int i = 0; i < humainButton.size(); i++) {
    if (humainButton.get(i).isEnabled()) humainButton.get(i).show();
  }

  // Plateau
  drawPlayersInfos();
  drawBoard();
  for (Arrow arrow : allArrows) arrow.show();

  // Promotion
  if (enPromotion != null) {
    fill(220, 220, 220, 200);
    rectMode(CORNER);
    rect(offsetX, offsetY, cols*w, rows*w);
    showPromoButtons();
  }

  // Écran et boutons de fin de partie
  if (gameEnded) {
    if (!disableEndScreen && millis() - timeAtEnd > timeBeforeEndDisplay) handleEndScreen();
    if (!useHacker) {
      newGameButton.show();
      rematchButton.show();
    }
  }

  // Page d'accueil du hacker
  if (useHacker && hackerState == CALIBRATION) drawHackerPage();
}

void initGUI() {
  yEndScreen = defaultEndScreenY;

  cp5 = new ControlP5(this);
  s1 = cp5.addSlider("j1depth")
     .setPosition(30, 80)
     .setSize(30,153)
     .setLabel("Profondeur")
     .setRange(1,30)
     .setNumberOfTickMarks(30)
     .setValue(j1depth)
     .setColorForeground(#8da75a)
     .setColorActive(#abcc6a)
     .setColorBackground(#5d6e3b);

  s2 = cp5.addSlider("j2depth")
     .setPosition(selectWidth-120, 80)
     .setSize(30, 153)
     .setLabel("Profondeur")
     .setRange(1,30)
     .setNumberOfTickMarks(30)
     .setValue(j2depth)
     .setColorForeground(#8da75a)
     .setColorActive(#abcc6a)
     .setColorBackground(#5d6e3b);

  t1 = cp5.addSlider("j1Time")
      .setPosition(90, 80)
      .setSize(30, 153)
      .setLabel("Temps")
      .setRange(0, 10000)
      .setValue(1000)
      .setColorForeground(#bdbd64)
      .setColorActive(#d6d46f)
      .setColorBackground(#827e40);

  t2 = cp5.addSlider("j2Time")
      .setPosition(selectWidth-60, 80)
      .setSize(30, 153)
      .setLabel("Temps")
      .setRange(0, 10000)
      .setValue(1000)
      .setColorForeground(#bdbd64)
      .setColorActive(#d6d46f)
      .setColorBackground(#827e40);

  // Boutons de FEN / Nouvelle partie / Random
  Condition hubCondition = new Condition() { public boolean c() { return gameState == MENU; } };
  hubButtons.add(new TextButton(selectWidth/2 - 190, selectHeight-125, 380, 75, "Nouvelle partie", 30, 10, "verifStartGame", hubCondition));
  hubButtons.add(new TextButton(selectWidth-110, selectHeight-40, 100, 30, "Coller FEN", 18, 8, "pasteFEN", hubCondition)); hubButtons.get(1).setColors(#1d1c1a, #ffffff);
  hubButtons.add(new TextButton(selectWidth-220, selectHeight-40, 100, 30, "Copier FEN", 18, 8, "copyFEN", hubCondition)); hubButtons.get(2).setColors(#1d1c1a, #ffffff);
  hubButtons.add(new TextButton(200, 317, 90, 30, "Aléatoire", 18, 8, "setRandomPlayers", hubCondition));  hubButtons.get(3).setColors(#1d1c1a, #ffffff);
  allButtons.addAll(hubButtons);

  // Boutons de promotion
  Condition promoCondition = new Condition() { public boolean c() { return (gameState == GAME && !blockPlaying() && enPromotion != null && isHumainTurn()); } };
  promoButtons.add(new PromotionButton(0.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[1], imageArrayN[1], 0, promoCondition));
  promoButtons.add(new PromotionButton(2.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[2], imageArrayN[2], 1, promoCondition));
  promoButtons.add(new PromotionButton(4.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[3], imageArrayN[3], 2, promoCondition));
  promoButtons.add(new PromotionButton(6.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[4], imageArrayN[4], 3, promoCondition));
  allButtons.addAll(promoButtons);

  // Selecteurs
  PImage[] imgs = new PImage[AI_NUMBER];
  for (int i = 0; i < imgs.length; i++) {
    imgs[i] = loadImage("joueurs/" + AI_CODE[i] + ".jpg");
  }
  selectors.add(new ImageSelector(230, 80, 165, imgs, AI_NAME, 0, hubCondition));
  selectors.add(new ImageSelector(selectWidth - 395, 80, 165, imgs, AI_NAME, 1, hubCondition));
  allButtons.addAll(selectors);

  // Hacker et éditeur de position
  positionEditor = new ImageButton(selectWidth-55, 10, 55, 55, 0, #ffffff, chess, true, "startEditor", hubCondition);
  hackerButton = new ImageButton(selectWidth-105, 11, 44, 44, 0, #ffffff, bot, true, "toggleHacker", hubCondition);
  hackerButton.display = false;
  allButtons.add(positionEditor);
  allButtons.add(hackerButton);

  // Bouton sur page du hacker
  int rectX = (gameWidth-offsetX)/2 + offsetX;
  int rectY = (gameHeight-offsetY)/2 + offsetY;
  int rectW = 7*w, rectH = 3*w;

  PImage img1 = (hackerSite == LICHESS ? lichessLogo : chesscomLogo);
  PImage img2 = (hackerSite == LICHESS ? chesscomLogo : lichessLogo);
  siteButton = new ToggleImage(rectX+rectW/2-25, rectY-rectH/2+25, 35, 35, img1, img2, "switchSite", new Condition() { public boolean c() { return (gameState == GAME && useHacker && hackerState == CALIBRATION);}});
  allButtons.add(siteButton);

  // Revanche et menu en fin de partie
  Condition endButtons = new Condition() { public boolean c() { return(gameState == GAME && gameEnded && !useHacker); } };
  rematchButton = new TextButton(offsetX - offsetX/1.08, offsetY+4*w-29, offsetX-2*(offsetX - offsetX/1.08), 24 * w/70, "Revanche", 15 * w/70, 3, "rematch", endButtons);
  rematchButton.setColors(#1d1c1a, #ffffff);
  newGameButton = new TextButton(offsetX - offsetX/1.08, offsetY+4*w+5, offsetX-2*(offsetX - offsetX/1.08), 24 * w/70, "Menu", 15 * w/70, 3, "newGame", endButtons);
  newGameButton.setColors(#1d1c1a, #ffffff);
  allButtons.add(rematchButton);
  allButtons.add(newGameButton);

  // Boutons du temps
  Condition timeCondition = new Condition() { public boolean c() { return (gameState == MENU && TIME_CONTROL); } };
  timeButtons[0] = new ArrayList<TimeButton>();
  timeButtons[1] = new ArrayList<TimeButton>();
  timeButtons[0].add(new TimeButton(whiteTimePosition.x,       whiteTimePosition.y - 8,  48, 11, 5, 0, 0, 0, #f0f0f0, #26211b, #d1cfcf, true, timeCondition));
  timeButtons[0].add(new TimeButton(whiteTimePosition.x + 49,  whiteTimePosition.y - 8,  49, 11, 0, 5, 0, 0, #f0f0f0, #26211b, #d1cfcf, true, timeCondition));
  timeButtons[0].add(new TimeButton(whiteTimePosition.x + 105, whiteTimePosition.y - 8,  49, 11, 5, 5, 0, 0, #f0f0f0, #26211b, #d1cfcf, true, timeCondition));
  timeButtons[0].add(new TimeButton(whiteTimePosition.x,       whiteTimePosition.y + 53, 48, 10, 0, 0, 0, 5, #f0f0f0, #26211b, #d1cfcf, false, timeCondition));
  timeButtons[0].add(new TimeButton(whiteTimePosition.x + 49,  whiteTimePosition.y + 53, 49, 10, 0, 0, 5, 0, #f0f0f0, #26211b, #d1cfcf, false, timeCondition));
  timeButtons[0].add(new TimeButton(whiteTimePosition.x + 105, whiteTimePosition.y + 53, 49, 10, 0, 0, 5, 5, #f0f0f0, #26211b, #d1cfcf, false, timeCondition));
  timeButtons[1].add(new TimeButton(blackTimePosition.x,       blackTimePosition.y - 8,  48, 10, 5, 0, 0, 0, #26211b, #f0f0f0, #2d2d2a, true, timeCondition));
  timeButtons[1].add(new TimeButton(blackTimePosition.x + 49,  blackTimePosition.y - 8,  49, 10, 0, 5, 0, 0, #26211b, #f0f0f0, #2d2d2a, true, timeCondition));
  timeButtons[1].add(new TimeButton(blackTimePosition.x + 105, blackTimePosition.y - 8,  49, 10, 5, 5, 0, 0, #26211b, #f0f0f0, #2d2d2a, true, timeCondition));
  timeButtons[1].add(new TimeButton(blackTimePosition.x,       blackTimePosition.y + 53, 48, 10, 0, 0, 0, 5, #26211b, #f0f0f0, #2d2d2a, false, timeCondition));
  timeButtons[1].add(new TimeButton(blackTimePosition.x + 49,  blackTimePosition.y + 53, 49, 10, 0, 0, 5, 0, #26211b, #f0f0f0, #2d2d2a, false, timeCondition));
  timeButtons[1].add(new TimeButton(blackTimePosition.x + 105, blackTimePosition.y + 53, 49, 10, 0, 0, 5, 5, #26211b, #f0f0f0, #2d2d2a, false, timeCondition));
  allButtons.addAll(timeButtons[0]);
  allButtons.addAll(timeButtons[1]);

  for (int i = 0; i < timeButtons.length; i++) {
    for (int j = 0; j < timeButtons[i].size(); j++) {
      timeButtons[i].get(j).setIndex(i, j % 3);
    }
  }

  // Boutons de presets
  presetButtons.add(new ImageButton(30, selectHeight-98, 65, 65, 5, #272522, loadImage("icons/rapid.png"), false, "rapidPreset", hubCondition));
  presetButtons.add(new ImageButton(110, selectHeight-98, 65, 65, 5, #272522, loadImage("icons/blitz.png"), false, "blitzPreset", hubCondition));
  presetButtons.add(new ImageButton(190, selectHeight-98, 65, 65, 5, #272522, loadImage("icons/bullet.png"), false, "bulletPreset", hubCondition));
  allButtons.addAll(presetButtons);

  // Aide et abandon
  Condition humainWCondition = new Condition() { public boolean c() { return(gameState == GAME && !useHacker && !gameEnded && isHumain(0)); } };
  Condition humainBCondition = new Condition() { public boolean c() { return(gameState == GAME && !useHacker && !gameEnded && isHumain(1)); } };
  float space = (offsetX - (76*w/70))/3;
  float hbSize = 38 * w/70;
  humainButton.add(new ImageButton(space,            offsetY + 7*w - 127, hbSize, hbSize, 10, #272522, loadImage("icons/resign.png"), false, "resignWhite", humainWCondition));
  humainButton.add(new ImageButton(space,            offsetY + w + 80   , hbSize, hbSize, 10, #272522, loadImage("icons/resign.png"), false, "resignBlack", humainBCondition));
  humainButton.add(new ImageButton(hbSize + 2*space, offsetY + 7*w - 127, hbSize, hbSize, 10, #272522, loadImage("icons/helpMove.png"), false, "helpMoveWhite", humainWCondition));
  humainButton.add(new ImageButton(hbSize + 2*space, offsetY + w + 80   , hbSize, hbSize, 10, #272522, loadImage("icons/helpMove.png"), false, "helpMoveBlack", humainBCondition));
  allButtons.addAll(humainButton);

  // Drag and drops
  Condition dragAndDropWCondition = new Condition() { public boolean c() { return (gameState == EDITOR && !showParameters && !showSavedPositions && addPiecesColor == 0); } };
  Condition dragAndDropBCondition = new Condition() { public boolean c() { return (gameState == EDITOR && !showParameters && !showSavedPositions && addPiecesColor == 1); } };
  Condition buttonEditorCondition = new Condition() { public boolean c() { return (gameState == EDITOR && !showParameters && !showSavedPositions); } };

  addPiecesColorSwitch = new CircleToggleButton(offsetX/2, (offsetY+w/2 + w*6) + 70, w/1.3, "switchAddPieceColor", buttonEditorCondition);
  allButtons.add(addPiecesColorSwitch);

  addPiecesButtons[0] = new ArrayList<DragAndDrop>();
  for (int i = 0; i < 6; i++) {
    addPiecesButtons[0].add(new DragAndDrop(offsetX/2, (offsetY+w/2 + w*i) + i*12.5, w, w, imageArrayB[i], i, dragAndDropWCondition));
  }
  allButtons.addAll(addPiecesButtons[0]);
  addPiecesButtons[1] = new ArrayList<DragAndDrop>();
  for (int i = 0; i < 6; i++) {
    addPiecesButtons[1].add(new DragAndDrop(offsetX/2, (offsetY+w/2 + w*i) + i*12.5, w, w, imageArrayN[i], i + 6, dragAndDropBCondition));
  }
  allButtons.addAll(addPiecesButtons[1]);

  // Icones de la partie
  Condition iconCondition = new Condition() { public boolean c() { return gameState == GAME; } };
  int iconSize = 40 * w/75;
  int edgeSpacing = (int)(offsetX - w) / 2 + 1;
  int distanceFromTop = (int)(offsetY - iconSize) / 2 + 1;
  int spacingBetweenIcons = ((cols * w + offsetX) - (edgeSpacing*2 + icons.length*iconSize)) / (icons.length-1);
  int[] numSc1 = {0, 1, 2, 3, 4, 5, 6, 7, 16, 10};
  PImage sbImg2;

  for (int i = 0; i < icons.length; i++) {
    if (i == 1) sbImg2 = loadImage("icons/variante.png");
    else if (i == 7) sbImg2 = loadImage("icons/pause.png");
    else sbImg2 = null;
    iconButtons.add(new ShortcutButton(edgeSpacing + i*iconSize + i*spacingBetweenIcons, distanceFromTop, iconSize, icons[i], sbImg2, iconCondition));
    iconButtons.get(i).setNumShortcut(numSc1[i]);
  }
  allButtons.addAll(iconButtons);

  // Icones de l'éditeur
  Condition editorCondition = new Condition() { public boolean c() { return gameState == EDITOR; } };
  int editorIconSize = 40 * w/75;
  int editorEdgeSpacing = (int)(offsetX - w) / 2 + 10;
  int spacingBetweenEditorIcons = ((cols * w + offsetX) - (editorEdgeSpacing*2 + editorIcons.length*editorIconSize)) / (editorIcons.length-1);
  int[] numSc2 = {0, 11, 13, 12, 15, 18, 17, 6, 14};

  for (int i = 0; i < editorIcons.length; i++) {
    editorIconButtons.add(new ShortcutButton(editorEdgeSpacing + i*editorIconSize + i*spacingBetweenEditorIcons, distanceFromTop, editorIconSize, editorIcons[i], editorIcons[i], editorCondition));
    editorIconButtons.get(i).setNumShortcut(numSc2[i]);
  }
  allButtons.addAll(editorIconButtons);

  // Boutons fens
  int startX = 12 + offsetX;
  int startY = 12 + offsetY;
  int size = 145;
  float espacementX = ( w*8 - (startX-offsetX)*2 - 3*size ) / 2;
  float espacementY = ( w*8 - (startX-offsetX) - 3*size ) / 3;
  Condition fenCondition = new Condition() { public boolean c() { return (gameState == EDITOR && showSavedPositions); } };

  for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
      int index = 3*i + j;
      if (index >= savedFENS.length) break;
      savedFENSbuttons.add(new ButtonFEN(startX + size/2 + j*(size + espacementX), startY + size/2 + i*(size + espacementY), size, savedFENSimage[index], savedFENSname[index], index, fenCondition));
    }
  }
  allButtons.addAll(savedFENSbuttons);

  // Boutons des paramètres de l'éditeur
  Condition paramCondition = new Condition() { public boolean c() { return (gameState == EDITOR && showParameters); } };
  parametersButtons.add(new ToggleImage(offsetX + 2.25*w, offsetY + 4.125*w, 2.5*w,   1.25*w, loadImage("icons/pRoqueB_off.png"), loadImage("icons/pRoqueB.png"), "togglePRoqueB", paramCondition));
  parametersButtons.add(new ToggleImage(offsetX + 6.06*w, offsetY + 4.125*w, 3.125*w, 1.25*w, loadImage("icons/gRoqueB_off.png"), loadImage("icons/gRoqueB.png"), "toggleGRoqueB", paramCondition));
  parametersButtons.add(new ToggleImage(offsetX + 2.25*w, offsetY + 6.125*w, 2.5*w,   1.25*w, loadImage("icons/pRoqueN_off.png"), loadImage("icons/pRoqueN.png"), "togglePRoqueN", paramCondition));
  parametersButtons.add(new ToggleImage(offsetX + 6.06*w, offsetY + 6.125*w, 3.125*w, 1.25*w, loadImage("icons/gRoqueN_off.png"), loadImage("icons/gRoqueN.png"), "toggleGRoqueN", paramCondition));
  parametersButtons.add(new ToggleImage(offsetX + 4*w, offsetY + 2*w, 1.25*w, 1.25*w, loadImage("pieces/roi_b.png"), loadImage("pieces/roi_n.png"), "toggleTrait", paramCondition));
  allButtons.addAll(parametersButtons);
}

void initImages() {
  imageArrayB = new PImage[6];
  imageArrayN = new PImage[6];

  imageArrayB[4] = loadImage("pieces/cavalier_b.png");
  imageArrayN[4] = loadImage("pieces/cavalier_n.png");
  imageArrayB[1] = loadImage("pieces/dame_b.png");
  imageArrayN[1] = loadImage("pieces/dame_n.png");
  imageArrayB[0] = loadImage("pieces/roi_b.png");
  imageArrayN[0] = loadImage("pieces/roi_n.png");
  imageArrayB[2] = loadImage("pieces/tour_b.png");
  imageArrayN[2] = loadImage("pieces/tour_n.png");
  imageArrayB[5] = loadImage("pieces/pion_b.png");
  imageArrayN[5] = loadImage("pieces/pion_n.png");
  imageArrayB[3] = loadImage("pieces/fou_b.png");
  imageArrayN[3] = loadImage("pieces/fou_n.png");

  icons[0] = loadImage("icons/pin.png");
  icons[1] = loadImage("icons/varianteGris.png");
  icons[2] = loadImage("icons/analysis.png");
  icons[3] = loadImage("icons/info.png");
  icons[4] = loadImage("icons/pgn.png");
  icons[5] = loadImage("icons/save.png");
  icons[6] = loadImage("icons/rotate.png");
  icons[7] = loadImage("icons/play.png");
  icons[8] = loadImage("icons/computer.png");
  icons[9] = loadImage("icons/quit.png");

  editorIcons[0] = loadImage("icons/pin.png");
  editorIcons[1] = loadImage("icons/delete.png");
  editorIcons[2] = loadImage("icons/copy.png");
  editorIcons[3] = loadImage("icons/info.png");
  editorIcons[4] = loadImage("icons/start.png");
  editorIcons[5] = loadImage("icons/paste.png");
  editorIcons[6] = loadImage("icons/parameter.png");
  editorIcons[7] = loadImage("icons/rotate.png");
  editorIcons[8] = loadImage("icons/quit.png");

  chess = loadImage("icons/chess.png");
  bot = loadImage("icons/hacker.png");
  botLarge = loadImage("icons/hacker-large.png");
  warning = loadImage("icons/warning.png");
  moutonAlertImg = loadImage("joueurs/lesmoutonsImgEnd.jpg");
  chesscomLogo = loadImage("icons/chesscom.png");
  lichessLogo = loadImage("icons/lichess.png");

  leftArrow = loadImage("icons/leftArrow.png");
  rightArrow = loadImage("icons/rightArrow.png");

  for (int i = 0; i < savedFENSimage.length; i++) {
    savedFENSimage[i] = loadImage("positions/position_" + i + ".png");
  }
}

void displayAlert() {
  if (millis() - alertStarted >= alertTime) {
    clearAlert();
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
  image(moutonAlertImg, alertPos.x + 0.125*w, alertPos.y + 0.125*w, 1.75*w, 1.75*w);

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
  noStroke();
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

  // Images
  imageMode(CORNER);
  image(botLarge, rectX - rectW/2 + 10*w/75, rectY - rectH/2 + 10*w/75, 90 * w/75, 90 * w/75);
  siteButton.show();

  // Titre
  fill(color(#b33430));
  textAlign(CENTER, CENTER);
  textSize(35 * w/75);
  text("Hacker mode activé", rectX + (100*w/75)/2, rectY - rectH/2 + 55*w/75);

  // Texte de configuration
  String hackerText = "";

  for (int i = 0; i < CALIBRATION_NUMBER; i++) {
    if (hackerPoints[i] == null) {
      hackerText = "Calibrer [" + calibrationDesc[i].toUpperCase() + "]";
      break;
    }
  }

  fill(0);
  noStroke();
  textSize(27 * w/75);
  text(hackerText, (width-offsetX)/2 + offsetX, rectY + (100*w/75)/3);

  String hg = (hackerPoints[UPLEFT] == null) ? "___" : str(hackerPoints[UPLEFT].x) + " ; " + str(hackerPoints[UPLEFT].y);
  String bd = (hackerPoints[DOWNRIGHT] == null) ? "___" : str(hackerPoints[DOWNRIGHT].x) + " ; " + str(hackerPoints[DOWNRIGHT].y);
  textSize(20 * w/75);
  text("HG : " + hg + "     " + "BD : " + bd, (width-offsetX)/2 + offsetX, rectY + (100*w/75)/1.15);

  // Informations en bas
  fill(255);
  rectMode(CORNER);
  rect(offsetX + 0.25*w, offsetY + 5.75*w, 7.5*w, 2*w);
  fill(color(#b33430));
  textSize(22 * w/75);
  text("Ne pas calibrer la nouvelle partie sur le +", offsetX + 4*w, offsetY + 7.35*w);
  textSize(19 * w/75);
  fill(0);
  textAlign(LEFT, CENTER);
  text("[ENTRÉE] Ajouter un point de calibration", offsetX+0.35*w, offsetY+6*w);
  text("[SUPPR] Restaurer les sauvegardes avec vérification", offsetX+0.35*w, offsetY+6.3*w);
  text("[SHIFT]  Forcer la restauration des sauvegardes", offsetX+0.35*w, offsetY+6.6*w);
  text("[ESPACE] Auto calibration", offsetX+0.35*w, offsetY+6.9*w);
}

void drawSavedPosition() {
  blur(220);
  for (ButtonFEN b : savedFENSbuttons) b.show();
}

void drawParameters() {
  blur(220);
  fill(0);
  stroke(0);

  for (ToggleImage ti : parametersButtons) ti.show();

  textAlign(CENTER);
  textSize(20 * w/70);
  text("Trait aux " + (tourDeQui == 0 ? "blancs" : "noirs"), offsetX + 4*w, offsetY + 3*w);
  textSize(18 * w/70);
  text("Petit roque blanc " + (pRoqueCondition(0) ? "activé" : "désactivé"), offsetX + 2.25*w, offsetY + 5*w);
  text("Grand roque blanc " + (gRoqueCondition(0) ? "activé" : "désactivé"), offsetX + 6.06*w, offsetY + 5*w);
  text("Petit roque noir " + (pRoqueCondition(1) ? "activé" : "désactivé"), offsetX + 2.25*w, offsetY + 7*w);
  text("Grand roque noir " + (gRoqueCondition(1) ? "activé" : "désactivé"), offsetX + 6.06*w, offsetY + 7*w);
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
  String playerName1 = (isMouton(0) ? "Mouton" : j1.name);
  String playerName2 = (isMouton(1) ? "Mouton" : j2.name);

  if (pointDeVue) {
    image(j1Img, space, height-(space+w), w, w);
    image(j2Img, space, offsetY + ( (offsetY<=10) ? space : 0), w, w);

    text(playerName1 + " (" + j1.elo + ")", space+w/2, height-(space+w)-space-5);
    text(playerName2 + " (" + j2.elo + ")", space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w);

    text(roundedString(j1.getScore()) + "/" + j1.getTotalScore(), space+w/2, height-(space+w)-space-40);
    text(roundedString(j2.getScore()) + "/" + j2.getTotalScore(), space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w+40);
    if (j1.lastEval != "") text("Eval : " + j1.lastEval, space+w/2, height-(space+w)-space-80);
    if (j2.lastEval != "") text("Eval : " + j2.lastEval, space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w+80);
  } else {
    image(j1Img, space, offsetY + ( (offsetY<=10) ? space : 0), w, w);
    image(j2Img, space, height-(space+w), w, w);

    text(playerName1 + " (" + j1.elo + ")", space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w);
    text(playerName2 + " (" + j2.elo + ")", space+w/2, height-(space+w)-space-5);

    text(roundedString(j1.getScore()) + "/" + j1.getTotalScore(), space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w+40);
    text(roundedString(j2.getScore()) + "/" + j2.getTotalScore(), space+w/2, height-(space+w)-space-40);
    if (j1.lastEval != "") text("Eval : " + j1.lastEval, space+w/2, (offsetY + ( (offsetY<=10) ? space : 0))+space+w+80);
    if (j2.lastEval != "") text("Eval : " + j2.lastEval, space+w/2, height-(space+w)-space-80);
  }
}

void drawTimeButtons() {
  fill(#f0f0f0);
  stroke(#f0f0f0);
  rect(whiteTimePosition.x, whiteTimePosition.y, 98, 55);
  rect(whiteTimePosition.x + 105, whiteTimePosition.y, 49, 55);
  fill(#26211b);
  textSize(30);
  textAlign(CENTER, CENTER);
  text(nf(times[0][0], 2) + ":" + nf(times[0][1], 2), whiteTimePosition.x + 50, whiteTimePosition.y + 24);
  text(nf(times[0][2], 2), whiteTimePosition.x + 130, whiteTimePosition.y + 24);

  fill(#26211b);
  stroke(#26211b);
  rect(blackTimePosition.x, blackTimePosition.y, 98, 55);
  rect(blackTimePosition.x + 105, blackTimePosition.y, 49, 55);
  fill(#f0f0f0);
  text(nf(times[1][0], 2) + ":" + nf(times[1][1], 2), blackTimePosition.x + 50, blackTimePosition.y + 24);
  text(nf(times[1][2], 2), blackTimePosition.x + 130, blackTimePosition.y + 24);
  for (int i = 0; i < timeButtons.length; i++) {
    for (int j = 0; j < timeButtons[i].size(); j++) {
      timeButtons[i].get(j).update();
      timeButtons[i].get(j).show();
    }
  }
}

void drawArrow(int fromI, int fromJ, int i, int j) {
  Arrow arrow = new Arrow(fromI, fromJ, i, j);
  for (Arrow a : allArrows) {
    if (a.equals(arrow)) {
      allArrows.remove(a);
      return;
    }
  }
  allArrows.add(arrow);
}

void updateDraggedArrow(int i, int j) {
  Arrow newArrow = new Arrow(lastCellRightClicked.i, lastCellRightClicked.j, i, j);
  allArrows.remove(lastArrowDrawn);
  allArrows.add(newArrow);
  lastArrowDrawn = newArrow;
}

void handleEndScreen() {
  if (useHacker) yEndScreen = targetEndScreenY;
  if (yEndScreen < targetEndScreenY) {
    float dy = targetEndScreenY - yEndScreen;
    dy = max(dy, 5);
    yEndScreen += dy * endScreenEasing;
  }

  float rectX = 1.75*w + offsetX, rectW = 4.5*w, rectH = 3*w;
  if (targetEndScreenY - yEndScreen <= 1 && mousePressed && (mouseX < rectX || mouseX >= rectX+rectW || mouseY < yEndScreen || mouseY >= yEndScreen+rectH)) disableEndScreen = true;

  drawEndScreen(yEndScreen);
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
  else if (isHumain(0) && !isHumain(1)) { //humain contre machine
    if (winner == 0) { fill(vert); title = "Vous avez gagné !"; }
    else { fill(gris); title = joueurs.get(1).victoryTitle; }
  }
  else if (!isHumain(0) && isHumain(1)) { //machine contre humain
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

void drawBoard() {
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].show();
    }
  }

  for (int i = 0; i < piecesToDisplay.size(); i++) {
    piecesToDisplay.get(i).show();
  }
}

void savePiecesPosition() {
  for (int i = 0; i < piecesToDisplay.size(); i++) {
    piecesToDisplay.get(i).savePosition();
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
      grid[i][j].yellow = false;
      grid[i][j].red = false;
    }
  }

  allArrows.clear();
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

void switchAddPieceColor() {
  addPiecesColor = opponent(addPiecesColor);
}

void clickedOnBoard(int i, int j) {
  if (enPromotion != null) return;

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
    pieceDisplayOnEnd(p);
    return;
  }
}

void clickedOnEditorBoard(int i, int j) {
  Piece p = grid[i][j].piece;

  if (grid[i][j].freeMove) {
    pieceSelectionne.quickMove(i, j);
    pieceSelectionne = null;
    deselectAll();
    return;
  }
  if (p == null) return;

  if (mouseButton == LEFT) {
    deselectAll();
    p.fly();
    grid[p.i][p.j].selected = true;
    pieceSelectionne = p;
    pieceDisplayOnEnd(p);
    return;
  }
  if (mouseButton == RIGHT) {
    for (int n = 0; n < piecesToDisplay.size(); n++) {
      if (piecesToDisplay.get(n) == p) { piecesToDisplay.remove(n); break; }
    }
    removePiece(p);
  }
}

void pieceDisplayOnEnd(Piece p) {
  for (int i = 0; i < piecesToDisplay.size(); i++) {
    Piece piece = piecesToDisplay.get(i);
    if (piece == p) {
      piecesToDisplay.remove(i);
      piecesToDisplay.add(p);
    }
  }
}

void setPieces() {
  importFEN(startFEN);

  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);

  calcEndGameWeight();
  zobrist.initHash();
  // println("[PARTIE] Hash de la position : " + zobrist.hash);
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
  if (SOUND_CONTROL < 1 || (gameEnded && !rewind)) return;

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
  if (TIME_CONTROL && !useHacker) {
    times[0][0] = 1; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 1; times[1][1] = 0; times[1][2] = 0;
  }
  t1.setValue(720);
  t2.setValue(720);
}

void blitzPreset() {
  if (TIME_CONTROL && !useHacker) {
    times[0][0] = 3; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 3; times[1][1] = 0; times[1][2] = 0;
  }
  t1.setValue(1964);
  t2.setValue(1964);
}

void rapidPreset() {
  if (TIME_CONTROL && !useHacker) {
    times[0][0] = 10; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 10; times[1][1] = 0; times[1][2] = 0;
  }
  t1.setValue(7500);
  t2.setValue(7500);
}

void noTimePreset() {
  if (TIME_CONTROL) {
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

  println("[EDITEUR] Plateau importé");

  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);

  calcEndGameWeight();
  zobrist.initHash();
}

void pasteFEN() {
  startFEN = GetTextFromClipboard();
  setPieces();
  println ("[MENU] Fen importée");
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

  // Position
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

  // Trait
  if (tourDeQui == 0) {
    fen = fen + "w";
  } else {
    fen = fen + "b";
  }

  // Roques (blancs, noirs)
  boolean pRoques[] = {false, false};
  boolean gRoques[] = {false, false};

  for (int i = 0; i < pieces.length; i++) {
    if (rois[i].roquable != 1) continue;
    for (int j = 0; j < pieces[i].size(); j++) {
      if (pieces[i].get(j).petitRoquable == 1) pRoques[i] = true;
      if (pieces[i].get(j).grandRoquable == 1) gRoques[i] = true;
    }
  }

  if (pRoques[0] || pRoques[1] || gRoques[0] || gRoques[1]) {
    fen = fen + " ";
    if (pRoques[0] && pRoquePosition(0)) fen = fen + "K";
    if (gRoques[0] && gRoquePosition(0)) fen = fen + "Q";
    if (pRoques[1] && pRoquePosition(1)) fen = fen + "k";
    if (gRoques[1] && gRoquePosition(1)) fen = fen + "q";
  }

  return fen;
}

void importFEN(String f) { // Fen simplifiée, sans en passant et règle des 50 coups
  removeAllPieces();

  // Roques et trait
  int pRoqueB = 0, pRoqueN = 0, gRoqueB = 0, gRoqueN = 0;
  int roiRoqueB = 0; int roiRoqueN = 0;
  boolean stopEvaluatingEnd = false;

  for (int i = f.length() - 1; i >= 0; i--) {
    if (stopEvaluatingEnd) break;
    char c = f.charAt(i); // Current char
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

  // Importe la position
  int cursorI = 0, cursorJ = 0;
  boolean stopEvaluatingStart = false;

  for (int i = 0; i < f.length(); i++) {
    if (stopEvaluatingStart) break;
    char c = f.charAt(i); // Current char
    int pieceColor = Character.isLowerCase(c) ? 1 : 0;

    switch (Character.toLowerCase(c)) {
      case '/':
        cursorI = 0; cursorJ++; // Nouvelle ligne
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

  // Règle les roques
  if (pRoqueB == 1 && pRoquePosition(0)) grid[7][7].piece.setRoques(1, 0);
  if (gRoqueB == 1 && gRoquePosition(0)) grid[0][7].piece.setRoques(0, 1);

  if (pRoqueN == 1 && pRoquePosition(1)) grid[7][0].piece.setRoques(1, 0);
  if (gRoqueN == 1 && gRoquePosition(1)) grid[0][0].piece.setRoques(0, 1);
}

void toggleTrait() {
  tourDeQui = opponent(tourDeQui);
}

boolean pRoquePosition(int qui) {
  int j_ = (qui == 0 ? 7 : 0);
  return (grid[4][j_].piece != null && rois[qui].i == 4 && rois[qui].j == j_ && grid[7][j_].piece != null && grid[7][j_].piece.pieceIndex == TOUR_INDEX && grid[7][j_].piece.c == qui);
}

boolean gRoquePosition(int qui) {
  int j_ = (qui == 0 ? 7 : 0);
  return (grid[4][j_].piece != null && rois[qui].i == 4 && rois[qui].j == j_ && grid[0][j_].piece != null && grid[0][j_].piece.pieceIndex == TOUR_INDEX && grid[0][j_].piece.c == qui);
}

boolean pRoqueCondition(int qui) {
  int j_ = (qui == 0 ? 7 : 0);
  return (pRoquePosition(qui) && rois[qui].roquable == 1 && grid[7][j_].piece.petitRoquable == 1);
}

boolean gRoqueCondition(int qui) {
  int j_ = (qui == 0 ? 7 : 0);
  return (gRoquePosition(qui) && rois[qui].roquable == 1 && grid[0][j_].piece.grandRoquable == 1);
}

void togglePRoqueB() {
  if (!pRoquePosition(0)) {
    parametersButtons.get(0).state = false;
    return;
  }

  if (pRoqueCondition(0)) {
    grid[7][7].piece.petitRoquable = 0;
    if (!gRoqueCondition(0)) rois[0].roquable = 0;
  } else {
    grid[7][7].piece.petitRoquable = 1;
    rois[0].roquable = 1;
  }
}

void togglePRoqueN() {
  if (!pRoquePosition(1)) {
    parametersButtons.get(2).state = false;
    return;
  }

  if (pRoqueCondition(1)) {
    grid[7][0].piece.petitRoquable = 0;
    if (!gRoqueCondition(1)) rois[1].roquable = 0;
  } else {
    grid[7][0].piece.petitRoquable = 1;
    rois[1].roquable = 1;
  }
}

void toggleGRoqueB() {
  if (!gRoquePosition(0)) {
    parametersButtons.get(1).state = false;
    return;
  }

  if (gRoqueCondition(0)) {
    grid[0][7].piece.grandRoquable = 0;
    if (!pRoqueCondition(0)) rois[0].roquable = 0;
  } else {
    grid[0][7].piece.grandRoquable = 1;
    rois[0].roquable = 1;
  }
}

void toggleGRoqueN() {
  if (!gRoquePosition(1)) {
    parametersButtons.get(3).state = false;
    return;
  }

  if (gRoqueCondition(1)) {
    grid[0][0].piece.grandRoquable = 0;
    if (!pRoqueCondition(1)) rois[1].roquable = 0;
  } else {
    grid[0][0].piece.grandRoquable = 1;
    rois[1].roquable = 1;
  }
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

Piece removePieceToDisplay(Piece piece) {
  for (int n = 0; n < piecesToDisplay.size(); n++) {
    if (piecesToDisplay.get(n) == piece) {
      Piece p = piecesToDisplay.get(n);
      piecesToDisplay.remove(n);
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
      int opColor = opponent(i);

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
    int opColor = opponent(checkColor);
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
