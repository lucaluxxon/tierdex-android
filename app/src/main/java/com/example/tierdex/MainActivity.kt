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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.BufferedReader
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
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
import androidx.compose.material.icons.filled.Edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


private const val ANIMALS_JSON_FILE_NAME = "animals.json"
private const val ANIMALS_CSV_FILE_NAME = "tierlistegesamt.csv"
private const val FINDING_IMAGES_DIR = "finding_images"
private const val STARTUP_HINT_SHOWN_KEY_PREFIX = "startup_hint_shown_"
private const val INTRO_PENDING_KEY_PREFIX = "intro_pending_"
private const val INTRO_SEEN_KEY_PREFIX = "intro_seen_"
private const val WISHLIST_ANIMAL_KEY_PREFIX = "wishAnimalId_"
private const val FAVORITE_ANIMAL_KEY_PREFIX = "favoriteAnimalId_"
private const val LOCAL_PREFERENCES_OWNER_ID = "local"
private val AppGreenBackground = Color(0xFF51734A)

private fun favoriteAnimalKey(ownerId: String): String = "$FAVORITE_ANIMAL_KEY_PREFIX$ownerId"

private fun wishlistAnimalKey(ownerId: String): String = "$WISHLIST_ANIMAL_KEY_PREFIX$ownerId"

private fun introPendingKey(ownerId: String): String = "$INTRO_PENDING_KEY_PREFIX$ownerId"

private fun introSeenKey(ownerId: String): String = "$INTRO_SEEN_KEY_PREFIX$ownerId"

private fun currentAppDateText(): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

private fun formatCoordinates(
    latitude: Double,
    longitude: Double
): String = String.format(Locale.US, "%.6f, %.6f", latitude, longitude)

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
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationSource: String? = null,
    val ownerId: String? = null
)

data class CsvLoadResult(
    val animals: List<AnimalEntry>,
    val debugMessage: String
)


