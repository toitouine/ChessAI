import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.awt.*; 
import java.awt.Frame; 
import java.awt.MouseInfo; 
import java.awt.Point; 
import java.awt.datatransfer.*; 
import java.awt.event.InputEvent; 
import java.awt.Robot; 
import processing.awt.PSurfaceAWT; 
import processing.awt.PSurfaceAWT.SmoothCanvas; 
import processing.sound.*; 
import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Echecs_IA extends PApplet {

/////////////////////////////////////////////////////////////////

// TODO :

// Editeur de position : Trait et roques
// Bouton d'abandon
// Bouton d'aide
// (Recherche de coup sur autre thread)
// Héritage : boutons et gui

/////////////////////////////////////////////////////////////////

// Libraries













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
PImage[] editorIcons = new PImage[9];
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
PImage mouton;

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

// Interfaces, boutons et arraylist

ArrayList<Piece> piecesToDisplay = new ArrayList<Piece>();
ArrayList<Piece>[] pieces = new ArrayList[2];
Piece[] currentEnPassantable = {null, null};
ArrayList<Joueur> joueurs = new ArrayList<Joueur>();
ArrayList<Button> promoButtons = new ArrayList<Button>();

ArrayList<ToggleButton> toggles1 = new ArrayList<ToggleButton>();
ArrayList<ToggleButton> toggles2 = new ArrayList<ToggleButton>();
ArrayList<Button> iconButtons = new ArrayList<Button>();
ArrayList<Button> editorIconButtons = new ArrayList<Button>();
ArrayList<TextButton> hubButtons = new ArrayList<TextButton>();
ArrayList<ButtonFEN> savedFENSbuttons = new ArrayList<ButtonFEN>();
ArrayList<DragAndDrop>[] addPiecesButtons = new ArrayList[2];
ArrayList<TimeButton>[] timeButtons = new ArrayList[2];
ArrayList<ImageButton> presetButtons = new ArrayList<ImageButton>();

ArrayList<String> book = new ArrayList<String>();
ArrayList<Arrow> bookArrows = new ArrayList<Arrow>();
ArrayList<Arrow> varianteArrows = new ArrayList<Arrow>();

ArrayList<String> positionHistory = new ArrayList<String>();
ArrayList<Move> movesHistory = new ArrayList<Move>();
ArrayList<Long> zobristHistory = new ArrayList<Long>();

CircleToggleButton addPiecesColorSwitch;
Button positionEditor;
Button hackerButton;
Piece pieceSelectionne = null;
Piece enPromotion = null;
TextButton rematchButton;
TextButton newGameButton;
DragAndDrop enAjoutPiece = null;
Slider s1, s2, q1, q2, t1, t2;

Shortcut sc = new Shortcut();

/////////////////////////////////////////////////////////////////

// Variables pour la partie
int cols = 8;
int rows = 8;
int tourDeQui = 0; //dakin
float nbTour = 0.5f;
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
boolean showParameters = false;
boolean blockPlaying = false;
boolean useTime = false;
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
String messageMouton = "";
int messageMoutonStarted = 0;
int messageMoutonTime = 0;
int tourPourApparition = 10;
int missclickCount = 0, appearCount = 0, timeCount = 0, messagesCount = 0;
Point alertPos = new Point();
boolean missclickDragNextMove = false;
float lastMissclick = 0;

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
Point upLeftCorner, downRightCorner, newgameLocation;
Point saveUpLeftCorner, saveDownRightCorner, saveNewgameLocation;
Color hackerWhitePieceColor, hackerBlackPieceColor;
Color saveWhitePieceColor, saveBlackPieceColor;
long lastHackerScan = 0;
boolean hackerWaitingToRestart = false;
int timeAtLastRestartTry = 0;
int currentHackerPOV = 0;
int timeAtHackerEnd = 0;
int lastMoveTime = 0;
int deltaTimeMesured = 0;
int numberOfScan = 0;
boolean isNextMoveRestranscrit = false;
boolean useHacker = false;
boolean hackerPret = false;
boolean hackerAPImode = false;
Point[][] hackerCoords = new Point[8][8];
Point[][] saveHackerCoords = new Point[8][8];

/////////////////////////////////////////////////////////////////

// Constantes

int CONSTANTE_DE_STOCKFISH = 3;
float TOTAL_DEPART = 3200.0f;

int ROI_INDEX = 0;
int DAME_INDEX = 1;
int TOUR_INDEX = 2;
int FOU_INDEX = 3;
int CAVALIER_INDEX = 4;
int PION_INDEX = 5;

int CHESSCOM = 0;
int LICHESS = 1;

int INITIAL_TOTAL_MAIRE_MATERIAL = 0;

/////////////////////////////////////////////////////////////////

public void settings() {
  size(selectWidth, selectHeight);
}

public void setup() {
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
     .setColorForeground(0xff8da75a)
     .setColorActive(0xffabcc6a)
     .setColorBackground(0xff5d6e3b);

  s2 = cp5.addSlider("j2depth")
     .setPosition(1180, 290)
     .setSize(30, 140)
     .setLabel("Profondeur")
     .setRange(1,30)
     .setNumberOfTickMarks(30)
     .setValue(j2depth)
     .setColorForeground(0xff8da75a)
     .setColorActive(0xffabcc6a)
     .setColorBackground(0xff5d6e3b);

  q1 = cp5.addSlider("j1Quiet")
     .setPosition(1250, 80)
     .setSize(30, 140)
     .setLabel("Max Quiet")
     .setRange(0,30)
     .setNumberOfTickMarks(31)
     .setValue(j1Quiet)
     .setColorForeground(0xff8da75a)
     .setColorActive(0xffabcc6a)
     .setColorBackground(0xff5d6e3b);

  q2 = cp5.addSlider("j2Quiet")
     .setPosition(1250, 290)
     .setSize(30, 140)
     .setLabel("Max Quiet")
     .setRange(0,30)
     .setNumberOfTickMarks(31)
     .setValue(j2Quiet)
     .setColorForeground(0xff8da75a)
     .setColorActive(0xffabcc6a)
     .setColorBackground(0xff5d6e3b);

  t1 = cp5.addSlider("j1Time")
      .setPosition(1320, 80)
      .setSize(30, 140)
      .setLabel("Temps")
      .setRange(0, 10000)
      .setValue(1000)
      .setColorForeground(0xffbdbd64)
      .setColorActive(0xffd6d46f)
      .setColorBackground(0xff827e40);

  t2 = cp5.addSlider("j2Time")
      .setPosition(1320, 290)
      .setSize(30, 140)
      .setLabel("Temps")
      .setRange(0, 10000)
      .setValue(1000)
      .setColorForeground(0xffbdbd64)
      .setColorActive(0xffd6d46f)
      .setColorBackground(0xff827e40);

  // Texte de départ
  println("---------------------");
  println(name + ", Antoine Mechulam");
  println("(https://github.com/toitouine/ChessAI)");
  println(" ");
  println("IAs disponibles :");
  println(" - LeMaire : Bon en ouverture et en finale, problème de sécurité du roi en milieu de jeu");
  println(" - Loic : Plutôt mauvais, préfère pater que mater");
  println(" - Stockfish : Extrêmement difficile de perdre contre lui");
  println(" - Antoine : Un jeu aléatoire de qualité");
  println(" - LesMoutons : Voleur, arnaqueur, tricheur, menaces en un !");
  println(" ");
  println("Profondeur (recherche classique et quiet) et temps (Iterative Deepening) ajustables avec les sliders du menu.");
  println("Voir fichier configs.pde pour les options, et notamment activer le jeu au temps ou non.");
  println("Appuyer sur H pour afficher l'aide (raccourcis claviers)");
  println(" ");
  println("/!\\ La direction rejette toute responsabilité en cas de CPU détruit par ce programme ou d'ordinateur brulé.");
  println("---------------------");

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
  editorIcons[5] = loadImage("icons/paste.png");
  editorIcons[6] = loadImage("icons/parameter.png");
  editorIcons[7] = loadImage("icons/rotate.png");
  editorIcons[8] = loadImage("icons/quit.png");

  pause = loadImage("icons/pause.png");
  chess = loadImage("icons/chess.png");
  bot = loadImage("icons/hacker.png");
  botLarge = loadImage("icons/hacker-large.png");
  warning = loadImage("icons/warning.png");
  mouton = loadImage("joueurs/lesmoutonsImgEnd.jpg");

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
  hubButtons.add(new TextButton(width/2 - 190, 480, 380, 75, "Nouvelle partie", 30, 10));
  hubButtons.add(new TextButton(width-110, height-40, 100, 30, "Coller FEN", 18, 8)); hubButtons.get(1).setColors(0xff1d1c1a, 0xffffffff);
  hubButtons.add(new TextButton(width-220, height-40, 100, 30, "Copier FEN", 18, 8)); hubButtons.get(2).setColors(0xff1d1c1a, 0xffffffff);

  promoButtons.add(new Button(0.25f*w + offsetX, 3.25f*w + offsetY, 1.5f*w, imageArrayB[1], imageArrayN[1]));
  promoButtons.add(new Button(2.25f*w + offsetX, 3.25f*w + offsetY, 1.5f*w, imageArrayB[2], imageArrayN[2]));
  promoButtons.add(new Button(4.25f*w + offsetX, 3.25f*w + offsetY, 1.5f*w, imageArrayB[3], imageArrayN[3]));
  promoButtons.add(new Button(6.25f*w + offsetX, 3.25f*w + offsetY, 1.5f*w, imageArrayB[4], imageArrayN[4]));

  toggles1.add(new ToggleButton(40, 80, 150, stockfish, "Stockfish"));
  toggles1.add(new ToggleButton(230, 80, 150, antoine, "Antoine"));
  toggles1.add(new ToggleButton(420, 80, 150, loic, "Loic"));
  toggles1.add(new ToggleButton(610, 80, 150, lesmoutons, "LesMoutons"));
  toggles1.add(new ToggleButton(800, 80, 150, lemaire, "LeMaire"));
  toggles1.add(new ToggleButton(990, 80, 150, human, "Humain"));

  toggles2.add(new ToggleButton(40, 290, 150, stockfish, "Stockfish"));
  toggles2.add(new ToggleButton(230, 290, 150, antoine, "Antoine"));
  toggles2.add(new ToggleButton(420, 290, 150, loic, "Loic"));
  toggles2.add(new ToggleButton(610, 290, 150, lesmoutons, "LesMoutons"));
  toggles2.add(new ToggleButton(800, 290, 150, lemaire, "LeMaire"));
  toggles2.add(new ToggleButton(990, 290, 150, human, "Humain"));

  addPiecesColorSwitch = new CircleToggleButton(offsetX/2, (offsetY+w/2 + w*6) + 70, w/1.3f);
  positionEditor = new Button(width-55, 10, 50, chess, chess);
  hackerButton = new Button(width-100, 11, 40, bot, bot);
  rematchButton = new TextButton(offsetX - offsetX/1.08f, offsetY+4*w-29, offsetX-2*(offsetX - offsetX/1.08f), 24, "Revanche", 15, 3);
  rematchButton.setColors(0xff1d1c1a, 0xffffffff);
  newGameButton = new TextButton(offsetX - offsetX/1.08f, offsetY+4*w+5, offsetX-2*(offsetX - offsetX/1.08f), 24, "Menu", 15, 3);
  newGameButton.setColors(0xff1d1c1a, 0xffffffff);

  timeButtons[0] = new ArrayList<TimeButton>();
  timeButtons[1] = new ArrayList<TimeButton>();
  timeButtons[0].add(new TimeButton(37, 472, 48, 11, 5, 0, 0, 0, 0xfff0f0f0, 0xff26211b, 0xffd1cfcf, true));
  timeButtons[0].add(new TimeButton(86, 472, 49, 11, 0, 5, 0, 0, 0xfff0f0f0, 0xff26211b, 0xffd1cfcf, true));
  timeButtons[0].add(new TimeButton(142, 472, 49, 11, 5, 5, 0, 0, 0xfff0f0f0, 0xff26211b, 0xffd1cfcf, true));
  timeButtons[0].add(new TimeButton(37, 533, 48, 10, 0, 0, 0, 5, 0xfff0f0f0, 0xff26211b, 0xffd1cfcf, false));
  timeButtons[0].add(new TimeButton(86, 533, 49, 10, 0, 0, 5, 0, 0xfff0f0f0, 0xff26211b, 0xffd1cfcf, false));
  timeButtons[0].add(new TimeButton(142, 533, 49, 10, 0, 0, 5, 5, 0xfff0f0f0, 0xff26211b, 0xffd1cfcf, false));
  timeButtons[1].add(new TimeButton(227, 472, 48, 10, 5, 0, 0, 0, 0xff26211b, 0xfff0f0f0, 0xff2d2d2a, true));
  timeButtons[1].add(new TimeButton(276, 472, 49, 10, 0, 5, 0, 0, 0xff26211b, 0xfff0f0f0, 0xff2d2d2a, true));
  timeButtons[1].add(new TimeButton(332, 472, 49, 10, 5, 5, 0, 0, 0xff26211b, 0xfff0f0f0, 0xff2d2d2a, true));
  timeButtons[1].add(new TimeButton(227, 533, 48, 10, 0, 0, 0, 5, 0xff26211b, 0xfff0f0f0, 0xff2d2d2a, false));
  timeButtons[1].add(new TimeButton(276, 533, 49, 10, 0, 0, 5, 0, 0xff26211b, 0xfff0f0f0, 0xff2d2d2a, false));
  timeButtons[1].add(new TimeButton(332, 533, 49, 10, 0, 0, 5, 5, 0xff26211b, 0xfff0f0f0, 0xff2d2d2a, false));

  presetButtons.add(new ImageButton(width-272, 465, 70, 70, 5, 0xff272522, loadImage("icons/rapid.png")));
  presetButtons.add(new ImageButton(width-177, 465, 70, 70, 5, 0xff272522, loadImage("icons/blitz.png")));
  presetButtons.add(new ImageButton(width-82, 465, 70, 70, 5, 0xff272522, loadImage("icons/bullet.png")));

  for (int i = 0; i < timeButtons.length; i++) {
    for (int j = 0; j < timeButtons[i].size(); j++) {
      timeButtons[i].get(j).setIndex(i, j % 3);
    }
  }

  // Drag and drops
  addPiecesButtons[0] = new ArrayList<DragAndDrop>();
  for (int i = 0; i < 6; i++) {
    addPiecesButtons[0].add(new DragAndDrop(offsetX/2, (offsetY+w/2 + w*i) + i*12.5f, w, w, imageArrayB[i], i));
  }
  addPiecesButtons[1] = new ArrayList<DragAndDrop>();
  for (int i = 0; i < 6; i++) {
    addPiecesButtons[1].add(new DragAndDrop(offsetX/2, (offsetY+w/2 + w*i) + i*12.5f, w, w, imageArrayN[i], i + 6));
  }

  // Icones de la partie
  int[] numSc1 = {0, 1, 2, 3, 4, 5, 6, 7, 16, 10};
  for (int i = 0; i < icons.length; i++) {
    iconButtons.add(new Button(edgeSpacing + i*iconSize + i*spacingBetweenIcons, distanceFromTop, iconSize, icons[i], pause));
    iconButtons.get(i).setNumShortcut(numSc1[i]);
  }

  // Icones de l'éditeur
  int[] numSc2 = {0, 11, 13, 12, 15, 18, 17, 6, 14};
  for (int i = 0; i < editorIcons.length; i++) {
    editorIconButtons.add(new Button(editorEdgeSpacing + i*editorIconSize + i*spacingBetweenEditorIcons, distanceFromTop, editorIconSize, editorIcons[i], editorIcons[i]));
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

  // Initialise les pièces
  pieces[0] = new ArrayList<Piece>();
  pieces[1] = new ArrayList<Piece>();

  // Initialise PreComputedData
  pc.init();

  // Place les pièces
  setPieces();
}

public void draw() {

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

    // Actualise "block playing", qui empêche éventuellement un joueur de jouer
    updateBlockPlaying();

    // Gestion du joueur
    if (playEngineMoveNextFrame) { joueurs.get(tourDeQui).play(); playEngineMoveNextFrame = false; engineToPlay = false; }
    if (engineToPlay) { playEngineMoveNextFrame = true; }

    if (!gameEnded && play && (!useHacker || hackerPret)) {
      if (joueurs.get(0).name != "Humain" && joueurs.get(1).name != "Humain") {
        if (speed == 0) joueurs.get(tourDeQui).play();
        else if (frameCount % speed == 0) joueurs.get(tourDeQui).play();
      }
    }

    // Hacker
    if (useHacker && hackerPret  && !hackerAPImode) {
      if (play && !gameEnded && enPromotion == null && millis() - lastHackerScan >= hackerScanCooldown) {
        deltaTimeMesured = millis() - lastMoveTime;
        scanMoveOnBoard();
      }

      if (gameEnded && !hackerWaitingToRestart && millis() - timeAtHackerEnd >= timeBeforeHackerRestart) hackStartGame();
      if (hackerWaitingToRestart && millis() - timeAtLastRestartTry >= hackerTestRestartCooldown) {
        handleWaitForRestart();
      }
    }

    if (MODE_SANS_AFFICHAGE && useHacker && hackerPret) return;

    // Affichages
    surface.setTitle(name + " - " + j1 + " (" + ((joueurs.get(0).useIterativeDeepening) ? "ID" : j1depth) +  ") contre " + j2 + " (" + ((joueurs.get(1).useIterativeDeepening) ? "ID" : j2depth) + ")" + ((infos == "") ? "" : " - ") + infos);

    // Plateau
    updateBoard();
    drawPlayersInfos();
    for (Arrow b : bookArrows) b.show();
    if (showVariante) {
      for (Arrow arrow : varianteArrows) arrow.show();
    }

    // Promotion
    if (enPromotion != null) {
      fill(220, 220, 220, 200);
      rectMode(CORNER);
      rect(offsetX, offsetY, cols*w, rows*w);
      showPromoButtons();
    }

    // Icones
    for (int i = 0; i < iconButtons.size(); i++) {
      Button b = iconButtons.get(i);
      if (i == 7) b.show(play ? 0 : 1); // Play / Pause
      else b.show(0);
    }

    // Écran de fin de partie
    if (!disableEndScreen && gameEnded && millis() - timeAtEnd > timeBeforeEndDisplay) {
      float dy = targetEndScreenY - yEndScreen;
      yEndScreen += dy * endScreenEasing;

      float rectX = 1.75f*w + offsetX, rectW = 4.5f*w, rectH = 3*w;
      if (targetEndScreenY - yEndScreen <= 1 && mousePressed && (mouseX < rectX || mouseX >= rectX+rectW || mouseY < yEndScreen || mouseY >= yEndScreen+rectH)) disableEndScreen = true;
      drawEndScreen(yEndScreen);
    }
    if (gameEnded && !useHacker && !hackerPret) {
      newGameButton.show();
      rematchButton.show();
    }

    // Page d'accueil du hacker
    if (useHacker && !hackerPret) drawHackerPage();

    // Messages
    if (alert != "") displayAlert();
    if (infoBox != "") drawInfoBox(infoBox);
    if (messageMouton != "") displayMoutonAlert();
  }

  ////////////////////////////////////////////////////////////////

  // Menu principal

  else if (gameState == 0) {
    background(49, 46, 43);

    for (TextButton b : hubButtons) b.show();

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

    if (timeControl) {
      fill(0xfff0f0f0);
      stroke(0xfff0f0f0);
      rect(37, 480, 98, 55);
      rect(142, 480, 49, 55);
      fill(0xff26211b);
      textSize(30);
      textAlign(CENTER, CENTER);
      text(nf(times[0][0], 2) + ":" + nf(times[0][1], 2), 87, 504);
      text(nf(times[0][2], 2), 167, 504);

      fill(0xff26211b);
      stroke(0xff26211b);
      rect(227, 480, 98, 55);
      rect(332, 480, 49, 55);
      fill(0xfff0f0f0);
      text(nf(times[1][0], 2) + ":" + nf(times[1][1], 2), 277, 504);
      text(nf(times[1][2], 2), 357, 504);
      for (int i = 0; i < timeButtons.length; i++) {
        for (int j = 0; j < timeButtons[i].size(); j++) {
          timeButtons[i].get(j).update();
          timeButtons[i].get(j).show();
        }
      }
    }

    for (int i = 0; i < presetButtons.size(); i++) {
      presetButtons.get(i).show();
    }

    for (ToggleButton t : toggles1) {
      t.show();
    }
    for (ToggleButton t : toggles2) {
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
    if (showParameters) drawParameters();

    if (infos != "") surface.setTitle(name + " - Editeur de position - " + infos);
    else surface.setTitle(name + " - Editeur de position");

    for (DragAndDrop d : addPiecesButtons[addPiecesColor]) d.show();
    addPiecesColorSwitch.show();
  }
}
/////////////////////////////////////////////////////////////////

// Search controller (contrôle les paramètres des recherches et temps pour iterative deepening)

public class SearchApplet extends PApplet {
  int sizeW = 503, sizeH = 198;
  boolean show = true;
  boolean showDepthTracker = false;

  // -1 : aucun, 0 : blanc, 1 : noir
  int inSearch = -1; // avec iterative deepening
  int inNormalSearch = -1; // sans iterative deepening
  int searchStartTime;
  int[] savedTimes = {0, 0};
  int[] times = {0, 0};

  String[] evals = {"0", "0"};
  String[] bestMoves = {"", ""};
  String[] depths = {"0", "0"};
  float[]  depthTrackers = {0, 0};
  String[] positions = {"0", "0"};
  String[] tris = {"0", "0"};
  String[] transpositions = {"0", "0"};
  String[] timesDisplay = {"0", "0"};

  public void settings() {
    size(sizeW, sizeH);
  }

  public void setup() {
    background(0xff272522);
    surface.setSize(sizeW, sizeH);
    surface.setLocation(displayWidth-sizeW, gameHeight+46);
    surface.setTitle("Search controller");
    surface.setFrameRate(30);
    surface.setAlwaysOnTop(attach);
  }

  public void draw() {

    if (this.inSearch != -1) {
      if (millis() - this.searchStartTime >= this.times[this.inSearch]) {
        this.inSearch = -1;
        stopSearch = true;
      }

      if (inSearch >= 0 && joueurs.get(inSearch).player != null) {
        positions[inSearch] = formatInt(joueurs.get(inSearch).player.numPos);
        transpositions[inSearch] = formatInt(joueurs.get(inSearch).player.numTranspositions);
      }
    }

    if (this.inNormalSearch != -1 && joueurs.get(inNormalSearch).player != null) {
      positions[inNormalSearch] = formatInt(joueurs.get(inNormalSearch).player.numPos);
      transpositions[inNormalSearch] = formatInt(joueurs.get(inNormalSearch).player.numTranspositions);
    }

    if (!show) return;
    surface.setAlwaysOnTop(attach);

    background(0xff272522);

    // Ligne de séparation
    stroke(0xff524d48);
    line(width/2, 20, width/2, height-20);

    // Nom des joueurs
    fill(255);
    textSize(23);
    textAlign(CENTER, CENTER);
    if (joueurs == null || joueurs.size() < 2) return;
    if (!joueurs.get(0).name.equals("Humain")) text(joueurs.get(0).name + " (blancs)", width/4, 27);
    else text(joueurs.get(0).name + " (blancs)", width/4, height/2);

    if (joueurs == null || joueurs.size() < 2) return;
    if (!joueurs.get(1).name.equals("Humain")) text(joueurs.get(1).name + " (noirs)", (3*width)/4, 27);
    else text(joueurs.get(1).name + " (noirs)", (3*width)/4, height/2);

    // Stats
    textSize(17);
    textAlign(LEFT, CENTER);

    for (int i = 0; i < 2; i++) {
      if (joueurs != null && joueurs.size() < 2) break;
      if (!gameEnded && !joueurs.get(i).name.equals("Humain")) {
        fill(0xfffbd156);
        if (bestMoves[i] != "") text("Evaluation : " + evals[i] + " (" + bestMoves[i] + ")", i*width/2 + 8, 65);
        else text("Evaluation : " + evals[i], i*width/2 + 8, 65);
        fill(0xffef5a2a);
        if (showDepthTracker && depthTrackers[i] != 0) text("Profondeur : " + depths[i] + " (" + roundNumber(depthTrackers[i], 0) + "%)", i*width/2 + 8, 89);
        else text("Profondeur : " + depths[i], i*width/2 + 8, 89);
        fill(0xff5c8cb1); text("Positions : " + positions[i] + " (" + tris[i] + ")", i*width/2 + 8, 113);
        fill(0xff93b46b); text("Transpositions : " + transpositions[i], i*width/2 + 8, 137);
        fill(0xffabb88a); text("Temps : " + timesDisplay[i], i*width/2 + 8, 161);
      }
    }
  }

  public void startSearch(int c) {
    this.inSearch = c;
    this.searchStartTime = millis();
  }

  public void endSearch() {
    this.inSearch = -1;
    stopSearch = true;
  }

  public void setTimes(int timeForSearch1, int timeForSearch2) {
    this.times[0] = timeForSearch1;
    this.times[1] = timeForSearch2;
    this.savedTimes[0] = timeForSearch1;
    this.savedTimes[1] = timeForSearch2;
  }

  public void setTime(int c, int time) {
    this.times[c] = time;
  }

  public int getTime(int c) {
    return this.times[c];
  }

  public void setEvals(String eval, int c) {
    evals[c] = eval;
  }

  public void setDepths(String depth, int c) {
    depths[c] = depth;
  }

  public void setPositions(String pos, int c) {
    positions[c] = pos;
  }

  public void setTris(String tri, int c) {
    tris[c] = tri;
  }

  public void setTranspositions(String transpo, int c) {
    transpositions[c] = transpo;
  }

  public void setTimeDisplays(String timeDisplay, int c) {
    timesDisplay[c] = timeDisplay;
  }

  public void setBestMoves(String move, int c) {
    bestMoves[c] = move;
  }

  public void resetDepthTracker(int c) {
    showDepthTracker = false;
    depthTrackers[c] = 0;
  }

  public void incrementSearchTracker(int c) {
    showDepthTracker = true;
    depthTrackers[c] = depthTrackers[c] + (100 / joueurs.get(c).player.firstPlyMoves);
  }

  public void reset() {
    for (int i = 0; i < 2; i++) {
      evals[i] = "0";
      depths[i] = "0";
      positions[i] = "0";
      tris[i] = "0";
      transpositions[i] = "0";
      bestMoves[i] = "";
      depthTrackers[i] = 0;
      timesDisplay[i] = "0";
    }
  }

  public void show() {
    surface.setVisible(true);
    show = true;
  }

  public void hide() {
    surface.setVisible(false);
    show = false;
  }

  public PSurface initSurface() {
    PSurface pSurface = super.initSurface();
    PSurfaceAWT awtSurface = (PSurfaceAWT) surface;
    SmoothCanvas smoothCanvas = (SmoothCanvas) awtSurface.getNative();
    Frame frame = smoothCanvas.getFrame();
    frame.setUndecorated(true);
    return pSurface;
  }
}

/////////////////////////////////////////////////////////////////

// Graph applet (graphique de l'évaluation)

public class GraphApplet extends PApplet {
  int graphW = 500, graphH = 480;
  int sizeW = graphW + 50, sizeH = graphH + 60;
  boolean show = false;
  Graph g;

  public void settings() {
    size(sizeW, sizeH);
  }

  public void setup() {
    background(0xff272522);
    surface.setLocation(0, 23);
    surface.setTitle("Analyse");
    surface.setFrameRate(5);
  }

  public void draw() {
    background(0xff272522);
    if (show) {
      g.plot();
    }
  }

  public void mouseDragged() {
    surface.setFrameRate(60);
    Point mouse;
    mouse = MouseInfo.getPointerInfo().getLocation();
    surface.setLocation(mouse.x - sizeW/2, mouse.y - sizeH/2);
  }

  public void mouseReleased() {
    surface.setFrameRate(5);
  }

  public PSurface initSurface() {
    PSurface pSurface = super.initSurface();
    PSurfaceAWT awtSurface = (PSurfaceAWT) surface;
    SmoothCanvas smoothCanvas = (SmoothCanvas) awtSurface.getNative();
    Frame frame = smoothCanvas.getFrame();
    frame.setUndecorated(true);
    return pSurface;
  }

  public void show() {
    surface.setVisible(true);
  }

  public void hide() {
    surface.setVisible(false);
  }

  public void disableGraph() {
    g = null;
    show = false;
    this.hide();
  }

  public void initGraph() {
    g = new Graph(10, 10);
    show = true;
    this.show();
  }

  public void clearGraph() {
    if (g != null) g.reset();
  }

  public void sendData(float[] xs, float[] ys, int c, String legende) {
    g.addValues(xs, ys, c, legende);
  }

  public class Graph {
    int x = 0, y = 0;
    int w = 500, h = 480;
    int xPas = 0, yPas = 10;
    int mateOffset = 200;
    ArrayList<float[]> allOrdonnes = new ArrayList<float[]>();
    ArrayList<float[]> allAbscisses = new ArrayList<float[]>();
    ArrayList<Integer> colors = new ArrayList<Integer>();
    ArrayList<String> legendes = new ArrayList<String>();

    Graph(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public void reset() {
      this.allOrdonnes.clear();
      this.allAbscisses.clear();
      this.colors.clear();
      this.legendes.clear();
    }

    public void addValues(float[] xs, float[] ys, int c, String legende) {
      float[] abscisses = xs;
      float[] ordonnees = ys;
      this.allAbscisses.add(abscisses);
      this.allOrdonnes.add(ordonnees);
      this.colors.add(c);
      this.legendes.add(legende);

      int indexReference = 0;
      for (int i = 0; i < this.allAbscisses.size(); i++) {
        if (this.allAbscisses.get(i).length > this.allAbscisses.get(indexReference).length) indexReference = i;
      }
      if (this.allAbscisses.get(indexReference).length > 1) this.xPas = this.w / (this.allAbscisses.get(indexReference).length-1);
      else xPas = this.w / 2;
    }

    public int isMateValue(float value) {
      // Retourne -1 si aucun des deux, 0 si pour les blancs et 1 si pour les noirs
      if (abs(500 - value) <= 1) return 0;
      if (abs(-500 - value) <= 1) return 1;
      return -1;
    }

    public float getOrdonne(float val) {
      int mate = this.isMateValue(val);
      if (mate != -1) return (mate == 0) ? 24 : -24;
      else if (val >= 20) return 20;
      else if (val <= -20) return -20;
      return val;
    }

    public void legende() {
      float ecartEnPlus = 30;
      float ecart = (this.w) / (this.legendes.size()+1) + ecartEnPlus;
      float lineSize = 35;
      float distanceLineText = 10;
      float yDistance = this.y + this.h + 25;

      for (int i = 0; i < this.legendes.size(); i++) {
        float totalDistance = lineSize + distanceLineText + textWidth(this.legendes.get(i));
        float centerOfPoint = this.x + ecart * (i+1) - ecartEnPlus;
        if (this.colors.size() > 0 && this.colors.size() > i) stroke(this.colors.get(i));
        strokeWeight(3);
        line(centerOfPoint - totalDistance/2, yDistance, centerOfPoint - totalDistance/2 + lineSize, yDistance);
        textAlign(LEFT, CENTER);
        textSize(14);
        if (this.legendes.size() > 0 && this.legendes.size() > i) text(this.legendes.get(i), centerOfPoint - totalDistance/2 + lineSize + distanceLineText, yDistance-2);
      }
    }

    public void plot() {
      push();

      translate(this.x, this.y+this.h/2);
      noStroke();
      fill(0xff32302d);
      rect(0, -this.h/2, this.w, this.h);
      stroke(0xff383434);
      strokeWeight(3);
      line(0, 0, this.w-3, 0);

      strokeWeight(1);
      textSize(13);
      for (int i = this.mateOffset; i >= -this.mateOffset; i -= 2*yPas) {
        textAlign(CENTER, CENTER);
        fill(0xff706f6d);
        text(-i/yPas, this.w + 20, i-2);

        strokeWeight(1);
        stroke(0xff383434);
        line(0, i, this.w-1, i);
      }
      text("MAT", this.w + 20, this.mateOffset + 3.5f*yPas);
      text("MAT", this.w + 20, -this.mateOffset - 3.5f*yPas);

      for (int i = 0; i < this.w; i += 2*xPas) line(i, -this.h/2, i, this.h/2-1);

      for (int n = 0; n < this.allAbscisses.size(); n++) {
        float[] currentAbs = this.allAbscisses.get(n);
        float[] currentOrd = this.allOrdonnes.get(n);

        for (int i = 0; i < currentAbs.length; i++) {
          if (i != 0) {
            float prevOrd = this.getOrdonne(currentOrd[i-1]);
            float ord = this.getOrdonne(currentOrd[i]);
            strokeWeight(3);
            if (this.colors.size() > 0 && this.colors.size() > n) stroke(this.colors.get(n)); // Condition pour éviter une erreur si on est en train de clear colors dans l'applet principal
            line(currentAbs[i-1] * xPas, -prevOrd * yPas, currentAbs[i] * xPas, -ord * yPas);
          }
        }
      }

      pop();

      this.legende();
    }
  }
}

public void updateGraph() {
  ga.clearGraph();
  sendValuesToGraph();
}

public void activateGraph() {
  ga.initGraph();
  sendValuesToGraph();
  delay(3);
  surface.setVisible(true);
}

public void sendValuesToGraph() {
  int[] colors = {0xff5c8cb1, 0xffb33430};

  for (int n = 0; n < joueurs.size(); n++) {
    if (joueurs.get(n).evals.size() > 0) {
      float[] x = new float[joueurs.get(n).evals.size()];
      float[] y = new float[joueurs.get(n).evals.size()];
      for (int i = 0; i < y.length; i++) x[i] = i;
      for (int i = 0; i < y.length; i++) y[i] = joueurs.get(n).evals.get(i);
      String name = joueurs.get(n).name + " (" + (joueurs.get(n).c == 0 ? "Blancs" : "Noirs") + ")";
      ga.sendData(x, y, colors[n], name);
    }
  }
}

public void disableGraph() {
  ga.disableGraph();
}

/////////////////////////////////////////////////////////////////

// Timer applet (gestion du temps des pendules)

public class TimerApplet extends PApplet {
  Timer[] timers = new Timer[2];
  boolean show = false;
  int windowWidth = 150;
  int windowHeight = 200;

  int upY = 50;
  int downY = windowHeight-upY;
  int timersX = windowWidth/2, timersTextSize = 30;

  int rate = 15;

  public void settings() {
    size(windowWidth, windowHeight);
  }

  public void setup() {
    background(49, 46, 43);
    surface.setLocation(displayWidth - (gameWidth + width), 45 + offsetY + 4*w - windowHeight/2);
    surface.setTitle("Pendules");
    surface.setAlwaysOnTop(attach);
    surface.setFrameRate(rate);

    initTimers();
  }

  public void draw() {

    if (!show) return;

    background(49, 46, 43);
    surface.setAlwaysOnTop(attach);

    for (int i = 0; i < timers.length; i++) {
      timers[i].update();
      if (pointDeVue) timers[i].show(timersX, (i == 0) ? downY : upY, timersTextSize);
      else timers[i].show(timersX, (i == 0) ? upY : downY, timersTextSize);
    }

    if (!gameEnded && play) checkTimes();
  }

  public void show() {
    show = true;
    surface.setVisible(true);
  }

  public void hide() {
    show = false;
    surface.setVisible(false);
  }

  public void goToHackerPosition() {
    surface.setLocation(displayWidth - gameWidth, displayHeight-height-51);
  }

  public void goToDefaultPosition() {
    surface.setLocation(displayWidth - (gameWidth + width), 45 + offsetY + 4*w - windowHeight/2);
  }

  public void mouseDragged() {
    // surface.setFrameRate(60);
    Point mouse;
    mouse = MouseInfo.getPointerInfo().getLocation();
    surface.setLocation(mouse.x - windowWidth/2, mouse.y - windowHeight/2);
  }

  public void mouseReleased() {
    // surface.setFrameRate(30);
  }

  public void switchTimers(int toward) {
    timers[(int)pow(toward-1, 2)].pause();
    timers[toward].resume();
  }

  public void checkTimes() {
    if (timers[0].currentTime <= 0) loseOnTime(0);
    if (timers[1].currentTime <= 1) loseOnTime(1);
  }

  public void startTimers(int startOne) {
    timers[startOne].resume();
  }

  public void pauseTimers() {
    timers[0].pause();
    timers[1].pause();
  }

  public void stopTimers() {
    timers[0].stop();
    timers[1].stop();
  }

  public void resetTimers() {
    timers[0] = null;
    timers[1] = null;
    initTimers();
  }

  public void initTimers() {
    for (int i = 0; i < timers.length; i++) {
      timers[i] = new Timer(times[i][0], times[i][1], times[i][2]);
    }
    timers[0].setColors(0xffffffff, 0xff26211b, 0xff989795, 0xff615e5b);
    timers[1].setColors(0xff26211b, 0xffffffff, 0xff2b2722, 0xff82807e);
  }

  public class Timer {
    int currentTime = 0; //temps à afficher
    int totalTime; //temps entré par l'utilisateur (ms)
    int backColorActive = 0xffffffff, textColorActive = 0xffffffff, backColor = 0xffffffff, textColor = 0xffffffff;
    int increment = 0;
    int timeOfSecond = 1000;
    boolean pause = true;

    Timer(int min, int sec, int increment) {
      this.totalTime = (min*60 + sec)*1000;
      this.currentTime = totalTime;
      this.increment = increment*1000;
    }

    public void setColors(int bca, int tca, int bc, int tc) {
      this.backColorActive = bca;
      this.textColorActive = tca;
      this.backColor = bc;
      this.textColor = tc;
    }

    public void setDurationOfSecond(int timeInMillis) {
      this.timeOfSecond = timeInMillis;
    }

    public void addTime(int timeInMillis) {
      this.currentTime += timeInMillis;
    }

    public void removeTime(int timeInMillis) {
      this.currentTime -= timeInMillis;
    }

    public void update() {
      if (!this.pause) {
        this.currentTime -= timeOfSecond/rate;
      }
    }

    public void pause() {
      this.pause = true;
      this.currentTime += this.increment;
    }

    public void stop() {
      this.pause = true;
    }

    public void resume() {
      this.pause = false;
    }

    public void show(int x, int y, int size) {
      int s = (this.currentTime/1000) % 60;
      int m = ((int)this.currentTime/1000) / 60;
      String sec;
      String min;
      if (s < 10) { sec = "0" + s; } else { sec = str(s); }
      if (m < 10) { min = "0" + m; } else { min = str(m); }

      String text = min + ":" + sec;

      if (this.pause) fill(this.backColor);
      else fill(this.backColorActive);
      noStroke();
      rectMode(CENTER);
      rect(x, y, textWidth(text)*1.5f, 60, 3);

      if (this.pause) fill(this.textColor);
      else fill(this.textColorActive);
      textSize(size);
      textAlign(CENTER, CENTER);
      text(text, x, y-5);
    }
  }

  public PSurface initSurface() {
    PSurface pSurface = super.initSurface();
    PSurfaceAWT awtSurface = (PSurfaceAWT) surface;
    SmoothCanvas smoothCanvas = (SmoothCanvas) awtSurface.getNative();
    Frame frame = smoothCanvas.getFrame();
    frame.setUndecorated(true);
    return pSurface;
  }
}

/////////////////////////////////////////////////////////////////
// Format du livre d'ouverture :
// fen:XXXXYYY_XXXXYYY_XXXXYYY
// XXXX représente le coup dans le format (i1 j1 i2 j2)
// YYY représente le nombre de fois que ce coup apparait
// _ représente la séparation
// Exception pour le roque, de la forme 88XX et 99XX

class Arrow {
  float x, y, tx, ty;
  int i, j, ti, tj;
  int arrowSpace = 15, arrowLength = 15;
  float angle = 0;
  boolean verticalDir; //true = haut, false = bas
  boolean horizontalDir; //true = gauche, false = droit
  float progressionDegrade = 0;

  Color colorDepart = new Color(255, 192, 67);
  Color colorArrivee = new Color(255, 0, 0);
  Color currentColor = new Color(255, 192, 67);;

  Arrow(int i, int j, int ti, int tj) {
    this.i = i; this.j = j; this.ti = ti; this.tj = tj;
    this.x = grid[i][j].x + w/2;
    this.y = grid[i][j].y + w/2;
    this.tx = grid[ti][tj].x + w/2;
    this.ty = grid[ti][tj].y + w/2;
    this.verticalDir = (this.tj < this.j);
    this.horizontalDir = (this.i > this.ti);

    float deltaI = abs(this.ti - this.i);
    float deltaJ = abs(this.tj - this.j);
    if (deltaJ == 0) this.angle = horizontalDir ? -PI/2.1f : PI/2.1f;
    else if (this.verticalDir && this.horizontalDir) this.angle = -atan(deltaI/deltaJ);
    else if (this.verticalDir && !this.horizontalDir) this.angle = atan(deltaI/deltaJ);
    else if (!this.verticalDir && !this.horizontalDir) this.angle = PI - atan(deltaI/deltaJ);
    else if (!this.verticalDir && this.horizontalDir) this.angle = PI + atan(deltaI/deltaJ);
  }

  public void setDegradeProgression(float newP) {
    this.progressionDegrade = newP;
    int red = (int)(this.colorDepart.getRed() * (1 - pow(this.progressionDegrade, 0.4f)) + this.colorArrivee.getRed() * pow(this.progressionDegrade, 0.4f));
    int green = (int)(this.colorDepart.getGreen() * (1 - pow(this.progressionDegrade, 0.4f)) + this.colorArrivee.getGreen() * pow(this.progressionDegrade, 0.4f));
    int blue = (int)(this.colorDepart.getBlue() * (1 - pow(this.progressionDegrade, 0.4f)) + this.colorArrivee.getBlue() * pow(this.progressionDegrade, 0.4f));
    this.currentColor = new Color(red, green, blue);
  }

  public void show() {
    strokeWeight(5);
    stroke(this.currentColor.getRed(), this.currentColor.getGreen(), this.currentColor.getBlue());
    float xDraw, yDraw, txDraw, tyDraw, angleDraw;
    if (pointDeVue) {
      xDraw = this.x; yDraw = this.y; txDraw = this.tx; tyDraw = this.ty;
      angleDraw = this.angle;
    } else {
      xDraw = grid[this.i][this.j].x + w/2; yDraw = grid[this.i][this.j].y + w/2;
      txDraw = grid[this.ti][this.tj].x + w/2; tyDraw = grid[this.ti][this.tj].y + w/2;
      angleDraw = PI + this.angle;
    }
    line(xDraw, yDraw, txDraw, tyDraw);

    push();
    translate(txDraw, tyDraw);
    rotate(angleDraw);
    line(0, 0, -this.arrowSpace, this.arrowLength);
    line(0, 0, this.arrowSpace, this.arrowLength);
    pop();
  }
}

public void printBook() {
  for (int i = 0; i < book.size(); i++) {
    println("[" + i + "] " + book.get(i));
  }
}

public void printMaxEffectif() {
  int effectifMax = 0;
  String[] moves = getMoveStringFromFEN(generateFEN());

  for (int i = 1; i < moves.length; i += 2) {
    effectifMax = max(effectifMax, Integer.valueOf(moves[i]));
  }

  println(effectifMax);
}

public void clearBookHighlight() {
  bookArrows.clear();
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].bookFrom = false;
      grid[i][j].bookTarget = false;
      grid[i][j].moveMark = false;
    }
  }
}

