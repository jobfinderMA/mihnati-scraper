package com.mihnati.app.ui.theme

import androidx.compose.runtime.Composable
import com.mihnati.app.translation.autoTranslate

/**
 * Centralized Application Strings.
 * All strings are maintained in clean source language (French).
 * Automatic runtime translation to Arabic is handled on-device
 * by the Google ML Kit Translation engine.
 */
object AppStrings {
    // Navigation Tabs
    val navHome: String @Composable get() = autoTranslate("Accueil")
    val navOpportunities: String @Composable get() = autoTranslate("Opportunités")
    val navApplications: String @Composable get() = autoTranslate("Candidatures")
    val navCalendar: String @Composable get() = autoTranslate("Calendrier")
    val navProfile: String @Composable get() = autoTranslate("Profil")

    // TopBar & Brand
    val brandName: String get() = "Mihnati"
    val brandSubtitle: String @Composable get() = autoTranslate("Emploi Public")
    val liveSyncBadge: String @Composable get() = autoTranslate("Live Sync")
    val assistantIa: String @Composable get() = autoTranslate("Assistant IA")
    val languageToggleFr: String get() = "🇫🇷 FR"
    val languageToggleAr: String get() = "🇲🇦 AR"

    // Home Screen
    val welcomeTitle: String @Composable get() = autoTranslate("Bienvenue sur Mihnati 👋")
    val welcomeSubtitle: String @Composable get() = autoTranslate("Préparez votre avenir professionnel au Maroc")
    val createProfileBadge: String @Composable get() = autoTranslate("Créer un profil")
    val verifiedProfileBadge: String @Composable get() = autoTranslate("Profil Vérifié ✓")
    val searchPlaceholder: String @Composable get() = autoTranslate("Rechercher concours, poste, ministère...")
    
    // Tools Header & Cards
    val toolsHeader: String @Composable get() = autoTranslate("BOÎTE À OUTILS CONCOURS 2026")
    val toolsCount: String @Composable get() = autoTranslate("5 outils")
    val salarySimulator: String @Composable get() = autoTranslate("Simulateur Salaire")
    val salarySubtitle: String @Composable get() = autoTranslate("Échelles 8 à 11")
    val contestFile: String @Composable get() = autoTranslate("Dossier Concours")
    val contestFileSubtitle: String @Composable get() = autoTranslate("Pièces & CNIE")
    val coverLetter: String @Composable get() = autoTranslate("Lettre Motivation")
    val coverLetterSubtitle: String @Composable get() = autoTranslate("Modèle officiel")
    val diplomaChecker: String @Composable get() = autoTranslate("Vérif. Diplôme")
    val diplomaCheckerSubtitle: String @Composable get() = autoTranslate("Échelle & Corps")
    val oralPrep: String @Composable get() = autoTranslate("Prépa Entretien")
    val oralPrepSubtitle: String @Composable get() = autoTranslate("Questions commissions")

    // Observatory Stats
    val observatoryTitle: String @Composable get() = autoTranslate("OBSERVATOIRE DE L'EMPLOI PUBLIC 2026")
    val liveStatus: String @Composable get() = autoTranslate("● En direct")
    val statConcours: String @Composable get() = autoTranslate("Concours ouverts")
    val statPositions: String @Composable get() = autoTranslate("Postes État")
    val statRegions: String @Composable get() = autoTranslate("Régions")
    val statVerified: String @Composable get() = autoTranslate("Officiel & Vérifié")

    // Category Tabs
    val categoryAll: String @Composable get() = autoTranslate("Tous")
    val categoryPublic: String @Composable get() = autoTranslate("Concours Publics")
    val categoryTech: String @Composable get() = autoTranslate("Tech & Digital")
    val categoryFinance: String @Composable get() = autoTranslate("Banque & Finance")
    val categoryMasters: String @Composable get() = autoTranslate("Masters & Formations")
    val categoryHealth: String @Composable get() = autoTranslate("Santé")
    val categoryCustomAlerts: String @Composable get() = autoTranslate("Mes alertes ciblées")

