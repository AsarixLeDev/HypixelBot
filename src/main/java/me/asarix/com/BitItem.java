package me.asarix.com;

public class BitItem extends ItemStack {
    int bitNumb;

    public BitItem(String itemName, int bitNumb) {
        super(itemName);
        this.bitNumb = bitNumb;
    }

    public int getBitNumb() {
        return bitNumb;
    }

    //TODO et si c'est un item bazaar ou pnj ?
    public float pricePerBit() {
        return Math.round((float) getLowestBin() / bitNumb);
    }
}
