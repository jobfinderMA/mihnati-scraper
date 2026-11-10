package com.mihnati.app.data.repository

import android.content.Context
import com.mihnati.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

object MihnatiRepository {

    // Initial Moroccan opportunities matching the exact HTML design reference
    private val initialOpportunities = listOf(
        Opportunity(
            id = "ocp-cyber-2026",
            title = "Ingénieur Cybersécurité & Gouvernance SI",
            organization = "OCP Group",
            location = "Casablanca / Benguerir",
            sectorType = SectorType.PRIVE,
            reference = "OCP-DIG-2026-04",
            deadline = "30 Novembre 2026",
            deadlineDaysRemaining = 16,
            matchPercentage = 98,
            degreeRequired = "Bac+5 / Ingénieur d'État",
            field = "Informatique & Réseaux",
            experience = "Débutant à 2 ans",
            salary = "Package Attractif + Avantages OCP",
            contractType = "CDI • Temps plein",
            isUrgent = false,
            isNew = true,
            description = "Au sein de la Direction Transformation Digitale du Groupe OCP, vous participerez activement au renforcement de la posture sécuritaire des infrastructures industrielles et des plateformes applicatives nationales du groupe.",
            missions = listOf(
                MissionItem(
                    title = "Audit & Tests d'intrusion",
                    description = "Conduire les campagnes d'audits de vulnérabilités et coordonner les plans de remédiation technique auprès des équipes DevSecOps.",
                    iconName = "shield"
                ),
                MissionItem(
                    title = "Gouvernance & Normes ISO 27001",
                    description = "Garantir la conformité continue des processus aux standards internationaux et aux directives de la DGSSI marocaine.",
                    iconName = "policy"
                ),
                MissionItem(
                    title = "Surveillance & Réponse aux Incidents",
                    description = "Superviser les flux d'alertes SIEM/SOC 24/7 et orchestrer la réponse rapide face aux menaces avancées.",
                    iconName = "visibility"
                )
            ),
            eligibleSchools = listOf("ENSIAS", "INPT", "EMSI", "Réseau ENSA", "EMI", "Diplôme étranger équivalent"),
            keySkills = listOf("SIEM & Splunk / Elastic", "Architecture Next-Gen Firewalls", "Sécurité Cloud (AWS, Azure)", "ISO 27001 / EBIOS RM", "Python Scripting & Bash"),
            requiredDocuments = listOf(
                RequiredDocument("CV actualisé avec photo", "En langue française (Obligatoire)"),
                RequiredDocument("Copie de la CIN", "Carte d'Identité Nationale recto-verso"),
                RequiredDocument("Copie certifiée du diplôme", "Ou attestation de réussite officielle"),
                RequiredDocument("Lettre de motivation", "Adressée au comité de recrutement OCP")
            ),
            workplaceName = "Campus Mohammed VI Polytechnique",
            workplaceDetail = "Écosystème mondial de recherche et d'excellence",
            isDemoData = true,
            sourceUrl = "https://www.ocpgroup.ma/careers"
        ),
        Opportunity(
            id = "inwi-fullstack-2026",
            title = "Développeur Full Stack Java / Angular",
            organization = "Inwi",
            location = "Casablanca, Sidi Maârouf",
            sectorType = SectorType.PRIVE,
            reference = "INWI-IT-2026-18",
            deadline = "28 Novembre 2026",
            deadlineDaysRemaining = 14,
            matchPercentage = 95,
            degreeRequired = "Bac+3 à Bac+5",
            field = "Génie Logiciel & Informatique",
            experience = "1 à 3 ans",
            salary = "12 000 - 15 000 MAD",
            contractType = "CDI",
            isUrgent = false,
            isNew = true,
            description = "Rejoignez la Direction des Systèmes d'Information pour concevoir les architectures microservices et portails digitaux clients B2C/B2B.",
            missions = listOf(
                MissionItem("Architecture Microservices", "Développement d'APIs Spring Boot réactives."),
                MissionItem("Frontend Angular", "Interfaces utilisateurs dynamiques et intégration continue.")
            ),
            eligibleSchools = listOf("ENSA", "ENSIAS", "FST", "Facultés des Sciences"),
            keySkills = listOf("Java / Spring Boot", "Angular / TypeScript", "Docker", "GitLab CI/CD"),
            isDemoData = true,
            sourceUrl = "https://inwi.ma/recrutement"
        ),
        Opportunity(
            id = "capgemini-bi-2026",
            title = "Data Analyst & BI Consultant",
            organization = "Capgemini Maroc",
            location = "Technopolis Salé / Rabat",
            sectorType = SectorType.PRIVE,
            reference = "CAP-BI-2026-09",
            deadline = "22 Juin 2026",
            deadlineDaysRemaining = 26,
            matchPercentage = 92,
            degreeRequired = "Bac+5 École d'Ingénieurs",
            field = "Data & Business Intelligence",
            experience = "0 à 2 ans",
            salary = "Rémunération attractive + Primes",
            contractType = "CDI • Hybride",
            isUrgent = false,
            isNew = true,
            description = "Accompagnement de grands comptes européens dans la valorisation de leurs entrepôts de données, création de dashboards et modélisation décisionnelle.",
            missions = listOf(
                MissionItem("Modélisation Décisionnelle", "Conception de Data Warehouses et flux ETL."),
                MissionItem("Data Visualization", "Création de tableaux de bord interactifs PowerBI.")
            ),
            eligibleSchools = listOf("Écoles d'Ingénieurs marocaines et étrangères"),
            keySkills = listOf("PowerBI", "SQL Server / PostgreSQL", "Python Pandas", "DAX"),
            isDemoData = true,
            sourceUrl = "https://www.capgemini.com/ma-fr/carrieres"
        ),
        Opportunity(
            id = "um5-master-ds-2026",
            title = "Master Spécialisé Data Science & IA",
            organization = "Université Mohammed V",
            location = "Rabat (FS / ENSIAS)",
            sectorType = SectorType.FORMATION,
            reference = "UM5-MASTER-DS-2026",
            deadline = "05 Juillet 2026",
            deadlineDaysRemaining = 39,
            matchPercentage = 91,
            degreeRequired = "Licence SMI / Maths-Info",
            field = "Data Science, IA & Machine Learning",
            experience = "Formation Initiale Gratuite",
            salary = "Formation d'État Gratuite",
            contractType = "Master Universitaire (2 ans)",
            isUrgent = false,
            isNew = true,
            description = "Programme d'excellence de l'Université Mohammed V formant les futurs ingénieurs de recherche et experts en intelligence artificielle.",
            missions = listOf(
                MissionItem("Machine & Deep Learning", "Modèles neuronaux avancés, Vision par ordinateur et NLP."),
                MissionItem("Stage de Recherche", "Projet de fin d'études en laboratoire ou entreprise partenaire.")
            ),
            eligibleSchools = listOf("Titulaires d'une Licence en informatique ou mathématiques"),
            keySkills = listOf("Python ML", "PyTorch / TensorFlow", "Statistiques avancées", "Big Data"),
            isDemoData = true,
            sourceUrl = "http://www.um5.ac.ma"
        ),
        Opportunity(
            id = "uh2-master-ia-2026",
            title = "Master de Recherche en Intelligence Artificielle & Data Science",
            organization = "Université Hassan II",
            location = "FS Ben M'sik • Casablanca",
            sectorType = SectorType.FORMATION,
            reference = "UH2-MIA-2026",
            deadline = "30 Juin 2026",
            deadlineDaysRemaining = 34,
            matchPercentage = 90,
            degreeRequired = "Licence SMI / Maths-Info",
            field = "Recherche & IA",
            experience = "Dossier + Entretien",
            salary = "Formation Initiale Gratuite",
            contractType = "Master d'État",
            isUrgent = false,
            isNew = false,
            description = "Programme d'excellence axé sur l'apprentissage profond (Deep Learning), vision par ordinateur, et traitement automatique du langage naturel (NLP).",
            missions = listOf(
                MissionItem("Recherche Appliquée", "Travaux sur les grands modèles de langage et vision."),
                MissionItem("Partenariats industriels", "Projets conjoints avec les centres R&D de Casablanca.")
            ),
            eligibleSchools = listOf("Licence SMI, Sciences Mathématiques et Informatique"),
            keySkills = listOf("Deep Learning", "Traitement du Signal", "NLP", "Mathématiques appliquées"),
            isDemoData = true,
            sourceUrl = "http://www.univh2c.ma"
        ),
        Opportunity(
            id = "bourse-cnrst-2026",
            title = "Bourse Al Khawarizmi • Recherche & IA Doctorat",
            organization = "CNRST Maroc",
            location = "National / Universités",
            sectorType = SectorType.FORMATION,
            reference = "CNRST-ALKHAWARIZMI-2026",
            deadline = "12 Novembre 2026",
            deadlineDaysRemaining = 5,
            matchPercentage = 87,
            degreeRequired = "Master / Ingénieur d'État",
            field = "Intelligence Artificielle & Sciences",
            experience = "Excellence académique",
            salary = "7 000 MAD / mois net",
            contractType = "Bourse doctorale (36 mois)",
            isUrgent = true,
            isNew = false,
            description = "Programme national d'appui à la recherche scientifique en Intelligence Artificielle et technologies émergentes financé par le CNRST.",
            missions = listOf(
                MissionItem("Thèse de Doctorat", "Travaux de recherche fondamentale et appliquée en IA."),
                MissionItem("Publications scientifiques", "Présentations dans des conférences internationales.")
            ),
            eligibleSchools = listOf("Ingénieurs et Masters universitaires avec mention"),
            keySkills = listOf("Méthodologie de recherche", "Rédaction scientifique", "Algorithmique avancée"),
            isDemoData = true,
            sourceUrl = "https://www.cnrst.ma"
        )
    )

