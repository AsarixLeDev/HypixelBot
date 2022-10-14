package me.asarix.com;

public class BazaarPrices {
    double instaBuy;
    double instaSell;
    double orderBuy;
    double orderSell;

    public BazaarPrices(double instaBuy, double instaSell, double orderBuy, double orderSell) {
        this.instaBuy = instaBuy;
        this.instaSell = instaSell;
        this.orderBuy = orderBuy;
        this.orderSell = orderSell;
    }

    @Override
    public String toString() {
        return "Insta-buy : " + instaBuy +
                " Insta-sell : " + instaSell +
                " Order buy : " + orderBuy +
                " Order sell : " + orderSell;
    }

    public double getInstaSell() {
        return instaSell;
    }
}
