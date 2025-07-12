package main.AlphaBeta;

import main.Board;
import main.Main;
import main.MoveHandler;
import main.models.Move;
import main.models.MoveContext;
import main.models.Player;

import java.util.List;

public class Minimax {
    public static int minimax(Board board, int depth, boolean maximizingPlayer, Player player) {
        Main.nodesVisited++;
        if (depth == 0 || board.generateMoves().isEmpty()) {
            return Evaluation.evaluate(board, player);
        }

        List<Move> moves = board.generateMoves();

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                MoveContext ctx = MoveHandler.applyMove(board, move);
                int eval = minimax(board, depth - 1, false, player);
                MoveHandler.undoMove(board, ctx);

                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                MoveContext ctx = MoveHandler.applyMove(board, move);
                int eval = minimax(board, depth - 1, true, player);
                MoveHandler.undoMove(board, ctx);

                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }

}
