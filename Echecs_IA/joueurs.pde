/////////////////////////////////////////////////////////////////

String j1;
String j2;
int j1depth = 3;
int j2depth = 3;
int j1Quiet = 30;
int j2Quiet = 30;
int j1Time = 1000;
int j2Time = 1000;

int[] totalScores = new int[AI_NUMBER];
float[] scores = new float[AI_NUMBER];

float TOTAL_DEPART = 3200.0;

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

    this.index = getIAIndex(this.name);
    this.title = AI_TITLE[this.index];
    this.victoryTitle = AI_VICTORY[this.index];
    this.elo = AI_ELO[this.index];

    if (name.equals(AI_NAME[LESMOUTONS_INDEX])) this.elo = str(int(random(stringToInt(this.elo) - 100, stringToInt(this.elo) + 100)));
    if (name.equals(AI_NAME[HUMAIN_INDEX])) this.victoryTitle = (this.c == 0) ? "Victoire des blancs" : "Victoire des noirs";

    if       (name.equals(AI_NAME[ANTOINE_INDEX]))    player = new Antoine(this.c);
    else if  (name.equals(AI_NAME[LEMAIRE_INDEX]))    player = new LeMaire(this.c, this.depth, this.maxDepth, this.useIterativeDeepening);
    else if  (name.equals(AI_NAME[LESMOUTONS_INDEX])) player = new LesMoutons(this.c, this.depth, this.maxDepth, this.useIterativeDeepening);
    else if  (name.equals(AI_NAME[STOCKFISH_INDEX]))  player = new Stockfish(this.c, this.depth, this.useIterativeDeepening);
    else if  (name.equals(AI_NAME[LOIC_INDEX]))       player = new Loic(this.c, this.depth, this.useIterativeDeepening);
    else if (!name.equals(AI_NAME[HUMAIN_INDEX]))     error("Joueur()", "aucun joueur ou joueur inconnu demandé");
  }

  void play() {
    if (name.equals(AI_NAME[HUMAIN_INDEX])) return;
    player.play();
  }

  float getScore() {
    return scores[this.index];
  }

  void addScore(float add) {
    scores[this.index] += add;
  }

  int getTotalScore() {
    return totalScores[this.index];
  }

  void addTotalScore(int add) {
    totalScores[this.index] += add;
  }
}

/////////////////////////////////////////////////////////////////

class IA {
  int c, depth, maxQuietDepth;
  boolean useIterativeDeepening = false;
  boolean inFastSearch = false;

  float time;
  int depthSearched;
  int numPos, numQuiet;
  int numMoves, numCaptures, numQuietCuts, numTranspositions;
  int firstPlyMoves, higherPlyFromRoot;
  int[] cuts;  int cutsFirst;

  float Infinity = 999999999;
  Move bestMoveFound = null;

  void play() {
    if (gameEnded || stopSearch) return;

    if (!(MODE_SANS_AFFICHAGE && useHacker)) cursor(WAIT);

    if (nbTour <= AI_OUVERTURE[joueurs.get(this.c).index]) {
     if (this.tryPlayingBookMove()) return;
    }

    // Recherche du meilleur coup
    float posEval;
    if (this.useIterativeDeepening) posEval = this.iterativeDeepening();
    else posEval = this.findBestMove();

    stopSearch = false;
    cursor(ARROW);

    // Ne joue pas le coup (et n'affiche pas les statistiques) si la partie est terminée
    if (gameEnded) return;

    // Joue le coup
    this.bestMoveFound.play();

    // Affichage des statistiques dans la console et l'interface
    if (!(MODE_SANS_AFFICHAGE && useHacker)) this.updateStats(posEval);

    // Reset les statistiques pour la prochaine recherche
    this.resetStats();
  }

  /////////////////////////////////////////////////////////////////

  // Recherche rapide (pour les aides notamment)
  Move getBestMove(int time) {
    sa.setTime(this.c, time);
    sa.startSearch(this.c);
    this.inFastSearch = true;
    Move bestMove = null;

    for (int d = 1; d < 1000; d++) {
      this.bestMoveFound = null;
      this.cuts = new int[d];

      float eval;
      if (this instanceof LesMoutons) eval = -this.moyennemax(d, 0, -Infinity, Infinity, null).eval;
      else eval = -this.minimax(d, 0, -Infinity, Infinity, null);

      if (stopSearch) break;
      bestMove = this.bestMoveFound;
    }

    this.inFastSearch = false;
    sa.setTime(this.c, sa.savedTimes[this.c]);
    stopSearch = false;
    return bestMove;
  }

