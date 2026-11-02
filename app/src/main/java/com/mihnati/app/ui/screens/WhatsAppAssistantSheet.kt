package com.mihnati.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.data.model.AiChatMessage
import com.mihnati.app.data.model.WhatsAppApiProvider
import com.mihnati.app.data.model.WhatsAppConfig
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.ui.components.DemoDataBanner
import com.mihnati.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppAssistantSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val chatMessages by MihnatiRepository.chatMessages.collectAsState()
    val whatsAppConfig by MihnatiRepository.whatsAppConfig.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showConfigDialog by remember { mutableStateOf(false) }

    // Dialog state
    var selectedProvider by remember { mutableStateOf(whatsAppConfig.provider) }
    var inputPhone by remember { mutableStateOf(whatsAppConfig.phoneNumber) }
    var inputApiKey by remember { mutableStateOf(whatsAppConfig.apiKeyOrToken) }
    var inputBusinessId by remember { mutableStateOf(whatsAppConfig.accountOrBusinessId) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🤖", fontSize = 22.sp)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (LanguageManager.isArabic) "المساعد الذكي مهنتي" else "Assistant IA Mihnati",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Secondary)
                            )
                        }
                        Text(
                            text = if (LanguageManager.isArabic) {
                                if (whatsAppConfig.isConfigured) "متصل : ${whatsAppConfig.provider.displayName}" else "وضع استشارات ومباريات الوظيفة العمومية"
                            } else {
                                if (whatsAppConfig.isConfigured) "Connecté : ${whatsAppConfig.provider.displayName}" else "Mode WhatsApp Direct & Conseils Concours"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // API Settings Gear
                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuration API",
                            tint = Outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Direct WhatsApp Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF25D366))
                            .clickable {
                                try {
                                    val phoneToUse = if (whatsAppConfig.phoneNumber.isNotBlank()) whatsAppConfig.phoneNumber else "+212600000000"
                                    val url = "https://api.whatsapp.com/send?phone=${phoneToUse.replace("+", "")}&text=" + Uri.encode("Bonjour Mihnati, je souhaite des informations sur les concours et offres.")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Application WhatsApp introuvable", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "WhatsApp",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            DemoDataBanner(
                label = if (LanguageManager.isArabic) "إجابات فورية مدربة وموثقة وفق الأنظمة والمراسيم الوزارية المغربية" else "Réponses entraînées sur la réglementation de la Fonction Publique marocaine"
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = SurfaceContainerHigh)

            // Chat Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatMessages) { message ->
                    ChatMessageBubble(
                        message = message,
                        onSuggestedActionClick = { actionText ->
                            if (actionText.contains("WhatsApp", ignoreCase = true)) {
                                try {
                                    val url = "https://api.whatsapp.com/send?phone=+212600000000&text=" + Uri.encode("Bonjour Mihnati, j'ai une question sur mon dossier de concours.")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp non installé", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                MihnatiRepository.sendUserMessage(actionText)
                            }
                        }
                    )
                }
            }

            // Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = SurfaceContainerLowest,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (LanguageManager.isArabic) "اطرح سؤالك (المباريات، الشواهد، السلالم والوثائق المطلوبة...)" else "Posez votre question (concours, éligibilité, pièces...)",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Outline)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                MihnatiRepository.sendUserMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Envoyer",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // WhatsApp API Configuration Dialog
    if (showConfigDialog) {
        var isApiKeyHidden by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Primary
                    )
                    Text(
                        text = if (LanguageManager.isArabic) "إعدادات بوابة واتساب الرسمية" else "Configuration Passerelle WhatsApp",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (LanguageManager.isArabic) "اضبط إعدادات الربط لتوصيل المساعد الذكي برقم واتساب للأعمال أو بوابة الإرسال المخصصة." else "Configurez vos identifiants pour connecter l'assistant IA directement à un numéro WhatsApp Business dédié.",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )

                    // Provider Options
                    Text(
                        text = if (LanguageManager.isArabic) "مزود الخدمة :" else "Fournisseur de service :",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    WhatsAppApiProvider.values().forEach { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedProvider == provider) SurfaceContainerHigh else SurfaceContainerLow)
                                .clickable { selectedProvider = provider }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RadioButton(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider }
                            )
                            Text(
                                text = provider.displayName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (selectedProvider == provider) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputPhone,
                        onValueChange = { inputPhone = it },
                        label = { Text(if (LanguageManager.isArabic) "رقم واتساب (+212...)" else "Numéro WhatsApp (+212...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (selectedProvider != WhatsAppApiProvider.WHATSAPP_DIRECT) {
                        OutlinedTextField(
                            value = inputApiKey,
                            onValueChange = { inputApiKey = it },
                            label = { Text(if (LanguageManager.isArabic) "مفتاح الربط API / الرمز" else "Clé API / Token d'accès") },
                            visualTransformation = if (isApiKeyHidden) PasswordVisualTransformation() else VisualTransformation.None,
                            trailingIcon = {
                                IconButton(onClick = { isApiKeyHidden = !isApiKeyHidden }) {
                                    Icon(
                                        imageVector = if (isApiKeyHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = inputBusinessId,
                            onValueChange = { inputBusinessId = it },
                            label = { Text("Phone Number ID / Account SID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newConfig = WhatsAppConfig(
                            provider = selectedProvider,
                            phoneNumber = inputPhone,
                            apiKeyOrToken = inputApiKey,
                            accountOrBusinessId = inputBusinessId,
                            isConfigured = selectedProvider == WhatsAppApiProvider.WHATSAPP_DIRECT || inputApiKey.isNotBlank()
                        )
                        MihnatiRepository.updateWhatsAppConfig(newConfig)
                        Toast.makeText(context, "Configuration WhatsApp enregistrée !", Toast.LENGTH_SHORT).show()
                        showConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(if (LanguageManager.isArabic) "حفظ الإعدادات" else "Sauvegarder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text(if (LanguageManager.isArabic) "إلغاء" else "Annuler")
                }
            }
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: AiChatMessage,
    onSuggestedActionClick: (String) -> Unit
) {
    val isUser = message.isFromUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) PrimaryContainer else SurfaceContainerLowest)
                .padding(14.dp)
        ) {
            val localizedBubbleText = if (LanguageManager.isArabic && !isUser) {
                when {
                    message.text.contains("Je suis l'Assistant IA Mihnati", ignoreCase = true) ->
                        "مرحباً بك! أنا المساعد الذكي لتطبيق مهنتي. يمكنني مساعدتك في التحضير لمباريات الوظيفة العمومية بالمغرب (وزارة المالية، إدارة الجمارك، الأمن الوطني)، تحليل شواهدك وتحديد السلم المناسب، أو ضبط تذكيرات مواعيد إيداع الترشيحات على تقويمك."
                    else -> message.text
                }
            } else message.text
            Text(
                text = localizedBubbleText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isUser) Color.White else OnSurface,
                    lineHeight = 20.sp
                )
            )
        }

        // Suggested action chips
        if (!isUser && message.suggestedActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                message.suggestedActions.forEach { action ->
                    val localizedAction = if (LanguageManager.isArabic) {
                        when {
                            action.contains("Comment postuler au concours MEF", ignoreCase = true) -> "كيفية الترشح لمباراة وزارة المالية ؟"
                            action.contains("Quelles pièces pour l'ADII", ignoreCase = true) -> "وثائق مباراة إدارة الجمارك ؟"
                            action.contains("Équivalence", ignoreCase = true) -> "معادلة الشواهد والسلالم"
                            else -> action
                        }
                    } else action
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (action.contains("WhatsApp", ignoreCase = true)) Color(0xFF25D366).copy(alpha = 0.2f) else SurfaceContainerHigh
                            )
                            .clickable { onSuggestedActionClick(action) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = localizedAction,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (action.contains("WhatsApp", ignoreCase = true)) Color(0xFF075E54) else Primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
