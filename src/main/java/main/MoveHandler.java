package main;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import main.models.Move;
import main.models.MoveContext;
import main.models.Piece;
import main.models.Player;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MoveHandler {
    public static void applyMoveFrontend(HttpExchange exchange, Board board, Move move) throws IOException {
        int toY = move.to.y;
        int toX = move.to.x;
        int fromY = move.from.y;
        int fromX = move.from.x;

        Piece movingPiece = board.board[fromY][fromX];
        Piece targetPiece = board.board[toY][toX];

        boolean stacking = (targetPiece != null && targetPiece.player != null &&
                targetPiece.player == movingPiece.player &&
                !targetPiece.isGuard && !movingPiece.isGuard);

        // Neues FEN generieren
        applyMove(board, move);
        String newFen = generateFEN(board.board, board.toMove);

        // Check for game-ending conditions
        boolean gameOver = false;
        String winner = null;
        String winReason = null;

        // Check if a guard was captured
        if (!stacking && targetPiece != null && targetPiece.player != null && targetPiece.isGuard) {
            gameOver = true;
            winner = movingPiece.player == Player.RED ? "red" : "blue";
            winReason = "Wächter wurde geschlagen";
            System.out.println("GUARD CAPTURED: Game over, winner: " + winner);
        }

        // Check if a guard reached opponent's guard starting position
        if (movingPiece.isGuard) {
            if (movingPiece.player == Player.RED && toX == 3 && toY == 6) {  // D1 (y=6 wegen 7-1=6)
                gameOver = true;
                winner = "red";
                winReason = "Roter Wächter hat D1 erreicht";
                System.out.println("RED GUARD REACHED D1: Game over, winner: red");
            } else if (movingPiece.player == Player.BLUE && toX == 3 && toY == 0) {  // D7 (y=0 wegen 7-7=0)
                gameOver = true;
                winner = "blue";
                winReason = "Blauer Wächter hat D7 erreicht";
                System.out.println("BLUE GUARD REACHED D7: Game over, winner: blue");
            }
        }

        // JSON-Antwort erstellen
        JSONObject response = new JSONObject();
        response.put("newFen", newFen);
        response.put("move", move.toString());

        // Add game-ending info to response if applicable
        if (gameOver) {
            response.put("gameOver", true);
            response.put("winner", winner);
            response.put("winReason", winReason);
            System.out.println("Sending game over response: " + response.toJSONString());
        } else {
            response.put("gameOver", false);
        }

        // Antwort senden
        sendJsonResponse(exchange, 200, response.toJSONString());
    }

    public static MoveContext applyMove(Board board, Move move){
        int toY = move.to.y;
        int toX = move.to.x;
        int fromY = move.from.y;
        int fromX = move.from.x;
        int steps = move.step;

        Piece movingPiece = board.board[move.from.y][move.from.x];
        Piece targetPiece = board.board[move.to.y][move.to.x];

        MoveContext ctx = new MoveContext(
                move.from, move.to,
                Piece.clonePiece(movingPiece),
                Piece.clonePiece(targetPiece),
                board.getToMove()
        );

        boolean stacking = (targetPiece != null && targetPiece.player != null &&
                targetPiece.player == movingPiece.player &&
                !targetPiece.isGuard && !movingPiece.isGuard);

        // For tower pieces with height > 1, implement splitting based on movement distance
        if (!movingPiece.isGuard && movingPiece.height > 1) {
            // Calculate pieces that move vs stay based on steps
            int remainingHeight = movingPiece.height - steps;

            // Only create a remaining piece if some height stays behind
            if (remainingHeight > 0) {
                // Create piece to move with height equal to steps (or piece height if smaller)
                Piece movingTower = new Piece(movingPiece.player, steps, false);

                // Create piece to stay with remaining height
                Piece remainingPiece = new Piece(movingPiece.player, remainingHeight, false);

                if (stacking) {
                    // Stack the moving piece onto target
                    targetPiece.height += steps;
                } else {
                    // Place the moving piece at destination
                    board.setSquare(toY, toX, movingTower);
                }

                // Keep remaining piece at original position
                board.setSquare(fromY, fromX, remainingPiece);
//                System.out.println("Split: Moving " + steps + ", leaving " + remainingHeight);
            } else {
                // Full tower moves (no splitting)
                if (stacking) {
                    // Stack the whole piece
                    targetPiece.height += movingPiece.height;
                    board.setSquare(fromY, fromX, new Piece());
                } else {
                    // Move the whole piece
                    board.setSquare(toY, toX, movingPiece);
                    board.setSquare(fromY, fromX, new Piece());
                }
//                System.out.println("Full move: Moving entire tower with height " + movingPiece.height);
            }
        } else {
            // For guards and height 1 towers, move the entire piece
            if (stacking) {
                // Stack the whole piece
                targetPiece.height += movingPiece.height;
                board.setSquare(fromY, fromX, new Piece());
            } else {
                // Move the whole piece
                board.setSquare(toY, toX, movingPiece);
                board.setSquare(fromY, fromX, new Piece());
            }
        }

        // Spieler wechseln für das neue FEN
        board.toMove = (board.getToMove() == Player.RED) ? Player.BLUE : Player.RED;

        // Neues FEN generieren
        return ctx;
    }

    public static void undoMove(Board board, MoveContext ctx) {
        board.setSquare(ctx.from.y, ctx.from.x, ctx.originalFromPiece);
        board.setSquare(ctx.to.y, ctx.to.x, ctx.originalToPiece);
        board.toMove = ctx.previousPlayer;
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/json");

        exchange.sendResponseHeaders(statusCode, json.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    public static String generateFEN(Piece[][] board, Player playerToMove) {
        StringBuilder fen = new StringBuilder();

        for (int y = 0; y < 7; y++) {
            int emptyCount = 0;

            for (int x = 0; x < 7; x++) {
                Piece piece = board[y][x];

                if (piece == null || piece.player == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }

                    if (piece.isGuard) {
                        fen.append(piece.player == Player.RED ? "R" : "B").append("G");
                    } else {
                        fen.append(piece.player == Player.RED ? "r" : "b").append(piece.height);
                    }
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (y < 6) {
                fen.append("/");
            }
        }
        fen.append(" ").append(playerToMove == Player.RED ? "r" : "b");

        return fen.toString();
    }
}