package me.asarix.com;

import me.asarix.com.prices.BazaarFetcher;
import me.asarix.com.prices.LowestFetcher;

import java.util.Objects;

public class ItemStack {
    protected String normalName;
    protected String locName;
    protected int amount;
    protected boolean fromBazaar;
    protected boolean fromPnj;

    public ItemStack(String itemName, int amount) throws RuntimeException {
        String locName = Main.normToLoc(itemName);
        if (locName == null) {
            String normalName = Main.locToNorm(itemName);
            if (normalName == null)
                throw new RuntimeException("Item " + itemName + " does not exist !");
            this.normalName = normalName;
            this.locName = Main.normToLoc(normalName);
        } else {
            this.locName = locName;
            this.normalName = Main.locToNorm(locName);
        }
        this.fromBazaar = BazaarFetcher.isFromBazaar(locName);

        this.fromPnj = Main.pnjItems.containsKey(locName);
        this.amount = amount;
    }

    public ItemStack(String itemName) {
        this(itemName, 1);
    }

    public String getNormalName() {
        return normalName;
    }

    public String getLocName() {
        return locName;
    }

    public boolean isFromBazaar() {
        return fromBazaar;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStack itemStack = (ItemStack) o;
        return locName.equals(itemStack.locName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locName);
    }

    public boolean hasName(String name) {
        return normalName.equalsIgnoreCase(name)
                || locName.equalsIgnoreCase(name);
    }

    public boolean similar(ItemStack item) {
        return hasName(item.locName);
    }

    public ItemStack copy() {
        return new ItemStack(this.locName, this.amount);
    }

    public double getUnitPrice() {
        if (fromPnj)
            return Main.pnjItems.get(locName);
        if (fromBazaar)
            return BazaarFetcher.getPrice(locName).getInstaBuy();
        return LowestFetcher.getPrice(locName);
    }

    public double getPrice() {
        if (fromPnj)
            return Main.pnjItems.get(locName) * amount;
        if (fromBazaar)
            return BazaarFetcher.getPrice(locName).getInstaBuy() * amount;
        return LowestFetcher.getPrice(locName) * amount;
    }

    public double getPrice(int amount) {
        if (fromPnj)
            return Main.pnjItems.get(locName) * amount;
        if (fromBazaar)
            return BazaarFetcher.getPrice(locName).getInstaBuy() * amount;
        return LowestFetcher.getPrice(locName) * amount;
    }

    public double getUnsafeUnitPrice() {
        if (fromPnj || fromBazaar)
            return getUnitPrice();
        return LowestFetcher.getUnsafePrice(locName);
    }

    public double getUnsafePrice() {
        if (fromPnj || fromBazaar)
            return getUnitPrice();
        return LowestFetcher.getUnsafePrice(locName) * amount;
    }

    public double getUnsafePrice(int amount) {
        if (fromPnj || fromBazaar)
            return getUnitPrice();
        return LowestFetcher.getUnsafePrice(locName) * amount;
    }
}