public void highlightBook() {
  bookArrows.clear();

  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].bookFrom = false;
      grid[i][j].bookTarget = false;
      grid[i][j].moveMark = false;
    }
  }

  String[] moves = getMoveStringFromFEN(generateFEN());
  for (int i = 0; i < moves.length; i+=2) {
    int effectif = Integer.valueOf(moves[i+1]);
    int fromI = Integer.valueOf(String.valueOf(moves[i].charAt(0)));
    int fromJ = Integer.valueOf(String.valueOf(moves[i].charAt(1)));
    int targetI = Integer.valueOf(String.valueOf(moves[i].charAt(2)));
    int targetJ = Integer.valueOf(String.valueOf(moves[i].charAt(3)));

    if (fromI != 8 && fromI != 9) {
      bookArrows.add(new Arrow(fromI, fromJ, targetI, targetJ));
    } else {
      // Roques
      if (fromI == 8 && targetI == 8) { bookArrows.add(new Arrow(4, 7, 6, 7)); }
      else if (fromI == 8 && targetI == 9) { bookArrows.add(new Arrow(4, 0, 6, 0)); }
      else if (fromI == 9 && targetI == 8) { bookArrows.add(new Arrow(4, 7, 2, 7)); }
      else if (fromI == 9 && targetI == 9) { bookArrows.add(new Arrow(4, 0, 2, 0)); }
    }

    // Couleur
    bookArrows.get(bookArrows.size()-1).setDegradeProgression(map(effectif, 1, 3497, 0, 1));
  }
}

// Renvoie la fen contenue à la ligne n du livre
public String extractFenFromBook(int n) {
  String b = book.get(n);
  String resultFen = "";

  for (int i = 0; i < b.length(); i++) {
    char c = b.charAt(i);
    if (c == ':') break;
    resultFen = resultFen + c;
  }

  return resultFen;
}

// Renvoie l'index de la fen si elle est trouvée dans le livre et renvoie -1 si elle n'est pas trouvée
public int searchFenInBook(String fen) {
  for (int i = 0; i < book.size(); i++) {
    if (extractFenFromBook(i).equals(fen)) return i;
  }
  return -1;
}

// Renvoie une liste des coups correspondant à la fen dans le même format que celui du livre (i1 j1 i2 j2)
public ArrayList<String> getMovesFromFen(String fen) {
  String movesString = "";

  int index = searchFenInBook(fen);
  if (index == -1) return new ArrayList<String>();

  String b = book.get(index);

  // Index auquel les coups commencent à être indiqués
  int startMoves = fen.length()+1;

  // String de tous les coups
  movesString = b.substring(startMoves, b.length());

  ArrayList<String> moves = new ArrayList<String>();

  // Ajoute les coups à l'array list autant de fois que nécessaire (pour que l'effectif corresponde)
  String[] preMovesArray = split(movesString, "_");
  for (int i = 0; i < preMovesArray.length; i+=2) {
    int numOfMoves = Integer.valueOf(preMovesArray[i+1]);
    for (int n = 0; n < numOfMoves; n++) {
      moves.add(preMovesArray[i]);
    }
  }

  return moves;
}

// Renvoie une liste des coups correspondant à l'index de la fen dans le livre dans le même format que celui du livre (i1 j1 i2 j2)
public ArrayList<String> getMovesFromIndex(int index) {
  String movesString = "";
  String b = book.get(index);
  int startMoves = 0;

  for (int i = b.length() - 1; i >= 0; i--) {
    if (b.charAt(i) == ':') startMoves = i+1;
  }

  movesString = b.substring(startMoves, b.length());

  ArrayList<String> moves = new ArrayList<String>();

  String[] preMovesArray = split(movesString, "_");
  for (int i = 0; i < preMovesArray.length; i+=2) {
    int numOfMoves = Integer.valueOf(preMovesArray[i+1]);
    for (int n = 0; n < numOfMoves; n++) {
      moves.add(preMovesArray[i]);
    }
  }

  return moves;
}

// Renvoie la liste des coups complètes (avec les effectifs) correspondant à l'index de la fen
public String[] getMoveStringFromFEN(String fen) {
  String movesString = "";

  int index = searchFenInBook(fen);
  if (index == -1) return new String[0];

  String b = book.get(index);

  // Index auquel les coups commencent à être indiqués
  int startMoves = fen.length()+1;

  movesString = b.substring(startMoves, b.length());

  String[] movesArray = split(movesString, "_");
  return movesArray;
}

// Renvoie la liste des coups complètes (avec les effectifs) correspondant à l'index de la fen
public String[] getMoveStringFromIndex(int index) {
  String movesString = "";
  String b = book.get(index);
  int startMoves = 0;

  for (int i = b.length() - 1; i >= 0; i--) {
    if (b.charAt(i) == ':') startMoves = i+1;
  }

  movesString = b.substring(startMoves, b.length());

  String[] movesArray = split(movesString, "_");
  return movesArray;
}

// Choisi aléatoirement un coup parmi ceux du livre qui sont passés en argument et le joue
public Move playMoveFromBook(ArrayList<String> moves) {
  String moveString = moves.get(floor(random(0, moves.size())));
  int fromI = Integer.valueOf(String.valueOf(moveString.charAt(0)));
  int fromJ = Integer.valueOf(String.valueOf(moveString.charAt(1)));
  int i = Integer.valueOf(String.valueOf(moveString.charAt(2)));
  int j = Integer.valueOf(String.valueOf(moveString.charAt(3)));
  Move m;
  if (fromI == 8) { //petit roque : 88xx
    if (i == 8) m = new Move(grid[4][7].piece, 6, 7, null, 1); //blanc 8888
    else m = new Move(grid[4][0].piece, 6, 0, null, 1); //noir 8899
  }
  else if (fromI == 9) { //grand roque 99xx
    if (i == 8) m = new Move(grid[4][7].piece, 2, 7, null, 2); //blanc 9988
    else m = new Move(grid[4][0].piece, 2, 0, null, 2); //noir 9999
  }
  else {
    m = new Move(grid[fromI][fromJ].piece, i, j, grid[i][j].piece, 0);
  }
  m.play();

  return m;
}

// Ajoute un coup au livre d'ouverture correspondant à la position dans laquelle il doit être joué
public void addMoveToBook(String fen, Move m) {
  int index = searchFenInBook(fen);
  String moveString;
  if (m.special == 1) moveString = "88" + (m.piece.c == 0 ? "88" : "99"); //petit roque
  else if (m.special == 2) moveString = "99" + (m.piece.c == 0 ? "88" : "99"); //grand roque
  else moveString = str(m.fromI) + str(m.fromJ) + str(m.i) + str(m.j);

  if (index == -1) {
    // La fen introuvable, donc on l'ajoute au livre
    book.add(fen + ":" + moveString + "_1");
    println(moveString + " ajouté + nouvelle position");
  }
  else {
    // La fen est déjà référencée (trouvée à index)
    String[] movesStringAtIndex = getMoveStringFromIndex(index);

    for (int i = 0; i < movesStringAtIndex.length; i+=2) {
      if (movesStringAtIndex[i].equals(moveString)) {
        // Le coup est déjà enregistré, on incrémente l'effectif (en reconstituant la ligne du livre)
        int effectif = Integer.valueOf(movesStringAtIndex[i+1]);
        movesStringAtIndex[i+1] = str(effectif+1);
        String bookLine = fen + ":" + join(movesStringAtIndex, "_");
        book.set(index, bookLine);
        println(moveString + " incrémenté : " + (effectif+1));
        return;
      }
    }

    // Si le coup n'est pas déjà répertorié, on l'ajoute au livre
    String b = book.get(index);
    b = b + "_" + moveString + "_1";
    book.set(index, b);
    println(moveString + " ajouté (nouveau coup, position connue)");
  }
}

// Sauvegarde le livre d'ouverture en écrivant les données dans le fichier book.txt OK
public void saveBook() {
  String[] savedBook = new String[book.size()];
  for (int i = 0; i < book.size(); i++) savedBook[i] = book.get(i);
  saveStrings("data/book.txt", savedBook);
}
class PreComputedData {
  boolean[][][][][] distanceTable = new boolean[6][8][8][8][8]; // piece index / grid[from][from] / grid[target][target]
  int[][] distanceFromCenter = new int[8][8]; // i, j
  int[][][][] tropismDistance = new int[8][8][8][8]; // i1, j1, i2, j2
  HashMap<Integer, ArrayList<Move>[][]> preComputedMovesMap = new HashMap<Integer, ArrayList<Move>[][]>(); // pieceIndex, i, j

