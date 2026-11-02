package com.mihnati.app.ui.theme

object AppStrings {
    private val isAr: Boolean
        get() = LanguageManager.isArabic

    // Navigation Tabs
    val navHome: String get() = if (isAr) "الرئيسية" else "Accueil"
    val navOpportunities: String get() = if (isAr) "المباريات" else "Opportunités"
    val navApplications: String get() = if (isAr) "ترشيحاتي" else "Candidatures"
    val navCalendar: String get() = if (isAr) "التقويم" else "Calendrier"
    val navProfile: String get() = if (isAr) "حسابي" else "Profil"

    // TopBar & Brand
    val brandName: String get() = if (isAr) "مهنتي" else "Mihnati"
    val brandSubtitle: String get() = if (isAr) "مباريات التوظيف العمومي" else "Emploi Public"
    val liveSyncBadge: String get() = if (isAr) "مزامنة مباشرة" else "Live Sync"
    val assistantIa: String get() = if (isAr) "المساعد الذكي" else "Assistant IA"
    val languageToggleFr: String get() = "🇫🇷 FR"
    val languageToggleAr: String get() = "🇲🇦 ع"

    // Home Screen
    val welcomeTitle: String get() = if (isAr) "مرحباً بك في مهنتي 👋" else "Bienvenue sur Mihnati 👋"
    val welcomeSubtitle: String get() = if (isAr) "حضّر مستقبلك المهني في المغرب" else "Préparez votre avenir professionnel au Maroc"
    val createProfileBadge: String get() = if (isAr) "إنشاء حساب" else "Créer un profil"
    val verifiedProfileBadge: String get() = if (isAr) "حساب موثق ✓" else "Profil Vérifié"
    val searchPlaceholder: String get() = if (isAr) "ابحث عن مباراة، إدارة، أو قطاع..." else "Rechercher concours, poste, ministère..."
    
    // Tools Header & Cards
    val toolsHeader: String get() = if (isAr) "صندوق أدوات المباريات 2026" else "BOÎTE À OUTILS CONCOURS 2026"
    val toolsCount: String get() = if (isAr) "5 أدوات" else "5 outils"
    val salarySimulator: String get() = if (isAr) "محاكي الأجور" else "Simulateur Salaire"
    val salarySubtitle: String get() = if (isAr) "السلالم 8 إلى 11" else "Échelles 8 à 11"
    val contestFile: String get() = if (isAr) "ملف الترشيح" else "Dossier Concours"
    val contestFileSubtitle: String get() = if (isAr) "الوثائق والبطاقة الوطنية" else "Pièces & CNIE"
    val coverLetter: String get() = if (isAr) "طلب خطي رسمي" else "Lettre Motivation"
    val coverLetterSubtitle: String get() = if (isAr) "نموذج قانوني موحد" else "Modèle officiel"
    val diplomaChecker: String get() = if (isAr) "معادلة الشواهد" else "Vérif. Diplôme"
    val diplomaCheckerSubtitle: String get() = if (isAr) "السلالم والوظائف" else "Échelle & Corps"
    val oralPrep: String get() = if (isAr) "التحضير للشفوي" else "Prépa Entretien"
    val oralPrepSubtitle: String get() = if (isAr) "أسئلة لجان المباريات" else "Questions commissions"

    // Observatory Stats
    val observatoryTitle: String get() = if (isAr) "مرصد التوظيف العمومي 2026" else "OBSERVATOIRE DE L'EMPLOI PUBLIC 2026"
    val liveStatus: String get() = if (isAr) "● مباشر" else "● En direct"
    val statConcours: String get() = if (isAr) "مباراة مفتوحة" else "Concours"
    val statPositions: String get() = if (isAr) "منصب مالي" else "Postes État"
    val statRegions: String get() = if (isAr) "جهة بالمملكة" else "Régions"
    val statVerified: String get() = if (isAr) "رسمي وموثق" else "Vérifié"

