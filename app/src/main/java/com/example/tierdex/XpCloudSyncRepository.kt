package com.example.tierdex

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions

data class XpCloudState(
    val totalXp: Int,
    val awardedXpKeys: Set<String>,
    val backfillV1Done: Boolean,
    val updatedAt: Timestamp? = null,
    val schemaVersion: Int = 1
)

object XpCloudSyncRepository {
    private const val TAG = "XpCloudSyncRepository"
    private const val XP_BACKFILL_V1_DONE_KEY_PREFIX = "xp_backfill_v1_done_"
    private const val XP_CLOUD_SCHEMA_VERSION = 1

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun xpBackfillV1DoneKey(ownerId: String): String = "$XP_BACKFILL_V1_DONE_KEY_PREFIX$ownerId"

    fun buildLocalXpState(
        uid: String,
        prefs: SharedPreferences
    ): XpCloudState {
        val cleanUid = uid.trim()
        val resolvedOwnerId = XpProgressRepository.resolveOwnerId(cleanUid)
        val awardedXpKeys = XpProgressRepository.loadAwardedXpKeys(prefs, cleanUid)
        return XpCloudState(
            totalXp = XpProgressRepository.recalculateTotalXpFromAwardedKeys(awardedXpKeys),
            awardedXpKeys = awardedXpKeys,
            backfillV1Done = prefs.getBoolean(xpBackfillV1DoneKey(resolvedOwnerId), false),
            updatedAt = null,
            schemaVersion = XP_CLOUD_SCHEMA_VERSION
        )
    }

    // Firestore needs a concrete document to read/write, so the requested private path is used
    // as a nested base with one fixed state document at the end.
    private fun xpStateDocument(userId: String) = firestore.collection("users")
        .document(userId)
        .collection("private")
        .document("meta")
        .collection("xpState")
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

