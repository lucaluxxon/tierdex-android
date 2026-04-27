package com.example.tierdex

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tierdex.AnimalFindingDao
import com.example.tierdex.AnimalFindingEntity

@Database(
    entities = [AnimalFindingEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AnimalFindingDatabase : RoomDatabase() {
    abstract fun animalFindingDao(): AnimalFindingDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE animal_findings ADD COLUMN ownerId TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE animal_findings ADD COLUMN latitude REAL")
                database.execSQL("ALTER TABLE animal_findings ADD COLUMN longitude REAL")
                database.execSQL("ALTER TABLE animal_findings ADD COLUMN locationSource TEXT")
            }
        }
    }
}
