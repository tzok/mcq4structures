package pl.poznan.put.comparison;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.apache.commons.math3.fraction.ProperFractionFormat;

import pl.poznan.put.helper.Constants;

public class FractionAngleFormat extends NumberFormat {
    private static final long serialVersionUID = 1L;

    private final ProperFractionFormat fractionFormat;

    public FractionAngleFormat(ProperFractionFormat fractionFormat) {
        this.fractionFormat = fractionFormat;
    }

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo,
            FieldPosition pos) {
        if (number == 0) {
            return toAppendTo.append("0");
        } else if (number == Math.PI) {
            toAppendTo.append(Constants.UNICODE_PI);
            toAppendTo.append(" = 180");
            toAppendTo.append(Constants.UNICODE_DEGREE);
            return toAppendTo;
        }

        fractionFormat.format(number / Math.PI, toAppendTo, pos);
        toAppendTo.append(" * ");
        toAppendTo.append(Constants.UNICODE_PI);
        toAppendTo.append(" = ");
        toAppendTo.append(Math.round(Math.toDegrees(number)));
        toAppendTo.append(Constants.UNICODE_DEGREE);
        return toAppendTo;
    }

    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo,
            FieldPosition pos) {
        return fractionFormat.format(number, toAppendTo, pos);
    }

    @Override
    public Number parse(String source, ParsePosition parsePosition) {
        return fractionFormat.parse(source, parsePosition);
    }
}