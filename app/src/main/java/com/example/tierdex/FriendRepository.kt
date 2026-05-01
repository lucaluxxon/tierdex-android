package com.example.tierdex

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

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

data class FriendFeedItem(
    val friendUserId: String,
    val friendDisplayName: String,
    val findingId: String,
    val finding: AnimalFinding,
    val likeCount: Int = 0,
    val likedByCurrentUser: Boolean = false
)

data class FriendFindingComment(
    val commentId: String,
    val commenterUid: String,
    val commenterDisplayName: String,
    val text: String,
    val createdAt: Timestamp? = null
)

object FriendRepository {
    private const val TAG = "FriendRepository"
    private const val STATUS_PENDING = "pending"
    private const val STATUS_ACCEPTED = "accepted"
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun normalizeDisplayName(displayName: String?): String {
        return displayName.orEmpty().trim().lowercase()
    }

    private fun userDocument(userId: String) = firestore.collection("users").document(userId)
    private fun findingLikesCollection(ownerUserId: String, findingId: String) =
        userDocument(ownerUserId).collection("findings").document(findingId).collection("likes")
    private fun findingCommentsCollection(ownerUserId: String, findingId: String) =
        userDocument(ownerUserId).collection("findings").document(findingId).collection("comments")

    private fun toFirestoreErrorMessage(
        functionName: String,
        operation: String,
        path: String,
        exception: Exception
    ): String {
        val isPermissionDenied =
            (exception as? FirebaseFirestoreException)?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ||
                exception.message?.contains("Missing or insufficient permissions", ignoreCase = true) == true ||
                exception.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true
        val reason = if (isPermissionDenied) {
            "Permission denied"
        } else {
            exception.message ?: "Unbekannter Fehler"
        }
        return "$functionName: $operation $path fehlgeschlagen ($reason)"
    }

    private fun toFirestoreException(
        functionName: String,
        operation: String,
        path: String,
        exception: Exception
    ): Exception = Exception(toFirestoreErrorMessage(functionName, operation, path, exception), exception)

    private fun parseFindingDateMillis(dateText: String): Long {
        if (dateText.isBlank()) return Long.MIN_VALUE

        val patterns = listOf("dd.MM.yyyy", "d.M.yyyy", "yyyy-MM-dd")
        patterns.forEach { pattern ->
            runCatching {
                val formatter = SimpleDateFormat(pattern, Locale.getDefault()).apply {
                    isLenient = false
                }
                formatter.parse(dateText)?.time
            }.getOrNull()?.let { parsed ->
                return parsed
            }
        }

        return Long.MIN_VALUE
    }

