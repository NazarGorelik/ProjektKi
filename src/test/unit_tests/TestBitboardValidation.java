package unit_tests;

import static org.junit.Assert.*;

import main.Board;
import main.models.Player;
import org.junit.Test;
import java.lang.reflect.Field;

public class TestBitboardValidation {
    
    /**
     * Test ob die Bitboards korrekt initialisiert werden
     */
    @Test
    public void testBitboardInitialization() throws Exception {
        String fen = "r1r1r1r1r1r1r1/7/7/7/7/7/b1b1b1b1b1b1b1 r";
        Board board = new Board(fen);
        
        // Use reflection to access private bitboard fields
        Field redPiecesField = Board.class.getDeclaredField("redPieces");
        Field bluePiecesField = Board.class.getDeclaredField("bluePieces");
        Field guardsField = Board.class.getDeclaredField("guards");
        Field heightsField = Board.class.getDeclaredField("heights");
        
        redPiecesField.setAccessible(true);
        bluePiecesField.setAccessible(true);
        guardsField.setAccessible(true);
        heightsField.setAccessible(true);
        
        long redPieces = redPiecesField.getLong(board);
        long bluePieces = bluePiecesField.getLong(board);
        long guards = guardsField.getLong(board);
        long[] heights = (long[]) heightsField.get(board);
        
        System.out.println("Red pieces bitboard: " + Long.toBinaryString(redPieces));
        System.out.println("Blue pieces bitboard: " + Long.toBinaryString(bluePieces));
        System.out.println("Guards bitboard: " + Long.toBinaryString(guards));
        
        // Überprüfe, dass rot und blau sich nicht überschneiden
        assertEquals("Rot und Blau dürfen sich nicht überschneiden", 0, redPieces & bluePieces);
        
        // Überprüfe, dass Guards ein Subset von allen Figuren sind
        assertEquals("Guards müssen Subset von allen Figuren sein", guards, guards & (redPieces | bluePieces));
        
        // Zähle Bits manuell und vergleiche mit dem Brett
        int redCount = countBitsSet(redPieces);
        int blueCount = countBitsSet(bluePieces);
        
        int redCountBoard = countPiecesOnBoard(board, Player.RED);
        int blueCountBoard = countPiecesOnBoard(board, Player.BLUE);
        
        assertEquals("Rote Figuren Bitboard vs Brett", redCountBoard, redCount);
        assertEquals("Blaue Figuren Bitboard vs Brett", blueCountBoard, blueCount);
    }
    
