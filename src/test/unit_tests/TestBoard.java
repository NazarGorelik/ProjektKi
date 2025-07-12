package unit_tests;

import static org.junit.Assert.*;

import main.Board;
import main.models.Move;
import main.models.Piece;
import main.models.Player;
import main.models.Position;
import org.junit.Test;
import java.util.List;

public class TestBoard {
    
    @Test
    public void testBoardInitialization() {
        String fen = "r1r1r1r1r1r1r1/7/7/7/7/7/b1b1b1b1b1b1b1 r";
        Board board = new Board(fen);
        assertEquals(Player.RED, board.getToMove());
        
        // Überprüfe ein paar Positionen
        assertNotNull(board.board[0][0]);
        assertEquals(Player.RED, board.board[0][0].player);
        assertFalse(board.board[0][0].isGuard);
        assertEquals(1, board.board[0][0].height);
        
        // Überprüfe leeres Feld
        assertNotNull(board.board[3][3]);
        assertNull(board.board[3][3].player);
    }
    
    @Test
    public void testTowerMovesInOpenField() {
        // Erstelle einfaches Brett mit einem roten Turm auf D4
        String fen = "7/7/7/3r13/7/7/7 r";
        Board board = new Board(fen);
        
        // Hole Position
        Position pos = new Position(3, 3); // D4
        Piece tower = board.board[3][3];
        
        // Generiere Züge
        List<Move> moves = board.generateMovesForPiece(tower, pos);
        
        // Überprüfe horizontale und vertikale Bewegungen
        assertTrue(containsMove(moves, new Position(3, 3), new Position(3, 2), 1)); // Nach oben
        assertTrue(containsMove(moves, new Position(3, 3), new Position(3, 4), 1)); // Nach unten
        assertTrue(containsMove(moves, new Position(3, 3), new Position(2, 3), 1)); // Nach links
        assertTrue(containsMove(moves, new Position(3, 3), new Position(4, 3), 1)); // Nach rechts
    }
    
    @Test
    public void testTowerBlockedBySameColor() {
        // Brett mit einem Turm und einem blockierenden Stein derselben Farbe
        String fen = "7/7/7/2r1r12/7/7/7 r";
        Board board = new Board(fen);
        
        // Hole Position des Turms (C4)
        Position pos = new Position(2, 3);
        Piece tower = board.board[3][2];
        
        // Generiere Züge
        List<Move> moves = board.generateMovesForPiece(tower, pos);
        
        // Überprüfe, dass der Zug zum Feld E4 nicht möglich ist
        assertFalse(containsMove(moves, new Position(2, 3), new Position(4, 3), 2));
    }
    
    @Test
    public void testTowerCapturesEnemy() {
        // Brett mit einem Turm und einem gegnerischen Stein 
        // Platziere den blauen Stein direkt neben dem roten Turm (D4)
        String fen = "7/7/7/2r1b12/7/7/7 r";
        Board board = new Board(fen);
        
        // Hole Position des Turms (C4)
        Position pos = new Position(2, 3);
        Piece tower = board.board[3][2];
        
        // Manuell einen blauen Stein direkt neben den roten Turm platzieren (D4)
        board.board[3][3] = new Piece(Player.BLUE, 1, false);
        
        // Generiere Züge
        List<Move> moves = board.generateMovesForPiece(tower, pos);
        
        // Überprüfe, dass der Zug zum direkt angrenzenden Feld D4 möglich ist (Capture)
        System.out.println("Available moves: " + moves);
        assertTrue(containsMove(moves, new Position(2, 3), new Position(3, 3), 1));
    }
    
