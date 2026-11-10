package com.mihnati.app.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.mihnati.app.data.model.CandidateConvocationRecord
import com.mihnati.app.data.model.ConcoursExamList
import com.mihnati.app.data.model.Opportunity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OfficialPdfDocumentHelper {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points

    private fun getPdfDirectory(context: Context): File {
        val dir = File(context.cacheDir, "official_pdfs")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Generates or retrieves the official ministerial decree recruitment notice (Avis de Concours A4).
     */
    fun getOrCreateAvisPdf(context: Context, opportunity: Opportunity): File {
        val dir = getPdfDirectory(context)
        val file = File(dir, "Avis_Officiel_${opportunity.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.pdf")
        if (file.exists() && file.length() > 0) return file

        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            drawAvisDecreePage(page.canvas, opportunity)
            pdfDocument.finishPage(page)

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
        return file
    }

    /**
     * Generates or retrieves the official Candidate Convocation Slip (Fiche de Convocation A4).
     */
    fun getOrCreateConvocationSlipPdf(context: Context, record: CandidateConvocationRecord): File {
        val dir = getPdfDirectory(context)
        val file = File(dir, "Convocation_${record.cnie}_${record.id}.pdf")
        if (file.exists() && file.length() > 0) return file

        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            drawConvocationSlipPage(page.canvas, record)
            pdfDocument.finishPage(page)

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
        return file
    }

    /**
     * Generates or retrieves the official Candidate List (Liste Officielle des Candidats A4).
     */
    fun getOrCreateCandidateListPdf(context: Context, examList: ConcoursExamList): File {
        val dir = getPdfDirectory(context)
        val file = File(dir, "Liste_Officielle_${examList.id}.pdf")
        if (file.exists() && file.length() > 0) return file

        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            drawCandidateListPage(page.canvas, examList)
            pdfDocument.finishPage(page)

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
        return file
    }

    /**
     * Opens a local PDF file in the user's built-in Android PDF Viewer app (e.g. Google Drive PDF Viewer).
     * NEVER redirects to a web browser or external website.
     */
    fun openPdfInSystemViewer(context: Context, file: File, chooserTitle: String = "Ouvrir avec le lecteur PDF") {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Aucun lecteur PDF trouve sur l'appareil", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares the official PDF file via standard Android share sheet (WhatsApp, Email, etc.).
     */
    fun sharePdfFile(context: Context, file: File, subject: String = "Document Officiel Concours 2026") {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, "$subject\nExtrait officiel portail emploi-public.ma")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Partager le PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur lors du partage du document", Toast.LENGTH_SHORT).show()
        }
    }

    // -------------------------------------------------------------
    // DRAWING: Avis de Concours Page (Format A4)
    // -------------------------------------------------------------
    private fun drawAvisDecreePage(canvas: Canvas, opp: Opportunity) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = 45f

        // Top Frame & Border
        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(30f, 30f, PAGE_WIDTH - 30f, PAGE_HEIGHT - 30f, paint)
        paint.style = Paint.Style.FILL

        // Header: Kingdom of Morocco
        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("ROYAUME DU MAROC", 45f, y, paint)
        y += 16f

        paint.textSize = 10f
        paint.color = Color.rgb(30, 64, 175)
        canvas.drawText(opp.organization.uppercase(Locale.ROOT), 45f, y, paint)
        y += 14f

        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText("Direction des Ressources Humaines - Division du Recrutement", 45f, y, paint)
        canvas.drawText("Portail Officiel : emploi-public.ma", PAGE_WIDTH - 210f, y - 14f, paint)
        y += 24f

        // Divider
        paint.color = Color.rgb(30, 64, 175)
        paint.strokeWidth = 2f
        canvas.drawLine(45f, y, PAGE_WIDTH - 45f, y, paint)
        y += 22f

        // Decree Title Banner
        val bannerRect = RectF(45f, y, PAGE_WIDTH - 45f, y + 42f)
        paint.color = Color.rgb(241, 245, 249)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(bannerRect, 6f, 6f, paint)

        paint.color = Color.rgb(30, 64, 175)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(bannerRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11.5f
        val decrTitle = "DECISION MINISTERIELLE D'OUVERTURE DE CONCOURS DE RECRUTEMENT"
        val decrWidth = paint.measureText(decrTitle)
        canvas.drawText(decrTitle, (PAGE_WIDTH - decrWidth) / 2f, y + 18f, paint)

        paint.textSize = 9f
        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val refStr = "Reference Officielle : ${opp.reference}  |  Exercice Budgetaire 2026"
        val refWidth = paint.measureText(refStr)
        canvas.drawText(refStr, (PAGE_WIDTH - refWidth) / 2f, y + 33f, paint)
        y += 56f

        // Section: Objet du Concours
        paint.color = Color.rgb(30, 64, 175)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("ARTICLE 1 : DISPOSITIONS GENERALES ET POSTES OUVERTS", 45f, y, paint)
        y += 14f

        paint.color = Color.rgb(51, 65, 85)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        val introText = "Il est porte a la connaissance des citoyens marocains qu'un concours de recrutement sur epreuves est ouvert aupres de : ${opp.organization} au titre de l'annee 2026."
        y = drawWrappedText(canvas, introText, 45f, y, PAGE_WIDTH - 90f, 12f, paint)
        y += 10f

        // Key terms table
        val tableTop = y
        val rowH = 18f
        val terms = listOf(
            "Intitule du poste / Cadre" to opp.title,
            "Nombre de postes budgetaires" to "${opp.postsCount} poste(s) ouverts",
            "Echelle de remuneration" to "Echelle 11 (Grille de la Fonction Publique)",
            "Diplome et profil requis" to opp.degreeRequired,
            "Lieu d'affectation" to opp.location,
            "Date limite de candidature" to opp.deadline,
            "Date prevue de l'epreuve ecrite" to (if (opp.examDate.isNotBlank()) opp.examDate else "A preciser apres publication des listes"),
            "Mode de depot officiel" to "Plateforme officielle emploi-public.ma"
        )

        terms.forEachIndexed { idx, pair ->
            val rowY = tableTop + idx * rowH
            val bg = if (idx % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
            paint.color = bg
            canvas.drawRect(45f, rowY, PAGE_WIDTH - 45f, rowY + rowH, paint)

            paint.color = Color.rgb(226, 232, 240)
            paint.strokeWidth = 0.5f
            canvas.drawLine(45f, rowY + rowH, PAGE_WIDTH - 45f, rowY + rowH, paint)

            paint.color = Color.rgb(30, 41, 59)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            canvas.drawText(pair.first, 52f, rowY + 12f, paint)

            paint.color = Color.rgb(71, 85, 105)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(pair.second, 220f, rowY + 12f, paint)
        }
        y = tableTop + terms.size * rowH + 20f

        // Section: Epreuves
        paint.color = Color.rgb(30, 64, 175)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("ARTICLE 2 : PROGRAMME ET NATURE DES EPREUVES", 45f, y, paint)
        y += 14f

        val examHeaders = listOf("Epreuve", "Duree", "Coeff.", "Note Eliminatoire")
        val examRows = listOf(
            listOf("1. Epreuve Ecrite : Sujet d'ordre general ou thematique lie aux attributions du ministere", "3 Heures", "3", "< 08 / 20"),
            listOf("2. Epreuve Ecrite : Epreuve de specialite technique / administrative", "3 Heures", "4", "< 10 / 20"),
            listOf("3. Epreuve Orale : Entretien individuel devant le jury de recrutement", "30 Min", "4", "< 10 / 20")
        )

        // Draw exam table
        paint.color = Color.rgb(30, 64, 175)
        canvas.drawRect(45f, y, PAGE_WIDTH - 45f, y + 16f, paint)
        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(examHeaders[0], 52f, y + 11f, paint)
        canvas.drawText(examHeaders[1], 370f, y + 11f, paint)
        canvas.drawText(examHeaders[2], 430f, y + 11f, paint)
        canvas.drawText(examHeaders[3], 480f, y + 11f, paint)
        y += 16f

        examRows.forEach { r ->
            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRect(45f, y, PAGE_WIDTH - 45f, y + 20f, paint)
            paint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(45f, y + 20f, PAGE_WIDTH - 45f, y + 20f, paint)

            paint.color = Color.rgb(30, 41, 59)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.5f
            canvas.drawText(r[0], 52f, y + 13f, paint)
            canvas.drawText(r[1], 370f, y + 13f, paint)
            canvas.drawText(r[2], 435f, y + 13f, paint)
            canvas.drawText(r[3], 485f, y + 13f, paint)
            y += 20f
        }
        y += 18f

        // Section: Pieces a fournir
        paint.color = Color.rgb(30, 64, 175)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("ARTICLE 3 : PIECES OBLIGATOIRES DU DOSSIER", 45f, y, paint)
        y += 14f

        val docs = listOf(
            "• Demande manuscrite adressee a Monsieur le Ministre avec mention du grade et de la reference.",
            "• Curriculum Vitae (CV) detaille avec photo d'identite recente et coordonnees exactes.",
            "• Copie certifiee conforme a l'originale de la Carte Nationale d'Identite Electronique (CNIE).",
            "• Copie certifiee conforme du diplome requis dans la specialite indiquee.",
            "• Copie de l'arrete d'equivalence scientifique pour les diplomes etrangers ou prives reconnus."
        )
        paint.color = Color.rgb(51, 65, 85)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        docs.forEach { doc ->
            canvas.drawText(doc, 50f, y, paint)
            y += 12f
        }
        y += 20f

        // Official Stamp & Visa Block
        val stampRect = RectF(PAGE_WIDTH - 220f, y, PAGE_WIDTH - 45f, y + 55f)
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(stampRect, 6f, 6f, paint)
        paint.color = Color.rgb(30, 64, 175)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(stampRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("POUR LE MINISTRE ET PAR DELEGATION", PAGE_WIDTH - 212f, y + 16f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        canvas.drawText("Le Directeur des Ressources Humaines", PAGE_WIDTH - 212f, y + 28f, paint)
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("[Cachet et Signature Officielle]", PAGE_WIDTH - 212f, y + 44f, paint)

        // Bottom Security Footer
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 7.5f
        val footerStr = "Document officiel certifie conforme - Portail de l'Emploi Public emploi-public.ma - Reference : ${opp.reference}"
        canvas.drawText(footerStr, 45f, PAGE_HEIGHT - 38f, paint)
    }

    // -------------------------------------------------------------
    // DRAWING: Fiche de Convocation Slip (Format A4)
    // -------------------------------------------------------------
    private fun drawConvocationSlipPage(canvas: Canvas, record: CandidateConvocationRecord) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = 45f

        // Border
        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(30f, 30f, PAGE_WIDTH - 30f, PAGE_HEIGHT - 30f, paint)
        paint.style = Paint.Style.FILL

        // Header
        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("ROYAUME DU MAROC", 45f, y, paint)
        y += 16f

        paint.textSize = 10f
        paint.color = Color.rgb(30, 64, 175)
        canvas.drawText(record.administration.uppercase(Locale.ROOT), 45f, y, paint)
        y += 14f

        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText("Commission Nationale des Concours de Recrutement", 45f, y, paint)
        canvas.drawText("Portail : emploi-public.ma", PAGE_WIDTH - 180f, y - 14f, paint)
        y += 24f

        // Title Box
        val titleRect = RectF(45f, y, PAGE_WIDTH - 45f, y + 44f)
        paint.color = Color.rgb(240, 253, 244)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(titleRect, 8f, 8f, paint)
        paint.color = Color.rgb(22, 101, 52)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(titleRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(20, 83, 45)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        val slipTitle = "CONVOCATION OFFICIELLE INDIVIDUELLE"
        val tw = paint.measureText(slipTitle)
        canvas.drawText(slipTitle, (PAGE_WIDTH - tw) / 2f, y + 20f, paint)

        paint.textSize = 9.5f
        paint.color = Color.rgb(22, 101, 52)
        val stageStr = "EPREUVE : ${record.listType.labelFr.uppercase(Locale.ROOT)}"
        val stw = paint.measureText(stageStr)
        canvas.drawText(stageStr, (PAGE_WIDTH - stw) / 2f, y + 36f, paint)
        y += 62f

        // Candidate Identity Card Box
        paint.color = Color.rgb(30, 64, 175)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("IDENTITE DU CANDIDAT CONVOQUE", 45f, y, paint)
        y += 12f

        val idBox = RectF(45f, y, PAGE_WIDTH - 45f, y + 80f)
        paint.color = Color.rgb(248, 250, 252)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(idBox, 6f, 6f, paint)
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(idBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        val fields1 = listOf(
            "Nom et Prenom :" to record.fullName,
            "N° Carte Nationale (CNIE) :" to record.cnie,
            "Grade / Cadre postule :" to record.grade,
            "Numero de Convocation :" to record.convocationNumber
        )

        var fy = y + 18f
        fields1.forEach { (lbl, valStr) ->
            paint.color = Color.rgb(100, 116, 139)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            canvas.drawText(lbl, 56f, fy, paint)

            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(valStr, 220f, fy, paint)
            fy += 16f
        }
        y += 98f

        // Convocation Venue & Schedule Box
        paint.color = Color.rgb(30, 64, 175)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("LIEU, DATE ET AFFECTATION DE LA SALLE", 45f, y, paint)
        y += 12f

        val venueBox = RectF(45f, y, PAGE_WIDTH - 45f, y + 90f)
        paint.color = Color.rgb(254, 252, 232)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(venueBox, 6f, 6f, paint)
        paint.color = Color.rgb(234, 179, 8)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(venueBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        val fields2 = listOf(
            "Date de l'epreuve :" to record.examDate,
            "Heure d'emargement :" to "${record.examTime} (Fermeture des portes 15 min avant)",
            "Centre d'examen :" to record.examCenter,
            "Salle / Amphitheatre :" to record.roomOrTable
        )

        var vy = y + 18f
        fields2.forEach { (lbl, valStr) ->
            paint.color = Color.rgb(113, 63, 18)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            canvas.drawText(lbl, 56f, vy, paint)

            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(valStr, 200f, vy, paint)
            vy += 18f
        }
        y += 108f

        // Mandatory Instructions Box
        paint.color = Color.rgb(185, 28, 28)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("CONSIGNES STRICTES ET SECURITE DE L'EPREUVE", 45f, y, paint)
        y += 12f

        val instructions = listOf(
            "1. La presentation de la Carte Nationale d'Identite Electronique (CNIE) originale et de la presente convocation est rigoureusement obligatoire pour acceder au centre d'examen.",
            "2. Tout retard au-dela de l'heure de fermeture des portes entrainera l'interdiction categorique de participer aux epreuves.",
            "3. L'utilisation des telephones portables, montres connectees et de tout support electronique est formellement interdite sous peine d'exclusion immediate et poursuites.",
            "4. Seuls les stylos a bille bleu ou noir sont autorises sur les feuilles d'examen normalisees.",
            "5. Consigne specifique : " + record.specialInstructions
        )

        paint.color = Color.rgb(51, 65, 85)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        instructions.forEach { instr ->
            y = drawWrappedText(canvas, instr, 45f, y, PAGE_WIDTH - 90f, 11f, paint)
            y += 4f
        }
        y += 24f

        // Simulated Barcode / QR pattern & Official Stamp
        paint.color = Color.rgb(15, 23, 42)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        var barX = 45f
        for (i in 0..40) {
            val sw = if (i % 3 == 0) 3f else 1.5f
            paint.strokeWidth = sw
            canvas.drawLine(barX, y, barX, y + 25f, paint)
            barX += sw + 3f
        }
        paint.style = Paint.Style.FILL

        paint.textSize = 7.5f
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("*${record.convocationNumber}*", 45f, y + 36f, paint)

        // Stamp on the right
        val stampBox = RectF(PAGE_WIDTH - 210f, y - 6f, PAGE_WIDTH - 45f, y + 45f)
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(stampBox, 6f, 6f, paint)
        paint.color = Color.rgb(30, 64, 175)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(stampBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8f
        canvas.drawText("VISA DE LA COMMISSION", PAGE_WIDTH - 200f, y + 12f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Le President du Jury", PAGE_WIDTH - 200f, y + 24f, paint)
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("[Emargement Officiel]", PAGE_WIDTH - 200f, y + 38f, paint)

        // Footer
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 7.5f
        canvas.drawText("Fiche de convocation officielle generee pour le candidat - Portail emploi-public.ma", 45f, PAGE_HEIGHT - 38f, paint)
    }

    // -------------------------------------------------------------
    // DRAWING: Candidate List Page (Format A4)
    // -------------------------------------------------------------
    private fun drawCandidateListPage(canvas: Canvas, exam: ConcoursExamList) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = 45f

        // Border
        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(30f, 30f, PAGE_WIDTH - 30f, PAGE_HEIGHT - 30f, paint)
        paint.style = Paint.Style.FILL

        // Header
        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("ROYAUME DU MAROC", 45f, y, paint)
        y += 16f

        paint.textSize = 10f
        paint.color = Color.rgb(30, 64, 175)
        canvas.drawText(exam.administration.uppercase(Locale.ROOT), 45f, y, paint)
        y += 14f

        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText("Commission du Recrutement - Portail : emploi-public.ma", 45f, y, paint)
        y += 24f

        // Banner
        val banner = RectF(45f, y, PAGE_WIDTH - 45f, y + 42f)
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(banner, 6f, 6f, paint)
        paint.color = Color.rgb(30, 64, 175)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(banner, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        val listTitle = "LISTE OFFICIELLE DES CANDIDATS CONVOQUES"
        val tw = paint.measureText(listTitle)
        canvas.drawText(listTitle, (PAGE_WIDTH - tw) / 2f, y + 17f, paint)

        paint.textSize = 8.5f
        paint.color = Color.rgb(30, 64, 175)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val subT = "${exam.titleFr} (${exam.candidatesCount} candidats)"
        val stw = paint.measureText(subT.take(65))
        canvas.drawText(subT.take(65), (PAGE_WIDTH - stw) / 2f, y + 33f, paint)
        y += 56f

        // Table Header
        paint.color = Color.rgb(30, 64, 175)
        canvas.drawRect(45f, y, PAGE_WIDTH - 45f, y + 18f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8f
        canvas.drawText("N°", 50f, y + 12f, paint)
        canvas.drawText("N° CNIE", 80f, y + 12f, paint)
        canvas.drawText("NOM ET PRENOM", 150f, y + 12f, paint)
        canvas.drawText("CENTRE D'EXAMEN", 310f, y + 12f, paint)
        canvas.drawText("SALLE / TABLE", 440f, y + 12f, paint)
        y += 18f

        // Sample Candidates Table Rows
        val mockCandidates = listOf(
            Triple("BE892415", "Bilal Test", "Amphi 3 • Table 142"),
            Triple("CD345678", "Mansouri Yassine", "Amphi 3 • Table 143"),
            Triple("BK789012", "Alami Mohamed", "Amphi 3 • Table 144"),
            Triple("AA123456", "Bennani Fatima", "Amphi 3 • Table 145"),
            Triple("EE654321", "Idrissi Sara", "Amphi 2 • Table 101"),
            Triple("WA987654", "Chraibi Omar", "Amphi 2 • Table 102"),
            Triple("KB112233", "Tazi Mehdi", "Amphi 2 • Table 103"),
            Triple("SH445566", "Berrada Salma", "Amphi 1 • Table 012"),
            Triple("ZT778899", "El Fassi Karim", "Amphi 1 • Table 013"),
            Triple("MC332211", "Amrani Houda", "Amphi 1 • Table 014"),
            Triple("PB554433", "Hassani Youssef", "Amphi 1 • Table 015"),
            Triple("LF889900", "Alaoui Nadia", "Amphi 1 • Table 016")
        )

        mockCandidates.forEachIndexed { i, cand ->
            val bg = if (i % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
            paint.color = bg
            canvas.drawRect(45f, y, PAGE_WIDTH - 45f, y + 18f, paint)

            paint.color = Color.rgb(226, 232, 240)
            paint.strokeWidth = 0.5f
            canvas.drawLine(45f, y + 18f, PAGE_WIDTH - 45f, y + 18f, paint)

            paint.color = Color.rgb(30, 41, 59)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            canvas.drawText(String.format("%03d", i + 1), 50f, y + 12f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.rgb(30, 64, 175)
            canvas.drawText(cand.first, 80f, y + 12f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(cand.second, 150f, y + 12f, paint)

            paint.color = Color.rgb(71, 85, 105)
            val centerName = exam.examCenters.firstOrNull() ?: "Rabat, Casablanca"
            canvas.drawText(centerName.take(22), 310f, y + 12f, paint)

            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(cand.third, 440f, y + 12f, paint)

            y += 18f
        }
        y += 24f

        // Footer stamp
        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        canvas.drawText("Date et heure de l'epreuve : ${exam.examDate}", 45f, y, paint)
        y += 12f
        val officialCenter = exam.examCenters.firstOrNull() ?: "Centres Nationaux"
        canvas.drawText("Lieu officiel : $officialCenter", 45f, y, paint)
        y += 20f

        val stampBox = RectF(PAGE_WIDTH - 220f, y, PAGE_WIDTH - 45f, y + 45f)
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(stampBox, 6f, 6f, paint)
        paint.color = Color.rgb(30, 64, 175)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(stampBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8f
        canvas.drawText("LE PRESIDENT DE LA COMMISSION", PAGE_WIDTH - 212f, y + 15f, paint)
        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("[Visa et Sceau Officiel]", PAGE_WIDTH - 212f, y + 32f, paint)

        // Footer
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 7.5f
        canvas.drawText("Liste certifiee conforme publiee par le ministere aupres de emploi-public.ma", 45f, PAGE_HEIGHT - 38f, paint)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        lineSpacing: Float,
        paint: Paint
    ): Float {
        var currentY = startY
        val words = text.split(" ")
        var currentLine = StringBuilder()

        for (w in words) {
            val testLine = if (currentLine.isEmpty()) w else "$currentLine $w"
            val width = paint.measureText(testLine)
            if (width > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine.toString(), x, currentY, paint)
                currentY += lineSpacing
                currentLine = StringBuilder(w)
            } else {
                currentLine.append(if (currentLine.isEmpty()) "" else " ").append(w)
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), x, currentY, paint)
            currentY += lineSpacing
        }
        return currentY
    }
}
