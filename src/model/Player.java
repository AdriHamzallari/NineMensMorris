package model;

public class Player {
    private final int id;
    private final String name;
    private final java.awt.Color color;
    private int piecesToPlace;
    private int piecesOnBoard;

    public Player(int id, String name, java.awt.Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.piecesToPlace = 9;
        this.piecesOnBoard = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public java.awt.Color getColor() { return color; }
    public int getPiecesToPlace() { return piecesToPlace; }
    public int getPiecesOnBoard() { return piecesOnBoard; }
    public int getTotalPieces() { return piecesToPlace + piecesOnBoard; }

    public void placePiece() {
        if (piecesToPlace > 0) { piecesToPlace--; piecesOnBoard++; }
    }
    public void removePiece() {
        if (piecesOnBoard > 0) piecesOnBoard--;
    }

    public void reset() {
        piecesToPlace = 9;
        piecesOnBoard = 0;
    }
}
