package com.mihnati.app.ui.components

import com.mihnati.app.translation.autoTranslate

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.*
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
import com.mihnati.app.ui.theme.*

/**
 * Moroccan Public Sector Salary Scale Simulator
 * Grille indiciaire de la fonction publique marocaine (Échelles 8, 9, 10, 11)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalarySimulatorBottomSheet(
    initialGrade: String = "Échelle 11",
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val grades = listOf(
        "Échelle 11" to "Administrateur 2ème grade / Ingénieur d'État (Bac+5)",
        "Échelle 10" to "Administrateur 3ème grade / Technicien Spécialisé Principal (Bac+3)",
        "Échelle 9" to "Technicien 3ème grade / Rédacteur (Bac+2)",
        "Échelle 8" to "Adjoint technique / Agent administratif (Bac / Niveau Bac)"
    )

    var selectedGrade by remember { mutableStateOf(if (initialGrade.contains("10")) "Échelle 10" else if (initialGrade.contains("9")) "Échelle 9" else if (initialGrade.contains("8")) "Échelle 8" else "Échelle 11") }
    var hasMinistryBonus by remember { mutableStateOf(true) } // Prime de ministère (MEF, DGI, Intérieur)

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💰", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = AppStrings.salarySimulatorTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        Text(
                            text = AppStrings.salarySimulatorOfficialGrid,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            // Grade Selector Chips
            Text(
                text = AppStrings.chooseScaleLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Outline,
                    letterSpacing = 1.sp
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                grades.forEach { (grade, subtitle) ->
                    val isSelected = selectedGrade == grade
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryContainer.copy(alpha = 0.15f) else SurfaceContainerLowest)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Primary else SurfaceContainerHighest,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedGrade = grade }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = grade,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Primary else OnSurface
                                    )
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedGrade = grade },
                                colors = RadioButtonDefaults.colors(selectedColor = Primary)
                            )
                        }
                    }
                }
            }

            // Ministry Bonus Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLowest)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.ministryBonusLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = AppStrings.ministryBonusSub,
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant, fontSize = 11.sp)
                    )
                }
                Switch(
                    checked = hasMinistryBonus,
                    onCheckedChange = { hasMinistryBonus = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Secondary)
                )
            }

            // Calculation Result Card
            val (baseNet, totalNet, brut, cmrAmo) = when (selectedGrade) {
                "Échelle 11" -> Quadruple(
                    "9 250 DH",
                    if (hasMinistryBonus) "11 400 DH / mois" else "9 250 DH / mois",
                    "13 800 DH",
                    "- 2 400 DH"
                )
                "Échelle 10" -> Quadruple(
                    "5 850 DH",
                    if (hasMinistryBonus) "6 950 DH / mois" else "5 850 DH / mois",
                    "8 400 DH",
                    "- 1 450 DH"
                )
                "Échelle 9" -> Quadruple(
                    "4 200 DH",
                    if (hasMinistryBonus) "4 900 DH / mois" else "4 200 DH / mois",
                    "5 800 DH",
                    "- 900 DH"
                )
                else -> Quadruple(
                    "3 600 DH",
                    if (hasMinistryBonus) "4 150 DH / mois" else "3 600 DH / mois",
                    "4 800 DH",
                    "- 650 DH"
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SecondaryContainer.copy(alpha = 0.45f))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = AppStrings.netSalaryEstimated,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = totalNet,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Secondary
                        )
                    )

                    HorizontalDivider(color = OnSecondaryContainer.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(AppStrings.brutTreatment, style = MaterialTheme.typography.bodySmall)
                        Text(brut, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(AppStrings.socialDeductions, style = MaterialTheme.typography.bodySmall)
                        Text(cmrAmo, style = MaterialTheme.typography.bodySmall.copy(color = Error))
                    }
                    if (hasMinistryBonus) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(AppStrings.ministryBonusAmount, style = MaterialTheme.typography.bodySmall)
                            Text("+ ~2 150 DH", style = MaterialTheme.typography.bodySmall.copy(color = Secondary, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            Text(
                text = AppStrings.salaryDisclaimer,
                style = MaterialTheme.typography.bodySmall.copy(color = Outline, fontSize = 11.sp, lineHeight = 16.sp)
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Concours Documents Checklist Dialog
 */
