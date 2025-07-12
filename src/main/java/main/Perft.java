package main;

import main.models.Move;
import main.models.MoveContext;

import java.util.List;

public class Perft {
    public static long perft(Board board, int depth) {
        if (depth == 0) {
            return 1;
        }
        long nodes = 0;
        List<Move> moves = board.generateMoves();

        for (Move move : moves) {
            MoveContext ctx = MoveHandler.applyMove(board, move);
            nodes += perft(board, depth - 1);
            MoveHandler.undoMove(board, ctx);
        }

        return nodes;
    }
}
