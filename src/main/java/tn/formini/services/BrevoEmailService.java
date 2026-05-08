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
 * Service email utilisant Brevo API de manière simplifiée
 */
public class BrevoEmailService {
    private static BrevoEmailService instance;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    private BrevoEmailService() {}
    
    public static BrevoEmailService getInstance() {
        if (instance == null) {
            instance = new BrevoEmailService();
        }
        return instance;
    }
    
    /**
     * Envoie un email avec facture PDF via Brevo
     */
    public boolean sendEmailWithInvoice(String toEmail, String customerName, 
                                   java.math.BigDecimal totalAmount, ByteArrayOutputStream pdfStream) {
        try {
            System.out.println("📧 Sending via Brevo to: " + toEmail);
            
            // Récupérer la clé API
            String brevoApiKey = tn.formini.utils.ConfigLoader.getBrevoKey();
            if (brevoApiKey == null || brevoApiKey.trim().isEmpty()) {
                System.err.println("❌ Brevo API key not found");
                return false;
            }
            
            System.out.println("🔑 Using Brevo key: " + maskKey(brevoApiKey));
            
            // Convertir PDF en Base64
            String pdfBase64 = "";
            if (pdfStream != null && pdfStream.size() > 0) {
                pdfBase64 = Base64.getEncoder().encodeToString(pdfStream.toByteArray());
                System.out.println("📎 PDF attached: " + pdfStream.size() + " bytes");
            }
            
            // Créer le contenu HTML
            String htmlContent = createEmailHTML(customerName, totalAmount);
            
            // Construire la requête Brevo
            return sendBrevoRequest(brevoApiKey, toEmail, customerName, htmlContent, pdfBase64);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending Brevo email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envoie la requête à l'API Brevo
     */
    private boolean sendBrevoRequest(String apiKey, String toEmail, String customerName, 
                                 String htmlContent, String pdfBase64) {
        try {
            // URL de l'API Brevo
            String apiUrl = "https://api.brevo.com/v3/smtp/email";
            
            // Construire le payload JSON selon la documentation Brevo
            Map<String, Object> emailData = new HashMap<>();
            
            // Expéditeur (doit être validé dans Brevo)
            emailData.put("sender", Map.of(
                "name", "Formini Shop",
                "email", "forminiapp@gmail.com"  // Utiliser admin@formini.com
            ));
            
            // Destinataire
            emailData.put("to", new Object[]{
                Map.of(
                    "email", toEmail,
                    "name", customerName
                )
            });
            
            // Sujet et contenu
            emailData.put("subject", "Confirmation de paiement - Formini Shop");
            emailData.put("htmlContent", htmlContent);
            
            // Ajouter la pièce jointe PDF si disponible
            if (!pdfBase64.isEmpty()) {
                emailData.put("attachment", new Object[]{
                    Map.of(
                        "name", "facture_" + System.currentTimeMillis() + ".pdf",
                        "content", pdfBase64
                    )
                });
            }
            
            // Convertir en JSON
            String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(emailData);
            
            System.out.println("📤 Sending to Brevo API...");
            System.out.println("📤 JSON size: " + jsonPayload.length() + " characters");
            
            // Créer et envoyer la requête HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            // Traiter la réponse
            int statusCode = response.statusCode();
            String responseBody = response.body();
            
            System.out.println("📥 Brevo Response Status: " + statusCode);
            System.out.println("📥 Brevo Response Body: " + responseBody);
            
            if (statusCode == 201) {
                System.out.println("✅ Email sent successfully via Brevo!");
                System.out.println("📧 Check inbox/spam: " + toEmail);
                return true;
            } else {
                System.err.println("❌ Brevo API error - Status: " + statusCode);
                System.err.println("❌ Response: " + responseBody);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Brevo request failed: " + e.getMessage());
            e.printStackTrace();
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
                + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; }"
                + ".container { max-width: 600px; margin: 0 auto; background: #f8f9fa; border-radius: 10px; overflow: hidden; }"
                + ".header { background: linear-gradient(135deg, #007bff, #0056b3); color: white; padding: 30px; text-align: center; }"
                + ".header h1 { margin: 0; font-size: 28px; }"
                + ".content { padding: 40px 30px; background: white; }"
                + ".greeting { font-size: 24px; color: #28a745; margin-bottom: 20px; font-weight: bold; }"
                + ".details { background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #007bff; }"
                + ".total { text-align: center; margin: 30px 0; }"
                + ".total-amount { font-size: 32px; color: #28a745; font-weight: bold; }"
                + ".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }"
                + "</style>"
                + "</head><body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h1>🎉 Formini Shop</h1>"
                + "<h2>Confirmation de paiement</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<div class='greeting'>Bonjour " + customerName + ",</div>"
                + "<p>Merci pour votre commande ! Votre paiement a été traité avec succès.</p>"
                + "<div class='details'>"
                + "<h3>📋 Détails de la commande</h3>"
                + "<p><strong>Date:</strong> " + timestamp + "</p>"
                + "<p><strong>Montant total:</strong></p>"
                + "<div class='total'>"
                + "<div class='total-amount'>" + String.format("%.3f DT", totalAmount.doubleValue()) + "</div>"
                + "</div>"
                + "<p>Une facture PDF est jointe à cet email pour vos archives.</p>"
                + "</div>"
                + "</div>"
                + "<div class='footer'>"
                + "<p><strong>Formini Shop</strong></p>"
                + "<p>L'EXCELLENCE EN FORMATION</p>"
                + "<p><em>Envoyé via Brevo API</em></p>"
                + "</div>"
                + "</div>"
                + "</body></html>";
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
     * Test simple de l'API Brevo
     */
    public boolean testBrevoConnection() {
        try {
            String brevoApiKey = tn.formini.utils.ConfigLoader.getBrevoKey();
            if (brevoApiKey == null) {
                System.err.println("❌ No Brevo API key found");
                return false;
            }
            
            System.out.println("🔑 Testing Brevo connection...");
            
            // Test avec l'endpoint account
            String accountUrl = "https://api.brevo.com/v3/account";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(accountUrl))
                    .header("api-key", brevoApiKey)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            System.out.println("📥 Account API Status: " + response.statusCode());
            System.out.println("📥 Account Response: " + response.body());
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            System.err.println("❌ Brevo connection test failed: " + e.getMessage());
            return false;
        }
    }
}
