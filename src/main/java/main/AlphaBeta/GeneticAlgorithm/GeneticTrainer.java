package main.AlphaBeta.GeneticAlgorithm;

import main.AlphaBeta.Evaluation;
import main.Board;
import main.models.GeneSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticTrainer {
    private static final int POP_SIZE = 50;
    private static final int GENERATIONS = 100;
    private static final int TOURNAMENT_SIZE = 5;
    private static final double MUTATION_RATE = 0.2;

    private static final Random rand = new Random();

    public static GeneSet train(List<Board> trainingPositions) {
        List<GeneSet> population = new ArrayList<>();
        for (int i = 0; i < POP_SIZE; i++) {
            population.add(new GeneSet(rand));
        }

        for (int generation = 0; generation < GENERATIONS; generation++) {
            population.sort((a, b) -> Double.compare(fitness(b, trainingPositions),
                    fitness(a, trainingPositions)));

            List<GeneSet> nextGen = new ArrayList<>();
            nextGen.add(population.get(0)); // Elitism

            while (nextGen.size() < POP_SIZE) {
                GeneSet parent1 = tournamentSelection(population, trainingPositions);
                GeneSet parent2 = tournamentSelection(population, trainingPositions);
                GeneSet child = crossover(parent1, parent2);
                if (rand.nextDouble() < MUTATION_RATE) {
                    child.mutate(rand);
                }
                nextGen.add(child);
            }
            population = nextGen;
            System.out.println("Generation " + generation + " best fitness: " +
                    fitness(population.get(0), trainingPositions));
        }

        return population.get(0);
    }

    private static double fitness(GeneSet genes, List<Board> positions) {
        double score = 0;
        for (Board board : positions) {
            score += Evaluation.geneticEvaluate(board, board.getToMove(), genes); // Use overload
        }
        return score;
    }

    private static GeneSet crossover(GeneSet a, GeneSet b) {
        GeneSet child = new GeneSet();
        child.towerCountBonus = (a.towerCountBonus + b.towerCountBonus) / 2;
        child.guardBaseValue = (a.guardBaseValue + b.guardBaseValue) / 2;
        child.towerBaseValue = (a.towerBaseValue + b.towerBaseValue) / 2;
        child.heightMultiplier = (a.heightMultiplier + b.heightMultiplier) / 2;

        child.centerBonus = (a.centerBonus + b.centerBonus) / 2;
        child.sameLineBonus = (a.sameLineBonus + b.sameLineBonus) / 2;
        child.guardGoalProximityMult = (a.guardGoalProximityMult + b.guardGoalProximityMult) / 2;
        child.crowdingPenalty = (a.crowdingPenalty + b.crowdingPenalty) / 2;

        child.mobilityMult = (a.mobilityMult + b.mobilityMult) / 2;
        child.controlledBonus = (a.controlledBonus + b.controlledBonus) / 2;
        child.blockedTowerPenalty = (a.blockedTowerPenalty + b.blockedTowerPenalty) / 2;
        child.guardMobilityMult = (a.guardMobilityMult + b.guardMobilityMult) / 2;
        child.guardAttackBonus = (a.guardAttackBonus + b.guardAttackBonus) / 2;
        child.guardCenterBonus = (a.guardCenterBonus + b.guardCenterBonus) / 2;

        child.friendlyGuardSupportBonus = (a.friendlyGuardSupportBonus + b.friendlyGuardSupportBonus) / 2;
        child.enemyThreatPenalty = (a.enemyThreatPenalty + b.enemyThreatPenalty) / 2;
        child.relativeRatioMultiplier = (a.relativeRatioMultiplier + b.relativeRatioMultiplier) / 2;

        return child;
    }

    private static GeneSet tournamentSelection(List<GeneSet> population, List<Board> positions) {
        GeneSet best = null;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            GeneSet g = population.get(rand.nextInt(POP_SIZE));
            if (best == null || fitness(g, positions) > fitness(best, positions)) {
                best = g;
            }
        }
        return best;
    }
}

