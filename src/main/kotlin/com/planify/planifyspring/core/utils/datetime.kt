package com.planify.planifyspring.core.utils

import java.util.*

fun Long.toDate(): Date = Date(this)

fun Date.toTimestamp(): Long = this.time

fun <T> validateRange(start: T?, end: T?): Boolean =(start != null && end == null) || (start == null && end != null)
