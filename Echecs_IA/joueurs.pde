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
      this.elo = str(int(random(1300, 1500)));
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

  void play() {
    if (name == "Humain") return;
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

  float iterativeDeepening() {
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

  float minimax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) { return 0; }

  SheepEval moyennemax(int depth, int plyFromRoot, float alpha, float beta, Move Cpere) { return new SheepEval(0, 0); }

  boolean tryPlayingBookMove() {
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
      joueurs.get(this.c).evals.add(0.00);
      cursor(ARROW);
      return true;
    }
    return false;
  }

  // Évaluation
  float Evaluation() { return 0; }

  float EvaluationRelative() {
    float eval = this.Evaluation();
    if (tourDeQui == 0) {
      return eval;
    } else {
      return -eval;
    }
  }

  int getManhattanDistanceBetweenKing() {
    int xDist = abs(rois[1].i - rois[0].i);
    int yDist = abs(rois[1].j - rois[0].j);
    return xDist + yDist;
  }

  float getEndGameKingEval(int friendlyMaterial, int opponentMaterial, Piece friendlyKing, Piece enemyKing) {
    if (friendlyMaterial > opponentMaterial + 150) {
      // Formule pas du tout copiée d'internet : 4,7 * CMD + 1,6 * (14 - MD)
      float eval = ( 4.7 * pc.getDistanceFromCenter(enemyKing.i, enemyKing.j) + 1.6 * (14 - this.getManhattanDistanceBetweenKing()) );
      return eval * endGameWeight;
    } else {
      return -pc.getDistanceFromCenter(friendlyKing.i, friendlyKing.j) * endGameWeight;
    }
  }

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

  ArrayList OrderMoves(ArrayList<Move> moves) {
    return moves;
  }

  // Statistiques
  void updateStats(float posEval) {
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
    joueurs.get(this.c).evals.add(posEval/100.0);
  }

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

  @Override
  void play() {
    if (!MODE_SANS_AFFICHAGE) cursor(WAIT);

    if (nbTour < 9) {
     if (this.tryPlayingBookMove()) return;
    }
    super.play();
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
  void play() {
    if (!MODE_SANS_AFFICHAGE) cursor(WAIT);
    if (nbTour < 5) {
     if (this.tryPlayingBookMove()) return;
    }
    super.play();
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

void arnaques() {
  int opponent = (int)pow(tourDeQui-1, 2);

  // Arnaque au temps
  if (joueurs.get(opponent).name == "LesMoutons") {
    if (ta.timers[tourDeQui].currentTime >= 45000 && random(1) <= 0.4) {
      timeCount++;
      ta.timers[tourDeQui].removeTime(5000);
    }
  }

  // Apparition
  if (joueurs.get(opponent).name == "LesMoutons" && (int)nbTour == tourPourApparition && endGameWeight <= 0.5) {
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
  if (random(1) <= 0.2) {
    float msgX = offsetX + random(0, 2)*w;
    float msgY = offsetX + random(0, 6)*w;
    sendMoutonMessage(moutonMessages[(int)random(0, moutonMessages.length)], msgX, msgY, 1500);
    messagesCount++;
  }

  // Missclick
  if (nbTour >= lastMissclick + missclickCooldown && random(1) <= 0.1) {
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
