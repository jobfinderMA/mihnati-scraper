package com.mihnati.app.ui.screens

import android.content.Intent
import android.provider.CalendarContract
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.CalendarEvent
import com.mihnati.app.data.model.CalendarEventType
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.ui.components.DemoDataBanner
import com.mihnati.app.ui.components.TagBadge
import com.mihnati.app.ui.theme.*
import java.util.UUID

@Composable
fun CalendarScreen(
    onNavigateToOpportunity: (String) -> Unit
) {
    val context = LocalContext.current
    val events by MihnatiRepository.calendarEvents.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedMonth by remember { mutableStateOf("Novembre 2026") }
    var showAddEventDialog by remember { mutableStateOf(false) }

    // Dialog form state
    var newEventTitle by remember { mutableStateOf("") }
    var newEventOrg by remember { mutableStateOf("") }
    var newEventDate by remember { mutableStateOf("28 Nov 2026") }
    var newEventTime by remember { mutableStateOf("10:00") }
    var newEventLocation by remember { mutableStateOf("Casablanca") }
    var newEventType by remember { mutableStateOf(CalendarEventType.RAPPEL_PERSO) }

    val isAr = LanguageManager.isArabic
    val months = listOf(
        if (isAr) AppStrings.monthNov2026 else "Novembre 2026",
        if (isAr) AppStrings.monthDec2026 else "Décembre 2026",
        if (isAr) AppStrings.monthJan2027 else "Janvier 2027"
    )

    val filteredEvents = remember(events, selectedFilter, selectedMonth) {
        events.filter { event ->
            val monthMatches = when (selectedMonth) {
                "Novembre 2026", AppStrings.monthNov2026 -> event.date.contains("Nov", ignoreCase = true) || event.date.contains("نونبر", ignoreCase = true)
                "Décembre 2026", AppStrings.monthDec2026 -> event.date.contains("Déc", ignoreCase = true) || event.date.contains("Dec", ignoreCase = true) || event.date.contains("دجنبر", ignoreCase = true)
                "Janvier 2027", AppStrings.monthJan2027 -> event.date.contains("Jan", ignoreCase = true) || event.date.contains("يناير", ignoreCase = true)
                else -> true
            }

            val typeMatches = when (selectedFilter) {
                "DEADLINES" -> event.eventType == CalendarEventType.CLOTURE_CONCOURS
                "INTERVIEWS" -> event.eventType == CalendarEventType.ENTRETIEN || event.eventType == CalendarEventType.EXAMEN_ECRIT
                "REMINDERS" -> event.eventType == CalendarEventType.RAPPEL_PERSO
                else -> true
            }

            monthMatches && typeMatches
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Counter Summary
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            text = AppStrings.calendarTitle,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${events.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Add Custom Reminder Action Button
                    Button(
                        onClick = { showAddEventDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.newReminderBtn,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Text(
                    text = AppStrings.calendarSubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                )
            }
        }

        // 2. Month Selector Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                months.forEach { month ->
                    val isSelected = selectedMonth == month
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryContainer else Color.Transparent)
                            .clickable { selectedMonth = month }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = month,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) Color.White else OnSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // 3. Filter Category Chips
        item {
            val filterChips = listOf(
                "ALL" to "${AppStrings.calAll} (${events.size})",
                "DEADLINES" to AppStrings.calDeadlines,
                "INTERVIEWS" to AppStrings.calExams,
                "REMINDERS" to AppStrings.calReminders
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterChips) { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Primary else SurfaceContainerLowest)
                            .clickable { selectedFilter = key }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else OnSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // 4. Events List
        if (filteredEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EventAvailable,
                            contentDescription = null,
                            tint = Outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (LanguageManager.isArabic) "لا توجد مواعيد أو مباريات مبرمجة لهذه الفترة" else "Aucun événement prévu pour cette sélection",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = OnSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        } else {
            items(filteredEvents) { event ->
                CalendarEventCard(
                    event = event,
                    onToggleDone = { MihnatiRepository.toggleCalendarEventDone(event.id) },
                    onDelete = { MihnatiRepository.deleteCalendarEvent(event.id) },
                    onSyncWithDeviceCalendar = {
                        val calIntent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, event.title)
                            putExtra(CalendarContract.Events.DESCRIPTION, "Organisme: ${event.organization} (Réf: ${event.reference})")
                            putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                        }
                        try {
                            context.startActivity(calIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Agenda Android synchronisé !", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onOpenDetails = {
                        if (event.opportunityId != null) {
                            onNavigateToOpportunity(event.opportunityId)
                        } else {
                            Toast.makeText(context, "Rappel : ${event.title}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // Add Custom Reminder Dialog
    if (showAddEventDialog) {
        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = {
                Text(
                    text = if (LanguageManager.isArabic) "إضافة موعد أو تذكير خاص" else "Ajouter un événement / rappel",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newEventTitle,
                        onValueChange = { newEventTitle = it },
                        label = { Text(if (LanguageManager.isArabic) "عنوان الموعد" else "Titre de l'échéance") },
                        placeholder = { Text("Ex: Dépôt légalisation diplôme") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newEventOrg,
                        onValueChange = { newEventOrg = it },
                        label = { Text("Organisme ou Ministère") },
                        placeholder = { Text("Ex: Ministère ou Wilaya") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newEventDate,
                            onValueChange = { newEventDate = it },
                            label = { Text("Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newEventTime,
                            onValueChange = { newEventTime = it },
                            label = { Text("Heure") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = newEventLocation,
                        onValueChange = { newEventLocation = it },
                        label = { Text("Lieu") },
                        placeholder = { Text("Ex: Faculté ou En ligne") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEventTitle.isNotBlank()) {
                            val newEvent = CalendarEvent(
                                id = "cal-" + UUID.randomUUID().toString().take(8),
                                title = newEventTitle,
                                organization = if (newEventOrg.isNotBlank()) newEventOrg else "Personnel",
                                date = newEventDate,
                                time = newEventTime,
                                daysRemaining = 4,
                                eventType = newEventType,
                                location = newEventLocation,
                                reference = "PERSO-2026",
                                isDone = false,
                                isDemoData = false
                            )
                            MihnatiRepository.addCalendarEvent(newEvent)
                            Toast.makeText(context, "Rappel ajouté à votre calendrier Mihnati", Toast.LENGTH_SHORT).show()
                            showAddEventDialog = false
                            newEventTitle = ""
                            newEventOrg = ""
                        } else {
                            Toast.makeText(context, "Veuillez renseigner un titre", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun CalendarEventCard(
    event: CalendarEvent,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onSyncWithDeviceCalendar: () -> Unit,
    onOpenDetails: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceContainerLowest)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Countdown capsule + Event Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TagBadge(
                        text = if (event.daysRemaining <= 3) {
                            if (LanguageManager.isArabic) "عاجل • باقي ${event.daysRemaining} أيام" else "J-${event.daysRemaining} URGENT"
                        } else {
                            if (LanguageManager.isArabic) "باقي ${event.daysRemaining} أيام" else "J-${event.daysRemaining} restants"
                        },
                        backgroundColor = if (event.daysRemaining <= 3) ErrorContainer else TertiaryFixed,
                        textColor = if (event.daysRemaining <= 3) OnErrorContainer else OnTertiaryFixed,
                        icon = Icons.Default.Timer
                    )
                    TagBadge(
                        text = if (LanguageManager.isArabic) {
                            when (event.eventType) {
                                CalendarEventType.CLOTURE_CONCOURS -> "آجال الترشيح"
                                CalendarEventType.EXAMEN_ECRIT -> "الاختبار الكتابي"
                                CalendarEventType.ENTRETIEN -> "المقابلة الشفوية"
                                CalendarEventType.RAPPEL_PERSO -> "تذكير خاص"
                            }
                        } else event.eventType.label,
                        backgroundColor = SurfaceContainer,
                        textColor = OnSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Prepared / Done Checkbox
                    IconButton(
                        onClick = onToggleDone,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (event.isDone) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = "Validé",
                            tint = if (event.isDone) Secondary else Outline,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Supprimer",
                            tint = Outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Title & Organization
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = MoroccanLocalization.localizedTitle(event.title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        textDecoration = if (event.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                )
                Text(
                    text = MoroccanLocalization.localizedOrg(event.organization),
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                )
            }

            // Date, Time & Location Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLow)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${event.date} à ${event.time}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = MoroccanLocalization.localizedCity(event.location),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OnSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sync with device Calendar
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSyncWithDeviceCalendar() }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (LanguageManager.isArabic) "إضافة إلى تقويم الهاتف" else "Ajouter à l'agenda",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (event.opportunityId != null) {
                    TextButton(onClick = onOpenDetails) {
                        Text(
                            text = if (LanguageManager.isArabic) "عرض الإعلان الرسمي" else "Voir l'avis officiel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
