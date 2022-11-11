package me.asarix.com.weight;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class CalculatedDungeon {

    public Weight totalWeight;
    double xp;
    double modifier;
    int level;
    double excess;
    double xpForNext;
    double progress;
    double levelWithProgress;
    double unlockableLevelWithProgress;
    int levelCap = 50;
    int uncappedLevel;
    double xpCurrent;
    double remaining;
    double weight;
    double excessWeight;

    public CalculatedDungeon(double xp, double modifier) {
        this.xp = xp;
        this.modifier = modifier;
        calculate(xp);
        int level50Experience = 569809640;
        weight = Math.pow(unlockableLevelWithProgress, 4.5) * this.modifier;
        excessWeight = 0;

        if (xp > level50Experience) {
            double remaining = xp - level50Experience;
            double splitter = (4 * level50Experience) / weight;
            excessWeight = Math.pow(remaining / splitter, 0.968);
        }

        totalWeight = new Weight(weight, excessWeight);
    }

    private void calculate(double xp) {
        /* the level ignoring the cap and using only the table */
        uncappedLevel = -1;

        /* the amount of xp over the amount required for the level (used for calculation progress to next level) */
        xpCurrent = xp;

        /* like xpCurrent but ignores cap */
        remaining = xp;

        for (int i = 0; i <= levelCap; i++) {
            int xpRequired = xpFromIndex(i);
            if (remaining > xpRequired) {
                uncappedLevel++;
                remaining -= xpRequired;
            } else {
                xpCurrent = remaining;
                break;
            }
        }

        // not sure why this is floored but I'm leaving it in for now
        xpCurrent = (int) Math.floor(xpCurrent);

        /* the level as displayed by in game UI */
        level = Math.min(levelCap, uncappedLevel);

        /* the amount amount of xp needed to reach the next level (used for calculation progress to next level) */
        xpForNext = level < levelCap ? Math.ceil(xpFromIndex(level + 1)) : Double.POSITIVE_INFINITY;

        /* the fraction of the way toward the next level */
        progress = Math.max(0, Math.min(xpCurrent / xpForNext, 1));

        /* a floating point value representing the current level for example if you are half way to level 5 it would be 4.5 */
        levelWithProgress = level + progress;

        /* a floating point value representing the current level ignoring the in-game unlockable caps for example if you are half way to level 5 it would be 4.5 */
        unlockableLevelWithProgress = Math.min(uncappedLevel + progress, levelCap);

        excess = level == levelCap ? remaining : 0;
    }

    private int xpFromIndex(int index) {
        LinkedHashMap<Integer, Integer> xpTable = xpTable();
        LinkedList<Integer> levels = new LinkedList<>(xpTable.keySet());
        return xpTable.get(levels.get(index));
    }

    private LinkedHashMap<Integer, Integer> xpTable() {
        LinkedHashMap<Integer, Integer> xpTable = new LinkedHashMap<>();
        xpTable.put(0, 0);
        xpTable.put(1, 50);
        xpTable.put(2, 75);
        xpTable.put(3, 110);
        xpTable.put(4, 160);
        xpTable.put(5, 230);
        xpTable.put(6, 330);
        xpTable.put(7, 470);
        xpTable.put(8, 670);
        xpTable.put(9, 950);
        xpTable.put(10, 1340);
        xpTable.put(11, 1890);
        xpTable.put(12, 2665);
        xpTable.put(13, 3760);
        xpTable.put(14, 5260);
        xpTable.put(15, 7380);
        xpTable.put(16, 10300);
        xpTable.put(17, 14400);
        xpTable.put(18, 20000);
        xpTable.put(19, 27600);
        xpTable.put(20, 38000);
        xpTable.put(21, 52500);
        xpTable.put(22, 71500);
        xpTable.put(23, 97000);
        xpTable.put(24, 132000);
        xpTable.put(25, 180000);
        xpTable.put(26, 243000);
        xpTable.put(27, 328000);
        xpTable.put(28, 445000);
        xpTable.put(29, 600000);
        xpTable.put(30, 800000);
        xpTable.put(31, 1065000);
        xpTable.put(32, 1410000);
        xpTable.put(33, 1900000);
        xpTable.put(34, 2500000);
        xpTable.put(35, 3300000);
        xpTable.put(36, 4300000);
        xpTable.put(37, 5600000);
        xpTable.put(38, 7200000);
        xpTable.put(39, 9200000);
        xpTable.put(40, 12000000);
        xpTable.put(41, 15000000);
        xpTable.put(42, 19000000);
        xpTable.put(43, 24000000);
        xpTable.put(44, 30000000);
        xpTable.put(45, 38000000);
        xpTable.put(46, 48000000);
        xpTable.put(47, 60000000);
        xpTable.put(48, 75000000);
        xpTable.put(49, 93000000);
        xpTable.put(50, 116250000);
        return xpTable;

    }
}
