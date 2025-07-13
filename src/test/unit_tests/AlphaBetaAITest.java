package unit_tests;

import main.AlphaBeta.AlphaBetaAI;
import main.AlphaBeta.BasicAI;
import main.Board;
import main.Main;
import main.models.Move;
import main.models.Position;
import org.junit.Test;

import static junit.framework.TestCase.*;

public class AlphaBetaAITest {
    /* Helper that returns the score the engine assigns to a FEN */
    private int score(String fen, int depth) {
        Board b = new Board(fen);
        return AlphaBetaAI.alphaBeta(
                b,
                depth,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                true,
                b.getToMove());
    }

    @Test
    public void testBestMoveCapturesGuard() {
        // Red guard (RG) can take the blue guard (BG) directly in front.
        String fen = "1RG5/1BG5/7/7/7/7/7 r";
        Board board = new Board(fen);
        Move best = BasicAI.findBestMove(board, 3);

        assertNotNull(best);
        // From (1,0) to (1,1) is the only guard capture
        assertEquals(new Position(1,0), best.from);
        assertEquals(new Position(1,1), best.to);
    }

    @Test
    public void testEnginePrefersKeepingMaterial() {
        String fenEqual ="r1r1RG4/7/7/7/7/7/4BG1b1 r";
        String fenUpMaterial = "r1r1RG4/7/7/7/BG6/7/7 r";

        int equalScore = score(fenEqual, 2);
        int upScore = score(fenUpMaterial, 2);

        // Being up a guard must yield a strictly higher score
        assertTrue(upScore > equalScore);
    }

    @Test
    public void testTranspositionTableHit() {
        String fen ="r1r11RG1r1r1/2r11r12/3r13/7/3b13/2b11b12/b1b11BG1b1b1 r";
        Board b = new Board(fen);

        // First search to depth 4
        Main.nodesVisited = 0;
        AlphaBetaAI.alphaBeta(b, 4,Integer.MIN_VALUE, Integer.MAX_VALUE, true, b.getToMove());
        long firstNodes = Main.nodesVisited;

        // Repeat the *same* search – should be much faster due to TT
        Main.nodesVisited = 0;
        AlphaBetaAI.alphaBeta(b, 4,Integer.MIN_VALUE, Integer.MAX_VALUE,true, b.getToMove());
        long secondNodes = Main.nodesVisited;

        assertTrue(secondNodes < firstNodes / 2);
    }
}
