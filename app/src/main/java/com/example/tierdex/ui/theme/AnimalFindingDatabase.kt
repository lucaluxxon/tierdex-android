package com.example.tierdex

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tierdex.AnimalFindingDao
import com.example.tierdex.AnimalFindingEntity
import com.example.tierdex.FriendFeedCacheDao
import com.example.tierdex.FriendFeedCacheEntity

@Database(
    entities = [AnimalFindingEntity::class, FriendFeedCacheEntity::class],
    version = 8,
    exportSchema = false
)
abstract class AnimalFindingDatabase : RoomDatabase() {
    abstract fun animalFindingDao(): AnimalFindingDao
    abstract fun friendFeedCacheDao(): FriendFeedCacheDao

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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE animal_findings ADD COLUMN thumbnailRemotePhotoPath TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `friend_feed_cache` (
                        `cacheOwnerUserId` TEXT NOT NULL,
                        `findingId` TEXT NOT NULL,
                        `ownerUserId` TEXT NOT NULL,
                        `ownerDisplayName` TEXT NOT NULL,
                        `ownerProfilePhotoPath` TEXT NOT NULL,
                        `animalId` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `location` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        `latitude` REAL,
                        `longitude` REAL,
                        `locationSource` TEXT,
                        `photoUri` TEXT NOT NULL,
                        `remotePhotoPath` TEXT NOT NULL,
                        `thumbnailRemotePhotoPath` TEXT NOT NULL,
                        `photoUrisJson` TEXT NOT NULL,
                        `remotePhotoPathsJson` TEXT NOT NULL,
                        `taggedFriendIdsJson` TEXT NOT NULL,
                        `likeCount` INTEGER NOT NULL,
                        `likedByCurrentUser` INTEGER NOT NULL,
                        `commentCount` INTEGER NOT NULL,
                        `sortDateMillis` INTEGER NOT NULL,
                        `cachedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`cacheOwnerUserId`, `ownerUserId`, `findingId`)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_friend_feed_cache_cacheOwnerUserId` ON `friend_feed_cache` (`cacheOwnerUserId`)"
                )
            }
        }
    }
}
