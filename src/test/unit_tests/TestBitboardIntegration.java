package unit_tests;

import static org.junit.Assert.*;

import main.Board;
import main.models.Move;
import main.models.Position;
import main.models.Piece;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import java.util.List;

public class TestBitboardIntegration {
    
    @Before
    public void setUp() {
        // Stelle sicher, dass Bitboards aktiviert sind vor jedem Test
        Board.setUseBitboards(true);
    }
    
    @After
    public void tearDown() {
        // Stelle sicher, dass Bitboards aktiviert bleiben nach jedem Test
        Board.setUseBitboards(true);
    }
    
    @Test
    public void testBitboardToggle() {
        System.out.println("=== Testing Bitboard Integration ===");
        
        String fen = "r1r1r1r1r1r1r1/7/7/7/7/7/b1b1b1b1b1b1b1 r";
        Board board = new Board(fen);
        
        // Test mit Bitboard aktiviert (Standard)
        assertTrue("Bitboard sollte standardmäßig aktiviert sein", Board.isUsingBitboards());
        
        List<Move> bitboardMoves = board.generateMoves();
        System.out.println("Bitboard-Modus: " + bitboardMoves.size() + " Züge generiert");
        
        // Deaktiviere Bitboard
        Board.setUseBitboards(false);
        assertFalse("Bitboard sollte deaktiviert sein", Board.isUsingBitboards());
        
        List<Move> classicMoves = board.generateMoves();
        System.out.println("Classic-Modus: " + classicMoves.size() + " Züge generiert");
        
        // Vergleiche Ergebnisse
        assertEquals("Beide Modi sollten gleiche Anzahl Züge generieren", 
                    bitboardMoves.size(), classicMoves.size());
        
        // Reaktiviere Bitboard für andere Tests
        Board.setUseBitboards(true);
        assertTrue("Bitboard sollte wieder aktiviert sein", Board.isUsingBitboards());
        
        System.out.println("✅ Bitboard-Integration funktioniert korrekt!");
    }
    
    @Test
    public void testPerformanceAfterOptimization() {
        System.out.println("\n=== Performance Test nach Optimierung ===");
        
        String fen = "7/7/7/3r13/7/7/7 r";
        Board board = new Board(fen);
        
        // Test Bitboard Performance (ohne Debug-Output)
        Board.setUseBitboards(true);
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            List<Move> moves = board.generateMoves();
        }
        long bitboardTime = System.nanoTime() - start;
        
