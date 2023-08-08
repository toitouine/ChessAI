/////////////////////////////////////////////////////////////////

// Configurations diverses

final String name = "Échecs on java";
String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq"; // Position de départ par défaut

final boolean MODE_PROBLEME = false; // Activer le mode résolution de problèmes (ou pas)
final boolean TIME_CONTROL = true; // Activer le temps (ou pas)
final int TIME_WAIT_AT_START = 750; // Temps (en millisecondes) avant que les IAs commencent à jouer après le lancement de la partie
final int SOUND_CONTROL = 0; // Contrôle du son (0 = aucun / 1 = partie / 2 = musique)
final boolean SHOW_SEARCH_PIECES = false; // Affiche les pièces pendant la recherche

/////////////////////////////////////////////////////////////////

// Configurations du hacker [voir hacker config helper]

int hackerSite = CHESSCOM; // Hacker sur chess.com (CHESSCOM) ou lichess (LICHESS)
final boolean hackerSansFin = true; // Activer le hacker sans fin (ou pas), permet de relancer automatiquement les parties

final int hackerTestRestartCooldown = 1300; // Temps (ms) entre chaque scan du hacker pour relancer la partie
final int scansBetweenEndDetect = 50; // Nombre de scans entre chaque détection de fin de partie
final int waitsBetweenStartRetry = 25; // Nombre d'essais de relance de partie avant de relancer une nouvelle fois (anti-revanche sur chess.com)
final int timeBeforeHackerRestart = 3500; // Temps d'attente avant de redémarrer une partie
final int timeCopycatSize = 3; // Taille du tableau des deltaTimeHistory de time copycat

final Color coupChesscomWhite = new Color(246, 249, 87); // Couleur de surlignage des cases blanches sur chess.com [voir config helper]
final Color coupChesscomBlack = new Color(174, 195, 34); // Couleur de surlignage des cases noires sur chess.com [voir config helper]
final Color expectChesscomWhitePieceColor = new Color(234, 184, 99); // Couleur des pièces blanches de chess.com (pour l'auto calibration) [voir config helper]
final Color expectChesscomBlackPieceColor = new Color(42, 42, 42); // Couleur des pièces noires de chess.com (pour l'auto calibration) [voir config helper]

final Color coupLichessWhite = new Color(194, 202, 87); // Couleur de surlignage des cases blanches sur Lichess [voir config helper]
final Color coupLichessBlack = new Color(153, 147, 45); // Couleur de surlignage des cases noires sur Lichess [voir config helper]
final Color expectLichessWhitePieceColor = new Color(255, 255, 255); // Couleur des pièces blanches de Lichess (pour l'auto calibration) [voir config helper]
final Color expectLichessBlackPieceColor = new Color(42, 42, 42); // Couleur des pièces noires de Lichess (pour l'auto calibration) [voir config helper]
final Color endColorLichess = new Color(67, 107, 27); // Couleur du bouton de nouvelle partie de Lichess (quand la souris est dessus) [voir config helper]

final int HACKER_RATE = 5; // FPS du hacker (correspond entre autres au nombre de scans par seconde)
final boolean MODE_SANS_AFFICHAGE = false; // Afficher (ou pas) l'échiquier pendant le hacker
final int MINIMUM_PIXEL_DETECTION = 6; // Nombre minimum de pixels à la suite pour que l'auto-calibration accepte le point
final int TIME_COPYCAT_FIX = 830; // Temps (en ms) d'erreur de mesure en trop de time copycat (730 ms mesuré) [voir TimeCopycatFix]

/////////////////////////////////////////////////////////////////

// Configuration des IAs

// Pour ajouter une IA, créer toutes les configurations nécessaires (et les images) en étendant les tableaux et en spécifiant l'index (ajouter 1 au nombre d'IAs)
// Créer la classe de la nouvelle IA dans joueurs.pde en héritant de la classe IA
// Implémenter minimax et evaluation (et autres si besoin), modifier bestMoveFound et les statistiques pendant la recherche
// Référencer dans le constructeur de la classe Joueur la classe correspondant à la nouvelle IA