enum class AppTab {
    HOME,
    FRIENDS,
    STATS,
    PROFILE
}

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
    val findingsFromRoom = allFindings.map {
        AnimalFinding(
            roomId = it.id,
            animalId = it.animalId,
            date = it.date,
            location = it.location,
            note = it.note,
            photoUri = it.photoUri,
            latitude = it.latitude,
            longitude = it.longitude,
            locationSource = it.locationSource,
            ownerId = it.ownerId
        )
    }
    val context = LocalContext.current
    val prefs =
        context.getSharedPreferences("tierdex_prefs", android.content.Context.MODE_PRIVATE)
    val preferenceOwnerId = ownerId ?: LOCAL_PREFERENCES_OWNER_ID
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedAnimalId by rememberSaveable { mutableStateOf<String?>(null) }
    var storageDebug by rememberSaveable { mutableStateOf("Funde werden geladen...") }
    var showFoundOnly by rememberSaveable { mutableStateOf(false) }
    var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var authEntryMode by rememberSaveable { mutableStateOf<String?>(null) }
    var showAnimalPicker by rememberSaveable { mutableStateOf(false) }
    var selectedFindingToEdit by remember { mutableStateOf<AnimalFinding?>(null) }
    var showSettingsScreen by rememberSaveable { mutableStateOf(false) }
    var showStartupHintDialog by rememberSaveable { mutableStateOf(false) }
    var showIntroScreen by rememberSaveable { mutableStateOf(false) }
    var introLaunchSource by rememberSaveable { mutableStateOf(IntroLaunchSource.AUTOMATIC.name) }
    var selectedGroupFilter by rememberSaveable { mutableStateOf("Alle") }
    var selectedSubgroupFilter by rememberSaveable { mutableStateOf("Alle") }
    var showTierdexMapScreen by rememberSaveable { mutableStateOf(false) }
    var openCreateFindingMode by rememberSaveable { mutableStateOf(false) }
    val resetSearchState = {
        searchText = ""
    }
    var favoriteAnimalId by rememberSaveable { mutableStateOf<String?>(null) }

    var wishlistAnimalId by rememberSaveable { mutableStateOf<String?>(null) }
    var wishlistCelebrationMessage by rememberSaveable { mutableStateOf<CelebrationMessage?>(null) }
    var questLevelUpMessage by rememberSaveable { mutableStateOf<CelebrationMessage?>(null) }
    var previousOwnerId by rememberSaveable { mutableStateOf(ownerId) }
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
                showStartupHintDialog = false
            } else {
                val startupHintKey = "$STARTUP_HINT_SHOWN_KEY_PREFIX$ownerId"
                val alreadyShown = prefs.getBoolean(startupHintKey, false)
                if (!alreadyShown) {
                    showStartupHintDialog = true
                    prefs.edit().putBoolean(startupHintKey, true).apply()
                }
            }
        } else if (ownerId != null && hasPendingIntro) {
            showIntroScreen = true
            introLaunchSource = IntroLaunchSource.AUTOMATIC.name
            showStartupHintDialog = false
        }
        previousOwnerId = ownerId

        if (ownerId != null) {
            val migrationKey = "global_findings_migrated_to_$ownerId"
            val alreadyMigrated = prefs.getBoolean(migrationKey, false)
            if (!alreadyMigrated) {
                dao.assignGlobalFindingsToOwner(ownerId)
                prefs.edit().putBoolean(migrationKey, true).apply()
            }

            FirestoreFindingRepository.loadCurrentUserFindings(
                onResult = { cloudFindings ->
                    scope.launch {
                        val localRoomFindings = dao.getAllFindingsByOwnerOnce(ownerId)
                        val localFindings = localRoomFindings.map { entity ->
                            AnimalFinding(
                                roomId = entity.id,
                                animalId = entity.animalId,
                                date = entity.date,
                                location = entity.location,
                                note = entity.note,
                                photoUri = entity.photoUri,
                                latitude = entity.latitude,
                                longitude = entity.longitude,
                                locationSource = entity.locationSource,
                                ownerId = entity.ownerId
                            )
                        }

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
                                dao.insertFinding(
                                    AnimalFindingEntity(
                                        animalId = cloudFinding.animalId,
                                        date = cloudFinding.date,
                                        location = cloudFinding.location,
                                        note = cloudFinding.note,
                                        photoUri = cloudFinding.photoUri,
                                        latitude = cloudFinding.latitude,
                                        longitude = cloudFinding.longitude,
                                        locationSource = cloudFinding.locationSource,
                                        ownerId = ownerId
                                    )
                                )
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
                    }
                },
                onError = { error ->
                    Log.e("CloudSync", "Cloud load failed: ${error ?: "Unbekannter Fehler"}")
                }
            )
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


    val animalLoadResult: CsvLoadResult = remember(context) {
        loadAnimalsFromJsonWithDebug(
            context = context,
            jsonFileName = ANIMALS_JSON_FILE_NAME,
            csvFallbackFileName = ANIMALS_CSV_FILE_NAME
        )
    }

    val animals: List<AnimalEntry> = animalLoadResult.animals

    val findingCountByAnimalId = allFindings
        .groupingBy { it.animalId }
        .eachCount()

    val collectedAnimalIds = findingCountByAnimalId.keys
    val collectedAnimalCount = animals.count { it.id in collectedAnimalIds }

    var selectedSortOption by rememberSaveable { mutableStateOf("A_Z") }

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

    val selectedAnimal = animals.find { it.id == selectedAnimalId }
    val showAuthStartScreen = ownerId == null && authEntryMode == null
    val showAuthEntryScreen = ownerId == null && authEntryMode != null
    val isIntroFromSettings = introLaunchSource == IntroLaunchSource.SETTINGS.name

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
    BackHandler(enabled = selectedAnimalId != null) {
        resetSearchState()
        selectedAnimalId = null
        selectedFindingToEdit = null
        openCreateFindingMode = false
    }

    BackHandler(enabled = showAnimalPicker && selectedAnimalId == null) {
        resetSearchState()
        showAnimalPicker = false
    }
    BackHandler(enabled = currentTab == AppTab.STATS && showTierdexMapScreen) {
        showTierdexMapScreen = false
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

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TierdexTopBar(
                onSettingsClick = {
                    resetSearchState()
                    showSettingsScreen = true
                }
            )
        },
        bottomBar = {
            if (selectedAnimal == null && !showAnimalPicker && !showAuthStartScreen && !showAuthEntryScreen && !showIntroScreen && !showSettingsScreen) {
                MainBottomBar(
                    currentTab = currentTab,
                    onTabSelected = {
                        resetSearchState()
                        currentTab = it
                    }
                )
            }
        },
        floatingActionButton = {
            if (selectedAnimal == null && !showAnimalPicker && !showAuthStartScreen && !showAuthEntryScreen && !showIntroScreen && !showSettingsScreen) {
                FloatingActionButton(
                    onClick = {
                        resetSearchState()
                        selectedFindingToEdit = null
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
                                showStartupHintDialog = false
                            }
                        }
                    )
                }

                showIntroScreen -> {
                    IntroScreen(
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
                            authEntryMode = null
                            showSettingsScreen = false
                            currentTab = AppTab.PROFILE
                        },
                        onShowIntro = {
                            introLaunchSource = IntroLaunchSource.SETTINGS.name
                            showIntroScreen = true
                            showStartupHintDialog = false
                        },
                        allFindings = findingsFromRoom,
                        onImportFindings = { importedFindings ->
                            scope.launch {
                                importedFindings.forEach { finding ->
                                    val importedFindingForCurrentOwner = finding.copy(
                                        ownerId = currentOwnerId ?: finding.ownerId
                                    )
                                    dao.insertFinding(
                                        AnimalFindingEntity(
                                            animalId = importedFindingForCurrentOwner.animalId,
                                            date = importedFindingForCurrentOwner.date,
                                            location = importedFindingForCurrentOwner.location,
                                            note = importedFindingForCurrentOwner.note,
                                            photoUri = importedFindingForCurrentOwner.photoUri,
                                            latitude = importedFindingForCurrentOwner.latitude,
                                            longitude = importedFindingForCurrentOwner.longitude,
                                            locationSource = importedFindingForCurrentOwner.locationSource,
                                            ownerId = importedFindingForCurrentOwner.ownerId
                                        )
                                    )

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

                selectedAnimal != null -> {
                    AnimalDetailScreen(
                        modifier = Modifier.padding(innerPadding),
                        animal = selectedAnimal,
                        findings = findingsFromRoom.filter { it.animalId == selectedAnimal.id },
                        storageDebug = storageDebug,
                        initialFinding = selectedFindingToEdit,
                        startInCreateMode = openCreateFindingMode && selectedFindingToEdit == null,
                        onBackClick = {
                            resetSearchState()
                            selectedAnimalId = null
                            selectedFindingToEdit = null
                            openCreateFindingMode = false
                        },
                        onSaveFinding = { finding ->
                            scope.launch {
                                val previousFindings = findingsFromRoom
                                val questCelebration = detectQuestLevelUpMessage(
                                    previousFindings = previousFindings,
                                    newFinding = finding,
                                    animals = animals
                                )

                                dao.insertFinding(
                                    AnimalFindingEntity(
                                        animalId = finding.animalId,
                                        date = finding.date,
                                        location = finding.location,
                                        note = finding.note,
                                        photoUri = finding.photoUri,
                                        latitude = finding.latitude,
                                        longitude = finding.longitude,
                                        locationSource = finding.locationSource,
                                        ownerId = currentOwnerId
                                    )
                                )

                                if (wishlistAnimalId == finding.animalId) {
                                    wishlistAnimalId = null
                                    prefs.edit().remove(wishlistAnimalKey(preferenceOwnerId)).apply()
                                    wishlistCelebrationMessage = CelebrationMessage(
                                        title = "Wunsch-Fund entdeckt!",
                                        subtitle = "Dein Wunsch-Tier ist jetzt gefunden."
                                    )
                                }

                                questLevelUpMessage = questCelebration

                                if (currentOwnerId != null) {
                                    FirestoreFindingRepository.saveCurrentUserFinding(finding) { success, result ->
                                        if (!success) {
                                            Log.e(
                                                "CloudWrite",
                                                "Firestore save on create failed: $result"
                                            )
                                        }
                                    }
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

                                    if (currentOwnerId != null) {
                                        FirestoreFindingRepository.deleteCurrentUserFinding(finding) { success, result ->
                                            if (success) {
                                                Log.d(
                                                    "CloudSyncDelete",
                                                    "Deleted Firestore finding: documentId=$result"
                                                )
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
                            selectedFindingToEdit = newFinding

                            scope.launch {
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
                                    dao.updateFinding(
                                        AnimalFindingEntity(
                                            id = roomMatch.id,
                                            animalId = newFinding.animalId,
                                            date = newFinding.date,
                                            location = newFinding.location,
                                            note = newFinding.note,
                                            photoUri = newFinding.photoUri,
                                            latitude = newFinding.latitude,
                                            longitude = newFinding.longitude,
                                            locationSource = newFinding.locationSource,
                                            ownerId = currentOwnerId
                                        )
                                    )

                                    if (currentOwnerId != null) {
                                        FirestoreFindingRepository.updateCurrentUserFinding(
                                            oldFinding,
                                            newFinding
                                        ) { success, result ->
                                            if (!success) {
                                                Log.e(
                                                    "CloudWrite",
                                                    "Firestore update on edit failed: $result"
                                                )
                                            }
                                        }
                                    }
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
                        onAnimalClick = { animal ->
                            resetSearchState()
                            selectedAnimalId = animal.id
                            showAnimalPicker = false
                            selectedFindingToEdit = null
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
                        favoriteAnimalId = favoriteAnimalId,
                        wishlistAnimalId = wishlistAnimalId,
                        roomFindingsCount = allFindings.size,
                        onEditFinding = { finding ->
                            selectedFindingToEdit = finding
                            selectedAnimalId = finding.animalId
                            openCreateFindingMode = false
                        },
                        extraTopPadding = innerPadding.calculateTopPadding(),
                        extraBottomPadding = innerPadding.calculateBottomPadding()
                    )
                }

                currentTab == AppTab.FRIENDS -> {
                    FriendsScreen(
                        extraTopPadding = innerPadding.calculateTopPadding(),
                        extraBottomPadding = innerPadding.calculateBottomPadding()
                    )
                }

                currentTab == AppTab.STATS -> {
                    if (showTierdexMapScreen) {
                        TierdexMapScreen(
                            findings = findingsFromRoom,
                            onBack = { showTierdexMapScreen = false },
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
                            onAnimalClick = { animal ->
                                resetSearchState()
                                selectedAnimalId = animal.id
                                selectedFindingToEdit = null
                                openCreateFindingMode = false
                            },
                            onOpenMap = { showTierdexMapScreen = true },
                            extraTopPadding = innerPadding.calculateTopPadding(),
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }
                }

                currentTab == AppTab.PROFILE -> {
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
                        onEditFinding = { finding ->
                            selectedFindingToEdit = finding
                            selectedAnimalId = finding.animalId
                            openCreateFindingMode = false
                        },
                        favoriteAnimalId = favoriteAnimalId,
                        wishlistAnimalId = wishlistAnimalId,
                        extraTopPadding = innerPadding.calculateTopPadding(),
                        extraBottomPadding = innerPadding.calculateBottomPadding()
                    )
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
            if (showStartupHintDialog && !showIntroScreen) {
                StartupHintDialog(
                    onDismiss = { showStartupHintDialog = false }
                )
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

            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.CenterEnd
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (quest.isCompleted) PrimaryGreenSoft.copy(alpha = 0.45f) else CardBackground,
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
            LinearProgressIndicator(
                progress = { quest.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = PrimaryGreen,
                trackColor = PrimaryGreenSoft.copy(alpha = 0.45f)
            )
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
    favoriteAnimalId: String?,
    wishlistAnimalId: String?,
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
    val quests =
        remember(findings, animals, collectedAnimalCount, totalFindings, photoFindingCount) {
            buildHomeQuests(
                findings = findings,
                animals = animals,
                collectedAnimalCount = collectedAnimalCount,
                totalFindings = totalFindings,
                photoFindingCount = photoFindingCount
            )
        }
    val nextQuest = quests.firstOrNull { !it.isCompleted } ?: quests.firstOrNull()

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
                    containerColor = PrimaryGreenSoft.copy(alpha = 0.95f),
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
                    Text(
                        text = "Dein Fortschritt wächst weiter. Hier siehst du deine Sammlung, dein nächstes Ziel und die wichtigsten Zahlen auf einen Blick.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground,
                    contentColor = TextPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeSectionTitle(
                        title = "Nächstes Ziel"
                    )

                    if (nextQuest == null) {
                        Text(
                            text = "Noch keine Questdaten verfügbar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nextQuest.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = nextQuest.percentLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = PrimaryGreen
                            )
                        }
                        Text(
                            text = nextQuest.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "${nextQuest.shownProgress} von ${nextQuest.goal} Funden",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        LinearProgressIndicator(
                            progress = { nextQuest.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = PrimaryGreen,
                            trackColor = PrimaryGreenSoft.copy(alpha = 0.45f)
                        )
                        Text(
                            text = nextQuest.encouragementLabel,
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

                        latestFinding.date.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = "Datum: $it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        latestFinding.location.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = "Fundort: $it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        latestFinding.note.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = "Notiz: $it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        if (latestFinding.photoUri.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            UriImage(
                                uriString = latestFinding.photoUri,
                                maxImageSizePx = 1024,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
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

    val firstPosition = findingsWithCoordinates.first().second
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(firstPosition, 12f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        findingsWithCoordinates.forEach { (_, latLng) ->
            Marker(
                state = MarkerState(position = latLng),
                title = "Fund"
            )
        }
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
            Marker(
                state = MarkerState(position = latLng),
                title = "Ausgewählter Standort"
            )
        }
    }
}

@Composable
fun TierdexMapScreen(
    findings: List<AnimalFinding>,
    onBack: () -> Unit,
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
            OutlinedButton(
                onClick = onBack,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Text("Zurück zu Mein Tierdex")
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
    var selectedSettingsPage by rememberSaveable { mutableStateOf("menu") }
    val pageTitle = when (selectedSettingsPage) {
        "menu" -> "Einstellungen"
        "rules" -> "Regeln"
        "display" -> "Darstellung"
        "features" -> "App-Funktionen"
        "info" -> "Info"
        else -> "Einstellungen"
    }
    val pageSubtitle = when (selectedSettingsPage) {
        "menu" -> "Einführung, Hinweise und wichtige App-Bereiche an einem Ort."
        "rules" -> "Kurz und klar zusammengefasst, was im Tierdex als Fund zählt."
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
                        title = "Einführung",
                        description = "Die kurze Einführung zur App erneut ansehen",
                        onClick = onShowIntro
                    )
                }

                item {
                    SettingsMenuCard(
                        title = "Regelliste",
                        description = "Regeln für den Tierdex ansehen",
                        onClick = { selectedSettingsPage = "rules" }
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

            "rules" -> {
                item {
                    SettingsContentCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Was im Tierdex gilt",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Damit Funde fair und nachvollziehbar bleiben, gelten diese einfachen Regeln:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "• Keine Haustiere oder Nutztiere",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "• Keine Tiere, die in Gefangenschaft leben",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "• Es dürfen nur eigene Fotos eingereicht werden",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "• Bitte keine Fotos von toten oder verletzten Tieren",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
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
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Tierdex Testversion",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
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
fun FriendsScreen(
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
            Text(
                text = "Freunde",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Dieser Bereich ist noch in Arbeit.")
        }
    }
}

@Composable
private fun IntroScreen(
    onClose: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Willkommen bei Tierdex",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        item {
            Text(
                text = "Hier bekommst du einen kurzen Überblick, damit du direkt gut starten kannst.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        item {
            SettingsContentCard {
                Text(
                    text = "Was die App kann",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Tierdex hilft dir dabei, Tierfunde festzuhalten, deine Sammlung aufzubauen und deinen Fortschritt in deiner persönlichen Übersicht zu sehen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            SettingsContentCard {
                Text(
                    text = "So benutzt du sie",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Lege über \"Neuer Fund\" einen Eintrag an, wähle ein Tier aus und ergänze Datum, Ort, Notiz oder Foto. Deine Funde findest du später in deinem Profil und in deinem Tierdex wieder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            SettingsContentCard {
                Text(
                    text = "Wofür sie gedacht ist",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Die App ist für eigene Naturbeobachtungen gedacht und soll dir helfen, besondere Begegnungen mit wild lebenden Tieren übersichtlich zu sammeln.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                )
            ) {
                Text("Verstanden")
            }
        }
    }
}

@Composable
private fun StartupHintDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Verstanden", color = PrimaryGreen)
            }
        },
        title = {
            Text(
                text = "Kurz vor dem Start",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Bitte beachte kurz diese Regeln:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                StartupHintRulesContent()
            }
        },
        containerColor = Color.White
    )
}

@Composable
private fun StartupHintRulesContent() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "- Keine Haustiere",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            "- Keine Nutztiere",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            "- Keine in Gefangenschaft lebenden Tiere",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            "- Nur eigene Fotos duerfen eingereicht werden",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            "- Bitte keine Fotos von toten oder verletzten Tieren",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
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
                                AuthSession.registerWithEmail(
                                    displayName,
                                    email,
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
                                AuthSession.loginWithEmail(email, password) { success, result ->
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
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    val animalById = remember(animals) { animals.associateBy { it.id } }
    val favoriteAnimal = animalById[favoriteAnimalId]
    val wishlistAnimal = animalById[wishlistAnimalId]
    val reversedFindings = remember(findings) { findings.asReversed() }
    var displayNameInput by rememberSaveable(currentDisplayName) {
        mutableStateOf(currentDisplayName ?: "")
    }
    var authMessage by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
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
                text = currentDisplayName?.takeIf { it.isNotBlank() }?.let { "Profil von $it" }
                    ?: "Profil",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
            )
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
                                AuthSession.updateCurrentDisplayName(displayNameInput) { success, result ->
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Deine Übersicht",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                    Text(
                        "Gesammelte Arten: $collectedAnimalCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        "Gesammelte Funde: $totalFindings",
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Lieblingstier",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    if (favoriteAnimal == null) {
                        Text(
                            text = "Noch kein Lieblingstier gewählt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    } else {
                        Text(
                            text = favoriteAnimal.germanName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = favoriteAnimal.latinName,
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
                        text = "Wunsch-Fund",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    if (wishlistAnimal == null) {
                        Text(
                            text = "Nicht gewählt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    } else {
                        Text(
                            text = wishlistAnimal.germanName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = wishlistAnimal.latinName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Gesamtsammlung",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        if (findings.isEmpty()) {
            item {
                Text("Noch keine Funde gespeichert.")
            }
        } else {
            items(
                items = reversedFindings,
                key = { finding ->
                    finding.roomId ?: FirestoreFindingRepository.findingFingerprint(finding)
                }
            ) { finding ->
                val animal = animalById[finding.animalId]

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
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = animal?.germanName ?: "Unbekanntes Tier",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (finding.date.isNotBlank()) {
                            Text(
                                text = "Datum: ${finding.date}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        if (finding.location.isNotBlank()) {
                            Text(
                                text = "Fundort: ${finding.location}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        if (finding.note.isNotBlank()) {
                            Text(
                                text = "Notiz: ${finding.note}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (finding.photoUri.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            UriImage(
                                uriString = finding.photoUri,
                                maxImageSizePx = 1024,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                                    .clip(RoundedCornerShape(12.dp))
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
fun AnimalDetailScreen(
    modifier: Modifier = Modifier,
    animal: AnimalEntry,
    findings: List<AnimalFinding>,
    storageDebug: String,
    initialFinding: AnimalFinding?,
    startInCreateMode: Boolean = false,
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
    var selectedPhotoUri by rememberSaveable(initial?.roomId) {
        mutableStateOf(initial?.photoUri ?: "")
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
    var showLocationPicker by rememberSaveable(initial?.roomId) { mutableStateOf(false) }
    var pendingLatitude by remember { mutableStateOf<Double?>(null) }
    var pendingLongitude by remember { mutableStateOf<Double?>(null) }
    var cropPhotoUri by remember { mutableStateOf<String?>(null) }
    val isWishlistSelected = animal.id == currentWishlistAnimalId
    val isFavoriteSelected = animal.id == currentFavoriteAnimalId
    var editingFinding by remember(initial?.roomId) { mutableStateOf(initial) }
    var isEditMode by rememberSaveable(initial?.roomId, startInCreateMode) {
        mutableStateOf(initial == null && startInCreateMode)
    }
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

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            persistReadPermission(context, uri)
            selectedPhotoUri = uri.toString()
        }
    }

    val currentFinding = editingFinding ?: initial
    val openFindingDetails: (AnimalFinding) -> Unit = { finding ->
        editingFinding = finding
        date = finding.date
        location = finding.location
        note = finding.note
        latitude = finding.latitude
        longitude = finding.longitude
        locationSource = finding.locationSource
        locationStatusMessage = ""
        selectedPhotoUri = finding.photoUri
        cropPhotoUri = null
        isEditMode = false
    }
    val editablePhotoUri = selectedPhotoUri.takeIf { it.isNotBlank() } ?: currentFinding?.photoUri
    val hasTextualFindingDetails =
        !currentFinding?.date.isNullOrBlank() ||
            !currentFinding?.location.isNullOrBlank() ||
            !currentFinding?.note.isNullOrBlank()
    val hasFindingPhoto = !editablePhotoUri.isNullOrBlank()

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

        if (!isEditMode && detailFindings.isNotEmpty()) {
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
                                onClick = { openFindingDetails(finding) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.82f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = finding.date.ifBlank { "Fund ohne Datum" },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    finding.location.takeIf { it.isNotBlank() }?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                    finding.note.takeIf { it.isNotBlank() }?.let {
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

        if (hasTextualFindingDetails || hasFindingPhoto) {
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
                            text = "Fundinfos",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        if (hasTextualFindingDetails) {
                            currentFinding?.date?.takeIf { it.isNotBlank() }?.let {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Datum",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary
                                    )
                                }
                            }

                            currentFinding?.location?.takeIf { it.isNotBlank() }?.let {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Fundort",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary
                                    )
                                }
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
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Notiz",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        if (hasFindingPhoto) {
                            if (hasTextualFindingDetails) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            editablePhotoUri?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = "Foto",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                ) {
                                    UriImage(
                                        uriString = it,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp),
                                        maxImageSizePx = 1280
                                    )

                                    if (editingFinding != null && isEditMode) {
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
                                                    cropPhotoUri = it
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

                                if (isEditMode) {
                                    OutlinedButton(
                                        onClick = {
                                            pickMedia.launch(
                                                PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Anderes Foto auswählen")
                                    }
                                }
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
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = CardBackground.copy(alpha = 0.72f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
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
                        Text(
                            text = "Später siehst du hier, welche Freunde dieses Tier bereits gefunden haben.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
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
                        latitude = currentFinding?.latitude
                        longitude = currentFinding?.longitude
                        locationSource = currentFinding?.locationSource
                        locationStatusMessage = ""
                        selectedPhotoUri = currentFinding?.photoUri.orEmpty()
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
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

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

                        if (currentFinding?.photoUri.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = {
                                    pickMedia.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Text(
                                    if (selectedPhotoUri.isBlank()) "Foto auswählen"
                                    else "Anderes Foto auswählen"
                                )
                            }
                        }

                        if (selectedPhotoUri.isNotBlank() && selectedPhotoUri != currentFinding?.photoUri && currentFinding == null) {
                            Text(
                                text = "Foto",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary
                            )
                            UriImage(
                                uriString = selectedPhotoUri,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                maxImageSizePx = 1280
                            )
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
                                        selectedPhotoUri.isNotBlank()
                                    ) {
                                        val storedPhotoUri = persistPhotoForFinding(
                                            context,
                                            selectedPhotoUri
                                        )

                                        val newFinding = AnimalFinding(
                                            roomId = editingFinding?.roomId,
                                            animalId = animal.id,
                                            date = date.trim(),
                                            location = location.trim(),
                                            note = note.trim(),
                                            photoUri = storedPhotoUri,
                                            latitude = latitude,
                                            longitude = longitude,
                                            locationSource = locationSource,
                                            ownerId = editingFinding?.ownerId
                                        )

                                        if (editingFinding == null) {
                                            onSaveFinding(newFinding)
                                            onBackClick()

                                            date = ""
                                            location = ""
                                            note = ""
                                            selectedPhotoUri = ""
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
                                            selectedPhotoUri = storedPhotoUri
                                            isEditMode = false
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
                                        latitude = currentFinding?.latitude
                                        longitude = currentFinding?.longitude
                                        locationSource = currentFinding?.locationSource
                                        locationStatusMessage = ""
                                        selectedPhotoUri =
                                            currentFinding?.photoUri.orEmpty()
                                        cropPhotoUri = null
                                        isEditMode = false
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
                            onBackClick()
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

    cropPhotoUri?.let { photoUriToCrop ->
        CropPhotoDialog(
            uriString = photoUriToCrop,
            onDismiss = { cropPhotoUri = null },
            onCropComplete = { croppedPhotoUri ->
                selectedPhotoUri = croppedPhotoUri
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
                            Column {
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
                        }
                    }
                    if (!isPickerMode && onOpenMap != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onOpenMap),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = CardBackground,
                                    contentColor = TextPrimary
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Fundorte anzeigen",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Alle gespeicherten Fundorte auf einer Karte",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
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
                            AnimalListItem(
                                animal = animal,
                                findingCount = findingCount,
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
            fun AnimalListItem(animal: AnimalEntry, findingCount: Int, onClick: () -> Unit) {

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
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )

                        Column(
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
                                text = if (findingCount > 0) "âœ“ Gefunden ($findingCount)" else "Nicht gefunden",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (findingCount > 0) PrimaryGreen else TextSecondary
                            )
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
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Schließen",
                                    tint = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val safeBitmap = loadedBitmap ?: return@Button
                                val croppedBitmap = cropBitmapToCenterFrame(
                                    bitmap = safeBitmap,
                                    containerSize = containerSize,
                                    baseImageWidth = baseImageSize.first,
                                    baseImageHeight = baseImageSize.second,
                                    cropSizePx = cropSizePx,
                                    scale = scale,
                                    offset = offset
                                ) ?: return@Button

                                val croppedUri = saveBitmapForFinding(context, croppedBitmap)
                                if (croppedUri.isNotBlank()) {
                                    onCropComplete(croppedUri)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .safeDrawingPadding()
                                .padding(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen
                            ),
                            enabled = loadedBitmap != null && containerSize != IntSize.Zero
                        ) {
                            Text("Zuschneiden")
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

                if (bitmap == null && loadFinished && uriString.startsWith("internal://")) {
                    Box(
                        modifier = modifier.background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Foto auf diesem Gerät nicht verfügbar",
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
