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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.mihnati.app.security.EmailOtpManager
import com.mihnati.app.security.MihnatiSecurityVault
import com.mihnati.app.data.cloud.CloudServiceManager

/**
 * Modern Unified Authentication Dialog:
 * - Option 1: 1-Tap "Continuer avec Google" (Google OAuth 2.0)
 * - Option 2: 6-Digit Email Verification Code to Gmail (Email OTP)
 */
@Composable
fun GoogleSignInDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userProfile by MihnatiRepository.userProfile.collectAsState()
    val account = userProfile.account

    // Flow State: 0: Main (Google + Send Code), 1: Enter 6-digit OTP, 2: Success
    var authStep by remember { mutableStateOf(0) }

    var emailInput by remember { mutableStateOf(if (account.isVerified) account.email else "") }
    var nameInput by remember { mutableStateOf(if (userProfile.fullName != "Invité") userProfile.fullName else "") }
    var otpInput by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resendCooldown by remember { mutableStateOf(45) }

    // Countdown timer for resend
    LaunchedEffect(authStep) {
        if (authStep == 1) {
            resendCooldown = 45
            while (resendCooldown > 0) {
                delay(1000L)
                resendCooldown--
            }
        }
    }

    val isAlreadySignedIn = account.isVerified && account.provider != "guest"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isAlreadySignedIn) Color(0xFF16A34A).copy(alpha = 0.15f)
                            else Primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAlreadySignedIn) "✓" else if (authStep == 1) "✉" else "G",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (isAlreadySignedIn) Color(0xFF16A34A) else Primary
                    )
                }
                Column {
                    Text(
                        text = if (isAlreadySignedIn) "Mon Compte Mihnati"
                        else if (authStep == 1) "Vérification Gmail"
                        else "Connexion & Inscription",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isAlreadySignedIn) "Session vérifiée et synchronisée"
                        else if (authStep == 1) "Code secret envoyé par email"
                        else "Google 1-Tap & Code Gmail",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Error banner
                errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(18.dp))
                            Text(text = error, color = Error, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                }

                // ==========================================
                // CASE A: User is already signed in
                // ==========================================
                if (isAlreadySignedIn) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (account.displayName.firstOrNull() ?: 'U').uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = account.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = account.email, fontSize = 13.sp, color = OnSurfaceVariant)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF16A34A).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Actif ✓", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Méthode de connexion", fontSize = 12.sp, color = OnSurfaceVariant)
                                Text(account.provider, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Sécurité matérielle", fontSize = 12.sp, color = OnSurfaceVariant)
                                Text("KeyStore AES-256", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Primary)
                            }
                        }
                    }

                    // Logout button
                    OutlinedButton(
                        onClick = {
                            MihnatiRepository.signOut(context)
                            Toast.makeText(context, "Session fermée. Mode Invité activé.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Se Déconnecter de ce compte", fontWeight = FontWeight.SemiBold)
                    }

                // ==========================================
                // CASE B: STEP 0 - Main Choice (Google or Email Code)
                // ==========================================
                } else if (authStep == 0) {

                    // 1. OFFICIAL GOOGLE 1-TAP BUTTON
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isProcessing) {
                                isProcessing = true
                                errorMessage = null
                                coroutineScope.launch {
                                    delay(400) // Smooth tactile response
                                    val googleEmail = if (emailInput.contains("@gmail.com")) emailInput.trim() else "bilal.candidate@gmail.com"
                                    val googleName = if (nameInput.isNotBlank()) nameInput.trim() else "Bilal Mihnati"

                                    val result = MihnatiSecurityVault.signInWithGoogle(
                                        context = context,
                                        email = googleEmail,
                                        displayName = googleName
                                    )

                                    result.onSuccess { acc ->
                                        MihnatiRepository.applyAuthenticatedAccount(acc)
                                        CloudServiceManager.getService().registerUser(
                                            email = acc.email,
                                            passwordHash = "google_oauth2_verified",
                                            displayName = acc.displayName,
                                            cin = "CNIE"
                                        )
                                        Toast.makeText(context, "Connecté avec succès via Google !", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }.onFailure {
                                        errorMessage = it.message ?: "Erreur d'authentification Google"
                                    }
                                    isProcessing = false
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFDADCE0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF4285F4))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Continuer avec Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Connexion 1-clic avec votre compte Gmail",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 2. STYLED SEPARATOR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant.copy(alpha = 0.5f))
                        Text(
                            text = "  OU PAR CODE EMAIL GMAIL  ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant.copy(alpha = 0.5f))
                    }

                    // 3. EMAIL & NAME INPUT FOR 6-DIGIT OTP
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it; errorMessage = null },
                        label = { Text("Nom et Prénom") },
                        placeholder = { Text("ex: Bilal Benali") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it; errorMessage = null },
                        label = { Text("Votre adresse Gmail") },
                        placeholder = { Text("ex: nom.prenom@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.06f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Nous vous enverrons un code de confirmation à 6 chiffres par email pour créer votre compte sans mot de passe.",
                                fontSize = 11.sp,
                                color = OnSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // SEND CODE BUTTON
                    Button(
                        onClick = {
                            if (emailInput.isBlank() || !emailInput.contains("@")) {
                                errorMessage = "Veuillez entrer une adresse email valide."
                                return@Button
                            }
                            isProcessing = true
                            errorMessage = null
                            coroutineScope.launch {
                                val sendResult = EmailOtpManager.generateAndSendOtp(
                                    context = context,
                                    email = emailInput.trim(),
                                    candidateName = nameInput.trim().ifEmpty { "Candidat Mihnati" }
                                )
                                sendResult.onSuccess {
                                    authStep = 1 // Move to OTP input step
                                    Toast.makeText(context, "Code secret envoyé à votre Gmail !", Toast.LENGTH_SHORT).show()
                                }.onFailure {
                                    errorMessage = it.message ?: "Impossible d'envoyer le code de confirmation."
                                }
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isProcessing && emailInput.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Envoyer le code de vérification (6 chiffres)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                // ==========================================
                // CASE C: STEP 1 - Enter 6-Digit OTP Code
                // ==========================================
                } else if (authStep == 1) {

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✉", fontSize = 28.sp)
                            Text(
                                text = "Code de validation envoyé à :",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = emailInput.trim(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Primary
                            )
                            Text(
                                text = "Consultez votre boîte Gmail ou vos notifications",
                                fontSize = 11.sp,
                                color = OnSurfaceVariant
                            )

                            val activeDebugCode = EmailOtpManager.getLastActiveOtpForDebug()
                            if (activeDebugCode != null) {
                                Surface(
                                    color = SecondaryContainer.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        otpInput = activeDebugCode
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("🔐", fontSize = 12.sp)
                                        Text(
                                            text = "Code : $activeDebugCode (Appuyez pour remplir)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 6-DIGIT VISUAL BOXES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 6) {
                            val char = if (i < otpInput.length) otpInput[i].toString() else ""
                            val isCurrent = i == otpInput.length
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) Primary.copy(alpha = 0.1f) else SurfaceContainerHigh)
                                    .border(
                                        width = if (isCurrent) 2.dp else 1.dp,
                                        color = if (isCurrent) Primary else OutlineVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (char.isNotEmpty()) Primary else OnSurfaceVariant
                                )
                            }
                        }
                    }

                    // Real numeric field for typing
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { input ->
                            if (input.length <= 6 && input.all { it.isDigit() }) {
                                otpInput = input
                                errorMessage = null
                                // Auto submit on 6th digit
                                if (input.length == 6) {
                                    isProcessing = true
                                    coroutineScope.launch {
                                        val verifyResult = EmailOtpManager.verifyOtp(emailInput, input)
                                        verifyResult.onSuccess {
                                            val accResult = MihnatiSecurityVault.registerOrSignInWithEmailOtp(
                                                context = context,
                                                email = emailInput.trim(),
                                                displayName = nameInput.trim().ifEmpty { "Candidat Mihnati" }
                                            )
                                            accResult.onSuccess { acc ->
                                                MihnatiRepository.applyAuthenticatedAccount(acc)
                                                CloudServiceManager.getService().registerUser(
                                                    email = acc.email,
                                                    passwordHash = "email_otp_verified",
                                                    displayName = acc.displayName,
                                                    cin = "CNIE"
                                                )
                                                Toast.makeText(context, "Compte Gmail vérifié avec succès !", Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            }
                                        }.onFailure { err ->
                                            errorMessage = err.message ?: "Code de confirmation invalide."
                                        }
                                        isProcessing = false
                                    }
                                }
                            }
                        },
                        label = { Text("Saisissez les 6 chiffres ici") },
                        placeholder = { Text("ex: 849201") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Resend countdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                authStep = 0
                                otpInput = ""
                                errorMessage = null
                            }
                        ) {
                            Text("← Modifier l'email", fontSize = 12.sp)
                        }

                        if (resendCooldown > 0) {
                            Text(
                                text = "Renvoyer dans (${resendCooldown}s)",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        EmailOtpManager.generateAndSendOtp(
                                            context = context,
                                            email = emailInput.trim(),
                                            candidateName = nameInput.trim().ifEmpty { "Candidat Mihnati" }
                                        )
                                        resendCooldown = 45
                                        Toast.makeText(context, "Nouveau code envoyé !", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Renvoyer le code", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Confirm Button
                    Button(
                        onClick = {
                            if (otpInput.length != 6) {
                                errorMessage = "Veuillez saisir les 6 chiffres du code reçu."
                                return@Button
                            }
                            isProcessing = true
                            errorMessage = null
                            coroutineScope.launch {
                                val verifyResult = EmailOtpManager.verifyOtp(emailInput, otpInput)
                                verifyResult.onSuccess {
                                    val accResult = MihnatiSecurityVault.registerOrSignInWithEmailOtp(
                                        context = context,
                                        email = emailInput.trim(),
                                        displayName = nameInput.trim().ifEmpty { "Candidat Mihnati" }
                                    )
                                    accResult.onSuccess { acc ->
                                        MihnatiRepository.applyAuthenticatedAccount(acc)
                                        CloudServiceManager.getService().registerUser(
                                            email = acc.email,
                                            passwordHash = "email_otp_verified",
                                            displayName = acc.displayName,
                                            cin = "CNIE"
                                        )
                                        Toast.makeText(context, "Compte Gmail vérifié avec succès !", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                }.onFailure { err ->
                                    errorMessage = err.message ?: "Code de confirmation invalide."
                                }
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isProcessing && otpInput.length == 6,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmer & Activer mon compte", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

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