  PreComputedData() { }

  public void init() {
    this.initDistanceTable();
    this.initDistanceFromCenter();
    this.initTropismDistance();
    this.initMoveMap();
  }

  public boolean getDistanceTable(int p, int fi, int fj, int ti, int tj) {
    return this.distanceTable[p][fi][fj][ti][tj];
  }

  public int getDistanceFromCenter(int i, int j) {
    return this.distanceFromCenter[i][j];
  }

  public int getTropismDistance(int i1, int i2, int j1, int j2) {
    return this.tropismDistance[i1][i2][j1][j2];
  }

  public ArrayList<Move> getMove(int pieceIndex, int i, int j) {
    return this.preComputedMovesMap.get(pieceIndex)[i][j];
  }

  public void initMoveMap() {
    preComputedMovesMap.put(ROI_INDEX, new ArrayList[8][8]);
    preComputedMovesMap.put(TOUR_INDEX, new ArrayList[8][8]);
    preComputedMovesMap.put(FOU_INDEX, new ArrayList[8][8]);
    preComputedMovesMap.put(CAVALIER_INDEX, new ArrayList[8][8]);
    // Pion ?

    // Cavalier : 336
    // Fou : 560
    // Tour : 896
    // Roi : 420
    // --> 2212
  }

  public void initDistanceTable() {
    for (int c = 0; c < 6; c++) {
      for (int fromI = 0; fromI < 8; fromI++) {
        for (int fromJ = 0; fromJ < 8; fromJ++) {
          for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
              if (c == 0) this.distanceTable[c][fromI][fromJ][i][j] = isAtKingDist(grid[fromI][fromJ], grid[i][j]);
              else if (c == 1) this.distanceTable[c][fromI][fromJ][i][j] = isAtQueenDist(grid[fromI][fromJ], grid[i][j]);
              else if (c == 2) this.distanceTable[c][fromI][fromJ][i][j] = isAtRookDist(grid[fromI][fromJ], grid[i][j]);
              else if (c == 3) this.distanceTable[c][fromI][fromJ][i][j] = isAtBishopDist(grid[fromI][fromJ], grid[i][j]);
              else if (c == 4) this.distanceTable[c][fromI][fromJ][i][j] = isAtKnightDist(grid[fromI][fromJ], grid[i][j]);
              else if (c == 5) this.distanceTable[c][fromI][fromJ][i][j] = isAtDiagDist(grid[fromI][fromJ], grid[i][j]);
            }
          }
        }
      }
    }
  }

  public void initDistanceFromCenter() {
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (i < 4) {
          if (j < 4) this.distanceFromCenter[i][j] = abs(3 - i) + abs(3 - j);
          else this.distanceFromCenter[i][j] = abs(3 - i) + abs(4 - j);
        } else {
          if (j < 4) this.distanceFromCenter[i][j] = abs(4 - i) + abs(3 - j);
          else this.distanceFromCenter[i][j] = abs(4 - i) + abs(4 - j);
        }
      }
    }
  }

  public void initTropismDistance() {
    for (int i1 = 0; i1 < 8; i1++) {
      for (int j1 = 0; j1 < 8; j1++) {
        for (int i2 = 0; i2 < 8; i2++) {
          for (int j2 = 0; j2 < 8; j2++) {
            this.tropismDistance[i1][j1][i2][j2] = 14 - ( abs(i1 - i2) + abs(j1 - j2) );
          }
        }
      }
    }
  }
}
/////////////////////////////////////////////////////////////////

// Configurations principales

boolean timeControl = false; // Activer le temps (ou pas)
boolean hackerSansFin = true; // Activer le hacker sans fin (ou pas)
int hackerSite = LICHESS; // Hacker sur chess.com ou lichess
int hackerScanCooldown = 100; // Temps (ms) entre chaque scan du hacker
int hackerTestRestartCooldown = 1300; // Temps (ms) entre chaque scan du hacker pour relancer la partie
int scansBetweenEndDetect = 50; // Nombre de scans entre chaque détection de fin de partie
Color endColorLichess = new Color(67, 107, 27);

boolean ENABLE_ARNAQUES = true; // Activer les arnaques des moutons (ou pas)
boolean MODE_PROBLEME = false; // Activer le mode résolution de problèmes (ou pas)
boolean MODE_SANS_AFFICHAGE = false; // Affiche (ou pas) l'échiquier pendant le hacker

/////////////////////////////////////////////////////////////////

// Configurations diverses et variées

String name = "Echecs on java";

String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq"; // Position de départ
// String startFEN = "7k/1pp3qp/3p3r/pQb1pr2/PP6/2P3B1/5PPP/3R1R1K b"; // Problème difficile
// String startFEN = "7K/P1p1p1p1/2P1P1Pk/6pP/3p2P1/1P6/3P4/8 w"; // Underpromotion

/////////////////////////////////////////////////////////////////

// Sound control et time control

int soundControl = 0; //0 = aucun, 1 = partie, 2 = musique
boolean attach = true;
boolean stats = true;
boolean details = true;
int[][] times = {
  {0, 0, 0}, //blancs : minutes, secondes, incrément
  {0, 0, 0}  //noirs : minutes, secondes, incrément
};

/////////////////////////////////////////////////////////////////

// Fenêtre principale

int w = 70; //100 pour Windows
float pieceSize = w;
boolean pointDeVue = true;
int offsetX = 100 * w/75; //100 * w/100 pour Windows
int offsetY = 50 * w/75; //50 * w/100 pour Windows
int selectWidth = 1380;
int selectHeight = 595;
int gameWidth = cols * w + offsetX;
int gameHeight = rows * w + offsetY;

/////////////////////////////////////////////////////////////////

// End screen

int restartTimeOut = 5000; //en ms, temps avant d'annuler automatiquement l'annulation de partie
int timeBeforeEndDisplay = 750; //en ms, temps avant d'afficher l'écran de fin de partie
float targetEndScreenY = 2.5f*w + offsetY;
float endScreenEasing = 0.07f;
float yEndScreen = 0;

/////////////////////////////////////////////////////////////////

// Gestionnaire de fens pour l'éditeur de position

String[] savedFENS = {
  "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq",
  "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w",
  "r5k1/6pp/5b2/4N3/8/q7/5PPP/3Q1RK1 w",
  "8/1RK5/8/3k4/8/8/8/8 w",
  "8/6p1/8/3k4/8/3K4/8/8 w",
  "8/3K4/4P3/8/8/8/6k1/7q w",
  "8/k7/3p4/p2P1p2/P2P1P2/8/8/K7 w"
};

String[] savedFENSname = {
  "Position de départ",
  "Vecteur vitesse",
  "Mat à l'étouffé",
  "Mat roi-tour",
  "Opposition",
  "Check-check-check",
  "Transpositions"
};

/////////////////////////////////////////////////////////////////

// Interface

int iconSize = 40 * w/75; //40 * w/100 pour Windows
int edgeSpacing = (int)(offsetX - w) / 2 + 1;
int distanceFromTop = (int)(offsetY - iconSize) / 2 + 1;
int spacingBetweenIcons = (gameWidth - (edgeSpacing*2 + icons.length*iconSize)) / (icons.length-1);

int editorIconSize = 40 * w/75; // w/100 pour Windows
int editorEdgeSpacing = (int)(offsetX - w) / 2 + 10;
int spacingBetweenEditorIcons = (gameWidth - (editorEdgeSpacing*2 + editorIcons.length*editorIconSize)) / (editorIcons.length-1);

int addPiecesColor = 0;

/////////////////////////////////////////////////////////////////

// Moutons, hacker et autres...

String[] moutonMessages = {
  "Moutonn !! YOU LOUSE",
  "YOU CHEAT",
  "LOIC LA GROSSE VACHE",
  "VOLEUR, ARNAQUEUR",
  "VIENS DANS LE TROUPEAU",
  "RESERVE D'ÉLO",
  "YOU LOUSE, YOU CHEAT",
  "T'ES NUL !!!",
  "TU VAS TE FAIRE MATER",
  "Sur prise, prise puis prise"
};

int missclickCooldown = 6;
int timeBeforeHackerRestart = 3500;

/////////////////////////////////////////////////////////////////

// Valeurs pour l'évaluation

int[] kingSafetyPenalty = {
    0,   0,   1,   2,   3,   5,   7,   9,  12,  15,
   18,  22,  26,  30,  35,  39,  44,  50,  56,  62,
   68,  75,  82,  85,  89,  97, 105, 113, 122, 131,
  140, 150, 169, 180, 191, 202, 213, 225, 237, 238,
  260, 272, 283, 295, 307, 319, 330, 342, 354, 366,
  377, 389, 401, 412, 424, 436, 448, 459, 471, 483,
  494, 500, 500, 500, 500, 500, 500, 500, 500, 500,
  500, 500, 500, 500, 500, 500, 500, 500, 500, 500,
  500, 500, 500, 500, 500, 500, 500, 500, 500, 500,
  500, 500, 500, 500, 500, 500, 500, 500, 500, 500,
};

float[][] maireKnightGrid = {
  {-50, -40, -30, -30,  -30, -30, -40, -50},
  {-40, -20,   0,   5,    0,   5, -20, -40},
  {-30,   0,  10,  15,   15,  10,   0, -30},
  {-30,   0,  15,  20,   20,  15,   0, -30},
  {-30,   0,  15,  20,   20,  15,   0, -30},
  {-30,   0,  10,  15,   15,  10,   0, -30},
  {-40, -20,   0,   5,    0,   5, -20, -40},
  {-50, -40, -30, -30,  -30, -30, -40, -50}
};

float[][] maireQueenGrid = {
  {-20, -10, -10, -5, 0, -10, -10, -20},
  {-10,   0,   0,  0, 0,   5,   0, -10},
  {-10,   0,   5,  5, 5,   5,   5, -10},
  { -5,   0,   5,  5, 5,   5,   0,  -5},
  { -5,   0,   5,  5, 5,   5,   0,  -5},
  {-10,   0,   5,  5, 5,   5,   5, -10},
  {-10,   0,   0,  0, 0,   5,   0, -10},
  {-20, -10, -10, -5, 0, -10, -10, -20}
};

float[][] maireBishopGrid = {
  {-20, -10, -10, -10, -10, -10, -10, -20},
  {-10,   0,   0,   5,   0,  10,   5, -10},
  {-10,   0,   5,   5,  10,  10,   0, -10},
  {-10,   0,  10,  10,  10,  10,   0, -10},
  {-10,   0,  10,  10,  10,  10,   0, -10},
  {-10,   0,   5,   5,  10,  10,   0, -10},
  {-10,   0,   0,   5,   0,  10,   5, -10},
  {-20, -10, -10, -10, -10, -10, -10, -20},
};

float[][] mairePawnGrid = {
  {100, 50, 10,  5,  0,   5,   5, 0},
  {100, 50, 10,  5,  0,  -5,  10, 0},
  {100, 50, 20, 10,  0, -10,  10, 0},
  {100, 50, 30, 25, 20,   0, -20, 0},
  {100, 50, 30, 25, 20,   0, -20, 0},
  {100, 50, 20, 10,  0, -10,  10, 0},
  {100, 50, 10,  5,  0,  -5,  10, 0},
  {100, 50, 10,  5,  0,   5,   5, 0}
};
float[][] mairePawnGridEnd = {
  {0, 100, 60, 40, 20, 10, 10, 0},
  {0, 100, 60, 40, 20, 10, 10, 0},
  {0, 100, 40, 30, 20, 10, 10, 0},
  {0, 100, 30, 20, 18, 10, 10, 0},
  {0, 100, 30, 20, 18, 10, 10, 0},
  {0, 100, 40, 30, 20, 10, 10, 0},
  {0, 100, 60, 40, 20, 10, 10, 0},
  {0, 100, 60, 40, 20, 10, 10, 0}
};

float[][] maireKingGrid = {
  {-30, -30, -30, -30, -20, -10, 20,  20},
  {-40, -40, -40, -40, -30, -20, 20,  35},
  {-40, -40, -40, -40, -30, -20,  0, -10}, // 0 à la fin avant
  {-50, -50, -50, -50, -40, -20,  0, -10}, // 0 à la fin avant
  {-50, -50, -50, -50, -40, -20,  0, -10}, // 0 à la fin avant
  {-40, -40, -40, -40, -30, -20,  0, -10}, // 0 à la fin avant
  {-40, -40, -40, -40, -30, -20, 20,  35},
  {-30, -30, -30, -30, -20, -10, 20,  20},
};

// float[][] maireKingGridEnd = {
//   {-55, -45, -35, -25, -25, -35, -45, -55},
//   {-45, -25, -10, -10, -10, -10, -25, -45},
//   {-35, -10,  20,  25,  25,  20, -10, -35},
//   {-25,   0,  25,  30,  30,  25,   0, -25},
//   {-25,   0,  25,  30,  30,  25,   0, -25},
//   {-35, -10,  20,  25,  25,  20, -10, -35},
//   {-45, -25, -10, -10, -10, -10, -25, -45},
//   {-55, -45, -35, -25, -25, -35, -45, -55},
// };

float[][] maireRookGrid = {
  {0,  5, -5, -5, -5, -5, -5, 0},
  {0, 10,  0,  0,  0,  0,  0, 0},
  {0, 10,  0,  0,  0,  0,  0, 0},
  {0, 10,  0,  0,  0,  0,  0, 5},
  {0, 10,  0,  0,  0,  0,  0, 5},
  {0, 10,  0,  0,  0,  0,  0, 0},
  {0, 10,  0,  0,  0,  0,  0, 0},
  {0,  5, -5, -5, -5, -5, -5, 0}
};

float[][] zeroArray = {
  {0, 0, 0, 0, 0, 0, 0, 0},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {0, 0, 0, 0, 0, 0, 0, 0}
};

/////////////////////////////////////////////////////////////////

float[][] loicKingGrid = {
  {-30, -30, -30, -30, -20, -10,  5,  0},
  {-40, -40, -40, -40, -30, -20,  5,  0},
  {-40, -40, -40, -40, -30, -20,  0,  0},
  {-50, -50, -50, -50, -40, -20,  0, 20},
  {-50, -50, -50, -50, -40, -20,  0, 20},
  {-40, -40, -40, -40, -30, -20,  0,  0},
  {-40, -40, -40, -40, -30, -20,  5,  0},
  {-30, -30, -30, -30, -20, -10,  5,  0},
};

float[][] loicQueenGrid = {
  {-20, -10, -10, -5, 0, -10, -10, -20},
  {-10,   0,   0,  0, 0,   5,   0, -10},
  {-10,   0,   5,  5, 5,   5,  -5, -10},
  { -5,   0,   5,  5, 5,   5,   0,  20},
  { -5,   0,   5,  5, 5,   5,   0,  20},
  {-10,   0,   5,  5, 5,   5,  -5, -10},
  {-10,   0,   0,  0, 0,   5,   0, -10},
  {-20, -10, -10, -5, 0, -10, -10, -20}
};

float[][] loicBishopGrid = {
  {-20, -10, - 10,  -10, -10, -10, -10, -20},
  {-10,   0,    0,  -15,   0, -10,  40,   0},
  {-10,   0,  -15,  -15, -10, -10, -20, -10},
  {-10,   0,  -10,  -10, -10, -10,   0, -10},
  {-10,   0,  -10,  -10, -10, -10,   0, -10},
  {-10,   0,  -15,  -15, -10, -10, -20, -10},
  {-10,   0,    0,  -15,   0, -10,  40,   0},
  {-20, -10,  -10,  -10, -10, -10, -10, -20},
};

float[][] loicKnightGrid = {
  {-50, -40, -30, -30, -30, -30, -40,  -50},
  {-40, -20,   0,   0,   0,   0, -20,  -40},
  {-30,   0,   0,   0,   0, -10,   0,  -30},
  {-30,   0,   0,   0,   0,   0,  30,  -30},
  {-30,   0,   0,   0,   0,   0,  30,  -30},
  {-30,   0,   0,   0,   0, -10,   0,  -30},
  {-40, -20,   0,   0,   0,   5, -20,  -40},
  {-50, -40, -30, -30, -30, -30, -40,  -50}
};

float[][] loicRookGrid = {
  {0,  5, -5, -5, -5, -5, -5, 10},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  5, -5, -5, -5, -5, -5, 10}
};

float[][] loicPawnGrid = {
  {100, 50, 10, 5, 0,  20,   5, 0},
  {100, 50, 10, 5, 0,  20,  10, 0},
  {100, 50, 20, 5, 0, -10,  20, 0},
  {100, 50, 25, 5, 0,  35, -20, 0},
  {100, 50, 25, 5, 0,  35, -20, 0},
  {100, 50, 20, 5, 0, -10,  20, 0},
  {100, 50, 10, 5, 0,  20,  10, 0},
  {100, 50, 10, 5, 0,  20,   5, 0}
};

int loicEvalArray[] = {100000, 900, 150, 300, 300, 100};
float[][] loicPosArray[] = {loicKingGrid, loicQueenGrid, loicRookGrid, loicBishopGrid, loicKnightGrid, loicPawnGrid};

int maireEvalArray[] = {100000, 900, 500, 330, 320, 100};
float[][] mairePosArray[] = {maireKingGrid, maireQueenGrid, maireRookGrid, maireBishopGrid, maireKnightGrid, mairePawnGrid};

// à voir pour les zéros
float[][] mairePosArrayEnd[] = {zeroArray, zeroArray, zeroArray, zeroArray, zeroArray, mairePawnGridEnd};

/////////////////////////////////////////////////////////////////

// Configurations diverses et variées

PImage[] imageArrayB = {roi_b, dame_b, tour_b, fou_b, cavalier_b, pion_b};
PImage[] imageArrayN = {roi_n, dame_n, tour_n, fou_n, cavalier_n, pion_n};
String codeArrayB[] = {"K", "Q", "R", "B", "N", "P"};
String codeArrayN[] = {"k", "q", "r", "b", "n", "p"};

/////////////////////////////////////////////////////////////////
public void test() {
  // Move move = new Move(grid[1][7].piece, 2, 5, null, 0);
  // Move move2 = new Move(grid[4][6].piece, 4, 4, null, 0);
  // Move move = new Move(grid[4][4].piece, 3, 3, grid[3][3].piece, 0);
  // Move move = new Move(grid[5][5].piece, 5, 2, grid[5][2].piece, 0);
  // LeMaire m = new LeMaire(1, 2, 3, true);

   long count = 0;
   long numTest = 1000000;
   long pas = numTest/10;

   int before = millis();

   for (int i = 0; i < numTest; i++) {
     formatInt((int)numTest);
     count++;
     if (i % pas == 0) println(i);
   }

   int temps = millis() - before;
   println("---------------");
   println(formatInt((int)count) + " itérations");
   println(temps + " ms");
   println(formatInt((int)(1000*count/temps)) + " par seconde");
}