    // Cards & Opportunities
    val expiresSoon: String @Composable get() = autoTranslate("Expire bientôt")
    val expiresPrefix: String @Composable get() = autoTranslate("Expire dans")
    val viewOffer: String @Composable get() = autoTranslate("Voir l'offre →")
    val imminentDeadlines: String @Composable get() = autoTranslate("Délais imminents")
    val viewCalendar: String @Composable get() = autoTranslate("Calendrier ➔")
    val daysRemaining: String @Composable get() = autoTranslate("j restants")
    val deadlineClosingPrefix: String @Composable get() = autoTranslate("Clôture :")
    val onlineSubmission: String @Composable get() = autoTranslate("Dépôt en ligne")
    val directApply: String @Composable get() = autoTranslate("Postuler directement")
    val recommendedForYou: String @Composable get() = autoTranslate("Opportunités pour vous")
    val recommendedSubtitle: String @Composable get() = autoTranslate("Sélectionnées selon votre profil")
    val viewAll: String @Composable get() = autoTranslate("Voir tout")
    val noAlertsFoundTitle: String @Composable get() = autoTranslate("Aucun concours ne correspond à vos filtres ciblés")
    val noAlertsFoundDesc: String @Composable get() = autoTranslate("Personnalisez vos diplômes et régions dans l'onglet Profil pour filtrer vos offres.")
    val editFiltersBtn: String @Composable get() = autoTranslate("Modifier mes filtres ➔")

    // Clean Opportunity Card
    val publicSectorScale11: String @Composable get() = autoTranslate("Grille Fonction Publique • Échelle 11")
    val publicSectorScale10: String @Composable get() = autoTranslate("Grille Fonction Publique • Échelle 10")
    val publicSectorScale9: String @Composable get() = autoTranslate("Grille Fonction Publique • Échelle 9")
    val publicSectorStatus: String @Composable get() = autoTranslate("Statut Fonction Publique (Selon arrêté)")
    val salaryAccordingProfile: String @Composable get() = autoTranslate("Rémunération selon profil")
    val estimateSalaryAction: String @Composable get() = autoTranslate("Estimer ➔")
    val matchWord: String @Composable get() = autoTranslate("match")

    // Opportunities Screen
    val searchOpportunitiesPlaceholder: String @Composable get() = autoTranslate("Poste, ministère, ville, Échelle 11...")
    val clearAction: String @Composable get() = autoTranslate("Effacer")
    val tabAll: String @Composable get() = autoTranslate("Tous")
    val tabFavorites: String @Composable get() = autoTranslate("Favoris")
    val tabPublicSector: String @Composable get() = autoTranslate("Concours Publics")
    val tabPrivateSector: String @Composable get() = autoTranslate("Secteur Privé")
    val tabMastersTrainings: String @Composable get() = autoTranslate("Masters & Stages")
    val levelAll: String @Composable get() = autoTranslate("Tous niveaux")
    val levelBac2: String @Composable get() = autoTranslate("Bac+2 / DUT / BTS")
    val levelBac3: String @Composable get() = autoTranslate("Bac+3 / Licence")
    val levelBac5: String @Composable get() = autoTranslate("Bac+5 / Master / Ingénieur")
    val levelDoctorate: String @Composable get() = autoTranslate("Doctorat / Ph.D")
    val sortDeadlineNear: String @Composable get() = autoTranslate("Échéance proche")
    val sortProfileMatch: String @Composable get() = autoTranslate("Pertinence profil")
    val opportunitiesFound: String @Composable get() = autoTranslate("opportunités trouvées")
    val noOffersFound: String @Composable get() = autoTranslate("Aucune offre trouvée")
    val modifySearchCriteria: String @Composable get() = autoTranslate("Essayez de modifier vos critères de recherche ou réinitialisez les filtres.")
    val resetFiltersBtn: String @Composable get() = autoTranslate("Réinitialiser les filtres")

    // Applications Screen
    val myApplicationsTitle: String @Composable get() = autoTranslate("Mes Candidatures & Concours")
    val refreshAction: String @Composable get() = autoTranslate("Actualiser")
    val activeFilesCount: String @Composable get() = autoTranslate("dossiers actifs")
    val finalizedSuccess: String @Composable get() = autoTranslate("finalisé avec succès")
    val tabMyFiles: String @Composable get() = autoTranslate("📂 Mes Dossiers")
    val tabConvocations: String @Composable get() = autoTranslate("📋 Listes Écrit & Oral")
    val filterAll: String @Composable get() = autoTranslate("Toutes")
    val filterPending: String @Composable get() = autoTranslate("En attente")
    val filterExam: String @Composable get() = autoTranslate("Écrit / Concours")
    val filterInterview: String @Composable get() = autoTranslate("Entretien")
    val filterResult: String @Composable get() = autoTranslate("Résultat")
    val stageSubmitted: String @Composable get() = autoTranslate("Déposé")
    val stageReview: String @Composable get() = autoTranslate("Revue")
    val stageWritten: String @Composable get() = autoTranslate("Écrit")
    val stageOral: String @Composable get() = autoTranslate("Entretien")
    val stageAdmitted: String @Composable get() = autoTranslate("Résultat")
    val addPersonalNote: String @Composable get() = autoTranslate("+ Note")
    val downloadConvocation: String @Composable get() = autoTranslate("Télécharger Convocation")
    val noApplicationsTitle: String @Composable get() = autoTranslate("Aucune candidature enregistrée")
    val noApplicationsDesc: String @Composable get() = autoTranslate("Explorez les concours et déposez votre dossier pour suivre les étapes ici.")

