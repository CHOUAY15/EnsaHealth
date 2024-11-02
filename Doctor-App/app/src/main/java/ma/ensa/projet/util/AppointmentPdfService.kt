package ma.ensa.projet.util

import android.content.Context
import android.os.Environment
import android.print.PrintAttributes
import android.webkit.WebView
import android.webkit.WebViewClient
import ma.ensa.projet.beans.Appointment
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AppointmentPdfService(private val context: Context) {

    suspend fun generatePdfFromHtml(appointment: Appointment): Result<File> =
        suspendCoroutine { continuation ->
            try {
                // Create HTML content
                val htmlContent = createHtmlContent(appointment)

                // Create directory in Downloads folder
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs()
                }

                // Create a unique filename using timestamp and appointment details
                val timestamp = System.currentTimeMillis()
                val sanitizedDoctorName = appointment.doctorName.replace("\\W+".toRegex(), "_")
                val fileName = "RDV_${sanitizedDoctorName}_$timestamp.pdf"
                val file = File(downloadDir, fileName)

                // Create WebView and load content
                val webView = WebView(context)
                webView.settings.javaScriptEnabled = true

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        try {
                            // Create print adapter
                            val printAdapter = webView.createPrintDocumentAdapter(fileName)

                            // Set print attributes
                            val printAttributes = PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                .build()

                            // Start print job
                            val printJob = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
                            printJob.print(
                                fileName,
                                printAdapter,
                                printAttributes
                            )

                            continuation.resume(Result.success(file))
                        } catch (e: Exception) {
                            continuation.resume(Result.failure(e))
                        }
                    }
                }

                // Load HTML content
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)

            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }

    private fun createHtmlContent(appointment: Appointment): String {
        val currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        val appointmentDateTime = LocalDateTime.parse(
            appointment.date,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        )
        val formattedDate = appointmentDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        padding: 40px; 
                        line-height: 1.6;
                    }
                    .header { 
                        font-size: 24px; 
                        font-weight: bold; 
                        text-align: center;
                        margin-bottom: 30px; 
                        color: #2c3e50;
                    }
                    .date { 
                        color: #666; 
                        margin-bottom: 40px;
                        text-align: right;
                    }
                    .details { 
                        margin: 30px 0;
                        padding: 20px;
                        border: 1px solid #eee;
                        border-radius: 5px;
                    }
                    .details h3 {
                        color: #2c3e50;
                        margin-bottom: 20px;
                    }
                    .footer { 
                        margin-top: 50px; 
                        font-style: italic;
                        text-align: center;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <div class="header">CONFIRMATION DE RENDEZ-VOUS</div>
                <div class="date">Date d'émission: $currentDate</div>
                
                <div class="details">
                    <h3>DÉTAILS DU RENDEZ-VOUS</h3>
                    <p><strong>Médecin:</strong> Dr. ${appointment.doctorName}</p>
                    <p><strong>Date du rendez-vous:</strong> $formattedDate</p>
                    <p><strong>Statut:</strong> ${if (appointment.statut) "Confirmé" else "En attente"}</p>
                </div>
                
                <div class="footer">
                    Ce document sert de justificatif de rendez-vous.
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}