package com.mihnati.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mihnati.app.pdf.OfficialPdfDocumentHelper
import com.mihnati.app.translation.autoTranslate
import com.mihnati.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun InAppPdfViewerDialog(
    pdfFile: File,
    title: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.8f, 3.5f)
        offset += offsetChange
    }

    LaunchedEffect(pdfFile) {
        withContext(Dispatchers.IO) {
            try {
                if (!pdfFile.exists() || pdfFile.length() == 0L) {
                    loadError = "Fichier PDF introuvable"
                    isLoading = false
                    return@withContext
                }

                val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val count = renderer.pageCount
                val bmpList = mutableListOf<Bitmap>()

                val maxPagesToRender = minOf(count, 8)
                for (i in 0 until maxPagesToRender) {
                    val page = renderer.openPage(i)
                    // Render at crisp 1.5x resolution for smooth performance and memory efficiency
                    val width = (page.width * 1.5f).toInt()
                    val height = (page.height * 1.5f).toInt()
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    canvas.drawColor(AndroidColor.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bmpList.add(bmp)
                }

                renderer.close()
                pfd.close()

                withContext(Dispatchers.Main) {
                    pages = bmpList
                    isLoading = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    loadError = e.message ?: "Erreur de lecture du PDF"
                    isLoading = false
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF1E293B) // Dark administrative slate viewer backdrop
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top App Bar
                Surface(
                    color = Surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
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
                                    .background(Primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = if (pages.isNotEmpty()) "${pages.size} " + autoTranslate("page(s) • Format A4 Officiel") else autoTranslate("Chargement du document..."),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Zoom Controls & Close
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    scale = (scale * 1.25f).coerceAtMost(3.5f)
                                }
                            ) {
                                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Outline)
                            }
                            IconButton(
                                onClick = {
                                    scale = (scale / 1.25f).coerceAtLeast(0.8f)
                                    if (scale <= 1f) offset = Offset.Zero
                                }
                            ) {
                                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Outline)
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = autoTranslate("Fermer"), tint = OnSurface)
                            }
                        }
                    }
                }

                // Document Viewer Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Text(
                                text = autoTranslate("Chargement du document PDF officiel..."),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else if (loadError != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 28.sp)
                            Text(
                                text = autoTranslate("Impossible d'afficher le document"),
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = {
                                    OfficialPdfDocumentHelper.openPdfInSystemViewer(context, pdfFile, title)
                                }
                            ) {
                                Text(autoTranslate("Ouvrir dans le lecteur PDF"))
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .transformable(state = transformState)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            itemsIndexed(pages) { index, bmp ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${autoTranslate("Page")} ${index + 1} / ${pages.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .shadow(8.dp, RoundedCornerShape(4.dp))
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                                    ) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Page ${index + 1}",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Bar: Open in System PDF Reader or Share
                Surface(
                    color = Surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                OfficialPdfDocumentHelper.openPdfInSystemViewer(context, pdfFile, title)
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = autoTranslate("Lecteur PDF Système"),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                OfficialPdfDocumentHelper.sharePdfFile(context, pdfFile, title)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = autoTranslate("Partager"),
                                style = MaterialTheme.typography.labelMedium.copy(color = Primary)
                            )
                        }
                    }
                }
            }
        }
    }
}