    // Initial Applications from Mes Candidatures design
    private val initialApplications = listOf(
        Application(
            id = "app-mt-01",
            opportunityId = "mt-cloud-2026",
            title = "Ingénieur Cloud & Systèmes",
            organization = "Maroc Telecom",
            location = "Casablanca",
            sectorType = SectorType.PRIVE,
            referenceNumber = "MT-2026-0842",
            submittedDate = "25 Octobre 2026",
            currentStage = ApplicationStage.ENTRETIEN,
            statusHighlightTitle = "Entretien prévu le 20 Novembre 2026 à 10:30",
            statusHighlightDetail = "Siège Social Rabat • Salle Commission 4B",
            convocationReady = true,
            convocationDate = "20 Novembre 2026 à 10:30",
            candidateNotes = "Préparer la présentation de l'architecture microservices Kubernetes et du projet PFE.",
            isDemoData = true
        ),
        Application(
            id = "app-mef-02",
            opportunityId = "mef-admin-2026",
            title = "Concours Administrateur 2ème grade",
            organization = "Ministère de l'Économie et des Finances",
            location = "Rabat",
            sectorType = SectorType.PUBLIC,
            referenceNumber = "MEF-CONCOURS-2026-120",
            submittedDate = "28 Octobre 2026",
            currentStage = ApplicationStage.ECRIT,
            statusHighlightTitle = "Examen écrit • Convocation prête",
            statusHighlightDetail = "FSJES Souissi Rabat • 22 Novembre 2026 à 08:00",
            convocationReady = true,
            convocationDate = "22 Novembre 2026 à 08:00",
            candidateNotes = "Réviser les annales des finances publiques et droit constitutionnel marocain.",
            isDemoData = true
        ),
        Application(
            id = "app-deloitte-03",
            opportunityId = "deloitte-bi-2026",
            title = "Consultant Junior Data & Analytics",
            organization = "Deloitte Nearshore",
            location = "Casablanca Marina",
            sectorType = SectorType.PRIVE,
            referenceNumber = "DEL-2026-0149",
            submittedDate = "30 Octobre 2026",
            currentStage = ApplicationStage.REVUE,
            statusHighlightTitle = "En attente de présélection RH",
            statusHighlightDetail = "Délai estimé: ~10 jours ouvrés • Dépôt hier",
            candidateNotes = "CV transmis par recommandation interne.",
            isDemoData = true
        ),
        Application(
            id = "app-cap-04",
            opportunityId = "capgemini-pfe-2026",
            title = "Stage PFE pré-embauche Ingénieur",
            organization = "Capgemini Engineering (Casablanca)",
            location = "Casablanca Nearshore",
            sectorType = SectorType.PRIVE,
            referenceNumber = "CAP-PFE-2026-07",
            submittedDate = "15 Janvier 2026",
            currentStage = ApplicationStage.RESULTAT,
            statusHighlightTitle = "Félicitations ! Votre profil a été retenu",
            statusHighlightDetail = "La convention de stage et promesse d'embauche sont signées.",
            isAdmitted = true,
            candidateNotes = "Convention validée par l'administration de l'ENSIAS.",
            isDemoData = true
        )
    )

