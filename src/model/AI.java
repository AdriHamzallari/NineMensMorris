package model;

import java.util.*;

/**
 * Computer player for Nine Men's Morris.
 * Uses minimax with alpha-beta pruning.
 * Depth is tuned per phase: placing=3, moving=5, flying=4.
 */
public class AI {

    private static final int INF = 1_000_000;
    private static final int WIN_SCORE = 100_000;

    // All possible mills — kept in sync with Board
    private static final int[][] MILLS = {
        {0,1,2},{3,4,5},{6,7,8},
        {9,10,11},{12,13,14},
        {15,16,17},{18,19,20},{21,22,23},
        {0,9,21},{3,10,18},{6,11,15},
        {1,4,7},{16,19,22},
        {2,14,23},{5,13,20},{8,12,17}
    };

    private static final int[][] NEIGHBORS = {
        {1,9},{0,2,4},{1,14},{4,10},{1,3,5,7},{4,13},
        {7,11},{4,6,8},{7,12},{0,10,21},{3,9,11,18},
        {6,10,15},{8,13,17},{5,12,14,20},{2,13,23},{11,16},
        {15,17,19},{12,16},{10,19},{16,18,20,22},{13,19},
        {9,22},{19,21,23},{14,22}
    };

    // Position weights — central/connective positions are stronger
    private static final int[] POS_WEIGHT = {
        0,1,0,1,2,1,0,1,0,
        1,2,1,2,2,1,2,1,
        0,1,0,1,2,1,0,1,0
    };

    private final int aiId;       // player id for the AI (always 2)
    private final int humanId;    // player id for human (always 1)

    public AI(int aiPlayerId) {
        this.aiId    = aiPlayerId;
        this.humanId = (aiPlayerId == 1) ? 2 : 1;
    }

    /**
     * Returns the best move for the AI given current game state.
     * For PLACING: returns position to place.
     * For MOVING/FLYING: returns int[]{from, to}.
     * For REMOVING: returns position to remove.
     */
    public int[] getBestMove(GameState state, int[] cells,
                              int aiPiecesToPlace, int humanPiecesToPlace) {
        switch (state) {
            case PLACING  -> { return bestPlaceMove(cells, aiId, humanId, aiPiecesToPlace, humanPiecesToPlace); }
            case MOVING   -> { return bestMoveMove(cells, aiId, humanId, false, aiPiecesToPlace, humanPiecesToPlace); }
            case FLYING   -> { return bestMoveMove(cells, aiId, humanId, true, aiPiecesToPlace, humanPiecesToPlace); }
            case REMOVING -> { return bestRemoveMove(cells, aiId, humanId); }
            default       -> { return new int[]{-1}; }
        }
    }

    // ─── PLACING ────────────────────────────────────────────────────────────

    private int[] bestPlaceMove(int[] cells, int ai, int human,
                                 int aiToPlace, int humanToPlace) {
        int bestScore = -INF;
        int bestPos   = -1;
        for (int pos = 0; pos < 24; pos++) {
            if (cells[pos] != 0) continue;
            int[] next = cells.clone();
            next[pos] = ai;
            int score = minimax(next, 3, false, -INF, INF,
                                ai, human, aiToPlace - 1, humanToPlace, false);
            if (score > bestScore) { bestScore = score; bestPos = pos; }
        }
        return new int[]{bestPos};
    }

    // ─── MOVING / FLYING ────────────────────────────────────────────────────

    private int[] bestMoveMove(int[] cells, int ai, int human,
                                boolean flying, int aiToPlace, int humanToPlace) {
        int bestScore = -INF;
        int bestFrom  = -1, bestTo = -1;
        for (int from = 0; from < 24; from++) {
            if (cells[from] != ai) continue;
            int[] dests = flying ? allEmpty(cells) : emptyNeighbors(cells, from);
            for (int to : dests) {
                int[] next = cells.clone();
                next[from] = 0;
                next[to]   = ai;
                int score = minimax(next, 5, false, -INF, INF,
                                    ai, human, aiToPlace, humanToPlace, false);
                if (score > bestScore) { bestScore = score; bestFrom = from; bestTo = to; }
            }
        }
        return new int[]{bestFrom, bestTo};
    }

    // ─── REMOVING ───────────────────────────────────────────────────────────

    private int[] bestRemoveMove(int[] cells, int ai, int human) {
        int bestScore = -INF;
        int bestPos   = -1;
        for (int pos = 0; pos < 24; pos++) {
            if (cells[pos] != human) continue;
            if (isMill(cells, pos, human) && !allInMills(cells, human)) continue;
            int[] next = cells.clone();
            next[pos] = 0;
            int score = evaluate(next, ai, human);
            if (score > bestScore) { bestScore = score; bestPos = pos; }
        }
        // fallback: remove any human piece (all are in mills)
        if (bestPos == -1) {
            for (int pos = 0; pos < 24; pos++) {
                if (cells[pos] == human) { bestPos = pos; break; }
            }
        }
        return new int[]{bestPos};
    }

    // ─── MINIMAX ────────────────────────────────────────────────────────────