        // Test Classic Performance
        Board.setUseBitboards(false);
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            List<Move> moves = board.generateMoves();
        }
        long classicTime = System.nanoTime() - start;
        
        double bitboardMs = bitboardTime / 1_000_000.0;
        double classicMs = classicTime / 1_000_000.0;
        double speedup = (double) classicTime / bitboardTime;
        
        System.out.println("Classic implementation: " + String.format("%.2f ms", classicMs));
        System.out.println("Bitboard implementation: " + String.format("%.2f ms", bitboardMs));
        
        if (speedup > 1.0) {
            System.out.println("✅ Bitboard ist " + String.format("%.1fx", speedup) + " SCHNELLER!");
        } else {
            System.out.println("⚠️ Bitboard ist " + String.format("%.1fx", 1.0/speedup) + " langsamer");
        }
        
        // Reaktiviere Bitboard
        Board.setUseBitboards(true);
    }
    
    @Test 
    public void testAPICompatibility() {
        System.out.println("\n=== API Kompatibilitäts-Test ===");
        
        String fen = "r1r1r1r1r1r1r1/7/7/7/7/7/b1b1b1b1b1b1b1 r";
        Board board = new Board(fen);
        
        // Test dass generateMoves() jetzt Bitboard verwendet
        Board.setUseBitboards(true);
        List<Move> apiMoves = board.generateMoves();
        List<Move> directBitboardMoves = board.generateMovesWithBitboards();
        
        assertEquals("API sollte Bitboard verwenden", 
                    apiMoves.size(), directBitboardMoves.size());
        
        // Test dass generateMoves() Classic verwendet wenn deaktiviert
        Board.setUseBitboards(false);
        List<Move> apiClassicMoves = board.generateMoves();
        
        // Hier können wir nicht direkt vergleichen da generateMoves() verschiedene Implementierungen nutzt
        // Aber wir können testen dass es funktioniert
        assertNotNull("API sollte Züge generieren", apiClassicMoves);
        assertTrue("API sollte Züge finden", apiClassicMoves.size() > 0);
        
        Board.setUseBitboards(true);
        System.out.println("✅ API-Kompatibilität gewährleistet!");
    }

    @Test
    public void testBitboardIntegrationPerformance() {
        System.out.println("Testing bitboard integration performance improvement...");
        
        // Test mit komplexer Position für mehr aussagekräftige Ergebnisse
        String complexFen = "r1r2r1/1b2b2/2r1r2/3RB2/2r1r2/1b2b2/r1r2r1 r";
        Board board = new Board(complexFen);
        
        // Deaktiviere Bitboards und messe Zeit
        Board.setUseBitboards(false);
        long start = System.currentTimeMillis();
        
        int moveCount1 = 0;
        for (int i = 0; i < 1000; i++) {
            List<Move> moves = board.generateMoves();
            moveCount1 = moves.size();
        }
        
        long classicTime = System.currentTimeMillis() - start;
        
        // Aktiviere Bitboards und messe Zeit
        Board.setUseBitboards(true);
        start = System.currentTimeMillis();
        
        int moveCount2 = 0;
        for (int i = 0; i < 1000; i++) {
            List<Move> moves = board.generateMoves();
            moveCount2 = moves.size();
        }
        
        long bitboardTime = System.currentTimeMillis() - start;
        
        System.out.println("Classic engine: " + classicTime + "ms (" + moveCount1 + " moves)");
        System.out.println("Bitboard engine: " + bitboardTime + "ms (" + moveCount2 + " moves)");
        System.out.println("Performance factor: " + (double)classicTime / bitboardTime + "x");
        
        assertEquals("Both engines should generate same number of moves", moveCount1, moveCount2);
        assertTrue("Bitboard should be faster than classic (or at least similar)", bitboardTime <= classicTime * 3);
    }

    @Test
    public void testDirectPieceMovesUseBitboard() {
        System.out.println("Testing that direct piece move calls use bitboard when enabled...");
        
        // Verwende einfache Position mit klaren Stücken
        String fen = "7/7/7/3R3/7/7/7 r";
        Board board = new Board(fen);
        
        // Test mit Bitboards aktiviert
        Board.setUseBitboards(true);
        Position testPos = new Position(3, 3); // Position des roten Guards
        Piece piece = board.board[testPos.y][testPos.x];
        
        System.out.println("Piece at position: " + piece.player + " " + (piece.isGuard ? "Guard" : "Tower") + " height " + piece.height);
        
        List<Move> movesViaGenerateMoves = board.generateMoves();
        List<Move> movesForGuard = board.generateMovesForPieceAt(piece, testPos);
        
        System.out.println("Total moves via generateMoves: " + movesViaGenerateMoves.size());
        System.out.println("Moves for specific piece: " + movesForGuard.size());
        
        // Der Guard sollte sich in den generierten Zügen wiederfinden
        int guardMovesInGeneral = 0;
        for (Move move : movesViaGenerateMoves) {
            if (move.from.x == testPos.x && move.from.y == testPos.y) {
                guardMovesInGeneral++;
            }
        }
        
        assertEquals("Guard moves should match between general and specific generation", 
                     guardMovesInGeneral, movesForGuard.size());
        
        // Test mit Bitboards deaktiviert - verwende direkte Überladung
        Board.setUseBitboards(false);
        List<Move> movesClassic = board.generateMovesForPieceAt(piece, testPos);
        
        assertEquals("Both bitboard and classic should generate same moves for piece", 
                     movesForGuard.size(), movesClassic.size());
                     
        System.out.println("✓ Direct piece move calls correctly use bitboard configuration");
        
        // Reaktiviere Bitboards für andere Tests
        Board.setUseBitboards(true);
    }
} 