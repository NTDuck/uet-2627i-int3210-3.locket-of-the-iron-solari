package com.solari.app.data.mappers

import java.time.Instant

fun String?.toEpochMillisOrNow(): Long {
    if (this == null) return System.currentTimeMillis()
    return runCatching { Instant.parse(this).toEpochMilli() }
        .getOrElse { System.currentTimeMillis() }
}
