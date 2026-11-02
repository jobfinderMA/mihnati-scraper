package com.mihnati.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.mihnati.app.data.model.ApplicationStage
import com.mihnati.app.ui.theme.*
import com.mihnati.app.ui.theme.LanguageManager
import com.mihnati.app.ui.theme.AppLanguage
import com.mihnati.app.ui.theme.AppStrings

@Composable
fun MihnatiTopBar(
    unreadNotificationCount: Int = 2,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onRefreshData: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Surface.copy(alpha = 0.96f),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Brand Header with Moroccan Royal Accent
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(listOf(PrimaryContainer, Primary, Color(0xFF00227B)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = "Mihnati Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Mihnati",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OnSurface,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.3).sp
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryFixed.copy(alpha = 0.8f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "2026",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OnPrimaryFixed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "مهنتي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = if (LanguageManager.isArabic) "• 🇲🇦 التوظيف العمومي" else "• 🇲🇦 Emploi Public",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OnSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Actions: Live Sync Capsule, Notifications & Profile
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Live Sync Capsule Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SecondaryContainer.copy(alpha = 0.5f))
                            .border(1.dp, Secondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable { onRefreshData() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Secondary)
                        )
                        Text(
                            text = if (LanguageManager.isArabic) "مباشر" else "Live Sync",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Actualiser",
                            tint = Secondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Bilingual Language Switcher (🇲🇦 عربي / 🇫🇷 FR)
                    val context = LocalContext.current
                    val currentLang by LanguageManager.currentLanguage.collectAsState()
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(Primary.copy(alpha = 0.12f))
                            .border(1.dp, Primary.copy(alpha = 0.35f), RoundedCornerShape(17.dp))
                            .clickable { LanguageManager.toggleLanguage(context) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "🇲🇦 ع" else "🇫🇷 FR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    // Dark / Light Theme Quick Toggle Button
                    val isDark by ThemeManager.isDarkMode.collectAsState()
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLow)
                            .clickable { ThemeManager.toggleDarkMode() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDark) "☀️" else "🌙",
                            fontSize = 16.sp
                        )
                    }

                    // Notifications icon with counter badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLow)
                            .clickable { onNotificationsClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 6.dp, end = 6.dp)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Error),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$unreadNotificationCount",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Profile Avatar Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryFixed)
                            .border(1.5.dp, Primary.copy(alpha = 0.3f), CircleShape)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Mon Profil",
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Hairline subtle separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(SurfaceContainerHighest.copy(alpha = 0.6f))
            )
        }
    }
}

enum class NavigationTab(val route: String, val title: String, val icon: ImageVector) {
    ACCUEIL("accueil", "Accueil", Icons.Default.Home),
    OPPORTUNITES("opportunites", "Opportunités", Icons.Outlined.WorkOutline),
    CANDIDATURES("candidatures", "Candidatures", Icons.Default.AssignmentTurnedIn),
    CALENDRIER("calendrier", "Calendrier", Icons.Outlined.CalendarMonth),
    PROFIL("profil", "Profil", Icons.Default.Person)
}

@Composable
fun MihnatiBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = Surface.copy(alpha = 0.96f),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceContainerHighest.copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationTab.values().forEach { tab ->
                    val selected = tab == currentTab
                    val itemBackground = if (selected) Primary.copy(alpha = 0.12f) else Color.Transparent
                    val contentColor = if (selected) Primary else OnSurfaceVariant

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(itemBackground)
                            .clickable { onTabSelected(tab) }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val tabLocalizedTitle = when (tab) {
                            NavigationTab.ACCUEIL -> AppStrings.navHome
                            NavigationTab.OPPORTUNITES -> AppStrings.navOpportunities
                            NavigationTab.CANDIDATURES -> AppStrings.navApplications
                            NavigationTab.CALENDRIER -> AppStrings.navCalendar
                            NavigationTab.PROFIL -> AppStrings.navProfile
                        }
                        Text(
                            text = tabLocalizedTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = contentColor,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TagBadge(
    text: String,
    backgroundColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    icon: ImageVector? = null
) {
    val effectiveBg = if (backgroundColor != Color.Unspecified) backgroundColor else SurfaceContainer
    val effectiveText = if (textColor != Color.Unspecified) textColor else OnSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(effectiveBg)
            .padding(horizontal = 9.dp, vertical = 3.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = effectiveText,
                modifier = Modifier.size(13.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = effectiveText,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun DemoDataBanner(
    modifier: Modifier = Modifier,
    label: String = if (LanguageManager.isArabic) "بيانات رسمية موثقة من بوابة التوظيف العمومي (emploi-public.ma)" else "Données officielles simulées (emploi-public.ma)"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainerHigh.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Outline,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = OnSurfaceVariant,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun ApplicationStepper(
    currentStage: ApplicationStage,
    modifier: Modifier = Modifier
) {
    val stages = ApplicationStage.values()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, stage ->
            val isPassed = stage.stepNumber < currentStage.stepNumber
            val isCurrent = stage == currentStage

            // Step Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isPassed -> Secondary
                                isCurrent -> PrimaryContainer
                                else -> SurfaceContainerHigh
                            }
                        )
                        .then(
                            if (isCurrent) Modifier.border(3.dp, PrimaryFixed, CircleShape) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isPassed -> Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        isCurrent -> Icon(
                            imageVector = when (stage) {
                                ApplicationStage.DEPOSE -> Icons.Default.Done
                                ApplicationStage.REVUE -> Icons.Default.PersonSearch
                                ApplicationStage.ECRIT -> Icons.Default.EditNote
                                ApplicationStage.ENTRETIEN -> Icons.Default.Mic
                                ApplicationStage.RESULTAT -> Icons.Default.DoneAll
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        else -> Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Outline)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val stageText = if (LanguageManager.isArabic) {
                    when (stage) {
                        ApplicationStage.DEPOSE -> "تم الإيداع"
                        ApplicationStage.REVUE -> "الانتقاء"
                        ApplicationStage.ECRIT -> "الكتابي"
                        ApplicationStage.ENTRETIEN -> "الشفوي"
                        ApplicationStage.RESULTAT -> "النتائج"
                    }
                } else {
                    stage.label
                }
                Text(
                    text = stageText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isCurrent -> PrimaryContainer
                            isPassed -> OnSurface
                            else -> OnSurfaceVariant
                        },
                        textAlign = TextAlign.Center
                    )
                )
            }

            // Connecting line
            if (index < stages.size - 1) {
                val linePassed = stage.stepNumber < currentStage.stepNumber
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.5.dp)
                        .offset(y = (-8).dp)
                        .background(
                            when {
                                linePassed -> Secondary
                                isCurrent -> PrimaryContainer
                                else -> SurfaceContainerHigh
                            }
                        )
                )
            }
        }
    }
}
