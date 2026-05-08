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
 * Contrôleur Stripe qui fonctionne immédiatement
 */
public class WorkingStripeController implements Initializable {

    @FXML private Button configureButton;
    @FXML private Button payButton;
    @FXML private Button testButton;
    @FXML private TextField amountField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> currencyCombo;
    @FXML private Label statusLabel;
    @FXML private TextArea resultArea;
    @FXML private ProgressIndicator loadingIndicator;
    
    private boolean stripeConfigured = false;
    private static final String STRIPE_KEY = "sk_test_51Syx8bGm24wH1K4NlMt4ot1bTPXUC0mEJlsvroYIM32vB5FuD0Vyqk3gY693nTe3HP2BJNjTHwlOiIEau1TcSw2Y00rUE0hRah";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        autoConfigure();
    }

    private void setupUI() {
        // Configuration des composants
        currencyCombo.getItems().addAll("EUR", "USD", "TND");
        currencyCombo.getSelectionModel().select("EUR");
        
        amountField.setText("20.00");
        emailField.setText("test@example.com");
        
        // Actions
        configureButton.setOnAction(event -> configureStripe());
        testButton.setOnAction(event -> testConnection());
        payButton.setOnAction(event -> createPayment());
        
        // État initial
        payButton.setDisable(true);
        loadingIndicator.setVisible(false);
        resultArea.setEditable(false);
        
        logMessage("🚀 Interface Stripe initialisée");
    }

    private void autoConfigure() {
        logMessage("🔑 Configuration automatique de Stripe...");
        configureStripe();
    }

    @FXML
    private void configureStripe() {
        try {
            logMessage("📦 Configuration de la clé API...");
            logMessage("   Clé: " + STRIPE_KEY.substring(0, 20) + "...");
            
            Stripe.apiKey = STRIPE_KEY;
            
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
            
            logMessage("\n🎉 Stripe est maintenant configuré et prêt à utiliser !");
            logMessage("💳 Vous pouvez maintenant créer des paiements");
            
        } catch (StripeException e) {
            logMessage("❌ ERREUR DE CONNEXION:");
            logMessage("   Message: " + e.getMessage());
            
            if (e.getStripeError() != null) {
                logMessage("   Code: " + e.getStripeError().getCode());
                logMessage("   Type: " + e.getStripeError().getType());
                logMessage("   HTTP Status: " + e.getStatusCode());
                
                // Solution suggérée
                switch (e.getStripeError().getCode()) {
                    case "api_key_invalid":
                        logMessage("💡 Solution: La clé est invalide");
                        break;
                    case "rate_limit":
                        logMessage("💡 Solution: Attendez 1 minute");
                        break;
                    default:
                        logMessage("💡 Solution: Vérifiez la clé et réseau");
                        break;
                }
            }
            
            stripeConfigured = false;
            payButton.setDisable(true);
            statusLabel.setText("❌ Erreur de connexion Stripe");
            
        } catch (Exception e) {
            logMessage("❌ ERREUR SYSTÈME:");
            logMessage("   Message: " + e.getMessage());
            stripeConfigured = false;
            payButton.setDisable(true);
            statusLabel.setText("❌ Erreur système");
        }
    }

    @FXML
    private void testConnection() {
        logMessage("\n🔍 Nouveau test de connexion...");
        configureStripe();
    }

    @FXML
    private void createPayment() {
        if (!stripeConfigured) {
            logMessage("❌ Stripe n'est pas configuré");
            return;
        }

        try {
            // Validation des entrées
            String amountText = amountField.getText().trim();
            String email = emailField.getText().trim();
            String currency = currencyCombo.getValue();
            
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
            
            logMessage("\n💳 CRÉATION DU PAIEMENT...");
            logMessage("   Montant: " + amount + " " + currency);
            logMessage("   Email: " + email);
            logMessage("   Montant en centimes: " + amountInCents);
            
            loadingIndicator.setVisible(true);
            statusLabel.setText("🔄 Création du paiement...");

            // Créer le PaymentIntent
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
                    .putMetadata("source", "javafx_app")
                    .putMetadata("order_id", "order_" + System.currentTimeMillis())
                    .build()
            );

            loadingIndicator.setVisible(false);
            
            // Succès
            logMessage("✅ PAIEMENT CRÉÉ AVEC SUCCÈS !");
            logMessage("🆔 ID PaymentIntent: " + intent.getId());
            logMessage("💰 Montant: " + (intent.getAmount() / 100.0) + " " + intent.getCurrency().toUpperCase());
            logMessage("📊 Statut: " + intent.getStatus());
            logMessage("📧 Email client: " + email);
            logMessage("📅 Date création: " + new java.util.Date(intent.getCreated() * 1000L));
            logMessage("🔗 Client Secret: " + intent.getClientSecret());
            
            statusLabel.setText("✅ Paiement créé: " + intent.getId());
            
            // Afficher un résumé
            showPaymentSummary(intent);
            
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
            }
            statusLabel.setText("❌ Erreur création paiement");
        } catch (Exception e) {
            loadingIndicator.setVisible(false);
            logMessage("❌ Erreur inattendue: " + e.getMessage());
            statusLabel.setText("❌ Erreur inattendue");
        }
    }

    private void showPaymentSummary(PaymentIntent intent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ Paiement créé");
        alert.setHeaderText("PaymentIntent créé avec succès");
        
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
