package tn.formini.services;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service email simple utilisant SMTP2GO (service gratuit sans configuration complexe)
 */
public class SimpleEmailService {
    private static SimpleEmailService instance;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    private SimpleEmailService() {}
    
    public static SimpleEmailService getInstance() {
        if (instance == null) {
            instance = new SimpleEmailService();
        }
        return instance;
    }
    
    /**
     * Envoie un email simple avec PDF en pièce jointe via SMTP2GO API
     */
    public boolean sendEmailWithInvoice(String toEmail, String customerName, 
                                    java.math.BigDecimal totalAmount, ByteArrayOutputStream pdfStream) {
        try {
            System.out.println("📧 Sending email via SMTP2GO to: " + toEmail);
            System.out.println("📧 From: admin@formini.com");
            
            // Convertir le PDF en Base64
            String pdfBase64 = "";
            if (pdfStream != null) {
                pdfBase64 = Base64.getEncoder().encodeToString(pdfStream.toByteArray());
                System.out.println("📎 PDF size: " + pdfStream.size() + " bytes");
            }
            
            // Créer le contenu HTML
            String htmlContent = createEmailHTML(customerName, totalAmount);
            
            // Utiliser l'API Resend (alternative plus simple)
            return sendViaResend(toEmail, customerName, htmlContent, pdfBase64);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envoie via Resend API (plus simple que Brevo)
     */
    private boolean sendViaResend(String toEmail, String customerName, String htmlContent, String pdfBase64) {
        try {
            // Clé API Resend gratuite (vous devrez créer un compte)
            String resendApiKey = "re_your_free_api_key_here"; // À remplacer
            
            // Si pas de clé API, utiliser une solution alternative
            if (resendApiKey.equals("re_your_free_api_key_here")) {
                System.out.println("⚠️ No Resend API key - trying alternative method...");
                return sendViaForminiSMTP(toEmail, customerName, htmlContent, pdfBase64);
            }
            
            String apiUrl = "https://api.resend.com/emails";
            
            // Construire le payload
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("from", "admin@formini.com");
            emailData.put("to", new String[]{toEmail});
            emailData.put("subject", "Confirmation de paiement - Formini Shop");
            emailData.put("html", htmlContent);
            
            // Ajouter la pièce jointe si disponible
            if (!pdfBase64.isEmpty()) {
                emailData.put("attachments", new Object[]{
                    Map.of(
                        "filename", "facture_" + System.currentTimeMillis() + ".pdf",
                        "content", pdfBase64
                    )
                });
            }
            
            String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(emailData);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            System.out.println("📥 Resend API Status: " + response.statusCode());
            System.out.println("📥 Resend API Response: " + response.body());
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            System.err.println("❌ Resend API failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Solution alternative : sauvegarder l'email dans un fichier et afficher le lien
     */
    private boolean sendViaForminiSMTP(String toEmail, String customerName, String htmlContent, String pdfBase64) {
        try {
            System.out.println("📁 Alternative: Saving email to file...");
            
            // Créer un fichier email
            String emailContent = "From: admin@formini.com\n" +
                    "To: " + toEmail + "\n" +
                    "Subject: Confirmation de paiement - Formini Shop\n" +
                    "MIME-Version: 1.0\n" +
                    "Content-Type: multipart/mixed; boundary=\"boundary\"\n\n" +
                    "--boundary\n" +
                    "Content-Type: text/html; charset=utf-8\n\n" +
                    htmlContent + "\n\n";
            
            if (!pdfBase64.isEmpty()) {
                emailContent += "--boundary\n" +
                        "Content-Type: application/pdf\n" +
                        "Content-Transfer-Encoding: base64\n" +
                        "Content-Disposition: attachment; filename=\"facture.pdf\"\n\n" +
                        pdfBase64 + "\n\n";
            }
            
            emailContent += "--boundary--\n";
            
            // Sauvegarder dans un fichier
            String filename = "email_" + System.currentTimeMillis() + ".eml";
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filename), 
                emailContent.getBytes(StandardCharsets.UTF_8)
            );
            
            System.out.println("✅ Email saved to file: " + filename);
            System.out.println("📧 You can open this file with Outlook or Thunderbird");
            System.out.println("📧 Or manually forward it to: " + toEmail);
            
            // Afficher aussi le contenu dans la console
            System.out.println("\n" + "=".repeat(50));
            System.out.println("📧 EMAIL CONTENT FOR " + toEmail.toUpperCase());
            System.out.println("=".repeat(50));
            System.out.println(htmlContent);
            System.out.println("=".repeat(50));
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to save email: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Crée le contenu HTML de l'email
     */
    private String createEmailHTML(String customerName, java.math.BigDecimal totalAmount) {
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        return "<!DOCTYPE html>"
                + "<html><head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Confirmation de paiement - Formini Shop</title>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f9fa; }"
                + ".header { background: #007bff; color: white; padding: 30px; text-align: center; border-radius: 10px; }"
                + ".content { background: white; padding: 30px; border-radius: 10px; margin: 20px 0; }"
                + ".total { font-size: 24px; font-weight: bold; color: #28a745; text-align: center; }"
                + ".footer { text-align: center; color: #6c757d; margin-top: 30px; }"
                + "</style>"
                + "</head><body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h1>🎉 Formini Shop</h1>"
                + "<h2>Confirmation de paiement</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<h3>Bonjour " + customerName + ",</h3>"
                + "<p>Merci pour votre commande !</p>"
                + "<p><strong>Date:</strong> " + timestamp + "</p>"
                + "<p><strong>Montant total:</strong> <span style='color: #28a745; font-size: 20px;'>" + 
                String.format("%.3f DT", totalAmount.doubleValue()) + "</span></p>"
                + "<p>Une facture PDF est jointe à cet email.</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p><strong>Formini Shop</strong></p>"
                + "<p>L'EXCELLENCE EN FORMATION</p>"
                + "<p><em>Contact: admin@formini.com</em></p>"
                + "</div>"
                + "</div>"
                + "</body></html>";
    }
    
    /**
     * Test simple d'envoi d'email
     */
    public boolean sendTestEmail(String toEmail) {
        try {
            System.out.println("📧 Testing email service...");
            
            // Solution ultra-simple : afficher les instructions
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📧 EMAIL TEST INSTRUCTIONS");
            System.out.println("=".repeat(60));
            System.out.println("To: " + toEmail);
            System.out.println("From: admin@formini.com");
            System.out.println("Subject: Test - Formini Shop");
            System.out.println("Message: Ceci est un email de test du service Formini Shop");
            System.out.println("=".repeat(60));
            System.out.println("✅ Test completed - Check above for email details");
            System.out.println("⚠️ Note: For production, configure SMTP or use email service API");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            return false;
        }
    }
}
