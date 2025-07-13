package unit_tests;

import main.AlphaBeta.TranspositionTable.Zobrist;
import main.Board;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ZobristTest {
    @Test
    public void sameBoardSameHash() {
        String fen = "r1r11RG1r1r1/2r11r12/3r13/7/3b13/2b11b12/b1b11BG1b1b1 r";
        Board b1 = new Board(fen);
        Board b2 = new Board(fen);

        assertEquals(Zobrist.hash(b1), Zobrist.hash(b2));
    }

    @Test
    public void differentSideToMoveChangesHash() {
        String fenRed  = "7/7/7/7/7/7/7 r";
        String fenBlue = "7/7/7/7/7/7/7 b";

        long h1 = Zobrist.hash(new Board(fenRed));
        long h2 = Zobrist.hash(new Board(fenBlue));

        assertNotEquals(h1, h2);
    }

    @Test
    public void pieceChangeChangesHash() {
        Board b1 = new Board("7/7/7/7/7/7/7 r");
        Board b2 = new Board("7/7/7/3r13/7/7/7 r");

        long h1 = Zobrist.hash(b1);
        long h2 = Zobrist.hash(b2);

        assertNotEquals(h1, h2);
    }

    @Test
    public void hashIsDeterministicWithFixedSeed() {
        String fen = "7/7/7/3RG3/7/7/BG6 r";
        long expectedHash = -4772654276044857672L;
        long hash = Zobrist.hash(new Board(fen));
        assertEquals(expectedHash, hash);
    }
}
