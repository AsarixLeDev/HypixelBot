package me.asarix.com.prices;

import me.asarix.com.ItemStack;

public class LowestBinItem extends ItemStack {
    boolean safe = true;
    private int instances = 1;
    private double price;

    public LowestBinItem(String itemName, double price) {
        super(itemName);
        this.price = price;
    }

    public int getInstanceNumb() {
        return instances;
    }

    public double getPrice(int amount) {
        return price * amount;
    }

    public double getUnitPrice() {
        return price;
    }

    public void request(double value) {
        this.instances++;
        if (this.price > value || this.price < 0)
            this.price = value;
    }

    public void disableSafe() {
        this.safe = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemStack item))
            return false;
        if (!similar(item))
            return false;
        return amount == item.getAmount();
    }
}
