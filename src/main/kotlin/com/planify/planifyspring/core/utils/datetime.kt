package com.planify.planifyspring.core.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

fun <T> validateRange(start: T?, end: T?): Boolean = (start != null && end == null) || (start == null && end != null)

fun Instant.atStartOfDay(): Instant =
    this.atZone(ZoneId.of("UTC"))
        .toLocalDate()
        .atStartOfDay(ZoneId.of("UTC"))
        .toInstant()


fun LocalDate.atStartOfDayInstant(): Instant =
    this.atStartOfDay(ZoneId.of("UTC"))
        .toInstant()


fun LocalDate.atEndOfDayInstant(): Instant =
    this.atTime(LocalTime.MAX)
        .atZone(ZoneOffset.UTC)
        .toInstant()


fun LocalDateTime.asUTCInstant(): Instant =
    this.atZone(ZoneOffset.UTC).toInstant()

fun Instant.atStartOfAnHour(): Instant {
    return this.atZone(ZoneOffset.UTC).withMinute(0).withSecond(0).withNano(0).toInstant()
}

fun Instant.elapsed(): Duration =
    java.time.Duration.between(this, Instant.now()).toKotlinDuration()

infix fun Instant.hasElapsed(duration: Duration): Boolean =
    elapsed() >= duration

infix fun Duration.since(instant: Instant): Boolean =
    instant.elapsed() >= this

infix fun Instant.within(duration: Duration): Boolean =
    elapsed() < duration

val Duration.ago: Instant get() = Instant.now() - this

val Duration.fromNow: Instant get() = Instant.now() + this

operator fun Instant.plus(duration: Duration): Instant = plus(duration.toJavaDuration())
operator fun Instant.minus(duration: Duration): Instant = minus(duration.toJavaDuration())

// TODO: Faster version?
//fun Instant.elapsedMillis(): Long = System.currentTimeMillis() - toEpochMilli()
//infix fun Duration.since(instant: Instant): Boolean =
//    instant.elapsedMillis() >= inWholeMilliseconds