public void keyPressed() {
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

  if (key == 'h' || key == 'H') printHelpMenu();

  if (gameState == 1) {

    if (useHacker && !hackerPret && !hackerAPImode) {
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
    else if (key == 'b' || key == 'B') highlightBook();
    else if (key == 'Q') forceQuit();
    else if (keyCode == 'v' || keyCode == 'V') toggleVariantes();
    else if (keyCode == 'c' || keyCode == 'C') savePGN();
    else if (keyCode == UP) delayUp();
    else if (keyCode == DOWN) delayDown();
    else if (keyCode == LEFT) rewindBack();
    else if (keyCode == RIGHT) rewindForward();
    else if (key == 'T') test();
    else if ((key == 'r' || key == 'R') && useHacker && hackerPret) {
      endOnHackerDetect();
      timeAtHackerEnd = millis();
    }

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

public void mouseReleased() {
  if (MODE_SANS_AFFICHAGE && useHacker && hackerPret) return;

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

public void mouseDragged() {
  if (MODE_SANS_AFFICHAGE && useHacker && hackerPret) return;

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
    if (random(1) <= 0.75f) {
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

public void mousePressed() {
  if (MODE_SANS_AFFICHAGE && useHacker && hackerPret) return;

  if (gameState == 1) {

    // Boutons de revanche
    if (gameEnded && !useHacker && !hackerPret) {
      if (rematchButton.contains(mouseX, mouseY)) { rematch(); return; }
      if (newGameButton.contains(mouseX, mouseY)) { resetGame(true); return; }
    }

    // Barre d'outils
    for (Button b : iconButtons) {
      if (b.contains(mouseX, mouseY)) {
        b.callShortcut();
        return;
      }
    }

    // Échiquier
    if (joueurs.get(tourDeQui).name == "Humain" && !blockPlaying) {

      if (enPromotion != null) {
        for (int i = 0; i < promoButtons.size(); i++) {
          if (promoButtons.get(i).contains(mouseX, mouseY)) {
            playerPromote(i);
          }
        }
        return;
      }

      int i = getGridI();
      int j = getGridJ();

      if (i >= 0 && i < cols && j >= 0 && j < rows) {
        clickedOnBoard(i, j);
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
        for (ToggleButton t : toggles1) t.state = false;
        toggles1.get(i).state = !toggles1.get(i).state;
        j1 = toggles1.get(i).name;
      }
    }

    for (int i = 0; i < toggles2.size(); i++) {
      if (toggles2.get(i).contains(mouseX, mouseY)) {
        for (ToggleButton t : toggles2) t.state = false;
        toggles2.get(i).state = !toggles2.get(i).state;
        j2 = toggles2.get(i).name;
      }
    }

    for (int i = 0; i < hubButtons.size(); i++) {
      TextButton b = hubButtons.get(i);
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
    for (Button b : editorIconButtons) {
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

public void mouseMoved() {
  if (MODE_SANS_AFFICHAGE && useHacker && hackerPret) return;

  if (gameState == 1) {

    // Button rematch
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
    for (ToggleButton t : toggles1) {
      if (t.contains(mouseX, mouseY)) {
        cursor(HAND);
        return;
      }
    }
    for (ToggleButton t2 : toggles2) {
      if (t2.contains(mouseX, mouseY)) {
        cursor(HAND);
        return;
      }
    }
    for (TextButton b : hubButtons) {
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
/////////////////////////////////////////////////////////////////

// 1) Fonctions utiles (ou pas)
// 2) Hacker
// 3) Affichages
// 4) Plateau et presets
// 5) FEN, historiques et données
// 6) Fonctions pour calculs et recherche

/////////////////////////////////////////////////////////////////

// Fonctions utiles (ou pas)

public void alert(String message, int time) {
  alert = message;
  alertTime = time;
  alertStarted = millis();
}

public void sendMoutonMessage(String message, float x, float y, int time) {
  messageMouton = message;
  messageMoutonTime = time;
  alertPos.x = (int)x;
  alertPos.y = (int)y;
  messageMoutonStarted = millis();
}

public boolean isSameColor(Color c1, Color c2) {
  return c1.getRed() == c2.getRed() && c1.getGreen() == c2.getGreen() && c1.getBlue() == c2.getBlue();
}

public boolean isSimilarColor(Color c1, Color c2) {
  return abs(c1.getRed()-c2.getRed()) <= 10 && abs(c1.getGreen()-c2.getGreen()) <= 10 && abs(c1.getBlue()-c2.getBlue()) <= 10;
}

public String roundedString(float num) {
  boolean isInteger = num % 1 == 0;
  return (isInteger ? str((int)num) : nf(num, 1, 1));
}

public int stringToInt(String str) {
  return Integer.parseInt(str);
}

public String roundNumber(float num, int digit) {
  return nf(num, 1, digit);
}

public String GetTextFromClipboard() {
  String text = (String) GetFromClipboard(DataFlavor.stringFlavor);
  if (text==null) return "";

  return text;
}

public String formatInt(int value) {
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

public boolean isMateValue(float eval, int plyFromRoot) {
 int sign = (eval < 0) ? -1 : 1;
 float value = eval * sign;
 value += plyFromRoot;
 return (value == 50000);
}

public String evalToStringMaire(float eval) {
  int sign = (eval < 0) ? -1 : 1;
  float value = eval * sign;
  if (value < 40000) return roundNumber(eval/100.0f, 3);
  int ply = (int)(50000 - value);
  return "MAT EN " + ply;
}

public String evalToStringLoic(float eval) {
  int sign = (eval < 0) ? -1 : 1;
  float value = eval * sign;
  if (value < 15000) return roundNumber(eval/100.0f, 3);
  if (value < 37000) {
    int ply = (int)(25000 - value);
    return "MAT EN " + ply;
  }
  int ply = (int)(50000 - value);
  return "PAT EN " + ply;
}

public void updateBlockPlaying() {
  blockPlaying = !play || gameEnded || rewind || (useHacker && !hackerPret);
}

public Object GetFromClipboard(DataFlavor flavor) {

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

public static final javax.swing.JFrame getJFrame(final PSurface surf) {
  return
    (javax.swing.JFrame)
    ((processing.awt.PSurfaceAWT.SmoothCanvas)
    surf.getNative()).getFrame();
}

public void error(String function, String message) {
  println();
  println(">>> !! ERREUR " + message.toUpperCase() + " DANS " + function.toUpperCase() + " !! <<<");
  println();
}

/////////////////////////////////////////////////////////////////

// Hacker

public void cheat(int c, int fromI, int fromJ, int i, int j, int special) {
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

public boolean chessComEndDetected() {
  Color endScreen = hacker.getPixelColor(hackerCoords[3][2].x, hackerCoords[3][2].y);
  return isSameColor(endScreen, Color.white);
}

public boolean lichessEndDetected() {
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

public boolean hackerEndDetected() {
  return ( (hackerSite == CHESSCOM && chessComEndDetected()) || (hackerSite == LICHESS && lichessEndDetected()) );
}

public Color[][] scanBoard(boolean debugPrint) {
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


public Move getMoveOnBoard() {
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

public void scanMoveOnBoard() {
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
public boolean verifyCalibration(int tolerance, boolean confondu) {
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

public void click(int x, int y) {
  hacker.mouseMove(x, y);
  hacker.mousePress(InputEvent.BUTTON1_DOWN_MASK);
  hacker.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
}

public void hackStartGame() {
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

public void handleWaitForRestart() {
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

public void restoreCalibrationSaves() {
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

public void forceCalibrationRestore() {
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

public void addPointToCalibration() {
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

public void calibrerHacker() {
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

public void setHackerPOV(int pov) {
  if (pov == currentHackerPOV) return;
  hackerCoords = reverseCoords(hackerCoords);
  currentHackerPOV = pov;
}

public Point[][] reverseCoords(Point[][] arr) {
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

public Color copyColor(Color c) {
  int r = c.getRed();
  int g = c.getGreen();
  int b = c.getBlue();
  return new Color(r, g, b);
}

public Point copyPoint(Point p) {
  Point result = new Point();
  result.x = p.x;
  result.y = p.y;
  return result;
}

public Point[][] copyCoords(Point[][] array) {
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

public void displayAlert() {
  if (millis() - alertStarted >= alertTime) {
    alert = ""; alertStarted = 0; alertTime = 0;
    return;
  }

  fill(255);
  rectMode(CORNER);
  rect(offsetX + w/2, offsetY + w/2, 7*w, 1.5f*w, 5, 5, 5, 5);

  imageMode(CORNER);
  image(warning, offsetX + 0.625f*w, offsetY + 0.625f*w, 1.25f*w, 1.25f*w);

  textAlign(CENTER, CENTER);
  fill(color(0xffb33430));
  textSize(28 * w/75);
  text(alert, offsetX + 4.5f*w, offsetY + 1.25f*w);
}

public void displayMoutonAlert() {
  if (millis() - messageMoutonStarted >= messageMoutonTime) {
    messageMouton = ""; messageMoutonStarted = 0; messageMoutonTime = 0;
    return;
  }

  fill(255);
  rectMode(CORNER);
  rect(alertPos.x, alertPos.y, 6*w, 2*w, 5, 5, 5, 5);

  imageMode(CORNER);
  image(mouton, alertPos.x + 0.125f*w, alertPos.y + 0.125f*w, 1.75f*w, 1.75f*w);

  textAlign(CENTER, CENTER);
  textSize(28 * w/75);
  fill(color(0xffb33430));
  text("Nouveau message :", alertPos.x + 4*w, alertPos.y + 0.5f*w);
  strokeWeight(2);
  stroke(color(0xffb33430));
  line(alertPos.x + 2.25f*w, alertPos.y + 0.8f*w, alertPos.x + 5.75f*w, alertPos.y + 0.8f*w);

  textAlign(LEFT, CENTER);
  textSize(23 * w/75);
  fill(color(0xff000000));
  text(messageMouton, alertPos.x + 2*w, alertPos.y + 1.1f*w);
}

public void blur(int alpha) {
  fill(220, 220, 220, alpha);
  rectMode(CORNER);
  rect(offsetX, offsetY, rows * w, cols * w);
}

public void drawHackerPage() {
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
  fill(color(0xffb33430));
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
    text("HG : " + hg + "     " + "BD : " + bd, (width-offsetX)/2 + offsetX, rectY + (100*w/75)/1.15f);
  } else {
    fill(0);
    noStroke();
    textSize(27 * w/75);
    text("En attente de l'API...", (width-offsetX)/2 + offsetX, rectY + (100*w/75)/2);
  }
}

public void drawSavedPosition() {
  blur(220);
  for (ButtonFEN b : savedFENSbuttons) b.show();
}

public void drawParameters() {
  blur(220);
  fill(0);
  stroke(0);
  textSize(35 * w/75);
  textAlign(LEFT, CENTER);
  text("Trait :", offsetX + w/2, offsetY + w/2);
  line(offsetX + w/2, offsetY + 0.875f*w, offsetX + w/2 + textWidth("Trait :"), offsetY + 0.875f*w);

  text("Roques :", offsetX + w/2, offsetY + 2.5f*w);
  line(offsetX + w/2, offsetY + 2.875f*w, offsetX + w/2 + textWidth("Roques :"), offsetY + 2.875f*w);
}

public void drawInfoBox(String i) {
  noStroke();
  fill(pointDeVue ? 255 : color(49, 46, 43));
  rectMode(CENTER);
  rect((width-offsetX)/2 + offsetX, offsetY + w/2, 15 * w/75 * i.length(), 50 * w/75);
  fill(pointDeVue ? color(49, 46, 43) : 255);
  textAlign(CENTER, CENTER);
  textSize(25 * w/75);
  text(i, (width-offsetX)/2 + offsetX, offsetY + w/2.3f);
}

public void drawPlayersInfos() {
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

public void drawEndScreen(float y) {
  noStroke();
  int gris = color(0xff666463);
  int vert = color(0xff8da75a);
  //int rouge = color(#b33430);

  blur(150);

  // grand rectangle
  float rectX = 1.75f*w + offsetX, rectY = y;
  float rectW = 4.5f*w, rectH = 3*w;
  fill(255);
  rect(rectX, rectY, rectW, rectH, 5);

  // images
  float imgW = 1.2f * w;
  float space = ((rectY + rectH) - (rectY + rectH/3) - imgW)/2; //se simplifie très probablement
  float imgY = (rectY + rectH/3) + space/1.5f;
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
  text(endReason, rectX+rectW/2, rectY+rectH - space/1.5f);

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
  text(title, rectX + rectW/2, rectY + rectH/6.5f);
}

/////////////////////////////////////////////////////////////////

// Plateau et presets

public void updateBoard() {
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].show();
    }
  }

  for (int i = 0; i < piecesToDisplay.size(); i++) {
    piecesToDisplay.get(i).show();
  }
}

public void deselectAll() {
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

public void playerPromote(int numButton) {
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

public void clickedOnBoard(int i, int j) {
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

public void setPieces() {
  importFEN(startFEN);

  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);

  calcEndGameWeight();
  zobrist.initHash();
  // println("Hash de la position : " + zobrist.hash);
}

public int getGridI() {
  if (mouseX < offsetX || mouseX > cols*w + offsetX || mouseY < offsetY || mouseY > rows*w + offsetY) return -1;

  int i;
  if (pointDeVue) i = (int)(mouseX-offsetX)/w;
  else i = 7 - (int)(mouseX-offsetX)/w;
  return i;
}

public int getGridJ() {
  if (mouseX < offsetX || mouseX > cols*w + offsetX || mouseY < offsetY || mouseY > rows*w + offsetY) return -1;

  int j;
  if (pointDeVue) j = (int)(mouseY-offsetY)/w;
  else j = 7 - (int)(mouseY-offsetY)/w;
  return j;
}

public void showPromoButtons() {
  for (int i = 0; i < promoButtons.size(); i++) {
    promoButtons.get(i).show(enPromotion.c);
  }
}

public void removeAllPieces() {
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

public void playSound(Move m) {
  if (soundControl < 1 || (gameEnded && !rewind)) return;

  if (m.special == 3) { enPassant.play(); }
  if (playerInCheck(2) != -1) { check_sound.play(); return; }
  if (m.special == 1 || m.special == 2) { castle_sound.play(); return; }
  if (m.capture != null) { prise_sound.play(); return; }

  move_sound.play();
}

public void setMoveMarks(Cell c1, Cell c2) {
  clearMoveMarks();
  c1.moveMark = true;
  c2.moveMark = true;
}

public void clearMoveMarks() {
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      grid[i][j].moveMark = false;
    }
  }
}

public boolean pieceHovered() {
  int i = getGridI();
  int j = getGridJ();
  if (i >= 0 && i < cols && j >= 0 && j < rows) {
    if (grid[i][j].piece != null) return true;
  }

  return false;
}

public void addPieceToBoardByDrop(int value, int i, int j) {

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

public void bulletPreset() {
  if (timeControl) {
    times[0][0] = 1; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 1; times[1][1] = 0; times[1][2] = 0;
  }
  t1.setValue(720);
  t2.setValue(720);
}

public void blitzPreset() {
  if (timeControl) {
    times[0][0] = 3; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 3; times[1][1] = 0; times[1][2] = 0;
  }
  t1.setValue(2214);
  t2.setValue(2214);
}

public void rapidPreset() {
  if (timeControl) {
    times[0][0] = 10; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 10; times[1][1] = 0; times[1][2] = 0;
  }
  t1.setValue(7500);
  t2.setValue(7500);
}

public void noTimePreset() {
  if (timeControl) {
    times[0][0] = 0; times[0][1] = 0; times[0][2] = 0;
    times[1][0] = 0; times[1][1] = 0; times[1][2] = 0;
  }
}

/////////////////////////////////////////////////////////////////

// FEN, historiques et données

public String[] parseHTMLData(String str) {
  String[] m = split(str, "<div class=\"piece ");
  String[] pieceData = new String[m.length-1];

  for (int i = 1; i < m.length; i++) {
    if (m[i].charAt(0) != 's') pieceData[i-1] = "" + m[i].charAt(0) + m[i].charAt(1) + m[i].charAt(10) + m[i].charAt(11);
    else pieceData[i-1] = "" + m[i].charAt(10) + m[i].charAt(11) + m[i].charAt(7) + m[i].charAt(8);
  }
  printArray(pieceData);
  return pieceData;
}

public void HTMLtoBoard(String str) {
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

public void pasteFEN() {
  startFEN = GetTextFromClipboard();
  setPieces();
  println (">>> Fen importée");
}

public void addFenToHistory(String f) {
  positionHistory.add(f);
}

public void removeLastFromFenHistory() {
  positionHistory.remove(positionHistory.size() - 1);
}

public void addHashToHistory(long hash) {
  zobristHistory.add(hash);
}

public void removeLastFromHashHistory() {
  zobristHistory.remove(zobristHistory.size() - 1);
}

public String generateFEN() {
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

public void importFEN(String f) { //fen simplifiée, sans en passant et règle des 50 coups
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

public float calcEndGameWeight() {
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

public Piece removePiece(Piece piece) {
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

public ArrayList<Piece> copyPieceArrayList(ArrayList<Piece> p) {
  ArrayList<Piece> copy = new ArrayList<Piece>();

  for (int i = 0; i < p.size(); i++) {
    copy.add(p.get(i));
  }

  return copy;
}

public int countMaireMaterial(int c) {
  int material = 0;
  for (int i = 0; i < pieces[c].size(); i++) {
    material += pieces[c].get(i).maireEval;
  }
  return material;
}

public ArrayList selectionSortMoves(ArrayList<Move> arr) {
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

public int playerInCheck(int checkColor) {

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

public ArrayList<Move> findIllegalMoves(Piece piece, ArrayList<Move> pseudoMoves) {
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

public boolean isMoveLegal(Piece p, Move move) {
  boolean isLegal = true;

  move.make();
  if (playerInCheck(p.c) == p.c) {
    isLegal = false;
  }
  move.unmake();

  return isLegal;
}

public ArrayList<Move> removeIllegalMoves(Piece piece, ArrayList<Move> pseudoMoves) {

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
public void startGame() {
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

  if (attach) infos = "Épinglé";

  s1.hide();
  s2.hide();
  if (soundControl >= 2) {
    violons.stop();
    pachamama.play(); pachamama.loop();
  }
  gameState = 1;

  println(" ");
  if (stats) {
    println("Nouvelle fenêtre (" + name + ") : " + width + " x " + height);
  }
  println("Nouvelle partie : " + j1 + " (aux blancs) contre " + j2 + " (aux noirs)");
  println(" ");

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
    println("--> EXPECTED : " + generateFEN() + " / GOT : " + startFEN);
    println(" ");
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

public void startEditor() {
  gameState = 3;

  if (attach) infos = "Épinglé";

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

public void verifStartGame() {
  if (j1 != null && j2 != null) {
    startGame();
  } else {
    println("Veuillez selectionner 2 joueurs");
  }
}

public void rematch() {
  String savedJ1 = j1;
  String savedJ2 = j2;
  resetGame(false);
  j1 = savedJ1;
  j2 = savedJ2;
  startGame();
}

public void newAIGame(int ia, String type) {
  resetGame(false);
  println(ia, type);
  j1 = (ia == 0 ? type : "Humain");
  j2 = (ia == 0 ? "Humain" : type);
  startGame();
}

public void resetGame(boolean menu) {
  // reset les timers
  if (useTime) {
    ta.resetTimers();
    ta.hide();
    ta.goToDefaultPosition();
  }
  ga.hide();
  sa.hide();

  // réinitialise les variables
  resetSettingsToDefault();

  // resize la fenêtre et gameState en mode sélection
  if (menu) {
    surface.setSize(selectWidth, selectHeight);
    surface.setLocation(displayWidth/2 - width/2, 0);
    surface.setTitle(name + " - Selection des joueurs");
    surface.setAlwaysOnTop(false);
    surface.setVisible(true);
    gameState = 0;
  }

  // arrête la musique :(
  if (soundControl >= 2) {
    pachamama.stop();
    diagnostic.stop();
    violons.play(); violons.loop();
  }

  // replace les pièces
  setPieces();
}

public void resetSettingsToDefault() {
  // Enlève les pièces, les joueurs et bien d'autres...
  pieces[0].clear();
  pieces[1].clear();
  rois[0] = null;
  rois[1] = null;
  currentEnPassantable[0] = null;
  currentEnPassantable[1] = null;
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
  for (ToggleButton t : toggles1) {
    t.state = false;
  }
  for (ToggleButton t : toggles2) {
    t.state = false;
  }

  // Variables
  numberOfScan = 0;
  hackerWaitingToRestart = false;
  lastMoveTime = 0;
  deltaTimeMesured = 0;
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
  upLeftCorner = null;
  downRightCorner = null;
  newgameLocation = null;
  hackerPret = false;
  timeAtHackerEnd = 0;
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
  nbTour = 0.5f;
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
  j1 = null;
  j2 = null;

  //controlP5
  s1.show();
  s2.show();
}

public boolean checkFastRepetition(long hash) {
  int counter = 0;
  for (int i = zobristHistory.size()-1; i >= 0; i--) {
    if (zobristHistory.get(i) == hash) {
      counter++;
      if (counter >= 2) return true;
    }
  }
  return false;
}

public boolean checkRepetition(long hash) {
  int counter = 0;
  for (int i = zobristHistory.size()-1; i >= 0; i--) {
    if (zobristHistory.get(i) == hash) {
      counter++;
      if (counter >= 3) return true;
    }
  }
  return false;
}

public boolean manqueDeMateriel() {
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

public void checkGameState() {
  if (useHacker) return;

  //Manque de matériel
  if (manqueDeMateriel()) {
    winner = 2;
    println();
    println("Nulle par manque de matériel");
    println();
    addPgnDraw();
    updateScores(0.5f);
    endReason = "par manque de matériel";

    gameEnded = true;
    timeAtEnd = millis();
    infos = "Game ended";

    if (soundControl >= 1) nulle_sound.play();
    if (useTime) ta.stopTimers();
    return;
  }

  //Répétition
  long hash = zobrist.hash;
  if (checkRepetition(hash)) {
    winner = 2;
    println();
    println("Nulle par répétition");
    println();
    addPgnDraw();
    updateScores(0.5f);
    endReason = "par répétition";

    gameEnded = true;
    timeAtEnd = millis();
    infos = "Game ended";

    if (soundControl >= 1) nulle_sound.play();
    if (useTime) ta.stopTimers();
    return;
  }

  //Mats et pat
  if (generateAllLegalMoves(tourDeQui, true, false).size() == 0) { //le joueur au trait n'a aucun coup
    if (playerInCheck(tourDeQui) == tourDeQui) { //Mat, le joueur au trait est en échec

      if (soundControl >= 2) { pachamama.stop(); diagnostic.play(); }
      if (soundControl >= 1) { mat_sound.play(); }

      winner = (int)pow(tourDeQui-1, 2);
      println();
      println("Victoire des " + (tourDeQui == 0 ? "noirs" : "blancs") + " (" + joueurs.get(winner).name + ") par échec et mat");
      println();
      addPgnMate(winner);
      updateScores(winner);
      endReason = "par échec et mat";

    } else { //Pat, le joueur au trait n'est pas en échec

      if (soundControl >= 2) { pachamama.stop(); diagnostic.play(); }
      if (soundControl >= 1) { nulle_sound.play(); }

      winner = 2;
      println();
      println("Nulle par pat");
      println();
      addPgnDraw();
      updateScores(0.5f);
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

public void loseOnTime(int loser) {
  if (useHacker && hackerPret) return;

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

public void endOnHackerDetect() {
  winner = 2;
  println();
  println("Fin de la partie (hacker)");
  println();
  addPgnDraw();
  updateScores(0.5f);
  endReason = "détection du hacker";
  if (hackerSansFin) endReason += " sans fin";

  gameEnded = true;
  timeAtEnd = millis();
  infos = "Game ended";

  if (soundControl >= 1) nulle_sound.play();
  if (useTime) ta.stopTimers();
}

public void updateScores(float num) {
  if (joueurs.get(0).name != joueurs.get(1).name) {

    if (num == 0.5f) { //nulle
      joueurs.get(0).addScore(0.5f);
      joueurs.get(1).addScore(0.5f);
    } else { //num = gagnant
      joueurs.get((int)num).addScore(1);
    }
    joueurs.get(0).addTotalScore(1);
    joueurs.get(1).addTotalScore(1);

  } else {

    //si les deux joueurs sont les mêmes
    if (num == 0.5f) {
      joueurs.get(0).addScore(0.5f);
    } else {
      joueurs.get((int)num).addScore(1);
    }
    joueurs.get(0).addTotalScore(1);

  }
}
class Cell {
  float xNorm, yNorm;
  float x, y;
  int i, j;
  String name;
  boolean noir = false, blanc = false;
  boolean selected = false; //Pièce sur la case sélectionnée
  boolean moveMark = false; //Dernier déplacement de pièce
  boolean bookFrom = false;
  boolean bookTarget = false;
  boolean freeMove = false; //Mouvement n'importe où (ou presque)
  Piece piece = null;
  Move possibleMove = null;

  Cell(int i, int j, int x, int y) {
    this.i = i;
    this.j = j;
    this.xNorm = x;
    this.yNorm = y;
    this.piece = null;
    this.possibleMove = null;
    this.name = (char)(97+i) + String.valueOf(8 - j);

    if (j % 2 == 0) {
      if (i % 2 == 0) this.blanc = true;
      else this.noir = true;
    } else {
      if (i % 2 == 0) this.noir = true;
      else this.blanc = true;
    }
  }

  public void show() {
    noStroke();
    if (this.blanc) fill(0xfff0d9b5);
    else if (this.noir) fill(0xffb58863);

    if (pointDeVue) {
      this.x = this.xNorm;
      this.y = this.yNorm;
    } else {
      this.x = width - (this.xNorm + w - offsetX);
      this.y = height - (this.yNorm + w - offsetY);
    }

    rectMode(CORNER);
    rect(this.x, this.y, w, w);

    if (this.moveMark) {
      fill(209, 206, 25, 100);
      rect(this.x, this.y, w, w);
    }
    if (this.selected) {
      fill(189, 186, 34, 100);
      rect(this.x, this.y, w, w);
    }
    if (this.bookFrom) {
      fill(237, 217, 36, 150);
      rect(this.x, this.y, w, w);
    }
    if (this.bookTarget) {
      fill(224, 76, 56, 200);
      rect(this.x, this.y, w, w);
    }

    if (this.possibleMove != null && this.possibleMove.capture != null) {
      noFill();
      stroke(75, 75, 75, 100);
      strokeWeight(w/16);
      ellipse(this.x + w/2, this.y+w/2, w - w/16, w - w/16);
    } else if (this.possibleMove != null) {
      fill(75, 75, 75, 100);
      ellipse(this.x + w/2, this.y + w/2, w/4, w/4);
    }
  }

  public boolean contains(int x, int y) {
    if (x >= this.x && x < this.x + w && y >= this.y && y < this.y + w) {
      return true;
    } else {
      return false;
    }
  }

  public void removePiece() {
    this.piece = null;
  }
}
class Button {
  float x, y, w;
  PImage i1, i2;
  int numShortcut = -1;

  Button(float x, float y, float w, PImage i1, PImage i2) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.i1 = i1;
    this.i2 = i2;
  }

  public void setNumShortcut(int n) {
    this.numShortcut = n;
  }

  public void callShortcut() {
    if (this.numShortcut == -1) { println("Erreur initialisation shortcut dans bouton"); return; }
    sc.call(this.numShortcut);
  }

  public String getDescription() {
    return sc.getDescription(this.numShortcut);
  }

  public void show(int c) {
    fill(0);
    imageMode(CENTER);
    if (c == 0) {
      image(this.i1, this.x+w/2, this.y+w/2, this.w/1.1f, this.w/1.1f);
    } else if (c == 1) {
      image(this.i2, this.x+w/2, this.y+w/2, this.w/1.1f, this.w/1.1f);
    }
  }

  public boolean contains(int x, int y) {
    if (x >= this.x && x < this.x+w && y >= this.y && y < this.y+w) {
      return true;
    } else {
      return false;
    }
  }
}

class TimeButton {
  float x, y, w, h;
  int r1, r2, r3, r4;
  int background, arrowColor, hoveredColor;
  boolean facingUp;
  boolean hovered, pressed;
  int cooldownFastIncrement = 500, pressedAt;
  int i, j;

  TimeButton(float x, float y, float w, float h, int r1, int r2, int r3, int r4, int background, int arrowColor, int hoveredColor, boolean facing) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.r1 = r1;
    this.r2 = r2;
    this.r3 = r3;
    this.r4 = r4;
    this.background = background;
    this.arrowColor = arrowColor;
    this.hoveredColor = hoveredColor;
    this.facingUp = facing;
  }

  public void show() {
    if (!this.hovered) {
      fill(this.background);
      stroke(this.background);
    } else {
      fill(this.hoveredColor);
      stroke(this.hoveredColor);
    }
    strokeWeight(1);
    rect(this.x, this.y, this.w, this.h, this.r1, this.r2, this.r3, this.r4);

    strokeWeight(2);
    stroke(this.arrowColor);
    if (this.facingUp) {
      line(this.x + this.w/2, this.y + this.h/2 - 2, this.x + this.w/2 + 5, this.y + this.h/2 + 2);
      line(this.x + this.w/2, this.y + this.h/2 - 2, this.x + this.w/2 - 5, this.y + this.h/2 + 2);
    } else {
      line(this.x + this.w/2, this.y + this.h/2 + 2, this.x + this.w/2 + 5, this.y + this.h/2 - 2);
      line(this.x + this.w/2, this.y + this.h/2 + 2, this.x + this.w/2 - 5, this.y + this.h/2 - 2);
    }
  }

  public void update() {
    if (this.pressed && millis() - pressedAt >= this.cooldownFastIncrement) {
      if (frameCount % 2 == 0) this.updateAssignedTimer();
    }
  }

  public void setIndex(int i, int j) {
    this.i = i;
    this.j = j;
  }

  public void updateAssignedTimer() {
    times[this.i][this.j] += (this.facingUp ? 1 : -1);
    times[this.i][this.j] = constrain(times[this.i][this.j], 0, 60);
  }

  public void click() {
    this.pressed = true;
    this.pressedAt = millis();
    this.updateAssignedTimer();
  }

  public void release() {
    this.pressed = false;
  }

  public boolean contains(int x, int y) {
   return (x >= this.x && x < this.x+this.w && y >= this.y && y < this.y+this.h);
  }
}

class ImageButton {
  float x, y, w, h;
  int background, r;
  PImage img;

  ImageButton(float x, float y, float w, float h, int r, int background, PImage img) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.r = r;
    this.img = img;
    this.background = background;
  }

  public void show() {
    rectMode(CORNER);
    fill(this.background);
    stroke(this.background);
    rect(this.x, this.y, this.w, this.h, this.r, this.r, this.r, this.r);
    imageMode(CENTER);
    image(this.img, this.x+this.w/2, this.y+this.h/2, this.w/1.9f, this.h/1.9f);
  }

  public boolean contains(int x, int y) {
   return (x >= this.x && x < this.x+this.w && y >= this.y && y < this.y+this.h);
  }
}

class CircleToggleButton {
  float x, y, d;
  boolean state = false;

  CircleToggleButton(float x, float y, float d) {
    this.x = x;
    this.y = y;
    this.d = d;
  }

  public void show() {
    stroke(0);
    if (state) fill(0xffe4e4e4);
    else fill(0xff444141);
    circle(this.x, this.y, this.d);
  }

  public void toggle() {
    this.state = !this.state;
  }

  public boolean contains(int x, int y) {
    return (dist(x, y, this.x, this.y) <= this.d/2);
  }
}

class DragAndDrop {
  float x, y, w, h;
  int value;
  boolean lock = false;
  PImage img;

  DragAndDrop(float x, float y, float w, float h, PImage img, int value) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.value = value;
    this.img = img;
  }

  public void show() {
    imageMode(CENTER);
    if (this.lock) image(this.img, mouseX, mouseY, this.w, this.h);
    else image(this.img, this.x, this.y, this.w, this.h);

    //rectMode(CENTER); noFill(); stroke(0); strokeWeight(3);
    //rect(this.x, this.y, this.w, this.h);
  }

  public int getValue() {
    return this.value;
  }

  public void lockToMouse() {
    this.lock = true;
  }

  public void unlockMouse() {
    this.lock = false;
  }

  public boolean contains(int x, int y) {
    return (x >= this.x-this.w/2 && x < this.x+this.w/2 && y >= this.y-this.h/2 && y < this.y+this.h/2);
  }
}

class TextButton {
  float x, y, w, h;
  int backColor = color(0xff8da75a);
  int textColor = color(0xffffffff);
  String text;
  int textSize;
  int arrondi;

  TextButton(float x, float y, float w, float h, String t, int textSize, int arrondi) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.text = t;
    this.textSize = textSize;
    this.arrondi = arrondi;
  }

  public void setColors(int b, int t) {
    this.backColor = b;
    this.textColor = t;
  }

  public void show() {
    noStroke();
    fill(this.backColor);
    rectMode(CORNER);
    rect(this.x, this.y, this.w, this.h, this.arrondi);

    textAlign(CENTER, CENTER);
    textSize(this.textSize);
    fill(this.textColor);
    text(this.text, this.x + this.w/2, (this.y + this.h/2) - this.textSize/5 + 2);
  }

  public boolean contains(int x, int y) {
   return (x >= this.x && x < this.x+this.w && y >= this.y && y < this.y+this.h);
  }
}

class ToggleButton {
  float x, y, imgWidth;
  PImage img;
  boolean state = false;
  String name;

  ToggleButton(float x, float y, float imgWidth, PImage img, String n) {
    this.x = x;
    this.y = y;
    this.imgWidth = imgWidth;
    this.img = img;
    this.name = n;
  }

  public void show() {
    imageMode(CORNER);
    if (this.state) {
      image(this.img, this.x, this.y, this.imgWidth, this.imgWidth);
      image(mark, this.x, this.y, this.imgWidth, this.imgWidth);
    } else {
      image(this.img, this.x, this.y, this.imgWidth, this.imgWidth);
    }
  }

  public boolean contains(int x, int y) {
    if (x >= this.x && x < this.x+this.imgWidth && y >= this.y && y < this.y+imgWidth) {
      return true;
    } else {
      return false;
    }
  }
}

class ButtonFEN {
  float x, y, size;
  PImage img;
  String text;

  ButtonFEN(float x, float y, float size, PImage img, String text) {
    this.x = x;
    this.y = y;
    this.size = size;
    this.img = img;
    this.text = text;
  }

  public void show() {
    image(this.img, this.x, this.y, this.size, this.size);
    textAlign(CENTER, CENTER);
    textSize(18 * w/75);
    fill(0);
    text(this.text, this.x, this.y + this.size/2 + 10);
  }

  public boolean contains(float mx, float my) {
    return (mx > this.x - this.size/2 &&
            mx < this.x + this.size/2 &&
            my > this.y - this.size/2 &&
            my < this.y + this.size/2 + 23);
  }
}
/////////////////////////////////////////////////////////////////

int[] totalScores = {0, 0, 0, 0, 0, 0};
float[] scores = {0, 0, 0, 0, 0, 0};

class Joueur {
  String name, elo, title = "", victoryTitle, lastEval = "";
  int c, depth, index, maxDepth;
  boolean useIterativeDeepening;
  ArrayList<Float> evals = new ArrayList<Float>();

  IA player;

  Joueur(String n, int c, int d, int md, boolean useID) {
    this.name = n;
    this.c = c;
    this.depth = d;
    this.maxDepth = md;
    this.useIterativeDeepening = useID;

    if (name == "Antoine") {
      player = new Antoine(this.c);
      this.elo = "100";
      this.index = 1;
      this.victoryTitle = "Tu t'es fait mater !";

    } else if (name == "LeMaire") {
      player = new LeMaire(this.c, this.depth, this.maxDepth, this.useIterativeDeepening);
      this.elo = "3845";
      this.title = "GM";
      this.index = 3;
      this.victoryTitle = "Cmaire";

    } else if (name == "LesMoutons") {
      player = new LesMoutons(this.c, this.depth, this.maxDepth, this.useIterativeDeepening);
      this.elo = str(PApplet.parseInt(random(1300, 1500)));
      this.title = "Mouton";
      this.index = 5;
      this.victoryTitle = "YOU LOUSE";

    } else if (name == "Stockfish") {
      player = new Stockfish(this.c, this.depth, this.useIterativeDeepening);
      this.elo = "284";
      this.title = "Noob";
      this.index = 0;
      this.victoryTitle = "??!?";

    } else if (name == "Loic") {
      player = new Loic(this.c, this.depth, this.useIterativeDeepening);
      this.elo = "-142";
      this.title = "IM";
      this.index = 2;
      this.victoryTitle = "Tu t'es fait mater !";

    } else if (name == "Humain") {
      this.elo = "???";
      this.index = 4;
      this.victoryTitle = (this.c == 0) ? "Victoire des blancs" : "Victoire des noirs";
    }
  }

  public void play() {
    if (name == "Humain") return;
    player.play();
    // thread("playPlayer");
  }

  public float getScore() {
    return scores[this.index];
  }

  public void addScore(float add) {
    scores[this.index] += add;
  }

  public int getTotalScore() {
    return totalScores[this.index];
  }

  public void addTotalScore(int add) {
    totalScores[this.index] += add;
  }
}

// void playPlayer() {
//   joueurs.get(tourDeQui).player.play();
// }

/////////////////////////////////////////////////////////////////

class IA {
  int c, depth, maxQuietDepth;
  boolean useIterativeDeepening = false;

  float time;
  int depthSearched;
  int numPos, numQuiet;
  int numMoves, numCaptures, numQuietCuts, numTranspositions;
  int firstPlyMoves, higherPlyFromRoot;
  int[] cuts;  int cutsFirst;

  float Infinity = 999999999;
  Move bestMoveFound = null;

  public void play() {
    if (gameEnded || stopSearch) return;
    if (!MODE_SANS_AFFICHAGE) cursor(WAIT);

    // Recherche du meilleur coup
    float posEval;
    if (this.useIterativeDeepening) posEval = this.iterativeDeepening();
    else posEval = this.findBestMove();

    // Joue le coup
    this.bestMoveFound.play();

    // Affichage des statistiques dans la console et l'interface
    this.updateStats(posEval);

    // Reset les statistiques pour la prochaine recherche
    this.resetStats();

    // Reset les infos du hacker
    lastMoveTime = millis();
    deltaTimeMesured = 0;

    stopSearch = false;
    cursor(ARROW);
  }

  public float findBestMove() {
    this.depthSearched = floor(this.depth + CONSTANTE_DE_STOCKFISH * pow(endGameWeight, 5));
    this.cuts = new int[depthSearched];

    float timeBefore = millis();
    float posEval;
    sa.inNormalSearch = this.c;
    sa.setDepths(str(this.depthSearched), this.c);

    if (this instanceof LesMoutons) {
      SheepEval sheep = this.moyennemax(this.depthSearched, 0, -Infinity, Infinity, null);
      posEval = -sheep.eval;
    } else {
      posEval = this.minimax(this.depthSearched, 0, -Infinity, Infinity, null);
    }

    this.time = millis() - timeBefore;
    sa.inNormalSearch = -1;

    return posEval;
  }

  public float iterativeDeepening() {
    // sauvegardes
    Move lastBestMove = null;
    float lastEval = 0;
    int lastNumPos = 0, lastNumQuiet = 0, lastNumMoves = 0, lastNumCaptures = 0, lastNumQuietCuts = 0, lastNumTranspositions = 0;
    int lastFirstPlyMoves = 0, lastHigherPlyFromRoot = 0;
    int[] lastCuts = {0};  int lastCutsFirst = 0;

    // démarre la recherche sur search controller
    if (useHacker && hackerPret) {
      if (nbTour > 1) {
        int timeToPlay = constrain((deltaTimeMesured/2)-500, 20, sa.savedTimes[this.c]*3);
        sa.setTime(this.c, timeToPlay);
      } else sa.setTime(this.c, sa.savedTimes[this.c]);
    }
    if (MODE_PROBLEME) sa.setTime(this.c, 10000000);
    sa.startSearch(this.c);

    for (int d = 1; d < 1000; d++) {
      this.resetStats();
      this.cuts = new int[d];

      // effectue la recherche à la profondeur
      float eval;
      if (this instanceof LesMoutons) {
        SheepEval sheep = this.moyennemax(d, 0, -Infinity, Infinity, null);
        eval = -sheep.eval;
      } else {
        eval = -this.minimax(d, 0, -Infinity, Infinity, null);
      }

      // si la recherche a été interrompue par search controller
      if (stopSearch) {
        this.numQuiet = lastNumQuiet;
        this.numPos = lastNumPos;
        this.depthSearched = d-1;
        this.cuts = lastCuts;
        this.cutsFirst = lastCutsFirst;
        this.numMoves = lastNumMoves;
        this.numCaptures = lastNumCaptures;
        this.numPos = lastNumPos;
        this.numQuiet = lastNumQuiet;
        this.numQuietCuts = lastNumQuietCuts;
        this.firstPlyMoves = lastFirstPlyMoves;
        this.higherPlyFromRoot = lastHigherPlyFromRoot;
        this.numTranspositions = lastNumTranspositions;
        this.time = sa.getTime(this.c);
        this.bestMoveFound = lastBestMove;
        return -lastEval;
      }

      // sauvegarde les résultats et statistiques
      lastEval = eval;
      lastBestMove = this.bestMoveFound;
      lastNumQuiet = this.numQuiet;
      lastNumPos = this.numPos;
      lastCuts = this.cuts;
      lastCutsFirst = this.cutsFirst;
      lastNumMoves = this.numMoves;
      lastNumCaptures = this.numCaptures;
      lastNumPos = this.numPos;
      lastNumQuiet = this.numQuiet;
      lastNumQuietCuts = this.numQuietCuts;
      lastFirstPlyMoves = this.firstPlyMoves;
      lastHigherPlyFromRoot = this.higherPlyFromRoot;
      lastNumTranspositions = this.numTranspositions;

      float evalToDisplay = (this.c == 0) ? -eval : eval;
      sa.setDepths(str(d), this.c);
      if (this instanceof Loic) sa.setEvals(evalToStringLoic(evalToDisplay), this.c);
      else sa.setEvals(evalToStringMaire(evalToDisplay), this.c);
      sa.setBestMoves(getPGNString(this.bestMoveFound), this.c);
      sa.setPositions(formatInt(this.numPos), this.c);
      sa.setTris("...", this.c);
      sa.setTranspositions(formatInt(this.numTranspositions), this.c);
      sa.setTimeDisplays(str((int)sa.getTime(this.c)) + " ms", this.c);
      sa.resetDepthTracker(this.c);

      // si la valeur est un mat, arrête la recherche
      if (abs(eval) > 20000) {
        this.depthSearched = d;
        this.time = sa.getTime(this.c);
        sa.endSearch();
        delay(min(850, (int)this.time));
        return -eval;
      }
    }

    return 0;
  }

  public float minimax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) { return 0; }

  public SheepEval moyennemax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) { return new SheepEval(0, 0); }

  public boolean tryPlayingBookMove() {
    ArrayList<String> moves = getMovesFromFen(generateFEN());

    if (moves.size() > 0 && !gameEnded) {
      delay(250);
      this.bestMoveFound = playMoveFromBook(moves);
      if (stats) {
        println(joueurs.get(this.c).name + " : " + "Book");
      }
      sa.setEvals("Book", this.c);
      sa.setBestMoves(getPGNString(this.bestMoveFound), this.c);
      sa.setDepths("0", this.c);
      sa.setPositions("0", this.c);
      sa.setTris("0", this.c);
      sa.setTranspositions("0", this.c);
      joueurs.get(this.c).lastEval = "Book";
      joueurs.get(this.c).evals.add(0.00f);
      cursor(ARROW);
      return true;
    }
    return false;
  }

  // Évaluation
  public float Evaluation() { return 0; }

  public float EvaluationRelative() {
    float eval = this.Evaluation();
    if (tourDeQui == 0) {
      return eval;
    } else {
      return -eval;
    }
  }

  public int getManhattanDistanceBetweenKing() {
    int xDist = abs(rois[1].i - rois[0].i);
    int yDist = abs(rois[1].j - rois[0].j);
    return xDist + yDist;
  }

  public float getEndGameKingEval(int friendlyMaterial, int opponentMaterial, Piece friendlyKing, Piece enemyKing) {
    if (friendlyMaterial > opponentMaterial + 150) {
      // Formule pas du tout copiée d'internet : 4,7 * CMD + 1,6 * (14 - MD)
      float eval = ( 4.7f * pc.getDistanceFromCenter(enemyKing.i, enemyKing.j) + 1.6f * (14 - this.getManhattanDistanceBetweenKing()) );
      return eval * endGameWeight;
    } else {
      return -pc.getDistanceFromCenter(friendlyKing.i, friendlyKing.j) * endGameWeight;
    }
  }

  public float getKingSafetyEval(int friendly, int opponent) {
    int sign = (friendly == 0) ? -1 : 1;
    float penalite = 0;
    Piece roi = rois[friendly];

    // Bouclier de pions
    int pawnShieldCount = 0;
    for (int i = -1; i < 2; i++) {
      if (roi.j + sign < 0 || roi.j + sign >= 8) break;
      if (roi.i + i < 0 || roi.i + i >= 8) continue;
      if (grid[roi.i+i][roi.j + sign].piece != null && grid[roi.i+i][roi.j + sign].piece.c == friendly && grid[roi.i+i][roi.j + sign].piece.pieceIndex == PION_INDEX) {
        pawnShieldCount++;
      }
    }
    if (pawnShieldCount == 0) penalite += 100;
    if (pawnShieldCount == 1) penalite += 75;
    if (pawnShieldCount == 2) penalite += 10;
    if (pawnShieldCount == 3) penalite += 0;

    // Distance pièces-roi
    for (int n = 0; n < pieces[opponent].size(); n++) {
      Piece p = pieces[opponent].get(n);
      penalite += pc.getTropismDistance(p.i, p.j, roi.i, roi.j);
    }

    penalite *= materials[opponent];
    penalite /= 104000;
    return penalite * (1 - endGameWeight);
  }

  public ArrayList OrderMoves(ArrayList<Move> moves) {
    return moves;
  }

  // Statistiques
  public void updateStats(float posEval) {
    // Calculs des statistiques
    if (tourDeQui == 0) posEval = -posEval;
    if (abs(posEval) == 0) posEval = 0;

    float totalCuts = 0;
    for (int i = 0; i < this.cuts.length; i++) totalCuts += this.cuts[i];
    float tri = 0;
    if (totalCuts != 0) tri = (float)this.cutsFirst / totalCuts;

    // Calcul de la variante
    varianteArrows.clear();
    String varianteText = "";
    varianteText = varianteText + getPGNString(bestMoveFound) + " ";
    Move actualMove = bestMoveFound;

    while (actualMove.bestChild != null) {
      varianteText = varianteText + getPGNString(actualMove.bestChild) + " ";
      varianteArrows.add(new Arrow(actualMove.bestChild.fromI, actualMove.bestChild.fromJ, actualMove.bestChild.i, actualMove.bestChild.j));
      actualMove = actualMove.bestChild;
    }

    // Affichage des statistiques de la console
    if (stats) {
      print(joueurs.get(this.c).name + " : "
            + getPGNString(this.bestMoveFound) + ", "
            + posEval/100 + ", ");

      String timeText;
      if (this.time >= 1000) timeText = this.time/1000 + " s";
      else timeText = this.time + " ms";
      println(formatInt(this.numPos) + " positions analysées ("
            + formatInt(numTranspositions) + " transposition" + ( (numTranspositions > 1) ? "s) + " : ") + ")
            + formatInt(this.numQuiet) + " quiets en",
            timeText,
            "(Profondeur " + (this.depthSearched) + ")");
    }
    if (details) {
      print(formatInt(this.numMoves) + " coups générés (" + this.firstPlyMoves + "), " + formatInt(this.numCaptures) + " captures générées (m" + this.higherPlyFromRoot + "), [");
      for (int i = 0; i < this.cuts.length; i++) print(this.cuts[i] + (i < this.cuts.length-1 ? ", " : ""));
      println("] cuts alpha-bêta (" + tri + "), " + formatInt(this.numQuietCuts) + " quiets cuts");
    }
    if (stats) println();

    // Update le graphique, la valeur de l'évaluation et search controller
    this.updateSearchController(posEval, tri);
    if (this instanceof Loic) joueurs.get(this.c).lastEval = evalToStringLoic(posEval);
    else joueurs.get(this.c).lastEval = evalToStringMaire(posEval);
    joueurs.get(this.c).evals.add(posEval/100.0f);
  }

  public void updateSearchController(float posEval, float tri) {
    if (this instanceof Loic) sa.setEvals(evalToStringLoic(posEval), this.c);
    else sa.setEvals(evalToStringMaire(posEval), this.c);

    sa.setBestMoves(getPGNString(this.bestMoveFound), this.c);
    sa.setDepths(str(this.depthSearched), this.c);
    sa.setPositions(formatInt(this.numPos), this.c);
    sa.setTris(roundNumber(tri, 2), this.c);
    sa.setTranspositions(formatInt(this.numTranspositions), this.c);
    sa.setTimeDisplays(str((int)this.time) + " ms", this.c);
    sa.resetDepthTracker(this.c);
  }

  public void resetStats() {
    this.numQuiet = 0;
    this.numPos = 0;
    this.time = 0;
    this.depthSearched = 0;
    this.cuts = null;
    this.cutsFirst = 0;
    this.numMoves = 0;
    this.numCaptures = 0;
    this.numPos = 0;
    this.numQuiet = 0;
    this.numQuietCuts = 0;
    this.firstPlyMoves = 0;
    this.higherPlyFromRoot = 0;
    this.numTranspositions = 0;
  }
}

/////////////////////////////////////////////////////////////////

class Antoine extends IA {
  int c;

  Antoine(int c) {
    this.c = c;
  }

  public @Override
  void play() {
    // Génération des coups légaux
    ArrayList<Move> moves = new ArrayList<Move>();
    moves = generateAllLegalMoves(this.c, true, true);

    // Recherche du MEILLEUR coup
    if (moves.size() != 0) moves.get(floor(random(0, moves.size()))).play();

    // Stats
    float eval = random(-10, 10);

    sa.setEvals(evalToStringMaire(eval), this.c);
    sa.setDepths(str((int)random(-10, 10)), this.c);
    sa.setPositions(formatInt((int)random(10, 10000)), this.c);
    sa.setTris(roundNumber(random(0, 1), 2), this.c);
    sa.setTranspositions(formatInt((int)random(0, 10000)), this.c);
    sa.setTimeDisplays(str((int)random(10, 3000)) + " ms", this.c);

    joueurs.get(this.c).lastEval = roundNumber(eval, 3);
    joueurs.get(this.c).evals.add(eval);
  }
}

/////////////////////////////////////////////////////////////////

class LeMaire extends IA {

  LeMaire(int c, int d, int md, boolean useID) {
    this.c = c;
    this.depth = d;
    this.maxQuietDepth = md;
    this.useIterativeDeepening = useID;
  }

  public @Override
  void play() {
    if (!MODE_SANS_AFFICHAGE) cursor(WAIT);

    if (nbTour < 9) {
     if (this.tryPlayingBookMove()) return;
    }
    super.play();
  }

  public @Override
  ArrayList OrderMoves(ArrayList<Move> moves) {

    // Place le meilleur coup de la table de transposition en premier
    Move hashMove = tt.getBestMove(zobrist.hash);
    float defaultEval = -this.EvaluationRelative();

    for (int i = 0; i < moves.size(); i++) {
      Move m = moves.get(i);

      // hash move
      if (m.equals(hashMove)) {
        m.scoreGuess = 50000;
        continue;
      }

      if (m.capture != null) {
        m.scoreGuess = (m.capture.maireEval - m.piece.maireEval) + defaultEval;
        continue;
      }

      m.make();
      m.scoreGuess = -this.EvaluationRelative();
      m.unmake();

    }

    return selectionSortMoves(moves);
  }

  public @Override
  float Evaluation() {
    float[] Evals = {0, 0};

    for (int i = 0; i < 2; i++) {
      int opponent = (i == 0) ? 1 : 0;

      Evals[i] += materials[i];

      for (int j = 0; j < pieces[i].size(); j++) {
        Evals[i] += pieces[i].get(j).mairePosEval;
      }

      Evals[i] -= this.getKingSafetyEval(i, opponent);
      Evals[i] += this.getEndGameKingEval(materials[i], materials[opponent], rois[i], rois[opponent]);
    }

    return (Evals[0] - Evals[1]);
  }

  public @Override
  float minimax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) {

    // On arrête la recherche si la partie est terminée (au temps notamment)
    if (stopSearch || gameEnded) return 0;

    this.numPos++;

    // Regarde la position dans la table de transposition et récupère la valeur (ou pas)
    Entry entry = tt.Probe(zobrist.hash, plyFromRoot);
    if (entry != null && entry.depth >= depth) {
      this.numTranspositions++;

      // La valeur stockée est exacte
      if (entry.nodeType == EXACT) {
        if (plyFromRoot == 0) this.bestMoveFound = entry.bestMove;
        return entry.value;
      }

      // La valeur stockée est LOWERBOUND donc non complète, on ajuste alpha
      else if (entry.nodeType == LOWERBOUND) alpha = max(alpha, entry.value);

      // La valeur stockée est UPPERBOUND donc non complète, on ajuste beta
      else if (entry.nodeType == UPPERBOUND) beta = min(beta, entry.value);

      if (alpha >= beta) {
        if (plyFromRoot == 0) this.bestMoveFound = entry.bestMove;
        return entry.value; // si la valeur de la table a provoqué un élagage alpha ou beta
      }
    }

    // Détection des répétitions
    // On ne regarde que si la position est arrivée une fois, pour la rapidité (et éviter des bugs de transpositions)
    if (plyFromRoot != 0 && checkFastRepetition(zobrist.hash)) {
      // tt.Store(zobrist.hash, 0, null, depth, plyFromRoot, EXACT);
      return 0;
    }

    // Appelle la recherche de captures si on est arrivé à la profondeur demandée
    if (depth == 0) {
      return this.searchAllCaptures(alpha, beta, plyFromRoot);
    }

    // Génération et classement des coups
    ArrayList<Move> moves = generateAllLegalMoves(tourDeQui, true, true);
    moves = this.OrderMoves(moves);
    this.numMoves += moves.size();
    if (plyFromRoot == 0) this.firstPlyMoves += moves.size();
    if (plyFromRoot == 1) sa.incrementSearchTracker(this.c);

    // Détection des mats et pats
    if (moves.size() == 0) {
      if (playerInCheck(tourDeQui) == tourDeQui) {
        int mateScore = 50000 - plyFromRoot;
        return -mateScore;
      } else {
        return 0;
      }
    }

    Move bestMoveInPosition = null;
    byte nodeType = UPPERBOUND;

    // Algorithme Négamax
    for (int i = 0; i < moves.size(); i++) {
      moves.get(i).make();
      float evaluation = -this.minimax(depth-1, plyFromRoot+1, -beta, -alpha, moves.get(i));
      moves.get(i).unmake();

      // Élagage alpha-beta
      if (evaluation >= beta) {
        this.cuts[plyFromRoot]++;
        if (i == 0) this.cutsFirst++;
        tt.Store(zobrist.hash, beta, moves.get(i), depth, plyFromRoot, LOWERBOUND);
        return beta;
      }

      // Nouveau meilleur coup
      if (evaluation > alpha) {
        nodeType = EXACT;
        alpha = evaluation;

        bestMoveInPosition = moves.get(i);
        if (plyFromRoot == 0) this.bestMoveFound = moves.get(i);
        if (Cpere != null) Cpere.bestChild = moves.get(i);
      }
    }

    tt.Store(zobrist.hash, alpha, bestMoveInPosition, depth, plyFromRoot, nodeType);

    return alpha;
  }

  public float searchAllCaptures(float alpha, float beta, int plyFromRoot) {
    this.higherPlyFromRoot = max(this.higherPlyFromRoot, plyFromRoot);
    this.numQuiet ++;

    if (gameEnded) return 0;

    float evaluation = this.EvaluationRelative();
    if (evaluation >= beta) {
      return beta;
    }
    if (evaluation > alpha) {
      alpha = evaluation;
    }
    if (plyFromRoot >= this.maxQuietDepth) return evaluation;

    ArrayList<Move> moves = generateAllCaptures(tourDeQui, true);
    moves = this.OrderMoves(moves);
    this.numCaptures += moves.size();

    for (int i = 0; i < moves.size(); i++) {
      moves.get(i).make();
      evaluation = -this.searchAllCaptures(-beta, -alpha, plyFromRoot+1);
      moves.get(i).unmake();

      if (evaluation >= beta) {
        this.numQuietCuts++;
        return beta;
      }
      alpha = max(alpha, evaluation);
    }

    return alpha;
  }
}

/////////////////////////////////////////////////////////////////

class LesMoutons extends IA {

  LesMoutons(int c, int d, int md, boolean useID) {
    this.c = c;
    this.depth = d;
    this.maxQuietDepth = md;
    this.useIterativeDeepening = useID;
  }

  public @Override
  void play() {
    if (!MODE_SANS_AFFICHAGE) cursor(WAIT);
    if (nbTour < 5) {
     if (this.tryPlayingBookMove()) return;
    }
    super.play();
  }

  public @Override
  ArrayList OrderMoves(ArrayList<Move> moves) {

    // Place le meilleur coup de la table de transposition en premier
    Move hashMove = tt.getBestMove(zobrist.hash);

    for (int i = 0; i < moves.size(); i++) {
      Move m = moves.get(i);

      // hash move
      if (m.equals(hashMove)) m.scoreGuess += 10000;

      // captures
      if (m.capture != null) {
        int scoreGuess = (m.capture.maireEval - m.piece.maireEval);
        m.scoreGuess += scoreGuess;
      }

      // pièce vers le centre
      Piece p = m.piece;
      m.scoreGuess -= pc.getDistanceFromCenter(p.i, p.j);
    }

    return selectionSortMoves(moves);
  }

  public SheepEval EvaluationMouton() {
    float[] Evals = {0, 0};

    for (int i = 0; i < 2; i++) {
      int opponent = (i == 0) ? 1 : 0;

      Evals[i] += materials[i];

      for (int j = 0; j < pieces[i].size(); j++) {
        Evals[i] += pieces[i].get(j).mairePosEval;
      }

      Evals[i] -= this.getKingSafetyEval(i, opponent);
      Evals[i] += this.getEndGameKingEval(materials[i], materials[opponent], rois[i], rois[opponent]);
    }

    float evaluation = Evals[0] - Evals[1];
    return new SheepEval(evaluation, evaluation);
  }

  public SheepEval EvaluationMoutonRelative() {
    SheepEval evaluation = this.EvaluationMouton();
    if (tourDeQui == 0) {
      return evaluation;
    } else {
      return new SheepEval(-evaluation.moyenne, -evaluation.eval);
    }
  }

  public SheepEval moyennemax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) {

    if (stopSearch || gameEnded) return new SheepEval(0, 0);

    this.numPos++;

    if (plyFromRoot != 0 && checkFastRepetition(zobrist.hash)) return new SheepEval(0, 0);

    if (depth == 0) {
      return this.EvaluationMoutonRelative();
    }

    ArrayList<Move> moves = generateAllLegalMoves(tourDeQui, true, true);
    moves = this.OrderMoves(moves);
    this.numMoves += moves.size();
    if (plyFromRoot == 0) this.firstPlyMoves += moves.size();
    if (plyFromRoot == 1) sa.incrementSearchTracker(this.c);

    if (moves.size() == 0) {
      if (playerInCheck(tourDeQui) == tourDeQui) {
        int mateScore = 50000 - plyFromRoot;
        return new SheepEval(-mateScore, -mateScore);
      } else {
        return new SheepEval(0, 0);
      }
    }

    float moyenneOfPosition;
    if (tourDeQui == this.c) moyenneOfPosition = 0;
    else moyenneOfPosition = -Infinity;
    float bestMoyenneAtRoot = -Infinity;
    boolean isBestMoveCapture = false;
    Move alphaMateMove = null;

    for (int i = 0; i < moves.size(); i++) {
      moves.get(i).make();
      SheepEval sheep = this.moyennemax(depth-1, plyFromRoot+1, -beta, -alpha, moves.get(i));
      float evaluation = -sheep.eval;
      float moyenne = -sheep.moyenne;
      moves.get(i).unmake();

      if (tourDeQui == this.c) moyenneOfPosition += (moyenne - moyenneOfPosition) / (i+1);
      else moyenneOfPosition = max(alpha, moyenneOfPosition);

      // Élagage alpha-beta
      if (alpha >= beta) {
        this.cuts[plyFromRoot]++;
        if (i == 0) cutsFirst++;
        if (isBestMoveCapture) return new SheepEval(beta, beta);
        else return new SheepEval(moyenneOfPosition, beta);
      }

      // Recherche du meilleur coup (à la racine et dans l'arbre)
      if (evaluation > alpha) {
        alpha = evaluation;
        if (moves.get(i).capture != null) isBestMoveCapture = true;
        else isBestMoveCapture = false;

        if (plyFromRoot == 0 && alpha > 49900) alphaMateMove = moves.get(i);
      }

      if (plyFromRoot == 0) {
        if (moyenne > bestMoyenneAtRoot) {
          bestMoyenneAtRoot = moyenne;
          this.bestMoveFound = moves.get(i);
        }
        if (alphaMateMove != null) this.bestMoveFound = alphaMateMove;
      }

    }

    if (isBestMoveCapture) return new SheepEval(alpha, alpha);
    else return new SheepEval(moyenneOfPosition, alpha);
  }
}

class SheepEval {
  float moyenne;
  float eval;

  SheepEval(float moy, float beval) {
    this.moyenne = moy;
    this.eval = beval;
  }
}

public void arnaques() {
  int opponent = (int)pow(tourDeQui-1, 2);

  // Arnaque au temps
  if (joueurs.get(opponent).name == "LesMoutons") {
    if (ta.timers[tourDeQui].currentTime >= 45000 && random(1) <= 0.4f) {
      timeCount++;
      ta.timers[tourDeQui].removeTime(5000);
    }
  }

  // Apparition
  if (joueurs.get(opponent).name == "LesMoutons" && (int)nbTour == tourPourApparition && endGameWeight <= 0.5f) {
    int knights = 0;
    int cblanc_bishops = 0;
    int cnoir_bishops = 0;
    int rooks = 0;

    for (int i = 0; i < pieces[opponent].size(); i++) {
      if (pieces[opponent].get(i).pieceIndex == CAVALIER_INDEX) knights++;
      if (pieces[opponent].get(i).pieceIndex == TOUR_INDEX) rooks++;
      if (pieces[opponent].get(i).pieceIndex == FOU_INDEX) {
        if (grid[pieces[opponent].get(i).i][pieces[opponent].get(i).j].blanc) cblanc_bishops++;
        else cnoir_bishops++;
      }
    }

    int j = (int)-7*opponent + 7;
    float cacheY = (opponent == 0) ? offsetY+6*w : offsetY;
    int tourAdd = 20;

    appearCount++;
    messagesCount++;

    if (knights < 2) {
      if (grid[1][j].piece == null) { sendMoutonMessage(moutonMessages[(int)random(0, moutonMessages.length)], offsetX, cacheY, 1500); pieces[opponent].add(new Cavalier(1, j, opponent)); materials[opponent] += 320; tourPourApparition += tourAdd; return; }
      if (grid[6][j].piece == null) { sendMoutonMessage(moutonMessages[(int)random(0, moutonMessages.length)], offsetX+2*w, cacheY, 1500); pieces[opponent].add(new Cavalier(6, j, opponent)); materials[opponent] += 320; tourPourApparition += tourAdd; return; }
    }
    if (cblanc_bishops < 1) {
      if (opponent == 0) {
        if (grid[5][j].piece == null) { sendMoutonMessage(moutonMessages[(int)random(0, moutonMessages.length)], offsetX, cacheY, 1500); pieces[opponent].add(new Fou(5, j, opponent)); materials[opponent] += 330; tourPourApparition += tourAdd; return; }
      } else {
        if (grid[2][j].piece == null) { sendMoutonMessage(moutonMessages[(int)random(0, moutonMessages.length)], offsetX, cacheY, 1500); pieces[opponent].add(new Fou(2, j, opponent)); materials[opponent] += 330; tourPourApparition += tourAdd; return; }
      }
    }
    if (cnoir_bishops < 1) {
      if (opponent == 0) {
        if (grid[2][j].piece == null) { sendMoutonMessage(moutonMessages[(int)random(0, moutonMessages.length)], offsetX, cacheY, 1500); pieces[opponent].add(new Fou(2, j, opponent)); materials[opponent] += 330; tourPourApparition += tourAdd; return; }
      } else {
        if (grid[5][j].piece == null) { sendMoutonMessage(moutonMessages[(int)random(0, moutonMessages.length)], offsetX, cacheY, 1500); pieces[opponent].add(new Fou(5, j, opponent)); materials[opponent] += 330; tourPourApparition += tourAdd; return; }
      }
    }
    if (rois[opponent].roquable == 1 && rooks < 2) {
      if (grid[0][j].piece == null) { sendMoutonMessage(moutonMessages[(int)random(0, moutonMessages.length)], offsetX, cacheY, 1500); pieces[opponent].add(new Tour(0, j, opponent)); materials[opponent] += 500; tourPourApparition += tourAdd; return; }
      if (grid[7][j].piece == null) { sendMoutonMessage(moutonMessages[(int)random(0, moutonMessages.length)], offsetX+2*w, cacheY, 1500); pieces[opponent].add(new Tour(7, j, opponent)); materials[opponent] += 500; tourPourApparition += tourAdd; return; }
    }

    messagesCount--;
    appearCount--;

    tourPourApparition += 2;
  }

  // Messages
  if (random(1) <= 0.2f) {
    float msgX = offsetX + random(0, 2)*w;
    float msgY = offsetX + random(0, 6)*w;
    sendMoutonMessage(moutonMessages[(int)random(0, moutonMessages.length)], msgX, msgY, 1500);
    messagesCount++;
  }

  // Missclick
  if (nbTour >= lastMissclick + missclickCooldown && random(1) <= 0.1f) {
    lastMissclick = nbTour;
    missclickDragNextMove = true;
  }
}

/////////////////////////////////////////////////////////////////

class Loic extends IA {

  Loic(int c, int d, boolean useID) {
    this.c = c;
    this.depth = d;
    this.useIterativeDeepening = useID;
  }

  public int countMaterial(int c) {
    int material = 0;
    for (int i = 0; i < pieces[c].size(); i++) {
      material += pieces[c].get(i).loicEval;
    }
    return material;
  }

  public @Override
  ArrayList OrderMoves(ArrayList<Move> moves) {
    for (int i = 0; i < moves.size(); i++) {
      Move m = moves.get(i);

      if (m.capture != null) {
        m.scoreGuess += 10*(m.capture.loicEval - m.piece.loicEval);;
      }
    }

    return selectionSortMoves(moves);
  }

  public @Override
  float Evaluation() {
    float[] Evals = {0, 0};

    for (int i = 0; i < 2; i++) {
      Evals[i] += this.countMaterial(i); //matériel

      for (int j = 0; j < pieces[i].size(); j++) {
        Evals[i] += 2 * pieces[i].get(j).loicPosEval; //positionnel
      }
    }

    return (Evals[0] - Evals[1]);
  }

  public @Override
  float minimax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) {

    if (stopSearch || gameEnded) return 0;

    this.numPos++;

    if (depth == 0) {
      return this.EvaluationRelative();
    }

    ArrayList<Move> moves = generateAllLegalMoves(tourDeQui, true, true);
    moves = this.OrderMoves(moves);
    if (plyFromRoot == 0) this.firstPlyMoves += moves.size();
    if (plyFromRoot == 1) sa.incrementSearchTracker(this.c);

    if (moves.size() == 0) {
      if (playerInCheck(tourDeQui) == tourDeQui) {
        int mateScore = 25000 - plyFromRoot;
        return -mateScore;
        //return 0;
      } else {
        int patScore = 50000 - plyFromRoot;
        return -patScore;
      }
    }

    // Répétition
    if (plyFromRoot == 1 && checkRepetition(zobrist.hash)) {
     return 0;
    }

    float bestEval = -Infinity;

    for (int i = 0; i < moves.size(); i++) {
      moves.get(i).make();
      float evaluation = -this.minimax(depth-1, plyFromRoot+1, -beta, -alpha, null);
      moves.get(i).unmake();

      if (evaluation >= beta) { // alpha-beta pruning
        this.cuts[plyFromRoot]++;
        if (i == 0) this.cutsFirst++;
        return beta;
      }

      if (plyFromRoot == 0) {
        if (evaluation > bestEval) { // nouveau  "meilleur"  coup
          bestEval = evaluation;
          this.bestMoveFound = moves.get(i);
        }
      }

      alpha = max(alpha, evaluation);
    }

    return alpha;
  }
}

/////////////////////////////////////////////////////////////////

class Stockfish extends IA {

  Stockfish(int c, int d, boolean useID) {
    this.c = c;
    this.depth = d;
    this.useIterativeDeepening = useID;
  }

  public @Override
  float Evaluation() {
    float[] Evals = {0, 0};

    for (int i = 0; i < 2; i++) {
      Evals[i] += materials[i];
      for (int j = 0; j < pieces[i].size(); j++) {
        Evals[i] += pieces[i].get(j).mairePosEval;
      }
    }

    return (Evals[0] - Evals[1]);
  }

  public @Override
  float minimax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) {

    if (stopSearch || gameEnded) return 0;

    this.numPos++;

    if (depth == 0) {
      return this.EvaluationRelative();
    }

    ArrayList<Move> moves = generateAllLegalMoves(tourDeQui, true, true);
    moves = this.OrderMoves(moves);
    if (plyFromRoot == 0) this.firstPlyMoves += moves.size();
    if (plyFromRoot == 1) sa.incrementSearchTracker(this.c);

    if (moves.size() == 0) {
      if (playerInCheck(tourDeQui) == tourDeQui) {
        int mateScore = 50000 - plyFromRoot;
        return -mateScore;
      } else {
        return 0;
      }
    }

    // Répétition
    if (plyFromRoot == 1 && checkRepetition(zobrist.hash)) {
     return 0;
    }

    float worstEval = Infinity;

    for (int i = 0; i < moves.size(); i++) {
      moves.get(i).make();
      float evaluation = -this.minimax(depth-1, plyFromRoot+1, -beta, -alpha, null);
      moves.get(i).unmake();

      if (evaluation >= beta) { // alpha-beta pruning
        this.cuts[plyFromRoot]++;
        if (i == 0) this.cutsFirst++;
        return beta;
      }

      if (plyFromRoot == 0) {
        if (evaluation < worstEval) { //nouveau  "meilleur"  coup
          worstEval = evaluation;
          this.bestMoveFound = moves.get(i);
        }
      } else {
        alpha = max(alpha, evaluation);
      }
    }

    if (plyFromRoot == 0) return worstEval;
    return alpha;
  }
}

/////////////////////////////////////////////////////////////////

//Fonctions test perft
public int searchMoves(int depth) {

  if (depth == 0) {
    return 1;
  }

  ArrayList<Move> moves = generateAllLegalMoves(tourDeQui, true, true);

  int numPos = 0;

  for (int i = 0; i < moves.size(); i++) {
    moves.get(i).make();
    numPos += searchMoves(depth - 1);
    moves.get(i).unmake();
  }

  // tt.Store(zobrist.hash, 1, mTest, 6, 2, EXACT);

  return numPos;
}

public int searchCaptures(int depth) {

  if (depth == 0) {
    return 1;
  }

  ArrayList<Move> moves = generateAllCaptures(tourDeQui, true);

  int numPos = 0;

  for (int i = 0; i < moves.size(); i++) {
    moves.get(i).make();
    numPos += searchCaptures(depth - 1);
    moves.get(i).unmake();
  }

  return numPos;
}

/////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////

// Move class

// Constructeur : Piece, targetI, targetJ, Capture, Special

// Play et Unplay : Jouer (vraiment) le coup
// Make et Unmake : Jouer (prévisualiser) le coup
// Handle et Unhandle : Commun aux deux

// 1 = petit roque; 2 = grand roque;  3 = en passant; 4 = promotion
// 5 = dame; 6 = tour; 7 = fou; 8 = cavalier

/////////////////////////////////////////////////////////////////

int[] promoMaterials = {800, 400, 230, 220};

class Move {
  Piece piece;
  Piece capture;
  byte fromI, fromJ, i, j, special;

  // Meilleur coup après
  Move bestChild = null;

  // Probabilité bon coup
  float scoreGuess = 0;

  // Sauvegardes pour make et unmake
  int saveRoque, savePRoque, saveGRoque;
  Piece[] saveEnPassantArray = new Piece[2];
  Piece savePromo = null;
  Piece tourQuiRoque = null;

  Move(Piece piece, int i, int j, Piece capture, int special) {
    this.piece = piece;
    this.i = PApplet.parseByte(i);
    this.j = PApplet.parseByte(j);
    this.fromI = PApplet.parseByte(piece.i);
    this.fromJ = PApplet.parseByte(piece.j);
    this.capture = capture;
    this.special = PApplet.parseByte(special);

    if (special == 1) this.tourQuiRoque = grid[this.i+1][this.j].piece;
    else if (special == 2) this.tourQuiRoque = grid[this.i-2][this.j].piece;
  }

  public String stringify() {
    return str(this.fromI) + str(this.fromJ) + str(this.i) + str(this.j);
  }

  public void log() {
    print(this.piece.type + "->" + grid[this.i][this.j].name + " (" + this.special + ") + " + this.scoreGuess + " | ");
  }

  public boolean equals(Move m2) {
    if (m2 == null) return false;
    return (this.piece == m2.piece && this.fromI == m2.fromI && this.fromJ == m2.fromJ && this.i == m2.i && this.j == m2.j && this.capture == m2.capture && this.special == m2.special);
  }

  public void savePieceData() {
    this.saveRoque = this.piece.roquable;
    this.savePRoque = this.piece.petitRoquable;
    this.saveGRoque = this.piece.grandRoquable;
    saveEnPassantArray[0] = currentEnPassantable[0];
    saveEnPassantArray[1] = currentEnPassantable[1];
    this.savePromo = null;
  }

  public void handle(boolean really) {
    // Sauvegardes
    this.savePieceData();

    // En passant
    if (this.piece.pieceIndex == PION_INDEX && this.piece.j + ( (this.piece.c == 0) ? -2 : 2 ) == this.j) {
     currentEnPassantable[this.piece.c] = this.piece;
    }

    // Déplacement et capture
    this.piece.move(this);

    // Coups spéciaux
    if (really && this.special == 4) enPromotion = this.piece;
    if (this.special == 1) { tourQuiRoque.setPlace(this.i-1, this.j); }
    else if (this.special == 2) { tourQuiRoque.setPlace(this.i+1, this.j); }
    else if (this.special >= 5) {
      removePiece(this.piece);
      if (this.special == 5) this.savePromo = new Dame(this.i, this.j, this.piece.c);
      if (this.special == 6) this.savePromo = new Tour(this.i, this.j, this.piece.c);
      if (this.special == 7) this.savePromo = new Fou(this.i, this.j, this.piece.c);
      if (this.special == 8) this.savePromo = new Cavalier(this.i, this.j, this.piece.c);
      pieces[this.piece.c].add(this.savePromo);
      materials[this.piece.c] += promoMaterials[this.special-5];
    }

    // Roques
    if (this.piece.roquable != -1) this.piece.roquable = 0;
    else if (this.piece.petitRoquable != -1) this.piece.petitRoquable = this.piece.grandRoquable = 0;

    // Enlève le pion en passant de l'adversaire
    int opp = (this.piece.c == 0) ? 1 : 0;
    if (currentEnPassantable[opp] != null) {
      currentEnPassantable[opp] = null;
    }

    // Variables
    if (really) { if (enPromotion == null) tourDeQui = (tourDeQui == 0) ? 1 : 0; }
    else { tourDeQui = (tourDeQui == 0) ? 1 : 0; }
    nbTour += 0.5f;
    if (this.capture != null) { calcEndGameWeight(); materials[this.capture.c] -= this.capture.maireEval; }

    zobrist.updateHash(this);
    addHashToHistory(zobrist.hash);
  }

  public void unhandle(boolean really) {
    //Retour des sauvegardes
    if (this.saveRoque != -1) this.piece.roquable = this.saveRoque;
    if (this.savePRoque != -1) this.piece.petitRoquable = this.savePRoque;
    if (this.saveGRoque != -1) this.piece.grandRoquable = this.saveGRoque;
    currentEnPassantable[0] = saveEnPassantArray[0];
    currentEnPassantable[1] = saveEnPassantArray[1];

    // Coups spéciaux
    if (this.special == 1) { Piece p = grid[this.i-1][this.j].piece; p.setPlace(this.i+1, this.j); }
    else if (this.special == 2) { Piece p = grid[this.i+1][this.j].piece; p.setPlace(this.i-2, this.j); }
    else if (this.special >= 5) {
      pieces[this.piece.c].add(this.piece);
      removePiece(this.savePromo);
      materials[this.piece.c] -= promoMaterials[this.special-5];
    }

    // Déplacement de la pièce
    this.piece.setPlace(this.fromI, this.fromJ);

    // Update les variables
    tourDeQui = (tourDeQui == 0) ? 1 : 0;
    nbTour -= 0.5f;

    // Update d'autres variables et réssucite la pièce capturée
    if (this.capture != null) {
      pieces[this.capture.c].add(this.capture);
      grid[this.capture.i][this.capture.j].piece = this.capture;

      calcEndGameWeight();
      materials[this.capture.c] += this.capture.maireEval;
    }

    zobrist.updateHash(this);
    removeLastFromHashHistory(); //retire le dernier hash de l'historique
  }

  public void play() {
    this.handle(true);

    // Update des pièces du plateau
    piecesToDisplay.clear();
    piecesToDisplay.addAll(pieces[0]);
    piecesToDisplay.addAll(pieces[1]);

    // Fonctions très utiles (ou pas)
    deselectAll();
    updatePGN(this);
    checkGameState();
    playSound(this);
    clearBookHighlight();

    // Move marks
    setMoveMarks(grid[this.fromI][this.fromJ], grid[this.i][this.j]);

    // Historiques
    addFenToHistory(generateFEN());
    movesHistory.add(this);

    // Divers et variés
    if (useTime && !gameEnded) ta.switchTimers(tourDeQui);
    if (showGraph) updateGraph();

    // Hacker
    if (useHacker && hackerPret && !hackerAPImode && !isNextMoveRestranscrit) {
      cheat(this.piece.c, this.fromI, this.fromJ, this.i, this.j, this.special);
    }
    isNextMoveRestranscrit = false;

    // Les Moutons !
    if (ENABLE_ARNAQUES && !useHacker && (joueurs.get(0).name == "LesMoutons" || joueurs.get(1).name == "LesMoutons")) {
      arnaques();
    }

    // Efface la table de transposition
    // On le fait à chaque coup (ou presque) pour éviter des conflits à propos de la table quand deux maires jouent ensemble, ou quand les moutons interviennent...
    //if (!((joueurs.get(0).name == "LeMaire" && joueurs.get(1).name == "Humain") || (joueurs.get(0).name == "Humain" && joueurs.get(1).name == "LeMaire"))) {
    tt.clear();
    //}
  }

  public void replay() {
    this.handle(true);

    //Update des pièces du plateau
    piecesToDisplay.clear();
    piecesToDisplay.addAll(pieces[0]);
    piecesToDisplay.addAll(pieces[1]);

    //Fonctions très utiles (ou pas)
    deselectAll();
    playSound(this);
    clearBookHighlight();

    //Move marks
    setMoveMarks(grid[this.fromI][this.fromJ], grid[this.i][this.j]);
  }

  public void unplay() {
    this.unhandle(true);

    piecesToDisplay.clear();
    piecesToDisplay.addAll(pieces[0]);
    piecesToDisplay.addAll(pieces[1]);

    deselectAll();
    playSound(this);
    clearBookHighlight();

    clearMoveMarks();
  }

  public void make() {
    this.handle(false);
  }

  public void unmake() {
    this.unhandle(false);
  }

}

/////////////////////////////////////////////////////////////////

// Génération des coups

public ArrayList<Move> generateAllMoves(int c, boolean withCastle, boolean engine) {

  ArrayList<Move> moves = new ArrayList<Move>();
  ArrayList<Piece> piecesToCheck = copyPieceArrayList(pieces[c]); //crée une *nouvelle instance* d'arraylist avec les pièces dans l'ordre (qui ne change pas dans la suite)

  for (int i = 0; i < piecesToCheck.size(); i++) {
    moves.addAll(piecesToCheck.get(i).generateMoves(withCastle, engine));
  }

  return moves;
}

public ArrayList<Move> generateAllLegalMoves(int c, boolean withCastle, boolean engine) {
  ArrayList<Move> moves = new ArrayList<Move>();
  ArrayList<Piece> piecesToCheck = copyPieceArrayList(pieces[c]);

  for (int i = 0; i < piecesToCheck.size(); i++) {
    moves.addAll(piecesToCheck.get(i).generateLegalMoves(withCastle, engine));
  }

  return moves;
}

public ArrayList<Move> generateAllCaptures(int c, boolean engine) {
  ArrayList<Move> moves = new ArrayList<Move>();
  ArrayList<Piece> piecesToCheck = copyPieceArrayList(pieces[c]);

  for (int i = 0; i < piecesToCheck.size(); i++) {
    moves.addAll(piecesToCheck.get(i).generateQuietLegalMoves(engine));
  }

  return moves;
}
public String getPGNString(Move m) {
  String movePgn = "";
  char ambichar = ' '; //ambichar = char pour ambiguités dans la pgn

  //ambiguités
  ArrayList<Piece> doubles = detectMoveDoubles(m);
  if (doubles.size() == 1) { //pour l'instant (pour simplifier), 1 seul doublon est pris en compte
    Piece doublon = doubles.get(0);
    if (doublon.i == m.fromI) { //même colonne
      ambichar = grid[m.fromI][m.fromJ].name.charAt(1);
    } else { //même ligne ou aucun des deux
      ambichar = grid[m.fromI][m.fromJ].name.charAt(0);
    }
  }

  //encodage du coup
  if (m.special == 0) {
    if (m.piece.type != "pion") {
      if (ambichar == ' ') movePgn = movePgn + (m.piece.code.toUpperCase() + ((m.capture != null) ? "x" : "") + grid[m.i][m.j].name);
      else movePgn = movePgn + (m.piece.code.toUpperCase() + ambichar + ((m.capture != null) ? "x" : "") + grid[m.i][m.j].name);
    } else {
      movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name);
    }
  } else if (m.special == 1) {
    movePgn = movePgn + "O-O";
  } else if (m.special == 2) {
    movePgn = movePgn + "O-O-O";
  } else if (m.special == 4) { //promotion humain, complétée dans events
    movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name + "=");
  } else if (m.special == 5) {
    movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name + "=Q");
  } else if (m.special == 6) {
    movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name + "=R");
  } else if (m.special == 7) {
    movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name + "=B");
  } else if (m.special == 8) {
    movePgn = movePgn + ( ((m.capture != null) ? (grid[m.fromI][m.fromJ].name.charAt(0) + "x") : "") + grid[m.i][m.j].name + "=N");
  }

  return movePgn;
}

public void updatePGN(Move m) {
  String movePgn = "";
  if (tourDeQui == 1) movePgn = (int)nbTour + ".";

  movePgn = movePgn + getPGNString(m);

  movePgn = movePgn + " ";
  pgn = pgn + movePgn;
}

public ArrayList detectMoveDoubles(Move m) {
  Piece p = m.piece;
  ArrayList<Piece> pieces = new ArrayList<Piece>();
  // uniquement "captures" de la même couleur que la pièce, et attention, ne marche pas pour les pions
  ArrayList<Move> friendlyMoves = getFriendlyMoves(p);

  for (int i = 0; i < friendlyMoves.size(); i++) {
    Move move = friendlyMoves.get(i);
    if (grid[move.i][move.j].piece.type == m.piece.type) {
      pieces.add(grid[move.i][move.j].piece);
    }
  }

  return pieces;
}

public ArrayList getFriendlyMoves(Piece p) {
  ArrayList<Move> moves = new ArrayList<Move>();
  int oppositeColor = (int)pow(p.c - 1, 2);

  switch (p.type) {
    case "cavalier":
      moves = getQuietKnightMoves(p, oppositeColor);
    break;

    case "fou":
      moves = getQuietBishopMoves(p, oppositeColor);
    break;

    case "tour":
      moves = getQuietRookMoves(p, oppositeColor);
    break;

    case "dame":
      moves = getQuietRookMoves(p, oppositeColor);
      moves.addAll(getQuietBishopMoves(p, oppositeColor));
    break;

    case "roi":
      moves = getQuietKingMoves(p, oppositeColor);
    break;
  }

  return moves;
}

public void addPgnChar(String s) {
  pgn = pgn.substring(0, pgn.length()-1); //remove last char (space)
  pgn = pgn + s + " ";
}

public void addPgnCheck() {
   pgn = pgn.substring(0, pgn.length()-1);
   pgn = pgn + "+ ";
}

public void addPgnMate(int c) {
   pgn = pgn.substring(0, pgn.length()-1);
   pgn = pgn + "# ";
   addPgnWin(c);
}

public void addPgnWin(int c) {
  pgn = pgn + ( (c == 0) ? "1-0" : "0-1");
}

public void addPgnDraw() {
  pgn = pgn + "1/2-1/2";
}

//////////////////////////////////////////////////////////////////////

public boolean isAtPawnCaptureDist(Cell c1, Cell c2, int c) {
  //c1 pour départ, c2 pour arrivée

  int iDistAbs = abs(c2.i-c1.i);
  int jDist = c2.j-c1.j;

  if (c == 0) { //pion blanc
    return (iDistAbs == 1 && jDist == -1);
  } else if (c == 1) { //pion noir
    return (iDistAbs == 1 && jDist == 1);
  } else { //erreur
    return false;
  }
}

public boolean isAtDiagDist(Cell c1, Cell c2) {
  int iDist = abs(c2.i-c1.i);
  int jDist = abs(c2.j-c1.j);
  return ((iDist == 1) && (jDist == 1));
}

public boolean isAtKingDist(Cell c1, Cell c2) {
  int iDist = abs(c2.i-c1.i);
  int jDist = abs(c2.j-c1.j);
  return ((iDist <= 1) && (jDist <= 1));
}

public boolean isAtKnightDist(Cell c1, Cell c2) {
  int iDist = abs(c2.i-c1.i);
  int jDist = abs(c2.j-c1.j);
  return ((iDist == 1 && jDist == 2) || (iDist == 2 && jDist == 1));
}

public boolean isAtBishopDist(Cell c1, Cell c2) {
  int iDist = abs(c2.i-c1.i);
  int jDist = abs(c2.j-c1.j);
  return (iDist == jDist);
}

public boolean isAtRookDist(Cell c1, Cell c2) {
  int iDist = abs(c2.i-c1.i);
  int jDist = abs(c2.j-c1.j);
  return ((iDist == 0 && jDist > 0)|| (iDist > 0 && jDist == 0));
}

public boolean isAtQueenDist(Cell c1, Cell c2) {
  return (isAtBishopDist(c1, c2) || isAtRookDist(c1, c2));
}

public boolean isAtPieceDist(Cell c1, Cell c2, String type) {
  switch (type) {
    case "cavalier": return (isAtKnightDist(c1, c2));
    case "fou": return (isAtBishopDist(c1, c2));
    case "dame": return (isAtQueenDist(c1, c2));
    case "tour": return (isAtRookDist(c1, c2));
    default: println("ERREUR DANS isAtPieceDist() : TYPE INVALIDE"); return false;
  }
}

public boolean canBePieceMove(Piece p, int i, int j) {
  ArrayList<Move> moves = p.generateLegalMoves(false, false);
  for (int n = 0; n < moves.size(); n++) {
    if (moves.get(n).i == i && moves.get(n).j == j) return true;
  }
  return false;
}

//////////////////////////////////////////////////////////////////////

// Lecture de pgns

// Sans promotion et sans fin de partie
public void playPgn(String pgn, int limit) {
  String pgnForOneMove[] = pgn.split(" ");

  if (limit == -1) limit = pgnForOneMove.length;

  String pieceType;
  int promotion = 0;
  int colonneRequise;
  int ligneRequise;
  boolean capture;
  boolean firstPly = (tourDeQui == 0) ? false : true; //premier coup du tour, aux blancs de jouer (false car on inverse après)

  for (int i = 0; i < limit; i++) {
    String word = pgnForOneMove[i];
    colonneRequise = -1;
    ligneRequise = -1;
    promotion = 0;
    capture = false;
    firstPly = !firstPly;

    if (Character.isLetter(word.charAt(0))) { //Le mot est un coup
      char c = word.charAt(0);

      //Recherche de la pièce
      if (c == 'O') { getRoquePgn(word, firstPly).play(); continue; } //roques
      else if (c == 'K') { pieceType = "roi"; word = word.substring(1, word.length()); }
      else if (c == 'Q') { pieceType = "dame"; word = word.substring(1, word.length()); }
      else if (c == 'B') { pieceType = "fou"; word = word.substring(1, word.length()); }
      else if (c == 'N') { pieceType = "cavalier"; word = word.substring(1, word.length()); }
      else if (c == 'R') { pieceType = "tour"; word = word.substring(1, word.length()); }
      else pieceType = "pion";

      //Recherche d'un prérequis
      char c1 = word.charAt(0);
      char c2 = ' ', c3 = ' ';
      if (word.length() >= 2) c2 = word.charAt(1);
      if (word.length() >= 3) c3 = word.charAt(2);

      if (isAtoH(c1) && (c2 == 'x' || isAtoH(c2))) {
        colonneRequise = letterToNum(c1);
        word = word.substring(1, word.length());
      }
      else if (is1to8(c1) && (c2 == 'x' || isAtoH(c2))) {
        ligneRequise = pgnNumToNum(c1);
        word = word.substring(1, word.length());
      }
      else if (isAtoH(c1) && is1to8(c2) && (c3 == 'x' || isAtoH(c3))) {
        colonneRequise = letterToNum(c1);
        ligneRequise = pgnNumToNum(c3);
        word = word.substring(2, word.length());
      }

      //Capture ou non
      if (word.charAt(0) == 'x') {
        capture = true;
        word = word.substring(1, word.length());
      }

      //Promotion ou non
      if (word.length() >= 4) {
        if (word.charAt(2) == '=') {
          if (word.charAt(3) == 'Q') promotion = 5;
          if (word.charAt(3) == 'R') promotion = 6;
          if (word.charAt(3) == 'B') promotion = 7;
          if (word.charAt(3) == 'N') promotion = 8;
        }
      }

      //Génération du coup
      if (pieceType != "pion") {

        int targetI = letterToNum(word.charAt(0));
        int targetJ = pgnNumToNum(word.charAt(1));
        Piece p = getPiecePgn(pieceType, ((firstPly) ? 0 : 1), targetI, targetJ, colonneRequise, ligneRequise);
        if (capture) { Move m = new Move(p, targetI, targetJ, grid[targetI][targetJ].piece, 0); m.play(); }
        else { Move m = new Move(p, targetI, targetJ, null, 0); m.play(); }

      } else {
        int targetI = letterToNum(word.charAt(0));
        int targetJ = pgnNumToNum(word.charAt(1));

        if (firstPly) { //blancs
          int fromI = (capture) ? colonneRequise : targetI;
          int fromJ = targetJ+1;
          if (targetJ == 4) {
            if (grid[fromI][5].piece != null) fromJ = 5;
            else fromJ = 6;
          }
          if (capture) {
            Piece p;
            boolean enPassant = false;
            if (grid[targetI][targetJ].piece == null) { p = grid[targetI][targetJ+1].piece; enPassant = true; }
            else p = grid[targetI][targetJ].piece;
            Move m = new Move(grid[fromI][fromJ].piece, targetI, targetJ, p, (enPassant) ? 3 : promotion); m.play(); continue;
          }
          else {
            Move m = new Move(grid[fromI][fromJ].piece, targetI, targetJ, null, promotion); m.play(); continue;
          }

        } else { //noirs
          int fromI = (capture) ? colonneRequise : targetI;
          int fromJ = targetJ-1;
          if (targetJ == 3) {
            if (grid[fromI][2].piece != null) fromJ = 2;
            else fromJ = 1;
          }
          if (capture) {
            Piece p;
            boolean enPassant = false;
            if (grid[targetI][targetJ].piece == null) { p = grid[targetI][targetJ-1].piece; enPassant = true; }
            else p = grid[targetI][targetJ].piece;
            Move m = new Move(grid[fromI][fromJ].piece, targetI, targetJ, p, (enPassant) ? 3 : promotion); m.play(); continue;
          }
          else {
            Move m = new Move(grid[fromI][fromJ].piece, targetI, targetJ, null, promotion); m.play(); continue;
          }
        }

      }
    }

  }

}

public Piece getPiecePgn(String type, int c, int targetI, int targetJ, int reqI, int reqJ) {
  ArrayList<Piece> matches = new ArrayList<Piece>();

  for (int i = 0; i < pieces[c].size(); i++) {
    if (pieces[c].get(i).type == type) matches.add(pieces[c].get(i));
  }

  for (int i = matches.size()-1; i >= 0; i--) {
    Piece p = matches.get(i);

    if (canBePieceMove(p, targetI, targetJ)) { //la pièce est à distance

      if (reqI == -1 && reqJ == -1) continue;
      else if (reqI != -1 && reqJ == -1) { //prérequis i
        if (reqI == p.i) continue;
        else matches.remove(i);
      } else if (reqI == -1 && reqJ != -1) { //prérequis j
        if (reqJ == p.j) continue;
        else matches.remove(i);
      } else { //prérequis i et j
        if (reqI == p.i && reqJ == p.j) continue;
        else matches.remove(i);
      }
    } else {
      matches.remove(i);
    }
  }

  if (matches.size() > 1) println("ERREUR DANS getPiecePgn() : TROP DE MATCHES");
  if (matches.size() == 0) return null;
  return matches.get(0);
}

public String shortPgn(String pgn, int num) {
  String newPgn = "";
  String words[] = pgn.split(" ");
  for (int i = 0; i < num; i++) {
    if (i == 0) newPgn = newPgn + words[i];
    else newPgn = newPgn + " " + words[i];
  }
  return newPgn;
}

public int letterToNum(char c) {
  //a to h -> 0 to 7
  int ascii = (int)c;
  return (ascii - 97);
}

public int pgnNumToNum(char c) {
  int num = Integer.valueOf(String.valueOf(c));
  num = 8 - num;
  return num;
}

public boolean isAtoH(char c) {
  int ascii = (int)c;
  return (ascii >= 97 && ascii <= 104);
}

public boolean is1to8(char c) {
  int ascii = (int)c;
  return (ascii >= 49 && ascii <= 56);
}

public Move getRoquePgn(String word, boolean firstPly) {
  if (firstPly) {
    if (word.equals("O-O") || word.equals("O-O+")) return(new Move(grid[4][7].piece, 6, 7, null, 1));
    else if (word.equals("O-O-O") || word.equals("O-O-O+")) return(new Move(grid[4][7].piece, 2, 7, null, 2));
  } else {
    if (word.equals("O-O") || word.equals("O-O+")) return(new Move(grid[4][0].piece, 6, 0, null, 1));
    else if (word.equals("O-O-O") || word.equals("O-O-O+")) return(new Move(grid[4][0].piece, 2, 0, null, 2));
  }

  return null;
}
/////////////////////////////////////////////////////////////////

// Pièces

class Piece {
  int i, j, c; //0 = blanc; 1 = noir
  int pieceIndex, zobristIndex = 0;
  String code, type;
  boolean dragging;

  int roquable = -1, petitRoquable = -1, grandRoquable = -1;

  int maireEval; //Matériel
  int loicEval;
  float mairePosEval; //Positionnel
  float loicPosEval;

  public void setRoques(int r1, int r2) {
    error("Piece.setRoques", "Impossible de gérer les roques pour une autre pièce qu'une tour");
  }

  public void show() {
    imageMode(CENTER);
    fill(0);
    noStroke();

    float posX, posY;
    if (this.dragging) {
      posX = mouseX;
      posY = mouseY;
    } else {
      if (pointDeVue) {
        posX = this.i*w + w/2 + offsetX;
        posY = this.j*w + w/2 + offsetY;
      } else {
        posX = width - (this.i*w + w/2);
        posY = height - (this.j*w + w/2);
      }
    }

    if (this.c == 0) image(imageArrayB[pieceIndex], posX, posY, pieceSize, pieceSize);
    else image(imageArrayN[pieceIndex], posX, posY, pieceSize, pieceSize);
  }

  public void updatePosEval() {
    if (this.c == 0) {
      this.mairePosEval = mairePosArray[pieceIndex][this.i][this.j] * (1 - endGameWeight);
      this.mairePosEval += mairePosArrayEnd[pieceIndex][this.i][this.j] * endGameWeight;

      this.loicPosEval = loicPosArray[pieceIndex][this.i][this.j];// * (1 - endGameWeight);
    } else {
      this.mairePosEval = mairePosArray[pieceIndex][7-this.i][7-this.j] * (1 - endGameWeight);
      this.mairePosEval += mairePosArrayEnd[pieceIndex][7-this.i][7-this.j] * endGameWeight;

      this.loicPosEval = loicPosArray[pieceIndex][7-this.i][7-this.j];// * (1 - endGameWeight);
    }
  }

  public void setPlace(int i, int j) {
    grid[this.i][this.j].piece = null;
    this.i = i;
    this.j = j;
    grid[i][j].piece = this;

    this.updatePosEval();
  }

  public void quickMove(int i, int j) {
    this.setPlace(i, j);
  }

  public void move(Move m) {
    grid[this.i][this.j].piece = null;
    if (m.capture != null) { //captures
      removePiece(m.capture);
    }
    this.i = m.i;
    this.j = m.j;
    grid[this.i][this.j].piece = this;

    this.updatePosEval();
  }

  public ArrayList generateMoves(boolean withCastle, boolean engine) {
    return new ArrayList<Move>();
  }

  public ArrayList generateQuietMoves(boolean engine) {
    return new ArrayList<Move>();
  }

  public ArrayList generateLegalMoves(boolean withCastle, boolean engine) {
    ArrayList<Move> pseudoMoves = this.generateMoves(withCastle, engine);
    pseudoMoves = removeIllegalMoves(this, pseudoMoves);
    return pseudoMoves;
  }

  public ArrayList generateQuietLegalMoves(boolean engine) {
    ArrayList<Move> quietsMoves = this.generateQuietMoves(engine);
    quietsMoves = removeIllegalMoves(this, quietsMoves);
    return quietsMoves;
  }

  public void select(boolean s) {
    if (s) { //sélection
      ArrayList<Move> moves = this.generateLegalMoves(true, false);

      //Pour chaque moves, on l'affiche
      for (int i = 0; i < moves.size(); i++) {
        grid[moves.get(i).i][moves.get(i).j].possibleMove = moves.get(i);
      }

    } else {
      this.dragging = false;
    }
  }

  public void fly() {
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (grid[i][j].piece == null) grid[i][j].freeMove = true;
      }
    }
  }
}

class Roi extends Piece {
  Roi(int i_, int j_, int c_) {
    this.i = i_;
    this.j = j_;
    this.c = c_;
    this.type = "roi";
    this.pieceIndex = ROI_INDEX;
    this.roquable = 1;

    // Index unique pour chaque type de pièce
    this.zobristIndex = (c == 0) ? 0 : 6;
    this.zobristIndex += pieceIndex;

    //Données pièce
    this.maireEval = maireEvalArray[pieceIndex];
    this.loicEval = loicEvalArray[pieceIndex];
    if (this.c == 0) this.code = codeArrayB[pieceIndex];
    else this.code = codeArrayN[pieceIndex];

    grid[this.i][this.j].piece = this;
    this.updatePosEval();
  }

  public @Override
  void quickMove(int i, int j) {
    super.quickMove(i, j);
    if ((i == 4 && j == 0 && this.c == 1) || (i == 4 && j == 7 && this.c == 0)) this.roquable = 1;
    else this.roquable = 0;
  }

  public @Override
  ArrayList generateMoves(boolean withCastle, boolean engine) {
    return getKingMoves(this, withCastle);
  }

  public @Override
  ArrayList generateQuietMoves(boolean engine) {
    return getQuietKingMoves(this, this.c);
  }
}

class Dame extends Piece {
  Dame(int i_, int j_, int c_) {
    this.i = i_;
    this.j = j_;
    this.c = c_;
    this.type = "dame";
    this.pieceIndex = DAME_INDEX;

    // Index unique pour chaque type de pièce
    this.zobristIndex = (c == 0) ? 0 : 6;
    this.zobristIndex += pieceIndex;

    //Données pièce
    this.maireEval = maireEvalArray[pieceIndex];
    this.loicEval = loicEvalArray[pieceIndex];
    if (this.c == 0) this.code = codeArrayB[pieceIndex];
    else this.code = codeArrayN[pieceIndex];

    grid[this.i][this.j].piece = this;
    this.updatePosEval();
  }

  public @Override
  ArrayList generateMoves(boolean withCastle, boolean engine) {
    return getQueenMoves(this);
  }

  public @Override
  ArrayList generateQuietMoves(boolean engine) {
    return getQuietQueenMoves(this, this.c);
  }
}

class Tour extends Piece {
  Tour(int i_, int j_, int c_) {
    this.i = i_;
    this.j = j_;
    this.c = c_;
    this.type = "tour";
    this.pieceIndex = TOUR_INDEX;

    // Index unique pour chaque type de pièce
    this.zobristIndex = (c == 0) ? 0 : 6;
    this.zobristIndex += pieceIndex;

    //Données pièce
    this.maireEval = maireEvalArray[pieceIndex];
    this.loicEval = loicEvalArray[pieceIndex];
    if (this.c == 0) this.code = codeArrayB[pieceIndex];
    else this.code = codeArrayN[pieceIndex];

    grid[this.i][this.j].piece = this;
    this.updatePosEval();
  }

  public @Override
  void setRoques(int petit, int grand) {
    this.petitRoquable = petit;
    this.grandRoquable = grand;
  }

  public @Override
  void quickMove(int i, int j) {
    super.quickMove(i, j);
     if (this.c == 0) {
       if ((i == 0 && j == 7) || (i == 7 && j == 7)) this.roquable = 1;
       else this.roquable = 0;
     } else {
       if ((i == 0 && j == 0) || (i == 7 && j == 0)) this.roquable = 1;
       else this.roquable = 0;
     }
  }

  public @Override
  ArrayList generateMoves(boolean withCastle, boolean engine) {
    return getRookMoves(this);
  }

  public @Override
  ArrayList generateQuietMoves(boolean engine) {
    return getQuietRookMoves(this, this.c);
  }
}

class Fou extends Piece {
  Fou(int i_, int j_, int c_) {
    this.i = i_;
    this.j = j_;
    this.c = c_;
    this.type = "fou";
    this.pieceIndex = FOU_INDEX;

    // Index unique pour chaque type de pièce
    this.zobristIndex = (c == 0) ? 0 : 6;
    this.zobristIndex += pieceIndex;

    //Données pièce
    this.maireEval = maireEvalArray[pieceIndex];
    this.loicEval = loicEvalArray[pieceIndex];
    if (this.c == 0) this.code = codeArrayB[pieceIndex];
    else this.code = codeArrayN[pieceIndex];

    grid[this.i][this.j].piece = this;
    this.updatePosEval();
  }

  public @Override
  ArrayList generateMoves(boolean withCastle, boolean engine) {
    return getBishopMoves(this);
  }

  public @Override
  ArrayList generateQuietMoves(boolean engine) {
    return getQuietBishopMoves(this, this.c);
  }
}

class Cavalier extends Piece {
  Cavalier(int i_, int j_, int c_) {
    this.i = i_;
    this.j = j_;
    this.c = c_;
    this.type = "cavalier";
    this.pieceIndex = CAVALIER_INDEX;

    // Index unique pour chaque type de pièce
    this.zobristIndex = (c == 0) ? 0 : 6;
    this.zobristIndex += pieceIndex;

    //Données pièce
    this.maireEval = maireEvalArray[pieceIndex];
    this.loicEval = loicEvalArray[pieceIndex];
    if (this.c == 0) this.code = codeArrayB[pieceIndex];
    else this.code = codeArrayN[pieceIndex];

    grid[this.i][this.j].piece = this;
    this.updatePosEval();
  }

  public @Override
  ArrayList generateMoves(boolean withCastle, boolean engine) {
    return getKnightMoves(this);
  }

  public @Override
  ArrayList generateQuietMoves(boolean engine) {
    return getQuietKnightMoves(this, this.c);
  }
}

class Pion extends Piece {
  Pion(int i_, int j_, int c_) {
    this.i = i_;
    this.j = j_;
    this.c = c_;
    this.type = "pion";
    this.pieceIndex = PION_INDEX;

    // Index unique pour chaque type de pièce
    this.zobristIndex = (c == 0) ? 0 : 6;
    this.zobristIndex += pieceIndex;

    //Données pièce
    this.maireEval = maireEvalArray[pieceIndex];
    this.loicEval = loicEvalArray[pieceIndex];
    if (this.c == 0) this.code = codeArrayB[pieceIndex];
    else this.code = codeArrayN[pieceIndex];

    grid[this.i][this.j].piece = this;
    this.updatePosEval();
  }

  public @Override
  ArrayList generateMoves(boolean withCastle, boolean engine) {
    return getPawnMoves(this, engine);
  }

  public @Override
  ArrayList generateQuietMoves(boolean engine) {
    return getQuietPawnMoves(this, this.c, engine);
  }
}

/////////////////////////////////////////////////////////////////

// Genération des coups des pièces

public ArrayList getKnightMoves(Piece p) {
  ArrayList<Move> moves = new ArrayList<Move>();

  int[] gi = {p.i-2, p.i-2, p.i+2, p.i+2, p.i+1, p.i+1, p.i-1, p.i-1};
  int[] gj = {p.j-1, p.j+1, p.j-1, p.j+1, p.j+2, p.j-2, p.j-2, p.j+2};

  for (int i = 0; i < gi.length; i++) {
    if (gi[i] >= 0 && gi[i] < rows && gj[i] >= 0 && gj[i] < cols) {
      if (grid[gi[i]][gj[i]].piece != null) { //si il y a une pièce
        if (grid[gi[i]][gj[i]].piece.c != p.c) { //si la pièce est adverse
          moves.add(new Move(p, gi[i], gj[i], grid[gi[i]][gj[i]].piece, 0));
        }
      } else {
        moves.add(new Move(p, gi[i], gj[i], null, 0));
      }
    }
  }

  return moves;
}

public ArrayList getBishopMoves(Piece p) {
  ArrayList<Move> moves = new ArrayList<Move>();

  //Bas-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  //Haut-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  //Haut-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  //Bas-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  return moves;
}

public ArrayList getRookMoves(Piece p) {
  ArrayList<Move> moves = new ArrayList<Move>();

  for (int i = p.i+1; i < cols; i++) { //Droite
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != p.c) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, i, p.j, null, 0));
  }

  for (int j = p.j+1; j < rows; j++) { //Bas
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != p.c) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, p.i, j, null, 0));
  }

  for (int i = p.i-1; i >= 0; i--) { //Gauche
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != p.c) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, i, p.j, null, 0));
  }

  for (int j = p.j-1; j >= 0; j--) { //Haut
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != p.c) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, p.i, j, null, 0));
  }

  return moves;
}

public ArrayList getQueenMoves(Piece p) {
  ArrayList<Move> moves = new ArrayList<Move>();

  for (int i = p.i+1; i < cols; i++) { //Droite
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != p.c) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, i, p.j, null, 0));
  }

  for (int j = p.j+1; j < rows; j++) { //Bas
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != p.c) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, p.i, j, null, 0));
  }

  for (int i = p.i-1; i >= 0; i--) { //Gauche
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != p.c) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, i, p.j, null, 0));
  }

  for (int j = p.j-1; j >= 0; j--) { //Haut
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != p.c) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, p.i, j, null, 0));
  }

  //Bas-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  //Haut-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  //Haut-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  //Bas-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != p.c) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
    moves.add(new Move(p, gi, gj, null, 0));
  }

  return moves;
}

public ArrayList getKingMoves(Piece p, boolean withCastle) {
  ArrayList<Move> moves = new ArrayList<Move>();

  //Déplacements classiques
  for (int i = -1; i <= 1; i++) {
    if (p.i+i < 0 || p.i+i >= rows) continue;
    for (int j = -1; j <= 1; j++) {
      if (p.j+j < 0 || p.j+j >= cols) continue;
      if (i == 0 && j == 0) continue;
      Cell cell = grid[p.i+i][p.j+j];
      if (cell.piece != null) {
        if (cell.piece.c != p.c) {
          moves.add(new Move(p, cell.i, cell.j, cell.piece, 0));
        }
      } else {
        moves.add(new Move(p, cell.i, cell.j, null, 0));
      }
    }
  }

  //Roque
  if (withCastle == true) { // ??? variables globales pRoque et gRoque (b et n)

    if (p.roquable == 1 && playerInCheck(p.c) == -1) {
      for (int i = 0; i < pieces[p.c].size(); i++) {
        Piece p2 = pieces[p.c].get(i);

        if (p2.petitRoquable == 1) {
          if (grid[p.i+1][p.j].piece == null && grid[p.i+2][p.j].piece == null) {
            ArrayList<Move> MovesToTest = new ArrayList<Move>();
            MovesToTest.add(new Move(p, p.i+1, p.j, null, 0));
            MovesToTest.add(new Move(p, p.i+2, p.j, null, 0));
            if (findIllegalMoves(p, MovesToTest).size() == 0) {
              moves.add(new Move(p, p.i+2, p.j, null, 1));
            }
          }

        } else if (p2.grandRoquable == 1) {
          if (grid[p.i-1][p.j].piece == null && grid[p.i-2][p.j].piece == null && grid[p.i-3][p.j].piece == null) {
            ArrayList<Move> MovesToTest = new ArrayList<Move>();
            MovesToTest.add(new Move(p, p.i-1, p.j, null, 0));
            MovesToTest.add(new Move(p, p.i-2, p.j, null, 0));
            if (findIllegalMoves(p, MovesToTest).size() == 0) {
              moves.add(new Move(p, p.i-2, p.j, null, 2));
            }
          }
        }

      }
    }

  }
  return moves;
}

public ArrayList getPawnMoves(Piece p, boolean engine) {
  ArrayList<Move> moves = new ArrayList<Move>();

  if (p.c == 0) { //pion blanc

    //captures diagonales
    int tempI = p.i+1;
    int tempJ = p.j-1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != p.c) {

          if (p.j == 1) {
            //promotion
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }

        }
      }
    }
    tempI = p.i-1;
    tempJ = p.j-1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != p.c) {

          if (p.j == 1) {
            //promotion
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }

        }
      }
    }

    if (p.j == 6) {
      //première avancée
      for (int j = 1; j <= 2; j++) {
        if (p.j-j < 0) continue;
        if (grid[p.i][p.j-j].piece != null) break;
        moves.add(new Move(p, p.i, p.j-j, null, 0));
      }
    } else {
      //autre avancée
      if (p.j-1 >= 0 && grid[p.i][p.j-1].piece == null) {
        if (p.j == 1) {
          if (engine == false) {
            moves.add(new Move(p, p.i, p.j-1, null, 4));
          } else {
            moves.add(new Move(p, p.i, p.j-1, null, 5));
            moves.add(new Move(p, p.i, p.j-1, null, 6));
            moves.add(new Move(p, p.i, p.j-1, null, 7));
            moves.add(new Move(p, p.i, p.j-1, null, 8));
          }
        } else {
          moves.add(new Move(p, p.i, p.j-1, null, 0));
        }
      }
    }

    //En passant
    if (p.j == 3) {
      if (p.i - 1 >= 0) { //à gauche
        if (grid[p.i-1][p.j].piece != null) {
          if (grid[p.i-1][p.j].piece == currentEnPassantable[grid[p.i-1][p.j].piece.c]) {
            moves.add(new Move(p, p.i-1, p.j-1, grid[p.i-1][p.j].piece, 3));
          }
        }
      }
      if (p.i + 1 < cols) { //à droite
        if (grid[p.i+1][p.j].piece != null) {
          if (grid[p.i+1][p.j].piece == currentEnPassantable[grid[p.i+1][p.j].piece.c]) {
            moves.add(new Move(p, p.i+1, p.j-1, grid[p.i+1][p.j].piece, 3));
          }
        }
      }
    }
  } else { //pion noir

    //captures diagonales
    int tempI = p.i+1;
    int tempJ = p.j+1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != p.c) {
          if (p.j == 6) {
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }
        }
      }
    }
    tempI = p.i-1;
    tempJ = p.j+1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != p.c) {
          if (p.j == 6) {
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }
        }
      }
    }
    if (p.j == 1) { //première avancée
      for (int j = 1; j <= 2; j++) {
        if (p.j+j >= cols) continue;
        if (grid[p.i][p.j+j].piece != null) break;
        moves.add(new Move(p, p.i, p.j+j, null, 0));
      }
    } else { //autre avancée
      if (p.j+1 < cols && grid[p.i][p.j+1].piece == null) {
          if (p.j == 6) {
            if (engine == false) {
              moves.add(new Move(p, p.i, p.j+1, null, 4));
            } else {
              moves.add(new Move(p, p.i, p.j+1, null, 5));
              moves.add(new Move(p, p.i, p.j+1, null, 6));
              moves.add(new Move(p, p.i, p.j+1, null, 7));
              moves.add(new Move(p, p.i, p.j+1, null, 8));
            }
          } else {
            moves.add(new Move(p, p.i, p.j+1, null, 0));
          }
      }
    }

    //En passant
    if (p.j == 4) {
      if (p.i - 1 >= 0) { //à gauche
        if (grid[p.i-1][p.j].piece != null) {
          if (grid[p.i-1][p.j].piece == currentEnPassantable[grid[p.i-1][p.j].piece.c]) {
            moves.add(new Move(p, p.i-1, p.j+1, grid[p.i-1][p.j].piece, 3));
          }
        }
      }
      if (p.i + 1 < cols) { //à droite
        if (grid[p.i+1][p.j].piece != null) {
          if (grid[p.i+1][p.j].piece == currentEnPassantable[grid[p.i+1][p.j].piece.c]) {
            moves.add(new Move(p, p.i+1, p.j+1, grid[p.i+1][p.j].piece, 3));
          }
        }
      }
    }
  }

  return moves;
}

