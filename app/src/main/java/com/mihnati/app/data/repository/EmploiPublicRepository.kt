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

    private val initialConvocationRecords = listOf(
        CandidateConvocationRecord(
            id = "conv-mef-0142",
            concoursId = "mef-admin-2026",
            examListId = "list-oral-mef",
            cnie = "BE892415",
            fullName = "Bilal Test",
            administration = "Ministère de l'Économie et des Finances",
            grade = "Administrateur 2ème grade (Échelle 11)",
            echelle = 11,
            listType = ExamListType.ORAL,
            examDate = "25 Novembre 2026",
            examTime = "08:30 précises",
            examCenter = "Faculté des Sciences Juridiques et Économiques Rabat-Souissi",
            roomOrTable = "Amphi 3 • Table N° 142",
            convocationNumber = "MEF-ORAL-2026-0142",
            officialPdfUrl = "https://www.finances.gov.ma/Docs/Liste_Oral_Admin_2026.pdf",
            specialInstructions = "Présentation obligatoire de la CNIE originale et de la convocation imprimée."
        ),
        CandidateConvocationRecord(
            id = "conv-douane-0284",
            concoursId = "ep-douane-180",
            examListId = "list-ecrit-douane",
            cnie = "BE892415",
            fullName = "Bilal Test",
            administration = "Administration des Douanes et Impôts Indirects",
            grade = "Inspecteur des Douanes (Échelle 9)",
            echelle = 9,
            listType = ExamListType.ECRIT,
            examDate = "28 Novembre 2026",
            examTime = "08:00",
            examCenter = "Faculté de Droit - Route d'El Jadida Casablanca",
            roomOrTable = "Salle 12 • Table N° 284",
            convocationNumber = "ADII-ECRIT-2026-0284",
            officialPdfUrl = "https://www.douane.gov.ma/Docs/Liste_Ecrit_Inspecteurs_2026.pdf",
            specialInstructions = "Calculatrices non programmables autorisées. Fermeture des portes à 08h15."
        ),
        CandidateConvocationRecord(
            id = "conv-sante-0512",
            concoursId = "ep-sante-600",
            examListId = "list-ecrit-sante",
            cnie = "BE892415",
            fullName = "Bilal Test",
            administration = "Ministère de la Santé et de la Protection Sociale",
            grade = "Infirmier 1er grade (Échelle 10)",
            echelle = 10,
            listType = ExamListType.ECRIT,
            examDate = "15 Décembre 2026",
            examTime = "09:00",
            examCenter = "ISPITS - Institut Supérieur des Professions Infirmières Rabat",
            roomOrTable = "Grand Amphithéâtre • Table N° 512",
            convocationNumber = "MSPS-ECRIT-2026-0512",
            officialPdfUrl = "https://sante.gov.ma/Docs/Convocations_Infirmiers_2026.pdf"
        ),
        CandidateConvocationRecord(
            id = "conv-justice-0033",
            concoursId = "ep-justice-150",
            examListId = "list-attente-justice",
            cnie = "AA123456",
            fullName = "Bennani Fatima",
            administration = "Ministère de la Justice",
            grade = "Délégué Judiciaire (Échelle 11)",
            echelle = 11,
            listType = ExamListType.RESULTAT_FINAL,
            examDate = "Session Novembre 2026",
            examTime = "10:00",
            examCenter = "Complexe Judiciaire Rabat",
            roomOrTable = "Commission d'Admission N° 1",
            convocationNumber = "MJ-ADMIS-2026-0033",
            officialPdfUrl = "https://justice.gov.ma/Docs/Liste_Attente_Delegues_2026.pdf"
        ),
        CandidateConvocationRecord(
            id = "conv-interieur-0089",
            concoursId = "ep-interieur-450",
            examListId = "list-final-interieur",
            cnie = "BK789012",
            fullName = "Alami Mohamed",
            administration = "Ministère de l'Intérieur",
            grade = "Administrateur 2ème grade (Échelle 11)",
            echelle = 11,
            listType = ExamListType.ECRIT,
            examDate = "22 Novembre 2026",
            examTime = "08:30",
            examCenter = "Faculté des Sciences Kénitra",
            roomOrTable = "Amphi Ibn Battouta • Table N° 89",
            convocationNumber = "MI-ECRIT-2026-0089",
            officialPdfUrl = "https://interieur.gov.ma/Docs/Resultats_Definitifs_Admin_2026.pdf"
        ),
        CandidateConvocationRecord(
            id = "conv-mef-0099",
            concoursId = "ep-mef-120",
            examListId = "list-oral-mef",
            cnie = "CD345678",
            fullName = "Mansouri Yassine",
            administration = "Ministère de l'Économie et des Finances",
            grade = "Administrateur 2ème grade (Échelle 11)",
            echelle = 11,
            listType = ExamListType.ORAL,
            examDate = "26 Novembre 2026",
            examTime = "09:00",
            examCenter = "Siège Central du Ministère des Finances Rabat",
            roomOrTable = "Salle de Réunion N° 4",
            convocationNumber = "MEF-ORAL-2026-0099",
            officialPdfUrl = "https://www.finances.gov.ma/Docs/Liste_Oral_Admin_2026.pdf"
        )
    )

    private val _convocationRecords = MutableStateFlow(initialConvocationRecords)
    val convocationRecords: StateFlow<List<CandidateConvocationRecord>> = _convocationRecords.asStateFlow()

    fun searchCandidateConvocations(query: String, targetConcoursId: String? = null): List<CandidateConvocationRecord> {
        val cleanQuery = query.trim().lowercase().replace(" ", "")
        if (cleanQuery.isEmpty()) return emptyList()

        val allMatches = _convocationRecords.value.filter { record ->
            val matchCnie = record.cnie.lowercase().replace(" ", "").contains(cleanQuery)
            val matchName = record.fullName.lowercase().contains(query.trim().lowercase())
            matchCnie || matchName
        }

        if (targetConcoursId == null) return allMatches

        val targetMatches = allMatches.filter { record ->
            record.concoursId.equals(targetConcoursId, ignoreCase = true) ||
            targetConcoursId.contains(record.concoursId, ignoreCase = true) ||
            record.concoursId.contains(targetConcoursId, ignoreCase = true)
        }

        return if (targetMatches.isNotEmpty()) targetMatches else allMatches
    }
}