    // Category Tabs
    val categoryAll: String get() = if (isAr) "الكل" else "Tous"
    val categoryPublic: String get() = if (isAr) "مباريات عمومية" else "Concours Publics"
    val categoryTech: String get() = if (isAr) "تكنولوجيا ورقمي" else "Tech & Digital"
    val categoryFinance: String get() = if (isAr) "أبناك ومالية" else "Banque & Finance"
    val categoryMasters: String get() = if (isAr) "ماستر وتكوين" else "Masters & Formations"
    val categoryHealth: String get() = if (isAr) "قطاع الصحة" else "Santé"
    val categoryCustomAlerts: String get() = if (isAr) "🎯 تنبيهاتي المخصصة" else "🎯 Mes alertes ciblées"

    // Cards & Opportunities
    val expiresSoon: String get() = if (isAr) "ينتهي قريباً" else "Expire bientôt"
    val expiresPrefix: String get() = if (isAr) "ينتهي خلال" else "Expire dans"
    val viewOffer: String get() = if (isAr) "تفاصيل المباراة ←" else "Voir l'offre →"
    val imminentDeadlines: String get() = if (isAr) "آجال وشيكة للإيداع" else "Délais imminents"
    val viewCalendar: String get() = if (isAr) "التقويم ➔" else "Calendrier ➔"
    val daysRemaining: String get() = if (isAr) "أيام متبقية" else "j restants"
    val deadlineClosingPrefix: String get() = if (isAr) "آخر أجل :" else "Clôture :"
    val onlineSubmission: String get() = if (isAr) "إيداع إلكتروني" else "Dépôt en ligne"
    val directApply: String get() = if (isAr) "ترشيح مباشر" else "Postuler directement"
    val recommendedForYou: String get() = if (isAr) "مباريات مقترحة لك" else "Opportunités pour vous"
    val recommendedSubtitle: String get() = if (isAr) "مختارة حسب ملفك وشواهدك" else "Sélectionnées selon votre profil"
    val viewAll: String get() = if (isAr) "عرض الكل" else "Voir tout"
    val noAlertsFoundTitle: String get() = if (isAr) "لا توجد مباريات مطابقة لمعاييرك حالياً" else "Aucun concours ne correspond à vos filtres ciblés"
    val noAlertsFoundDesc: String get() = if (isAr) "عدل الشهادات والجهات في صفحة حسابي لعرض النتائج." else "Personnalisez vos diplômes et régions dans l'onglet Profil pour filtrer vos offres."
    val editFiltersBtn: String get() = if (isAr) "تعديل الفلاتر ➔" else "Modifier mes filtres ➔"

    // Clean Opportunity Card
    val publicSectorScale11: String get() = if (isAr) "سلم الوظيفة العمومية • السلم 11" else "Grille Fonction Publique • Échelle 11"
    val publicSectorScale10: String get() = if (isAr) "سلم الوظيفة العمومية • السلم 10" else "Grille Fonction Publique • Échelle 10"
    val publicSectorScale9: String get() = if (isAr) "سلم الوظيفة العمومية • السلم 9" else "Grille Fonction Publique • Échelle 9"
    val publicSectorStatus: String get() = if (isAr) "نظام الوظيفة العمومية (حسب القرار)" else "Statut Fonction Publique (Selon arrêté)"
    val salaryAccordingProfile: String get() = if (isAr) "الراتب حسب المؤهلات" else "Rémunération selon profil"
    val estimateSalaryAction: String get() = if (isAr) "تقدير الأجر ➔" else "Estimer ➔"
    val matchWord: String get() = if (isAr) "تطابق" else "match"

