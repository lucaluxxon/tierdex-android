package com.example.tierdex

import android.content.SharedPreferences
import android.util.Log
import kotlin.math.roundToInt

private const val XP_TOTAL_KEY_PREFIX = "xp_total_"
private const val XP_AWARDED_KEYS_KEY_PREFIX = "xp_awarded_keys_"
private const val LOCAL_XP_OWNER_ID = "local"
private const val MAX_TIERDEX_LEVEL = 60

data class XpProgressSnapshot(
    val ownerId: String,
    val totalXp: Int,
    val awardedXpKeys: Set<String>,
    val level: Int,
    val title: String,
    val currentLevelStartXp: Int,
    val nextLevelStartXp: Int?,
    val xpIntoCurrentLevel: Int,
    val xpNeededForNextLevel: Int,
    val progressWithinLevel: Float
)

data class XpAwardGrantResult(
    val awardedXp: Int,
    val grantedKeys: Set<String>,
    val totalXpAfterGrant: Int
)

object XpProgressRepository {
    private const val TAG = "XpProgressRepository"
    private val xpStepAnchors = sortedMapOf(
        1 to 50,
        2 to 75,
        3 to 100,
        4 to 125,
        5 to 150,
        10 to 400,
        20 to 1000,
        30 to 1800,
        40 to 2800
    )

    private val levelTitles = sortedMapOf(
        1 to "Natur-Neuling",
        5 to "Spurenleser",
        10 to "Feldforscher",
        15 to "Artenkenner",
        20 to "Tierkundiger",
        25 to "Naturbeobachter",
        30 to "Wildnisforscher",
        35 to "Artenexperte",
        40 to "Tierdex-Meister",
        45 to "Natur-Chronist",
        50 to "Wandelndes Tier-Lexikon",
        55 to "Tier-König",
        60 to "Legende des Tierdex"
    )

    private fun parseTrailingPositiveInt(awardKey: String): Int? {
        return awardKey.substringAfterLast(':', missingDelimiterValue = "")
            .trim()
            .toIntOrNull()
            ?.takeIf { it > 0 }
    }

    // Keeps XP mapping centralized so future local/cloud consistency checks can rebuild totals
    // from the same stable award keys that already prevent double grants today.
    fun xpForAwardKey(awardKey: String): Int {
        val cleanAwardKey = awardKey.trim()
        if (cleanAwardKey.isBlank()) return 0

        return when {
            cleanAwardKey.startsWith("daily_login:") -> 2
            cleanAwardKey.startsWith("social_like:") -> 2
            cleanAwardKey.startsWith("social_comment:") -> 2

            cleanAwardKey.startsWith("finding_base:") -> when (parseTrailingPositiveInt(cleanAwardKey)) {
                1 -> 10
                2 -> 5
                3 -> 3
                else -> 0
            }

            cleanAwardKey.startsWith("finding_photo:") ||
                cleanAwardKey.startsWith("finding_location:") -> when (parseTrailingPositiveInt(cleanAwardKey)) {
                1, 2, 3 -> 3
                else -> 0
            }

            cleanAwardKey.startsWith("total_findings:") ||
                cleanAwardKey.startsWith("photo_findings:") ||
                cleanAwardKey.startsWith("location_findings:") ||
                cleanAwardKey.startsWith("total_species_entries:") ||
                cleanAwardKey.startsWith("group_species_") ||
                cleanAwardKey.startsWith("social_friend_tagged_findings:") ->
                parseTrailingPositiveInt(cleanAwardKey)?.times(10) ?: 0

            cleanAwardKey.startsWith("daily_animal:") ||
                cleanAwardKey.startsWith("social_friends:") ->
                parseTrailingPositiveInt(cleanAwardKey)?.times(20) ?: 0

            cleanAwardKey.startsWith("social_likes_given:") ->
                parseTrailingPositiveInt(cleanAwardKey)?.times(5) ?: 0

            cleanAwardKey.startsWith("social_comments_written:") ->
                parseTrailingPositiveInt(cleanAwardKey)?.times(10) ?: 0

            cleanAwardKey.startsWith("special_perfect_finding:") ->
                parseTrailingPositiveInt(cleanAwardKey)?.times(250) ?: 0

            cleanAwardKey.startsWith("special_single_subgroup_species:") ->
                parseTrailingPositiveInt(cleanAwardKey)?.times(50) ?: 0

            cleanAwardKey == "special_alphabet_species:all" -> 500

            cleanAwardKey.startsWith("special_alphabet_species:") ->
                parseTrailingPositiveInt(cleanAwardKey)?.times(10) ?: 0

            cleanAwardKey.startsWith("special_photo_upgrade:") ->
                parseTrailingPositiveInt(cleanAwardKey)?.times(25) ?: 0

            cleanAwardKey == "special_wish_animal_found:1" -> 100

            else -> 0
        }
    }

    fun recalculateTotalXpFromAwardedKeys(keys: Collection<String>): Int {
        return keys
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()
            .sumOf(::xpForAwardKey)
            .coerceAtLeast(0)
    }

