package me.asarix.com;

public class MinNumb {
    private int value;

    public MinNumb(int value) {
        this.value = value;
    }

    void request(int value) {
        if (value < this.value)
            this.value = value;
    }

    int getValue() {
        return value;
    }
}