// Index des différentes IAs (ou humain) dans les tableaux de configurations (ne pas mélanger !)
final int HUMAIN_INDEX = 0;
final int LEMAIRE_INDEX = 1;
final int LESMOUTONS_INDEX = 2;
final int LOIC_INDEX = 3;
final int ANTOINE_INDEX = 4;
final int STOCKFISH_INDEX = 5;

final int CONSTANTE_DE_STOCKFISH = 3; // On ne sait pas

final int AI_NUMBER = 6; // Nombre d'IAs et humain différents
final String[] AI_NAME = {"Humain", "LeMaire", "LesMoutons", "Loic", "Antoine", "Stockfish"}; // Nom complet des joueurs
final String[] AI_CODE = {"humain", "lemaire", "lesmoutons", "loic", "antoine", "stockfish"}; // Nom des joueurs (utilisé pour les images)

final String[] AI_ELO = {"???", "3845", "1400", "-142", "100", "284"}; // Élo des différentes IAs
final String[] AI_TITLE = {"", "GM", "Mouton", "IM", "", "Noob"}; // Titre des différentes IAs
final int[] AI_OUVERTURE = {0, 10, 5, 0, 0, 0}; // Nombre maximum de coups du livre d'ouverture

final String[] AI_DESCRIPTION = { // Description de chaque IA
  "",
  "Très bon en ouverture et en finale",
  "Voleur, arnaqueur, tricheur, menaces en un !!",
  "Plutôt mauvais, préfère pater que mater",
  "Un jeu aléatoire de qualité",
  "Extrêmement difficile de perdre contre lui"
};
final String[] AI_VICTORY = { // Texte de victoire de chaque IA
  "",
  "Cmaire",
  "YOU LOUSE",
  "Tu t'es fait mater !",
  "Tu t'es fait mater !",
  "??!?"
};

/////////////////////////////////////////////////////////////////

// Autres

boolean attach = true; // Épingler la fenêtre par défaut (ou pas)
boolean stats = true; // Afficher les statistiques et informations pendant le programme
boolean details = true; // Afficher les statistiques détaillées

int[][] times = { // Temps par défaut
  {0, 0, 0}, // blancs : minutes, secondes, incrément
  {0, 0, 0}  // noirs : minutes, secondes, incrément
};

/////////////////////////////////////////////////////////////////

// Configuration de l'interface

int w = 70; // Taille d'une case (définit la taille de tout)
float pieceSize = w; // Taille d'une pièce
int offsetX = 95 * w/70; // Taille de la bande verticale à gauche
int offsetY = 50 * w/70; // Taille de la bande horizontale en haut
int gameWidth = cols * w + offsetX; // Largeur de la fenêtre de la partie
int gameHeight = rows * w + offsetY; // Hauteur de la fenêtre de la partie
int selectWidth = 1100; // Largeur de la page d'acceuil
int selectHeight = 460; // Hauteur de la page d'acceuil

int timeBeforeEndDisplay = 750; // Temps avant d'afficher l'écran de fin de partie en ms
float targetEndScreenY = 2.5*w + offsetY; // Position (hauteur) de fin de l'écran de fin de partie
float defaultEndScreenY = -210; // Position (hauteur) de départ de l'écran de fin de partie
float endScreenEasing = 0.04; // Vitesse de descente de l'écran de fin de partie

Point whiteTimePosition = new Point(30, 283); // Position du sélecteur de temps des blancs
Point blackTimePosition = new Point(selectWidth - 184, 283); // Position du sélecteur de temps des noirs

Color arrowDefaultColor = new Color(255, 192, 67); // Couleur des flèches (niveau 0 du dégradé)
Color arrowFinalColor = new Color(255, 0, 0); // Couleur des flèches (niveau 1 du dégradé)

/////////////////////////////////////////////////////////////////

// Gestionnaire de fens pour l'éditeur de position