    // Opportunities Screen
    val searchOpportunitiesPlaceholder: String get() = if (isAr) "المنصب، الوزارة، المدينة، السلم 11..." else "Poste, ministère, ville, Échelle 11..."
    val clearAction: String get() = if (isAr) "مسح" else "Effacer"
    val tabAll: String get() = if (isAr) "الكل" else "Tous"
    val tabFavorites: String get() = if (isAr) "المفضلة" else "Favoris"
    val tabPublicSector: String get() = if (isAr) "المباريات العمومية" else "Concours Publics"
    val tabPrivateSector: String get() = if (isAr) "القطاع الخاص" else "Secteur Privé"
    val tabMastersTrainings: String get() = if (isAr) "ماستر وتدريب" else "Masters & Stages"
    val levelAll: String get() = if (isAr) "كل المستويات" else "Tous niveaux"
    val levelBac2: String get() = if (isAr) "باك+2 / تقني / دبلوم" else "Bac+2 / DUT / BTS"
    val levelBac3: String get() = if (isAr) "باك+3 / إجازة" else "Bac+3 / Licence"
    val levelBac5: String get() = if (isAr) "باك+5 / ماستر / مهندس" else "Bac+5 / Master / Ingénieur"
    val levelDoctorate: String get() = if (isAr) "دكتوراه / باحث" else "Doctorat / Ph.D"
    val sortDeadlineNear: String get() = if (isAr) "الأقرب أجلاً" else "Échéance proche"
    val sortProfileMatch: String get() = if (isAr) "الأكثر تطابقاً" else "Pertinence profil"
    val opportunitiesFound: String get() = if (isAr) "مباراة متوفرة" else "opportunités trouvées"
    val noOffersFound: String get() = if (isAr) "لم يتم العثور على أي مباراة" else "Aucune offre trouvée"
    val modifySearchCriteria: String get() = if (isAr) "يرجى تجربة كلمات بحث أخرى أو تغيير معايير التصفية." else "Essayez de modifier vos critères de recherche ou réinitialisez les filtres."
    val resetFiltersBtn: String get() = if (isAr) "إعادة ضبط الفلاتر" else "Réinitialiser les filtres"

    // Applications Screen
    val myApplicationsTitle: String get() = if (isAr) "ترشيحاتي وتتبّع المباريات" else "Mes Candidatures"
    val refreshAction: String get() = if (isAr) "تحديث" else "Actualiser"
    val activeFilesCount: String get() = if (isAr) "ملفات قيد المتابعة" else "dossiers actifs"
    val finalizedSuccess: String get() = if (isAr) "مكتمل بنجاح" else "finalisé avec succès"
    val tabMyFiles: String get() = if (isAr) "📂 ملفات الترشيح" else "📂 Mes Dossiers"
    val tabConvocations: String get() = if (isAr) "📋 لوائح الكتابي والشفوي" else "📋 Listes Écrit & Oral"
    val filterAll: String get() = if (isAr) "الكل" else "Toutes"
    val filterPending: String get() = if (isAr) "قيد المعالجة" else "En attente"
    val filterExam: String get() = if (isAr) "الاختبار الكتابي" else "Écrit / Concours"
    val filterInterview: String get() = if (isAr) "المقابلة الشفوية" else "Entretien"
    val filterResult: String get() = if (isAr) "النتائج النهائية" else "Résultat"
    val stageSubmitted: String get() = if (isAr) "تم الإيداع" else "Déposé"
    val stageReview: String get() = if (isAr) "قيد المراجعة" else "Revue"
    val stageWritten: String get() = if (isAr) "الاختبار الكتابي" else "Écrit"
    val stageOral: String get() = if (isAr) "الشفوي" else "Entretien"
    val stageAdmitted: String get() = if (isAr) "القبول النهائي" else "Résultat"
    val addPersonalNote: String get() = if (isAr) "+ ملاحظة خاصة" else "+ Note"
    val downloadConvocation: String get() = if (isAr) "تحميل الاستدعاء" else "Télécharger Convocation"
    val noApplicationsTitle: String get() = if (isAr) "لا توجد ترشيحات حالياً" else "Aucune candidature enregistrée"
    val noApplicationsDesc: String get() = if (isAr) "تصفح المباريات المفتوحة وقدم ترشيحك لمتابعة مراحل الانتقاء هنا." else "Explorez les concours et déposez votre dossier pour suivre les étapes ici."