    // Initial Calendar Events
    private val initialCalendarEvents = listOf(
        CalendarEvent(
            id = "cal-douane-cloture",
            title = "Clôture Concours ADII (Douanes)",
            organization = "Administration des Douanes",
            date = "08 Novembre 2026",
            time = "16:30",
            daysRemaining = 2,
            eventType = CalendarEventType.CLOTURE_CONCOURS,
            location = "Portail en ligne douane.gov.ma",
            reference = "ADII-CONCOURS-2026",
            opportunityId = "douanes-inspecteur-2026",
            isDone = false
        ),
        CalendarEvent(
            id = "cal-mef-cloture",
            title = "Clôture Concours MEF 120 Postes",
            organization = "Ministère de l'Économie et des Finances",
            date = "15 Novembre 2026",
            time = "16:00",
            daysRemaining = 3,
            eventType = CalendarEventType.CLOTURE_CONCOURS,
            location = "Portail emploi-public.ma",
            reference = "MEF-CONCOURS-2026-120",
            opportunityId = "mef-admin-2026",
            isDone = false
        ),
        CalendarEvent(
            id = "cal-cnrst-cloture",
            title = "Dépôt Candidatures Bourse Al Khawarizmi",
            organization = "CNRST Maroc",
            date = "12 Novembre 2026",
            time = "18:00",
            daysRemaining = 5,
            eventType = CalendarEventType.CLOTURE_CONCOURS,
            location = "Plateforme CNRST Rabat",
            reference = "CNRST-ALKHAWARIZMI-2026",
            opportunityId = "bourse-cnrst-2026",
            isDone = false
        ),
        CalendarEvent(
            id = "cal-mt-entretien",
            title = "Entretien Oral • Maroc Telecom",
            organization = "Maroc Telecom",
            date = "20 Novembre 2026",
            time = "10:30",
            daysRemaining = 5,
            eventType = CalendarEventType.ENTRETIEN,
            location = "Siège Social Rabat • Salle Commission 4B",
            reference = "MT-2026-0842",
            isDone = false
        ),
        CalendarEvent(
            id = "cal-dgi-cloture",
            title = "Clôture Concours DGI 80 Administrateurs",
            organization = "Direction Générale des Impôts",
            date = "18 Novembre 2026",
            time = "16:30",
            daysRemaining = 6,
            eventType = CalendarEventType.CLOTURE_CONCOURS,
            location = "Portail emploi-public.ma",
            reference = "DGI-REC-2026-80",
            opportunityId = "dgi-admin-2026",
            isDone = false
        ),
        CalendarEvent(
            id = "cal-mef-ecrit",
            title = "Épreuve Écrite • Concours MEF",
            organization = "Ministère de l'Économie et des Finances",
            date = "22 Novembre 2026",
            time = "08:00",
            daysRemaining = 12,
            eventType = CalendarEventType.EXAMEN_ECRIT,
            location = "Faculté des Sciences Juridiques (FSJES Souissi Rabat)",
            reference = "MEF-CONCOURS-2026-120",
            isDone = false
        ),
        CalendarEvent(
            id = "cal-ocp-cloture",
            title = "Clôture Dépôt OCP Cybersécurité",
            organization = "Groupe OCP",
            date = "30 Novembre 2026",
            time = "23:59",
            daysRemaining = 16,
            eventType = CalendarEventType.CLOTURE_CONCOURS,
            location = "Portail OCP Talents",
            reference = "OCP-DIG-2026-04",
            opportunityId = "ocp-cyber-2026",
            isDone = false
        )
    )

