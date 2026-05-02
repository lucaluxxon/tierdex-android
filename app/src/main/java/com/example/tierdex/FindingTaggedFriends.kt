package com.example.tierdex

import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val taggedFriendIdsListType = object : TypeToken<List<String>>() {}.type

fun encodeTaggedFriendIds(taggedFriendIds: List<String>): String {
    val normalizedIds = taggedFriendIds
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
    return Gson().toJson(normalizedIds, taggedFriendIdsListType)
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

fun AnimalFindingEntity.toDomainFinding(): AnimalFinding {
    return AnimalFinding(
        roomId = id,
        animalId = animalId,
        date = date,
        location = location,
        note = note,
        photoUri = photoUri,
        latitude = latitude,
        longitude = longitude,
        locationSource = locationSource,
        ownerId = ownerId,
        taggedFriendIds = decodeTaggedFriendIds(taggedFriendIdsJson)
    )
}

fun AnimalFinding.toEntity(ownerIdOverride: String? = ownerId, roomIdOverride: Int? = roomId): AnimalFindingEntity {
    return AnimalFindingEntity(
        id = roomIdOverride ?: 0,
        animalId = animalId,
        date = date,
        location = location,
        note = note,
        photoUri = photoUri,
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
