package unit_tests;

import main.AlphaBeta.BasicAI;
import main.Board;
import main.models.Move;
import main.models.Position;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.*;

public class BasicAITest {
    @Test
    public void testFindBestMovePrefersCapture() {
        // Red Guard can capture a nearby Blue Tower
        String fen = "1RG5/1b15/7/7/7/7/7 r";
        Board board = new Board(fen);

        Move best = BasicAI.findBestMove(board, 2);

        assertNotNull(best);
        // AI must move the guard
        assertEquals(new Position(1, 0), best.from);
        // It should capture the blue tower
        assertEquals(new Position(1, 1), best.to);
    }

    @Test
    public void testPreferNotToCapture() {
        Board board = new Board("7/7/7/7/7/7/RGb1b14 r");
        Move best = BasicAI.findBestMove(board, 2);
        assertNotNull(best);
        List<Move> all = board.generateMoves();
        assertTrue(all.size() == 2);
        assertTrue(best.to.equals(new Position(0, 5)));
    }
}
