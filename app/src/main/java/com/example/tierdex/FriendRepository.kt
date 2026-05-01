package com.example.tierdex

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class PublicUserProfile(
    val userId: String,
    val displayName: String,
    val searchDisplayName: String,
    val updatedAt: Timestamp? = null
)

data class FriendUser(
    val userId: String,
    val displayName: String,
    val searchDisplayName: String,
    val connectedAt: Timestamp? = null
)

data class FriendRequest(
    val fromUserId: String,
    val toUserId: String,
    val status: String,
    val createdAt: Timestamp? = null,
    val displayName: String = ""
)

object FriendRepository {
    private const val TAG = "FriendRepository"
    private const val STATUS_PENDING = "pending"
    private const val STATUS_ACCEPTED = "accepted"
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun normalizeDisplayName(displayName: String?): String {
        return displayName.orEmpty().trim().lowercase()
    }

    fun ensureUserProfile(
        userId: String,
        displayName: String?,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (userId.isBlank()) {
            onResult(false, "Leere userId")
            return
        }

        val safeDisplayName = displayName.orEmpty().trim()
        val profileData = hashMapOf(
            "displayName" to safeDisplayName,
            "searchDisplayName" to normalizeDisplayName(safeDisplayName),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(userId)
            .set(profileData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "User profile ensured for $userId")
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to ensure user profile: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onResult(false, exception.message)
            }
    }

    fun loadUserProfile(
        userId: String,
        onResult: (PublicUserProfile?) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        if (userId.isBlank()) {
            onResult(null)
            return
        }

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onResult(null)
                    return@addOnSuccessListener
                }

