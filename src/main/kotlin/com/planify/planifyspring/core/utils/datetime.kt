package com.planify.planifyspring.core.utils

import java.util.*

fun Long.toDate(): Date = Date(this)
fun Date.toTimestamp(): Long = this.time
