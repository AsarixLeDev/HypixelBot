package me.asarix.com.weight;

public enum DungeonType {
    CATACOMBS(0.0002149604615),
    HEALER(0.0000045254834),
    MAGE(0.0000045254834),
    BERSERK(0.0000045254834),
    ARCHER(0.0000045254834),
    TANK(0.0000045254834);

    final double modifier;

    DungeonType(double modifier) {
        this.modifier = modifier;
    }

    public Weight calculate(double xp) {
        return new CalculatedDungeon(xp, modifier).totalWeight;
    }
}
