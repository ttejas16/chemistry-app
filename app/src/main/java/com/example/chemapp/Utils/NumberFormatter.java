package com.example.chemapp.Utils;

import java.text.DecimalFormat;

public class NumberFormatter {
    private static final String[] SUPERSCRIPT_DIGITS = {
            "⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"
    };

    /**
     * Formats a double value according to these rules:
     * 1. If in scientific notation OR < 0.0001 -> convert to readable scientific notation
     * 2. Else -> reduce to 3 significant digits
     */
    public static String formatNumber(double value) {
        if (value == 0) return "0";

        // Check if number is in scientific notation range or very small
        if (Math.abs(value) < 0.0001 || Math.abs(value) >= 1e6 || isInScientificNotation(value)) {
            return formatScientific(value);
        } else {
            return formatThreeSignificantDigits(value);
        }
    }

    /**
     * Converts number to scientific notation with Unicode superscripts
     */
    private static String formatScientific(double value) {
        DecimalFormat df = new DecimalFormat("0.###E0");
        String scientific = df.format(value);

        String[] parts = scientific.split("E");
        String mantissa = parts[0];
        int exponent = Integer.parseInt(parts[1]);

        String superscriptExp = convertToSuperscript(exponent);
        return mantissa + " × 10" + superscriptExp;
    }

    /**
     * Reduces number to 3 significant digits
     */
    private static String formatThreeSignificantDigits(double value) {
        DecimalFormat df = new DecimalFormat("0.##");
        df.setMaximumFractionDigits(3);
        return df.format(value);
    }

    /**
     * Checks if a number would naturally be represented in scientific notation
     */
    private static boolean isInScientificNotation(double value) {
        String str = Double.toString(value);
        return str.contains("E") || str.contains("e");
    }

    /**
     * Converts integer to Unicode superscript
     */
    private static String convertToSuperscript(int number) {
        StringBuilder result = new StringBuilder();
        String numStr = String.valueOf(Math.abs(number));

        if (number < 0) {
            result.append("⁻");
        }

        for (char digit : numStr.toCharArray()) {
            int index = Character.getNumericValue(digit);
            result.append(SUPERSCRIPT_DIGITS[index]);
        }

        return result.toString();
    }
}
