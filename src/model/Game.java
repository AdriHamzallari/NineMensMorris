package model;

public class Game {
    private Board board;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private GameState state;
    private int selectedPos = -1;
    private String message = "";
    private Player winner = null;

    // AI support
    private boolean vsComputer = false;
    private AI ai = null;
    private boolean aiThinking = false;

    public Game() {
        board = new Board();
        player1 = new Player(1, "Lojtari 1", new java.awt.Color(220, 80, 60));
        player2 = new Player(2, "Lojtari 2", new java.awt.Color(60, 140, 220));
        reset();
    }

    public void setVsComputer(boolean vsComputer) {
        this.vsComputer = vsComputer;
        if (vsComputer) {
            player2 = new Player(2, "Kompjuteri", new java.awt.Color(60, 140, 220));
            ai = new AI(2);
        } else {
            player2 = new Player(2, "Lojtari 2", new java.awt.Color(60, 140, 220));
            ai = null;
        }
        reset();
    }

    public boolean isVsComputer() { return vsComputer; }
    public boolean isAiThinking()  { return aiThinking; }

    public void reset() {
        board.reset();
        player1.reset();
        player2.reset();
        currentPlayer = player1;
        state = GameState.PLACING;
        selectedPos = -1;
        winner = null;
        aiThinking = false;
        message = currentPlayer.getName() + " vendos gurin e parë!";
    }

    public boolean handleClick(int pos) {
        if (state == GameState.GAME_OVER) return false;
        if (vsComputer && currentPlayer == player2) return false;

        boolean changed;
        if (state == GameState.PLACING) {
            changed = handlePlacing(pos);
        } else if (state == GameState.REMOVING) {
            changed = handleRemoving(pos);
        } else {
            changed = handleMoving(pos);
        }
        return changed;
    }

    public boolean triggerAiMove() {
        if (!vsComputer) return false;
        if (state == GameState.GAME_OVER) return false;
        if (currentPlayer != player2) return false;
        if (aiThinking) return false;

        aiThinking = true;
        int[] move = ai.getBestMove(state, board.getCells(),
                                     player2.getPiecesToPlace(),
                                     player1.getPiecesToPlace());
        aiThinking = false;

        if (move == null || move[0] == -1) return false;

        switch (state) {
            case PLACING  -> executeAiPlace(move[0]);
            case REMOVING -> executeAiRemove(move[0]);
            case MOVING, FLYING -> executeAiMove(move[0], move[1]);
        }
        return true;
    }

    private void executeAiPlace(int pos) {
        if (!board.isEmpty(pos)) return;
        board.setCell(pos, player2.getId());
        player2.placePiece();
        if (board.isMill(pos, player2.getId())) {
            state = GameState.REMOVING;
            message = "Kompjuteri formoi mulli! Hiq një gur.";
        } else {
            checkTransitionToMoving();
        }
    }

    private void executeAiRemove(int pos) {
        if (board.getCell(pos) != player1.getId()) return;
        board.clearCell(pos);
        player1.removePiece();
        if (checkWin()) return;
        checkTransitionToMoving();
    }

    private void executeAiMove(int from, int to) {
        if (board.getCell(from) != player2.getId()) return;
        if (!board.isEmpty(to)) return;
        board.clearCell(from);
        board.setCell(to, player2.getId());
        selectedPos = -1;
        if (board.isMill(to, player2.getId())) {
            state = GameState.REMOVING;
            message = "Kompjuteri formoi mulli! Hiq një gur.";
        } else {
            if (checkWin()) return;
            switchPlayer();
        }
    }

    private boolean handlePlacing(int pos) {
        if (!board.isEmpty(pos)) {
            message = "Pozicioni është i zënë!";
            return false;
        }
        board.setCell(pos, currentPlayer.getId());
        currentPlayer.placePiece();

        if (board.isMill(pos, currentPlayer.getId())) {
            state = GameState.REMOVING;
            message = currentPlayer.getName() + " formoi mulli! Hiq një gur të armikut.";
            return true;
        }

        checkTransitionToMoving();
        return true;
    }

