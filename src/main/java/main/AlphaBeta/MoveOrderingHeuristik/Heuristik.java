package main.AlphaBeta.MoveOrderingHeuristik;

import main.Board;
import main.models.Move;
import main.models.Player;

import java.util.List;
import java.util.stream.Collectors;

public class Heuristik {
    // Zugsortierungsheuristiken (Move Ordering Heuristics)
    public static List<Move> sortMovesHeuristically(Board board, List<Move> moves, Player player) {
        return moves.stream()
                .sorted((m1, m2) -> Integer.compare(
                        scoreMove(board, m2, player),
                        scoreMove(board, m1, player)))
                .collect(Collectors.toList());
    }

    private static int scoreMove(Board board, Move move, Player player) {
        int score = 0;
        int index = move.to.y * 7 + move.to.x;

        // Prefer capturing opponent pieces
        long mask = 1L << (move.to.y * 7 + move.to.x);
        boolean isOpponentPiece = (player == Player.RED)
                ? (board.bluePieces & mask) != 0
                : (board.redPieces  & mask) != 0;
        if (isOpponentPiece) score += 10;

        // Prefer capturing guards
        boolean isGuard = (board.guards >>> index & 1L) == 1L;
        if (isGuard) score += 5;

        // Prefer moves with higher step count
        score += move.step;

        return score;
    }
}
