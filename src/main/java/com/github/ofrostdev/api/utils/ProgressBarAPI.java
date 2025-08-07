package com.github.ofrostdev.api.utils;

public class ProgressBarAPI {

    private static String repeat(String symbol, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(symbol);
        }
        return sb.toString();
    }

    public static String createProgressBar(double current, double max, int totalBars, String symbol, String completedColor, String notCompletedColor) {
        if (max == 0) return completedColor + repeat(symbol, totalBars);

        double percent = current / max;
        int progressBars = (int) Math.round(totalBars * percent);

        StringBuilder bar = new StringBuilder();
        bar.append(completedColor);
        bar.append(repeat(symbol, Math.max(0, progressBars)));

        bar.append(notCompletedColor);
        bar.append(repeat(symbol, Math.max(0, totalBars - progressBars)));

        return bar.toString();
    }

    public static String getPercent(double current, double max) {
        if (max == 0) return "100%";
        double percent = (current / max) * 100;

        if (percent == (int) percent) {
            return String.format("%d%%", (int) percent);
        } else {
            return String.format("%.1f%%", percent);
        }
    }
}
