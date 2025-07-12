package main.models;

public class Piece {
    public Player player;
    public int height;
    public boolean isGuard;

    public Piece(Player player, int height, boolean isGuard) {
        this.player = player;
        this.height = height;
        this.isGuard = isGuard;
    }

    public Piece(){}

    public static Piece clonePiece(Piece p) {
        if (p == null || p.player == null) return new Piece();
        return new Piece(p.player, p.height, p.isGuard);
    }

    public String toString() {
        if(this.player == null){
            return "null";
        }
        return player.toString() + Integer.toString(height) + isGuard;
    }
}
