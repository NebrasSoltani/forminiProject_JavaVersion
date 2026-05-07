package tn.formini.controllers.frontend;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class StripePaymentController implements Initializable {

    @FXML private Button payButton;
    @FXML private Label statusLabel;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> currencyCombo;
    @FXML private VBox mainContainer;
    @FXML private Button configureButton;
    
    private String stripeSecretKey;
    private boolean stripeConfigured = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        checkStripeConfiguration();
    }

    private void setupUI() {
        // Configuration des composants
        currencyCombo.getItems().addAll("EUR", "USD", "TND");
        currencyCombo.getSelectionModel().select("EUR");
        
        amountField.setText("20.00");
        
        // Actions
        payButton.setOnAction(event -> processPayment());
        configureButton.setOnAction(event -> configureStripe());
        
        // État initial
        payButton.setDisable(true);
        statusLabel.setText("⚠️ Stripe non configuré");
    }

    private void checkStripeConfiguration() {
        // 1. Vérifier les variables d'environnement
        stripeSecretKey = System.getenv("STRIPE_SECRET_KEY");
        
        if (stripeSecretKey != null && !stripeSecretKey.isEmpty()) {
            initializeStripe();
            return;
        }
        
        // 2. Vérifier les propriétés système
        stripeSecretKey = System.getProperty("stripe.secret.key");
        if (stripeSecretKey != null && !stripeSecretKey.isEmpty()) {
            initializeStripe();
            return;
        }
        
        // 3. Afficher le bouton de configuration
        statusLabel.setText("🔑 Cliquez sur 'Configurer Stripe' pour continuer");
        configureButton.setVisible(true);
        payButton.setDisable(true);
    }

    @FXML
    private void configureStripe() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Configuration Stripe");
        dialog.setHeaderText("Configuration requise pour Stripe");
        dialog.setContentText("Entrez votre clé secrète Stripe (sk_test_...):");
        
        // Mettre un placeholder pour guider l'utilisateur
        dialog.getEditor().setPromptText("sk_test_51TUXHqCTsTDYpm1n1ScuYXQjLFIC91lAxHW6iPJoLv7TIvNDd5JRKFLRrMxKfxHeiPFcpROzR7TZp9tSa41vp0PY00WN6AMrT2");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            stripeSecretKey = result.get().trim();
            if (stripeSecretKey.startsWith("sk_test_") || stripeSecretKey.startsWith("sk_live_")) {
                initializeStripe();
            } else {
                showError("Clé invalide", "La clé doit commencer par 'sk_test_' ou 'sk_live_'");
            }
        }
    }

    private void initializeStripe() {
        try {
            Stripe.apiKey = stripeSecretKey;
            
            // Test de connexion
            Account account = Account.retrieve();
            
            // Succès
            stripeConfigured = true;
            statusLabel.setText("✅ Stripe connecté - Compte: " + account.getCountry());
            payButton.setDisable(false);
            configureButton.setVisible(false);
            
            System.out.println("✅ Stripe initialisé avec succès");
            System.out.println("📧 Email: " + account.getEmail());
            System.out.println("🌍 Pays: " + account.getCountry());
            
        } catch (StripeException e) {
            stripeConfigured = false;
            statusLabel.setText("❌ Erreur de connexion: " + e.getMessage());
            payButton.setDisable(true);
            configureButton.setVisible(true);
            
            System.err.println("❌ Erreur Stripe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void processPayment() {
        if (!stripeConfigured) {
            showError("Non configuré", "Veuillez configurer Stripe d'abord");
            return;
        }

        try {
            // Valider le montant
            String amountText = amountField.getText();
            if (amountText.isEmpty()) {
                showError("Montant requis", "Veuillez entrer un montant");
                return;
            }

            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showError("Montant invalide", "Le montant doit être positif");
                return;
            }

            long amountInCents = (long) (amount * 100);
            String currency = currencyCombo.getValue().toLowerCase();

            statusLabel.setText("🔄 Création du paiement...");

            // Créer le PaymentIntent
            PaymentIntent intent = PaymentIntent.create(
                new PaymentIntentCreateParams.Builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency)
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
                    )
                    .putMetadata("order_id", "order_" + System.currentTimeMillis())
                    .build()
            );

            // Succès
            statusLabel.setText("✅ Paiement créé - ID: " + intent.getId());
            System.out.println("✅ PaymentIntent créé: " + intent.getId());
            System.out.println("💰 Montant: " + (amountInCents / 100.0) + " " + currency.toUpperCase());
            System.out.println("📊 Statut: " + intent.getStatus());

            // Afficher les détails
            showPaymentDetails(intent);

        } catch (NumberFormatException e) {
            showError("Format invalide", "Le montant doit être un nombre valide");
        } catch (StripeException e) {
            statusLabel.setText("❌ Erreur paiement: " + e.getMessage());
            System.err.println("❌ Erreur PaymentIntent: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showPaymentDetails(PaymentIntent intent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Paiement créé");
        alert.setHeaderText("✅ PaymentIntent créé avec succès");
        
        String content = "ID: " + intent.getId() + "\n" +
                        "Montant: " + (intent.getAmount() / 100.0) + " " + intent.getCurrency().toUpperCase() + "\n" +
                        "Statut: " + intent.getStatus() + "\n" +
                        "Créé le: " + intent.getCreated();
        
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void resetConfiguration() {
        stripeConfigured = false;
        stripeSecretKey = null;
        payButton.setDisable(true);
        configureButton.setVisible(true);
        statusLabel.setText("⚠️ Stripe non configuré");
        System.out.println("🔄 Configuration réinitialisée");
    }
}
