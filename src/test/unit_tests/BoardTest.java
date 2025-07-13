package unit_tests;

import main.Board;
import main.models.Move;
import main.models.Piece;
import main.models.Player;
import main.models.Position;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertThrows;


public class BoardTest {
    private Board build(String fen) { return new Board(fen); }

    @Test
    public void parsesSideToMove() {
        Board b1 = build("7/7/7/7/7/7/7 r");
        assertEquals(Player.RED,  b1.getToMove());

        Board b2 = build("7/7/7/7/7/7/7 b");
        assertEquals(Player.BLUE, b2.getToMove());
    }

    @Test
    public void parsesGuardAndTower() {
        Board b = build("RGBGr11b22/7/7/7/7/7/7 r");
        // Row 0, Col 0 must be a red guard:
        Piece p = b.board[0][0];
        assertNotNull(p);
        assertTrue(p.isGuard);
        assertEquals(Player.RED, p.player);

        // Row 0, Col 1 must be a blue guard:
        Piece q = b.board[0][1];
        assertTrue(q.isGuard);
        assertEquals(Player.BLUE, q.player);

        // Bitboards reflect the same:
        long mask00 = 1L << 0;      // idx 0
        long mask01 = 1L << 1;      // idx 1
        assertTrue((b.redPieces  & mask00) != 0);
        assertTrue((b.bluePieces & mask01) != 0);
        assertTrue((b.guards     & mask00) != 0);
        assertTrue((b.guards     & mask01) != 0);
    }

    @Test
    public void bitboardsConsistentAfterParsing() {
        Board b = build("r1r11RG1r1r1/2r11r12/3r13/7/3b13/2b11b12/b1b11BG1b1b1 b");

        long occupied = b.redPieces | b.bluePieces;
        // No overlap
        assertEquals(0L, b.redPieces & b.bluePieces);
        // Guards always occupied
        assertEquals(b.guards, b.guards & occupied);

        // Heights union must cover all non-empty squares
        long heightsUnion = 0L;
        for (int h = 1; h <= 7; h++) heightsUnion |= b.heights[h];
        assertEquals(occupied, heightsUnion);
    }

    @Test
    public void startPositionMoveCount() {
        String start = "r1r11RG1r1r1/2r11r12/3r13/7/3b13/2b11b12/b1b11BG1b1b1 r";
        Board b = build(start);
        List<Move> moves = b.generateMoves();
        assertEquals(25, moves.size());
    }

    @Test
    public void simpleCaptureIsGenerated() {
        Board b = build("1RG5/1b15/7/7/7/7/7 r"); // red guard can capture blue tower
        List<Move> moves = b.generateMoves();
        boolean found = moves.stream()
                .anyMatch(m -> m.from.equals(new Position(1,0))
                        && m.to.equals(new Position(1,1)));
        assertTrue(found);
    }

    @Test
    public void setSquareUpdatesBitboards() {
        Board b = build("7/7/7/7/7/7/7 r");
        b.setSquare(3, 3, new Piece(Player.RED, 2, false));

        long mask = 1L << (3*7 + 3);
        assertTrue((b.redPieces & mask) != 0);
        assertTrue((b.heights[2] & mask) != 0);
        assertSame(Player.RED, b.board[3][3].player);

        // Remove it again
        b.setSquare(3, 3, new Piece());
        assertTrue((b.redPieces & mask) == 0);
        assertTrue((b.heights[2] & mask) == 0);
        assertNull(b.board[3][3].player);
    }

    @Test
    public void towerCannotCaptureOwnGuard() {
        Board b = build("1r1RG4/7/7/7/7/7/7 r");  // Tower in (0,0), Guard in (1,0)
        List<Move> moves = b.generateMoves();
        boolean found = moves.stream().anyMatch(m -> m.from.equals(new Position(0,0))
                && m.to.equals(new Position(1,0)));
        assertFalse(found);  // darf den Guard nicht angreifen
    }

    @Test
    public void guardCanCaptureAnyTower() {
        Board b = build("RG6/b56/7/7/7/7/7 r");
        List<Move> moves = b.generateMoves();
        boolean found = moves.stream().anyMatch(m -> m.from.equals(new Position(0,0))
                && m.to.equals(new Position(0,1)));
        assertTrue(found);
    }

    @Test
    public void tower1CanOnlyCaptureHeight1() {
        Board b = build("b1r2RG4/1b23/BG6/7/7/7/7 r");
        List<Move> moves = b.generateMoves();
        boolean foundLow = moves.stream().anyMatch(m -> m.to.equals(new Position(0,0)));
        boolean foundHigh = moves.stream().anyMatch(m -> m.to.equals(new Position(1,1)));
        assertTrue(foundLow);   // Höhe 1 Gegner -> erlaubt
        assertFalse(foundHigh); // Höhe 2 Gegner -> verboten
    }

    @Test
    public void invalidFenThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> build("7/7/7/7/7/7/7"));
        assertThrows(IllegalArgumentException.class, () -> build("INVALID r"));
    }
}
