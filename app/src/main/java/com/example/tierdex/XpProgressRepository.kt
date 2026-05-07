package com.example.tierdex

import android.content.SharedPreferences
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

object XpProgressRepository {
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

    fun buildSnapshot(
        prefs: SharedPreferences,
        userId: String?
    ): XpProgressSnapshot {
        val resolvedOwnerId = resolveOwnerId(userId)
        val totalXp = loadTotalXp(prefs, userId)
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
            awardedXpKeys = loadAwardedXpKeys(prefs, userId),
            level = level,
            title = titleForLevel(level),
            currentLevelStartXp = currentLevelStartXp,
            nextLevelStartXp = nextLevelStartXp,
            xpIntoCurrentLevel = xpIntoCurrentLevel,
            xpNeededForNextLevel = xpNeededForNextLevel,
            progressWithinLevel = progressWithinLevel
        )
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
