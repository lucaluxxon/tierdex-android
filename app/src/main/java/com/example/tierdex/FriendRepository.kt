package com.example.tierdex

import android.util.Log
import android.os.SystemClock
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
    val bio: String = "",
    val profilePhotoPath: String = "",
    val wishAnimalId: String = "",
    val favoriteAnimalId: String = "",
    val updatedAt: Timestamp? = null
)

data class FriendUser(
    val userId: String,
    val displayName: String,
    val searchDisplayName: String,
    val profilePhotoPath: String = "",
    val wishAnimalId: String = "",
    val favoriteAnimalId: String = "",
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
    val friendProfilePhotoPath: String = "",
    val findingId: String,
    val finding: AnimalFinding,
    val likeCount: Int = 0,
    val likedByCurrentUser: Boolean = false,
    val commentCount: Int = 0
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

    fun parseFindingDateMillisForCache(dateText: String): Long = parseFindingDateMillis(dateText)

    fun sortFriendFeedItems(items: List<FriendFeedItem>): List<FriendFeedItem> {
        val sortedItems = items.sortedWith(
            compareByDescending<FriendFeedItem> {
                parseFindingDateMillis(it.finding.date)
            }
                .thenBy { it.friendUserId }
                .thenBy { it.findingId }
        )
        val firstSortMillis = sortedItems.firstOrNull()?.let { item ->
            parseFindingDateMillis(item.finding.date)
        } ?: Long.MIN_VALUE
        Log.d(
            "FriendFeedTiming",
            "cloud items sorted count=${sortedItems.size} firstItemSortMillis=$firstSortMillis firstItemDateParsed=${firstSortMillis != Long.MIN_VALUE}"
        )
        return sortedItems
    }

    fun sortFriendFeedCacheEntities(items: List<FriendFeedCacheEntity>): List<FriendFeedCacheEntity> {
        val sortedItems = items.sortedWith(
            compareByDescending<FriendFeedCacheEntity> {
                if (it.sortDateMillis != Long.MIN_VALUE) it.sortDateMillis else it.cachedAtMillis
            }
                .thenBy { it.ownerUserId }
                .thenBy { it.findingId }
        )
        val firstSortMillis = sortedItems.firstOrNull()?.let { item ->
            if (item.sortDateMillis != Long.MIN_VALUE) item.sortDateMillis else item.cachedAtMillis
        } ?: Long.MIN_VALUE
        Log.d(
            "FriendFeedCache",
            "cache items sorted count=${sortedItems.size} firstItemSortMillis=$firstSortMillis firstItemDateParsed=${sortedItems.firstOrNull()?.sortDateMillis != Long.MIN_VALUE}"
        )
        return sortedItems
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

    fun loadCommentCountForFinding(
        ownerUserId: String,
        findingId: String,
        onResult: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (ownerUserId.isBlank() || findingId.isBlank()) {
            onResult(0)
            return
        }

        findingCommentsCollection(ownerUserId, findingId)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.documents.count { !it.getString("text").isNullOrBlank() })
            }
            .addOnFailureListener { exception ->
                val wrappedException = toFirestoreException(
                    functionName = "loadCommentCountForFinding",
                    operation = "READ",
                    path = "users/$ownerUserId/findings/$findingId/comments",
                    exception = exception
                )
                Log.e(
                    TAG,
                    wrappedException.message ?: "Failed to load comment count",
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

    fun updatePublicUserProfile(
        userId: String,
        displayName: String? = null,
        bio: String? = null,
        profilePhotoPath: String? = null,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (userId.isBlank()) {
            onResult(false, "Leere userId")
            return
        }

        val profileData = hashMapOf<String, Any>(
            "updatedAt" to FieldValue.serverTimestamp()
        )

        displayName?.let { rawDisplayName ->
            val safeDisplayName = rawDisplayName.trim()
            profileData["displayName"] = safeDisplayName
            profileData["searchDisplayName"] = normalizeDisplayName(safeDisplayName)
        }

        bio?.let { rawBio ->
            profileData["bio"] = rawBio.trim().take(300)
        }

        profilePhotoPath?.let { rawProfilePhotoPath ->
            profileData["profilePhotoPath"] = rawProfilePhotoPath.trim()
        }

        firestore.collection("users")
            .document(userId)
            .set(profileData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "updatePublicUserProfile",
                    operation = "WRITE",
                    path = "users/$userId",
                    exception = exception
                )
                Log.e(TAG, errorMessage, exception)
                onResult(false, errorMessage)
            }
    }

    fun updatePublicProfileAnimalPreferences(
        userId: String,
        wishAnimalId: String,
        favoriteAnimalId: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (userId.isBlank()) {
            onResult(false, "Leere userId")
            return
        }

        val profileData = hashMapOf<String, Any>(
            "wishAnimalId" to wishAnimalId.trim(),
            "favoriteAnimalId" to favoriteAnimalId.trim(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(userId)
            .set(profileData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "updatePublicProfileAnimalPreferences",
                    operation = "WRITE",
                    path = "users/$userId",
                    exception = exception
                )
                Log.e(TAG, errorMessage, exception)
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
                        bio = document.getString("bio").orEmpty(),
                        profilePhotoPath = document.getString("profilePhotoPath").orEmpty(),
                        wishAnimalId = document.getString("wishAnimalId").orEmpty(),
                        favoriteAnimalId = document.getString("favoriteAnimalId").orEmpty(),
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
                            bio = document.getString("bio").orEmpty(),
                            profilePhotoPath = document.getString("profilePhotoPath").orEmpty(),
                            wishAnimalId = document.getString("wishAnimalId").orEmpty(),
                            favoriteAnimalId = document.getString("favoriteAnimalId").orEmpty(),
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
                                    profilePhotoPath = it.profilePhotoPath,
                                    wishAnimalId = it.wishAnimalId,
                                    favoriteAnimalId = it.favoriteAnimalId,
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
        val feedStartedAt = SystemClock.elapsedRealtime()
        Log.d("FriendFeedTiming", "loadFriendsFeed start currentUserIdPresent=${currentUserId.isNotBlank()}")
        if (currentUserId.isBlank()) {
            Log.d("FriendFeedTiming", "loadFriendsFeed end reason=blankUserId durationMs=${SystemClock.elapsedRealtime() - feedStartedAt}")
            onResult(emptyList())
            return
        }

        val friendsReadStartedAt = SystemClock.elapsedRealtime()
        userDocument(currentUserId)
            .collection("friends")
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d(
                    "FriendFeedTiming",
                    "friends read end durationMs=${SystemClock.elapsedRealtime() - friendsReadStartedAt} friendCount=${snapshot.documents.size}"
                )
                if (snapshot.isEmpty) {
                    Log.d(
                        "FriendFeedTiming",
                        "loadFriendsFeed end friendCount=0 itemCount=0 totalDurationMs=${SystemClock.elapsedRealtime() - feedStartedAt}"
                    )
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val feedItems = mutableListOf<FriendFeedItem>()
                var remaining = snapshot.documents.size
                var firstError: Exception? = null

                fun finishIfReady() {
                    remaining -= 1
                    if (remaining == 0) {
                        val sortedFeedItems = sortFriendFeedItems(feedItems)
                        Log.d(
                            "FriendFeedTiming",
                            "loadFriendsFeed end friendCount=${snapshot.documents.size} itemCount=${sortedFeedItems.size} totalDurationMs=${SystemClock.elapsedRealtime() - feedStartedAt}"
                        )
                        firstError?.let { onError(it) }
                        onResult(sortedFeedItems)
                    }
                }

                snapshot.documents.forEach { friendDocument ->
                    val friendUserId = friendDocument.getString("friendUserId").orEmpty()
                        .ifBlank { friendDocument.id }
                    val safeFriendId = friendUserId.takeLast(6)
                    val profileLoadStartedAt = SystemClock.elapsedRealtime()

                    loadUserProfile(
                        userId = friendUserId,
                        onResult = { profile ->
                            Log.d(
                                "FriendFeedTiming",
                                "friend profile loaded friend=*${safeFriendId} durationMs=${SystemClock.elapsedRealtime() - profileLoadStartedAt}"
                            )
                            val friendDisplayName = profile?.displayName.orEmpty()
                            val friendProfilePhotoPath = profile?.profilePhotoPath.orEmpty()
                            val findingsLoadStartedAt = SystemClock.elapsedRealtime()
                            userDocument(friendUserId)
                                .collection("findings")
                                .get()
                                .addOnSuccessListener { findingsSnapshot ->
                                    Log.d(
                                        "FriendFeedTiming",
                                        "friend findings loaded friend=*${safeFriendId} durationMs=${SystemClock.elapsedRealtime() - findingsLoadStartedAt} findingCount=${findingsSnapshot.documents.size}"
                                    )
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
                                        val thumbnailRemotePhotoPath =
                                            findingDocument.getString("thumbnailRemotePhotoPath").orEmpty()
                                        val hasRemotePhotoPaths =
                                            findingDocument.getPhotoValuesOrEmpty("remotePhotoPaths").isNotEmpty() ||
                                                !findingDocument.getString("remotePhotoPath").isNullOrBlank()
                                        val finding = AnimalFinding(
                                            animalId = findingDocument.getString("animalId").orEmpty(),
                                            date = findingDocument.getString("date").orEmpty(),
                                            location = findingDocument.getString("location").orEmpty(),
                                            note = findingDocument.getString("note").orEmpty(),
                                            photoUri = findingDocument.getString("photoUri").orEmpty(),
                                            remotePhotoPath = findingDocument.getString("remotePhotoPath").orEmpty(),
                                            thumbnailRemotePhotoPath = thumbnailRemotePhotoPath,
                                            photoUris = findingDocument.getPhotoValuesOrEmpty("photoUris"),
                                            remotePhotoPaths = findingDocument.getPhotoValuesOrEmpty("remotePhotoPaths"),
                                            latitude = findingDocument.getDouble("latitude"),
                                            longitude = findingDocument.getDouble("longitude"),
                                            locationSource = findingDocument.getString("locationSource"),
                                            ownerId = friendUserId,
                                            taggedFriendIds = findingDocument.getTaggedFriendIdsOrEmpty()
                                        )
                                        Log.d(
                                            "FriendFeedTiming",
                                            "finding meta friend=*${safeFriendId} hasThumbnailRemotePhotoPath=${thumbnailRemotePhotoPath.isNotBlank()} hasRemotePhotoPaths=$hasRemotePhotoPaths remotePhotoPathCount=${effectiveRemotePhotoPaths(finding).size}"
                                        )
                                        val likeLoadStartedAt = SystemClock.elapsedRealtime()
                                        loadLikeInfoForFinding(
                                            ownerUserId = friendUserId,
                                            findingId = findingDocument.id,
                                            currentUserId = currentUserId,
                                            onResult = { likeCount, likedByCurrentUser ->
                                                Log.d(
                                                    "FriendFeedTiming",
                                                    "like info loaded friend=*${safeFriendId} durationMs=${SystemClock.elapsedRealtime() - likeLoadStartedAt}"
                                                )
                                                val commentCountStartedAt = SystemClock.elapsedRealtime()
                                                loadCommentCountForFinding(
                                                    ownerUserId = friendUserId,
                                                    findingId = findingDocument.id,
                                                    onResult = { commentCount ->
                                                        Log.d(
                                                            "FriendFeedTiming",
                                                            "comment count loaded friend=*${safeFriendId} durationMs=${SystemClock.elapsedRealtime() - commentCountStartedAt}"
                                                        )
                                                        feedItems += FriendFeedItem(
                                                            friendUserId = friendUserId,
                                                            friendDisplayName = friendDisplayName,
                                                            friendProfilePhotoPath = friendProfilePhotoPath,
                                                            findingId = findingDocument.id,
                                                            finding = finding,
                                                            likeCount = likeCount,
                                                            likedByCurrentUser = likedByCurrentUser,
                                                            commentCount = commentCount
                                                        )
                                                        finishFriendLoad()
                                                    },
                                                    onError = { exception ->
                                                        Log.w(
                                                            "FriendFeedTiming",
                                                            "comment count failed friend=*${safeFriendId} durationMs=${SystemClock.elapsedRealtime() - commentCountStartedAt} error=${exception.message ?: "Unbekannter Fehler"}"
                                                        )
                                                        if (firstError == null) {
                                                            firstError = exception
                                                        }
                                                        feedItems += FriendFeedItem(
                                                            friendUserId = friendUserId,
                                                            friendDisplayName = friendDisplayName,
                                                            friendProfilePhotoPath = friendProfilePhotoPath,
                                                            findingId = findingDocument.id,
                                                            finding = finding,
                                                            likeCount = likeCount,
                                                            likedByCurrentUser = likedByCurrentUser
                                                        )
                                                        finishFriendLoad()
                                                    }
                                                )
                                            },
                                            onError = { exception ->
                                                Log.w(
                                                    "FriendFeedTiming",
                                                    "like info failed friend=*${safeFriendId} durationMs=${SystemClock.elapsedRealtime() - likeLoadStartedAt} error=${exception.message ?: "Unbekannter Fehler"}"
                                                )
                                                if (firstError == null) {
                                                    firstError = exception
                                                }
                                                val commentCountStartedAt = SystemClock.elapsedRealtime()
                                                loadCommentCountForFinding(
                                                    ownerUserId = friendUserId,
                                                    findingId = findingDocument.id,
                                                    onResult = { commentCount ->
                                                        Log.d(
                                                            "FriendFeedTiming",
                                                            "comment count loaded after like failure friend=*${safeFriendId} durationMs=${SystemClock.elapsedRealtime() - commentCountStartedAt}"
                                                        )
                                                        feedItems += FriendFeedItem(
                                                            friendUserId = friendUserId,
                                                            friendDisplayName = friendDisplayName,
                                                            friendProfilePhotoPath = friendProfilePhotoPath,
                                                            findingId = findingDocument.id,
                                                            finding = finding,
                                                            commentCount = commentCount
                                                        )
                                                        finishFriendLoad()
                                                    },
                                                    onError = {
                                                        Log.w(
                                                            "FriendFeedTiming",
                                                            "comment count failed after like failure friend=*${safeFriendId} durationMs=${SystemClock.elapsedRealtime() - commentCountStartedAt}"
                                                        )
                                                        feedItems += FriendFeedItem(
                                                            friendUserId = friendUserId,
                                                            friendDisplayName = friendDisplayName,
                                                            friendProfilePhotoPath = friendProfilePhotoPath,
                                                            findingId = findingDocument.id,
                                                            finding = finding
                                                        )
                                                        finishFriendLoad()
                                                    }
                                                )
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
                            Log.w(
                                "FriendFeedTiming",
                                "friend profile failed friend=*${safeFriendId} durationMs=${SystemClock.elapsedRealtime() - profileLoadStartedAt} error=${error ?: "Unbekannter Fehler"}"
                            )
                            if (firstError == null) {
                                firstError = Exception(error ?: "Freundesprofil konnte nicht geladen werden")
                            }
                            finishIfReady()
                        }
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w(
                    "FriendFeedTiming",
                    "friends read failed durationMs=${SystemClock.elapsedRealtime() - friendsReadStartedAt} error=${exception.message ?: "Unbekannter Fehler"}"
                )
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
                                            remotePhotoPath = findingDocument.getString("remotePhotoPath").orEmpty(),
                                            thumbnailRemotePhotoPath = findingDocument.getString("thumbnailRemotePhotoPath").orEmpty(),
                                            photoUris = findingDocument.getPhotoValuesOrEmpty("photoUris"),
                                            remotePhotoPaths = findingDocument.getPhotoValuesOrEmpty("remotePhotoPaths"),
                                            latitude = findingDocument.getDouble("latitude"),
                                            longitude = findingDocument.getDouble("longitude"),
                                            locationSource = findingDocument.getString("locationSource"),
                                            ownerId = friendUserId,
                                            taggedFriendIds = findingDocument.getTaggedFriendIdsOrEmpty()
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
