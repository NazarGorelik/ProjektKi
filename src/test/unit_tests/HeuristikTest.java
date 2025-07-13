package unit_tests;

import main.AlphaBeta.MoveOrderingHeuristik.Heuristik;
import main.Board;
import main.models.Move;
import main.models.Player;
import main.models.Position;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class HeuristikTest {

    @Test
    public void testHeuristicSortingPrefersCapturesAndGuards() {
        // Setup: Guard red, two blue Türme als Targets (rechts und unten), einer ist Guard
        Board b = new Board("RG6/b16/1B14/7/7/7/7 r");

        List<Move> allMoves = b.generateMoves();
        assertTrue(allMoves.size() > 1);
        List<Move> sorted = Heuristik.sortMovesHeuristically(b, allMoves, Player.RED);
        Move best = sorted.get(0);
        Position guardTarget = new Position(0,1);
        assertEquals(guardTarget, best.to);
    }

    @Test
    public void testHigherStepPreferred() {
        Board b = new Board("RG6/7/7/3r3/7/7/BG6 r");

        List<Move> allMoves = b.generateMoves();
        List<Move> sorted = Heuristik.sortMovesHeuristically(b, allMoves, Player.RED);

        List<Move> rightMoves = sorted.stream()
                .filter(m -> m.from.equals(new Position(3,3)) && m.to.y == 3 && m.to.x > 3).collect(Collectors.toList());;

        assertEquals(3, rightMoves.size());

        // Prüfen, ob zuerst der weiteste Zug kommt
        assertTrue(rightMoves.get(0).step > rightMoves.get(1).step);
        assertTrue(rightMoves.get(1).step > rightMoves.get(2).step);
    }
}
