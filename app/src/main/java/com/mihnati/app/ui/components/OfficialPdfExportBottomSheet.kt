package com.mihnati.app.ui.components

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
import com.mihnati.app.data.model.Opportunity
import com.mihnati.app.data.repository.MihnatiRepository
import com.mihnati.app.pdf.CandidateDossierInput
import com.mihnati.app.pdf.OfficialDossierPdfGenerator
import com.mihnati.app.translation.autoTranslate
import com.mihnati.app.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialPdfExportBottomSheet(
    targetOpportunity: Opportunity? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val userProfile by MihnatiRepository.userProfile.collectAsState()

    var candidateName by remember { mutableStateOf(if (userProfile.fullName.isNotBlank() && !userProfile.fullName.contains("Invit")) userProfile.fullName else "Yassine Mansouri") }
    var candidateCnie by remember { mutableStateOf("BE892415") }
    var candidatePhone by remember { mutableStateOf("+212 661-234567") }
    var candidateEmail by remember { mutableStateOf("yassine.mansouri@email.ma") }
    var candidateCity by remember { mutableStateOf(if (userProfile.location.isNotBlank() && userProfile.location != "Maroc") userProfile.location else "Casablanca") }
    var candidateAddress by remember { mutableStateOf("Quartier Oasis, Rue 14 N° 28") }

    var candidateDiploma by remember { mutableStateOf(targetOpportunity?.degreeRequired ?: "Master en Droit des Affaires & Gestion Publique") }
    var candidateUniversity by remember { mutableStateOf("Universite Hassan II - Casablanca") }

    var targetMinistry by remember { mutableStateOf(targetOpportunity?.organization ?: "Ministere de l'Economie et des Finances") }
    var targetPoste by remember { mutableStateOf(targetOpportunity?.title ?: "Administrateur 2eme grade (Echelle 11)") }
    var referenceNumber by remember { mutableStateOf(targetOpportunity?.reference ?: "CONCOURS-FP-2026-11") }
    var echelle by remember { mutableStateOf(if (targetPoste.contains("10")) "Echelle 10" else if (targetPoste.contains("9")) "Echelle 9" else "Echelle 11") }

    var generatedFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

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
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🖨️", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = autoTranslate("Export Dossier PDF Officiel"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        Text(
                            text = autoTranslate("Demande manuscrite + CV normalise fonction publique"),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            // Dossier Content Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = autoTranslate("Composantes du dossier PDF (Format A4 officiel) :"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📄", fontSize = 14.sp)
                        Text(
                            text = autoTranslate("Page 1 : Demande manuscrite adressee a Monsieur le Ministre / Directeur"),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurface)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🎓", fontSize = 14.sp)
                        Text(
                            text = autoTranslate("Page 2 : Curriculum Vitae normalise avec attestation sur l'honneur"),
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurface)
                        )
                    }
                }
            }

            // Generated Success State
            if (generatedFile != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = autoTranslate("Dossier PDF Officiel genere avec succes !"),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        )
                        Text(
                            text = generatedFile!!.name,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF2E7D32))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    OfficialDossierPdfGenerator.openPdf(context, generatedFile!!)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(autoTranslate("Ouvrir le PDF"))
                            }

                            OutlinedButton(
                                onClick = {
                                    OfficialDossierPdfGenerator.sharePdf(
                                        context,
                                        generatedFile!!,
                                        subject = "Dossier Candidature - " + targetPoste
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(autoTranslate("Partager / Imprimer"))
                            }
                        }
                    }
                }
            }

            // Editable Candidate Inputs
            Text(
                text = autoTranslate("Informations du Candidat :"),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            )

            OutlinedTextField(
                value = candidateName,
                onValueChange = { candidateName = it },
                label = { Text(autoTranslate("Nom et Prenom")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = candidateCnie,
                    onValueChange = { candidateCnie = it },
                    label = { Text(autoTranslate("Numero CNIE")) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = candidateCity,
                    onValueChange = { candidateCity = it },
                    label = { Text(autoTranslate("Ville de residence")) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = candidatePhone,
                    onValueChange = { candidatePhone = it },
                    label = { Text(autoTranslate("Telephone")) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = candidateEmail,
                    onValueChange = { candidateEmail = it },
                    label = { Text(autoTranslate("Email")) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = candidateAddress,
                onValueChange = { candidateAddress = it },
                label = { Text(autoTranslate("Adresse complete")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = autoTranslate("Diplome et Universite :"),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            )

            OutlinedTextField(
                value = candidateDiploma,
                onValueChange = { candidateDiploma = it },
                label = { Text(autoTranslate("Intitule du Diplome")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = candidateUniversity,
                onValueChange = { candidateUniversity = it },
                label = { Text(autoTranslate("Etablissement / Universite")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = autoTranslate("Concours et Administration cible :"),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            )

            OutlinedTextField(
                value = targetMinistry,
                onValueChange = { targetMinistry = it },
                label = { Text(autoTranslate("Administration / Ministere")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = targetPoste,
                onValueChange = { targetPoste = it },
                label = { Text(autoTranslate("Intitule du Poste / Cadre")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Submit / Generate Button
            Button(
                onClick = {
                    isGenerating = true
                    val input = CandidateDossierInput(
                        fullName = candidateName,
                        cnie = candidateCnie,
                        phone = candidatePhone,
                        email = candidateEmail,
                        city = candidateCity,
                        address = candidateAddress,
                        diploma = candidateDiploma,
                        university = candidateUniversity,
                        targetMinistry = targetMinistry,
                        targetPoste = targetPoste,
                        referenceNumber = referenceNumber,
                        echelle = echelle
                    )
                    val file = OfficialDossierPdfGenerator.generateDossierPdf(context, input)
                    isGenerating = false
                    if (file != null) {
                        generatedFile = file
                        Toast.makeText(context, "Dossier PDF généré avec succès", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Erreur de génération PDF", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !isGenerating && candidateName.isNotBlank() && candidateCnie.isNotBlank()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = autoTranslate("Generer le Dossier PDF Officiel (Format A4)"),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
