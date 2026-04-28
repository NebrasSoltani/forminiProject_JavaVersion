package tn.formini.controllers.frontend;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.formini.controllers.frontend.FrontMainController;
import tn.formini.entities.produits.Produit;
import tn.formini.services.AdvancedProductAIService;
import tn.formini.services.cart.CartService;
import tn.formini.services.produitsService.ProduitService;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopController implements Initializable {

    @FXML private FlowPane productsContainer;
    @FXML private Label labelCartHint;
    @FXML private TextField fieldSearch;
    @FXML private Button btnFirstPage;
    @FXML private Button btnPreviousPage;
    @FXML private Button btnNextPage;
    @FXML private Button btnLastPage;
    @FXML private HBox pageNumbersContainer;
    @FXML private Button btnViewCart;

    private final ProduitService produitService = new ProduitService();
    private final CartService cart = CartService.getInstance();
    private final AdvancedProductAIService aiService = AdvancedProductAIService.getInstance();
    private List<Produit> allProducts;
    private List<Produit> filteredProducts;
    private int currentPage = 1;
    private final int productsPerPage = 4;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshCartHint();
        loadProducts();
        
        // Add real-time search listener
        fieldSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            searchProducts();
        });
    }

    private void loadProducts() {
        allProducts = produitService.afficher();
        System.out.println("Loaded " + allProducts.size() + " products from database");
        displayProducts(allProducts);
    }

    private void displayProducts(List<Produit> products) {
        this.filteredProducts = products;
        this.currentPage = 1;
        updatePagination();
        displayCurrentPage();
    }

    private void displayCurrentPage() {
        productsContainer.getChildren().clear();
        
        int startIndex = (currentPage - 1) * productsPerPage;
        int endIndex = Math.min(startIndex + productsPerPage, filteredProducts.size());
        
        System.out.println("Displaying page " + currentPage + " - products " + startIndex + " to " + endIndex + " of " + filteredProducts.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            productsContainer.getChildren().add(createProductCard(filteredProducts.get(i)));
        }
        
        updatePaginationControls();
    }

    private void updatePaginationControls() {
        int totalPages = (int) Math.ceil((double) filteredProducts.size() / productsPerPage);
        
        // Enable/disable navigation buttons
        btnFirstPage.setDisable(currentPage <= 1);
        btnPreviousPage.setDisable(currentPage <= 1);
        btnNextPage.setDisable(currentPage >= totalPages || totalPages == 0);
        btnLastPage.setDisable(currentPage >= totalPages || totalPages == 0);
        
        // Create page number buttons
        createPageNumberButtons(totalPages);
    }
    
    private void createPageNumberButtons(int totalPages) {
        pageNumbersContainer.getChildren().clear();
        
        // Debug output
        System.out.println("Creating pagination - Total pages: " + totalPages + ", Current page: " + currentPage);
        
        // Always show at least page 1 for visibility testing
        if (totalPages >= 1) {
            // Always show page 1
            addPageNumberButton(1);
            
            // Show additional pages if they exist
            if (totalPages > 1) {
                // Show page numbers with smart display logic
                int startPage = Math.max(2, currentPage - 1);
                int endPage = Math.min(totalPages, startPage + 2);
                
                // Adjust start page if we're near the end
                if (endPage - startPage < 2) {
                    startPage = Math.max(2, endPage - 2);
                }
                
                // Add ellipsis if needed
                if (startPage > 2) {
                    addEllipsis();
                }
                
                // Add page range
                for (int i = startPage; i <= endPage; i++) {
                    addPageNumberButton(i);
                }
                
                // Add ellipsis if needed
                if (endPage < totalPages) {
                    addEllipsis();
                    addPageNumberButton(totalPages);
                }
            }
        } else {
            // No products - still show pagination for testing
            Label testLabel = new Label("Page 1");
            testLabel.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; -fx-font-weight: bold;");
            pageNumbersContainer.getChildren().add(testLabel);
        }
    }
    
    private void addPageNumberButton(int pageNumber) {
        Button pageBtn = new Button(String.valueOf(pageNumber));
        pageBtn.setPrefWidth(30);
        pageBtn.setPrefHeight(28);
        
        if (pageNumber == currentPage) {
            pageBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-border-color: #3b82f6; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 12px;");
        } else {
            pageBtn.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: #374151; -fx-font-size: 12px;");
            pageBtn.setOnAction(e -> goToPage(pageNumber));
        }
        
        pageNumbersContainer.getChildren().add(pageBtn);
    }
    
    private void addEllipsis() {
        Label ellipsis = new Label("...");
        ellipsis.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: bold; -fx-padding: 0 5;");
        pageNumbersContainer.getChildren().add(ellipsis);
    }

    private void updatePagination() {
        // Reset to first page when filters change
        currentPage = 1;
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("shop-product-card");
        card.setPadding(new Insets(12));
        card.setPrefWidth(220);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Reuse the same image strategy: if URL is invalid, keep empty.
        if (produit.getImage() != null && !produit.getImage().trim().isEmpty()) {
            try {
                imageView.setImage(new Image(produit.getImage().trim(), true));
            } catch (Exception ignored) {}
        }

        HBox badges = new HBox(8);
        Label badgeCat = new Label(nullToEmpty(produit.getCategorie()));
        badgeCat.getStyleClass().add("shop-badge");
        Label badgeStatus = new Label(produit.getStock() <= 0 ? "Rupture" : "Disponible");
        badgeStatus.getStyleClass().addAll("shop-badge", produit.getStock() <= 0 ? "shop-badge-danger" : "shop-badge-success");
        badges.getChildren().addAll(badgeCat, badgeStatus);

        Label nameLabel = new Label(produit.getNom());
        nameLabel.getStyleClass().add("shop-product-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label descLabel = new Label(truncateText(nullToEmpty(produit.getDescription()), 60));
        descLabel.getStyleClass().add("shop-product-desc");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(200);
        descLabel.setStyle("-fx-font-size: 11px;");

        HBox priceStock = new HBox(10);
        Label priceLabel = new Label(formatPrice(produit.getPrix()));
        priceLabel.getStyleClass().add("shop-product-price");
        priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label stockLabel = new Label("Stock: " + produit.getStock());
        stockLabel.getStyleClass().add("shop-product-stock");
        stockLabel.setStyle("-fx-font-size: 11px;");
        if (produit.getStock() <= 0) stockLabel.getStyleClass().add("shop-text-danger");

        priceStock.getChildren().addAll(priceLabel, spacer, stockLabel);

        int initialQtyInCart = cart.getQuantityForProduct(produit.getId());
        Label inCartLabel = new Label(initialQtyInCart > 0 ? ("Dans panier: " + initialQtyInCart) : "Pas encore ajouté");
        inCartLabel.getStyleClass().add("shop-in-cart");
        inCartLabel.setStyle("-fx-font-size: 10px;");

        Button btnDetails = new Button("👁");
        btnDetails.getStyleClass().addAll("btn-secondary", "shop-icon-btn");
        btnDetails.setPrefWidth(30);
        btnDetails.setPrefHeight(25);
        btnDetails.setOnAction(e -> showProductDetails(produit, initialQtyInCart));

        Button btnAdd = new Button("🛒 Ajouter");
        btnAdd.getStyleClass().addAll("btn-primary", "shop-add-btn");
        btnAdd.setDisable(produit.getStock() <= 0);
        btnAdd.setPrefHeight(25);
        btnAdd.setStyle("-fx-font-size: 11px;");
        btnAdd.setOnAction(e -> {
            cart.add(produit, 1);
            int q = cart.getQuantityForProduct(produit.getId());
            refreshCartHint();
            inCartLabel.setText("Dans panier: " + q);
        });

        HBox actions = new HBox(10, btnDetails, btnAdd);
        actions.getStyleClass().add("shop-card-actions");
        HBox.setHgrow(btnAdd, Priority.ALWAYS);
        btnAdd.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(imageView, badges, nameLabel, descLabel, priceStock, inCartLabel, actions);
        return card;
    }

    private void refreshCartHint() {
        if (labelCartHint == null) return;
        int total = cart.getItemsCountTotal();
        if (total <= 0) {
            labelCartHint.setText("Panier vide");
        } else if (total == 1) {
            labelCartHint.setText("Panier: 1 article");
        } else {
            labelCartHint.setText("Panier: " + total + " articles");
        }
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showProductDetails(Produit produit, int qtyInCart) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(420);
        imageView.setFitHeight(260);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.getStyleClass().add("shop-detail-image");

        if (produit.getImage() != null && !produit.getImage().trim().isEmpty()) {
            try {
                imageView.setImage(new Image(produit.getImage().trim(), true));
            } catch (Exception ignored) {}
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label name = new Label(produit.getNom());
        name.getStyleClass().add("shop-detail-title");

        Label cat = new Label("Catégorie: " + nullToEmpty(produit.getCategorie()));
        Label price = new Label("Prix: " + formatPrice(produit.getPrix()));
        Label stock = new Label("Stock: " + produit.getStock());
        Label qty = new Label("Dans votre panier: " + Math.max(0, qtyInCart));
        Label desc = new Label(nullToEmpty(produit.getDescription()));
        desc.setWrapText(true);

        content.getChildren().addAll(name, imageView, cat, price, stock, qty, new Label("Description:"), desc);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du produit");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(content);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private static String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @FXML
    public void searchProducts() {
        String searchTerm = fieldSearch.getText().trim().toLowerCase();

        if (searchTerm.isEmpty()) {
            displayProducts(allProducts);
            return;
        }

        // Recherche simple locale d'abord
        List<Produit> localResults = allProducts.stream()
                .filter(p -> p.getNom().toLowerCase().contains(searchTerm) ||
                         p.getDescription().toLowerCase().contains(searchTerm))
                .toList();

        if (!localResults.isEmpty()) {
            displayProducts(localResults);
            return;
        }

        // Si aucun résultat local, utiliser l'IA pour suggestions
        showLoadingSearch();
        String userContext = "Client intéressé par: " + searchTerm + 
                           ", catégories disponibles: " + getAvailableCategories();
        
        aiService.getSearchSuggestions(searchTerm, userContext)
            .thenAccept(suggestions -> {
                javafx.application.Platform.runLater(() -> {
                    hideLoadingSearch();
                    displayAISearchSuggestions(suggestions, searchTerm);
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    hideLoadingSearch();
                    showSearchError(searchTerm);
                });
                return null;
            });
    }

    @FXML
    public void firstPage() {
        if (currentPage > 1) {
            currentPage = 1;
            displayCurrentPage();
        }
    }

    @FXML
    public void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            displayCurrentPage();
        }
    }

    @FXML
    public void nextPage() {
        int totalPages = (int) Math.ceil((double) filteredProducts.size() / productsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            displayCurrentPage();
        }
    }
    
    @FXML
    public void lastPage() {
        int totalPages = (int) Math.ceil((double) filteredProducts.size() / productsPerPage);
        if (currentPage < totalPages) {
            currentPage = totalPages;
            displayCurrentPage();
        }
    }
    
    private void goToPage(int pageNumber) {
        int totalPages = (int) Math.ceil((double) filteredProducts.size() / productsPerPage);
        if (pageNumber >= 1 && pageNumber <= totalPages) {
            currentPage = pageNumber;
            displayCurrentPage();
        }
    }

    @FXML
    public void filterByCategory(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String category = (String) clickedButton.getUserData();
        
        // Update button styles
        updateCategoryButtonStyles(clickedButton);
        
        // Filter products by category
        if (category.equals("Tous")) {
            displayProducts(allProducts);
        } else {
            List<Produit> filteredProducts = allProducts.stream()
                    .filter(p -> p.getCategorie().equals(category))
                    .toList();
            displayProducts(filteredProducts);
        }
        
        // Clear search field
        fieldSearch.clear();
    }

    private void updateCategoryButtonStyles(Button activeButton) {
        // This would require access to the parent container to reset all buttons
        // For simplicity, we'll just highlight the active button
        activeButton.setStyle("-fx-background-color: -fx-primary; -fx-text-fill: white;");
    }

    @FXML
    public void viewCart() {
        try {
            // Naviguer vers la page du panier
            FrontMainController.getInstance().loadView("/fxml/frontend/Cart.fxml");
        } catch (Exception e) {
            System.err.println("Erreur lors de la navigation vers le panier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthodes utilitaires pour la recherche IA
    
    private String getAvailableCategories() {
        return allProducts.stream()
            .map(Produit::getCategorie)
            .filter(cat -> cat != null && !cat.isEmpty())
            .distinct()
            .collect(java.util.stream.Collectors.joining(", "));
    }

    private String formatPrice(BigDecimal prix) {
        if (prix == null) return "0.000 DT";
        return String.format("%.3f DT", prix.doubleValue());
    }

    private void showLoadingSearch() {
        // Afficher un indicateur de chargement pendant la recherche IA
        productsContainer.getChildren().clear();
        javafx.scene.control.ProgressIndicator loading = new javafx.scene.control.ProgressIndicator();
        loading.setPrefSize(50, 50);
        javafx.scene.control.Label loadingText = new javafx.scene.control.Label("🤖 Recherche IA en cours...");
        loadingText.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        javafx.scene.layout.VBox loadingBox = new javafx.scene.layout.VBox(10, loading, loadingText);
        loadingBox.setAlignment(javafx.geometry.Pos.CENTER);
        productsContainer.getChildren().add(loadingBox);
    }

    private void hideLoadingSearch() {
        // Cache l'indicateur de chargement (sera remplacé par displayProducts)
    }

    private void displayAISearchSuggestions(List<String> suggestions, String originalSearch) {
        productsContainer.getChildren().clear();
        
        // Titre des suggestions IA
        javafx.scene.control.Label title = new javafx.scene.control.Label(
            "🤖 Suggestions IA pour: \"" + originalSearch + "\""
        );
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4f46e5; -fx-padding: 10px;");
        productsContainer.getChildren().add(title);

        // Afficher les suggestions IA comme des cartes de produits virtuels
        for (int i = 0; i < suggestions.size(); i++) {
            String suggestion = suggestions.get(i);
            javafx.scene.layout.VBox suggestionCard = createAISuggestionCard(suggestion, i + 1);
            productsContainer.getChildren().add(suggestionCard);
        }

        // Bouton pour réinitialiser la recherche
        javafx.scene.control.Button resetBtn = new javafx.scene.control.Button("🔄 Afficher tous les produits");
        resetBtn.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16px;");
        resetBtn.setOnAction(e -> displayProducts(allProducts));
        
        javafx.scene.layout.HBox buttonContainer = new javafx.scene.layout.HBox(resetBtn);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        buttonContainer.setStyle("-fx-padding: 20px;");
        productsContainer.getChildren().add(buttonContainer);
    }

    private javafx.scene.layout.VBox createAISuggestionCard(String suggestion, int index) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(10);
        card.setStyle("-fx-background-color: white; " +
                      "-fx-border-color: #e5e7eb; " +
                      "-fx-border-radius: 8; " +
                      "-fx-background-radius: 8; " +
                      "-fx-padding: 15; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Badge de suggestion
        javafx.scene.control.Label badge = new javafx.scene.control.Label(String.valueOf(index));
        badge.setStyle("-fx-background-color: #4f46e5; " +
                       "-fx-text-fill: white; " +
                       "-fx-background-radius: 50%; " +
                       "-fx-pref-width: 25; " +
                       "-fx-pref-height: 25; " +
                       "-fx-alignment: center; " +
                       "-fx-font-weight: bold;");

        // Nom de la suggestion
        javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(suggestion);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);

        // Badge IA
        javafx.scene.control.Label aiBadge = new javafx.scene.control.Label("🤖 Suggestion IA");
        aiBadge.setStyle("-fx-background-color: #f3f4f6; " +
                         "-fx-text-fill: #6b7280; " +
                         "-fx-background-radius: 4; " +
                         "-fx-font-size: 10px; " +
                         "-fx-padding: 2 8px;");

        // Bouton d'action
        javafx.scene.control.Button actionBtn = new javafx.scene.control.Button("🔍 Rechercher ce produit");
        actionBtn.setStyle("-fx-background-color: #22c55e; " +
                          "-fx-text-fill: white; " +
                          "-fx-background-radius: 6; " +
                          "-fx-cursor: hand; " +
                          "-fx-font-size: 11px;");
        actionBtn.setOnAction(e -> {
            fieldSearch.setText(suggestion);
            searchProducts(); // Relancer la recherche avec la suggestion
        });

        // Layout
        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(10, badge, nameLabel);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        card.getChildren().addAll(header, aiBadge, actionBtn);
        return card;
    }

    private void showSearchError(String searchTerm) {
        productsContainer.getChildren().clear();
        
        javafx.scene.control.Label errorTitle = new javafx.scene.control.Label(
            "❌ Erreur lors de la recherche IA pour: \"" + searchTerm + "\""
        );
        errorTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-padding: 10px;");
        
        javafx.scene.control.Label errorDesc = new javafx.scene.control.Label(
            "Veuillez réessayer ou utiliser une recherche plus simple."
        );
        errorDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-padding: 0 10px;");
        
        javafx.scene.control.Button retryBtn = new javafx.scene.control.Button("🔄 Réessayer");
        retryBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16px;");
        retryBtn.setOnAction(e -> searchProducts());
        
        javafx.scene.control.Button resetBtn = new javafx.scene.control.Button("📦 Tous les produits");
        resetBtn.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16px;");
        resetBtn.setOnAction(e -> displayProducts(allProducts));
        
        javafx.scene.layout.HBox buttonContainer = new javafx.scene.layout.HBox(10, retryBtn, resetBtn);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        buttonContainer.setStyle("-fx-padding: 20px;");
        
        javafx.scene.layout.VBox errorBox = new javafx.scene.layout.VBox(10, errorTitle, errorDesc, buttonContainer);
        errorBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        productsContainer.getChildren().add(errorBox);
    }
}