    // Calendar Screen
    val calendarTitle: String @Composable get() = autoTranslate("Mon Calendrier des Concours")
    val newReminderBtn: String @Composable get() = autoTranslate("Nouveau rappel")
    val calendarSubtitle: String @Composable get() = autoTranslate("Suivez les clôtures de concours, épreuves écrites et entretiens RH.")
    val monthNov2026: String @Composable get() = autoTranslate("Novembre 2026")
    val monthDec2026: String @Composable get() = autoTranslate("Décembre 2026")
    val monthJan2027: String @Composable get() = autoTranslate("Janvier 2027")
    val calAll: String @Composable get() = autoTranslate("Tous")
    val calDeadlines: String @Composable get() = autoTranslate("⏳ Délais Concours")
    val calExams: String @Composable get() = autoTranslate("🎙️ Écrits & Oraux")
    val calReminders: String @Composable get() = autoTranslate("⏰ Mes Rappels")
    val addCalendarDialogTitle: String @Composable get() = autoTranslate("Ajouter un événement au calendrier")
    val eventTitleLabel: String @Composable get() = autoTranslate("Titre de l'événement")
    val eventOrgLabel: String @Composable get() = autoTranslate("Ministère ou Organisme")
    val eventDateLabel: String @Composable get() = autoTranslate("Date")
    val eventTimeLabel: String @Composable get() = autoTranslate("Heure")
    val eventLocationLabel: String @Composable get() = autoTranslate("Lieu / Ville")
    val saveAction: String @Composable get() = autoTranslate("Enregistrer")
    val cancelAction: String @Composable get() = autoTranslate("Annuler")
    val closeAction: String @Composable get() = autoTranslate("Fermer")

    // Profile Screen
    val myAccount: String @Composable get() = autoTranslate("Compte Candidat")
    val guestMode: String @Composable get() = autoTranslate("Mode Invité • Non connecté")
    val guestName: String @Composable get() = autoTranslate("Invité")
    val guestBio: String @Composable get() = autoTranslate("Créez votre compte gratuit pour postuler aux concours et synchroniser votre dossier.")
    val activelySearching: String @Composable get() = autoTranslate("En recherche active")
    val passiveSearching: String @Composable get() = autoTranslate("En veille d'opportunités")
    val editProfile: String @Composable get() = autoTranslate("Modifier mon profil")
    val notConnected: String @Composable get() = autoTranslate("Non connecté")
    val connected: String @Composable get() = autoTranslate("Connecté ✓")
    val loginButton: String @Composable get() = autoTranslate("Connexion")
    val registerButton: String @Composable get() = autoTranslate("Créer compte")
    val manageAccount: String @Composable get() = autoTranslate("Gérer")
    val accountMihnati: String @Composable get() = autoTranslate("Compte Mihnati")
    val accountGoogle: String @Composable get() = autoTranslate("Compte Google")
    val syncAccountHelp: String @Composable get() = autoTranslate("Créez votre compte pour postuler et synchroniser vos candidatures")

    // Profile Sections
    val myDossier: String @Composable get() = autoTranslate("Dossier de candidature")
    val degreesHeader: String @Composable get() = autoTranslate("Diplômes & Formations")
    val addDegree: String @Composable get() = autoTranslate("+ Ajouter un diplôme ou certificat")
    val skillsHeader: String @Composable get() = autoTranslate("Spécialités & Compétences")
    val docVaultHeader: String @Composable get() = autoTranslate("Porte-documents Sécurisé & Vérifié")
    
