package me.asarix.com;

import java.util.Objects;

public class ItemStack {
    String normalName;
    String locName;
    int amount;
    boolean fromBazaar;
    boolean fromPnj;

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
        this.fromBazaar = Main.bazaarNames.contains(locName);

        this.fromPnj = Main.pnjItems.containsKey(locName);
        if (fromPnj) {
            System.out.println("From pnj : " + normalName);
        }
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

    public ItemStack copy() {
        return new ItemStack(this.locName, this.amount);
    }
//TODO non
    public double getLowestBin() {
        return LowestFetcher.getPrice(this);
    }
}