                onResult(
                    PublicUserProfile(
                        userId = document.id,
                        displayName = document.getString("displayName").orEmpty(),
                        searchDisplayName = document.getString("searchDisplayName").orEmpty(),
                        updatedAt = document.getTimestamp("updatedAt")
                    )
                )
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load user profile: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onError(exception.message)
            }
    }

    fun searchUsersByDisplayName(
        query: String,
        currentUserId: String,
        onResult: (List<PublicUserProfile>) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        val normalizedQuery = normalizeDisplayName(query)
        if (normalizedQuery.isBlank()) {
            onResult(emptyList())
            return
        }

        firestore.collection("users")
            .orderBy("searchDisplayName")
            .startAt(normalizedQuery)
            .endAt(normalizedQuery + "\uf8ff")
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                val results = snapshot.documents.mapNotNull { document ->
                    if (document.id == currentUserId) {
                        null
                    } else {
                        PublicUserProfile(
                            userId = document.id,
                            displayName = document.getString("displayName").orEmpty(),
                            searchDisplayName = document.getString("searchDisplayName").orEmpty(),
                            updatedAt = document.getTimestamp("updatedAt")
                        )
                    }
                }
                onResult(results)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to search users: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onError(exception.message)
            }
    }

    fun sendFriendRequest(
        currentUserId: String,
        targetUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (currentUserId.isBlank() || targetUserId.isBlank()) {
            onResult(false, "Ungültige Nutzer-ID")
            return
        }

        if (currentUserId == targetUserId) {
            onResult(false, "Du kannst dich nicht selbst als Freund hinzufügen")
            return
        }

        val requestData = hashMapOf(
            "fromUserId" to currentUserId,
            "toUserId" to targetUserId,
            "status" to STATUS_PENDING,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val batch = firestore.batch()
        val outgoingRef = firestore.collection("users")
            .document(currentUserId)
            .collection("friendRequestsOutgoing")
            .document(targetUserId)
        val incomingRef = firestore.collection("users")
            .document(targetUserId)
            .collection("friendRequestsIncoming")
            .document(currentUserId)

        batch.set(outgoingRef, requestData)
        batch.set(incomingRef, requestData)
        batch.commit()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to send friend request: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onResult(false, exception.message)
            }
    }

    fun acceptFriendRequest(
        currentUserId: String,
        requesterUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (currentUserId.isBlank() || requesterUserId.isBlank()) {
            onResult(false, "Ungültige Nutzer-ID")
            return
        }

        val acceptedAt = FieldValue.serverTimestamp()
        val currentUserFriendRef = firestore.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(requesterUserId)
        val requesterFriendRef = firestore.collection("users")
            .document(requesterUserId)
            .collection("friends")
            .document(currentUserId)

        val currentUserFriendData = hashMapOf(
            "friendUserId" to requesterUserId,
            "status" to STATUS_ACCEPTED,
            "createdAt" to acceptedAt
        )
        val requesterFriendData = hashMapOf(
            "friendUserId" to currentUserId,
            "status" to STATUS_ACCEPTED,
            "createdAt" to acceptedAt
        )

        val incomingRef = firestore.collection("users")
            .document(currentUserId)
            .collection("friendRequestsIncoming")
            .document(requesterUserId)
        val outgoingRef = firestore.collection("users")
            .document(requesterUserId)
            .collection("friendRequestsOutgoing")
            .document(currentUserId)

        val batch = firestore.batch()
        batch.set(currentUserFriendRef, currentUserFriendData)
        batch.set(requesterFriendRef, requesterFriendData)
        batch.delete(incomingRef)
        batch.delete(outgoingRef)
        batch.commit()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to accept friend request: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onResult(false, exception.message)
            }
    }

    fun loadFriends(
        currentUserId: String,
        onResult: (List<FriendUser>) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        if (currentUserId.isBlank()) {
            onResult(emptyList())
            return
        }

        firestore.collection("users")
            .document(currentUserId)
            .collection("friends")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val pendingResults = mutableListOf<FriendUser>()
                var remaining = snapshot.documents.size
                var firstError: String? = null

                snapshot.documents.forEach { document ->
                    val friendId = document.getString("friendUserId").orEmpty().ifBlank { document.id }
                    val connectedAt = document.getTimestamp("createdAt")

                    loadUserProfile(
                        userId = friendId,
                        onResult = { profile ->
                            profile?.let {
                                pendingResults += FriendUser(
                                    userId = it.userId,
                                    displayName = it.displayName,
                                    searchDisplayName = it.searchDisplayName,
                                    connectedAt = connectedAt
                                )
                            }

                            remaining -= 1
                            if (remaining == 0) {
                                onResult(pendingResults.sortedBy { it.displayName.lowercase() })
                            }
                        },
                        onError = { error ->
                            if (firstError == null) {
                                firstError = error
                            }
                            remaining -= 1
                            if (remaining == 0) {
                                if (pendingResults.isEmpty() && firstError != null) {
                                    onError(firstError)
                                } else {
                                    onResult(pendingResults.sortedBy { it.displayName.lowercase() })
                                }
                            }
                        }
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load friends: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onError(exception.message)
            }
    }

    fun loadIncomingFriendRequests(
        currentUserId: String,
        onResult: (List<FriendRequest>) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        if (currentUserId.isBlank()) {
            onResult(emptyList())
            return
        }

        firestore.collection("users")
            .document(currentUserId)
            .collection("friendRequestsIncoming")
            .whereEqualTo("status", STATUS_PENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val pendingResults = mutableListOf<FriendRequest>()
                var remaining = snapshot.documents.size
                var firstError: String? = null

                snapshot.documents.forEach { document ->
                    val fromUserId = document.getString("fromUserId").orEmpty().ifBlank { document.id }
                    val toUserId = document.getString("toUserId").orEmpty().ifBlank { currentUserId }
                    val status = document.getString("status").orEmpty().ifBlank { STATUS_PENDING }
                    val createdAt = document.getTimestamp("createdAt")

                    loadUserProfile(
                        userId = fromUserId,
                        onResult = { profile ->
                            pendingResults += FriendRequest(
                                fromUserId = fromUserId,
                                toUserId = toUserId,
                                status = status,
                                createdAt = createdAt,
                                displayName = profile?.displayName.orEmpty()
                            )

                            remaining -= 1
                            if (remaining == 0) {
                                onResult(
                                    pendingResults.sortedBy {
                                        it.displayName.ifBlank { it.fromUserId }.lowercase()
                                    }
                                )
                            }
                        },
                        onError = { error ->
                            if (firstError == null) {
                                firstError = error
                            }
                            pendingResults += FriendRequest(
                                fromUserId = fromUserId,
                                toUserId = toUserId,
                                status = status,
                                createdAt = createdAt,
                                displayName = ""
                            )
                            remaining -= 1
                            if (remaining == 0) {
                                if (firstError != null) {
                                    onError(firstError)
                                }
                                onResult(
                                    pendingResults.sortedBy {
                                        it.displayName.ifBlank { it.fromUserId }.lowercase()
                                    }
                                )
                            }
                        }
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load incoming friend requests: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onError(exception.message)
            }
    }
}
