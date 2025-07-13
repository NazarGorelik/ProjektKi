package main.AlphaBeta.QuiescenceSearch;

import main.AlphaBeta.Evaluation;
import main.Board;
import main.MoveHandler;
import main.models.Move;
import main.models.MoveContext;
import main.models.Piece;
import main.models.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuiescenceSearch {
    private static final int QS_MAX_DEPTH   = 3;
    private static final int DELTA_MARGIN   = 120;

    public static int quiescence(Board b,int alpha, int beta, Player rootSide, int depth) {

        int standPat = Evaluation.evaluateStatic(b, rootSide);
        if (standPat >= beta)  return beta;
        if (standPat >  alpha) alpha = standPat;


        if (depth >= QS_MAX_DEPTH)  return alpha;

        List<Move> caps = captureMoves(b, b.getToMove());
        if (caps.isEmpty())         return alpha;

        for (Move m : caps) {

            int estGain = estimateMaterialGain(b, m);
            if (standPat + estGain + DELTA_MARGIN < alpha) continue;

            MoveContext ctx = MoveHandler.applyMove(b, m);
            int score = -quiescence(b, -beta, -alpha, rootSide, depth + 1);
            MoveHandler.undoMove(b, ctx);

            if (score >= beta)  return beta;
            if (score >  alpha) alpha = score;
        }
        return alpha;
    }


    private static int estimateMaterialGain(Board b, Move m) {
        Piece tgt = b.board[m.to.y][m.to.x];
        if (tgt == null || tgt.player == null) return 0;
        return tgt.isGuard ? 1000 : tgt.height * 10;
    }

    private static List<Move> captureMoves(Board b, Player side) {
        List<Move> caps = new ArrayList<>();
        for (Move m : b.generateMoves()) {
            Piece tgt = b.board[m.to.y][m.to.x];
            if (tgt != null && tgt.player != null
                    && tgt.player != side) {
                caps.add(m);
            }
        }

        caps.sort((a, c) -> {
            Piece ta = b.board[a.to.y][a.to.x];
            Piece tc = b.board[c.to.y][c.to.x];
            int va = ta.isGuard ? 1000 : ta.height * 10;
            int vc = tc.isGuard ? 1000 : tc.height * 10;
            return Integer.compare(vc, va);
        });
        return caps;
    }
}
