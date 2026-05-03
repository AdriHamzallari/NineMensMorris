package model;

public class Board {
    public static final int SIZE = 24;

    // Adjacency list for each position
    private static final int[][] NEIGHBORS = {
        {1, 9},           // 0
        {0, 2, 4},        // 1
        {1, 14},          // 2
        {4, 10},          // 3
        {1, 3, 5, 7},     // 4
        {4, 13},          // 5
        {7, 11},          // 6
        {4, 6, 8},        // 7
        {7, 12},          // 8
        {0, 10, 21},      // 9
        {3, 9, 11, 18},   // 10
        {6, 10, 15},      // 11
        {8, 13, 17},      // 12
        {5, 12, 14, 20},  // 13
        {2, 13, 23},      // 14
        {11, 16},         // 15
        {15, 17, 19},     // 16
        {12, 16},         // 17
        {10, 19},         // 18
        {16, 18, 20, 22}, // 19
        {13, 19},         // 20
        {9, 22},          // 21
        {19, 21, 23},     // 22
        {14, 22}          // 23
    };

    // All possible mills (3 in a row)
    private static final int[][] MILLS = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
        {9, 10, 11}, {12, 13, 14},
        {15, 16, 17}, {18, 19, 20}, {21, 22, 23},
        {0, 9, 21}, {3, 10, 18}, {6, 11, 15},
        {1, 4, 7}, {16, 19, 22},
        {2, 14, 23}, {5, 13, 20}, {8, 12, 17}
    };

    private int[] cells; // 0 = empty, 1 = player1, 2 = player2

    public Board() {
        cells = new int[SIZE];
    }

    public void reset() {
        cells = new int[SIZE];
    }

    public int getCell(int pos) { return cells[pos]; }

    public boolean isEmpty(int pos) { return cells[pos] == 0; }

    public void setCell(int pos, int playerId) { cells[pos] = playerId; }

    public void clearCell(int pos) { cells[pos] = 0; }

    public boolean areNeighbors(int from, int to) {
        for (int n : NEIGHBORS[from]) if (n == to) return true;
        return false;
    }

    public boolean isMill(int pos, int playerId) {
        for (int[] mill : MILLS) {
            boolean inMill = false;
            for (int m : mill) if (m == pos) { inMill = true; break; }
            if (!inMill) continue;
            boolean allSame = true;
            for (int m : mill) if (cells[m] != playerId) { allSame = false; break; }
            if (allSame) return true;
        }
        return false;
    }

    public boolean formsMillWith(int pos, int playerId) {
        int old = cells[pos];
        cells[pos] = playerId;
        boolean result = isMill(pos, playerId);
        cells[pos] = old;
        return result;
    }

    public boolean allInMills(int playerId) {
        for (int i = 0; i < SIZE; i++) {
            if (cells[i] == playerId && !isMill(i, playerId)) return false;
        }
        return true;
    }

    public boolean canPlayerMove(int playerId, boolean flying) {
        if (flying) {
            for (int i = 0; i < SIZE; i++) if (cells[i] == 0) return true;
            return false;
        }
        for (int i = 0; i < SIZE; i++) {
            if (cells[i] == playerId) {
                for (int n : NEIGHBORS[i]) {
                    if (cells[n] == 0) return true;
                }
            }
        }
        return false;
    }

    public int countPieces(int playerId) {
        int count = 0;
        for (int c : cells) if (c == playerId) count++;
        return count;
    }

    public int[] getCells() { return cells.clone(); }
}
