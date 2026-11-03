package com.mihnati.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.Opportunity
import com.mihnati.app.data.model.SectorType
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.ui.components.ConcoursChecklistDialog
import com.mihnati.app.ui.components.DiplomaEligibilityBottomSheet
import com.mihnati.app.ui.components.SalarySimulatorBottomSheet
import com.mihnati.app.ui.components.TagBadge
import com.mihnati.app.ui.theme.*
import com.mihnati.app.translation.autoTranslate

@Composable
fun OpportunitiesScreen(
    onNavigateToOpportunity: (String) -> Unit,
    onOpenWhatsAppAssistant: () -> Unit
) {
    val opportunities by MihnatiRepository.opportunities.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSector by remember { mutableStateOf<SectorType?>(null) }
    var selectedLevel by remember { mutableStateOf("Tous niveaux") }
    var onlyFavorites by remember { mutableStateOf(false) }
    var onlyUrgent by remember { mutableStateOf(false) }
    var isLevelTrayExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Échéance proche") }

    // Dialog & Sheet States
    var showSalarySimulator by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var showDiplomaEligibility by remember { mutableStateOf(false) }

    val studyLevels = listOf(
        "Tous niveaux",
        "Bac+2 / DUT / BTS",
        "Bac+3 / Licence",
        "Bac+5 / Master / Ingénieur",
        "Doctorat / Ph.D"
    )

    val bookmarkedCount = remember(opportunities) { opportunities.count { it.isBookmarked } }
    val urgentCount = remember(opportunities) { opportunities.count { it.deadlineDaysRemaining <= 5 } }

    // Filter logic
    val filteredOpportunities = remember(
        opportunities,
        searchQuery,
        selectedSector,
        selectedLevel,
        onlyFavorites,
        onlyUrgent,
        selectedSort
    ) {
        var list = opportunities

        if (onlyFavorites) {
            list = list.filter { it.isBookmarked }
        }

        if (onlyUrgent) {
            list = list.filter { it.deadlineDaysRemaining <= 5 }
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.organization.contains(searchQuery, ignoreCase = true) ||
                it.field.contains(searchQuery, ignoreCase = true) ||
                it.location.contains(searchQuery, ignoreCase = true) ||
                it.degreeRequired.contains(searchQuery, ignoreCase = true)
            }
        }

        if (selectedSector != null) {
            list = list.filter { it.sectorType == selectedSector }
        }

        if (selectedLevel != "Tous niveaux") {
            list = list.filter { opp ->
                when (selectedLevel) {
                    "Bac+2 / DUT / BTS" -> opp.degreeRequired.contains("Bac+2", ignoreCase = true) || opp.degreeRequired.contains("DUT", ignoreCase = true) || opp.degreeRequired.contains("BTS", ignoreCase = true) || opp.degreeRequired.contains("Technicien", ignoreCase = true)
                    "Bac+3 / Licence" -> opp.degreeRequired.contains("Bac+3", ignoreCase = true) || opp.degreeRequired.contains("Licence", ignoreCase = true)
                    "Bac+5 / Master / Ingénieur" -> opp.degreeRequired.contains("Bac+5", ignoreCase = true) || opp.degreeRequired.contains("Master", ignoreCase = true) || opp.degreeRequired.contains("Ingénieur", ignoreCase = true) || opp.degreeRequired.contains("DESA", ignoreCase = true)
                    "Doctorat / Ph.D" -> opp.degreeRequired.contains("Doctorat", ignoreCase = true) || opp.degreeRequired.contains("Recherche", ignoreCase = true) || opp.degreeRequired.contains("Thèse", ignoreCase = true)
                    else -> true
                }
            }
        }

        when (selectedSort) {
            "Échéance proche" -> list.sortedBy { it.deadlineDaysRemaining }
            "Pertinence profil" -> list.sortedByDescending { it.matchPercentage }
            else -> list
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Search Bar with Instant Clear
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Recherche",
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = AppStrings.searchOpportunitiesPlaceholder,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Outline)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = AppStrings.clearAction,
                                tint = Outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Main Filter Chips Bar (Tous, Favoris, Public, Privé, etc.)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tab "Tous"
                item {
                    val isAll = selectedSector == null && !onlyFavorites && !onlyUrgent
                    FilterChipPill(
                        label = AppStrings.tabAll,
                        count = opportunities.size,
                        isSelected = isAll,
                        onClick = {
                            selectedSector = null
                            onlyFavorites = false
                            onlyUrgent = false
                        }
                    )
                }

                // Tab "Favoris"
                item {
                    FilterChipPill(
                        label = AppStrings.tabFavorites,
                        count = bookmarkedCount,
                        isSelected = onlyFavorites,
                        emoji = "❤️",
                        activeColor = Error,
                        onClick = {
                            onlyFavorites = !onlyFavorites
                            if (onlyFavorites) {
                                selectedSector = null
                                onlyUrgent = false
                            }
                        }
                    )
                }

                // Tab "Concours Publics"
                item {
                    val isSelected = selectedSector == SectorType.PUBLIC && !onlyFavorites && !onlyUrgent
                    val count = opportunities.count { it.sectorType == SectorType.PUBLIC }
                    FilterChipPill(
                        label = AppStrings.tabPublicSector,
                        count = count,
                        isSelected = isSelected,
                        emoji = "🏛️",
                        activeColor = Secondary,
                        onClick = {
                            selectedSector = if (isSelected) null else SectorType.PUBLIC
                            onlyFavorites = false
                            onlyUrgent = false
                        }
                    )
                }

                // Tab "Secteur Privé"
                item {
                    val isSelected = selectedSector == SectorType.PRIVE && !onlyFavorites && !onlyUrgent
                    val count = opportunities.count { it.sectorType == SectorType.PRIVE }
                    FilterChipPill(
                        label = AppStrings.tabPrivateSector,
                        count = count,
                        isSelected = isSelected,
                        emoji = "🏢",
                        activeColor = Primary,
                        onClick = {
                            selectedSector = if (isSelected) null else SectorType.PRIVE
                            onlyFavorites = false
                            onlyUrgent = false
                        }
                    )
                }

                // Tab "Urgent (<5j)"
                item {
                    FilterChipPill(
                        label = autoTranslate("Urgent (<5j)"),
                        count = urgentCount,
                        isSelected = onlyUrgent,
                        emoji = "⏱️",
                        activeColor = Tertiary,
                        onClick = {
                            onlyUrgent = !onlyUrgent
                            if (onlyUrgent) {
                                selectedSector = null
                                onlyFavorites = false
                            }
                        }
                    )
                }

                // Tab "Masters & Formations"
                item {
                    val isSelected = selectedSector == SectorType.FORMATION && !onlyFavorites && !onlyUrgent
                    val count = opportunities.count { it.sectorType == SectorType.FORMATION }
                    FilterChipPill(
                        label = AppStrings.tabMastersTrainings,
                        count = count,
                        isSelected = isSelected,
                        emoji = "🎓",
                        activeColor = PrimaryContainer,
                        onClick = {
                            selectedSector = if (isSelected) null else SectorType.FORMATION
                            onlyFavorites = false
                            onlyUrgent = false
                        }
                    )
                }
            }
        }

        // 3. Quick Action Tools Bar: Salary Simulator & Checklist
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tool 1: Salary Simulator
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SecondaryContainer.copy(alpha = 0.5f))
                        .clickable { showSalarySimulator = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("💰", fontSize = 16.sp)
                        Column {
                            Text(
                                text = AppStrings.salarySimulator,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSecondaryContainer
                                )
                            )
                            Text(
                                text = AppStrings.salarySubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = OnSecondaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }

                // Tool 2: Checklist Pièces
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryFixed.copy(alpha = 0.6f))
                        .clickable { showChecklistDialog = true }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("📋", fontSize = 16.sp)
                        Column {
                            Text(
                                text = AppStrings.contestFile,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnPrimaryFixed
                                )
                            )
                            Text(
                                text = AppStrings.contestFileSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = OnPrimaryFixed.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }

                // Tool 3: Éligibilité Diplôme
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(12.dp))
                        .clickable { showDiplomaEligibility = true }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("🎓", fontSize = 16.sp)
                        Column {
                            Text(
                                text = AppStrings.diplomaChecker,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Text(
                                text = AppStrings.diplomaCheckerSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = OnSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick Level & City Filter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level Dropdown trigger
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(14.dp))
                        .clickable { isLevelTrayExpanded = !isLevelTrayExpanded }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    val displayLevel = if (LanguageManager.isArabic) {
                        when (selectedLevel) {
                            "Tous niveaux" -> AppStrings.levelAll
                            "Bac+2 / DUT / BTS" -> AppStrings.levelBac2
                            "Bac+3 / Licence" -> AppStrings.levelBac3
                            "Bac+5 / Master / Ingénieur" -> AppStrings.levelBac5
                            "Doctorat / Ph.D" -> AppStrings.levelDoctorate
                            else -> selectedLevel
                        }
                    } else {
                        selectedLevel
                    }
                    Text(
                        text = displayLevel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Icon(
                        imageVector = if (isLevelTrayExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Outline,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Sort Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(14.dp))
                        .clickable {
                            selectedSort = if (selectedSort == "Échéance proche") "Pertinence profil" else "Échéance proche"
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    val displaySort = if (LanguageManager.isArabic) {
                        if (selectedSort == "Échéance proche") AppStrings.sortDeadlineNear else AppStrings.sortProfileMatch
                    } else {
                        selectedSort
                    }
                    Text(
                        text = displaySort,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Expandable Study Level Drawer
        item {
            AnimatedVisibility(visible = isLevelTrayExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLow)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = autoTranslate("FILTRER PAR DIPLÔME REQUIS"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Outline,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            studyLevels.forEach { level ->
                                val isChosen = selectedLevel == level
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) Primary else SurfaceContainerLowest)
                                        .clickable {
                                            selectedLevel = level
                                            isLevelTrayExpanded = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = level.split("/").firstOrNull()?.trim() ?: level,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isChosen) OnPrimary else OnSurfaceVariant,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Results Count Line
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Secondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${filteredOpportunities.size} ${AppStrings.opportunitiesFound}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                if (selectedSector != null || selectedLevel != "Tous niveaux" || onlyFavorites || onlyUrgent || searchQuery.isNotEmpty()) {
                    Text(
                        text = autoTranslate("Réinitialiser"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable {
                            searchQuery = ""
                            selectedSector = null
                            selectedLevel = "Tous niveaux"
                            onlyFavorites = false
                            onlyUrgent = false
                        }
                    )
                }
            }
        }

        // Empty state when no match
        if (filteredOpportunities.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔍", fontSize = 36.sp)
                        Text(
                            text = AppStrings.noOffersFound,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AppStrings.modifySearchCriteria,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedSector = null
                                selectedLevel = "Tous niveaux"
                                onlyFavorites = false
                                onlyUrgent = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(autoTranslate("Voir toutes les opportunités"))
                        }
                    }
                }
            }
        }

        // 6. Modern Clean Opportunities List
        items(filteredOpportunities, key = { it.id }) { opp ->
            CleanOpportunityCard(
                opportunity = opp,
                onCardClick = { onNavigateToOpportunity(opp.id) },
                onBookmarkToggle = { MihnatiRepository.toggleBookmark(opp.id) },
                onOpenSalarySimulator = { showSalarySimulator = true }
            )
        }

        // 7. WhatsApp Assistant Banner Callout
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
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
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = autoTranslate("Assistant IA Mihnati"),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Text(
                                text = autoTranslate("Questions sur les oraux & barèmes"),
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                            )
                        }
                    }

                    Button(
                        onClick = onOpenWhatsAppAssistant,
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryContainer, contentColor = OnSecondaryContainer),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = autoTranslate("Poser une question"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets
    if (showSalarySimulator) {
        SalarySimulatorBottomSheet(
            initialGrade = "Échelle 11",
            onDismiss = { showSalarySimulator = false }
        )
    }

    if (showChecklistDialog) {
        ConcoursChecklistDialog(
            onDismiss = { showChecklistDialog = false }
        )
    }

    if (showDiplomaEligibility) {
        DiplomaEligibilityBottomSheet(
            onDismiss = { showDiplomaEligibility = false }
        )
    }
}

/**
 * Filter Chip Pill Component
 */
@Composable
private fun FilterChipPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    emoji: String? = null,
    activeColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val effectiveActiveColor = if (activeColor != Color.Unspecified) activeColor else Primary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) effectiveActiveColor else SurfaceContainerLowest)
            .border(
                1.dp,
                if (isSelected) effectiveActiveColor else SurfaceContainerHighest,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 13.sp)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isSelected) Color.White else OnSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isSelected) Color.White.copy(alpha = 0.25f) else SurfaceContainerHighest
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = if (isSelected) Color.White else OnSurfaceVariant
                )
            )
        }
    }
}

