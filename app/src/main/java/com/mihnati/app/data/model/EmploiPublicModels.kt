package com.mihnati.app.data.model

enum class ExamListType(val labelFr: String, val labelAr: String, val badgeColorHex: Long) {
    ECRIT("Épreuve Écrite", "Épreuve Écrite", 0xFF0D53FC),
    ORAL("Épreuve Orale", "Épreuve Orale", 0xFF793F00),
    RESULTAT_FINAL("Résultats Définitifs", "Résultats Définitifs", 0xFF006C4A),
    LISTE_ATTENTE("Liste d'Attente", "Liste d'Attente", 0xFF5B6070)
}

enum class AdministrationCategory(val labelFr: String, val labelAr: String) {
    MINISTERE("Ministères", "Ministères"),
    ETABLISSEMENT_PUBLIC("Établissements & Entreprises Publics", "Établissements & Entreprises Publics"),
    COLLECTIVITE("Collectivités Territoriales", "Collectivités Territoriales")
}

data class EmploiPublicConcours(
    val id: String,
    val reference: String,
    val titleAr: String,
    val titleFr: String,
    val administrationAr: String,
    val administrationFr: String,
    val category: AdministrationCategory,
    val grade: String,
    val echelle: Int,
    val diplomaRequired: String,
    val specialties: List<String>,
    val numPosts: Int,
    val publishDate: String,
    val deadlineDate: String,
    val deadlineDaysRemaining: Int,
    val examDate: String? = null,
    val location: String = "National / Rabat",
    val avisPdfUrl: String = "https://www.emploi-public.ma",
    val detailsUrl: String = "https://www.emploi-public.ma/ar/concoursRecrutement.asp",
    val source: String = "emploi-public.ma",
    val isWeeklySpecial: Boolean = false,
    val isSaved: Boolean = false
)

data class ConcoursExamList(
    val id: String,
    val concoursId: String,
    val titleAr: String,
    val titleFr: String,
    val administration: String,
    val grade: String,
    val echelle: Int,
    val listType: ExamListType,
    val candidatesCount: Int,
    val examDate: String,
    val examCenters: List<String>,
    val pdfDownloadUrl: String,
    val publishDate: String,
    val source: String = "emploi-public.ma"
)

data class EmploiPublicGrade(
    val nameFr: String,
    val nameAr: String,
    val echelle: Int,
    val minDiploma: String,
    val startingSalary: String,
    val description: String
)

data class ScraperSyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: String = "Aujourd'hui à 11:30",
    val totalConcoursCount: Int = 18,
    val totalListsCount: Int = 12,
    val isSuccess: Boolean = true,
    val message: String = "Portail officiel emploi-public.ma synchronisé"
)

data class CandidateConvocationRecord(
    val id: String,
    val concoursId: String,
    val examListId: String,
    val cnie: String,
    val fullName: String,
    val administration: String,
    val grade: String,
    val echelle: Int,
    val listType: ExamListType,
    val examDate: String,
    val examTime: String,
    val examCenter: String,
    val roomOrTable: String,
    val convocationNumber: String,
    val officialPdfUrl: String,
    val specialInstructions: String = "Présentation obligatoire de la CNIE originale et de la convocation imprimée."
)
