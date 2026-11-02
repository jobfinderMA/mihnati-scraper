package com.mihnati.app.data.model

enum class SectorType(val label: String, val emoji: String) {
    PUBLIC("Concours Publics", "🏛️"),
    PRIVE("Secteur Privé", "🏢"),
    FORMATION("Formations & Masters", "🎓")
}

enum class ApplicationStage(val label: String, val stepNumber: Int) {
    DEPOSE("Déposé", 1),
    REVUE("Présélection", 2),
    ECRIT("Écrit", 3),
    ENTRETIEN("Entretien", 4),
    RESULTAT("Résultat", 5)
}

data class MissionItem(
    val title: String,
    val description: String,
    val iconName: String = "shield"
)

data class RequiredDocument(
    val name: String,
    val detail: String,
    val isReady: Boolean = true
)

data class Opportunity(
    val id: String,
    val title: String,
    val organization: String,
    val location: String,
    val sectorType: SectorType,
    val reference: String,
    val deadline: String,
    val deadlineDaysRemaining: Int,
    val matchPercentage: Int,
    val degreeRequired: String,
    val field: String,
    val experience: String,
    val salary: String,
    val contractType: String = "CDI",
    val isUrgent: Boolean = false,
    val isNew: Boolean = false,
    val description: String,
    val missions: List<MissionItem> = emptyList(),
    val eligibleSchools: List<String> = emptyList(),
    val keySkills: List<String> = emptyList(),
    val requiredDocuments: List<RequiredDocument> = emptyList(),
    val workplaceName: String = "Campus d'excellence",
    val workplaceDetail: String = "Environnement technologique et collaboratif",
    val isBookmarked: Boolean = false,
    val isDemoData: Boolean = true,
    val sourceUrl: String = "https://www.emploi-public.ma"
)

data class Application(
    val id: String,
    val opportunityId: String,
    val title: String,
    val organization: String,
    val location: String,
    val sectorType: SectorType,
    val referenceNumber: String,
    val submittedDate: String,
    val currentStage: ApplicationStage,
    val statusHighlightTitle: String,
    val statusHighlightDetail: String,
    val isAdmitted: Boolean = false,
    val convocationReady: Boolean = false,
    val convocationDate: String? = null,
    val candidateNotes: String = "",
    val attachedDocuments: List<String> = emptyList(),
    val isDemoData: Boolean = true
)

enum class CalendarEventType(val label: String, val emoji: String) {
    CLOTURE_CONCOURS("Clôture Concours", "⏳"),
    ENTRETIEN("Entretien Oral", "🎙️"),
    EXAMEN_ECRIT("Épreuve Écrite", "📝"),
    RAPPEL_PERSO("Rappel Personnel", "⏰")
}

data class CalendarEvent(
    val id: String,
    val title: String,
    val organization: String,
    val date: String,
    val time: String,
    val daysRemaining: Int,
    val eventType: CalendarEventType,
    val location: String,
    val reference: String,
    val opportunityId: String? = null,
    val isDone: Boolean = false,
    val isDemoData: Boolean = true
)

enum class NotificationType {
    DEADLINE_URGENTE,
    CONVOCATION,
    NOUVELLE_OFFRE,
    STATUS_DOSSIER
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val targetOpportunityId: String? = null,
    val targetApplicationId: String? = null,
    val isDemoData: Boolean = true
)

data class Degree(
    val id: String,
    val title: String,
    val institution: String,
    val period: String,
    val mention: String,
    val isVerified: Boolean = true
)

enum class DocumentCategory(val label: String, val emoji: String) {
    CV("Curriculum Vitae", "📄"),
    CIN("Carte Nationale (CNIE)", "🪪"),
    DIPLOME("Diplôme / Équivalence", "🎓"),
    CASIER_JUDICIAIRE("Fiche Anthropométrique", "⚖️"),
    LETTRE_MOTIVATION("Lettre de Motivation", "📝"),
    AUTRE("Autre Pièce Officielle", "📁")
}

data class UserDocument(
    val id: String,
    val name: String,
    val detail: String = "",
    val status: String = "Conforme",
    val category: DocumentCategory = DocumentCategory.CV,
    val fileName: String = "document.pdf",
    val fileSizeKb: Int = 450,
    val uploadDate: String = "01 Nov 2026",
    val fileExtension: String = "pdf",
    val isVerified: Boolean = true,
    val sha256Fingerprint: String = "SHA256:8f4a...2026"
)