/////////////////////////////////////////////////////////////////

// Génération des coups quiets des pièces
// colorToNotDetect : Si le paramètre est de la même couleur que la pièce, renvoie les captures, sinon renvoie les coups pour pgn

public ArrayList getQuietKnightMoves(Piece p, int colorToNotDetect) {
  ArrayList<Move> moves = new ArrayList<Move>();

  int[] gi = {p.i-2, p.i-2, p.i+2, p.i+2, p.i+1, p.i+1, p.i-1, p.i-1};
  int[] gj = {p.j-1, p.j+1, p.j-1, p.j+1, p.j+2, p.j-2, p.j-2, p.j+2};

  for (int i = 0; i < gi.length; i++) {
    if (gi[i] >= 0 && gi[i] < rows && gj[i] >= 0 && gj[i] < cols) {
      if (grid[gi[i]][gj[i]].piece != null) { //si il y a une pièce
        if (grid[gi[i]][gj[i]].piece.c != colorToNotDetect) { //si la pièce n'est pas de la couleur à ne pas détecter
          moves.add(new Move(p, gi[i], gj[i], grid[gi[i]][gj[i]].piece, 0));
        }
      }
    }
  }

  return moves;
}

public ArrayList getQuietBishopMoves(Piece p, int colorToNotDetect) {
  ArrayList<Move> moves = new ArrayList<Move>();

  //Bas-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  //Haut-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  //Haut-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  //Bas-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  return moves;
}