    private int minimax(int[] cells, int depth, boolean isHuman,
                        int alpha, int beta,
                        int ai, int human,
                        int aiToPlace, int humanToPlace,
                        boolean humanFlying) {
        // Terminal checks
        int aiCount    = count(cells, ai);
        int humanCount = count(cells, human);
        if (aiToPlace == 0 && aiCount < 3)    return -WIN_SCORE;
        if (humanToPlace == 0 && humanCount < 3) return WIN_SCORE;
        if (depth == 0) return evaluate(cells, ai, human);

        if (!isHuman) {
            // AI turn
            boolean aiFlying = (aiToPlace == 0 && aiCount == 3);
            int bestScore = -INF;
            for (int from = 0; from < 24; from++) {
                if (cells[from] != ai) continue;
                int[] dests = aiFlying ? allEmpty(cells) : emptyNeighbors(cells, from);
                for (int to : dests) {
                    int[] next = cells.clone();
                    next[from] = 0;
                    next[to]   = ai;
                    int score = minimax(next, depth - 1, true, alpha, beta,
                                        ai, human, aiToPlace, humanToPlace, humanFlying);
                    bestScore = Math.max(bestScore, score);
                    alpha = Math.max(alpha, score);
                    if (beta <= alpha) break;
                }
            }
            return bestScore == -INF ? evaluate(cells, ai, human) : bestScore;
        } else {
            // Human turn
            int bestScore = INF;
            for (int from = 0; from < 24; from++) {
                if (cells[from] != human) continue;
                int[] dests = humanFlying ? allEmpty(cells) : emptyNeighbors(cells, from);
                for (int to : dests) {
                    int[] next = cells.clone();
                    next[from] = 0;
                    next[to]   = human;
                    boolean hFlying2 = (humanToPlace == 0 && count(next, human) == 3);
                    int score = minimax(next, depth - 1, false, alpha, beta,
                                        ai, human, aiToPlace, humanToPlace, hFlying2);
                    bestScore = Math.min(bestScore, score);
                    beta = Math.min(beta, score);
                    if (beta <= alpha) break;
                }
            }
            return bestScore == INF ? evaluate(cells, ai, human) : bestScore;
        }
    }

    // ─── EVALUATION ─────────────────────────────────────────────────────────

    private int evaluate(int[] cells, int ai, int human) {
        int score = 0;

        int aiPieces    = count(cells, ai);
        int humanPieces = count(cells, human);

        // Piece advantage
        score += (aiPieces - humanPieces) * 10;

        // Mills formed
        score += countMills(cells, ai)    *  6;
        score -= countMills(cells, human) *  6;

        // Potential mills (2-in-a-row with open third)
        score += countPotential(cells, ai)    * 3;
        score -= countPotential(cells, human) * 3;

        // Mobility
        score += mobility(cells, ai)    * 1;
        score -= mobility(cells, human) * 1;

        // Position weights
        for (int i = 0; i < 24; i++) {
            if (cells[i] == ai)    score += POS_WEIGHT[i % POS_WEIGHT.length];
            else if (cells[i] == human) score -= POS_WEIGHT[i % POS_WEIGHT.length];
        }

        return score;
    }

    private int countMills(int[] cells, int player) {
        int c = 0;
        for (int[] mill : MILLS) {
            if (cells[mill[0]] == player && cells[mill[1]] == player && cells[mill[2]] == player) c++;
        }
        return c;
    }

    private int countPotential(int[] cells, int player) {
        int c = 0;
        for (int[] mill : MILLS) {
            int mine = 0, empty = 0;
            for (int pos : mill) {
                if (cells[pos] == player) mine++;
                else if (cells[pos] == 0) empty++;
            }
            if (mine == 2 && empty == 1) c++;
        }
        return c;
    }

    private int mobility(int[] cells, int player) {
        int moves = 0;
        for (int i = 0; i < 24; i++) {
            if (cells[i] != player) continue;
            for (int n : NEIGHBORS[i]) {
                if (cells[n] == 0) moves++;
            }
        }
        return moves;
    }

    // ─── HELPERS ────────────────────────────────────────────────────────────

    private int count(int[] cells, int player) {
        int c = 0;
        for (int cell : cells) if (cell == player) c++;
        return c;
    }

    private int[] allEmpty(int[] cells) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 24; i++) if (cells[i] == 0) list.add(i);
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private int[] emptyNeighbors(int[] cells, int pos) {
        List<Integer> list = new ArrayList<>();
        for (int n : NEIGHBORS[pos]) if (cells[n] == 0) list.add(n);
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private boolean isMill(int[] cells, int pos, int player) {
        for (int[] mill : MILLS) {
            boolean inMill = false;
            for (int m : mill) if (m == pos) { inMill = true; break; }
            if (!inMill) continue;
            boolean allSame = true;
            for (int m : mill) if (cells[m] != player) { allSame = false; break; }
            if (allSame) return true;
        }
        return false;
    }

    private boolean allInMills(int[] cells, int player) {
        for (int i = 0; i < 24; i++) {
            if (cells[i] == player && !isMill(cells, i, player)) return false;
        }
        return true;
    }
}
