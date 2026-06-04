package com.vahitkeskin.rubiksync.cube

import kotlin.math.pow

/** Smooth ease-in-out curve for layer turns and layout transitions (t in 0..1). */
internal fun easeInOutCubic(t: Float): Float =
    if (t < 0.5f) {
        4f * t * t * t
    } else {
        1f - (-2f * t + 2f).pow(3) / 2f
    }
