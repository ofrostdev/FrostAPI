package com.github.ofrostdev.api.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberAPI {

    private static final String[] SUFFIXES = {
            "", "K", "M", "B", "T", "Q", "QQ", "S", "SS", "OC", "N", "D",
            "UN", "DD", "TR", "QT", "QN", "SD", "SPD", "OD", "ND",
            "VG", "UVG", "DVG", "TVG", "QVG", "QQVG", "SVG", "SSVG", "OVG", "NVG",
            "TG", "UTG", "DTG", "TTG", "QTG", "QQTG", "STG", "SSTG", "OTG", "NTG",
            "QG", "UQG", "DQG", "TQG", "QQG", "QQQG", "SQG", "SSQG", "OQG", "NQG",
            "QuG", "UQuG", "DQuG", "TQuG", "QQuG", "QQQuG", "SuG", "SSuG", "OuG", "NuG",
            "SG", "USG", "DSG", "TSG", "QSG", "QQSG", "SSG", "SSSG", "OSG", "NSG",
            "SeG", "USeG", "DSeG", "TSeG", "QSeG", "QQSeG", "SSeG", "SSSeG", "OSeG", "NSeG",
            "OG", "UOG", "DOG", "TOG", "QOG", "QQOG", "SOG", "SSOG", "OOG", "NOG",
            "NG", "UNG", "DNG", "TNG", "QNG", "QQNG", "SNG", "SSNG", "ONG", "NNG",
            "CT"
    };

    public static String format(BigDecimal decimal) {
        decimal = decimal.setScale(0, RoundingMode.DOWN);
        int zeros = decimal.precision() - 1;

        if (zeros < 3) return decimal.setScale(0).toString();

        int k = zeros / 3;
        if (k >= SUFFIXES.length) return "999.9CT";

        return decimal.movePointLeft(k * 3).setScale(1, RoundingMode.DOWN) + SUFFIXES[k];
    }

    public static String format(Number number) {
        return format(new BigDecimal(number.toString()));
    }

    public static String nFormat(double amount) {
        amount = mRound(amount);
        return formatValue(amount);
    }

    public static double mRound(double amount) {
        DecimalFormat df = new DecimalFormat("###.##");
        String formatted = df.format(amount).replace(",", ".");
        return Double.parseDouble(formatted);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Casas decimais não podem ser negativas.");
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    public static BigDecimal parse(String input) {
        int zeros = 3;

        for (String suffix : SUFFIXES) {
            if (!suffix.isEmpty() && input.endsWith(suffix)) {
                String numberPart = input.substring(0, input.length() - suffix.length());

                try {
                    return new BigDecimal(numberPart).movePointRight(zeros);
                } catch (NumberFormatException ignored) {}

                zeros += 3;
            }
        }

        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static boolean isInt(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String formatValue(double value) {
        boolean isWhole = value == Math.round(value);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setDecimalSeparator('.');
        String pattern = isWhole ? "###,###" : "###,##0.00";
        return new DecimalFormat(pattern, symbols).format(value);
    }
}
