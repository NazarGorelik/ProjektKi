package main;

import main.AlphaBeta.AlphaBetaAI;
import main.AlphaBeta.BasicAI;
import main.AlphaBeta.GeneticAlgorithm.GeneticDataset;
import main.AlphaBeta.GeneticAlgorithm.GeneticTrainer;
import main.models.GeneSet;
import main.models.Move;
import main.models.Player;

import java.util.List;

public class Main {
    public static long nodesVisited = 0;
    static final int SEARCH_DEPTH = 5;
    public static void main(String[] args) {
        // to run genetic algorithm
//        GeneSet bestGenes = GeneticTrainer.train(GeneticDataset.trainingBoards);

        // to start the game run main function + right click on index.html -> "open in browser"
        String fen = "4r22/4b22/2b1RG3/7/3r23/1b11BG3/b15b1 r";
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

          // to run performance tests
        //für M und A Zustände mit der Zeit und best Moves
//         for (int depth = 1; depth <= 9; depth++) {
//             System.out.println("\nTiefe " + depth + ":");
//
//             nodesVisited = 0;
//             long startTimeA = System.nanoTime();
//             AlphaBetaAI.alphaBeta(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, Player.RED);
//             long endTimeA = System.nanoTime();
//             System.out.println("Alpha-Beta:  " + nodesVisited + " Zustände besucht");
//             long durationNsA = endTimeA - startTimeA;
//             double durationMsA = durationNsA / 1_000_000.0;
//             System.out.println(durationMsA + " ms");
//             System.out.println("Best Move: " + BasicAI.findBestMove(board, depth));
//         }

        // to run perft function
//         long nodes = Perft.perft(board, SEARCH_DEPTH);
//         System.out.println("Perft to depth " + SEARCH_DEPTH + ": " + nodes + " nodes");
    }
}
