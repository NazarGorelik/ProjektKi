
package main;

import java.util.ArrayList;
import java.util.List;
import main.models.Move;
import main.models.Piece;
import main.models.Player;
import main.models.Position;

public class Board {

   public final Piece[][] board = new Piece[7][7];
   private static final int SIZE = 7;
   private static final int GUARD_HEIGHT = 1;
   public Player toMove;

   public long redPieces  = 0L;
   public long bluePieces = 0L;
   public long guards     = 0L;
   public final long[] heights = new long[8];

   private static long sqMask(int row, int col) { return 1L << (row * SIZE + col); }

   public Board(String fen) {
      if (!fen.contains(" ") || fen.split(" ").length != 2)
         throw new IllegalArgumentException("Invalid FEN: missing side-to-move token");

      String[] parts = fen.split(" ");
      String[] rows  = parts[0].split("/");
      if (rows.length != SIZE)
         throw new IllegalArgumentException("FEN must have 7 rows");

      toMove = parts[1].equals("r") ? Player.RED : Player.BLUE;

      for (int x = 0; x < SIZE; x++) {
         String row = rows[x];
         int positionInLine = 0;
         for (int y = 0; y < row.length(); ) {
            char ch = row.charAt(y);

            if(row.charAt(y) == 'r' || row.charAt(y) == 'R') {
               // big letter - guard red
               if (Character.isUpperCase(row.charAt(y))) {
                  setSquare(x, positionInLine, new Piece(Player.RED, GUARD_HEIGHT, true));
                  y+=2;
               }
               //small letter - turm red
               else{
                  int height = row.charAt(++y) - '0';
                  setSquare(x, positionInLine, new Piece(Player.RED, height, false));
                  y++;
               }
            }else if(row.charAt(y) == 'b' || row.charAt(y) == 'B'){
               // big letter - guard blue
               if (Character.isUpperCase(row.charAt(y))) {
                  setSquare(x, positionInLine, new Piece(Player.BLUE, GUARD_HEIGHT, true));
                  y+=2;
               }
               //small letter - turm blue
               else{
                  int height = row.charAt(++y) - '0';
                  setSquare(x, positionInLine, new Piece(Player.BLUE, height, false));
                  y++;
               }
            }else if(Character.isDigit(row.charAt(y))) {
               int endEmptyCellPosition = positionInLine+row.charAt(y) - '0';
               for(int a = positionInLine; a < endEmptyCellPosition;a++, positionInLine++){
                  setSquare(x, a, new Piece());
               }
               y++;
               continue;
            }
            positionInLine++;
         }
      }
   }

   public List<Move> generateMoves() {
      return generateMovesBitboard();
   }

   private List<Move> generateMovesBitboard() {
      List<Move> moves = new ArrayList<>();
      long own = toMove == Player.RED ? redPieces : bluePieces;

      for (int idx = 0; idx < 49; idx++) {
         if ((own & (1L << idx)) == 0) continue;
         int row = idx / SIZE, col = idx % SIZE;
         Piece p = board[row][col];
         moves.addAll(generateMovesForPieceBit(p, new Position(col, row)));
      }
      return moves;
   }

   public List<Move> generateMovesForPieceBit(Piece piece, Position from) {
      List<Move> result = new ArrayList<>();
      int[][] DIRS = {{0,-1},{-1,0},{1,0},{0,1}};
      for (int[] d : DIRS) {
         for (int step = 1; step <= piece.height; step++) {
            int tx = from.x + d[0] * step, ty = from.y + d[1] * step;
            if (tx < 0 || tx >= SIZE || ty < 0 || ty >= SIZE) break;

            long mask = sqMask(ty, tx);
            boolean occRed  = (redPieces  & mask) != 0;
            boolean occBlue = (bluePieces & mask) != 0;

            if (!occRed && !occBlue) {                     // empty
               result.add(new Move(from, new Position(tx, ty), step));
               continue;
            }

            Piece target = board[ty][tx];
            if (target.player == piece.player) {           // own piece
               if (!piece.isGuard && !target.isGuard)
                  result.add(new Move(from, new Position(tx, ty), step));
               break;                                     // stop ray
            }

            if (piece.isGuard) {
               result.add(new Move(from, new Position(tx, ty), step));
            } else if (piece.height == 1) {
               if (target.height == 1)
                  result.add(new Move(from, new Position(tx, ty), step));
            } else {                                       // tower >1
               if (target.height <= step)
                  result.add(new Move(from, new Position(tx, ty), step));
            }
            break;                                         // stop ray after capture test
         }
      }
      return result;
   }

   public void setSquare(int row, int col, Piece p) {
      long mask = sqMask(row, col);
      redPieces &= ~mask;
      bluePieces &= ~mask;
      guards &= ~mask;
      for (int h = 1; h <= 7; h++) heights[h] &= ~mask;

      if (p != null && p.player != null) {
         if (p.player == Player.RED) redPieces |= mask;
         else bluePieces |= mask;
         if (p.isGuard) guards |= mask;
         heights[p.height] |= mask;
      }
      board[row][col] = p;
   }

   public Player getToMove() { return toMove; }
}