    fun loadLikeInfoForFinding(
        ownerUserId: String,
        findingId: String,
        currentUserId: String,
        onResult: (likeCount: Int, likedByCurrentUser: Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (ownerUserId.isBlank() || findingId.isBlank() || currentUserId.isBlank()) {
            onResult(0, false)
            return
        }

        findingLikesCollection(ownerUserId, findingId)
            .get()
            .addOnSuccessListener { snapshot ->
                val likeCount = snapshot.size()
                val likedByCurrentUser = snapshot.documents.any { it.id == currentUserId }
                onResult(likeCount, likedByCurrentUser)
            }
            .addOnFailureListener { exception ->
                val wrappedException = toFirestoreException(
                    functionName = "loadLikeInfoForFinding",
                    operation = "READ",
                    path = "users/$ownerUserId/findings/$findingId/likes",
                    exception = exception
                )
                Log.e(
                    TAG,
                    wrappedException.message ?: "Failed to load like info",
                    wrappedException
                )
                onError(wrappedException)
            }
    }

    fun toggleLikeForFinding(
        ownerUserId: String,
        findingId: String,
        currentUserId: String,
        currentDisplayName: String?,
        currentlyLiked: Boolean,
        onResult: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (ownerUserId.isBlank() || findingId.isBlank() || currentUserId.isBlank()) {
            onError(IllegalArgumentException("Ungültige Like-Daten"))
            return
        }

        val likeDocument = findingLikesCollection(ownerUserId, findingId).document(currentUserId)
        if (currentlyLiked) {
            likeDocument.delete()
                .addOnSuccessListener { onResult(false) }
                .addOnFailureListener { exception ->
                    val wrappedException = toFirestoreException(
                        functionName = "toggleLikeForFinding",
                        operation = "DELETE",
                        path = "users/$ownerUserId/findings/$findingId/likes/$currentUserId",
                        exception = exception
                    )
                    Log.e(
                        TAG,
                        wrappedException.message ?: "Failed to unlike finding",
                        wrappedException
                    )
                    onError(wrappedException)
                }
        } else {
            val likeData = hashMapOf(
                "likerUid" to currentUserId,
                "createdAt" to FieldValue.serverTimestamp(),
                "likerDisplayName" to currentDisplayName.orEmpty().trim()
            )
            likeDocument.set(likeData)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { exception ->
                    val wrappedException = toFirestoreException(
                        functionName = "toggleLikeForFinding",
                        operation = "WRITE",
                        path = "users/$ownerUserId/findings/$findingId/likes/$currentUserId",
                        exception = exception
                    )
                    Log.e(
                        TAG,
                        wrappedException.message ?: "Failed to like finding",
                        wrappedException
                    )
                    onError(wrappedException)
                }
        }
    }

    fun loadCommentsForFinding(
        ownerUserId: String,
        findingId: String,
        onResult: (List<FriendFindingComment>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (ownerUserId.isBlank() || findingId.isBlank()) {
            onResult(emptyList())
            return
        }

        findingCommentsCollection(ownerUserId, findingId)
            .get()
            .addOnSuccessListener { snapshot ->
                val comments = snapshot.documents.map { document ->
                    FriendFindingComment(
                        commentId = document.id,
                        commenterUid = document.getString("commenterUid").orEmpty(),
                        commenterDisplayName = document.getString("commenterDisplayName").orEmpty(),
                        text = document.getString("text").orEmpty(),
                        createdAt = document.getTimestamp("createdAt")
                    )
                }.filter { it.text.isNotBlank() }
                    .sortedBy { it.createdAt?.seconds ?: Long.MIN_VALUE }
                onResult(comments)
            }
            .addOnFailureListener { exception ->
                val wrappedException = toFirestoreException(
                    functionName = "loadCommentsForFinding",
                    operation = "READ",
                    path = "users/$ownerUserId/findings/$findingId/comments",
                    exception = exception
                )
                Log.e(
                    TAG,
                    wrappedException.message ?: "Failed to load comments",
                    wrappedException
                )
                onError(wrappedException)
            }
    }

    fun addCommentToFinding(
        ownerUserId: String,
        findingId: String,
        currentUserId: String,
        currentDisplayName: String?,
        text: String,
        onResult: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val trimmedText = text.trim()
        if (ownerUserId.isBlank() || findingId.isBlank() || currentUserId.isBlank()) {
            onError(IllegalArgumentException("Ungültige Kommentar-Daten"))
            return
        }
        if (trimmedText.isBlank()) {
            onResult(false)
            return
        }

        val commentData = hashMapOf(
            "commenterUid" to currentUserId,
            "commenterDisplayName" to currentDisplayName.orEmpty().trim(),
            "text" to trimmedText,
            "createdAt" to FieldValue.serverTimestamp()
        )

        findingCommentsCollection(ownerUserId, findingId)
            .document()
            .set(commentData)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { exception ->
                val wrappedException = toFirestoreException(
                    functionName = "addCommentToFinding",
                    operation = "WRITE",
                    path = "users/$ownerUserId/findings/$findingId/comments",
                    exception = exception
                )
                Log.e(
                    TAG,
                    wrappedException.message ?: "Failed to add comment",
                    wrappedException
                )
                onError(wrappedException)
            }
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
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "ensureUserProfile",
                    operation = "WRITE",
                    path = "users/$userId",
                    exception = exception
                )
                Log.e(
                    TAG,
                    errorMessage,
                    exception
                )
                onResult(false, errorMessage)
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
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "loadUserProfile",
                    operation = "READ",
                    path = "users/$userId",
                    exception = exception
                )
                Log.e(
                    TAG,
                    errorMessage,
                    exception
                )
                onError(errorMessage)
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
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "searchUsersByDisplayName",
                    operation = "READ",
                    path = "users (orderBy searchDisplayName)",
                    exception = exception
                )
                Log.e(
                    TAG,
                    errorMessage,
                    exception
                )
                onError(errorMessage)
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