public ArrayList getQuietRookMoves(Piece p, int colorToNotDetect) {
  ArrayList<Move> moves = new ArrayList<Move>();

  for (int i = p.i+1; i < cols; i++) { //Droite
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
  }

  for (int j = p.j+1; j < rows; j++) { //Bas
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
  }

  for (int i = p.i-1; i >= 0; i--) { //Gauche
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
  }

  for (int j = p.j-1; j >= 0; j--) { //Haut
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
  }

  return moves;
}

public ArrayList getQuietQueenMoves(Piece p, int colorToNotDetect) {
  ArrayList<Move> moves = new ArrayList<Move>();

  for (int i = p.i+1; i < cols; i++) { //Droite
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
  }

  for (int j = p.j+1; j < rows; j++) { //Bas
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
  }

  for (int i = p.i-1; i >= 0; i--) { //Gauche
    if (grid[i][p.j].piece != null) {
      if (grid[i][p.j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, i, p.j, grid[i][p.j].piece, 0));
      }
      break;
    }
  }

  for (int j = p.j-1; j >= 0; j--) { //Haut
    if (grid[p.i][j].piece != null) {
      if (grid[p.i][j].piece.c != colorToNotDetect) {
        moves.add(new Move(p, p.i, j, grid[p.i][j].piece, 0));
      }
      break;
    }
  }

  //Bas-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  //Haut-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  //Haut-droite
  for (int i = 1; i < cols; i++) {
    int gi = p.i+i;
    int gj = p.j-i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  //Bas-gauche
  for (int i = 1; i < cols; i++) {
    int gi = p.i-i;
    int gj = p.j+i;
    if (gi < 0 || gi >= rows || gj  < 0 || gj >= cols) break;
    if (grid[gi][gj].piece != null) {
      if (grid[gi][gj].piece.c != colorToNotDetect) {
        moves.add(new Move(p, gi, gj, grid[gi][gj].piece, 0));
      }
      break;
    }
  }

  return moves;
}

