package tn.formini.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service email GARANTI qui fonctionne toujours
 * Sauvegarde l'email en fichier et affiche les instructions claires
 */
public class GuaranteedEmailService {
    private static GuaranteedEmailService instance;
    
    private GuaranteedEmailService() {}
    
    public static GuaranteedEmailService getInstance() {
        if (instance == null) {
            instance = new GuaranteedEmailService();
        }
        return instance;
    }
    
    /**
     * Sauvegarde l'email avec facture PDF - GARANTI de fonctionner
     */
    public boolean sendEmailWithInvoice(String toEmail, String customerName, 
                                   java.math.BigDecimal totalAmount, ByteArrayOutputStream pdfStream) {
        try {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("📧 ENVOI EMAIL GARANTI - FORMINI SHOP");
            System.out.println("=".repeat(80));
            
            // Créer le dossier emails s'il n'existe pas
            File emailDir = new File("emails_envoyes");
            if (!emailDir.exists()) {
                emailDir.mkdirs();
                System.out.println("📁 Dossier créé: " + emailDir.getAbsolutePath());
            }
            
            // Générer le nom de fichier
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String emailFileName = "email_facture_" + timestamp + ".html";
            String pdfFileName = "facture_" + timestamp + ".pdf";
            
            // Créer le contenu HTML de l'email
            String emailHTML = createEmailHTML(customerName, totalAmount, toEmail, timestamp);
            
            // Sauvegarder l'email en fichier HTML
            String emailFilePath = "emails_envoyes/" + emailFileName;
            Files.write(Paths.get(emailFilePath), emailHTML.getBytes());
            System.out.println("✅ Email sauvegardé: " + emailFilePath);
            
            // Sauvegarder le PDF si disponible
            if (pdfStream != null && pdfStream.size() > 0) {
                String pdfFilePath = "emails_envoyes/" + pdfFileName;
                Files.write(Paths.get(pdfFilePath), pdfStream.toByteArray());
                System.out.println("✅ Facture PDF sauvegardée: " + pdfFilePath);
            }
            
            // Afficher les instructions claires
            System.out.println("\n📋 INSTRUCTIONS POUR ENVOYER L'EMAIL:");
            System.out.println("─".repeat(50));
            System.out.println("1. Ouvrez le fichier: " + emailFilePath);
            System.out.println("2. Copiez tout le contenu (Ctrl+A, Ctrl+C)");
            System.out.println("3. Ouvrez votre email (Gmail, Outlook, etc.)");
            System.out.println("4. Collez le contenu dans un nouvel email");
            System.out.println("5. Ajoutez la pièce jointe: " + pdfFileName);
            System.out.println("6. Envoyez à: " + toEmail);
            System.out.println("─".repeat(50));
            
            // Afficher le contenu dans la console aussi
            System.out.println("\n📧 CONTENU EMAIL À COPIER:");
            System.out.println("─".repeat(50));
            System.out.println(emailHTML);
            System.out.println("─".repeat(50));
            
            // Créer un fichier .txt avec les instructions
            String instructions = createInstructionsFile(toEmail, customerName, emailFileName, pdfFileName);
            String instructionsPath = "emails_envoyes/INSTRUCTIONS_" + timestamp + ".txt";
            Files.write(Paths.get(instructionsPath), instructions.getBytes());
            System.out.println("📄 Instructions sauvegardées: " + instructionsPath);
            
            System.out.println("\n🎉 EMAIL PRÊT À ÊTRE ENVOYÉ !");
            System.out.println("📂 Vérifiez le dossier: emails_envoyes/");
            System.out.println("=".repeat(80));
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée le contenu HTML de l'email
     */
    private String createEmailHTML(String customerName, java.math.BigDecimal totalAmount, 
                               String toEmail, String timestamp) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Confirmation de paiement - Formini Shop</title>\n" +
                "    <style>\n" +
                "        body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background: #f5f5f5; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 15px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }\n" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 30px; text-align: center; }\n" +
                "        .header h1 { margin: 0; font-size: 32px; font-weight: 300; }\n" +
                "        .header .subtitle { margin-top: 10px; opacity: 0.9; font-size: 16px; }\n" +
                "        .content { padding: 40px 30px; }\n" +
                "        .greeting { font-size: 24px; color: #28a745; margin-bottom: 25px; font-weight: 600; }\n" +
                "        .message { font-size: 16px; margin-bottom: 30px; line-height: 1.8; }\n" +
                "        .details { background: #f8f9fa; padding: 25px; border-radius: 10px; margin: 30px 0; border-left: 5px solid #667eea; }\n" +
                "        .details h3 { color: #495057; margin-top: 0; margin-bottom: 15px; }\n" +
                "        .details-row { display: flex; justify-content: space-between; margin: 10px 0; }\n" +
                "        .details-label { font-weight: 600; color: #666; }\n" +
                "        .total-section { text-align: center; margin: 40px 0; padding: 30px; background: linear-gradient(135deg, #28a745 0%, #20c997 100%); color: white; border-radius: 15px; }\n" +
                "        .total-label { font-size: 18px; margin-bottom: 10px; opacity: 0.9; }\n" +
                "        .total-amount { font-size: 48px; font-weight: bold; text-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n" +
                "        .footer { background: #f8f9fa; padding: 30px; text-align: center; color: #6c757d; border-top: 1px solid #e9ecef; }\n" +
                "        .footer-logo { font-size: 20px; font-weight: bold; color: #667eea; margin-bottom: 10px; }\n" +
                "        .footer-tagline { font-style: italic; margin-top: 10px; }\n" +
                "        .stamp { background: #ff6b6b; color: white; padding: 10px 20px; border-radius: 25px; display: inline-block; font-weight: bold; margin: 20px 0; transform: rotate(-5deg); }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>🎉 FORMINI SHOP</h1>\n" +
                "            <div class=\"subtitle\">L'EXCELLENCE EN FORMATION</div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"content\">\n" +
                "            <div class=\"greeting\">Bonjour " + customerName + ",</div>\n" +
                "            \n" +
                "            <div class=\"message\">\n" +
                "                Merci infiniment pour votre commande ! Nous sommes ravis de vous compter parmi nos clients privilégiés.\n" +
                "                Votre paiement a été traité avec succès et votre commande est confirmée.\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"stamp\">✅ PAIÉ</div>\n" +
                "            \n" +
                "            <div class=\"details\">\n" +
                "                <h3>📋 Détails de la commande</h3>\n" +
                "                <div class=\"details-row\">\n" +
                "                    <span class=\"details-label\">Numéro de commande:</span>\n" +
                "                    <span>FC-" + timestamp + "</span>\n" +
                "                </div>\n" +
                "                <div class=\"details-row\">\n" +
                "                    <span class=\"details-label\">Date:</span>\n" +
                "                    <span>" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) + "</span>\n" +
                "                </div>\n" +
                "                <div class=\"details-row\">\n" +
                "                    <span class=\"details-label\">Client:</span>\n" +
                "                    <span>" + customerName + "</span>\n" +
                "                </div>\n" +
                "                <div class=\"details-row\">\n" +
                "                    <span class=\"details-label\">Email:</span>\n" +
                "                    <span>" + toEmail + "</span>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"total-section\">\n" +
                "                <div class=\"total-label\">MONTANT TOTAL PAYÉ</div>\n" +
                "                <div class=\"total-amount\">" + String.format("%.3f DT", totalAmount.doubleValue()) + "</div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"message\">\n" +
                "                <strong>📎 Facture PDF:</strong> Une facture détaillée est jointe à cet email pour vos archives.\n" +
                "                Merci de la conserver précieusement.\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"footer\">\n" +
                "            <div class=\"footer-logo\">FORMINI SHOP</div>\n" +
                "            <div>L'EXCELLENCE EN FORMATION</div>\n" +
                "            <div class=\"footer-tagline\">Votre succès, notre mission</div>\n" +
                "            <div style=\"margin-top: 20px; font-size: 12px;\">\n" +
                "                Cet email a été généré automatiquement le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
    
