package me.asarix.com.weight;

public record Weight(double base, double overflow) {
    double total() {
        return base + overflow;
    }
}