    @Test
    public void testGuardMoves() {
        // Brett mit einem blauen Wächter
        String fen = "7/7/7/3BG3/7/7/7 b";
        Board board = new Board(fen);
        
        // Hole Position des Wächters (D4)
        Position pos = new Position(3, 3);
        Piece guard = board.board[3][3];
        
        // Generiere Züge
        List<Move> moves = board.generateMovesForPiece(guard, pos);
        
        // Überprüfe Bewegung in alle Richtungen (1 Feld)
        assertTrue(containsMove(moves, new Position(3, 3), new Position(3, 2), 1)); // Nach oben
        assertTrue(containsMove(moves, new Position(3, 3), new Position(3, 4), 1)); // Nach unten
        assertTrue(containsMove(moves, new Position(3, 3), new Position(2, 3), 1)); // Nach links
        assertTrue(containsMove(moves, new Position(3, 3), new Position(4, 3), 1)); // Nach rechts
        
        // Wächter sollte sich nur 1 Feld bewegen können
        assertEquals(4, moves.size());
    }
    
    @Test
    public void testTowerHeightMovement() {
        // Brett mit einem roten Turm der Höhe 3
        String fen = "7/7/7/3r33/7/7/7 r";
        Board board = new Board(fen);
        
        // Hole Position des Turms (D4)
        Position pos = new Position(3, 3);
        Piece tower = board.board[3][3];
        
        // Überprüfe Höhe
        assertEquals(3, tower.height);
        
        // Generiere Züge
        List<Move> moves = board.generateMovesForPiece(tower, pos);
        
        // Überprüfe Bewegungen mit Reichweite 3
        assertTrue(containsMove(moves, new Position(3, 3), new Position(3, 0), 3)); // 3 Felder nach oben
        assertTrue(containsMove(moves, new Position(3, 3), new Position(3, 6), 3)); // 3 Felder nach unten
        assertTrue(containsMove(moves, new Position(3, 3), new Position(0, 3), 3)); // 3 Felder nach links
        assertTrue(containsMove(moves, new Position(3, 3), new Position(6, 3), 3)); // 3 Felder nach rechts
        
        // Überprüfe auch Bewegungen mit 1 und 2 Schritte
        assertTrue(containsMove(moves, new Position(3, 3), new Position(3, 2), 1)); // 1 Feld nach oben
        assertTrue(containsMove(moves, new Position(3, 3), new Position(3, 1), 2)); // 2 Felder nach oben
    }
    
    @Test
    public void testEmptyBoardNoMoves() {
        // Völlig leeres Brett
        String fen = "7/7/7/7/7/7/7 r";
        Board board = new Board(fen);
        
        // Generiere alle Züge für Rot
        List<Move> moves = board.generateMoves();
        
        // Es sollten keine Züge möglich sein
        assertTrue(moves.isEmpty());
    }
    
    @Test
    public void testBitboardImplementation() {
        // Erstelle einfaches Brett mit einem roten Turm auf D4
        String fen = "7/7/7/3r13/7/7/7 r";
        Board board = new Board(fen);
        
        // Generiere Züge mit normaler Implementierung
        List<Move> normalMoves = board.generateMoves();
        
        // Generiere Züge mit Bitboard-Implementierung
        List<Move> bitboardMoves = board.generateMovesWithBitboards();
        
        // Überprüfe, ob beide Implementierungen die gleiche Anzahl von Zügen generieren
        assertEquals(normalMoves.size(), bitboardMoves.size());
        
        // Überprüfe, ob alle Züge aus der normalen Implementierung auch in der Bitboard-Implementierung vorhanden sind
        for (Move move : normalMoves) {
            boolean found = false;
            for (Move bitMove : bitboardMoves) {
                if (move.from.x == bitMove.from.x && move.from.y == bitMove.from.y &&
                    move.to.x == bitMove.to.x && move.to.y == bitMove.to.y &&
                    move.step == bitMove.step) {
                    found = true;
                    break;
                }
            }
            assertTrue("Zug " + move + " fehlt in der Bitboard-Implementierung", found);
        }
    }
    
    // Hilfsmethode zum Prüfen, ob ein bestimmter Zug in der Liste enthalten ist
    private boolean containsMove(List<Move> moves, Position from, Position to, int step) {
        for (Move move : moves) {
            if (move.from.x == from.x && move.from.y == from.y && 
                move.to.x == to.x && move.to.y == to.y && 
                move.step == step) {
                return true;
            }
        }
        return false;
    }
}