package main.AlphaBeta;

import main.Board;
import main.models.Move;
import main.models.Piece;
import main.models.Player;
import main.models.Position;

import java.util.List;

// positive -> good for the player
// negative -> bad for the player
// zero -> balanced board (same probabilities of winning)
public class Evaluation {

    public static int evaluateStatic(Board board, Player perspective) {
        int score = 0;

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                Piece p = board.board[y][x];
                if (p == null || p.player == null) continue;

                int value = p.isGuard ? 1000 : 50 + (p.height * 10);
                score += (p.player == perspective) ? value : -value;
            }
        }

        return score;
    }

    public static int evaluate(Board board, Player perspective)
    {
        int materialScore = 0;
        int positionScore = 0;
        int mobilityScore = 0;

        final int TOWER_COUNT_BONUS = 5;

        int perspectiveTowerCount = 0;
        int opponentTowerCount = 0;

        int friendlyTowersNearGuard = 0;
        int enemyTowersNearGuard = 0;

        Position ownGuardPos = null;
        Position enemyGuardPos = null;

        // First, identify guards to avoid scanning twice
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                Piece p = board.board[x][y];
                if (p.player != null && p.isGuard) {
                    if (p.player == perspective)
                        ownGuardPos = new Position(x, y);
                    else
                        enemyGuardPos = new Position(x, y);
                }
            }
        }

        // Now single loop for material and position scoring
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                Piece p = board.board[x][y];
                if (p.player == null) continue;

                materialScore += evaluateMaterial(perspective, p);
                positionScore += evaluatePosition(x, y, perspective, p, ownGuardPos, enemyGuardPos);
                mobilityScore += evaluateMobility(board, x, y, perspective, p);

                // Count towers (for tower count bonus)
                if (!p.isGuard) {
                    if (p.player == perspective) perspectiveTowerCount++;
                    else opponentTowerCount++;
                }

                if (ownGuardPos != null && !p.isGuard) {
                    int dist = Math.abs(x - ownGuardPos.x) + Math.abs(y - ownGuardPos.y);
                    if (dist <= 2) {
                        if (p.player == perspective) friendlyTowersNearGuard++;
                        else enemyTowersNearGuard++;
                    }
                }
            }
        }

        // Apply tower count bonus
        int countBonus = (perspectiveTowerCount - opponentTowerCount) * TOWER_COUNT_BONUS;
        // Apply security score
        int securityScore = evaluateSecurity(friendlyTowersNearGuard, enemyTowersNearGuard);

        return materialScore + positionScore + mobilityScore + securityScore + countBonus;
    }

    private static int evaluateMaterial(Player perspective, Piece p) {
        final int GUARD_BASE_VALUE = 1000;
        final int TOWER_BASE_VALUE = 50;
        final int HEIGHT_MULTIPLIER = 10;

        int pieceScore;

        if (p.isGuard) {
            pieceScore = GUARD_BASE_VALUE;
        } else {
            pieceScore = TOWER_BASE_VALUE + (p.height * HEIGHT_MULTIPLIER);
        }

        return (p.player == perspective) ? pieceScore : -pieceScore;
    }

    private static int evaluatePosition(int x, int y, Player perspective, Piece p,
                                        Position ownGuardPos, Position enemyGuardPos) {
        final int CENTER_BONUS = 15;
        final int SAME_LINE_BONUS = 25;
        final int GUARD_GOAL_PROXIMITY_MULT = 10;
        final int CROWDING_PENALTY = 10;

        int pieceScore = 0;

        if (p.isGuard) {
            int goalRow = (p.player == Player.RED) ? 0 : 6;
            int distToGoal = Math.abs(y - goalRow);
            pieceScore += (6 - distToGoal) * GUARD_GOAL_PROXIMITY_MULT;
        }

        // Center 3x3
        if (x >= 2 && x <= 4 && y >= 2 && y <= 4) {
            pieceScore += CENTER_BONUS;
        }

        // Same line/column and distance to enemy guard
        if (enemyGuardPos != null && !p.isGuard) {
            if (x == enemyGuardPos.x || y == enemyGuardPos.y)
                pieceScore += SAME_LINE_BONUS;

            int dist = Math.abs(x - enemyGuardPos.x) + Math.abs(y - enemyGuardPos.y);
            pieceScore += Math.max(0, 10 - dist);
        }

        // Crowding near own guard
        if (ownGuardPos != null && !p.isGuard && p.player == perspective) {
            int dist = Math.abs(x - ownGuardPos.x) + Math.abs(y - ownGuardPos.y);
            if (dist <= 2) pieceScore -= CROWDING_PENALTY;
        }

        return (p.player == perspective) ? pieceScore : -pieceScore;
    }

    private static int evaluateMobility(Board board, int x, int y, Player perspective, Piece p) {
        if (p.player != perspective || p.isGuard) return 0; // Only consider own towers

        final int MOBILITY_MULT = 2;       // Flat value per move
        final int CONTROLLED_BONUS = 1;    // Bonus per controlled square
        final int BLOCKED_TOWER_PENALTY = 20;

        int score = 0;

        Position from = new Position(x, y);
        List<Move> legalMoves = board.generateMovesForPieceBit(p, from);
        int mobility = legalMoves.size();

        // Bonus for number of legal moves
        score += mobility * MOBILITY_MULT;

        // Bonus: mobility × height (taller mobile towers are more valuable)
        score += mobility * p.height;

        // Bonus for each controlled square (own or enemy territory)
        for (Move move : legalMoves) {
            Piece target = board.board[move.to.x][move.to.y];
            if (target.player == null || target.player != perspective) {
                score += CONTROLLED_BONUS;
            }
        }

        // Penalty for blocked towers
        if (mobility == 0) {
            score -= BLOCKED_TOWER_PENALTY;
        }

        return score;
    }

    private static int evaluateSecurity(int friendlyNearby, int enemyNearby) {
        final int FRIENDLY_GUARD_SUPPORT_BONUS = 20;
        final int ENEMY_THREAT_PENALTY = 25;
        final int RELATIVE_RATIO_MULTIPLIER = 10;

        int score = 0;
        score += friendlyNearby * FRIENDLY_GUARD_SUPPORT_BONUS;
        score -= enemyNearby * ENEMY_THREAT_PENALTY;
        score += (friendlyNearby - enemyNearby) * RELATIVE_RATIO_MULTIPLIER;

        return score;
    }
}
