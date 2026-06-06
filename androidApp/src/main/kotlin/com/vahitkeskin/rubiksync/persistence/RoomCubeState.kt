package com.vahitkeskin.rubiksync.persistence

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "cube_state")
data class CubeStateEntity(
    @PrimaryKey val id: Int = 1,
    val cubiesData: String,
    val moveHistoryData: String,
    val manualMovesData: String,
    val editorFacesData: String
)

@Entity(tableName = "solve_sessions")
data class SolveSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationMillis: Long,
    val moveCount: Int,
    val timestamp: Long
)

@Dao
interface CubeStateDao {
    @Query("SELECT * FROM cube_state WHERE id = 1")
    suspend fun getCubeState(): CubeStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCubeState(state: CubeStateEntity)

    @Query("DELETE FROM cube_state")
    suspend fun clearCubeState()
}

@Dao
interface SolveSessionDao {
    @Query("SELECT * FROM solve_sessions ORDER BY durationMillis ASC")
    suspend fun getBestSessions(): List<SolveSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SolveSessionEntity)

    @Query("DELETE FROM solve_sessions")
    suspend fun clearSessions()
}
