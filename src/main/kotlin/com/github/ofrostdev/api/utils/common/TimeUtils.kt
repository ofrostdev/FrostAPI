package com.github.ofrostdev.api.utils.common

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object TimeUtils {
    private val DEFAULT_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    private val DATE_ONLY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val TIME_ONLY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    private val UNIT_MULTIPLIERS: MutableMap<String, Long> = HashMap()

    init {
        UNIT_MULTIPLIERS["s"] = TimeUnit.SECONDS.toMillis(1)
        UNIT_MULTIPLIERS["m"] =
            TimeUnit.MINUTES.toMillis(1)
        UNIT_MULTIPLIERS["h"] =
            TimeUnit.HOURS.toMillis(1)
        UNIT_MULTIPLIERS["d"] = TimeUnit.DAYS.toMillis(1)
    }

    fun parseDuration(input: String?): Long {
        if (input.isNullOrEmpty()) return 0
        var totalMillis: Long = 0
        val pattern = Pattern.compile("(\\d+)([smhd])")
        val matcher = pattern.matcher(input.lowercase(Locale.getDefault()))

        while (matcher.find()) {
            val value = matcher.group(1).toLong()
            val unit = matcher.group(2)
            totalMillis += value * UNIT_MULTIPLIERS.getOrDefault(unit, 0L)
        }

        return totalMillis
    }

    @JvmOverloads
    fun now(formatter: DateTimeFormatter? = DEFAULT_FORMAT): String {
        return LocalDateTime.now().format(formatter)
    }

    fun format(dateTime: LocalDateTime): String {
        return dateTime.format(DEFAULT_FORMAT)
    }

    fun format(dateTime: LocalDateTime, formatter: DateTimeFormatter?): String {
        return dateTime.format(formatter)
    }

    fun difference(from: LocalDateTime?, to: LocalDateTime?, unit: ChronoUnit): Long {
        return unit.between(from, to)
    }

    fun formatDuration(millis: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        val sb = StringBuilder()
        if (days > 0) sb.append(days).append("d ")
        if (hours > 0) sb.append(hours).append("h ")
        if (minutes > 0) sb.append(minutes).append("m ")
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("s")
        return sb.toString().trim { it <= ' ' }
    }

    @JvmOverloads
    fun parse(input: String?, formatter: DateTimeFormatter? = DEFAULT_FORMAT): LocalDateTime? {
        return formatter?.let { LocalDateTime.parse(input, it) }
    }

    fun addNow(amountToAdd: Long, unit: ChronoUnit?): LocalDateTime {
        return LocalDateTime.now().plus(amountToAdd, unit)
    }

    fun subtractNow(amountToSubtract: Long, unit: ChronoUnit?): LocalDateTime {
        return LocalDateTime.now().minus(amountToSubtract, unit)
    }

    fun isPast(dateTime: LocalDateTime): Boolean {
        return dateTime.isBefore(LocalDateTime.now())
    }

    fun isFuture(dateTime: LocalDateTime): Boolean {
        return dateTime.isAfter(LocalDateTime.now())
    }

    fun timeUntil(futureDateTime: LocalDateTime?): String {
        val duration = Duration.between(LocalDateTime.now(), futureDateTime)
        return formatDuration(duration.toMillis())
    }

    fun toMinuteSeconds(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    fun timeAgo(pastDateTime: LocalDateTime?): String {
        val now = LocalDateTime.now()
        val years = ChronoUnit.YEARS.between(pastDateTime, now)
        val months = ChronoUnit.MONTHS.between(pastDateTime, now)
        val days = ChronoUnit.DAYS.between(pastDateTime, now)
        val hours = ChronoUnit.HOURS.between(pastDateTime, now)
        val minutes = ChronoUnit.MINUTES.between(pastDateTime, now)
        val seconds = ChronoUnit.SECONDS.between(pastDateTime, now)

        if (years > 0) return "$years ano(s) atrás"
        if (months > 0) return "$months mês(es) atrás"
        if (days > 0) return "$days dia(s) atrás"
        if (hours > 0) return "$hours hora(s) atrás"
        if (minutes > 0) return "$minutes minuto(s) atrás"
        return "$seconds segundo(s) atrás"
    }

    fun formatter(pattern: String?): DateTimeFormatter? {
        return pattern?.let { DateTimeFormatter.ofPattern(it, Locale.getDefault()) }
    }
}