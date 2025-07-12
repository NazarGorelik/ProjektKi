package main;

import main.AlphaBeta.AlphaBetaAI;
import main.AlphaBeta.BasicAI;
import main.AlphaBeta.Evaluation;
import main.AlphaBeta.Minimax;
import main.models.Move;
import main.models.Player;

import java.util.List;

public class Main {
    public static long nodesVisited = 0;
    static final int SEARCH_DEPTH = 5;
    public static void main(String[] args) {
      String fen = "r1r11RG1r1r1/2r11r12/3r13/7/3b13/2b11b12/b1b11BG1b1b1 b";
      Board board = new Board(fen);
      List<Move> moves = board.generateMoves();

      System.out.println("Mögliche Züge: " + moves.size());
      System.out.println(String.join(", ", moves.toString()));

      for (int y = 0; y < board.board.length; y++) {
          for (int x = 0; x < board.board[y].length; x++) {
              System.out.print(board.board[y][x] + " ");
          }
          System.out.println();
      }

      System.out.println("\n--- Starte API-Server ---");
      try {
          // Starte den GameController (API-Server)
          GameController controller = new GameController();
          controller.start();
          System.out.println("API-Server läuft auf http://localhost:8080/api");
          System.out.println("Zum Beenden drücke STRG+C");
      } catch (Exception e) {
          System.err.println("Fehler beim Starten des API-Servers: " + e.getMessage());
          e.printStackTrace();
      }

        //für M und A Zustände mit der Zeit und best Moves
        // for (int depth = 1; depth <= 6; depth++) {
        //     System.out.println("\nTiefe " + depth + ":");

        //     nodesVisited = 0;
        //     long startTimeM = System.nanoTime();
        //     Minimax.minimax(board, depth, true, Player.RED);
        //     long endTimeM = System.nanoTime();
        //     System.out.println("Minimax:     " + nodesVisited + " Zustände besucht");
        //     long durationNsM = endTimeM - startTimeM;
        //     double durationMsM = durationNsM / 1_000_000.0;
        //     System.out.println(durationMsM + " ms");
        //     System.out.println("Best Move: " + BasicAI.findBestMove(board, depth));

        //     System.out.println();

        //     nodesVisited = 0;
        //     long startTimeA = System.nanoTime();
        //     AlphaBetaAI.alphaBeta(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, Player.RED);
        //     long endTimeA = System.nanoTime();
        //     System.out.println("Alpha-Beta:  " + nodesVisited + " Zustände besucht");
        //     long durationNsA = endTimeA - startTimeA;
        //     double durationMsA = durationNsA / 1_000_000.0;
        //     System.out.println(durationMsA + " ms");
        //     System.out.println("Best Move: " + BasicAI.findBestMoveMinimax(board, depth));
        // }

        //run perft
//         long nodes = Perft.perft(board, SEARCH_DEPTH);
//         System.out.println("Perft to depth " + SEARCH_DEPTH + ": " + nodes + " nodes");
    }
}
