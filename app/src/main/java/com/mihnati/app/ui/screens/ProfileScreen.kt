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
    var showUploadDocDialog by remember { mutableStateOf(false) }
    var selectedCategoryToUpload by remember { mutableStateOf(DocumentCategory.CV) }

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
                                text = if (userProfile.isActivelySearching) "En recherche active" else "En veille d'opportunités",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OnSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = userProfile.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )

                        Text(
                            text = userProfile.headline,
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
                                text = userProfile.location,
                                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
                            )
                        }
                    }
                }

                // Bio
                if (userProfile.bio.isNotBlank()) {
                    Text(
                        text = userProfile.bio,
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
                            text = "Modifier mon profil",
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

        // Google Account Banner (Gmail Sync & Auth)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF4285F4))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Compte Google",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                            )
                            if (userProfile.account.isSignedInWithGoogle) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SecondaryContainer)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("Connecté", style = MaterialTheme.typography.labelSmall.copy(color = OnSecondaryContainer, fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                }
                            }
                        }
                        Text(
                            text = userProfile.account.email,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant, fontSize = 11.5.sp)
                        )
                    }
                }

                Button(
                    onClick = { showGoogleSignInDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userProfile.account.isSignedInWithGoogle) SurfaceContainerLow else Primary,
                        contentColor = if (userProfile.account.isSignedInWithGoogle) Primary else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (userProfile.account.isSignedInWithGoogle) "Gérer" else "Connexion Gmail",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
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
                            text = "Dossier de candidature",
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
                                text = "Optimisez vos chances auprès des recruteurs",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Text(
                                text = "Ajoutez votre attestation de stage PFE pour 100% de visibilité.",
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
                            text = "Ajouter",
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
                            text = "Diplômes & Formations",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                    }
                    TagBadge(
                        text = "Bac+5 Validé",
                        backgroundColor = PrimaryFixed,
                        textColor = OnPrimaryFixed
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
                        text = "Ajouter un diplôme ou certificat",
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
                            text = "Spécialités & Compétences",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                    }
                    Text(
                        text = "Modifier",
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
                    text = "Compétences techniques & Domaines",
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
                    text = "Maîtrise linguistique",
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
                            text = "Porte-documents Sécurisé",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                    }
                    TagBadge(
                        text = "${userProfile.documents.size} Pièces vérifiées",
                        backgroundColor = SecondaryFixed,
                        textColor = OnSecondaryFixed
                    )
                }

                Text(
                    text = "Vos pièces officielles sont certifiées (AES-256) et prêtes pour la candidature directe aux concours de l'État.",
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
                        text = "+ Téléverser une pièce officielle (Sécurisé & Vérifié)",
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
                        text = "Préférences de veille automatique",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    )
                }

                // Switch 1: Public Sector
                PreferenceRow(
                    title = "Alertes Concours Fonction Publique",
                    subtitle = "Ministères, offices nationaux et collectivités",
                    checked = userProfile.preferences.publicSectorAlerts,
                    onCheckedChange = {
                        MihnatiRepository.updatePreferences(userProfile.preferences.copy(publicSectorAlerts = it))
                    }
                )

                // Switch 2: Private Sector
                PreferenceRow(
                    title = "Offres Secteur Privé ciblées Bac+5",
                    subtitle = "Grandes entreprises, multinationales et startups",
                    checked = userProfile.preferences.privateSectorAlerts,
                    onCheckedChange = {
                        MihnatiRepository.updatePreferences(userProfile.preferences.copy(privateSectorAlerts = it))
                    }
                )

                // Switch 3: Deadlines
                PreferenceRow(
                    title = "Rappels des dates limites",
                    subtitle = "Alerte SMS & Push 48h avant la clôture",
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
                        MihnatiRepository.updatePreferences(userProfile.preferences.copy(universityPrograms = it))
                    }
                )
            }
        }

        // 7. Guides & Footer
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
                            text = "Guide des concours marocains",
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
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Modifier mon profil",
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
                        label = { Text("Nom complet") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editHeadline,
                        onValueChange = { editHeadline = it },
                        label = { Text("Titre / Fonction actuelle") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Courte bio professionnelle") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Ville / Région") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editField,
                        onValueChange = { editField = it },
                        label = { Text("Spécialité cible") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("En recherche active")
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
                        Toast.makeText(context, "Profil mis à jour !", Toast.LENGTH_SHORT).show()
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Sauvegarder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Add Diploma Dialog
    if (showAddDegreeDialog) {
        AlertDialog(
            onDismissRequest = { showAddDegreeDialog = false },
            title = {
                Text(
                    text = "Ajouter un diplôme",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newDegreeTitle,
                        onValueChange = { newDegreeTitle = it },
                        label = { Text("Intitulé du diplôme") },
                        placeholder = { Text("Ex: Master Data Science") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDegreeInstitution,
                        onValueChange = { newDegreeInstitution = it },
                        label = { Text("Établissement / Université") },
                        placeholder = { Text("Ex: Université Hassan II") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDegreePeriod,
                        onValueChange = { newDegreePeriod = it },
                        label = { Text("Années") },
                        placeholder = { Text("Ex: 2024 - 2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDegreeMention,
                        onValueChange = { newDegreeMention = it },
                        label = { Text("Mention") },
                        placeholder = { Text("Ex: Mention Bien") },
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
                            Toast.makeText(context, "Nouveau diplôme ajouté avec succès !", Toast.LENGTH_SHORT).show()
                            showAddDegreeDialog = false
                            newDegreeTitle = ""
                            newDegreeInstitution = ""
                            newDegreePeriod = ""
                            newDegreeMention = ""
                        } else {
                            Toast.makeText(context, "Veuillez saisir l'intitulé et l'établissement", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDegreeDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showGoogleSignInDialog) {
        GoogleSignInDialog(
            onDismiss = { showGoogleSignInDialog = false }
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