@Composable
fun ConcoursChecklistDialog(
    onDismiss: () -> Unit
) {
    val isAr = LanguageManager.isArabic
    val doc1 = autoTranslate("Copie certifiée conforme du diplôme national ou attestation de réussite")
    val doc2 = autoTranslate("Arrêté d'équivalence publié au Bulletin Officiel (pour diplômes privés ou étrangers)")
    val doc3 = autoTranslate("Copie de la Carte Nationale d'Identité Électronique (CNIE recto-verso)")
    val doc4 = autoTranslate("Curriculum Vitae (CV) actualisé avec photo d'identité récente")
    val doc5 = autoTranslate("Récépissé ou fiche d'inscription générée sur emploi-public.ma")
    val doc6 = autoTranslate("Extrait du casier judiciaire (Fiche anthropométrique)")
    val doc7 = autoTranslate("Certificat médical d'aptitude physique (pour certains ministères)")

    val items = remember(doc1, doc2, doc3, doc4, doc5, doc6, doc7) {
        mutableStateListOf(
            ChecklistItem(doc1, isChecked = false),
            ChecklistItem(doc2, isChecked = false),
            ChecklistItem(doc3, isChecked = false),
            ChecklistItem(doc4, isChecked = false),
            ChecklistItem(doc5, isChecked = false),
            ChecklistItem(doc6, isChecked = false),
            ChecklistItem(doc7, isChecked = false)
        )
    }

    val completedCount = items.count { it.isChecked }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Checklist, contentDescription = null, tint = Primary)
                Text(AppStrings.checklistTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${AppStrings.readyDocsCount} $completedCount / ${items.size}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Text("${(completedCount * 100) / items.size}%", color = if (completedCount == items.size) Secondary else Primary, fontWeight = FontWeight.Bold)
                }

                LinearProgressIndicator(
                    progress = { completedCount.toFloat() / items.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (completedCount == items.size) Secondary else Primary,
                    trackColor = SurfaceContainerHigh
                )

                Spacer(modifier = Modifier.height(4.dp))

                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { items[index] = item.copy(isChecked = !item.isChecked) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { items[index] = item.copy(isChecked = it) },
                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                        )
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (item.isChecked) OnSurface else OnSurfaceVariant,
                                fontWeight = if (item.isChecked) FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(AppStrings.closeAction)
            }
        }
    )
}

private data class ChecklistItem(val text: String, val isChecked: Boolean)

