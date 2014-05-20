package pl.poznan.put.utility;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class NumberFormatter {
    private static final NumberFormatter INSTANCE = new NumberFormatter();

    public static String format(double value) {
        return NumberFormatter.INSTANCE.numberFormat.format(value);
    }

    private final NumberFormat numberFormat;

    private NumberFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        numberFormat = new DecimalFormat("###.###", symbols);
    }
}
