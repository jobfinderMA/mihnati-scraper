package com.mihnati.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.mihnati.app.data.model.ConcoursExamList
import com.mihnati.app.data.model.ExamListType
import com.mihnati.app.data.model.Opportunity
import com.mihnati.app.data.repository.EmploiPublicRepository
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.translation.autoTranslate
import com.mihnati.app.ui.theme.*

fun openOfficialPdf(context: Context, url: String, label: String = "Avis Officiel") {
    val cleanUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        "https://www.emploi-public.ma"
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        Toast.makeText(context, "Ouverture du document PDF...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Impossible d'ouvrir le document", Toast.LENGTH_SHORT).show()
    }
}

fun shareOfficialPdf(context: Context, title: String, url: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n$url\nVia Mihnati - Portail de l'Emploi Public")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Partager le document officiel"))
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur de partage", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialConcoursPdfBottomSheet(
    targetOpportunity: Opportunity? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val examLists by EmploiPublicRepository.examLists.collectAsState()
    val allOpportunities by MihnatiRepository.opportunities.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Avis Officiel PDF, 1: Listes Candidats PDF
    var selectedExamFilter by remember { mutableStateOf<ExamListType?>(null) }

    val publicConcoursList = remember(allOpportunities) {
        allOpportunities.filter { it.sectorType == com.mihnati.app.data.model.SectorType.PUBLIC }
    }

    var selectedOpportunity by remember {
        mutableStateOf(targetOpportunity ?: publicConcoursList.firstOrNull())
    }

    val effectiveAvisUrl = remember(selectedOpportunity) {
        val opp = selectedOpportunity
        when {
            opp == null -> "https://www.emploi-public.ma"
            opp.avisPdfUrl.isNotBlank() -> opp.avisPdfUrl
            opp.sourceUrl.endsWith(".pdf", ignoreCase = true) -> opp.sourceUrl
            else -> "https://www.emploi-public.ma/upload/concours/${opp.reference.ifEmpty { "Avis_Officiel" }}.pdf"
        }
    }

    val filteredLists = remember(examLists, selectedOpportunity, selectedExamFilter) {
        examLists.filter { exam ->
            val matchFilter = selectedExamFilter == null || exam.listType == selectedExamFilter
            matchFilter
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏛️", fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = autoTranslate("Documents & Listes Officielles (PDF)"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        Text(
                            text = autoTranslate("Portail officiel de la Fonction Publique"),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = Outline)
                }
            }

            // Tab Selector: Option 1 (Avis PDF) vs Option 2 (Listes Candidats PDF)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHigh)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == 0) Primary else Color.Transparent)
                        .clickable { activeTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = if (activeTab == 0) Color.White else OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = autoTranslate("1. Avis de Concours"),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (activeTab == 0) Color.White else OnSurfaceVariant
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == 1) Primary else Color.Transparent)
                        .clickable { activeTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            tint = if (activeTab == 1) Color.White else OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = autoTranslate("2. Listes Candidats"),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (activeTab == 1) Color.White else OnSurfaceVariant
                            )
                        )
                    }
                }
            }

            if (activeTab == 0) {
                // TAB 1: Avis de Concours Officiel (PDF)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Official Decree Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Seal badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SecondaryContainer.copy(alpha = 0.6f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("🇲🇦", fontSize = 14.sp)
                                        Text(
                                            text = autoTranslate("Avis Officiel Certifié"),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = OnSecondaryContainer
                                            )
                                        )
                                    }

                                    Text(
                                        text = selectedOpportunity?.deadline ?: "",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Outline)
                                    )
                                }

                                Text(
                                    text = autoTranslate(selectedOpportunity?.title ?: "Concours de Recrutement"),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🏛️", fontSize = 14.sp)
                                    Text(
                                        text = autoTranslate(selectedOpportunity?.organization ?: "Ministère"),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                // Decree details box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceContainerHigh.copy(alpha = 0.5f))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = autoTranslate("Référence de l'arrêté :"),
                                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                                            )
                                            Text(
                                                text = selectedOpportunity?.reference ?: "CONCOURS-2026",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = autoTranslate("Postes ouverts :"),
                                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                                            )
                                            Text(
                                                text = "${selectedOpportunity?.postsCount ?: 1} ${autoTranslate("postes")}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Secondary
                                                )
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = autoTranslate("Cachet officiel :"),
                                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                                            )
                                            Text(
                                                text = autoTranslate("Signé par le Ministre"),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Primary
                                                )
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = autoTranslate("Le document PDF contient l'arrêté ministériel complet, le barème de notation, les dates des épreuves et les matières du concours."),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )

                                // Action Buttons
                                Button(
                                    onClick = {
                                        openOfficialPdf(context, effectiveAvisUrl, selectedOpportunity?.title ?: "Avis de Concours")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = autoTranslate("Consulter l'Avis Officiel (PDF)"),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        shareOfficialPdf(
                                            context,
                                            selectedOpportunity?.title ?: "Avis de Concours Officiel",
                                            effectiveAvisUrl
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(autoTranslate("Partager l'Avis de Concours"))
                                }
                            }
                        }
                    }

                    // Selector for other public concours if needed
                    if (publicConcoursList.size > 1) {
                        item {
                            Text(
                                text = autoTranslate("Autres Avis Officiels Disponibles :"),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        items(publicConcoursList.filter { it.id != selectedOpportunity?.id }) { opp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerLow)
                                    .clickable { selectedOpportunity = opp }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = autoTranslate(opp.title),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = autoTranslate(opp.organization),
                                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant),
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // TAB 2: Listes Officielles des Candidats (PDF)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Filter Chips: Tous, Écrit, Oral, Résultats
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val isAll = selectedExamFilter == null
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isAll) Primary else SurfaceContainerHigh)
                                    .clickable { selectedExamFilter = null }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = autoTranslate("Toutes les listes"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAll) Color.White else OnSurfaceVariant
                                    )
                                )
                            }
                        }

                        items(ExamListType.values()) { type ->
                            val isSelected = selectedExamFilter == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(type.badgeColorHex) else SurfaceContainerHigh)
                                    .clickable { selectedExamFilter = type }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = autoTranslate(type.labelFr),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else OnSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredLists, key = { it.id }) { exam ->
                            OfficialExamListCard(
                                exam = exam,
                                onOpenPdf = {
                                    openOfficialPdf(context, exam.pdfDownloadUrl, exam.titleFr)
                                },
                                onAddToCalendar = {
                                    EmploiPublicRepository.addExamToUserCalendar(exam)
                                    Toast.makeText(context, "Épreuve ajoutée au calendrier !", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfficialExamListCard(
    exam: ConcoursExamList,
    onOpenPdf: () -> Unit,
    onAddToCalendar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(exam.listType.badgeColorHex).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
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
                    text = "📋 ${exam.candidatesCount} ${autoTranslate("convoqués")}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Secondary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = autoTranslate(exam.titleFr),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🏛️", fontSize = 12.sp)
                Text(
                    text = autoTranslate(exam.administration),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Exam date & centers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerLow)
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${autoTranslate("Date d'épreuve :")} ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = exam.examDate,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                    if (exam.examCenters.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${autoTranslate("Centres :")} ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = exam.examCenters.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OnSurfaceVariant,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenPdf,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = autoTranslate("Télécharger Liste PDF"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = onAddToCalendar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = autoTranslate("Agenda"),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// Backward compatibility alias so existing calls work without breaking
@Composable
fun OfficialPdfExportBottomSheet(
    targetOpportunity: Opportunity? = null,
    onDismiss: () -> Unit
) {
    OfficialConcoursPdfBottomSheet(targetOpportunity, onDismiss)
}