String[] savedFENS = {
  "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq",
  "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq",
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

// Moutons

final boolean ENABLE_ARNAQUES = true; // Activer les arnaques des moutons (ou pas)
final int missclickCooldown = 6; // Nombre minimum de tour entre chaque missclick

final String[] moutonMessages = { // Liste des messages envoyés par les moutons
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

/////////////////////////////////////////////////////////////////

// Valeurs positionnelles du maire

final int[] kingSafetyPenalty = {
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

final float[][] maireKnightGrid = {
  {-50, -40, -30, -30,  -30, -30, -40, -50},
  {-40, -20,   0,   5,    0,   5, -20, -40},
  {-30,   0,  10,  15,   15,  10,   0, -30},
  {-30,   0,  15,  20,   20,  15,   0, -30},
  {-30,   0,  15,  20,   20,  15,   0, -30},
  {-30,   0,  10,  15,   15,  10,   0, -30},
  {-40, -20,   0,   5,    0,   5, -20, -40},
  {-50, -40, -30, -30,  -30, -30, -40, -50}
};

final float[][] maireQueenGrid = {
  {-20, -10, -10, -5, 0, -10, -10, -20},
  {-10,   0,   0,  0, 0,   5,   0, -10},
  {-10,   0,   5,  5, 5,   5,   5, -10},
  { -5,   0,   5,  5, 5,   5,   0,  -5},
  { -5,   0,   5,  5, 5,   5,   0,  -5},
  {-10,   0,   5,  5, 5,   5,   5, -10},
  {-10,   0,   0,  0, 0,   5,   0, -10},
  {-20, -10, -10, -5, 0, -10, -10, -20}
};

final float[][] maireBishopGrid = {
  {-20, -10, -10, -10, -10, -10, -10, -20},
  {-10,   0,   0,   5,   0,  10,   5, -10},
  {-10,   0,   5,   5,  10,  10,   0, -10},
  {-10,   0,  10,  10,  10,  10,   0, -10},
  {-10,   0,  10,  10,  10,  10,   0, -10},
  {-10,   0,   5,   5,  10,  10,   0, -10},
  {-10,   0,   0,   5,   0,  10,   5, -10},
  {-20, -10, -10, -10, -10, -10, -10, -20},
};

final float[][] mairePawnGrid = {
  {100, 50, 10,  5,  0,   5,   5, 0},
  {100, 50, 10,  5,  0,  -5,  10, 0},
  {100, 50, 20, 10,  0, -10,  10, 0},
  {100, 50, 30, 25, 20,   0, -20, 0},
  {100, 50, 30, 25, 20,   0, -20, 0},
  {100, 50, 20, 10,  0, -10,  10, 0},
  {100, 50, 10,  5,  0,  -5,  10, 0},
  {100, 50, 10,  5,  0,   5,   5, 0}
};
final float[][] mairePawnGridEnd = {
  {0, 100, 60, 40, 20, 10, 10, 0},
  {0, 100, 60, 40, 20, 10, 10, 0},
  {0, 100, 40, 30, 20, 10, 10, 0},
  {0, 100, 30, 20, 18, 10, 10, 0},
  {0, 100, 30, 20, 18, 10, 10, 0},
  {0, 100, 40, 30, 20, 10, 10, 0},
  {0, 100, 60, 40, 20, 10, 10, 0},
  {0, 100, 60, 40, 20, 10, 10, 0}
};

final float[][] maireKingGrid = {
  {-30, -30, -30, -30, -20, -10, 20,  20},
  {-40, -40, -40, -40, -30, -20, 20,  35},
  {-40, -40, -40, -40, -30, -20,  0, -10},
  {-50, -50, -50, -50, -40, -20,  0, -10},
  {-50, -50, -50, -50, -40, -20,  0, -10},
  {-40, -40, -40, -40, -30, -20,  0, -10},
  {-40, -40, -40, -40, -30, -20, 20,  35},
  {-30, -30, -30, -30, -20, -10, 20,  20},
};

final float[][] maireRookGrid = {
  {0,  5, -5, -5, -5, -5, -5, 0},
  {0, 10,  0,  0,  0,  0,  0, 0},
  {0, 10,  0,  0,  0,  0,  0, 0},
  {0, 10,  0,  0,  0,  0,  0, 5},
  {0, 10,  0,  0,  0,  0,  0, 5},
  {0, 10,  0,  0,  0,  0,  0, 0},
  {0, 10,  0,  0,  0,  0,  0, 0},
  {0,  5, -5, -5, -5, -5, -5, 0}
};

final float[][] zeroArray = {
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

// Valeurs positionnelles de Loic

final float[][] loicKingGrid = {
  {-30, -30, -30, -30, -20, -10,  5,  0},
  {-40, -40, -40, -40, -30, -20,  5,  0},
  {-40, -40, -40, -40, -30, -20,  0,  0},
  {-50, -50, -50, -50, -40, -20,  0, 20},
  {-50, -50, -50, -50, -40, -20,  0, 20},
  {-40, -40, -40, -40, -30, -20,  0,  0},
  {-40, -40, -40, -40, -30, -20,  5,  0},
  {-30, -30, -30, -30, -20, -10,  5,  0},
};

final float[][] loicQueenGrid = {
  {-20, -10, -10, -5, 0, -10, -10, -20},
  {-10,   0,   0,  0, 0,   5,   0, -10},
  {-10,   0,   5,  5, 5,   5,  -5, -10},
  { -5,   0,   5,  5, 5,   5,   0,  20},
  { -5,   0,   5,  5, 5,   5,   0,  20},
  {-10,   0,   5,  5, 5,   5,  -5, -10},
  {-10,   0,   0,  0, 0,   5,   0, -10},
  {-20, -10, -10, -5, 0, -10, -10, -20}
};

final float[][] loicBishopGrid = {
  {-20, -10, - 10,  -10, -10, -10, -10, -20},
  {-10,   0,    0,  -15,   0, -10,  40,   0},
  {-10,   0,  -15,  -15, -10, -10, -20, -10},
  {-10,   0,  -10,  -10, -10, -10,   0, -10},
  {-10,   0,  -10,  -10, -10, -10,   0, -10},
  {-10,   0,  -15,  -15, -10, -10, -20, -10},
  {-10,   0,    0,  -15,   0, -10,  40,   0},
  {-20, -10,  -10,  -10, -10, -10, -10, -20},
};

final float[][] loicKnightGrid = {
  {-50, -40, -30, -30, -30, -30, -40,  -50},
  {-40, -20,   0,   0,   0,   0, -20,  -40},
  {-30,   0,   0,   0,   0, -10,   0,  -30},
  {-30,   0,   0,   0,   0,   0,  30,  -30},
  {-30,   0,   0,   0,   0,   0,  30,  -30},
  {-30,   0,   0,   0,   0, -10,   0,  -30},
  {-40, -20,   0,   0,   0,   5, -20,  -40},
  {-50, -40, -30, -30, -30, -30, -40,  -50}
};

final float[][] loicRookGrid = {
  {0,  5, -5, -5, -5, -5, -5, 10},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  0,  0,  0,  0,  0,  0,  0},
  {0,  5, -5, -5, -5, -5, -5, 10}
};

final float[][] loicPawnGrid = {
  {100, 50, 10, 5, 0,  20,   5, 0},
  {100, 50, 10, 5, 0,  20,  10, 0},
  {100, 50, 20, 5, 0, -10,  20, 0},
  {100, 50, 25, 5, 0,  35, -20, 0},
  {100, 50, 25, 5, 0,  35, -20, 0},
  {100, 50, 20, 5, 0, -10,  20, 0},
  {100, 50, 10, 5, 0,  20,  10, 0},
  {100, 50, 10, 5, 0,  20,   5, 0}
};

/////////////////////////////////////////////////////////////////

final int maireEvalArray[] = {100000, 900, 500, 330, 320, 100}; // Valeurs de chaque pièce selon LeMaire
final int loicEvalArray[] = {100000, 900, 150, 300, 300, 100}; // Valeurs de chaque pièce selon Loic

final float[][] mairePosArray[] = {maireKingGrid, maireQueenGrid, maireRookGrid, maireBishopGrid, maireKnightGrid, mairePawnGrid};
final float[][] mairePosArrayEnd[] = {zeroArray, zeroArray, zeroArray, zeroArray, zeroArray, mairePawnGridEnd};
final float[][] loicPosArray[] = {loicKingGrid, loicQueenGrid, loicRookGrid, loicBishopGrid, loicKnightGrid, loicPawnGrid};

/////////////////////////////////////////////////////////////////
