package tn.formini.controllers.frontend;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.layout.*;

import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.print.PrinterJob;
import javafx.application.Platform;
import tn.formini.services.produitsService.CommandeService;
import tn.formini.services.cart.CartItem;
import tn.formini.services.cart.CartService;
import tn.formini.entities.produits.Produit;
import tn.formini.services.SimpleCartAIService;
import tn.formini.services.SimpleAdvancedProductAIService;
import tn.formini.services.StripePaymentService;


import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;


public class CartController implements Initializable {

    // TableView supprimée - utilisation de FlowPane pour les cards

    @FXML private Label labelGrandTotal;
    @FXML private Label labelItemsCount;
    @FXML private Label labelEmpty;
    @FXML private Button btnClear;
    @FXML private Button btnValidate;
    @FXML private Button btnPrintInvoice;
    @FXML private Button btnGetSuggestions;
    @FXML private Button btnPayStripe;
    @FXML private FlowPane suggestionsContainer;
    @FXML private FlowPane cartItemsContainer;

    @FXML private ProgressIndicator suggestionsLoading;
    @FXML private VBox noSuggestionsContainer;
    @FXML private ScrollPane suggestionsScrollPane;

    private final CartService cart = CartService.getInstance();

    // Implémentations simples pour remplacer les services manquants
    private final SimpleCartAIService cartAIService = new SimpleCartAIService();
    private final SimpleAdvancedProductAIService advancedAIService = new SimpleAdvancedProductAIService();

    // Customer information for payment processing
    private String currentCustomerName;
    private String currentCustomerEmail;
    private String currentCustomerPhone;
    private String currentCustomerAddress;
    private String lastEmailMessageId;

    private enum SuggestionSort {
        ORDER, NAME, PRICE, STOCK
    }

    private SuggestionSort suggestionSort = SuggestionSort.ORDER;
    private FlowPane suggestionsCardsFlow;
    private final List<Produit> suggestionsSource = new ArrayList<>();
    private final Map<SuggestionSort, Button> suggestionSortButtons = new EnumMap<>(SuggestionSort.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des bindings de base
        labelGrandTotal.textProperty().bind(Bindings.createStringBinding(
                () -> formatMoney(cart.getGrandTotal()),
                cart.getItems()
        ));

        if (labelItemsCount != null) {
            labelItemsCount.textProperty().bind(Bindings.createStringBinding(
                    () -> cart.getItemsCountDistinct() + " produits / " + cart.getItemsCountTotal() + " articles",
                    cart.getItems()
            ));
        }

        labelEmpty.visibleProperty().bind(Bindings.isEmpty(cart.getItems()));
        btnClear.disableProperty().bind(Bindings.isEmpty(cart.getItems()));
        if (btnValidate != null) {
            btnValidate.disableProperty().bind(Bindings.isEmpty(cart.getItems()));
        }
        if (btnPrintInvoice != null) {
            btnPrintInvoice.disableProperty().bind(Bindings.isEmpty(cart.getItems()));
        }

        // Initialize Stripe button
        if (btnPayStripe != null) {
            btnPayStripe.disableProperty().bind(Bindings.isEmpty(cart.getItems()));
        }

        // Initialiser les composants de suggestions IA
        if (btnGetSuggestions != null) {
            btnGetSuggestions.setOnAction(event -> handleGetSuggestions());
            // Désactiver si le panier est vide
            btnGetSuggestions.disableProperty().bind(Bindings.isEmpty(cart.getItems()));
        }

        // Initialiser le conteneur de cards du panier
        if (cartItemsContainer != null) {
            refreshCartCards();
        }

        // Observer les changements dans le panier
        cart.getItems().addListener((javafx.collections.ListChangeListener<CartItem>) change -> {
            refreshCartCards();
        });

        if (suggestionsLoading != null) {
            suggestionsLoading.setVisible(false);
        }

        if (suggestionsScrollPane != null) {
            suggestionsScrollPane.setVisible(false);
            suggestionsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            suggestionsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }

        if (noSuggestionsContainer != null) {
            noSuggestionsContainer.setVisible(true);
        }
    }

    private void refreshCartCards() {
        if (cartItemsContainer == null) return;

        Platform.runLater(() -> {
            cartItemsContainer.getChildren().clear();

            for (CartItem item : cart.getItems()) {
                VBox card = createCartItemCard(item);
                cartItemsContainer.getChildren().add(card);
            }
        });
    }

