package main.AlphaBeta;

import main.Board;
import main.Main;
import main.MoveHandler;
import main.models.Move;
import main.models.MoveContext;
import main.models.Player;

import java.util.List;

public class AlphaBetaAI {
    public static int alphaBeta(Board board, int depth, int alpha, int beta, boolean maximizingPlayer, Player player) {
        Main.nodesVisited++;
        if (depth == 0 || board.generateMoves().isEmpty()) {
            return Evaluation.evaluate(board, player);
        }

        List<Move> moves = board.generateMoves();

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                MoveContext ctx = MoveHandler.applyMove(board, move);
                int eval = alphaBeta(board, depth - 1, alpha, beta, false, player);
                MoveHandler.undoMove(board, ctx);

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // β cutoff
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                MoveContext ctx = MoveHandler.applyMove(board, move);
                int eval = alphaBeta(board, depth - 1, alpha, beta, true, player);
                MoveHandler.undoMove(board, ctx);

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // α cutoff
            }
            return minEval;
        }
    }
}
