package main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import main.AlphaBeta.BasicAI;
import main.models.Move;
import main.models.Piece;
import main.models.Position;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static main.Main.SEARCH_DEPTH;

public class GameController {

    private static final int PORT = 8080;
    private HttpServer server;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API-Endpunkte
        server.createContext("/api", new TestHandler());
        server.createContext("/api/moves", new CorsDecorator(new GetMovesHandler()));
        server.createContext("/api/move", new CorsDecorator(new MakeMoveHandler()));
        server.createContext("/api/ai-move", new CorsDecorator(new AiMoveHandler()));
        server.createContext("/api/bitboard", new CorsDecorator(new BitboardConfigHandler()));

        server.setExecutor(null);
        server.start();

        System.out.println("Server gestartet auf Port " + PORT);
    }

    // CORS-Decorator für die Handler
    class CorsDecorator implements HttpHandler {
        private final HttpHandler handler;

        public CorsDecorator(HttpHandler handler) {
            this.handler = handler;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Weiterleiten an den dekorierten Handler
            handler.handle(exchange);
        }
    }

    // Test-Handler für /api
    class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Content-Type", "application/json");

            JSONObject response = new JSONObject();
            response.put("status", "ok");
            response.put("message", "API is running");
            response.put("engine", true ? "Bitboard Engine" : "Classic Engine");
            response.put("useBitboards", true);
            
            String responseString = response.toJSONString();
            exchange.sendResponseHeaders(200, responseString.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(responseString.getBytes());
            os.close();
        }
    }

    // Handler für GET /api/moves?fen=
    class GetMovesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
                return;
            }

            // Parameter auslesen
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String fen = params.get("fen");
            String position = params.get("position");

            if (fen == null || position == null) {
                sendError(exchange, 400, "Missing parameters");
                return;
            }

            try {
                // Brett initialisieren
                Board board = new Board(fen);

                // Position in Koordinaten umwandeln
                int x = position.charAt(0) - 'A';
                int y = 7 - Character.getNumericValue(position.charAt(1));

                Position posFrom = new Position(x, y);
                Piece piece = board.board[y][x];

                // Mögliche Züge generieren
                List<Move> moves = board.generateMovesForPieceBit(piece, posFrom);

                // JSON-Antwort erstellen
                JSONObject response = new JSONObject();
                JSONArray movesArray = new JSONArray();

                for (Move move : moves) {
                    movesArray.add(move.toString());
                }
                response.put("moves", movesArray);

                // Antwort senden
                sendJsonResponse(exchange, 200, response.toJSONString());

            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Error generating moves: " + e.getMessage());
            }
        }
    }

    // Handler für POST /api/move mit Body { fen: "XXX", from: "A1", to: "B2" }
    class MakeMoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
                return;
            }

            try {
                // JSON-Body parsen
                JSONObject requestBody = parseJsonRequest(exchange);
                String fen = (String) requestBody.get("fen");
                String from = (String) requestBody.get("from");
                String to = (String) requestBody.get("to");

                if (fen == null || from == null || to == null) {
                    sendError(exchange, 400, "Missing parameters");
                    return;
                }

                // Brett initialisieren
                Board board = new Board(fen);

                // Positionen in Koordinaten umwandeln
                int fromX = from.charAt(0) - 'A';
                int fromY = 7 - Character.getNumericValue(from.charAt(1));
                int toX = to.charAt(0) - 'A';
                int toY = 7 - Character.getNumericValue(to.charAt(1));

                // Zug überprüfen und ausführen
                Piece movingPiece = board.board[fromY][fromX];
                Position fromPos = new Position(fromX, fromY);

                List<Move> legalMoves = board.generateMovesForPieceBit(movingPiece, fromPos);
                Move move = null;

                System.out.println("FromX: " + fromX + ", FromY: " + fromY + ", ToX: " + toX + ", ToY: " + toY);
                System.out.println("Looking for move from (" + fromX + "," + fromY + ") to (" + toX + "," + toY + ")");
                System.out.println("Legal moves: " + legalMoves);

                for (Move m : legalMoves) {
                    System.out.println("Checking move: " + m + " with to coordinates: (" + m.to.x + "," + m.to.y + ")");
                    if (m.to.x == toX && m.to.y == toY) {
                        move = m;
                        break;
                    }
                }

                if (move == null) {
                    sendError(exchange, 400, "Illegal move");
                    return;
                }

                // Zug ausfuhren
                MoveHandler.applyMoveFrontend(exchange, board, move);

            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Error making move: " + e.getMessage());
            }
        }
    }

    // Handler für GET/POST /api/bitboard (Bitboard-Konfiguration)
    class BitboardConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("GET")) {
                // Status der Bitboard-Konfiguration zurückgeben
                JSONObject response = new JSONObject();
                response.put("useBitboards", true);
                response.put("message", true ? "Bitboard engine is active" : "Classic engine is active");
                
                sendJsonResponse(exchange, 200, response.toJSONString());
                
            } else if (exchange.getRequestMethod().equals("POST")) {
                // Bitboard-Konfiguration ändern
                try {
                    JSONObject requestBody = parseJsonRequest(exchange);
                    Boolean useBitboards = (Boolean) requestBody.get("useBitboards");
                    
                    if (useBitboards == null) {
                        sendError(exchange, 400, "Missing useBitboards parameter");
                        return;
                    }
                    
                    JSONObject response = new JSONObject();
                    response.put("useBitboards", true);
                    response.put("message", "Bitboard configuration updated to: " + (useBitboards ? "ENABLED" : "DISABLED"));
                    
                    sendJsonResponse(exchange, 200, response.toJSONString());
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    sendError(exchange, 500, "Error updating bitboard configuration: " + e.getMessage());
                }
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }
    }

    // Handler für GET /api/ai-move?fen=
    class AiMoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
                return;
            }

            // Parameter auslesen
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String fen = params.get("fen");

            if (fen == null) {
                sendError(exchange, 400, "Missing FEN parameter");
                return;
            }

            try {
                // Brett initialisieren
                Board board = new Board(fen);

                // Alle möglichen Züge für den aktuellen Spieler generieren
                List<Move> moves = board.generateMoves();

                if (moves.isEmpty()) {
                    sendError(exchange, 400, "No legal moves available");
                    return;
                }

                long start = System.currentTimeMillis();
                Move move = BasicAI.findBestMove(board, SEARCH_DEPTH);
                long end = System.currentTimeMillis();

                System.out.println("Execution time: " + (end - start) + " ms");

                // Zug ausfuhren
                MoveHandler.applyMoveFrontend(exchange, board, move);

            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Error generating AI move: " + e.getMessage());
            }
        }
    }

    // Hilfsmethoden

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    private JSONObject parseJsonRequest(HttpExchange exchange) throws IOException, ParseException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);

        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(br);
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/json");

        exchange.sendResponseHeaders(statusCode, json.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private void sendError(HttpExchange exchange, int statusCode, String errorMsg) throws IOException {
        JSONObject error = new JSONObject();
        error.put("error", errorMsg);
        sendJsonResponse(exchange, statusCode, error.toJSONString());
    }

    public static void main(String[] args) {
        try {
            GameController controller = new GameController();
            controller.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 