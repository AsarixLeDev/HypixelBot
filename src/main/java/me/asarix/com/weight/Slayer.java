package me.asarix.com.weight;

public enum Slayer {
    WOLF(1962, 0.015),
    SPIDER(2118, 0.08),
    ZOMBIE(2208, 0.15),
    ENDERMAN(1430, 0.017);

    final int div;
    final double modifier;

    Slayer(int div, double modifier) {
        this.modifier = modifier;
        this.div = div;
    }

    public double calculate(long xp) {
        if (xp <= 1000000)
            return (float) xp / this.div;
        double base = 1000000.0 / div;
        long remaining = xp - 1000000;

        double overflow = 0;
        double modifier = this.modifier;

        while (remaining > 0) {
            double left = Math.min(remaining, 1000000);

            overflow += Math.pow(left / (div * (1.5 + modifier)), 0.942);
            modifier += this.modifier;
            remaining -= left;
        }
        return base + overflow;
    }
}
