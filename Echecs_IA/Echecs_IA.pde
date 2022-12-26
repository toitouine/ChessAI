// TODO :

// Bon en finale, très bon en finale de pion et de roi
// Correct en ouverture, très mauvais en milieu de jeu (sécurité du roi)

// Sécurité du roi
// Editeur de position : Trait et roques
// Structures de pion
// Bouton d'abandon
// Plus d'ouvertures
// Système de vérification de fens
// Analyse de parties
// Les moutons :
//   - Menaces OK
//   - Missclick
//   - Pièces qui apparaissent (ou pas)
//   - Arnaques au temps OK
// Scan automatique d'échiquier
// Gestion du temps dans le menu

/////////////////////////////////////////////////////////////////

// Libraries

import java.awt.*;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import java.awt.Robot;
import com.sun.awt.AWTUtilities;
import processing.awt.PSurfaceAWT;
import processing.awt.PSurfaceAWT.SmoothCanvas;
import processing.sound.*;
import controlP5.*;

/////////////////////////////////////////////////////////////////

// Objects

ControlP5 cp5;
SoundFile enPassant;
SoundFile pachamama;
SoundFile violons;
SoundFile diagnostic;
SoundFile start_sound;
SoundFile castle_sound;
SoundFile check_sound;
SoundFile mat_sound;
SoundFile move_sound;
SoundFile nulle_sound;
SoundFile prise_sound;

TimerApplet ta;
GraphApplet ga;
SearchApplet sa;
HackerApplet ha;

PImage cavalier_b;
PImage cavalier_n;
PImage dame_b;
PImage dame_n;
PImage roi_b;
PImage roi_n;
PImage tour_b;
PImage tour_n;
PImage pion_b;
PImage pion_n;
PImage fou_b;
PImage fou_n;

PImage loic;
PImage antoine;
PImage stockfish;
PImage lemaire;
PImage lesmoutons;
PImage human;
PImage mark;

PImage j1Img;
PImage j2Img;
PImage j1ImgEnd;
PImage j2ImgEnd;

PImage[] icons = new PImage[10];
PImage[] editorIcons = new PImage[7];
PImage[] saveFENSimage = new PImage[7];
PImage upArrow;
PImage downArrow;
PImage pause;
PImage chess;
PImage bot;
PImage botLarge;
PImage idIcon;
PImage idIconOff;
PImage warning;

Robot hacker;

PreComputedData pc = new PreComputedData();
Zobrist zobrist = new Zobrist();

// Initialise la table de transposition
// Pour éviter les collisions, elle a une taille de 16 777 216
// Cette taille a besoin d'être une puissance de 2 (pour que l'index maximum ne soit composé que de 1)
TranspositionTable tt = new TranspositionTable(16777216);

Cell[][] grid = new Cell[8][8];
Piece[] rois = new Piece[2];
int[] materials = new int[2];

/////////////////////////////////////////////////////////////////

// Interfaces - Boutons - ArrayList

ArrayList<Piece> piecesToDisplay = new ArrayList<Piece>();
ArrayList<Piece>[] pieces = new ArrayList[2];
ArrayList<Joueur> joueurs = new ArrayList<Joueur>();
ArrayList<Bouton> promoButtons = new ArrayList<Bouton>();

ArrayList<Toggle> toggles1 = new ArrayList<Toggle>();
ArrayList<Toggle> toggles2 = new ArrayList<Toggle>();
ArrayList<Bouton> iconButtons = new ArrayList<Bouton>();
ArrayList<Bouton> editorIconButtons = new ArrayList<Bouton>();
ArrayList<TextBouton> hubButtons = new ArrayList<TextBouton>();
ArrayList<ButtonFEN> savedFENSbuttons = new ArrayList<ButtonFEN>();
ArrayList<DragAndDrop>[] addPiecesButtons = new ArrayList[2];

ArrayList<String> book = new ArrayList<String>();
ArrayList<Arrow> bookArrows = new ArrayList<Arrow>();
ArrayList<Arrow> varianteArrows = new ArrayList<Arrow>();

ArrayList<String> positionHistory = new ArrayList<String>();
ArrayList<Move> movesHistory = new ArrayList<Move>();
ArrayList<Long> zobristHistory = new ArrayList<Long>();

CircleToggle addPiecesColorSwitch;
Bouton positionEditor;
Bouton hackerButton;
Piece pieceSelectionne = null;
Piece enPromotion = null;
TextBouton rematchButton;
TextBouton newGameButton;
DragAndDrop enAjoutPiece = null;
Slider s1, s2, q1, q2, t1, t2;

