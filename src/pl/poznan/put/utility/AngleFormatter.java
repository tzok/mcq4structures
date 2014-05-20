package pl.poznan.put.utility;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class AngleFormatter {
    private static final AngleFormatter INSTANCE = new AngleFormatter();

    public static String format(double value) {
        return AngleFormatter.INSTANCE.numberFormat.format(value);
    }

    private final NumberFormat numberFormat;

    private AngleFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        numberFormat = new DecimalFormat("###.###", symbols);
    }
}
