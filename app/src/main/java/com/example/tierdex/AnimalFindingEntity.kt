package com.example.tierdex

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animal_findings")
data class AnimalFindingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val findingId: String? = null,
    val animalId: String,
    val date: String,
    val location: String,
    val note: String,
    val photoUri: String,
    val remotePhotoPath: String = "",
    val thumbnailRemotePhotoPath: String = "",
    val photoUrisJson: String = "[]",
    val remotePhotoPathsJson: String = "[]",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationSource: String? = null,
    val ownerId: String? = null,
    val taggedFriendIdsJson: String = "[]"
)
