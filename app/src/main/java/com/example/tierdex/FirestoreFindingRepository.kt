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
            "photoUri" to finding.photoUri
        )

        val fingerprint = findingFingerprint(finding)
        val documentId = hashedDocumentId(fingerprint)
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