    fun resolveOwnerId(userId: String?): String {
        return userId?.trim()?.takeIf { it.isNotBlank() } ?: LOCAL_XP_OWNER_ID
    }

    fun totalXpKey(userId: String?): String = "$XP_TOTAL_KEY_PREFIX${resolveOwnerId(userId)}"

    fun awardedXpKeysKey(userId: String?): String = "$XP_AWARDED_KEYS_KEY_PREFIX${resolveOwnerId(userId)}"

    fun loadTotalXp(
        prefs: SharedPreferences,
        userId: String?
    ): Int {
        return prefs.getInt(totalXpKey(userId), 0).coerceAtLeast(0)
    }

    fun storeTotalXp(
        prefs: SharedPreferences,
        userId: String?,
        totalXp: Int
    ) {
        prefs.edit()
            .putInt(totalXpKey(userId), totalXp.coerceAtLeast(0))
            .apply()
    }

    fun loadAwardedXpKeys(
        prefs: SharedPreferences,
        userId: String?
    ): Set<String> {
        return prefs.getStringSet(awardedXpKeysKey(userId), emptySet())
            ?.toSet()
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()
    }

    fun storeAwardedXpKeys(
        prefs: SharedPreferences,
        userId: String?,
        awardedXpKeys: Set<String>
    ) {
        prefs.edit()
            .putStringSet(
                awardedXpKeysKey(userId),
                awardedXpKeys.map { it.trim() }.filter { it.isNotBlank() }.toSet()
            )
            .apply()
    }

    fun hasAwardedXpKey(
        prefs: SharedPreferences,
        userId: String?,
        awardKey: String
    ): Boolean {
        return awardKey.trim().takeIf { it.isNotBlank() } in loadAwardedXpKeys(prefs, userId)
    }

    fun addAwardedXpKey(
        prefs: SharedPreferences,
        userId: String?,
        awardKey: String
    ) {
        val cleanAwardKey = awardKey.trim()
        if (cleanAwardKey.isBlank()) return
        storeAwardedXpKeys(
            prefs = prefs,
            userId = userId,
            awardedXpKeys = loadAwardedXpKeys(prefs, userId) + cleanAwardKey
        )
    }

    fun grantXpAwardsIfAbsent(
        prefs: SharedPreferences,
        userId: String?,
        awards: Collection<Pair<String, Int>>
    ): XpAwardGrantResult {
        val existingAwardedKeys = loadAwardedXpKeys(prefs, userId).toMutableSet()
        val cleanAwards = awards
            .mapNotNull { (awardKey, xpValue) ->
                val cleanAwardKey = awardKey.trim()
                if (cleanAwardKey.isBlank()) {
                    null
                } else {
                    val authoritativeXp = xpForAwardKey(cleanAwardKey)
                    when {
                        authoritativeXp > 0 -> {
                            if (xpValue != authoritativeXp) {
                                Log.w(
                                    TAG,
                                    "grantXpAwardsIfAbsent xp mismatch awardKeyPresent=true providedXp=$xpValue authoritativeXp=$authoritativeXp"
                                )
                            }
                            cleanAwardKey to authoritativeXp
                        }

                        else -> {
                            Log.w(
                                TAG,
                                "grantXpAwardsIfAbsent skipped unknown or zero-xp awardKeyPresent=true providedXp=$xpValue"
                            )
                            null
                        }
                    }
                }
            }
            .distinctBy { it.first }

        if (cleanAwards.isEmpty()) {
            return XpAwardGrantResult(
                awardedXp = 0,
                grantedKeys = emptySet(),
                totalXpAfterGrant = reconcileStoredTotalXp(prefs, userId)
            )
        }

        val grantedKeys = mutableSetOf<String>()

        cleanAwards.forEach { (awardKey, _) ->
            if (awardKey !in existingAwardedKeys) {
                existingAwardedKeys += awardKey
                grantedKeys += awardKey
            }
        }

        val updatedTotalXp = recalculateTotalXpFromAwardedKeys(existingAwardedKeys)

        if (grantedKeys.isNotEmpty()) {
            prefs.edit()
                .putInt(totalXpKey(userId), updatedTotalXp.coerceAtLeast(0))
                .putStringSet(awardedXpKeysKey(userId), existingAwardedKeys)
                .apply()
        }

        return XpAwardGrantResult(
            awardedXp = cleanAwards
                .filter { (awardKey, _) -> awardKey in grantedKeys }
                .sumOf { it.second },
            grantedKeys = grantedKeys,
            totalXpAfterGrant = updatedTotalXp.coerceAtLeast(0)
        )
    }

