package com.mihnati.app.data.repository

import com.mihnati.app.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object EmploiPublicRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _concours = MutableStateFlow<List<EmploiPublicConcours>>(EmploiPublicScraperService.getSeededMoroccanConcours())
    val concours: StateFlow<List<EmploiPublicConcours>> = _concours.asStateFlow()

    private val _examLists = MutableStateFlow<List<ConcoursExamList>>(EmploiPublicScraperService.getSeededExamLists())
    val examLists: StateFlow<List<ConcoursExamList>> = _examLists.asStateFlow()

    private val _grades = MutableStateFlow<List<EmploiPublicGrade>>(EmploiPublicScraperService.getGradesGuide())
    val grades: StateFlow<List<EmploiPublicGrade>> = _grades.asStateFlow()

    private val _syncStatus = MutableStateFlow(
        ScraperSyncStatus(
            isSyncing = false,
            lastSyncTimestamp = "Aujourd'hui à 11:30",
            totalConcoursCount = 8,
            totalListsCount = 5,
            isSuccess = true,
            message = "Données emploi-public.ma à jour"
        )
    )
    val syncStatus: StateFlow<ScraperSyncStatus> = _syncStatus.asStateFlow()

    init {
        // Automatically bring scraped data from the Cloud CDN on app startup
        triggerLiveSync { success, _ ->
            if (success) {
                MihnatiRepository.addNotification(
                    AppNotification(
                        id = "notif-auto-sync-" + System.currentTimeMillis(),
                        title = "Synchronisation Cloud ☁️",
                        message = "Concours officiels récents synchronisés automatiquement depuis le bot cloud.",
                        timeAgo = "À l'instant",
                        type = NotificationType.NOUVELLE_OFFRE
                    )
                )
            }
        }

        // Periodic background auto-sync every 15 minutes while app is running
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(15 * 60 * 1000L)
                triggerLiveSync()
            }
        }
    }

    fun initContext(context: android.content.Context) {
        EmploiPublicScraperService.initContext(context)
        triggerLiveSync()
    }

    fun triggerLiveSync(onComplete: ((Boolean, String) -> Unit)? = null) {
        if (_syncStatus.value.isSyncing) return

        scope.launch {
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = true,
                message = "Extraction des données depuis emploi-public.ma..."
            )

            try {
                val liveConcours = EmploiPublicScraperService.fetchLiveConcours()
                val liveLists = EmploiPublicScraperService.fetchLiveExamLists()

                _concours.value = liveConcours
                _examLists.value = liveLists

                // Inject scraped concours directly into MihnatiRepository's opportunities
                MihnatiRepository.mergeScrapedConcours(liveConcours)

                val currentTime = SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date())
                _syncStatus.value = ScraperSyncStatus(
                    isSyncing = false,
                    lastSyncTimestamp = "Aujourd'hui à $currentTime",
                    totalConcoursCount = liveConcours.size,
                    totalListsCount = liveLists.size,
                    isSuccess = true,
                    message = "Données emploi-public.ma synchronisées (${liveConcours.size} concours, ${liveLists.size} listes écrites/orales)"
                )

                // Dispatch callback safely on Main Thread to avoid Looper crash
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(true, "Données actualisées depuis emploi-public.ma (${liveConcours.size} concours)")
                }
            } catch (e: Exception) {
                _syncStatus.value = _syncStatus.value.copy(
                    isSyncing = false,
                    isSuccess = false,
                    message = "Données emploi-public.ma chargées depuis le cache officiel"
                )
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false, "Données chargées depuis le référentiel officiel")
                }
            }
        }
    }

    fun toggleSaveConcours(id: String) {
        _concours.update { list ->
            list.map { if (it.id == id) it.copy(isSaved = !it.isSaved) else it }
        }
    }

    fun importToApplications(concours: EmploiPublicConcours) {
        val newApp = Application(
            id = "app-${UUID.randomUUID().toString().take(8)}",
            opportunityId = concours.id,
            title = concours.titleFr,
            organization = concours.administrationFr,
            location = concours.location,
            sectorType = SectorType.PUBLIC,
            referenceNumber = concours.reference,
            submittedDate = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH).format(Date()),
            currentStage = ApplicationStage.DEPOSE,
            statusHighlightTitle = "Dossier transmis",
            statusHighlightDetail = "Concours ${concours.grade} (${concours.numPosts} postes)",
            candidateNotes = "Concours importé depuis emploi-public.ma. Diplôme requis : ${concours.diplomaRequired}."
        )
        MihnatiRepository.addApplication(newApp)
    }

    fun addExamToUserCalendar(exam: ConcoursExamList) {
        val event = CalendarEvent(
            id = "evt-${UUID.randomUUID().toString().take(8)}",
            title = "${exam.listType.labelFr} : ${exam.titleFr}",
            organization = exam.administration,
            date = exam.examDate,
            time = "08:30",
            daysRemaining = 5,
            eventType = if (exam.listType == ExamListType.ORAL) CalendarEventType.ENTRETIEN else CalendarEventType.EXAMEN_ECRIT,
            location = exam.examCenters.firstOrNull() ?: "Rabat",
            reference = exam.concoursId,
            opportunityId = exam.concoursId
        )
        MihnatiRepository.addCalendarEvent(event)
    }
}