    private boolean handleRemoving(int pos) {
        int opponentId = (currentPlayer.getId() == 1) ? 2 : 1;
        if (board.getCell(pos) != opponentId) {
            message = "Duhet të heqësh gurin e armikut!";
            return false;
        }
        if (board.isMill(pos, opponentId) && !board.allInMills(opponentId)) {
            message = "Nuk mund të heqësh gurin që është në mulli!";
            return false;
        }
        board.clearCell(pos);
        getOpponent().removePiece();

        if (checkWin()) return true;

        checkTransitionToMoving();
        return true;
    }

    private boolean handleMoving(int pos) {
        boolean flying = (state == GameState.FLYING);

        if (selectedPos == -1) {
            if (board.getCell(pos) != currentPlayer.getId()) {
                message = "Zgjidh gurin tënd!";
                return false;
            }
            selectedPos = pos;
            message = "Zgjedhur pozicioni " + pos + ". Zgjidh destinacionin.";
            return true;
        } else {
            if (pos == selectedPos) {
                selectedPos = -1;
                message = "Zgjedhja u anulua.";
                return true;
            }
            if (board.getCell(pos) == currentPlayer.getId()) {
                selectedPos = pos;
                message = "Zgjedhur pozicioni " + pos + ". Zgjidh destinacionin.";
                return true;
            }
            if (!board.isEmpty(pos)) {
                message = "Pozicioni i zënë!";
                return false;
            }
            if (!flying && !board.areNeighbors(selectedPos, pos)) {
                message = "Lëvizja e pavlefshme! Duhet të lëvizësh te fqinji.";
                return false;
            }

            board.clearCell(selectedPos);
            board.setCell(pos, currentPlayer.getId());
            selectedPos = -1;

            if (board.isMill(pos, currentPlayer.getId())) {
                state = GameState.REMOVING;
                message = currentPlayer.getName() + " formoi mulli! Hiq një gur të armikut.";
                return true;
            }

            if (checkWin()) return true;
            switchPlayer();
            return true;
        }
    }

    private void checkTransitionToMoving() {
        boolean p1Done = player1.getPiecesToPlace() == 0;
        boolean p2Done = player2.getPiecesToPlace() == 0;

        if (p1Done && p2Done) {
            switchPlayerState();
            return;
        }
        switchPlayer();
    }

    private void switchPlayerState() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
        int pid = currentPlayer.getId();
        int total = board.countPieces(pid);
        if (total == 3) {
            state = GameState.FLYING;
        } else {
            state = GameState.MOVING;
        }
        if (!board.canPlayerMove(pid, state == GameState.FLYING)) {
            winner = getOpponent();
            state = GameState.GAME_OVER;
            message = winner.getName() + " FITOI! Armiku nuk mund të lëvizë.";
        } else {
            message = currentPlayer.getName() + (state == GameState.FLYING ? " fluturon!" : " lëviz gurin.");
        }
    }

    private void switchPlayer() {
        if (state == GameState.PLACING) {
            currentPlayer = (currentPlayer == player1) ? player2 : player1;
            message = currentPlayer.getName() + " vendos gurin.";
        } else {
            switchPlayerState();
        }
    }

    private boolean checkWin() {
        Player opponent = getOpponent();
        int opponentId = opponent.getId();
        boolean opponentDone = opponent.getPiecesToPlace() == 0;

        if (opponentDone && (board.countPieces(opponentId) + opponent.getPiecesToPlace()) < 3) {
            winner = currentPlayer;
            state = GameState.GAME_OVER;
            message = currentPlayer.getName() + " FITOI! Armiku ka < 3 gurë.";
            return true;
        }
        return false;
    }

    private Player getOpponent() {
        return (currentPlayer == player1) ? player2 : player1;
    }

    public Board getBoard()               { return board; }
    public Player getPlayer1()            { return player1; }
    public Player getPlayer2()            { return player2; }
    public Player getCurrentPlayer()      { return currentPlayer; }
    public GameState getState()           { return state; }
    public int getSelectedPos()           { return selectedPos; }
    public String getMessage()            { return message; }
    public Player getWinner()             { return winner; }
}
