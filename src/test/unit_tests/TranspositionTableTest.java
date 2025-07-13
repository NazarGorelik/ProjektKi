package unit_tests;

import main.AlphaBeta.TranspositionTable.TableEntry;
import main.AlphaBeta.TranspositionTable.TranspositionTable;
import main.models.Move;
import main.models.Position;
import org.junit.Test;

import static junit.framework.TestCase.*;

public class TranspositionTableTest {
    @Test
    public void testStoreAndRetrieve() {
        TranspositionTable tt = new TranspositionTable();
        long key = 123456L;

        TableEntry entry = new TableEntry(4, 100, TableEntry.EXACT, new Move(new Position(0,0), new Position(1,1), 1));
        tt.store(key, entry.depth, entry.score, entry.flag, entry.bestMove);

        TableEntry result = tt.get(key);
        assertNotNull(result);
        assertEquals(4, result.depth);
        assertEquals(100, result.score);
        assertEquals(TableEntry.EXACT, result.flag);
        assertEquals(entry.bestMove, result.bestMove);
    }

    @Test
    public void testOverwriteWithHigherDepth() {
        TranspositionTable tt = new TranspositionTable();
        long key = 789L;

        tt.store(key, 3, 50, TableEntry.LOWER, null);
        tt.store(key, 5, 80, TableEntry.EXACT, new Move(new Position(1,1), new Position(2,2), 2));

        TableEntry result = tt.get(key);
        assertEquals(5, result.depth);
        assertEquals(80, result.score);
        assertEquals(TableEntry.EXACT, result.flag);
    }

    @Test
    public void testDoNotOverwriteWithLowerDepth() {
        TranspositionTable tt = new TranspositionTable();
        long key = 42L;

        tt.store(key, 6, 90, TableEntry.UPPER, null);
        tt.store(key, 4, 30, TableEntry.LOWER, null);  // Should not overwrite

        TableEntry result = tt.get(key);
        assertEquals(6, result.depth);
        assertEquals(90, result.score);
        assertEquals(TableEntry.UPPER, result.flag);
    }

    @Test
    public void testGetMissingReturnsNull() {
        TranspositionTable tt = new TranspositionTable();
        assertNull(tt.get(999999L));
    }
}
