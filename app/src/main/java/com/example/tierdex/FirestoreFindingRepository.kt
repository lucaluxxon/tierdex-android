package com.example.tierdex

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

object FirestoreFindingRepository {
    private const val TAG = "FirestoreFindings"
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

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

    private fun documentIdForFinding(finding: AnimalFinding): String {
        return hashedDocumentId(findingFingerprint(finding))
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
            "latitude" to finding.latitude,
            "longitude" to finding.longitude,
            "locationSource" to finding.locationSource
        )

        val documentId = documentIdForFinding(finding)
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

        val documentId = documentIdForFinding(finding)
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
        val oldDocumentId = documentIdForFinding(oldFinding)
        val newDocumentId = documentIdForFinding(newFinding)

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
                        latitude = document.getDouble("latitude"),
                        longitude = document.getDouble("longitude"),
                        locationSource = document.getString("locationSource"),
                        ownerId = uid
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
}