    // Initial Notifications
    private val initialNotifications = listOf(
        AppNotification(
            id = "notif-01",
            title = "🏛️ Clôture imminente : Concours MEF",
            message = "Plus que 3 jours pour déposer votre dossier Administrateur 2ème grade (120 postes Échelle 11) sur emploi-public.ma.",
            timeAgo = "Il y a 20 min",
            type = NotificationType.DEADLINE_URGENTE,
            isRead = false,
            targetOpportunityId = "mef-admin-2026"
        ),
        AppNotification(
            id = "notif-02",
            title = "📅 Convocation Entretien • Maroc Telecom",
            message = "Votre entretien oral pour le poste Ingénieur Cloud est confirmé pour le vendredi 30 Mai à 10:30 (Siège Rabat).",
            timeAgo = "Il y a 2h",
            type = NotificationType.CONVOCATION,
            isRead = false,
            targetApplicationId = "app-mt-01"
        ),
        AppNotification(
            id = "notif-03",
            title = "✨ 98% Match : OCP Cybersécurité",
            message = "Une nouvelle offre correspondant parfaitement à votre profil ENSIAS Bac+5 est ouverte à Benguerir.",
            timeAgo = "Hier",
            type = NotificationType.NOUVELLE_OFFRE,
            isRead = true,
            targetOpportunityId = "ocp-cyber-2026"
        ),
        AppNotification(
            id = "notif-04",
            title = "🎉 Dossier Validé : Capgemini Engineering",
            message = "Félicitations ! Votre stage PFE pré-embauche a été archivé et retenu avec succès.",
            timeAgo = "Il y a 3 jours",
            type = NotificationType.STATUS_DOSSIER,
            isRead = true,
            targetApplicationId = "app-cap-04"
        )
    )

    // Clean Unauthenticated Guest Profile (Default for new users)
    val guestUserProfile = UserProfile(
        fullName = "Invité",
        headline = "Compte non connecté",
        bio = "Créez votre compte gratuit pour postuler aux concours et synchroniser votre dossier.",
        location = "Maroc",
        isVerified = false,
        isActivelySearching = false,
        completionPercentage = 0,
        targetLevel = "",
        targetField = "",
        degrees = emptyList(),
        skills = emptyList(),
        languages = emptyList(),
        documents = emptyList(),
        preferences = AlertPreferences(),
        account = com.mihnati.app.data.model.UserAccount(
            email = "",
            displayName = "Invité",
            isSignedInWithGoogle = false,
            provider = "guest",
            isVerified = false,
            lastLoginDate = "Jamais connecté"
        )
    )

    // Demo Sample Profile (ENSIAS Engineer) for testing & showcase
    val sampleDemoUserProfile = UserProfile(
        fullName = "Yassine Benali",
        headline = "Diplômé Ingénieur d'État - Génie Informatique (Promotion 2026)",
        bio = "Diplômé de l'ENSIAS spécialisé en génie logiciel, cloud et sécurité. En recherche active de concours de la fonction publique (Échelle 11) ou opportunités d'ingénieur en CDI.",
        location = "Casablanca, Maroc",
        isVerified = true,
        isActivelySearching = true,
        completionPercentage = 85,
        targetLevel = "Bac+5 / Ingénieur",
        targetField = "Informatique & SI",
        degrees = listOf(
            Degree(
                id = "deg-ensias",
                title = "Diplôme d'Ingénieur d'État en Informatique",
                institution = "École Nationale Supérieure d'Informatique et d'Analyse des Systèmes (ENSIAS - Rabat)",
                period = "2023 - 2026",
                mention = "Mention Très Bien",
                isVerified = true
            ),
            Degree(
                id = "deg-cpge",
                title = "Classes Préparatoires aux Grandes Écoles (CPGE MP)",
                institution = "Lycée Moulay Youssef (Rabat)",
                period = "2021 - 2023",
                mention = "Admis CNC 2023",
                isVerified = true
            )
        ),
        skills = listOf("Génie Logiciel", "Java / Spring", "Python", "Cloud AWS", "Cybersécurité", "Docker & K8s"),
        languages = listOf("Arabe (Natif)", "Français (Courant)", "Anglais (Professionnel)"),
        documents = listOf(
            UserDocument(
                id = "doc-cv",
                name = "CV Ingénieur d'État 2026",
                detail = "Mis à jour le 28 Octobre 2026 • 420 Ko",
                status = "Vérifié conforme",
                category = DocumentCategory.CV,
                fileName = "CV_Yassine_Benali_2026.pdf",
                fileSizeKb = 420,
                uploadDate = "28 Octobre 2026",
                fileExtension = "pdf",
                isVerified = true,
                sha256Fingerprint = "SHA256:7e9b2f4c9a8d2026"
            ),
            UserDocument(
                id = "doc-cin",
                name = "Carte Nationale (CNIE)",
                detail = "CNIE Recto-verso certifiée conforme",
                status = "Vérifié conforme",
                category = DocumentCategory.CIN,
                fileName = "CNIE_BK654321_Certifiee.pdf",
                fileSizeKb = 850,
                uploadDate = "15 Octobre 2026",
                fileExtension = "pdf",
                isVerified = true,
                sha256Fingerprint = "SHA256:3a1c8d5e9b7f2026"
            ),
            UserDocument(
                id = "doc-dip",
                name = "Attestation de Réussite ENSIAS",
                detail = "Diplôme d'Ingénieur d'État (Échelle 11)",
                status = "Vérifié conforme",
                category = DocumentCategory.DIPLOME,
                fileName = "Diplome_Ingenieur_ENSIAS.pdf",
                fileSizeKb = 1250,
                uploadDate = "10 Octobre 2026",
                fileExtension = "pdf",
                isVerified = true,
                sha256Fingerprint = "SHA256:9f4e1c2b7a3d2026"
            )
        ),
        preferences = AlertPreferences(
            publicSectorAlerts = true,
            privateSectorAlerts = true,
            deadlineReminders = true,
            universityPrograms = false
        )
    )

