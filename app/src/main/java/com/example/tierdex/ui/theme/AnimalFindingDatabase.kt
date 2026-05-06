package com.example.tierdex

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tierdex.AnimalFindingDao
import com.example.tierdex.AnimalFindingEntity

@Database(
    entities = [AnimalFindingEntity::class],
    version = 6,
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE animal_findings ADD COLUMN taggedFriendIdsJson TEXT NOT NULL DEFAULT '[]'"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE animal_findings ADD COLUMN remotePhotoPath TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE animal_findings ADD COLUMN photoUrisJson TEXT NOT NULL DEFAULT '[]'"
                )
                database.execSQL(
                    "ALTER TABLE animal_findings ADD COLUMN remotePhotoPathsJson TEXT NOT NULL DEFAULT '[]'"
                )
            }
        }
    }
}
