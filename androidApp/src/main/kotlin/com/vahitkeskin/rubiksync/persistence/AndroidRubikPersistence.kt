package com.vahitkeskin.rubiksync.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
    private val solveSessionDao = database.solveSessionDao()

    private val KEY_YAW = floatPreferencesKey("yaw")
    private val KEY_PITCH = floatPreferencesKey("pitch")
    private val KEY_CAMERA_DISTANCE = floatPreferencesKey("camera_distance")
    private val KEY_PAN_X = floatPreferencesKey("pan_x")
    private val KEY_PAN_Y = floatPreferencesKey("pan_y")
    private val KEY_ROTATION_SPEED = floatPreferencesKey("rotation_speed_ms")
    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    private val KEY_LANGUAGE = stringPreferencesKey("app_language")
    private val KEY_CUBE_EDITABLE = booleanPreferencesKey("cube_editable")
    private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    private val KEY_SHOWCASE_COMPLETED = booleanPreferencesKey("showcase_completed")
    private val KEY_SHAKE_TO_SCRAMBLE = booleanPreferencesKey("shake_to_scramble")
    private val KEY_EDITOR_SHOWCASE_COMPLETED = booleanPreferencesKey("editor_showcase_completed")
    private val KEY_SCANNER_SHOWCASE_COMPLETED = booleanPreferencesKey("scanner_showcase_completed")
    private val KEY_SCRAMBLE_SOUND_TOOLTIP_SHOWN = booleanPreferencesKey("scramble_sound_tooltip_shown")


    override suspend fun saveCubeState(
        cubies: List<CubiePersistable>,
        moveHistory: List<MoveType>,
        manualMoves: List<MoveType>,
        editorFaces: Map<FaceName, Array<Array<CubeColor>>>
    ) {
        val cubiesStr = serializeCubies(cubies)
        val historyStr = serializeMoves(moveHistory)
        val manualStr = serializeMoves(manualMoves)
        val editorStr = serializeEditorFaces(editorFaces)
        
        val entity = CubeStateEntity(
            id = 1,
            cubiesData = cubiesStr,
            moveHistoryData = historyStr,
            manualMovesData = manualStr,
            editorFacesData = editorStr
        )
        cubeDao.insertCubeState(entity)
    }

    override suspend fun loadCubeState(): LoadedCubeState? {
        val entity = cubeDao.getCubeState() ?: return null
        val cubies = deserializeCubies(entity.cubiesData) ?: return null
        val history = deserializeMoves(entity.moveHistoryData)
        val manual = deserializeMoves(entity.manualMovesData)
        val editor = deserializeEditorFaces(entity.editorFacesData) ?: return null
        return LoadedCubeState(cubies, history, manual, editor)
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
            CameraSettings(
                yaw = prefs[KEY_YAW] ?: -0.55f,
                pitch = prefs[KEY_PITCH] ?: 0.40f,
                cameraDistance = prefs[KEY_CAMERA_DISTANCE] ?: 10f,
                panX = prefs[KEY_PAN_X] ?: 0f,
                panY = prefs[KEY_PAN_Y] ?: 0f,
                rotationSpeedMs = prefs[KEY_ROTATION_SPEED] ?: 400f
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    override suspend fun loadThemeMode(): String? {
        return try {
            val prefs = context.dataStore.data.first()
            prefs[KEY_THEME_MODE]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveLanguage(langCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = langCode
        }
    }

    override suspend fun loadLanguage(): String? {
        return try {
            val prefs = context.dataStore.data.first()
            prefs[KEY_LANGUAGE]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveCubeEditable(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CUBE_EDITABLE] = enabled
        }
    }

    override suspend fun loadCubeEditable(): Boolean? {
        return try {
            context.dataStore.data.first()[KEY_CUBE_EDITABLE]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SOUND_ENABLED] = enabled
        }
    }

    override suspend fun loadSoundEnabled(): Boolean? {
        return try {
            context.dataStore.data.first()[KEY_SOUND_ENABLED]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveShowcaseCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SHOWCASE_COMPLETED] = completed
        }
    }

    override suspend fun loadShowcaseCompleted(): Boolean? {
        return try {
            context.dataStore.data.first()[KEY_SHOWCASE_COMPLETED]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveEditorShowcaseCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_EDITOR_SHOWCASE_COMPLETED] = completed
        }
    }

    override suspend fun loadEditorShowcaseCompleted(): Boolean? {
        return try {
            context.dataStore.data.first()[KEY_EDITOR_SHOWCASE_COMPLETED]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveScannerShowcaseCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SCANNER_SHOWCASE_COMPLETED] = completed
        }
    }

    override suspend fun loadScannerShowcaseCompleted(): Boolean? {
        return try {
            context.dataStore.data.first()[KEY_SCANNER_SHOWCASE_COMPLETED]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveShakeToScramble(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SHAKE_TO_SCRAMBLE] = enabled
        }
    }

    override suspend fun loadShakeToScramble(): Boolean? {
        return try {
            context.dataStore.data.first()[KEY_SHAKE_TO_SCRAMBLE]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveScrambleSoundTooltipShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SCRAMBLE_SOUND_TOOLTIP_SHOWN] = shown
        }
    }

    override suspend fun loadScrambleSoundTooltipShown(): Boolean? {
        return try {
            context.dataStore.data.first()[KEY_SCRAMBLE_SOUND_TOOLTIP_SHOWN]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    override suspend fun saveSolveSession(durationMillis: Long, moveCount: Int, timestamp: Long) {
        solveSessionDao.insertSession(
            SolveSessionEntity(
                durationMillis = durationMillis,
                moveCount = moveCount,
                timestamp = timestamp
            )
        )
    }

    override suspend fun loadSolveSessions(): List<com.vahitkeskin.rubiksync.utils.SolveSession> {
        return solveSessionDao.getBestSessions().map {
            com.vahitkeskin.rubiksync.utils.SolveSession(
                durationMillis = it.durationMillis,
                moveCount = it.moveCount,
                timestamp = it.timestamp
            )
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