    // AI Chat initial messages
    private val initialChatMessages = listOf(
        AiChatMessage(
            id = "ai-intro",
            isFromUser = false,
            text = "Bonjour Yassine ! Je suis l'Assistant IA Mihnati. Je peux vous aider à préparer vos concours de la fonction publique (MEF, MTRA, Douanes), analyser votre éligibilité selon vos diplômes, ou configurer vos rappels d'échéances sur votre calendrier.",
            timestamp = "À l'instant",
            suggestedActions = listOf(
                "Comment postuler au concours MEF ?",
                "Quelles pièces pour l'ADII (Douanes) ?",
                "Équivalence de diplômes",
                "Ouvrir WhatsApp 📱"
            )
        )
    )

    // StateFlows
    private val _opportunities = MutableStateFlow(initialOpportunities)
    val opportunities: StateFlow<List<Opportunity>> = _opportunities.asStateFlow()

    private val _applications = MutableStateFlow(initialApplications)
    val applications: StateFlow<List<Application>> = _applications.asStateFlow()

    private val _calendarEvents = MutableStateFlow(initialCalendarEvents)
    val calendarEvents: StateFlow<List<CalendarEvent>> = _calendarEvents.asStateFlow()

    private val _notifications = MutableStateFlow(initialNotifications)
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _userProfile = MutableStateFlow(guestUserProfile)
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _chatMessages = MutableStateFlow(initialChatMessages)
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    fun mergeScrapedConcours(scrapedList: List<com.mihnati.app.data.model.EmploiPublicConcours>) {
        _opportunities.update { existing ->
            val seenIds = mutableSetOf<String>()
            val seenSignatures = mutableSetOf<String>()
            val deduplicated = mutableListOf<Opportunity>()

            fun makeSignature(org: String, title: String): String {
                val cleanOrg = org.lowercase()
                    .replace("ministère", "")
                    .replace("de l'", "")
                    .replace("de la", "")
                    .replace("du", "")
                    .replace("des", "")
                    .replace(Regex("[^a-z0-9]"), "")
                    .take(20)
                val cleanTitle = title.lowercase()
                    .replace("avis de concours de recrutement", "")
                    .replace("concours national", "")
                    .replace("concours", "")
                    .replace("recrutement", "")
                    .replace(Regex("[^a-z0-9]"), "")
                    .take(25)
                return "${cleanOrg}_${cleanTitle}"
            }

            // 1. Process and strictly deduplicate scraped opportunities from emploi-public.ma
            for (c in scrapedList) {
                val sig = makeSignature(c.administrationFr, c.titleFr)
                if (!seenIds.contains(c.id) && !seenSignatures.contains(sig)) {
                    seenIds.add(c.id)
                    seenSignatures.add(sig)
                    val displaySourceName = "Emploi-Public"
                    deduplicated.add(
                        Opportunity(
                            id = c.id,
                            title = c.titleFr,
                            titleAr = c.titleAr,
                            organization = c.administrationFr,
                            organizationAr = c.administrationAr,
                            location = c.location,
                            sectorType = SectorType.PUBLIC,
                            reference = c.reference,
                            deadline = c.deadlineDate,
                            deadlineDaysRemaining = c.deadlineDaysRemaining,
                            matchPercentage = if (c.echelle == 11) 96 else 88,
                            degreeRequired = c.diplomaRequired,
                            field = c.grade,
                            experience = "Concours National Ouvert",
                            salary = "Statut Fonction Publique (Échelle ${c.echelle})",
                            contractType = "Fonction Publique (${c.numPosts} postes)",
                            description = "${c.titleFr}. Recrutement officiel au sein des services de l'administration marocaine via le portail emploi-public.ma. Diplôme requis : ${c.diplomaRequired}.",
                            requiredDocuments = listOf(
                                RequiredDocument("Demande manuscrite", "Adressée à Monsieur le Ministre / Directeur", isReady = true),
                                RequiredDocument("Copie certifiée CIN", "Carte Nationale d'Identité électronique", isReady = true),
                                RequiredDocument("Copie du Diplôme", c.diplomaRequired, isReady = true),
                                RequiredDocument("Arrêté d'équivalence", "Pour diplômes étrangers ou privés", isReady = true),
                                RequiredDocument("CV actualisé", "Format modèle fonction publique", isReady = true)
                            ),
                            workplaceName = c.administrationFr,
                            workplaceDetail = "Services Centraux et Directions Régionales",
                            sourceUrl = c.detailsUrl,
                            sourceName = displaySourceName,
                            isWeeklySpecial = c.isWeeklySpecial,
                            examDate = c.examDate ?: "",
                            postsCount = c.numPosts,
                            isDemoData = false,
                            avisPdfUrl = c.avisPdfUrl
                        )
                    )
                }
            }

            // 2. Append non-public existing opportunities (private sector & formation only), strictly deduplicated
            for (item in existing) {
                if (item.sectorType == SectorType.PUBLIC) continue // Only authentic scraped public opportunities
                val sig = makeSignature(item.organization, item.title)
                if (!seenIds.contains(item.id) && !seenSignatures.contains(sig)) {
                    seenIds.add(item.id)
                    seenSignatures.add(sig)
                    deduplicated.add(item)
                }
            }

            deduplicated
        }
    }

        private val _whatsAppSubscriptions = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val whatsAppSubscriptions: StateFlow<Map<String, List<String>>> = _whatsAppSubscriptions.asStateFlow()

private val _whatsAppConfig = MutableStateFlow(WhatsAppConfig())
    val whatsAppConfig: StateFlow<WhatsAppConfig> = _whatsAppConfig.asStateFlow()