  // Recherche à une profondeur donnée uniquement
  float findBestMove() {
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

  // Iterative Deepening : Recherche le plus loin jusque plus de temps de réflexion
  float iterativeDeepening() {
    // Sauvegardes des statistiques
    Move lastBestMove = null;
    float lastEval = 0;
    int lastNumPos = 0, lastNumQuiet = 0, lastNumMoves = 0, lastNumCaptures = 0, lastNumQuietCuts = 0, lastNumTranspositions = 0;
    int lastFirstPlyMoves = 0, lastHigherPlyFromRoot = 0;
    int[] lastCuts = {0};  int lastCutsFirst = 0;

    // Gestion du temps
    int timeToPlay = sa.savedTimes[this.c]; // en millisecondes

    if (useHacker && hackerState != CALIBRATION && nbTour > 1) timeToPlay = getTimeCopycat();
    else if (MODE_PROBLEME) timeToPlay = 10000000;

    if (timeToPlay <= 20) timeToPlay = 20;

    sa.setTime(this.c, timeToPlay);
    sa.startSearch(this.c);

    // Iterative deepening
    for (int d = 1; d < 10000; d++) {
      // Pour éviter de sortir trop vite de la boucle en cas de position "évidente" (répétition immédiate)
      if (d == 1001) d = 1000;

      if (!(MODE_SANS_AFFICHAGE && useHacker)) this.resetStats();
      this.cuts = new int[d];

      // effectue la recherche à la profondeur
      float eval;
      if (this instanceof LesMoutons) {
        SheepEval sheep = this.moyennemax(d, 0, -Infinity, Infinity, null);
        eval = -sheep.eval;
      } else {
        eval = -this.minimax(d, 0, -Infinity, Infinity, null);
      }

      // Si la recherche a été interrompue par search controller (ou défaite)
      if (gameEnded) sa.endSearch();
      if (stopSearch) {
        if (!(MODE_SANS_AFFICHAGE && useHacker)) {
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
        }
        this.bestMoveFound = lastBestMove;
        return -lastEval;
      }

      // sauvegarde les résultats et statistiques
      lastEval = eval;
      lastBestMove = this.bestMoveFound;

      if (!(MODE_SANS_AFFICHAGE && useHacker)) {
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
      }

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

  // Retourne le temps à jouer en fonction de celui de l'adversaire (avec le hacker) en millisecondes
  int getTimeCopycat() {
    int time;

    int deltaTime = millis() - lastMoveTime;
    printArray(deltaTimeHistory);

    if (deltaTimeHistory.size() < timeCopycatSize) {
      time = sa.savedTimes[this.c] - floor(random(sa.savedTimes[this.c]/1.5, sa.savedTimes[this.c]/3));
    }
    else {
      int index = floor(random(0, timeCopycatSize));
      int prevTime = deltaTimeHistory.remove(index);
      println("prevTime (avant) : " + prevTime);
      prevTime -= TIME_COPYCAT_FIX;
      println("prevTime (après) : " + prevTime);

      if (prevTime >= 0) {
        // La formule prend une valeur en seconde et renvoie en ms
        float x = (float)prevTime/1000;
        print("x :", x, " ");
        time = ceil((((random(1)*100000) % ((x - x/2)*1000)) /1000 + x/2) * 1000);
      }
      else time = 20;
      println("time ms : " + time);
      println(" ");
    }

    if (useHacker && nbTour > 1) deltaTimeHistory.add(deltaTime);

    return max(time, 20);
  }

  // Fonction de recherche classique
  float minimax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) { return 0; }

  // Fonction de recherche pour les moutons
  SheepEval moyennemax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) { return new SheepEval(0, 0); }

  // Essaye de jouer un coup du livre d'ouverture (renvoie true si réussi / false si impossible)
  boolean tryPlayingBookMove() {
    ArrayList<String> moves = getMovesFromFen(generateFEN());

    if (moves.size() > 0 && !gameEnded) {
      int timeToWait = 250;
      if (useHacker) getTimeCopycat(); // Pour sauvegarder les temps dans l'array
      delay(timeToWait);

      this.bestMoveFound = playMoveFromBook(moves);
      if (stats && !(MODE_SANS_AFFICHAGE && useHacker)) {
        println("[BOT] " + joueurs.get(this.c).name + " : " + "Book");
        println("");
      }
      sa.setEvals("Book", this.c);
      sa.setBestMoves(getPGNString(this.bestMoveFound), this.c);
      sa.setDepths("/", this.c);
      sa.setPositions("/", this.c);
      sa.setTris("/", this.c);
      sa.setTranspositions("/", this.c);
      sa.setTimeDisplays(str(timeToWait) + " ms", this.c);
      joueurs.get(this.c).lastEval = "Book";
      joueurs.get(this.c).evals.add(0.00);
      cursor(ARROW);
      return true;
    }
    return false;
  }

  /////////////////////////////////////////////////////////////////

  // Évaluation statique de la position
  float Evaluation() { return 0; }

  // Évaluation statique de la position en fonction de tourDeQui
  float EvaluationRelative() {
    float eval = this.Evaluation();
    if (tourDeQui == 0) {
      return eval;
    } else {
      return -eval;
    }
  }

  // Renvoie la distance entre les rois blancs et noirs (sans utiliser les diagonales)
  int getManhattanDistanceBetweenKing() {
    int xDist = abs(rois[1].i - rois[0].i);
    int yDist = abs(rois[1].j - rois[0].j);
    return xDist + yDist;
  }

  // Renvoie une évaluation des positions en finales
  float getEndGameKingEval(int friendlyMaterial, int opponentMaterial, Piece friendlyKing, Piece enemyKing) {
    if (friendlyMaterial > opponentMaterial + 150) {
      // Formule pas du tout copiée d'internet : 4,7 * CMD + 1,6 * (14 - MD)
      float eval = ( 4.7 * pc.getDistanceFromCenter(enemyKing.i, enemyKing.j) + 1.6 * (14 - this.getManhattanDistanceBetweenKing()) );
      return eval * endGameWeight;
    } else {
      return -pc.getDistanceFromCenter(friendlyKing.i, friendlyKing.j) * endGameWeight;
    }
  }

  // Renvoie une évaluation de la sécurité du roi (ou pas) sous forme de pénalité
  float getKingSafetyEval(int friendly, int opponent) {
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

  // Trie les coups du meilleur au moins bon (classement heuristique)
  ArrayList OrderMoves(ArrayList<Move> moves) {
    return moves;
  }

  /////////////////////////////////////////////////////////////////

  // Affiche les statistiques et les ajoute aux applets
  void updateStats(float posEval) {
    // Calculs des statistiques
    if (tourDeQui == 0) posEval = -posEval; // On inverse car le coup a déjà été joué et c'est une évaluation relative
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

    if (showVariante) allArrows.addAll(varianteArrows);

    // Affichage des statistiques de la console
    if (stats) {
      print("[BOT] " + joueurs.get(this.c).name + " : "
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
    joueurs.get(this.c).evals.add(posEval/100.0);
  }

  // Ajoute les statistiques à search controller
  void updateSearchController(float posEval, float tri) {
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

  // Réinitialise les statistiques
  void resetStats() {
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

  @Override
  void play() {
    if (nbTour <= AI_OUVERTURE[joueurs.get(this.c).index]) {
     if (this.tryPlayingBookMove()) return;
    }

    // Génération des coups légaux
    ArrayList<Move> moves = new ArrayList<Move>();
    moves = generateAllLegalMoves(this.c, true, true);

    // Recherche du MEILLEUR coup
    int moveIndex = floor(random(0, moves.size()));
    if (moves.size() != 0) moves.get(moveIndex).play();

    // Stats
    float eval = random(-10, 10);

    sa.setEvals(evalToStringMaire(eval), this.c);
    sa.setDepths(str(floor(random(-10, 10))), this.c);
    sa.setPositions(formatInt(floor(random(10, 10000))), this.c);
    sa.setTris(roundNumber(random(0, 1), 2), this.c);
    sa.setTranspositions(formatInt(floor(random(0, 10000))), this.c);
    sa.setTimeDisplays(str(floor(random(10, 3000))) + " ms", this.c);

    joueurs.get(this.c).lastEval = roundNumber(eval, 3);
    joueurs.get(this.c).evals.add(eval);

    println("[BOT] " + joueurs.get(this.c).name + " : " + getPGNString(moves.get(moveIndex)));
    println();
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

  @Override
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

  @Override
  float Evaluation() {
    float[] Evals = {0, 0};

    for (int i = 0; i < 2; i++) {
      int opponent = opponent(i);

      Evals[i] += materials[i];

      for (int j = 0; j < pieces[i].size(); j++) {
        Evals[i] += pieces[i].get(j).mairePosEval;
      }

      Evals[i] -= this.getKingSafetyEval(i, opponent);
      Evals[i] += this.getEndGameKingEval(materials[i], materials[opponent], rois[i], rois[opponent]);
    }

    return (Evals[0] - Evals[1]);
  }

  @Override
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
    if (plyFromRoot > 1 && checkFastRepetition(zobrist.hash)) {
      // tt.Store(zobrist.hash, 0, null, depth, plyFromRoot, EXACT);
      return 0;
    }
    if (plyFromRoot <= 1 && checkRepetition(zobrist.hash)) {
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
    if (plyFromRoot == 1 && !this.inFastSearch) sa.incrementSearchTracker(this.c);

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

  float searchAllCaptures(float alpha, float beta, int plyFromRoot) {
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

  @Override
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

  SheepEval EvaluationMouton() {
    float[] Evals = {0, 0};

    for (int i = 0; i < 2; i++) {
      int opponent = opponent(i);

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

  SheepEval EvaluationMoutonRelative() {
    SheepEval evaluation = this.EvaluationMouton();
    if (tourDeQui == 0) {
      return evaluation;
    } else {
      return new SheepEval(-evaluation.moyenne, -evaluation.eval);
    }
  }

  SheepEval moyennemax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) {

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
    if (plyFromRoot == 1 && !this.inFastSearch) sa.incrementSearchTracker(this.c);

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

void arnaques() {
  int opponent = opponent(tourDeQui);

  // Arnaque au temps
  if (isMouton(opponent)) {
    if (ta.timers[tourDeQui].currentTime >= 45000 && random(1) <= 0.4) {
      timeCount++;
      ta.timers[tourDeQui].removeTime(5000);
    }
  }

  // Apparition
  if (isMouton(opponent) && (int)nbTour == tourPourApparition && endGameWeight <= 0.5) {
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
      if (grid[1][j].piece == null) {
        sendMoutonMessage(moutonMessages[floor(random(0, moutonMessages.length))], offsetX, cacheY, 1500);
        pieces[opponent].add(new Cavalier(1, j, opponent));
        materials[opponent] += 320;
        tourPourApparition += tourAdd;
        return;
      }
      if (grid[6][j].piece == null) {
        sendMoutonMessage(moutonMessages[floor(random(0, moutonMessages.length))], offsetX+2*w, cacheY, 1500);
        pieces[opponent].add(new Cavalier(6, j, opponent));
        materials[opponent] += 320;
        tourPourApparition += tourAdd;
        return;
      }
    }
    if (cblanc_bishops < 1) {
      if (opponent == 0) {
        if (grid[5][j].piece == null) {
          sendMoutonMessage(moutonMessages[floor(random(0, moutonMessages.length))], offsetX, cacheY, 1500);
          pieces[opponent].add(new Fou(5, j, opponent));
          materials[opponent] += 330;
          tourPourApparition += tourAdd;
          return;
        }
      } else {
        if (grid[2][j].piece == null) {
          sendMoutonMessage(moutonMessages[floor(random(0, moutonMessages.length))], offsetX, cacheY, 1500);
          pieces[opponent].add(new Fou(2, j, opponent));
          materials[opponent] += 330;
          tourPourApparition += tourAdd;
          return;
        }
      }
    }
    if (cnoir_bishops < 1) {
      if (opponent == 0) {
        if (grid[2][j].piece == null) {
          sendMoutonMessage(moutonMessages[floor(random(0, moutonMessages.length))], offsetX, cacheY, 1500);
          pieces[opponent].add(new Fou(2, j, opponent));
          materials[opponent] += 330;
          tourPourApparition += tourAdd;
          return;
        }
      } else {
        if (grid[5][j].piece == null) {
          sendMoutonMessage(moutonMessages[floor(random(0, moutonMessages.length))], offsetX, cacheY, 1500);
          pieces[opponent].add(new Fou(5, j, opponent));
          materials[opponent] += 330;
          tourPourApparition += tourAdd;
          return;
        }
      }
    }
    if (rois[opponent].roquable == 1 && rooks < 2) {
      if (grid[0][j].piece == null) {
        sendMoutonMessage(moutonMessages[floor(random(0, moutonMessages.length))], offsetX, cacheY, 1500);
        pieces[opponent].add(new Tour(0, j, opponent));
        materials[opponent] += 500;
        tourPourApparition += tourAdd;
        return;
      }
      if (grid[7][j].piece == null) {
        sendMoutonMessage(moutonMessages[floor(random(0, moutonMessages.length))], offsetX+2*w, cacheY, 1500);
        pieces[opponent].add(new Tour(7, j, opponent));
        materials[opponent] += 500;
        tourPourApparition += tourAdd;
        return;
      }
    }

    messagesCount--;
    appearCount--;

    tourPourApparition += 2;
  }

  // Messages
  if (random(1) <= 0.2) {
    float msgX = offsetX + random(0, 2)*w;
    float msgY = offsetX + random(0, 6)*w;
    sendMoutonMessage(moutonMessages[floor(random(0, moutonMessages.length))], msgX, msgY, 1500);
    messagesCount++;
  }

  // Missclick
  if (nbTour >= lastMissclick + missclickCooldown && random(1) <= 0.1) {
    lastMissclick = nbTour;
    missclickDragNextMove = true;
  }
}

void missclick(int i, int j) {
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
  if (i < 0 || i >= cols || j < 0 || j >= rows) return;

  if (grid[i][j].possibleMove != null) {
    grid[i][j].possibleMove.play();
    missclickCount++;
    pieceSelectionne = null;
    missclickDragNextMove = false;
  }
}

/////////////////////////////////////////////////////////////////

class Loic extends IA {

  Loic(int c, int d, boolean useID) {
    this.c = c;
    this.depth = d;
    this.useIterativeDeepening = useID;
  }

  int countMaterial(int c) {
    int material = 0;
    for (int i = 0; i < pieces[c].size(); i++) {
      material += pieces[c].get(i).loicEval;
    }
    return material;
  }

  @Override
  ArrayList OrderMoves(ArrayList<Move> moves) {
    for (int i = 0; i < moves.size(); i++) {
      Move m = moves.get(i);

      if (m.capture != null) {
        m.scoreGuess += 10*(m.capture.loicEval - m.piece.loicEval);;
      }
    }

    return selectionSortMoves(moves);
  }

  @Override
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

  @Override
  float minimax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) {

    if (stopSearch || gameEnded) return 0;

    this.numPos++;

    if (depth == 0) {
      return this.EvaluationRelative();
    }

    ArrayList<Move> moves = generateAllLegalMoves(tourDeQui, true, true);
    moves = this.OrderMoves(moves);
    this.numMoves += moves.size();
    if (plyFromRoot == 0) this.firstPlyMoves += moves.size();
    if (plyFromRoot == 1 && !this.inFastSearch) sa.incrementSearchTracker(this.c);

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
    // if (plyFromRoot == 1 && checkRepetition(zobrist.hash)) {
    if (checkRepetition(zobrist.hash)) {
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

  @Override
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

  @Override
  float minimax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) {

    if (stopSearch || gameEnded) return 0;

    this.numPos++;

    if (depth == 0) {
      return this.EvaluationRelative();
    }

    ArrayList<Move> moves = generateAllLegalMoves(tourDeQui, true, true);
    moves = this.OrderMoves(moves);
    this.numMoves += moves.size();
    if (plyFromRoot == 0) this.firstPlyMoves += moves.size();
    if (plyFromRoot == 1 && !this.inFastSearch) sa.incrementSearchTracker(this.c);

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

int searchMoves(int depth) {

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

int searchCaptures(int depth) {

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
