package com.example.repit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ExerciseLogEntity::class, RestDaySettings::class], version = 3, exportSchema = false)
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
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add the `isRestDay` column to `exercise_logs`
        database.execSQL("ALTER TABLE exercise_logs ADD COLUMN isRestDay INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the `rest_day_settings` table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS rest_day_settings (
                dayOfWeek TEXT PRIMARY KEY NOT NULL,
                isRestDay INTEGER NOT NULL
            )
        """)
    }
}
