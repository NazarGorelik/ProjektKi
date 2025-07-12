package unit_tests;

import main.Board;
import main.models.Move;
import org.junit.Test;
import java.util.List;

public class TestBitboardPerformance {
    
    @Test
    public void testPerformanceComparison() {
        System.out.println("=== Bitboard Performance Test ===");
        
        // Test verschiedene Brettstellungen
        String[] testPositions = {
            "r1r1r1r1r1r1r1/7/7/7/7/7/b1b1b1b1b1b1b1 r", // Startposition
            "7/7/7/3r13/7/7/7 r", // Ein Turm
            "r2r3RG/3b14/7/2r1b15/7/1B6/b5r1 r", // Komplexe Position
            "RG6/1r2r23/2B5/3r1b12/1b16/4BG3/5b1 b", // Sehr komplexe Position
        };
        
        for (int i = 0; i < testPositions.length; i++) {
            String fen = testPositions[i];
            System.out.println("\nTest Position " + (i + 1) + ": " + fen);
            
            Board board = new Board(fen);
            
            // Warmup
            for (int j = 0; j < 100; j++) {
                board.generateMoves();
                board.generateMovesWithBitboards();
            }
            
            // Test normale Implementierung
            long startTime = System.nanoTime();
            int iterations = 10000;
            List<Move> normalMoves = null;
            
            for (int j = 0; j < iterations; j++) {
                normalMoves = board.generateMoves();
            }
            
            long normalTime = System.nanoTime() - startTime;
            
            // Test Bitboard-Implementierung
            startTime = System.nanoTime();
            List<Move> bitboardMoves = null;
            
            for (int j = 0; j < iterations; j++) {
                bitboardMoves = board.generateMovesWithBitboards();
            }
            
            long bitboardTime = System.nanoTime() - startTime;
            
            // Ergebnisse
            double normalMs = normalTime / 1_000_000.0;
            double bitboardMs = bitboardTime / 1_000_000.0;
            double speedup = (double) normalTime / bitboardTime;
            
            System.out.println("  Moves generated: " + normalMoves.size());
            System.out.println("  Normal implementation: " + String.format("%.2f ms", normalMs));
            System.out.println("  Bitboard implementation: " + String.format("%.2f ms", bitboardMs));
            System.out.println("  Speedup: " + String.format("%.2fx", speedup));
            
            if (speedup > 1.0) {
                System.out.println("  -> Bitboard is FASTER");
            } else {
                System.out.println("  -> Normal implementation is FASTER");
            }
            
            // Überprüfe Korrektheit
            if (normalMoves.size() != bitboardMoves.size()) {
                System.out.println("  ERROR: Different number of moves generated!");
            }
        }
    }
    
    @Test
    public void testMemoryUsage() {
        System.out.println("\n=== Memory Usage Test ===");
        
        String fen = "r2r3RG/3b14/7/2r1b15/7/1B6/b5r1 r";
        
        // Messe Speicherverbrauch vor Test
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Garbage Collection
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        Board board = new Board(fen);
        
        // Normale Implementierung - viele Iterationen
        for (int i = 0; i < 1000; i++) {
            List<Move> moves = board.generateMoves();
        }
        
        runtime.gc();
        long memoryAfterNormal = runtime.totalMemory() - runtime.freeMemory();
        
        // Bitboard-Implementierung - viele Iterationen
        for (int i = 0; i < 1000; i++) {
            List<Move> moves = board.generateMovesWithBitboards();
        }
        
        runtime.gc();
        long memoryAfterBitboard = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.println("Memory before: " + (memoryBefore / 1024) + " KB");
        System.out.println("Memory after normal: " + (memoryAfterNormal / 1024) + " KB");
        System.out.println("Memory after bitboard: " + (memoryAfterBitboard / 1024) + " KB");
        
        long normalIncrease = memoryAfterNormal - memoryBefore;
        long bitboardIncrease = memoryAfterBitboard - memoryAfterNormal;
        
        System.out.println("Normal implementation memory increase: " + (normalIncrease / 1024) + " KB");
        System.out.println("Bitboard implementation memory increase: " + (bitboardIncrease / 1024) + " KB");
    }
} 