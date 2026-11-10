package com.mihnati.app.ui.screens

import com.mihnati.app.translation.autoTranslate

import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.DocumentCategory
import com.mihnati.app.data.model.Opportunity
import com.mihnati.app.data.model.SectorType
import com.mihnati.app.data.repository.EmploiPublicRepository
import com.mihnati.app.ui.components.openOfficialPdf
import com.mihnati.app.ui.components.shareOfficialPdf
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.ui.components.ConcoursChecklistDialog
import com.mihnati.app.ui.components.DirectApplicationBottomSheet
import com.mihnati.app.ui.components.MotivationLetterBottomSheet
import com.mihnati.app.ui.components.SalarySimulatorBottomSheet
import com.mihnati.app.ui.components.SecureDocumentUploadBottomSheet
import com.mihnati.app.ui.components.TagBadge
import com.mihnati.app.ui.theme.*

@Composable
fun OpportunityDetailScreen(
    opportunityId: String,
    onBack: () -> Unit,
    onOpenWhatsAppAssistant: () -> Unit
) {
    val context = LocalContext.current
    val opportunities by MihnatiRepository.opportunities.collectAsState()
    val applications by MihnatiRepository.applications.collectAsState()
    val userProfile by MihnatiRepository.userProfile.collectAsState()
    val opportunity = opportunities.find { it.id == opportunityId } ?: opportunities.firstOrNull()
    val examLists by EmploiPublicRepository.examLists.collectAsState()

    val effectiveAvisPdfUrl = remember(opportunity) {
        when {
            opportunity == null -> "https://www.emploi-public.ma"
            opportunity.avisPdfUrl.isNotBlank() -> opportunity.avisPdfUrl
            opportunity.sourceUrl.endsWith(".pdf", ignoreCase = true) -> opportunity.sourceUrl
            else -> "https://www.emploi-public.ma/upload/concours/${opportunity.reference.ifEmpty { "Avis_Officiel" }}.pdf"
        }
    }

    val matchingExamLists = remember(opportunity, examLists) {
        if (opportunity == null) examLists.take(2)
        else {
            examLists.filter { list ->
                list.concoursId == opportunity.id ||
                list.administration.contains(opportunity.organization.take(8), ignoreCase = true) ||
                opportunity.organization.contains(list.administration.take(8), ignoreCase = true)
            }.ifEmpty {
                examLists.take(2)
            }
        }
    }
    val currentLang by com.mihnati.app.ui.theme.LanguageManager.currentLanguage.collectAsState()
    val isArabic = currentLang == com.mihnati.app.ui.theme.AppLanguage.ARABIC
    val orgDisplayName = if (isArabic) {
        if (opportunity != null && opportunity.organizationAr.isNotBlank() && com.mihnati.app.translation.AutoTranslationService.isTextArabic(opportunity.organizationAr)) {
            opportunity.organizationAr
        } else {
            autoTranslate(opportunity?.organization ?: "")
        }
    } else {
        if (opportunity != null && com.mihnati.app.translation.AutoTranslationService.isTextArabic(opportunity.organization)) {
            autoTranslate(opportunity.organization)
        } else {
            opportunity?.organization ?: ""
        }
    }

    val titleDisplayName = if (isArabic) {
        if (opportunity != null && opportunity.titleAr.isNotBlank() && com.mihnati.app.translation.AutoTranslationService.isTextArabic(opportunity.titleAr)) {
            opportunity.titleAr
        } else {
            autoTranslate(opportunity?.title ?: "")
        }
    } else {
        if (opportunity != null && com.mihnati.app.translation.AutoTranslationService.isTextArabic(opportunity.title)) {
            autoTranslate(opportunity.title)
        } else {
            opportunity?.title ?: ""
        }
    }
    val existingApp = remember(applications, opportunity) {
        applications.find { it.opportunityId == opportunity?.id }
    }

    var showApplySuccessToast by remember { mutableStateOf(false) }
    var showSalarySimulator by remember { mutableStateOf(false) }
    var showMotivationLetter by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var showDirectApplySheet by remember { mutableStateOf(false) }
    var showUploadDocSheet by remember { mutableStateOf(false) }
    var showPdfExportSheet by remember { mutableStateOf(false) }
    var showCnieFinderSheet by remember { mutableStateOf(false) }
    var activePdfViewer by remember { mutableStateOf<Pair<java.io.File, String>?>(null) }
    var selectedCategoryToUpload by remember { mutableStateOf(DocumentCategory.CV) }

    if (opportunity == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Opportunité introuvable", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = OnSurface
                        )
                    }
                    Text(
                        text = autoTranslate("Détail Opportunité"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, opportunity.title)
                                putExtra(Intent.EXTRA_TEXT, "Découvre cette offre sur Mihnati : ${opportunity.title} à ${opportunity.organization} (Réf: ${opportunity.reference})")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager avec"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Partager",
                            tint = OnSurfaceVariant
                        )
                    }
                }
            }

            // 1. Hero Visual Framing Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryContainer, Primary, Secondary)
                        )
                    )
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Secondary)
                            )
                            Text(
                                text = autoTranslate("Recrutement actif 2026"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${autoTranslate("Réf :")} ${opportunity.reference}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                            )
                        }
                    }

                    // Bottom info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Domain,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = autoTranslate(opportunity.location),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = autoTranslate("Officiel emploi-public.ma"),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Main Entity & Title Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceContainerLowest)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                            .background(SecondaryContainer.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = opportunity.sectorType.emoji, fontSize = 24.sp)
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = orgDisplayName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = autoTranslate("Direction & Encadrement"),
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                            )
                        }
                    }

                    Text(
                        text = titleDisplayName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                            lineHeight = 24.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TagBadge(
                            text = autoTranslate(opportunity.sectorType.label),
                            backgroundColor = SurfaceContainerHigh,
                            textColor = Primary,
                            icon = Icons.Default.Apartment
                        )
                        TagBadge(
                            text = "🏛️ Emploi-Public",
                            backgroundColor = SecondaryContainer.copy(alpha = 0.6f),
                            textColor = OnSecondaryContainer
                        )
                        if (opportunity.postsCount > 1) {
                            TagBadge(
                                text = "👥 ${opportunity.postsCount} " + autoTranslate("postes"),
                                backgroundColor = SurfaceContainerHigh,
                                textColor = OnSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Deadline Alert & Match Score
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Deadline Alert
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerHigh)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = autoTranslate("Délai limite de candidature"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = autoTranslate(opportunity.deadline),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = OnSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerLowest)
                                .clickable {
                                    val calIntent = Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(CalendarContract.Events.TITLE, "Clôture: ${opportunity.title}")
                                        putExtra(CalendarContract.Events.DESCRIPTION, "Délai limite pour l'offre ${opportunity.organization} (Réf: ${opportunity.reference})")
                                        putExtra(CalendarContract.Events.EVENT_LOCATION, opportunity.location)
                                    }
                                    context.startActivity(calIntent)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = autoTranslate("J-${opportunity.deadlineDaysRemaining}"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Written Exam Date Card (from Emploi-Public official notices)
                if (opportunity.examDate.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SecondaryContainer.copy(alpha = 0.45f))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EventAvailable,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = autoTranslate("Date de l'épreuve écrite"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = autoTranslate(opportunity.examDate),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = OnSecondaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Match Score
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = autoTranslate("Compatibilité avec votre profil"),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                )
                            }
                            Text(
                                text = "${opportunity.matchPercentage}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        LinearProgressIndicator(
                            progress = { opportunity.matchPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Secondary,
                            trackColor = SurfaceContainerLow
                        )

                        Text(
                            text = autoTranslate("✓ Parfait match : Diplôme Bac+5 & spécialité informatique enregistrés sur votre CV Mihnati."),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Key Metrics Summary
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 1: Degree
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = autoTranslate("DIPLÔME REQUIS"),
                                style = MaterialTheme.typography.labelSmall.copy(color = Outline, fontSize = 10.sp)
                            )
                            Text(
                                text = autoTranslate(opportunity.degreeRequired),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    // Metric 2: Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = autoTranslate("SPÉCIALITÉ"),
                                style = MaterialTheme.typography.labelSmall.copy(color = Outline, fontSize = 10.sp)
                            )
                            Text(
                                text = autoTranslate(opportunity.field),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 3: Experience
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = autoTranslate("EXPÉRIENCE"),
                                style = MaterialTheme.typography.labelSmall.copy(color = Outline, fontSize = 10.sp)
                            )
                            Text(
                                text = autoTranslate(opportunity.experience),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    // Metric 4: Compensation / Grade
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (opportunity.sectorType == SectorType.PUBLIC) autoTranslate("GRILLE ADMINISTRATIVE") else autoTranslate("RÉMUNÉRATION"),
                                style = MaterialTheme.typography.labelSmall.copy(color = Outline, fontSize = 10.sp)
                            )
                            Text(
                                text = if (opportunity.sectorType == SectorType.PUBLIC) {
                                    if (opportunity.title.contains("11") || opportunity.degreeRequired.contains("11") || opportunity.degreeRequired.contains("Ingénieur"))
                                        autoTranslate("Échelle 11 (Grille État)")
                                    else if (opportunity.title.contains("10") || opportunity.degreeRequired.contains("10"))
                                        autoTranslate("Échelle 10 (Grille État)")
                                    else if (opportunity.title.contains("9") || opportunity.degreeRequired.contains("9"))
                                        autoTranslate("Échelle 9 (Grille État)")
                                    else
                                        autoTranslate("Statut fonctionnaire")
                                } else {
                                    if (opportunity.salary.isNotBlank() && !opportunity.salary.contains("Grille", ignoreCase = true))
                                        opportunity.salary
                                    else
                                        autoTranslate("Selon profil & barème")
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }

                // Moroccan Public Tools Shortcuts Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showSalarySimulator = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryContainer, contentColor = OnSecondaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text("💰", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.salarySimulator, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }

                    Button(
                        onClick = { showMotivationLetter = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryFixed, contentColor = OnPrimaryFixed),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text("📄", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.coverLetter, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Description & Missions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceContainerLowest)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = autoTranslate("Description du poste"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    )
                    Text(
                        text = autoTranslate(opportunity.description),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OnSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    )

                    if (opportunity.missions.isNotEmpty()) {
                        Text(
                            text = autoTranslate("Missions principales :"),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        opportunity.missions.forEach { mission ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryFixed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = OnPrimaryFixed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = autoTranslate(mission.title),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurface
                                        )
                                    )
                                    Text(
                                        text = autoTranslate(mission.description),
                                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Required Documents Checklist
            if (opportunity.requiredDocuments.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceContainerLowest)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = autoTranslate("Pièces à fournir pour le dossier"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            TagBadge(
                                text = "${opportunity.requiredDocuments.size} " + autoTranslate("Documents"),
                                backgroundColor = SecondaryFixed,
                                textColor = OnSecondaryFixed
                            )
                        }

                        opportunity.requiredDocuments.forEach { doc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = autoTranslate(doc.name),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = OnSurface
                                            )
                                        )
                                        Text(
                                            text = autoTranslate(doc.detail),
                                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Prêt",
                                    tint = Secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // 7. Smart 48h / 24h Native Deadline Alarms Card (Feature B)
                val isAlarmActive = remember(opportunity.id) {
                    mutableStateOf(com.mihnati.app.notifications.ConcoursAlarmScheduler.isAlarmActive(context, opportunity.id))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SecondaryContainer.copy(alpha = 0.45f))
                        .border(1.dp, Secondary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⏰", fontSize = 18.sp)
                                }
                                Column {
                                    Text(
                                        text = autoTranslate("Rappels de Cloture (J-2 & J-1)"),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OnSecondaryContainer
                                        )
                                    )
                                    Text(
                                        text = autoTranslate("Alertes directes 48h et 24h avant la fermeture"),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = OnSecondaryContainer.copy(alpha = 0.85f),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = isAlarmActive.value,
                                onCheckedChange = { checked ->
                                    isAlarmActive.value = checked
                                    if (checked) {
                                        com.mihnati.app.notifications.ConcoursAlarmScheduler.scheduleDeadlineAlarms(context, opportunity)
                                        Toast.makeText(context, "Rappels 48h & 24h programmes avec succes ⏰", Toast.LENGTH_SHORT).show()
                                    } else {
                                        com.mihnati.app.notifications.ConcoursAlarmScheduler.cancelDeadlineAlarms(context, opportunity.id)
                                        Toast.makeText(context, "Rappels desactives", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Secondary,
                                    uncheckedThumbColor = Outline,
                                    uncheckedTrackColor = SurfaceContainerHigh
                                )
                            )
                        }

                        if (isAlarmActive.value) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔔 " + autoTranslate("Alarmes actives sur votre appareil (100% hors-ligne)"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                TextButton(
                                    onClick = {
                                        com.mihnati.app.notifications.ConcoursAlarmScheduler.scheduleQuickTestAlarm(context, opportunity, delaySeconds = 3)
                                        Toast.makeText(context, "Alarme test programmee dans 3s !", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = autoTranslate("Tester l'alarme (3s)"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. Official Government Documents & Candidate Lists (PDF) (Options 1 & 2)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📜", fontSize = 18.sp)
                                }
                                Column {
                                    Text(
                                        text = autoTranslate("Documents Officiels de la Fonction Publique"),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurface
                                        )
                                    )
                                    Text(
                                        text = autoTranslate("Portail officiel emploi-public.ma"),
                                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant, fontSize = 11.sp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SecondaryContainer.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = autoTranslate("Certifié"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OnSecondaryContainer,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = SurfaceContainerHigh)

                        // 1. OPTION 1: Avis de Concours Officiel (PDF)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = autoTranslate("1. Avis de Concours Officiel (PDF)"),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                )
                            }

                            Text(
                                text = autoTranslate("Arrêté ministériel officiel portant ouverture du concours, nombre de postes (${opportunity.postsCount}), barème et conditions de candidature."),
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        openOfficialPdf(context, effectiveAvisPdfUrl, opportunity.title)
                                    },
                                    modifier = Modifier.weight(1.4f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = autoTranslate("Consulter l'Avis PDF"),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        shareOfficialPdf(context, opportunity.title, effectiveAvisPdfUrl)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = autoTranslate("Partager"),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = SurfaceContainerHigh)

                        // 2. OPTION 2: Listes Officielles des Candidats (PDF)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatListNumbered,
                                    contentDescription = null,
                                    tint = Secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = autoTranslate("2. Listes Officielles des Candidats (PDF)"),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Secondary
                                    )
                                )
                            }

                            Text(
                                text = autoTranslate("Listes des convoqués aux épreuves écrites, orales et résultats définitifs d'admission."),
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                            )

                            Button(
                                onClick = { showCnieFinderSheet = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                            ) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = autoTranslate("Suis-je convoqué(e) ? (Recherche par CNIE)"),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            matchingExamLists.forEach { exam ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceContainerLow)
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(exam.listType.badgeColorHex).copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = autoTranslate(exam.listType.labelFr),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(exam.listType.badgeColorHex),
                                                        fontSize = 11.sp
                                                    )
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

                                        Text(
                                            text = autoTranslate(exam.titleFr),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )

                                        Text(
                                            text = "📅 ${exam.examDate} • 📍 ${exam.examCenters.take(2).joinToString(", ")}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant, fontSize = 11.sp)
                                        )

                                        Button(
                                            onClick = {
                                                val file = com.mihnati.app.pdf.OfficialPdfDocumentHelper.getOrCreateCandidateListPdf(context, exam)
                                                activePdfViewer = file to exam.titleFr
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.9f)),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = autoTranslate("Télécharger la Liste Officielle (PDF)"),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
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

        // Sticky Bottom Interactive Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = Surface.copy(alpha = 0.95f),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bookmark button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceContainerHigh)
                        .clickable {
                            MihnatiRepository.toggleBookmark(opportunity.id)
                            val state = !opportunity.isBookmarked
                            if (state) {
                                com.mihnati.app.notifications.ConcoursAlarmScheduler.scheduleDeadlineAlarms(context, opportunity)
                                Toast.makeText(context, "Offre ajoutee aux favoris & Rappels J-2/J-1 actives", Toast.LENGTH_SHORT).show()
                            } else {
                                com.mihnati.app.notifications.ConcoursAlarmScheduler.cancelDeadlineAlarms(context, opportunity.id)
                                Toast.makeText(context, "Offre retiree des favoris", Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (opportunity.isBookmarked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favoris",
                        tint = if (opportunity.isBookmarked) Error else OnSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // One-click Official Dossier PDF generator button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SecondaryContainer.copy(alpha = 0.5f))
                        .border(1.dp, Secondary.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .clickable { showPdfExportSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Dossier PDF",
                        tint = Secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Apply button
                val isAlreadyApplied = existingApp != null
                Button(
                    onClick = {
                        showDirectApplySheet = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAlreadyApplied) Secondary else Primary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (isAlreadyApplied) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAlreadyApplied) {
                            autoTranslate("Candidature déposée (Voir récépissé)")
                        } else {
                            autoTranslate("Postuler directement")
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }

    // Modal Bottom Sheets
    if (showDirectApplySheet) {
        DirectApplicationBottomSheet(
            opportunity = opportunity,
            onDismiss = { showDirectApplySheet = false },
            onOpenUploadDialog = { category ->
                selectedCategoryToUpload = category
                showUploadDocSheet = true
            }
        )
    }

    if (showUploadDocSheet) {
        SecureDocumentUploadBottomSheet(
            initialCategory = selectedCategoryToUpload,
            onDismiss = { showUploadDocSheet = false }
        )
    }

    if (showSalarySimulator) {
        SalarySimulatorBottomSheet(
            initialGrade = opportunity.degreeRequired,
            onDismiss = { showSalarySimulator = false }
        )
    }

    if (showMotivationLetter) {
        MotivationLetterBottomSheet(
            opportunityTitle = opportunity.title,
            organization = opportunity.organization,
            reference = opportunity.reference,
            candidateName = userProfile.fullName,
            degreeName = userProfile.degrees.firstOrNull()?.title ?: "Diplôme d'Ingénieur d'État",
            onDismiss = { showMotivationLetter = false }
        )
    }

    if (showChecklistDialog) {
        ConcoursChecklistDialog(
            onDismiss = { showChecklistDialog = false }
        )
    }

    if (showPdfExportSheet) {
        com.mihnati.app.ui.components.OfficialPdfExportBottomSheet(
            targetOpportunity = opportunity,
            onDismiss = { showPdfExportSheet = false }
        )
    }

    if (showCnieFinderSheet) {
        com.mihnati.app.ui.components.CnieConvocationFinderSheet(
            targetConcoursId = opportunity?.id,
            onDismiss = { showCnieFinderSheet = false }
        )
    }

    activePdfViewer?.let { (file, title) ->
        com.mihnati.app.ui.components.InAppPdfViewerDialog(
            pdfFile = file,
            title = title,
            onDismiss = { activePdfViewer = null }
        )
    }
}