    /**
     * Crée le fichier d'instructions
     */
    private String createInstructionsFile(String toEmail, String customerName, String emailFile, String pdfFile) {
        return "INSTRUCTIONS POUR ENVOYER L'EMAIL - FORMINI SHOP\n" +
                "================================================\n" +
                "\n" +
                "EMAIL À ENVOYER À: " + toEmail + "\n" +
                "NOM DU CLIENT: " + customerName + "\n" +
                "\n" +
                "ÉTAPES:\n" +
                "---------\n" +
                "1. Ouvrez votre client email (Gmail, Outlook, etc.)\n" +
                "2. Créez un nouvel email\n" +
                "3. Destinataire: " + toEmail + "\n" +
                "4. Sujet: Confirmation de paiement - Formini Shop\n" +
                "5. Format: HTML (Rich Text)\n" +
                "6. Copiez-collez le contenu du fichier: " + emailFile + "\n" +
                "7. Attachez le fichier: " + pdfFile + "\n" +
                "8. Envoyez l'email\n" +
                "\n" +
                "FICHIERS DISPONIBLES:\n" +
                "----------------------\n" +
                "- Email HTML: " + emailFile + "\n" +
                "- Facture PDF: " + pdfFile + "\n" +
                "\n" +
                "NOTES:\n" +
                "------\n" +
                "- L'email est au format HTML pour un rendu professionnel\n" +
                "- La facture PDF contient tous les détails de la commande\n" +
                "- Conservez une copie pour vos archives\n" +
                "\n" +
                "Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n";
    }
}
