package com.mihnati.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.Opportunity
import com.mihnati.app.data.model.SectorType
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.ui.components.ConcoursChecklistDialog
import com.mihnati.app.ui.components.DiplomaEligibilityBottomSheet
import com.mihnati.app.ui.components.MotivationLetterBottomSheet
import com.mihnati.app.ui.components.OralExamPrepBottomSheet
import com.mihnati.app.ui.components.SalarySimulatorBottomSheet
import com.mihnati.app.ui.components.TagBadge
import com.mihnati.app.ui.theme.*
import com.mihnati.app.ui.theme.AppStrings
import com.mihnati.app.ui.theme.LanguageManager
import com.mihnati.app.translation.autoTranslate
import androidx.compose.ui.text.style.TextAlign

@Composable
fun HomeScreen(
    onNavigateToOpportunity: (String) -> Unit,
    onNavigateToTab: (String) -> Unit,
    onOpenSearch: () -> Unit
) {
    val opportunities by MihnatiRepository.opportunities.collectAsState()
    val userProfile by MihnatiRepository.userProfile.collectAsState()

    val isArabic = LanguageManager.isArabic
    val catAll = AppStrings.categoryAll
    val catAlerts = AppStrings.categoryCustomAlerts
    val catPublic = AppStrings.categoryPublic
    val catTech = AppStrings.categoryTech
    val catFinance = AppStrings.categoryFinance
    val catMasters = AppStrings.categoryMasters
    val catHealth = AppStrings.categoryHealth

    var selectedCategory by remember(isArabic) { mutableStateOf(catAll) }
    val categories = remember(catAll, catAlerts, catPublic, catTech, catFinance, catMasters, catHealth) {
        listOf(
            catAll to "✨",
            catAlerts to "🎯",
            catPublic to "🏛️",
            catTech to "💻",
            catFinance to "💼",
            catMasters to "🎓",
            catHealth to "🏥"
        )
    }

    var showSalarySimulator by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var showMotivationLetter by remember { mutableStateOf(false) }
    var showDiplomaEligibility by remember { mutableStateOf(false) }
    var showOralExamPrep by remember { mutableStateOf(false) }
    var showPdfExport by remember { mutableStateOf(false) }
    var showCnieFinder by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.mihnati.app.data.repository.EmploiPublicRepository.triggerLiveSync()
    }

    val urgentOpportunity = opportunities.find { it.id == "mef-admin-2026" } ?: opportunities.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 88.dp)
    ) {
        // 1. Header Greeting & Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isGuest = userProfile.account.provider == "guest" || userProfile.fullName == "Invité"
                    val greetingTitle = if (!isGuest && userProfile.fullName.isNotBlank()) {
                        "${autoTranslate("Bonjour")} ${userProfile.fullName.split(" ").firstOrNull() ?: ""}"
                    } else {
                        AppStrings.welcomeTitle
                    }
                    Text(
                        text = greetingTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "👋", fontSize = 20.sp)
                }
                Text(
                    text = AppStrings.welcomeSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant)
                )
            }

            // Profile status pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SecondaryContainer.copy(alpha = 0.5f))
                    .clickable { onNavigateToTab("profil") }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Secondary)
                )
                val pillText = if (userProfile.account.provider != "guest" && userProfile.fullName != "Invité") {
                    if (userProfile.targetLevel.isNotBlank()) "${userProfile.targetLevel} ${autoTranslate("Vérifié")}" else AppStrings.verifiedProfileBadge
                } else {
                    AppStrings.createProfileBadge
                }
                Text(
                    text = pillText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OnSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // 2. Search & Quick Action
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerLowest)
                .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                .clickable { onOpenSearch() }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Recherche",
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = AppStrings.searchPlaceholder,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Outline)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filtres",
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Moroccan Career Toolkit (5 Official Tools)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.toolsHeader,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Outline,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = AppStrings.toolsCount,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                // Tool 1: Salary Simulator
                item {
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SecondaryContainer.copy(alpha = 0.5f))
                            .border(1.dp, Secondary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .clickable { showSalarySimulator = true }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("💰", fontSize = 18.sp)
                            Text(
                                text = AppStrings.salarySimulator,
                                style = MaterialTheme.typography.labelMedium.copy(
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

                // Tool 2: Checklist Dossier
                item {
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PrimaryFixed.copy(alpha = 0.6f))
                            .border(1.dp, Primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .clickable { showChecklistDialog = true }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📋", fontSize = 18.sp)
                            Text(
                                text = AppStrings.contestFile,
                                style = MaterialTheme.typography.labelMedium.copy(
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

                // Tool 3: Motivation Letter
                item {
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                            .clickable { showMotivationLetter = true }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📄", fontSize = 18.sp)
                            Text(
                                text = AppStrings.coverLetter,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Text(
                                text = AppStrings.coverLetterSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = OnSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Tool 4: Diploma Eligibility Checker
                item {
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                            .clickable { showDiplomaEligibility = true }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🎓", fontSize = 18.sp)
                            Text(
                                text = AppStrings.diplomaChecker,
                                style = MaterialTheme.typography.labelMedium.copy(
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

                // Tool 5: Oral Exam Prep
                item {
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                            .clickable { showOralExamPrep = true }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🎙️", fontSize = 18.sp)
                            Text(
                                text = AppStrings.oralPrep,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Text(
                                text = AppStrings.oralPrepSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = OnSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Tool 6: Official PDF Application Dossier Exporter (Feature C)
                item {
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SecondaryContainer.copy(alpha = 0.6f))
                            .border(1.dp, Secondary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable { showPdfExport = true }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🖨️", fontSize = 18.sp)
                            Text(
                                text = AppStrings.pdfExportTitle,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSecondaryContainer
                                )
                            )
                            Text(
                                text = AppStrings.pdfExportSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = OnSecondaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }

                // Tool 7: Instant CNIE Convocation Checker
                item {
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SecondaryContainer.copy(alpha = 0.6f))
                            .border(1.dp, Secondary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable { showCnieFinder = true }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🔍", fontSize = 18.sp)
                            Text(
                                text = AppStrings.cnieFinderTitle,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSecondaryContainer
                                )
                            )
                            Text(
                                text = AppStrings.cnieFinderSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = OnSecondaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Observatoire de l'Emploi Public (Morocco Live Dashboard KPI)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerLowest)
                .border(1.dp, SurfaceContainerHighest.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🇲🇦", fontSize = 15.sp)
                        Text(
                            text = AppStrings.observatoryTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SecondaryContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = AppStrings.liveStatus,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${opportunities.size}+",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary
                            )
                        )
                        Text(
                            text = AppStrings.statConcours,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = OnSurfaceVariant)
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(22.dp).background(SurfaceContainerHighest))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2 450+",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Secondary
                            )
                        )
                        Text(
                            text = AppStrings.statPositions,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = OnSurfaceVariant)
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(22.dp).background(SurfaceContainerHighest))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "12",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Tertiary
                            )
                        )
                        Text(
                            text = AppStrings.statRegions,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = OnSurfaceVariant)
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(22.dp).background(SurfaceContainerHighest))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF006C4A)
                            )
                        )
                        Text(
                            text = AppStrings.statVerified,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = OnSurfaceVariant)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Horizontal Categories
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { (category, emoji) ->
                val selected = selectedCategory == category
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) Primary else SurfaceContainerLowest)
                        .border(
                            1.dp,
                            if (selected) Primary else SurfaceContainerHighest,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = emoji, fontSize = 13.sp)
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (selected) OnPrimary else OnSurfaceVariant,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Flash Hero Card (Featured Concours)
        if (urgentOpportunity != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryContainer, Primary, OnPrimaryFixed)
                        )
                    )
                    .clickable { onNavigateToOpportunity(urgentOpportunity.id) }
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Top alert row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(TertiaryFixed)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = OnTertiaryFixed,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${AppStrings.expiresPrefix} ${urgentOpportunity.deadlineDaysRemaining} ${autoTranslate("jours")}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OnTertiaryFixed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = autoTranslate(urgentOpportunity.deadline),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryFixed,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Text(
                        text = autoTranslate(urgentOpportunity.title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = OnPrimary,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                    )

                    Text(
                        text = "${autoTranslate(urgentOpportunity.organization)} • 120 ${autoTranslate("postes")}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryFixed
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏛️ ${autoTranslate("Échelle 11 • Grille indiciaire")}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SecondaryFixed,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Button(
                            onClick = { onNavigateToOpportunity(urgentOpportunity.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceContainerLowest,
                                contentColor = Primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = AppStrings.viewOffer,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Mini Deadlines Carousel
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationImportant,
                        contentDescription = null,
                        tint = Tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.imminentDeadlines,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = AppStrings.viewCalendar,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { onNavigateToTab("calendrier") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mini Deadline 1: Douanes
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToOpportunity("douanes-inspecteur-2026") }
                        .padding(14.dp)
                ) {
                    Column {
                        TagBadge(
                            text = autoTranslate("Restants 2j"),
                            backgroundColor = ErrorContainer,
                            textColor = OnErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = autoTranslate("Douanes & Impôts"),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = autoTranslate("Inspecteur 3ème grade"),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant),
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = autoTranslate("Clôture: 04 Nov 2026"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Mini Deadline 2: Bourse Al Khawarizmi
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToOpportunity("bourse-cnrst-2026") }
                        .padding(14.dp)
                ) {
                    Column {
                        TagBadge(
                            text = autoTranslate("Restants 5j"),
                            backgroundColor = TertiaryFixed,
                            textColor = OnTertiaryFixed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = autoTranslate("Bourse Khawarizmi"),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = autoTranslate("Doctorat & IA CNRST"),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant),
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppStrings.onlineSubmission,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 7. Recommended Opportunities
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = AppStrings.recommendedForYou,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = AppStrings.recommendedSubtitle,
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
                    )
                }
                Text(
                    text = "${AppStrings.viewAll} (${opportunities.size}) ➔",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { onNavigateToTab("opportunites") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val strCustomAlerts = AppStrings.categoryCustomAlerts
            val strPublicOpp = AppStrings.categoryPublic
            val strTechOpp = AppStrings.categoryTech
            val strFinanceOpp = AppStrings.categoryFinance
            val strMastersOpp = AppStrings.categoryMasters
            val strHealthOpp = AppStrings.categoryHealth

            val displayedOpportunities = remember(opportunities, selectedCategory, userProfile.preferences, strCustomAlerts, strPublicOpp, strTechOpp, strFinanceOpp, strMastersOpp, strHealthOpp) {
                val list = when (selectedCategory) {
                    "Mes alertes ciblées", strCustomAlerts ->
                        opportunities.filter { userProfile.preferences.matches(it) }
                    "Concours Publics", strPublicOpp -> opportunities.filter { it.sectorType == SectorType.PUBLIC }
                    "Tech & Digital", strTechOpp -> opportunities.filter {
                        it.field.contains("Informatique", ignoreCase = true) ||
                        it.field.contains("SI", ignoreCase = true) ||
                        it.title.contains("Java", ignoreCase = true) ||
                        it.title.contains("Cloud", ignoreCase = true)
                    }
                    "Banque & Finance", strFinanceOpp -> opportunities.filter {
                        it.organization.contains("Banque", ignoreCase = true) ||
                        it.field.contains("Économie", ignoreCase = true) ||
                        it.field.contains("Audit", ignoreCase = true)
                    }
                    "Masters", "Masters & Formations", strMastersOpp -> opportunities.filter { it.sectorType == SectorType.FORMATION }
                    "Santé", strHealthOpp -> opportunities.filter {
                        it.organization.contains("Santé", ignoreCase = true) ||
                        it.field.contains("Santé", ignoreCase = true)
                    }
                    else -> opportunities
                }
                list.take(3)
            }

            if (displayedOpportunities.isEmpty() && selectedCategory == "Mes alertes ciblées") {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🎯", fontSize = 24.sp)
                        Text(
                            text = AppStrings.noAlertsFoundTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Primary
                        )
                        Text(
                            text = AppStrings.noAlertsFoundDesc,
                            fontSize = 11.sp,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = { onNavigateToTab("profil") },
                            modifier = Modifier.padding(top = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(AppStrings.editFiltersBtn, fontSize = 11.sp)
                        }
                    }
                }
            }

            displayedOpportunities.forEach { opp ->
                CleanOpportunityCard(
                    opportunity = opp,
                    onCardClick = { onNavigateToOpportunity(opp.id) },
                    onBookmarkToggle = { MihnatiRepository.toggleBookmark(opp.id) },
                    onOpenSalarySimulator = { showSalarySimulator = true }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Sheets & Dialogs
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

    if (showMotivationLetter) {
        MotivationLetterBottomSheet(
            opportunityTitle = urgentOpportunity?.title ?: "Administrateur 2ème grade",
            organization = urgentOpportunity?.organization ?: "Ministère de l'Économie et des Finances",
            reference = urgentOpportunity?.reference ?: "MEF-CONCOURS-2026",
            onDismiss = { showMotivationLetter = false }
        )
    }

    if (showDiplomaEligibility) {
        DiplomaEligibilityBottomSheet(
            onDismiss = { showDiplomaEligibility = false }
        )
    }

    if (showOralExamPrep) {
        OralExamPrepBottomSheet(
            onDismiss = { showOralExamPrep = false }
        )
    }

    if (showPdfExport) {
        com.mihnati.app.ui.components.OfficialPdfExportBottomSheet(
            targetOpportunity = urgentOpportunity,
            onDismiss = { showPdfExport = false }
        )
    }

    if (showCnieFinder) {
        com.mihnati.app.ui.components.CnieConvocationFinderSheet(
            onDismiss = { showCnieFinder = false }
        )
    }
}