    // Calendar Screen
    val calendarTitle: String get() = if (isAr) "مواعيد المباريات والتقويم" else "Mon Calendrier"
    val newReminderBtn: String get() = if (isAr) "+ تذكير جديد" else "Nouveau rappel"
    val calendarSubtitle: String get() = if (isAr) "تتبّع مواعيد إيداع الترشيحات، الاختبارات الكتابية والمقابلات الشفوية." else "Suivez les clôtures de concours, épreuves écrites et entretiens RH."
    val monthNov2026: String get() = if (isAr) "نونبر 2026" else "Novembre 2026"
    val monthDec2026: String get() = if (isAr) "دجنبر 2026" else "Décembre 2026"
    val monthJan2027: String get() = if (isAr) "يناير 2027" else "Janvier 2027"
    val calAll: String get() = if (isAr) "الكل" else "Tous"
    val calDeadlines: String get() = if (isAr) "⏳ آجال الترشيح" else "⏳ Délais Concours"
    val calExams: String get() = if (isAr) "🎙️ كتابي وشفوي" else "🎙️ Écrits & Oraux"
    val calReminders: String get() = if (isAr) "⏰ تذكيراتي" else "⏰ Mes Rappels"
    val addCalendarDialogTitle: String get() = if (isAr) "إضافة موعد أو تذكير خاص" else "Ajouter un événement au calendrier"
    val eventTitleLabel: String get() = if (isAr) "عنوان الموعد أو المباراة" else "Titre de l'événement"
    val eventOrgLabel: String get() = if (isAr) "الوزارة أو الإدارة" else "Ministère ou Organisme"
    val eventDateLabel: String get() = if (isAr) "التاريخ" else "Date"
    val eventTimeLabel: String get() = if (isAr) "التوقيت" else "Heure"
    val eventLocationLabel: String get() = if (isAr) "المدينة أو المركز" else "Lieu / Ville"
    val saveAction: String get() = if (isAr) "حفظ" else "Enregistrer"
    val cancelAction: String get() = if (isAr) "إلغاء" else "Annuler"
    val closeAction: String get() = if (isAr) "إغلاق" else "Fermer"

    // Profile Screen
    val myAccount: String get() = if (isAr) "حساب المترشح" else "Compte Candidat"
    val guestMode: String get() = if (isAr) "وضع الزائر • غير متصل" else "Mode Invité • Non connecté"
    val guestName: String get() = if (isAr) "زائر" else "Invité"
    val guestBio: String get() = if (isAr) "أنشئ حسابك المجاني للترشح ومتابعة لوائح المباريات." else "Créez votre compte gratuit pour postuler aux concours et synchroniser votre dossier."
    val activelySearching: String get() = if (isAr) "في بحث نشط عن وظيفة" else "En recherche active"
    val passiveSearching: String get() = if (isAr) "في متابعة لمباريات الدولة" else "En veille d'opportunités"
    val editProfile: String get() = if (isAr) "تعديل حسابي" else "Modifier mon profil"
    val notConnected: String get() = if (isAr) "غير متصل" else "Non connecté"
    val connected: String get() = if (isAr) "متصل ✓" else "Connecté ✓"
    val loginButton: String get() = if (isAr) "تسجيل الدخول" else "Connexion"
    val registerButton: String get() = if (isAr) "إنشاء حساب" else "Créer compte"
    val manageAccount: String get() = if (isAr) "إدارة الحساب" else "Gérer"
    val accountMihnati: String get() = if (isAr) "حساب مهنتي" else "Compte Mihnati"
    val accountGoogle: String get() = if (isAr) "حساب Google" else "Compte Google"
    val syncAccountHelp: String get() = if (isAr) "أنشئ حسابك للترشح المباشر ومزامنة الوثائق بأمان" else "Créez votre compte pour postuler et synchroniser vos candidatures"

