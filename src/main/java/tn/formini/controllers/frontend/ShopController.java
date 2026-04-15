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
import tn.formini.entities.produits.Produit;
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

    private final ProduitService produitService = new ProduitService();
    private final CartService cart = CartService.getInstance();
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

        List<Produit> filteredProducts = allProducts.stream()
                .filter(p -> searchTerm.isEmpty() || 
                         p.getNom().toLowerCase().startsWith(searchTerm))
                .toList();

        displayProducts(filteredProducts);
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

    private static String formatPrice(BigDecimal prix) {
        if (prix == null) return "0.000 DT";
        return String.format("%.3f DT", prix.doubleValue());
    }
}