    // Personalized Alert Filters
    val alertFiltersTitle: String @Composable get() = autoTranslate("Filtres d'alertes personnalisées 🎯")
    val alertFiltersSubtitle: String @Composable get() = autoTranslate("Ciblez vos diplômes et régions pour recevoir uniquement les concours pertinents")
    val targetDegreesLabel: String @Composable get() = autoTranslate("Diplômes & Niveaux ciblés :")
    val targetRegionsLabel: String @Composable get() = autoTranslate("Régions ciblées au Maroc :")
    val minScaleLabel: String @Composable get() = autoTranslate("Niveau hiérarchique minimum :")
    val strictAlertsSwitch: String @Composable get() = autoTranslate("Filtrage strict des notifications")
    val strictAlertsSubtitle: String @Composable get() = autoTranslate("Envoyer des alertes Push & WhatsApp UNIQUEMENT pour les concours correspondants")

    // WhatsApp Gateway Section
    val waGatewayHeader: String @Composable get() = autoTranslate("Passerelle WhatsApp & Convocations")
    val waGatewaySubtitle: String @Composable get() = autoTranslate("Envoi instantané des listes Écrit/Oral & Admis avec PDF")
    val waLinkedNumber: String @Composable get() = autoTranslate("Numéro WhatsApp lié :")
    val waConfigureButton: String @Composable get() = autoTranslate("Configurer la passerelle Green-API")
    val waConnectedBadge: String @Composable get() = autoTranslate("✓ Connectée")
    val waDefaultBadge: String @Composable get() = autoTranslate("Passerelle par défaut")

    // Settings
    val darkModeHeader: String @Composable get() = autoTranslate("Apparence & Mode Sombre")
    val darkModeTitle: String @Composable get() = autoTranslate("Mode Sombre Royal 🌙")
    val darkModeSubtitle: String @Composable get() = autoTranslate("Thème noir profond émeraude pour le confort visuel")
    val guidesHeader: String @Composable get() = autoTranslate("Guide des concours marocains")
    val logoutButton: String @Composable get() = autoTranslate("Se Déconnecter")

    // Moroccan Career Tools Sheets
    val salarySimulatorTitle: String @Composable get() = autoTranslate("Simulateur de Salaire Public")
    val salarySimulatorOfficialGrid: String @Composable get() = autoTranslate("Grille indiciaire officielle du Royaume du Maroc")
    val chooseScaleLabel: String @Composable get() = autoTranslate("CHOIX DE L'ÉCHELLE ADMINISTRATIVE")
    val ministryBonusLabel: String @Composable get() = autoTranslate("Primes spécifiques de ministère")
    val ministryBonusSub: String @Composable get() = autoTranslate("Ex: Finances (MEF), Impôts (DGI), Douanes")
    val netSalaryEstimated: String @Composable get() = autoTranslate("SALAIRE NET MENSUEL ESTIMÉ")
    val brutTreatment: String @Composable get() = autoTranslate("Traitement brut indiciaire :")
    val socialDeductions: String @Composable get() = autoTranslate("Retenues sociales (CMR & AMO) :")
    val ministryBonusAmount: String @Composable get() = autoTranslate("Indemnité de rendement / primes :")
    val salaryDisclaimer: String @Composable get() = autoTranslate("Estimation indicative basée sur le statut des administrateurs et techniciens de l'État et l'accord de revalorisation salariale.")

    val checklistTitle: String @Composable get() = autoTranslate("Dossier de Candidature")
    val readyDocsCount: String @Composable get() = autoTranslate("Pièces prêtes :")

    val motivationLetterTitle: String @Composable get() = autoTranslate("Lettre de Motivation Pré-remplie")
    val motivationLetterSubtitle: String @Composable get() = autoTranslate("Adaptée à votre profil et au concours officiel")
    val copyLetterAction: String @Composable get() = autoTranslate("Copier la lettre de motivation")
    val letterCopiedToast: String @Composable get() = autoTranslate("Lettre copiée dans le presse-papiers !")

    val eligibilityCheckerTitle: String @Composable get() = autoTranslate("Vérificateur d'Éligibilité")
    val eligibilityCheckerSubtitle: String @Composable get() = autoTranslate("Correspondance officielle Diplôme ➔ Échelle de l'État")
    val chooseDiplomaLabel: String @Composable get() = autoTranslate("CHOISISSEZ VOTRE DIPLÔME ACTUEL :")
    val publicAccessGrade: String @Composable get() = autoTranslate("GRADE D'ACCÈS DE LA FONCTION PUBLIQUE")

    val oralExamTitle: String @Composable get() = autoTranslate("Préparation à l'Oral du Concours")
    val oralExamSubtitle: String @Composable get() = autoTranslate("Questions récurrentes posées par les jurys ministériels")
}
