package com.example.tierdex

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.LruCache
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.tierdex.ui.theme.AppBackground
import com.example.tierdex.ui.theme.PrimaryGreen
import com.example.tierdex.ui.theme.PrimaryGreenSoft
import com.example.tierdex.ui.theme.TextPrimary
import com.example.tierdex.ui.theme.TextSecondary
import com.example.tierdex.ui.theme.CardBackground
import com.example.tierdex.ui.theme.BorderColor
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.lightColorScheme
import com.example.tierdex.ui.theme.TierdexTheme
import androidx.room.Room
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.clustering.ClusterItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore


private const val ANIMALS_JSON_FILE_NAME = "animals.json"
private const val ANIMALS_CSV_FILE_NAME = "tierlistegesamt.csv"
private const val FINDING_IMAGES_DIR = "finding_images"
private const val STARTUP_HINT_SHOWN_KEY_PREFIX = "startup_hint_shown_"
private const val INTRO_PENDING_KEY_PREFIX = "intro_pending_"
private const val INTRO_SEEN_KEY_PREFIX = "intro_seen_"
private const val HAS_USED_AUTH_BEFORE_KEY = "has_used_auth_before"
private const val WISHLIST_ANIMAL_KEY_PREFIX = "wishAnimalId_"
private const val FAVORITE_ANIMAL_KEY_PREFIX = "favoriteAnimalId_"
private const val PROFILE_BIO_KEY_PREFIX = "profileBio_"
private const val PROFILE_IMAGE_KEY_PREFIX = "profileImage_"
private const val PROFILE_BACKGROUND_IMAGE_KEY_PREFIX = "profileBackgroundImage_"
private const val NOTIFICATION_READ_IDS_KEY_PREFIX = "notification_read_ids_"

private fun defaultAuthEntryMode(prefs: android.content.SharedPreferences): String =
    if (prefs.getBoolean(HAS_USED_AUTH_BEFORE_KEY, false)) "login" else "register"
private const val DAILY_ANIMAL_DATE_KEY_PREFIX = "daily_animal_date_"
private const val DAILY_ANIMAL_ID_KEY_PREFIX = "daily_animal_id_"
private const val DAILY_ANIMAL_DISMISSED_KEY_PREFIX = "daily_animal_dismissed_"
private const val DAILY_ANIMAL_HISTORY_KEY_PREFIX = "daily_animal_history_"
private const val DAILY_ANIMAL_HISTORY_RECORDED_DATE_KEY_PREFIX = "daily_animal_history_recorded_date_"
private const val LOCAL_PREFERENCES_OWNER_ID = "local"
private val AppGreenBackground = Color(0xFF51734A)

private enum class ProfileCollectionSortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

private enum class ProfileCollectionDateFilter {
    ALL,
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    THIS_YEAR
}

private fun favoriteAnimalKey(ownerId: String): String = "$FAVORITE_ANIMAL_KEY_PREFIX$ownerId"

private fun wishlistAnimalKey(ownerId: String): String = "$WISHLIST_ANIMAL_KEY_PREFIX$ownerId"
private fun profileBioKey(ownerId: String): String = "$PROFILE_BIO_KEY_PREFIX$ownerId"
private fun profileImageKey(ownerId: String): String = "$PROFILE_IMAGE_KEY_PREFIX$ownerId"
private fun profileBackgroundImageKey(ownerId: String): String =
    "$PROFILE_BACKGROUND_IMAGE_KEY_PREFIX$ownerId"
private fun notificationReadIdsKey(ownerId: String): String = "$NOTIFICATION_READ_IDS_KEY_PREFIX$ownerId"
private fun dailyAnimalDateKey(ownerId: String): String = "$DAILY_ANIMAL_DATE_KEY_PREFIX$ownerId"
private fun dailyAnimalIdKey(ownerId: String): String = "$DAILY_ANIMAL_ID_KEY_PREFIX$ownerId"
private fun dailyAnimalDismissedKey(ownerId: String): String = "$DAILY_ANIMAL_DISMISSED_KEY_PREFIX$ownerId"
private fun dailyAnimalHistoryKey(ownerId: String): String = "$DAILY_ANIMAL_HISTORY_KEY_PREFIX$ownerId"
private fun dailyAnimalHistoryRecordedDateKey(ownerId: String): String =
    "$DAILY_ANIMAL_HISTORY_RECORDED_DATE_KEY_PREFIX$ownerId"

private fun introPendingKey(ownerId: String): String = "$INTRO_PENDING_KEY_PREFIX$ownerId"

private fun introSeenKey(ownerId: String): String = "$INTRO_SEEN_KEY_PREFIX$ownerId"

private fun currentAppDateText(): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

private fun currentDailyDateKey(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private data class DailyAnimalHistoryEntry(
    val count: Int = 0,
    val lastDate: String = ""
)

private fun loadDailyAnimalHistory(
    prefs: android.content.SharedPreferences,
    ownerId: String
): Map<String, DailyAnimalHistoryEntry> {
    val historyJson = prefs.getString(dailyAnimalHistoryKey(ownerId), null).orEmpty().trim()
    if (historyJson.isBlank()) return emptyMap()

    return runCatching {
        val rootObject = Gson().fromJson(historyJson, JsonObject::class.java) ?: JsonObject()
        rootObject.entrySet().associate { (animalId, jsonElement) ->
            val entryObject = jsonElement?.asJsonObject
            val count = entryObject?.get("count")?.asInt ?: 0
            val lastDate = entryObject?.get("lastDate")?.asString.orEmpty()
            animalId to DailyAnimalHistoryEntry(
                count = count.coerceAtLeast(0),
                lastDate = lastDate
            )
        }
    }.getOrDefault(emptyMap())
}

private fun saveDailyAnimalHistory(
    prefs: android.content.SharedPreferences,
    ownerId: String,
    history: Map<String, DailyAnimalHistoryEntry>
) {
    val historyJson = JsonObject().apply {
        history.toSortedMap().forEach { (animalId, entry) ->
            add(animalId, JsonObject().apply {
                addProperty("count", entry.count.coerceAtLeast(0))
                addProperty("lastDate", entry.lastDate)
            })
        }
    }.toString()

    prefs.edit().putString(dailyAnimalHistoryKey(ownerId), historyJson).apply()
}

private fun recordDailyAnimalHistoryIfNeeded(
    prefs: android.content.SharedPreferences,
    ownerId: String,
    animalId: String,
    todayKey: String
) {
    if (animalId.isBlank() || todayKey.isBlank()) return

    val recordedDateKey = dailyAnimalHistoryRecordedDateKey(ownerId)
    val alreadyRecordedDate = prefs.getString(recordedDateKey, null)
    if (alreadyRecordedDate == todayKey) return

    val history = loadDailyAnimalHistory(prefs, ownerId).toMutableMap()
    val existingEntry = history[animalId] ?: DailyAnimalHistoryEntry()
    history[animalId] = existingEntry.copy(
        count = existingEntry.count + 1,
        lastDate = todayKey
    )
    saveDailyAnimalHistory(prefs, ownerId, history)
    prefs.edit().putString(recordedDateKey, todayKey).apply()
}

private fun getDailyAnimalHistoryEntry(
    prefs: android.content.SharedPreferences,
    ownerId: String,
    animalId: String
): DailyAnimalHistoryEntry? {
    if (animalId.isBlank()) return null
    return loadDailyAnimalHistory(prefs, ownerId)[animalId]
}

private fun formatDailyAnimalHistoryDate(dateKey: String): String {
    val normalizedDateKey = dateKey.trim()
    if (normalizedDateKey.isBlank()) return ""

    return runCatching {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            isLenient = false
        }
        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
            isLenient = false
        }
        val parsedDate = inputFormat.parse(normalizedDateKey)
        if (parsedDate != null) outputFormat.format(parsedDate) else normalizedDateKey
    }.getOrDefault(normalizedDateKey)
}

private fun formatDailyAnimalHistoryText(
    entry: DailyAnimalHistoryEntry?,
    includeNeverText: Boolean = true
): String? {
    if (entry == null || entry.count <= 0) {
        return if (includeNeverText) "Tier des Tages: noch nie" else null
    }

    val formattedLastDate = formatDailyAnimalHistoryDate(entry.lastDate)
    return if (formattedLastDate.isBlank()) {
        "Tier des Tages: ${entry.count}x"
    } else {
        "Tier des Tages: ${entry.count}x, zuletzt: $formattedLastDate"
    }
}

private fun friendAnimalPreferenceLine(
    label: String,
    matchingFriends: List<FriendUser>
): String? {
    val resolvedNames = matchingFriends
        .map { it.displayName.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    return when {
        resolvedNames.isEmpty() -> null
        resolvedNames.size == 1 -> "$label von ${resolvedNames.first()}"
        resolvedNames.size == 2 -> "$label von ${resolvedNames[0]} und ${resolvedNames[1]}"
        else -> "$label von ${resolvedNames.size} Freunden"
    }
}

private fun parseFindingLocalDateOrNull(dateText: String): Date? {
    val normalizedDateText = dateText.trim()
    if (normalizedDateText.isBlank()) return null

    val supportedFormats = listOf("dd.MM.yyyy", "yyyy-MM-dd")
    return supportedFormats.firstNotNullOfOrNull { formatPattern ->
        runCatching {
            SimpleDateFormat(formatPattern, Locale.getDefault()).apply {
                isLenient = false
            }.parse(normalizedDateText)
        }.getOrNull()
    }
}

private fun calendarForDate(date: Date): Calendar =
    Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

private fun isSameCalendarDay(leftDate: Date, rightDate: Date): Boolean {
    val leftCalendar = calendarForDate(leftDate)
    val rightCalendar = calendarForDate(rightDate)
    return leftCalendar.get(Calendar.YEAR) == rightCalendar.get(Calendar.YEAR) &&
        leftCalendar.get(Calendar.DAY_OF_YEAR) == rightCalendar.get(Calendar.DAY_OF_YEAR)
}

private fun sortProfileFindings(
    findings: List<AnimalFinding>,
    sortOrder: ProfileCollectionSortOrder
): List<AnimalFinding> {
    return findings.withIndex()
        .sortedWith { left, right ->
            val leftDate = parseFindingLocalDateOrNull(left.value.date)
            val rightDate = parseFindingLocalDateOrNull(right.value.date)

            when {
                leftDate != null && rightDate != null -> {
                    val dateComparison = when (sortOrder) {
                        ProfileCollectionSortOrder.NEWEST_FIRST -> rightDate.time.compareTo(leftDate.time)
                        ProfileCollectionSortOrder.OLDEST_FIRST -> leftDate.time.compareTo(rightDate.time)
                    }
                    if (dateComparison != 0) {
                        dateComparison
                    } else {
                        val leftRoomId = left.value.roomId
                        val rightRoomId = right.value.roomId
                        if (leftRoomId != null && rightRoomId != null && leftRoomId != rightRoomId) {
                            when (sortOrder) {
                                ProfileCollectionSortOrder.NEWEST_FIRST -> rightRoomId.compareTo(leftRoomId)
                                ProfileCollectionSortOrder.OLDEST_FIRST -> leftRoomId.compareTo(rightRoomId)
                            }
                        } else {
                            left.index.compareTo(right.index)
                        }
                    }
                }

                leftDate != null -> -1
                rightDate != null -> 1
                else -> left.index.compareTo(right.index)
            }
        }
        .map { it.value }
}

private fun filterProfileFindings(
    findings: List<AnimalFinding>,
    dateFilter: ProfileCollectionDateFilter,
    now: Date = Date()
): List<AnimalFinding> {
    if (dateFilter == ProfileCollectionDateFilter.ALL) return findings

    val todayCalendar = calendarForDate(now)
    val todayStartMillis = todayCalendar.timeInMillis
    val sevenDaysStartMillis = Calendar.getInstance().apply {
        timeInMillis = todayStartMillis
        add(Calendar.DAY_OF_YEAR, -6)
    }.timeInMillis
    val thirtyDaysStartMillis = Calendar.getInstance().apply {
        timeInMillis = todayStartMillis
        add(Calendar.DAY_OF_YEAR, -29)
    }.timeInMillis
    val currentYear = todayCalendar.get(Calendar.YEAR)

    return findings.filter { finding ->
        val findingDate = parseFindingLocalDateOrNull(finding.date) ?: return@filter false
        val findingCalendar = calendarForDate(findingDate)
        val findingTimeMillis = findingCalendar.timeInMillis
        when (dateFilter) {
            ProfileCollectionDateFilter.ALL -> true
            ProfileCollectionDateFilter.TODAY -> isSameCalendarDay(findingDate, now)
            ProfileCollectionDateFilter.LAST_7_DAYS ->
                findingTimeMillis in sevenDaysStartMillis..todayStartMillis
            ProfileCollectionDateFilter.LAST_30_DAYS ->
                findingTimeMillis in thirtyDaysStartMillis..todayStartMillis
            ProfileCollectionDateFilter.THIS_YEAR ->
                findingCalendar.get(Calendar.YEAR) == currentYear
        }
    }
}

private fun formatCoordinates(
    latitude: Double,
    longitude: Double
): String = String.format(Locale.US, "%.6f, %.6f", latitude, longitude)

private data class FindingClusterItem(
    private val positionValue: LatLng,
    private val titleValue: String,
    private val snippetValue: String
) : ClusterItem {
    override fun getPosition(): LatLng = positionValue
    override fun getTitle(): String = titleValue
    override fun getSnippet(): String = snippetValue
    override fun getZIndex(): Float? = null
}

private val uriImageMemoryCache = object : LruCache<String, Bitmap>(20 * 1024 * 1024) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
}

private fun uriImageCacheKey(uriString: String, maxImageSizePx: Int?): String =
    "$uriString|${maxImageSizePx ?: -1}"


private var wishAnimalId by mutableStateOf<String?>(null)
private var favoriteAnimalId by mutableStateOf<String?>(null)

class MainActivity : ComponentActivity() {

    private val prefs by lazy {
        getSharedPreferences("tierdex_prefs", MODE_PRIVATE)
    }

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AnimalFindingDatabase::class.java,
            "animal_finding_database"
        )
            .addMigrations(AnimalFindingDatabase.MIGRATION_1_2)
            .addMigrations(AnimalFindingDatabase.MIGRATION_2_3)
            .addMigrations(AnimalFindingDatabase.MIGRATION_3_4)
            .addMigrations(AnimalFindingDatabase.MIGRATION_4_5)
            .addMigrations(AnimalFindingDatabase.MIGRATION_5_6)
            .addMigrations(AnimalFindingDatabase.MIGRATION_6_7)
            .addMigrations(AnimalFindingDatabase.MIGRATION_7_8)
            .build()
    }

    private var wishAnimalId by mutableStateOf<String?>(null)
    private var favoriteAnimalId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wishAnimalId = null
        favoriteAnimalId = null
        setContent {
            var showSplashScreen by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1600)
                showSplashScreen = false
            }

            TierdexTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    if (showSplashScreen) {
                        AppSplashScreen()
                    } else {
                        TierdexApp(database = database)
                    }
                }
            }
        }
    }
}


data class AnimalEntry(
    val id: String,
    val group: String,
    val subgroup: String,
    val germanName: String,
    val latinName: String,
    val habitat: String,
    val distribution: String,
    val rarity: String,
    val habitats: List<String> = emptyList(),
    val distributionGermany: String = "",
    val rarityGame: String = "",
    val redListGermany: String = "",
    val activity: String = "",
    val season: String = "",
    val protectionStatus: String = "",
    val shortDescription: String = "",
    val observationTip: String = "",
    val sources: List<String> = emptyList(),
    val needsReview: Boolean = true,
    val reviewNote: String = ""
)

data class AnimalFinding(
    val roomId: Int? = null,
    val animalId: String,
    val date: String,
    val location: String,
    val note: String,
    val photoUri: String = "",
    val remotePhotoPath: String = "",
    val thumbnailRemotePhotoPath: String = "",
    val photoUris: List<String> = emptyList(),
    val remotePhotoPaths: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationSource: String? = null,
    val ownerId: String? = null,
    val taggedFriendIds: List<String> = emptyList()
)

data class CsvLoadResult(
    val animals: List<AnimalEntry>,
    val debugMessage: String
)

data class TierdexNotification(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val createdAtText: String,
    val createdAt: Timestamp? = null,
    val isRead: Boolean,
    val relatedUserId: String? = null
)

private fun friendRequestNotificationId(request: FriendRequest): String =
    "friend_request_${request.fromUserId}_${request.toUserId}"

private fun formatNotificationTimestamp(timestamp: Timestamp?): String {
    return timestamp?.toDate()?.let {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it)
    }.orEmpty()
}

@Composable
private fun FindingMetaRow(
    date: String?,
    location: String?,
    latitude: Double? = null,
    longitude: Double? = null,
    modifier: Modifier = Modifier
) {
    var showLocationDialog by remember(latitude, longitude) { mutableStateOf(false) }
    val hasMapLocation = latitude != null && longitude != null
    val hasDate = !date.isNullOrBlank()
    val hasLocation = !location.isNullOrBlank()

    if (!hasDate && !hasLocation) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        date?.takeIf { it.isNotBlank() }?.let { dateText ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TextSecondary
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }

        location?.takeIf { it.isNotBlank() }?.let { locationText ->
            Row(
                modifier = if (hasMapLocation) {
                    Modifier.clickable { showLocationDialog = true }
                } else {
                    Modifier
                },
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (hasMapLocation) TextPrimary else TextSecondary
                )
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (hasMapLocation) TextPrimary else TextSecondary
                )
            }
        }
    }

    if (showLocationDialog && hasMapLocation) {
        FindingLocationDialog(
            latitude = latitude!!,
            longitude = longitude!!,
            locationLabel = location,
            onDismiss = { showLocationDialog = false }
        )
    }
}

private fun isCrossDeviceDisplayableLocalPhoto(
    photoUri: String,
    ownerUserId: String?,
    currentUserId: String?
): Boolean {
    val trimmedUri = photoUri.trim()
    if (trimmedUri.isBlank()) return false

    return ownerUserId == currentUserId ||
        trimmedUri.startsWith("http://", ignoreCase = true) ||
        trimmedUri.startsWith("https://", ignoreCase = true) ||
        trimmedUri.startsWith("android.resource://")
}

fun normalizePhotoList(
    photoValues: List<String>,
    fallbackPhotoValue: String = ""
): List<String> {
    val normalizedPhotos = photoValues
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    return (normalizedPhotos + listOf(fallbackPhotoValue.trim()))
        .filter { it.isNotBlank() }
        .distinct()
        .take(3)
}

fun effectiveLocalPhotoUris(finding: AnimalFinding): List<String> {
    return normalizePhotoList(
        photoValues = finding.photoUris,
        fallbackPhotoValue = finding.photoUri
    )
}

fun effectiveRemotePhotoPaths(finding: AnimalFinding): List<String> {
    return normalizePhotoList(
        photoValues = finding.remotePhotoPaths,
        fallbackPhotoValue = finding.remotePhotoPath
    )
}

fun effectiveOwnPhotoSources(finding: AnimalFinding): List<String> {
    val localPhotoUris = effectiveLocalPhotoUris(finding)
    val remotePhotoUris = effectiveRemotePhotoPaths(finding).map(::storageUriFromPath)
    return (localPhotoUris + remotePhotoUris)
        .filter { it.isNotBlank() }
        .distinct()
        .take(3)
}

fun effectiveFriendPhotoSources(
    finding: AnimalFinding,
    ownerUserId: String?,
    currentUserId: String?
): List<String> {
    val effectiveRemotePreviewUris = effectiveRemotePhotoPaths(finding).let { remotePaths ->
        val trimmedThumbnailPath = finding.thumbnailRemotePhotoPath.trim()
        if (trimmedThumbnailPath.isNotBlank()) {
            listOf(storageUriFromPath(trimmedThumbnailPath)) +
                remotePaths.drop(1).map(::storageUriFromPath)
        } else {
            remotePaths.map(::storageUriFromPath)
        }
    }
    val localPhotoUris = effectiveLocalPhotoUris(finding).filter { photoUri ->
        isCrossDeviceDisplayableLocalPhoto(
            photoUri = photoUri,
            ownerUserId = ownerUserId,
            currentUserId = currentUserId
        )
    }

    val resolvedSources = (effectiveRemotePreviewUris + localPhotoUris)
        .filter { it.isNotBlank() }
        .distinct()
        .take(3)
    val firstSourceIsThumbnail = resolvedSources.firstOrNull()?.contains(
        "thumb_photo",
        ignoreCase = true
    ) == true
    Log.d(
        "FriendFeedCache",
        "photo sources animalId=${finding.animalId} hasThumbnail=${finding.thumbnailRemotePhotoPath.trim().isNotBlank()} sourceCount=${resolvedSources.size} firstSourceIsThumbnail=$firstSourceIsThumbnail"
    )
    return resolvedSources
}

private fun hasAnyFindingPhoto(finding: AnimalFinding): Boolean {
    return effectiveOwnPhotoSources(finding).isNotEmpty()
}

private fun preferredOwnedFindingPhotoUri(finding: AnimalFinding): String? {
    return effectiveOwnPhotoSources(finding).firstOrNull()
}

private fun profileFindingPreviewPhotoUri(finding: AnimalFinding): String? {
    return effectiveLocalPhotoUris(finding).firstOrNull()
        ?: finding.thumbnailRemotePhotoPath
            .takeIf { it.isNotBlank() }
            ?.let(::storageUriFromPath)
        ?: effectiveRemotePhotoPaths(finding)
            .firstOrNull()
            ?.let(::storageUriFromPath)
}

private fun preferredFriendFindingPhotoUri(
    finding: AnimalFinding,
    ownerUserId: String?,
    currentUserId: String?
): String? {
    return effectiveFriendPhotoSources(
        finding = finding,
        ownerUserId = ownerUserId,
        currentUserId = currentUserId
    ).firstOrNull()
}

private fun taggedFriendsSummaryText(
    taggedFriendIds: List<String>,
    currentUserId: String?,
    ownerUserId: String,
    ownerDisplayName: String,
    friendNamesById: Map<String, String>
): String? {
    val normalizedIds = taggedFriendIds
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
    if (normalizedIds.isEmpty()) return null

    val resolvedNames = normalizedIds.mapNotNull { taggedId ->
        when {
            !currentUserId.isNullOrBlank() && taggedId == currentUserId -> "Du"
            taggedId == ownerUserId -> ownerDisplayName.ifBlank { null }
            else -> friendNamesById[taggedId]?.takeIf { it.isNotBlank() }
        }
    }.distinct()
    val unknownCount = (normalizedIds.size - resolvedNames.size).coerceAtLeast(0)

    return when {
        resolvedNames.isEmpty() -> "Mit Freunden gefunden (${normalizedIds.size})"
        unknownCount > 0 -> "Mit Freunden gefunden: ${resolvedNames.joinToString(", ")} + $unknownCount weitere"
        else -> "Mit Freunden gefunden: ${resolvedNames.joinToString(", ")}"
    }
}

