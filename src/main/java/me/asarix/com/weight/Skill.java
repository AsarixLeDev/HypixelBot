package me.asarix.com.weight;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class Skill {
    int level;

    double excess;
    double xpForNext;
    double progress;
    double levelWithProgress;
    double unlockableLevelWithProgress;
    int levelCap;
    int uncappedLevel;
    double xpCurrent;
    double remaining;

    public Skill(int exp, String skillName) {
        System.out.println("=========================");
        levelCap = (List.of("foraging", "fishing", "alchemy", "taming").contains(skillName)) ? 50 : 60;
        levelByXp(exp);
        System.out.println(skillName + " LEVEL " + level);
        System.out.println("Excess : " + excess);
        System.out.println("xpForNext : " + xpForNext);
        System.out.println("progress : " + progress);
        System.out.println("levelWithProgress : " + levelWithProgress);
        System.out.println("unlockableLevelWithProgress : " + unlockableLevelWithProgress);
        System.out.println("levelCap : " + levelCap);
        System.out.println("uncappedLevel : " + uncappedLevel);
        System.out.println("xpCurrent : " + xpCurrent);
        System.out.println("xpRemaining : " + remaining);
        System.out.println("Maxed : " + isMaxed());
    }

    private void levelByXp(int xp) {
        LinkedHashMap<Integer, Integer> xpTable = levelingXp();

        /** the level ignoring the cap and using only the table */
        uncappedLevel = 0;

        /** the amount of xp over the amount required for the level (used for calculation progress to next level) */
        xpCurrent = xp;

        /** like xpCurrent but ignores cap */
        remaining = xp;

        LinkedList<Integer> levels = new LinkedList<>(xpTable.keySet());

        while (uncappedLevel < 60 && xpTable.get(levels.get(uncappedLevel+1)) <= remaining) {
            uncappedLevel++;
            remaining -= xpTable.get(levels.get(uncappedLevel));
            if (uncappedLevel <= levelCap) {
                xpCurrent = remaining;
            }
        }

        // not sure why this is floored but I'm leaving it in for now
        xpCurrent = (int) Math.floor(xpCurrent);

        /** the level as displayed by in game UI */
        level = Math.min(levelCap, uncappedLevel);

        /** the amount amount of xp needed to reach the next level (used for calculation progress to next level) */
        xpForNext = level < levelCap ? Math.ceil(xpTable.get(levels.get(level + 1))) : Integer.MAX_VALUE;

        /** the fraction of the way toward the next level */
        progress = Math.max(0, Math.min(xpCurrent / xpForNext, 1));

        /** a floating point value representing the current level for example if you are half way to level 5 it would be 4.5 */
        levelWithProgress = level + progress;

        /** a floating point value representing the current level ignoring the in-game unlockable caps for example if you are half way to level 5 it would be 4.5 */
        unlockableLevelWithProgress = Math.min(uncappedLevel + progress, levelCap);

        excess = level == levelCap ? remaining : 0;
    }

    private LinkedHashMap<Integer, Integer> levelingXp() {
        LinkedHashMap<Integer, Integer> table = new LinkedHashMap<>();
        table.put(0, 0);
        table.put(1, 50);
        table.put(2, 125);
        table.put(3, 200);
        table.put(4, 300);
        table.put(5, 500);
        table.put(6, 750);
        table.put(7, 1000);
        table.put(8, 1500);
        table.put(9, 2000);
        table.put(10, 3500);
        table.put(11, 5000);
        table.put(12, 7500);
        table.put(13, 10000);
        table.put(14, 15000);
        table.put(15, 20000);
        table.put(16, 30000);
        table.put(17, 50000);
        table.put(18, 75000);
        table.put(19, 100000);
        table.put(20, 200000);
        table.put(21, 300000);
        table.put(22, 400000);
        table.put(23, 500000);
        table.put(24, 600000);
        table.put(25, 700000);
        table.put(26, 800000);
        table.put(27, 900000);
        table.put(28, 1000000);
        table.put(29, 1100000);
        table.put(30, 1200000);
        table.put(31, 1300000);
        table.put(32, 1400000);
        table.put(33, 1500000);
        table.put(34, 1600000);
        table.put(35, 1700000);
        table.put(36, 1800000);
        table.put(37, 1900000);
        table.put(38, 2000000);
        table.put(39, 2100000);
        table.put(40, 2200000);
        table.put(41, 2300000);
        table.put(42, 2400000);
        table.put(43, 2500000);
        table.put(44, 2600000);
        table.put(45, 2750000);
        table.put(46, 2900000);
        table.put(47, 3100000);
        table.put(48, 3400000);
        table.put(49, 3700000);
        table.put(50, 4000000);
        table.put(51, 4300000);
        table.put(52, 4600000);
        table.put(53, 4900000);
        table.put(54, 5200000);
        table.put(55, 5500000);
        table.put(56, 5800000);
        table.put(57, 6100000);
        table.put(58, 6400000);
        table.put(59, 6700000);
        table.put(60, 7000000);
        return table;
    }

    public double getLevel() {
        return Math.round(level);
    }

    public double getExcess() {
        return excess;
    }

    public boolean isMaxed() {
        return level == levelCap;
    }

    public int getMaxXp() {
        return levelCap == 50 ? 55172425 : 111672425;
    }
}