Shortcut sc = new Shortcut();

/////////////////////////////////////////////////////////////////

// Variables pour la partie
int cols = 8;
int rows = 8;
int tourDeQui = 0; //dakin
float nbTour = 0.5;
float endGameWeight = 0;
boolean gameEnded = false;
String infos = "";
String infoBox = "";
String pgn = "";

boolean play = true;
boolean disableEndScreen = false;
boolean engineToPlay = false;
boolean playEngineMoveNextFrame = false;
boolean stopSearch = false;
boolean rewind = false;
boolean showGraph = false;
boolean showVariante = false;
boolean showSavedPositions = false;
boolean showSearchController = false;
boolean blockPlaying = false;
int gameState = 0;
int winner = -1;
int timeAtEnd = 0;
int rewindCount = 0;
int requestToRestart = -1;
long rngState = 1804289383;
String endReason = "";
String alert = "";
int alertTime = 0;
long alertStarted = 0;

// Les Moutons !
boolean missClickDragNextMove = false;

// En mémoire du vecteur vitesse
int slider;
int speed = 30;

// Joueurs
String j1;
String j2;
int j1depth = 3;
int j2depth = 3;
int j1Quiet = 30;
int j2Quiet = 30;
int j1Time = 1000;
int j2Time = 1000;

// Hacker
Point upLeftCorner, downRightCorner;
Point saveUpLeftCorner, saveDownRightCorner;
Color hackerWhitePieceColor, hackerBlackPieceColor;
Color saveWhitePieceColor, saveBlackPieceColor;
long lastHackerScan = 0;
int hackerScanCooldown = 400;
boolean useHacker = false;
boolean hackerPret = false;
Point[][] hackerCoords = new Point[8][8];
Point[][] saveHackerCoords = new Point[8][8];

/////////////////////////////////////////////////////////////////

void settings() {
  size(selectWidth, selectHeight);
}

