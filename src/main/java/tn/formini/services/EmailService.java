package tn.formini.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour envoyer des emails via l'API Brevo
 */
public class EmailService {
    private static EmailService instance;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    private EmailService() {}
    
    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }
    
    /**
     * Envoie un email de test simple sans pièce jointe
     */
    public boolean sendTestEmail(String toEmail, String customerName) {
        try {
            String brevoApiKey = tn.formini.utils.ConfigLoader.getBrevoKey();
            if (brevoApiKey == null || brevoApiKey.trim().isEmpty()) {
                System.err.println("❌ NO BREVO API KEY FOUND IN CONFIGURATION");
                return false;
            }
            
            System.out.println("📧 Sending TEST email to: " + toEmail);
            
            String apiUrl = "https://api.brevo.com/v3/smtp/email";
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("sender", Map.of(
                "name", "Formini Shop Test",
                "email", "mail@brevo.com"
            ));
            emailData.put("to", new Object[]{
                Map.of(
                    "email", toEmail,
                    "name", customerName
                )
            });
            emailData.put("subject", "🧪 TEST - Formini Shop Email");
            emailData.put("htmlContent", "<h2>Test Email</h2><p>Ceci est un email de test pour vérifier la livraison.</p><p>Envoyé à: " + toEmail + "</p><p>Heure: " + java.time.LocalDateTime.now() + "</p>");
            
            String jsonPayload = objectMapper.writeValueAsString(emailData);
            System.out.println("📤 TEST JSON Payload: " + jsonPayload);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("api-key", brevoApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            int statusCode = response.statusCode();
            String responseBody = response.body();
            
            System.out.println("📥 TEST Email Response Status: " + statusCode);
            System.out.println("📥 TEST Email Response Body: " + responseBody);
            
            if (statusCode == 201) {
                System.out.println("✅ TEST email sent successfully to: " + toEmail);
                return true;
            } else {
                System.err.println("❌ TEST email failed. Status: " + statusCode);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error sending TEST email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envoie un email de confirmation de paiement avec facture PDF jointe
     */
    public boolean sendPaymentConfirmationEmail(String toEmail, String customerName, 
                                         java.math.BigDecimal totalAmount, String pdfBase64) {
        try {
            String brevoApiKey = tn.formini.utils.ConfigLoader.getBrevoKey();
            if (brevoApiKey == null || brevoApiKey.trim().isEmpty()) {
                System.err.println("❌ NO BREVO API KEY FOUND IN CONFIGURATION");
                return false;
            }
            
            System.out.println("📧 Sending payment confirmation email to: " + toEmail);
            System.out.println("📧 Customer: " + customerName);
            System.out.println("💰 Total: " + totalAmount);
            System.out.println("🔑 Brevo API Key: " + maskKey(brevoApiKey));
            
            // URL de l'API Brevo
            String apiUrl = "https://api.brevo.com/v3/smtp/email";
            
            // Construire le corps de l'email
            String htmlContent = createPaymentConfirmationHTML(customerName, totalAmount);
            
            // Créer le payload JSON
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("sender", Map.of(
                "name", "Formini Shop",
                "email", "mail@brevo.com"
            ));
            emailData.put("to", new Object[]{
                Map.of(
                    "email", toEmail,
                    "name", customerName
                )
            });
            emailData.put("subject", "Confirmation de paiement - Formini Shop");
            emailData.put("htmlContent", htmlContent);
            
            // Ajouter la pièce jointe (PDF) - Brevo attend un tableau d'attachments
            if (pdfBase64 != null && !pdfBase64.trim().isEmpty()) {
                System.out.println("📎 PDF attachment size: " + pdfBase64.length() + " characters");
                System.out.println("📎 PDF base64 preview: " + pdfBase64.substring(0, Math.min(50, pdfBase64.length())) + "...");
                
                emailData.put("attachment", new Object[]{
                    Map.of(
                        "name", "facture_" + System.currentTimeMillis() + ".pdf",
                        "content", pdfBase64
                    )
                });
            } else {
                System.out.println("⚠️ No PDF attachment provided");
            }
            
            // Convertir en JSON
            String jsonPayload = objectMapper.writeValueAsString(emailData);
            System.out.println("📤 JSON Payload (first 500 chars): " + jsonPayload.substring(0, Math.min(500, jsonPayload.length())) + "...");
            
            // Créer la requête HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("api-key", brevoApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            
            // Envoyer la requête
            System.out.println("📤 Sending email request to Brevo API...");
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            // Traiter la réponse
            int statusCode = response.statusCode();
            String responseBody = response.body();
            
            System.out.println("📥 Brevo API Response Status: " + statusCode);
            System.out.println("📥 Brevo API Response Body: " + responseBody);
            
            if (statusCode == 201) {
                System.out.println("✅ Email sent successfully to: " + toEmail);
                System.out.println("📧 Check your inbox/spam folder for the email");
                System.out.println("📧 Subject: Confirmation de paiement - Formini Shop");
                System.out.println("📧 From: mail@brevo.com");
                
                // Valider le format de l'email destinataire
                if (!isValidEmail(toEmail)) {
                    System.err.println("⚠️ WARNING: Recipient email format may be invalid: " + toEmail);
                }
                
                return true;
            } else {
                System.err.println("❌ Failed to send email. Status: " + statusCode);
                System.err.println("❌ Response: " + responseBody);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée le contenu HTML pour l'email de confirmation de paiement
     */
    private String createPaymentConfirmationHTML(String customerName, java.math.BigDecimal totalAmount) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        return "<!DOCTYPE html>"
                + "<html><head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Confirmation de paiement - Formini Shop</title>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f9fa; }"
                + ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px; }"
                + ".header h1 { margin: 0; font-size: 28px; font-weight: bold; }"
                + ".content { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }"
                + ".thank-you { font-size: 24px; color: #28a745; text-align: center; margin-bottom: 20px; font-weight: bold; }"
                + ".details { background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }"
                + ".details h3 { color: #495057; margin-top: 0; }"
                + ".total { font-size: 20px; font-weight: bold; color: #dc3545; text-align: center; margin: 20px 0; }"
                + ".total-amount { font-size: 32px; color: #28a745; }"
                + ".footer { text-align: center; margin-top: 30px; color: #6c757d; font-size: 14px; }"
                + ".signature { margin-top: 20px; padding-top: 20px; border-top: 1px solid #e9ecef; font-style: italic; }"
                + "</style>"
                + "</head><body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h1>🎉 Merci pour votre commande !</h1>"
                + "</div>"
                + "<div class='content'>"
                + "<div class='thank-you'>Cher " + customerName + ",</div>"
                + "<p>Nous vous remercions sincèrement pour votre commande. Votre paiement a été traité avec succès.</p>"
                + "<div class='details'>"
                + "<h3>📋 Détails de la commande</h3>"
                + "<p><strong>Date:</strong> " + timestamp + "</p>"
                + "<p><strong>Montant total:</strong> <span class='total-amount'>" + String.format("%.3f DT", totalAmount.doubleValue()) + "</span></p>"
                + "<p>Une facture PDF est jointe à cet email pour vos archives.</p>"
                + "</div>"
                + "</div>"
                + "<div class='footer'>"
                + "<p><strong>Formini Shop</strong></p>"
                + "<p>L'EXCELLENCE EN FORMATION</p>"
                + "<div class='signature'>"
                + "<p>Cet email a été généré automatiquement. Pour toute question, contactez notre service client.</p>"
                + "</div>"
                + "</div>"
                + "</div>"
                + "</body></html>";
    }
    
    /**
     * Valide le format d'une adresse email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Masque la clé API pour les logs
     */
    private String maskKey(String key) {
        if (key == null || key.length() < 10) {
            return "***";
        }
        return key.substring(0, 8) + "..." + key.substring(key.length() - 4);
    }
    
    /**
     * Teste la configuration du service email
     */
    public boolean isConfigured() {
        try {
            String brevoKey = tn.formini.utils.ConfigLoader.getBrevoKey();
            return brevoKey != null && !brevoKey.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Teste la validité de la clé API Brevo
     */
    public boolean testBrevoAPIKey() {
        try {
            String brevoApiKey = tn.formini.utils.ConfigLoader.getBrevoKey();
            if (brevoApiKey == null || brevoApiKey.trim().isEmpty()) {
                System.err.println("❌ No Brevo API key found");
                return false;
            }
            
            System.out.println("🔑 Testing Brevo API key: " + maskKey(brevoApiKey));
            
            // Test avec l'endpoint de compte Brevo
            String apiUrl = "https://api.brevo.com/v3/account";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("api-key", brevoApiKey)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            int statusCode = response.statusCode();
            String responseBody = response.body();
            
            System.out.println("📥 Brevo Account API Status: " + statusCode);
            System.out.println("📥 Brevo Account Response: " + responseBody);
            
            if (statusCode == 200) {
                System.out.println("✅ Brevo API key is valid");
                return true;
            } else {
                System.err.println("❌ Brevo API key is invalid or account issue");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error testing Brevo API: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envoie un email ultra-simple pour tester
     */
    public boolean sendUltraSimpleTestEmail(String toEmail) {
        try {
            String brevoApiKey = tn.formini.utils.ConfigLoader.getBrevoKey();
            if (brevoApiKey == null) {
                System.err.println("❌ No API key");
                return false;
            }
            
            // JSON minimaliste
            String jsonPayload = "{\"sender\":{\"name\":\"Test\",\"email\":\"mail@brevo.com\"},\"to\":[{\"email\":\"" + toEmail + "\"}],\"subject\":\"Test\",\"htmlContent\":\"<p>Test</p>\"}";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("Content-Type", "application/json")
                    .header("api-key", brevoApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            System.out.println("🔥 Ultra-simple test - Status: " + response.statusCode());
            System.out.println("🔥 Ultra-simple test - Body: " + response.body());
            
            return response.statusCode() == 201;
            
        } catch (Exception e) {
            System.err.println("❌ Ultra-simple test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtient le statut de la configuration
     */
    public String getConfigurationStatus() {
        if (isConfigured()) {
            String key = tn.formini.utils.ConfigLoader.getBrevoKey();
            return "Brevo API configured (" + maskKey(key) + ")";
        } else {
            return "Brevo API not configured - see console for instructions";
        }
    }
}
