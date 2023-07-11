/////////////////////////////////////////////////////////////////

// TODO :

// Réparer time copycat
// (Note : pour l'instant deltaTimeHistory est en secondes, modifiable dans getTimeCopycat())
// Editeur de position : Trait et roques
// Hacker : anti-annulation Lichess
// Auto calibration
// La brebis / Statistiques bot
// Mode tournoi
// Tester améliorations (killer moves, voir taux d'overwrite dans transpositions...)

/////////////////////////////////////////////////////////////////

// Bibliothèques

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

PImage[] imageArrayB;
PImage[] imageArrayN;

PImage leftArrow;
PImage rightArrow;

PImage j1Img;
PImage j2Img;
PImage j1ImgEnd;
PImage j2ImgEnd;

PImage[] icons = new PImage[10];
PImage[] editorIcons = new PImage[9];
PImage[] savedFENSimage = new PImage[7];
PImage chess;
PImage bot;
PImage botLarge;
PImage warning;
PImage moutonAlertImg;
PImage chesscomLogo;
PImage lichessLogo;

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

ArrayList<Button> allButtons = new ArrayList<Button>(); // ArrayList de tous les boutons de l'interface

ArrayList<Piece> piecesToDisplay = new ArrayList<Piece>();
ArrayList<Piece>[] pieces = new ArrayList[2];
Piece[] currentEnPassantable = {null, null};
ArrayList<Joueur> joueurs = new ArrayList<Joueur>();

ArrayList<PromotionButton> promoButtons = new ArrayList<PromotionButton>();
ArrayList<ImageSelector> selectors = new ArrayList<ImageSelector>();
ArrayList<ShortcutButton> iconButtons = new ArrayList<ShortcutButton>();
ArrayList<ShortcutButton> editorIconButtons = new ArrayList<ShortcutButton>();
ArrayList<TextButton> hubButtons = new ArrayList<TextButton>();
ArrayList<ButtonFEN> savedFENSbuttons = new ArrayList<ButtonFEN>();
ArrayList<DragAndDrop>[] addPiecesButtons = new ArrayList[2];
ArrayList<TimeButton>[] timeButtons = new ArrayList[2];
ArrayList<ImageButton> presetButtons = new ArrayList<ImageButton>();
ArrayList<ImageButton> humainButton = new ArrayList<ImageButton>();

ArrayList<String> book = new ArrayList<String>();
ArrayList<Arrow> varianteArrows = new ArrayList<Arrow>();
ArrayList<Arrow> allArrows = new ArrayList<Arrow>();

ArrayList<String> positionHistory = new ArrayList<String>();
ArrayList<Move> movesHistory = new ArrayList<Move>();
ArrayList<Long> zobristHistory = new ArrayList<Long>();

CircleToggleButton addPiecesColorSwitch;
ImageButton positionEditor;
ImageButton hackerButton;
ToggleImage siteButton;
Piece pieceSelectionne = null;
Piece enPromotion = null;
TextButton rematchButton;
TextButton newGameButton;
DragAndDrop enAjoutPiece = null;
Slider s1, s2, t1, t2;
Cell lastCellRightClicked;
Arrow lastArrowDrawn;

Shortcut sc = new Shortcut();

/////////////////////////////////////////////////////////////////

// Variables pour la partie
int cols = 8;
int rows = 8;
int tourDeQui = 0; // Dakin
int playDelay = 0;

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
boolean useTime = false;
boolean pointDeVue = true;
int gameState;
int winner = -1;
int timeAtEnd = 0;
int rewindCount = 0;
String endReason = "";
String alert = "";
int alertTime = 0;
long alertStarted = 0;
int addPiecesColor = 0;
int timeAtGameStart = 0;

// Les Moutons !
String messageMouton = "";
int messageMoutonStarted = 0;
int messageMoutonTime = 0;
int tourPourApparition = 10;
int missclickCount = 0, appearCount = 0, timeCount = 0, messagesCount = 0;
Point alertPos = new Point();
boolean missclickDragNextMove = false;
float lastMissclick = 0;

