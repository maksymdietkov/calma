package com.maksymdietkov.calma.ui.format

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("EE d.MM", Locale.forLanguageTag("ru"))

/** "08:42" */
fun formatTime(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
    timeFormatter.format(instant.atZone(zone))

/** "сегодня в 08:42", "вчера в 23:10", "пт 30.10 в 08:42" */
fun formatDayTime(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
    val date = instant.atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    val day = when (date) {
        today -> "сегодня"
        today.minusDays(1) -> "вчера"
        else -> dateFormatter.format(date)
    }
    return "$day в ${formatTime(instant, zone)}"
}