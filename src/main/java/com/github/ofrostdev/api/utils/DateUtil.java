package com.github.ofrostdev.api.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class DateUtil {

    private DateUtil() {}

    private static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_ONLY_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String now() {
        return now(DEFAULT_FORMAT);
    }

    public static String now(DateTimeFormatter formatter) {
        return LocalDateTime.now().format(formatter);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_FORMAT);
    }

    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }

    public static long difference(LocalDateTime from, LocalDateTime to, ChronoUnit unit) {
        return unit.between(from, to);
    }

    public static String formatDuration(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static LocalDateTime parse(String input) {
        return parse(input, DEFAULT_FORMAT);
    }

    public static LocalDateTime parse(String input, DateTimeFormatter formatter) {
        return LocalDateTime.parse(input, formatter);
    }

    public static LocalDateTime addNow(long amountToAdd, ChronoUnit unit) {
        return LocalDateTime.now().plus(amountToAdd, unit);
    }

    public static LocalDateTime subtractNow(long amountToSubtract, ChronoUnit unit) {
        return LocalDateTime.now().minus(amountToSubtract, unit);
    }

    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime.isBefore(LocalDateTime.now());
    }

    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime.isAfter(LocalDateTime.now());
    }

    public static String timeUntil(LocalDateTime futureDateTime) {
        Duration duration = Duration.between(LocalDateTime.now(), futureDateTime);
        return formatDuration(duration.toMillis());
    }

    public static String toMinuteSeconds(long seconds) {
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public static String timeAgo(LocalDateTime pastDateTime) {
        LocalDateTime now = LocalDateTime.now();
        long years = ChronoUnit.YEARS.between(pastDateTime, now);
        long months = ChronoUnit.MONTHS.between(pastDateTime, now);
        long days = ChronoUnit.DAYS.between(pastDateTime, now);
        long hours = ChronoUnit.HOURS.between(pastDateTime, now);
        long minutes = ChronoUnit.MINUTES.between(pastDateTime, now);
        long seconds = ChronoUnit.SECONDS.between(pastDateTime, now);

        if (years > 0) return years + " ano(s) atrás";
        if (months > 0) return months + " mês(es) atrás";
        if (days > 0) return days + " dia(s) atrás";
        if (hours > 0) return hours + " hora(s) atrás";
        if (minutes > 0) return minutes + " minuto(s) atrás";
        return seconds + " segundo(s) atrás";
    }

    public static DateTimeFormatter formatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault());
    }
}