public ArrayList getQuietKingMoves(Piece p, int colorToNotDetect) {
  ArrayList<Move> moves = new ArrayList<Move>();

  //Déplacements classiques
  for (int i = -1; i <= 1; i++) {
    if (p.i+i < 0 || p.i+i >= rows) continue;
    for (int j = -1; j <= 1; j++) {
      if (p.j+j < 0 || p.j+j >= cols) continue;
      if (i == 0 && j == 0) continue;
      Cell cell = grid[p.i+i][p.j+j];
      if (cell.piece != null) {
        if (cell.piece.c != colorToNotDetect) {
          moves.add(new Move(p, cell.i, cell.j, cell.piece, 0));
        }
      }
    }
  }
  return moves;
}

public ArrayList getQuietPawnMoves(Piece p, int colorToDetect, boolean engine) {
  ArrayList<Move> moves = new ArrayList<Move>();

  if (p.c == 0) { //pion blanc

    //captures diagonales
    int tempI = p.i+1;
    int tempJ = p.j-1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != colorToDetect) {

          if (p.j == 1) {
            //promotion
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }

        }
      }
    }
    tempI = p.i-1;
    tempJ = p.j-1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != colorToDetect) {

          if (p.j == 1) {
            //promotion
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }

        }
      }
    }

    //En passant
    if (p.j == 3) {
      if (p.i - 1 >= 0) { //à gauche
        if (grid[p.i-1][p.j].piece != null) {
          if (grid[p.i-1][p.j].piece == currentEnPassantable[grid[p.i-1][p.j].piece.c]) {
            moves.add(new Move(p, p.i-1, p.j-1, grid[p.i-1][p.j].piece, 3));
          }
        }
      }
      if (p.i + 1 < cols) { //à droite
        if (grid[p.i+1][p.j].piece != null) {
          if (grid[p.i+1][p.j].piece == currentEnPassantable[grid[p.i+1][p.j].piece.c]) {
            moves.add(new Move(p, p.i+1, p.j-1, grid[p.i+1][p.j].piece, 3));
          }
        }
      }
    }
  } else { //pion noir

    //captures diagonales
    int tempI = p.i+1;
    int tempJ = p.j+1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != colorToDetect) {
          if (p.j == 6) {
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }
        }
      }
    }
    tempI = p.i-1;
    tempJ = p.j+1;
    if (tempI >= 0 && tempI < rows && tempJ >= 0 && tempJ < cols) {
      if (grid[tempI][tempJ].piece != null) {
        if (grid[tempI][tempJ].piece.c != colorToDetect) {
          if (p.j == 6) {
            if (engine == false) {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 4));
            } else {
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 5));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 6));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 7));
              moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 8));
            }
          } else {
            moves.add(new Move(p, tempI, tempJ, grid[tempI][tempJ].piece, 0));
          }
        }
      }
    }

    //En passant
    if (p.j == 4) {
      if (p.i - 1 >= 0) { //à gauche
        if (grid[p.i-1][p.j].piece != null) {
          if (grid[p.i-1][p.j].piece == currentEnPassantable[grid[p.i-1][p.j].piece.c]) {
            moves.add(new Move(p, p.i-1, p.j+1, grid[p.i-1][p.j].piece, 3));
          }
        }
      }
      if (p.i + 1 < cols) { //à droite
        if (grid[p.i+1][p.j].piece != null) {
          if (grid[p.i+1][p.j].piece == currentEnPassantable[grid[p.i+1][p.j].piece.c]) {
            moves.add(new Move(p, p.i+1, p.j+1, grid[p.i+1][p.j].piece, 3));
          }
        }
      }
    }
  }

  return moves;
}
class Shortcut {
  Shortcut() { }

