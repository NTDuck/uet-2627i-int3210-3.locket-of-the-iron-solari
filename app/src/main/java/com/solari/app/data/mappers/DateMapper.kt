package com.solari.app.data.mappers

import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

fun String?.toEpochMillisOrNow(): Long {
    if (this == null) return System.currentTimeMillis()
    return runCatching { parseBackendTimestamp(this).toEpochMilli() }
        .getOrElse { System.currentTimeMillis() }
}

private fun parseBackendTimestamp(value: String): Instant {
    return runCatching { Instant.parse(value) }
        .getOrElse {
            runCatching {
                OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
            }
                .getOrElse {
                    PostgresTimestampFormatters.firstNotNullOf { formatter ->
                        runCatching { OffsetDateTime.parse(value, formatter).toInstant() }
                            .getOrNull()
                    }
                }
        }
}

private val PostgresTimestampFormatters: List<DateTimeFormatter> = listOf(
    postgresTimestampFormatter("X"),
    postgresTimestampFormatter("XXX")
)

private fun postgresTimestampFormatter(offsetPattern: String): DateTimeFormatter {
    return DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm:ss")
        .optionalStart()
        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
        .optionalEnd()
        .appendPattern(offsetPattern)
        .toFormatter()
}
