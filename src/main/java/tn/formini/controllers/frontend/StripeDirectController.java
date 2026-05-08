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
 * Contrôleur Stripe qui fonctionne DIRECTEMENT sans configuration externe
 */
public class StripeDirectController implements Initializable {

    @FXML private Button payButton;
    @FXML private Button testButton;
    @FXML private TextField amountField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> currencyCombo;
    @FXML private Label statusLabel;
    @FXML private TextArea resultArea;
    @FXML private ProgressIndicator loadingIndicator;
    
    private boolean stripeConfigured = false;
    
    // VOTRE CLÉ STRIPE DIRECTEMENT ICI
    private static final String STRIPE_KEY = "sk_test_51Syx8bGm24wH1K4NlMt4ot1bTPXUC0mEJlsvroYIM32vB5FuD0Vyqk3gY693nTe3HP2BJNjTHwlOiIEau1TcSw2Y00rUE0hRah";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        configureStripeDirect();
    }

    private void setupUI() {
        System.out.println("🚀 Initialisation de l'interface Stripe DIRECT...");
        
        // Configuration des composants
        currencyCombo.getItems().addAll("EUR", "USD", "TND");
        currencyCombo.getSelectionModel().select("EUR");
        
        amountField.setText("20.00");
        emailField.setText("test@example.com");
        
        // Actions
        testButton.setOnAction(event -> testStripeDirect());
        payButton.setOnAction(event -> createPaymentDirect());
        
        // État initial
        payButton.setDisable(true);
        loadingIndicator.setVisible(false);
        resultArea.setEditable(false);
        
        logMessage("✅ Interface Stripe initialisée");
        logMessage("🔑 Clé configurée: " + STRIPE_KEY.substring(0, 20) + "...");
    }

    private void configureStripeDirect() {
        logMessage("\n🔑 Configuration DIRECTE de Stripe...");
        
        try {
            // Configuration DIRECTE sans passer par les properties
            Stripe.apiKey = STRIPE_KEY;
            logMessage("✅ Clé configurée directement");
            
            logMessage("🌐 Test de connexion...");
            Account account = Account.retrieve();
            
            logMessage("✅ CONNEXION RÉUSSIE !");
            logMessage("📧 Email: " + (account.getEmail() != null ? account.getEmail() : "Non disponible"));
            logMessage("🌍 Pays: " + account.getCountry());
            logMessage("🏢 ID Compte: " + account.getId());
            logMessage("💼 Type: " + account.getType());
            
            stripeConfigured = true;
            payButton.setDisable(false);
            statusLabel.setText("✅ Stripe connecté et prêt !");
            
            logMessage("\n🎉 STRIPE EST CONFIGURÉ ET FONCTIONNEL !");
            logMessage("💳 Vous pouvez maintenant créer des paiements");
            
        } catch (StripeException e) {
            logMessage("❌ ERREUR DE CONNEXION:");
            logMessage("   Message: " + e.getMessage());
            
            if (e.getStripeError() != null) {
                logMessage("   Code: " + e.getStripeError().getCode());
                logMessage("   Type: " + e.getStripeError().getType());
                logMessage("   HTTP Status: " + e.getStatusCode());
            }
            
            stripeConfigured = false;
            payButton.setDisable(true);
            statusLabel.setText("❌ Erreur de connexion");
            
        } catch (Exception e) {
            logMessage("❌ ERREUR SYSTÈME:");
            logMessage("   Message: " + e.getMessage());
            logMessage("   Type: " + e.getClass().getName());
            
            stripeConfigured = false;
            payButton.setDisable(true);
            statusLabel.setText("❌ Erreur système");
        }
    }

    @FXML
    private void testStripeDirect() {
        logMessage("\n🔍 Nouveau test DIRECT...");
        configureStripeDirect();
    }

    @FXML
    private void createPaymentDirect() {
        if (!stripeConfigured) {
            logMessage("❌ Stripe n'est pas configuré");
            return;
        }

        try {
            // Validation des entrées
            String amountText = amountField.getText().trim();
            String email = emailField.getText().trim();
            String currency = currencyCombo.getValue();
            
            logMessage("\n💳 DÉBUT CRÉATION PAIEMENT DIRECT...");
            logMessage("   Montant: " + amountText + " " + currency);
            logMessage("   Email: " + email);
            logMessage("   Devise: " + currency);
            
            if (amountText.isEmpty()) {
                logMessage("❌ Montant requis");
                return;
            }
            
            if (email.isEmpty()) {
                logMessage("❌ Email requis");
                return;
            }

            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                logMessage("❌ Montant doit être positif");
                return;
            }

            long amountInCents = (long) (amount * 100);
            
            logMessage("   Montant en centimes: " + amountInCents);
            
            loadingIndicator.setVisible(true);
            statusLabel.setText("🔄 Création du paiement...");

            // CRÉATION DIRECTE du PaymentIntent
            logMessage("🌐 Appel à l'API Stripe...");
            
            PaymentIntent intent = PaymentIntent.create(
                new PaymentIntentCreateParams.Builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
                    )
                    .putMetadata("email", email)
                    .putMetadata("source", "javafx_direct")
                    .putMetadata("order_id", "order_" + System.currentTimeMillis())
                    .build()
            );

            loadingIndicator.setVisible(false);
            
            // SUCCÈS COMPLET
            logMessage("\n✅ PAIEMENT CRÉÉ AVEC SUCCÈS !");
            logMessage("🆔 ID PaymentIntent: " + intent.getId());
            logMessage("💰 Montant: " + (intent.getAmount() / 100.0) + " " + intent.getCurrency().toUpperCase());
            logMessage("📊 Statut: " + intent.getStatus());
            logMessage("📧 Email client: " + email);
            logMessage("📅 Date création: " + new java.util.Date(intent.getCreated() * 1000L));
            logMessage("🔗 Client Secret: " + intent.getClientSecret());
            
            statusLabel.setText("✅ Paiement créé: " + intent.getId());
            
            // Afficher le résumé
            showPaymentSummary(intent);
            
            logMessage("\n🎉 PAIEMENT TERMINÉ AVEC SUCCÈS !");
            
        } catch (NumberFormatException e) {
            loadingIndicator.setVisible(false);
            logMessage("❌ Erreur format montant: " + e.getMessage());
            statusLabel.setText("❌ Format montant invalide");
        } catch (StripeException e) {
            loadingIndicator.setVisible(false);
            logMessage("❌ Erreur création paiement:");
            logMessage("   Message: " + e.getMessage());
            if (e.getStripeError() != null) {
                logMessage("   Code: " + e.getStripeError().getCode());
                logMessage("   Type: " + e.getStripeError().getType());
            }
            statusLabel.setText("❌ Erreur création paiement");
        } catch (Exception e) {
            loadingIndicator.setVisible(false);
            logMessage("❌ Erreur inattendue: " + e.getMessage());
            logMessage("   Type: " + e.getClass().getName());
            e.printStackTrace();
            statusLabel.setText("❌ Erreur inattendue");
        }
    }

    private void showPaymentSummary(PaymentIntent intent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ Paiement créé avec succès");
        alert.setHeaderText("PaymentIntent créé");
        
        String content = "🆔 ID: " + intent.getId() + "\n" +
                        "💰 Montant: " + (intent.getAmount() / 100.0) + " " + intent.getCurrency().toUpperCase() + "\n" +
                        "📊 Statut: " + intent.getStatus() + "\n" +
                        "📧 Email: " + emailField.getText() + "\n" +
                        "📅 Créé le: " + new java.util.Date(intent.getCreated() * 1000L) + "\n\n" +
                        "🔗 Client Secret (pour frontend):\n" + intent.getClientSecret();
        
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void logMessage(String message) {
        System.out.println(message);
        resultArea.appendText(message + "\n");
        
        // Auto-scroll vers le bas
        resultArea.setScrollTop(Double.MAX_VALUE);
    }

    @FXML
    private void clearLogs() {
        resultArea.clear();
        logMessage("🔍 Logs effacés - Interface prête");
    }

    @FXML
    private void resetForm() {
        amountField.setText("20.00");
        emailField.setText("test@example.com");
        currencyCombo.getSelectionModel().select("EUR");
        statusLabel.setText("🔄 Formulaire réinitialisé");
        logMessage("🔄 Formulaire réinitialisé");
    }
}
