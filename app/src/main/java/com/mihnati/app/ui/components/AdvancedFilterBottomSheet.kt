package com.mihnati.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihnati.app.translation.autoTranslate
import com.mihnati.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterBottomSheet(
    initialEchelle: Int?,
    initialLevel: String,
    initialRegion: String,
    initialPostsRange: String,
    initialSort: String,
    onApplyFilters: (echelle: Int?, level: String, region: String, postsRange: String, sort: String) -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentEchelle by remember { mutableStateOf(initialEchelle) }
    var currentLevel by remember { mutableStateOf(initialLevel) }
    var currentRegion by remember { mutableStateOf(initialRegion) }
    var currentPostsRange by remember { mutableStateOf(initialPostsRange) }
    var currentSort by remember { mutableStateOf(initialSort) }

    val echelles = listOf(
        null to "Toutes",
        11 to "Échelle 11 (Master / Ingénieur)",
        10 to "Échelle 10 (Licence / Santé)",
        9 to "Échelle 9 (DUT / BTS / Technicien)",
        8 to "Échelle 8 (Technicien 4ème)",
        6 to "Échelle 6 (Adjoint)"
    )

    val levels = listOf(
        "Tous niveaux",
        "Bac+5 / Master / Ingénieur",
        "Bac+3 / Licence",
        "Bac+2 / DUT / BTS",
        "Doctorat / Ph.D"
    )

    val regions = listOf(
        "Toutes les régions",
        "Rabat-Salé-Kénitra",
        "Casablanca-Settat",
        "Tanger-Tétouan",
        "Fès-Meknès",
        "Marrakech-Safi",
        "Souss-Massa",
        "National"
    )

    val postsRanges = listOf(
        "Tous volumes",
        "+50 postes (Grands concours)",
        "10 à 50 postes",
        "1 à 10 postes"
    )

    val sortOptions = listOf(
        "Échéance proche" to "Échéance la plus proche ⏳",
        "Nombre de postes" to "Nombre de postes (Max) 💼",
        "Pertinence profil" to "Compatibilité avec mon profil 🎯"
    )

    val activeCount = (if (currentEchelle != null) 1 else 0) +
            (if (currentLevel != "Tous niveaux") 1 else 0) +
            (if (currentRegion != "Toutes les régions") 1 else 0) +
            (if (currentPostsRange != "Tous volumes") 1 else 0) +
            (if (currentSort != "Échéance proche") 1 else 0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = autoTranslate("Filtres Avancés & Tri"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        Text(
                            text = if (activeCount > 0) "$activeCount ${autoTranslate("critères actifs")}" else autoTranslate("Affinez selon votre profil"),
                            style = MaterialTheme.typography.bodySmall.copy(color = if (activeCount > 0) Primary else OnSurfaceVariant)
                        )
                    }
                }

                TextButton(
                    onClick = {
                        currentEchelle = null
                        currentLevel = "Tous niveaux"
                        currentRegion = "Toutes les régions"
                        currentPostsRange = "Tous volumes"
                        currentSort = "Échéance proche"
                        onResetFilters()
                    }
                ) {
                    Text(
                        text = autoTranslate("Réinitialiser"),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Error
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Échelle Fonction Publique
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = autoTranslate("Échelle Fonction Publique"),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(echelles) { (ech, label) ->
                                val isSelected = currentEchelle == ech
                                FilterSelectChip(
                                    label = autoTranslate(label),
                                    isSelected = isSelected,
                                    onClick = { currentEchelle = ech }
                                )
                            }
                        }
                    }
                }

                // Section 2: Niveau d'études / Diplôme
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = autoTranslate("Niveau de Diplôme Requis"),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(levels) { lvl ->
                                val isSelected = currentLevel == lvl
                                FilterSelectChip(
                                    label = autoTranslate(lvl),
                                    isSelected = isSelected,
                                    onClick = { currentLevel = lvl }
                                )
                            }
                        }
                    }
                }

                // Section 3: Région & Ville
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = autoTranslate("Région d'Affectation"),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(regions) { reg ->
                                val isSelected = currentRegion == reg
                                FilterSelectChip(
                                    label = autoTranslate(reg),
                                    isSelected = isSelected,
                                    onClick = { currentRegion = reg }
                                )
                            }
                        }
                    }
                }

                // Section 4: Volume de Recrutement (Postes)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = autoTranslate("Nombre de Postes Ouverts"),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(postsRanges) { range ->
                                val isSelected = currentPostsRange == range
                                FilterSelectChip(
                                    label = autoTranslate(range),
                                    isSelected = isSelected,
                                    onClick = { currentPostsRange = range }
                                )
                            }
                        }
                    }
                }

                // Section 5: Ordre de Tri
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = autoTranslate("Critère de Tri"),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(sortOptions) { (key, label) ->
                                val isSelected = currentSort == key
                                FilterSelectChip(
                                    label = autoTranslate(label),
                                    isSelected = isSelected,
                                    onClick = { currentSort = key }
                                )
                            }
                        }
                    }
                }
            }

            // Apply CTA Button
            Button(
                onClick = {
                    onApplyFilters(currentEchelle, currentLevel, currentRegion, currentPostsRange, currentSort)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = autoTranslate("Appliquer les filtres"),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun FilterSelectChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary else SurfaceContainerHigh)
            .border(
                width = 1.dp,
                color = if (isSelected) Primary else SurfaceContainerHighest,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else OnSurfaceVariant
            )
        )
    }
}
