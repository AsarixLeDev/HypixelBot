package me.asarix.com.weight;

public class SlayerLevel {

    double level;
    double exp;
    double excess = 0;

    public SlayerLevel(String slayerName, int exp) {
        this.exp = exp;
        int[] revExp = {5, 15, 200, 1000, 5000, 20000, 100000, 400000, 1000000};
        int[] taraExp = {5, 25, 200, 1000, 5000, 20000, 100000, 400000, 1000000};
        int[] svenExp = {10, 35, 250, 1500, 5000, 20000, 100000, 400000, 1000000};
        int[] endExp = {10, 30, 250, 1500, 5000, 20000, 100000, 400000, 1000000};
        int[] exps = switch (slayerName) {
            case "zombie" -> revExp;
            case "spider" -> taraExp;
            case "wolf" -> svenExp;
            case "enderman" -> endExp;
            default -> null;
        };
        if (exps == null) {
            this.level = 0;
            this.exp = 0;
            this.excess = 0;
            return;
        }
        System.out.println("BZUIHUBHKZG" + exps.length);
        int round_lvl;
        for (round_lvl = 0; round_lvl < exps.length; round_lvl++) {
            int xp = exps[round_lvl];
            if (exp < xp) {
                break;
            }
        }
        System.out.println("LEVEL " + round_lvl);
        if (round_lvl == 9) {
            excess = exp - exps[8];
            level = 9;
        }
        else {
            double prevLvlXp = exps[round_lvl-1];
            double ex = exp - prevLvlXp;
            double lvl_xp = exps[round_lvl] - prevLvlXp;
            double digits = lvl_xp / ex;
            level = round_lvl + digits;
        }
    }

    public double getExcess() {
        return excess;
    }
}
