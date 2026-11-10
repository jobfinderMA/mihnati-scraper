package com.mihnati.app.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.CandidateConvocationRecord
import com.mihnati.app.data.model.ExamListType
import com.mihnati.app.data.repository.EmploiPublicRepository
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.translation.autoTranslate
import com.mihnati.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CnieConvocationFinderSheet(
    targetConcoursId: String? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val userProfile by MihnatiRepository.userProfile.collectAsState()

    val profileCnie = remember(userProfile) {
        if (userProfile.account.cnieNumber.isNotBlank()) {
            userProfile.account.cnieNumber
        } else {
            "BE892415"
        }
    }

    var searchQuery by remember { mutableStateOf(profileCnie) }
    var searchResults by remember {
        mutableStateOf(EmploiPublicRepository.searchCandidateConvocations(profileCnie, targetConcoursId))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            .background(SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔍", fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = autoTranslate("Vérificateur de Convocations"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        Text(
                            text = autoTranslate("Recherche instantanée par CNIE ou Nom"),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = autoTranslate("Fermer"), tint = Outline)
                }
            }

            // Search Bar Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            searchResults = EmploiPublicRepository.searchCandidateConvocations(it, targetConcoursId)
                        },
                        placeholder = {
                            Text(
                                text = autoTranslate("Entrez votre N° CNIE ou Nom..."),
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
                        IconButton(onClick = {
                            searchQuery = ""
                            searchResults = emptyList()
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = autoTranslate("Effacer"), tint = Outline, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Quick CNIE Suggestion Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = autoTranslate("Exemples :"),
                    style = MaterialTheme.typography.labelSmall.copy(color = Outline)
                )

                listOf("BE892415", "BK789012", "AA123456").forEach { cnieSample ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh)
                            .clickable {
                                searchQuery = cnieSample
                                searchResults = EmploiPublicRepository.searchCandidateConvocations(cnieSample, targetConcoursId)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cnieSample,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        )
                    }
                }
            }

            // Results Section
            if (searchResults.isNotEmpty()) {
                // Found State
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SecondaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎉", fontSize = 18.sp)
                    Text(
                        text = "${searchResults.size} ${autoTranslate("convocation(s) officielle(s) trouvée(s) !")}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSecondaryContainer
                        )
                    )
                }

                val toastCalendarAdded = autoTranslate("Épreuve ajoutée à votre agenda Mihnati !")
                val shareSubject = autoTranslate("Convocation Officielle Mihnati")
                val shareChooserTitle = autoTranslate("Partager la convocation")

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(searchResults, key = { it.id }) { record ->
                        ConvocationSlipCard(
                            record = record,
                            onOpenPdf = {
                                openOfficialPdf(context, record.officialPdfUrl, record.grade)
                            },
                            onAddToCalendar = {
                                val examList = EmploiPublicRepository.examLists.value.find { it.id == record.examListId }
                                if (examList != null) {
                                    EmploiPublicRepository.addExamToUserCalendar(examList)
                                }
                                Toast.makeText(context, toastCalendarAdded, Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                val shareText = "${record.fullName} (${record.cnie})\n" +
                                        "Convoqué(e) au concours : ${record.administration}\n" +
                                        "Grade : ${record.grade}\n" +
                                        "Date : ${record.examDate} à ${record.examTime}\n" +
                                        "Centre : ${record.examCenter}\n" +
                                        "Salle/Table : ${record.roomOrTable}\n" +
                                        "N° Convocation : ${record.convocationNumber}"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
                            }
                        )
                    }
                }
            } else if (searchQuery.isNotBlank()) {
                // Not Found State
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 18.sp)
                            Text(
                                text = "${autoTranslate("Aucune convocation trouvée pour :")} $searchQuery",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                        }

                        Text(
                            text = autoTranslate("Conseils de vérification :"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Primary)
                        )
                        Text(
                            text = "• " + autoTranslate("Vérifiez la saisie de votre CNIE sans espaces (ex: BE892415)."),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                        Text(
                            text = "• " + autoTranslate("Les listes de certains ministères sont publiées par spécialité ou par région."),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                        Text(
                            text = "• " + autoTranslate("Vous pouvez consulter la liste PDF complète pour une vérification manuelle."),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )

                        Button(
                            onClick = {
                                openOfficialPdf(context, "https://www.emploi-public.ma", "Portail emploi-public.ma")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = autoTranslate("Consulter la liste PDF complète"),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            } else {
                // Initial Empty State
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🇲🇦", fontSize = 28.sp)
                        Text(
                            text = autoTranslate("Vérification Rapide des Convocations"),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                        )
                        Text(
                            text = autoTranslate("Saisissez votre numéro de CNIE ci-dessus pour vérifier instantanément si votre nom figure sur les listes officielles des épreuves écrites et orales."),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConvocationSlipCard(
    record: CandidateConvocationRecord,
    onOpenPdf: () -> Unit,
    onAddToCalendar: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Badge & Candidate info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(record.listType.badgeColorHex).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = autoTranslate(record.listType.labelFr),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(record.listType.badgeColorHex)
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "CNIE : ${record.cnie}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    )
                }
            }

            // Candidate Name & Ministry
            Text(
                text = "${autoTranslate("Candidat(e) :")} ${record.fullName}",
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
                    text = autoTranslate(record.administration),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Text(
                text = "${autoTranslate("Grade :")} ${autoTranslate(record.grade)}",
                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
            )

            // Details Slip Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHigh.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📅 " + autoTranslate("Date & Heure :") + " ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = autoTranslate("${record.examDate} - ${record.examTime}"),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📍 " + autoTranslate("Centre d'examen :") + " ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = autoTranslate(record.examCenter),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🏛️ " + autoTranslate("Salle / Table :") + " ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = autoTranslate(record.roomOrTable),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Secondary
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📋 " + autoTranslate("N° Convocation :") + " ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = record.convocationNumber,
                            style = MaterialTheme.typography.bodySmall.copy(color = Outline)
                        )
                    }

                    HorizontalDivider(color = SurfaceContainerHighest)

                    Text(
                        text = "⚠️ " + autoTranslate(record.specialInstructions),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Primary
                        )
                    )
                }
            }

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
                    Text(
                        text = autoTranslate("Voir Liste PDF"),
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

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = autoTranslate("Partager"), tint = Primary)
                }
            }
        }
    }
}
