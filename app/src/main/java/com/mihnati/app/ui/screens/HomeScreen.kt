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

@Composable
fun HomeScreen(
    onNavigateToOpportunity: (String) -> Unit,
    onNavigateToTab: (String) -> Unit,
    onOpenSearch: () -> Unit
) {
    val opportunities by MihnatiRepository.opportunities.collectAsState()
    val userProfile by MihnatiRepository.userProfile.collectAsState()

    var selectedCategory by remember { mutableStateOf("Tous") }
    val categories = listOf(
        "Tous" to "✨",
        "Concours Publics" to "🏛️",
        "Tech & Digital" to "💻",
        "Banque & Finance" to "💼",
        "Masters & Formations" to "🎓",
        "Santé" to "🏥"
    )

    var showSalarySimulator by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var showMotivationLetter by remember { mutableStateOf(false) }
    var showDiplomaEligibility by remember { mutableStateOf(false) }
    var showOralExamPrep by remember { mutableStateOf(false) }

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
                    Text(
                        text = "Bonjour ${userProfile.fullName.split(" ").firstOrNull() ?: "Yassine"}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "👋", fontSize = 20.sp)
                }
                Text(
                    text = "Préparez votre avenir professionnel au Maroc",
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
                Text(
                    text = "Bac+5 Vérifié",
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
                        text = "Rechercher concours, poste, ministère...",
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
                    text = "BOÎTE À OUTILS CONCOURS 2026",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Outline,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "5 outils",
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
                                text = "Simulateur Salaire",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSecondaryContainer
                                )
                            )
                            Text(
                                text = "Échelles 8 à 11",
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
                                text = "Dossier Concours",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnPrimaryFixed
                                )
                            )
                            Text(
                                text = "Pièces & CNIE",
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
                                text = "Lettre Motivation",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Text(
                                text = "Modèle officiel",
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
                                text = "Vérif. Diplôme",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Text(
                                text = "Échelle & Corps",
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
                                text = "Prépa Entretien",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Text(
                                text = "Questions commissions",
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
                            text = "OBSERVATOIRE DE L'EMPLOI PUBLIC 2026",
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
                            text = "● En direct",
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
                            text = "Concours",
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
                            text = "Postes État",
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
                            text = "Régions",
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
                            text = "Vérifié",
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
                                text = "Expire dans ${urgentOpportunity.deadlineDaysRemaining}j",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OnTertiaryFixed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = urgentOpportunity.deadline,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryFixed,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Text(
                        text = urgentOpportunity.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = OnPrimary,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                    )

                    Text(
                        text = "${urgentOpportunity.organization} • 120 postes",
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
                            text = "🏛️ Échelle 11 • Grille indiciaire",
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
                                text = "Voir l'offre",
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
                        text = "Délais imminents",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = "Calendrier ➔",
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
                            text = "2j restants",
                            backgroundColor = ErrorContainer,
                            textColor = OnErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Douanes & Impôts",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Inspecteur 3ème grade",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant),
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Clôture: 04 Nov 2026",
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
                            text = "5j restants",
                            backgroundColor = TertiaryFixed,
                            textColor = OnTertiaryFixed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bourse Khawarizmi",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Doctorat & IA CNRST",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant),
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dépôt en ligne",
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
                            text = "Opportunités pour vous",
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
                        text = "Sélectionnées selon votre profil Bac+5",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
                    )
                }
                Text(
                    text = "Voir tout (${opportunities.size}) ➔",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { onNavigateToTab("opportunites") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val displayedOpportunities = remember(opportunities, selectedCategory) {
                val list = when (selectedCategory) {
                    "Concours Publics" -> opportunities.filter { it.sectorType == SectorType.PUBLIC }
                    "Tech & Digital" -> opportunities.filter {
                        it.field.contains("Informatique", ignoreCase = true) ||
                        it.field.contains("SI", ignoreCase = true) ||
                        it.title.contains("Java", ignoreCase = true) ||
                        it.title.contains("Cloud", ignoreCase = true)
                    }
                    "Banque & Finance" -> opportunities.filter {
                        it.organization.contains("Banque", ignoreCase = true) ||
                        it.field.contains("Économie", ignoreCase = true) ||
                        it.field.contains("Audit", ignoreCase = true)
                    }
                    "Masters & Formations" -> opportunities.filter { it.sectorType == SectorType.FORMATION }
                    "Santé" -> opportunities.filter {
                        it.organization.contains("Santé", ignoreCase = true) ||
                        it.field.contains("Santé", ignoreCase = true)
                    }
                    else -> opportunities
                }
                list.take(3)
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
}
