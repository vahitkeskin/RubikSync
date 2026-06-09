package com.vahitkeskin.rubiksync.utils

/**
 * Extension to return a default value if the receiver is null.
 */
fun <T> T?.orDefault(default: T): T = this ?: default

/**
 * Boolean? extension to return false if null.
 */
fun Boolean?.orFalse(): Boolean = this ?: false

/**
 * Boolean? extension to return true if null.
 */
fun Boolean?.orTrue(): Boolean = this ?: true

/**
 * Int? extension to return 0 if null.
 */
fun Int?.orZero(): Int = this ?: 0

/**
 * Long? extension to return 0L if null.
 */
fun Long?.orZero(): Long = this ?: 0L

/**
 * Double? extension to return 0.0 if null.
 */
fun Double?.orZero(): Double = this ?: 0.0

/**
 * Float? extension to return 0.0f if null.
 */
fun Float?.orZero(): Float = this ?: 0.0f
