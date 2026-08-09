package com.example.tierdex

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

object FirestoreFindingRepository {
    private const val TAG = "FirestoreFindings"
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun normalizedIdentityText(value: String?): String = value.orEmpty().trim()

    private fun normalizedIdentityCoordinate(value: Double?): String {
        return value?.let { "%.6f".format(java.util.Locale.US, it) }.orEmpty()
    }

    private fun normalizedTaggedFriendIds(taggedFriendIds: List<String>): String {
        return taggedFriendIds
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .joinToString(",")
    }

    private fun findingFingerprint(
        ownerId: String?,
        animalId: String,
        date: String,
        location: String,
        note: String,
        latitude: Double?,
        longitude: Double?,
        taggedFriendIds: List<String>
    ): String {
        return listOf(
            normalizedIdentityText(ownerId),
            normalizedIdentityText(animalId),
            normalizedIdentityText(date),
            normalizedIdentityText(location),
            normalizedIdentityText(note),
            normalizedIdentityCoordinate(latitude),
            normalizedIdentityCoordinate(longitude),
            normalizedTaggedFriendIds(taggedFriendIds)
        ).joinToString("|")
    }

    fun findingFingerprint(finding: AnimalFinding): String {
        return findingFingerprint(
            ownerId = finding.ownerId,
            animalId = finding.animalId,
            date = finding.date,
            location = finding.location,
            note = finding.note,
            latitude = finding.latitude,
            longitude = finding.longitude,
            taggedFriendIds = finding.taggedFriendIds
        )
    }