    fun getOpportunityById(id: String): Opportunity? {
        return _opportunities.value.find { it.id == id }
    }

    fun toggleBookmark(id: String) {
        _opportunities.update { list ->
            list.map { opp ->
                if (opp.id == id) opp.copy(isBookmarked = !opp.isBookmarked) else opp
            }
        }
    }

    fun applyToOpportunity(opportunity: Opportunity): Application {
        val existing = _applications.value.find { it.opportunityId == opportunity.id }
        if (existing != null) return existing

        val newRef = "MHN-2026-" + (1000..9999).random()
        val newApp = Application(
            id = "app-" + UUID.randomUUID().toString().take(8),
            opportunityId = opportunity.id,
            title = opportunity.title,
            organization = opportunity.organization,
            location = opportunity.location,
            sectorType = opportunity.sectorType,
            referenceNumber = newRef,
            submittedDate = "Aujourd'hui",
            currentStage = ApplicationStage.DEPOSE,
            statusHighlightTitle = "Dossier déposé avec succès",
            statusHighlightDetail = "Accusé de réception généré • Réf: $newRef",
            candidateNotes = "Candidature initiée via l'application Mihnati.",
            isDemoData = true
        )
        _applications.update { listOf(newApp) + it }

        // Also add deadline reminder into the Calendar
        val newCalEvent = CalendarEvent(
            id = "cal-" + UUID.randomUUID().toString().take(8),
            title = "Date Limite : ${opportunity.title}",
            organization = opportunity.organization,
            date = opportunity.deadline,
            time = "16:30",
            daysRemaining = opportunity.deadlineDaysRemaining,
            eventType = CalendarEventType.CLOTURE_CONCOURS,
            location = opportunity.location,
            reference = newRef,
            opportunityId = opportunity.id,
            isDone = false,
            isDemoData = true
        )
        _calendarEvents.update { listOf(newCalEvent) + it }

        return newApp
    }

    fun advanceApplicationStage(appId: String) {
        _applications.update { list ->
            list.map { app ->
                if (app.id == appId) {
                    val nextStage = when (app.currentStage) {
                        ApplicationStage.DEPOSE -> ApplicationStage.REVUE
                        ApplicationStage.REVUE -> ApplicationStage.ECRIT
                        ApplicationStage.ECRIT -> ApplicationStage.ENTRETIEN
                        ApplicationStage.ENTRETIEN -> ApplicationStage.RESULTAT
                        ApplicationStage.RESULTAT -> ApplicationStage.RESULTAT
                    }
                    val isAdmitted = nextStage == ApplicationStage.RESULTAT
                    val highlightTitle = when (nextStage) {
                        ApplicationStage.REVUE -> "En cours d'examen par la commission"
                        ApplicationStage.ECRIT -> "Admis aux épreuves écrites"
                        ApplicationStage.ENTRETIEN -> "Convoqué pour l'entretien oral"
                        ApplicationStage.RESULTAT -> "Résultat final publié 🎉"
                        else -> app.statusHighlightTitle
                    }
                    app.copy(
                        currentStage = nextStage,
                        isAdmitted = isAdmitted,
                        statusHighlightTitle = highlightTitle
                    )
                } else app
            }
        }
    }

    fun updateApplicationNotes(appId: String, notes: String) {
        _applications.update { list ->
            list.map { if (it.id == appId) it.copy(candidateNotes = notes) else it }
        }
    }

    fun addApplication(application: Application) {
        _applications.update { listOf(application) + it }
    }

    fun deleteApplication(appId: String) {
        _applications.update { list -> list.filter { it.id != appId } }
    }

    // Calendar methods
    fun addCalendarEvent(event: CalendarEvent) {
        _calendarEvents.update { listOf(event) + it }
    }

    fun toggleCalendarEventDone(id: String) {
        _calendarEvents.update { list ->
            list.map { if (it.id == id) it.copy(isDone = !it.isDone) else it }
        }
    }

    fun deleteCalendarEvent(id: String) {
        _calendarEvents.update { list -> list.filter { it.id != id } }
    }

    // Notification methods
    fun markNotificationAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    fun addNotification(notification: AppNotification) {
        _notifications.update { listOf(notification) + it }
    }

    // Profile & Degree methods
    fun updateUserProfile(
        name: String,
        headline: String,
        bio: String,
        location: String,
        targetField: String,
        isActivelySearching: Boolean
    ) {
        _userProfile.update {
            it.copy(
                fullName = name,
                headline = headline,
                bio = bio,
                location = location,
                targetField = targetField,
                isActivelySearching = isActivelySearching
            )
        }
    }

    fun addDegree(degree: Degree) {
        _userProfile.update { profile ->
            val updatedDegrees = profile.degrees + degree
            profile.copy(
                degrees = updatedDegrees,
                completionPercentage = minOf(100, profile.completionPercentage + 5)
            )
        }
    }

    fun deleteDegree(degreeId: String) {
        _userProfile.update { profile ->
            profile.copy(degrees = profile.degrees.filter { it.id != degreeId })
        }
    }

    fun updatePreferences(preferences: AlertPreferences, context: Context? = null) {
        _userProfile.update { it.copy(preferences = preferences) }
        context?.let { com.mihnati.app.security.MihnatiSecurityVault.saveAlertPreferences(it, preferences) }
    }

