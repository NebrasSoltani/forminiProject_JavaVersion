package tn.formini.controllers.frontend;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur Stripe simplifié pour diagnostiquer les problèmes
 */
public class StripePaymentSimpleController implements Initializable {

    @FXML private Button testButton;
    @FXML private Button payButton;
    @FXML private TextField amountField;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;
    
    private boolean stripeConnected = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        logMessage("🔍 Initialisation du contrôleur Stripe...");
        
        // Test automatique au démarrage
        testStripeConnection();
    }

    private void setupUI() {
        amountField.setText("20.00");
        
        testButton.setOnAction(event -> testStripeConnection());
        payButton.setOnAction(event -> createPayment());
        
        payButton.setDisable(true);
        logArea.setEditable(false);
        logArea.setWrapText(true);
    }

    @FXML
    private void testStripeConnection() {
        logMessage("\n🔑 Test de connexion Stripe...");
        
        String stripeKey = "sk_test_51TUXHqCTsTDYpm1n1ScuYXQjLFIC91lAxHW6iPJoLv7TIvNDd5JRKFLRrMxKfxHeiPFcpROzR7TZp9tSa41vp0PY00WN6AMrT2";
        
        try {
            logMessage("📦 Configuration de la clé API...");
            logMessage("   Clé: " + stripeKey.substring(0, 20) + "...");
            
            Stripe.apiKey = stripeKey;
            
            logMessage("🌐 Test de connexion à l'API Stripe...");
            Account account = Account.retrieve();
            
            logMessage("✅ CONNEXION RÉUSSIE !");
            logMessage("📧 Email: " + (account.getEmail() != null ? account.getEmail() : "Non disponible"));
            logMessage("🌍 Pays: " + account.getCountry());
            logMessage("🏢 ID Compte: " + account.getId());
            logMessage("💼 Type: " + account.getType());
            
            stripeConnected = true;
            payButton.setDisable(false);
            statusLabel.setText("✅ Stripe connecté");
            
        } catch (StripeException e) {
            logMessage("❌ ERREUR STRIPE:");
            logMessage("   Message: " + e.getMessage());
            
            if (e.getStripeError() != null) {
                logMessage("   Code: " + e.getStripeError().getCode());
                logMessage("   Type: " + e.getStripeError().getType());
                logMessage("   HTTP Status: " + e.getStatusCode());
            }
            
            stripeConnected = false;
            payButton.setDisable(true);
            statusLabel.setText("❌ Erreur de connexion");
            
            analyzeStripeError(e);
            
        } catch (Exception e) {
            logMessage("❌ ERREUR INATTENDUE:");
            logMessage("   Message: " + e.getMessage());
            logMessage("   Type: " + e.getClass().getName());
            
            stripeConnected = false;
            payButton.setDisable(true);
            statusLabel.setText("❌ Erreur système");
        }
    }

    @FXML
    private void createPayment() {
        if (!stripeConnected) {
            logMessage("❌ Veuillez d'abord tester la connexion");
            return;
        }

        try {
            String amountText = amountField.getText();
            double amount = Double.parseDouble(amountText);
            long amountInCents = (long) (amount * 100);

            logMessage("\n💳 Création du paiement...");
            logMessage("   Montant: " + amount + " EUR");
            logMessage("   Montant en centimes: " + amountInCents);

            PaymentIntent intent = PaymentIntent.create(
                new PaymentIntentCreateParams.Builder()
                    .setAmount(amountInCents)
                    .setCurrency("eur")
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
                    )
                    .putMetadata("test", "simple_payment")
                    .build()
            );

            logMessage("✅ PAIEMENT CRÉÉ !");
            logMessage("🆔 ID: " + intent.getId());
            logMessage("💰 Montant: " + (intent.getAmount() / 100.0) + " " + intent.getCurrency().toUpperCase());
            logMessage("📊 Statut: " + intent.getStatus());
            logMessage("📅 Créé le: " + new java.util.Date(intent.getCreated() * 1000L));

            statusLabel.setText("✅ Paiement créé: " + intent.getId());

        } catch (NumberFormatException e) {
            logMessage("❌ Erreur format montant: " + e.getMessage());
        } catch (StripeException e) {
            logMessage("❌ Erreur création paiement:");
            logMessage("   Message: " + e.getMessage());
            if (e.getStripeError() != null) {
                logMessage("   Code: " + e.getStripeError().getCode());
            }
        } catch (Exception e) {
            logMessage("❌ Erreur inattendue: " + e.getMessage());
        }
    }

    private void analyzeStripeError(StripeException e) {
        String code = e.getStripeError() != null ? e.getStripeError().getCode() : "unknown";
        
        logMessage("\n🔍 Analyse de l'erreur:");
        
        switch (code) {
            case "api_key_invalid":
                logMessage("❌ Clé API invalide");
                logMessage("💡 Vérifiez que la clé est correcte");
                break;
            case "api_key_expired":
                logMessage("❌ Clé API expirée");
                logMessage("💡 Générez une nouvelle clé");
                break;
            case "rate_limit":
                logMessage("❌ Limite de dépassée");
                logMessage("💡 Attendez avant de réessayer");
                break;
            default:
                logMessage("❌ Erreur: " + code);
                break;
        }
    }

    private void logMessage(String message) {
        System.out.println(message);
        logArea.appendText(message + "\n");
        
        // Auto-scroll vers le bas
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    @FXML
    private void clearLogs() {
        logArea.clear();
        logMessage("🔍 Logs effacés");
    }
}