    // Profile Sections
    val myDossier: String get() = if (isAr) "ملف الترشيح الرقمي" else "Dossier de candidature"
    val degreesHeader: String get() = if (isAr) "الشواهد والدبلومات" else "Diplômes & Formations"
    val addDegree: String get() = if (isAr) "+ إضافة دبلوم أو شهادة" else "+ Ajouter un diplôme ou certificat"
    val skillsHeader: String get() = if (isAr) "التخصصات والكفاءات" else "Spécialités & Compétences"
    val docVaultHeader: String get() = if (isAr) "المحفظة الرقمية الآمنة (CV، البطاقة الوطنية، الشواهد)" else "Porte-documents Sécurisé & Vérifié"
    
    // Personalized Alert Filters
    val alertFiltersTitle: String get() = if (isAr) "إعدادات التنبيهات المخصصة 🎯" else "Filtres d'alertes personnalisées 🎯"
    val alertFiltersSubtitle: String get() = if (isAr) "حدد الشواهد والجهات لتلقي تنبيهات مطابقة فقط على هاتفك وواتساب" else "Ciblez vos diplômes et régions pour recevoir uniquement les concours pertinents"
    val targetDegreesLabel: String get() = if (isAr) "الشهادات والدبلومات المستهدفة :" else "Diplômes & Niveaux ciblés :"
    val targetRegionsLabel: String get() = if (isAr) "الجهات والمدن المستهدفة بالمغرب :" else "Régions ciblées au Maroc :"
    val minScaleLabel: String get() = if (isAr) "الحد الأدنى للسلم الإداري :" else "Niveau hiérarchique minimum :"
    val strictAlertsSwitch: String get() = if (isAr) "تفعيل التصفية الدقيقة للإشعارات" else "Filtrage strict des notifications"
    val strictAlertsSubtitle: String get() = if (isAr) "إرسال إشعارات وواتساب فقط للمباريات التي تطابق معاييرك تماماً" else "Envoyer des alertes Push & WhatsApp UNIQUEMENT pour les concours correspondants"

    // WhatsApp Gateway Section
    val waGatewayHeader: String get() = if (isAr) "بوابة واتساب ومتابعة الاستدعاءات" else "Passerelle WhatsApp & Convocations"
    val waGatewaySubtitle: String get() = if (isAr) "إرسال تلقائي للوائح الاختبار الكتابي، الشفوي والنتائج النهائية مع ملفات PDF" else "Envoi instantané des listes Écrit/Oral & Admis avec PDF"
    val waLinkedNumber: String get() = if (isAr) "رقم واتساب المربوط :" else "Numéro WhatsApp lié :"
    val waConfigureButton: String get() = if (isAr) "إعدادات بوابة Green-API" else "Configurer la passerelle Green-API"
    val waConnectedBadge: String get() = if (isAr) "✓ متصلة" else "✓ Connectée"
    val waDefaultBadge: String get() = if (isAr) "بوابة افتراضية" else "Passerelle par défaut"

    // Settings
    val darkModeHeader: String get() = if (isAr) "المظهر والوضع الليلي" else "Apparence & Mode Sombre"
    val darkModeTitle: String get() = if (isAr) "الوضع الليلي الملكي 🌙" else "Mode Sombre Royal 🌙"
    val darkModeSubtitle: String get() = if (isAr) "سمة سوداء مريحة للعين وموفرة للبطارية" else "Thème noir profond émeraude pour le confort visuel"
    val guidesHeader: String get() = if (isAr) "دليل مباريات التوظيف المغربية" else "Guide des concours marocains"
    val logoutButton: String get() = if (isAr) "تسجيل الخروج" else "Se Déconnecter"

