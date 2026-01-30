package com.planify.planifyspring.core.utils

import java.time.Instant
import java.time.ZoneId

fun <T> validateRange(start: T?, end: T?): Boolean = (start != null && end == null) || (start == null && end != null)

fun Instant.atStartOfDay(): Instant =
    this.atZone(ZoneId.of("UTC"))
        .toLocalDate()
        .atStartOfDay(ZoneId.of("UTC"))
        .toInstant()
