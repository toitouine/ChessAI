/////////////////////////////////////////////////////////////////

// TODO :

// Editeur de position : Trait et roques
// Bouton d'abandon
// Bouton d'aide

/////////////////////////////////////////////////////////////////

// Libraries

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

/////////////////////////////////////////////////////////////////

// Constantes

int CONSTANTE_DE_STOCKFISH = 3;
float TOTAL_DEPART = 3200.0;

int ROI_INDEX = 0;
int DAME_INDEX = 1;
int TOUR_INDEX = 2;
int FOU_INDEX = 3;
int CAVALIER_INDEX = 4;
int PION_INDEX = 5;

int CHESSCOM = 0;
int LICHESS = 1;

int MENU = 0;
int GAME = 1;
int EDITOR = 2;

int INITIAL_TOTAL_MAIRE_MATERIAL = 0;

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

ArrayList<Button> allButtons = new ArrayList<Button>(); // ArrayList de tous les boutons (et toggles) de l'interface

ArrayList<Piece> piecesToDisplay = new ArrayList<Piece>();
ArrayList<Piece>[] pieces = new ArrayList[2];
Piece[] currentEnPassantable = {null, null};
ArrayList<Joueur> joueurs = new ArrayList<Joueur>();

ArrayList<PromotionButton> promoButtons = new ArrayList<PromotionButton>();
ArrayList<ToggleButton> toggles1 = new ArrayList<ToggleButton>();
ArrayList<ToggleButton> toggles2 = new ArrayList<ToggleButton>();
ArrayList<ShortcutButton> iconButtons = new ArrayList<ShortcutButton>();
ArrayList<ShortcutButton> editorIconButtons = new ArrayList<ShortcutButton>();
ArrayList<TextButton> hubButtons = new ArrayList<TextButton>();
ArrayList<ButtonFEN> savedFENSbuttons = new ArrayList<ButtonFEN>();
ArrayList<DragAndDrop>[] addPiecesButtons = new ArrayList[2];
ArrayList<TimeButton>[] timeButtons = new ArrayList[2];
ArrayList<ImageButton> presetButtons = new ArrayList<ImageButton>();
ArrayList<ImageButton> humanButton = new ArrayList<ImageButton>();

ArrayList<String> book = new ArrayList<String>();
ArrayList<Arrow> bookArrows = new ArrayList<Arrow>();
ArrayList<Arrow> varianteArrows = new ArrayList<Arrow>();
Arrow bestMoveArrow;

ArrayList<String> positionHistory = new ArrayList<String>();
ArrayList<Move> movesHistory = new ArrayList<Move>();
ArrayList<Long> zobristHistory = new ArrayList<Long>();

CircleToggleButton addPiecesColorSwitch;
ImageButton positionEditor;
ImageButton hackerButton;
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
float nbTour = 0.5;
float endGameWeight = 0;
boolean gameEnded = false;
String infos = "";
String infoBox = "";
String pgn = "";

