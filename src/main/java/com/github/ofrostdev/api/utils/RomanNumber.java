package com.github.ofrostdev.api.utils;

public class RomanNumber {

    private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    private static final int[] NUMBERS = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

    public static String numural(int number) {
        for (int i = 0; i < NUMBERS.length; i++) {
            if (number >= NUMBERS[i]) {
                return SYMBOLS[i] + numural(number - NUMBERS[i]);
            }
        }
        return "";
    }

    public static int unnumural(String number) {
        for (int i = 0; i < SYMBOLS.length; i++) {
            if (number.startsWith(SYMBOLS[i])) {
                return NUMBERS[i] + unnumural(number.replaceFirst(SYMBOLS[i], ""));
            }
        }
        return 0;
    }
}