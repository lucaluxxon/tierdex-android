package com.example.tierdex

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FriendFeedCacheDao {
    @Query(
        "SELECT * FROM friend_feed_cache WHERE cacheOwnerUserId = :cacheOwnerUserId " +
            "ORDER BY sortDateMillis DESC, cachedAtMillis DESC, ownerUserId ASC, findingId ASC"
    )
    suspend fun getFeedCacheForUser(cacheOwnerUserId: String): List<FriendFeedCacheEntity>

    @Query("DELETE FROM friend_feed_cache WHERE cacheOwnerUserId = :cacheOwnerUserId")
    suspend fun clearFeedCacheForUser(cacheOwnerUserId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedCacheItems(items: List<FriendFeedCacheEntity>)

    @Transaction
    suspend fun replaceFeedCacheForUser(
        cacheOwnerUserId: String,
        items: List<FriendFeedCacheEntity>
    ) {
        clearFeedCacheForUser(cacheOwnerUserId)
        if (items.isNotEmpty()) {
            insertFeedCacheItems(items)
        }
    }
}