    private fun hashedDocumentId(fingerprint: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(fingerprint.toByteArray())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun hashedDocumentIdForFinding(finding: AnimalFinding): String {
        return hashedDocumentId(findingFingerprint(finding))
    }

    fun documentIdForFinding(finding: AnimalFinding): String {
        return finding.findingId
            ?.takeIf { it.isNotBlank() }
            ?: hashedDocumentIdForFinding(finding)
    }

    private fun matchesStableFindingIdentity(
        finding: AnimalFinding,
        ownerId: String,
        animalId: String,
        date: String,
        location: String,
        note: String,
        latitude: Double?,
        longitude: Double?,
        taggedFriendIds: List<String>
    ): Boolean {
        return findingFingerprint(finding) == findingFingerprint(
            ownerId = ownerId,
            animalId = animalId,
            date = date,
            location = location,
            note = note,
            latitude = latitude,
            longitude = longitude,
            taggedFriendIds = taggedFriendIds
        )
    }

    private fun globalFindingCountFromValue(rawValue: Any?): Long {
        return when (rawValue) {
            is Long -> rawValue
            is Int -> rawValue.toLong()
            is Double -> rawValue.toLong()
            is Float -> rawValue.toLong()
            else -> 0L
        }.coerceAtLeast(0L)
    }

    fun saveCurrentUserFinding(
        finding: AnimalFinding,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = AuthSession.getCurrentFirebaseUserId()
        if (uid.isNullOrBlank()) {
            onResult(false, "Kein Firebase-Nutzer eingeloggt")
            return
        }

        val documentId = hashedDocumentIdForFinding(finding)
        val documentRef = firestore.collection("users")
            .document(uid)
            .collection("findings")
            .document(documentId)
        Log.d(TAG, "Generated hashed Firestore documentId documentIdPresent=${documentId.isNotBlank()}")

        documentRef
            .get()
            .addOnSuccessListener { existingDocument ->
                val findingData = hashMapOf<String, Any?>(
                    "animalId" to finding.animalId,
                    "date" to finding.date,
                    "location" to finding.location,
                    "note" to finding.note,
                    "remotePhotoPath" to finding.remotePhotoPath,
                    "thumbnailRemotePhotoPath" to finding.thumbnailRemotePhotoPath,
                    "remotePhotoPaths" to effectiveRemotePhotoPaths(finding),
                    "latitude" to finding.latitude,
                    "longitude" to finding.longitude,
                    "locationSource" to finding.locationSource,
                    "taggedFriendIds" to finding.taggedFriendIds,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                existingDocument.getTimestamp("createdAt")?.let { existingCreatedAt ->
                    findingData["createdAt"] = existingCreatedAt
                } ?: run {
                    findingData["createdAt"] = FieldValue.serverTimestamp()
                }

                documentRef
                    .set(findingData)
                    .addOnSuccessListener {
                        Log.d(TAG, "Saved finding documentIdPresent=${documentId.isNotBlank()} userPresent=${uid.isNotBlank()}")
                        onResult(true, documentId)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            TAG,
                            "Failed to save finding to Firestore: ${exception.message ?: "Unbekannter Fehler"}",
                            exception
                        )
                        onResult(false, exception.message)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to prepare finding save for Firestore: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onResult(false, exception.message)
            }
    }

    fun deleteCurrentUserFinding(
        finding: AnimalFinding,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = AuthSession.getCurrentFirebaseUserId()
        if (uid.isNullOrBlank()) {
            onResult(false, "Kein Firebase-Nutzer eingeloggt")
            return
        }

        val documentId = hashedDocumentIdForFinding(finding)
        val findingsCollection = firestore.collection("users")
            .document(uid)
            .collection("findings")

        Log.d("CloudSyncDelete", "Hash-delete attempted documentIdPresent=${documentId.isNotBlank()}")

        findingsCollection
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                findingsCollection
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val legacyMatches = snapshot.documents.filter { document ->
                            matchesStableFindingIdentity(
                                finding = finding,
                                ownerId = uid,
                                animalId = document.getString("animalId").orEmpty(),
                                date = document.getString("date").orEmpty(),
                                location = document.getString("location").orEmpty(),
                                note = document.getString("note").orEmpty(),
                                latitude = document.getDouble("latitude"),
                                longitude = document.getDouble("longitude"),
                                taggedFriendIds = document.getTaggedFriendIdsOrEmpty()
                            )
                        }

                        Log.d("CloudSyncDelete", "Legacy cloud matches found: ${legacyMatches.size}")

                        if (legacyMatches.isEmpty()) {
                            Log.d("CloudSyncDelete", "No matching legacy cloud documents found")
                            Log.d("CloudSyncDelete", "Delete finished documentIdPresent=${documentId.isNotBlank()}")
                            onResult(true, documentId)
                            return@addOnSuccessListener
                        }

                        var pendingDeletes = legacyMatches.size
                        var hasFailure = false

                        legacyMatches.forEach { document ->
                            findingsCollection
                                .document(document.id)
                                .delete()
                                .addOnSuccessListener {
                                    Log.d(
                                        "CloudSyncDelete",
                                        "Legacy cloud document deleted documentIdPresent=${document.id.isNotBlank()}"
                                    )
                                    pendingDeletes -= 1
                                    if (pendingDeletes == 0) {
                                        Log.d("CloudSyncDelete", "Delete finished documentIdPresent=${documentId.isNotBlank()}")
                                        onResult(!hasFailure, documentId)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    hasFailure = true
                                    pendingDeletes -= 1
                                    Log.e(
                                        "CloudSyncDelete",
                                        "Legacy cloud delete failed documentIdPresent=${document.id.isNotBlank()} reason=${exception.message ?: "Unbekannter Fehler"}",
                                        exception
                                    )
                                    if (pendingDeletes == 0) {
                                        Log.d("CloudSyncDelete", "Delete finished documentIdPresent=${documentId.isNotBlank()}")
                                        onResult(false, exception.message)
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "CloudSyncDelete",
                            "Legacy cloud lookup failed: ${exception.message ?: "Unbekannter Fehler"}",
                            exception
                        )
                        onResult(false, exception.message)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to delete finding from Firestore: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onResult(false, exception.message)
            }
    }

    fun updateCurrentUserFinding(
        oldFinding: AnimalFinding,
        newFinding: AnimalFinding,
        onResult: (Boolean, String?) -> Unit
    ) {
        val oldDocumentId = hashedDocumentIdForFinding(oldFinding)
        val newDocumentId = hashedDocumentIdForFinding(newFinding)

        if (oldDocumentId == newDocumentId) {
            saveCurrentUserFinding(newFinding, onResult)
            return
        }

        deleteCurrentUserFinding(oldFinding) { deleteSuccess, deleteResult ->
            if (!deleteSuccess) {
                onResult(false, deleteResult)
                return@deleteCurrentUserFinding
            }

            saveCurrentUserFinding(newFinding, onResult)
        }
    }

    fun loadCurrentUserFindings(
        onResult: (List<AnimalFinding>) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        Log.d(TAG, "Firestore: Lade gestartet")
        val uid = AuthSession.getCurrentFirebaseUserId()
        if (uid.isNullOrBlank()) {
            Log.d(TAG, "Firestore: Keine Firebase-UID vorhanden, gebe leere Liste zurück")
            onResult(emptyList())
            return
        }

        firestore.collection("users")
            .document(uid)
            .collection("findings")
            .get()
            .addOnSuccessListener { snapshot ->
                val findings = snapshot.documents.map { document ->
                    AnimalFinding(
                        findingId = document.id,
                        animalId = document.getString("animalId").orEmpty(),
                        date = document.getString("date").orEmpty(),
                        location = document.getString("location").orEmpty(),
                        note = document.getString("note").orEmpty(),
                        photoUri = document.getString("photoUri").orEmpty(),
                        remotePhotoPath = document.getString("remotePhotoPath").orEmpty(),
                        thumbnailRemotePhotoPath = document.getString("thumbnailRemotePhotoPath").orEmpty(),
                        photoUris = document.getPhotoValuesOrEmpty("photoUris"),
                        remotePhotoPaths = document.getPhotoValuesOrEmpty("remotePhotoPaths"),
                        latitude = document.getDouble("latitude"),
                        longitude = document.getDouble("longitude"),
                        locationSource = document.getString("locationSource"),
                        ownerId = uid,
                        taggedFriendIds = document.getTaggedFriendIdsOrEmpty()
                    )
                }

                Log.d(TAG, "Loaded ${findings.size} findings from Firestore userPresent=${uid.isNotBlank()}")
                onResult(findings)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load findings from Firestore: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onError(exception.message)
            }
    }

    fun loadAnimalStats(
        onResult: (Map<String, Int>) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        firestore.collection("animalStats")
            .get()
            .addOnSuccessListener { snapshot ->
                val countsByAnimalId = snapshot.documents.associateNotNull { document ->
                    val animalId = document.getString("animalId").orEmpty().trim()
                    if (animalId.isBlank()) {
                        null
                    } else {
                        animalId to globalFindingCountFromValue(document.get("globalFindingCount")).toInt()
                    }
                }
                onResult(countsByAnimalId)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load animalStats: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onError(exception.message)
            }
    }
}

private inline fun <T, R : Any> Iterable<T>.associateNotNull(transform: (T) -> Pair<String, R>?): Map<String, R> {
    val destination = LinkedHashMap<String, R>()
    for (item in this) {
        val entry = transform(item) ?: continue
        destination[entry.first] = entry.second
    }
    return destination
}
