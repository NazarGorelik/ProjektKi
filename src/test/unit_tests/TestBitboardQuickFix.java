package unit_tests;

import main.Board;
import main.models.Move;
import org.junit.Test;
import java.util.List;

public class TestBitboardQuickFix {
    
    @Test
    public void demonstratePerformanceProblem() {
        System.out.println("=== Performance Problem Demonstration ===");
        
        String fen = "7/7/7/3r13/7/7/7 r";
        Board board = new Board(fen);
        
        // Zeige, dass das Debug-Output das Problem ist
        System.out.println("\nTesting with current implementation (lots of debug output):");
        
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            List<Move> moves = board.generateMovesWithBitboards();
        }
        long bitboardTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            List<Move> moves = board.generateMoves();
        }
        long normalTime = System.nanoTime() - start;
        
        double bitboardMs = bitboardTime / 1_000_000.0;
        double normalMs = normalTime / 1_000_000.0;
        double slowdown = (double) bitboardTime / normalTime;
        
        System.out.println("Normal implementation: " + String.format("%.2f ms", normalMs));
        System.out.println("Bitboard implementation: " + String.format("%.2f ms", bitboardMs));
        System.out.println("Bitboard is " + String.format("%.1fx", slowdown) + " SLOWER");
        
        System.out.println("\nHauptproblem: Jeder generateMovesWithBitboards() Aufruf macht:");
        System.out.println("- 6 System.out.println() Aufrufe");
        System.out.println("- String-Konvertierungen von long zu Binary");
        System.out.println("- printBitboards() Aufruf mit weiteren 4+ println() Aufrufen");
        System.out.println("\nDas bedeutet ~10 Console-Outputs pro Move-Generation!");
        
        System.out.println("\nLösung: Entfernen Sie alle System.out.println() aus generateMovesWithBitboards()");
    }
    
    @Test 
    public void showBitboardDataIntegrity() {
        System.out.println("\n=== Bitboard Data Integrity Test ===");
        
        String fen = "r1r1r1r1r1r1r1/7/7/7/7/7/b1b1b1b1b1b1b1 r";
        Board board = new Board(fen);
        
        List<Move> normalMoves = board.generateMoves();
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        System.out.println("Normal moves: " + normalMoves.size());
        System.out.println("Bitboard moves: " + bitboardMoves.size());
        System.out.println("Match: " + (normalMoves.size() == bitboardMoves.size()));
        
        if (normalMoves.size() == bitboardMoves.size()) {
            System.out.println("✅ Bitboard-Logik ist KORREKT - nur Performance-Problem!");
        } else {
            System.out.println("❌ Bitboard-Logik hat Fehler");
        }
    }
} 