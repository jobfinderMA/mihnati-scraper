package com.mihnati.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.AppNotification
import com.mihnati.app.data.model.NotificationType
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.ui.components.DemoDataBanner
import com.mihnati.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    onDismiss: () -> Unit,
    onNavigateToOpportunity: (String) -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    val notifications by MihnatiRepository.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    )
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Error)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$unreadCount non lues",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                if (unreadCount > 0) {
                    Text(
                        text = "Tout marquer lu",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable {
                            MihnatiRepository.markAllNotificationsAsRead()
                        }
                    )
                }
            }

            DemoDataBanner(
                label = "Alertes synchronisées avec emploi-public.ma et vos candidatures",
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Android System Push Notifications Banner & Test Action
            val context = androidx.compose.ui.platform.LocalContext.current
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = SecondaryContainer.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Secondary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "🔔", fontSize = 14.sp)
                            Text(
                                text = "Alertes Système Android",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = OnSecondaryContainer)
                            )
                        }
                        Text(
                            text = "Actives 24/7 en arrière-plan (WorkManager Cloud)",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = OnSurfaceVariant)
                        )
                    }
                    Button(
                        onClick = {
                            com.mihnati.app.notifications.MihnatiNotificationHelper.showTestNotification(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Tester 📲", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }

            HorizontalDivider(color = SurfaceContainerHigh)

            // Notifications List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { notif ->
                    NotificationCardItem(
                        notification = notif,
                        onClick = {
                            MihnatiRepository.markNotificationAsRead(notif.id)
                            onDismiss()
                            if (notif.targetOpportunityId != null) {
                                onNavigateToOpportunity(notif.targetOpportunityId)
                            } else if (notif.targetApplicationId != null) {
                                onNavigateToTab("candidatures")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCardItem(
    notification: AppNotification,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (!notification.isRead) SurfaceContainerLowest else SurfaceContainerLow)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon per type
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            NotificationType.DEADLINE_URGENTE -> ErrorContainer
                            NotificationType.CONVOCATION -> PrimaryFixed
                            NotificationType.NOUVELLE_OFFRE -> SecondaryContainer
                            NotificationType.STATUS_DOSSIER -> SurfaceContainerHigh
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        NotificationType.DEADLINE_URGENTE -> Icons.Default.Timer
                        NotificationType.CONVOCATION -> Icons.Default.EventAvailable
                        NotificationType.NOUVELLE_OFFRE -> Icons.Default.AutoAwesome
                        NotificationType.STATUS_DOSSIER -> Icons.Default.TaskAlt
                    },
                    contentDescription = null,
                    tint = when (notification.type) {
                        NotificationType.DEADLINE_URGENTE -> OnErrorContainer
                        NotificationType.CONVOCATION -> OnPrimaryFixed
                        NotificationType.NOUVELLE_OFFRE -> OnSecondaryContainer
                        NotificationType.STATUS_DOSSIER -> Secondary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                            color = OnSurface
                        )
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Primary)
                        )
                    }
                }

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OnSurfaceVariant,
                        lineHeight = 18.sp
                    )
                )

                Text(
                    text = notification.timeAgo,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Outline,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
