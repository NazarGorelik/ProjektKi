package main.AlphaBeta;

/* Zobrist.java */
import main.Board;
import main.models.Piece;
import main.models.Player;

import java.util.Random;

public final class Zobrist {

    private static final long[][][] PIECE_KEYS = new long[7][7][14]; // 7×7×(max piece types)
    private static final long SIDE_KEY; // whose move?

    static {
        Random rng = new Random(2025); // fixed seed = deterministic
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                for (int t = 0; t < 14; t++) // 7 heights × 2 colours
                    PIECE_KEYS[r][c][t] = rng.nextLong();
        SIDE_KEY = rng.nextLong();
    }

    private static int pieceIndex(Piece p) {
        if (p.player == null) return -1;
        int base = p.isGuard ? 0 : p.height; // guard index 0, towers 1-7
        return base + (p.player == Player.BLUE ? 7 : 0); // RED 0-7, BLUE 7-13
    }

    public static long hash(Board b) {
        long h = 0;
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                Piece p = b.board[r][c];
                int idx = pieceIndex(p);
                if (idx >= 0) h ^= PIECE_KEYS[r][c][idx];
            }
        }
        if (b.toMove == Player.BLUE) h ^= SIDE_KEY;
        return h;
    }
}