    @Test
    public void testBitboardConsistencyWithBoard() throws Exception {
        String fen = "r23r4RG/3b13/7/2r12b2/7/1B5/b6r r";
        Board board = new Board(fen);
        
        // Access private fields
        Field redPiecesField = Board.class.getDeclaredField("redPieces");
        Field bluePiecesField = Board.class.getDeclaredField("bluePieces");
        Field guardsField = Board.class.getDeclaredField("guards");
        Field heightsField = Board.class.getDeclaredField("heights");
        
        redPiecesField.setAccessible(true);
        bluePiecesField.setAccessible(true);
        guardsField.setAccessible(true);
        heightsField.setAccessible(true);
        
        long redPieces = redPiecesField.getLong(board);
        long bluePieces = bluePiecesField.getLong(board);
        long guards = guardsField.getLong(board);
        long[] heights = (long[]) heightsField.get(board);
        
        // Überprüfe jede Position des Bretts
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                int index = y * 7 + x;
                boolean redBit = (redPieces & (1L << index)) != 0;
                boolean blueBit = (bluePieces & (1L << index)) != 0;
                boolean guardBit = (guards & (1L << index)) != 0;
                
                // Vergleiche mit dem tatsächlichen Brett
                if (board.board[y][x].player == Player.RED) {
                    assertTrue("Position (" + x + "," + y + ") sollte rotes Bit haben", redBit);
                    assertFalse("Position (" + x + "," + y + ") sollte kein blaues Bit haben", blueBit);
                } else if (board.board[y][x].player == Player.BLUE) {
                    assertFalse("Position (" + x + "," + y + ") sollte kein rotes Bit haben", redBit);
                    assertTrue("Position (" + x + "," + y + ") sollte blaues Bit haben", blueBit);
                } else {
                    assertFalse("Leere Position (" + x + "," + y + ") sollte kein rotes Bit haben", redBit);
                    assertFalse("Leere Position (" + x + "," + y + ") sollte kein blaues Bit haben", blueBit);
                }
                
                // Überprüfe Guard-Bit
                if (board.board[y][x].player != null && board.board[y][x].isGuard) {
                    assertTrue("Guard Position (" + x + "," + y + ") sollte Guard-Bit haben", guardBit);
                } else {
                    assertFalse("Nicht-Guard Position (" + x + "," + y + ") sollte kein Guard-Bit haben", guardBit);
                }
                
                // Überprüfe Höhen-Bits
                if (board.board[y][x].player != null) {
                    int pieceHeight = board.board[y][x].height;
                    boolean heightBit = (heights[pieceHeight] & (1L << index)) != 0;
                    assertTrue("Position (" + x + "," + y + ") sollte Höhen-Bit für Höhe " + pieceHeight + " haben", heightBit);
                    
                    // Überprüfe, dass keine anderen Höhen-Bits gesetzt sind
                    for (int h = 1; h <= 7; h++) {
                        if (h != pieceHeight) {
                            boolean wrongHeightBit = (heights[h] & (1L << index)) != 0;
                            assertFalse("Position (" + x + "," + y + ") sollte kein Höhen-Bit für Höhe " + h + " haben", wrongHeightBit);
                        }
                    }
                }
            }
        }
    }
    
    @Test
    public void testBitboardOperations() throws Exception {
        String fen = "7/7/7/3r13/7/7/7 r";
        Board board = new Board(fen);
        
        // Access private methods via reflection
        java.lang.reflect.Method getBitMethod = Board.class.getDeclaredMethod("getBit", long.class, int.class, int.class);
        java.lang.reflect.Method getIndexMethod = Board.class.getDeclaredMethod("getIndex", int.class, int.class);
        java.lang.reflect.Method isOccupiedMethod = Board.class.getDeclaredMethod("isOccupiedByPlayer", int.class, int.class, Player.class);
        java.lang.reflect.Method getPieceHeightMethod = Board.class.getDeclaredMethod("getPieceHeight", int.class, int.class);
        java.lang.reflect.Method isGuardMethod = Board.class.getDeclaredMethod("isGuard", int.class, int.class);
        
        getBitMethod.setAccessible(true);
        getIndexMethod.setAccessible(true);
        isOccupiedMethod.setAccessible(true);
        getPieceHeightMethod.setAccessible(true);
        isGuardMethod.setAccessible(true);
        
        // Test getIndex
        int index = (Integer) getIndexMethod.invoke(board, 3, 3);
        assertEquals("Index für (3,3) sollte 24 sein", 24, index);
        
        // Test isOccupiedByPlayer
        boolean isOccupiedRed = (Boolean) isOccupiedMethod.invoke(board, 3, 3, Player.RED);
        boolean isOccupiedBlue = (Boolean) isOccupiedMethod.invoke(board, 3, 3, Player.BLUE);
        
        assertTrue("Position (3,3) sollte von Rot besetzt sein", isOccupiedRed);
        assertFalse("Position (3,3) sollte nicht von Blau besetzt sein", isOccupiedBlue);
        
        // Test getPieceHeight
        int height = (Integer) getPieceHeightMethod.invoke(board, 3, 3);
        assertEquals("Höhe der Figur auf (3,3) sollte 1 sein", 1, height);
        
        // Test isGuard
        boolean isGuard = (Boolean) isGuardMethod.invoke(board, 3, 3);
        assertFalse("Figur auf (3,3) sollte kein Guard sein", isGuard);
        
        // Test leere Position
        boolean isEmptyOccupiedRed = (Boolean) isOccupiedMethod.invoke(board, 0, 0, Player.RED);
        boolean isEmptyOccupiedBlue = (Boolean) isOccupiedMethod.invoke(board, 0, 0, Player.BLUE);
        int emptyHeight = (Integer) getPieceHeightMethod.invoke(board, 0, 0);
        
        assertFalse("Leere Position sollte nicht von Rot besetzt sein", isEmptyOccupiedRed);
        assertFalse("Leere Position sollte nicht von Blau besetzt sein", isEmptyOccupiedBlue);
        assertEquals("Leere Position sollte Höhe 0 haben", 0, emptyHeight);
    }
    
    @Test
    public void testGuardBitboard() throws Exception {
        String fen = "RG6/7/7/7/7/7/6BG b";
        Board board = new Board(fen);
        
        Field guardsField = Board.class.getDeclaredField("guards");
        guardsField.setAccessible(true);
        long guards = guardsField.getLong(board);
        
        System.out.println("Guards bitboard: " + Long.toBinaryString(guards));
        
        // Überprüfe spezifische Guard-Positionen
        java.lang.reflect.Method isGuardMethod = Board.class.getDeclaredMethod("isGuard", int.class, int.class);
        isGuardMethod.setAccessible(true);
        
        boolean isGuardRed = (Boolean) isGuardMethod.invoke(board, 0, 1); // RG Position
        boolean isGuardBlue = (Boolean) isGuardMethod.invoke(board, 6, 6); // BG Position
        
        assertTrue("Rote Guard-Position sollte Guard-Bit haben", isGuardRed);
        assertTrue("Blaue Guard-Position sollte Guard-Bit haben", isGuardBlue);
        
        // Überprüfe Non-Guard-Position
        boolean isNonGuard = (Boolean) isGuardMethod.invoke(board, 0, 0); // R Position
        assertFalse("Normale Figur sollte kein Guard-Bit haben", isNonGuard);
    }
    
    private int countBitsSet(long bitboard) {
        return Long.bitCount(bitboard);
    }
    
    private int countPiecesOnBoard(Board board, Player player) {
        int count = 0;
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (board.board[y][x].player == player) {
                    count++;
                }
            }
        }
        return count;
    }
} 