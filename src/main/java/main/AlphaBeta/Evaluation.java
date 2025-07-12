package main.AlphaBeta;

import main.Board;
import main.models.*;

import java.util.List;

// positive -> good for the player
// negative -> bad for the player
// zero -> balanced board (same probabilities of winning)
public class Evaluation {
    static int TOWER_COUNT_BONUS = 1;
    static int GUARD_BASE_VALUE = 1267;
    static int TOWER_BASE_VALUE = 59;
    static int HEIGHT_MULTIPLIER = 7;
    static int CENTER_BONUS = 27;
    static int SAME_LINE_BONUS = 1;
    static int GUARD_GOAL_PROXIMITY_MULT = 13;
    static int CROWDING_PENALTY = 1;
    static int MOBILITY_MULT = 5;
    static int CONTROLLED_BONUS = 12;
    static int BLOCKED_TOWER_PENALTY = 16;
    static int GUARD_MOBILITY_MULT = 1;
    static int GUARD_ATTACK_BONUS = 32;
    static int GUARD_CENTER_BONUS = 5;
    static int FRIENDLY_GUARD_SUPPORT_BONUS = 62;
    static int ENEMY_THREAT_PENALTY = 31;
    static int RELATIVE_RATIO_MULTIPLIER = 17;

    public static int geneticEvaluate(Board board, Player perspective, GeneSet genes){
        TOWER_COUNT_BONUS = genes.towerCountBonus;
        GUARD_BASE_VALUE = genes.guardBaseValue;
        TOWER_BASE_VALUE =  genes.towerBaseValue;
        HEIGHT_MULTIPLIER =  genes.heightMultiplier;
        CENTER_BONUS = genes.centerBonus;
        SAME_LINE_BONUS =  genes.sameLineBonus;
        GUARD_GOAL_PROXIMITY_MULT = genes.guardGoalProximityMult;
        CROWDING_PENALTY = genes.crowdingPenalty;
        MOBILITY_MULT = genes.mobilityMult;
        CONTROLLED_BONUS = genes.controlledBonus;
        BLOCKED_TOWER_PENALTY = genes.blockedTowerPenalty;
        GUARD_MOBILITY_MULT = genes.guardMobilityMult;
        GUARD_ATTACK_BONUS = genes.guardAttackBonus;
        GUARD_CENTER_BONUS = genes.centerBonus;
        FRIENDLY_GUARD_SUPPORT_BONUS = genes.friendlyGuardSupportBonus;
        ENEMY_THREAT_PENALTY = genes.enemyThreatPenalty;
        RELATIVE_RATIO_MULTIPLIER = genes.relativeRatioMultiplier;

        return evaluate(board, perspective);
    }

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
                if (p != null && p.player != null && p.isGuard) {
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
                if (p == null || p.player == null) continue;

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
        if (p.player != perspective) return 0; // Only consider own pieces
        int score = 0;

        Position from = new Position(x, y);
        List<Move> legalMoves = board.generateMovesForPieceBit(p,from);
        int mobility = legalMoves.size();

        if (p.isGuard) {
            // GUARD-SPEZIFISCHE BEWERTUNG
            score += mobility * GUARD_MOBILITY_MULT;

            // Bewerte jeden Guard-Zug einzeln
            for (Move move : legalMoves) {
                Piece target = board.board[move.to.y][move.to.x];

                // Angriffs-Bonus für Guards
                if (target != null && target.player != null && target.player != perspective) {
                    score += GUARD_ATTACK_BONUS;

                    // Extra Bonus für Turm-Angriffe basierend auf Turmhöhe
                    if (!target.isGuard) {
                        score += target.height * 5;
                    }
                }

                // Center-Kontrolle für Guards
                if (move.to.x >= 2 && move.to.x <= 4 && move.to.y >= 2 && move.to.y <= 4) {
                    score += GUARD_CENTER_BONUS;
                }

                // Feindlicher Wächter-Nähe
                if (target == null || target.player == null) {
                    score += CONTROLLED_BONUS;
                }
            }

        } else {
            // TURM-BEWERTUNG (wie vorher)
            score += mobility * MOBILITY_MULT;
            score += mobility * p.height;

            for (Move move : legalMoves) {
                Piece target = board.board[move.to.y][move.to.x];
                if (target == null || target.player == null || target.player != perspective) {
                    score += CONTROLLED_BONUS;
                }
            }
        }

        // Penalty für blockierte Figuren (gilt für alle)
        if (mobility == 0) {
            score -= BLOCKED_TOWER_PENALTY;
        }

        return score;
    }

    private static int evaluateSecurity(int friendlyNearby, int enemyNearby) {
        int score = 0;
        score += friendlyNearby * FRIENDLY_GUARD_SUPPORT_BONUS;
        score -= enemyNearby * ENEMY_THREAT_PENALTY;
        score += (friendlyNearby - enemyNearby) * RELATIVE_RATIO_MULTIPLIER;

        return score;
    }
}
