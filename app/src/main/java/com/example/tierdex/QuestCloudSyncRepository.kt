package com.example.tierdex

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions

data class QuestCloudState(
    val socialLikesGivenCount: Int,
    val socialCommentsWrittenCount: Int,
    val socialLikeQuestFindingKeys: Set<String>,
    val socialCommentQuestFindingKeys: Set<String>,
    val dailyAnimalQuestHitFindingIds: Set<String>,
    val updatedAt: Timestamp? = null,
    val schemaVersion: Int = 1
)

object QuestCloudSyncRepository {
    private const val TAG = "QuestCloudSyncRepository"
    private const val QUEST_CLOUD_SCHEMA_VERSION = 1
    private const val MAX_SOCIAL_LIKE_QUEST_FINDING_KEYS = 5000
    private const val MAX_SOCIAL_COMMENT_QUEST_FINDING_KEYS = 5000

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun questStateDocument(userId: String) = firestore.collection("users")
        .document(userId)
        .collection("private")
        .document("meta")
        .collection("questState")
        .document("state")

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

    fun loadQuestState(
        uid: String,
        onResult: (QuestCloudState?) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        val cleanUid = uid.trim()
        if (cleanUid.isBlank()) {
            onResult(null)
            return
        }

        questStateDocument(cleanUid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.d(TAG, "loadQuestState userId=$cleanUid exists=false")
                    onResult(null)
                    return@addOnSuccessListener
                }

                val socialLikesGivenCount =
                    document.getLong("socialLikesGivenCount")?.toInt()?.coerceAtLeast(0) ?: 0
                val socialCommentsWrittenCount =
                    document.getLong("socialCommentsWrittenCount")?.toInt()?.coerceAtLeast(0) ?: 0
                val socialLikeQuestFindingKeys =
                    (document.get("socialLikeQuestFindingKeys") as? List<*>)
                        .orEmpty()
                        .mapNotNull { value -> (value as? String)?.trim() }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                        .take(MAX_SOCIAL_LIKE_QUEST_FINDING_KEYS)
                        .toSet()
                val socialCommentQuestFindingKeys =
                    (document.get("socialCommentQuestFindingKeys") as? List<*>)
                        .orEmpty()
                        .mapNotNull { value -> (value as? String)?.trim() }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                        .take(MAX_SOCIAL_COMMENT_QUEST_FINDING_KEYS)
                        .toSet()
                val dailyAnimalQuestHitFindingIds =
                    (document.get("dailyAnimalQuestHitFindingIds") as? List<*>)
                        .orEmpty()
                        .mapNotNull { value -> (value as? String)?.trim() }
                        .filter { it.isNotBlank() }
                        .toSet()
                val schemaVersion =
                    document.getLong("schemaVersion")?.toInt() ?: QUEST_CLOUD_SCHEMA_VERSION

                Log.d(
                    TAG,
                    "loadQuestState userId=$cleanUid exists=true likes=$socialLikesGivenCount comments=$socialCommentsWrittenCount likeQuestFindingKeyCount=${socialLikeQuestFindingKeys.size} commentQuestFindingKeyCount=${socialCommentQuestFindingKeys.size} hitFindingIdCount=${dailyAnimalQuestHitFindingIds.size} schemaVersion=$schemaVersion"
                )

                onResult(
                    QuestCloudState(
                        socialLikesGivenCount = socialLikesGivenCount,
                        socialCommentsWrittenCount = socialCommentsWrittenCount,
                        socialLikeQuestFindingKeys = socialLikeQuestFindingKeys,
                        socialCommentQuestFindingKeys = socialCommentQuestFindingKeys,
                        dailyAnimalQuestHitFindingIds = dailyAnimalQuestHitFindingIds,
                        updatedAt = document.getTimestamp("updatedAt"),
                        schemaVersion = schemaVersion
                    )
                )
            }
            .addOnFailureListener { exception ->
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "loadQuestState",
                    operation = "READ",
                    path = "users/$cleanUid/private/meta/questState/state",
                    exception = exception
                )
                Log.e(TAG, errorMessage, exception)
                onError(errorMessage)
            }
    }

    fun saveQuestState(
        uid: String,
        state: QuestCloudState,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val cleanUid = uid.trim()
        if (cleanUid.isBlank()) {
            onResult(false, "Leere userId")
            return
        }

        val data = hashMapOf<String, Any>(
            "socialLikesGivenCount" to state.socialLikesGivenCount.coerceAtLeast(0),
            "socialCommentsWrittenCount" to state.socialCommentsWrittenCount.coerceAtLeast(0),
            "socialLikeQuestFindingKeys" to state.socialLikeQuestFindingKeys
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
                .take(MAX_SOCIAL_LIKE_QUEST_FINDING_KEYS),
            "socialCommentQuestFindingKeys" to state.socialCommentQuestFindingKeys
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
                .take(MAX_SOCIAL_COMMENT_QUEST_FINDING_KEYS),
            "dailyAnimalQuestHitFindingIds" to state.dailyAnimalQuestHitFindingIds
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted(),
            "schemaVersion" to QUEST_CLOUD_SCHEMA_VERSION,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        questStateDocument(cleanUid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(
                    TAG,
                    "saveQuestState success userId=$cleanUid likes=${state.socialLikesGivenCount} comments=${state.socialCommentsWrittenCount} likeQuestFindingKeyCount=${state.socialLikeQuestFindingKeys.size} commentQuestFindingKeyCount=${state.socialCommentQuestFindingKeys.size} hitFindingIdCount=${state.dailyAnimalQuestHitFindingIds.size} path=users/$cleanUid/private/meta/questState/state"
                )
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "saveQuestState",
                    operation = "WRITE",
                    path = "users/$cleanUid/private/meta/questState/state",
                    exception = exception
                )
                Log.e(TAG, errorMessage, exception)
                onResult(false, errorMessage)
            }
    }
}