final int CHESSCOM = 0;
final int LICHESS = 1;

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

  j1 = AI_NAME[HUMAIN_INDEX];
  j2 = AI_NAME[HUMAIN_INDEX];

  // Initialise le hacker
  try {
    hacker = new Robot();
  } catch(Exception e) {
    e.printStackTrace();
  }

  // Importe les sons
  if (SOUND_CONTROL >= 1) {
    start_sound = new SoundFile(this, "sons/start.wav");
    castle_sound = new SoundFile(this, "sons/castle.wav");
    check_sound = new SoundFile(this, "sons/check.wav");
    mat_sound = new SoundFile(this, "sons/mat.wav");
    move_sound = new SoundFile(this, "sons/move.wav");
    nulle_sound = new SoundFile(this, "sons/nulle.wav");
    prise_sound = new SoundFile(this, "sons/prise.wav");
    enPassant = new SoundFile(this, "sons/enPassant.mp3");
  }
  if (SOUND_CONTROL >= 2) {
    pachamama = new SoundFile(this, "sons/pachamama.mp3");
    diagnostic = new SoundFile(this, "sons/diagnostic.mp3");
    violons = new SoundFile(this, "sons/violons.mp3");
    violons.play(); violons.loop();
  }

  // Importe les images
  initImages();

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
  initGUI();

  // Texte de départ
  println("—————————————————————");
  println(name + ", Antoine Mechulam");
  println("(https://github.com/toitouine/ChessAI)");
  println();
  println("IAs disponibles :");
  for (int i = 1; i < AI_NUMBER; i++) {
    println(" - " + AI_NAME[i] + " : " + AI_DESCRIPTION[i]);
  }
  println(" ");
  println("Profondeur (recherche classique et quiet) et temps (Iterative Deepening) ajustables avec les sliders du menu.");
  println("Voir fichier configs.pde pour les options / paramètres");
  println("Appuyer sur H pour afficher l'aide (raccourcis claviers)");
  println();
  println("/!\\ La direction rejette toute responsabilité en cas de CPU détruit par ce programme ou d'ordinateur brulé.");
  println("—————————————————————");

  // Initialise les pièces
  pieces[0] = new ArrayList<Piece>();
  pieces[1] = new ArrayList<Piece>();

  // Initialise PreComputedData
  pc.init();

  // Place les pièces
  setPieces();

  // Démarre le menu
  gameState = MENU;
}

void draw() {

  /////////////////////////////////////////////////////////////////

  if (gameState == GAME) {
    background(49, 46, 43);

    // Bot vs humain
    if (engineToPlay) {
      joueurs.get(tourDeQui).play();
      engineToPlay = false;
    }
    if (!blockPlaying() && isAIvsHumain() && !isHumainTurn()) engineToPlay = true;

    // Bot vs bot
    if (!gameEnded && play && (!useHacker || hackerState != CALIBRATION)) {
      if (!isHumain(0) && !isHumain(1) && (nbTour > 1 || millis()-timeAtGameStart >= TIME_WAIT_AT_START)) {
        if (playDelay == 0) joueurs.get(tourDeQui).play();
        else if (frameCount % playDelay == 0) joueurs.get(tourDeQui).play();
      }
    }

    // Hacker
    if (useHacker && hackerState != CALIBRATION) {
      if (hackerState == WAITING_TO_RESTART) handleWaitForRestart();
      if (hackerState == END && millis() - timeAtHackerEnd >= timeBeforeHackerRestart) hackStartGame();
      if (hackerState == INGAME && play) scanMoveOnBoard();
    }

    // Affichages / Interface
    if (MODE_SANS_AFFICHAGE && useHacker && hackerState != CALIBRATION) return;

    drawGameInterface();
    if (alert != "") displayAlert();
    if (infoBox != "") drawInfoBox(infoBox);
    if (messageMouton != "") displayMoutonAlert();
  }

  ////////////////////////////////////////////////////////////////

  else if (gameState == MENU) {
    background(49, 46, 43);

    fill(255);
    textSize(30);
    textAlign(LEFT, LEFT);
    text("Échecs on Java :", 20, 45);
    strokeWeight(2);
    stroke(255);
    line(20, 51, 253, 51);

    fill(255);
    textAlign(LEFT, LEFT);
    textSize(15);
    text(startFEN, 10, selectHeight-10);

    positionEditor.show();
    if (useHacker) hackerButton.show();
    if (TIME_CONTROL) drawTimeButtons();
    for (TextButton b : hubButtons) b.show();
    for (int i = 0; i < presetButtons.size(); i++) presetButtons.get(i).show();
    for (ImageSelector s : selectors) s.show();
  }

  ////////////////////////////////////////////////////////////////

  else if (gameState == EDITOR) {
    background(49, 46, 43);
    drawBoard();

    if (infoBox != "") drawInfoBox(infoBox);
    if (showSavedPositions) drawSavedPosition();
    if (showParameters) drawParameters();

    if (infos != "") surface.setTitle(name + " - Editeur de position - " + infos);
    else surface.setTitle(name + " - Editeur de position");

    for (ShortcutButton sb : editorIconButtons) sb.show();
    for (DragAndDrop d : addPiecesButtons[addPiecesColor]) d.show();
    addPiecesColorSwitch.show();
  }

  /////////////////////////////////////////////////////////////////

}
