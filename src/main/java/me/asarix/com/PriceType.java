package me.asarix.com;

public enum PriceType {
    AUCTION,
    BAZAAR;
    double value = 0;

    PriceType of(double value) {
        this.value = value;
        return this;
    }
}
