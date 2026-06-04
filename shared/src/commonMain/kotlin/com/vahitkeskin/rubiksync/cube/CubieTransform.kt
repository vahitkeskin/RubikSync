package com.vahitkeskin.rubiksync.cube

import androidx.compose.runtime.withFrameMillis

internal data class CubieTransform(
    val gridPos: Vector3,
    val rightBasis: Vector3,
    val upBasis: Vector3,
    val forwardBasis: Vector3,
)

internal fun Cubie.captureTransform(): CubieTransform =
    CubieTransform(gridPos, rightBasis, upBasis, forwardBasis)

internal fun Cubie.applyTransform(transform: CubieTransform) {
    gridPos = transform.gridPos
    rightBasis = transform.rightBasis
    upBasis = transform.upBasis
    forwardBasis = transform.forwardBasis
}

internal fun CubieTransform.lerpToward(target: CubieTransform, progress: Float): CubieTransform {
    val t = progress.coerceIn(0f, 1f)
    return CubieTransform(
        gridPos = gridPos + (target.gridPos - gridPos) * t,
        rightBasis = (rightBasis + (target.rightBasis - rightBasis) * t).normalized(),
        upBasis = (upBasis + (target.upBasis - upBasis) * t).normalized(),
        forwardBasis = (forwardBasis + (target.forwardBasis - forwardBasis) * t).normalized(),
    )
}

internal suspend fun List<Cubie>.animateTransforms(
    targets: Map<Int, CubieTransform>,
    durationMs: Float,
    onProgress: (Float) -> Unit = {},
) {
    val starts = associate { cubie -> cubie.id to cubie.captureTransform() }
    val startTime = withFrameMillis { it }
    var elapsed = 0f

    while (elapsed < durationMs) {
        val progress = easeInOutCubic((elapsed / durationMs).coerceIn(0f, 1f))

        forEach { cubie ->
            val target = targets[cubie.id] ?: return@forEach
            val start = starts.getValue(cubie.id)
            cubie.applyTransform(start.lerpToward(target, progress))
        }

        onProgress(progress)
        elapsed = (withFrameMillis { it } - startTime).toFloat()
    }

    forEach { cubie ->
        targets[cubie.id]?.let { cubie.applyTransform(it) }
    }
    onProgress(1f)
}