        val outgoingRef = userDocument(currentUserId)
            .collection("friendRequestsOutgoing")
            .document(targetUserId)
        val incomingRef = userDocument(targetUserId)
            .collection("friendRequestsIncoming")
            .document(currentUserId)
        val currentUserFriendRef = userDocument(currentUserId)
            .collection("friends")
            .document(targetUserId)

        outgoingRef.get()
            .addOnSuccessListener { outgoingDocument ->
                if (outgoingDocument.exists()) {
                    onResult(false, "Anfrage wurde bereits gesendet")
                    return@addOnSuccessListener
                }

                currentUserFriendRef.get()
                    .addOnSuccessListener { friendDocument ->
                        if (friendDocument.exists()) {
                            onResult(false, "Ihr seid bereits befreundet")
                            return@addOnSuccessListener
                        }

                        val requestData = hashMapOf(
                            "fromUserId" to currentUserId,
                            "toUserId" to targetUserId,
                            "status" to STATUS_PENDING,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        val batch = firestore.batch()
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
                    .addOnFailureListener { exception ->
                        Log.e(
                            TAG,
                            "Failed to check friend status: ${exception.message ?: "Unbekannter Fehler"}",
                            exception
                        )
                        onResult(false, exception.message)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to check outgoing request: ${exception.message ?: "Unbekannter Fehler"}",
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
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "loadFriends",
                    operation = "READ",
                    path = "users/$currentUserId/friends",
                    exception = exception
                )
                Log.e(
                    TAG,
                    errorMessage,
                    exception
                )
                onError(errorMessage)
            }
    }

    fun loadOutgoingFriendRequestIds(
        currentUserId: String,
        onResult: (Set<String>) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        if (currentUserId.isBlank()) {
            onResult(emptySet())
            return
        }

        userDocument(currentUserId)
            .collection("friendRequestsOutgoing")
            .whereEqualTo("status", STATUS_PENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(
                    snapshot.documents.map { document ->
                        document.getString("toUserId").orEmpty().ifBlank { document.id }
                    }.toSet()
                )
            }
            .addOnFailureListener { exception ->
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "loadOutgoingFriendRequestIds",
                    operation = "READ",
                    path = "users/$currentUserId/friendRequestsOutgoing",
                    exception = exception
                )
                Log.e(
                    TAG,
                    errorMessage,
                    exception
                )
                onError(errorMessage)
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
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "loadIncomingFriendRequests",
                    operation = "READ",
                    path = "users/$currentUserId/friendRequestsIncoming",
                    exception = exception
                )
                Log.e(
                    TAG,
                    errorMessage,
                    exception
                )
                onError(errorMessage)
            }
    }

    fun declineFriendRequest(
        currentUserId: String,
        requesterUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (currentUserId.isBlank() || requesterUserId.isBlank()) {
            onResult(false, "Ungültige Nutzer-ID")
            return
        }

        val incomingRef = userDocument(currentUserId)
            .collection("friendRequestsIncoming")
            .document(requesterUserId)
        val outgoingRef = userDocument(requesterUserId)
            .collection("friendRequestsOutgoing")
            .document(currentUserId)

        val batch = firestore.batch()
        batch.delete(incomingRef)
        batch.delete(outgoingRef)
        batch.commit()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to decline friend request: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onResult(false, exception.message)
            }
    }

    fun cancelFriendRequest(
        currentUserId: String,
        targetUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (currentUserId.isBlank() || targetUserId.isBlank()) {
            onResult(false, "Ungültige Nutzer-ID")
            return
        }

        val outgoingRef = userDocument(currentUserId)
            .collection("friendRequestsOutgoing")
            .document(targetUserId)
        val incomingRef = userDocument(targetUserId)
            .collection("friendRequestsIncoming")
            .document(currentUserId)

        val batch = firestore.batch()
        batch.delete(outgoingRef)
        batch.delete(incomingRef)
        batch.commit()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to cancel friend request: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onResult(false, exception.message)
            }
    }

    fun removeFriend(
        currentUserId: String,
        friendUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (currentUserId.isBlank() || friendUserId.isBlank()) {
            onResult(false, "Ungültige Nutzer-ID")
            return
        }

        val currentUserFriendRef = userDocument(currentUserId)
            .collection("friends")
            .document(friendUserId)
        val otherUserFriendRef = userDocument(friendUserId)
            .collection("friends")
            .document(currentUserId)

        val batch = firestore.batch()
        batch.delete(currentUserFriendRef)
        batch.delete(otherUserFriendRef)
        batch.commit()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to remove friend: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onResult(false, exception.message)
            }
    }

    fun loadFriendsFeed(
        currentUserId: String,
        onResult: (List<FriendFeedItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUserId.isBlank()) {
            onResult(emptyList())
            return
        }

        userDocument(currentUserId)
            .collection("friends")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val feedItems = mutableListOf<FriendFeedItem>()
                var remaining = snapshot.documents.size
                var firstError: Exception? = null

                fun finishIfReady() {
                    remaining -= 1
                    if (remaining == 0) {
                        firstError?.let { onError(it) }
                        onResult(
                            feedItems.sortedByDescending {
                                parseFindingDateMillis(it.finding.date)
                            }
                        )
                    }
                }

                snapshot.documents.forEach { friendDocument ->
                    val friendUserId = friendDocument.getString("friendUserId").orEmpty()
                        .ifBlank { friendDocument.id }

                    loadUserProfile(
                        userId = friendUserId,
                        onResult = { profile ->
                            val friendDisplayName = profile?.displayName.orEmpty()
                            userDocument(friendUserId)
                                .collection("findings")
                                .get()
                                .addOnSuccessListener { findingsSnapshot ->
                                    if (findingsSnapshot.isEmpty) {
                                        finishIfReady()
                                        return@addOnSuccessListener
                                    }

                                    var pendingFindingLikes = findingsSnapshot.documents.size
                                    fun finishFriendLoad() {
                                        pendingFindingLikes -= 1
                                        if (pendingFindingLikes == 0) {
                                            finishIfReady()
                                        }
                                    }

                                    findingsSnapshot.documents.forEach { findingDocument ->
                                        val finding = AnimalFinding(
                                            animalId = findingDocument.getString("animalId").orEmpty(),
                                            date = findingDocument.getString("date").orEmpty(),
                                            location = findingDocument.getString("location").orEmpty(),
                                            note = findingDocument.getString("note").orEmpty(),
                                            photoUri = findingDocument.getString("photoUri").orEmpty(),
                                            latitude = findingDocument.getDouble("latitude"),
                                            longitude = findingDocument.getDouble("longitude"),
                                            locationSource = findingDocument.getString("locationSource"),
                                            ownerId = friendUserId
                                        )
                                        loadLikeInfoForFinding(
                                            ownerUserId = friendUserId,
                                            findingId = findingDocument.id,
                                            currentUserId = currentUserId,
                                            onResult = { likeCount, likedByCurrentUser ->
                                                feedItems += FriendFeedItem(
                                                    friendUserId = friendUserId,
                                                    friendDisplayName = friendDisplayName,
                                                    findingId = findingDocument.id,
                                                    finding = finding,
                                                    likeCount = likeCount,
                                                    likedByCurrentUser = likedByCurrentUser
                                                )
                                                finishFriendLoad()
                                            },
                                            onError = { exception ->
                                                if (firstError == null) {
                                                    firstError = exception
                                                }
                                                feedItems += FriendFeedItem(
                                                    friendUserId = friendUserId,
                                                    friendDisplayName = friendDisplayName,
                                                    findingId = findingDocument.id,
                                                    finding = finding
                                                )
                                                finishFriendLoad()
                                            }
                                        )
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        TAG,
                                        "Failed to load friend findings: ${exception.message ?: "Unbekannter Fehler"}",
                                        exception
                                    )
                                    if (firstError == null) {
                                        firstError = toFirestoreException(
                                            functionName = "loadFriendsFeed",
                                            operation = "READ",
                                            path = "users/$friendUserId/findings",
                                            exception = exception
                                        )
                                    }
                                    finishIfReady()
                                }
                        },
                        onError = { error ->
                            if (firstError == null) {
                                firstError = Exception(error ?: "Freundesprofil konnte nicht geladen werden")
                            }
                            finishIfReady()
                        }
                    )
                }
            }
            .addOnFailureListener { exception ->
                val wrappedException = toFirestoreException(
                    functionName = "loadFriendsFeed",
                    operation = "READ",
                    path = "users/$currentUserId/friends",
                    exception = exception
                )
                Log.e(
                    TAG,
                    wrappedException.message ?: "Failed to load friends feed",
                    wrappedException
                )
                onError(wrappedException)
            }
    }

    fun loadFriendFindingsForAnimal(
        currentUserId: String,
        animalId: String,
        onResult: (List<FriendFeedItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUserId.isBlank() || animalId.isBlank()) {
            onResult(emptyList())
            return
        }

        userDocument(currentUserId)
            .collection("friends")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val friendFindings = mutableListOf<FriendFeedItem>()
                var remaining = snapshot.documents.size
                var firstError: Exception? = null

                fun finishIfReady() {
                    remaining -= 1
                    if (remaining == 0) {
                        firstError?.let { onError(it) }
                        onResult(
                            friendFindings.sortedByDescending {
                                parseFindingDateMillis(it.finding.date)
                            }
                        )
                    }
                }

                snapshot.documents.forEach { friendDocument ->
                    val friendUserId = friendDocument.getString("friendUserId").orEmpty()
                        .ifBlank { friendDocument.id }

                    loadUserProfile(
                        userId = friendUserId,
                        onResult = { profile ->
                            val friendDisplayName = profile?.displayName.orEmpty()
                            userDocument(friendUserId)
                                .collection("findings")
                                .whereEqualTo("animalId", animalId)
                                .get()
                                .addOnSuccessListener { findingsSnapshot ->
                                    if (findingsSnapshot.isEmpty) {
                                        finishIfReady()
                                        return@addOnSuccessListener
                                    }

                                    var pendingFindingLikes = findingsSnapshot.documents.size
                                    fun finishFriendLoad() {
                                        pendingFindingLikes -= 1
                                        if (pendingFindingLikes == 0) {
                                            finishIfReady()
                                        }
                                    }

                                    findingsSnapshot.documents.forEach { findingDocument ->
                                        val finding = AnimalFinding(
                                            animalId = findingDocument.getString("animalId").orEmpty(),
                                            date = findingDocument.getString("date").orEmpty(),
                                            location = findingDocument.getString("location").orEmpty(),
                                            note = findingDocument.getString("note").orEmpty(),
                                            photoUri = findingDocument.getString("photoUri").orEmpty(),
                                            latitude = findingDocument.getDouble("latitude"),
                                            longitude = findingDocument.getDouble("longitude"),
                                            locationSource = findingDocument.getString("locationSource"),
                                            ownerId = friendUserId
                                        )
                                        loadLikeInfoForFinding(
                                            ownerUserId = friendUserId,
                                            findingId = findingDocument.id,
                                            currentUserId = currentUserId,
                                            onResult = { likeCount, likedByCurrentUser ->
                                                friendFindings += FriendFeedItem(
                                                    friendUserId = friendUserId,
                                                    friendDisplayName = friendDisplayName,
                                                    findingId = findingDocument.id,
                                                    finding = finding,
                                                    likeCount = likeCount,
                                                    likedByCurrentUser = likedByCurrentUser
                                                )
                                                finishFriendLoad()
                                            },
                                            onError = { exception ->
                                                if (firstError == null) {
                                                    firstError = exception
                                                }
                                                friendFindings += FriendFeedItem(
                                                    friendUserId = friendUserId,
                                                    friendDisplayName = friendDisplayName,
                                                    findingId = findingDocument.id,
                                                    finding = finding
                                                )
                                                finishFriendLoad()
                                            }
                                        )
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        TAG,
                                        "Failed to load friend findings for animal: ${exception.message ?: "Unbekannter Fehler"}",
                                        exception
                                    )
                                    if (firstError == null) {
                                        firstError = exception
                                    }
                                    finishIfReady()
                                }
                        },
                        onError = { error ->
                            if (firstError == null) {
                                firstError = Exception(error ?: "Freundesprofil konnte nicht geladen werden")
                            }
                            finishIfReady()
                        }
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load friend findings for animal: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                onError(exception)
            }
    }
}
