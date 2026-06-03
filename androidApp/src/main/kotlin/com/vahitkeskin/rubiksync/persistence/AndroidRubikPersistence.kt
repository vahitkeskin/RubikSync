package com.vahitkeskin.rubiksync.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.utils.CameraSettings
import com.vahitkeskin.rubiksync.utils.CubiePersistable
import com.vahitkeskin.rubiksync.utils.LoadedCubeState
import com.vahitkeskin.rubiksync.utils.RubikPersistence
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rubik_settings")

class AndroidRubikPersistence(private val context: Context) : RubikPersistence {

    private val database = RubikDatabase.getDatabase(context)
    private val cubeDao = database.cubeStateDao()

    private val KEY_YAW = floatPreferencesKey("yaw")
    private val KEY_PITCH = floatPreferencesKey("pitch")
    private val KEY_CAMERA_DISTANCE = floatPreferencesKey("camera_distance")
    private val KEY_PAN_X = floatPreferencesKey("pan_x")
    private val KEY_PAN_Y = floatPreferencesKey("pan_y")
    private val KEY_ROTATION_SPEED = floatPreferencesKey("rotation_speed_ms")

    override suspend fun saveCubeState(
        cubies: List<CubiePersistable>,
        moveHistory: List<MoveType>,
        manualMoves: List<MoveType>,
        editorFaces: Map<FaceName, Array<Array<CubeColor>>>
    ) {
        val cubiesStr = serializeCubies(cubies)
        val moveHistoryStr = serializeMoves(moveHistory)
        val manualMovesStr = serializeMoves(manualMoves)
        val editorFacesStr = serializeEditorFaces(editorFaces)

        val entity = CubeStateEntity(
            id = 1,
            cubiesData = cubiesStr,
            moveHistoryData = moveHistoryStr,
            manualMovesData = manualMovesStr,
            editorFacesData = editorFacesStr
        )
        cubeDao.insertCubeState(entity)
    }

    override suspend fun loadCubeState(): LoadedCubeState? {
        val entity = cubeDao.getCubeState() ?: return null
        return try {
            LoadedCubeState(
                cubies = deserializeCubies(entity.cubiesData),
                moveHistory = deserializeMoves(entity.moveHistoryData),
                manualMoves = deserializeMoves(entity.manualMovesData),
                editorFaces = deserializeEditorFaces(entity.editorFacesData)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveCameraSettings(
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
        rotationSpeedMs: Float
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_YAW] = yaw
            preferences[KEY_PITCH] = pitch
            preferences[KEY_CAMERA_DISTANCE] = cameraDistance
            preferences[KEY_PAN_X] = panX
            preferences[KEY_PAN_Y] = panY
            preferences[KEY_ROTATION_SPEED] = rotationSpeedMs
        }
    }

    override suspend fun loadCameraSettings(): CameraSettings? {
        return try {
            val prefs = context.dataStore.data.first()
            val yaw = prefs[KEY_YAW] ?: return null
            val pitch = prefs[KEY_PITCH] ?: 0.40f
            val distance = prefs[KEY_CAMERA_DISTANCE] ?: 10.0f
            val px = prefs[KEY_PAN_X] ?: 0f
            val py = prefs[KEY_PAN_Y] ?: 0f
            val speed = prefs[KEY_ROTATION_SPEED] ?: 250f
            CameraSettings(yaw, pitch, distance, px, py, speed)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- Custom Serializers / Deserializers ---

    private fun serializeCubies(cubies: List<CubiePersistable>): String {
        return cubies.joinToString(";") { c ->
            "${c.id},${c.gridX},${c.gridY},${c.gridZ},${c.rightX},${c.rightY},${c.rightZ},${c.upX},${c.upY},${c.upZ},${c.forwardX},${c.forwardY},${c.forwardZ}"
        }
    }

    private fun deserializeCubies(str: String): List<CubiePersistable> {
        if (str.isEmpty()) return emptyList()
        return str.split(";").map { line ->
            val parts = line.split(",")
            CubiePersistable(
                id = parts[0].toInt(),
                gridX = parts[1].toInt(),
                gridY = parts[2].toInt(),
                gridZ = parts[3].toInt(),
                rightX = parts[4].toInt(),
                rightY = parts[5].toInt(),
                rightZ = parts[6].toInt(),
                upX = parts[7].toInt(),
                upY = parts[8].toInt(),
                upZ = parts[9].toInt(),
                forwardX = parts[10].toInt(),
                forwardY = parts[11].toInt(),
                forwardZ = parts[12].toInt()
            )
        }
    }

    private fun serializeMoves(moves: List<MoveType>): String {
        return moves.joinToString(",") { it.name }
    }

    private fun deserializeMoves(str: String): List<MoveType> {
        if (str.isBlank()) return emptyList()
        return str.split(",").map { MoveType.valueOf(it) }
    }

    private fun serializeEditorFaces(faces: Map<FaceName, Array<Array<CubeColor>>>): String {
        return faces.entries.joinToString(";") { (faceName, grid) ->
            val colorsStr = grid.flatMap { row -> row.toList() }.joinToString(",") { it.name }
            "${faceName.name}:$colorsStr"
        }
    }

    private fun deserializeEditorFaces(str: String): Map<FaceName, Array<Array<CubeColor>>> {
        if (str.isEmpty()) return emptyMap()
        val map = mutableMapOf<FaceName, Array<Array<CubeColor>>>()
        str.split(";").forEach { faceStr ->
            val parts = faceStr.split(":")
            val faceName = FaceName.valueOf(parts[0])
            val colors = parts[1].split(",").map { CubeColor.valueOf(it) }
            val grid = Array(3) { r ->
                Array(3) { c ->
                    colors[r * 3 + c]
                }
            }
            map[faceName] = grid
        }
        return map
    }
}
