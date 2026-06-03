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

@Dao
interface CubeStateDao {
    @Query("SELECT * FROM cube_state WHERE id = 1")
    suspend fun getCubeState(): CubeStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCubeState(state: CubeStateEntity)

    @Query("DELETE FROM cube_state")
    suspend fun clearCubeState()
}
