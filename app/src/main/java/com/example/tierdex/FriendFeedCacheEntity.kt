package com.example.tierdex

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "friend_feed_cache",
    primaryKeys = ["cacheOwnerUserId", "ownerUserId", "findingId"],
    indices = [Index(value = ["cacheOwnerUserId"])]
)
data class FriendFeedCacheEntity(
    val cacheOwnerUserId: String,
    val findingId: String,
    val ownerUserId: String,
    val ownerDisplayName: String,
    val ownerProfilePhotoPath: String,
    val animalId: String,
    val date: String,
    val location: String,
    val note: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationSource: String? = null,
    val photoUri: String = "",
    val remotePhotoPath: String = "",
    val thumbnailRemotePhotoPath: String = "",
    val photoUrisJson: String = "[]",
    val remotePhotoPathsJson: String = "[]",
    val taggedFriendIdsJson: String = "[]",
    val likeCount: Int = 0,
    val likedByCurrentUser: Boolean = false,
    val commentCount: Int = 0,
    val sortDateMillis: Long = Long.MIN_VALUE,
    val cachedAtMillis: Long = 0L
)

fun FriendFeedItem.toCacheEntity(cacheOwnerUserId: String): FriendFeedCacheEntity {
    return FriendFeedCacheEntity(
        cacheOwnerUserId = cacheOwnerUserId,
        findingId = findingId,
        ownerUserId = friendUserId,
        ownerDisplayName = friendDisplayName.trim(),
        ownerProfilePhotoPath = friendProfilePhotoPath,
        animalId = finding.animalId,
        date = finding.date,
        location = finding.location,
        note = finding.note,
        latitude = finding.latitude,
        longitude = finding.longitude,
        locationSource = finding.locationSource,
        photoUri = finding.photoUri,
        remotePhotoPath = finding.remotePhotoPath,
        thumbnailRemotePhotoPath = finding.thumbnailRemotePhotoPath,
        photoUrisJson = encodeFindingPhotoList(finding.photoUris),
        remotePhotoPathsJson = encodeFindingPhotoList(finding.remotePhotoPaths),
        taggedFriendIdsJson = encodeTaggedFriendIds(finding.taggedFriendIds),
        likeCount = likeCount,
        likedByCurrentUser = likedByCurrentUser,
        commentCount = commentCount,
        sortDateMillis = FriendRepository.parseFindingDateMillisForCache(finding.date),
        cachedAtMillis = System.currentTimeMillis()
    )
}

fun FriendFeedCacheEntity.toFriendFeedItem(): FriendFeedItem {
    return FriendFeedItem(
        friendUserId = ownerUserId,
        friendDisplayName = ownerDisplayName.trim(),
        friendProfilePhotoPath = ownerProfilePhotoPath,
        findingId = findingId,
        finding = AnimalFinding(
            animalId = animalId,
            date = date,
            location = location,
            note = note,
            photoUri = photoUri,
            remotePhotoPath = remotePhotoPath,
            thumbnailRemotePhotoPath = thumbnailRemotePhotoPath,
            photoUris = decodeFindingPhotoList(photoUrisJson),
            remotePhotoPaths = decodeFindingPhotoList(remotePhotoPathsJson),
            latitude = latitude,
            longitude = longitude,
            locationSource = locationSource,
            ownerId = ownerUserId,
            taggedFriendIds = decodeTaggedFriendIds(taggedFriendIdsJson)
        ),
        likeCount = likeCount,
        likedByCurrentUser = likedByCurrentUser,
        commentCount = commentCount
    )
}
