package me.asarix.com.weight;

public enum Skill {
    MINING(1.18207448, 259634, 60),
    FORAGING(1.232826, 259634, 50),
    ENCHANTING(0.96976583, 882758, 60),
    FARMING(1.217848139, 220689, 60),
    COMBAT(1.15797687265, 275862, 60),
    FISHING(1.406418, 88274, 50),
    ALCHEMY(1.0, 1103448, 50),
    TAMING(1.14744, 441379, 50);

    final double exp;
    final int div;
    final int cap;

    Skill(double exp, int div, int cap) {
        this.exp = exp;
        this.div = div;
        this.cap = cap;
    }

    public CalculatedSkill calculate(long xp) {
        return new CalculatedSkill(xp, exp, div, cap);
    }
}