    // Moroccan Career Tools Sheets (Bilingual)
    val salarySimulatorTitle: String get() = if (isAr) "محاكي الأجور بالوظيفة العمومية" else "Simulateur de Salaire Public"
    val salarySimulatorOfficialGrid: String get() = if (isAr) "الشبكة الاستدلالية الرسمية للمملكة المغربية" else "Grille indiciaire officielle du Royaume du Maroc"
    val chooseScaleLabel: String get() = if (isAr) "اختيار السلم الإداري :" else "CHOIX DE L'ÉCHELLE ADMINISTRATIVE"
    val ministryBonusLabel: String get() = if (isAr) "تعويضات الوزارات الخاصة" else "Primes spécifiques de ministère"
    val ministryBonusSub: String get() = if (isAr) "مثال: المالية (MEF)، الضرائب (DGI)، الجمارك" else "Ex: Finances (MEF), Impôts (DGI), Douanes"
    val netSalaryEstimated: String get() = if (isAr) "الراتب الشهري الصافي التقديري" else "SALAIRE NET MENSUEL ESTIMÉ"
    val brutTreatment: String get() = if (isAr) "الراتب الإجمالي الخام الأساسي :" else "Traitement brut indiciaire :"
    val socialDeductions: String get() = if (isAr) "اقتطاعات التقاعد والتأمين (CMR & AMO) :" else "Retenues sociales (CMR & AMO) :"
    val ministryBonusAmount: String get() = if (isAr) "تعويض المردودية والمهام :" else "Indemnité de rendement / primes :"
    val salaryDisclaimer: String get() = if (isAr) "ℹ️ تقدير تقريبي يستند إلى المرسوم رقم 2-04-403 بشأن النظام الأساسي لأطر وموظفي الدولة واتفاق الحوار الاجتماعي 2024-2026." else "ℹ️ Estimation indicative basée sur le décret n° 2-04-403 fixant le statut des administrateurs et techniciens de l'État et l'accord de revalorisation salariale 2024-2026."

    val checklistTitle: String get() = if (isAr) "لائحة وثائق ملف الترشيح" else "Dossier de Candidature"
    val readyDocsCount: String get() = if (isAr) "الوثائق الجاهزة :" else "Pièces prêtes :"

    val motivationLetterTitle: String get() = if (isAr) "طلب خطي رسمي معبأ للمباراة" else "Lettre de Motivation Pré-remplie"
    val motivationLetterSubtitle: String get() = if (isAr) "مكيف وفق معطيات ملفك والمباراة الرسمية" else "Adaptée à votre profil et au concours officiel"
    val copyLetterAction: String get() = if (isAr) "نسخ نص الطلب الخطي" else "Copier la lettre de motivation"
    val letterCopiedToast: String get() = if (isAr) "تم نسخ الطلب الخطي بنجاح !" else "Lettre copiée dans le presse-papiers !"

    val eligibilityCheckerTitle: String get() = if (isAr) "التحقق من معادلة الدبلوم والسلالم" else "Vérificateur d'Éligibilité"
    val eligibilityCheckerSubtitle: String get() = if (isAr) "المطابقة الرسمية بين الدبلوم وسلم الوظيفة العمومية" else "Correspondance officielle Diplôme ➔ Échelle de l'État"
    val chooseDiplomaLabel: String get() = if (isAr) "اختر دبلومك الحالي :" else "CHOISISSEZ VOTRE DIPLÔME ACTUEL :"
    val publicAccessGrade: String get() = if (isAr) "إطار التوظيف بالوظيفة العمومية" else "GRADE D'ACCÈS DE LA FONCTION PUBLIQUE"

    val oralExamTitle: String get() = if (isAr) "دليل التحضير للاختبار الشفوي" else "Préparation à l'Oral du Concours"
    val oralExamSubtitle: String get() = if (isAr) "الأسئلة الأكثر تكراراً لدى لجان الوزارات مع المراجع القانونية" else "Questions récurrentes posées par les jurys ministériels"
}
