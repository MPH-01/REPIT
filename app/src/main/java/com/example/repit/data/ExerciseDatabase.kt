package com.example.repit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ExerciseLogEntity::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateConverter::class)
abstract class ExerciseDatabase : RoomDatabase() {

    abstract fun exerciseLogDao(): ExerciseLogDao

    companion object {
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        fun getDatabase(context: Context): ExerciseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    "exercise_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
