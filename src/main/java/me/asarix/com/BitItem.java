package me.asarix.com;

public class BitItem extends ItemStack {
    String itemName;
    int bitNumb;
    double lowestBin;

    public BitItem(String itemName, int bitNumb, double lowestBin) {
        super(itemName);
        this.itemName = normalName;
        this.bitNumb = bitNumb;
        this.lowestBin = lowestBin;
    }

    public float pricePerBit() {
        return Math.round((float) lowestBin / bitNumb);
    }
}
