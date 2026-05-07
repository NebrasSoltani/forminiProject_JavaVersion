package tn.formini.controllers.frontend;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.formini.entities.produits.Produit;
import tn.formini.services.RealProductAIService;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AISearchFormController implements Initializable {

    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea preferencesArea;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ScrollPane resultsScrollPane;
    @FXML private VBox resultsContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;

    private RealProductAIService aiService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le service IA
        aiService = RealProductAIService.getInstance();

        // Configurer les catégories de produits
        categoryCombo.getItems().addAll(
            "Toutes les catégories",
            "Scientifique",
            "Informatique",
            "Outils intelligents",
            "Accessoires"
        );

        // Sélectionner "Toutes les catégories" par défaut
        categoryCombo.getSelectionModel().selectFirst();

        // Message de bienvenue simple
        statusLabel.setText("Prêt à rechercher...");
    }

    @FXML
    public void performAISearch() {
        System.out.println("🔍 Début de la recherche IA...");

        final String category = categoryCombo.getValue();
        final String preferences = preferencesArea.getText().trim();

        System.out.println("📝 Catégorie: " + category);
        System.out.println("📝 Préférences: '" + preferences + "'");

        // Récupérer les prix
        final Double minPrice;
        final Double maxPrice;

        try {
            String minPriceText = minPriceField.getText().trim();
            if (!minPriceText.isEmpty()) {
                minPrice = Double.parseDouble(minPriceText);
                System.out.println("💰 Prix min: " + minPrice);
            } else {
                minPrice = null;
                System.out.println("💰 Prix min: non spécifié");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Erreur prix min: " + e.getMessage());
            statusLabel.setText("⚠️ Prix minimum invalide");
            return;
        }

        try {
            String maxPriceText = maxPriceField.getText().trim();
            if (!maxPriceText.isEmpty()) {
                maxPrice = Double.parseDouble(maxPriceText);
                System.out.println("💰 Prix max: " + maxPrice);
            } else {
                maxPrice = null;
                System.out.println("💰 Prix max: non spécifié");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Erreur prix max: " + e.getMessage());
            statusLabel.setText("⚠️ Prix maximum invalide");
            return;
        }

        if (preferences.isEmpty()) {
            System.out.println("❌ Préférences vides");
            statusLabel.setText("⚠️ Veuillez décrire vos préférences");
            return;
        }

        // Afficher le chargement
        loadingIndicator.setVisible(true);
        statusLabel.setText("🔍 Recherche des produits dans la boutique...");
        resultsContainer.getChildren().clear();
        System.out.println("🔄 Chargement affiché, conteneur vidé");

        // D'ABORD TESTER AVEC DES PRODUITS FIXES POUR DÉBOGUER
        if (preferences.toLowerCase().contains("test")) {
            System.out.println("🧪 Mode test activé - utilisation de produits fixes");
            showTestProducts();
            return;
        }

        // Lancer la recherche dans un thread séparé
        CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🚀 Lancement recherche asynchrone...");
                List<Produit> products = aiService.searchProducts(preferences, category, minPrice, maxPrice);
                System.out.println("🎯 " + products.size() + " produits trouvés");
                return products;
            } catch (Exception e) {
                System.err.println("❌ Erreur recherche: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).thenAccept(products -> {
            javafx.application.Platform.runLater(() -> {
                System.out.println("📱 Mise à jour UI avec " + products.size() + " produits");
                loadingIndicator.setVisible(false);

                if (products.isEmpty()) {
                    System.out.println("😔 Aucun produit trouvé");
                    statusLabel.setText("😔 Aucun produit trouvé correspondant à vos critères");
                    showEmptyResults();
                } else {
                    System.out.println("✅ Affichage de " + products.size() + " produits");
                    statusLabel.setText("✅ " + products.size() + " produits trouvés dans la boutique");
                    displayRealProducts(products);
                }
            });
        }).exceptionally(throwable -> {
            javafx.application.Platform.runLater(() -> {
                System.err.println("💥 Exception dans la recherche: " + throwable.getMessage());
                throwable.printStackTrace();
                loadingIndicator.setVisible(false);
                statusLabel.setText("❌ Erreur lors de la recherche: " + throwable.getMessage());
                showErrorResults();
            });
            return null;
        });
    }

    @FXML
    public void clearSearch() {
        preferencesArea.clear();
        minPriceField.clear();
        maxPriceField.clear();
        categoryCombo.getSelectionModel().selectFirst();
        resultsContainer.getChildren().clear();
        statusLabel.setText("Prêt à rechercher...");
        loadingIndicator.setVisible(false);
    }

    /**
     * Affiche des produits de test pour déboguer
     */
    private void showTestProducts() {
        System.out.println("🧪 Création de produits de test...");

        loadingIndicator.setVisible(false);
        statusLabel.setText("🧪 Test d'affichage avec produits fixes");

        // Créer des produits de test
        List<Produit> testProducts = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Produit product = new Produit();
            product.setId(i);
            product.setNom("Produit Test " + i + " - Calculatrice Scientifique");
            product.setCategorie("Scientifique");
            product.setDescription("Ceci est un produit de test numéro " + i + " pour vérifier l'affichage.");
            product.setPrix(new java.math.BigDecimal("99.99"));
            product.setStock(10 + i);
            product.setImage("https://dummyimage.com/h/260x140/ff6b6b/ffffff&text=TEST" + i);
            product.setStatut("Disponible");
            product.setDate_creation(new java.util.Date());

            testProducts.add(product);
            System.out.println("✅ Produit test " + i + " créé: " + product.getNom());
        }

        System.out.println("🎯 Affichage de " + testProducts.size() + " produits de test");
        displayRealProducts(testProducts);
        System.out.println("✅ Test terminé");
    }

    @FXML
    public void showRandomSuggestions() {
        System.out.println("🎲 Test avec suggestions aléatoires");

        loadingIndicator.setVisible(true);
        statusLabel.setText("🎲 Génération de suggestions aléatoires...");
        resultsContainer.getChildren().clear();

        // Créer un produit de test pour vérifier l'affichage
        try {
            Produit testProduct = new Produit();
            testProduct.setId(1);
            testProduct.setNom("Calculatrice Scientifique Test");
            testProduct.setCategorie("Scientifique");
            testProduct.setDescription("Ceci est un produit de test pour vérifier l'affichage");
            testProduct.setPrix(new java.math.BigDecimal("99.99"));
            testProduct.setStock(10);
            testProduct.setImage("https://dummyimage.com/h/280x180/ff6b6b/ffffff&text=TEST");

            System.out.println("🧪 Création produit de test: " + testProduct.getNom());

            // Simuler un délai
            CompletableFuture.delayedExecutor(500, java.util.concurrent.TimeUnit.MILLISECONDS)
                .execute(() -> {
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        statusLabel.setText("🎲 Test d'affichage");

                        // Afficher le produit de test
                        List<Produit> testProducts = java.util.Arrays.asList(testProduct);
                        displayRealProducts(testProducts);

                        System.out.println("✅ Test d'affichage terminé");
                    });
                });

        } catch (Exception e) {
            System.err.println("❌ Erreur test: " + e.getMessage());
            e.printStackTrace();
            loadingIndicator.setVisible(false);
            statusLabel.setText("❌ Erreur lors du test");
        }
    }

    @FXML
    public void goBack() {
        try {
            // Fermer la fenêtre actuelle
            preferencesArea.getScene().getWindow().hide();
            statusLabel.setText("🏠 Retour à l'accueil");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du retour: " + e.getMessage());
            statusLabel.setText("❌ Erreur lors du retour: " + e.getMessage());
        }
    }

    private void displayRealProducts(List<Produit> products) {
        System.out.println("📦 Début affichage de " + products.size() + " produits");

        resultsContainer.getChildren().clear();
        System.out.println("🧹 Conteneur vidé");

        for (int i = 0; i < products.size(); i++) {
            Produit product = products.get(i);
            System.out.println("🏷️ Création carte pour produit: " + product.getNom() + " (ID: " + product.getId() + ")");

            // Utiliser les cartes de vrais produits avec toutes les informations
            VBox productCard = createRealProductCard(product, i + 1);
            resultsContainer.getChildren().add(productCard);
            System.out.println("✅ Carte ajoutée au conteneur (" + (i + 1) + "/" + products.size() + ")");
        }

        System.out.println("🎉 Affichage terminé: " + resultsContainer.getChildren().size() + " cartes dans le conteneur");
    }

    private void displaySuggestions(List<String> suggestions) {
        resultsContainer.getChildren().clear();

        for (int i = 0; i < suggestions.size(); i++) {
            String suggestion = suggestions.get(i);

            // Utiliser les cartes de produits avec images
            VBox productCard = createProductCard(suggestion, i + 1);
            resultsContainer.getChildren().add(productCard);
        }
    }

    /**
     * Crée une carte pour un vrai produit avec design agrandi
     */
    private VBox createRealProductCard(Produit product, int number) {
        VBox card = new VBox(12);
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px; -fx-border-color: #000000; -fx-border-width: 3px; -fx-border-radius: 10px; -fx-padding: 18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 5);");

        // Numéro du produit
        Label numberLabel = new Label("#" + number);
        numberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FF0000; -fx-background-color: #FFF0F0; -fx-background-radius: 12px; -fx-padding: 5 12;");

        // Image agrandie
        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 6px; -fx-border-color: #000000; -fx-border-width: 2px;");

        // Charger l'image réelle du produit
        try {
            String imageUrl = product.getImage();
            System.out.println("🖼️ Image URL: " + imageUrl);

            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                if (imageUrl.startsWith("http")) {
                    imageView.setImage(new Image(imageUrl, true));
                } else if (imageUrl.startsWith("file:")) {
                    imageView.setImage(new Image(imageUrl, true));
                } else {
                    try {
                        Image resourceImage = new Image(getClass().getResourceAsStream("/" + imageUrl));
                        if (!resourceImage.isError()) {
                            imageView.setImage(resourceImage);
                        } else {
                            imageView.setImage(getColorfulProductImage(product.getNom(), product.getCategorie()));
                        }
                    } catch (Exception e) {
                        imageView.setImage(getColorfulProductImage(product.getNom(), product.getCategorie()));
                    }
                }
            } else {
                imageView.setImage(getColorfulProductImage(product.getNom(), product.getCategorie()));
            }
        } catch (Exception e) {
            imageView.setImage(getColorfulProductImage(product.getNom(), product.getCategorie()));
        }

        // Nom du produit
        Label nameLabel = new Label(product.getNom());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        nameLabel.setWrapText(true);

        // Prix
        Label priceLabel = new Label(String.format("%.2f DT", product.getPrix()));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FF0000;");

        // Stock
        Label stockLabel = new Label();
        if (product.getStock() > 0) {
            stockLabel.setText("✅ " + product.getStock() + " unités");
            stockLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #008000;");
        } else {
            stockLabel.setText("❌ Rupture");
            stockLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF0000;");
        }

        // Description courte
        String description = product.getDescription();
        if (description != null && description.length() > 100) {
            description = description.substring(0, 100) + "...";
        }
        Label descLabel = new Label(description != null ? description : "");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        descLabel.setWrapText(true);

        // Boutons agrandis
        HBox actionsBox = new HBox(12);
        actionsBox.setStyle("-fx-alignment: center;");

        Button detailsBtn = new Button("Détails");
        detailsBtn.setStyle("-fx-background-color: #FF0000; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 6px; -fx-padding: 8 16; -fx-cursor: hand;");
        detailsBtn.setOnAction(event -> showProductDetails(product));

        Button addToCartBtn = new Button("Ajouter");
        addToCartBtn.setStyle("-fx-background-color: #FF0000; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 6px; -fx-padding: 8 16; -fx-cursor: hand;");
        addToCartBtn.setOnAction(event -> addToCart(product));
        addToCartBtn.setDisable(product.getStock() <= 0);

        actionsBox.getChildren().addAll(detailsBtn, addToCartBtn);

        // Assembler la carte
        card.getChildren().addAll(numberLabel, imageView, nameLabel, priceLabel, stockLabel, descLabel, actionsBox);

        return card;
    }

    /**
     * Crée une carte de suggestion agrandie
     */
    private VBox createProductCard(String productName, int number) {
        VBox card = new VBox(12);
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px; -fx-border-color: #000000; -fx-border-width: 3px; -fx-border-radius: 10px; -fx-padding: 18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 5);");

        // Numéro du produit
        Label numberLabel = new Label("#" + number);
        numberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FF0000; -fx-background-color: #FFF0F0; -fx-background-radius: 12px; -fx-padding: 5 12;");

        // Image agrandie
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 6px; -fx-border-color: #000000; -fx-border-width: 2px;");

        // Image par défaut selon le type de produit
        Image productImage = getDefaultProductImage(productName);
        imageView.setImage(productImage);

        // Nom du produit
        Label nameLabel = new Label(productName);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        nameLabel.setWrapText(true);

        // Description
        Label descLabel = new Label("🤖 Suggestion IA");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-font-style: italic;");
        descLabel.setWrapText(true);

        // Boutons agrandis
        HBox actionsBox = new HBox(12);
        actionsBox.setStyle("-fx-alignment: center;");

        Button detailsBtn = new Button("Détails");
        detailsBtn.setStyle("-fx-background-color: #FF0000; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 6px; -fx-padding: 8 16; -fx-cursor: hand;");
        detailsBtn.setOnAction(event -> showDetails(productName));

        Button searchBtn = new Button("Chercher");
        searchBtn.setStyle("-fx-background-color: #FF0000; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 6px; -fx-padding: 8 16; -fx-cursor: hand;");
        searchBtn.setOnAction(event -> searchInShop(productName));

        actionsBox.getChildren().addAll(detailsBtn, searchBtn);

        // Assembler la carte
        card.getChildren().addAll(numberLabel, imageView, nameLabel, descLabel, actionsBox);

        return card;
    }

    /**
     * Obtient une image par défaut selon le type de produit
     */
    private Image getDefaultProductImage(String productName) {
        String name = productName.toLowerCase();

        if (name.contains("laptop") || name.contains("portable")) {
            return new Image("https://dummyimage.com/h/320x200/ff6b6b/ffffff&text=LAPTOP", true);
        } else if (name.contains("iphone") || name.contains("smartphone") || name.contains("téléphone")) {
            return new Image("https://dummyimage.com/h/320x200/4ecdc4/ffffff&text=SMARTPHONE", true);
        } else if (name.contains("calculatrice")) {
            return new Image("https://dummyimage.com/h/320x200/45b7d1/ffffff&text=CALCULATOR", true);
        } else if (name.contains("clavier")) {
            return new Image("https://dummyimage.com/h/320x200/96ceb4/ffffff&text=KEYBOARD", true);
        } else if (name.contains("souris")) {
            return new Image("https://dummyimage.com/h/320x200/ffeaa7/000000&text=MOUSE", true);
        } else if (name.contains("écran") || name.contains("monitor")) {
            return new Image("https://dummyimage.com/h/320x200/667eea/ffffff&text=MONITOR", true);
        } else if (name.contains("casque") || name.contains("audio")) {
            return new Image("https://dummyimage.com/h/320x200/ff6b6b/ffffff&text=HEADPHONES", true);
        } else if (name.contains("imprimante")) {
            return new Image("https://dummyimage.com/h/320x200/4ecdc4/ffffff&text=PRINTER", true);
        } else {
            return new Image("https://dummyimage.com/h/320x200/ffeaa7/000000&text=PRODUCT", true);
        }
    }

    /**
     * Obtient une image colorée garantie pour le produit (fallback ultime)
     */
    private Image getColorfulProductImage(String productName, String category) {
        String name = productName.toLowerCase();
        String cat = category != null ? category.toLowerCase() : "";

        // Utiliser des images garanties avec couleurs vives
        try {
            // Catégorie Scientifique - images colorées garanties
            if (cat.contains("scientifique") || name.contains("calculatrice")) {
                if (name.contains("casio")) {
                    return new Image("https://dummyimage.com/h/320x200/ff6b6b/ffffff&text=CASIO+FX", true);
                } else if (name.contains("ti")) {
                    return new Image("https://dummyimage.com/h/320x200/4ecdc4/ffffff&text=TI+CALC", true);
                } else {
                    return new Image("https://dummyimage.com/h/320x200/45b7d1/ffffff&text=CALCULATOR", true);
                }
            }

            // Catégorie Informatique - images colorées garanties
            if (cat.contains("informatique") || name.contains("laptop") || name.contains("portable")) {
                if (name.contains("dell")) {
                    return new Image("https://dummyimage.com/h/320x200/667eea/ffffff&text=DELL+LAPTOP", true);
                } else if (name.contains("hp")) {
                    return new Image("https://dummyimage.com/h/320x200/96ceb4/ffffff&text=HP+LAPTOP", true);
                } else {
                    return new Image("https://dummyimage.com/h/320x200/ffeaa7/000000&text=LAPTOP", true);
                }
            }

            // Catégorie Outils intelligents - images colorées garanties
            if (cat.contains("outils") || cat.contains("intelligent") || name.contains("smart")) {
                if (name.contains("montre")) {
                    return new Image("https://dummyimage.com/h/320x200/ff6b6b/ffffff&text=SMART+WATCH", true);
                } else {
                    return new Image("https://dummyimage.com/h/320x200/4ecdc4/ffffff&text=SMART+GADGET", true);
                }
            }

            // Catégorie Accessoires - images colorées garanties
            if (cat.contains("accessoire") || name.contains("clavier") || name.contains("souris")) {
                if (name.contains("clavier")) {
                    return new Image("https://dummyimage.com/h/320x200/45b7d1/ffffff&text=KEYBOARD", true);
                } else if (name.contains("souris")) {
                    return new Image("https://dummyimage.com/h/320x200/96ceb4/ffffff&text=MOUSE", true);
                } else if (name.contains("casque")) {
                    return new Image("https://dummyimage.com/h/320x200/ffeaa7/000000&text=HEADPHONES", true);
                } else {
                    return new Image("https://dummyimage.com/h/320x200/667eea/ffffff&text=ACCESSORY", true);
                }
            }

            // Fallback générique coloré
            if (name.contains("smartphone") || name.contains("phone")) {
                return new Image("https://dummyimage.com/h/320x200/ff6b6b/ffffff&text=SMARTPHONE", true);
            } else if (name.contains("écran") || name.contains("monitor")) {
                return new Image("https://dummyimage.com/h/320x200/4ecdc4/ffffff&text=MONITOR", true);
            } else {
                return new Image("https://dummyimage.com/h/320x200/ffeaa7/000000&text=PRODUCT", true);
            }

        } catch (Exception e) {
            // Fallback final avec couleur garantie
            return new Image("https://dummyimage.com/h/320x200/45b7d1/ffffff&text=PRODUCT", true);
        }
    }

    private void showEmptyResults() {
        System.out.println("📭 Affichage résultats vides");

        VBox emptyBox = new VBox(10);
        emptyBox.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label emptyIcon = new Label("🔍");
        emptyIcon.setStyle("-fx-font-size: 48px;");

        Label emptyText = new Label("Aucune suggestion trouvée");
        emptyText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #718096;");

        Label emptySubtext = new Label("Essayez avec d'autres mots-clés ou catégories");
        emptySubtext.setStyle("-fx-font-size: 12px; -fx-text-fill: #a0aec0;");

        emptyBox.getChildren().addAll(emptyIcon, emptyText, emptySubtext);
        resultsContainer.getChildren().add(emptyBox);

        System.out.println("✅ Message vide affiché");
    }

    private void showErrorResults() {
        System.out.println("💥 Affichage erreur");

        VBox errorBox = new VBox(10);
        errorBox.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label errorIcon = new Label("⚠️");
        errorIcon.setStyle("-fx-font-size: 48px;");

        Label errorText = new Label("Erreur de recherche");
        errorText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e53e3e;");

        Label errorSubtext = new Label("Veuillez réessayer plus tard");
        errorSubtext.setStyle("-fx-font-size: 12px; -fx-text-fill: #a0aec0;");

        errorBox.getChildren().addAll(errorIcon, errorText, errorSubtext);
        resultsContainer.getChildren().add(errorBox);

        System.out.println("✅ Message erreur affiché");
    }

    private void showDetails(String suggestion) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la suggestion");
        alert.setHeaderText("📋 " + suggestion);
        alert.setContentText("Ce produit pourrait correspondre à vos besoins.\n\n"
                          + "• Haute qualité garantie\n"
                          + "• Livraison rapide\n"
                          + "• Garantie incluse\n"
                          + "• Support client disponible");
        alert.showAndWait();
    }

    private void showProductDetails(Produit product) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du produit");
        alert.setHeaderText("📋 " + product.getNom());

        String details = "Catégorie: " + product.getCategorie() + "\n" +
                        "Prix: " + String.format("%.2f DT", product.getPrix()) + "\n" +
                        "Stock: " + product.getStock() + " unités\n" +
                        "Description: " + (product.getDescription() != null ? product.getDescription() : "Non spécifiée") + "\n\n" +
                        "Ce produit correspond à vos critères de recherche.";

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void addToCart(Produit product) {
        try {
            // Utiliser le service panier
            tn.formini.services.cart.CartService cartService = tn.formini.services.cart.CartService.getInstance();
            cartService.add(product);
            statusLabel.setText("✅ \"" + product.getNom() + "\" ajouté au panier !");
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur lors de l'ajout au panier: " + e.getMessage());
        }
    }

    private void searchInShop(String searchTerm) {
        try {
            // Naviguer vers la boutique avec le terme de recherche
            System.out.println("🔍 Recherche de \"" + searchTerm + "\" dans la boutique...");
            statusLabel.setText("🔍 Recherche de \"" + searchTerm + "\" dans la boutique...");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la navigation: " + e.getMessage());
            statusLabel.setText("❌ Erreur lors de la navigation: " + e.getMessage());
        }
    }
}
