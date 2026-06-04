package com.vahitkeskin.rubiksync.utils

import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.MoveType

class CubiePersistable(
    val id: Int,
    val gridX: Int,
    val gridY: Int,
    val gridZ: Int,
    val rightX: Int,
    val rightY: Int,
    val rightZ: Int,
    val upX: Int,
    val upY: Int,
    val upZ: Int,
    val forwardX: Int,
    val forwardY: Int,
    val forwardZ: Int
)

data class LoadedCubeState(
    val cubies: List<CubiePersistable>,
    val moveHistory: List<MoveType>,
    val manualMoves: List<MoveType>,
    val editorFaces: Map<FaceName, Array<Array<CubeColor>>>
)

data class CameraSettings(
    val yaw: Float,
    val pitch: Float,
    val cameraDistance: Float,
    val panX: Float,
    val panY: Float,
    val rotationSpeedMs: Float
)

interface RubikPersistence {
    suspend fun saveCubeState(
        cubies: List<CubiePersistable>,
        moveHistory: List<MoveType>,
        manualMoves: List<MoveType>,
        editorFaces: Map<FaceName, Array<Array<CubeColor>>>
    )
    suspend fun loadCubeState(): LoadedCubeState?
    
    suspend fun saveCameraSettings(
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
        rotationSpeedMs: Float
    )
    suspend fun loadCameraSettings(): CameraSettings?

    suspend fun saveThemeMode(mode: String)
    suspend fun loadThemeMode(): String?

    suspend fun saveLanguage(langCode: String)
    suspend fun loadLanguage(): String?

    suspend fun saveCubeEditable(enabled: Boolean)
    suspend fun loadCubeEditable(): Boolean?

    suspend fun saveSoundEnabled(enabled: Boolean)
    suspend fun loadSoundEnabled(): Boolean?
}

object RubikPersistenceRegistry {
    var persistence: RubikPersistence? = null
}