    fun buildSnapshot(
        prefs: SharedPreferences,
        userId: String?
    ): XpProgressSnapshot {
        val resolvedOwnerId = resolveOwnerId(userId)
        val awardedXpKeys = loadAwardedXpKeys(prefs, userId)
        val totalXp = reconcileStoredTotalXp(
            prefs = prefs,
            userId = userId,
            awardedXpKeys = awardedXpKeys
        )
        val level = calculateLevelFromXp(totalXp)
        val currentLevelStartXp = xpRequiredToReachLevel(level)
        val nextLevelStartXp = nextLevelStartXp(totalXp)
        val xpIntoCurrentLevel = (totalXp - currentLevelStartXp).coerceAtLeast(0)
        val xpNeededForNextLevel = nextLevelStartXp?.let { (it - totalXp).coerceAtLeast(0) } ?: 0
        val progressWithinLevel = if (nextLevelStartXp != null && nextLevelStartXp > currentLevelStartXp) {
            (xpIntoCurrentLevel.toFloat() / (nextLevelStartXp - currentLevelStartXp).toFloat()).coerceIn(0f, 1f)
        } else {
            1f
        }

        return XpProgressSnapshot(
            ownerId = resolvedOwnerId,
            totalXp = totalXp,
            awardedXpKeys = awardedXpKeys,
            level = level,
            title = titleForLevel(level),
            currentLevelStartXp = currentLevelStartXp,
            nextLevelStartXp = nextLevelStartXp,
            xpIntoCurrentLevel = xpIntoCurrentLevel,
            xpNeededForNextLevel = xpNeededForNextLevel,
            progressWithinLevel = progressWithinLevel
        )
    }

    private fun reconcileStoredTotalXp(
        prefs: SharedPreferences,
        userId: String?,
        awardedXpKeys: Set<String> = loadAwardedXpKeys(prefs, userId)
    ): Int {
        val authoritativeTotalXp = recalculateTotalXpFromAwardedKeys(awardedXpKeys)
        val storedTotalXp = loadTotalXp(prefs, userId)
        if (storedTotalXp != authoritativeTotalXp) {
            Log.w(
                TAG,
                "Reconciled stored totalXp for ownerId=${resolveOwnerId(userId)} stored=$storedTotalXp authoritative=$authoritativeTotalXp"
            )
            storeTotalXp(
                prefs = prefs,
                userId = userId,
                totalXp = authoritativeTotalXp
            )
        }
        return authoritativeTotalXp
    }

    fun calculateLevelFromXp(totalXp: Int): Int {
        var remainingXp = totalXp.coerceAtLeast(0)
        for (level in 1 until MAX_TIERDEX_LEVEL) {
            val requiredXp = xpNeededForNextLevel(level)
            if (remainingXp < requiredXp) {
                return level
            }
            remainingXp -= requiredXp
        }
        return MAX_TIERDEX_LEVEL
    }

    fun xpNeededForNextLevel(level: Int): Int {
        val clampedLevel = level.coerceIn(1, MAX_TIERDEX_LEVEL - 1)
        xpStepAnchors[clampedLevel]?.let { return it }

        val lowerAnchor = xpStepAnchors.entries.lastOrNull { it.key < clampedLevel }
        val upperAnchor = xpStepAnchors.entries.firstOrNull { it.key > clampedLevel }

        if (lowerAnchor != null && upperAnchor != null) {
            val fraction = (clampedLevel - lowerAnchor.key).toFloat() / (upperAnchor.key - lowerAnchor.key).toFloat()
            val interpolated = lowerAnchor.value + ((upperAnchor.value - lowerAnchor.value) * fraction)
            return interpolated.roundToInt()
        }

        val anchorEntries = xpStepAnchors.entries.toList()
        val lastAnchor = anchorEntries.last()
        val previousAnchor = anchorEntries[anchorEntries.lastIndex - 1]
        val slopePerLevel = (lastAnchor.value - previousAnchor.value).toFloat() / (lastAnchor.key - previousAnchor.key).toFloat()
        val extrapolated = lastAnchor.value + ((clampedLevel - lastAnchor.key) * slopePerLevel)
        return extrapolated.roundToInt()
    }

    fun xpRequiredToReachLevel(level: Int): Int {
        val clampedLevel = level.coerceIn(1, MAX_TIERDEX_LEVEL)
        var total = 0
        for (currentLevel in 1 until clampedLevel) {
            total += xpNeededForNextLevel(currentLevel)
        }
        return total
    }

    fun nextLevelStartXp(totalXp: Int): Int? {
        val level = calculateLevelFromXp(totalXp)
        return if (level >= MAX_TIERDEX_LEVEL) {
            null
        } else {
            xpRequiredToReachLevel(level + 1)
        }
    }

    fun levelProgressFraction(totalXp: Int): Float {
        val level = calculateLevelFromXp(totalXp)
        if (level >= MAX_TIERDEX_LEVEL) return 1f

        val currentLevelStartXp = xpRequiredToReachLevel(level)
        val nextLevelStartXp = xpRequiredToReachLevel(level + 1)
        val xpIntoCurrentLevel = (totalXp.coerceAtLeast(0) - currentLevelStartXp).coerceAtLeast(0)
        return (xpIntoCurrentLevel.toFloat() / (nextLevelStartXp - currentLevelStartXp).toFloat()).coerceIn(0f, 1f)
    }

    fun titleForLevel(level: Int): String {
        return levelTitles.entries.lastOrNull { level >= it.key }?.value ?: levelTitles.getValue(1)
    }
}