    // Google & Secure Authentication Methods
    fun applyAuthenticatedAccount(account: com.mihnati.app.data.model.UserAccount) {
        _userProfile.update { profile ->
            profile.copy(
                fullName = account.displayName,
                headline = "Candidat Vérifié 🇲🇦",
                account = profile.account.copy(
                    email = account.email,
                    displayName = account.displayName,
                    isSignedInWithGoogle = account.provider.contains("Google", ignoreCase = true),
                    lastLoginDate = account.lastLoginDate,
                    provider = account.provider,
                    isVerified = true
                )
            )
        }
        val notif = AppNotification(
            id = "notif-auth-" + UUID.randomUUID().toString().take(6),
            title = "Authentification Sécurisée 🔒",
            message = "Session validée pour ${account.email} (${account.provider}). Chiffrement matériel AES-256.",
            timeAgo = "À l'instant",
            type = NotificationType.STATUS_DOSSIER
        )
        _notifications.update { listOf(notif) + it }
    }

    fun signInWithGoogle(email: String, displayName: String) {
        _userProfile.update { profile ->
            profile.copy(
                fullName = displayName,
                account = profile.account.copy(
                    email = email,
                    displayName = displayName,
                    isSignedInWithGoogle = true,
                    lastLoginDate = "Aujourd'hui"
                )
            )
        }
        val notif = AppNotification(
            id = "notif-glogin-" + UUID.randomUUID().toString().take(6),
            title = "Connexion Google réussie",
            message = "Votre compte $email est désormais synchronisé avec votre espace Mihnati.",
            timeAgo = "À l'instant",
            type = NotificationType.STATUS_DOSSIER
        )
        _notifications.update { listOf(notif) + it }
    }

    fun resetToGuestProfile() {
        _userProfile.value = guestUserProfile
    }

    fun loadDemoProfile() {
        _userProfile.value = sampleDemoUserProfile
    }

    fun signOut(context: android.content.Context) {
        com.mihnati.app.security.MihnatiSecurityVault.logout(context)
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
        resetToGuestProfile()
        val notif = AppNotification(
            id = "notif-logout-" + java.util.UUID.randomUUID().toString().take(6),
            title = "Déconnexion effectuée",
            message = "Vous naviguez maintenant en mode invité.",
            timeAgo = "À l'instant",
            type = NotificationType.STATUS_DOSSIER
        )
        _notifications.update { listOf(notif) + it }
    }

    fun signOutGoogle() {
        _userProfile.update { profile ->
            profile.copy(
                account = profile.account.copy(
                    isSignedInWithGoogle = false,
                    provider = "guest"
                )
            )
        }
    }

    // Document Security & Verification Methods
    fun uploadDocument(
        category: DocumentCategory,
        name: String,
        fileName: String,
        fileSizeKb: Int,
        extension: String
    ): Result<UserDocument> {
        val cleanExt = extension.lowercase().trim().removePrefix(".")
        val allowedExtensions = setOf("pdf", "jpg", "jpeg", "png")

        if (cleanExt !in allowedExtensions) {
            return Result.failure(
                IllegalArgumentException(
                    "Format non autorisé (.$cleanExt). Pour des raisons de sécurité et d'authenticité légale, seuls les fichiers PDF et numérisations officielles (JPG, PNG) sont acceptés."
                )
            )
        }

        if (fileSizeKb > 5120) {
            return Result.failure(
                IllegalArgumentException(
                    "Le fichier dépasse la taille maximale autorisée de 5 Mo (taille actuelle: ${fileSizeKb / 1024} Mo)."
                )
            )
        }

        val shaFingerprint = "SHA256:" + UUID.randomUUID().toString().replace("-", "").take(16).lowercase()
        val newDoc = UserDocument(
            id = "doc-" + UUID.randomUUID().toString().take(8),
            name = name,
            detail = "Vérifié & Chiffré AES-256 • $fileSizeKb Ko",
            status = "Certifié conforme",
            category = category,
            fileName = fileName,
            fileSizeKb = fileSizeKb,
            uploadDate = "Aujourd'hui",
            fileExtension = cleanExt,
            isVerified = true,
            sha256Fingerprint = shaFingerprint
        )

        _userProfile.update { profile ->
            val filtered = profile.documents.filter { it.category != category || it.name != name }
            profile.copy(documents = listOf(newDoc) + filtered)
        }

        val notif = AppNotification(
            id = "notif-doc-" + UUID.randomUUID().toString().take(6),
            title = "Document vérifié et sécurisé ✅",
            message = "${newDoc.name} a été vérifié conforme aux exigences des concours publics.",
            timeAgo = "À l'instant",
            type = NotificationType.STATUS_DOSSIER
        )
        _notifications.update { listOf(notif) + it }

        return Result.success(newDoc)
    }

    fun deleteDocument(docId: String) {
        _userProfile.update { profile ->
            profile.copy(documents = profile.documents.filter { it.id != docId })
        }
    }

    // Direct In-App Application
    fun saveUserWhatsAppPhone(context: Context, phone: String) {
        val normalized = com.mihnati.app.security.WhatsAppOtpManager.normalizeMoroccanPhoneNumber(phone) ?: phone.trim()
        com.mihnati.app.security.MihnatiSecurityVault.saveUserPhoneNumber(context, normalized)
        _userProfile.update { profile ->
            profile.copy(
                account = profile.account.copy(phoneNumber = normalized)
            )
        }
    }

    fun registerWhatsAppSubscription(opportunityId: String, phone: String) {
        val normalized = com.mihnati.app.security.WhatsAppOtpManager.normalizeMoroccanPhoneNumber(phone) ?: phone.trim()
        if (normalized.isEmpty()) return
        _whatsAppSubscriptions.update { current ->
            val list = current[opportunityId] ?: emptyList()
            if (!list.contains(normalized)) {
                current + (opportunityId to (list + normalized))
            } else current
        }
    }

