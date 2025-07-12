package unit_tests;

import static org.junit.Assert.*;

import main.Board;
import main.models.Move;
import main.models.Player;
import main.models.Position;
import org.junit.Test;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class TestBitboard {
    
    /**
     * Test ob die Bitboard-Implementierung die gleichen Züge wie die normale Implementierung generiert
     */
    @Test
    public void testBitboardVsNormalImplementation_SimpleCase() {
        String fen = "r1r1r1r1r1r1r1/7/7/7/7/7/b1b1b1b1b1b1b1 r";
        Board board = new Board(fen);
        
        List<Move> normalMoves = board.generateMoves();
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        System.out.println("Normal moves: " + normalMoves.size());
        System.out.println("Bitboard moves: " + bitboardMoves.size());
        
        assertEquals("Anzahl der Züge muss gleich sein", normalMoves.size(), bitboardMoves.size());
        assertMovesEqual(normalMoves, bitboardMoves);
    }
    
    @Test
    public void testBitboardVsNormalImplementation_EmptyBoard() {
        String fen = "7/7/7/7/7/7/7 r";
        Board board = new Board(fen);
        
        List<Move> normalMoves = board.generateMoves();
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        assertEquals(0, normalMoves.size());
        assertEquals(0, bitboardMoves.size());
    }
    
    @Test
    public void testBitboardVsNormalImplementation_SingleTower() {
        String fen = "7/7/7/3r13/7/7/7 r";
        Board board = new Board(fen);
        
        List<Move> normalMoves = board.generateMoves();
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        System.out.println("Single tower - Normal moves: " + normalMoves.size());
        System.out.println("Single tower - Bitboard moves: " + bitboardMoves.size());
        
        assertEquals("Anzahl der Züge muss gleich sein", normalMoves.size(), bitboardMoves.size());
        assertMovesEqual(normalMoves, bitboardMoves);
    }
    
    @Test
    public void testBitboardVsNormalImplementation_WithGuards() {
        String fen = "RG6/7/7/7/7/7/6BG b";
        Board board = new Board(fen);
        
        List<Move> normalMoves = board.generateMoves();
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        System.out.println("With guards - Normal moves: " + normalMoves.size());
        System.out.println("With guards - Bitboard moves: " + bitboardMoves.size());
        
        assertEquals("Anzahl der Züge muss gleich sein", normalMoves.size(), bitboardMoves.size());
        assertMovesEqual(normalMoves, bitboardMoves);
    }
    
    @Test
    public void testBitboardVsNormalImplementation_MixedHeights() {
        String fen = "r1r2r3/7/7/7/7/7/b1b2b3 r";
        Board board = new Board(fen);
        
        List<Move> normalMoves = board.generateMoves();
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        System.out.println("Mixed heights - Normal moves: " + normalMoves.size());
        System.out.println("Mixed heights - Bitboard moves: " + bitboardMoves.size());
        
        assertEquals("Anzahl der Züge muss gleich sein", normalMoves.size(), bitboardMoves.size());
        assertMovesEqual(normalMoves, bitboardMoves);
    }
    
    @Test
    public void testBitboardVsNormalImplementation_BlueToMove() {
        String fen = "r1r1r1r1r1r1r1/7/7/7/7/7/b1b1b1b1b1b1b1 b";
        Board board = new Board(fen);
        
        List<Move> normalMoves = board.generateMoves();
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        System.out.println("Blue to move - Normal moves: " + normalMoves.size());
        System.out.println("Blue to move - Bitboard moves: " + bitboardMoves.size());
        
        assertEquals("Anzahl der Züge muss gleich sein", normalMoves.size(), bitboardMoves.size());
        assertMovesEqual(normalMoves, bitboardMoves);
    }
    
    @Test
    public void testBitboardVsNormalImplementation_ComplexPosition() {
        String fen = "r2r3/3b13/7/2r1b12/7/1B6/b5r1 r";
        Board board = new Board(fen);
        
        List<Move> normalMoves = board.generateMoves();
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        System.out.println("Complex position - Normal moves: " + normalMoves.size());
        System.out.println("Complex position - Bitboard moves: " + bitboardMoves.size());
        
        if (normalMoves.size() != bitboardMoves.size()) {
            System.out.println("DIFFERENCE FOUND!");
            System.out.println("Normal moves:");
            for (Move m : normalMoves) {
                System.out.println("  " + m);
            }
            System.out.println("Bitboard moves:");
            for (Move m : bitboardMoves) {
                System.out.println("  " + m);
            }
        }
        
        assertEquals("Anzahl der Züge muss gleich sein", normalMoves.size(), bitboardMoves.size());
        assertMovesEqual(normalMoves, bitboardMoves);
    }
    
    @Test
    public void testBitboardInternalConsistency() {
        String fen = "r2r3/3b13/7/2r1b12/7/1B6/b5r1 r";
        Board board = new Board(fen);
        
        // Test mehrfache Ausführung - sollte immer das gleiche Ergebnis liefern
        List<Move> moves1 = board.generateMovesWithBitboards();
        List<Move> moves2 = board.generateMovesWithBitboards();
        List<Move> moves3 = board.generateMovesWithBitboards();
        
        assertEquals("Bitboard sollte deterministisch sein", moves1.size(), moves2.size());
        assertEquals("Bitboard sollte deterministisch sein", moves2.size(), moves3.size());
        assertMovesEqual(moves1, moves2);
        assertMovesEqual(moves2, moves3);
    }
    
    @Test
    public void testBitboardVsNormalImplementation_EdgePositions() {
        // Teste Figuren an den Rändern des Bretts
        String fen = "r16/7/7/7/7/7/6b1 r";
        Board board = new Board(fen);
        
        List<Move> normalMoves = board.generateMoves();
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        System.out.println("Edge positions - Normal moves: " + normalMoves.size());
        System.out.println("Edge positions - Bitboard moves: " + bitboardMoves.size());
        
        assertEquals("Anzahl der Züge muss gleich sein", normalMoves.size(), bitboardMoves.size());
        assertMovesEqual(normalMoves, bitboardMoves);
    }
    
    @Test
    public void testBitboardVsNormalImplementation_CaptureScenarios() {
        // Teste verschiedene Eroberungsszenarien
        String fen = "7/7/7/3r1b12/7/7/7 r";
        Board board = new Board(fen);
        
        List<Move> normalMoves = board.generateMoves();
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        System.out.println("Capture scenarios - Normal moves: " + normalMoves.size());
        System.out.println("Capture scenarios - Bitboard moves: " + bitboardMoves.size());
        
        assertEquals("Anzahl der Züge muss gleich sein", normalMoves.size(), bitboardMoves.size());
        assertMovesEqual(normalMoves, bitboardMoves);
    }
    
    /**
     * Hilfsmethode zum Vergleichen von Move-Listen
     */
    private void assertMovesEqual(List<Move> expected, List<Move> actual) {
        Set<String> expectedSet = new HashSet<>();
        Set<String> actualSet = new HashSet<>();
        
        for (Move move : expected) {
            expectedSet.add(moveToString(move));
        }
        
        for (Move move : actual) {
            actualSet.add(moveToString(move));
        }
        
        // Finde fehlende Züge
        Set<String> missing = new HashSet<>(expectedSet);
        missing.removeAll(actualSet);
        
        // Finde zusätzliche Züge
        Set<String> extra = new HashSet<>(actualSet);
        extra.removeAll(expectedSet);
        
        if (!missing.isEmpty() || !extra.isEmpty()) {
            StringBuilder error = new StringBuilder();
            error.append("Move lists are not equal!\n");
            
            if (!missing.isEmpty()) {
                error.append("Missing moves in bitboard implementation:\n");
                for (String move : missing) {
                    error.append("  ").append(move).append("\n");
                }
            }
            
            if (!extra.isEmpty()) {
                error.append("Extra moves in bitboard implementation:\n");
                for (String move : extra) {
                    error.append("  ").append(move).append("\n");
                }
            }
            
            fail(error.toString());
        }
    }
    
    private String moveToString(Move move) {
        return String.format("(%d,%d)->(%d,%d):step%d", 
                           move.from.x, move.from.y, 
                           move.to.x, move.to.y, 
                           move.step);
    }
} 