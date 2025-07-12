package main.AlphaBeta;

import main.AlphaBeta.MoveOrderingHeuristik.Heuristik;
import main.AlphaBeta.QuiescenceSearch.QuiescenceSearch;
import main.AlphaBeta.TranspositionTable.TranspositionTable;
import main.AlphaBeta.TranspositionTable.Zobrist;
import main.Board;
import main.Main;
import main.MoveHandler;
import main.models.Move;
import main.models.MoveContext;
import main.models.Player;
import main.AlphaBeta.TranspositionTable.TableEntry;

import java.util.List;

public class AlphaBetaAI {

    private static final TranspositionTable TT = new TranspositionTable();
    private static long hash(Board b) {
        return Zobrist.hash(b);
    }

    public static int alphaBeta(Board board,int depth,int alpha, int beta,boolean maximizingPlayer, Player player) {
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

        // Am Suchhorizont: Quiescence Search statt direkter Evaluation
//        if (depth == 0)
//            return QuiescenceSearch.quiescence(board, alpha, beta, player, 0);
//
//        List<Move> moves = Heuristik.sortMovesHeuristically(board, board.generateMoves(), player);
//
//        // Spiel beendet
//        if (moves.isEmpty()) {
//            return Evaluation.evaluate(board, player);
//        }

        List<Move> moves = board.generateMoves();
        if (depth == 0 || board.generateMoves().isEmpty())
            return Evaluation.evaluate(board, player);

        if (hit != null && hit.bestMove != null) {          // PV move first
            moves.remove(hit.bestMove);
            moves.add(0, hit.bestMove);
        }
        moves = Heuristik.sortMovesHeuristically(board, moves, player);

        if (hit != null && hit.bestMove != null) {          // PV move first
            moves.remove(hit.bestMove);
            moves.add(0, hit.bestMove);
        }

        Move bestMove = null;
        int bestScore = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move m : moves) {
            MoveContext ctx = MoveHandler.applyMove(board, m);
            int score = alphaBeta(board, depth - 1, alpha, beta,
                    !maximizingPlayer, player);
            MoveHandler.undoMove(board, ctx);

            if (maximizingPlayer) {
                if (score > bestScore) {
                    bestScore = score;
                }
                alpha = Math.max(alpha, score);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                }
                beta  = Math.min(beta,  score);
            }
            bestMove = m;

            if (alpha >= beta) break;                       // α-β cut
        }

        int flag = (bestScore <= alpha) ? TableEntry.UPPER
                : (bestScore >= beta) ? TableEntry.LOWER
                : TableEntry.EXACT;
        TT.store(key, depth, bestScore, (byte)flag, bestMove);

        return bestScore;
    }
}