boolean play = true;
boolean disableEndScreen = false;
boolean engineToPlay = false;
boolean stopSearch = false;
boolean rewind = false;
boolean showGraph = false;
boolean showVariante = false;
boolean showSavedPositions = false;
boolean showSearchController = false;
boolean showParameters = false;
boolean blockPlaying = false;
boolean useTime = false;
int gameState = MENU;
int winner = -1;
int timeAtEnd = 0;
int rewindCount = 0;
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
boolean hackerWaitingToRestart = false;
int timeAtLastRestartTry = 0;
int currentHackerPOV = 0;
int timeAtHackerEnd = 0;
int lastMoveTime = 0;
int numberOfScan = 0;
boolean isNextMoveRestranscrit = false;
boolean useHacker = false;
boolean hackerPret = false;
boolean hackerAPImode = false;
Point[][] hackerCoords = new Point[8][8];
Point[][] saveHackerCoords = new Point[8][8];
ArrayList<Move> hackerMoves = new ArrayList<Move>();

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
  Condition hubCondition = new Condition() { public boolean c() { return gameState == MENU; } };
  hubButtons.add(new TextButton(width/2 - 190, 480, 380, 75, "Nouvelle partie", 30, 10, "verifStartGame", hubCondition));
  hubButtons.add(new TextButton(width-110, height-40, 100, 30, "Coller FEN", 18, 8, "pasteFEN", hubCondition)); hubButtons.get(1).setColors(#1d1c1a, #ffffff);
  hubButtons.add(new TextButton(width-220, height-40, 100, 30, "Copier FEN", 18, 8, "copyFEN", hubCondition)); hubButtons.get(2).setColors(#1d1c1a, #ffffff);
  allButtons.addAll(hubButtons);

  Condition promoCondition = new Condition() { public boolean c() { return (gameState == GAME && !blockPlaying && enPromotion != null && joueurs.get(tourDeQui).name == "Humain"); } };
  promoButtons.add(new PromotionButton(0.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[1], imageArrayN[1], 0, promoCondition));
  promoButtons.add(new PromotionButton(2.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[2], imageArrayN[2], 1, promoCondition));
  promoButtons.add(new PromotionButton(4.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[3], imageArrayN[3], 2, promoCondition));
  promoButtons.add(new PromotionButton(6.25*w + offsetX, 3.25*w + offsetY, 1.5*w, imageArrayB[4], imageArrayN[4], 3, promoCondition));
  allButtons.addAll(promoButtons);

  toggles1.add(new ToggleButton(40, 80, 150, stockfish, "Stockfish", 0, hubCondition));
  toggles1.add(new ToggleButton(230, 80, 150, antoine, "Antoine", 0, hubCondition));
  toggles1.add(new ToggleButton(420, 80, 150, loic, "Loic", 0, hubCondition));
  toggles1.add(new ToggleButton(610, 80, 150, lesmoutons, "LesMoutons", 0, hubCondition));
  toggles1.add(new ToggleButton(800, 80, 150, lemaire, "LeMaire", 0, hubCondition));
  toggles1.add(new ToggleButton(990, 80, 150, human, "Humain", 0, hubCondition));
  allButtons.addAll(toggles1);

  toggles2.add(new ToggleButton(40, 290, 150, stockfish, "Stockfish", 1, hubCondition));
  toggles2.add(new ToggleButton(230, 290, 150, antoine, "Antoine", 1, hubCondition));
  toggles2.add(new ToggleButton(420, 290, 150, loic, "Loic", 1, hubCondition));
  toggles2.add(new ToggleButton(610, 290, 150, lesmoutons, "LesMoutons", 1, hubCondition));
  toggles2.add(new ToggleButton(800, 290, 150, lemaire, "LeMaire", 1, hubCondition));
  toggles2.add(new ToggleButton(990, 290, 150, human, "Humain", 1, hubCondition));
  allButtons.addAll(toggles2);

  Condition buttonEditorCondition = new Condition() { public boolean c() { return (gameState == EDITOR && !showParameters && !showSavedPositions); } };
  addPiecesColorSwitch = new CircleToggleButton(offsetX/2, (offsetY+w/2 + w*6) + 70, w/1.3, "switchAddPieceColor", buttonEditorCondition);
  positionEditor = new ImageButton(width-55, 10, 50, 50, 0, #ffffff, chess, true, "startEditor", hubCondition);
  hackerButton = new ImageButton(width-100, 11, 40, 40, 0, #ffffff, bot, true, "toggleUseHacker", hubCondition);
  hackerButton.display = false;
  allButtons.add(addPiecesColorSwitch);
  allButtons.add(positionEditor);
  allButtons.add(hackerButton);

  Condition endButtons = new Condition() { public boolean c() { return(gameState == GAME && gameEnded && !useHacker && !hackerPret); } };
  rematchButton = new TextButton(offsetX - offsetX/1.08, offsetY+4*w-29, offsetX-2*(offsetX - offsetX/1.08), 24, "Revanche", 15, 3, "rematch", endButtons);
  rematchButton.setColors(#1d1c1a, #ffffff);
  newGameButton = new TextButton(offsetX - offsetX/1.08, offsetY+4*w+5, offsetX-2*(offsetX - offsetX/1.08), 24, "Menu", 15, 3, "newGame", endButtons);
  newGameButton.setColors(#1d1c1a, #ffffff);
  allButtons.add(rematchButton);
  allButtons.add(newGameButton);

  Condition timeCondition = new Condition() { public boolean c() { return (gameState == MENU && timeControl); } };
  timeButtons[0] = new ArrayList<TimeButton>();
  timeButtons[1] = new ArrayList<TimeButton>();
  timeButtons[0].add(new TimeButton(37, 472, 48, 11, 5, 0, 0, 0, #f0f0f0, #26211b, #d1cfcf, true, timeCondition));
  timeButtons[0].add(new TimeButton(86, 472, 49, 11, 0, 5, 0, 0, #f0f0f0, #26211b, #d1cfcf, true, timeCondition));
  timeButtons[0].add(new TimeButton(142, 472, 49, 11, 5, 5, 0, 0, #f0f0f0, #26211b, #d1cfcf, true, timeCondition));
  timeButtons[0].add(new TimeButton(37, 533, 48, 10, 0, 0, 0, 5, #f0f0f0, #26211b, #d1cfcf, false, timeCondition));
  timeButtons[0].add(new TimeButton(86, 533, 49, 10, 0, 0, 5, 0, #f0f0f0, #26211b, #d1cfcf, false, timeCondition));
  timeButtons[0].add(new TimeButton(142, 533, 49, 10, 0, 0, 5, 5, #f0f0f0, #26211b, #d1cfcf, false, timeCondition));
  timeButtons[1].add(new TimeButton(227, 472, 48, 10, 5, 0, 0, 0, #26211b, #f0f0f0, #2d2d2a, true, timeCondition));
  timeButtons[1].add(new TimeButton(276, 472, 49, 10, 0, 5, 0, 0, #26211b, #f0f0f0, #2d2d2a, true, timeCondition));
  timeButtons[1].add(new TimeButton(332, 472, 49, 10, 5, 5, 0, 0, #26211b, #f0f0f0, #2d2d2a, true, timeCondition));
  timeButtons[1].add(new TimeButton(227, 533, 48, 10, 0, 0, 0, 5, #26211b, #f0f0f0, #2d2d2a, false, timeCondition));
  timeButtons[1].add(new TimeButton(276, 533, 49, 10, 0, 0, 5, 0, #26211b, #f0f0f0, #2d2d2a, false, timeCondition));
  timeButtons[1].add(new TimeButton(332, 533, 49, 10, 0, 0, 5, 5, #26211b, #f0f0f0, #2d2d2a, false, timeCondition));
  allButtons.addAll(timeButtons[0]);
  allButtons.addAll(timeButtons[1]);

  for (int i = 0; i < timeButtons.length; i++) {
    for (int j = 0; j < timeButtons[i].size(); j++) {
      timeButtons[i].get(j).setIndex(i, j % 3);
    }
  }

  presetButtons.add(new ImageButton(width-272, 465, 70, 70, 5, #272522, loadImage("icons/rapid.png"), false, "rapidPreset", hubCondition));
  presetButtons.add(new ImageButton(width-177, 465, 70, 70, 5, #272522, loadImage("icons/blitz.png"), false, "blitzPreset", hubCondition));
  presetButtons.add(new ImageButton(width-82, 465, 70, 70, 5, #272522, loadImage("icons/bullet.png"), false, "bulletPreset", hubCondition));
  allButtons.addAll(presetButtons);

  Condition humanWCondition = new Condition() { public boolean c() { return(gameState == GAME && !gameEnded && joueurs.get(0).name == "Humain"); } };
  Condition humanBCondition = new Condition() { public boolean c() { return(gameState == GAME && !gameEnded && joueurs.get(1).name == "Humain"); } };
  humanButton.add(new ImageButton(6, height - w - 117, 38, 38, 10, #272522, loadImage("icons/resign.png"), false, "resignWhite", humanWCondition));
  humanButton.add(new ImageButton(6, offsetY + w + 80, 38, 38, 10, #272522, loadImage("icons/resign.png"), false, "resignBlack", humanBCondition));
  humanButton.add(new ImageButton(offsetX-44, height - w - 117, 38, 38, 10, #272522, loadImage("icons/helpMove.png"), false, "helpMoveWhite", humanWCondition));
  humanButton.add(new ImageButton(offsetX-44, offsetY + w + 80, 38, 38, 10, #272522, loadImage("icons/helpMove.png"), false, "helpMoveBlack", humanBCondition));
  allButtons.addAll(humanButton);

  // Drag and drops
  Condition dragAndDropWCondition = new Condition() { public boolean c() { return (gameState == EDITOR && !showParameters && !showSavedPositions && addPiecesColor == 0); } };
  Condition dragAndDropBCondition = new Condition() { public boolean c() { return (gameState == EDITOR && !showParameters && !showSavedPositions && addPiecesColor == 1); } };

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
  int[] numSc1 = {0, 1, 2, 3, 4, 5, 6, 7, 16, 10};
  for (int i = 0; i < icons.length; i++) {
    iconButtons.add(new ShortcutButton(edgeSpacing + i*iconSize + i*spacingBetweenIcons, distanceFromTop, iconSize, icons[i], pause, iconCondition));
    iconButtons.get(i).setNumShortcut(numSc1[i]);
  }
  allButtons.addAll(iconButtons);

  // Icones de l'éditeur
  Condition editorCondition = new Condition() { public boolean c() { return gameState == EDITOR; } };
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
      savedFENSbuttons.add(new ButtonFEN(startX + size/2 + j*(size + espacementX), startY + size/2 + i*(size + espacementY), size, saveFENSimage[index], savedFENSname[index], index, fenCondition));
    }
  }
  allButtons.addAll(savedFENSbuttons);

  // Initialise les pièces
  pieces[0] = new ArrayList<Piece>();
  pieces[1] = new ArrayList<Piece>();

  // Initialise PreComputedData
  pc.init();

  // Place les pièces
  setPieces();
}

void draw() {

  ////////////////////////////////////////////////////////////////

  if (gameState == GAME) {
    background(49, 46, 43);

    // Actualise blockPlaying, qui empêche éventuellement un joueur de jouer
    updateBlockPlaying();

    // Bot vs humain
    if (engineToPlay) { joueurs.get(tourDeQui).play(); engineToPlay = false; }
    if (!blockPlaying && ((joueurs.get(0).name == "Humain" && joueurs.get(1).name != "Humain") || (joueurs.get(0).name != "Humain" && joueurs.get(1).name == "Humain"))) {
      if (joueurs.get(tourDeQui).name != "Humain") engineToPlay = true;
    }

    // Bot vs bot
    if (!gameEnded && play && (!useHacker || hackerPret)) {
      if (joueurs.get(0).name != "Humain" && joueurs.get(1).name != "Humain") {
        if (speed == 0) joueurs.get(tourDeQui).play();
        else if (frameCount % speed == 0) joueurs.get(tourDeQui).play();
      }
    }

    // Hacker
    if (useHacker && hackerPret  && !hackerAPImode) {
      if (play && !gameEnded && enPromotion == null) scanMoveOnBoard();

      if (gameEnded && !hackerWaitingToRestart && millis() - timeAtHackerEnd >= timeBeforeHackerRestart) hackStartGame();
      if (hackerWaitingToRestart && millis() - timeAtLastRestartTry >= hackerTestRestartCooldown) handleWaitForRestart();
    }

    if (MODE_SANS_AFFICHAGE && useHacker && hackerPret) return;

    // Titre
    surface.setTitle(name + " - " + j1 + " (" + ((joueurs.get(0).useIterativeDeepening) ? "ID" : j1depth) +  ") contre " + j2 + " (" + ((joueurs.get(1).useIterativeDeepening) ? "ID" : j2depth) + ")" + ((infos == "") ? "" : " - ") + infos);

    // Icones
    for (int i = 0; i < iconButtons.size(); i++) {
      ShortcutButton b = iconButtons.get(i);
      if (i == 7) b.show(play ? 0 : 1); // Play / Pause
      else b.show(0);
    }
    for (int i = 0; i < humanButton.size(); i++) {
      if (humanButton.get(i).isEnabled()) humanButton.get(i).show();
    }

    // Plateau
    drawPlayersInfos();
    updateBoard();
    for (Arrow b : bookArrows) b.show();
    if (showVariante) {
      for (Arrow arrow : varianteArrows) arrow.show();
    }
    if (bestMoveArrow != null) bestMoveArrow.show();

    // Promotion
    if (enPromotion != null) {
      fill(220, 220, 220, 200);
      rectMode(CORNER);
      rect(offsetX, offsetY, cols*w, rows*w);
      showPromoButtons();
    }

    // Écran de fin de partie
    if (!disableEndScreen && gameEnded && millis() - timeAtEnd > timeBeforeEndDisplay) {
      float dy = targetEndScreenY - yEndScreen;
      yEndScreen += dy * endScreenEasing;

      float rectX = 1.75*w + offsetX, rectW = 4.5*w, rectH = 3*w;
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

  else if (gameState == MENU) {
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

    positionEditor.show();
    if (useHacker) hackerButton.show();

    if (timeControl) {
      fill(#f0f0f0);
      stroke(#f0f0f0);
      rect(37, 480, 98, 55);
      rect(142, 480, 49, 55);
      fill(#26211b);
      textSize(30);
      textAlign(CENTER, CENTER);
      text(nf(times[0][0], 2) + ":" + nf(times[0][1], 2), 87, 504);
      text(nf(times[0][2], 2), 167, 504);

      fill(#26211b);
      stroke(#26211b);
      rect(227, 480, 98, 55);
      rect(332, 480, 49, 55);
      fill(#f0f0f0);
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

  else if (gameState == EDITOR) {
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
