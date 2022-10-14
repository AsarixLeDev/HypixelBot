package me.asarix.com;

public class LowestBinItem extends ItemStack {
    private int instances = 1;
    private double price;

    public LowestBinItem(String itemName, double price) {
        this(itemName, price, 1);
    }

    public LowestBinItem(String itemName, double price, int amount) {
        super(itemName, amount);
        this.price = price;
        this.amount = amount;
    }

    public LowestBinItem(ItemStack item) {
        this(item.normalName, -1, item.amount);
    }

    public int getInstanceNumb() {
        return instances;
    }

    public double getPrice() {
        return price * amount;
    }

    public void request(double value) {
        this.instances++;
        if (this.price > value || this.price < 0)
            this.price = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemStack item))
            return false;
        if (!item.locName.equalsIgnoreCase(locName))
            return false;
        return amount == item.amount;
    }
}
