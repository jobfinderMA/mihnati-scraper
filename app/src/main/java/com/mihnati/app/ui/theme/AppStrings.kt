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

    // Home Screen
    val welcomeTitle: String get() = if (isAr) "مرحباً بك في مهنتي 👋" else "Bienvenue sur Mihnati 👋"
    val welcomeSubtitle: String get() = if (isAr) "حضّر مستقبلك المهني في المغرب" else "Préparez votre avenir professionnel au Maroc"
    val createProfileBadge: String get() = if (isAr) "إنشاء حساب" else "Créer un profil"
    val searchPlaceholder: String get() = if (isAr) "ابحث عن مباراة، إدارة، أو قطاع..." else "Rechercher concours, poste, ministère..."
    
    // Tools
    val toolsHeader: String get() = if (isAr) "صندوق أدوات المباريات 2026" else "BOÎTE À OUTILS CONCOURS 2026"
    val salarySimulator: String get() = if (isAr) "محاكي الأجور" else "Simulateur Salaire"
    val salarySubtitle: String get() = if (isAr) "السلالم 8 إلى 11" else "Échelles 8 à 11"
    val contestFile: String get() = if (isAr) "ملف الترشيح" else "Dossier Concours"
    val contestFileSubtitle: String get() = if (isAr) "الوثائق والبطاقة الوطنية" else "Pièces & CNIE"
    val coverLetter: String get() = if (isAr) "طلب خطي رسمي" else "Lettre Motivation"
    val coverLetterSubtitle: String get() = if (isAr) "نموذج قانوني موحد" else "Modèle officiel"

    // Observatory Stats
    val observatoryTitle: String get() = if (isAr) "مرصد التوظيف العمومي 2026" else "OBSERVATOIRE DE L'EMPLOI PUBLIC 2026"
    val statConcours: String get() = if (isAr) "مباراة مفتوحة" else "Concours"
    val statPositions: String get() = if (isAr) "منصب مالي" else "Postes État"
    val statRegions: String get() = if (isAr) "جهة بالمملكة" else "Régions"
    val statVerified: String get() = if (isAr) "رسمي وموثق" else "Vérifié"

    // Category Tabs
    val categoryAll: String get() = if (isAr) "الكل" else "Tous"
    val categoryPublic: String get() = if (isAr) "مباريات عمومية" else "Concours Publics"
    val categoryTech: String get() = if (isAr) "تكنولوجيا ورقمي" else "Tech & Digital"
    val categoryCustomAlerts: String get() = if (isAr) "🎯 تنبيهاتي المخصصة" else "🎯 Mes alertes ciblées"

    // Cards & Opportunities
    val expiresSoon: String get() = if (isAr) "ينتهي قريباً" else "Expire bientôt"
    val viewOffer: String get() = if (isAr) "تفاصيل المباراة ←" else "Voir l'offre →"
    val imminentDeadlines: String get() = if (isAr) "آجال وشيكة للإيداع" else "Délais imminents"
    val daysRemaining: String get() = if (isAr) "أيام متبقية" else "j restants"
    val directApply: String get() = if (isAr) "ترشيح مباشر" else "Postuler directement"

    // Profile Screen
    val myAccount: String get() = if (isAr) "حساب المترشح" else "Compte Candidat"
    val guestMode: String get() = if (isAr) "وضع الزائر • غير متصل" else "Mode Invité • Non connecté"
    val guestName: String get() = if (isAr) "زائر" else "Invité"
    val guestBio: String get() = if (isAr) "أنشئ حسابك المجاني للترشح ومتابعة لوائح المباريات." else "Créez votre compte gratuit pour postuler aux concours et synchroniser votre dossier."
    val editProfile: String get() = if (isAr) "تعديل حسابي" else "Modifier mon profil"
    val notConnected: String get() = if (isAr) "غير متصل" else "Non connecté"
    val connected: String get() = if (isAr) "متصل ✓" else "Connecté ✓"
    val loginButton: String get() = if (isAr) "تسجيل الدخول" else "Connexion"
    val registerButton: String get() = if (isAr) "إنشاء حساب" else "Créer compte"
    val manageAccount: String get() = if (isAr) "إدارة الحساب" else "Gérer"

    // Sections
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
}
