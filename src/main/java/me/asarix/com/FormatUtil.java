package me.asarix.com;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtil {

    public static String format(double numb) {
        return f(Math.round(numb)) + "$";
    }

    public static String round(double numb) {
        return format((int) Math.round(numb));
    }

    public static String round(double numb, int dec) {
        double x = Math.pow(10.0, dec);
        double roundOff = Math.round(numb * x) / x;
        return f(roundOff);
    }

    private static String f(double numb) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

        symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(numb);
    }

    public static String firstCap(String word) {
        String fLetter = word.substring(0, 1).toUpperCase();
        return fLetter + word.substring(1);
    }
}
