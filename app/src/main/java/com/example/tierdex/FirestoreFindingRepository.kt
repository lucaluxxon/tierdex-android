package com.example.tierdex

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.security.MessageDigest

object FirestoreFindingRepository {
    private const val TAG = "FirestoreFindings"
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private fun animalStatsDocument(animalId: String) =
        firestore.collection("animalStats").document(animalId)
    private fun globalFindingContributionDocument(ownerUid: String, findingId: String) =
        firestore.collection("globalFindingContributions")
            .document("${ownerUid.trim()}_${findingId.trim()}")

    private fun findingFingerprint(
        animalId: String,
        date: String,
        location: String,
        note: String,
        photoUri: String
    ): String {
        return listOf(animalId, date, location, note, photoUri)
            .joinToString("|") { it.trim() }
    }

    fun findingFingerprint(finding: AnimalFinding): String {
        return findingFingerprint(
            animalId = finding.animalId,
            date = finding.date,
            location = finding.location,
            note = finding.note,
            photoUri = finding.photoUri
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
        return hashedDocumentIdForFinding(finding)
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

        val findingData = hashMapOf(
            "animalId" to finding.animalId,
            "date" to finding.date,
            "location" to finding.location,
            "note" to finding.note,
            "photoUri" to finding.photoUri,
            "remotePhotoPath" to finding.remotePhotoPath,
            "thumbnailRemotePhotoPath" to finding.thumbnailRemotePhotoPath,
            "photoUris" to effectiveLocalPhotoUris(finding),
            "remotePhotoPaths" to effectiveRemotePhotoPaths(finding),
            "latitude" to finding.latitude,
            "longitude" to finding.longitude,
            "locationSource" to finding.locationSource,
            "taggedFriendIds" to finding.taggedFriendIds
        )

        val documentId = hashedDocumentIdForFinding(finding)
        Log.d(TAG, "Generated hashed Firestore documentId for finding: $documentId")

        firestore.collection("users")
            .document(uid)
            .collection("findings")
            .document(documentId)
            .set(findingData)
            .addOnSuccessListener {
                Log.d(TAG, "Saved finding $documentId for user $uid")
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

        Log.d("CloudSyncDelete", "Hash-delete attempted: $documentId")

        findingsCollection
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                findingsCollection
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val legacyMatches = snapshot.documents.filter { document ->
                            document.getString("animalId").orEmpty() == finding.animalId &&
                                    document.getString("date").orEmpty() == finding.date &&
                                    document.getString("location").orEmpty() == finding.location &&
                                    document.getString("note").orEmpty() == finding.note &&
                                    document.getString("photoUri").orEmpty() == finding.photoUri
                        }

                        Log.d("CloudSyncDelete", "Legacy cloud matches found: ${legacyMatches.size}")

                        if (legacyMatches.isEmpty()) {
                            Log.d("CloudSyncDelete", "No matching legacy cloud documents found")
                            Log.d("CloudSyncDelete", "Delete finished for finding $documentId")
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
                                        "Legacy cloud document deleted: ${document.id}"
                                    )
                                    pendingDeletes -= 1
                                    if (pendingDeletes == 0) {
                                        Log.d("CloudSyncDelete", "Delete finished for finding $documentId")
                                        onResult(!hasFailure, documentId)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    hasFailure = true
                                    pendingDeletes -= 1
                                    Log.e(
                                        "CloudSyncDelete",
                                        "Legacy cloud delete failed for ${document.id}: ${exception.message ?: "Unbekannter Fehler"}",
                                        exception
                                    )
                                    if (pendingDeletes == 0) {
                                        Log.d("CloudSyncDelete", "Delete finished for finding $documentId")
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

                Log.d(TAG, "Loaded ${findings.size} findings for user $uid from Firestore")
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

    fun recordGlobalFindingContributionIfNeeded(
        ownerUid: String,
        finding: AnimalFinding,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val normalizedOwnerUid = ownerUid.trim()
        val normalizedAnimalId = finding.animalId.trim()
        val findingId = documentIdForFinding(finding).trim()

        if (normalizedOwnerUid.isBlank() || normalizedAnimalId.isBlank() || findingId.isBlank()) {
            onResult(false, "Ungültige Contribution-Daten")
            return
        }

        val contributionDocument = globalFindingContributionDocument(normalizedOwnerUid, findingId)
        val statsDocument = animalStatsDocument(normalizedAnimalId)
        firestore.runTransaction { transaction ->
            val contributionSnapshot = transaction.get(contributionDocument)
            if (contributionSnapshot.exists()) {
                return@runTransaction "exists"
            }

            val snapshot = transaction.get(statsDocument)
            val currentCount = globalFindingCountFromValue(snapshot.get("globalFindingCount"))
            val updatedCount = currentCount + 1L
            transaction.set(
                statsDocument,
                hashMapOf(
                    "animalId" to normalizedAnimalId,
                    "globalFindingCount" to updatedCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            transaction.set(
                contributionDocument,
                hashMapOf(
                    "ownerUid" to normalizedOwnerUid,
                    "findingId" to findingId,
                    "animalId" to normalizedAnimalId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            "created"
        }.addOnSuccessListener {
            onResult(true, it)
        }.addOnFailureListener { exception ->
            Log.e(
                TAG,
                "Failed to record global finding contribution for $normalizedOwnerUid/$findingId: ${exception.message ?: "Unbekannter Fehler"}",
                exception
            )
            onResult(false, exception.message)
        }
    }

    fun removeGlobalFindingContributionIfExists(
        ownerUid: String,
        finding: AnimalFinding,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val normalizedOwnerUid = ownerUid.trim()
        val normalizedAnimalId = finding.animalId.trim()
        val findingId = documentIdForFinding(finding).trim()

        if (normalizedOwnerUid.isBlank() || normalizedAnimalId.isBlank() || findingId.isBlank()) {
            onResult(false, "Ungültige Contribution-Daten")
            return
        }

        val contributionDocument = globalFindingContributionDocument(normalizedOwnerUid, findingId)
        val statsDocument = animalStatsDocument(normalizedAnimalId)
        firestore.runTransaction { transaction ->
            val contributionSnapshot = transaction.get(contributionDocument)
            if (!contributionSnapshot.exists()) {
                return@runTransaction "missing"
            }

            val snapshot = transaction.get(statsDocument)
            val currentCount = globalFindingCountFromValue(snapshot.get("globalFindingCount"))
            val updatedCount = (currentCount - 1L).coerceAtLeast(0L)
            transaction.set(
                statsDocument,
                hashMapOf(
                    "animalId" to normalizedAnimalId,
                    "globalFindingCount" to updatedCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            transaction.delete(contributionDocument)
            "removed"
        }.addOnSuccessListener {
            onResult(true, it)
        }.addOnFailureListener { exception ->
            Log.e(
                TAG,
                "Failed to remove global finding contribution for $normalizedOwnerUid/$findingId: ${exception.message ?: "Unbekannter Fehler"}",
                exception
            )
            onResult(false, exception.message)
        }
    }

    fun updateGlobalFindingContributionForChange(
        ownerUid: String,
        oldFinding: AnimalFinding,
        newFinding: AnimalFinding,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val normalizedOwnerUid = ownerUid.trim()
        if (normalizedOwnerUid.isBlank()) {
            onResult(false, "Leere ownerUid")
            return
        }

        val oldFindingId = documentIdForFinding(oldFinding).trim()
        val newFindingId = documentIdForFinding(newFinding).trim()

        if (oldFindingId == newFindingId) {
            onResult(true, "unchanged")
            return
        }

        removeGlobalFindingContributionIfExists(
            ownerUid = normalizedOwnerUid,
            finding = oldFinding
        ) { removeSuccess, removeResult ->
            if (!removeSuccess) {
                onResult(false, removeResult)
                return@removeGlobalFindingContributionIfExists
            }

            recordGlobalFindingContributionIfNeeded(
                ownerUid = normalizedOwnerUid,
                finding = newFinding,
                onResult = { recordSuccess, recordResult ->
                    if (!recordSuccess) {
                        onResult(false, recordResult)
                    } else {
                        val status = when {
                            removeResult == "removed" && recordResult == "created" -> "moved"
                            removeResult == "missing" && recordResult == "created" -> "created"
                            removeResult == "removed" && recordResult == "exists" -> "removed_only"
                            else -> recordResult ?: removeResult
                        }
                        onResult(true, status)
                    }
                }
            )
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

    fun backfillGlobalFindingCountsForCurrentUser(
        ownerUid: String,
        findings: List<AnimalFinding>,
        onResult: (Boolean) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        val normalizedOwnerUid = ownerUid.trim()
        if (normalizedOwnerUid.isBlank()) {
            onResult(false)
            return
        }

        val uniqueFindings = findings
            .filter { it.animalId.isNotBlank() }
            .distinctBy { documentIdForFinding(it) }

        if (uniqueFindings.isEmpty()) {
            onResult(true)
            return
        }

        var currentIndex = 0

        fun processNext() {
            if (currentIndex >= uniqueFindings.size) {
                onResult(true)
                return
            }

            val finding = uniqueFindings[currentIndex]
            currentIndex += 1

            recordGlobalFindingContributionIfNeeded(
                ownerUid = normalizedOwnerUid,
                finding = finding
            ) { success, result ->
                if (!success) {
                    onError(result)
                    onResult(false)
                    return@recordGlobalFindingContributionIfNeeded
                }
                processNext()
            }
        }

        processNext()
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