  public String getDescription(int n) {
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
      case 18: return "Coller le code HTML du plateau (P)";

      default: return "";
    }
  }

  public void call(int n) {
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
      case 18: pasteHTMLtoBoard(); break;

      default: println(">>> Erreur dans shortcut.call()");
      break;
    }
  }
}

/////////////////////////////////////////////////////////////////

// Fonctions de shortcuts

public void pasteHTMLtoBoard() {
  String str = GetTextFromClipboard();
  HTMLtoBoard(str);
}

public void toggleSearchController() {
  showSearchController = !showSearchController;
  if (showSearchController) sa.show();
  else sa.hide();

  delay(3);
  surface.setVisible(true);
}

public void toggleParameters() {
  showParameters =! showParameters;
  showSavedPositions = false;
}

public void toggleSavedPos() {
  showSavedPositions =! showSavedPositions;
  showParameters = false;
}

public void flipBoard() {
  pointDeVue = !pointDeVue;
}

public void toggleVariantes() {
  showVariante =! showVariante;
}

public void importSavedFEN(int number) {
  String fen = savedFENS[number];
  importFEN(fen);
  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);
}

public void makeStartPos() {
  importFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
  piecesToDisplay.clear();
  piecesToDisplay.addAll(pieces[0]);
  piecesToDisplay.addAll(pieces[1]);
}

public void toggleGraph() {
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

public void rewindBack() {
  if (!play || gameEnded) {
    if (movesHistory.size() == 0 || rewindCount == movesHistory.size()) return;
    rewind = true;
    rewindCount++;
    if (rewindCount > movesHistory.size()) rewindCount = movesHistory.size();
    movesHistory.get(movesHistory.size() - rewindCount).unplay();
  }
}

public void rewindForward() {
  if (!play || gameEnded) {
    if (rewindCount <= 0) return;
    movesHistory.get(movesHistory.size() - rewindCount).replay();
    rewindCount--;
    if (rewindCount <= 0) { rewindCount = 0; rewind = false; }
  }
}

public void forceQuit() {
  resetGame(true);
}

public void clearPosition() {
  removeAllPieces();
  piecesToDisplay.clear();
}

public void printFEN() {
  println("Position fen : " + generateFEN());
}

public void copyFEN() {
  String f = generateFEN();
  StringSelection data = new StringSelection(f);
  Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  clipboard.setContents(data, data);

  println("Fen copiée (" + generateFEN() + ")");
}

public void goToSelectScreen() {
  // requestToRestart = millis();
  // println();
  // println("Quitter la partie et revenir à la sélection ? [y/n]");
  // println();
  resetGame(true);
}

public void playPause() {
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

public void printPGN() {
  println(pgn);
}

public void printInfos() {
  printMaireEval();
  println("Endgame weight : " + endGameWeight);
  println("FEN : " + generateFEN());
  println("Zobrist hash key : " + zobrist.hash);
}

public void toggleAttach() {
  attach = !attach;
  surface.setAlwaysOnTop(attach);
  if (attach) infos = "Épinglé";
  else infos = "";
}

public void printMaireEval() {
  print("Evaluation statique du maire : ");
  LeMaire m = new LeMaire(1, 3, 0, false);
  println(m.Evaluation()/100); //Evaluation statique de la position selon le maire
}

public void delayUp() {
  speed += 6;
  speed = constrain(speed, 0, 1200);
  printNewSpeed();
}

public void delayDown() {
  speed -= 6;
  speed = constrain(speed, 0, 1200);
  printNewSpeed();
}

public void printNewSpeed() {
  println("Délai par coups : " + (float)speed/60 + " s");
}

public void savePGN() {
  String[] s = new String[1];
  s[0] = pgn;
  String names = joueurs.get(0).name + " vs " + joueurs.get(1).name;
  String times =  year() + "-" + month() + "-" + day() + " à " + hour() + "." + minute() + "." + second();
  String pgnTitle = names + " - " + times;
  saveStrings("pgn/" + pgnTitle + ".pgn", s);
  println("PGN sauvegardée dans pgn/" + pgnTitle + ".pgn");
}

public void printHelpMenu() {
  println("---------------------");
  println("Menu d'aide et raccourcis clavier");
  println();
  println("  Touche  |       Endroit       |     Description     ");
  println("----------+---------------------+---------------------");
  println(" B        | En partie           | Afficher les coups du livre d'ouverture");
  println(" C        | En partie           | Sauvegarder la PGN");
  println(" C        | Éditeur             | Copier la FEN");
  println(" D        | En partie           | Ouvrir Search Controller");
  println(" F        | En partie / Editeur | Informations sur la position");
  println(" G        | En partie           | Afficher l'analyse");
  println(" H        | Partout             | Raccourcis clavier");
  println(" J        | En partie           | Evaluation statique du maire");
  println(" K        | En partie / Editeur | Retourner l'échiquier");
  println(" L        | En partie / Editeur | Épingler/Désépingler");
  println(" P        | En partie           | Afficher la PGN");
  println(" P        | Éditeur             | Coller le HTML (chess.com) pour récupérer la position");
  println(" Q (MAJ)  | En partie           | Quitter la partie / Revenir à l'accueil");
  println(" R        | En partie + Hacker  | Forcer la détection de fin de partie du hacker sans fin");
  println(" S        | En partie           | Lancer perft 5");
  println(" T        | En partie           | Fonction de tests");
  println(" V        | En partie           | Afficher les variantes");
  println(" UP       | En partie           | Augmenter délai par coups");
  println(" DOWN     | En partie           | Diminuer délai par coups");
  println(" GAUCHE   | En partie           | Reculer d'un coup (uniquement en pause)");
  println(" DROITE   | En partie           | Avancer d'un coup (uniquement en pause)");
  println(" ESPACE   | En partie           | Pause/Play");
  println(" ENTRER   | Menu                | Démarrer la partie");
  println(" SUPPR    | Éditeur             | Effacer la position");
}

public void runPerft() {
  perft(5);
}

public void perft(int d) {
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
  println("Total    : " + total);
  // println("Expected : 4865609");
}

public void runCaptureSearch(int d) {
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
/////////////////////////////////////////////////////////////////

// Table de transposition

// Méthode Probe : Recherche la position dans la table. Si elle est trouvée, la renvoie (sinon null)
// Méthode Store : Enregistre la position dans la table, et écrase l'entrée précédente si collision
// L'index est calculé selon : hash & indexMax (où indexMax est size-1 et size une puissance de 2)
// (Collision : 1 sur 100 hash environ)

/////////////////////////////////////////////////////////////////

// Constantes
byte EXACT = 0;
byte LOWERBOUND = 1;
byte UPPERBOUND = 2;

class TranspositionTable {

  // Taille de la table de transposition
  int size;

  // Index maximal de la table
  int indexMax;

  // Tableau des entrées de la table
  Entry[] entries;

  TranspositionTable(int size) {
    this.size = size;
    this.indexMax = this.size-1;
    this.entries = new Entry[size];
  }

  public Entry Probe(long hashKey, int plyFromRoot) {
    // calcule l'index de la clé
    int ind = this.Index(hashKey);

    Entry entry;
    if (this.entries[ind] != null) entry = this.entries[ind].copy();
    else return null;

    // compare avec le hash s'y trouvant
    if (entry.hash == hashKey) {
      entry.value = retrieveMateValue(entry.value, plyFromRoot);
      return entry;
    }

    return null;
  }

  public void Store(long hashKey, float value, Move move, int depth, int plyFromRoot, byte nodeType) {
    // calcule l'index de la clé
    int ind = this.Index(hashKey);

    // corrige la valeur si c'est un mat (50000 ou -50000)
    float evalToStore = this.storeMateEval(value, plyFromRoot);

    // place l'entrée à l'index
    this.entries[ind] = new Entry(hashKey, evalToStore, move, depth, nodeType);
  }

  public Move getBestMove(long hash) {
    int ind = this.Index(hash);

    Entry entry = this.entries[ind];
    if (entry == null) return null;

    if (entry.hash == hash) {
      return entry.bestMove;
    }
    return null;
  }

  public int Index(long hash) {
    long index = hash & this.indexMax;
    return (int)index;
  }

  public void clear() {
    for (int i = this.entries.length-1; i >= 0; i--) {
      this.entries[i] = null;
    }
  }

  public int getFillState() {
    int num = 0;
    for (int i = 0; i < this.entries.length; i++) {
      if (this.entries[i] != null) num++;
    }
    return num;
  }

  public float storeMateEval(float eval, int ply) {
    int sign = (eval < 0) ? -1 : 1;
    float value = eval * sign;
    value += ply;
    if (value == 50000) return value * sign;
    else return eval;
  }

  public float retrieveMateValue(float eval, int ply) {
    int sign = (eval < 0) ? -1 : 1;
    float value = eval * sign;
    if (value != 50000) return eval;
    value -= ply;
    return value * sign;
  }

}

/////////////////////////////////////////////////////////////////

class Entry {

  // Clé de hachage de la position
  long hash;

  // Evaluation de la position (potentiellement incomplète)
  float value;

  // Meilleur coup
  Move bestMove;

  // Profondeur (nombre de coups cherchés à partir du coup)
  int depth;

  // Type de noeud (Exact, Lowerbound, Upperbound)
  byte nodeType;


  Entry(long hashKey, float eval, Move best, int depthAhead, byte type) {
    this.hash = hashKey;
    this.value = eval;
    this.bestMove = best;
    this.depth = depthAhead;
    this.nodeType = type;
  }

  public Entry copy() {
    Entry newEntry = new Entry(this.hash, this.value, this.bestMove, this.depth, this.nodeType);
    return newEntry;
  }
}
class Zobrist {

  long hash = 0;
  long[][][] piecesOnSquare = new long[12][8][8];
  long[] castlingRights = new long[16];
  long[] enPassantSquare = new long[16];
  long blackToMove;

  int castleState = 0;
  final int whitePetitRoque = 8;
  final int whiteGrandRoque = 4;
  final int blackPetitRoque = 2;
  final int blackGrandRoque = 1;

  int[][] promoZobristIndex = new int[2][4];

  Zobrist() {
    int[] index = {1, 2, 3, 4};

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 4; j++) {
        if (i == 0) this.promoZobristIndex[i][j] = index[j];
        else this.promoZobristIndex[i][j] = index[j] + 6;
      }
    }


    this.initZobristKeys();
  }

  public void initZobristKeys() {
    rngState = 1804289383;

    // Init pieces on square (zobristIndex, i, j);
    for (int p = 0; p < 12; p++) {
      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
          this.piecesOnSquare[p][i][j] = generateRandomNumber();
        }
      }
    }

    // Init caslingRights
    for (int i = 0; i < 16; i++) {
      this.castlingRights[i] = generateRandomNumber();
    }

    // Init enPassant (pas pour l'instant)
    for (int i = 0; i < 16; i++) {
      this.enPassantSquare[i] = generateRandomNumber();
    }

    // Init blackToMove
    blackToMove = generateRandomNumber();
  }

  public long initHash() {
    this.hash = 0;

    // pièces
    for (int i = 0; i < pieces.length; i++) {
      for (int j = 0; j < pieces[i].size(); j++) {
        Piece p = pieces[i].get(j);
        this.hash ^= this.piecesOnSquare[p.zobristIndex][p.i][p.j];
      }
    }

    // droits au roque
    castleState = 0;
    if (rois[0] != null && rois[0].roquable == 1) {
      if (grid[7][7].piece != null && grid[7][7].piece.petitRoquable == 1) castleState += whitePetitRoque;
      if (grid[0][7].piece != null && grid[0][7].piece.grandRoquable == 1) castleState += whiteGrandRoque;
    }
    if (rois[1] != null && rois[1].roquable == 1) {
      if (grid[7][0].piece != null && grid[7][0].piece.petitRoquable == 1) castleState += blackPetitRoque;
      if (grid[0][0].piece != null && grid[0][0].piece.grandRoquable == 1) castleState += blackGrandRoque;
    }
    this.hash ^= this.castlingRights[castleState];

    // tour de qui
    if (tourDeQui == 1) this.hash ^= this.blackToMove;

    // println("Generate from scratch zobrist key : " + this.hash);

    return this.hash;
  }

  public long updateHash(Move m) {
    // XOR out
    this.hash ^= this.piecesOnSquare[m.piece.zobristIndex][m.fromI][m.fromJ];
    if (m.capture != null) this.hash ^= this.piecesOnSquare[m.capture.zobristIndex][m.capture.i][m.capture.j];

    // XOR in
    this.hash ^= this.piecesOnSquare[m.piece.zobristIndex][m.i][m.j];

    // Changement de tour
    this.hash ^= this.blackToMove;

    // Déplacements du roque
    if (m.special == 1) {
      int jPos = (m.piece.c == 0) ? 7 : 0;
      this.hash ^= this.piecesOnSquare[m.tourQuiRoque.zobristIndex][7][jPos];
      this.hash ^= this.piecesOnSquare[m.tourQuiRoque.zobristIndex][5][jPos];
    } else if (m.special == 2) {
      int jPos = (m.piece.c == 0) ? 7 : 0;
      this.hash ^= this.piecesOnSquare[m.tourQuiRoque.zobristIndex][0][jPos];
      this.hash ^= this.piecesOnSquare[m.tourQuiRoque.zobristIndex][3][jPos];
    }

    // Droits au roque
    this.hash ^= this.castlingRights[castleState]; // Retire tous les droits au roque du hash

    castleState = 0; // Update la variable de droits au roque (4 bits)
    if (rois[0].roquable == 1) {
      if (grid[7][7].piece != null && grid[7][7].piece.petitRoquable == 1) castleState += whitePetitRoque;
      if (grid[0][7].piece != null && grid[0][7].piece.grandRoquable == 1) castleState += whiteGrandRoque;
    }
    if (rois[1].roquable == 1) {
      if (grid[7][0].piece != null && grid[7][0].piece.petitRoquable == 1) castleState += blackPetitRoque;
      if (grid[0][0].piece != null && grid[0][0].piece.grandRoquable == 1) castleState += blackGrandRoque;
    }
    this.hash ^= this.castlingRights[castleState]; // Ajoute les droits au roques au hash

    // Promotion
    if (m.special >= 5) {
      // On retire le pion du hash
      this.hash ^= this.piecesOnSquare[m.piece.zobristIndex][m.i][m.j];

      // On ajoute la pièce de promotion au hash
      int index = this.promoZobristIndex[m.piece.c][m.special-5];
      this.hash ^= this.piecesOnSquare[index][m.i][m.j];
    }

    // println("Incrementally updated zobrist key : " + this.hash);

    return this.hash;
  }

}

// Pseudo Random Number Generator (XOR-Shift algorithm) pour avoir les mêmes clés à chaque fois
public long generateRandomNumber() {
  long number = rngState;

  number ^= number << 13;
  number ^= number >> 17;
  number ^= number << 5;

  rngState = number;

  return number;
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Echecs_IA" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
