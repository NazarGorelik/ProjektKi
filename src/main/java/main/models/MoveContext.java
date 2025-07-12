package main.models;

public class MoveContext {
    public Position from, to;
    public Piece originalFromPiece, originalToPiece;
    public Player previousPlayer;

    public MoveContext(Position from, Position to, Piece origFrom, Piece origTo, Player prevPlayer) {
        this.from = from;
        this.to = to;
        this.originalFromPiece = origFrom;
        this.originalToPiece = origTo;
        this.previousPlayer = prevPlayer;
    }
}