    fun loadXpState(
        uid: String,
        onResult: (XpCloudState?) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        val cleanUid = uid.trim()
        if (cleanUid.isBlank()) {
            onResult(null)
            return
        }

        xpStateDocument(cleanUid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.d(TAG, "loadXpState userId=$cleanUid exists=false")
                    onResult(null)
                    return@addOnSuccessListener
                }

                val totalXp = document.getLong("totalXp")?.toInt()?.coerceAtLeast(0) ?: 0
                val awardedXpKeys = (document.get("awardedXpKeys") as? List<*>)
                    .orEmpty()
                    .mapNotNull { value -> (value as? String)?.trim() }
                    .filter { it.isNotBlank() }
                    .toSet()
                val backfillV1Done = document.getBoolean("backfillV1Done") ?: false
                val schemaVersion = document.getLong("schemaVersion")?.toInt() ?: XP_CLOUD_SCHEMA_VERSION

                Log.d(
                    TAG,
                    "loadXpState userId=$cleanUid exists=true totalXp=$totalXp awardedKeyCount=${awardedXpKeys.size} backfillV1Done=$backfillV1Done schemaVersion=$schemaVersion"
                )

                onResult(
                    XpCloudState(
                        totalXp = totalXp,
                        awardedXpKeys = awardedXpKeys,
                        backfillV1Done = backfillV1Done,
                        updatedAt = document.getTimestamp("updatedAt"),
                        schemaVersion = schemaVersion
                    )
                )
            }
            .addOnFailureListener { exception ->
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "loadXpState",
                    operation = "READ",
                    path = "users/$cleanUid/private/meta/xpState/state",
                    exception = exception
                )
                Log.e(TAG, errorMessage, exception)
                onError(errorMessage)
            }
    }

    fun saveXpState(
        uid: String,
        state: XpCloudState,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val cleanUid = uid.trim()
        if (cleanUid.isBlank()) {
            onResult(false, "Leere userId")
            return
        }

        val normalizedAwardedXpKeys = state.awardedXpKeys
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        val authoritativeTotalXp =
            XpProgressRepository.recalculateTotalXpFromAwardedKeys(normalizedAwardedXpKeys)
        if (state.totalXp != authoritativeTotalXp) {
            Log.w(
                TAG,
                "saveXpState totalXp mismatch userId=$cleanUid providedTotalXp=${state.totalXp} authoritativeTotalXp=$authoritativeTotalXp"
            )
        }

        val data = hashMapOf<String, Any>(
            "totalXp" to authoritativeTotalXp,
            "awardedXpKeys" to normalizedAwardedXpKeys,
            "backfillV1Done" to state.backfillV1Done,
            "schemaVersion" to XP_CLOUD_SCHEMA_VERSION,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        xpStateDocument(cleanUid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(
                    TAG,
                    "saveXpState success userId=$cleanUid totalXp=$authoritativeTotalXp awardedKeyCount=${normalizedAwardedXpKeys.size} backfillV1Done=${state.backfillV1Done} path=users/$cleanUid/private/meta/xpState/state"
                )
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                val errorMessage = toFirestoreErrorMessage(
                    functionName = "saveXpState",
                    operation = "WRITE",
                    path = "users/$cleanUid/private/meta/xpState/state",
                    exception = exception
                )
                Log.e(TAG, errorMessage, exception)
                onResult(false, errorMessage)
            }
    }

    fun mergeLocalAndCloudXpState(
        uid: String,
        prefs: SharedPreferences,
        onResult: (XpCloudState?) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        val cleanUid = uid.trim()
        if (cleanUid.isBlank()) {
            onResult(null)
            return
        }

        val resolvedOwnerId = XpProgressRepository.resolveOwnerId(cleanUid)

        loadXpState(
            uid = cleanUid,
            onResult = { cloudState ->
                val localState = buildLocalXpState(cleanUid, prefs)
                Log.d(
                    TAG,
                    "merge start userId=$cleanUid localTotalXp=${localState.totalXp} localAwardedKeyCount=${localState.awardedXpKeys.size} localBackfillV1Done=${localState.backfillV1Done} cloudTotalXp=${cloudState?.totalXp ?: 0} cloudAwardedKeyCount=${cloudState?.awardedXpKeys?.size ?: 0} cloudBackfillV1Done=${cloudState?.backfillV1Done ?: false}"
                )
                val mergedAwardedKeys = (localState.awardedXpKeys + (cloudState?.awardedXpKeys ?: emptySet()))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toSet()
                val mergedBackfillV1Done = localState.backfillV1Done || (cloudState?.backfillV1Done == true)
                val mergedTotalXp = XpProgressRepository.recalculateTotalXpFromAwardedKeys(mergedAwardedKeys)
                val mergedState = XpCloudState(
                    totalXp = mergedTotalXp,
                    awardedXpKeys = mergedAwardedKeys,
                    backfillV1Done = mergedBackfillV1Done,
                    updatedAt = cloudState?.updatedAt,
                    schemaVersion = XP_CLOUD_SCHEMA_VERSION
                )
                Log.d(
                    TAG,
                    "merge result userId=$cleanUid mergedTotalXp=${mergedState.totalXp} mergedAwardedKeyCount=${mergedState.awardedXpKeys.size} mergedBackfillV1Done=${mergedState.backfillV1Done}"
                )

                XpProgressRepository.storeAwardedXpKeys(
                    prefs = prefs,
                    userId = cleanUid,
                    awardedXpKeys = mergedState.awardedXpKeys
                )
                XpProgressRepository.storeTotalXp(
                    prefs = prefs,
                    userId = cleanUid,
                    totalXp = mergedState.totalXp
                )
                prefs.edit()
                    .putBoolean(xpBackfillV1DoneKey(resolvedOwnerId), mergedState.backfillV1Done)
                    .apply()

                saveXpState(
                    uid = cleanUid,
                    state = mergedState
                ) { success, errorMessage ->
                    if (success) {
                        onResult(mergedState)
                    } else {
                        onError(errorMessage)
                    }
                }
            },
            onError = onError
        )
    }
}
