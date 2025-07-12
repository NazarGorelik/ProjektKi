package main.AlphaBeta;

import main.Board;
import main.Main;
import main.MoveHandler;
import main.models.Move;
import main.models.MoveContext;
import main.models.Player;
import main.models.TableEntry;

import java.util.List;
import java.util.stream.Collectors;

public class AlphaBetaAI {

    private static final TranspositionTable TT = new TranspositionTable();
    private static long hash(Board b) {
        return Zobrist.hash(b);
    }

    public static int alphaBeta(Board board,int depth,int alpha, int beta,boolean maximizingPlayer,Player rootSide) {
        Main.nodesVisited++;
        long key = hash(board);
        TableEntry hit = TT.get(key);
        if (hit != null && hit.depth >= depth) {
            if (hit.flag == TableEntry.EXACT)
                return hit.score;

            if (hit.flag == TableEntry.LOWER)
                alpha = Math.max(alpha, hit.score);
            else
                beta = Math.min(beta,  hit.score);

            if (alpha >= beta)
                return hit.score; // cut
        }

        if (depth == 0 || board.generateMoves().isEmpty())
            return Evaluation.evaluate(board, rootSide);

        List<Move> moves = board.generateMoves();
        if (hit != null && hit.bestMove != null) {          // PV move first
            moves.remove(hit.bestMove);
            moves.add(0, hit.bestMove);
        }
        moves = sortMovesHeuristically(board, moves, rootSide);

        Move bestMove = null;
        int bestScore = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move m : moves) {
            MoveContext ctx = MoveHandler.applyMove(board, m);
            int score = alphaBeta(board, depth - 1, alpha, beta,
                    !maximizingPlayer, rootSide);
            MoveHandler.undoMove(board, ctx);

            if (maximizingPlayer) {
                if (score > bestScore) { bestScore = score; bestMove = m; }
                alpha = Math.max(alpha, score);
            } else {
                if (score < bestScore) { bestScore = score; bestMove = m; }
                beta  = Math.min(beta,  score);
            }
            if (alpha >= beta) break;                       // α-β cut
        }

        int flag = (bestScore <= alpha) ? TableEntry.UPPER
                : (bestScore >= beta) ? TableEntry.LOWER
                : TableEntry.EXACT;
        TT.store(key, depth, bestScore, (byte)flag, bestMove);

        return bestScore;
    }

    // Zugsortierungsheuristiken (Move Ordering Heuristics)
    private static List<Move> sortMovesHeuristically(Board board, List<Move> moves, Player player) {
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
