package me.asarix.com.prices;

public class BazaarPrices {
    double instaBuy;
    double instaSell;
    double orderBuy;
    double orderSell;
    boolean safe = true;

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

    public void disableSafe() {
        this.safe = false;
    }

    public double getInstaBuy() {
        return instaBuy;
    }

    public double getInstaSell() {
        return instaSell;
    }
}
