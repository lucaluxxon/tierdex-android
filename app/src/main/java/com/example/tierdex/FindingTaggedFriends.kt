package com.example.tierdex

import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val taggedFriendIdsListType = object : TypeToken<List<String>>() {}.type
private val findingPhotoListType = object : TypeToken<List<String>>() {}.type

fun encodeTaggedFriendIds(taggedFriendIds: List<String>): String {
    val normalizedIds = taggedFriendIds
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
    return Gson().toJson(normalizedIds, taggedFriendIdsListType)
}

fun encodeFindingPhotoList(photoValues: List<String>): String {
    val normalizedValues = photoValues
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(3)
    return Gson().toJson(normalizedValues, findingPhotoListType)
}

fun decodeTaggedFriendIds(rawValue: String?): List<String> {
    if (rawValue.isNullOrBlank()) return emptyList()

    return runCatching {
        Gson().fromJson<List<String>>(rawValue, taggedFriendIdsListType)
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            .orEmpty()
    }.getOrDefault(emptyList())
}

fun decodeFindingPhotoList(rawValue: String?): List<String> {
    if (rawValue.isNullOrBlank()) return emptyList()

    return runCatching {
        Gson().fromJson<List<String>>(rawValue, findingPhotoListType)
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?.take(3)
            .orEmpty()
    }.getOrDefault(emptyList())
}

fun AnimalFindingEntity.toDomainFinding(): AnimalFinding {
    val photoUris = decodeFindingPhotoList(photoUrisJson)
    val remotePhotoPaths = decodeFindingPhotoList(remotePhotoPathsJson)

    return AnimalFinding(
        roomId = id,
        animalId = animalId,
        date = date,
        location = location,
        note = note,
        photoUri = photoUri,
        remotePhotoPath = remotePhotoPath,
        photoUris = photoUris,
        remotePhotoPaths = remotePhotoPaths,
        latitude = latitude,
        longitude = longitude,
        locationSource = locationSource,
        ownerId = ownerId,
        taggedFriendIds = decodeTaggedFriendIds(taggedFriendIdsJson)
    )
}

fun AnimalFinding.toEntity(ownerIdOverride: String? = ownerId, roomIdOverride: Int? = roomId): AnimalFindingEntity {
    val normalizedPhotoUris = (photoUris + listOf(photoUri))
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(3)
    val normalizedRemotePhotoPaths = (remotePhotoPaths + listOf(remotePhotoPath))
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(3)

    return AnimalFindingEntity(
        id = roomIdOverride ?: 0,
        animalId = animalId,
        date = date,
        location = location,
        note = note,
        photoUri = normalizedPhotoUris.firstOrNull().orEmpty(),
        remotePhotoPath = normalizedRemotePhotoPaths.firstOrNull().orEmpty(),
        photoUrisJson = encodeFindingPhotoList(normalizedPhotoUris),
        remotePhotoPathsJson = encodeFindingPhotoList(normalizedRemotePhotoPaths),
        latitude = latitude,
        longitude = longitude,
        locationSource = locationSource,
        ownerId = ownerIdOverride,
        taggedFriendIdsJson = encodeTaggedFriendIds(taggedFriendIds)
    )
}

fun DocumentSnapshot.getTaggedFriendIdsOrEmpty(): List<String> {
    return (get("taggedFriendIds") as? List<*>)
        ?.mapNotNull { value -> (value as? String)?.trim() }
        ?.filter { it.isNotBlank() }
        ?.distinct()
        .orEmpty()
}

fun DocumentSnapshot.getPhotoValuesOrEmpty(fieldName: String): List<String> {
    return (get(fieldName) as? List<*>)
        ?.mapNotNull { value -> (value as? String)?.trim() }
        ?.filter { it.isNotBlank() }
        ?.distinct()
        ?.take(3)
        .orEmpty()
}