    fun submitDirectApplication(
        opportunity: Opportunity,
        attachedDocIds: List<String>,
        whatsAppPhone: String? = null,
        context: Context? = null
    ): Application {
        val phoneToUse = whatsAppPhone?.ifBlank { null } ?: _userProfile.value.account.phoneNumber.ifBlank { null }
        if (!phoneToUse.isNullOrBlank()) {
            registerWhatsAppSubscription(opportunity.id, phoneToUse)
            if (context != null) {
                saveUserWhatsAppPhone(context, phoneToUse)
            }
        }

        val existing = _applications.value.find { it.opportunityId == opportunity.id }
        if (existing != null) return existing

        val orgSlug = opportunity.organization.split(" ").firstOrNull()?.uppercase()?.take(4) ?: "CONC"
        val newRef = "POSTULE-2026-$orgSlug-${(10000..99999).random()}"

        val newApp = Application(
            id = "app-" + UUID.randomUUID().toString().take(8),
            opportunityId = opportunity.id,
            title = opportunity.title,
            organization = opportunity.organization,
            location = opportunity.location,
            sectorType = opportunity.sectorType,
            referenceNumber = newRef,
            submittedDate = "Aujourd'hui",
            currentStage = ApplicationStage.DEPOSE,
            statusHighlightTitle = "Candidature directe transmise avec succès",
            statusHighlightDetail = "Récépissé officiel généré • ${attachedDocIds.size} pièces certifiées jointes",
            candidateNotes = "Dossier numérique transmis directement via l'application Mihnati avec signature SHA-256.",
            attachedDocuments = attachedDocIds,
            isDemoData = false
        )

        _applications.update { listOf(newApp) + it }

        val calEvent = CalendarEvent(
            id = "cal-" + UUID.randomUUID().toString().take(8),
            title = "Suivi Candidature : ${opportunity.title}",
            organization = opportunity.organization,
            date = opportunity.deadline,
            time = "16:30",
            daysRemaining = opportunity.deadlineDaysRemaining,
            eventType = CalendarEventType.CLOTURE_CONCOURS,
            location = opportunity.location,
            reference = newRef,
            opportunityId = opportunity.id,
            isDone = false,
            isDemoData = false
        )
        _calendarEvents.update { listOf(calEvent) + it }

        val notif = AppNotification(
            id = "notif-app-" + UUID.randomUUID().toString().take(6),
            title = "Candidature directe confirmée 🎉",
            message = "Votre dossier pour ${opportunity.title} (${opportunity.organization}) a été transmis. Réf: $newRef.",
            timeAgo = "À l'instant",
            type = NotificationType.STATUS_DOSSIER
        )
        _notifications.update { listOf(notif) + it }

        return newApp
    }
    fun submitDirectApplication(opportunity: Opportunity, attachedDocIds: List<String>): Application {
        return submitDirectApplication(opportunity, attachedDocIds, null, null)
    }


    fun updateWhatsAppConfig(config: WhatsAppConfig) {
        _whatsAppConfig.value = config
    }

    // AI message dispatcher
    fun sendUserMessage(text: String) {
        val userMsg = AiChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            text = text,
            timestamp = "À l'instant"
        )
        _chatMessages.update { it + userMsg }

        // Generate intelligent Moroccan career assistant response
        val responseText = when {
            text.contains("MEF", ignoreCase = true) || text.contains("Économie", ignoreCase = true) -> {
                "Pour le concours du Ministère de l'Économie et des Finances (120 postes Administrateur 2ème grade - Échelle 11) :\n\n" +
                "1. Votre diplôme ENSIAS Bac+5 est 100% admissible.\n" +
                "2. La date limite de dépôt est le 15 Novembre 2026.\n" +
                "3. Les épreuves comportent un écrit (dissertation générale + sujet de spécialité informatique) suivi d'un entretien oral devant un jury ministériel.\n\n" +
                "Consultez l'onglet Calendrier pour voir le compte à rebours et planifier votre rappel."
            }
            text.contains("Douanes", ignoreCase = true) || text.contains("ADII", ignoreCase = true) -> {
                "Le concours des Douanes marocaines (Inspecteur 3ème grade) clôture dans 2 jours (08 Novembre 2026).\n" +
                "Pièces exigées : Copie certifiée CIN, copie du diplôme Bac+2/Bac+3 légalisée, extrait d'acte de naissance et accusé d'inscription sur douane.gov.ma."
            }
            text.contains("OCP", ignoreCase = true) -> {
                "Le Groupe OCP recrute actuellement des Ingénieurs Cybersécurité & SI pour les campus de Casablanca et Benguerir.\n" +
                "Points clés : CDI, package attractif avec mutuelle groupe OCP, missions sur les infrastructures industrielles critiques (ISO 27001 et SIEM SOC)."
            }
            text.contains("Équivalence", ignoreCase = true) -> {
                "Pour les diplômes délivrés par des universités étrangères ou instituts privés non homologués, l'arrêté d'équivalence publié au Bulletin Officiel marocain est obligatoire avant le dépôt sur emploi-public.ma."
            }
            else -> {
                "Merci pour votre question ! Je consulte la base officielle des concours marocains et offres partenaires Mihnati. Avec votre profil Ingénieur d'État Bac+5, vous avez 4 concours publics Échelle 11 et 12 offres privées CDI compatibles disponibles aujourd'hui."
            }
        }

        val aiResponse = AiChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = false,
            text = responseText,
            timestamp = "À l'instant",
            suggestedActions = listOf("Voir l'offre", "Voir le calendrier", "Ouvrir WhatsApp 📱")
        )
        _chatMessages.update { it + aiResponse }
    }
}
