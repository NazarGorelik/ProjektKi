package main.AlphaBeta;

import main.Board;
import main.MoveHandler;
import main.models.Move;
import main.models.MoveContext;
import main.models.Player;

import java.util.List;

public class BasicAI {
    public static Move findBestMove(Board board, int depth) {
        Player currentPlayer = board.getToMove();
        List<Move> moves = board.generateMoves();

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move : moves) {
            MoveContext ctx = MoveHandler.applyMove(board, move);
            int score = AlphaBetaAI.alphaBeta(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, currentPlayer);
            MoveHandler.undoMove(board, ctx);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    public static Move findBestMoveMinimax(Board board, int depth) {
        Player currentPlayer = board.getToMove();
        List<Move> moves = board.generateMoves();

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move : moves) {
            MoveContext ctx = MoveHandler.applyMove(board, move);
            int score = Minimax.minimax(board, depth - 1, false, currentPlayer);
            MoveHandler.undoMove(board, ctx);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }
}
