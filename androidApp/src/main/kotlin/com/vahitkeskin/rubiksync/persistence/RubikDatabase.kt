package com.vahitkeskin.rubiksync.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CubeStateEntity::class], version = 1, exportSchema = false)
abstract class RubikDatabase : RoomDatabase() {
    abstract fun cubeStateDao(): CubeStateDao

    companion object {
        @Volatile
        private var INSTANCE: RubikDatabase? = null

        fun getDatabase(context: Context): RubikDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RubikDatabase::class.java,
                    "rubik_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
