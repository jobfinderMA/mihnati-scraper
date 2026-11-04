package com.mihnati.app.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.mihnati.app.data.model.Opportunity
import com.mihnati.app.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CandidateDossierInput(
    val fullName: String,
    val cnie: String,
    val phone: String,
    val email: String,
    val city: String,
    val address: String,
    val diploma: String,
    val university: String,
    val targetMinistry: String,
    val targetPoste: String,
    val referenceNumber: String,
    val echelle: String
)

object OfficialDossierPdfGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points

    fun generateDossierPdf(
        context: Context,
        input: CandidateDossierInput
    ): File? {
        val pdfDocument = PdfDocument()

        try {
            val dossierDir = File(context.cacheDir, "dossiers")
            if (!dossierDir.exists()) dossierDir.mkdirs()

            val sanitizedRef = input.referenceNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val targetFile = File(dossierDir, "Dossier_${sanitizedRef}_${System.currentTimeMillis()}.pdf")

            // Page 1: Demande Manuscrite Officielle (Administrative Cover Letter)
            val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page1 = pdfDocument.startPage(pageInfo1)
            drawCoverLetterPage(page1.canvas, input)
            pdfDocument.finishPage(page1)

            // Page 2: CV Normalise Fonction Publique
            val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
            val page2 = pdfDocument.startPage(pageInfo2)
            drawAdministrativeCvPage(page2.canvas, input)
            pdfDocument.finishPage(page2)

            FileOutputStream(targetFile).use { out ->
                pdfDocument.writeTo(out)
            }
            return targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawCoverLetterPage(canvas: Canvas, input: CandidateDossierInput) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10.5f
            isFakeBoldText = true
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 34, 123) // Royal Navy
            textSize = 13f
            isFakeBoldText = true
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        // Draw page decorative border
        val margin = 36f
        canvas.drawRoundRect(RectF(margin, margin, PAGE_WIDTH - margin, PAGE_HEIGHT - margin), 8f, 8f, borderPaint)

        // Top Official Seal Accent
        val sealPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(margin + 10, margin + 10, PAGE_WIDTH - margin - 10, margin + 45), 6f, 6f, sealPaint)

        val sealTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 34, 123)
            textSize = 9.5f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ROYAUME DU MAROC - REPUBLIQUE & ADMINISTRATION PUBLIQUE", PAGE_WIDTH / 2f, margin + 26, sealTextPaint)
        sealTextPaint.textSize = 8.5f
        sealTextPaint.color = Color.rgb(100, 116, 139)
        sealTextPaint.isFakeBoldText = false
        canvas.drawText("DEMANDE DE PARTICIPATION AU CONCOURS DE RECRUTEMENT OFFICIEL", PAGE_WIDTH / 2f, margin + 38, sealTextPaint)

        var y = margin + 70

        // Candidate Coordinate Block (Left side)
        boldPaint.textSize = 10.5f
        canvas.drawText(input.fullName.uppercase(), margin + 20, y, boldPaint)
        y += 14
        canvas.drawText("CNIE : " + input.cnie.uppercase(), margin + 20, y, textPaint)
        y += 14
        canvas.drawText("Tel : " + input.phone, margin + 20, y, textPaint)
        y += 14
        canvas.drawText("Email : " + input.email, margin + 20, y, textPaint)
        y += 14
        canvas.drawText("Adresse : " + input.address + ", " + input.city, margin + 20, y, textPaint)

        // Date and Destination Block (Right side)
        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(Date())
        val rightX = PAGE_WIDTH - margin - 220
        var rightY = margin + 70
        canvas.drawText("Fait a " + input.city + ", le " + dateStr, rightX, rightY, textPaint)
        rightY += 24
        boldPaint.color = Color.rgb(0, 34, 123)
        canvas.drawText("A Monsieur le Ministre / Directeur de :", rightX, rightY, boldPaint)
        rightY += 15
        boldPaint.color = Color.rgb(15, 23, 42)
        canvas.drawText(input.targetMinistry.take(38), rightX, rightY, boldPaint)
        if (input.targetMinistry.length > 38) {
            rightY += 13
            canvas.drawText(input.targetMinistry.substring(38).take(38), rightX, rightY, boldPaint)
        }
        rightY += 14
        canvas.drawText("Services des Ressources Humaines & Concours", rightX, rightY, textPaint)

        y = maxOf(y, rightY) + 30

        // Subject Block Box
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(margin + 20, y, PAGE_WIDTH - margin - 20, y + 46), 6f, 6f, boxPaint)
        canvas.drawRoundRect(RectF(margin + 20, y, PAGE_WIDTH - margin - 20, y + 46), 6f, 6f, borderPaint)

        boldPaint.textSize = 10f
        boldPaint.color = Color.rgb(0, 34, 123)
        canvas.drawText("OBJET : Candidature au concours de recrutement de " + input.targetPoste.take(45), margin + 30, y + 18, boldPaint)
        textPaint.textSize = 9.5f
        canvas.drawText("REFERENCE : Avis Officiel n° " + input.referenceNumber + " (Session 2026)", margin + 30, y + 34, textPaint)

        y += 75

        // Salutations
        boldPaint.textSize = 11f
        boldPaint.color = Color.rgb(15, 23, 42)
        canvas.drawText("Monsieur le Ministre / Monsieur le Directeur,", margin + 20, y, boldPaint)
        y += 24

        // Body Paragraph 1
        textPaint.textSize = 10f
        val p1 = "J'ai l'honneur et le plaisir de solliciter de votre haute bienveillance de bien vouloir accepter ma candidature au concours de recrutement organise par vos soins, au titre de la session 2026, pour l'acces au cadre de : " + input.targetPoste + " (" + input.echelle + ")."
        y = drawWrappedText(canvas, p1, margin + 20, y, PAGE_WIDTH - (margin * 2) - 40, textPaint, 15f)
        y += 12

        // Body Paragraph 2
        val p2 = "Titulaire du diplome : " + input.diploma + " delivre par " + input.university + ", j'ai acquis tout au long de mon cursus universitaire des connaissances solides, des competences methodologiques rigoureuses et une solide deontologie du service public."
        y = drawWrappedText(canvas, p2, margin + 20, y, PAGE_WIDTH - (margin * 2) - 40, textPaint, 15f)
        y += 12

        // Body Paragraph 3
        val p3 = "Integrer votre prestigieuse administration represente pour moi un engagement d'honneur au service de l'interet general du Royaume et des citoyens marocains, conformement aux principes d'efficacite, de transparence et de modernisation de la fonction publique."
        y = drawWrappedText(canvas, p3, margin + 20, y, PAGE_WIDTH - (margin * 2) - 40, textPaint, 15f)
        y += 12

        // Body Paragraph 4 (Formal closing)
        val p4 = "Dans l'attente d'une reponse favorable de votre part, je vous prie d'agreer, Monsieur le Ministre / Directeur, l'expression de ma tres haute et fidele consideration."
        y = drawWrappedText(canvas, p4, margin + 20, y, PAGE_WIDTH - (margin * 2) - 40, textPaint, 15f)
        y += 25

        // Signature Area Box
        val sigX = PAGE_WIDTH - margin - 200
        boldPaint.textSize = 10.5f
        canvas.drawText("Signature du Candidat :", sigX, y, boldPaint)
        y += 12
        val sigBoxRect = RectF(sigX, y, PAGE_WIDTH - margin - 20, y + 60)
        canvas.drawRoundRect(sigBoxRect, 4f, 4f, borderPaint)
        textPaint.textSize = 9f
        textPaint.color = Color.rgb(148, 163, 184)
        canvas.drawText(input.fullName, sigX + 10, y + 25, textPaint)
        canvas.drawText("(Mention manuscrite : Lu et approuve)", sigX + 10, y + 45, textPaint)

        // Footer Legal Mention
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Document officiel conforme aux normes administratives marocaines (Loi n° 55-19) - Genere via Mihnati Concours", PAGE_WIDTH / 2f, PAGE_HEIGHT - margin - 12, footerPaint)
    }

    private fun drawAdministrativeCvPage(canvas: Canvas, input: CandidateDossierInput) {
        val margin = 36f
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(RectF(margin, margin, PAGE_WIDTH - margin, PAGE_HEIGHT - margin), 8f, 8f, borderPaint)

        // CV Top Header Banner (Royal Navy)
        val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 34, 123)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(margin + 10, margin + 10, PAGE_WIDTH - margin - 10, margin + 70), 6f, 6f, headerBg)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 15f
            isFakeBoldText = true
        }
        canvas.drawText(input.fullName.uppercase(), margin + 25, margin + 35, namePaint)

        val subNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            textSize = 9.5f
        }
        canvas.drawText("CURRICULUM VITAE NORMALISE - FONCTION PUBLIQUE DU MAROC", margin + 25, margin + 50, subNamePaint)
        canvas.drawText("CNIE : " + input.cnie.uppercase() + "  |  " + input.phone + "  |  " + input.email, margin + 25, margin + 63, subNamePaint)

        var y = margin + 95

        // Section Helper
        fun drawSectionHeader(title: String) {
            val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(241, 245, 249)
                style = Paint.Style.FILL
            }
            canvas.drawRect(RectF(margin + 15, y - 10, PAGE_WIDTH - margin - 15, y + 8), bg)

            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(0, 34, 123)
                textSize = 10.5f
                isFakeBoldText = true
            }
            canvas.drawText("▪  " + title.uppercase(), margin + 20, y, p)
            y += 20
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.5f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            isFakeBoldText = true
        }

        // Section 1: Etat Civil & Identification
        drawSectionHeader("1. Etat Civil & Identification Officielle")
        canvas.drawText("Nom et Prenom : " + input.fullName, margin + 25, y, boldPaint)
        canvas.drawText("Nationalite : Marocaine", PAGE_WIDTH / 2f, y, textPaint)
        y += 14
        canvas.drawText("Carte Nationale (CNIE) : " + input.cnie.uppercase(), margin + 25, y, textPaint)
        canvas.drawText("Ville de residence : " + input.city, PAGE_WIDTH / 2f, y, textPaint)
        y += 14
        canvas.drawText("Adresse : " + input.address, margin + 25, y, textPaint)
        y += 24

        // Section 2: Diplomes & Titres Universitaires
        drawSectionHeader("2. Diplomes & Titres Universitaires")
        canvas.drawText("Diplome : " + input.diploma, margin + 25, y, boldPaint)
        y += 14
        canvas.drawText("Etablissement d'obtention : " + input.university, margin + 25, y, textPaint)
        y += 14
        canvas.drawText("Cadre vise : " + input.targetPoste + " (" + input.echelle + ")", margin + 25, y, textPaint)
        y += 14
        canvas.drawText("Equivalence / Homologation : Conforme aux arretes du Ministere de l'Enseignement Superieur", margin + 25, y, textPaint)
        y += 24

        // Section 3: Competences & Connaissances Administratives
        drawSectionHeader("3. Competences Professionnelles & Administratives")
        val skills = listOf(
            "Connaissance du Droit Administratif marocain et de la Loi n° 55-19",
            "Maitrise des regles statutaires du Dahir 1-58-008 (Statut de la Fonction Publique)",
            "Gestion axee sur les resultats, budgetisation selon la LOLF 130-13",
            "Outils informatiques bureautiques et systemes d'information publics"
        )
        for (s in skills) {
            canvas.drawText("•  " + s, margin + 25, y, textPaint)
            y += 14
        }
        y += 10

        // Section 4: Langues
        drawSectionHeader("4. Langues Officielles & Pratiquees")
        canvas.drawText("•  Arabe : Langue maternelle (Excellente maitrise redactionnelle et juridique)", margin + 25, y, textPaint)
        y += 14
        canvas.drawText("•  Francais : Courant / Niveau professionnel et administratif", margin + 25, y, textPaint)
        y += 14
        canvas.drawText("•  Anglais : Pratique professionnelle", margin + 25, y, textPaint)
        y += 24

        // Section 5: Attestation sur l'Honneur
        drawSectionHeader("5. Attestation sur l'Honneur")
        val cert = "Je soussigne(e), certifie sur l'honneur l'exactitude absolue des informations mentionnees dans ce present curriculum vitae et confirme l'authenticite des pieces jointes a mon dossier de candidature."
        y = drawWrappedText(canvas, cert, margin + 25, y, PAGE_WIDTH - (margin * 2) - 50, textPaint, 14f)
        y += 20

        val sigX = PAGE_WIDTH - margin - 200
        canvas.drawText("Fait a " + input.city + ", le " + SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date()), sigX, y, textPaint)
        y += 14
        canvas.drawText("Signature certifiee :", sigX, y, boldPaint)
        y += 8
        canvas.drawRoundRect(RectF(sigX, y, PAGE_WIDTH - margin - 20, y + 45), 4f, 4f, borderPaint)

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Modele officiel standardise conforme aux concours de la fonction publique marocaine - Mihnati", PAGE_WIDTH / 2f, PAGE_HEIGHT - margin - 12, footerPaint)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineSpacing: Float
    ): Float {
        val words = text.split(" ")
        var currentY = startY
        var currentLine = StringBuilder()

        for (w in words) {
            val candidate = if (currentLine.isEmpty()) w else currentLine.toString() + " " + w
            val width = paint.measureText(candidate)
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

    fun openPdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Ouvrir le Dossier PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sharePdf(context: Context, file: File, subject: String = "Dossier de Candidature Concours 2026") {
        try {
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, "Veuillez trouver ci-joint mon dossier de candidature officiel (Demande manuscrite + CV normalise).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Partager le Dossier PDF via WhatsApp / Email").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
