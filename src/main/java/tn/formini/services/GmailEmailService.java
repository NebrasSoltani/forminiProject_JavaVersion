package tn.formini.services;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * Service email utilisant Gmail SMTP directement
 */
public class GmailEmailService {
    private static GmailEmailService instance;
    
    private GmailEmailService() {}
    
    public static GmailEmailService getInstance() {
        if (instance == null) {
            instance = new GmailEmailService();
        }
        return instance;
    }
    
    /**
     * Envoie un email de test via Gmail SMTP
     */
    public boolean sendTestEmail(String toEmail, String customerName) {
        try {
            System.out.println("📧 Sending TEST email via Gmail SMTP to: " + toEmail);
            
            // Configuration Gmail SMTP
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            
            // Authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("soltaninebras304@gmail.com", "gooq dlkh xdxl btda");
                }
            });
            
            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("soltaninebras304@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🧪 TEST - Formini Shop (Gmail SMTP)");
            
            // Contenu HTML
            String htmlContent = "<html><body>" +
                    "<h2>Test Email - Gmail SMTP</h2>" +
                    "<p>Bonjour " + customerName + ",</p>" +
                    "<p>Ceci est un email de test envoyé via Gmail SMTP.</p>" +
                    "<p><strong>Destinataire:</strong> " + toEmail + "</p>" +
                    "<p><strong>Heure:</strong> " + java.time.LocalDateTime.now() + "</p>" +
                    "<p><strong>Expéditeur:</strong> soltaninebras304@gmail.com</p>" +
                    "<hr>" +
                    "<p><em>Si vous recevez cet email, le service Gmail SMTP fonctionne !</em></p>" +
                    "</body></html>";
            
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            // Envoyer
            Transport.send(message);
            System.out.println("✅ Gmail SMTP email sent successfully to: " + toEmail);
            return true;
            
        } catch (AuthenticationFailedException e) {
            System.err.println("❌ Gmail authentication failed: " + e.getMessage());
            System.err.println("   Check Gmail app password settings");
            return false;
        } catch (MessagingException e) {
            System.err.println("❌ Gmail SMTP error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envoie un email de confirmation de paiement avec PDF via Gmail SMTP
     */
    public boolean sendPaymentConfirmationEmail(String toEmail, String customerName, 
                                          java.math.BigDecimal totalAmount, String pdfBase64) {
        try {
            System.out.println("📧 Sending payment confirmation via Gmail SMTP to: " + toEmail);
            
            // Configuration Gmail SMTP
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            
            // Authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("soltaninebras304@gmail.com", "gooq dlkh xdxl btda");
                }
            });
            
            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("soltaninebras304@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Confirmation de paiement - Formini Shop");
            
            // Créer la partie multipart pour HTML + PDF
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent = createPaymentConfirmationHTML(customerName, totalAmount);
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            
            // Ajouter le PDF si disponible
            MimeBodyPart attachmentPart = null;
            if (pdfBase64 != null && !pdfBase64.trim().isEmpty()) {
                try {
                    attachmentPart = new MimeBodyPart();
                    byte[] pdfBytes = java.util.Base64.getDecoder().decode(pdfBase64);
                    attachmentPart.setContent(pdfBytes, "application/pdf");
                    attachmentPart.setFileName("facture_" + System.currentTimeMillis() + ".pdf");
                    attachmentPart.setDisposition(MimeBodyPart.ATTACHMENT);
                    System.out.println("📎 PDF attachment added: " + pdfBytes.length + " bytes");
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to attach PDF: " + e.getMessage());
                }
            }
            
            // Assembler le message
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);
            if (attachmentPart != null) {
                multipart.addBodyPart(attachmentPart);
            }
            
            message.setContent(multipart);
            
            // Envoyer
            Transport.send(message);
            System.out.println("✅ Gmail SMTP payment confirmation sent to: " + toEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error sending Gmail SMTP payment email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée le contenu HTML pour l'email de confirmation
     */
    private String createPaymentConfirmationHTML(String customerName, java.math.BigDecimal totalAmount) {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
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
                + "<p><em>Envoyé via Gmail SMTP</em></p>"
                + "</div>"
                + "</div>"
                + "</body></html>";
    }
}