@Composable
private fun FindingPhotoCounter(
    currentPage: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    showSingleCounter: Boolean = false
) {
    if (totalCount <= 0 || (totalCount == 1 && !showSingleCounter)) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Collections,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = TextSecondary
        )
        Text(
            text = "${currentPage.coerceIn(0, totalCount - 1) + 1}/$totalCount",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun FindingPhotoPager(
    photoSources: List<String>,
    imageModifier: Modifier,
    modifier: Modifier = Modifier,
    onPageChanged: ((Int) -> Unit)? = null
) {
    val normalizedPhotoSources = photoSources
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(3)

    if (normalizedPhotoSources.isEmpty()) return

    if (normalizedPhotoSources.size == 1) {
        LaunchedEffect(normalizedPhotoSources.first()) {
            onPageChanged?.invoke(0)
        }
        UriImage(
            uriString = normalizedPhotoSources.first(),
            maxImageSizePx = 1024,
            modifier = imageModifier
        )
        return
    }

    key(normalizedPhotoSources.joinToString(separator = "|")) {
        val pagerState = rememberPagerState(pageCount = { normalizedPhotoSources.size })

        LaunchedEffect(pagerState.currentPage, normalizedPhotoSources.size) {
            onPageChanged?.invoke(pagerState.currentPage)
        }

        HorizontalPager(
            state = pagerState,
            modifier = modifier.fillMaxWidth()
        ) { page ->
            UriImage(
                uriString = normalizedPhotoSources[page],
                maxImageSizePx = 1024,
                modifier = imageModifier
            )
        }
    }
}

@Composable
private fun FriendFindingPhotoBlock(
    photoSources: List<String>,
    hasPhoto: Boolean,
    modifier: Modifier = Modifier,
    onPageChanged: ((Int) -> Unit)? = null
) {
    if (!hasPhoto) return

    if (photoSources.isNotEmpty()) {
        FindingPhotoPager(
            photoSources = photoSources,
            imageModifier = modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(14.dp)),
            onPageChanged = onPageChanged
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PrimaryGreenSoft.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Collections,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = "Foto vorhanden",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Auf diesem Ger\u00e4t nicht direkt verf\u00fcgbar",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun FriendFindingEngagementSummary(
    likeCount: Int,
    commentCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = TextSecondary
            )
            Text(
                text = likeCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Comment,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = TextSecondary
            )
            Text(
                text = commentCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun FriendAvatar(
    displayName: String,
    modifier: Modifier = Modifier,
    profileImageUri: String? = null
) {
    val safeDisplayName = displayName.ifBlank { "Unbenannter Nutzer" }
    val initial = safeDisplayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    if (!profileImageUri.isNullOrBlank()) {
        UriImage(
            uriString = profileImageUri,
            maxImageSizePx = 256,
            modifier = modifier.clip(CircleShape)
        )
        return
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = PrimaryGreenSoft.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FriendIdentityRow(
    displayName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    profileImageUri: String? = null
) {
    val safeDisplayName = displayName.ifBlank { "Unbenannter Nutzer" }

    Row(
        modifier = modifier.clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FriendAvatar(
            displayName = safeDisplayName,
            profileImageUri = profileImageUri,
            modifier = Modifier.size(40.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = safeDisplayName,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = "Öffentliches Profil ansehen",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun FindingLocationDialog(
    latitude: Double,
    longitude: Double,
    locationLabel: String?,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    val markerPosition = remember(latitude, longitude) { LatLng(latitude, longitude) }
    val markerState = remember(markerPosition) { MarkerState(position = markerPosition) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 14f)
    }
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.36f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .fillMaxHeight(0.68f),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Fundort",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            locationLabel?.takeIf { it.isNotBlank() }?.let { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Karte schließen",
                                tint = TextPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = mapUiSettings
                        ) {
                            Marker(
                                state = markerState
                            )
                        }
                    }

                    Text(
                        text = formatCoordinates(latitude, longitude),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}


enum class AppTab {
    HOME,
    FRIENDS,
    STATS,
    PROFILE
}

private const val FINDING_NAV_SOURCE_ANIMAL_DETAIL = "ANIMAL_DETAIL"

private enum class IntroLaunchSource {
    AUTOMATIC,
    SETTINGS
}

@Composable
fun AppSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.tierdex01_playstore),
                contentDescription = "Tierdex Logo",
                modifier = Modifier.size(232.dp)
            )
            Text(
                text = "Tierdex",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierdexApp(database: AnimalFindingDatabase) {
    val dao = database.animalFindingDao()
    val friendFeedCacheDao = database.friendFeedCacheDao()
    val scope = rememberCoroutineScope()
    var currentOwnerId by rememberSaveable { mutableStateOf(AuthSession.currentUserId) }
    var currentDisplayName by rememberSaveable { mutableStateOf(AuthSession.getCurrentDisplayName()) }

    val ownerId = currentOwnerId
    val findingsFlow = if (ownerId == null) {
        dao.getAllGlobalFindings()
    } else {
        dao.getAllFindingsVisibleForOwner(ownerId)
    }
    val allFindings by findingsFlow.collectAsState(initial = emptyList())
    val findingsFromRoom = allFindings.map { it.toDomainFinding() }
    val context = LocalContext.current
    val prefs =
        context.getSharedPreferences("tierdex_prefs", android.content.Context.MODE_PRIVATE)
    val preferenceOwnerId = ownerId ?: LOCAL_PREFERENCES_OWNER_ID
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedAnimalId by rememberSaveable { mutableStateOf<String?>(null) }
    var storageDebug by rememberSaveable { mutableStateOf("Funde werden geladen...") }
    var showFoundOnly by rememberSaveable { mutableStateOf(false) }
    var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var isFriendSearchOpen by rememberSaveable { mutableStateOf(false) }
    var selectedFriendProfileUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedFriendProfileDisplayName by rememberSaveable { mutableStateOf<String?>(null) }
    var showProfileFriendsScreen by rememberSaveable { mutableStateOf(false) }
    var showProfilePhotoGalleryScreen by rememberSaveable { mutableStateOf(false) }
    var selectedFindingDetail by remember { mutableStateOf<AnimalFinding?>(null) }
    var selectedFindingDetailSource by rememberSaveable { mutableStateOf<String?>(null) }
    val profileCollectionListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    var profileCollectionSortOrder by rememberSaveable {
        mutableStateOf(ProfileCollectionSortOrder.NEWEST_FIRST.name)
    }
    var profileCollectionDateFilter by rememberSaveable {
        mutableStateOf(ProfileCollectionDateFilter.ALL.name)
    }
    var incomingRequestCount by rememberSaveable { mutableStateOf(0) }
    var animalGlobalFindingCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var animalGlobalFindingCountsLoaded by remember { mutableStateOf(false) }
    var animalGlobalFindingCountsLoadAttempted by remember { mutableStateOf(false) }
    var globalFindingBackfillStartedForOwnerId by remember { mutableStateOf<String?>(null) }
    var showNotificationsScreen by rememberSaveable { mutableStateOf(false) }
    var authEntryMode by rememberSaveable {
        mutableStateOf<String?>(defaultAuthEntryMode(prefs))
    }
    var showAnimalPicker by rememberSaveable { mutableStateOf(false) }
    var selectedFindingToEdit by remember { mutableStateOf<AnimalFinding?>(null) }
    var findingEditReturnSource by rememberSaveable { mutableStateOf<String?>(null) }
    var showSettingsScreen by rememberSaveable { mutableStateOf(false) }
    var showIntroScreen by rememberSaveable { mutableStateOf(false) }
    var showDailyAnimalScreen by rememberSaveable { mutableStateOf(false) }
    var dailyAnimalId by rememberSaveable { mutableStateOf<String?>(null) }
    var isDailyAnimalOpenedFromHomeTile by rememberSaveable { mutableStateOf(false) }
    var introLaunchSource by rememberSaveable { mutableStateOf(IntroLaunchSource.AUTOMATIC.name) }
    var selectedGroupFilter by rememberSaveable { mutableStateOf("Alle") }
    var selectedSubgroupFilter by rememberSaveable { mutableStateOf("Alle") }
    var showTierdexMapScreen by rememberSaveable { mutableStateOf(false) }
    var openCreateFindingMode by rememberSaveable { mutableStateOf(false) }
    var startInFindingEditMode by rememberSaveable { mutableStateOf(false) }
    val resetSearchState = {
        searchText = ""
    }
    var favoriteAnimalId by rememberSaveable { mutableStateOf<String?>(null) }

    var wishlistAnimalId by rememberSaveable { mutableStateOf<String?>(null) }
    var wishlistCelebrationMessage by rememberSaveable { mutableStateOf<CelebrationMessage?>(null) }
    var questLevelUpMessage by rememberSaveable { mutableStateOf<CelebrationMessage?>(null) }
    var previousOwnerId by rememberSaveable { mutableStateOf(ownerId) }
    var lastSyncedAnimalPreferenceSignature by rememberSaveable { mutableStateOf<String?>(null) }

    fun isRepairReadableLocalPhoto(photoUri: String): Boolean {
        val trimmedPhotoUri = photoUri.trim()
        if (trimmedPhotoUri.isBlank() || trimmedPhotoUri.startsWith(STORAGE_URI_PREFIX)) {
            return false
        }

        return runCatching {
            when {
                trimmedPhotoUri.startsWith("internal://") -> {
                    val fileName = trimmedPhotoUri.removePrefix("internal://").trim()
                    if (fileName.isBlank()) {
                        false
                    } else {
                        val sourceFile = File(
                            File(context.applicationContext.filesDir, "finding_images"),
                            fileName
                        )
                        sourceFile.exists() && sourceFile.isFile
                    }
                }

                else -> {
                    context.applicationContext.contentResolver.openInputStream(Uri.parse(trimmedPhotoUri))?.use { input ->
                        input.read() >= -1
                    } ?: false
                }
            }
        }.getOrDefault(false)
    }

    suspend fun saveCurrentUserFindingAwait(finding: AnimalFinding): Pair<Boolean, String?> {
        return suspendCancellableCoroutine { continuation ->
            FirestoreFindingRepository.saveCurrentUserFinding(finding) { success, result ->
                if (continuation.isActive) {
                    continuation.resume(success to result)
                }
            }
        }
    }

    suspend fun loadCurrentUserFindingsAwait(): Result<List<AnimalFinding>> {
        return suspendCancellableCoroutine { continuation ->
            FirestoreFindingRepository.loadCurrentUserFindings(
                onResult = { findings ->
                    if (continuation.isActive) {
                        continuation.resume(Result.success(findings))
                    }
                },
                onError = { error ->
                    if (continuation.isActive) {
                        continuation.resume(
                            Result.failure(
                                IllegalStateException(
                                    error ?: "Cloud-Funde konnten nicht geladen werden."
                                )
                            )
                        )
                    }
                }
            )
        }
    }

    suspend fun repairIncompleteFindingPhotoSyncForOwner(ownerId: String) {
        val repairStartedAt = SystemClock.elapsedRealtime()
        val localRoomFindings = dao.getAllFindingsByOwnerOnce(ownerId)
        val localFindings = localRoomFindings.map { entity -> entity.toDomainFinding() }

        data class PhotoRepairCandidate(
            val finding: AnimalFinding,
            val localPhotoCount: Int,
            val remotePhotoCount: Int,
            val hasRemotePhotos: Boolean,
            val hasThumbnail: Boolean,
            val needsOriginalUpload: Boolean,
            val needsThumbnailUploadFromLocal: Boolean,
            val needsThumbnailUploadFromRemote: Boolean
        )

        val candidates = localFindings.mapNotNull { finding ->
            val localPhotoUris = effectiveLocalPhotoUris(finding)
            if (finding.ownerId != ownerId) {
                return@mapNotNull null
            }

            val remotePhotoPaths = effectiveRemotePhotoPaths(finding)
            val hasLocalPhotos = localPhotoUris.isNotEmpty()
            val hasRemotePhotos = remotePhotoPaths.isNotEmpty()
            val hasThumbnail = finding.thumbnailRemotePhotoPath.trim().isNotBlank()
            val needsOriginalUpload = hasLocalPhotos && !hasRemotePhotos
            val needsThumbnailUploadFromLocal = !hasThumbnail && hasLocalPhotos
            val needsThumbnailUploadFromRemote =
                !hasThumbnail && !hasLocalPhotos && hasRemotePhotos
            if (!needsOriginalUpload && !needsThumbnailUploadFromLocal && !needsThumbnailUploadFromRemote) {
                return@mapNotNull null
            }

            val requiredLocalUris = when {
                needsOriginalUpload -> localPhotoUris
                needsThumbnailUploadFromLocal -> listOf(localPhotoUris.first())
                else -> emptyList()
            }
            if (!requiredLocalUris.all(::isRepairReadableLocalPhoto)) {
                return@mapNotNull null
            }

            PhotoRepairCandidate(
                finding = finding,
                localPhotoCount = localPhotoUris.size,
                remotePhotoCount = remotePhotoPaths.size,
                hasRemotePhotos = hasRemotePhotos,
                hasThumbnail = hasThumbnail,
                needsOriginalUpload = needsOriginalUpload,
                needsThumbnailUploadFromLocal = needsThumbnailUploadFromLocal,
                needsThumbnailUploadFromRemote = needsThumbnailUploadFromRemote
            )
        }

        val remoteThumbnailRepairCandidateCount = candidates.count { candidate ->
            candidate.needsThumbnailUploadFromRemote
        }

        Log.d(
            "FindingPhotoRepair",
            "repair scan start ownerId=$ownerId localFindingCount=${localFindings.size} candidateCount=${candidates.size} remoteThumbnailRepairCandidateCount=$remoteThumbnailRepairCandidateCount"
        )

        candidates.forEach { candidate ->
            val candidateStartedAt = SystemClock.elapsedRealtime()
            Log.d(
                "FindingPhotoRepair",
                "candidate start animalId=${candidate.finding.animalId} localPhotoCount=${candidate.localPhotoCount} remotePhotoCount=${candidate.remotePhotoCount} hasRemoteOriginal=${candidate.hasRemotePhotos} hasThumbnail=${candidate.hasThumbnail} repairOriginals=${candidate.needsOriginalUpload} repairThumbnailFromLocal=${candidate.needsThumbnailUploadFromLocal} repairThumbnailFromRemote=${candidate.needsThumbnailUploadFromRemote}"
            )

            var repairedRemotePhotoPaths = effectiveRemotePhotoPaths(candidate.finding)
            var repairedThumbnailPath = candidate.finding.thumbnailRemotePhotoPath.trim()

            if (candidate.needsOriginalUpload) {
                repairedRemotePhotoPaths = FindingPhotoStorageRepository.uploadFindingPhotos(
                    context = context.applicationContext,
                    userId = ownerId,
                    finding = candidate.finding
                )
                if (repairedRemotePhotoPaths.size < candidate.localPhotoCount) {
                    Log.w(
                        "FindingPhotoRepair",
                        "candidate original upload incomplete animalId=${candidate.finding.animalId} expectedRemotePhotoCount=${candidate.localPhotoCount} actualRemotePhotoCount=${repairedRemotePhotoPaths.size}"
                    )
                    return@forEach
                }
            }

            if (candidate.needsThumbnailUploadFromLocal) {
                val thumbnailUploadStartedAt = SystemClock.elapsedRealtime()
                repairedThumbnailPath = FindingPhotoStorageRepository.uploadFindingThumbnail(
                    context = context.applicationContext,
                    userId = ownerId,
                    finding = candidate.finding
                )
                Log.d(
                    "FindingPhotoRepair",
                    "candidate thumbnail upload animalId=${candidate.finding.animalId} source=local durationMs=${SystemClock.elapsedRealtime() - thumbnailUploadStartedAt}"
                )
                if (repairedThumbnailPath.isBlank()) {
                    Log.w(
                        "FindingPhotoRepair",
                        "candidate thumbnail upload failed animalId=${candidate.finding.animalId} source=local"
                    )
                    return@forEach
                }
            } else if (candidate.needsThumbnailUploadFromRemote) {
                val remoteOriginalPath = repairedRemotePhotoPaths.firstOrNull().orEmpty()
                val remoteOriginalDownloadStartedAt = SystemClock.elapsedRealtime()
                repairedThumbnailPath = FindingPhotoStorageRepository.uploadFindingThumbnailFromRemoteOriginal(
                    context = context.applicationContext,
                    userId = ownerId,
                    finding = candidate.finding,
                    remoteOriginalPhotoPath = remoteOriginalPath
                )
                val remoteThumbnailDurationMs =
                    SystemClock.elapsedRealtime() - remoteOriginalDownloadStartedAt
                Log.d(
                    "FindingPhotoRepair",
                    "candidate remote thumbnail upload animalId=${candidate.finding.animalId} hasRemoteOriginal=${remoteOriginalPath.isNotBlank()} remoteOriginalDownloadDurationMs=$remoteThumbnailDurationMs thumbnailUploadDurationMs=$remoteThumbnailDurationMs"
                )
                if (repairedThumbnailPath.isBlank()) {
                    Log.w(
                        "FindingPhotoRepair",
                        "candidate thumbnail upload failed animalId=${candidate.finding.animalId} source=remoteOriginal"
                    )
                    return@forEach
                }
            }

            val repairedFinding = candidate.finding.copy(
                ownerId = ownerId,
                remotePhotoPath = repairedRemotePhotoPaths.firstOrNull().orEmpty(),
                remotePhotoPaths = repairedRemotePhotoPaths,
                thumbnailRemotePhotoPath = if (candidate.hasThumbnail) {
                    candidate.finding.thumbnailRemotePhotoPath
                } else {
                    repairedThumbnailPath
                }
            )

            val roomId = repairedFinding.roomId
            if (roomId == null) {
                Log.w(
                    "FindingPhotoRepair",
                    "candidate skipped withoutRoomId animalId=${candidate.finding.animalId}"
                )
                return@forEach
            }

            dao.updateFinding(
                repairedFinding.toEntity(
                    ownerIdOverride = ownerId,
                    roomIdOverride = roomId
                )
            )
            Log.d(
                "FindingPhotoRepair",
                "candidate room update animalId=${candidate.finding.animalId} roomUpdated=true"
            )

            val (saveSuccess, saveResult) = saveCurrentUserFindingAwait(repairedFinding)
            if (saveSuccess) {
                Log.d(
                    "FindingPhotoRepair",
                    "candidate firestore save animalId=${candidate.finding.animalId} firestoreSaved=true durationMs=${SystemClock.elapsedRealtime() - candidateStartedAt}"
                )
            } else {
                Log.w(
                    "FindingPhotoRepair",
                    "candidate firestore save failed animalId=${candidate.finding.animalId} error=${saveResult ?: "Unbekannter Fehler"} durationMs=${SystemClock.elapsedRealtime() - candidateStartedAt}"
                )
            }
        }

        Log.d(
            "FindingPhotoRepair",
            "repair scan end ownerId=$ownerId candidateCount=${candidates.size} durationMs=${SystemClock.elapsedRealtime() - repairStartedAt}"
        )
    }

    LaunchedEffect(ownerId) {
        favoriteAnimalId = prefs.getString(favoriteAnimalKey(preferenceOwnerId), null)
        wishlistAnimalId = prefs.getString(wishlistAnimalKey(preferenceOwnerId), null)

        val hasPendingIntro = ownerId?.let {
            val pending = prefs.getBoolean(introPendingKey(it), false)
            val seen = prefs.getBoolean(introSeenKey(it), false)
            pending && !seen
        } ?: false

        if (previousOwnerId == null && ownerId != null) {
            if (hasPendingIntro) {
                showIntroScreen = true
                introLaunchSource = IntroLaunchSource.AUTOMATIC.name
            } else {
                val startupHintKey = "$STARTUP_HINT_SHOWN_KEY_PREFIX$ownerId"
                val alreadyShown = prefs.getBoolean(startupHintKey, false)
                if (!alreadyShown) {
                    showIntroScreen = true
                    introLaunchSource = IntroLaunchSource.AUTOMATIC.name
                    prefs.edit().putBoolean(startupHintKey, true).apply()
                }
            }
        } else if (ownerId != null && hasPendingIntro) {
            showIntroScreen = true
            introLaunchSource = IntroLaunchSource.AUTOMATIC.name
        }
        previousOwnerId = ownerId

        if (ownerId != null) {
            val migrationKey = "global_findings_migrated_to_$ownerId"
            val alreadyMigrated = prefs.getBoolean(migrationKey, false)
            if (!alreadyMigrated) {
                dao.assignGlobalFindingsToOwner(ownerId)
                prefs.edit().putBoolean(migrationKey, true).apply()
            }

            repairIncompleteFindingPhotoSyncForOwner(ownerId)

            val cloudFindingsResult = loadCurrentUserFindingsAwait()
            if (cloudFindingsResult.isSuccess) {
                val cloudFindings = cloudFindingsResult.getOrNull().orEmpty()
                val localRoomFindings = dao.getAllFindingsByOwnerOnce(ownerId)
                val localFindings = localRoomFindings.map { entity -> entity.toDomainFinding() }

                val localFingerprints = localFindings
                    .map { FirestoreFindingRepository.findingFingerprint(it) }
                    .toMutableSet()
                val cloudFingerprints = cloudFindings
                    .map { FirestoreFindingRepository.findingFingerprint(it) }
                    .toMutableSet()

                Log.d(
                    "CloudSync",
                    "Sync start: found ${localFindings.size} local findings and ${cloudFindings.size} cloud findings for user $ownerId"
                )

                var uploadedCount = 0
                var skippedDuplicateCount = 0
                localFindings.forEach { localFinding ->
                    val fingerprint =
                        FirestoreFindingRepository.findingFingerprint(localFinding)
                    if (fingerprint in cloudFingerprints) {
                        skippedDuplicateCount += 1
                    } else {
                        FirestoreFindingRepository.saveCurrentUserFinding(localFinding) { success, result ->
                            if (!success) {
                                Log.e("CloudSync", "Upload local finding failed: $result")
                            }
                        }
                        uploadedCount += 1
                        cloudFingerprints.add(fingerprint)
                    }
                }

                var insertedCount = 0
                cloudFindings.forEach { cloudFinding ->
                    val fingerprint =
                        FirestoreFindingRepository.findingFingerprint(cloudFinding)
                    if (fingerprint !in localFingerprints) {
                        dao.insertFinding(cloudFinding.toEntity(ownerIdOverride = ownerId))
                        insertedCount += 1
                        localFingerprints.add(fingerprint)
                    } else {
                        skippedDuplicateCount += 1
                    }
                }

                val finalTotalCount = localFingerprints.size
                Log.d(
                    "CloudSync",
                    "Sync result: local=${localFindings.size}, cloud=${cloudFindings.size}, uploaded=$uploadedCount, insertedIntoRoom=$insertedCount, duplicatesSkipped=$skippedDuplicateCount, finalTotal=$finalTotalCount"
                )
            } else {
                Log.e(
                    "CloudSync",
                    "Cloud load failed: ${cloudFindingsResult.exceptionOrNull()?.message ?: "Unbekannter Fehler"}"
                )
            }
        }
    }
    LaunchedEffect(wishlistAnimalId, findingsFromRoom, preferenceOwnerId) {
        val currentWishlistAnimalId = wishlistAnimalId ?: return@LaunchedEffect
        val wishlistAnimalWasFound = findingsFromRoom.any { it.animalId == currentWishlistAnimalId }
        if (wishlistAnimalWasFound) {
            wishlistAnimalId = null
            prefs.edit().remove(wishlistAnimalKey(preferenceOwnerId)).apply()
        }
    }
    LaunchedEffect(wishlistCelebrationMessage) {
        if (wishlistCelebrationMessage != null) {
            delay(2200)
            wishlistCelebrationMessage = null
        }
    }
    LaunchedEffect(questLevelUpMessage) {
        if (questLevelUpMessage != null) {
            delay(2200)
            questLevelUpMessage = null
        }
    }
    LaunchedEffect(ownerId, currentDisplayName) {
        if (!ownerId.isNullOrBlank()) {
            FriendRepository.ensureUserProfile(
                userId = ownerId,
                displayName = currentDisplayName
            ) { success, result ->
                if (!success) {
                    Log.e(
                        "FriendProfile",
                        "User profile ensure failed: ${result ?: "Unbekannter Fehler"}"
                    )
                }
            }
        }
    }
    LaunchedEffect(ownerId, preferenceOwnerId, wishlistAnimalId, favoriteAnimalId) {
        val safeOwnerId = ownerId ?: return@LaunchedEffect
        val preferenceSignature = listOf(
            safeOwnerId,
            wishlistAnimalId.orEmpty().trim(),
            favoriteAnimalId.orEmpty().trim()
        ).joinToString("|")

        if (lastSyncedAnimalPreferenceSignature == preferenceSignature) {
            return@LaunchedEffect
        }

        lastSyncedAnimalPreferenceSignature = preferenceSignature
        FriendRepository.updatePublicProfileAnimalPreferences(
            userId = safeOwnerId,
            wishAnimalId = wishlistAnimalId.orEmpty(),
            favoriteAnimalId = favoriteAnimalId.orEmpty()
        ) { success, result ->
            if (!success) {
                lastSyncedAnimalPreferenceSignature = null
                Log.e(
                    "FriendProfile",
                    "Animal preferences sync failed: ${result ?: "Unbekannter Fehler"}"
                )
            }
        }
    }


    val animalLoadResult: CsvLoadResult = remember(context) {
        loadAnimalsFromJsonWithDebug(
            context = context,
            jsonFileName = ANIMALS_JSON_FILE_NAME,
            csvFallbackFileName = ANIMALS_CSV_FILE_NAME
        )
    }

    val animals: List<AnimalEntry> = animalLoadResult.animals
    val dailyAnimal = animals.find { it.id == dailyAnimalId }

    LaunchedEffect(preferenceOwnerId, animals) {
        if (animals.isEmpty()) {
            dailyAnimalId = null
            showDailyAnimalScreen = false
            return@LaunchedEffect
        }

        val todayKey = currentDailyDateKey()
        val savedDateKey = prefs.getString(dailyAnimalDateKey(preferenceOwnerId), null)
        val savedAnimalId = prefs.getString(dailyAnimalIdKey(preferenceOwnerId), null)
        val savedAnimal = savedAnimalId?.let { id -> animals.find { it.id == id } }

        val activeAnimal = if (savedDateKey == todayKey && savedAnimal != null) {
            savedAnimal
        } else {
            animals.random()
        }

        val shouldResetForToday = savedDateKey != todayKey || savedAnimal == null
        if (shouldResetForToday) {
            prefs.edit()
                .putString(dailyAnimalDateKey(preferenceOwnerId), todayKey)
                .putString(dailyAnimalIdKey(preferenceOwnerId), activeAnimal.id)
                .putBoolean(dailyAnimalDismissedKey(preferenceOwnerId), false)
                .apply()
        }

        recordDailyAnimalHistoryIfNeeded(
            prefs = prefs,
            ownerId = preferenceOwnerId,
            animalId = activeAnimal.id,
            todayKey = todayKey
        )

        dailyAnimalId = activeAnimal.id
        isDailyAnimalOpenedFromHomeTile = false
        val isDismissedToday = prefs.getBoolean(dailyAnimalDismissedKey(preferenceOwnerId), false)
        showDailyAnimalScreen = !isDismissedToday
    }

    val findingCountByAnimalId = allFindings
        .groupingBy { it.animalId }
        .eachCount()

    val collectedAnimalIds = findingCountByAnimalId.keys
    val collectedAnimalCount = animals.count { it.id in collectedAnimalIds }

    var selectedSortOption by rememberSaveable { mutableStateOf("A_Z") }
    var notifications by remember { mutableStateOf<List<TierdexNotification>>(emptyList()) }
    var notificationsErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val filteredAnimals = animals.filter { animal: AnimalEntry ->
        val searchTokens = tokenizeSearchText(searchText)
        val searchableText = listOf(
            animal.germanName,
            animal.latinName,
            animal.group,
            animal.subgroup
        ).joinToString(" ")
        val normalizedSearchableText = normalizeSearchText(searchableText)
        val compactSearchableText = normalizedSearchableText.replace(" ", "")

        val matchesSearch =
            searchTokens.isEmpty() ||
                    searchTokens.all { token: String ->
                        normalizedSearchableText.contains(token) ||
                                compactSearchableText.contains(token.replace(" ", ""))
                    }

        val matchesFound =
            !showFoundOnly || (findingCountByAnimalId[animal.id] ?: 0) > 0

        val matchesGroup =
            selectedGroupFilter == "Alle" || animal.group == selectedGroupFilter

        val matchesSubgroup =
            selectedSubgroupFilter == "Alle" || animal.subgroup == selectedSubgroupFilter

        matchesSearch && matchesFound && matchesGroup && matchesSubgroup
    }
    val foundAnimalIds = findingsFromRoom.map { it.animalId }.toSet()

    val sortedAnimals = when (selectedSortOption) {
        "A_Z" -> filteredAnimals.sortedBy { it.germanName.lowercase() }
        "Z_A" -> filteredAnimals.sortedByDescending { it.germanName.lowercase() }
        "FOUND_FIRST" -> filteredAnimals.sortedWith(
            compareByDescending<AnimalEntry> { it.id in foundAnimalIds }
                .thenBy { it.germanName.lowercase() }
        )

        "NOT_FOUND_FIRST" -> filteredAnimals.sortedWith(
            compareBy<AnimalEntry> { it.id in foundAnimalIds }
                .thenBy { it.germanName.lowercase() }
        )

        else -> filteredAnimals.sortedBy { it.germanName.lowercase() }
    }

    val appContext = LocalContext.current.applicationContext
    val selectedAnimal = animals.find { it.id == selectedAnimalId }
    val selectedFindingAnimal = selectedFindingDetail?.let { finding ->
        animals.find { it.id == finding.animalId }
    }
    val selectedAnimalDailyAnimalHistoryText = remember(selectedAnimal?.id, preferenceOwnerId) {
        selectedAnimal?.let { animal ->
            formatDailyAnimalHistoryText(
                entry = getDailyAnimalHistoryEntry(prefs, preferenceOwnerId, animal.id),
                includeNeverText = true
            )
        }
    }
    val dailyAnimalHistoryText = remember(dailyAnimal?.id, preferenceOwnerId) {
        dailyAnimal?.let { animal ->
            formatDailyAnimalHistoryText(
                entry = getDailyAnimalHistoryEntry(prefs, preferenceOwnerId, animal.id),
                includeNeverText = false
            )
        }
    }
    val showAuthStartScreen = ownerId == null && authEntryMode == null
    val showAuthEntryScreen = ownerId == null && authEntryMode != null
    val isIntroFromSettings = introLaunchSource == IntroLaunchSource.SETTINGS.name

    fun currentReadNotificationIds(): Set<String> {
        val safeOwnerId = currentOwnerId ?: return emptySet()
        return prefs.getStringSet(notificationReadIdsKey(safeOwnerId), emptySet())?.toSet().orEmpty()
    }

    fun storeReadNotificationIds(ids: Set<String>) {
        val safeOwnerId = currentOwnerId ?: return
        prefs.edit().putStringSet(notificationReadIdsKey(safeOwnerId), ids).apply()
    }

    fun mapFriendRequestsToNotifications(requests: List<FriendRequest>): List<TierdexNotification> {
        val readIds = currentReadNotificationIds()
        return requests
            .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            .map { request ->
                val notificationId = friendRequestNotificationId(request)
                TierdexNotification(
                    id = notificationId,
                    type = "friend_request",
                    title = "Neue Freundschaftsanfrage",
                    message = "${request.displayName.ifBlank { "Jemand" }} möchte dich als Freund hinzufügen.",
                    createdAtText = formatNotificationTimestamp(request.createdAt),
                    createdAt = request.createdAt,
                    isRead = notificationId in readIds,
                    relatedUserId = request.fromUserId
                )
            }
    }

    fun loadInteractionNotifications(
        currentUserId: String,
        onResult: (List<TierdexNotification>) -> Unit,
        onError: (String?) -> Unit
    ) {
        val readIds = currentReadNotificationIds()
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .document(currentUserId)
            .collection("findings")
            .get()
            .addOnSuccessListener { findingsSnapshot ->
                if (findingsSnapshot.isEmpty) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val interactionNotifications = mutableListOf<TierdexNotification>()
                var pendingLoads = findingsSnapshot.documents.size * 2
                var firstError: String? = null

                fun finishLoad() {
                    pendingLoads -= 1
                    if (pendingLoads <= 0) {
                        if (interactionNotifications.isEmpty() && firstError != null) {
                            onError(firstError)
                        } else {
                            onResult(interactionNotifications.distinctBy { it.id })
                        }
                    }
                }

                findingsSnapshot.documents.forEach { findingDocument ->
                    val findingId = findingDocument.id

                    findingDocument.reference.collection("likes")
                        .get()
                        .addOnSuccessListener { likeSnapshot ->
                            likeSnapshot.documents.forEach { likeDocument ->
                                val likerUid = likeDocument.getString("likerUid").orEmpty()
                                if (likerUid.isBlank() || likerUid == currentUserId) return@forEach

                                val createdAt = likeDocument.getTimestamp("createdAt")
                                val likerDisplayName = likeDocument.getString("likerDisplayName").orEmpty()
                                val notificationId = "like_${findingId}_${likeDocument.id}"
                                interactionNotifications += TierdexNotification(
                                    id = notificationId,
                                    type = "like",
                                    title = "Neuer Like",
                                    message = "${likerDisplayName.ifBlank { "Jemand" }} gefällt dein Fund.",
                                    createdAtText = formatNotificationTimestamp(createdAt),
                                    createdAt = createdAt,
                                    isRead = notificationId in readIds,
                                    relatedUserId = likerUid
                                )
                            }
                            finishLoad()
                        }
                        .addOnFailureListener { exception ->
                            if (firstError == null) {
                                firstError = exception.message ?: "Likes konnten nicht geladen werden"
                            }
                            finishLoad()
                        }

                    findingDocument.reference.collection("comments")
                        .get()
                        .addOnSuccessListener { commentSnapshot ->
                            commentSnapshot.documents.forEach { commentDocument ->
                                val commenterUid = commentDocument.getString("commenterUid").orEmpty()
                                if (commenterUid.isBlank() || commenterUid == currentUserId) return@forEach

                                val createdAt = commentDocument.getTimestamp("createdAt")
                                val commenterDisplayName =
                                    commentDocument.getString("commenterDisplayName").orEmpty()
                                val commentText = commentDocument.getString("text").orEmpty().trim()
                                val notificationId = "comment_${findingId}_${commentDocument.id}"
                                interactionNotifications += TierdexNotification(
                                    id = notificationId,
                                    type = "comment",
                                    title = "Neuer Kommentar",
                                    message = "${commenterDisplayName.ifBlank { "Jemand" }}: $commentText",
                                    createdAtText = formatNotificationTimestamp(createdAt),
                                    createdAt = createdAt,
                                    isRead = notificationId in readIds,
                                    relatedUserId = commenterUid
                                )
                            }
                            finishLoad()
                        }
                        .addOnFailureListener { exception ->
                            if (firstError == null) {
                                firstError = exception.message ?: "Kommentare konnten nicht geladen werden"
                            }
                            finishLoad()
                        }
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Eigene Funde konnten nicht geladen werden")
            }
    }

    fun updateNotificationState(
        requests: List<FriendRequest>,
        interactionNotifications: List<TierdexNotification>
    ) {
        notifications = (mapFriendRequestsToNotifications(requests) + interactionNotifications)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MIN_VALUE }
        incomingRequestCount = notifications.count { !it.isRead }
    }

    fun refreshNotifications() {
        val safeUserId = currentOwnerId
        if (safeUserId.isNullOrBlank()) {
            incomingRequestCount = 0
            notifications = emptyList()
            notificationsErrorMessage = null
            return
        }

        var friendRequests: List<FriendRequest> = emptyList()
        var interactionNotifications: List<TierdexNotification> = emptyList()
        var pendingLoads = 2
        var firstError: String? = null

        fun finishRefresh() {
            pendingLoads -= 1
            if (pendingLoads <= 0) {
                updateNotificationState(friendRequests, interactionNotifications)
                notificationsErrorMessage = firstError
            }
        }

        FriendRepository.loadIncomingFriendRequests(
            currentUserId = safeUserId,
            onResult = { requests ->
                friendRequests = requests
                finishRefresh()
            },
            onError = { error ->
                firstError = firstError ?: error ?: "Freundschaftsanfragen konnten nicht geladen werden"
                finishRefresh()
            }
        )

        loadInteractionNotifications(
            currentUserId = safeUserId,
            onResult = { loadedNotifications ->
                interactionNotifications = loadedNotifications
                finishRefresh()
            },
            onError = { error ->
                firstError = firstError ?: error ?: "Interaktionen konnten nicht geladen werden"
                finishRefresh()
            }
        )
    }

    fun markAllNotificationsAsRead() {
        if (notifications.isEmpty()) return
        storeReadNotificationIds(currentReadNotificationIds() + notifications.map { it.id })
        notifications = notifications.map { it.copy(isRead = true) }
        incomingRequestCount = 0
    }

    fun markNotificationAsRead(notificationId: String) {
        if (notificationId.isBlank()) return
        val updatedReadIds = currentReadNotificationIds() + notificationId
        storeReadNotificationIds(updatedReadIds)
        notifications = notifications.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }
        incomingRequestCount = notifications.count { !it.isRead }
    }

    fun refreshAnimalGlobalFindingCounts() {
        if (currentOwnerId.isNullOrBlank()) {
            animalGlobalFindingCounts = emptyMap()
            animalGlobalFindingCountsLoaded = false
            animalGlobalFindingCountsLoadAttempted = false
            return
        }

        FirestoreFindingRepository.loadAnimalStats(
            onResult = { counts ->
                animalGlobalFindingCounts = counts
                animalGlobalFindingCountsLoaded = true
                animalGlobalFindingCountsLoadAttempted = true
            },
            onError = { error ->
                Log.e(
                    "GlobalStats",
                    "stats load failed from TierdexApp ownerId=$currentOwnerId error=${error ?: "Unbekannter Fehler"}"
                )
                animalGlobalFindingCountsLoadAttempted = true
            }
        )
    }

    fun updateAnimalGlobalFindingCountLocally(animalId: String, delta: Int) {
        val normalizedAnimalId = animalId.trim()
        if (normalizedAnimalId.isBlank()) return

        animalGlobalFindingCounts = animalGlobalFindingCounts.toMutableMap().apply {
            val currentCount = this[normalizedAnimalId] ?: 0
            this[normalizedAnimalId] = (currentCount + delta).coerceAtLeast(0)
        }
    }

    fun updateAnimalGlobalFindingCountLocallyForChange(oldAnimalId: String, newAnimalId: String) {
        val normalizedOldAnimalId = oldAnimalId.trim()
        val normalizedNewAnimalId = newAnimalId.trim()
        if (
            normalizedOldAnimalId.isBlank() ||
            normalizedNewAnimalId.isBlank() ||
            normalizedOldAnimalId == normalizedNewAnimalId
        ) {
            return
        }

        animalGlobalFindingCounts = animalGlobalFindingCounts.toMutableMap().apply {
            val oldCount = this[normalizedOldAnimalId] ?: 0
            val newCount = this[normalizedNewAnimalId] ?: 0
            this[normalizedOldAnimalId] = (oldCount - 1).coerceAtLeast(0)
            this[normalizedNewAnimalId] = newCount + 1
        }
    }

    LaunchedEffect(currentOwnerId) {
        refreshNotifications()
        refreshAnimalGlobalFindingCounts()
    }
    LaunchedEffect(currentOwnerId, findingsFromRoom) {
        val safeOwnerId = currentOwnerId
        if (safeOwnerId.isNullOrBlank()) {
            globalFindingBackfillStartedForOwnerId = null
            return@LaunchedEffect
        }
        if (findingsFromRoom.isEmpty()) {
            return@LaunchedEffect
        }
        if (globalFindingBackfillStartedForOwnerId == safeOwnerId) {
            return@LaunchedEffect
        }

        globalFindingBackfillStartedForOwnerId = safeOwnerId
        FirestoreFindingRepository.backfillGlobalFindingCountsForCurrentUser(
            ownerUid = safeOwnerId,
            findings = findingsFromRoom,
            onResult = { success ->
                if (success) {
                    refreshAnimalGlobalFindingCounts()
                } else {
                    globalFindingBackfillStartedForOwnerId = null
                }
            },
            onError = { error ->
                Log.e(
                    "GlobalStats",
                    "backfill error ownerId=$safeOwnerId error=${error ?: "Unbekannter Fehler"}"
                )
            }
        )
    }

    LaunchedEffect(currentTab, currentOwnerId, showNotificationsScreen) {
        if (currentTab == AppTab.FRIENDS && !currentOwnerId.isNullOrBlank()) {
            refreshNotifications()
        }
        if (showNotificationsScreen && !currentOwnerId.isNullOrBlank()) {
            refreshNotifications()
        }
    }

    val groupOptions = listOf("Alle") + animals.map { it.group }.distinct().sorted()

    val subgroupOptions =
        if (selectedGroupFilter == "Alle") {
            listOf("Alle")
        } else {
            listOf("Alle") + animals
                .filter { it.group == selectedGroupFilter }
                .map { it.subgroup }
                .distinct()
                .sorted()
        }

    BackHandler(enabled = showSettingsScreen && !showIntroScreen) {
        resetSearchState()
        showSettingsScreen = false
    }
    BackHandler(enabled = selectedAnimalId != null && selectedFindingDetail == null) {
        if (startInFindingEditMode && selectedFindingToEdit != null) {
            selectedFindingDetail = selectedFindingToEdit
            selectedAnimalId = null
            selectedFindingToEdit = null
            startInFindingEditMode = false
            openCreateFindingMode = false
        } else {
            resetSearchState()
            selectedAnimalId = null
            selectedFindingToEdit = null
            startInFindingEditMode = false
            openCreateFindingMode = false
        }
    }
    BackHandler(enabled = selectedFindingDetail != null) {
        selectedFindingDetail = null
        selectedFindingDetailSource = null
    }

    BackHandler(enabled = showAnimalPicker && selectedAnimalId == null) {
        resetSearchState()
        showAnimalPicker = false
    }
    BackHandler(enabled = currentTab == AppTab.STATS && showTierdexMapScreen) {
        showTierdexMapScreen = false
    }
    BackHandler(enabled = showNotificationsScreen) {
        showNotificationsScreen = false
    }
    BackHandler(
        enabled = currentTab == AppTab.FRIENDS &&
            !selectedFriendProfileUserId.isNullOrBlank() &&
            !showNotificationsScreen
    ) {
        selectedFriendProfileUserId = null
        selectedFriendProfileDisplayName = null
    }
    BackHandler(enabled = showAuthEntryScreen && !showSettingsScreen) {
        resetSearchState()
        authEntryMode = null
    }
    BackHandler(enabled = showIntroScreen) {
        if (isIntroFromSettings) {
            showIntroScreen = false
        } else {
            ownerId?.let {
                prefs.edit()
                    .putBoolean(introSeenKey(it), true)
                    .putBoolean(introPendingKey(it), false)
                    .apply()
            }
            showIntroScreen = false
            currentTab = AppTab.PROFILE
        }
    }

    LaunchedEffect(currentTab) {
        if (currentTab != AppTab.FRIENDS) {
            isFriendSearchOpen = false
            selectedFriendProfileUserId = null
            selectedFriendProfileDisplayName = null
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TierdexTopBar(
                currentTab = currentTab,
                showFriendSearchAction = currentTab == AppTab.FRIENDS &&
                    selectedFriendProfileUserId == null &&
                    selectedAnimal == null &&
                    selectedFindingDetail == null &&
                    !showAnimalPicker &&
                    !showAuthStartScreen &&
                    !showAuthEntryScreen &&
                    !showIntroScreen &&
                    !showSettingsScreen,
                isFriendSearchOpen = isFriendSearchOpen,
                incomingRequestCount = incomingRequestCount,
                onFriendSearchClick = {
                    isFriendSearchOpen = !isFriendSearchOpen
                },
                onNotificationsClick = {
                    resetSearchState()
                    isFriendSearchOpen = false
                    selectedAnimalId = null
                    selectedFindingDetail = null
                    selectedFindingDetailSource = null
                    selectedFindingToEdit = null
                    findingEditReturnSource = null
                    startInFindingEditMode = false
                    openCreateFindingMode = false
                    showAnimalPicker = false
                    showTierdexMapScreen = false
                    showSettingsScreen = false
                    showNotificationsScreen = true
                    refreshNotifications()
                },
                onSettingsClick = {
                    resetSearchState()
                    isFriendSearchOpen = false
                    showNotificationsScreen = false
                    showSettingsScreen = true
                }
            )
        },
        bottomBar = {
            if (selectedAnimal == null && selectedFindingDetail == null && !showAnimalPicker && !showAuthStartScreen && !showAuthEntryScreen && !showIntroScreen && !showSettingsScreen && !showNotificationsScreen) {
                MainBottomBar(
                    currentTab = currentTab,
                    onTabSelected = {
                        resetSearchState()
                        if (it != AppTab.FRIENDS) {
                            isFriendSearchOpen = false
                        }
                        if (it != AppTab.FRIENDS) {
                            selectedFriendProfileUserId = null
                            selectedFriendProfileDisplayName = null
                        }
                        if (it != AppTab.PROFILE) {
                            showProfileFriendsScreen = false
                            showProfilePhotoGalleryScreen = false
                        }
                        selectedFindingDetailSource = null
                        findingEditReturnSource = null
                        showNotificationsScreen = false
                        currentTab = it
                    }
                )
            }
        },
        floatingActionButton = {
            if (selectedAnimal == null && selectedFindingDetail == null && !showAnimalPicker && !showAuthStartScreen && !showAuthEntryScreen && !showIntroScreen && !showSettingsScreen && !showNotificationsScreen) {
                FloatingActionButton(
                    onClick = {
                        resetSearchState()
                        selectedFindingToEdit = null
                        startInFindingEditMode = false
                        openCreateFindingMode = false
                        showAnimalPicker = true
                    },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Neuer Fund")
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                showAuthStartScreen -> {
                    AuthStartScreen(
                        onLoginClick = {
                            resetSearchState()
                            authEntryMode = "login"
                        },
                        onRegisterClick = {
                            resetSearchState()
                            authEntryMode = "register"
                        }
                    )
                }

                showAuthEntryScreen -> {
                    AuthEntryScreen(
                        initialAuthMode = authEntryMode ?: "login",
                        onBack = {
                            resetSearchState()
                            authEntryMode = null
                        },
                        onAuthSuccess = { userId, fromRegistration ->
                            prefs.edit().putBoolean(HAS_USED_AUTH_BEFORE_KEY, true).apply()
                            AuthSession.setCurrentUserId(userId)
                            currentOwnerId = userId
                            currentDisplayName = AuthSession.getCurrentDisplayName()
                            authEntryMode = null
                            currentTab = AppTab.PROFILE
                            if (fromRegistration && userId != null) {
                                prefs.edit()
                                    .putBoolean(introPendingKey(userId), true)
                                    .putBoolean(introSeenKey(userId), false)
                                    .apply()
                                introLaunchSource = IntroLaunchSource.AUTOMATIC.name
                                showIntroScreen = true
                            }
                        }
                    )
                }

                showIntroScreen -> {
                    AboutTierdexScreen(
                        extraTopPadding = innerPadding.calculateTopPadding(),
                        extraBottomPadding = innerPadding.calculateBottomPadding(),
                        onClose = {
                            if (isIntroFromSettings) {
                                showIntroScreen = false
                            } else {
                                ownerId?.let {
                                    prefs.edit()
                                        .putBoolean(introSeenKey(it), true)
                                        .putBoolean(introPendingKey(it), false)
                                        .apply()
                                }
                                showIntroScreen = false
                                currentTab = AppTab.PROFILE
                            }
                        }
                    )
                }

                showSettingsScreen -> {
                    SettingsScreen(
                        onBack = {
                            resetSearchState()
                            showSettingsScreen = false
                        },
                        onLogout = {
                            resetSearchState()
                            currentOwnerId = null
                            currentDisplayName = null
                            authEntryMode = defaultAuthEntryMode(prefs)
                            showSettingsScreen = false
                            currentTab = AppTab.PROFILE
                        },
                        onShowIntro = {
                            introLaunchSource = IntroLaunchSource.SETTINGS.name
                            showIntroScreen = true
                        },
                        allFindings = findingsFromRoom,
                        onImportFindings = { importedFindings ->
                            scope.launch {
                                importedFindings.forEach { finding ->
                                    val importedFindingForCurrentOwner = finding.copy(
                                        ownerId = currentOwnerId ?: finding.ownerId
                                    )
                                    dao.insertFinding(importedFindingForCurrentOwner.toEntity())

                                    if (currentOwnerId != null) {
                                        FirestoreFindingRepository.saveCurrentUserFinding(
                                            importedFindingForCurrentOwner
                                        ) { success, result ->
                                            if (!success) {
                                                Log.e(
                                                    "CloudWrite",
                                                    "Firestore save on import failed: $result"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        extraTopPadding = innerPadding.calculateTopPadding(),
                        extraBottomPadding = innerPadding.calculateBottomPadding()
                    )
                }

                showNotificationsScreen -> {
                    NotificationsScreen(
                        notifications = notifications,
                        errorMessage = notificationsErrorMessage,
                        onBack = { showNotificationsScreen = false },
                        onNotificationClick = { notification ->
                            markNotificationAsRead(notification.id)
                            showNotificationsScreen = false
                            currentTab = AppTab.FRIENDS
                            refreshNotifications()
                        },
                        onMarkAllAsRead = { markAllNotificationsAsRead() },
                        extraTopPadding = innerPadding.calculateTopPadding(),
                        extraBottomPadding = innerPadding.calculateBottomPadding()
                    )
                }

                selectedFindingDetail != null -> {
                    val findingDetail = selectedFindingDetail ?: return@Box
                    FundDetailScreen(
                        modifier = Modifier.padding(innerPadding),
                        finding = findingDetail,
                        animal = selectedFindingAnimal,
                        onBackClick = {
                            selectedFindingDetail = null
                            selectedFindingDetailSource = null
                        },
                        onEditFinding = { finding ->
                            val detailSource = selectedFindingDetailSource
                            selectedFindingDetail = null
                            selectedFindingToEdit = finding
                            selectedAnimalId = finding.animalId
                            findingEditReturnSource = detailSource ?: currentTab.name
                            startInFindingEditMode = true
                            openCreateFindingMode = false
                        },
                        onOpenAnimalDetails = { finding ->
                            selectedFindingDetail = null
                            selectedFindingToEdit = null
                            selectedAnimalId = finding.animalId
                            startInFindingEditMode = false
                            openCreateFindingMode = false
                        }
                    )
                }

                selectedAnimal != null -> {
                    AnimalDetailScreen(
                        modifier = Modifier.padding(innerPadding),
                        currentUserId = currentOwnerId,
                        currentDisplayName = currentDisplayName,
                        animal = selectedAnimal,
                        findings = findingsFromRoom.filter { it.animalId == selectedAnimal.id },
                        storageDebug = storageDebug,
                        initialFinding = selectedFindingToEdit,
                        startInCreateMode = openCreateFindingMode && selectedFindingToEdit == null,
                        startInFindingEditMode = startInFindingEditMode,
                        dailyAnimalHistoryText = selectedAnimalDailyAnimalHistoryText,
                        onOpenFindingDetail = { finding ->
                            selectedFindingDetail = finding
                            selectedFindingDetailSource = FINDING_NAV_SOURCE_ANIMAL_DETAIL
                            selectedFindingToEdit = null
                            findingEditReturnSource = FINDING_NAV_SOURCE_ANIMAL_DETAIL
                            startInFindingEditMode = false
                            openCreateFindingMode = false
                        },
                        onReturnToFindingDetail = { finding ->
                            selectedFindingDetail = finding
                            selectedFindingDetailSource = FINDING_NAV_SOURCE_ANIMAL_DETAIL
                            selectedAnimalId = null
                            selectedFindingToEdit = null
                            findingEditReturnSource = FINDING_NAV_SOURCE_ANIMAL_DETAIL
                            startInFindingEditMode = false
                            openCreateFindingMode = false
                        },
                        onBackClick = {
                            if (startInFindingEditMode && selectedFindingToEdit != null) {
                                selectedFindingDetail = selectedFindingToEdit
                                selectedFindingDetailSource = findingEditReturnSource
                                selectedAnimalId = null
                                selectedFindingToEdit = null
                                startInFindingEditMode = false
                                openCreateFindingMode = false
                            } else {
                                resetSearchState()
                                selectedAnimalId = null
                                selectedFindingDetailSource = null
                                selectedFindingToEdit = null
                                findingEditReturnSource = null
                                startInFindingEditMode = false
                                openCreateFindingMode = false
                            }
                        },
                        onSaveFinding = { finding ->
                            scope.launch {
                                val saveStartedAt = SystemClock.elapsedRealtime()
                                val localPhotoCountBeforeUpload = effectiveLocalPhotoUris(finding).size
                                Log.d(
                                    "FindingSaveTiming",
                                    "onSaveFinding start animalId=${finding.animalId} localPhotoCount=$localPhotoCountBeforeUpload"
                                )
                                val ownerIdForUpload = currentOwnerId
                                val localFinding = finding.copy(
                                    ownerId = ownerIdForUpload,
                                    photoUri = effectiveLocalPhotoUris(finding).firstOrNull().orEmpty(),
                                    remotePhotoPath = effectiveRemotePhotoPaths(finding).firstOrNull().orEmpty(),
                                    thumbnailRemotePhotoPath = finding.thumbnailRemotePhotoPath,
                                    photoUris = effectiveLocalPhotoUris(finding),
                                    remotePhotoPaths = effectiveRemotePhotoPaths(finding)
                                )
                                val previousFindings = findingsFromRoom
                                val questCelebration = detectQuestLevelUpMessage(
                                    previousFindings = previousFindings,
                                    newFinding = localFinding,
                                    animals = animals
                                )

                                val roomInsertStartedAt = SystemClock.elapsedRealtime()
                                Log.d(
                                    "FindingSaveTiming",
                                    "dao.insertFinding start animalId=${localFinding.animalId}"
                                )
                                val insertedRowId = dao.insertFinding(
                                    localFinding.toEntity(ownerIdOverride = currentOwnerId)
                                )
                                Log.d(
                                    "FindingSaveTiming",
                                    "dao.insertFinding end animalId=${localFinding.animalId} durationMs=${SystemClock.elapsedRealtime() - roomInsertStartedAt} rowId=$insertedRowId"
                                )
                                val localFindingWithRoomId = localFinding.copy(roomId = insertedRowId.toInt())

                                if (wishlistAnimalId == localFindingWithRoomId.animalId) {
                                    wishlistAnimalId = null
                                    prefs.edit().remove(wishlistAnimalKey(preferenceOwnerId)).apply()
                                    wishlistCelebrationMessage = CelebrationMessage(
                                        title = "Wunsch-Fund entdeckt!",
                                        subtitle = "Dein Wunsch-Tier ist jetzt gefunden."
                                    )
                                }

                                questLevelUpMessage = questCelebration
                                Log.d(
                                    "FindingSaveTiming",
                                    "questLevelUpMessage set animalId=${localFindingWithRoomId.animalId} hasQuestCelebration=${questCelebration != null} elapsedMs=${SystemClock.elapsedRealtime() - saveStartedAt}"
                                )
                                Log.d(
                                    "FindingSaveTiming",
                                    "local visible end animalId=${localFindingWithRoomId.animalId} durationMs=${SystemClock.elapsedRealtime() - saveStartedAt}"
                                )

                                if (currentOwnerId != null) {
                                    val cloudSyncStartedAt = SystemClock.elapsedRealtime()
                                    Log.d(
                                        "FindingSaveTiming",
                                        "background/cloud sync start animalId=${localFindingWithRoomId.animalId}"
                                    )
                                    val cloudReadyFinding = if (
                                        !ownerIdForUpload.isNullOrBlank() &&
                                        effectiveLocalPhotoUris(localFindingWithRoomId).isNotEmpty()
                                    ) {
                                        val uploadStartedAt = SystemClock.elapsedRealtime()
                                        Log.d(
                                            "FindingSaveTiming",
                                            "uploadFindingPhotos start animalId=${localFindingWithRoomId.animalId} localPhotoCount=$localPhotoCountBeforeUpload"
                                        )
                                        val remotePhotoPaths = FindingPhotoStorageRepository.uploadFindingPhotos(
                                            context = appContext,
                                            userId = ownerIdForUpload,
                                            finding = localFindingWithRoomId
                                        )
                                        Log.d(
                                            "FindingSaveTiming",
                                            "uploadFindingPhotos end animalId=${localFindingWithRoomId.animalId} durationMs=${SystemClock.elapsedRealtime() - uploadStartedAt} remotePhotoCount=${remotePhotoPaths.size}"
                                        )
                                        val thumbnailUploadStartedAt = SystemClock.elapsedRealtime()
                                        var thumbnailSizeKb: Int? = null
                                        Log.d(
                                            "FindingSaveTiming",
                                            "thumbnail upload start animalId=${localFindingWithRoomId.animalId}"
                                        )
                                        val uploadedThumbnailPath = FindingPhotoStorageRepository.uploadFindingThumbnail(
                                            context = appContext,
                                            userId = ownerIdForUpload,
                                            finding = localFindingWithRoomId,
                                            onPrepared = { preparedSizeKb ->
                                                thumbnailSizeKb = preparedSizeKb
                                            }
                                        )
                                        Log.d(
                                            "FindingSaveTiming",
                                            "thumbnail upload end animalId=${localFindingWithRoomId.animalId} durationMs=${SystemClock.elapsedRealtime() - thumbnailUploadStartedAt} success=${uploadedThumbnailPath.isNotBlank()} sizeKb=${thumbnailSizeKb ?: -1}"
                                        )
                                        localFindingWithRoomId.copy(
                                            remotePhotoPath = remotePhotoPaths.firstOrNull().orEmpty(),
                                            thumbnailRemotePhotoPath = uploadedThumbnailPath,
                                            remotePhotoPaths = remotePhotoPaths
                                        )
                                    } else {
                                        Log.d(
                                            "FindingSaveTiming",
                                            "uploadFindingPhotos skipped animalId=${localFindingWithRoomId.animalId} loggedIn=${!ownerIdForUpload.isNullOrBlank()} localPhotoCount=$localPhotoCountBeforeUpload"
                                        )
                                        localFindingWithRoomId
                                    }

                                    val roomRemoteUpdateStartedAt = SystemClock.elapsedRealtime()
                                    Log.d(
                                        "FindingSaveTiming",
                                        "local remote fields update start animalId=${cloudReadyFinding.animalId}"
                                    )
                                    dao.updateFinding(
                                        cloudReadyFinding.toEntity(
                                            ownerIdOverride = currentOwnerId,
                                            roomIdOverride = insertedRowId.toInt()
                                        )
                                    )
                                    Log.d(
                                        "FindingSaveTiming",
                                        "local remote fields update end animalId=${cloudReadyFinding.animalId} durationMs=${SystemClock.elapsedRealtime() - roomRemoteUpdateStartedAt}"
                                    )

                                    val firestoreSaveStartedAt = SystemClock.elapsedRealtime()
                                    Log.d(
                                        "FindingSaveTiming",
                                        "Firestore save start animalId=${cloudReadyFinding.animalId} remotePhotoCount=${effectiveRemotePhotoPaths(cloudReadyFinding).size}"
                                    )
                                    FirestoreFindingRepository.saveCurrentUserFinding(cloudReadyFinding) { success, result ->
                                        Log.d(
                                            "FindingSaveTiming",
                                            "Firestore save end animalId=${cloudReadyFinding.animalId} success=$success durationMs=${SystemClock.elapsedRealtime() - firestoreSaveStartedAt}"
                                        )
                                        if (!success) {
                                            Log.e(
                                                "CloudWrite",
                                                "Firestore save on create failed: $result"
                                            )
                                            Log.d(
                                                "FindingSaveTiming",
                                                "cloud sync total end animalId=${cloudReadyFinding.animalId} durationMs=${SystemClock.elapsedRealtime() - cloudSyncStartedAt} firestoreSaveFailed=true"
                                            )
                                        } else {
                                            val contributionStartedAt = SystemClock.elapsedRealtime()
                                            Log.d(
                                                "FindingSaveTiming",
                                                "Contribution update start animalId=${cloudReadyFinding.animalId}"
                                            )
                                            FirestoreFindingRepository.recordGlobalFindingContributionIfNeeded(
                                                ownerUid = currentOwnerId.orEmpty(),
                                                finding = cloudReadyFinding
                                            ) { counterSuccess, counterResult ->
                                                Log.d(
                                                    "FindingSaveTiming",
                                                    "Contribution update end animalId=${cloudReadyFinding.animalId} success=$counterSuccess result=$counterResult durationMs=${SystemClock.elapsedRealtime() - contributionStartedAt}"
                                                )
                                                if (!counterSuccess) {
                                                    Log.e(
                                                        "CloudWrite",
                                                        "global finding contribution on create failed: $counterResult"
                                                    )
                                                } else {
                                                    if (counterResult == "created") {
                                                        updateAnimalGlobalFindingCountLocally(
                                                            animalId = cloudReadyFinding.animalId,
                                                            delta = 1
                                                        )
                                                    }
                                                }
                                                refreshAnimalGlobalFindingCounts()
                                                Log.d(
                                                    "FindingSaveTiming",
                                                    "cloud sync total end animalId=${cloudReadyFinding.animalId} durationMs=${SystemClock.elapsedRealtime() - cloudSyncStartedAt}"
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Log.d(
                                        "FindingSaveTiming",
                                        "cloud sync skipped animalId=${localFindingWithRoomId.animalId} firestoreSkipped=true"
                                    )
                                }
                            }
                        },
                        onDeleteFinding = { finding ->
                            scope.launch {
                                val roomMatch = if (finding.roomId != null) {
                                    allFindings.lastOrNull { it.id == finding.roomId }
                                } else {
                                    allFindings.lastOrNull {
                                        it.animalId == finding.animalId &&
                                                it.date == finding.date &&
                                                it.location == finding.location &&
                                                it.note == finding.note &&
                                                it.photoUri == finding.photoUri
                                    }
                                }

                                if (roomMatch != null) {
                                    dao.deleteFinding(roomMatch)
                                    Log.d(
                                        "CloudSyncDelete",
                                        "Deleted local finding: animalId=${finding.animalId}, date=${finding.date}, location=${finding.location}"
                                    )

                                    val returnSource = findingEditReturnSource
                                    selectedFindingDetail = null
                                    selectedFindingDetailSource = null
                                    selectedFindingToEdit = null
                                    startInFindingEditMode = false
                                    openCreateFindingMode = false
                                    if (returnSource == FINDING_NAV_SOURCE_ANIMAL_DETAIL) {
                                        selectedAnimalId = finding.animalId
                                    } else {
                                        selectedAnimalId = null
                                        returnSource
                                            ?.takeIf { it.isNotBlank() }
                                            ?.runCatching { AppTab.valueOf(this) }
                                            ?.getOrNull()
                                            ?.let { returnTab ->
                                                currentTab = returnTab
                                            }
                                    }
                                    findingEditReturnSource = null

                                    if (currentOwnerId != null) {
                                        FirestoreFindingRepository.deleteCurrentUserFinding(finding) { success, result ->
                                            if (success) {
                                                Log.d(
                                                    "CloudSyncDelete",
                                                    "Deleted Firestore finding: documentId=$result"
                                                )
                                                FirestoreFindingRepository.removeGlobalFindingContributionIfExists(
                                                    ownerUid = currentOwnerId.orEmpty(),
                                                    finding = finding
                                                ) { counterSuccess, counterResult ->
                                                    if (!counterSuccess) {
                                                        Log.e(
                                                            "CloudSyncDelete",
                                                            "global finding contribution removal on delete failed: $counterResult"
                                                        )
                                                    } else {
                                                        if (counterResult == "removed") {
                                                            updateAnimalGlobalFindingCountLocally(
                                                                animalId = finding.animalId,
                                                                delta = -1
                                                            )
                                                        }
                                                    }
                                                    refreshAnimalGlobalFindingCounts()
                                                }
                                            } else {
                                                Log.e(
                                                    "CloudSyncDelete",
                                                    "Delete in Firestore failed: $result"
                                                )
                                            }
                                        }
                                    } else {
                                        Log.d(
                                            "CloudSyncDelete",
                                            "Skipped Firestore delete because no user is logged in"
                                        )
                                    }
                                } else {
                                    Log.d(
                                        "CloudSyncDelete",
                                        "Skipped local delete because no matching Room finding was found"
                                    )
                                }
                            }
                        },
                        onUpdateFinding = { oldFinding, newFinding ->
                            scope.launch {
                                val updateStartedAt = SystemClock.elapsedRealtime()
                                val oldLocalPhotoCount = effectiveLocalPhotoUris(oldFinding).size
                                val newLocalPhotoCount = effectiveLocalPhotoUris(newFinding).size
                                val oldRemotePhotoCount = effectiveRemotePhotoPaths(oldFinding).size
                                Log.d(
                                    "FindingUpdateTiming",
                                    "onUpdateFinding start animalId=${oldFinding.animalId} oldLocalPhotoCount=$oldLocalPhotoCount newLocalPhotoCount=$newLocalPhotoCount oldRemotePhotoCount=$oldRemotePhotoCount"
                                )
                                val ownerIdForUpload = currentOwnerId
                                val preparedNewFinding = if (
                                    !ownerIdForUpload.isNullOrBlank() &&
                                    effectiveLocalPhotoUris(newFinding).isNotEmpty()
                                ) {
                                    val newLocalPhotoUris = effectiveLocalPhotoUris(newFinding)
                                    val oldLocalPhotoUris = effectiveLocalPhotoUris(oldFinding)
                                    val shouldUploadPhoto =
                                        newLocalPhotoUris != oldLocalPhotoUris ||
                                            effectiveRemotePhotoPaths(newFinding).isEmpty()
                                    val shouldUploadThumbnail =
                                        newLocalPhotoUris.firstOrNull() != oldLocalPhotoUris.firstOrNull() ||
                                            oldFinding.thumbnailRemotePhotoPath.isBlank()
                                    Log.d(
                                        "FindingUpdateTiming",
                                        "photo comparison animalId=${oldFinding.animalId} localPhotosUnchanged=${!shouldUploadPhoto} uploadExecuted=$shouldUploadPhoto thumbnailRetained=${!shouldUploadThumbnail}"
                                    )
                                    val remotePhotoPaths = if (shouldUploadPhoto) {
                                        val uploadStartedAt = SystemClock.elapsedRealtime()
                                        Log.d(
                                            "FindingUpdateTiming",
                                            "uploadFindingPhotos start animalId=${oldFinding.animalId} newLocalPhotoCount=$newLocalPhotoCount"
                                        )
                                        val uploadedPhotoPaths = FindingPhotoStorageRepository.uploadFindingPhotos(
                                            context = appContext,
                                            userId = ownerIdForUpload,
                                            finding = newFinding
                                        )
                                        Log.d(
                                            "FindingUpdateTiming",
                                            "uploadFindingPhotos end animalId=${oldFinding.animalId} durationMs=${SystemClock.elapsedRealtime() - uploadStartedAt} remotePhotoCount=${uploadedPhotoPaths.size}"
                                        )
                                        uploadedPhotoPaths
                                    } else {
                                        effectiveRemotePhotoPaths(newFinding).ifEmpty {
                                            effectiveRemotePhotoPaths(oldFinding)
                                        }
                                    }
                                    var thumbnailRemotePhotoPath = if (shouldUploadThumbnail) {
                                        val thumbnailUploadStartedAt = SystemClock.elapsedRealtime()
                                        var thumbnailSizeKb: Int? = null
                                        Log.d(
                                            "FindingUpdateTiming",
                                            "thumbnail upload start animalId=${oldFinding.animalId}"
                                        )
                                        val uploadedThumbnailPath = FindingPhotoStorageRepository.uploadFindingThumbnail(
                                            context = appContext,
                                            userId = ownerIdForUpload,
                                            finding = newFinding,
                                            onPrepared = { preparedSizeKb ->
                                                thumbnailSizeKb = preparedSizeKb
                                            }
                                        )
                                        val finalThumbnailPath = if (uploadedThumbnailPath.isNotBlank()) {
                                            uploadedThumbnailPath
                                        } else {
                                            oldFinding.thumbnailRemotePhotoPath.ifBlank {
                                                newFinding.thumbnailRemotePhotoPath
                                            }
                                        }
                                        Log.d(
                                            "FindingUpdateTiming",
                                            "thumbnail upload end animalId=${oldFinding.animalId} durationMs=${SystemClock.elapsedRealtime() - thumbnailUploadStartedAt} success=${uploadedThumbnailPath.isNotBlank()} sizeKb=${thumbnailSizeKb ?: -1}"
                                        )
                                        finalThumbnailPath
                                    } else {
                                        oldFinding.thumbnailRemotePhotoPath.ifBlank {
                                            newFinding.thumbnailRemotePhotoPath
                                        }
                                    }
                                    newFinding.copy(
                                        ownerId = ownerIdForUpload,
                                        photoUri = effectiveLocalPhotoUris(newFinding).firstOrNull().orEmpty(),
                                        remotePhotoPath = remotePhotoPaths.firstOrNull().orEmpty(),
                                        thumbnailRemotePhotoPath = thumbnailRemotePhotoPath,
                                        photoUris = effectiveLocalPhotoUris(newFinding),
                                        remotePhotoPaths = remotePhotoPaths
                                    )
                                } else {
                                    Log.d(
                                        "FindingUpdateTiming",
                                        "uploadFindingPhotos skipped animalId=${oldFinding.animalId} loggedIn=${!ownerIdForUpload.isNullOrBlank()} newLocalPhotoCount=$newLocalPhotoCount"
                                    )
                                    newFinding.copy(
                                        ownerId = ownerIdForUpload,
                                        photoUri = effectiveLocalPhotoUris(newFinding).firstOrNull().orEmpty(),
                                        remotePhotoPath = effectiveRemotePhotoPaths(newFinding)
                                            .ifEmpty { effectiveRemotePhotoPaths(oldFinding) }
                                            .firstOrNull()
                                            .orEmpty(),
                                        thumbnailRemotePhotoPath = newFinding.thumbnailRemotePhotoPath.ifBlank {
                                            oldFinding.thumbnailRemotePhotoPath
                                        },
                                        photoUris = effectiveLocalPhotoUris(newFinding),
                                        remotePhotoPaths = effectiveRemotePhotoPaths(newFinding)
                                            .ifEmpty { effectiveRemotePhotoPaths(oldFinding) }
                                    )
                                }
                                if (startInFindingEditMode) {
                                    selectedFindingDetail = preparedNewFinding
                                    selectedAnimalId = null
                                    selectedFindingToEdit = null
                                    startInFindingEditMode = false
                                    openCreateFindingMode = false
                                } else {
                                    selectedFindingToEdit = preparedNewFinding
                                }
                                val roomMatch = if (oldFinding.roomId != null) {
                                    allFindings.lastOrNull { it.id == oldFinding.roomId }
                                } else {
                                    allFindings.lastOrNull {
                                        it.animalId == oldFinding.animalId &&
                                                it.date == oldFinding.date &&
                                                it.location == oldFinding.location &&
                                                it.note == oldFinding.note &&
                                                it.photoUri == oldFinding.photoUri
                                    }
                                }

                                if (roomMatch != null) {
                                    val roomUpdateStartedAt = SystemClock.elapsedRealtime()
                                    Log.d(
                                        "FindingUpdateTiming",
                                        "dao.updateFinding start animalId=${preparedNewFinding.animalId}"
                                    )
                                    dao.updateFinding(
                                        preparedNewFinding.toEntity(
                                            ownerIdOverride = currentOwnerId,
                                            roomIdOverride = roomMatch.id
                                        )
                                    )
                                    Log.d(
                                        "FindingUpdateTiming",
                                        "dao.updateFinding end animalId=${preparedNewFinding.animalId} durationMs=${SystemClock.elapsedRealtime() - roomUpdateStartedAt}"
                                    )

                                    if (currentOwnerId != null) {
                                        val firestoreUpdateStartedAt = SystemClock.elapsedRealtime()
                                        Log.d(
                                            "FindingUpdateTiming",
                                            "Firestore update start oldAnimalId=${oldFinding.animalId} newAnimalId=${preparedNewFinding.animalId}"
                                        )
                                        FirestoreFindingRepository.updateCurrentUserFinding(
                                            oldFinding,
                                            preparedNewFinding
                                        ) { success, result ->
                                            Log.d(
                                                "FindingUpdateTiming",
                                                "Firestore update end oldAnimalId=${oldFinding.animalId} newAnimalId=${preparedNewFinding.animalId} success=$success durationMs=${SystemClock.elapsedRealtime() - firestoreUpdateStartedAt}"
                                            )
                                            if (!success) {
                                                Log.e(
                                                    "CloudWrite",
                                                    "Firestore update on edit failed: $result"
                                                )
                                            } else {
                                                FirestoreFindingRepository.updateGlobalFindingContributionForChange(
                                                    ownerUid = currentOwnerId.orEmpty(),
                                                    oldFinding = oldFinding,
                                                    newFinding = preparedNewFinding
                                                ) { counterSuccess, counterResult ->
                                                    Log.d(
                                                        "FindingUpdateTiming",
                                                        "Contribution update end oldAnimalId=${oldFinding.animalId} newAnimalId=${preparedNewFinding.animalId} success=$counterSuccess result=$counterResult totalDurationMs=${SystemClock.elapsedRealtime() - updateStartedAt}"
                                                    )
                                                    if (!counterSuccess) {
                                                        Log.e(
                                                            "CloudWrite",
                                                            "global finding contribution update on edit failed: $counterResult"
                                                        )
                                                    }
                                                    refreshAnimalGlobalFindingCounts()
                                                }
                                            }
                                        }
                                    } else {
                                        Log.d(
                                            "FindingUpdateTiming",
                                            "onUpdateFinding end animalId=${preparedNewFinding.animalId} durationMs=${SystemClock.elapsedRealtime() - updateStartedAt} firestoreSkipped=true"
                                        )
                                    }
                                } else {
                                    Log.w(
                                        "FindingUpdateTiming",
                                        "onUpdateFinding skipped because no Room match was found animalId=${oldFinding.animalId}"
                                    )
                                }
                            }
                        },
                        onSetFavoriteFindingAnimal = { animal ->
                            val animalHasFinding = findingsFromRoom.any { it.animalId == animal.id }
                            if (animalHasFinding) {
                                favoriteAnimalId = animal.id
                                prefs.edit()
                                    .putString(favoriteAnimalKey(preferenceOwnerId), animal.id)
                                    .apply()
                            }
                        },
                        currentFavoriteAnimalId = favoriteAnimalId,
                        onSetWishlistAnimal = { animal ->
                            val animalHasFinding = findingsFromRoom.any { it.animalId == animal.id }
                            if (!animalHasFinding && wishlistAnimalId != animal.id) {
                                wishlistAnimalId = animal.id
                                prefs.edit()
                                    .putString(wishlistAnimalKey(preferenceOwnerId), animal.id)
                                    .apply()
                            }
                        },
                        currentWishlistAnimalId = wishlistAnimalId,
                    )
                }


                showAnimalPicker -> {
                    AnimalListScreen(
                        onOpenSettings = {
                            resetSearchState()
                            showSettingsScreen = true
                        },
                        debugMessage = "Wähle ein Tier für einen neuen Fund",
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        animals = sortedAnimals,
                        totalAnimalCount = animals.size,
                        collectedAnimalCount = collectedAnimalCount,
                        showFoundOnly = showFoundOnly,
                        onToggleShowFoundOnly = { showFoundOnly = !showFoundOnly },
                        currentSortOption = selectedSortOption,
                        onSortOptionChange = { selectedSortOption = it },
                        onResetFiltersAndSort = {
                            showFoundOnly = false
                            selectedSortOption = "A_Z"
                            selectedGroupFilter = "Alle"
                            selectedSubgroupFilter = "Alle"
                        },
                        availableGroups = groupOptions,
                        selectedGroup = selectedGroupFilter,
                        onSelectedGroupChange = {
                            selectedGroupFilter = it
                            selectedSubgroupFilter = "Alle"
                        },
                        availableSubgroups = subgroupOptions,
                        selectedSubgroup = selectedSubgroupFilter,
                        onSelectedSubgroupChange = { selectedSubgroupFilter = it },
                        findingCountByAnimalId = findingCountByAnimalId,
                        animalGlobalFindingCounts = emptyMap(),
                        animalGlobalFindingCountsLoaded = false,
                        animalGlobalFindingCountsLoadAttempted = true,
                        onAnimalClick = { animal ->
                            resetSearchState()
                            selectedAnimalId = animal.id
                            showAnimalPicker = false
                            selectedFindingToEdit = null
                            startInFindingEditMode = false
                            openCreateFindingMode = true
                        },
                        isPickerMode = true,
                        extraTopPadding = innerPadding.calculateTopPadding(),
                        extraBottomPadding = innerPadding.calculateBottomPadding()
                    )
                }

                currentTab == AppTab.HOME -> {
                    HomeScreen(
                        collectedAnimalCount = collectedAnimalCount,
                        totalAnimalCount = animals.size,
                        totalFindings = findingsFromRoom.size,
                        findings = findingsFromRoom,
                        animals = animals,
                        dailyAnimal = dailyAnimal,
                        favoriteAnimalId = favoriteAnimalId,
                        wishlistAnimalId = wishlistAnimalId,
                        roomFindingsCount = allFindings.size,
                        onOpenDailyAnimal = {
                            if (dailyAnimal != null) {
                                isDailyAnimalOpenedFromHomeTile = true
                                showDailyAnimalScreen = true
                            }
                        },
                        onEditFinding = { finding ->
                            selectedFindingDetail = finding
                            selectedFindingDetailSource = currentTab.name
                            selectedFindingToEdit = null
                            findingEditReturnSource = currentTab.name
                            startInFindingEditMode = false
                            openCreateFindingMode = false
                        },
                        extraTopPadding = innerPadding.calculateTopPadding(),
                        extraBottomPadding = innerPadding.calculateBottomPadding()
                    )
                }

                currentTab == AppTab.FRIENDS -> {
                    val selectedFriendUserId = selectedFriendProfileUserId
                    if (selectedFriendUserId != null) {
                        FriendProfileScreen(
                            currentUserId = currentOwnerId,
                            friendUserId = selectedFriendUserId,
                            initialDisplayName = selectedFriendProfileDisplayName,
                            allAnimals = animals,
                            onBack = {
                                selectedFriendProfileUserId = null
                                selectedFriendProfileDisplayName = null
                            },
                            extraTopPadding = innerPadding.calculateTopPadding(),
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    } else {
                        FriendsScreen(
                            friendFeedCacheDao = friendFeedCacheDao,
                            currentUserId = currentOwnerId,
                            currentDisplayName = currentDisplayName,
                            allAnimals = animals,
                            isFriendSearchOpen = isFriendSearchOpen,
                            onCloseFriendSearch = { isFriendSearchOpen = false },
                            onIncomingRequestsChanged = { refreshNotifications() },
                            onOpenFriendProfile = { friendUserId, friendDisplayName ->
                                selectedFriendProfileUserId = friendUserId
                                selectedFriendProfileDisplayName = friendDisplayName
                                isFriendSearchOpen = false
                            },
                            extraTopPadding = innerPadding.calculateTopPadding(),
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }
                }

                currentTab == AppTab.STATS -> {
                    if (showTierdexMapScreen) {
                        TierdexMapScreen(
                            findings = findingsFromRoom,
                            extraTopPadding = innerPadding.calculateTopPadding(),
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    } else {
                        AnimalListScreen(
                            onOpenSettings = {
                                resetSearchState()
                                showSettingsScreen = true
                            },
                            debugMessage = "Mein Tierdex",
                            searchText = searchText,
                            onSearchTextChange = { searchText = it },
                            animals = sortedAnimals,
                            totalAnimalCount = animals.size,
                            collectedAnimalCount = collectedAnimalCount,
                            showFoundOnly = showFoundOnly,
                            onToggleShowFoundOnly = { showFoundOnly = !showFoundOnly },
                            currentSortOption = selectedSortOption,
                            onSortOptionChange = { selectedSortOption = it },
                            onResetFiltersAndSort = {
                                showFoundOnly = false
                                selectedSortOption = "A_Z"
                                selectedGroupFilter = "Alle"
                                selectedSubgroupFilter = "Alle"
                            },
                            availableGroups = groupOptions,
                            selectedGroup = selectedGroupFilter,
                            onSelectedGroupChange = {
                                selectedGroupFilter = it
                                selectedSubgroupFilter = "Alle"
                            },
                            availableSubgroups = subgroupOptions,
                            selectedSubgroup = selectedSubgroupFilter,
                            onSelectedSubgroupChange = { selectedSubgroupFilter = it },
                            findingCountByAnimalId = findingCountByAnimalId,
                            animalGlobalFindingCounts = animalGlobalFindingCounts,
                            animalGlobalFindingCountsLoaded = animalGlobalFindingCountsLoaded,
                            animalGlobalFindingCountsLoadAttempted = animalGlobalFindingCountsLoadAttempted,
                            onAnimalClick = { animal ->
                                resetSearchState()
                                selectedAnimalId = animal.id
                                selectedFindingToEdit = null
                                startInFindingEditMode = false
                                openCreateFindingMode = false
                            },
                            onOpenMap = { showTierdexMapScreen = true },
                            extraTopPadding = innerPadding.calculateTopPadding(),
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }
                }

                currentTab == AppTab.PROFILE -> {
                    if (showProfileFriendsScreen) {
                        ProfileFriendsScreen(
                            currentUserId = currentOwnerId,
                            onBack = { showProfileFriendsScreen = false },
                            extraTopPadding = innerPadding.calculateTopPadding(),
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    } else if (showProfilePhotoGalleryScreen) {
                        ProfilePhotoGalleryScreen(
                            findings = findingsFromRoom,
                            onBack = { showProfilePhotoGalleryScreen = false },
                            onOpenFinding = { finding ->
                                selectedFindingDetail = finding
                                selectedFindingDetailSource = currentTab.name
                                selectedFindingToEdit = null
                                findingEditReturnSource = currentTab.name
                                startInFindingEditMode = false
                                openCreateFindingMode = false
                            },
                            extraTopPadding = innerPadding.calculateTopPadding(),
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    } else {
                        ProfileScreen(
                            currentUserId = currentOwnerId,
                            currentDisplayName = currentDisplayName,
                            onDisplayNameSaved = { newDisplayName ->
                                currentDisplayName = newDisplayName
                            },
                            collectedAnimalCount = collectedAnimalCount,
                            totalFindings = allFindings.size,
                            findings = findingsFromRoom,
                            animals = animals,
                            favoriteAnimalId = favoriteAnimalId,
                            wishlistAnimalId = wishlistAnimalId,
                            onEditFinding = { finding ->
                                selectedFindingDetail = finding
                                selectedFindingDetailSource = currentTab.name
                                selectedFindingToEdit = null
                                findingEditReturnSource = currentTab.name
                                startInFindingEditMode = false
                                openCreateFindingMode = false
                            },
                            onOpenFriends = {
                                showProfilePhotoGalleryScreen = false
                                showProfileFriendsScreen = true
                            },
                            onOpenPhotoGallery = {
                                showProfileFriendsScreen = false
                                showProfilePhotoGalleryScreen = true
                            },
                            profileCollectionListState = profileCollectionListState,
                            profileCollectionSortOrder = profileCollectionSortOrder,
                            onProfileCollectionSortOrderChange = {
                                profileCollectionSortOrder = it
                            },
                            profileCollectionDateFilter = profileCollectionDateFilter,
                            onProfileCollectionDateFilterChange = {
                                profileCollectionDateFilter = it
                            },
                            extraTopPadding = innerPadding.calculateTopPadding(),
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }
                }
            }
            CelebrationBanner(
                visible = wishlistCelebrationMessage != null,
                message = wishlistCelebrationMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
            )
            CelebrationBanner(
                visible = questLevelUpMessage != null,
                message = questLevelUpMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = innerPadding.calculateTopPadding() + 108.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
            )

            if (showDailyAnimalScreen && dailyAnimal != null) {
                Dialog(
                    onDismissRequest = {
                        prefs.edit()
                            .putBoolean(dailyAnimalDismissedKey(preferenceOwnerId), true)
                            .apply()
                        isDailyAnimalOpenedFromHomeTile = false
                        showDailyAnimalScreen = false
                    },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        color = Color.White
                    ) {
                        DailyAnimalScreen(
                            animal = dailyAnimal,
                            currentUserId = currentOwnerId,
                            dailyAnimalHistoryText = dailyAnimalHistoryText,
                            showCloseButton = !isDailyAnimalOpenedFromHomeTile,
                            onClose = {
                                prefs.edit()
                                    .putBoolean(dailyAnimalDismissedKey(preferenceOwnerId), true)
                                    .apply()
                                isDailyAnimalOpenedFromHomeTile = false
                                showDailyAnimalScreen = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CelebrationBanner(
    visible: Boolean,
    message: CelebrationMessage?,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        label = "celebrationScale"
    )

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }) + scaleIn(initialScale = 0.92f),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 3 }) + scaleOut(targetScale = 0.96f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = PrimaryGreenSoft.copy(alpha = 0.96f)
            ),
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✦", color = PrimaryGreen, style = MaterialTheme.typography.titleLarge)
                    Text(text = "★", color = PrimaryGreen, style = MaterialTheme.typography.headlineSmall)
                    Text(text = "✦", color = PrimaryGreen, style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    text = message?.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = message?.subtitle.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun TierdexTopBar(
    currentTab: AppTab,
    showFriendSearchAction: Boolean,
    isFriendSearchOpen: Boolean,
    incomingRequestCount: Int,
    onFriendSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tierdex01_playstore),
                    contentDescription = "Tierdex Logo",
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Tierdex",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.width(132.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.width(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showFriendSearchAction && currentTab == AppTab.FRIENDS) {
                        IconButton(onClick = onFriendSearchClick) {
                            Icon(
                                imageVector = if (isFriendSearchOpen) {
                                    Icons.Filled.Close
                                } else {
                                    Icons.Filled.Search
                                },
                                contentDescription = if (isFriendSearchOpen) {
                                    "Freundesuche schließen"
                                } else {
                                    "Freundesuche öffnen"
                                },
                                tint = TextPrimary
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier.width(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Freundesanfragen",
                            tint = TextPrimary
                        )
                    }
                    if (incomingRequestCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .background(Color(0xFFD84C4C), CircleShape)
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (incomingRequestCount > 9) "9+" else incomingRequestCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier.width(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Einstellungen",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    notifications: List<TierdexNotification>,
    errorMessage: String?,
    onBack: () -> Unit,
    onNotificationClick: (TierdexNotification) -> Unit,
    onMarkAllAsRead: () -> Unit,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    val unreadCount = notifications.count { !it.isRead }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                top = 16.dp + extraTopPadding,
                end = 16.dp
            ),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = extraBottomPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Benachrichtigungen",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (unreadCount > 0) {
                            "$unreadCount neu"
                        } else {
                            "Keine neuen Benachrichtigungen"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                OutlinedButton(
                    onClick = onBack,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text("Schließen")
                }
            }
        }

        if (notifications.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onMarkAllAsRead,
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Text("Alle als gelesen markieren")
                    }
                }
            }
        }

        errorMessage?.let { message ->
            item {
                CompactSectionError(
                    summary = "Benachrichtigungen konnten gerade nicht geladen werden.",
                    technicalDetails = message
                )
            }
        }

        if (notifications.isEmpty()) {
            item {
                Text(
                    text = "Noch keine Benachrichtigungen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            items(notifications, key = { it.id }) { notification ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNotificationClick(notification) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (notification.isRead) {
                            CardBackground
                        } else {
                            PrimaryGreenSoft.copy(alpha = 0.32f)
                        },
                        contentColor = TextPrimary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        if (notification.createdAtText.isNotBlank()) {
                            Text(
                                text = notification.createdAtText,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = currentTab == AppTab.HOME,
            onClick = { onTabSelected(AppTab.HOME) },
            icon = {},
            label = { Text("Start") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = currentTab == AppTab.FRIENDS,
            onClick = { onTabSelected(AppTab.FRIENDS) },
            icon = {},
            label = { Text("Freunde") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = currentTab == AppTab.STATS,
            onClick = { onTabSelected(AppTab.STATS) },
            icon = {},
            label = { Text("Mein Tierdex") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = currentTab == AppTab.PROFILE,
            onClick = { onTabSelected(AppTab.PROFILE) },
            icon = {},
            label = { Text("Profil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
    }
}

@Composable
fun HomeSectionTitle(
    title: String,
    subtitle: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun CompactSectionError(
    summary: String,
    technicalDetails: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = summary,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9A3D3D)
        )
        Text(
            text = technicalDetails,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
fun HomeStatTile(
    title: String,
    value: String,
    supportingText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground,
            contentColor = TextPrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            if (supportingText.isNotBlank()) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun HomeQuestCompactCard(
    quest: QuestUiModel,
    modifier: Modifier = Modifier
) {
    val completedCardColor = PrimaryGreenSoft.copy(alpha = 0.9f)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (quest.isCompleted) completedCardColor else CardBackground,
            contentColor = TextPrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = quest.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${quest.shownProgress}/${quest.goal}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = if (quest.isCompleted) "Geschafft" else quest.percentLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (quest.isCompleted) PrimaryGreen else TextSecondary
                )
            }
            if (!quest.isCompleted) {
                LinearProgressIndicator(
                    progress = { quest.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = PrimaryGreen,
                    trackColor = PrimaryGreenSoft.copy(alpha = 0.45f)
                )
            }
            Text(
                text = quest.encouragementLabel,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun HomeScreen(
    collectedAnimalCount: Int,
    totalAnimalCount: Int,
    totalFindings: Int,
    findings: List<AnimalFinding>,
    animals: List<AnimalEntry>,
    dailyAnimal: AnimalEntry?,
    favoriteAnimalId: String?,
    wishlistAnimalId: String?,
    onOpenDailyAnimal: () -> Unit,
    onEditFinding: (AnimalFinding) -> Unit,
    roomFindingsCount: Int,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    val latestFinding = findings.firstOrNull()
    val animalById = remember(animals) { animals.associateBy { it.id } }
    val latestAnimal = latestFinding?.animalId?.let { animalById[it] }
    val wishlistAnimal = wishlistAnimalId?.let { animalById[it] }

    val photoFindingCount = findings.count { it.photoUri.isNotBlank() }
    val findingsWithLocationCount = findings.count { it.latitude != null && it.longitude != null }
    val collectionPercent = if (totalAnimalCount > 0) {
        (collectedAnimalCount.toFloat() / totalAnimalCount.toFloat()) * 100f
    } else {
        0f
    }
    val collectionProgress = (collectionPercent / 100f).coerceIn(0f, 1f)
    val collectionPercentLabel = String.format(Locale.GERMANY, "%.2f %%", collectionPercent.toDouble())
    val quests =
        remember(findings, animals, dailyAnimal, collectedAnimalCount, totalFindings, photoFindingCount) {
            buildHomeQuests(
                findings = findings,
                animals = animals,
                dailyAnimal = dailyAnimal,
                collectedAnimalCount = collectedAnimalCount,
                totalFindings = totalFindings,
                photoFindingCount = photoFindingCount
            )
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                top = 16.dp + extraTopPadding,
                end = 16.dp
            ),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = extraBottomPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground,
                    contentColor = TextPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Willkommen zurück",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Du hast $collectedAnimalCount ${if (collectedAnimalCount == 1) "Tier" else "Tiere"} entdeckt",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = collectionPercentLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryGreen
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .border(
                                border = BorderStroke(1.dp, BorderColor),
                                shape = RoundedCornerShape(999.dp)
                            )
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFF6F7F8))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(collectionProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(PrimaryGreen)
                        )
                    }
                    Text(
                        text = "$collectedAnimalCount von $totalAnimalCount Arten entdeckt",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        dailyAnimal?.let { todayAnimal ->
            item {
                Card(
                    onClick = onOpenDailyAnimal,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground,
                        contentColor = TextPrimary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Tier des Tages",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        Text(
                            text = todayAnimal.germanName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = todayAnimal.group,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Heute im Fokus",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            HomeSectionTitle(
                title = "Überblick"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeStatTile(
                    title = "Funde",
                    value = totalFindings.toString(),
                    supportingText = "gespeicherte Beobachtungen",
                    modifier = Modifier.weight(1f)
                )
                HomeStatTile(
                    title = "Entdeckt",
                    value = collectedAnimalCount.toString(),
                    supportingText = "verschiedene Tierarten",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (wishlistAnimal != null) {
                    Card(
                        modifier = Modifier.weight(1.25f),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE7F0E2),
                            contentColor = TextPrimary
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Wunsch-Fund",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = wishlistAnimal.germanName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = wishlistAnimal.latinName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    HomeStatTile(
                        title = "Fundorte",
                        value = findingsWithLocationCount.toString(),
                        supportingText = "mit gespeicherten Koordinaten",
                        modifier = Modifier.weight(0.9f)
                    )
                } else {
                    HomeStatTile(
                        title = "Wunsch-Fund",
                        value = "Offen",
                        supportingText = "",
                        modifier = Modifier.weight(1f)
                    )
                    HomeStatTile(
                        title = "Fundorte",
                        value = findingsWithLocationCount.toString(),
                        supportingText = "mit gespeicherten Koordinaten",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        latestFinding?.let { finding ->
            item {
                val ownPhotoSources = effectiveOwnPhotoSources(latestFinding)
                var currentPhotoPage by remember(latestFinding.roomId, latestFinding.photoUri, latestFinding.photoUris) {
                    mutableStateOf(0)
                }
                Card(
                    onClick = { onEditFinding(finding) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground,
                        contentColor = TextPrimary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HomeSectionTitle(
                            title = "Zuletzt entdeckt"
                        )

                        Text(
                            text = latestAnimal?.germanName ?: "Unbekanntes Tier",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        if (ownPhotoSources.isNotEmpty()) {
                            FindingPhotoPager(
                                photoSources = ownPhotoSources,
                                imageModifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                onPageChanged = { currentPhotoPage = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FindingMetaRow(
                                date = latestFinding.date,
                                location = latestFinding.location,
                                latitude = latestFinding.latitude,
                                longitude = latestFinding.longitude,
                                modifier = Modifier.weight(1f)
                            )
                            FindingPhotoCounter(
                                currentPage = currentPhotoPage,
                                totalCount = ownPhotoSources.size
                            )
                        }
                        latestFinding.note.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        item {
            HomeSectionTitle(
                title = "Quests"
            )
        }

        items(quests.chunked(2)) { questRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                questRow.forEach { quest ->
                    HomeQuestCompactCard(
                        quest = quest,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (questRow.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground,
                    contentColor = TextPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Aktive Challenges",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Hier kommen später wechselnde zeitlich begrenzte Aufgaben hin.")
                    Text("Beispiel: Finde in den nächsten 24 Stunden 2 Vogelarten.")
                }
            }
        }
    }
}

@Composable
fun FindingsMapPreview(
    findings: List<AnimalFinding>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val findingsWithCoordinates = findings.mapNotNull { finding ->
        val latitude = finding.latitude
        val longitude = finding.longitude
        if (latitude == null || longitude == null) {
            null
        } else {
            finding to LatLng(latitude, longitude)
        }
    }

    if (findingsWithCoordinates.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Noch keine Fundorte gespeichert",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val germanyCenter = LatLng(51.1657, 10.4515)
    val clusterItems = remember(findingsWithCoordinates) {
        findingsWithCoordinates.map { (finding, latLng) ->
            FindingClusterItem(
                latLng,
                finding.location.ifBlank { "Fund" },
                buildString {
                    if (finding.date.isNotBlank()) {
                        append(finding.date)
                    }
                    if (finding.location.isNotBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append(finding.location)
                    }
                }
            )
        }
    }
    val hasLocationPermission = remember(context) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(germanyCenter, 5.5f)
    }
    val mapProperties = remember(hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission
        )
    }
    val mapUiSettings = remember(hasLocationPermission) {
        MapUiSettings(
            myLocationButtonEnabled = hasLocationPermission
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        Clustering(
            items = clusterItems
        )
    }
}

@Composable
fun LocationPickerMap(
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    modifier: Modifier = Modifier,
    onLocationSelected: (Double, Double) -> Unit
) {
    val defaultPosition = LatLng(51.1657, 10.4515)
    val initialPosition = if (initialLatitude != null && initialLongitude != null) {
        LatLng(initialLatitude, initialLongitude)
    } else {
        defaultPosition
    }
    var selectedPosition by remember(initialLatitude, initialLongitude) {
        mutableStateOf(
            if (initialLatitude != null && initialLongitude != null) {
                LatLng(initialLatitude, initialLongitude)
            } else {
                null
            }
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            initialPosition,
            if (selectedPosition != null) 14f else 5.5f
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            selectedPosition = latLng
            onLocationSelected(latLng.latitude, latLng.longitude)
        }
    ) {
        selectedPosition?.let { latLng ->
            val markerState = remember(latLng) { MarkerState(position = latLng) }
            Marker(
                state = markerState,
                title = "Ausgewählter Standort"
            )
        }
    }
}

@Composable
fun TierdexMapScreen(
    findings: List<AnimalFinding>,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                top = 16.dp + extraTopPadding,
                end = 16.dp
            ),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = extraBottomPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground,
                    contentColor = TextPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Fundorte",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Alle gespeicherten Fundorte auf einer Karte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground,
                    contentColor = TextPrimary
                )
            ) {
                FindingsMapPreview(
                    findings = findings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onShowIntro: () -> Unit,
    allFindings: List<AnimalFinding>,
    onImportFindings: (List<AnimalFinding>) -> Unit,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val appVersion = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
    var selectedSettingsPage by rememberSaveable { mutableStateOf("menu") }
    val pageTitle = when (selectedSettingsPage) {
        "menu" -> "Einstellungen"
        "display" -> "Darstellung"
        "features" -> "App-Funktionen"
        "info" -> "Info"
        else -> "Einstellungen"
    }
    val pageSubtitle = when (selectedSettingsPage) {
        "menu" -> "Einführung, Hinweise und wichtige App-Bereiche an einem Ort."
        "display" -> "Gestaltung und visuelle Optionen werden hier später ergänzt."
        "features" -> "Quests, Challenges und weitere Bereiche werden hier gebündelt."
        "info" -> "Backup, Hinweise zur Datensicherheit und Informationen zur App."
        else -> ""
    }
    BackHandler {
        if (selectedSettingsPage == "menu") {
            onBack()
        } else {
            selectedSettingsPage = "menu"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                top = 16.dp + extraTopPadding,
                end = 16.dp
            ),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = extraBottomPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SettingsContentCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = pageTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    if (pageSubtitle.isNotBlank()) {
                        Text(
                            text = pageSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        when (selectedSettingsPage) {
            "menu" -> {
                item {
                    SettingsMenuCard(
                        title = "Über den Tierdex",
                        description = "Einführung, Nutzung und Regeln der App ansehen",
                        onClick = onShowIntro
                    )
                }

                item {
                    SettingsMenuCard(
                        title = "Darstellung",
                        description = "Farben und Designoptionen",
                        onClick = { selectedSettingsPage = "display" }
                    )
                }

                item {
                    SettingsMenuCard(
                        title = "App-Funktionen",
                        description = "Quests, Challenges und weitere Funktionen",
                        onClick = { selectedSettingsPage = "features" }
                    )
                }

                item {
                    SettingsMenuCard(
                        title = "Info",
                        description = "Infos zur App und Backup",
                        onClick = { selectedSettingsPage = "info" }
                    )
                }
            }

            "display" -> {
                item {
                    SettingsContentCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Darstellung",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Farben und Designoptionen werden später ergänzt. Die App bleibt bis dahin bewusst ruhig und einheitlich.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            "features" -> {
                item {
                    SettingsContentCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "App-Funktionen",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Quests, Challenges und weitere Funktionen bekommen hier später ihren festen Platz.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            "info" -> {
                item {
                    SettingsContentCard {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Tierdex Testversion",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Version: ${appVersion.ifBlank { "Unbekannt" }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = "Backups und Sync sichern aktuell nur Funddaten. Fotos bleiben lokal auf diesem Gerät.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                item {
                    SettingsContentCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Backup",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Du kannst deine aktuellen Funddaten sichern und wieder einspielen.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Button(
                                onClick = {
                                    val file = exportFindings(context, allFindings)
                                    shareBackup(context, file)
                                    Toast.makeText(context, "Backup erstellt", Toast.LENGTH_SHORT)
                                        .show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryGreen
                                )
                            ) {
                                Text("Backup teilen")
                            }
                            Button(
                                onClick = {
                                    val importResult = importFindings(context)
                                    if (importResult.success) {
                                        onImportFindings(importResult.findings)
                                    }
                                    Toast.makeText(
                                        context,
                                        importResult.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryGreen
                                )
                            ) {
                                Text("Backup laden")
                            }
                            Text(
                                text = "Bei Gerätewechsel oder Neuinstallation können Fundfotos fehlen, auch wenn ein Backup oder Sync vorhanden ist.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

            }
        }

        if (selectedSettingsPage == "menu") {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Button(
                    onClick = {
                        AuthSession.signOut()
                        onLogout()
                        Toast.makeText(context, "Ausgeloggt", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    )
                ) {
                    Text("Ausloggen")
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground,
            contentColor = TextPrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SettingsContentCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground,
            contentColor = TextPrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

enum class QuestType {
    TOTAL_FINDINGS,
    PHOTO_FINDINGS,
    DAILY_ANIMAL,
    BIRDS,
    FISH,
    MAMMALS,
    AMPHIBIANS,
    REPTILES
}

data class QuestUiModel(
    val id: String,
    val type: QuestType,
    val title: String,
    val description: String,
    val progress: Int,
    val goal: Int,
    val isCompleted: Boolean
) {
    val shownProgress: Int
        get() = progress.coerceAtMost(goal)

    val progressFraction: Float
        get() = if (goal > 0) shownProgress.toFloat() / goal.toFloat() else 0f

    val percentLabel: String
        get() = "${(progressFraction * 100f).toInt()}%"

    val remainingToGoal: Int
        get() = (goal - progress).coerceAtLeast(0)

    val nextStageLabel: String
        get() = if (isCompleted) {
            "Stufe gemeistert"
        } else {
            "Nächste Stufe: $goal"
        }

    val encouragementLabel: String
        get() = if (isCompleted) {
            "Belohnung freigeschaltet"
        } else if (remainingToGoal == 1) {
            "Noch 1 Fund bis zum Ziel"
        } else {
            "Noch $remainingToGoal bis zum Ziel"
        }

    val icon: ImageVector
        get() = when (type) {
            QuestType.TOTAL_FINDINGS -> Icons.Filled.Collections
            QuestType.PHOTO_FINDINGS -> Icons.Filled.PhotoCamera
            QuestType.DAILY_ANIMAL -> Icons.Filled.Star
            QuestType.BIRDS -> Icons.Filled.Air
            QuestType.FISH -> Icons.Filled.SetMeal
            QuestType.MAMMALS -> Icons.Filled.Pets
            QuestType.AMPHIBIANS -> Icons.Filled.WaterDrop
            QuestType.REPTILES -> Icons.Filled.BugReport
        }
}

data class CelebrationMessage(
    val title: String,
    val subtitle: String
)

@Composable
fun QuestCard(quest: QuestUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (quest.isCompleted) PrimaryGreenSoft.copy(alpha = 0.45f) else CardBackground,
            contentColor = TextPrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = quest.icon,
                        contentDescription = null,
                        tint = if (quest.isCompleted) PrimaryGreen else TextSecondary
                    )
                    Text(
                        text = quest.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (quest.isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = if (quest.isCompleted) "Geschafft" else "Aktiv",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (quest.isCompleted) PrimaryGreen else TextSecondary
                    )
                }
            }

            Text(
                text = when (quest.type) {
                    QuestType.TOTAL_FINDINGS -> "Gesamtfunde"
                    QuestType.PHOTO_FINDINGS -> "Fotoquest"
                    QuestType.DAILY_ANIMAL -> "Tagesquest"
                    QuestType.BIRDS -> "Vögel"
                    QuestType.FISH -> "Fische"
                    QuestType.MAMMALS -> "Säugetiere"
                    QuestType.AMPHIBIANS -> "Amphibien"
                    QuestType.REPTILES -> "Reptilien"
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Text(
                text = quest.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Text(
                text = "Fortschritt: ${quest.shownProgress} / ${quest.goal}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            LinearProgressIndicator(
                progress = { quest.progressFraction },
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryGreen,
                trackColor = PrimaryGreenSoft
            )

            Text(
                text = quest.nextStageLabel,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quest.percentLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = quest.encouragementLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (quest.isCompleted) PrimaryGreen else TextSecondary
                )
            }
        }
    }
}

fun getNextQuestGoal(
    progress: Int,
    goals: List<Int>
): Int {
    for (goal in goals) {
        if (progress < goal) {
            return goal
        }
    }
    return goals.last()
}

fun buildHomeQuests(
    findings: List<AnimalFinding>,
    animals: List<AnimalEntry>,
    dailyAnimal: AnimalEntry?,
    collectedAnimalCount: Int,
    totalFindings: Int,
    photoFindingCount: Int
): List<QuestUiModel> {
    val animalById = animals.associateBy { it.id }
    val findingsByGroup = findings
        .mapNotNull { finding -> animalById[finding.animalId]?.group }
        .groupingBy { normalizeQuestGroupName(it) }
        .eachCount()

    val totalQuest = QuestUiModel(
        id = "total_findings",
        type = QuestType.TOTAL_FINDINGS,
        title = "Funde sammeln",
        description = "Erreiche die nächste Stufe über alle gespeicherten Funde hinweg.",
        progress = totalFindings,
        goal = getNextQuestGoal(totalFindings, listOf(1, 5, 10, 25, 50)),
        isCompleted = totalFindings >= 50
    )

    val photoQuest = QuestUiModel(
        id = "photo_findings",
        type = QuestType.PHOTO_FINDINGS,
        title = "Funde mit Foto",
        description = "Dokumentiere deine Beobachtungen mit Bildern.",
        progress = photoFindingCount,
        goal = getNextQuestGoal(photoFindingCount, listOf(1, 3, 5, 10, 20)),
        isCompleted = photoFindingCount >= 20
    )

    val todayDateText = currentAppDateText()
    val dailyAnimalFoundToday = dailyAnimal?.let { todayAnimal ->
        findings.any { finding ->
            finding.animalId == todayAnimal.id && finding.date == todayDateText
        }
    } ?: false
    val dailyAnimalQuest = dailyAnimal?.let { todayAnimal ->
        QuestUiModel(
            id = "daily_animal_${todayAnimal.id}_${currentDailyDateKey()}",
            type = QuestType.DAILY_ANIMAL,
            title = "Tier des Tages finden",
            description = "Finde heute das Tier des Tages und trage deinen Fund ein.",
            progress = if (dailyAnimalFoundToday) 1 else 0,
            goal = 1,
            isCompleted = dailyAnimalFoundToday
        )
    }

    val groupQuestConfigs = listOf(
        Triple("Vögel", QuestType.BIRDS, listOf("Vogel", "Vögel")),
        Triple("Fische", QuestType.FISH, listOf("Fisch", "Fische")),
        Triple("Säugetiere", QuestType.MAMMALS, listOf("Säugetier", "Säugetiere")),
        Triple("Amphibien", QuestType.AMPHIBIANS, listOf("Amphibie", "Amphibien")),
        Triple("Reptilien", QuestType.REPTILES, listOf("Reptil", "Reptilien"))
    )

    val groupQuests = groupQuestConfigs.map { (label, type, aliases) ->
        val progress = findingsByGroup
            .filterKeys { key -> key in aliases.map { normalizeQuestGroupName(it) } }
            .values
            .sum()
        QuestUiModel(
            id = "group_$label",
            type = type,
            title = "$label entdecken",
            description = "Sammle Funde aus der Tiergruppe $label.",
            progress = progress,
            goal = getNextQuestGoal(progress, listOf(1, 3, 5, 10)),
            isCompleted = progress >= 10
        )
    }

    return buildList {
        add(totalQuest)
        add(photoQuest)
        dailyAnimalQuest?.let { add(it) }
        addAll(
            groupQuests
                .sortedWith(
                    compareBy<QuestUiModel> { it.isCompleted }
                        .thenByDescending { it.progressFraction }
                        .thenByDescending { it.progress }
                )
                .take(3)
        )
    }
}

fun normalizeQuestGroupName(group: String): String {
    return group.trim().lowercase()
}

fun detectQuestLevelUpMessage(
    previousFindings: List<AnimalFinding>,
    newFinding: AnimalFinding,
    animals: List<AnimalEntry>
): CelebrationMessage? {
    fun reachedGoal(previous: Int, current: Int, goals: List<Int>): Int? {
        return goals.firstOrNull { goal -> previous < goal && current >= goal }
    }

    val totalGoal = reachedGoal(
        previous = previousFindings.size,
        current = previousFindings.size + 1,
        goals = listOf(1, 5, 10, 25, 50)
    )
    if (totalGoal != null) {
        return CelebrationMessage(
            title = "Quest geschafft: $totalGoal Funde!",
            subtitle = "Deine Gesamtfund-Quest hat eine neue Stufe erreicht."
        )
    }

    if (newFinding.photoUri.isNotBlank()) {
        val previousPhotoCount = previousFindings.count { it.photoUri.isNotBlank() }
        val photoGoal = reachedGoal(
            previous = previousPhotoCount,
            current = previousPhotoCount + 1,
            goals = listOf(1, 3, 5, 10, 20)
        )
        if (photoGoal != null) {
            return CelebrationMessage(
                title = "Quest geschafft: $photoGoal Foto-Funde!",
                subtitle = "Deine Fotoquest hat eine neue Stufe erreicht."
            )
        }
    }

    val foundAnimal = animals.firstOrNull { it.id == newFinding.animalId }
    val normalizedGroup = foundAnimal?.group?.let(::normalizeQuestGroupName) ?: return null
    val groupQuestConfigs = listOf(
        "Vögel" to listOf("Vogel", "Vögel"),
        "Fische" to listOf("Fisch", "Fische"),
        "Säugetiere" to listOf("Säugetier", "Säugetiere"),
        "Amphibien" to listOf("Amphibie", "Amphibien"),
        "Reptilien" to listOf("Reptil", "Reptilien")
    )
    val matchedGroupConfig = groupQuestConfigs.firstOrNull { (_, aliases) ->
        normalizedGroup in aliases.map(::normalizeQuestGroupName)
    } ?: return null

    val relevantAliases = matchedGroupConfig.second.map(::normalizeQuestGroupName).toSet()
    val animalById = animals.associateBy { it.id }
    val previousGroupCount = previousFindings.count { finding ->
        animalById[finding.animalId]?.group?.let(::normalizeQuestGroupName) in relevantAliases
    }
    val groupGoal = reachedGoal(
        previous = previousGroupCount,
        current = previousGroupCount + 1,
        goals = listOf(1, 3, 5, 10)
    )

    return if (groupGoal != null) {
        CelebrationMessage(
            title = "Quest geschafft: $groupGoal ${matchedGroupConfig.first}!",
            subtitle = "Deine ${matchedGroupConfig.first}-Quest hat eine neue Stufe erreicht."
        )
    } else {
        null
    }
}

@Composable
fun FriendProfileScreen(
    currentUserId: String?,
    friendUserId: String,
    initialDisplayName: String?,
    allAnimals: List<AnimalEntry>,
    onBack: () -> Unit,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    BackHandler(onBack = onBack)

    var profile by remember(friendUserId) { mutableStateOf<PublicUserProfile?>(null) }
    var friendFeed by remember(friendUserId) { mutableStateOf<List<FriendFeedItem>>(emptyList()) }
    var friends by remember(currentUserId) { mutableStateOf<List<FriendUser>>(emptyList()) }
    var isLoading by remember(friendUserId) { mutableStateOf(true) }
    var errorMessage by rememberSaveable(friendUserId) { mutableStateOf<String?>(null) }
    val animalById = remember(allAnimals) { allAnimals.associateBy { it.id } }

    LaunchedEffect(currentUserId, friendUserId) {
        profile = null
        friendFeed = emptyList()
        friends = emptyList()
        errorMessage = null

        val safeUserId = currentUserId
        if (safeUserId.isNullOrBlank()) {
            isLoading = false
            errorMessage = "Melde dich an, um dieses Profil zu sehen."
            return@LaunchedEffect
        }

        isLoading = true
        var pendingLoads = 3
        var firstError: String? = null

        fun finishLoad() {
            pendingLoads -= 1
            if (pendingLoads <= 0) {
                errorMessage = firstError
                isLoading = false
            }
        }

        FriendRepository.loadUserProfile(
            userId = friendUserId,
            onResult = {
                profile = it
                finishLoad()
            },
            onError = { error ->
                firstError = firstError ?: error ?: "Profil konnte nicht geladen werden."
                finishLoad()
            }
        )

        FriendRepository.loadFriends(
            currentUserId = safeUserId,
            onResult = {
                friends = it
                finishLoad()
            },
            onError = { error ->
                firstError = firstError ?: error ?: "Freundesliste konnte nicht geladen werden."
                finishLoad()
            }
        )

        FriendRepository.loadFriendsFeed(
            currentUserId = safeUserId,
            onResult = {
                friendFeed = it.filter { feedItem -> feedItem.friendUserId == friendUserId }
                finishLoad()
            },
            onError = { error ->
                firstError = firstError ?: error.message ?: "Funde konnten nicht geladen werden."
                finishLoad()
            }
        )
    }

    val effectiveDisplayName = profile?.displayName
        ?.takeIf { it.isNotBlank() }
        ?: initialDisplayName?.takeIf { it.isNotBlank() }
        ?: "Unbenannter Nutzer"
    val friendProfileImageUri = profile?.profilePhotoPath
        ?.takeIf { it.isNotBlank() }
        ?.let(::storageUriFromPath)
    val publicBio = profile?.bio.orEmpty().trim()
    val friendNamesById = remember(friends, effectiveDisplayName, friendUserId) {
        friends.associate { friend ->
            friend.userId to friend.displayName.ifBlank { "Unbenannter Nutzer" }
        } + mapOf(friendUserId to effectiveDisplayName)
    }
    val totalFindings = friendFeed.size
    val distinctAnimalCount = friendFeed.map { it.finding.animalId }.toSet().size
    val mappedLocationCount = friendFeed.count {
        it.finding.latitude != null && it.finding.longitude != null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                top = 16.dp + extraTopPadding,
                end = 16.dp
            ),
        contentPadding = PaddingValues(bottom = extraBottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "Zurück zum Feed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground,
                    contentColor = TextPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FriendAvatar(
                        displayName = effectiveDisplayName,
                        profileImageUri = friendProfileImageUri,
                        modifier = Modifier.size(72.dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = effectiveDisplayName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Öffentliches Freundesprofil",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HomeStatTile(
                            title = "Funde",
                            value = totalFindings.toString(),
                            supportingText = "gespeichert",
                            modifier = Modifier.weight(1f)
                        )
                        HomeStatTile(
                            title = "Arten",
                            value = distinctAnimalCount.toString(),
                            supportingText = "entdeckt",
                            modifier = Modifier.weight(1f)
                        )
                        HomeStatTile(
                            title = "Fundorte",
                            value = mappedLocationCount.toString(),
                            supportingText = "mit Karte",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (publicBio.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground,
                        contentColor = TextPrimary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Bio",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = publicBio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Sichtbare Funde",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        errorMessage?.let { message ->
            item {
                CompactSectionError(
                    summary = "Freundesprofil konnte nicht vollständig geladen werden.",
                    technicalDetails = message
                )
            }
        }

        when {
            isLoading -> {
                item {
                    Text(
                        text = "Profil wird geladen...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            friendFeed.isEmpty() -> {
                item {
                    Text(
                        text = "Noch keine sichtbaren Funde dieses Freundes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            else -> {
                items(friendFeed, key = { it.friendUserId + "_" + it.findingId }) { feedItem ->
                    val animal = animalById[feedItem.finding.animalId]
                    val friendPhotoSources = effectiveFriendPhotoSources(
                        finding = feedItem.finding,
                        ownerUserId = feedItem.friendUserId,
                        currentUserId = currentUserId
                    )
                    var currentPhotoPage by remember(feedItem.friendUserId, feedItem.findingId, friendPhotoSources) {
                        mutableStateOf(0)
                    }
                    val taggedFriendsSummary = taggedFriendsSummaryText(
                        taggedFriendIds = feedItem.finding.taggedFriendIds,
                        currentUserId = currentUserId,
                        ownerUserId = feedItem.friendUserId,
                        ownerDisplayName = effectiveDisplayName,
                        friendNamesById = friendNamesById
                    )
                    val hasFindingMeta = feedItem.finding.date.isNotBlank() ||
                        feedItem.finding.location.isNotBlank()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FriendFindingPhotoBlock(
                                photoSources = friendPhotoSources,
                                hasPhoto = hasAnyFindingPhoto(feedItem.finding),
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                                onPageChanged = { currentPhotoPage = it }
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = animal?.germanName ?: "Unbekanntes Tier",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                animal?.group?.takeIf { it.isNotBlank() }?.let { groupName ->
                                    Text(
                                        text = groupName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (hasFindingMeta) {
                                        FindingMetaRow(
                                            date = feedItem.finding.date,
                                            location = feedItem.finding.location,
                                            latitude = feedItem.finding.latitude,
                                            longitude = feedItem.finding.longitude,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                    FindingPhotoCounter(
                                        currentPage = currentPhotoPage,
                                        totalCount = friendPhotoSources.size
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                FriendFindingEngagementSummary(
                                    likeCount = feedItem.likeCount,
                                    commentCount = feedItem.commentCount
                                )
                            }

                            feedItem.finding.note.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }

                            taggedFriendsSummary?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendsScreen(
    friendFeedCacheDao: FriendFeedCacheDao,
    currentUserId: String?,
    currentDisplayName: String?,
    allAnimals: List<AnimalEntry>,
    isFriendSearchOpen: Boolean,
    onCloseFriendSearch: () -> Unit,
    onIncomingRequestsChanged: () -> Unit,
    onOpenFriendProfile: (String, String) -> Unit,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    val scope = rememberCoroutineScope()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<PublicUserProfile>>(emptyList()) }
    var friends by remember { mutableStateOf<List<FriendUser>>(emptyList()) }
    var friendFeed by remember { mutableStateOf<List<FriendFeedItem>>(emptyList()) }
    var expandedCommentKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var commentsByFeedKey by remember { mutableStateOf<Map<String, List<FriendFindingComment>>>(emptyMap()) }
    var loadingCommentKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var commentInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var incomingRequests by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
    var outgoingRequestIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var infoMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var feedStatusMessage by remember(currentUserId) { mutableStateOf<String?>(null) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var searchErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var requestsErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var friendsErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var feedErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isShowingCachedFeed by remember(currentUserId) { mutableStateOf(false) }
    var friendToRemove by remember { mutableStateOf<FriendUser?>(null) }
    var isFriendsListExpanded by rememberSaveable { mutableStateOf(false) }
    val animalById = remember(allAnimals) { allAnimals.associateBy { it.id } }

    fun feedKey(feedItem: FriendFeedItem): String = "${feedItem.friendUserId}_${feedItem.findingId}"

    fun overlayFriendFeedDisplayNames(
        cacheOwnerUserId: String,
        profileDisplayNamesByUserId: Map<String, String>
    ) {
        if (profileDisplayNamesByUserId.isEmpty()) return

        val updatedFeed = friendFeed.map { existingItem ->
            val preferredDisplayName = FriendRepository.resolvePreferredFriendFeedDisplayName(
                currentProfileDisplayName = profileDisplayNamesByUserId[existingItem.friendUserId],
                freshFeedDisplayName = existingItem.friendDisplayName,
                cachedDisplayName = existingItem.friendDisplayName
            )
            if (preferredDisplayName == existingItem.friendDisplayName) {
                existingItem
            } else {
                existingItem.copy(friendDisplayName = preferredDisplayName)
            }
        }

        if (updatedFeed == friendFeed) return

        friendFeed = updatedFeed
        scope.launch {
            withContext(Dispatchers.IO) {
                friendFeedCacheDao.replaceFeedCacheForUser(
                    cacheOwnerUserId = cacheOwnerUserId,
                    items = updatedFeed.map { feedItem -> feedItem.toCacheEntity(cacheOwnerUserId) }
                )
            }
        }
    }

    fun loadCommentsForFeedItem(feedItem: FriendFeedItem) {
        val key = feedKey(feedItem)
        loadingCommentKeys = loadingCommentKeys + key
        FriendRepository.loadCommentsForFinding(
            ownerUserId = feedItem.friendUserId,
            findingId = feedItem.findingId,
            onResult = { comments ->
                commentsByFeedKey = commentsByFeedKey + (key to comments)
                friendFeed = friendFeed.map { existingItem ->
                    if (feedKey(existingItem) == key) {
                        existingItem.copy(commentCount = comments.size)
                    } else {
                        existingItem
                    }
                }
                loadingCommentKeys = loadingCommentKeys - key
            },
            onError = {
                errorMessage = "Kommentare konnten gerade nicht geladen werden"
                loadingCommentKeys = loadingCommentKeys - key
            }
        )
    }

    fun refreshFriendsData() {
        val safeUserId = currentUserId ?: return
        Log.d("FriendFeedCache", "cloud refresh start cacheOwnerUserId=$safeUserId")
        isRefreshing = true
        feedStatusMessage = if (isShowingCachedFeed) "Aktualisiere…" else null
        Log.d(
            "FriendFeedCache",
            "inline refresh shown=${isShowingCachedFeed} fullScreenLoadingShown=${friendFeed.isEmpty()}"
        )
        requestsErrorMessage = null
        friendsErrorMessage = null
        feedErrorMessage = null
        var pendingLoads = 4

        fun finishLoad() {
            pendingLoads -= 1
            if (pendingLoads <= 0) {
                isRefreshing = false
            }
        }

        FriendRepository.loadFriends(
            currentUserId = safeUserId,
            onResult = {
                friends = it
                finishLoad()
            },
            onError = { error ->
                friendsErrorMessage = error ?: "Freunde konnten gerade nicht geladen werden"
                finishLoad()
            }
        )

        FriendRepository.loadIncomingFriendRequests(
            currentUserId = safeUserId,
            onResult = {
                incomingRequests = it
                onIncomingRequestsChanged()
                finishLoad()
            },
            onError = { error ->
                onIncomingRequestsChanged()
                requestsErrorMessage = error ?: "Anfragen konnten gerade nicht geladen werden"
                finishLoad()
            }
        )

        FriendRepository.loadOutgoingFriendRequestIds(
            currentUserId = safeUserId,
            onResult = {
                outgoingRequestIds = it
                finishLoad()
            },
            onError = { error ->
                requestsErrorMessage = error ?: "Anfragen konnten gerade nicht vollständig geladen werden"
                finishLoad()
            }
        )

        FriendRepository.loadFriendsFeed(
            currentUserId = safeUserId,
            onResult = {
                val sortedFeed = FriendRepository.sortFriendFeedItems(it)
                Log.d(
                    "FriendFeedCache",
                    "cloud refresh end cacheOwnerUserId=$safeUserId itemCount=${sortedFeed.size}"
                )
                friendFeed = sortedFeed
                isShowingCachedFeed = false
                feedStatusMessage = null
                Log.d(
                    "FriendFeedCache",
                    "cloud result applied count=${sortedFeed.size} fullScreenLoadingShown=false inlineRefreshShown=false"
                )
                scope.launch {
                    val thumbnailPathCount = sortedFeed.count { feedItem ->
                        feedItem.finding.thumbnailRemotePhotoPath.isNotBlank()
                    }
                    val remotePhotoPathCount = sortedFeed.sumOf { feedItem ->
                        effectiveRemotePhotoPaths(feedItem.finding).size
                    }
                    Log.d(
                        "FriendFeedCache",
                        "cache replace start cacheOwnerUserId=$safeUserId itemCount=${sortedFeed.size} thumbnailPathCount=$thumbnailPathCount remotePhotoPathCount=$remotePhotoPathCount"
                    )
                    withContext(Dispatchers.IO) {
                        friendFeedCacheDao.replaceFeedCacheForUser(
                            cacheOwnerUserId = safeUserId,
                            items = sortedFeed.map { feedItem -> feedItem.toCacheEntity(safeUserId) }
                        )
                    }
                    Log.d(
                        "FriendFeedCache",
                        "cache replace end cacheOwnerUserId=$safeUserId itemCount=${sortedFeed.size} thumbnailPathCount=$thumbnailPathCount remotePhotoPathCount=$remotePhotoPathCount"
                    )
                }
                finishLoad()
            },
            onError = { error ->
                Log.w(
                    "FriendFeedCache",
                    "cloud refresh failed cacheOwnerUserId=$safeUserId error=${error.message ?: "Unbekannter Fehler"}"
                )
                feedStatusMessage = null
                feedErrorMessage = error.message ?: "Der Freunde-Feed konnte gerade nicht geladen werden"
                finishLoad()
            }
        )
    }

    LaunchedEffect(currentUserId) {
        searchResults = emptyList()
        friends = emptyList()
        Log.d(
            "FriendFeedCache",
            "friendFeed cleared reason=currentUserChanged hasCurrentUser=${!currentUserId.isNullOrBlank()}"
        )
        friendFeed = emptyList()
        expandedCommentKeys = emptySet()
        commentsByFeedKey = emptyMap()
        loadingCommentKeys = emptySet()
        commentInputs = emptyMap()
        incomingRequests = emptyList()
        onIncomingRequestsChanged()
        outgoingRequestIds = emptySet()
        infoMessage = null
        errorMessage = null
        searchErrorMessage = null
        requestsErrorMessage = null
        friendsErrorMessage = null
        feedErrorMessage = null
        feedStatusMessage = null
        if (!currentUserId.isNullOrBlank()) {
            val safeUserId = currentUserId
            Log.d("FriendFeedCache", "cache load start cacheOwnerUserId=$safeUserId")
            val cachedFeed = withContext(Dispatchers.IO) {
                FriendRepository.sortFriendFeedCacheEntities(
                    friendFeedCacheDao.getFeedCacheForUser(safeUserId)
                ).map { entity ->
                    entity.toFriendFeedItem()
                }
            }
            Log.d(
                "FriendFeedCache",
                "cache load end cacheOwnerUserId=$safeUserId itemCount=${cachedFeed.size}"
            )
            Log.d(
                "FriendFeedCache",
                "cache load detail cacheOwnerUserId=$safeUserId itemCount=${cachedFeed.size} thumbnailPathCount=${cachedFeed.count { it.finding.thumbnailRemotePhotoPath.isNotBlank() }} remotePhotoPathCount=${cachedFeed.sumOf { effectiveRemotePhotoPaths(it.finding).size }}"
            )
            if (cachedFeed.isNotEmpty()) {
                friendFeed = cachedFeed
                isShowingCachedFeed = true
                feedStatusMessage = "Aktualisiere…"
                Log.d("FriendFeedCache", "ui source=cache cacheOwnerUserId=$safeUserId")
                Log.d(
                    "FriendFeedCache",
                    "cached items applied to UI count=${cachedFeed.size} fullScreenLoadingShown=false inlineRefreshShown=true"
                )
                FriendRepository.loadDisplayNamesForUserIds(
                    userIds = cachedFeed.map { it.friendUserId }
                ) { profileDisplayNamesByUserId ->
                    overlayFriendFeedDisplayNames(
                        cacheOwnerUserId = safeUserId,
                        profileDisplayNamesByUserId = profileDisplayNamesByUserId
                    )
                }
            } else {
                isShowingCachedFeed = false
                Log.d("FriendFeedCache", "ui source=cloudOnly cacheOwnerUserId=$safeUserId")
                Log.d(
                    "FriendFeedCache",
                    "cached items applied to UI count=0 fullScreenLoadingShown=true inlineRefreshShown=false"
                )
            }
            refreshFriendsData()
        }
    }

    LaunchedEffect(isFriendSearchOpen) {
        if (!isFriendSearchOpen) {
            searchQuery = ""
            searchResults = emptyList()
            isSearching = false
            searchErrorMessage = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                top = 16.dp + extraTopPadding,
                end = 16.dp
            ),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = extraBottomPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Freunde",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        if (isFriendSearchOpen) {
            item {
                if (currentUserId.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = "Melde dich an, um Freunde zu finden und Anfragen zu verwalten.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Freunde suchen",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Name suchen") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        val trimmedQuery = searchQuery.trim()
                                        if (trimmedQuery.isBlank()) {
                                            infoMessage = "Gib bitte einen Namen ein."
                                            return@Button
                                        }
                                        isSearching = true
                                        searchErrorMessage = null
                                        errorMessage = null
                                        infoMessage = null
                                        FriendRepository.searchUsersByDisplayName(
                                            query = trimmedQuery,
                                            currentUserId = currentUserId,
                                            onResult = {
                                                searchResults = it
                                                infoMessage = if (it.isEmpty()) {
                                                    "Keine passenden Freunde gefunden."
                                                } else {
                                                    null
                                                }
                                                isSearching = false
                                            },
                                            onError = { error ->
                                                searchErrorMessage = error ?: "Die Suche ist gerade nicht verfügbar."
                                                isSearching = false
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                ) {
                                    Text("Suchen")
                                }
                                OutlinedButton(
                                    onClick = {
                                        searchQuery = ""
                                        searchResults = emptyList()
                                        isSearching = false
                                        searchErrorMessage = null
                                        onCloseFriendSearch()
                                    },
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Text("Schließen")
                                }
                            }
                        }
                    }
                }
            }
        }

        infoMessage?.let { message ->
            item {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9A3D3D)
                )
            }
        }

        if (!currentUserId.isNullOrBlank()) {
            if (isFriendSearchOpen) {
                item {
                    if (isSearching) {
                        Text(
                            text = "Suche läuft …",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                searchErrorMessage?.let { message ->
                    item {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9A3D3D)
                        )
                    }
                }
            }

            if (isFriendSearchOpen && searchResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Suchergebnisse",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                items(searchResults, key = { it.userId }) { user ->
                    val isAlreadyFriend = friends.any { it.userId == user.userId }
                    val hasOutgoingRequest = user.userId in outgoingRequestIds
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = user.displayName.ifBlank { "Unbenannter Nutzer" },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = user.userId,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            when {
                                isAlreadyFriend -> {
                                    Text(
                                        text = "Bereits befreundet",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PrimaryGreen
                                    )
                                }

                                hasOutgoingRequest -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Anfrage gesendet",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextSecondary
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                errorMessage = null
                                                FriendRepository.cancelFriendRequest(
                                                    currentUserId = currentUserId,
                                                    targetUserId = user.userId
                                                ) { success, result ->
                                                    if (success) {
                                                        infoMessage = "Anfrage zurückgezogen."
                                                        refreshFriendsData()
                                                    } else {
                                                        errorMessage =
                                                            result ?: "Die Anfrage konnte nicht zurückgezogen werden."
                                                    }
                                                }
                                            },
                                            border = BorderStroke(1.dp, BorderColor)
                                        ) {
                                            Text("Zurückziehen")
                                        }
                                    }
                                }

                                else -> {
                                    OutlinedButton(
                                        onClick = {
                                            errorMessage = null
                                            FriendRepository.sendFriendRequest(
                                                currentUserId = currentUserId,
                                                targetUserId = user.userId
                                            ) { success, result ->
                                                if (success) {
                                                    infoMessage = "Anfrage gesendet."
                                                    refreshFriendsData()
                                                } else {
                                                    infoMessage = when (result) {
                                                        "Anfrage wurde bereits gesendet" -> "Die Anfrage wurde bereits gesendet."
                                                        "Ihr seid bereits befreundet" -> "Ihr seid bereits befreundet."
                                                        else -> null
                                                    }
                                                    if (infoMessage == null) {
                                                        errorMessage =
                                                            result ?: "Die Anfrage konnte nicht gesendet werden."
                                                    } else {
                                                        refreshFriendsData()
                                                    }
                                                }
                                            }
                                        },
                                        border = BorderStroke(1.dp, BorderColor)
                                    ) {
                                        Text("Anfragen")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (incomingRequests.isNotEmpty() || requestsErrorMessage != null) {
                item {
                    Text(
                        text = "Freundschaftsanfragen",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }
            }

            requestsErrorMessage?.let { message ->
                item {
                    CompactSectionError(
                        summary = "Anfragen konnten nicht geladen werden.",
                        technicalDetails = message
                    )
                }
            }

            if (incomingRequests.isNotEmpty()) {
                items(incomingRequests, key = { it.fromUserId }) { request ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = request.displayName.ifBlank { "Unbenannter Nutzer" },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = request.fromUserId,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        FriendRepository.acceptFriendRequest(
                                            currentUserId = currentUserId,
                                            requesterUserId = request.fromUserId
                                        ) { success, result ->
                                            if (success) {
                                                infoMessage = "Anfrage angenommen."
                                                refreshFriendsData()
                                            } else {
                                                errorMessage =
                                                    result ?: "Die Anfrage konnte nicht angenommen werden."
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                ) {
                                    Text("Annehmen")
                                }
                                OutlinedButton(
                                    onClick = {
                                        errorMessage = null
                                        FriendRepository.declineFriendRequest(
                                            currentUserId = currentUserId,
                                            requesterUserId = request.fromUserId
                                        ) { success, result ->
                                            if (success) {
                                                infoMessage = "Anfrage abgelehnt."
                                                refreshFriendsData()
                                            } else {
                                                errorMessage =
                                                    result ?: "Die Anfrage konnte nicht abgelehnt werden."
                                            }
                                        }
                                    },
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Text("Ablehnen")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Aktuelle Funde deiner Freunde",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            feedStatusMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            feedErrorMessage?.let { message ->
                item {
                    CompactSectionError(
                        summary = "Feed konnte nicht geladen werden.",
                        technicalDetails = message
                    )
                }
            }

            val friendNamesById = friends.associate { friend ->
                friend.userId to friend.displayName.ifBlank { "Unbenannter Nutzer" }
            }
            val friendProfileImageUrisById = friends.associate { friend ->
                friend.userId to friend.profilePhotoPath.takeIf { it.isNotBlank() }?.let(::storageUriFromPath)
            }

            when {
                friends.isEmpty() && friendFeed.isEmpty() -> {
                    item {
                        Text(
                            text = "Füge Freunde hinzu, um ihre Funde hier zu sehen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                friendFeed.isEmpty() -> {
                    item {
                        Text(
                            text = if (isRefreshing) {
                                "Feed wird geladen..."
                            } else {
                                "Noch keine Funde von Freunden."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                else -> {
                    items(friendFeed, key = { it.friendUserId + "_" + it.findingId }) { feedItem ->
                        val animal = animalById[feedItem.finding.animalId]
                        val feedItemKey = feedKey(feedItem)
                        val isCommentsExpanded = feedItemKey in expandedCommentKeys
                        val comments = commentsByFeedKey[feedItemKey].orEmpty()
                        val isLoadingComments = feedItemKey in loadingCommentKeys
                        val commentInput = commentInputs[feedItemKey].orEmpty()
                        val friendPhotoSources = effectiveFriendPhotoSources(
                            finding = feedItem.finding,
                            ownerUserId = feedItem.friendUserId,
                            currentUserId = currentUserId
                        )
                        var currentPhotoPage by remember(feedItem.friendUserId, feedItem.findingId, friendPhotoSources) {
                            mutableStateOf(0)
                        }
                        val taggedFriendsSummary = taggedFriendsSummaryText(
                            taggedFriendIds = feedItem.finding.taggedFriendIds,
                            currentUserId = currentUserId,
                            ownerUserId = feedItem.friendUserId,
                            ownerDisplayName = feedItem.friendDisplayName,
                            friendNamesById = friendNamesById
                        )
                        val hasFindingMeta = feedItem.finding.date.isNotBlank() ||
                            feedItem.finding.location.isNotBlank()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CardBackground,
                                contentColor = TextPrimary
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FriendFindingPhotoBlock(
                                    photoSources = friendPhotoSources,
                                    hasPhoto = hasAnyFindingPhoto(feedItem.finding),
                                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                                    onPageChanged = { currentPhotoPage = it }
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FriendIdentityRow(
                                        displayName = feedItem.friendDisplayName,
                                        profileImageUri = friendProfileImageUrisById[feedItem.friendUserId]
                                            ?: feedItem.friendProfilePhotoPath
                                                .takeIf { it.isNotBlank() }
                                                ?.let(::storageUriFromPath),
                                        onClick = {
                                            onOpenFriendProfile(
                                                feedItem.friendUserId,
                                                feedItem.friendDisplayName
                                            )
                                        }
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = animal?.germanName ?: "Unbekanntes Tier",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        animal?.group?.takeIf { it.isNotBlank() }?.let { groupName ->
                                            Text(
                                                text = groupName,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (hasFindingMeta) {
                                            FindingMetaRow(
                                                date = feedItem.finding.date,
                                                location = feedItem.finding.location,
                                                latitude = feedItem.finding.latitude,
                                                longitude = feedItem.finding.longitude,
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                        FindingPhotoCounter(
                                            currentPage = currentPhotoPage,
                                            totalCount = friendPhotoSources.size
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    FriendFindingEngagementSummary(
                                        likeCount = feedItem.likeCount,
                                        commentCount = feedItem.commentCount
                                    )
                                }

                                feedItem.finding.note.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                                taggedFriendsSummary?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                if (feedItem.friendUserId != currentUserId) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                            onClick = {
                                                errorMessage = null
                                                FriendRepository.toggleLikeForFinding(
                                                    ownerUserId = feedItem.friendUserId,
                                                    findingId = feedItem.findingId,
                                                    currentUserId = currentUserId.orEmpty(),
                                                    currentDisplayName = currentDisplayName,
                                                    currentlyLiked = feedItem.likedByCurrentUser,
                                                    onResult = { isNowLiked ->
                                                        friendFeed = friendFeed.map { existingItem ->
                                                            if (existingItem.friendUserId == feedItem.friendUserId &&
                                                                existingItem.findingId == feedItem.findingId
                                                            ) {
                                                                existingItem.copy(
                                                                    likedByCurrentUser = isNowLiked,
                                                                    likeCount = if (isNowLiked) {
                                                                        existingItem.likeCount + 1
                                                                    } else {
                                                                        max(0, existingItem.likeCount - 1)
                                                                    }
                                                                )
                                                            } else {
                                                                existingItem
                                                            }
                                                        }
                                                        infoMessage = if (isNowLiked) {
                                                            "Gefällt mir gesetzt."
                                                        } else {
                                                            "Gefällt mir entfernt."
                                                        }
                                                    },
                                                    onError = {
                                                        errorMessage = "Der Like konnte nicht gespeichert werden."
                                                    }
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Favorite,
                                                contentDescription = null,
                                                tint = if (feedItem.likedByCurrentUser) {
                                                    PrimaryGreen
                                                } else {
                                                    TextSecondary
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Gefällt mir",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (feedItem.likedByCurrentUser) {
                                                    PrimaryGreen
                                                } else {
                                                    TextSecondary
                                                }
                                            )
                                        }
                                        Text(
                                            text = "${feedItem.likeCount} Likes",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                TextButton(
                                    modifier = Modifier.align(Alignment.Start),
                                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                    onClick = {
                                        errorMessage = null
                                        if (isCommentsExpanded) {
                                            expandedCommentKeys = expandedCommentKeys - feedItemKey
                                        } else {
                                            expandedCommentKeys = expandedCommentKeys + feedItemKey
                                            if (feedItemKey !in commentsByFeedKey &&
                                                feedItemKey !in loadingCommentKeys
                                            ) {
                                                loadCommentsForFeedItem(feedItem)
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (isCommentsExpanded) {
                                            "Kommentare ausblenden"
                                        } else {
                                            "Kommentare"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                if (isCommentsExpanded) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        when {
                                            isLoadingComments -> {
                                                Text(
                                                    text = "Kommentare werden geladen …",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }

                                            comments.isEmpty() -> {
                                                Text(
                                                    text = "Noch keine Kommentare.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }

                                            else -> {
                                                comments.forEach { comment ->
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        Text(
                                                            text = comment.commenterDisplayName.ifBlank { "Unbenannter Nutzer" },
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = TextPrimary,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = comment.text,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = TextPrimary
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (currentUserId.isNullOrBlank()) {
                                            Text(
                                                text = "Melde dich an, um zu kommentieren.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        } else if (feedItem.friendUserId != currentUserId) {
                                            OutlinedTextField(
                                                value = commentInput,
                                                onValueChange = {
                                                    commentInputs = commentInputs + (feedItemKey to it)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                label = { Text("Kommentar") },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            Button(
                                                onClick = {
                                                    val trimmedComment = commentInput.trim()
                                                    if (trimmedComment.isBlank()) {
                                                        errorMessage = "Bitte gib einen Kommentar ein."
                                                        return@Button
                                                    }
                                                    errorMessage = null
                                                    FriendRepository.addCommentToFinding(
                                                        ownerUserId = feedItem.friendUserId,
                                                        findingId = feedItem.findingId,
                                                        currentUserId = currentUserId,
                                                        currentDisplayName = currentDisplayName,
                                                        text = trimmedComment,
                                                        onResult = { success ->
                                                            if (success) {
                                                                commentInputs = commentInputs + (feedItemKey to "")
                                                                loadCommentsForFeedItem(feedItem)
                                                            } else {
                                                                errorMessage = "Der Kommentar konnte nicht gespeichert werden."
                                                            }
                                                        },
                                                        onError = {
                                                            errorMessage = "Der Kommentar konnte nicht gespeichert werden."
                                                        }
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                            ) {
                                                Text("Senden")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground,
                        contentColor = TextPrimary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Deine Freunde",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (friends.isEmpty()) {
                                        "Noch keine Freunde hinzugefügt."
                                    } else {
                                        "${friends.size} Freunde"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            if (friends.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { isFriendsListExpanded = !isFriendsListExpanded },
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Text(if (isFriendsListExpanded) "Ausblenden" else "Anzeigen")
                                }
                            }
                        }

                        friendsErrorMessage?.let { message ->
                            CompactSectionError(
                                summary = "Freundesliste konnte nicht geladen werden.",
                                technicalDetails = message
                            )
                        }

                        if (friends.isNotEmpty()) {
                            Text(
                                text = friends.take(4)
                                    .joinToString(", ") { it.displayName.ifBlank { "Unbenannter Nutzer" } },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        if (isFriendsListExpanded && friends.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                friends.forEach { friend ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = friend.displayName.ifBlank { "Unbenannter Nutzer" },
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = friend.userId,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { friendToRemove = friend },
                                            border = BorderStroke(1.dp, BorderColor)
                                        ) {
                                            Text("Entfernen")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    friendToRemove?.let { friend ->
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            title = {
                Text(
                    text = "Freund entfernen",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Möchtest du diesen Freund wirklich entfernen?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val safeUserId = currentUserId
                        if (safeUserId.isNullOrBlank()) {
                            errorMessage = "Freund konnte nicht entfernt werden"
                            friendToRemove = null
                            return@TextButton
                        }

                        errorMessage = null
                        FriendRepository.removeFriend(
                            currentUserId = safeUserId,
                            friendUserId = friend.userId
                        ) { success, result ->
                            if (success) {
                                infoMessage = "Freund entfernt"
                                refreshFriendsData()
                            } else {
                                errorMessage = result ?: "Der Freund konnte nicht entfernt werden."
                            }
                        }
                        friendToRemove = null
                    }
                ) {
                    Text("Entfernen", color = PrimaryGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { friendToRemove = null }) {
                    Text("Abbrechen", color = TextSecondary)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun AboutTierdexScreen(
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                top = 16.dp + extraTopPadding,
                end = 16.dp
            ),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = extraBottomPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SettingsContentCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Über den Tierdex",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Alles Wichtige zur App, ihrer Nutzung und den wichtigsten Regeln auf einen Blick.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            AboutTierdexSectionCard(
                icon = Icons.Filled.Pets,
                title = "Wofür ist die App gedacht?",
                body = "Der Tierdex bringt Naturfreunde zusammen. Du kannst Tierfunde eintragen, sammeln und dich mit anderen austauschen. Aktuell lassen sich in Deutschland heimische Wirbeltiere erfassen. Die App ist für Menschen gedacht, die aufmerksam durch ihre Umgebung gehen oder mehr über Natur lernen möchten."
            )
        }

        item {
            AboutTierdexSectionCard(
                icon = Icons.Filled.Collections,
                title = "Wie wird der Tierdex genutzt?",
                body = "Auf der Startseite bekommst du einen Überblick. Unter Freunde kannst du Nutzer hinzufügen und ihre Funde sehen, liken und kommentieren. Mein Tierdex zeigt dir deine Sammlung, Wunschtiere und Tierinfos. Im Profil gestaltest du deinen persönlichen Bereich. Über den grünen Button trägst du neue Funde mit Standort, Datum und Notiz ein."
            )
        }

        item {
            SettingsContentCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Hilfreich bei Unsicherheit",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Wenn du eine Tierart nicht sicher bestimmen kannst, helfen dir Werkzeuge wie Google Lens, ChatGPT oder Gemini oft schon gut beim ersten Einordnen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryGreen.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryGreen
                        )
                        Text(
                            text = "Regeln",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Damit Funde fair, respektvoll und nachvollziehbar bleiben, beachte bitte diese Punkte:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AboutTierdexRuleItem("Als gefunden gilt ein Tier nur, wenn es nicht in Gefangenschaft lebt.")
                        AboutTierdexRuleItem("Bitte lade keine toten oder stark verletzten Tiere hoch.")
                        AboutTierdexRuleItem("Halte immer ausreichend Abstand zu Wildtieren.")
                        AboutTierdexRuleItem("Beachte Regeln zu Privatgrundstücken, Straßenverkehr, Naturschutzgebieten und ähnlichen Bereichen.")
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutTierdexSectionCard(
    icon: ImageVector,
    title: String,
    body: String
) {
    SettingsContentCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun AboutTierdexRuleItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryGreen
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun DailyAnimalScreen(
    animal: AnimalEntry,
    currentUserId: String?,
    dailyAnimalHistoryText: String? = null,
    showCloseButton: Boolean = true,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    var availableFriends by remember(currentUserId) { mutableStateOf<List<FriendUser>>(emptyList()) }
    var friendFindings by remember(animal.id, currentUserId) {
        mutableStateOf<List<FriendFeedItem>>(emptyList())
    }
    var friendFindingsError by remember(animal.id, currentUserId) { mutableStateOf<String?>(null) }
    var isLoadingFriendFindings by remember(animal.id, currentUserId) { mutableStateOf(false) }

    val additionalAnimalInfo = listOf(
        "Lebensraum" to animal.habitats.joinToString(", "),
        "Lebensraum" to animal.habitat,
        "Verbreitung in Deutschland" to animal.distributionGermany,
        "Verbreitung" to animal.distribution,
        "Seltenheit" to animal.rarity,
        "Aktivität" to animal.activity,
        "Beste Beobachtungszeit" to animal.season
    ).filter { (_, value) -> value.isNotBlank() }

    LaunchedEffect(currentUserId, animal.id) {
        friendFindings = emptyList()
        friendFindingsError = null

        if (currentUserId.isNullOrBlank()) {
            isLoadingFriendFindings = false
            return@LaunchedEffect
        }

        isLoadingFriendFindings = true
        FriendRepository.loadFriendFindingsForAnimal(
            currentUserId = currentUserId,
            animalId = animal.id,
            onResult = {
                friendFindings = it
                isLoadingFriendFindings = false
            },
            onError = {
                friendFindingsError = "Freundesfunde konnten nicht geladen werden."
                isLoadingFriendFindings = false
            }
        )
    }

    LaunchedEffect(currentUserId) {
        availableFriends = emptyList()

        if (currentUserId.isNullOrBlank()) {
            return@LaunchedEffect
        }

        FriendRepository.loadFriends(
            currentUserId = currentUserId,
            onResult = { friends ->
                availableFriends = friends
            }
        )
    }

    val wishAnimalFriends = remember(animal.id, availableFriends) {
        availableFriends.filter { it.wishAnimalId == animal.id }
    }
    val favoriteAnimalFriends = remember(animal.id, availableFriends) {
        availableFriends.filter { it.favoriteAnimalId == animal.id }
    }
    val wishAnimalFriendsText = remember(wishAnimalFriends) {
        friendAnimalPreferenceLine("Wunschfund", wishAnimalFriends)
    }
    val favoriteAnimalFriendsText = remember(favoriteAnimalFriends) {
        friendAnimalPreferenceLine("Lieblingstier", favoriteAnimalFriends)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = 12.dp,
            bottom = (if (showCloseButton) 40.dp else 48.dp) + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tier des Tages",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            SettingsContentCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = animal.germanName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    animal.latinName.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Text(
                        text = animal.group,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    animal.subgroup.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    animal.shortDescription.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                    dailyAnimalHistoryText?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    wishAnimalFriendsText?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    favoriteAnimalFriendsText?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        if (additionalAnimalInfo.isNotEmpty() || animal.observationTip.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Mehr zu diesem Tier",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        additionalAnimalInfo.forEach { (label, value) ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        animal.observationTip.takeIf { it.isNotBlank() }?.let {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Fundtipp",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Von Freunden gefunden",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    when {
                        currentUserId.isNullOrBlank() -> {
                            Text(
                                text = "Melde dich an, um Funde von Freunden zu sehen.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        isLoadingFriendFindings -> {
                            Text(
                                text = "Freundesfunde werden geladen…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        friendFindingsError != null -> {
                            Text(
                                text = friendFindingsError ?: "Freundesfunde konnten nicht geladen werden.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        friendFindings.isEmpty() -> {
                            Text(
                                text = "Noch keiner deiner Freunde hat dieses Tier gefunden.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        else -> {
                            friendFindings.forEach { feedItem ->
                                val friendPhotoSources = effectiveFriendPhotoSources(
                                    finding = feedItem.finding,
                                    ownerUserId = feedItem.friendUserId,
                                    currentUserId = currentUserId
                                )
                                var currentPhotoPage by remember(feedItem.friendUserId, feedItem.findingId, friendPhotoSources) {
                                    mutableStateOf(0)
                                }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.82f)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        FriendFindingPhotoBlock(
                                            photoSources = friendPhotoSources,
                                            hasPhoto = hasAnyFindingPhoto(feedItem.finding),
                                            onPageChanged = { currentPhotoPage = it }
                                        )
                                        Text(
                                            text = feedItem.friendDisplayName.ifBlank { "Unbenannter Nutzer" },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            FindingMetaRow(
                                                date = feedItem.finding.date,
                                                location = feedItem.finding.location,
                                                latitude = feedItem.finding.latitude,
                                                longitude = feedItem.finding.longitude,
                                                modifier = Modifier.weight(1f)
                                            )
                                            FindingPhotoCounter(
                                                currentPage = currentPhotoPage,
                                                totalCount = friendPhotoSources.size
                                            )
                                        }
                                        feedItem.finding.note.takeIf { it.isNotBlank() }?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCloseButton) {
            item {
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Schließen")
                }
            }
        }
    }
}

@Composable
fun AuthEntryScreen(
    initialAuthMode: String = "login",
    onBack: () -> Unit,
    onAuthSuccess: (String?, Boolean) -> Unit
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var authMode by rememberSaveable(initialAuthMode) { mutableStateOf(initialAuthMode) }
    var authMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val trimmedDisplayName = displayName.trim()
    val trimmedEmail = email.trim()
    val trimmedPassword = password.trim()

    BackHandler(onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (authMode == "register") "Registrieren" else "Einloggen",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground,
                    contentColor = TextPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                authMode = "login"
                                authMessage = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (authMode == "login") {
                                    PrimaryGreen.copy(alpha = 0.1f)
                                } else {
                                    Color.Transparent
                                },
                                contentColor = if (authMode == "login") PrimaryGreen else TextPrimary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (authMode == "login") PrimaryGreen else BorderColor
                            )
                        ) {
                            Text("Einloggen")
                        }

                        OutlinedButton(
                            onClick = {
                                authMode = "register"
                                authMessage = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (authMode == "register") {
                                    PrimaryGreen.copy(alpha = 0.1f)
                                } else {
                                    Color.Transparent
                                },
                                contentColor = if (authMode == "register") PrimaryGreen else TextPrimary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (authMode == "register") PrimaryGreen else BorderColor
                            )
                        ) {
                            Text("Registrieren")
                        }
                    }

                    if (authMode == "register") {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("E-Mail") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Passwort") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (authMode == "register") {
                                if (
                                    trimmedDisplayName.isBlank() ||
                                    trimmedEmail.isBlank() ||
                                    trimmedPassword.isBlank()
                                ) {
                                    authMessage = "Bitte fülle alle Felder aus."
                                    return@Button
                                }
                                AuthSession.registerWithEmail(
                                    trimmedDisplayName,
                                    trimmedEmail,
                                    password
                                ) { success, result ->
                                    if (success) {
                                        AuthSession.getCurrentFirebaseUserId()
                                            ?.let { firebaseUserId ->
                                                onAuthSuccess(firebaseUserId, true)
                                            }
                                        authMessage = "Registrierung erfolgreich"
                                    } else {
                                        authMessage = result ?: "Registrierung fehlgeschlagen"
                                    }
                                }
                            } else {
                                if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                                    authMessage = "Bitte fülle alle Felder aus."
                                    return@Button
                                }
                                AuthSession.loginWithEmail(trimmedEmail, password) { success, result ->
                                    if (success) {
                                        AuthSession.getCurrentFirebaseUserId()
                                            ?.let { firebaseUserId ->
                                                onAuthSuccess(firebaseUserId, false)
                                            }
                                        authMessage = "Login erfolgreich"
                                    } else {
                                        authMessage = result ?: "Login fehlgeschlagen"
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen
                        )
                    ) {
                        Text(if (authMode == "register") "Registrieren" else "Einloggen")
                    }

                    authMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    currentUserId: String?,
    currentDisplayName: String?,
    onDisplayNameSaved: (String?) -> Unit,
    collectedAnimalCount: Int,
    totalFindings: Int,
    findings: List<AnimalFinding>,
    animals: List<AnimalEntry>,
    favoriteAnimalId: String?,
    wishlistAnimalId: String?,
    onEditFinding: (AnimalFinding) -> Unit,
    onOpenFriends: () -> Unit,
    onOpenPhotoGallery: () -> Unit,
    profileCollectionListState: LazyListState,
    profileCollectionSortOrder: String,
    onProfileCollectionSortOrderChange: (String) -> Unit,
    profileCollectionDateFilter: String,
    onProfileCollectionDateFilterChange: (String) -> Unit,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember(context) {
        context.getSharedPreferences("tierdex_prefs", android.content.Context.MODE_PRIVATE)
    }
    val preferenceOwnerId = currentUserId ?: LOCAL_PREFERENCES_OWNER_ID
    val animalById = remember(animals) { animals.associateBy { it.id } }
    val favoriteAnimal = animalById[favoriteAnimalId]
    val wishlistAnimal = animalById[wishlistAnimalId]
    var profileCollectionFilterMenuExpanded by remember { mutableStateOf(false) }
    val activeProfileCollectionSortOrder = remember(profileCollectionSortOrder) {
        runCatching { ProfileCollectionSortOrder.valueOf(profileCollectionSortOrder) }
            .getOrDefault(ProfileCollectionSortOrder.NEWEST_FIRST)
    }
    val activeProfileCollectionDateFilter = remember(profileCollectionDateFilter) {
        runCatching { ProfileCollectionDateFilter.valueOf(profileCollectionDateFilter) }
            .getOrDefault(ProfileCollectionDateFilter.ALL)
    }
    val filteredAndSortedProfileFindings = remember(
        findings,
        activeProfileCollectionDateFilter,
        activeProfileCollectionSortOrder
    ) {
        sortProfileFindings(
            findings = filterProfileFindings(
                findings = findings,
                dateFilter = activeProfileCollectionDateFilter
            ),
            sortOrder = activeProfileCollectionSortOrder
        )
    }
    val profilePhotoPreviewFindings = remember(findings) {
        sortProfileFindings(
            findings = findings,
            sortOrder = ProfileCollectionSortOrder.NEWEST_FIRST
        ).mapNotNull { finding ->
            val previewSource = profileFindingPreviewPhotoUri(finding)
            if (previewSource.isNullOrBlank()) {
                null
            } else {
                finding to previewSource
            }
        }
    }
    val visibleProfilePhotoPreviewFindings = remember(profilePhotoPreviewFindings) {
        profilePhotoPreviewFindings.take(5)
    }
    val activeProfileCollectionFilterLabel = when (activeProfileCollectionDateFilter) {
        ProfileCollectionDateFilter.ALL -> "Alle"
        ProfileCollectionDateFilter.TODAY -> "Heute"
        ProfileCollectionDateFilter.LAST_7_DAYS -> "7 Tage"
        ProfileCollectionDateFilter.LAST_30_DAYS -> "30 Tage"
        ProfileCollectionDateFilter.THIS_YEAR -> "Dieses Jahr"
    }
    val groupIconForAnimal: (AnimalEntry?) -> ImageVector = { entry ->
        when (entry?.group) {
            "Vögel" -> Icons.Filled.Air
            "Fische" -> Icons.Filled.SetMeal
            "Säugetiere" -> Icons.Filled.Pets
            "Reptilien" -> Icons.Filled.BugReport
            "Amphibien" -> Icons.Filled.WaterDrop
            else -> Icons.Filled.Help
        }
    }
    var displayNameInput by rememberSaveable(currentDisplayName) {
        mutableStateOf(currentDisplayName ?: "")
    }
    var authMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var profileBio by rememberSaveable(preferenceOwnerId) {
        mutableStateOf(prefs.getString(profileBioKey(preferenceOwnerId), "").orEmpty())
    }
    var profileImageUri by rememberSaveable(preferenceOwnerId) {
        mutableStateOf(prefs.getString(profileImageKey(preferenceOwnerId), "").orEmpty())
    }
    var profileBackgroundImageUri by rememberSaveable(preferenceOwnerId) {
        mutableStateOf(prefs.getString(profileBackgroundImageKey(preferenceOwnerId), "").orEmpty())
    }
    var remoteProfilePhotoPath by rememberSaveable(preferenceOwnerId) { mutableStateOf("") }
    var showBioEditor by rememberSaveable(preferenceOwnerId) { mutableStateOf(false) }
    var bioDraft by rememberSaveable(preferenceOwnerId) { mutableStateOf(profileBio) }
    val displayedProfileImageUri = profileImageUri.ifBlank {
        remoteProfilePhotoPath.takeIf { it.isNotBlank() }?.let(::storageUriFromPath).orEmpty()
    }
    val profileBackgroundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val storedBackgroundUri = persistPhotoForFinding(context, it.toString())
            profileBackgroundImageUri = storedBackgroundUri
            prefs.edit()
                .putString(profileBackgroundImageKey(preferenceOwnerId), storedBackgroundUri)
                .apply()
        }
    }
    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val storedPhotoUri = persistPhotoForFinding(context, it.toString())
            profileImageUri = storedPhotoUri
            prefs.edit().putString(profileImageKey(preferenceOwnerId), storedPhotoUri).apply()
            currentUserId?.takeIf { userId -> userId.isNotBlank() }?.let { userId ->
                scope.launch {
                    val uploadedPath = FindingPhotoStorageRepository.uploadProfilePhoto(
                        context = context,
                        userId = userId,
                        localPhotoUri = storedPhotoUri,
                        currentProfilePhotoPath = remoteProfilePhotoPath
                    )
                    remoteProfilePhotoPath = uploadedPath
                    FriendRepository.updatePublicUserProfile(
                        userId = userId,
                        profilePhotoPath = uploadedPath
                    )
                }
            }
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            FriendRepository.loadUserProfile(
                userId = currentUserId,
                onResult = { publicProfile ->
                    remoteProfilePhotoPath = publicProfile?.profilePhotoPath.orEmpty()
                    val remoteBio = publicProfile?.bio.orEmpty()
                    val localBio = profileBio.trim().take(300)
                    if (localBio.isNotBlank() && localBio != remoteBio) {
                        FriendRepository.updatePublicUserProfile(
                            userId = currentUserId,
                            bio = localBio
                        )
                    }
                    if (profileImageUri.isNotBlank() && publicProfile?.profilePhotoPath.isNullOrBlank()) {
                        scope.launch {
                            val uploadedPath = FindingPhotoStorageRepository.uploadProfilePhoto(
                                context = context,
                                userId = currentUserId,
                                localPhotoUri = profileImageUri,
                                currentProfilePhotoPath = remoteProfilePhotoPath
                            )
                            remoteProfilePhotoPath = uploadedPath
                            FriendRepository.updatePublicUserProfile(
                                userId = currentUserId,
                                profilePhotoPath = uploadedPath
                            )
                        }
                    }
                }
            )
            Log.d("ProfileScreen", "Firestore test call started")
            Log.d(
                "ProfileScreen",
                "Firestore test current AuthSession.currentUserId = ${AuthSession.currentUserId}"
            )
            FirestoreFindingRepository.loadCurrentUserFindings(
                onResult = { findings ->
                    Log.d("ProfileScreen", "Firestore test load: ${findings.size} findings")
                },
                onError = { error ->
                    Log.e("ProfileScreen", "Firestore test load failed: $error")
                }
            )
        }
    }

    LazyColumn(
        state = profileCollectionListState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                top = 16.dp + extraTopPadding,
                end = 16.dp
            ),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = extraBottomPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground,
                    contentColor = TextPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(204.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(196.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryGreenSoft.copy(alpha = 0.6f)
                        ) {
                            if (profileBackgroundImageUri.isNotBlank()) {
                                UriImage(
                                    uriString = profileBackgroundImageUri,
                                    maxImageSizePx = 1400,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Color.White.copy(alpha = if (profileBackgroundImageUri.isNotBlank()) 0.12f else 0f)
                                    )
                            )
                        }

                        IconButton(
                            onClick = {
                                profileBackgroundPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(7.dp)
                                .size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Hintergrundbild bearbeiten",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .offset(x = 0.dp, y = (-22).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier.size(152.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(152.dp)
                                        .border(
                                            width = 2.dp,
                                            color = Color.White.copy(alpha = 0.9f),
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape),
                                    shape = CircleShape,
                                    color = PrimaryGreenSoft.copy(alpha = 0.65f)
                                ) {
                                    if (displayedProfileImageUri.isNotBlank()) {
                                        UriImage(
                                            uriString = displayedProfileImageUri,
                                            maxImageSizePx = 900,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Pets,
                                                contentDescription = "Profilbild Platzhalter",
                                                tint = PrimaryGreen,
                                                modifier = Modifier.size(44.dp)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        profileImagePicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 4.dp, y = 4.dp)
                                        .size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Profilbild bearbeiten",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = currentDisplayName?.takeIf { it.isNotBlank() }?.let { "Profil von $it" }
                            ?: "Profil",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = profileBio.ifBlank { "Erzähl etwas über dich…" },
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (profileBio.isBlank()) TextSecondary else TextPrimary
                            )
                            IconButton(
                                onClick = {
                                    bioDraft = profileBio
                                    showBioEditor = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Bio bearbeiten",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Collections,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = totalFindings.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Funde",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Pets,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = collectedAnimalCount.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Entdeckte Arten",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CardBackground,
                                contentColor = TextPrimary
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Lieblingstier",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                if (favoriteAnimal == null) {
                                    Text(
                                        text = "Noch nicht gewählt",
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = groupIconForAnimal(favoriteAnimal),
                                            contentDescription = favoriteAnimal.group,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = favoriteAnimal.germanName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CardBackground,
                                contentColor = TextPrimary
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Wunschfund",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                if (wishlistAnimal == null) {
                                    Text(
                                        text = "Nicht gewählt",
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = groupIconForAnimal(wishlistAnimal),
                                            contentDescription = wishlistAnimal.group,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = wishlistAnimal.germanName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (currentUserId != null && currentDisplayName.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground,
                        contentColor = TextPrimary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Sichtbarer Name",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = displayNameInput,
                            onValueChange = { displayNameInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                val cleanDisplayName = displayNameInput.trim()
                                if (cleanDisplayName.isBlank()) {
                                    authMessage = "Bitte gib einen Namen ein"
                                    return@Button
                                }
                                AuthSession.updateCurrentDisplayName(cleanDisplayName) { success, result ->
                                    if (success) {
                                        onDisplayNameSaved(AuthSession.getCurrentDisplayName())
                                        authMessage = "Name gespeichert"
                                    } else {
                                        authMessage =
                                            result ?: "Name konnte nicht gespeichert werden"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen
                            )
                        ) {
                            Text("Name speichern")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedButton(
                    onClick = onOpenFriends,
                    border = BorderStroke(1.dp, BorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardBackground,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = null,
                        tint = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Freunde",
                        color = TextPrimary
                    )
                }
            }
        }

        if (profilePhotoPreviewFindings.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground,
                        contentColor = TextPrimary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Fotos",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            visibleProfilePhotoPreviewFindings.forEachIndexed { index, (finding, previewSource) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                ) {
                                    Card(
                                        onClick = { onEditFinding(finding) },
                                        modifier = Modifier.fillMaxSize(),
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        )
                                    ) {
                                        UriImage(
                                            uriString = previewSource,
                                            maxImageSizePx = 420,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    if (
                                        index == visibleProfilePhotoPreviewFindings.lastIndex &&
                                        profilePhotoPreviewFindings.size > visibleProfilePhotoPreviewFindings.size
                                    ) {
                                        IconButton(
                                            onClick = {
                                                onOpenPhotoGallery()
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(24.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.22f),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Collections,
                                                contentDescription = "Alle Bilder ansehen",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gesamtsammlung",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onProfileCollectionSortOrderChange(
                                when (activeProfileCollectionSortOrder) {
                                ProfileCollectionSortOrder.NEWEST_FIRST ->
                                    ProfileCollectionSortOrder.OLDEST_FIRST.name
                                ProfileCollectionSortOrder.OLDEST_FIRST ->
                                    ProfileCollectionSortOrder.NEWEST_FIRST.name
                                }
                            )
                        },
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = when (activeProfileCollectionSortOrder) {
                                ProfileCollectionSortOrder.NEWEST_FIRST -> "Neu zuerst"
                                ProfileCollectionSortOrder.OLDEST_FIRST -> "Alt zuerst"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Box {
                        OutlinedButton(
                            onClick = { profileCollectionFilterMenuExpanded = true },
                            shape = RoundedCornerShape(999.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CardBackground,
                                contentColor = TextPrimary
                            )
                        ) {
                            Text(
                                text = activeProfileCollectionFilterLabel,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        DropdownMenu(
                            expanded = profileCollectionFilterMenuExpanded,
                            onDismissRequest = { profileCollectionFilterMenuExpanded = false },
                            modifier = Modifier.background(CardBackground)
                        ) {
                            listOf(
                                ProfileCollectionDateFilter.ALL to "Alle",
                                ProfileCollectionDateFilter.TODAY to "Heute",
                                ProfileCollectionDateFilter.LAST_7_DAYS to "7 Tage",
                                ProfileCollectionDateFilter.LAST_30_DAYS to "30 Tage",
                                ProfileCollectionDateFilter.THIS_YEAR to "Dieses Jahr"
                            ).forEach { (filterOption, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        onProfileCollectionDateFilterChange(filterOption.name)
                                        profileCollectionFilterMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (findings.isEmpty()) {
            item {
                Text("Noch keine Funde gespeichert.")
            }
        } else if (filteredAndSortedProfileFindings.isEmpty()) {
            item {
                Text(
                    text = "Für diesen Zeitraum gibt es noch keine Funde.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            items(
                items = filteredAndSortedProfileFindings,
                key = { finding ->
                    finding.roomId ?: FirestoreFindingRepository.findingFingerprint(finding)
                }
            ) { finding ->
                val animal = animalById[finding.animalId]
                val ownPhotoSources = effectiveOwnPhotoSources(finding)
                var currentPhotoPage by remember(finding.roomId, finding.photoUri, finding.photoUris) {
                    mutableStateOf(0)
                }

                Card(
                    onClick = { onEditFinding(finding) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground,
                        contentColor = TextPrimary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = animal?.germanName ?: "Unbekanntes Tier",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (ownPhotoSources.isNotEmpty()) {
                            FindingPhotoPager(
                                photoSources = ownPhotoSources,
                                imageModifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                onPageChanged = { currentPhotoPage = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FindingMetaRow(
                                date = finding.date,
                                location = finding.location,
                                latitude = finding.latitude,
                                longitude = finding.longitude,
                                modifier = Modifier.weight(1f)
                            )
                            FindingPhotoCounter(
                                currentPage = currentPhotoPage,
                                totalCount = ownPhotoSources.size
                            )
                        }

                        if (finding.note.isNotBlank()) {
                            Text(
                                text = finding.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBioEditor) {
        AlertDialog(
            onDismissRequest = { showBioEditor = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val savedBio = bioDraft.trim().take(300)
                        profileBio = savedBio
                        prefs.edit().putString(profileBioKey(preferenceOwnerId), savedBio).apply()
                        currentUserId?.takeIf { it.isNotBlank() }?.let { userId ->
                            FriendRepository.updatePublicUserProfile(
                                userId = userId,
                                bio = savedBio
                            )
                        }
                        showBioEditor = false
                    }
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBioEditor = false }
                ) {
                    Text("Abbrechen")
                }
            },
            title = {
                Text("Bio bearbeiten")
            },
            text = {
                OutlinedTextField(
                    value = bioDraft,
                    onValueChange = { bioDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Bio") },
                    minLines = 3
                )
            }
        )
    }
}

@Composable
private fun ProfileFriendsScreen(
    currentUserId: String?,
    onBack: () -> Unit,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    BackHandler(onBack = onBack)

    var friends by remember(currentUserId) { mutableStateOf<List<FriendUser>>(emptyList()) }
    var isLoading by remember(currentUserId) { mutableStateOf(!currentUserId.isNullOrBlank()) }
    var errorMessage by remember(currentUserId) { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserId) {
        val safeUserId = currentUserId
        if (safeUserId.isNullOrBlank()) {
            friends = emptyList()
            isLoading = false
            errorMessage = "Freundesliste ist nur mit Login verfügbar"
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null
        FriendRepository.loadFriends(
            currentUserId = safeUserId,
            onResult = { loadedFriends ->
                friends = loadedFriends
                isLoading = false
                errorMessage = null
            },
            onError = { error ->
                friends = emptyList()
                isLoading = false
                errorMessage = error ?: "Freunde konnten nicht geladen werden"
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                top = extraTopPadding + 16.dp,
                end = 16.dp
            ),
        contentPadding = PaddingValues(bottom = extraBottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück zum Profil",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Freunde",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }
        }

        when {
            isLoading -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Freunde werden geladen",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = PrimaryGreen
                            )
                        }
                    }
                }
            }

            !errorMessage.isNullOrBlank() -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            friends.isEmpty() -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = "Noch keine Freunde hinzugefügt",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            else -> {
                items(
                    items = friends,
                    key = { it.userId }
                ) { friend ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FriendAvatar(
                                displayName = friend.displayName,
                                profileImageUri = friend.profilePhotoPath
                                    .takeIf { it.isNotBlank() }
                                    ?.let(::storageUriFromPath),
                                modifier = Modifier.size(44.dp)
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = friend.displayName.ifBlank { "Unbenannter Nutzer" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Profilansicht folgt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePhotoGalleryScreen(
    findings: List<AnimalFinding>,
    onBack: () -> Unit,
    onOpenFinding: (AnimalFinding) -> Unit,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    BackHandler(onBack = onBack)

    val photoFindings = remember(findings) {
        sortProfileFindings(
            findings = findings,
            sortOrder = ProfileCollectionSortOrder.NEWEST_FIRST
        ).mapNotNull { finding ->
            profileFindingPreviewPhotoUri(finding)?.takeIf { it.isNotBlank() }?.let { previewUri ->
                finding to previewUri
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                top = extraTopPadding + 16.dp,
                end = 16.dp
            ),
        contentPadding = PaddingValues(bottom = extraBottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück zum Profil",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Fundbilder",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }
        }

        if (photoFindings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        text = "Noch keine Fundbilder vorhanden",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            photoFindings.chunked(3).forEach { photoRow ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        photoRow.forEach { (finding, previewUri) ->
                            Card(
                                onClick = { onOpenFinding(finding) },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                UriImage(
                                    uriString = previewUri,
                                    maxImageSizePx = 720,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        repeat(3 - photoRow.size) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthStartScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Willkommen bei Tierdex",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Bitte logge dich ein oder registriere dich, um fortzufahren.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen
            )
        ) {
            Text("Einloggen")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, PrimaryGreen),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryGreen
            )
        ) {
            Text("Registrieren")
        }
    }
}

@Composable
fun StatisticsScreen(
    animals: List<AnimalEntry>,
    findings: List<AnimalFinding>,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    val collectedAnimalIds = findings.map { it.animalId }.toSet()
    val collectedAnimalCount = animals.count { it.id in collectedAnimalIds }
    val totalFindings = findings.size

    val groupStats = animals
        .filter { it.id in collectedAnimalIds }
        .groupingBy { it.group }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(
                start = 16.dp,
                top = 16.dp + extraTopPadding,
                end = 16.dp,
                bottom = 16.dp + extraBottomPadding
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Statistik",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gesammelte Arten: $collectedAnimalCount von ${animals.size}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Gesamte Funde: $totalFindings",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item {
            Text(
                text = "Gefundene Tiergruppen",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (groupStats.isEmpty()) {
            item {
                Text(
                    text = "Noch keine Statistik vorhanden, weil noch keine Funde gespeichert wurden.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(groupStats) { (group, count) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = group,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$count gesammelte Art(en)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FundDetailScreen(
    modifier: Modifier = Modifier,
    finding: AnimalFinding,
    animal: AnimalEntry?,
    onBackClick: () -> Unit,
    onEditFinding: (AnimalFinding) -> Unit,
    onOpenAnimalDetails: (AnimalFinding) -> Unit
) {
    val photoSources = effectiveOwnPhotoSources(finding)
    var selectedPhotoPage by remember(finding.roomId, photoSources) { mutableStateOf(0) }
    val locationDetailText = when {
        finding.locationSource == "map" -> "Standort auf Karte gewählt"
        finding.locationSource == "gps" ||
            (finding.latitude != null && finding.longitude != null) -> "GPS-Standort gespeichert"
        else -> null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Zurück",
                            tint = TextPrimary
                        )
                    }
                }
                Text(
                    text = "Fund ansehen",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = animal?.germanName ?: "Unbekanntes Tier",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    animal?.latinName?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextSecondary,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    animal?.group?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (photoSources.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                        ) {
                            FindingPhotoPager(
                                photoSources = photoSources,
                                imageModifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                onPageChanged = { selectedPhotoPage = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FindingMetaRow(
                                date = finding.date,
                                location = finding.location,
                                latitude = finding.latitude,
                                longitude = finding.longitude,
                                modifier = Modifier.weight(1f)
                            )
                            FindingPhotoCounter(
                                currentPage = selectedPhotoPage,
                                totalCount = photoSources.size
                            )
                        }
                    } else {
                        FindingMetaRow(
                            date = finding.date,
                            location = finding.location,
                            latitude = finding.latitude,
                            longitude = finding.longitude
                        )
                    }

                    locationDetailText?.let {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Standortdetails",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary
                            )
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    finding.note.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onEditFinding(finding) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("Fund bearbeiten")
                        }

                        Button(
                            onClick = { onOpenAnimalDetails(finding) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("Tierdetails")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalDetailScreen(
    modifier: Modifier = Modifier,
    currentUserId: String?,
    currentDisplayName: String?,
    animal: AnimalEntry,
    findings: List<AnimalFinding>,
    storageDebug: String,
    initialFinding: AnimalFinding?,
    startInCreateMode: Boolean = false,
    startInFindingEditMode: Boolean = false,
    dailyAnimalHistoryText: String?,
    onOpenFindingDetail: (AnimalFinding) -> Unit,
    onReturnToFindingDetail: (AnimalFinding) -> Unit,
    onBackClick: () -> Unit,
    onSaveFinding: (AnimalFinding) -> Unit,
    onDeleteFinding: (AnimalFinding) -> Unit,
    onUpdateFinding: (AnimalFinding, AnimalFinding) -> Unit,
    onSetFavoriteFindingAnimal: (AnimalEntry) -> Unit,
    currentFavoriteAnimalId: String?,
    onSetWishlistAnimal: (AnimalEntry) -> Unit,
    currentWishlistAnimalId: String?,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val initial = initialFinding
    var date by rememberSaveable(initial?.roomId) {
        mutableStateOf(initial?.date ?: currentAppDateText())
    }
    var location by rememberSaveable(initial?.roomId) {
        mutableStateOf(initial?.location ?: "")
    }
    var note by rememberSaveable(initial?.roomId) {
        mutableStateOf(initial?.note ?: "")
    }
    var selectedPhotoUris by rememberSaveable(initial?.roomId) {
        mutableStateOf(initial?.let(::effectiveLocalPhotoUris).orEmpty())
    }
    var latitude by rememberSaveable(initial?.roomId) {
        mutableStateOf(initial?.latitude)
    }
    var longitude by rememberSaveable(initial?.roomId) {
        mutableStateOf(initial?.longitude)
    }
    var locationSource by rememberSaveable(initial?.roomId) {
        mutableStateOf(initial?.locationSource)
    }
    var locationStatusMessage by rememberSaveable(initial?.roomId) {
        mutableStateOf("")
    }
    var selectedTaggedFriendIds by rememberSaveable(initial?.roomId) {
        mutableStateOf(initial?.taggedFriendIds ?: emptyList())
    }
    var showLocationPicker by rememberSaveable(initial?.roomId) { mutableStateOf(false) }
    var pendingLatitude by remember { mutableStateOf<Double?>(null) }
    var pendingLongitude by remember { mutableStateOf<Double?>(null) }
    var cropPhotoIndex by remember { mutableStateOf<Int?>(null) }
    var cropPhotoUri by remember { mutableStateOf<String?>(null) }
    var selectedPhotoPage by rememberSaveable(initial?.roomId) { mutableStateOf(0) }
    var draggingPhotoUri by remember { mutableStateOf<String?>(null) }
    var draggingPhotoOffsetX by remember { mutableStateOf(0f) }
    var availableFriends by remember(currentUserId) { mutableStateOf<List<FriendUser>>(emptyList()) }
    var availableFriendsError by remember(currentUserId) { mutableStateOf<String?>(null) }
    var friendFindings by remember(animal.id, currentUserId) {
        mutableStateOf<List<FriendFeedItem>>(emptyList())
    }
    var friendFindingsError by remember(animal.id, currentUserId) { mutableStateOf<String?>(null) }
    var isLoadingFriendFindings by remember(animal.id, currentUserId) { mutableStateOf(false) }
    val isWishlistSelected = animal.id == currentWishlistAnimalId
    val isFavoriteSelected = animal.id == currentFavoriteAnimalId
    var editingFinding by remember(initial?.roomId) { mutableStateOf(initial) }
    var isEditMode by rememberSaveable(initial?.roomId, startInCreateMode, startInFindingEditMode) {
        mutableStateOf((initial == null && startInCreateMode) || startInFindingEditMode)
    }
    val showsFindingFormOnly = isEditMode
    val hasAnyFinding = findings.isNotEmpty()
    val detailFindings = remember(findings) { findings.asReversed() }
    val additionalAnimalInfo = listOf(
                                                "Lebensraum" to animal.habitats.joinToString(", "),
        "Lebensraum" to animal.habitat,
        "Verbreitung in Deutschland" to animal.distributionGermany,
        "Verbreitung" to animal.distribution,
        "Seltenheit" to animal.rarity,
        "Aktivität" to animal.activity,
        "Beste Beobachtungszeit" to animal.season
    ).filter { (_, value) -> value.isNotBlank() }

    val requestCurrentLocation = {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
            .addOnSuccessListener { currentLocation ->
                if (currentLocation != null) {
                    latitude = currentLocation.latitude
                    longitude = currentLocation.longitude
                    locationSource = "gps"
                    location = formatCoordinates(
                        currentLocation.latitude,
                        currentLocation.longitude
                    )
                    locationStatusMessage = "Standort gespeichert"
                } else {
                    locationStatusMessage = "Standort konnte nicht ermittelt werden"
                }
            }
            .addOnFailureListener {
                locationStatusMessage = "Standort konnte nicht ermittelt werden"
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            requestCurrentLocation()
        } else {
            locationStatusMessage = "Standortberechtigung nicht erteilt"
        }
    }

    fun normalizeSelectedPhotoUris(photoValues: List<String>): List<String> {
        return photoValues
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)
    }

    fun reorderSelectedPhotos(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex !in selectedPhotoUris.indices || toIndex !in selectedPhotoUris.indices) return

        val updatedPhotoUris = selectedPhotoUris.toMutableList().apply {
            val movedPhotoUri = removeAt(fromIndex)
            add(toIndex, movedPhotoUri)
        }

        selectedPhotoUris = normalizeSelectedPhotoUris(updatedPhotoUris)
        selectedPhotoPage = toIndex.coerceIn(0, selectedPhotoUris.lastIndex.coerceAtLeast(0))
    }

    fun appendPickedPhotoUris(pickedUris: List<Uri>) {
        val remainingPhotoSlots = (3 - selectedPhotoUris.size).coerceAtLeast(0)
        if (remainingPhotoSlots <= 0) return

        pickedUris.forEach { pickedUri ->
            persistReadPermission(context, pickedUri)
        }

        val updatedPhotoUris = normalizeSelectedPhotoUris(
            selectedPhotoUris + pickedUris
                .map { it.toString() }
                .filter { it.isNotBlank() }
                .take(remainingPhotoSlots)
        )
        selectedPhotoUris = updatedPhotoUris
        selectedPhotoPage = updatedPhotoUris.lastIndex.coerceAtLeast(0)
    }

    val pickSingleMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { appendPickedPhotoUris(listOf(it)) }
    }

    val pickUpToTwoMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(2)
    ) { uris ->
        appendPickedPhotoUris(uris)
    }

    val pickUpToThreeMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { uris ->
        appendPickedPhotoUris(uris)
    }

    val currentFinding = editingFinding ?: initial
    val editablePhotoSources = when {
        isEditMode -> selectedPhotoUris
        currentFinding != null -> effectiveOwnPhotoSources(currentFinding)
        else -> selectedPhotoUris
    }
    val hasTextualFindingDetails =
        !currentFinding?.date.isNullOrBlank() ||
            !currentFinding?.location.isNullOrBlank() ||
            !currentFinding?.note.isNullOrBlank()
    val hasFindingPhoto = editablePhotoSources.isNotEmpty()

    LaunchedEffect(currentUserId, animal.id) {
        friendFindings = emptyList()
        friendFindingsError = null

        if (currentUserId.isNullOrBlank()) {
            isLoadingFriendFindings = false
            return@LaunchedEffect
        }

        isLoadingFriendFindings = true
        FriendRepository.loadFriendFindingsForAnimal(
            currentUserId = currentUserId,
            animalId = animal.id,
            onResult = {
                friendFindings = it
                isLoadingFriendFindings = false
            },
            onError = {
                friendFindingsError = "Freundesfunde konnten nicht geladen werden."
                isLoadingFriendFindings = false
            }
        )
    }

    LaunchedEffect(currentUserId) {
        availableFriends = emptyList()
        availableFriendsError = null

        if (currentUserId.isNullOrBlank()) {
            return@LaunchedEffect
        }

        FriendRepository.loadFriends(
            currentUserId = currentUserId,
            onResult = { friends ->
                availableFriends = friends
            },
            onError = { error ->
                availableFriendsError = error ?: "Freunde konnten nicht geladen werden."
            }
        )
    }

    val wishAnimalFriends = remember(animal.id, availableFriends) {
        availableFriends.filter { it.wishAnimalId == animal.id }
    }
    val favoriteAnimalFriends = remember(animal.id, availableFriends) {
        availableFriends.filter { it.favoriteAnimalId == animal.id }
    }
    val wishAnimalFriendsText = remember(wishAnimalFriends) {
        friendAnimalPreferenceLine("Wunschfund", wishAnimalFriends)
    }
    val favoriteAnimalFriendsText = remember(favoriteAnimalFriends) {
        friendAnimalPreferenceLine("Lieblingstier", favoriteAnimalFriends)
    }

    @Composable
    fun FindingPhotoEditorSection() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val remainingPhotoSlots = (3 - selectedPhotoUris.size).coerceAtLeast(0)
            if (selectedPhotoUris.size < 3) {
                OutlinedButton(
                    onClick = {
                        val imageOnlyRequest = PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                        when (remainingPhotoSlots) {
                            1 -> pickSingleMedia.launch(imageOnlyRequest)
                            2 -> pickUpToTwoMedia.launch(imageOnlyRequest)
                            else -> pickUpToThreeMedia.launch(imageOnlyRequest)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (selectedPhotoUris.isEmpty()) {
                            "Foto hinzufügen"
                        } else {
                            "Weiteres Foto hinzufügen"
                        }
                    )
                }
            } else {
                Text(
                    text = "Maximal 3 Fotos",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (selectedPhotoUris.isNotEmpty()) {
                val thumbnailWidth = 122.dp
                val thumbnailSpacing = 10.dp
                val density = LocalDensity.current
                val reorderStepPx = remember(density) {
                    with(density) { (thumbnailWidth + thumbnailSpacing).toPx() }
                }
                Text(
                    text = "Fotos",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )

                FindingPhotoPager(
                    photoSources = selectedPhotoUris,
                    imageModifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    onPageChanged = { selectedPhotoPage = it }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    selectedPhotoUris.forEachIndexed { index, photoUri ->
                        key(photoUri) {
                            val isDragged = draggingPhotoUri == photoUri
                            Card(
                                modifier = Modifier
                                    .width(thumbnailWidth)
                                    .offset {
                                        IntOffset(
                                            x = if (isDragged) draggingPhotoOffsetX.roundToInt() else 0,
                                            y = 0
                                        )
                                    }
                                    .zIndex(if (isDragged) 1f else 0f)
                                    .pointerInput(photoUri, reorderStepPx, selectedPhotoUris) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggingPhotoUri = photoUri
                                                draggingPhotoOffsetX = 0f
                                            },
                                            onDragCancel = {
                                                draggingPhotoUri = null
                                                draggingPhotoOffsetX = 0f
                                            },
                                            onDragEnd = {
                                                draggingPhotoUri = null
                                                draggingPhotoOffsetX = 0f
                                            }
                                        ) { change, dragAmount ->
                                            change.consume()
                                            val activePhotoUri =
                                                draggingPhotoUri ?: return@detectDragGesturesAfterLongPress
                                            var currentDraggedIndex =
                                                selectedPhotoUris.indexOf(activePhotoUri)
                                            if (currentDraggedIndex == -1) {
                                                draggingPhotoUri = null
                                                draggingPhotoOffsetX = 0f
                                                return@detectDragGesturesAfterLongPress
                                            }

                                            var newOffsetX = draggingPhotoOffsetX + dragAmount.x
                                            val reorderThreshold = reorderStepPx / 2f

                                            while (
                                                newOffsetX > reorderThreshold &&
                                                currentDraggedIndex < selectedPhotoUris.lastIndex
                                            ) {
                                                reorderSelectedPhotos(
                                                    fromIndex = currentDraggedIndex,
                                                    toIndex = currentDraggedIndex + 1
                                                )
                                                currentDraggedIndex += 1
                                                newOffsetX -= reorderStepPx
                                            }

                                            while (
                                                newOffsetX < -reorderThreshold &&
                                                currentDraggedIndex > 0
                                            ) {
                                                reorderSelectedPhotos(
                                                    fromIndex = currentDraggedIndex,
                                                    toIndex = currentDraggedIndex - 1
                                                )
                                                currentDraggedIndex -= 1
                                                newOffsetX += reorderStepPx
                                            }

                                            draggingPhotoOffsetX = newOffsetX
                                        }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isDragged) 8.dp else 2.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(
                                        alpha = if (isDragged) 0.96f else 0.9f
                                    )
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp)
                                ) {
                                    UriImage(
                                        uriString = photoUri,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(96.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        maxImageSizePx = 512
                                    )
                                    if (index == 0) {
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp),
                                            shape = RoundedCornerShape(999.dp),
                                            color = Color.White.copy(alpha = 0.94f),
                                            shadowElevation = 2.dp
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp
                                                ),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = "Titelbild",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = PrimaryGreen
                                                )
                                                Text(
                                                    text = "Titelbild",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp),
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color.White.copy(alpha = 0.94f),
                                        shadowElevation = 2.dp
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val updatedPhotoUris = selectedPhotoUris
                                                    .filterIndexed { currentIndex, _ ->
                                                        currentIndex != index
                                                    }
                                                    .take(3)
                                                selectedPhotoUris = updatedPhotoUris
                                                selectedPhotoPage = selectedPhotoPage
                                                    .coerceAtMost(updatedPhotoUris.lastIndex.coerceAtLeast(0))
                                                if (cropPhotoIndex == index) {
                                                    cropPhotoIndex = null
                                                    cropPhotoUri = null
                                                }
                                                if (draggingPhotoUri == photoUri) {
                                                    draggingPhotoUri = null
                                                    draggingPhotoOffsetX = 0f
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Foto entfernen",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp),
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color.White.copy(alpha = 0.94f),
                                        shadowElevation = 2.dp
                                    ) {
                                        IconButton(
                                            onClick = {
                                                cropPhotoIndex = index
                                                cropPhotoUri = photoUri
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = "Foto zuschneiden",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Halten und ziehen, um Reihenfolge und Titelbild zu ändern.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = extraTopPadding + 12.dp,
            bottom = 24.dp + extraBottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                if (showsFindingFormOnly) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val animalSubgroupText = animal.subgroup
                            .takeIf { it.isNotBlank() }
                            ?: animal.group.takeIf { it.isNotBlank() }
                        Text(
                            text = animal.germanName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        animal.latinName.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        animalSubgroupText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .padding(end = 56.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = animal.germanName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = animal.latinName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = animal.group,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                            animal.subgroup.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            animal.shortDescription.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.92f),
                            shadowElevation = 2.dp
                        ) {
                            IconButton(
                                onClick = {
                                    if (hasAnyFinding) {
                                        if (!isFavoriteSelected) {
                                            onSetFavoriteFindingAnimal(animal)
                                        }
                                    } else {
                                        if (!isWishlistSelected) {
                                            onSetWishlistAnimal(animal)
                                        }
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (hasAnyFinding) {
                                        if (isFavoriteSelected) {
                                            Icons.Filled.Favorite
                                        } else {
                                            Icons.Outlined.FavoriteBorder
                                        }
                                    } else {
                                        if (isWishlistSelected) {
                                            Icons.Filled.Star
                                        } else {
                                            Icons.Outlined.StarBorder
                                        }
                                    },
                                    contentDescription = if (hasAnyFinding) {
                                        "Als Lieblingstier speichern"
                                    } else {
                                        "Als Wunsch-Fund speichern"
                                    },
                                    tint = PrimaryGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!showsFindingFormOnly && !isEditMode && detailFindings.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Vorhandene Funde",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        detailFindings.forEach { finding ->  
                            Card(
                                onClick = { onOpenFindingDetail(finding) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.82f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                val previewPhotoUri = preferredOwnedFindingPhotoUri(finding)
                                    ?: finding.thumbnailRemotePhotoPath
                                        .takeIf { it.isNotBlank() }
                                        ?.let(::storageUriFromPath)
                                    ?: effectiveRemotePhotoPaths(finding)
                                        .firstOrNull()
                                        ?.let(::storageUriFromPath)
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(RoundedCornerShape(14.dp)),
                                        color = Color.White,
                                        shadowElevation = 0.dp
                                    ) {
                                        if (previewPhotoUri != null) {
                                            UriImage(
                                                uriString = previewPhotoUri,
                                                modifier = Modifier.fillMaxSize(),
                                                maxImageSizePx = 512
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Collections,
                                                    contentDescription = null,
                                                    tint = TextSecondary
                                                )
                                            }
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = finding.date.ifBlank { "Fund ohne Datum" },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary,
                                            textAlign = TextAlign.Start
                                        )

                                        FindingMetaRow(
                                            date = null,
                                            location = finding.location,
                                            latitude = finding.latitude,
                                            longitude = finding.longitude,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        finding.note.takeIf { it.isNotBlank() }?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!showsFindingFormOnly && (hasTextualFindingDetails || hasFindingPhoto)) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Fundinfos",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        if (hasFindingPhoto) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                            ) {
                                FindingPhotoPager(
                                    photoSources = editablePhotoSources,
                                    imageModifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    onPageChanged = { selectedPhotoPage = it }
                                )

                                if (editingFinding != null && isEditMode && selectedPhotoUris.isNotEmpty()) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp),
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color.White.copy(alpha = 0.92f),
                                        shadowElevation = 4.dp
                                    ) {
                                        IconButton(
                                            onClick = {
                                                selectedPhotoUris.getOrNull(selectedPhotoPage)?.let { currentPhotoUri ->
                                                    cropPhotoIndex = selectedPhotoPage
                                                    cropPhotoUri = currentPhotoUri
                                                }
                                            },
                                            modifier = Modifier
                                                .width(40.dp)
                                                .height(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = "Foto zuschneiden",
                                                tint = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (hasTextualFindingDetails) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FindingMetaRow(
                                    date = currentFinding?.date,
                                    location = currentFinding?.location,
                                    latitude = currentFinding?.latitude,
                                    longitude = currentFinding?.longitude,
                                    modifier = Modifier.weight(1f)
                                )
                                FindingPhotoCounter(
                                    currentPage = selectedPhotoPage,
                                    totalCount = editablePhotoSources.size
                                )
                            }

                            val locationDetailText = when {
                                currentFinding?.locationSource == "map" -> "Standort auf Karte gewählt"
                                currentFinding?.locationSource == "gps" ||
                                        (currentFinding?.latitude != null && currentFinding.longitude != null) ->
                                    "GPS-Standort gespeichert"
                                else -> null
                            }
                            locationDetailText?.let {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Standortdetails",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }

                            currentFinding?.note?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!isEditMode) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Weitere Tierinfos",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        if (additionalAnimalInfo.isEmpty() && animal.observationTip.isBlank()) {
                            Text(
                                text = "Hier können später Lebensraum, Verbreitung, Seltenheit und weitere Informationen erscheinen.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        } else {
                            additionalAnimalInfo.forEach { (label, value) ->
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        animal.observationTip.takeIf { it.isNotBlank() }?.let {
                            Column(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Fundtipp",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Freunde",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        when {
                            currentUserId.isNullOrBlank() -> {
                                Text(
                                    text = "Melde dich an, um Funde von Freunden zu sehen.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            isLoadingFriendFindings -> {
                                Text(
                                    text = "Freundesfunde werden geladen...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            friendFindingsError != null -> {
                                Text(
                                    text = friendFindingsError.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            friendFindings.isEmpty() -> {
                                Text(
                                    text = "Noch keiner deiner Freunde hat dieses Tier gefunden.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            else -> {
                                Text(
                                    text = "Von Freunden gefunden",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                friendFindings.forEach { feedItem ->
                                    val friendPhotoSources = effectiveFriendPhotoSources(
                                        finding = feedItem.finding,
                                        ownerUserId = feedItem.friendUserId,
                                        currentUserId = currentUserId
                                    )
                                    var currentPhotoPage by remember(feedItem.friendUserId, feedItem.findingId, friendPhotoSources) {
                                        mutableStateOf(0)
                                    }
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.82f),
                                            contentColor = TextPrimary
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            FriendFindingPhotoBlock(
                                                photoSources = friendPhotoSources,
                                                hasPhoto = hasAnyFindingPhoto(feedItem.finding),
                                                onPageChanged = { currentPhotoPage = it }
                                            )
                                            Text(
                                                text = feedItem.friendDisplayName.ifBlank { "Unbenannter Nutzer" },
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextPrimary
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                FindingMetaRow(
                                                    date = feedItem.finding.date,
                                                    location = feedItem.finding.location,
                                                    latitude = feedItem.finding.latitude,
                                                    longitude = feedItem.finding.longitude,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                FindingPhotoCounter(
                                                    currentPage = currentPhotoPage,
                                                    totalCount = friendPhotoSources.size
                                                )
                                            }
                                            feedItem.finding.note.takeIf { it.isNotBlank() }?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }
                                            if (feedItem.friendUserId != currentUserId) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TextButton(
                                                        onClick = {
                                                            friendFindingsError = null
                                                            FriendRepository.toggleLikeForFinding(
                                                                ownerUserId = feedItem.friendUserId,
                                                                findingId = feedItem.findingId,
                                                                currentUserId = currentUserId.orEmpty(),
                                                                currentDisplayName = currentDisplayName,
                                                                currentlyLiked = feedItem.likedByCurrentUser,
                                                                onResult = { isNowLiked ->
                                                                    friendFindings = friendFindings.map { existingItem ->
                                                                        if (existingItem.friendUserId == feedItem.friendUserId &&
                                                                            existingItem.findingId == feedItem.findingId
                                                                        ) {
                                                                            existingItem.copy(
                                                                                likedByCurrentUser = isNowLiked,
                                                                                likeCount = if (isNowLiked) {
                                                                                    existingItem.likeCount + 1
                                                                                } else {
                                                                                    max(0, existingItem.likeCount - 1)
                                                                                }
                                                                            )
                                                                        } else {
                                                                            existingItem
                                                                        }
                                                                    }
                                                                },
                                                                onError = {
                                                                    friendFindingsError = "Fehler beim Liken"
                                                                }
                                                            )
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Favorite,
                                                            contentDescription = null,
                                                            tint = if (feedItem.likedByCurrentUser) {
                                                                PrimaryGreen
                                                            } else {
                                                                TextSecondary
                                                            },
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Gefällt mir",
                                                            color = if (feedItem.likedByCurrentUser) {
                                                                PrimaryGreen
                                                            } else {
                                                                TextSecondary
                                                            }
                                                        )
                                                    }
                                                    Text(
                                                        text = "${feedItem.likeCount} Likes",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (editingFinding != null && !isEditMode) {
            item {
                Button(
                    onClick = {
                        editingFinding = currentFinding
                        date = currentFinding?.date.orEmpty()
                        location = currentFinding?.location.orEmpty()
                        note = currentFinding?.note.orEmpty()
                        selectedTaggedFriendIds = currentFinding?.taggedFriendIds.orEmpty()
                        latitude = currentFinding?.latitude
                        longitude = currentFinding?.longitude
                        locationSource = currentFinding?.locationSource
                        locationStatusMessage = ""
                        selectedPhotoUris = currentFinding?.let(::effectiveLocalPhotoUris).orEmpty()
                        selectedPhotoPage = 0
                        cropPhotoIndex = null
                        cropPhotoUri = null
                        draggingPhotoUri = null
                        draggingPhotoOffsetX = 0f
                        isEditMode = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    )
                ) {
                    Text("Bearbeiten")
                }
            }
        }

        if (
            !showsFindingFormOnly &&
            !isEditMode &&
            (
                !dailyAnimalHistoryText.isNullOrBlank() ||
                    wishAnimalFriendsText != null ||
                    favoriteAnimalFriendsText != null
            )
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        dailyAnimalHistoryText?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        wishAnimalFriendsText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        favoriteAnimalFriendsText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (isEditMode) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (editingFinding == null) "Neuen Fund eintragen" else "Fund bearbeiten",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        FindingPhotoEditorSection()

                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Datum") },
                            trailingIcon = {
                                if (date.isNotBlank()) {
                                    IconButton(onClick = { date = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Datum löschen",
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Fundort") },
                            trailingIcon = {
                                if (latitude != null && longitude != null) {
                                    IconButton(
                                        onClick = {
                                            val previousLatitude = latitude
                                            val previousLongitude = longitude
                                            val previousLocationSource = locationSource
                                            val autoLocationText = if (
                                                previousLatitude != null &&
                                                previousLongitude != null
                                            ) {
                                                formatCoordinates(
                                                    previousLatitude,
                                                    previousLongitude
                                                )
                                            } else {
                                                null
                                            }
                                            latitude = null
                                            longitude = null
                                            locationSource = null
                                            if (
                                                previousLocationSource in setOf("gps", "map") &&
                                                autoLocationText != null &&
                                                location == autoLocationText
                                            ) {
                                                location = ""
                                            }
                                            locationStatusMessage = "Standort entfernt"
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Standort entfernen",
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                        )

                        OutlinedButton(
                            onClick = {
                                val fineLocationGranted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                val coarseLocationGranted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (fineLocationGranted || coarseLocationGranted) {
                                    requestCurrentLocation()
                                } else {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Aktuellen Standort übernehmen")
                        }

                        OutlinedButton(
                            onClick = {
                                pendingLatitude = latitude
                                pendingLongitude = longitude
                                showLocationPicker = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Standort auf Karte wählen")
                        }

                        val currentLocationStatus = when {
                            locationSource == "map" && latitude != null && longitude != null ->
                                "Standort auf Karte gewählt"
                            locationSource == "gps" && latitude != null && longitude != null ->
                                "Standort gespeichert"
                            latitude != null && longitude != null ->
                                "Standort gespeichert"
                            else -> locationStatusMessage
                        }
                        if (currentLocationStatus.isNotBlank()) {
                            Text(
                                text = currentLocationStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Notiz") },
                            trailingIcon = {
                                if (note.isNotBlank()) {
                                    IconButton(onClick = { note = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Notiz löschen",
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = PrimaryGreen
                            )
                        )

                        val hiddenTaggedFriendIds = selectedTaggedFriendIds.filterNot { selectedId ->
                            availableFriends.any { it.userId == selectedId }
                        }

                        if (!currentUserId.isNullOrBlank()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Mit Freunden gefunden",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextSecondary
                                )

                                when {
                                    availableFriendsError != null -> {
                                        Text(
                                            text = "Freunde konnten gerade nicht geladen werden.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }

                                    availableFriends.isEmpty() -> {
                                        Text(
                                            text = "Keine bestätigten Freunde verfügbar.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }

                                    else -> {
                                        availableFriends.forEach { friend ->
                                            val isSelected = friend.userId in selectedTaggedFriendIds
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedTaggedFriendIds =
                                                            if (isSelected) {
                                                                (selectedTaggedFriendIds - friend.userId).distinct()
                                                            } else {
                                                                (selectedTaggedFriendIds + friend.userId).distinct()
                                                            }
                                                    }
                                                    .padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = { checked ->
                                                        selectedTaggedFriendIds =
                                                            if (checked) {
                                                                (selectedTaggedFriendIds + friend.userId).distinct()
                                                            } else {
                                                                (selectedTaggedFriendIds - friend.userId).distinct()
                                                            }
                                                    }
                                                )
                                                Text(
                                                    text = friend.displayName.ifBlank { "Unbenannter Freund" },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextPrimary
                                                )
                                            }
                                        }

                                        if (hiddenTaggedFriendIds.isNotEmpty()) {
                                            Text(
                                                text = "Bestehende Tags von aktuell nicht verfügbaren Freunden bleiben erhalten.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (
                                        date.isNotBlank() ||
                                        location.isNotBlank() ||
                                        note.isNotBlank() ||
                                        selectedPhotoUris.isNotEmpty()
                                    ) {
                                        val existingLocalPhotoUris = currentFinding
                                            ?.let(::effectiveLocalPhotoUris)
                                            .orEmpty()
                                        val selectedLocalPhotoUris = selectedPhotoUris
                                            .map { it.trim() }
                                            .filter { it.isNotBlank() }
                                            .distinct()
                                            .take(3)
                                        val rawLocalPhotoUris = when {
                                            selectedLocalPhotoUris.isNotEmpty() -> selectedLocalPhotoUris
                                            editingFinding != null -> existingLocalPhotoUris
                                            else -> emptyList()
                                        }
                                        val storedPhotoUris = rawLocalPhotoUris
                                            .map { photoValue ->
                                                persistPhotoForFinding(context, photoValue)
                                            }
                                            .map { it.trim() }
                                            .filter { it.isNotBlank() }
                                            .distinct()
                                            .take(3)
                                        val existingRemotePhotoPaths = currentFinding
                                            ?.let(::effectiveRemotePhotoPaths)
                                            .orEmpty()
                                        val synchronizedRemotePhotoPaths = if (
                                            editingFinding != null &&
                                            selectedLocalPhotoUris == existingLocalPhotoUris
                                        ) {
                                            existingRemotePhotoPaths
                                        } else {
                                            emptyList()
                                        }
                                        val synchronizedThumbnailRemotePhotoPath = if (
                                            editingFinding != null &&
                                            selectedLocalPhotoUris == existingLocalPhotoUris
                                        ) {
                                            currentFinding?.thumbnailRemotePhotoPath.orEmpty()
                                        } else {
                                            ""
                                        }

                                        val newFinding = AnimalFinding(
                                            roomId = editingFinding?.roomId,
                                            animalId = animal.id,
                                            date = date.trim(),
                                            location = location.trim(),
                                            note = note.trim(),
                                            photoUri = storedPhotoUris.firstOrNull().orEmpty(),
                                            remotePhotoPath = synchronizedRemotePhotoPaths.firstOrNull().orEmpty(),
                                            thumbnailRemotePhotoPath = synchronizedThumbnailRemotePhotoPath,
                                            photoUris = storedPhotoUris,
                                            remotePhotoPaths = synchronizedRemotePhotoPaths,
                                            latitude = latitude,
                                            longitude = longitude,
                                            locationSource = locationSource,
                                            ownerId = editingFinding?.ownerId,
                                            taggedFriendIds = (
                                                hiddenTaggedFriendIds +
                                                    availableFriends
                                                        .map { it.userId }
                                                        .filter { it in selectedTaggedFriendIds }
                                            ).distinct()
                                        )

                                        if (editingFinding == null) {
                                            onSaveFinding(newFinding)
                                            onBackClick()

                                            date = ""
                                            location = ""
                                            note = ""
                                            selectedPhotoUris = emptyList()
                                            selectedPhotoPage = 0
                                            cropPhotoIndex = null
                                            cropPhotoUri = null
                                            draggingPhotoUri = null
                                            draggingPhotoOffsetX = 0f
                                            selectedTaggedFriendIds = emptyList()
                                            latitude = null
                                            longitude = null
                                            locationSource = null
                                            locationStatusMessage = ""
                                            editingFinding = null
                                        } else {
                                            onUpdateFinding(
                                                editingFinding!!,
                                                newFinding
                                            )
                                            editingFinding = newFinding
                                            selectedPhotoUris = storedPhotoUris
                                            selectedPhotoPage = 0
                                            cropPhotoIndex = null
                                            cropPhotoUri = null
                                            draggingPhotoUri = null
                                            draggingPhotoOffsetX = 0f
                                            if (!startInFindingEditMode) {
                                                isEditMode = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (editingFinding == null) "Fund speichern"
                                    else "Änderungen speichern"
                                )
                            }

                            if (editingFinding != null) {
                                OutlinedButton(
                                    onClick = {
                                        date = currentFinding?.date.orEmpty()
                                        location = currentFinding?.location.orEmpty()
                                        note = currentFinding?.note.orEmpty()
                                        selectedTaggedFriendIds = currentFinding?.taggedFriendIds.orEmpty()
                                        latitude = currentFinding?.latitude
                                        longitude = currentFinding?.longitude
                                        locationSource = currentFinding?.locationSource
                                        locationStatusMessage = ""
                                        selectedPhotoUris =
                                            currentFinding?.let(::effectiveLocalPhotoUris).orEmpty()
                                        selectedPhotoPage = 0
                                        cropPhotoIndex = null
                                        cropPhotoUri = null
                                        draggingPhotoUri = null
                                        draggingPhotoOffsetX = 0f
                                        if (startInFindingEditMode && currentFinding != null) {
                                            onReturnToFindingDetail(currentFinding)
                                        } else {
                                            isEditMode = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Text("Abbrechen")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isEditMode && editingFinding != null) {
            item {
                OutlinedButton(
                    onClick = {
                        editingFinding?.let {
                            onDeleteFinding(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text("Fund löschen")
                }
            }
        }
    }

    if (cropPhotoIndex != null && !cropPhotoUri.isNullOrBlank()) {
        val photoIndexToCrop = cropPhotoIndex ?: -1
        val photoUriToCrop = cropPhotoUri.orEmpty()
        CropPhotoDialog(
            uriString = photoUriToCrop,
            onDismiss = {
                cropPhotoIndex = null
                cropPhotoUri = null
            },
            onCropComplete = { croppedPhotoUri ->
                selectedPhotoUris = normalizeSelectedPhotoUris(
                    selectedPhotoUris
                    .mapIndexed { index, existingPhotoUri ->
                        if (index == photoIndexToCrop) croppedPhotoUri else existingPhotoUri
                    }
                )
                selectedPhotoPage = photoIndexToCrop.coerceAtMost(selectedPhotoUris.lastIndex.coerceAtLeast(0))
                cropPhotoIndex = null
                cropPhotoUri = null
            }
        )
    }

    if (showLocationPicker) {
        Dialog(
            onDismissRequest = { showLocationPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = CardBackground
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Standort auf Karte wählen",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    LocationPickerMap(
                        initialLatitude = pendingLatitude,
                        initialLongitude = pendingLongitude,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        onLocationSelected = { selectedLatitude, selectedLongitude ->
                            pendingLatitude = selectedLatitude
                            pendingLongitude = selectedLongitude
                        }
                    )

                    Button(
                        onClick = {
                            if (pendingLatitude == null || pendingLongitude == null) {
                                locationStatusMessage = "Bitte Standort auf der Karte auswählen"
                            } else {
                                latitude = pendingLatitude
                                longitude = pendingLongitude
                                locationSource = "map"
                                location = formatCoordinates(
                                    pendingLatitude!!,
                                    pendingLongitude!!
                                )
                                locationStatusMessage = "Standort auf Karte gewählt"
                                showLocationPicker = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Übernehmen")
                    }

                    OutlinedButton(
                        onClick = { showLocationPicker = false },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Text("Abbrechen")
                    }
                }
            }
        }
    }
}

            @Composable
            private fun AnimalListScreen(
                debugMessage: String,
                onOpenSettings: () -> Unit,
                searchText: String,
                onSearchTextChange: (String) -> Unit,
                animals: List<AnimalEntry>,
                totalAnimalCount: Int,
                collectedAnimalCount: Int,
                showFoundOnly: Boolean,
                onToggleShowFoundOnly: () -> Unit,
                onResetFiltersAndSort: () -> Unit,
                availableGroups: List<String>,
                selectedGroup: String,
                onSelectedGroupChange: (String) -> Unit,
                availableSubgroups: List<String>,
                selectedSubgroup: String,
                onSelectedSubgroupChange: (String) -> Unit,
                findingCountByAnimalId: Map<String, Int>,
                animalGlobalFindingCounts: Map<String, Int>,
                animalGlobalFindingCountsLoaded: Boolean,
                animalGlobalFindingCountsLoadAttempted: Boolean,
                onAnimalClick: (AnimalEntry) -> Unit,
                onOpenMap: (() -> Unit)? = null,
                currentSortOption: String,
                onSortOptionChange: (String) -> Unit,
                isPickerMode: Boolean = false,
                extraTopPadding: Dp = 0.dp,
                extraBottomPadding: Dp = 0.dp
            ) {
                val sortLabel = when (currentSortOption) {
                    "A_Z" -> "A-Z"
                    "Z_A" -> "Z-A"
                    "FOUND_FIRST" -> "Gefundene zuerst"
                    "NOT_FOUND_FIRST" -> "Offene zuerst"
                    else -> "A-Z"
                }
                var groupMenuExpanded by remember { mutableStateOf(false) }
                var subgroupMenuExpanded by remember { mutableStateOf(false) }
                var sortMenuExpanded by remember { mutableStateOf(false) }
                val subgroupEnabled = selectedGroup != "Alle"
                val hasActiveFiltersOrSort = showFoundOnly ||
                        selectedGroup != "Alle" ||
                        selectedSubgroup != "Alle" ||
                        currentSortOption != "A_Z"

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(
                        top = extraTopPadding + 16.dp,
                        bottom = extraBottomPadding + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isPickerMode) debugMessage else "Mein Tierdex",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextPrimary
                                )

                                if (!isPickerMode) {
                                    Text(
                                        text = "Gesammelt: $collectedAnimalCount von $totalAnimalCount Arten",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (!isPickerMode && onOpenMap != null) {
                                OutlinedButton(
                                    onClick = onOpenMap,
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Map,
                                        contentDescription = null,
                                        tint = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Fundorte",
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = onSearchTextChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Tier suchen") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (searchText.isNotEmpty()) {
                                        IconButton(onClick = { onSearchTextChange("") }) {
                                            Icon(Icons.Default.Close, "Suche löschen")
                                        }
                                    }
                                }
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isPickerMode) {
                                    FilterChip(
                                        label = if (showFoundOnly) "Nur gefundene" else "Alle anzeigen",
                                        onClick = onToggleShowFoundOnly,
                                        active = showFoundOnly
                                    )
                                }

                                FilterDropdown(
                                    label = "Gruppe: $selectedGroup",
                                    expanded = groupMenuExpanded,
                                    onDismiss = { groupMenuExpanded = false },
                                    onClick = { groupMenuExpanded = true },
                                    active = selectedGroup != "Alle"
                                ) {
                                    availableGroups.forEach { group ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = group,
                                                    color = TextPrimary
                                                )
                                            },
                                            colors = MenuDefaults.itemColors(
                                                textColor = TextPrimary
                                            ),
                                            onClick = {
                                                onSelectedGroupChange(group)
                                                groupMenuExpanded = false
                                            }
                                        )
                                    }
                                }

                                FilterDropdown(
                                    label = if (subgroupEnabled) "Untergr.: $selectedSubgroup" else "Untergr.: Gruppe auswählen",
                                    expanded = subgroupMenuExpanded,
                                    onDismiss = { subgroupMenuExpanded = false },
                                    onClick = { subgroupMenuExpanded = true },
                                    enabled = subgroupEnabled,
                                    active = subgroupEnabled && selectedSubgroup != "Alle"
                                ) {
                                    availableSubgroups.forEach { subgroup ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = subgroup,
                                                    color = TextPrimary
                                                )
                                            },
                                            colors = MenuDefaults.itemColors(
                                                textColor = TextPrimary
                                            ),
                                            onClick = {
                                                onSelectedSubgroupChange(subgroup)
                                                subgroupMenuExpanded = false
                                            }
                                        )
                                    }
                                }

                                FilterDropdown(
                                    label = "Sort.: $sortLabel",
                                    expanded = sortMenuExpanded,
                                    onDismiss = { sortMenuExpanded = false },
                                    onClick = { sortMenuExpanded = true },
                                    active = currentSortOption != "A_Z"
                                ) {
                                    listOf(
                                        "A_Z" to "A-Z",
                                        "Z_A" to "Z-A",
                                        "FOUND_FIRST" to "Gefundene zuerst",
                                        "NOT_FOUND_FIRST" to "Offene zuerst"
                                    ).forEach { (option, label) ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = label,
                                                    color = TextPrimary
                                                )
                                            },
                                            colors = MenuDefaults.itemColors(
                                                textColor = TextPrimary
                                            ),
                                            onClick = {
                                                onSortOptionChange(option)
                                                sortMenuExpanded = false
                                            }
                                        )
                                    }
                                }

                                if (hasActiveFiltersOrSort) {
                                    OutlinedButton(
                                        onClick = onResetFiltersAndSort,
                                        modifier = Modifier.height(40.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, BorderColor),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = CardBackground,
                                            contentColor = TextPrimary
                                        )
                                    ) {
                                        Text(
                                            "Zurücksetzen",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (animals.isEmpty()) {
                        item {
                            Text(
                                text = "Keine Tiere gefunden.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    } else {
                        items(animals) { animal ->
                            val findingCount = findingCountByAnimalId[animal.id] ?: 0
                            val globalFindingCount = if (animalGlobalFindingCountsLoaded) {
                                max(animalGlobalFindingCounts[animal.id] ?: 0, findingCount)
                            } else {
                                null
                            }
                            AnimalListItem(
                                animal = animal,
                                findingCount = findingCount,
                                globalFindingCount = globalFindingCount,
                                animalGlobalFindingCountsLoadAttempted = animalGlobalFindingCountsLoadAttempted,
                                showGlobalFindingCount = !isPickerMode,
                                onClick = { onAnimalClick(animal) }
                            )
                        }
                    }
                }
            }

            @Composable
            fun FilterChip(label: String, onClick: () -> Unit, active: Boolean) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (active) PrimaryGreen.copy(alpha = 0.1f) else Color.Transparent,
                        contentColor = if (active) PrimaryGreen else TextPrimary
                    ),
                    border = if (active) BorderStroke(1.dp, PrimaryGreen) else BorderStroke(
                        1.dp,
                        BorderColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.bodySmall)
                }
            }

            @Composable
            fun FilterDropdown(
                label: String,
                expanded: Boolean,
                onDismiss: () -> Unit,
                onClick: () -> Unit,
                enabled: Boolean = true,
                active: Boolean = false,
                content: @Composable () -> Unit
            ) {
                Box {
                    OutlinedButton(
                        onClick = onClick,
                        enabled = enabled,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        border = if (active) BorderStroke(
                            1.dp,
                            PrimaryGreen
                        ) else BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (active) PrimaryGreen.copy(alpha = 0.1f) else CardBackground,
                            contentColor = if (active) PrimaryGreen else TextPrimary
                        )
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (active) PrimaryGreen else TextPrimary,
                            maxLines = 1
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = onDismiss,
                        modifier = Modifier.background(CardBackground)
                    ) {
                        content()
                    }
                }
            }

            @Composable
            fun AnimalListItem(
                animal: AnimalEntry,
                findingCount: Int,
                globalFindingCount: Int?,
                animalGlobalFindingCountsLoadAttempted: Boolean,
                showGlobalFindingCount: Boolean,
                onClick: () -> Unit
            ) {

                val isFound = findingCount > 0

                val icon = when (animal.group) {
                    "Vögel" -> Icons.Filled.Air
                    "Fische" -> Icons.Filled.SetMeal
                    "Säugetiere" -> Icons.Filled.Pets
                    "Reptilien" -> Icons.Filled.BugReport
                    "Amphibien" -> Icons.Filled.WaterDrop
                    else -> Icons.Filled.Help
                }

                val backgroundColor by animateColorAsState(
                    targetValue = if (isFound) PrimaryGreenSoft else CardBackground,
                    label = "CardBackgroundAnimation"
                )

                Card(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor,
                        contentColor = TextPrimary
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 2.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = animal.germanName,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = animal.latinName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Text(
                                text = if (findingCount > 0) {
                                    "Davon eigene Funde: $findingCount"
                                } else {
                                    "Nicht gefunden"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (findingCount > 0) PrimaryGreen else TextSecondary
                            )

                            if (showGlobalFindingCount && globalFindingCount != null) {
                                Text(
                                    text = "Globale Funde: $globalFindingCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            } else if (showGlobalFindingCount) {
                                Text(
                                    text = "Globale Funde: …",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (isFound) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = PrimaryGreen.copy(alpha = 0.12f),
                                contentColor = PrimaryGreen
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Gefunden",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }


            @Composable
            private fun CropPhotoDialog(
                uriString: String,
                onDismiss: () -> Unit,
                onCropComplete: (String) -> Unit
            ) {
                Dialog(
                    onDismissRequest = onDismiss,
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    val context = LocalContext.current
                    val density = LocalDensity.current
                    var bitmap by remember(uriString) { mutableStateOf<Bitmap?>(null) }
                    var containerSize by remember { mutableStateOf(IntSize.Zero) }
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    LaunchedEffect(uriString) {
                        bitmap = withContext(Dispatchers.IO) {
                            loadCorrectlyOrientedBitmapFromUriString(
                                context = context,
                                uriString = uriString,
                                maxImageSizePx = null
                            )
                        }
                    }

                    val loadedBitmap = bitmap
                    val cropSizePx = remember(containerSize) {
                        min(containerSize.width, containerSize.height) * 0.72f
                    }
                    val baseImageSize = remember(loadedBitmap, containerSize) {
                        if (loadedBitmap == null || containerSize == IntSize.Zero) {
                            Pair(0f, 0f)
                        } else {
                            val widthScale =
                                containerSize.width.toFloat() / loadedBitmap.width.toFloat()
                            val heightScale =
                                containerSize.height.toFloat() / loadedBitmap.height.toFloat()
                            val fitScale = min(widthScale, heightScale)
                            Pair(
                                loadedBitmap.width * fitScale,
                                loadedBitmap.height * fitScale
                            )
                        }
                    }
                    val minScale = remember(baseImageSize, cropSizePx) {
                        val baseWidth = baseImageSize.first
                        val baseHeight = baseImageSize.second
                        if (baseWidth <= 0f || baseHeight <= 0f || cropSizePx <= 0f) {
                            1f
                        } else {
                            max(1f, max(cropSizePx / baseWidth, cropSizePx / baseHeight))
                        }
                    }
                    val maxScale = max(5f, minScale)

                    LaunchedEffect(minScale) {
                        if (scale < minScale) {
                            scale = minScale
                            offset = Offset.Zero
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.92f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { onDismiss() })
                                }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 96.dp)
                                .onSizeChanged { containerSize = it },
                            contentAlignment = Alignment.Center
                        ) {
                            loadedBitmap?.let { safeBitmap ->
                                Image(
                                    bitmap = safeBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(
                                            safeBitmap,
                                            minScale,
                                            cropSizePx,
                                            containerSize
                                        ) {
                                            detectTransformGestures { _, pan, zoom, _ ->
                                                val baseWidth = baseImageSize.first
                                                val baseHeight = baseImageSize.second
                                                if (baseWidth <= 0f || baseHeight <= 0f || cropSizePx <= 0f) return@detectTransformGestures

                                                val newScale =
                                                    (scale * zoom).coerceIn(minScale, maxScale)
                                                val scaledWidth = baseWidth * newScale
                                                val scaledHeight = baseHeight * newScale
                                                val maxOffsetX =
                                                    max(0f, (scaledWidth - cropSizePx) / 2f)
                                                val maxOffsetY =
                                                    max(0f, (scaledHeight - cropSizePx) / 2f)
                                                val newOffset = offset + pan

                                                scale = newScale
                                                offset = Offset(
                                                    x = newOffset.x.coerceIn(
                                                        -maxOffsetX,
                                                        maxOffsetX
                                                    ),
                                                    y = newOffset.y.coerceIn(
                                                        -maxOffsetY,
                                                        maxOffsetY
                                                    )
                                                )
                                            }
                                        }
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            translationX = offset.x
                                            translationY = offset.y
                                        }
                                )

                                if (cropSizePx > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .size(with(density) { cropSizePx.toDp() })
                                            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Abbrechen", color = Color.White)
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                shadowElevation = 2.dp
                            ) {
                                IconButton(
                                    onClick = {
                                        val safeBitmap = loadedBitmap ?: return@IconButton
                                        val croppedBitmap = cropBitmapToCenterFrame(
                                            bitmap = safeBitmap,
                                            containerSize = containerSize,
                                            baseImageWidth = baseImageSize.first,
                                            baseImageHeight = baseImageSize.second,
                                            cropSizePx = cropSizePx,
                                            scale = scale,
                                            offset = offset
                                        ) ?: return@IconButton

                                        val croppedUri = saveBitmapForFinding(context, croppedBitmap)
                                        if (croppedUri.isNotBlank()) {
                                            onCropComplete(croppedUri)
                                        }
                                    },
                                    enabled = loadedBitmap != null && containerSize != IntSize.Zero,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = "Zuschnitt bestätigen",
                                        tint = Color.White
                                    )
                                }
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Schließen",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            private fun cropBitmapToCenterFrame(
                bitmap: Bitmap,
                containerSize: IntSize,
                baseImageWidth: Float,
                baseImageHeight: Float,
                cropSizePx: Float,
                scale: Float,
                offset: Offset
            ): Bitmap? {
                if (containerSize == IntSize.Zero || baseImageWidth <= 0f || baseImageHeight <= 0f || cropSizePx <= 0f) {
                    return null
                }

                val scaledWidth = baseImageWidth * scale
                val scaledHeight = baseImageHeight * scale
                val imageLeft = (containerSize.width / 2f) + offset.x - (scaledWidth / 2f)
                val imageTop = (containerSize.height / 2f) + offset.y - (scaledHeight / 2f)
                val cropLeft = (containerSize.width - cropSizePx) / 2f
                val cropTop = (containerSize.height - cropSizePx) / 2f

                val bitmapLeft =
                    (((cropLeft - imageLeft) / scaledWidth) * bitmap.width).roundToInt()
                        .coerceIn(0, bitmap.width - 1)
                val bitmapTop = (((cropTop - imageTop) / scaledHeight) * bitmap.height).roundToInt()
                    .coerceIn(0, bitmap.height - 1)
                val bitmapRight =
                    ((((cropLeft + cropSizePx) - imageLeft) / scaledWidth) * bitmap.width).roundToInt()
                        .coerceIn(bitmapLeft + 1, bitmap.width)
                val bitmapBottom =
                    ((((cropTop + cropSizePx) - imageTop) / scaledHeight) * bitmap.height).roundToInt()
                        .coerceIn(bitmapTop + 1, bitmap.height)

                return try {
                    Bitmap.createBitmap(
                        bitmap,
                        bitmapLeft,
                        bitmapTop,
                        bitmapRight - bitmapLeft,
                        bitmapBottom - bitmapTop
                    )
                } catch (_: Exception) {
                    null
                }
            }

            fun saveBitmapForFinding(context: Context, bitmap: Bitmap): String {
                return try {
                    val imagesDir = File(context.filesDir, FINDING_IMAGES_DIR).apply { mkdirs() }
                    val fileName = "${UUID.randomUUID()}.jpg"
                    val targetFile = File(imagesDir, fileName)

                    targetFile.outputStream().use { output ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
                    }

                    "internal://$fileName"
                } catch (_: Exception) {
                    ""
                }
            }

            @Composable
            fun UriImage(
                uriString: String,
                maxImageSizePx: Int? = null,
                modifier: Modifier = Modifier
            ) {
                val context = LocalContext.current
                var showFullscreenZoom by rememberSaveable(uriString) { mutableStateOf(false) }
                val cacheKey = remember(uriString, maxImageSizePx) {
                    uriImageCacheKey(uriString, maxImageSizePx)
                }
                var bitmap by remember(cacheKey) {
                    mutableStateOf(uriImageMemoryCache.get(cacheKey))
                }
                var loadFinished by remember(cacheKey) {
                    mutableStateOf(bitmap != null)
                }

                LaunchedEffect(cacheKey) {
                    if (bitmap == null) {
                        val imageLoadStartedAt = SystemClock.elapsedRealtime()
                        if (uriString.startsWith(STORAGE_URI_PREFIX)) {
                            val isThumbnail = uriString.contains("thumb_photo", ignoreCase = true)
                            Log.d(
                                "FriendPhotoTiming",
                                "UriImage load start isRemote=true isThumbnail=$isThumbnail maxImageSizePx=${maxImageSizePx ?: -1}"
                            )
                        }
                        if (maxImageSizePx != null) {
                            Log.d(
                                "ProfilePerformance",
                                "Loading preview image for $uriString with maxSize=$maxImageSizePx"
                            )
                        }
                        bitmap = withContext(Dispatchers.IO) {
                            loadCorrectlyOrientedBitmapFromUriString(
                                context = context,
                                uriString = uriString,
                                maxImageSizePx = maxImageSizePx
                            )
                        }?.also { loadedBitmap ->
                            uriImageMemoryCache.put(cacheKey, loadedBitmap)
                        }
                        if (uriString.startsWith(STORAGE_URI_PREFIX)) {
                            val isThumbnail = uriString.contains("thumb_photo", ignoreCase = true)
                            Log.d(
                                "FriendPhotoTiming",
                                "UriImage load end isRemote=true isThumbnail=$isThumbnail success=${bitmap != null} durationMs=${SystemClock.elapsedRealtime() - imageLoadStartedAt}"
                            )
                        }
                    }
                    loadFinished = true
                }

                bitmap?.let { loadedBitmap ->
                    Image(
                        bitmap = loadedBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = modifier.clickable {
                            showFullscreenZoom = true
                        },
                        contentScale = ContentScale.Crop
                    )
                }

                if (
                    bitmap == null &&
                    !loadFinished &&
                    (uriString.startsWith("internal://") || uriString.startsWith(STORAGE_URI_PREFIX))
                ) {
                    Box(
                        modifier = modifier.background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Foto wird geladen…",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (
                    bitmap == null &&
                    loadFinished &&
                    (uriString.startsWith("internal://") || uriString.startsWith(STORAGE_URI_PREFIX))
                ) {
                    Box(
                        modifier = modifier.background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uriString.startsWith(STORAGE_URI_PREFIX)) {
                                "Foto konnte gerade nicht aus der Cloud geladen werden"
                            } else {
                                "Foto auf diesem Gerät nicht verfügbar"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (showFullscreenZoom) {
                    FullscreenZoomImageDialog(
                        uriString = uriString,
                        onDismiss = { showFullscreenZoom = false }
                    )
                }
            }

            @Composable
            private fun FullscreenZoomImageDialog(
                uriString: String,
                onDismiss: () -> Unit
            ) {
                Dialog(
                    onDismissRequest = onDismiss,
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    val context = LocalContext.current
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }
                    var bitmap by remember(uriString) {
                        mutableStateOf<Bitmap?>(
                            uriImageMemoryCache.get(
                                uriImageCacheKey(
                                    uriString,
                                    null
                                )
                            )
                        )
                    }

                    LaunchedEffect(uriString) {
                        if (bitmap == null) {
                            bitmap = withContext(Dispatchers.IO) {
                                loadCorrectlyOrientedBitmapFromUriString(
                                    context = context,
                                    uriString = uriString,
                                    maxImageSizePx = null
                                )
                            }?.also { loadedBitmap ->
                                uriImageMemoryCache.put(
                                    uriImageCacheKey(uriString, null),
                                    loadedBitmap
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { onDismiss() })
                                }
                        )

                        bitmap?.let { loadedBitmap ->
                            Image(
                                bitmap = loadedBitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                                    .pointerInput(uriString) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                                            scale = newScale
                                            offset = if (newScale <= 1f) {
                                                Offset.Zero
                                            } else {
                                                offset + pan
                                            }
                                        }
                                    }
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offset.x
                                        translationY = offset.y
                                    }
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Schließen",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            private fun persistReadPermission(context: Context, uri: Uri) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                } catch (_: Exception) {
                }
            }

            private fun loadAnimalsFromCsvWithDebug(
                context: Context,
                fileName: String
            ): CsvLoadResult {
                return try {
                    val assetFiles = context.assets.list("")?.toList().orEmpty()

                    if (!assetFiles.contains(fileName)) {
                        return CsvLoadResult(
                            animals = emptyList(),
                            debugMessage = "Datei nicht gefunden. Assets enthalten: ${assetFiles.joinToString()}"
                        )
                    }

                    val lines = context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { reader ->
                        reader.readLines()
                    }

                    if (lines.isEmpty()) {
                        return CsvLoadResult(
                            animals = emptyList(),
                            debugMessage = "CSV ist leer."
                        )
                    }

                    val cleanedLines = lines
                        .map { it.replace("\uFEFF", "").trim() }
                        .filter { it.isNotBlank() }

                    if (cleanedLines.isEmpty()) {
                        return CsvLoadResult(
                            animals = emptyList(),
                            debugMessage = "CSV enthält keine lesbaren Zeilen."
                        )
                    }

                    val header = cleanedLines.first()
                    val delimiter = if (header.contains(";")) ";" else ","

                    val animals = cleanedLines
                        .drop(1)
                        .mapNotNull { line ->
                            val parts = line.split(delimiter).map { it.trim() }

                            if (parts.size < 5) {
                                null
                            } else {
                                AnimalEntry(
                                    id = parts.getOrElse(0) { "" },
                                    group = parts.getOrElse(1) { "" },
                                    subgroup = parts.getOrElse(2) { "" },
                                    germanName = parts.getOrElse(3) { "" },
                                    latinName = parts.getOrElse(4) { "" },
                                    habitat = parts.getOrElse(5) { "" },
                                    distribution = parts.getOrElse(6) { "" },
                                    rarity = parts.getOrElse(7) { "" },
                                    habitats = emptyList(),
                                    distributionGermany = "",
                                    rarityGame = "",
                                    redListGermany = "",
                                    activity = "",
                                    season = "",
                                    protectionStatus = "",
                                    shortDescription = "",
                                    observationTip = "",
                                    sources = emptyList(),
                                    needsReview = true,
                                    reviewNote = ""
                                )
                            }
                        }

                    CsvLoadResult(
                        animals = animals,
                        debugMessage = "Datei gefunden. Zeilen: ${cleanedLines.size}. Trennzeichen: '$delimiter'"
                    )
                } catch (e: Exception) {
                    CsvLoadResult(
                        animals = emptyList(),
                        debugMessage = "Fehler beim Laden: ${e.message}"
                    )
                }
            }

            private fun normalizeAnimalText(text: String): String {
                if (text.isBlank()) return text

                val repaired = try {
                    val candidate = text.toByteArray(Charsets.ISO_8859_1).toString(Charsets.UTF_8)
                    val originalScore = text.count { it == 'Ã' || it == 'Â' || it == 'â' }
                    val candidateScore = candidate.count { it == 'Ã' || it == 'Â' || it == 'â' }
                    if (candidateScore < originalScore) candidate else text
                } catch (_: Exception) {
                    text
                }

                return repaired.trim()
            }

            private fun JsonObject.stringOrEmpty(key: String): String {
                return get(key)
                    ?.takeIf { !it.isJsonNull }
                    ?.asString
                    ?.let(::normalizeAnimalText)
                    .orEmpty()
            }

            private fun JsonObject.stringListOrEmpty(key: String): List<String> {
                val element = get(key) ?: return emptyList()
                if (!element.isJsonArray) return emptyList()
                return element.asJsonArray.mapNotNull { item ->
                    item
                        ?.takeIf { !it.isJsonNull }
                        ?.asString
                        ?.let(::normalizeAnimalText)
                        ?.takeIf { it.isNotEmpty() }
                }
            }

            private fun JsonObject.booleanOrDefault(
                key: String,
                defaultValue: Boolean
            ): Boolean {
                return get(key)?.takeIf { !it.isJsonNull }?.asBoolean ?: defaultValue
            }

            private fun JsonObject.toAnimalEntry(): AnimalEntry {
                return AnimalEntry(
                    id = stringOrEmpty("id"),
                    group = stringOrEmpty("group"),
                    subgroup = stringOrEmpty("subgroup"),
                    germanName = stringOrEmpty("germanName"),
                    latinName = stringOrEmpty("latinName"),
                    habitat = stringOrEmpty("habitat"),
                    distribution = stringOrEmpty("distribution"),
                    rarity = stringOrEmpty("rarity"),
                    habitats = stringListOrEmpty("habitats"),
                    distributionGermany = stringOrEmpty("distributionGermany"),
                    rarityGame = stringOrEmpty("rarityGame"),
                    redListGermany = stringOrEmpty("redListGermany"),
                    activity = stringOrEmpty("activity"),
                    season = stringOrEmpty("season"),
                    protectionStatus = stringOrEmpty("protectionStatus"),
                    shortDescription = stringOrEmpty("shortDescription"),
                    observationTip = stringOrEmpty("observationTip"),
                    sources = stringListOrEmpty("sources"),
                    needsReview = booleanOrDefault("needsReview", true),
                    reviewNote = stringOrEmpty("reviewNote")
                )
            }

            private fun loadAnimalsFromJsonWithDebug(
                context: Context,
                jsonFileName: String,
                csvFallbackFileName: String
            ): CsvLoadResult {
                return try {
                    val assetFiles = context.assets.list("")?.toList().orEmpty()

                    if (!assetFiles.contains(jsonFileName)) {
                        val fallback = loadAnimalsFromCsvWithDebug(context, csvFallbackFileName)
                        fallback.copy(
                            debugMessage = "JSON nicht gefunden, CSV-Fallback aktiv. ${fallback.debugMessage}"
                        )
                    } else {
                        context.assets.open(jsonFileName).bufferedReader(Charsets.UTF_8).use { reader ->
                                val jsonText = reader.readText()
                                val parsedAnimals = Gson()
                                    .fromJson(jsonText, JsonArray::class.java)
                                    ?.mapNotNull { element ->
                                        element
                                            ?.takeIf { it.isJsonObject }
                                            ?.asJsonObject
                                            ?.toAnimalEntry()
                                    }
                                    .orEmpty()

                                if (parsedAnimals.isEmpty()) {
                                    val fallback =
                                        loadAnimalsFromCsvWithDebug(context, csvFallbackFileName)
                                    fallback.copy(
                                        debugMessage = "JSON leer oder unlesbar, CSV-Fallback aktiv. ${fallback.debugMessage}"
                                    )
                                } else {
                                    val sampleAnimal =
                                        parsedAnimals.firstOrNull { it.id == "saeugetier_braunes_langohr" }
                                    CsvLoadResult(
                                        animals = parsedAnimals,
                                        debugMessage = buildString {
                                            append("JSON geladen. Einträge: ${parsedAnimals.size}")
                                            sampleAnimal?.let { animal ->
                                                append(" Beispiel: ${animal.germanName}")
                                                animal.habitats.firstOrNull()?.let { habitat ->
                                                    append(" / $habitat")
                                                }
                                            }
                                        }
                                    )
                                }
                        }
                    }
                } catch (e: Exception) {
                    val fallback = loadAnimalsFromCsvWithDebug(context, csvFallbackFileName)
                    fallback.copy(
                        debugMessage = "JSON-Fehler, CSV-Fallback aktiv: ${e.message}. ${fallback.debugMessage}"
                    )
                }
            }

            private fun persistPhotoForFinding(context: Context, uriString: String): String {
                if (uriString.isBlank()) return ""
                if (uriString.startsWith("android.resource://")) return uriString
                if (uriString.startsWith("internal://")) return uriString

                return try {
                    val sourceUri = Uri.parse(uriString)
                    val imagesDir = File(context.filesDir, FINDING_IMAGES_DIR).apply { mkdirs() }

                    val fileName = "${UUID.randomUUID()}.img"
                    val targetFile = File(imagesDir, fileName)

                    context.contentResolver.openInputStream(sourceUri)?.use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    } ?: return uriString

                    "internal://$fileName"
                } catch (_: Exception) {
                    uriString
                }
            }

            private fun normalizeSearchText(text: String): String {
                return text
                    .lowercase()
                    .replace("ä", "ae")
                    .replace("ö", "oe")
                    .replace("ü", "ue")
                    .replace("ß", "ss")
                    .map { character ->
                        if (character.isLetterOrDigit()) character else ' '
                    }
                    .joinToString("")
                    .trim()
                    .replace(Regex("\\s+"), " ")
            }

            private fun tokenizeSearchText(text: String): List<String> {
                return normalizeSearchText(text)
                    .split(" ")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }

            fun loadCorrectlyOrientedBitmapFromUriString(
                context: Context,
                uriString: String,
                maxImageSizePx: Int? = null
            ): Bitmap? {
                if (uriString.isBlank()) return null

                return if (uriString.startsWith("internal://")) {
                    val fileName = uriString.removePrefix("internal://")
                    val file = File(File(context.filesDir, FINDING_IMAGES_DIR), fileName)
                    loadCorrectlyOrientedBitmapFromFile(file, maxImageSizePx)
                } else if (uriString.startsWith(STORAGE_URI_PREFIX)) {
                    loadCorrectlyOrientedBitmapFromStoragePath(
                        context = context,
                        storagePathFromUri(uriString).orEmpty(),
                        maxImageSizePx
                    )
                } else {
                    loadCorrectlyOrientedBitmapFromUri(
                        context,
                        Uri.parse(uriString),
                        maxImageSizePx
                    )
                }
            }

            fun loadCorrectlyOrientedBitmapFromUri(
                context: Context,
                uri: Uri,
                maxImageSizePx: Int? = null
            ): Bitmap? {
                val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                    decodeSampledBitmapFromStream(input, maxImageSizePx)
                } ?: return null

                val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
                    ExifInterface(input).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL

                return applyExifOrientation(bitmap, orientation)
            }

            fun loadCorrectlyOrientedBitmapFromStoragePath(
                context: Context,
                remotePhotoPath: String,
                maxImageSizePx: Int? = null
            ): Bitmap? {
                val decodeStartedAt = SystemClock.elapsedRealtime()
                val isThumbnail = remotePhotoPath.contains("thumb_photo", ignoreCase = true)
                Log.d(
                    "FriendPhotoTiming",
                    "bitmap decode start isRemote=true isThumbnail=$isThumbnail maxImageSizePx=${maxImageSizePx ?: -1}"
                )
                val imageBytes =
                    if (isThumbnail) {
                        FindingPhotoStorageRepository.loadFriendFeedThumbnailBytesCached(
                            context = context.applicationContext,
                            remotePhotoPath = remotePhotoPath
                        )
                    } else {
                        FindingPhotoStorageRepository.loadFindingPhotoBytes(remotePhotoPath)
                    } ?: return null
                val bitmap = decodeSampledBitmapFromBytes(imageBytes, maxImageSizePx) ?: return null
                val orientation = ByteArrayInputStream(imageBytes).use { input ->
                    ExifInterface(input).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }

                val orientedBitmap = applyExifOrientation(bitmap, orientation)
                Log.d(
                    "FriendPhotoTiming",
                    "bitmap decode end isRemote=true isThumbnail=$isThumbnail durationMs=${SystemClock.elapsedRealtime() - decodeStartedAt} sizeKb=${imageBytes.size / 1024}"
                )
                return orientedBitmap
            }

            fun loadCorrectlyOrientedBitmapFromFile(
                file: File,
                maxImageSizePx: Int? = null
            ): Bitmap? {
                if (!file.exists()) return null

                val bitmap =
                    decodeSampledBitmapFromFile(file.absolutePath, maxImageSizePx) ?: return null

                val orientation = file.inputStream().use { input ->
                    ExifInterface(input).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }

                return applyExifOrientation(bitmap, orientation)
            }

            fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
                return when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(bitmap, true)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(bitmap, false)
                    ExifInterface.ORIENTATION_TRANSPOSE -> rotateAndFlipBitmap(bitmap, 90f, true)
                    ExifInterface.ORIENTATION_TRANSVERSE -> rotateAndFlipBitmap(bitmap, 270f, true)
                    else -> bitmap
                }
            }

            fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
                val matrix = Matrix().apply {
                    postRotate(degrees)
                }
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            fun flipBitmap(bitmap: Bitmap, horizontal: Boolean): Bitmap {
                val matrix = Matrix().apply {
                    postScale(
                        if (horizontal) -1f else 1f,
                        if (horizontal) 1f else -1f
                    )
                }
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            fun rotateAndFlipBitmap(
                bitmap: Bitmap,
                degrees: Float,
                horizontalFlip: Boolean
            ): Bitmap {
                val matrix = Matrix().apply {
                    postRotate(degrees)
                    postScale(if (horizontalFlip) -1f else 1f, 1f)
                }
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            fun decodeSampledBitmapFromStream(
                inputStream: java.io.InputStream,
                maxImageSizePx: Int?
            ): Bitmap? {
                if (maxImageSizePx == null) {
                    return BitmapFactory.decodeStream(inputStream)
                }

                val imageBytes = inputStream.readBytes()
                val boundsOptions = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, boundsOptions)

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(
                        boundsOptions.outWidth,
                        boundsOptions.outHeight,
                        maxImageSizePx
                    )
                }

                return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, decodeOptions)
            }

            fun decodeSampledBitmapFromFile(filePath: String, maxImageSizePx: Int?): Bitmap? {
                if (maxImageSizePx == null) {
                    return BitmapFactory.decodeFile(filePath)
                }

                val boundsOptions = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(filePath, boundsOptions)

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(
                        boundsOptions.outWidth,
                        boundsOptions.outHeight,
                        maxImageSizePx
                    )
                }

                return BitmapFactory.decodeFile(filePath, decodeOptions)
            }

            fun decodeSampledBitmapFromBytes(imageBytes: ByteArray, maxImageSizePx: Int?): Bitmap? {
                if (imageBytes.isEmpty()) return null
                if (maxImageSizePx == null) {
                    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                }

                val boundsOptions = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, boundsOptions)

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(
                        boundsOptions.outWidth,
                        boundsOptions.outHeight,
                        maxImageSizePx
                    )
                }

                return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, decodeOptions)
            }

            fun calculateInSampleSize(width: Int, height: Int, maxImageSizePx: Int): Int {
                var inSampleSize = 1
                var currentWidth = width
                var currentHeight = height

                while (currentWidth > maxImageSizePx || currentHeight > maxImageSizePx) {
                    currentWidth /= 2
                    currentHeight /= 2
                    inSampleSize *= 2
                }

                return inSampleSize.coerceAtLeast(1)
            }

            private val LightColorScheme = lightColorScheme(
                primary = PrimaryGreen,
                secondary = TextSecondary,
                tertiary = PrimaryGreenSoft,
                background = AppBackground,
                surface = CardBackground,
                surfaceVariant = AppBackground, // Ersetzt das Standard-Lila durch dein helles Grün
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = TextPrimary,
                onSurface = TextPrimary,
                onSurfaceVariant = TextSecondary,
                outline = BorderColor // Nutzt dein definiertes Grau-Grün fÃ¼r Umrandungen
            )
