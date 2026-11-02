package com.mihnati.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import com.mihnati.app.ui.theme.AppStrings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.Degree
import com.mihnati.app.data.model.DocumentCategory
import com.mihnati.app.data.model.UserDocument
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.ui.components.DemoDataBanner
import com.mihnati.app.ui.components.GoogleSignInDialog
import com.mihnati.app.ui.components.WhatsAppGatewayConfigDialog
import com.mihnati.app.security.WhatsAppOtpManager
import com.mihnati.app.ui.components.SecureDocumentUploadBottomSheet
import com.mihnati.app.ui.components.TagBadge
import com.mihnati.app.ui.theme.*
import java.util.UUID

@Composable
fun ProfileScreen(
    onOpenConcoursGuide: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by MihnatiRepository.userProfile.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userProfile.fullName) }
    var editHeadline by remember { mutableStateOf(userProfile.headline) }
    var editBio by remember { mutableStateOf(userProfile.bio) }
    var editLocation by remember { mutableStateOf(userProfile.location) }
    var editField by remember { mutableStateOf(userProfile.targetField) }
    var editActiveSearch by remember { mutableStateOf(userProfile.isActivelySearching) }

    var showAddDegreeDialog by remember { mutableStateOf(false) }
    var newDegreeTitle by remember { mutableStateOf("") }
    var newDegreeInstitution by remember { mutableStateOf("") }
    var newDegreePeriod by remember { mutableStateOf("") }
    var newDegreeMention by remember { mutableStateOf("") }

    var showGoogleSignInDialog by remember { mutableStateOf(false) }
    var initialAuthRegisterMode by remember { mutableStateOf(false) }
    var showWhatsAppGatewayDialog by remember { mutableStateOf(false) }
    var showUploadDocDialog by remember { mutableStateOf(false) }
    var selectedCategoryToUpload by remember { mutableStateOf(DocumentCategory.CV) }
    val isGuest = userProfile.account.provider == "guest" || userProfile.fullName == "Invité" || (!userProfile.account.isSignedInWithGoogle && userProfile.account.email.isEmpty())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. En-tête Profil Utilisateur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerLowest)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(PrimaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(42.dp)
                        )
                        if (!isGuest && userProfile.isVerified) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SecondaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Secondary)
                            )
                            Text(
                                text = if (isGuest) AppStrings.guestMode else if (userProfile.isActivelySearching) AppStrings.activelySearching else AppStrings.passiveSearching,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isGuest) OnSurfaceVariant else OnSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = if (isGuest && LanguageManager.isArabic) AppStrings.guestName else userProfile.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )

                        Text(
                            text = if (isGuest && LanguageManager.isArabic) "حساب غير متصل" else userProfile.headline,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant),
                            maxLines = 2
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (LanguageManager.isArabic && (userProfile.location == "Maroc" || userProfile.location == "Rabat, Maroc")) "المغرب 🇲🇦" else userProfile.location,
                                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
                            )
                        }
                    }
                }

                // Bio
                if (userProfile.bio.isNotBlank()) {
                    Text(
                        text = if (isGuest && LanguageManager.isArabic) AppStrings.guestBio else userProfile.bio,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            editName = userProfile.fullName
                            editHeadline = userProfile.headline
                            editBio = userProfile.bio
                            editLocation = userProfile.location
                            editField = userProfile.targetField
                            editActiveSearch = userProfile.isActivelySearching
                            showEditProfileDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.editProfile,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerLow)
                            .clickable {
                                Toast.makeText(context, "Lien profil partagé !", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = OnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Account Banner (Google Sync & Auth Status)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerLowest)
                .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isGuest) PrimaryFixed else SurfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isGuest) "👤" else if (userProfile.account.isSignedInWithGoogle) "G" else "🔒",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = if (isGuest) Primary else Color(0xFF4285F4)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (isGuest) AppStrings.accountMihnati else if (userProfile.account.isSignedInWithGoogle) AppStrings.accountGoogle else AppStrings.myAccount,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isGuest) SurfaceContainerHigh else SecondaryContainer)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (isGuest) AppStrings.notConnected else AppStrings.connected,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isGuest) OnSurfaceVariant else OnSecondaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = if (isGuest) AppStrings.syncAccountHelp else userProfile.account.email,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant, fontSize = 11.5.sp),
                            maxLines = 1
                        )
                    }
                }

                if (isGuest) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                initialAuthRegisterMode = false
                                showGoogleSignInDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = AppStrings.loginButton,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Button(
                            onClick = {
                                initialAuthRegisterMode = true
                                showGoogleSignInDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = AppStrings.registerButton,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            initialAuthRegisterMode = false
                            showGoogleSignInDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceContainerLow,
                            contentColor = Primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = AppStrings.manageAccount,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // 2. Complétion du dossier
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerLowest)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = AppStrings.myDossier,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                    }
                    Text(
                        text = "${userProfile.completionPercentage}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                LinearProgressIndicator(
                    progress = { userProfile.completionPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Primary,
                    trackColor = SurfaceContainerHigh
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (LanguageManager.isArabic) "عزّز فرصك لدى لجان التوظيف والمشغلين" else "Optimisez vos chances auprès des recruteurs",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Text(
                                text = if (LanguageManager.isArabic) "أضف شواهد التدريب أو مشاريع التخرج لاكتمال ملفك بنسبة 100%." else "Ajoutez votre attestation de stage PFE pour 100% de visibilité.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHighest)
                            .clickable { showAddDegreeDialog = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (LanguageManager.isArabic) "إضافة" else "Ajouter",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 3. Mes Diplômes & Formations
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerLowest)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = AppStrings.degreesHeader,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                    }
                    TagBadge(
                        text = if (LanguageManager.isArabic) {
                            if (userProfile.degrees.isNotEmpty()) "${userProfile.degrees.size} شواهد" else "0 شواهد"
                        } else {
                            if (userProfile.degrees.isNotEmpty()) "${userProfile.degrees.size} Diplôme(s)" else "0 Diplôme"
                        },
                        backgroundColor = if (userProfile.degrees.isNotEmpty()) PrimaryFixed else SurfaceContainer,
                        textColor = if (userProfile.degrees.isNotEmpty()) OnPrimaryFixed else OnSurfaceVariant
                    )
                }

                userProfile.degrees.forEach { deg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerLow)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = deg.title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (deg.isVerified) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SecondaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = OnSecondaryContainer,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = "Vérifié",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    color = OnSecondaryContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            MihnatiRepository.deleteDegree(deg.id)
                                            Toast.makeText(context, "Diplôme supprimé", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Supprimer",
                                            tint = Outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = deg.institution,
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = deg.period,
                                    style = MaterialTheme.typography.labelSmall.copy(color = Outline)
                                )
                                Text(text = "•", color = OutlineVariant)
                                Text(
                                    text = deg.mention,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OnSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }

                // Add diploma button
                Button(
                    onClick = { showAddDegreeDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceContainer,
                        contentColor = Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.addDegree,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // 4. Spécialités & Compétences
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerLowest)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = AppStrings.skillsHeader,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                    }
                    Text(
                        text = if (LanguageManager.isArabic) "تعديل" else "Modifier",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Compétences synchronisées avec votre profil", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Text(
                    text = if (LanguageManager.isArabic) "الكفاءات والمهارات التقنية" else "Compétences techniques & Domaines",
                    style = MaterialTheme.typography.labelMedium.copy(color = OnSurfaceVariant)
                )

                // Skill tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    userProfile.skills.take(3).forEach { skill ->
                        TagBadge(
                            text = skill,
                            backgroundColor = SurfaceContainer,
                            textColor = OnSurface
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    userProfile.skills.drop(3).forEach { skill ->
                        TagBadge(
                            text = skill,
                            backgroundColor = PrimaryFixed,
                            textColor = OnPrimaryFixed
                        )
                    }
                }

                Text(
                    text = if (LanguageManager.isArabic) "اللغات المتقنة" else "Maîtrise linguistique",
                    style = MaterialTheme.typography.labelMedium.copy(color = OnSurfaceVariant),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    userProfile.languages.forEach { lang ->
                        TagBadge(
                            text = lang,
                            backgroundColor = SecondaryContainer,
                            textColor = OnSecondaryContainer
                        )
                    }
                }
            }
        }

        // 5. Porte-documents Sécurisé & Vérifié (CV, CNIE, Diplômes)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerLowest)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderShared,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (LanguageManager.isArabic) "المحفظة الرقمية الآمنة" else "Porte-documents Sécurisé",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                    }
                    TagBadge(
                        text = if (LanguageManager.isArabic) "${userProfile.documents.size} وثائق رسمية" else "${userProfile.documents.size} Pièce(s)",
                        backgroundColor = SecondaryFixed,
                        textColor = OnSecondaryFixed
                    )
                }

                Text(
                    text = if (LanguageManager.isArabic) "وثائقك الرسمية مشفرة ومؤمنة (AES-256) وجاهزة للترشح المباشر في مباريات الدولة." else "Vos pièces officielles sont certifiées (AES-256) et prêtes pour la candidature directe aux concours de l'État.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant, fontSize = 11.5.sp)
                )

                // List of uploaded documents
                userProfile.documents.forEach { doc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerLow)
                            .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when (doc.category) {
                                            DocumentCategory.CV -> ErrorContainer
                                            DocumentCategory.CIN -> SecondaryContainer
                                            DocumentCategory.DIPLOME -> PrimaryFixed
                                            else -> SurfaceContainerHigh
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(doc.category.emoji, fontSize = 20.sp)
                            }
                            Column {
                                Text(
                                    text = doc.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                )
                                Text(
                                    text = "${doc.fileName} • ${doc.fileSizeKb} Ko",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OnSurfaceVariant,
                                        fontSize = 10.5.sp
                                    )
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Secondary, modifier = Modifier.size(12.dp))
                                    Text(
                                        text = doc.status,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Secondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        )
                                    )
                                    Text("•", color = Outline, fontSize = 9.sp)
                                    Text(
                                        text = doc.sha256Fingerprint.take(15) + "...",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Outline,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Téléchargement sécurisé de ${doc.fileName}...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Télécharger",
                                    tint = Primary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    MihnatiRepository.deleteDocument(doc.id)
                                    Toast.makeText(context, "${doc.name} supprimé de votre coffre.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Supprimer",
                                    tint = Outline,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // Upload button
                Button(
                    onClick = {
                        selectedCategoryToUpload = DocumentCategory.CV
                        showUploadDocDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (LanguageManager.isArabic) "+ إضافة وثيقة رسمية مشفرة وموثقة" else "+ Téléverser une pièce officielle (Sécurisé & Vérifié)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // 6. Préférences de veille
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerLowest)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (LanguageManager.isArabic) "خيارات التنبيهات التلقائية" else "Préférences de veille automatique",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    )
                }

                // Switch 1: Public Sector
                PreferenceRow(
                    title = if (LanguageManager.isArabic) "إشعارات مباريات الوظيفة العمومية" else "Alertes Concours Fonction Publique",
                    subtitle = if (LanguageManager.isArabic) "الوزارات، المكاتب والمؤسسات الوطنية والجماعات" else "Ministères, offices nationaux et collectivités",
                    checked = userProfile.preferences.publicSectorAlerts,
                    onCheckedChange = {
                        MihnatiRepository.updatePreferences(userProfile.preferences.copy(publicSectorAlerts = it))
                    }
                )

                // Switch 2: Private Sector
                PreferenceRow(
                    title = if (LanguageManager.isArabic) "عروض القطاع الخاص لحاملي الشهادات" else "Offres Secteur Privé ciblées Bac+5",
                    subtitle = if (LanguageManager.isArabic) "الشركات الكبرى، المقاولات والشركات الناشئة" else "Grandes entreprises, multinationales et startups",
                    checked = userProfile.preferences.privateSectorAlerts,
                    onCheckedChange = {
                        MihnatiRepository.updatePreferences(userProfile.preferences.copy(privateSectorAlerts = it))
                    }
                )

                // Switch 3: Deadlines
                PreferenceRow(
                    title = if (LanguageManager.isArabic) "تذكيرات آجال الترشيح" else "Rappels des dates limites",
                    subtitle = if (LanguageManager.isArabic) "تنبيه قبل 48 ساعة من إغلاق باب الترشيح" else "Alerte SMS & Push 48h avant la clôture",
                    checked = userProfile.preferences.deadlineReminders,
                    onCheckedChange = {
                        MihnatiRepository.updatePreferences(userProfile.preferences.copy(deadlineReminders = it))
                    }
                )

                // Switch 4: Masters
                PreferenceRow(
                    title = "Programmes universitaires & Masters",
                    subtitle = "Inscriptions doctorales et bourses d'études",
                    checked = userProfile.preferences.universityPrograms,
                    onCheckedChange = {
                        MihnatiRepository.updatePreferences(userProfile.preferences.copy(universityPrograms = it), context)
                    }
                )

                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                // Title for Granular Personalized Filters
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🎯", fontSize = 16.sp)
                    Text(
                        text = AppStrings.alertFiltersTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Primary)
                    )
                }

                Text(
                    text = AppStrings.alertFiltersSubtitle,
                    fontSize = 11.5.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 15.sp
                )

                // 1. Diplômes ciblés
                Text(
                    text = AppStrings.targetDegreesLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                val degreeOptions = listOf("Master", "Ingénieur", "Licence", "Technicien", "Bac", "Doctorat")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    degreeOptions.forEach { deg ->
                        val isSelected = userProfile.preferences.targetDegrees.contains(deg)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val current = userProfile.preferences.targetDegrees.toMutableList()
                                if (isSelected) current.remove(deg) else current.add(deg)
                                val updated = userProfile.preferences.copy(targetDegrees = current)
                                MihnatiRepository.updatePreferences(updated, context)
                            },
                            label = { Text(deg, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 2. Régions ciblées
                Text(
                    text = AppStrings.targetRegionsLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                val regionOptions = listOf(
                    "Rabat-Salé-Kénitra", "Casablanca-Settat", "Tanger-Tétouan",
                    "Fès-Meknès", "Marrakech-Safi", "Souss-Massa", "Oriental", "Béni Mellal"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    regionOptions.forEach { reg ->
                        val isSelected = userProfile.preferences.targetRegions.contains(reg)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val current = userProfile.preferences.targetRegions.toMutableList()
                                if (isSelected) current.remove(reg) else current.add(reg)
                                val updated = userProfile.preferences.copy(targetRegions = current)
                                MihnatiRepository.updatePreferences(updated, context)
                            },
                            label = { Text(reg, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Secondary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 3. Minimum Échelle
                Text(
                    text = AppStrings.minScaleLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                val isArabic = LanguageManager.isArabic
                val scaleOptions = listOf(
                    0 to (if (isArabic) "الكل" else "Toutes"),
                    8 to (if (isArabic) "السلم 8+" else "Échelle 8+"),
                    10 to (if (isArabic) "السلم 10+" else "Échelle 10+"),
                    11 to (if (isArabic) "السلم 11" else "Échelle 11")
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    scaleOptions.forEach { (scaleVal, scaleLabel) ->
                        val isSelected = userProfile.preferences.minEchelle == scaleVal
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val updated = userProfile.preferences.copy(minEchelle = scaleVal)
                                MihnatiRepository.updatePreferences(updated, context)
                            },
                            label = { Text(scaleLabel, fontSize = 10.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 4. Strict alert notifications switch
                PreferenceRow(
                    title = AppStrings.strictAlertsSwitch,
                    subtitle = AppStrings.strictAlertsSubtitle,
                    checked = userProfile.preferences.onlyFilteredPushNotifications,
                    onCheckedChange = {
                        val updated = userProfile.preferences.copy(onlyFilteredPushNotifications = it)
                        MihnatiRepository.updatePreferences(updated, context)
                    }
                )
            }
        }

        // 6.1 Passerelle WhatsApp & Suivi Convocations (Green-API)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerLowest)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💬", fontSize = 20.sp)
                        Column {
                            Text(
                                text = AppStrings.waGatewayHeader,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Text(
                                text = AppStrings.waGatewaySubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariant
                                )
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (WhatsAppOtpManager.isGatewayConfigured()) Color(0xFF16A34A).copy(alpha = 0.15f)
                                else SurfaceContainerHigh
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (WhatsAppOtpManager.isGatewayConfigured()) AppStrings.waConnectedBadge else AppStrings.waDefaultBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (WhatsAppOtpManager.isGatewayConfigured()) Color(0xFF16A34A) else OnSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                val currentPhone = userProfile.account.phoneNumber.ifEmpty { com.mihnati.app.security.MihnatiSecurityVault.getUserPhoneNumber(context) }
                if (currentPhone.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.waLinkedNumber,
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = currentPhone,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                Button(
                    onClick = { showWhatsAppGatewayDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.waConfigureButton,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 7. Paramètres d'Affichage & Thème
        val isDarkModeActive by com.mihnati.app.ui.theme.ThemeManager.isDarkMode.collectAsState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerLowest)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = if (isDarkModeActive) "🌙" else "☀️", fontSize = 18.sp)
                    Text(
                        text = AppStrings.darkModeHeader,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    )
                }

                PreferenceRow(
                    title = AppStrings.darkModeTitle,
                    subtitle = AppStrings.darkModeSubtitle,
                    checked = isDarkModeActive,
                    onCheckedChange = {
                        com.mihnati.app.ui.theme.ThemeManager.setDarkMode(it)
                    }
                )
            }
        }

        // 8. Guides & Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenConcoursGuide,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = AppStrings.guidesHeader,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = OnSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    TagBadge(
                        text = "Édition 2026 / 2027",
                        backgroundColor = SecondaryContainer,
                        textColor = OnSecondaryContainer
                    )
                }
            }

            // Session & Demo Actions Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Session & Démonstration",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                    )

                    if (!isGuest) {
                        Button(
                            onClick = {
                                MihnatiRepository.signOut(context)
                                Toast.makeText(context, "Session déconnectée avec succès", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer, contentColor = OnErrorContainer),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(AppStrings.logoutButton, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (userProfile.fullName.contains("Démo") || userProfile.fullName.contains("Yassine")) {
                                MihnatiRepository.resetToGuestProfile()
                                Toast.makeText(context, "Mode Invité rétabli", Toast.LENGTH_SHORT).show()
                            } else {
                                MihnatiRepository.loadDemoProfile()
                                Toast.makeText(context, "Exemple de profil ENSIAS chargé", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (userProfile.fullName.contains("Démo") || userProfile.fullName.contains("Yassine")) "🗑️ Réinitialiser le profil (Mode Invité)" else "🧪 Charger un profil exemple (Démo ENSIAS)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            DemoDataBanner(
                label = "Profil synchronisé avec le référentiel national des diplômes",
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "Mihnati مهنتي • Version 2.4.0 (Build 2026.11) • Royaume du Maroc",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Outline,
                    fontSize = 11.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // Edit Profile Modal
    if (showEditProfileDialog) {
        val isAr = LanguageManager.isArabic
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = if (isAr) "تعديل بيانات الحساب" else "Modifier mon profil",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(if (isAr) "الاسم الكامل" else "Nom complet") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editHeadline,
                        onValueChange = { editHeadline = it },
                        label = { Text(if (isAr) "المسمى المهني / الصفة الحالية" else "Titre / Fonction actuelle") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text(if (isAr) "نبذة مهنية موجزة" else "Courte bio professionnelle") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text(if (isAr) "المدينة / الجهة" else "Ville / Région") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editField,
                        onValueChange = { editField = it },
                        label = { Text(if (isAr) "التخصص المستهدف" else "Spécialité cible") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isAr) "في بحث نشط عن وظيفة" else "En recherche active")
                        Switch(
                            checked = editActiveSearch,
                            onCheckedChange = { editActiveSearch = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        MihnatiRepository.updateUserProfile(
                            name = editName,
                            headline = editHeadline,
                            bio = editBio,
                            location = editLocation,
                            targetField = editField,
                            isActivelySearching = editActiveSearch
                        )
                        Toast.makeText(context, if (isAr) "تم تحديث الملف بنجاح !" else "Profil mis à jour !", Toast.LENGTH_SHORT).show()
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(if (isAr) "حفظ التعديلات" else "Sauvegarder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(if (isAr) "إلغاء" else "Annuler")
                }
            }
        )
    }

    // Add Diploma Dialog
    if (showAddDegreeDialog) {
        val isAr = LanguageManager.isArabic
        AlertDialog(
            onDismissRequest = { showAddDegreeDialog = false },
            title = {
                Text(
                    text = if (isAr) "إضافة دبلوم أو شهادة" else "Ajouter un diplôme",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newDegreeTitle,
                        onValueChange = { newDegreeTitle = it },
                        label = { Text(if (isAr) "عنوان الدبلوم أو الشهادة" else "Intitulé du diplôme") },
                        placeholder = { Text(if (isAr) "مثال: ماستر في المعلوميات" else "Ex: Master Data Science") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDegreeInstitution,
                        onValueChange = { newDegreeInstitution = it },
                        label = { Text(if (isAr) "المؤسسة أو الجامعة" else "Établissement / Université") },
                        placeholder = { Text(if (isAr) "مثال: جامعة محمد الخامس بالرباط" else "Ex: Université Hassan II") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDegreePeriod,
                        onValueChange = { newDegreePeriod = it },
                        label = { Text(if (isAr) "سنة التخرج أو الفترة" else "Années") },
                        placeholder = { Text(if (isAr) "مثال: 2024 - 2026" else "Ex: 2024 - 2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDegreeMention,
                        onValueChange = { newDegreeMention = it },
                        label = { Text(if (isAr) "الميزة" else "Mention") },
                        placeholder = { Text(if (isAr) "مثال: حسن جداً" else "Ex: Mention Bien") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDegreeTitle.isNotBlank() && newDegreeInstitution.isNotBlank()) {
                            val newDeg = Degree(
                                id = UUID.randomUUID().toString(),
                                title = newDegreeTitle,
                                institution = newDegreeInstitution,
                                period = if (newDegreePeriod.isNotBlank()) newDegreePeriod else "2026",
                                mention = if (newDegreeMention.isNotBlank()) newDegreeMention else "Validé",
                                isVerified = true
                            )
                            MihnatiRepository.addDegree(newDeg)
                            Toast.makeText(context, if (isAr) "تمت إضافة الدبلوم بنجاح !" else "Nouveau diplôme ajouté avec succès !", Toast.LENGTH_SHORT).show()
                            showAddDegreeDialog = false
                            newDegreeTitle = ""
                            newDegreeInstitution = ""
                            newDegreePeriod = ""
                            newDegreeMention = ""
                        } else {
                            Toast.makeText(context, if (isAr) "يرجى إدخال عنوان الدبلوم والجامعة" else "Veuillez saisir l'intitulé et l'établissement", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(if (isAr) "إضافة الدبلوم" else "Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDegreeDialog = false }) {
                    Text(if (isAr) "إلغاء" else "Annuler")
                }
            }
        )
    }

    if (showGoogleSignInDialog) {
        GoogleSignInDialog(
            initialRegisterMode = initialAuthRegisterMode,
            onDismiss = { showGoogleSignInDialog = false }
        )
    }

    if (showWhatsAppGatewayDialog) {
        WhatsAppGatewayConfigDialog(
            onDismiss = { showWhatsAppGatewayDialog = false }
        )
    }

    if (showUploadDocDialog) {
        SecureDocumentUploadBottomSheet(
            initialCategory = selectedCategoryToUpload,
            onDismiss = { showUploadDocDialog = false }
        )
    }
}

@Composable
private fun PreferenceRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OnSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryContainer,
                uncheckedThumbColor = Outline,
                uncheckedTrackColor = SurfaceContainerHigh
            )
        )
    }
}
