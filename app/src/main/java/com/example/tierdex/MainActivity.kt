package com.example.tierdex

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
import java.util.UUID
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
import kotlinx.coroutines.Dispatchers
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.MenuDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.WaterDrop


private const val ANIMALS_FILE_NAME = "tierlistegesamt.csv"
private const val FINDING_IMAGES_DIR = "finding_images"
private val AppGreenBackground = Color(0xFF51734A)


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
            ).build()
        }

        private var wishAnimalId by mutableStateOf<String?>(null)
        private var favoriteAnimalId by mutableStateOf<String?>(null)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            wishAnimalId = prefs.getString("wishAnimalId", null)
            favoriteAnimalId = prefs.getString("favoriteAnimalId", null)

            setContent {
                TierdexTheme(
                    darkTheme = false,
                    dynamicColor = false
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White
                    ) {
                        TierdexApp(database = database)
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
        val rarity: String
    )

    data class AnimalFinding(
        val animalId: String,
        val date: String,
        val location: String,
        val note: String,
        val photoUri: String = ""
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TierdexApp(database: AnimalFindingDatabase) {
        val dao = database.animalFindingDao()
        val scope = rememberCoroutineScope()
        val allFindings by dao.getAllFindings().collectAsState(initial = emptyList())
        val findingsFromRoom = allFindings.map {
            AnimalFinding(
                animalId = it.animalId,
                date = it.date,
                location = it.location,
                note = it.note,
                photoUri = it.photoUri
            )
        }
        val context = LocalContext.current
        val prefs =
            context.getSharedPreferences("tierdex_prefs", android.content.Context.MODE_PRIVATE)
        var searchText by rememberSaveable { mutableStateOf("") }
        var selectedAnimalId by rememberSaveable { mutableStateOf<String?>(null) }
        var storageDebug by rememberSaveable { mutableStateOf("Funde werden geladen...") }
        var showFoundOnly by rememberSaveable { mutableStateOf(false) }
        var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
        var showAnimalPicker by rememberSaveable { mutableStateOf(false) }
        var selectedFindingToEdit by remember { mutableStateOf<AnimalFinding?>(null) }
        var showSettingsScreen by rememberSaveable { mutableStateOf(false) }
        var selectedGroupFilter by rememberSaveable { mutableStateOf("Alle") }
        var selectedSubgroupFilter by rememberSaveable { mutableStateOf("Alle") }
        var favoriteAnimalId by rememberSaveable {
            mutableStateOf<String?>(prefs.getString("favoriteAnimalId", null))
        }

        var wishlistAnimalId by rememberSaveable {
            mutableStateOf<String?>(prefs.getString("wishAnimalId", null))
        }
        LaunchedEffect(Unit) {
        }


        val animalLoadResult = remember(context) {
            loadAnimalsFromCsvWithDebug(context, ANIMALS_FILE_NAME)
        }

        val animals = animalLoadResult.animals

        val findingCountByAnimalId = allFindings
            .groupingBy { it.animalId }
            .eachCount()

        val collectedAnimalIds = findingCountByAnimalId.keys
        val collectedAnimalCount = animals.count { it.id in collectedAnimalIds }

        val baseFilteredAnimals = animals.filter { animal ->
            val query = searchText.trim().lowercase()
            query.isEmpty() ||
                    animal.germanName.lowercase().contains(query) ||
                    animal.latinName.lowercase().contains(query) ||
                    animal.group.lowercase().contains(query)
        }
        var selectedSortOption by rememberSaveable { mutableStateOf("A_Z") }

        val filteredAnimals = animals.filter { animal ->
            val normalizedSearch = normalizeSearchText(searchText)

            val matchesSearch =
                normalizedSearch.isBlank() ||
                        normalizeSearchText(animal.germanName).contains(normalizedSearch) ||
                        normalizeSearchText(animal.latinName).contains(normalizedSearch) ||
                        normalizeSearchText(animal.group).contains(normalizedSearch) ||
                        normalizeSearchText(animal.subgroup).contains(normalizedSearch)

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

        val groupOptions = listOf("Alle") + animals.map { it.group }.distinct().sorted()

        val subgroupOptions =
            if (selectedGroupFilter == "Alle") {
                listOf("Alle") + animals.map { it.subgroup }.distinct().sorted()
            } else {
                listOf("Alle") + animals
                    .filter { it.group == selectedGroupFilter }
                    .map { it.subgroup }
                    .distinct()
                    .sorted()
            }

        BackHandler(enabled = showSettingsScreen) {
            showSettingsScreen = false
        }
        BackHandler(enabled = selectedAnimalId != null) {
            selectedAnimalId = null
        }

        BackHandler(enabled = showAnimalPicker && selectedAnimalId == null) {
            showAnimalPicker = false
        }

        Scaffold(
            containerColor = Color.White,
            topBar = {},
            bottomBar = {
                if (selectedAnimal == null && !showAnimalPicker) {
                    MainBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )
                }
            },
            floatingActionButton = {
                if (selectedAnimal == null && !showAnimalPicker) {
                    FloatingActionButton(
                        onClick = { showAnimalPicker = true },
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
                    showSettingsScreen -> {
                        SettingsScreen(
                            onBack = { showSettingsScreen = false },
                            allFindings = findingsFromRoom,
                            onImportFindings = { importedFindings ->
                                scope.launch {
                                    importedFindings.forEach { finding ->
                                        dao.insertFinding(
                                            AnimalFindingEntity(
                                                animalId = finding.animalId,
                                                date = finding.date,
                                                location = finding.location,
                                                note = finding.note,
                                                photoUri = finding.photoUri
                                            )
                                        )
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
                            onBackClick = {
                                selectedAnimalId = null
                                selectedFindingToEdit = null
                            },
                            onSaveFinding = { finding ->
                                scope.launch {
                                    dao.insertFinding(
                                        AnimalFindingEntity(
                                            animalId = finding.animalId,
                                            date = finding.date,
                                            location = finding.location,
                                            note = finding.note,
                                            photoUri = finding.photoUri
                                        )
                                    )
                                }
                            },
                            onDeleteFinding = { finding ->
                                scope.launch {
                                    val roomMatch = allFindings.lastOrNull {
                                        it.animalId == finding.animalId &&
                                                it.date == finding.date &&
                                                it.location == finding.location &&
                                                it.note == finding.note &&
                                                it.photoUri == finding.photoUri
                                    }

                                    if (roomMatch != null) {
                                        dao.deleteFinding(roomMatch)
                                    }
                                }
                            },
                            onUpdateFinding = { oldFinding, newFinding ->
                                selectedFindingToEdit = null

                                scope.launch {
                                    val roomMatch = allFindings.lastOrNull {
                                        it.animalId == oldFinding.animalId &&
                                                it.date == oldFinding.date &&
                                                it.location == oldFinding.location &&
                                                it.note == oldFinding.note &&
                                                it.photoUri == oldFinding.photoUri
                                    }

                                    if (roomMatch != null) {
                                        dao.updateFinding(
                                            AnimalFindingEntity(
                                                id = roomMatch.id,
                                                animalId = newFinding.animalId,
                                                date = newFinding.date,
                                                location = newFinding.location,
                                                note = newFinding.note,
                                                photoUri = newFinding.photoUri
                                            )
                                        )
                                    }
                                }
                            },
                            onSetFavoriteFindingAnimal = { animal ->
                                favoriteAnimalId = animal.id
                                prefs.edit().putString("favoriteAnimalId", animal.id).apply()
                            },
                            onSetWishlistAnimal = { animal ->
                                wishlistAnimalId =
                                    if (wishlistAnimalId == animal.id) null else animal.id
                                prefs.edit().putString("wishAnimalId", wishlistAnimalId).apply()
                            },
                            currentWishlistAnimalId = wishlistAnimalId,
                        )
                    }


                    showAnimalPicker -> {
                        AnimalListScreen(
                            onOpenSettings = { showSettingsScreen = true },
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
                                selectedAnimalId = animal.id
                                showAnimalPicker = false
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
                            roomFindingsCount = allFindings.size,
                            onEditFinding = { finding ->
                                selectedFindingToEdit = finding
                                selectedAnimalId = finding.animalId
                            },
                            extraTopPadding = 0.dp,
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }

                    currentTab == AppTab.FRIENDS -> {
                        FriendsScreen(
                            extraTopPadding = 0.dp,
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }

                    currentTab == AppTab.STATS -> {
                        AnimalListScreen(
                            onOpenSettings = { showSettingsScreen = true },
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
                                selectedAnimalId = animal.id
                            },
                            extraTopPadding = innerPadding.calculateTopPadding(),
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }

                    currentTab == AppTab.PROFILE -> {
                        ProfileScreen(
                            collectedAnimalCount = collectedAnimalCount,
                            totalFindings = allFindings.size,
                            findings = findingsFromRoom,
                            animals = animals,
                            onEditFinding = { finding ->
                                selectedFindingToEdit = finding
                                selectedAnimalId = finding.animalId
                            },
                            favoriteAnimalId = favoriteAnimalId,
                            wishlistAnimalId = wishlistAnimalId,
                            extraTopPadding = 0.dp,
                            extraBottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }
                }
                if (
                    selectedAnimalId == null &&
                    !showAnimalPicker &&
                    !showSettingsScreen
                ) {
                    IconButton(
                        onClick = { showSettingsScreen = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .offset(y = 2.dp)
                            .padding(top = 8.dp, end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Einstellungen"
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
    fun HomeScreen(
        collectedAnimalCount: Int,
        totalAnimalCount: Int,
        totalFindings: Int,
        findings: List<AnimalFinding>,
        animals: List<AnimalEntry>,
        onEditFinding: (AnimalFinding) -> Unit,
        roomFindingsCount: Int,
        extraTopPadding: Dp = 0.dp,
        extraBottomPadding: Dp = 0.dp
    ) {
        val latestFinding = findings.firstOrNull()
        val latestAnimal = animals.find {
            it.id.trim().lowercase() == latestFinding?.animalId?.trim()?.lowercase()
        }

        val collectedAnimalIds = findings.map { it.animalId }.toSet()

        val collectedBirdSpeciesCount = animals
            .filter { it.group.equals("Vogel", ignoreCase = true) && it.id in collectedAnimalIds }
            .size

        val photoFindingCount = findings.count { it.photoUri.isNotBlank() }

        val birdQuestGoal = getNextQuestGoal(
            progress = collectedBirdSpeciesCount,
            goals = listOf(3, 10, 25)
        )

        val photoQuestGoal = getNextQuestGoal(
            progress = photoFindingCount,
            goals = listOf(5, 15, 30)
        )

        val speciesQuestGoal = getNextQuestGoal(
            progress = collectedAnimalCount,
            goals = listOf(10, 25, 50)
        )
        val collectionPercent = if (totalAnimalCount > 0) {
            (collectedAnimalCount.toFloat() / totalAnimalCount.toFloat()) * 100f
        } else {
            0f
        }

        val collectionProgress = if (totalAnimalCount > 0) {
            collectedAnimalCount.toFloat() / totalAnimalCount.toFloat()
        } else {
            0f
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
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
                    text = "Startseite",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Fortschritt",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Gesammelt: $collectedAnimalCount von $totalAnimalCount Arten",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(
                                    width = 1.dp,
                                    color = TextSecondary.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(collectionProgress)
                                    .background(PrimaryGreen)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = String.format("%.1f %%", collectionPercent),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Gesamte Funde: $totalFindings",
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                ){
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Letzter Fund",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (latestFinding == null) {
                            Text("Noch kein Fund gespeichert.")
                        } else {
                            if (latestFinding.date.isNotBlank()) {
                                Text(
                                    text = latestAnimal?.germanName ?: "Unbekanntes Tier",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Datum: ${latestFinding.date}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                if (latestFinding.location.isNotBlank()) {
                                    Text(
                                        text = "Fundort: ${latestFinding.location}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (latestFinding.note.isNotBlank()) {
                                Text(
                                    text = "Notiz: ${latestFinding.note}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            if (latestFinding.photoUri.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                UriImage(
                                    uriString = latestFinding.photoUri,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onEditFinding(latestFinding) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryGreen
                                )
                            ) {
                                Text(
                                    "Bearbeiten",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Quests",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            item {
                QuestCard(
                    title = "Vogelarten sammeln",
                    progress = collectedBirdSpeciesCount,
                    goal = birdQuestGoal,
                    color = TextPrimary
                )
            }

            item {
                QuestCard(
                    title = "Funde mit Foto",
                    progress = photoFindingCount,
                    goal = photoQuestGoal,
                    color = TextPrimary
                )
            }

            item {
                QuestCard(
                    title = "Arten sammeln",
                    progress = collectedAnimalCount,
                    goal = speciesQuestGoal,
                    color = TextPrimary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
fun SettingsScreen(
    onBack: () -> Unit,
    allFindings: List<AnimalFinding>,
    onImportFindings: (List<AnimalFinding>) -> Unit,
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    var selectedSettingsPage by rememberSaveable { mutableStateOf("menu") }
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
            .statusBarsPadding()
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
            Button(
                onClick = {
                    if (selectedSettingsPage == "menu") {
                        onBack()
                    } else {
                        selectedSettingsPage = "menu"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                )
            ) {
                Text("Zurück")
            }
        }

        item {
            Text(
                text = when (selectedSettingsPage) {
                    "menu" -> "Einstellungen"
                    "rules" -> "Regeln"
                    "display" -> "Darstellung"
                    "features" -> "App-Funktionen"
                    "info" -> "Info"
                    else -> "Einstellungen"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        when (selectedSettingsPage) {
            "menu" -> {
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
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Keine Haustiere", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("• Keine Nutztiere", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("• Keine in Gefangenschaft lebenden Tiere", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("• Nur eigene Fotos dürfen eingereicht werden", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("• Bitte keine Fotos von toten oder verletzten Tieren", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }
            }

            "display" -> {
                item {
                    SettingsContentCard {
                        Text(
                            text = "Farben und Designoptionen kommen später.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            "features" -> {
                item {
                    SettingsContentCard {
                        Text(
                            text = "Einstellungen für Quests und Challenges kommen später.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
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
                                text = "Bei Neuinstallation gehen Daten ohne Backup verloren.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            val file = exportFindings(context, allFindings)
                            shareBackup(context, file)
                            Toast.makeText(context, "Backup erstellt", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen
                        )
                    ) {
                        Text("Backup teilen")
                    }
                }

                item {
                    Button(
                        onClick = {
                            val imported = importFindings(context)
                            onImportFindings(imported)
                            Toast.makeText(context, "Backup geladen", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen
                        )
                    ) {
                        Text("Backup laden")
                    }
                }

                item {
                    Text(
                        text = "⚠️ Bei Neuinstallation gehen Daten verloren. Bitte vorher Backup erstellen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = TextPrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = TextPrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

    @Composable
    fun QuestCard(
        title: String,
        progress: Int,
        goal: Int,
        color: Color
    ) {
        val isDone = progress >= goal
        val shownProgress = if (progress > goal) goal else progress

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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Fortschritt: $shownProgress / $goal",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isDone) "Stufe abgeschlossen" else "Aktuelle Stufe",
                    style = MaterialTheme.typography.bodyMedium
                )
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

@Composable
fun FriendsScreen(
    extraTopPadding: Dp = 0.dp,
    extraBottomPadding: Dp = 0.dp
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
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
    fun ProfileScreen(
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
        val favoriteAnimal = animals.find { it.id == favoriteAnimalId }
        val wishlistAnimal = animals.find { it.id == wishlistAnimalId }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
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
                    text = "Profil",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground, contentColor = TextPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Deine Übersicht",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp)
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
                    colors = CardDefaults.cardColors(containerColor = CardBackground, contentColor = TextPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Lieblings-Fund",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        if (favoriteAnimal == null) {
                            Text(
                                text = "Noch kein Lieblingsfund gewählt",
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
                    colors = CardDefaults.cardColors(containerColor = CardBackground, contentColor = TextPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                items(findings.asReversed()) { finding ->
                    val animal = animals.find { it.id == finding.animalId }

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
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
        onBackClick: () -> Unit,
        onSaveFinding: (AnimalFinding) -> Unit,
        onDeleteFinding: (AnimalFinding) -> Unit,
        onUpdateFinding: (AnimalFinding, AnimalFinding) -> Unit,
        onSetFavoriteFindingAnimal: (AnimalEntry) -> Unit,
        onSetWishlistAnimal: (AnimalEntry) -> Unit,
        currentWishlistAnimalId: String?,
        extraTopPadding: Dp = 0.dp,
        extraBottomPadding: Dp = 0.dp
    ) {
        val context = LocalContext.current
        var date by rememberSaveable { mutableStateOf("") }
        var location by rememberSaveable { mutableStateOf("") }
        var note by rememberSaveable { mutableStateOf("") }
        var selectedPhotoUri by rememberSaveable { mutableStateOf("") }
        val isWishlistSelected = animal.id == currentWishlistAnimalId
        var editingFinding by remember { mutableStateOf<AnimalFinding?>(null) }
        val hasAnyFinding = findings.isNotEmpty()
        LaunchedEffect(initialFinding) {
            if (initialFinding != null) {
                date = initialFinding.date
                location = initialFinding.location
                note = initialFinding.note
                selectedPhotoUri = initialFinding.photoUri
                editingFinding = initialFinding
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

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(onClick = onBackClick) {
                    Text("Zurück")
                }
            }

            item {
                Text(
                    text = animal.germanName,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!hasAnyFinding) {
                        OutlinedButton(
                            onClick = {
                                onSetWishlistAnimal(animal)
                                onBackClick()
                            }
                        ) {
                            Text("Als Wunsch-Fund")
                        }
                    }

                    if (hasAnyFinding) {
                        Button(
                            onClick = {
                                onSetFavoriteFindingAnimal(animal)
                                onBackClick()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen
                            )
                        ) {
                            Text("Als liebsten Fund")
                        }
                    }
                }
            }
            item {
                Text(
                    text = animal.latinName,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Text(
                    text = "Gruppe: ${animal.group}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Text(
                    text = "ID: ${animal.id}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Text(
                    text = storageDebug,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            item {
                Text(
                    text = if (editingFinding == null) "Neuen Fund eintragen" else "Fund bearbeiten",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Datum, z. B. 03.04.2026") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fundort") },
                    singleLine = true,
                )
            }

            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PrimaryGreen
                    )
                )
            }

            item {
                Button(
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

            if (selectedPhotoUri.isBlank()) {
                item {
                    Text(
                        text = "Noch kein Foto ausgewählt.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                item {
                    Text(
                        text = "Foto ausgewählt",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                item {
                    UriImage(
                        uriString = selectedPhotoUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (
                                date.isNotBlank() ||
                                location.isNotBlank() ||
                                note.isNotBlank() ||
                                selectedPhotoUri.isNotBlank()
                            ) {
                                val storedPhotoUri = persistPhotoForFinding(context, selectedPhotoUri)

                                val newFinding = AnimalFinding(
                                    animalId = animal.id,
                                    date = date.trim(),
                                    location = location.trim(),
                                    note = note.trim(),
                                    photoUri = storedPhotoUri
                                )

                                if (editingFinding == null) {
                                    onSaveFinding(newFinding)
                                } else {
                                    onUpdateFinding(editingFinding!!, newFinding)
                                }

                                onBackClick()

                                date = ""
                                location = ""
                                note = ""
                                selectedPhotoUri = ""
                                editingFinding = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (editingFinding == null) "Fund speichern"
                            else "Änderungen speichern"
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        editingFinding?.let {
                            onDeleteFinding(it)
                            onBackClick()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Fund löschen")
                }
            }
        }
    }

    @Composable
    fun AnimalListScreen(
        debugMessage: String,
        onOpenSettings: () -> Unit,
        searchText: String,
        onSearchTextChange: (String) -> Unit,
        animals: List<AnimalEntry>,
        totalAnimalCount: Int,
        collectedAnimalCount: Int,
        showFoundOnly: Boolean,
        onToggleShowFoundOnly: () -> Unit,
        availableGroups: List<String>,
        selectedGroup: String,
        onSelectedGroupChange: (String) -> Unit,
        availableSubgroups: List<String>,
        selectedSubgroup: String,
        onSelectedSubgroupChange: (String) -> Unit,
        findingCountByAnimalId: Map<String, Int>,
        onAnimalClick: (AnimalEntry) -> Unit,
        currentSortOption: String,
        onSortOptionChange: (String) -> Unit,
        isPickerMode: Boolean = false,
        extraTopPadding: Dp = 0.dp,
        extraBottomPadding: Dp = 0.dp
    ) {
        val sortLabel = when (currentSortOption) {
            "A_Z" -> "A-Z"
            "Z_A" -> "Z-A"
            "FOUND_FIRST" -> "Gefunden"
            "NOT_FOUND_FIRST" -> "Offen"
            else -> "A-Z"
        }
        var groupMenuExpanded by remember { mutableStateOf(false) }
        var subgroupMenuExpanded by remember { mutableStateOf(false) }

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
                            text = if (isPickerMode) debugMessage else "Tierdex",
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
                            onClick = { groupMenuExpanded = true }
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
                            label = "Untergr.: $selectedSubgroup",
                            expanded = subgroupMenuExpanded,
                            onDismiss = { subgroupMenuExpanded = false },
                            onClick = { subgroupMenuExpanded = true }
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

                        OutlinedButton(
                            onClick = {
                                val nextOption = when (currentSortOption) {
                                    "A_Z" -> "Z_A"
                                    "Z_A" -> "FOUND_FIRST"
                                    "FOUND_FIRST" -> "NOT_FOUND_FIRST"
                                    else -> "A_Z"
                                }
                                onSortOptionChange(nextOption)
                            },
                            modifier = Modifier.height(40.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sort.: $sortLabel", style = MaterialTheme.typography.bodySmall)
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
        content: @Composable () -> Unit
    ) {
        Box {
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardBackground,
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
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
                        text = if (findingCount > 0) "✓ Gefunden ($findingCount)" else "Nicht gefunden",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (findingCount > 0) PrimaryGreen else TextSecondary
                    )
                }
            }
        }
    }


    @Composable
    fun UriImage(
        uriString: String,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        var bitmap by remember(uriString) { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(uriString) {
            bitmap = withContext(Dispatchers.IO) {
                loadCorrectlyOrientedBitmapFromUriString(context, uriString)
            }
        }

        bitmap?.let { loadedBitmap ->
            Image(
                bitmap = loadedBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
    }

    fun persistReadPermission(context: Context, uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    fun loadAnimalsFromCsvWithDebug(context: Context, fileName: String): CsvLoadResult {
        return try {
            val assetFiles = context.assets.list("")?.toList().orEmpty()

            if (!assetFiles.contains(fileName)) {
                return CsvLoadResult(
                    animals = emptyList(),
                    debugMessage = "Datei nicht gefunden. Assets enthalten: ${assetFiles.joinToString()}"
                )
            }

            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines()

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
                            rarity = parts.getOrElse(7) { "" }
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

    fun persistPhotoForFinding(context: Context, uriString: String): String {
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

    fun normalizeSearchText(text: String): String {
        return text
            .lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
            .replace("-", "")
            .replace(" ", "")
    }

    fun loadCorrectlyOrientedBitmapFromUriString(context: Context, uriString: String): Bitmap? {
        if (uriString.isBlank()) return null

        return if (uriString.startsWith("internal://")) {
            val fileName = uriString.removePrefix("internal://")
            val file = File(File(context.filesDir, FINDING_IMAGES_DIR), fileName)
            loadCorrectlyOrientedBitmapFromFile(file)
        } else {
            loadCorrectlyOrientedBitmapFromUri(context, Uri.parse(uriString))
        }
    }

    fun loadCorrectlyOrientedBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null

        val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        return applyExifOrientation(bitmap, orientation)
    }

    fun loadCorrectlyOrientedBitmapFromFile(file: File): Bitmap? {
        if (!file.exists()) return null

        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null

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

    fun rotateAndFlipBitmap(bitmap: Bitmap, degrees: Float, horizontalFlip: Boolean): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
            postScale(if (horizontalFlip) -1f else 1f, 1f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
        outline = BorderColor // Nutzt dein definiertes Grau-Grün für Umrandungen
    )