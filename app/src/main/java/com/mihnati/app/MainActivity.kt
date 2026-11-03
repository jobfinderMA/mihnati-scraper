package com.mihnati.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mihnati.app.data.repository.EmploiPublicRepository
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.notifications.MihnatiNotificationHelper
import com.mihnati.app.ui.components.MihnatiBottomBar
import com.mihnati.app.ui.components.MihnatiTopBar
import com.mihnati.app.ui.components.NavigationTab
import com.mihnati.app.ui.screens.*
import com.mihnati.app.ui.theme.MihnatiTheme
import com.mihnati.app.ui.theme.AppStrings
import com.mihnati.app.ui.theme.PrimaryContainer
import com.mihnati.app.ui.theme.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Android System Notification Channels
        MihnatiNotificationHelper.initNotificationChannels(this)

        // 1.1 Load WhatsApp Gateway API Configuration
        com.mihnati.app.security.WhatsAppOtpManager.loadGatewayConfig(this)

        // 1.2 Initialize Bilingual Language System
        com.mihnati.app.ui.theme.LanguageManager.init(this)
        com.mihnati.app.translation.AutoTranslationService.initialize(this)

        // 1.3 Load Personalized Alert Filters
        val savedPrefs = com.mihnati.app.security.MihnatiSecurityVault.loadAlertPreferences(this)
        MihnatiRepository.updatePreferences(savedPrefs)

        // 2. Schedule 24/7 background WorkManager cloud sync
        MihnatiNotificationHelper.schedulePeriodicCloudSync(this)

        // 3. Automatically initialize scraper repository with bundled asset & Cloud CDN
        EmploiPublicRepository.initContext(this.applicationContext)

        // 4. Initialize Cloud Backend (Universal Adapter - Firebase)
        com.mihnati.app.data.cloud.CloudServiceManager.init(this, "Firebase")

        val activeUser = com.mihnati.app.security.MihnatiSecurityVault.getCurrentUser(this)
        if (activeUser != null && activeUser.provider != "guest" && activeUser.email.isNotEmpty()) {
            MihnatiRepository.applyAuthenticatedAccount(activeUser)
        } else {
            MihnatiRepository.resetToGuestProfile()
        }

        val initialOppId = intent?.getStringExtra(MihnatiNotificationHelper.EXTRA_OPPORTUNITY_ID)

        setContent {
            val currentLang by com.mihnati.app.ui.theme.LanguageManager.currentLanguage.collectAsState()
            val layoutDir = if (currentLang.isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDir) {
                MihnatiTheme {
                    MainAppScaffold(initialOpportunityId = initialOppId)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Automatically refresh cloud data when user returns to app
        EmploiPublicRepository.triggerLiveSync()
    }
}

@Composable
fun MainAppScaffold(initialOpportunityId: String? = null) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.ACCUEIL) }
    var selectedOpportunityId by remember { mutableStateOf(initialOpportunityId) }
    var showWhatsAppAssistant by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showGuidesDialog by remember { mutableStateOf(false) }

    // Request Android 13+ Notification Permission on startup
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Alertes concours activées 🔔", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!MihnatiNotificationHelper.hasNotificationPermission(context)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val notifications by MihnatiRepository.notifications.collectAsState()
    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    // System Back Handler
    BackHandler(enabled = selectedOpportunityId != null || currentTab != NavigationTab.ACCUEIL) {
        if (selectedOpportunityId != null) {
            selectedOpportunityId = null
        } else if (currentTab != NavigationTab.ACCUEIL) {
            currentTab = NavigationTab.ACCUEIL
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Surface,
        topBar = {
            if (selectedOpportunityId == null) {
                MihnatiTopBar(
                    unreadNotificationCount = unreadCount,
                    onNotificationsClick = { showNotificationsSheet = true },
                    onProfileClick = { currentTab = NavigationTab.PROFIL },
                    onRefreshData = {
                        EmploiPublicRepository.triggerLiveSync { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (selectedOpportunityId == null) {
                MihnatiBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        selectedOpportunityId = null
                    }
                )
            }
        },
        floatingActionButton = {
            if (selectedOpportunityId == null && !showWhatsAppAssistant) {
                FloatingActionButton(
                    onClick = { showWhatsAppAssistant = true },
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 76.dp, end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "💬", fontSize = 18.sp)
                        Text(
                            text = AppStrings.assistantIa,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedOpportunityId != null) {
                OpportunityDetailScreen(
                    opportunityId = selectedOpportunityId!!,
                    onBack = { selectedOpportunityId = null },
                    onOpenWhatsAppAssistant = { showWhatsAppAssistant = true }
                )
            } else {
                when (currentTab) {
                    NavigationTab.ACCUEIL -> {
                        HomeScreen(
                            onNavigateToOpportunity = { id -> selectedOpportunityId = id },
                            onNavigateToTab = { tabName ->
                                currentTab = when (tabName) {
                                    "opportunites" -> NavigationTab.OPPORTUNITES
                                    "candidatures" -> NavigationTab.CANDIDATURES
                                    "calendrier" -> NavigationTab.CALENDRIER
                                    "profil" -> NavigationTab.PROFIL
                                    else -> NavigationTab.ACCUEIL
                                }
                            },
                            onOpenSearch = { currentTab = NavigationTab.OPPORTUNITES }
                        )
                    }
                    NavigationTab.OPPORTUNITES -> {
                        OpportunitiesScreen(
                            onNavigateToOpportunity = { id -> selectedOpportunityId = id },
                            onOpenWhatsAppAssistant = { showWhatsAppAssistant = true }
                        )
                    }
                    NavigationTab.CANDIDATURES -> {
                        ApplicationsScreen(
                            onNavigateToOpportunity = { id -> selectedOpportunityId = id },
                            onOpenGuides = { showGuidesDialog = true }
                        )
                    }
                    NavigationTab.CALENDRIER -> {
                        CalendarScreen(
                            onNavigateToOpportunity = { id -> selectedOpportunityId = id }
                        )
                    }
                    NavigationTab.PROFIL -> {
                        ProfileScreen(
                            onOpenConcoursGuide = { showGuidesDialog = true }
                        )
                    }
                }
            }
        }
    }

    // AI Assistant Bottom Sheet
    if (showWhatsAppAssistant) {
        WhatsAppAssistantSheet(
            onDismiss = { showWhatsAppAssistant = false }
        )
    }

    // Full Notifications Sheet with item clicks & demo banner
    if (showNotificationsSheet) {
        NotificationsSheet(
            onDismiss = { showNotificationsSheet = false },
            onNavigateToOpportunity = { id ->
                selectedOpportunityId = id
                showNotificationsSheet = false
            },
            onNavigateToTab = { tabName ->
                currentTab = when (tabName) {
                    "opportunites" -> NavigationTab.OPPORTUNITES
                    "candidatures" -> NavigationTab.CANDIDATURES
                    "calendrier" -> NavigationTab.CALENDRIER
                    "profil" -> NavigationTab.PROFIL
                    else -> NavigationTab.ACCUEIL
                }
                showNotificationsSheet = false
            }
        )
    }

    // Moroccan Concours Guide Dialog
    if (showGuidesDialog) {
        AlertDialog(
            onDismissRequest = { showGuidesDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = PrimaryContainer
                )
            },
            title = {
                Text("Guide des Concours Marocains 2026 / 2027", style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. Portails officiels : emploi-public.ma, sgg.gov.ma, douane.gov.ma.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "2. Échelles de rémunération : Échelle 11 (Master / Ingénieur d'État), Échelle 10 (Licence / Bac+3), Échelle 9 (DUT / BTS).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "3. Équivalences : Attestation obligatoire du Ministère de l'Enseignement Supérieur pour diplômes privés ou étrangers.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showGuidesDialog = false }) {
                    Text("Compris")
                }
            }
        )
    }
}
