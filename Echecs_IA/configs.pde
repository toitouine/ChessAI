/////////////////////////////////////////////////////////////////

// Configurations générales

String name = "Echecs on java";

String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq"; // Position de départ
// String startFEN = "r3k1nr/p2p1ppp/1p1Rp3/8/5N2/4B3/PPP2PPP/2K2R2 b kq"; // Partie Bete-a-corne - LeMaire
// String startFEN = "r1b1kbnr/ppppqppp/2n5/4p2Q/2B5/4P3/PPPP1PPP/RNB1K1NR w KQkq"; // Partie Lewis - LeMaire
// String startFEN = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w"; // Vecteur vitesse
// String startFEN = "r5k1/6pp/5b2/4N3/8/q7/5PPP/3Q1RK1 w"; // Mat à l'étouffée
// String startFEN = "8/1RK5/8/3k4/8/8/8/8 w"; // Finale facile : Mat roi-tour
// String startFEN = "8/6p1/8/3k4/8/3K4/8/8 w"; // Finale facile : Opposition
// String startFEN = "8/3K4/4P3/8/8/8/6k1/7q w"; // Finale difficile: Check-check-check
// String startFEN = "8/k7/3p4/p2P1p2/P2P1P2/8/8/K7 w"; // Finale difficile : table de transposition

/////////////////////////////////////////////////////////////////

// Sound control et time control

int soundControl = 0; //0 = aucun, 1 = partie, 2 = musique
boolean attach = true;
boolean stats = true;
boolean details = true;
boolean timeControl = false;
int[][] times = {
  {0, 0, 0}, //blancs : minutes, secondes, incrément
  {0, 0, 0}  //noirs : minutes, secondes, incrément
};

/////////////////////////////////////////////////////////////////

// Fenêtre principale

int w = 70; //100 pour Windo£ws
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
float targetEndScreenY = 2.5*w + offsetY;
float endScreenEasing = 0.07;
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

// Moutons et autres

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

/////////////////////////////////////////////////////////////////

// Constantes

int CONSTANTE_DE_STOCKFISH = 3; //ou 5
float TOTAL_DEPART = 3200.0; //moyenne de la somme du matériel des blancs et des noirs (sur la position de départ)

int ROI_INDEX = 0;
int DAME_INDEX = 1;
int TOUR_INDEX = 2;
int FOU_INDEX = 3;
int CAVALIER_INDEX = 4;
int PION_INDEX = 5;

/////////////////////////////////////////////////////////////////

// Valeurs pour l'évaluation

int[] kingSafety = {
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
  {-30, -30, -30, -30, -20, -10, 20, 20},
  {-40, -40, -40, -40, -30, -20, 20, 35},
  {-40, -40, -40, -40, -30, -20,  0, 0},
  {-50, -50, -50, -50, -40, -20,  0,  0},
  {-50, -50, -50, -50, -40, -20,  0,  0},
  {-40, -40, -40, -40, -30, -20,  0, 0},
  {-40, -40, -40, -40, -30, -20, 20, 35},
  {-30, -30, -30, -30, -20, -10, 20, 20},
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
