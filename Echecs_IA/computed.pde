class PreComputedData {
  boolean[][][][][] distanceTable = new boolean[6][8][8][8][8]; // piece index / grid[from][from] / grid[target][target]
  int[][] distanceFromCenter = new int[8][8]; // i, j
  int[][][][] tropismDistance = new int[8][8][8][8]; // i1, j1, i2, j2

  PreComputedData() { }

  void init() {
    this.initDistanceTable();
    this.initDistanceFromCenter();
    this.initTropismDistance();
  }

  void initDistanceTable() {
    // Magnifique
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

  void initDistanceFromCenter() {
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

  void initTropismDistance() {
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

  boolean getDistanceTable(int p, int fi, int fj, int ti, int tj) {
    return (this.distanceTable[p][fi][fj][ti][tj]);
  }

  int getDistanceFromCenter(int i, int j) {
    return (this.distanceFromCenter[i][j]);
  }

  int getTropismDistance(int i1, int i2, int j1, int j2) {
    return (this.tropismDistance[i1][i2][j1][j2]);
  }
}
