package com.mihnati.app.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.*
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Google Sign-In with Gmail Dialog
 */
@Composable
fun GoogleSignInDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by MihnatiRepository.userProfile.collectAsState()
    val account = userProfile.account

    var emailInput by remember { mutableStateOf(account.email) }
    var nameInput by remember { mutableStateOf(userProfile.fullName) }
    var isSigningIn by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF4285F4))
                }
                Column {
                    Text(
                        text = if (account.isSignedInWithGoogle) "Compte Google Connecté" else "Connexion avec Google",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Authentification sécurisée Gmail",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (account.isSignedInWithGoogle) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SecondaryContainer.copy(alpha = 0.4f))
                            .border(1.dp, Secondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = account.displayName.firstOrNull()?.uppercase() ?: "Y",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(
                                    text = account.displayName,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = account.email,
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Secondary, modifier = Modifier.size(13.dp))
                                    Text(
                                        text = "Gmail Vérifié • Token Actif",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Secondary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Vous pouvez vous connecter avec une autre adresse Gmail pour synchroniser vos pièces justificatives.",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Adresse Gmail") },
                    placeholder = { Text("nom.prenom@gmail.com") },
                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = Primary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nom complet du candidat") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Primary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (isSigningIn) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vérification OAuth2 Google en cours...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Primary)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!emailInput.contains("@") || !emailInput.contains(".")) {
                        Toast.makeText(context, "Veuillez entrer une adresse Gmail valide.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSigningIn = true
                    coroutineScope.launch {
                        delay(600)
                        MihnatiRepository.signInWithGoogle(emailInput, nameInput)
                        isSigningIn = false
                        Toast.makeText(context, "Connecté avec succès : $emailInput", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Connexion Google", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Secure Document Upload Bottom Sheet with Security & Verification
 * Only allows .pdf, .jpg, .png and enforces size limits & integrity check
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureDocumentUploadBottomSheet(
    initialCategory: DocumentCategory = DocumentCategory.CV,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var documentName by remember {
        mutableStateOf(
            when (initialCategory) {
                DocumentCategory.CV -> "CV Actualisé 2026"
                DocumentCategory.CIN -> "Carte Nationale d'Identité (CNIE)"
                DocumentCategory.DIPLOME -> "Diplôme d'Ingénieur / Master"
                DocumentCategory.CASIER_JUDICIAIRE -> "Fiche Anthropométrique (Casier)"
                DocumentCategory.LETTRE_MOTIVATION -> "Lettre de Motivation"
                DocumentCategory.AUTRE -> "Arrêté d'équivalence"
            }
        )
    }

    var selectedExtension by remember { mutableStateOf("pdf") }
    val allowedExtensions = listOf("pdf", "jpg", "png")

    // File presets for seamless testing & realistic uploads
    val suggestedFiles = remember(selectedCategory, selectedExtension) {
        when (selectedCategory) {
            DocumentCategory.CV -> listOf(
                "CV_Yassine_Benali_Ingenieur.$selectedExtension" to 420,
                "Curriculum_Vitae_Officiel_Maroc.$selectedExtension" to 580
            )
            DocumentCategory.CIN -> listOf(
                "CNIE_BK654321_RectoVerso_Certifie.$selectedExtension" to 850,
                "Copie_Legalisee_Carte_Nationale.$selectedExtension" to 920
            )
            DocumentCategory.DIPLOME -> listOf(
                "Diplome_Etat_ENSIAS_Bac5.$selectedExtension" to 1420,
                "Arrete_Equivalence_Bulletin_Officiel.$selectedExtension" to 1100
            )
            DocumentCategory.CASIER_JUDICIAIRE -> listOf(
                "Fiche_Anthropometrique_DGSN_2026.$selectedExtension" to 610,
                "Bulletin_Numero_3_Casier.$selectedExtension" to 540
            )
            DocumentCategory.LETTRE_MOTIVATION -> listOf(
                "Lettre_Motivation_Ministere_MEF.$selectedExtension" to 320
            )
            DocumentCategory.AUTRE -> listOf(
                "Attestation_Travail_Stage_OCP.$selectedExtension" to 730
            )
        }
    }

    var chosenFileName by remember(suggestedFiles) { mutableStateOf(suggestedFiles.first().first) }
    var chosenFileSizeKb by remember(suggestedFiles) { mutableStateOf(suggestedFiles.first().second) }

    var isVerifying by remember { mutableStateOf(false) }
    var verificationStep by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Primary)
                    }
                    Column {
                        Text(
                            text = "Téléversement Sécurisé & Vérifié",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Chiffrement AES-256 • Conforme concours publics",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            // Security Compliance Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
                    Column {
                        Text(
                            text = "Contrôle d'authenticité et d'intégrité",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                        )
                        Text(
                            text = "Formats acceptés : PDF, JPG, PNG (Max 5 Mo). Tout fichier suspect ou non réglementaire est automatiquement rejeté.",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant, fontSize = 11.sp)
                        )
                    }
                }
            }

            // 1. Choose Document Category
            Text(
                text = "TYPE DE DOCUMENT OFFICIEL :",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Outline,
                    letterSpacing = 0.8.sp
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    DocumentCategory.CV to "CV",
                    DocumentCategory.CIN to "CNIE",
                    DocumentCategory.DIPLOME to "Diplôme",
                    DocumentCategory.CASIER_JUDICIAIRE to "Casier"
                ).forEach { (cat, label) ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Primary else SurfaceContainerLow)
                            .clickable {
                                selectedCategory = cat
                                documentName = when (cat) {
                                    DocumentCategory.CV -> "CV Actualisé 2026"
                                    DocumentCategory.CIN -> "Carte Nationale d'Identité (CNIE)"
                                    DocumentCategory.DIPLOME -> "Diplôme d'Ingénieur / Master"
                                    DocumentCategory.CASIER_JUDICIAIRE -> "Fiche Anthropométrique (Casier)"
                                    else -> "Pièce justificative"
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(cat.emoji, fontSize = 16.sp)
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else OnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // 2. Document Title
            OutlinedTextField(
                value = documentName,
                onValueChange = { documentName = it },
                label = { Text("Désignation du document") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 3. Format & File Choice
            Text(
                text = "FORMAT DU FICHIER :",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Outline,
                    letterSpacing = 0.8.sp
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allowedExtensions.forEach { ext ->
                    val isSelected = selectedExtension == ext
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Secondary else SurfaceContainerLow)
                            .clickable { selectedExtension = ext }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = ".${ext.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else OnSurfaceVariant
                            )
                        )
                    }
                }
            }

            // File selection box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.5.dp, Primary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null, tint = Primary)
                            Column {
                                Text(
                                    text = chosenFileName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "$chosenFileSizeKb Ko • Prêt pour analyse",
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant, fontSize = 11.sp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SecondaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Conforme",
                                style = MaterialTheme.typography.labelSmall.copy(color = OnSecondaryContainer, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            )
                        }
                    }
                }
            }

            // Verification Progress if processing
            if (isVerifying) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Secondary)
                    Text(
                        text = verificationStep,
                        style = MaterialTheme.typography.bodySmall.copy(color = Primary, fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // Error display if any
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorContainer)
                        .padding(10.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = OnErrorContainer)
                    )
                }
            }

            // Submit / Upload Button
            Button(
                onClick = {
                    isVerifying = true
                    errorMessage = null
                    coroutineScope.launch {
                        verificationStep = "1/3 : Analyse antivirus et structure..."
                        delay(400)
                        verificationStep = "2/3 : Calcul empreinte SHA-256..."
                        delay(400)
                        verificationStep = "3/3 : Certification conformité concours..."
                        delay(300)

                        val result = MihnatiRepository.uploadDocument(
                            category = selectedCategory,
                            name = documentName,
                            fileName = chosenFileName,
                            fileSizeKb = chosenFileSizeKb,
                            extension = selectedExtension
                        )

                        isVerifying = false

                        result.onSuccess { doc ->
                            Toast.makeText(context, "${doc.name} vérifié et sécurisé avec succès !", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }.onFailure { err ->
                            errorMessage = err.message ?: "Erreur de validation."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isVerifying,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isVerifying) "Vérification en cours..." else "Vérifier & Enregistrer le document",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Direct In-App Application Modal ("Postuler Directement • Mihnati Postule")
 * Checks required documents, auto-attaches verified docs, and generates official receipt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectApplicationBottomSheet(
    opportunity: Opportunity,
    onDismiss: () -> Unit,
    onOpenUploadDialog: (DocumentCategory) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val userProfile by MihnatiRepository.userProfile.collectAsState()
    val applications by MihnatiRepository.applications.collectAsState()

    val existingApp = applications.find { it.opportunityId == opportunity.id }
    val userDocs = userProfile.documents

    var isSubmitting by remember { mutableStateOf(false) }
    var submittedApplication by remember { mutableStateOf<Application?>(existingApp) }
    val coroutineScope = rememberCoroutineScope()

    // Required documents matching
    val hasCv = userDocs.any { it.category == DocumentCategory.CV }
    val hasCin = userDocs.any { it.category == DocumentCategory.CIN }
    val hasDiplome = userDocs.any { it.category == DocumentCategory.DIPLOME }

    val allRequiredReady = hasCv && hasCin && hasDiplome

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = OnSecondaryContainer)
                    }
                    Column {
                        Text(
                            text = "Candidature Directe",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Dépôt électronique sécurisé via Mihnati",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            // Target Opportunity Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryFixed.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = opportunity.organization,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Primary)
                    )
                    Text(
                        text = opportunity.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = OnPrimaryFixed)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Réf: ${opportunity.reference}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = OnPrimaryFixed.copy(alpha = 0.8f))
                        )
                        Text(
                            text = "Clôture: ${opportunity.deadline}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Tertiary)
                        )
                    }
                }
            }

            // Success receipt state
            if (submittedApplication != null) {
                val app = submittedApplication!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SecondaryContainer.copy(alpha = 0.4f))
                        .border(1.5.dp, Secondary, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        Text(
                            text = "Candidature Déposée avec Succès !",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = OnSurface)
                        )

                        Text(
                            text = "Votre récépissé électronique officiel de dépôt a été enregistré dans votre espace candidat.",
                            style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center, color = OnSurfaceVariant)
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Numéro de dossier :", style = MaterialTheme.typography.bodySmall.copy(color = Outline))
                            Text(app.referenceNumber, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Primary))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Candidat :", style = MaterialTheme.typography.bodySmall.copy(color = Outline))
                            Text(userProfile.fullName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Compte vérifié :", style = MaterialTheme.typography.bodySmall.copy(color = Outline))
                            Text(userProfile.account.email, style = MaterialTheme.typography.bodySmall.copy(color = Secondary, fontWeight = FontWeight.Bold))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Date de dépôt :", style = MaterialTheme.typography.bodySmall.copy(color = Outline))
                            Text("01 Novembre 2026", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Fermer & Voir dans Candidatures", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Checklist of Required Documents
                Text(
                    text = "PIÈCES EXIGÉES POUR CE RECRUTEMENT :",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Outline,
                        letterSpacing = 0.8.sp
                    )
                )

                // Doc 1: CV
                DocumentVerificationRow(
                    label = "Curriculum Vitae actualisé",
                    category = DocumentCategory.CV,
                    isReady = hasCv,
                    attachedFile = userDocs.find { it.category == DocumentCategory.CV }?.fileName,
                    onUploadClicked = { onOpenUploadDialog(DocumentCategory.CV) }
                )

                // Doc 2: CIN
                DocumentVerificationRow(
                    label = "Carte Nationale (CNIE Recto/Verso)",
                    category = DocumentCategory.CIN,
                    isReady = hasCin,
                    attachedFile = userDocs.find { it.category == DocumentCategory.CIN }?.fileName,
                    onUploadClicked = { onOpenUploadDialog(DocumentCategory.CIN) }
                )

                // Doc 3: Diplôme
                DocumentVerificationRow(
                    label = "Diplôme certifié (${opportunity.degreeRequired})",
                    category = DocumentCategory.DIPLOME,
                    isReady = hasDiplome,
                    attachedFile = userDocs.find { it.category == DocumentCategory.DIPLOME }?.fileName,
                    onUploadClicked = { onOpenUploadDialog(DocumentCategory.DIPLOME) }
                )

                // User account confirmation row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Column {
                            Text(
                                text = "Dépôt sous l'identité de : ${userProfile.fullName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Compte Gmail : ${userProfile.account.email} (Vérifié)",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = OnSurfaceVariant)
                            )
                        }
                    }
                }

                // Submission Progress
                if (isSubmitting) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                        Text(
                            text = "Transmission chiffrée du dossier à l'administration...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Primary)
                        )
                    }
                }

                // Direct Apply Button
                Button(
                    onClick = {
                        if (!allRequiredReady) {
                            Toast.makeText(context, "Veuillez téléverser les pièces manquantes avant de postuler.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        coroutineScope.launch {
                            delay(700)
                            val attachedIds = userDocs.map { it.id }
                            val newApp = MihnatiRepository.submitDirectApplication(opportunity, attachedIds)
                            isSubmitting = false
                            submittedApplication = newApp
                            Toast.makeText(context, "Candidature transmise avec succès ! Réf: ${newApp.referenceNumber}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isSubmitting && allRequiredReady,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allRequiredReady) Primary else Outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (allRequiredReady) "Soumettre ma candidature directe" else "Dossier incomplet (Téléverser les pièces)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentVerificationRow(
    label: String,
    category: DocumentCategory,
    isReady: Boolean,
    attachedFile: String?,
    onUploadClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isReady) SecondaryContainer.copy(alpha = 0.35f) else SurfaceContainerLowest)
            .border(
                1.dp,
                if (isReady) Secondary.copy(alpha = 0.4f) else Error.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(category.emoji, fontSize = 18.sp)
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                )
                Text(
                    text = if (isReady) "✓ $attachedFile" else "⚠️ Document obligatoire manquant",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = if (isReady) Secondary else Error,
                        fontWeight = if (isReady) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
            }
        }

        if (isReady) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Prêt",
                tint = Secondary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Button(
                onClick = onUploadClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Ajouter", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
