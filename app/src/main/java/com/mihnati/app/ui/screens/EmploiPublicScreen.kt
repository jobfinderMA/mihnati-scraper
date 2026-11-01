package com.mihnati.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.*
import com.mihnati.app.data.repository.EmploiPublicRepository
import com.mihnati.app.ui.components.DemoDataBanner
import com.mihnati.app.ui.components.TagBadge
import com.mihnati.app.ui.theme.*

enum class EmploiPublicTab(val titleFr: String, val titleAr: String, val icon: String) {
    CONCOURS("Concours & Postes", "مباريات التوظيف", "🏛️"),
    LISTES_EXAMEN("Listes Écrit & Oral", "لوائح الاختبارات", "📋"),
    GRADES_DIPLOMES("Grades & Diplômes", "السلالم والشهادات", "🎓")
}

@Composable
fun EmploiPublicScreen(
    onNavigateBack: () -> Unit = {},
    onConcoursAddedToApplications: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(EmploiPublicTab.CONCOURS) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedEchelleFilter by remember { mutableStateOf(0) } // 0 = all
    var selectedListTypeFilter by remember { mutableStateOf<ExamListType?>(null) }

    val concoursList by EmploiPublicRepository.concours.collectAsState()
    val examLists by EmploiPublicRepository.examLists.collectAsState()
    val gradesList by EmploiPublicRepository.grades.collectAsState()
    val syncStatus by EmploiPublicRepository.syncStatus.collectAsState()

    // Filtered lists
    val filteredConcours = remember(concoursList, searchQuery, selectedEchelleFilter) {
        concoursList.filter { c ->
            val matchQuery = searchQuery.isEmpty() ||
                    c.titleFr.contains(searchQuery, ignoreCase = true) ||
                    c.titleAr.contains(searchQuery) ||
                    c.administrationFr.contains(searchQuery, ignoreCase = true) ||
                    c.diplomaRequired.contains(searchQuery, ignoreCase = true)
            val matchEchelle = selectedEchelleFilter == 0 || c.echelle == selectedEchelleFilter
            matchQuery && matchEchelle
        }
    }

    val filteredExamLists = remember(examLists, searchQuery, selectedListTypeFilter) {
        examLists.filter { l ->
            val matchQuery = searchQuery.isEmpty() ||
                    l.titleFr.contains(searchQuery, ignoreCase = true) ||
                    l.titleAr.contains(searchQuery) ||
                    l.administration.contains(searchQuery, ignoreCase = true)
            val matchType = selectedListTypeFilter == null || l.listType == selectedListTypeFilter
            matchQuery && matchType
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        // 1. Header with Emploi-Public Branding
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PrimaryContainer,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🇲🇦", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = "Portail Emploi-Public.ma",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "بوابة التشغيل العمومي بالمملكة المغربية",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryFixedDim,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Scraper Sync Button
                    Button(
                        onClick = {
                            EmploiPublicRepository.triggerLiveSync { success, serverMsg ->
                                Toast.makeText(context, serverMsg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (syncStatus.isSyncing) Secondary else Color.White,
                            contentColor = if (syncStatus.isSyncing) Color.White else PrimaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        enabled = !syncStatus.isSyncing
                    ) {
                        if (syncStatus.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Scraping...", style = MaterialTheme.typography.labelSmall)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Actualiser",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sync status chip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (syncStatus.isSuccess) SecondaryFixed else Color(0xFFFFB77D))
                        )
                        Text(
                            text = syncStatus.message,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = syncStatus.lastSyncTimestamp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryFixedDim,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        // 2. Navigation Tabs (Concours, Listes Écrit/Oral, Grades/Diplômes)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerLow)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmploiPublicTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val bg = if (isSelected) PrimaryContainer else Color.Transparent
                val contentColor = if (isSelected) Color.White else OnSurfaceVariant

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = tab.icon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tab.titleFr,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = contentColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 3. Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLowest)
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Outline,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = when (selectedTab) {
                                EmploiPublicTab.CONCOURS -> "Ministère, diplôme, échelle, intitulé..."
                                EmploiPublicTab.LISTES_EXAMEN -> "Administration, convocations oral/écrit..."
                                EmploiPublicTab.GRADES_DIPLOMES -> "Rechercher un grade, échelle ou salaire..."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = Outline)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Effacer",
                            tint = Outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. Content by Tab
        when (selectedTab) {
            EmploiPublicTab.CONCOURS -> {
                ConcoursTabContent(
                    concoursList = filteredConcours,
                    selectedEchelle = selectedEchelleFilter,
                    onEchelleSelected = { selectedEchelleFilter = it },
                    onAddToApplications = { concours ->
                        EmploiPublicRepository.importToApplications(concours)
                        Toast.makeText(
                            context,
                            "Concours ajouté avec succès dans 'Mes Candidatures' !",
                            Toast.LENGTH_LONG
                        ).show()
                        onConcoursAddedToApplications()
                    },
                    onOpenPdf = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Ouverture de l'Avis officiel sur emploi-public.ma...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            EmploiPublicTab.LISTES_EXAMEN -> {
                ExamListsTabContent(
                    lists = filteredExamLists,
                    selectedType = selectedListTypeFilter,
                    onTypeSelected = { selectedListTypeFilter = it },
                    onAddToCalendar = { exam ->
                        EmploiPublicRepository.addExamToUserCalendar(exam)
                        Toast.makeText(
                            context,
                            "Épreuve ajoutée à votre Calendrier Mihnati avec rappel !",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onOpenPdf = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Téléchargement de la liste officielle des convoqués...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            EmploiPublicTab.GRADES_DIPLOMES -> {
                GradesTabContent(
                    grades = gradesList.filter {
                        searchQuery.isEmpty() ||
                                it.nameFr.contains(searchQuery, ignoreCase = true) ||
                                it.nameAr.contains(searchQuery) ||
                                it.minDiploma.contains(searchQuery, ignoreCase = true)
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: Concours & Postes (مباريات التوظيف)
// -------------------------------------------------------------
@Composable
private fun ConcoursTabContent(
    concoursList: List<EmploiPublicConcours>,
    selectedEchelle: Int,
    onEchelleSelected: (Int) -> Unit,
    onAddToApplications: (EmploiPublicConcours) -> Unit,
    onOpenPdf: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Echelle Filter Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedEchelle == 0,
                        onClick = { onEchelleSelected(0) },
                        label = { Text("Toutes échelles (${concoursList.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedEchelle == 11,
                        onClick = { onEchelleSelected(if (selectedEchelle == 11) 0 else 11) },
                        label = { Text("Échelle 11 (Master / Ingénieur)") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedEchelle == 10,
                        onClick = { onEchelleSelected(if (selectedEchelle == 10) 0 else 10) },
                        label = { Text("Échelle 10 (Licence)") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedEchelle == 9,
                        onClick = { onEchelleSelected(if (selectedEchelle == 9) 0 else 9) },
                        label = { Text("Échelle 9 (DUT / BTS)") }
                    )
                }
            }
        }

        items(concoursList, key = { it.id }) { concours ->
            ConcoursCard(
                concours = concours,
                onAddToApplications = { onAddToApplications(concours) },
                onOpenPdf = { onOpenPdf(concours.avisPdfUrl) }
            )
        }
    }
}

@Composable
private fun ConcoursCard(
    concours: EmploiPublicConcours,
    onAddToApplications: () -> Unit,
    onOpenPdf: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category & Posts Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TagBadge(
                    text = concours.category.labelFr,
                    textColor = Primary,
                    backgroundColor = SurfaceContainerLow
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecondaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 ${concours.numPosts} postes",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Secondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // French & Arabic Titles
            Text(
                text = concours.titleFr,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    lineHeight = 22.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = concours.titleAr,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = OnSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Administration
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🏛️", fontSize = 14.sp)
                Text(
                    text = concours.administrationFr,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges: Grade, Échelle, Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TagBadge(text = "Échelle ${concours.echelle}", textColor = Tertiary, backgroundColor = TertiaryFixed.copy(alpha = 0.4f))
                TagBadge(text = concours.grade, textColor = OnSurfaceVariant, backgroundColor = SurfaceContainer)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Diploma requirement box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerLow)
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎓 Diplôme requis : ", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = concours.diplomaRequired,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏰ Date limite : ", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "${concours.deadlineDate} (reste ${concours.deadlineDaysRemaining} jours)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (concours.deadlineDaysRemaining <= 3) Error else Tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenPdf,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Avis PDF", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onAddToApplications,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Suivre dossier", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: Listes Écrit & Oral & Résultats (لوائح الاختبارات)
// -------------------------------------------------------------
@Composable
private fun ExamListsTabContent(
    lists: List<ConcoursExamList>,
    selectedType: ExamListType?,
    onTypeSelected: (ExamListType?) -> Unit,
    onAddToCalendar: (ConcoursExamList) -> Unit,
    onOpenPdf: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Filter by List Type (Écrit, Oral, Résultat final)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { onTypeSelected(null) },
                        label = { Text("Toutes les listes (${lists.size})") }
                    )
                }
                items(ExamListType.values()) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeSelected(if (selectedType == type) null else type) },
                        label = { Text("${type.labelFr} / ${type.labelAr}") }
                    )
                }
            }
        }

        items(lists, key = { it.id }) { examList ->
            ExamListCard(
                exam = examList,
                onAddToCalendar = { onAddToCalendar(examList) },
                onOpenPdf = { onOpenPdf(examList.pdfDownloadUrl) }
            )
        }
    }
}

@Composable
private fun ExamListCard(
    exam: ConcoursExamList,
    onAddToCalendar: () -> Unit,
    onOpenPdf: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Type Badge & Publication time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(exam.listType.badgeColorHex).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${exam.listType.labelFr} • ${exam.listType.labelAr}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(exam.listType.badgeColorHex)
                        )
                    )
                }
                Text(
                    text = exam.publishDate,
                    style = MaterialTheme.typography.labelSmall.copy(color = Outline, fontSize = 11.sp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // French & Arabic Title
            Text(
                text = exam.titleFr,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    lineHeight = 22.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exam.titleAr,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = OnSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Administration & Candidates Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "🏛️", fontSize = 14.sp)
                    Text(
                        text = exam.administration,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "📋 ${exam.candidatesCount} candidats",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Secondary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date & Examination Centers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerLow)
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📅 Date d'épreuve : ", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = exam.examDate, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📍 Centres : ", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = exam.examCenters.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenPdf,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Télécharger Liste PDF", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onAddToCalendar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Rappel Agenda", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: Grades & Diplômes (السلالم والشهادات)
// -------------------------------------------------------------
@Composable
private fun GradesTabContent(grades: List<EmploiPublicGrade>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryFixed.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Échelles & Rémunérations dans la Fonction Publique",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryFixed
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Grille officielle de correspondance entre vos diplômes d'études supérieures et les grades de recrutement au Maroc (Statut Général de la Fonction Publique).",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }
            }
        }

        items(grades, key = { "${it.echelle}-${it.nameFr}" }) { grade ->
            GradeCard(grade = grade)
        }
    }
}

@Composable
private fun GradeCard(grade: EmploiPublicGrade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${grade.echelle}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Column {
                        Text(
                            text = grade.nameFr,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = grade.nameAr,
                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecondaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Échelle ${grade.echelle}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = grade.description,
                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Diplôme requis & salaire
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerLow)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "🎓 Diplômes éligibles : ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = grade.minDiploma,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = OnSurface)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰 Salaire net indicatif : ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = grade.startingSalary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
