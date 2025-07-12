package main.models;

import java.util.Random;

public class GeneSet {
    public int towerCountBonus;
    public int guardBaseValue;
    public int towerBaseValue;
    public int heightMultiplier;
    public int centerBonus;
    public int sameLineBonus;
    public int guardGoalProximityMult;
    public int crowdingPenalty;
    public int mobilityMult;
    public int controlledBonus;
    public int blockedTowerPenalty;
    public int guardMobilityMult;
    public int guardAttackBonus;
    public int guardCenterBonus;
    public int friendlyGuardSupportBonus;
    public int enemyThreatPenalty;
    public int relativeRatioMultiplier;

    // Constructor with random values in a reasonable range
    public GeneSet(Random rand) {
        towerCountBonus = rand.nextInt(11) + 1; // 1 to 11
        guardBaseValue = rand.nextInt(500) + 800;
        towerBaseValue = rand.nextInt(40) + 30;
        heightMultiplier = rand.nextInt(10) + 5;
        centerBonus = rand.nextInt(30);
        sameLineBonus = rand.nextInt(30);
        guardGoalProximityMult = rand.nextInt(20);
        crowdingPenalty = rand.nextInt(20);
        mobilityMult = rand.nextInt(5) + 1;
        controlledBonus = rand.nextInt(5);
        blockedTowerPenalty = rand.nextInt(50);
        guardMobilityMult = rand.nextInt(5) + 1;
        guardAttackBonus = rand.nextInt(50);
        guardCenterBonus = rand.nextInt(10);
        friendlyGuardSupportBonus = rand.nextInt(50);
        enemyThreatPenalty = rand.nextInt(50);
        relativeRatioMultiplier = rand.nextInt(20);
    }

    public GeneSet(){}

    public void mutate(Random rand) {
        int index = rand.nextInt(17);
        switch (index) {
            case 0 -> towerCountBonus += rand.nextInt(3) - 1;
            case 1 -> guardBaseValue += rand.nextInt(21) - 10;
            case 2 -> towerBaseValue += rand.nextInt(11) - 5;
            case 3 -> heightMultiplier += rand.nextInt(5) - 2;
            case 4 -> centerBonus += rand.nextInt(5) - 2;
            case 5 -> sameLineBonus += rand.nextInt(5) - 2;
            case 6 -> guardGoalProximityMult += rand.nextInt(5) - 2;
            case 7 -> crowdingPenalty += rand.nextInt(5) - 2;
            case 8 -> mobilityMult += rand.nextInt(3) - 1;
            case 9 -> controlledBonus += rand.nextInt(3) - 1;
            case 10 -> blockedTowerPenalty += rand.nextInt(11) - 5;
            case 11 -> guardMobilityMult += rand.nextInt(3) - 1;
            case 12 -> guardAttackBonus += rand.nextInt(11) - 5;
            case 13 -> guardCenterBonus += rand.nextInt(3) - 1;
            case 14 -> friendlyGuardSupportBonus += rand.nextInt(11) - 5;
            case 15 -> enemyThreatPenalty += rand.nextInt(11) - 5;
            case 16 -> relativeRatioMultiplier += rand.nextInt(5) - 2;
        }
    }
}