    private VBox createCartItemCard(CartItem item) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1px; " +
                "-fx-border-radius: 15px; -fx-background-radius: 15px; -fx-padding: 20px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");
        card.setPrefWidth(280);

        // Header avec image et nom
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.TOP_LEFT);

        // Image du produit
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        if (item.getProduit().getImage() != null && !item.getProduit().getImage().trim().isEmpty()) {
            try {
                imageView.setImage(new Image(item.getProduit().getImage().trim(), true));
            } catch (Exception ignored) {
                // Image par défaut si erreur
                imageView.setImage(new Image("/images/default-product.png", true));
            }
        }

        // Info produit
        VBox productInfo = new VBox(5);
        Label nameLabel = new Label(item.getProduit().getNom());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-wrap-text: true;");
        nameLabel.setMaxWidth(180);

        Label categoryLabel = new Label(item.getProduit().getCategorie());
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #667eea; -fx-background-color: #e3f2fd; " +
                "-fx-background-radius: 8px; -fx-padding: 4px 8px;");

        productInfo.getChildren().addAll(nameLabel, categoryLabel);
        header.getChildren().addAll(imageView, productInfo);

        // Prix et quantité
        HBox priceQtyBox = new HBox(15);
        priceQtyBox.setAlignment(javafx.geometry.Pos.CENTER);
        priceQtyBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10px; -fx-padding: 10px;");

        VBox priceBox = new VBox(3);
        priceBox.setAlignment(javafx.geometry.Pos.CENTER);
        Label unitPriceLabel = new Label(formatMoney(item.getProduit().getPrix()));
        unitPriceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        Label totalPriceLabel = new Label(formatMoney(item.getLineTotal()));
        totalPriceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        priceBox.getChildren().addAll(unitPriceLabel, totalPriceLabel);

        // Contrôles de quantité
        HBox qtyControls = new HBox(5);
        qtyControls.setAlignment(javafx.geometry.Pos.CENTER);

        Button minusBtn = new Button("-");
        minusBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 12px; -fx-cursor: hand; -fx-border-radius: 8px; -fx-border-color: transparent;");
        minusBtn.setOnAction(e -> updateQuantity(item, item.getQuantity() - 1));

        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; " +
                "-fx-background-color: #f8f9fa; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-border-color: #dee2e6; -fx-border-width: 1px;");

        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 12px; -fx-cursor: hand; -fx-border-radius: 8px; -fx-border-color: transparent;");
        plusBtn.setOnAction(e -> updateQuantity(item, item.getQuantity() + 1));

        qtyControls.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

        priceQtyBox.getChildren().addAll(priceBox, qtyControls);

        // Bouton supprimer
        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; " +
                "-fx-background-radius: 8px; -fx-padding: 10px 15px; -fx-cursor: hand; -fx-border-radius: 8px; -fx-border-color: transparent;");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> removeFromCart(item));

        // Assemblage de la card
        card.getChildren().addAll(header, priceQtyBox, deleteBtn);

        return card;
    }

    private void updateQuantity(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            removeFromCart(item);
        } else if (newQuantity <= item.getProduit().getStock()) {
            item.setQuantity(newQuantity);
            refreshCartCards();
        } else {
            showAlert(Alert.AlertType.WARNING, "Stock limité",
                    "Seulement " + item.getProduit().getStock() + " articles disponibles.");
        }
    }

    private void removeFromCart(CartItem item) {
        cart.remove(item);
        refreshCartCards();
    }


    @FXML
    public void clearCart() {
        cart.clear();
        suggestionsSource.clear();
        suggestionsCardsFlow = null;
        suggestionSortButtons.clear();
        if (suggestionsContainer != null) {
            suggestionsContainer.getChildren().clear();
        }
        if (noSuggestionsContainer != null) {
            noSuggestionsContainer.setVisible(true);
        }
        if (suggestionsScrollPane != null) {
            suggestionsScrollPane.setVisible(false);
        }
        refreshCartCards();
    }

    @FXML
    public void handleGetSuggestions() {
        if (cart.getItems().isEmpty()) {
            showError("Panier vide", "Ajoutez des produits au panier pour obtenir des suggestions.");
            return;
        }

        System.out.println("=== DÉBUT SUGGESTIONS PANIER ===");
        showSuggestionsLoading(true);
        if (suggestionsScrollPane != null) {
            suggestionsScrollPane.setVisible(true);
        }

        // Utiliser directement les suggestions par catégories
        // Utiliser directement les suggestions par catégories
        List<Produit> cartProducts = cart.getItems().stream()
                .map(CartItem::getProduit)
                .toList();

        System.out.println("Produits dans le panier: " + cartProducts.size());
        System.out.println("Produits dans le panier: " + cartProducts.size());
        for (int i = 0; i < cartProducts.size(); i++) {
            Produit p = cartProducts.get(i);
            System.out.println("  " + (i+1) + ". " + p.getNom() + " (" + p.getCategorie() + ")");
        }

        try {
            System.out.println("=== UTILISATION SUGGESTIONS PAR CATEGORIE ===");
            List<Produit> categorySuggestions = cartAIService.getProduitsSimilairesParCategorie(cartProducts);
            System.out.println("Suggestions trouvées: " + categorySuggestions.size());

            Platform.runLater(() -> {
                showSuggestionsLoading(false);
                displaySuggestions(categorySuggestions);
            });

        } catch (Exception e) {
            System.err.println("Erreur suggestions par catégorie: " + e.getMessage());
            // Fallback vers suggestions aléatoires
            try {
                List<Produit> randomSuggestions = cartAIService.getProduitsComplementaires();
                System.out.println("Fallback suggestions: " + randomSuggestions.size());
                Platform.runLater(() -> {
                    showSuggestionsLoading(false);
                    displaySuggestions(randomSuggestions);
                });
            } catch (Exception finalError) {
                System.err.println("Erreur fallback final: " + finalError.getMessage());
                Platform.runLater(() -> {
                    showSuggestionsLoading(false);
                    showError("Erreur de suggestions", "Impossible d'obtenir des suggestions: " + finalError.getMessage());
                });
            }
        }
    }

    @FXML
    public void validateOrder() {
        if (cart.getItems().isEmpty()) return;

        VBox form = new VBox(10);
        form.setPadding(new Insets(10));

        Label hint = new Label("Veuillez renseigner vos informations de livraison.");
        hint.setStyle("-fx-text-fill: #334155;");

        TextArea address = new TextArea();
        address.setPromptText("Adresse de livraison");
        address.setWrapText(true);
        address.setPrefRowCount(3);

        TextField phone = new TextField();
        phone.setPromptText("Téléphone (ex: 22123456)");

        Label total = new Label("Total à payer: " + formatMoney(cart.getGrandTotal()));
        total.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");

        form.getChildren().addAll(hint, new Label("Adresse:"), address, new Label("Téléphone:"), phone, total);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Valider la commande");
        confirm.setHeaderText("Confirmer la commande");
        confirm.getDialogPane().setContent(form);
        confirm.setResizable(true);

        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK) return;

        String adresseLivraison = address.getText() == null ? "" : address.getText().trim();
        String telephone = phone.getText() == null ? "" : phone.getText().trim();

        if (adresseLivraison.isEmpty()) {
            showError("Adresse obligatoire", "Veuillez saisir une adresse de livraison.");
            return;
        }

        try {
            int commandeId = new CommandeService().createOrderFromCart(
                    java.util.List.copyOf(cart.getItems()),
                    adresseLivraison,
                    telephone.isEmpty() ? null : telephone
            );

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Commande validée");
            ok.setHeaderText("Merci !");
            ok.setContentText("Votre commande a été enregistrée avec succès.\nID commande: " + commandeId);
            ok.showAndWait();

            // Ask if user wants to print invoice
            Alert printConfirm = new Alert(Alert.AlertType.CONFIRMATION);
            printConfirm.setTitle("Imprimer la facture");
            printConfirm.setHeaderText("Voulez-vous imprimer la facture maintenant ?");
            printConfirm.setContentText("Vous pouvez imprimer une facture pour votre commande.");

            var printResult = printConfirm.showAndWait();
            if (printResult.isPresent() && printResult.get() == javafx.scene.control.ButtonType.OK) {
                printInvoice();
            }

            cart.clear();
        } catch (IllegalArgumentException ex) {
            showError("Commande refusée", ex.getMessage());
        } catch (SQLException ex) {
            showError("Erreur base de données", ex.getMessage());
        } catch (Exception ex) {
            showError("Erreur", ex.getMessage());
        }
    }

    @FXML
    private void payWithStripe() {
        if (cart.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Panier vide", "Votre panier est vide !");
            return;
        }

        // Show customer information form first
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white;");

        Label title = new Label("Informations de livraison");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom complet");
        nameField.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-radius: 5px;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-radius: 5px;");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Téléphone");
        phoneField.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-radius: 5px;");

        TextArea addressField = new TextArea();
        addressField.setPromptText("Adresse de livraison");
        addressField.setWrapText(true);
        addressField.setPrefRowCount(3);
        addressField.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-radius: 5px;");

        Label totalLabel = new Label("Total à payer: " + formatMoney(cart.getGrandTotal()));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        form.getChildren().addAll(
                title,
                new Label("Nom complet:"), nameField,
                new Label("Email:"), emailField,
                new Label("Téléphone:"), phoneField,
                new Label("Adresse:"), addressField,
                totalLabel
        );

        Alert customerInfoDialog = new Alert(Alert.AlertType.CONFIRMATION);
        customerInfoDialog.setTitle("Finaliser la commande");
        customerInfoDialog.setHeaderText("Veuillez renseigner vos informations");
        customerInfoDialog.getDialogPane().setContent(form);
        customerInfoDialog.setResizable(true);

        var customerResult = customerInfoDialog.showAndWait();
        if (customerResult.isEmpty() || customerResult.get() != ButtonType.OK) {
            return;
        }

        // Validate customer information
        String customerName = nameField.getText().trim();
        String customerEmail = emailField.getText().trim();
        String customerPhone = phoneField.getText().trim();
        String customerAddress = addressField.getText().trim();

        if (customerName.isEmpty() || customerEmail.isEmpty() || customerAddress.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Informations incomplètes",
                    "Veuillez remplir tous les champs obligatoires (nom, email, adresse).");
            return;
        }

        // Validate email format
        if (!customerEmail.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showAlert(Alert.AlertType.ERROR, "Email invalide", "Veuillez entrer une adresse email valide.");
            return;
        }

        try {
            // Initialize Stripe service
            StripePaymentService stripeService = StripePaymentService.getInstance();

            if (!stripeService.isConfigured()) {
                showAlert(Alert.AlertType.ERROR, "Stripe non configuré",
                        "Le service de paiement Stripe n'est pas configuré.\n\n" +
                                "Veuillez vérifier votre clé API Stripe dans config.properties");
                return;
            }

            // Calculate total amount
            BigDecimal total = cart.getGrandTotal();

            // Create checkout session
            String successUrl = "http://localhost:8080/payment-success";
            String cancelUrl = "http://localhost:8080/payment-cancel";

            String checkoutUrl = stripeService.createCheckoutSession(total, successUrl, cancelUrl);

            // Show payment confirmation dialog
            Alert paymentConfirm = new Alert(Alert.AlertType.CONFIRMATION);
            paymentConfirm.setTitle("Paiement Stripe");
            paymentConfirm.setHeaderText("Confirmer le paiement");
            paymentConfirm.setContentText("Montant: " + formatMoney(total) + "\n\n" +
                    "Vous allez être redirigé vers la page de paiement sécurisée Stripe.\n" +
                    "Après le paiement, un email de confirmation avec facture PDF sera envoyé à: " + customerEmail + "\n\n" +
                    "Voulez-vous continuer ?");

            if (paymentConfirm.showAndWait().get() == ButtonType.OK) {
                // Store customer info for post-payment processing
                this.currentCustomerName = customerName;
                this.currentCustomerEmail = customerEmail;
                this.currentCustomerPhone = customerPhone;
                this.currentCustomerAddress = customerAddress;

                // Open browser for payment
                try {
                    java.awt.Desktop.getDesktop().browse(java.net.URI.create(checkoutUrl));

                    showAlert(Alert.AlertType.INFORMATION, "Redirection Stripe",
                            "Vous avez été redirigé vers la page de paiement Stripe.\n\n" +
                                    "Après le paiement, vous recevrez un email de confirmation avec votre facture PDF.\n" +
                                    "Si vous ne recevez pas l'email dans les 5 minutes, vérifiez votre dossier spam.");

                    // Start monitoring for payment completion (simulate for demo)
                    // In production, this would be handled by Stripe webhooks
                    monitorPaymentCompletion();

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                            "Impossible d'ouvrir le navigateur: " + e.getMessage() + "\n\n" +
                                    "URL de paiement: " + checkoutUrl);
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de paiement",
                    "Erreur lors de l'initialisation du paiement Stripe: " + e.getMessage());
        }
    }

    private void monitorPaymentCompletion() {
        // Simulate payment completion after 10 seconds (for demo purposes)
        // In production, this would be handled by Stripe webhooks
        javafx.application.Platform.runLater(() -> {
            // Show a loading dialog
            Alert loadingDialog = new Alert(Alert.AlertType.INFORMATION);
            loadingDialog.setTitle("Traitement du paiement");
            loadingDialog.setHeaderText("Veuillez patienter...");
            loadingDialog.setContentText("Nous traitons votre paiement et préparons votre facture.\n\n" +
                    "Ceci peut prendre quelques secondes...");
            loadingDialog.show();

            // Schedule payment completion check
            javafx.concurrent.Task<Void> paymentTask = new javafx.concurrent.Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // Simulate processing time
                    Thread.sleep(10000); // 10 seconds
                    return null;
                }

                @Override
                protected void succeeded() {
                    loadingDialog.close();
                    // Simulate successful payment and send email
                   // sendPaymentConfirmationEmail();
                }

                @Override
                protected void failed() {
                    loadingDialog.close();
                    showAlert(Alert.AlertType.ERROR, "Erreur de paiement",
                            "Une erreur est survenue lors du traitement du paiement.");
                }
            };

            new Thread(paymentTask).start();
        });
    }

    /*private void sendPaymentConfirmationEmail() {
        try {
            if (currentCustomerEmail == null || currentCustomerName == null) {
                showAlert(Alert.AlertType.ERROR, "Informations manquantes",
                        "Impossible d'envoyer l'email : informations client manquantes.");
                return;
            }

            // Lancer un diagnostic complet avant d'envoyer
            System.out.println("🔍 Lancement diagnostic complet email...");
            tn.formini.services.EmailDiagnosticService diagnostic = new tn.formini.services.EmailDiagnosticService();
            diagnostic.runFullDiagnostic(currentCustomerEmail);

            // Utiliser le service email direct pour contourner les problèmes de cache
            System.out.println("🔄 Utilisation du service email direct...");
            tn.formini.services.DirectEmailService directEmailService = new tn.formini.services.DirectEmailService();

            // Tester la clé API Brevo directement
            System.out.println("🔍 Test direct de la clé API Brevo...");
            boolean apiKeyValid = directEmailService.testBrevoKeyDirect();
            if (!apiKeyValid) {
                showAlert(Alert.AlertType.ERROR, "Clé Brevo invalide",
                        "La clé API Brevo n'est pas valide.\n\n" +
                                "Actions recommandées:\n" +
                                "1. Vérifiez que la clé dans config-local.properties est correcte\n" +
                                "2. Assurez-vous que la clé n'est pas expirée\n" +
                                "3. Obtenez une nouvelle clé depuis: https://app.brevo.com/settings/keys/api\n\n" +
                                "La clé sera lue directement depuis config-local.properties");
                return;
            }

            // Créer une facture PDF
            java.io.ByteArrayOutputStream pdfStream = new java.io.ByteArrayOutputStream();
            tn.formini.services.PDFInvoiceService pdfService = tn.formini.services.PDFInvoiceService.getInstance();

            // Créer la facture PDF pour le client
            pdfService.generateInvoicePDF(
                    cart.getItems(),
                    cart.getGrandTotal(),
                    currentCustomerName,
                    currentCustomerEmail,
                    currentCustomerPhone,
                    currentCustomerAddress,
                    pdfStream
            );

            // Convertir en base64 pour l'email
            String pdfBase64 = java.util.Base64.getEncoder().encodeToString(pdfStream.toByteArray());

            // Envoyer un email de test simple d'abord
            boolean testEmailSent = directEmailService.sendTestEmailDirect(
                    currentCustomerEmail,
                    currentCustomerName
            );

            if (testEmailSent) {
                showAlert(Alert.AlertType.INFORMATION, "✅ Email envoyé + Facture disponible",
                        "L'email a été envoyé à " + currentCustomerEmail + "\n\n" +
                                "📧 Si vous ne recevez pas l'email (problème Gmail courant):\n" +
                                "• Vérifiez Spam/Promotions/Social/Others\n" +
                                "• La facture est disponible ci-dessous\n\n" +
                                "📄 ALTERNATIVE: Téléchargez la facture directement");

                // Afficher la facture PDF directement dans l'application
                showInvoiceInApp(pdfStream.toByteArray());

                // Clear cart after successful payment
                cart.clear();

                // Clear customer info
                currentCustomerName = null;
                currentCustomerEmail = null;
                currentCustomerPhone = null;
                currentCustomerAddress = null;

            } else {
                showAlert(Alert.AlertType.ERROR, "❌ Erreur d'envoi d'email",
                        "L'envoi de l'email a échoué.\n\n" +
                                "Vérifiez la console pour les détails de l'erreur.\n" +
                                "La clé a été lue directement depuis config-local.properties.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'envoi de l'email",
                    "Une erreur est survenue: " + e.getMessage() + "\n\n" +
                            "Veuillez réessayer ou contacter le support technique.");
        }
    }
*/
    /**
     * Planifie la vérification du statut de livraison après 30 secondes
     */
    /*private void scheduleDeliveryStatusCheck() {
        if (lastEmailMessageId == null || lastEmailMessageId.isEmpty()) {
            System.out.println("⚠️ Aucun ID d'email à vérifier");
            return;
        }

        System.out.println("⏰ Planification vérification statut dans 30 secondes pour l'email: " + lastEmailMessageId);

        javafx.concurrent.Task<Void> statusCheckTask = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Attendre 30 secondes
                Thread.sleep(30000);
                return null;
            }

            @Override
            protected void succeeded() {
                // Vérifier le statut de livraison
                System.out.println("🔍 Vérification du statut de livraison...");
                tn.formini.services.DirectEmailService directService = new tn.formini.services.DirectEmailService();
                directService.checkEmailDeliveryStatus(lastEmailMessageId);

                // Afficher les résultats à l'utilisateur
                showAlert(Alert.AlertType.INFORMATION, "📊 Statut de livraison vérifié",
                        "La vérification du statut de livraison a été effectuée.\n\n" +
                                "📋 Consultez la console pour les détails complets:\n" +
                                "• delivered = Email reçu avec succès\n" +
                                "• bounced = Email rejeté (adresse invalide)\n" +
                                "• spam = Email marqué comme spam\n" +
                                "• blocked = Email bloqué par le filtre\n\n" +
                                "📧 Si 'delivered', vérifiez les dossiers Spam/Promotions dans Gmail");
            }

            @Override
            protected void failed() {
                System.err.println("❌ Erreur lors de la vérification du statut");
            }
        };

        new Thread(statusCheckTask).start();
    }*/

    /**
     * Affiche la facture PDF directement dans l'application
     */
    private void showInvoiceInApp(byte[] pdfBytes) {
        try {
            // Sauvegarder le PDF temporairement
            java.io.File tempFile = java.io.File.createTempFile("facture_", ".pdf");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
            fos.write(pdfBytes);
            fos.close();

            // Créer une boîte de dialogue pour afficher les options
            Alert invoiceDialog = new Alert(Alert.AlertType.INFORMATION);
            invoiceDialog.setTitle("📄 Facture disponible");
            invoiceDialog.setHeaderText("Votre facture est prête !");

            // Créer le contenu personnalisé
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));

            Label infoLabel = new Label("Votre facture PDF a été générée avec succès.");
            infoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50;");

            Label actionsLabel = new Label("Que souhaitez-vous faire ?");
            actionsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3498db;");

            Button openButton = new Button("📂 Ouvrir la facture");
            openButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px;");
            openButton.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().open(tempFile);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier: " + ex.getMessage());
                }
            });

            Button saveButton = new Button("💾 Sauvegarder la facture");
            saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px;");
            saveButton.setOnAction(e -> {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("Sauvegarder la facture");
                fileChooser.setInitialFileName("facture_formini_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");

                java.io.File selectedFile = fileChooser.showSaveDialog(null);
                if (selectedFile != null) {
                    try {
                        java.io.FileOutputStream saveFos = new java.io.FileOutputStream(selectedFile);
                        saveFos.write(pdfBytes);
                        saveFos.close();
                        showAlert(Alert.AlertType.INFORMATION, "✅ Sauvegardé", "Facture sauvegardée dans: " + selectedFile.getAbsolutePath());
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder: " + ex.getMessage());
                    }
                }
            });

            Button printButton = new Button("🖨️ Imprimer la facture");
            printButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px;");
            printButton.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().print(tempFile);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'imprimer: " + ex.getMessage());
                }
            });

            HBox buttonBox = new HBox(10, openButton, saveButton, printButton);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

            content.getChildren().addAll(infoLabel, actionsLabel, buttonBox);

            invoiceDialog.getDialogPane().setContent(content);
            invoiceDialog.setResizable(true);
            invoiceDialog.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'affichage de la facture: " + e.getMessage());
        }
    }

    private VBox detailsBox(Label... labels) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(0, 0, 0, 0));
        for (Label l : labels) {
            l.setStyle("-fx-text-fill: #334155;");
            box.getChildren().add(l);
        }
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        box.getChildren().add(spacer);
        return box;
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) return "0.000 DT";
        return String.format("%.3f DT", value.doubleValue());
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuggestionsLoading(boolean show) {
        if (suggestionsLoading != null) {
            suggestionsLoading.setVisible(show);
        }
    }

    @FXML
    public void printInvoice() {
        if (cart.getItems().isEmpty()) {
            showError("Panier vide", "Impossible d'imprimer une facture pour un panier vide.");
            return;
        }

        try {
            // Create invoice content for preview
            VBox previewInvoice = createInvoiceContent();

            // Show invoice preview dialog first
            Alert previewDialog = new Alert(Alert.AlertType.INFORMATION);
            previewDialog.setTitle("Aperçu Facture");
            previewDialog.setHeaderText("Aperçu de votre facture");
            previewDialog.getDialogPane().setContent(previewInvoice);
            previewDialog.setResizable(true);

            // Add custom buttons
            ButtonType printButtonType = new ButtonType("Imprimer", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
            previewDialog.getButtonTypes().setAll(printButtonType, cancelButtonType);

            // Show dialog and wait for response
            var result = previewDialog.showAndWait();

            if (result.isPresent() && result.get() == printButtonType) {
                // User wants to print - try to print
                attemptPrint();
            }

        } catch (Exception e) {
            System.out.println("Print error: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur d'impression", "Une erreur est survenue: " + e.getMessage());
        }
    }

    private void attemptPrint() {
        try {
            // Always create a new VBox for printing to avoid "already inside a parent" error
            VBox printContent = createInvoiceContent();

            // Create a new stage for printing
            javafx.stage.Stage printStage = new javafx.stage.Stage();
            printStage.setTitle("Impression Facture");
            javafx.scene.Scene scene = new javafx.scene.Scene(printContent, 600, 800);
            printStage.setScene(scene);

            // Try to print
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null) {
                System.out.println("Printer job created successfully");
                boolean success = job.showPrintDialog(printStage);
                System.out.println("Print dialog shown: " + success);

                if (success) {
                    boolean printed = job.printPage(printContent);
                    System.out.println("Print page result: " + printed);

                    if (printed) {
                        job.endJob();
                        showInfo("Impression réussie", "La facture a été imprimée avec succès.");
                    } else {
                        showError("Erreur d'impression", "L'impression a échoué.");
                    }
                }
            } else {
                System.out.println("No printer available");
                // Fallback: show save dialog message
                Alert fallbackAlert = new Alert(Alert.AlertType.INFORMATION);
                fallbackAlert.setTitle("Impression non disponible");
                fallbackAlert.setHeaderText("Aucune imprimante disponible");
                fallbackAlert.setContentText("Aucune imprimante n'est disponible sur ce système. Vous pouvez prendre une capture d'écran de cette facture comme preuve d'achat.");
                fallbackAlert.showAndWait();
            }

            printStage.close();

        } catch (Exception e) {
            System.out.println("Print attempt error: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur d'impression", "Erreur lors de l'impression: " + e.getMessage());
        }
    }

    private VBox createInvoiceContent() {
        VBox invoice = new VBox(15);
        invoice.setPadding(new Insets(30));
        invoice.setStyle("-fx-background-color: white; -fx-border: 1px solid #ccc; -fx-border-radius: 5;");

        // Header
        Label header = new Label("FACTURE");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label company = new Label("Formini.tn - L'EXCELLENCE EN FORMATION");
        company.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        // Date and reference
        String date = java.time.LocalDate.now().toString();
        String reference = "FACT-" + System.currentTimeMillis();
        Label dateLabel = new Label("Date: " + date);
        Label refLabel = new Label("Référence: " + reference);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        HBox headerInfo = new HBox(20, dateLabel, refLabel);
        headerInfo.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Customer info placeholder
        Label customerTitle = new Label("Informations client:");
        customerTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label customerInfo = new Label("[À compléter avec les informations du client]");
        customerInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        // Products table
        Label productsTitle = new Label("Détails des produits:");
        productsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        VBox productsList = new VBox(5);
        for (CartItem item : cart.getItems()) {
            HBox productLine = new HBox(10);
            Label productName = new Label(item.getProduit().getNom());
            productName.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
            productName.setPrefWidth(200);

            Label qty = new Label("x" + item.getQuantity());
            qty.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            qty.setPrefWidth(50);

            Label price = new Label(formatMoney(item.getProduit().getPrix()));
            price.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            price.setPrefWidth(80);

            Label lineTotal = new Label(formatMoney(item.getLineTotal()));
            lineTotal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
            lineTotal.setPrefWidth(80);

            productLine.getChildren().addAll(productName, qty, price, lineTotal);
            productsList.getChildren().add(productLine);
        }

        // Total
        HBox totalLine = new HBox();
        totalLine.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label totalLabel = new Label("TOTAL: " + formatMoney(cart.getGrandTotal()));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        totalLine.getChildren().add(totalLabel);

        // Footer
        Label footer = new Label("Merci de votre confiance!");
        footer.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");
        footer.setAlignment(javafx.geometry.Pos.CENTER);

        // Add all components
        invoice.getChildren().addAll(
                header, company, headerInfo,
                new javafx.scene.control.Separator(),
                customerTitle, customerInfo,
                new javafx.scene.control.Separator(),
                productsTitle, productsList,
                new javafx.scene.control.Separator(),
                totalLine,
                new javafx.scene.control.Separator(),
                footer
        );

        return invoice;
    }

    /**
     * Convertit les suggestions textuelles de l'IA en objets Produit
     */
    private List<Produit> convertSuggestionsToProducts(List<String> suggestions) {
        List<Produit> produits = new ArrayList<>();

        System.out.println("=== CONVERSION SUGGESTIONS IA ===");
        System.out.println("Suggestions reçues: " + suggestions.size());

        // Si aucune suggestion, créer des suggestions par défaut
        if (suggestions.isEmpty()) {
            System.out.println("⚠️ Aucune suggestion IA, utilisation du fallback");
            return getFallbackSuggestions();
        }

        for (int i = 0; i < suggestions.size(); i++) {
            String suggestion = suggestions.get(i);
            System.out.println("Suggestion " + (i+1) + ": " + suggestion);

            // Essayer de trouver un produit correspondant dans la base
            Produit produit = trouverProduitParNom(suggestion);
            if (produit != null) {
                produits.add(produit);
                System.out.println("✅ Produit trouvé: " + produit.getNom());
            } else {
                // Créer un produit virtuel basé sur la suggestion
                Produit virtuel = createProduitVirtuel(suggestion);
                produits.add(virtuel);
                System.out.println("🤖 Produit virtuel créé: " + virtuel.getNom());
            }
        }

        // Si aucun produit trouvé, utiliser le fallback
        if (produits.isEmpty()) {
            System.out.println("⚠️ Aucun produit trouvé, utilisation du fallback");
            return getFallbackSuggestions();
        }

        System.out.println("Total produits convertis: " + produits.size());
        return produits;
    }

    /**
     * Cherche un produit par nom dans la base de données
     */
    private Produit trouverProduitParNom(String nom) {
        try {
            // Utiliser une recherche floue pour trouver le produit
            List<Produit> tousProduits = cartAIService.getAllProducts();
            System.out.println("Recherche de: '" + nom + "' parmi " + tousProduits.size() + " produits");

            // Nettoyer la recherche
            String searchClean = nom.toLowerCase().trim();
            String[] searchWords = searchClean.split("\\s+");

            for (Produit produit : tousProduits) {
                String productClean = produit.getNom().toLowerCase().trim();

                // Recherche exacte
                if (productClean.contains(searchClean) || searchClean.contains(productClean)) {
                    System.out.println("✅ Correspondance exacte: " + produit.getNom());
                    return produit;
                }

                // Recherche par mots-clés
                int matchCount = 0;
                for (String word : searchWords) {
                    if (productClean.contains(word)) {
                        matchCount++;
                    }
                }

                // Si au moins 50% des mots correspondent
                if (matchCount > 0 && matchCount >= Math.ceil(searchWords.length * 0.5)) {
                    System.out.println("✅ Correspondance partielle (" + matchCount + "/" + searchWords.length + "): " + produit.getNom());
                    return produit;
                }
            }

            System.out.println("❌ Aucune correspondance trouvée pour: " + nom);
        } catch (Exception e) {
            System.err.println("Erreur recherche produit: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retourne des suggestions par défaut quand l'IA ne fonctionne pas
     */
    private List<Produit> getFallbackSuggestions() {
        List<Produit> fallback = new ArrayList<>();

        try {
            // Obtenir des produits de la même catégorie que ceux du panier
            List<CartItem> cartItems = cart.getItems();
            Set<String> cartCategories = cartItems.stream()
                    .map(item -> item.getProduit().getCategorie())
                    .filter(cat -> cat != null && !cat.isEmpty())
                    .collect(java.util.stream.Collectors.toSet());

            System.out.println("Catégories dans le panier: " + cartCategories);

            // Prendre quelques produits aléatoires de ces catégories
            List<Produit> allProducts = cartAIService.getAllProducts();
            int count = 0;

            for (Produit produit : allProducts) {
                if (count >= 5) break; // Maximum 5 suggestions

                // Vérifier si le produit est dans une catégorie du panier
                if (cartCategories.contains(produit.getCategorie())) {
                    // Vérifier que le produit n'est pas déjà dans le panier
                    boolean alreadyInCart = cartItems.stream()
                            .anyMatch(item -> item.getProduit().getId() == produit.getId());

                    if (!alreadyInCart && produit.getStock() > 0) {
                        fallback.add(produit);
                        System.out.println("📦 Ajout au fallback: " + produit.getNom() + " (" + produit.getCategorie() + ")");
                        count++;
                    }
                }
            }

            // Si toujours vide, ajouter quelques produits populaires
            if (fallback.isEmpty()) {
                System.out.println("⚠️ Fallback vide, ajout de produits populaires");
                for (Produit produit : allProducts) {
                    if (count >= 3) break;
                    if (produit.getStock() > 0) {
                        fallback.add(produit);
                        System.out.println("📦 Produit populaire ajouté: " + produit.getNom());
                        count++;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur dans getFallbackSuggestions: " + e.getMessage());
        }

        return fallback;
    }

    /**
     * Crée un produit virtuel basé sur la suggestion IA
     */
    private Produit createProduitVirtuel(String suggestion) {
        Produit virtuel = new Produit();
        virtuel.setId(-1); // ID négatif pour indiquer un produit virtuel
        virtuel.setNom(suggestion);
        virtuel.setDescription("🤖 Suggestion générée par IA basée sur votre panier");
        virtuel.setCategorie("Suggestion IA");
        virtuel.setPrix(new java.math.BigDecimal("0.00")); // Prix à déterminer
        virtuel.setStock(999); // Stock virtuel illimité
        virtuel.setImage("https://via.placeholder.com/200x150/4f46e5/ffffff?text=IA");
        virtuel.setStatut("Disponible");
        return virtuel;
    }

    /**
     * Affiche les suggestions avec un badge IA pour les produits virtuels
     */
    private void displayAdvancedSuggestions(List<Produit> suggestions) {
        suggestionsContainer.setVisible(true);
        suggestionsContainer.getChildren().clear();

        // Titre avec badge IA
        Label title = new Label("🤖 Suggestions IA Intelligentes");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4f46e5;");
        suggestionsContainer.getChildren().add(title);

        // Description
        Label desc = new Label("Basées sur l'analyse de votre panier avec GPT-4");
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-padding: 0 0 10px 0;");
        suggestionsContainer.getChildren().add(desc);

        // Conteneur pour les cartes
        FlowPane cardsFlow = new FlowPane(15, 15);
        cardsFlow.setPadding(new Insets(10));

        for (Produit produit : suggestions) {
            VBox card = createAdvancedSuggestionCard(produit);
            cardsFlow.getChildren().add(card);
        }

        ScrollPane scrollPane = new ScrollPane(cardsFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        suggestionsContainer.getChildren().add(scrollPane);
    }

    /**
     * Crée une carte de suggestion avancée
     */
    private VBox createAdvancedSuggestionCard(Produit produit) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);");

        // Header avec badge IA si virtuel
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (produit.getId() == -1) {
            // Badge IA pour produit virtuel
            Label badgeIA = new Label("🤖 IA");
            badgeIA.setStyle("-fx-background-color: linear-gradient(45deg, #4f46e5, #7c3aed); " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 20; " +
                    "-fx-font-size: 10px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 2 8px;");
            header.getChildren().add(badgeIA);
        }

        Label nameLabel = new Label(produit.getNom());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);
        header.getChildren().add(nameLabel);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        try {
            if (produit.getImage() != null && !produit.getImage().trim().isEmpty()) {
                imageView.setImage(new Image(produit.getImage(), true));
            }
        } catch (Exception e) {
            // Image par défaut
        }

        // Description
        Label descLabel = new Label(produit.getDescription());
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(180);

        // Prix et badge spécial
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label priceLabel = new Label(formatMoney(produit.getPrix()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #059669;");

        if (produit.getId() == -1) {
            Label virtuelLabel = new Label("Virtuel");
            virtuelLabel.setStyle("-fx-background-color: #f3f4f6; " +
                    "-fx-text-fill: #6b7280; " +
                    "-fx-background-radius: 4; " +
                    "-fx-font-size: 9px; " +
                    "-fx-padding: 2 6px;");
            priceBox.getChildren().addAll(priceLabel, virtuelLabel);
        } else {
            priceBox.getChildren().add(priceLabel);
        }

        // Boutons d'action
        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.CENTER);

        Button detailsBtn = new Button("👁 Détails");
        detailsBtn.setStyle("-fx-background-color: #f3f4f6; " +
                "-fx-text-fill: #374151; " +
                "-fx-background-radius: 6; " +
                "-fx-font-size: 11px; " +
                "-fx-cursor: hand;");
        detailsBtn.setOnAction(e -> showProductDetails(produit));

        if (produit.getId() == -1) {
            // Pour les produits virtuels, bouton de recherche
            Button searchBtn = new Button("🔍 Rechercher");
            searchBtn.setStyle("-fx-background-color: #3b82f6; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 6; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand;");
            searchBtn.setOnAction(e -> searchForProduct(produit.getNom()));
            actions.getChildren().addAll(detailsBtn, searchBtn);
        } else {
            // Pour les produits réels, bouton d'ajout
            Button addBtn = new Button("🛒 Ajouter");
            addBtn.setStyle("-fx-background-color: #22c55e; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 6; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand;");
            addBtn.setDisable(produit.getStock() <= 0);
            addBtn.setOnAction(e -> {
                cart.add(produit, 1);
                refreshCart();
                showSuccess("Produit ajouté", produit.getNom() + " a été ajouté au panier");
            });
            actions.getChildren().addAll(detailsBtn, addBtn);
        }

        card.getChildren().addAll(header, imageView, descLabel, priceBox, actions);
        return card;
    }

    /**
     * Recherche un produit dans la boutique
     */
    private void searchForProduct(String productName) {
        try {
            // Naviguer vers la boutique avec le terme de recherche
            FrontMainController.getInstance().loadView("/fxml/frontend/Shop.fxml");
            // TODO: Passer le terme de recherche au ShopController
        } catch (Exception e) {
            showError("Erreur navigation", "Impossible d'ouvrir la boutique: " + e.getMessage());
        }
    }



    /**
     * Affiche un message de succès
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Rafraîchit l'affichage du panier
     */
    private void refreshCart() {
        // TableView supprimée - pas besoin de refresh
        // Le labelGrandTotal est déjà bindé automatiquement
        System.out.println("Cart refreshed - items: " + cart.getItemsCountTotal());
    }

    private void displaySuggestions(List<Produit> produits) {
        if (suggestionsContainer == null) {
            return;
        }

        if (suggestionsScrollPane != null) {
            suggestionsScrollPane.setVisible(true);
        }
        if (noSuggestionsContainer != null) {
            noSuggestionsContainer.setVisible(false);
        }

        suggestionsSource.clear();
        suggestionSortButtons.clear();
        suggestionsContainer.getChildren().clear();

        if (produits == null || produits.isEmpty()) {
            Label emptyLabel = new Label(
                    "Aucune suggestion pour le moment. Vérifiez que le catalogue contient d'autres produits "
                            + "dans les mêmes catégories que votre panier (hors articles déjà ajoutés).");
            emptyLabel.getStyleClass().add("cart-suggestion-meta");
            emptyLabel.setWrapText(true);
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            suggestionsContainer.getChildren().add(emptyLabel);
            return;
        }

        // Limiter à 4 suggestions maximum
        List<Produit> limitedSuggestions = produits.stream()
                .limit(4)
                .collect(Collectors.toList());

        suggestionSort = SuggestionSort.ORDER;
        suggestionsSource.addAll(limitedSuggestions);

        VBox toolbar = buildSuggestionToolbar(produits.size());
        suggestionsCardsFlow = new FlowPane();
        suggestionsCardsFlow.getStyleClass().add("cart-suggestions-flow");
        suggestionsCardsFlow.setPrefWrapLength(640);
        if (suggestionsScrollPane != null) {
            suggestionsCardsFlow.prefWrapLengthProperty().bind(
                    suggestionsScrollPane.widthProperty().subtract(56));
        }

        fillSuggestionCards();

        Button hideBtn = new Button("Masquer les suggestions");
        hideBtn.setStyle("-fx-background-color: #f3e5f5; -fx-border-color: #9c27b0; -fx-border-width: 2px; " +
                "-fx-text-fill: #7b1fa2; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 10px 16px; -fx-cursor: hand;");
        hideBtn.setMaxWidth(Double.MAX_VALUE);
        hideBtn.setOnAction(e -> hideSuggestionsPanel());

        suggestionsContainer.getChildren().addAll(toolbar, suggestionsCardsFlow, hideBtn);
    }

    private VBox buildSuggestionToolbar(int count) {
        VBox wrap = new VBox(8);
        wrap.getStyleClass().add("cart-suggestions-toolbar-wrap");

        List<String> categoriesPanier = cart.getItems().stream()
                .map(i -> i.getProduit() != null ? i.getProduit().getCategorie() : null)
                .filter(c -> c != null && !c.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .toList();
        if (!categoriesPanier.isEmpty()) {
            Label filterHint = new Label("🎯 Uniquement les catégories de votre panier : " + String.join(" · ", categoriesPanier));
            filterHint.setStyle("-fx-background-color: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%); " +
                    "-fx-text-fill: #2d3436; -fx-font-weight: bold; -fx-font-size: 13px; " +
                    "-fx-background-radius: 10px; -fx-padding: 10px 14px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(253,203,110,0.3), 2, 0, 0, 1);");
            filterHint.setWrapText(true);
            filterHint.setMaxWidth(Double.MAX_VALUE);
            wrap.getChildren().add(filterHint);
        }

        HBox bar = new HBox(8);
        bar.getStyleClass().add("cart-suggestions-toolbar");
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String countText = count == 1 ? "1 produit proposé" : count + " produits proposés";
        Label countLabel = new Label(countText);
        countLabel.getStyleClass().add("cart-suggestions-count");
        countLabel.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        suggestionSortButtons.clear();
        Button bOrder = makeSortButton("Ordre", SuggestionSort.ORDER);
        Button bName = makeSortButton("A → Z", SuggestionSort.NAME);
        Button bPrice = makeSortButton("Prix", SuggestionSort.PRICE);
        Button bStock = makeSortButton("Stock", SuggestionSort.STOCK);

        suggestionSortButtons.put(SuggestionSort.ORDER, bOrder);
        suggestionSortButtons.put(SuggestionSort.NAME, bName);
        suggestionSortButtons.put(SuggestionSort.PRICE, bPrice);
        suggestionSortButtons.put(SuggestionSort.STOCK, bStock);

        updateSortButtonStyles();
        bar.getChildren().addAll(countLabel, spacer, bOrder, bName, bPrice, bStock);
        wrap.getChildren().add(bar);
        return wrap;
    }

    private Button makeSortButton(String text, SuggestionSort mode) {
        Button b = new Button(text);

        // Couleurs différentes selon le mode
        String colorStyle = switch (mode) {
            case ORDER -> "-fx-background-color: #e8f5e8; -fx-border-color: #4caf50; -fx-border-width: 2px; " +
                    "-fx-text-fill: #2e7d32;";
            case NAME -> "-fx-background-color: #fff3e0; -fx-border-color: #ff9800; -fx-border-width: 2px; " +
                    "-fx-text-fill: #e65100;";
            case PRICE -> "-fx-background-color: #e3f2fd; -fx-border-color: #03a9f4; -fx-border-width: 2px; " +
                    "-fx-text-fill: #0277bd;";
            case STOCK -> "-fx-background-color: #fce4ec; -fx-border-color: #e91e63; -fx-border-width: 2px; " +
                    "-fx-text-fill: #c2185b;";
        };

        b.setStyle(colorStyle +
                "-fx-font-weight: bold; -fx-font-size: 12px; " +
                "-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 6px 12px; -fx-cursor: hand;");
        b.getStyleClass().addAll("cart-sort-btn", "cart-sort-" + mode.name().toLowerCase());
        b.setMnemonicParsing(false);
        b.setOnAction(e -> {
            suggestionSort = mode;
            updateSortButtonStyles();
            fillSuggestionCards();
        });
        return b;
    }

    private void updateSortButtonStyles() {
        suggestionSortButtons.forEach((sort, btn) -> {
            btn.getStyleClass().removeAll("cart-sort-btn-active");
            if (sort == suggestionSort) {
                btn.getStyleClass().add("cart-sort-btn-active");
            }
        });
    }

    private void fillSuggestionCards() {
        if (suggestionsCardsFlow == null) {
            return;
        }
        suggestionsCardsFlow.getChildren().clear();
        List<Produit> ordered = new ArrayList<>(suggestionsSource);
        if (suggestionSort != SuggestionSort.ORDER) {
            ordered.sort(comparatorFor(suggestionSort));
        }
        int rank = 1;
        for (Produit p : ordered) {
            suggestionsCardsFlow.getChildren().add(createSuggestionCard(p, rank++));
        }
    }

    private Comparator<Produit> comparatorFor(SuggestionSort sort) {
        return switch (sort) {
            case ORDER -> Comparator.comparingInt(suggestionsSource::indexOf);
            case NAME -> Comparator.comparing(
                    (Produit p) -> nullToEmpty(p.getNom()), String.CASE_INSENSITIVE_ORDER);
            case PRICE -> Comparator.comparing(Produit::getPrix, Comparator.nullsLast(BigDecimal::compareTo));
            case STOCK -> Comparator.comparingInt(Produit::getStock).reversed();
        };
    }

    private VBox createSuggestionCard(Produit produit, int rank) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1px; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px; " +
                "-fx-padding: 16px;");
        card.setFillWidth(true);

        // Header simple
        HBox topRow = new HBox(12);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label rankBadge = new Label("#" + rank);
        rankBadge.setStyle("-fx-background-color: #667eea; " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; " +
                "-fx-background-radius: 10px; -fx-padding: 4px 8px;");
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);

        // Catégorie simple
        String cat = produit.getCategorie() == null || produit.getCategorie().isBlank()
                ? "Non catégorisé"
                : produit.getCategorie();
        Label categoryBadge = new Label(cat);
        categoryBadge.setStyle("-fx-background-color: #6c757d; " +
                "-fx-text-fill: white; -fx-font-size: 11px; " +
                "-fx-background-radius: 6px; -fx-padding: 3px 6px;");

        topRow.getChildren().addAll(rankBadge, grow, categoryBadge);

        // Image simple
        HBox thumbRow = new HBox();
        thumbRow.setAlignment(javafx.geometry.Pos.CENTER);
        thumbRow.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-width: 1px; " +
                "-fx-border-radius: 6px; -fx-padding: 10px;");
        ImageView img = new ImageView();
        img.setFitWidth(120);
        img.setFitHeight(120);
        img.setPreserveRatio(true);
        img.setSmooth(true);
        if (produit.getImage() != null && !produit.getImage().trim().isEmpty()) {
            try {
                img.setImage(new Image(produit.getImage().trim(), true));
            } catch (Exception ignored) {
            }
        }
        thumbRow.getChildren().add(img);

        // Nom du produit
        Label name = new Label(nullToEmpty(produit.getNom()));
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; " +
                "-fx-wrap-text: true; -fx-max-width: 260px;");

        // Prix simple
        Label price = new Label(formatMoney(produit.getPrix()));
        price.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        // Stock simple
        String stockTxt = produit.getStock() <= 0
                ? "Rupture de stock"
                : produit.getStock() + " disponibles";
        Label stockLabel = new Label(stockTxt);
        stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " +
                (produit.getStock() > 0 ? "#28a745" : "#dc3545") + ";");

        // Description si disponible
        String desc = nullToEmpty(produit.getDescription()).trim();
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Boutons d'action
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(javafx.geometry.Pos.CENTER);

        // Bouton pour voir les détails
        Button detailsBtn = new Button("�️");
        detailsBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 12px; -fx-cursor: hand; -fx-border-radius: 8px; -fx-border-color: transparent;");
        detailsBtn.setOnAction(e -> {
            showProductDetails(produit);
        });

        // Bouton d'ajout coloré
        Button add = new Button("🛒 Ajouter");
        add.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 8px; -fx-padding: 12px 16px; -fx-cursor: hand; -fx-border-radius: 8px; -fx-border-color: transparent;");
        HBox.setHgrow(add, Priority.ALWAYS);
        add.setMaxWidth(Double.MAX_VALUE);
        add.setDisable(produit.getStock() <= 0);

        add.setOnAction(e -> {
            cart.add(produit);
            showInfo("Ajouté", nullToEmpty(produit.getNom()) + " a été ajouté au panier.");
        });

        actionButtons.getChildren().addAll(detailsBtn, add);

        // Assemblage de la carte
        card.getChildren().addAll(topRow, thumbRow, name, price, stockLabel);
        if (!desc.isEmpty()) {
            Label descLabel = new Label(desc);
            descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-wrap-text: true; " +
                    "-fx-max-width: 260px; -fx-font-style: italic;");
            card.getChildren().add(descLabel);
        }
        card.getChildren().addAll(spacer, actionButtons);
        return card;
    }

    private void showProductDetails(Produit produit) {
        // Créer une boîte de dialogue pour afficher les détails
        javafx.stage.Stage detailStage = new javafx.stage.Stage();
        detailStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        detailStage.setTitle("Détails du produit");

        VBox detailLayout = new VBox(15);
        detailLayout.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-background-radius: 10px;");
        detailLayout.setPrefSize(400, 500);

        // Image du produit
        ImageView productImage = new ImageView();
        productImage.setFitWidth(200);
        productImage.setFitHeight(200);
        productImage.setPreserveRatio(true);
        productImage.setSmooth(true);

        if (produit.getImage() != null && !produit.getImage().trim().isEmpty()) {
            try {
                productImage.setImage(new Image(produit.getImage().trim(), true));
            } catch (Exception e) {
                // Image par défaut si erreur
                productImage.setImage(new Image("https://via.placeholder.com/200x200/f0f0f0/666666?text=No+Image", true));
            }
        } else {
            productImage.setImage(new Image("https://via.placeholder.com/200x200/f0f0f0/666666?text=No+Image", true));
        }

        // Informations du produit
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15px; -fx-background-radius: 8px;");

        Label nameLabel = new Label("📦 " + nullToEmpty(produit.getNom()));
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label categoryLabel = new Label("🏷️ " + nullToEmpty(produit.getCategorie()));
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #667eea; -fx-font-weight: 500;");

        Label priceLabel = new Label("💰 " + formatMoney(produit.getPrix()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label stockLabel = new Label("📊 Stock: " + produit.getStock() + " unités");
        stockLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " +
                (produit.getStock() > 0 ? "#27ae60" : "#e74c3c") + ";");

        // Description
        String desc = nullToEmpty(produit.getDescription()).trim();
        Label descLabel = new Label("📝 " + (desc.isEmpty() ? "Aucune description disponible" : desc));
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d; -fx-wrap-text: true;");
        descLabel.setMaxWidth(350);

        infoBox.getChildren().addAll(nameLabel, categoryLabel, priceLabel, stockLabel, descLabel);

        // Bouton fermer
        Button closeButton = new Button("✅ Fermer");
        closeButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 10px 20px; -fx-cursor: hand;");
        closeButton.setOnAction(e -> detailStage.close());

        // Assemblage
        HBox imageBox = new HBox();
        imageBox.setAlignment(javafx.geometry.Pos.CENTER);
        imageBox.getChildren().add(productImage);

        detailLayout.getChildren().addAll(imageBox, infoBox, closeButton);

        // Scene et affichage
        javafx.scene.Scene scene = new javafx.scene.Scene(detailLayout);
        detailStage.setScene(scene);
        detailStage.showAndWait();
    }

    private void hideSuggestionsPanel() {
        if (suggestionsScrollPane != null) {
            suggestionsScrollPane.setVisible(false);
        }
        if (noSuggestionsContainer != null) {
            noSuggestionsContainer.setVisible(true);
        }
    }
}
