package com.mihnati.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.*
import com.mihnati.app.data.repository.EmploiPublicRepository
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.ui.components.ApplicationStepper
import com.mihnati.app.ui.components.DemoDataBanner
import com.mihnati.app.ui.components.TagBadge
import com.mihnati.app.ui.theme.*
import com.mihnati.app.translation.autoTranslate

@Composable
fun ApplicationsScreen(
    onNavigateToOpportunity: (String) -> Unit,
    onOpenGuides: () -> Unit
) {
    val context = LocalContext.current
    val applications by MihnatiRepository.applications.collectAsState()
    val examLists by EmploiPublicRepository.examLists.collectAsState()

    var activeSection by remember { mutableStateOf("dossiers") } // "dossiers" or "convocations"
    var selectedFilter by remember { mutableStateOf("all") }
    var showConvocationDialog by remember { mutableStateOf<Application?>(null) }
    var showNotesDialog by remember { mutableStateOf<Application?>(null) }
    var currentNoteText by remember { mutableStateOf("") }

    val filterOptions = listOf(
        "all" to AppStrings.filterAll,
        "pending" to AppStrings.filterPending,
        "exam" to AppStrings.filterExam,
        "interview" to AppStrings.filterInterview,
        "result" to AppStrings.filterResult
    )

    val filteredApps = remember(applications, selectedFilter) {
        when (selectedFilter) {
            "pending" -> applications.filter { it.currentStage == ApplicationStage.REVUE || it.currentStage == ApplicationStage.DEPOSE }
            "exam" -> applications.filter { it.currentStage == ApplicationStage.ECRIT }
            "interview" -> applications.filter { it.currentStage == ApplicationStage.ENTRETIEN }
            "result" -> applications.filter { it.currentStage == ApplicationStage.RESULTAT }
            else -> applications
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Title & Counter Summary
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = AppStrings.myApplicationsTitle,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${applications.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OnPrimary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Refresh button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerHigh)
                            .clickable {
                                EmploiPublicRepository.triggerLiveSync { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = PrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = AppStrings.refreshAction,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Secondary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${applications.count { !it.isAdmitted }} ${AppStrings.activeFilesCount}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Secondary)
                        )
                    }
                    Text(text = "•", color = OutlineVariant)
                    Text(
                        text = "${applications.count { it.isAdmitted }} ${AppStrings.finalizedSuccess}",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }
            }
        }

        // 1.5 Section Segment Picker: Mes Dossiers vs Convocations Écrit & Oral
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerLow)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Segment 1: Mes Dossiers
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeSection == "dossiers") SurfaceContainerLowest else Color.Transparent)
                        .clickable { activeSection = "dossiers" }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${AppStrings.tabMyFiles} (${applications.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (activeSection == "dossiers") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeSection == "dossiers") PrimaryContainer else OnSurfaceVariant
                        )
                    )
                }

                // Segment 2: Listes Écrit & Oral
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeSection == "convocations") SurfaceContainerLowest else Color.Transparent)
                        .clickable { activeSection = "convocations" }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${AppStrings.tabConvocations} (${examLists.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (activeSection == "convocations") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeSection == "convocations") PrimaryContainer else OnSurfaceVariant
                        )
                    )
                }
            }
        }

        if (activeSection == "dossiers") {
            // 2. Filter Pills
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { (key, label) ->
                        val isSelected = selectedFilter == key
                        val count = when (key) {
                            "pending" -> applications.count { it.currentStage == ApplicationStage.REVUE || it.currentStage == ApplicationStage.DEPOSE }
                            "exam" -> applications.count { it.currentStage == ApplicationStage.ECRIT }
                            "interview" -> applications.count { it.currentStage == ApplicationStage.ENTRETIEN }
                            "result" -> applications.count { it.currentStage == ApplicationStage.RESULTAT }
                            else -> applications.size
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PrimaryContainer else SurfaceContainer)
                                .clickable { selectedFilter = key }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) OnPrimary else OnSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else SurfaceContainerHighest)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = if (isSelected) OnPrimary else OnSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. Applications Cards Stack
            items(filteredApps) { app ->
                ApplicationItemCard(
                    application = app,
                    onConvocationClick = { showConvocationDialog = app },
                    onDetailsClick = { onNavigateToOpportunity(app.opportunityId) },
                    onAdvanceStage = {
                        MihnatiRepository.advanceApplicationStage(app.id)
                        Toast.makeText(context, "Étape du dossier mise à jour !", Toast.LENGTH_SHORT).show()
                    },
                    onEditNotes = {
                        showNotesDialog = app
                        currentNoteText = app.candidateNotes
                    },
                    onDelete = {
                        MihnatiRepository.deleteApplication(app.id)
                        Toast.makeText(context, "Dossier archivé", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            // Convocations & Listes Écrit / Oral Stack
            items(examLists, key = { it.id }) { exam ->
                NativeExamListCard(
                    exam = exam,
                    onAddToCalendar = {
                        EmploiPublicRepository.addExamToUserCalendar(exam)
                        Toast.makeText(context, "Épreuve ajoutée à votre Calendrier Mihnati !", Toast.LENGTH_SHORT).show()
                    },
                    onOpenPdf = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(exam.pdfDownloadUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Téléchargement de la liste officielle (${exam.candidatesCount} convoqués)...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        // 4. Candidate Support & Guidance Callout Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerHigh)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = autoTranslate("Préparez vos épreuves & entretiens"),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        Text(
                            text = autoTranslate("Accédez gratuitement aux annales corrigées des concours publics et aux conseils de simulation d'entretien RH Mihnati."),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OnSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        )
                        Row(
                            modifier = Modifier
                                .clickable { onOpenGuides() }
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = autoTranslate("Télécharger les guides"),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = PrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Convocation Dialog
    if (showConvocationDialog != null) {
        val app = showConvocationDialog!!
        AlertDialog(
            onDismissRequest = { showConvocationDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = PrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Convocation Officielle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = app.title,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Organisme : ${app.organization}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Référence : ${app.referenceNumber}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Outline)
                    )
                    HorizontalDivider(color = SurfaceContainerHigh)
                    Text(
                        text = app.statusHighlightTitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = app.statusHighlightDetail,
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Convocation téléchargée en PDF", Toast.LENGTH_SHORT).show()
                        showConvocationDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                ) {
                    Text("Télécharger PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConvocationDialog = null }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Candidate Notes Dialog
    if (showNotesDialog != null) {
        val app = showNotesDialog!!
        AlertDialog(
            onDismissRequest = { showNotesDialog = null },
            title = {
                Text(
                    text = "Notes personnelles",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Ajoutez des mémos, pièces déposées ou contacts pour ${app.organization} :",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                    OutlinedTextField(
                        value = currentNoteText,
                        onValueChange = { currentNoteText = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        placeholder = { Text("Ex: Dossier légalisé déposé au bureau d'ordre") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        MihnatiRepository.updateApplicationNotes(app.id, currentNoteText)
                        Toast.makeText(context, "Notes enregistrées !", Toast.LENGTH_SHORT).show()
                        showNotesDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun ApplicationItemCard(
    application: Application,
    onConvocationClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onAdvanceStage: () -> Unit,
    onEditNotes: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceContainerLowest)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Admitted Celebration Tag
            if (application.isAdmitted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SecondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = OnSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Admis & Retenu 🎉",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = "Offre validée",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Header Row: Org Logo & Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = application.sectorType.emoji, fontSize = 22.sp)
                    }

                    Column {
                        Text(
                            text = autoTranslate(application.title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            ),
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = autoTranslate(application.organization),
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant),
                                maxLines = 1
                            )
                            Text(text = "•", color = OutlineVariant)
                            Text(
                                text = autoTranslate(application.location),
                                style = MaterialTheme.typography.bodySmall.copy(color = Outline)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TagBadge(
                        text = application.sectorType.label,
                        backgroundColor = if (application.isAdmitted) SecondaryContainer else PrimaryFixed,
                        textColor = if (application.isAdmitted) OnSecondaryContainer else OnPrimaryFixed
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Archiver",
                            tint = Outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Highlight Status Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            application.isAdmitted -> SecondaryFixed.copy(alpha = 0.4f)
                            application.currentStage == ApplicationStage.ENTRETIEN -> SurfaceContainerLow
                            application.currentStage == ApplicationStage.ECRIT -> TertiaryFixed.copy(alpha = 0.35f)
                            else -> SurfaceContainerLow
                        }
                    )
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = when {
                            application.isAdmitted -> Icons.Default.TaskAlt
                            application.currentStage == ApplicationStage.ENTRETIEN -> Icons.Default.EventAvailable
                            application.currentStage == ApplicationStage.ECRIT -> Icons.Default.Description
                            else -> Icons.Default.HourglassTop
                        },
                        contentDescription = null,
                        tint = when {
                            application.isAdmitted -> Secondary
                            application.currentStage == ApplicationStage.ENTRETIEN -> PrimaryContainer
                            application.currentStage == ApplicationStage.ECRIT -> Tertiary
                            else -> PrimaryContainer
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = application.statusHighlightTitle,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    application.isAdmitted -> OnSecondaryFixed
                                    application.currentStage == ApplicationStage.ENTRETIEN -> PrimaryContainer
                                    application.currentStage == ApplicationStage.ECRIT -> Tertiary
                                    else -> OnSurface
                                }
                            )
                        )
                        Text(
                            text = application.statusHighlightDetail,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
            }

            // Stepper / Timeline
            ApplicationStepper(currentStage = application.currentStage)

            // Candidate notes if any
            if (application.candidateNotes.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerHigh.copy(alpha = 0.5f))
                        .clickable { onEditNotes() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Notes : ${application.candidateNotes}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnSurfaceVariant,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            HorizontalDivider(color = SurfaceContainerHigh)

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Réf: ${application.referenceNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Outline,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "${autoTranslate("Déposé le")} ${application.submittedDate}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Advance step button
                    if (!application.isAdmitted) {
                        OutlinedButton(
                            onClick = onAdvanceStage,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = autoTranslate("Étape +"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (application.convocationReady) {
                        Button(
                            onClick = onConvocationClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (application.currentStage == ApplicationStage.ECRIT) SurfaceContainerHigh else PrimaryContainer,
                                contentColor = if (application.currentStage == ApplicationStage.ECRIT) OnSurface else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (application.currentStage == ApplicationStage.ECRIT) Icons.Default.Download else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (LanguageManager.isArabic) {
                                    if (application.currentStage == ApplicationStage.ECRIT) autoTranslate("Convocation PDF") else autoTranslate("Voir convocation")
                                } else {
                                    if (application.currentStage == ApplicationStage.ECRIT) "Convocation PDF" else "Voir convocation"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        TextButton(onClick = onDetailsClick) {
                            Text(
                                text = autoTranslate("Détails"),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = PrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NativeExamListCard(
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
                        text = autoTranslate(exam.listType.labelFr),
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
                text = autoTranslate(exam.titleFr),
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
                            color = PrimaryContainer
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "📋 ${exam.candidatesCount} ${autoTranslate("convoqués")}",
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
                        Text(text = autoTranslate("📅 Date d'épreuve : "), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
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

            // Actions
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