/**
 * Modern, airy, decluttered Opportunity Card
 */
@Composable
fun CleanOpportunityCard(
    opportunity: Opportunity,
    onCardClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onOpenSalarySimulator: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, SurfaceContainerHighest.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Organization logo, Name, City, Heart Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (opportunity.sectorType) {
                                    SectorType.PUBLIC -> SecondaryContainer.copy(alpha = 0.5f)
                                    SectorType.PRIVE -> PrimaryFixed.copy(alpha = 0.8f)
                                    SectorType.FORMATION -> SurfaceContainerHigh
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = opportunity.sectorType.emoji, fontSize = 20.sp)
                    }

                    Column {
                        Text(
                            text = autoTranslate(opportunity.organization),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Outline,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = autoTranslate(opportunity.location),
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Heart / Bookmark Button
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (opportunity.isBookmarked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favoris",
                        tint = if (opportunity.isBookmarked) Error else Outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Title
            Text(
                text = autoTranslate(opportunity.title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    lineHeight = 22.sp
                ),
                maxLines = 2
            )

            // Salary / Grade Transparency Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerLow)
                    .clickable {
                        if (opportunity.sectorType == SectorType.PUBLIC) {
                            onOpenSalarySimulator()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (opportunity.sectorType == SectorType.PUBLIC) "🏛️" else "💼",
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (opportunity.sectorType == SectorType.PUBLIC) {
                            when {
                                opportunity.title.contains("11") || opportunity.degreeRequired.contains("Échelle 11") || opportunity.degreeRequired.contains("Ingénieur") ->
                                    AppStrings.publicSectorScale11
                                opportunity.title.contains("10") || opportunity.degreeRequired.contains("Échelle 10") ->
                                    AppStrings.publicSectorScale10
                                opportunity.title.contains("9") || opportunity.degreeRequired.contains("Échelle 9") || opportunity.degreeRequired.contains("Technicien") ->
                                    AppStrings.publicSectorScale9
                                else -> AppStrings.publicSectorStatus
                            }
                        } else {
                            if (opportunity.salary.isNotBlank() && !opportunity.salary.contains("Grille", ignoreCase = true)) {
                                opportunity.salary
                            } else {
                                AppStrings.salaryAccordingProfile
                            }
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (opportunity.sectorType == SectorType.PUBLIC) Secondary else Primary
                        )
                    )
                }

                if (opportunity.sectorType == SectorType.PUBLIC) {
                    Text(
                        text = AppStrings.estimateSalaryAction,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Degree Badge
                TagBadge(
                    text = autoTranslate(opportunity.degreeRequired.split("(").firstOrNull()?.trim() ?: opportunity.degreeRequired),
                    backgroundColor = SurfaceContainer,
                    textColor = OnSurface
                )

                // Match Percentage
                TagBadge(
                    text = "${opportunity.matchPercentage}% ${AppStrings.matchWord}",
                    backgroundColor = SecondaryContainer.copy(alpha = 0.6f),
                    textColor = OnSecondaryContainer
                )

                // Deadline Badge
                val isUrgent = opportunity.deadlineDaysRemaining <= 4
                TagBadge(
                    text = if (isUrgent) {
                        "J-${opportunity.deadlineDaysRemaining}"
                    } else {
                        "${opportunity.deadlineDaysRemaining} ${autoTranslate("j restants")}"
                    },
                    backgroundColor = if (isUrgent) ErrorContainer else TertiaryFixed,
                    textColor = if (isUrgent) OnErrorContainer else OnTertiaryFixed
                )
            }
        }
    }
}
