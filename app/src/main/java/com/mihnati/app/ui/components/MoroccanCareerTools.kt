package com.mihnati.app.ui.components

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
                            text = "Simulateur de Salaire Public",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        Text(
                            text = "Grille indiciaire officielle du Royaume du Maroc",
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
                text = "CHOIX DE L'ÉCHELLE ADMINISTRATIVE",
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
                        text = "Primes spécifiques de ministère",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Ex: Finances (MEF), Impôts (DGI), Douanes",
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
                        text = "SALAIRE NET MENSUEL ESTIMÉ",
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
                        Text("Traitement brut indiciaire :", style = MaterialTheme.typography.bodySmall)
                        Text(brut, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Retenues sociales (CMR & AMO) :", style = MaterialTheme.typography.bodySmall)
                        Text(cmrAmo, style = MaterialTheme.typography.bodySmall.copy(color = Error))
                    }
                    if (hasMinistryBonus) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Indemnité de rendement / primes :", style = MaterialTheme.typography.bodySmall)
                            Text("+ ~2 150 DH", style = MaterialTheme.typography.bodySmall.copy(color = Secondary, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            Text(
                text = "ℹ️ Estimation indicative basée sur le décret n° 2-04-403 fixant le statut des administrateurs et techniciens de l'État et l'accord de revalorisation salariale 2024-2026.",
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
    val items = remember {
        mutableStateListOf(
            ChecklistItem("Copie certifiée conforme du diplôme national ou attestation de réussite", true),
            ChecklistItem("Arrêté d'équivalence publié au Bulletin Officiel (pour diplômes privés ou étrangers)", true),
            ChecklistItem("Copie de la Carte Nationale d'Identité Électronique (CNIE recto-verso)", true),
            ChecklistItem("Curriculum Vitae (CV) actualisé avec photo d'identité récente", true),
            ChecklistItem("Récépissé ou fiche d'inscription générée sur emploi-public.ma", false),
            ChecklistItem("Extrait du casier judiciaire (Fiche anthropométrique)", false),
            ChecklistItem("Certificat médical d'aptitude physique (pour certains ministères)", false)
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
                Text("Dossier de Candidature", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
                    Text("Pièces prêtes : $completedCount / ${items.size}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
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
                Text("Fermer")
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
                            text = "Lettre de Motivation Pré-remplie",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Adaptée à votre profil et au concours officiel",
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

            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Lettre de Motivation Mihnati", letterContent)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Lettre copiée dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copier la lettre de motivation", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
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
                            text = "Vérificateur d'Éligibilité",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Correspondance officielle Diplôme ➔ Échelle de l'État",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Text(
                text = "CHOISISSEZ VOTRE DIPLÔME ACTUEL :",
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
                            text = "GRADE D'ACCÈS DE LA FONCTION PUBLIQUE",
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
                        text = "✓ Accès ouvert par concours national (Décret n° 2-04-403 fixant le statut des cadres de l'État).",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSecondaryContainer)
                    )
                    Text(
                        text = "✓ Pour les diplômes d'écoles privées ou étrangères : Arrêté d'équivalence ministériel publié au B.O. obligatoire.",
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

    val questions = listOf(
        OralQuestion(
            q = "1. « Présentez-vous en 3 minutes devant la commission »",
            tip = "Structurez en 3 temps : Parcours académique, projets réalisés, et pourquoi vous avez choisi de servir l'intérêt général au sein de ce ministère.",
            keywords = listOf("Rigueur", "Service public", "Sens du devoir", "Esprit d'équipe")
        ),
        OralQuestion(
            q = "2. « Que savez-vous de la déconcentration et de la régionalisation avancée ? »",
            tip = "Mentionnez la Charte nationale de la déconcentration administrative (Décret n° 2-17-618) et le transfert des compétences des ministères centraux vers les délégations régionales.",
            keywords = listOf("Décret 2-17-618", "Services déconcentrés", "Délégation régionale")
        ),
        OralQuestion(
            q = "3. « Qu'apporte la loi 55-19 aux usagers des administrations marocaines ? »",
            tip = "Elle interdit d'exiger plus d'un exemplaire d'un document, supprime la légalisation de signature pour certaines démarches et fixe un délai maximum de réponse de l'administration.",
            keywords = listOf("Loi 55-19", "Idarati.ma", "Simplification des procédures", "Digitalisation")
        ),
        OralQuestion(
            q = "4. « Comment réagiriez-vous face à une instruction d'un supérieur qui vous semble discutable ? »",
            tip = "Rappelez le respect du principe hiérarchique du statut général de la fonction publique (Dahir n° 1-58-008) tout en privilégiant le dialogue et la conformité aux textes légaux.",
            keywords = listOf("Dahir 1-58-008", "Devoir de réserve", "Loyauté", "Respect hiérarchique")
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
                            text = "Préparation à l'Oral du Concours",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Questions récurrentes posées par les jurys ministériels",
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