void setup() {
  background(49, 46, 43);

  // Importe le livre
  String[] bookArray = loadStrings("book.txt");
  for (int i = 0; i < bookArray.length; i++) {
    book.add(bookArray[i]);
  }

  // Crée la fenêtre du contrôle du temps, graphique et réglage de la fenêtre principale
  String[] argsT = {"Pendules"};
  ta = new TimerApplet();
  PApplet.runSketch(argsT, ta);
  ta.hide();

  String[] argsG = {"Analyse"};
  ga = new GraphApplet();
  PApplet.runSketch(argsG, ga);
  ga.hide();

  String[] argsS = {"Search controller"};
  sa = new SearchApplet();
  PApplet.runSketch(argsS, sa);
  sa.hide();

  String[] argsH = {"Hacker"};
  ha = new HackerApplet();
  PApplet.runSketch(argsH, ha);
  ha.hide();

  surface.setVisible(true);
  surface.setTitle(name + " - Selection des joueurs");
  surface.setLocation(displayWidth/2 - width/2, 23);

  // Initialise le hacker
  try {
    hacker = new Robot();
  } catch(Exception e) {
    e.printStackTrace();
  }

  // Importe les sons
  if (soundControl >= 1) {
    start_sound = new SoundFile(this, "sons/start.wav");
    castle_sound = new SoundFile(this, "sons/castle.wav");
    check_sound = new SoundFile(this, "sons/check.wav");
    mat_sound = new SoundFile(this, "sons/mat.wav");
    move_sound = new SoundFile(this, "sons/move.wav");
    nulle_sound = new SoundFile(this, "sons/nulle.wav");
    prise_sound = new SoundFile(this, "sons/prise.wav");
    enPassant = new SoundFile(this, "sons/enPassant.mp3");
  }
  if (soundControl >= 2) {
    pachamama = new SoundFile(this, "sons/pachamama.mp3");
    diagnostic = new SoundFile(this, "sons/diagnostic.mp3");
    violons = new SoundFile(this, "sons/violons.mp3");
    violons.play(); violons.loop();
  }

  // Control P5
  cp5 = new ControlP5(this);
  s1 = cp5.addSlider("j1depth")
     .setPosition(1180, 80)
     .setSize(30,140)
     .setLabel("Profondeur")
     .setRange(1,30)
     .setNumberOfTickMarks(30)
     .setValue(j1depth)
     .setColorForeground(#8da75a)
     .setColorActive(#abcc6a)
     .setColorBackground(#5d6e3b);

  s2 = cp5.addSlider("j2depth")
     .setPosition(1180, 290)
     .setSize(30, 140)
     .setLabel("Profondeur")
     .setRange(1,30)
     .setNumberOfTickMarks(30)
     .setValue(j2depth)
     .setColorForeground(#8da75a)
     .setColorActive(#abcc6a)
     .setColorBackground(#5d6e3b);

  q1 = cp5.addSlider("j1Quiet")
     .setPosition(1250, 80)
     .setSize(30, 140)
     .setLabel("Max Quiet")
     .setRange(0,30)
     .setNumberOfTickMarks(31)
     .setValue(j1Quiet)
     .setColorForeground(#8da75a)
     .setColorActive(#abcc6a)
     .setColorBackground(#5d6e3b);

  q2 = cp5.addSlider("j2Quiet")
     .setPosition(1250, 290)
     .setSize(30, 140)
     .setLabel("Max Quiet")
     .setRange(0,30)
     .setNumberOfTickMarks(31)
     .setValue(j2Quiet)
     .setColorForeground(#8da75a)
     .setColorActive(#abcc6a)
     .setColorBackground(#5d6e3b);

  t1 = cp5.addSlider("j1Time")
      .setPosition(1320, 80)
      .setSize(30, 140)
      .setLabel("Temps")
      .setRange(0, 10000)
      .setValue(1000)
      .setColorForeground(#bdbd64)
      .setColorActive(#d6d46f)
      .setColorBackground(#827e40);

  t2 = cp5.addSlider("j2Time")
      .setPosition(1320, 290)
      .setSize(30, 140)
      .setLabel("Temps")
      .setRange(0, 10000)
      .setValue(1000)
      .setColorForeground(#bdbd64)
      .setColorActive(#d6d46f)
      .setColorBackground(#827e40);

  // Importe les images
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
  icons[1] = loadImage("icons/variante.png");
  icons[2] = loadImage("icons/analysis.png");
  icons[3] = loadImage("icons/info.png");
  icons[4] = loadImage("icons/pgn.png");
  icons[5] = loadImage("icons/save.png");
  icons[6] = loadImage("icons/rotate.png");
  icons[7] = loadImage("icons/play.png");
  icons[8] = loadImage("icons/parameter.png");
  icons[9] = loadImage("icons/quit.png");

  editorIcons[0] = loadImage("icons/pin.png");
  editorIcons[1] = loadImage("icons/delete.png");
  editorIcons[2] = loadImage("icons/copy.png");
  editorIcons[3] = loadImage("icons/info.png");
  editorIcons[4] = loadImage("icons/start.png");
  editorIcons[5] = loadImage("icons/rotate.png");
  editorIcons[6] = loadImage("icons/quit.png");

  pause = loadImage("icons/pause.png");
  chess = loadImage("icons/chess.png");
  bot = loadImage("icons/hacker.png");
  botLarge = loadImage("icons/hacker-large.png");
  warning = loadImage("icons/warning.png");

  loic = loadImage("joueurs/loic.jpeg");
  antoine = loadImage("joueurs/antoine.jpg");
  stockfish = loadImage("joueurs/stockfish.png");
  lemaire = loadImage("joueurs/lemaire.jpg");
  lesmoutons = loadImage("joueurs/lesmoutons.jpg");
  human = loadImage("joueurs/human.png");
  mark = loadImage("checkmark.png");

  for (int i = 0; i < saveFENSimage.length; i++) {
    saveFENSimage[i] = loadImage("positions/position_" + i + ".png");
  }

  // Initialise le plateau et les coordonnées du hacker
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j] = new Cell(i, j, i*w+offsetX, j*w+offsetY);
    }
  }

  for (int i = 0; i < 8; i++) {
    for (int j = 0; j < 8; j++) {
      hackerCoords[i][j] = new Point();
    }
  }

  // Initialise les boutons et interfaces
  hubButtons.add(new TextBouton(width/2 - 190, 480, 380, 75, "Nouvelle partie", 30, 10));
  hubButtons.add(new TextBouton(width-110, height-40, 100, 30, "Coller FEN", 18, 8)); hubButtons.get(1).setColors(#1d1c1a, #ffffff);
  hubButtons.add(new TextBouton(width-220, height-40, 100, 30, "Copier FEN", 18, 8)); hubButtons.get(2).setColors(#1d1c1a, #ffffff);

  promoButtons.add(new Bouton(0.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[1], imageArrayN[1]));
  promoButtons.add(new Bouton(2.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[2], imageArrayN[2]));
  promoButtons.add(new Bouton(4.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[3], imageArrayN[3]));
  promoButtons.add(new Bouton(6.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[4], imageArrayN[4]));

  toggles1.add(new Toggle(40, 80, 150, stockfish, "Stockfish"));
  toggles1.add(new Toggle(230, 80, 150, antoine, "Antoine"));
  toggles1.add(new Toggle(420, 80, 150, loic, "Loic"));
  toggles1.add(new Toggle(610, 80, 150, lesmoutons, "LesMoutons"));
  toggles1.add(new Toggle(800, 80, 150, lemaire, "LeMaire"));
  toggles1.add(new Toggle(990, 80, 150, human, "Humain"));

  toggles2.add(new Toggle(40, 290, 150, stockfish, "Stockfish"));
  toggles2.add(new Toggle(230, 290, 150, antoine, "Antoine"));
  toggles2.add(new Toggle(420, 290, 150, loic, "Loic"));
  toggles2.add(new Toggle(610, 290, 150, lesmoutons, "LesMoutons"));
  toggles2.add(new Toggle(800, 290, 150, lemaire, "LeMaire"));
  toggles2.add(new Toggle(990, 290, 150, human, "Humain"));

  addPiecesColorSwitch = new CircleToggle(offsetX/2, (offsetY+w/2 + w*6) + 70, w/1.3);
  positionEditor = new Bouton(width-55, 10, 50, chess, chess);
  hackerButton = new Bouton(width-100, 11, 40, bot, bot);
  rematchButton = new TextBouton(offsetX - offsetX/1.08, offsetY+4*w-29, offsetX-2*(offsetX - offsetX/1.08), 24, "Revanche", 15, 3);
  rematchButton.setColors(#1d1c1a, #ffffff);
  newGameButton = new TextBouton(offsetX - offsetX/1.08, offsetY+4*w+5, offsetX-2*(offsetX - offsetX/1.08), 24, "Menu", 15, 3);
  newGameButton.setColors(#1d1c1a, #ffffff);

  // Drag and drops
  addPiecesButtons[0] = new ArrayList<DragAndDrop>();
  for (int i = 0; i < 6; i++) {
    addPiecesButtons[0].add(new DragAndDrop(offsetX/2, (offsetY+w/2 + w*i) + i*12.5, w, w, imageArrayB[i], i));
  }
  addPiecesButtons[1] = new ArrayList<DragAndDrop>();
  for (int i = 0; i < 6; i++) {
    addPiecesButtons[1].add(new DragAndDrop(offsetX/2, (offsetY+w/2 + w*i) + i*12.5, w, w, imageArrayN[i], i + 6));
  }

  // Icones de la partie
  int[] numSc1 = {0, 1, 2, 3, 4, 5, 6, 7, 16, 10};
  for (int i = 0; i < icons.length; i++) {
    //pour simplifier, tous les boutons ont "pause" comme deuxième état
    iconButtons.add(new Bouton(edgeSpacing + i*iconSize + i*spacingBetweenIcons, distanceFromTop, iconSize, icons[i], pause));
    iconButtons.get(i).setNumShortcut(numSc1[i]);
  }

  // Icones de l'éditeur
  int[] numSc2 = {0, 11, 13, 12, 15, 6, 14};
  for (int i = 0; i < editorIcons.length; i++) {
    editorIconButtons.add(new Bouton(editorEdgeSpacing + i*editorIconSize + i*spacingBetweenEditorIcons, distanceFromTop, editorIconSize, editorIcons[i], editorIcons[i]));
    editorIconButtons.get(i).setNumShortcut(numSc2[i]);
  }

  // Boutons fens
  int startX = 12 + offsetX;
  int startY = 12 + offsetY;
  int size = 145;
  float espacementX = ( w*8 - (startX-offsetX)*2 - 3*size ) / 2;
  float espacementY = ( w*8 - (startX-offsetX) - 3*size ) / 3;

  for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
      int index = 3*i + j;
      if (index >= savedFENS.length) break;
      savedFENSbuttons.add(new ButtonFEN(startX + size/2 + j*(size + espacementX), startY + size/2 + i*(size + espacementY), size, saveFENSimage[index], savedFENSname[index]));
    }
  }

  // Place les pièces
  pieces[0] = new ArrayList<Piece>();
  pieces[1] = new ArrayList<Piece>();
  setPieces();

  // Initialise PreComputedData
  pc.init();

  for (int i = 0; i < 18; i++) println();
}

void draw() {

  ////////////////////////////////////////////////////////////////

  // Update request to restart
  if (requestToRestart != -1 && millis() - requestToRestart >= restartTimeOut) {
    requestToRestart = -1;
    println(">>> Annulation\n");
  }

  ////////////////////////////////////////////////////////////////

  // Partie

  if (gameState == 1) {
    background(49, 46, 43);

    // Titre de la fenêtre
    surface.setTitle(name + " - " + j1 + " (" + ((joueurs.get(0).useIterativeDeepening) ? "ID" : j1depth) +  ") contre " + j2 + " (" + ((joueurs.get(1).useIterativeDeepening) ? "ID" : j2depth) + ")" + ((infos == "") ? "" : " - ") + infos);

    updateBoard();
    drawPlayersInfos();
    for (Arrow b : bookArrows) b.show();

    if (enPromotion != null) {
      fill(220, 220, 220, 200);
      rectMode(CORNER);
      rect(offsetX, offsetY, cols*w, rows*w);
      showPromoButtons();
    }

    if (playEngineMoveNextFrame) { joueurs.get(tourDeQui).play(); playEngineMoveNextFrame = false; engineToPlay = false; }
    if (engineToPlay) { playEngineMoveNextFrame = true; }

    if (!gameEnded && play && (!useHacker || hackerPret)) {
      if (joueurs.get(0).name != "Humain" && joueurs.get(1).name != "Humain") {
        if (speed == 0) joueurs.get(tourDeQui).play();
        else if (frameCount % speed == 0) joueurs.get(tourDeQui).play();
      }
    }

    for (int i = 0; i < iconButtons.size(); i++) {
      Bouton b = iconButtons.get(i);
      if (i == 7) b.show(play ? 0 : 1); // Play / Pause
      else b.show(0);
    }

    // Affichage de l'écran de fin de partie
    if (!disableEndScreen && gameEnded && millis() - timeAtEnd > timeBeforeEndDisplay) {
      float dy = targetEndScreenY - yEndScreen;
      yEndScreen += dy * endScreenEasing;

      float rectX = 1.75*w + offsetX, rectW = 4.5*w, rectH = 3*w;
      if (targetEndScreenY - yEndScreen <= 1 && mousePressed && (mouseX < rectX || mouseX >= rectX+rectW || mouseY < yEndScreen || mouseY >= yEndScreen+rectH)) disableEndScreen = true;
      drawEndScreen(yEndScreen);
    }
    if (gameEnded) {
      newGameButton.show();
      rematchButton.show();
    }

    // Variantes
    if (showVariante) {
      for (Arrow arrow : varianteArrows) arrow.show();
    }

    // Hacker
    if (useHacker) {
      if (!hackerPret) { drawHackerPage(); }
      else {
        if (play && !gameEnded && enPromotion == null && millis() - lastHackerScan >= hackerScanCooldown) scanMoveOnBoard();
      }
    }

    // Affichages
    if (alert != "") displayAlert();
    if (infoBox != "") drawInfoBox(infoBox);

    // Actualise "block playing", qui empêche éventuellement un joueur de jouer
    updateBlockPlaying();
  }

  ////////////////////////////////////////////////////////////////

  // Menu principal

  else if (gameState == 0) {
    background(49, 46, 43);

    for (TextBouton b : hubButtons) b.show();

    fill(255);
    textSize(30);
    textAlign(LEFT, LEFT);
    text("Sélection des joueurs :", 40, 45);
    strokeWeight(1);
    stroke(255);
    line(40, 50, 370, 50);

    fill(255);
    textAlign(LEFT, LEFT);
    textSize(15);
    text(startFEN, 10, height-10);

    positionEditor.show(0);
    if (useHacker) hackerButton.show(0);

    for (Toggle t : toggles1) {
      t.show();
    }
    for (Toggle t : toggles2) {
      t.show();
    }
  }

  ////////////////////////////////////////////////////////////////

  // Éditeur de position

  else if (gameState == 3) {
    background(49, 46, 43);

    updateBoard();

    for (int i = 0; i < editorIconButtons.size(); i++) {
      editorIconButtons.get(i).show(0);
    }

    if (infoBox != "") drawInfoBox(infoBox);
    if (showSavedPositions) drawSavedPosition();

    if (infos != "") surface.setTitle(name + " - Editeur de position - " + infos);
    else surface.setTitle(name + " - Editeur de position");

    for (DragAndDrop d : addPiecesButtons[addPiecesColor]) d.show();
    addPiecesColorSwitch.show();
  }
}
