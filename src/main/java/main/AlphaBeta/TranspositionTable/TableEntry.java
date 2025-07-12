package main.AlphaBeta.TranspositionTable;

import main.models.Move;

public final class TableEntry {
    public static final byte EXACT = 0, LOWER = 1, UPPER = 2;

    public final int  depth;
    public final int  score;
    public final byte flag;
    public final Move bestMove;      // optional: for PV & move ordering

    public TableEntry(int depth, int score, byte flag, Move bestMove) {
        this.depth = depth;
        this.score = score;
        this.flag  = flag;
        this.bestMove  = bestMove;
    }
}