data class UserAccount(
    val email: String = "",
    val displayName: String = "Invité",
    val photoUrl: String? = null,
    val isSignedInWithGoogle: Boolean = false,
    val googleIdToken: String = "",
    val cnieNumber: String = "",
    val phoneNumber: String = "",
    val lastLoginDate: String = "Non connecté",
    val provider: String = "guest",
    val isVerified: Boolean = false
)

data class AlertPreferences(
    val publicSectorAlerts: Boolean = true,
    val privateSectorAlerts: Boolean = true,
    val deadlineReminders: Boolean = true,
    val universityPrograms: Boolean = false,
    val targetDegrees: List<String> = emptyList(),
    val targetRegions: List<String> = emptyList(),
    val minEchelle: Int = 0,
    val onlyFilteredPushNotifications: Boolean = false
) {
    fun matches(opportunity: Opportunity): Boolean {
        // 1. Degree filter
        if (targetDegrees.isNotEmpty()) {
            val oppDegree = opportunity.degreeRequired.lowercase()
            val matchesDegree = targetDegrees.any { target ->
                when (target) {
                    "Master" -> oppDegree.contains("master") || oppDegree.contains("bac+5") || oppDegree.contains("bac +5") || oppDegree.contains("ماستر") || oppDegree.contains("دراسات عليا")
                    "Ingénieur" -> oppDegree.contains("ingénieur") || oppDegree.contains("ingenieur") || oppDegree.contains("مهندس")
                    "Licence" -> oppDegree.contains("licence") || oppDegree.contains("bac+3") || oppDegree.contains("bac +3") || oppDegree.contains("إجازة") || oppDegree.contains("اجازة")
                    "Technicien" -> oppDegree.contains("technicien") || oppDegree.contains("dts") || oppDegree.contains("dut") || oppDegree.contains("bts") || oppDegree.contains("تقني")
                    "Bac" -> oppDegree.contains("baccalauréat") || oppDegree.contains("bac") || oppDegree.contains("باكالوريا")
                    "Doctorat" -> oppDegree.contains("doctorat") || oppDegree.contains("دكتوراه")
                    else -> oppDegree.contains(target.lowercase())
                }
            }
            if (!matchesDegree) return false
        }

        // 2. Region filter
        if (targetRegions.isNotEmpty()) {
            val oppLoc = opportunity.location.lowercase()
            val matchesRegion = targetRegions.any { reg ->
                oppLoc.contains(reg.lowercase()) || oppLoc.contains("maroc") || oppLoc.contains("national") || oppLoc.contains("rabat") || oppLoc.contains("المغرب")
            }
            if (!matchesRegion) return false
        }

        // 3. Echelle filter
        if (minEchelle > 0) {
            val oppEchelleNum = Regex("""\d+""").find(opportunity.salary)?.value?.toIntOrNull() ?: 10
            if (oppEchelleNum < minEchelle) return false
        }

        return true
    }
}

data class UserProfile(
    val fullName: String = "Invité",
    val headline: String = "Compte non connecté",
    val bio: String = "Créez votre compte pour postuler aux concours et enregistrer vos candidatures.",
    val location: String = "Maroc",
    val isVerified: Boolean = false,
    val isActivelySearching: Boolean = false,
    val completionPercentage: Int = 0,
    val targetLevel: String = "",
    val targetField: String = "",
    val degrees: List<Degree> = emptyList(),
    val skills: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val documents: List<UserDocument> = emptyList(),
    val preferences: AlertPreferences = AlertPreferences(),
    val account: UserAccount = UserAccount()
)

data class AiChatMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val timestamp: String = "Maintenant",
    val suggestedActions: List<String> = emptyList()
)

enum class WhatsAppApiProvider(val displayName: String) {
    WHATSAPP_DIRECT("Lien Direct WhatsApp (Sans configuration)"),
    META_CLOUD_API("Meta Cloud API (Officiel)"),
    TWILIO("Twilio WhatsApp Gateway")
}

data class WhatsAppConfig(
    val provider: WhatsAppApiProvider = WhatsAppApiProvider.WHATSAPP_DIRECT,
    val phoneNumber: String = "+212600000000",
    val apiKeyOrToken: String = "",
    val accountOrBusinessId: String = "",
    val isConfigured: Boolean = false
)