/**
 * Motivation Letter Generator Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotivationLetterBottomSheet(
    opportunityTitle: String,
    organization: String,
    reference: String,
    candidateName: String = "Yassine Benali",
    degreeName: String = "Ingénieur d'État en Génie Informatique (ENSIAS)",
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val isAr = LanguageManager.isArabic
    val letterContent = """
À l'attention de Monsieur / Madame le Responsable du Recrutement
$organization
Royaume du Maroc

Objet : Candidature au poste de $opportunityTitle
Référence du concours : $reference

Madame, Monsieur,

Titulaire d'un diplôme d'$degreeName, j'ai l'honneur de vous soumettre ma candidature pour le concours de recrutement de $opportunityTitle au sein du $organization.

Au cours de mon parcours académique et de mes expériences pratiques, j'ai développé de solides compétences techniques, une rigueur méthodologique et une réelle capacité d'adaptation aux projets d'envergure. Intégrer votre institution représente pour moi l'opportunité de mettre mes compétences au service de l'intérêt général et du développement des services publics au Maroc.

Dynamique, investi et doté d'un fort sens des responsabilités, je serais honoré d'échanger avec vous lors d'un prochain entretien afin de vous exposer plus en détail mes motivations et ma détermination.

Dans l'attente de votre réponse, je vous prie d'agréer, Madame, Monsieur, l'expression de mes salutations les plus distinguées.

$candidateName
Casablanca, Royaume du Maroc
        """.trimIndent()

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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = AppStrings.motivationLetterTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AppStrings.motivationLetterSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = letterContent,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OnSurface,
                        lineHeight = 20.sp
                    )
                )
            }

            val letterToastMsg = AppStrings.letterCopiedToast
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Lettre de Motivation Mihnati", letterContent)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, letterToastMsg, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(AppStrings.copyLetterAction, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

/**
 * Moroccan Diploma Eligibility Checker
 * Vérificateur d'éligibilité aux échelles et concours de l'État
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiplomaEligibilityBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isAr = LanguageManager.isArabic
    val diplomas = listOf(
            "Ingénieur d'État (ENSIAS, EMI, INPT, ENSA...)" to ("Échelle 11" to "Ingénieur 1er grade • Administrateur 2ème grade"),
            "Master / Master Spécialisé / DESA (Bac+5)" to ("Échelle 11" to "Administrateur 2ème grade • Concours MEF & Ministères"),
            "Licence Fondamentale ou Professionnelle (Bac+3)" to ("Échelle 10" to "Administrateur 3ème grade • Enseignement / Santé"),
            "DUT / BTS / DTS / Technicien Spécialisé (Bac+2)" to ("Échelle 9" to "Technicien 3ème grade • Douanes (ADII) / DGI"),
            "Baccalauréat National ou Équivalent" to ("Échelle 8" to "Adjoint technique / Agent administratif")
        )

    var selectedIndex by remember { mutableStateOf(0) }
    val (echelle, gradesDesc) = diplomas[selectedIndex].second

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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎓", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = AppStrings.eligibilityCheckerTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AppStrings.eligibilityCheckerSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Text(
                text = AppStrings.chooseDiplomaLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Outline,
                    letterSpacing = 1.sp
                )
            )

            diplomas.forEachIndexed { idx, (dipName, _) ->
                val isSelected = selectedIndex == idx
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) PrimaryContainer.copy(alpha = 0.12f) else SurfaceContainerLowest)
                        .border(
                            1.5.dp,
                            if (isSelected) Primary else SurfaceContainerHighest,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedIndex = idx }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dipName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Primary else OnSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedIndex = idx },
                            colors = RadioButtonDefaults.colors(selectedColor = Primary)
                        )
                    }
                }
            }

            // Result Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SecondaryContainer.copy(alpha = 0.45f))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.publicAccessGrade,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Secondary)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(echelle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = gradesDesc,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    )

                    HorizontalDivider(color = OnSecondaryContainer.copy(alpha = 0.2f))

                    Text(
                        text = autoTranslate("✓ Accès ouvert par concours national (Décret n° 2-04-403 fixant le statut des cadres de l'État)."),
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSecondaryContainer)
                    )
                    Text(
                        text = autoTranslate("✓ Pour les diplômes d'écoles privées ou étrangères : Arrêté d'équivalence ministériel publié au B.O. obligatoire."),
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSecondaryContainer)
                    )
                }
            }
        }
    }
}

/**
 * Moroccan Oral Exam Questions Guide
 * Guide des questions types de l'épreuve orale devant le jury de concours
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OralExamPrepBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isAr = LanguageManager.isArabic
        val questions = listOf(
            OralQuestion(
                q = autoTranslate("1. « Présentez-vous en 3 minutes devant le jury »"),
                tip = autoTranslate("Structurez en 3 axes : Parcours académique, projets réalisés et motivation à servir l'intérêt général."),
                keywords = listOf(autoTranslate("Rigueur"), autoTranslate("Service public"), autoTranslate("Sens des responsabilités"), autoTranslate("Travail d'équipe"))
            ),
            OralQuestion(
                q = autoTranslate("2. « Que savez-vous de la Charte de la Déconcentration et de la Régionalisation Avancée ? »"),
                tip = autoTranslate("Citez le Décret n° 2-17-618 fixant la Charte Nationale de la Déconcentration Administrative et le transfert des compétences aux services déconcentrés."),
                keywords = listOf(autoTranslate("Décret 2-17-618"), autoTranslate("Services Déconcentrés"), autoTranslate("Régionalisation Avancée"))
            ),
            OralQuestion(
                q = autoTranslate("3. « Quelles sont les avancées majeures de la Loi 55-19 sur la simplification des procédures administratives ? »"),
                tip = autoTranslate("Suppression de la légalisation de signature pour plusieurs actes, interdiction d'exiger plus d'une copie, portail Idarati.ma et délais stricts de traitement."),
                keywords = listOf(autoTranslate("Loi 55-19"), autoTranslate("Portail Idarati.ma"), autoTranslate("Numérisation"), autoTranslate("Délais de réponse"))
            ),
            OralQuestion(
                q = autoTranslate("4. « Comment réagiriez-vous si votre supérieur vous donnait une instruction discutable ? »"),
                tip = autoTranslate("Rappeler le devoir d'obéissance hiérarchique selon le Statut Général de la Fonction Publique (Dahir 1-58-008), tout en formulant des réserves écrites professionnelles en cas d'irrégularité manifeste."),
                keywords = listOf(autoTranslate("Dahir 1-58-008"), autoTranslate("Devoir de réserve"), autoTranslate("Hiérarchie"), autoTranslate("Éthique"))
            )
        )

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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎙️", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = AppStrings.oralExamTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AppStrings.oralExamSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            questions.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = item.q,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        )
                        Text(
                            text = item.tip,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OnSurface,
                                lineHeight = 18.sp
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.keywords.forEach { kw ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceContainerLow)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = kw,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = OnSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class OralQuestion(val q: String, val tip: String, val keywords: List<String>)

