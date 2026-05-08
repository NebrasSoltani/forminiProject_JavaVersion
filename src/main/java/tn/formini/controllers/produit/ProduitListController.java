package tn.formini.controllers.produit;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.controllers.MainController;
import tn.formini.controllers.produit.ProduitEditFormController;
import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ProduitListController implements Initializable {

    @FXML private Label labelFormTitle;
    @FXML private TextField fieldSearch;
    @FXML private ComboBox<String> fieldFilterCategorie;
    @FXML private TilePane productsContainer;
    @FXML private StackPane emptyState;
    @FXML private HBox paginationContainer;
    @FXML private Button firstPageBtn;
    @FXML private Button prevPageBtn;
    @FXML private Label currentPageLabel;
    @FXML private Button nextPageBtn;
    @FXML private Button lastPageBtn;
    @FXML private Label totalItemsLabel;
    
    // Statistics labels
    @FXML private Label statTotalProducts;
    @FXML private Label statAvailableProducts;
    @FXML private Label statLowStockProducts;
    @FXML private Label statStockValue;

    private MainController mainController;
    private final ProduitService service = new ProduitService();
    private List<Produit> allProducts;
    private List<Produit> filteredProducts;
    private Consumer<Void> onClose;
    
    // Pagination settings
    private static final int ITEMS_PER_PAGE = 12;
    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("=== PRODUIT LIST CONTROLLER: INITIALIZE() CALLED! ===");
        
        // Initialize category filter
        fieldFilterCategorie.setItems(FXCollections.observableArrayList(
                "Toutes les catégories", "Informatique", "Scientifique", "Outils intelligents", "Accessoires"
        ));
        
        // Initialize pagination
        initializePagination();
        
        System.out.println("=== PRODUIT LIST CONTROLLER: ABOUT TO CALL REFRESHPRODUCTS() ===");
        refreshProducts();
        fieldFilterCategorie.setValue("Toutes les catégories");

        // Load products
        loadProducts();
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    private void loadProducts() {
        allProducts = service.afficher();
        filteredProducts = allProducts;
        updateStatistics();
        updatePagination();
        displayPaginatedProducts();
    }
    
    private void refreshProducts() {
        System.out.println("=== PRODUIT LIST CONTROLLER: REFRESHPRODUCTS() CALLED! ===");
        allProducts = service.afficher();
        filteredProducts = allProducts;
        currentPage = 1;
        updateStatistics();
        updatePagination();
        displayPaginatedProducts();
    }
    
    private void initializePagination() {
        // Set pagination button symbols
        firstPageBtn.setText("«");
        prevPageBtn.setText("«");
        nextPageBtn.setText("»");
        lastPageBtn.setText("»");
        
        // Set button actions
        firstPageBtn.setOnAction(e -> goToFirstPage());
        prevPageBtn.setOnAction(e -> goToPreviousPage());
        nextPageBtn.setOnAction(e -> goToNextPage());
        lastPageBtn.setOnAction(e -> goToLastPage());
    }
    
    private void updatePagination() {
        if (filteredProducts == null) {
            totalPages = 0;
        } else {
            totalPages = (int) Math.ceil((double) filteredProducts.size() / ITEMS_PER_PAGE);
        }
        
        // Update pagination controls
        currentPageLabel.setText("Page " + currentPage + " sur " + totalPages);
        totalItemsLabel.setText(filteredProducts != null ? filteredProducts.size() + " produits" : "0 produits");
        
        // Enable/disable buttons
        firstPageBtn.setDisable(currentPage <= 1);
        prevPageBtn.setDisable(currentPage <= 1);
        nextPageBtn.setDisable(currentPage >= totalPages);
        lastPageBtn.setDisable(currentPage >= totalPages);
        
        // Show/hide pagination container
        paginationContainer.setVisible(totalPages > 1);
    }
    
    private void displayPaginatedProducts() {
        if (filteredProducts == null || filteredProducts.isEmpty()) {
            productsContainer.getChildren().clear();
            emptyState.setVisible(true);
            return;
        }
        
        emptyState.setVisible(false);
        
        // Get items for current page
        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredProducts.size());
        
        List<Produit> currentPageProducts = filteredProducts.subList(startIndex, endIndex);
        displayProducts(currentPageProducts);
    }
    
    @FXML
    private void goToFirstPage() {
        currentPage = 1;
        updatePagination();
        displayPaginatedProducts();
    }
    
    @FXML
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
            displayPaginatedProducts();
        }
    }
    
    @FXML
    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
            displayPaginatedProducts();
        }
    }
    
    @FXML
    private void goToLastPage() {
        currentPage = totalPages;
        updatePagination();
        displayPaginatedProducts();
    }

    @FXML
    public void deleteProductsWithoutImages() {
        // Find products without real images
        List<Produit> productsToDelete = allProducts.stream()
                .filter(p -> !hasRealImage(p))
                .collect(java.util.stream.Collectors.toList());
        
        List<Produit> productsToKeep = allProducts.stream()
                .filter(p -> hasRealImage(p))
                .collect(java.util.stream.Collectors.toList());
        
        if (productsToDelete.isEmpty()) {
            showInfo("Tous les produits ont des images. Aucun produit à supprimer.");
            return;
        }
        
        if (productsToKeep.isEmpty()) {
            showWarning("Aucun produit n'a d'image valide. Voulez-vous supprimer tous les produits ?");
            return;
        }
        
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Nettoyer les produits sans image");
        alert.setHeaderText("Supprimer " + productsToDelete.size() + " produit(s) sans image ?");
        alert.setContentText("Cette action est irréversible.\n\n" +
                "Produits à supprimer (sans image) :\n" + 
                productsToDelete.stream()
                        .limit(10) // Limit display to first 10
                        .map(p -> "- " + p.getNom() + " (ID: " + p.getId() + ")")
                        .collect(java.util.stream.Collectors.joining("\n")) + 
                (productsToDelete.size() > 10 ? "\n... et " + (productsToDelete.size() - 10) + " autres" : "") +
                "\n\nProduits qui seront conservés (avec image) :\n" +
                productsToKeep.stream()
                        .limit(10) // Limit display to first 10
                        .map(p -> "- " + p.getNom() + " (ID: " + p.getId() + ")")
                        .collect(java.util.stream.Collectors.joining("\n")) +
                (productsToKeep.size() > 10 ? "\n... et " + (productsToKeep.size() - 10) + " autres" : ""));
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                int deletedCount = 0;
                for (Produit produit : productsToDelete) {
                    service.supprimer(produit.getId());
                    deletedCount++;
                }
                
                showSuccess(deletedCount + " produit(s) sans image ont été supprimés. " +
                          productsToKeep.size() + " produit(s) avec image ont été conservés.");
                loadProducts(); // Reload the product list
                
            } catch (Exception e) {
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }
    
    private boolean hasRealImage(Produit produit) {
        if (produit.getImage() == null || produit.getImage().trim().isEmpty()) {
            return false;
        }
        
        String imageUrl = produit.getImage().trim();
        
        // Check if it's a placeholder/default image
        if (imageUrl.contains("placeholder") || 
            imageUrl.contains("default") || 
            imageUrl.contains("no-image") ||
            imageUrl.contains("empty") ||
            imageUrl.contains("null")) {
            return false;
        }
        
        // Check if it's a valid URL or file path
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return true; // Assume valid HTTP URLs have real images
        }
        
        if (imageUrl.startsWith("file://") || imageUrl.contains(":/")) {
            return true; // Assume file paths have real images
        }
        
        // Check for common image extensions
        String lowerUrl = imageUrl.toLowerCase();
        return lowerUrl.endsWith(".jpg") || 
               lowerUrl.endsWith(".jpeg") || 
               lowerUrl.endsWith(".png") || 
               lowerUrl.endsWith(".gif") || 
               lowerUrl.endsWith(".bmp") ||
               lowerUrl.endsWith(".webp");
    }

    private void displayProducts(List<Produit> products) {
        System.out.println("=== DISPLAYPRODUCTS() CALLED ===");
        System.out.println("Products container: " + (productsContainer != null ? "NOT NULL" : "NULL"));
        System.out.println("Products container children count: " + (productsContainer != null ? productsContainer.getChildren().size() : "N/A"));
        
        if (productsContainer == null) {
            System.err.println("ERROR: productsContainer is null!");
            return;
        }
        
        productsContainer.getChildren().clear();
        System.out.println("Cleared products container");
        
        if (products.isEmpty()) {
            System.out.println("No products to display, showing empty state");
            emptyState.setVisible(true);
            return;
        }
        
        emptyState.setVisible(false);
        
        System.out.println("Total products: " + products.size());
        
        // Show all products (no filtering)
        for (Produit produit : products) {
            System.out.println("Adding product card for: " + produit.getNom());
            productsContainer.getChildren().add(createProductCard(produit));
        }
        
        System.out.println("Final products container children count: " + productsContainer.getChildren().size());
    }
    
    private void updateStatistics() {
        if (allProducts == null) return;
        
        int totalProducts = allProducts.size();
        int availableProducts = (int) allProducts.stream()
                .filter(p -> "disponible".equalsIgnoreCase(p.getStatut()) && p.getStock() > 0)
                .count();
        int lowStockProducts = (int) allProducts.stream()
                .filter(p -> p.getStock() > 0 && p.getStock() < 5)
                .count();
        double totalStockValue = allProducts.stream()
                .filter(p -> p.getStock() > 0)
                .mapToDouble(p -> p.getPrix().doubleValue() * p.getStock())
                .sum();
        
        // Update statistics labels
        statTotalProducts.setText(String.valueOf(totalProducts));
        statAvailableProducts.setText(String.valueOf(availableProducts));
        statLowStockProducts.setText(String.valueOf(lowStockProducts));
        statStockValue.setText(String.format("%.2f DT", totalStockValue));
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox();
        card.getStyleClass().addAll("product-card", "card");
        card.setSpacing(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 8); -fx-cursor: hand;");

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-background-color: #334155; -fx-border-color: #475569; -fx-border-radius: 12; -fx-background-radius: 12;");
        
        // Load image if available
        if (produit.getImage() != null && !produit.getImage().trim().isEmpty()) {
            String imageUrl = produit.getImage().trim();
            
            // Debug: Print the image URL to console
            System.out.println("Loading image: " + imageUrl);
            
            try {
                javafx.scene.image.Image image = new javafx.scene.image.Image(imageUrl, true);
                
                // Wait for image to load and check if it's valid
                image.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == 1.0) {
                        if (image.getException() == null && image.getWidth() > 0 && image.getHeight() > 0) {
                            imageView.setImage(image);
                            System.out.println("Image loaded successfully: " + image.getWidth() + "x" + image.getHeight());
                        } else {
                            System.out.println("Image failed to load: " + (image.getException() != null ? image.getException().getMessage() : "Invalid dimensions"));
                            showPlaceholderImage(imageView);
                        }
                    }
                });
                
                // Set a timeout in case image doesn't load
                javafx.util.Duration timeout = javafx.util.Duration.seconds(5);
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(timeout);
                pause.setOnFinished(e -> {
                    if (imageView.getImage() == null) {
                        System.out.println("Image loading timeout, showing placeholder");
                        showPlaceholderImage(imageView);
                    }
                });
                pause.play();
                
            } catch (Exception e) {
                System.out.println("Exception loading image: " + e.getMessage());
                showPlaceholderImage(imageView);
            }
        } else {
            System.out.println("No image URL provided, showing placeholder");
            showPlaceholderImage(imageView);
        }

        // Product Header
        HBox header = new HBox();
        header.setSpacing(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label categoryLabel = new Label(produit.getCategorie());
        categoryLabel.getStyleClass().add("product-category");
        categoryLabel.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-font-weight: 600;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(getStatusText(produit.getStatut()));
        statusLabel.getStyleClass().add("product-status");
        statusLabel.setStyle(getStatusStyle(produit.getStatut()));

        header.getChildren().addAll(categoryLabel, spacer, statusLabel);

        // Product Name
        Label nameLabel = new Label(produit.getNom());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(240);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: white; -fx-line-spacing: 2px;");

        // Product Description
        Label descLabel = new Label(truncateText(produit.getDescription(), 100));
        descLabel.getStyleClass().add("product-description");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(240);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-line-spacing: 1.5px;");

        // Product Price and Stock
        HBox priceStockBox = new HBox();
        priceStockBox.setSpacing(16);
        priceStockBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label priceLabel = new Label(String.format("%.3f DT", produit.getPrix()));
        priceLabel.getStyleClass().add("product-price");
        priceLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 18px; -fx-text-fill: #10b981;");

        Label stockLabel = new Label("Stock: " + produit.getStock());
        stockLabel.getStyleClass().add("product-stock");
        stockLabel.setStyle(getStockStyle(produit.getStock()));

        priceStockBox.getChildren().addAll(priceLabel, stockLabel);

        // Action Buttons
        HBox actionsBox = new HBox();
        actionsBox.setSpacing(12);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button editBtn = new Button("✏️ Modifier");
        editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
        editBtn.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-size: 13px; -fx-font-weight: 600; -fx-cursor: hand;");
        editBtn.setOnAction(e -> editProduct(produit));

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.getStyleClass().addAll("btn-danger", "btn-small");
        deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-size: 13px; -fx-font-weight: 600; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteProduct(produit));

        actionsBox.getChildren().addAll(editBtn, deleteBtn);

        // Add all elements to card
        card.getChildren().addAll(imageView, header, nameLabel, descLabel, priceStockBox, actionsBox);
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #334155; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0, 0, 10); -fx-cursor: hand; -fx-translate-y: -2;");
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 8); -fx-cursor: hand; -fx-translate-y: 0;");
        });

        return card;
    }

    private String getStatusText(String statut) {
        switch (statut.toLowerCase()) {
            case "disponible": return "✅ Disponible";
            case "épuisé": return "❌ Épuisé";
            case "archive": return "📁 Archivé";
            default: return statut;
        }
    }

    private String getStatusStyle(String statut) {
        switch (statut.toLowerCase()) {
            case "disponible": return "-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-font-weight: 600;";
            case "épuisé": return "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-font-weight: 600;";
            case "archive": return "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-font-weight: 600;";
            default: return "-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-font-weight: 600;";
        }
    }

    private String getStockStyle(int stock) {
        if (stock == 0) {
            return "-fx-text-fill: #ef4444; -fx-font-weight: 700; -fx-background-color: rgba(239, 68, 68, 0.1); -fx-background-radius: 6; -fx-padding: 4 8;";
        } else if (stock < 5) {
            return "-fx-text-fill: #f59e0b; -fx-font-weight: 700; -fx-background-color: rgba(245, 158, 11, 0.1); -fx-background-radius: 6; -fx-padding: 4 8;";
        } else {
            return "-fx-text-fill: #10b981; -fx-font-weight: 600;";
        }
    }

    private void showPlaceholderImage(ImageView imageView) {
        // Create a modern placeholder with a pattern
        String placeholderSvg = "data:image/svg+xml;base64," + 
            java.util.Base64.getEncoder().encodeToString(
                ("<svg width='240' height='160' xmlns='http://www.w3.org/2000/svg'>" +
                "<rect width='240' height='160' fill='#334155' stroke='#475569' stroke-width='2' rx='12'/>" +
                "<text x='120' y='80' text-anchor='middle' font-family='Arial' font-size='16' fill='#94a3b8'>No Image</text>" +
                "<rect x='100' y='50' width='40' height='40' fill='none' stroke='#64748b' stroke-width='2' rx='4'/>" +
                "<circle cx='120' cy='60' r='8' fill='none' stroke='#64748b' stroke-width='2'/>" +
                "<path d='M110 75 L120 65 L130 75' stroke='#64748b' stroke-width='2' fill='none'/>" +
                "</svg>").getBytes()
            );
        
        try {
            javafx.scene.image.Image placeholderImage = new javafx.scene.image.Image(placeholderSvg);
            imageView.setImage(placeholderImage);
            imageView.setStyle("-fx-background-color: transparent; -fx-border-color: #475569; -fx-border-radius: 12;");
        } catch (Exception e) {
            // Fallback: create a simple colored rectangle
            imageView.setImage(null);
            imageView.setStyle("-fx-background-color: #334155; -fx-border-color: #475569; -fx-border-radius: 12;");
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    @FXML
    public void searchProducts() {
        String searchTerm = fieldSearch.getText().trim().toLowerCase();
        String selectedCategory = fieldFilterCategorie.getValue();

        filteredProducts = allProducts.stream()
                .filter(p -> searchTerm.isEmpty() || 
                         p.getNom().toLowerCase().startsWith(searchTerm) || 
                         p.getDescription().toLowerCase().contains(searchTerm))
                .filter(p -> selectedCategory.equals("Toutes les catégories") || 
                         p.getCategorie().equals(selectedCategory))
                .toList();

        currentPage = 1;
        updatePagination();
        displayPaginatedProducts();
        
        // Show search feedback
        showSearchFeedback(searchTerm, filteredProducts.size());
    }

    @FXML
    public void filterByCategory(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String category = (String) clickedButton.getUserData();
        
        // Update button styles
        updateCategoryButtonStyles(clickedButton);
        
        // Filter products by category
        if (category.equals("Tous")) {
            filteredProducts = allProducts;
            showSearchFeedback("", allProducts.size());
        } else {
            filteredProducts = allProducts.stream()
                    .filter(p -> p.getCategorie().equals(category))
                    .toList();
            showSearchFeedback("", filteredProducts.size());
        }
        
        currentPage = 1;
        updatePagination();
        displayPaginatedProducts();
        
        // Update search field
        fieldSearch.clear();
    }

    private void showSearchFeedback(String searchTerm, int resultCount) {
        if (searchTerm.isEmpty()) {
            labelFormTitle.setText("Gestion des Produits");
            return;
        }
        
        if (resultCount == 0) {
            labelFormTitle.setText("Aucun produit trouvé pour \"" + searchTerm + "\"");
        } else if (resultCount == 1) {
            labelFormTitle.setText("1 produit trouvé pour \"" + searchTerm + "\"");
        } else {
            labelFormTitle.setText(resultCount + " produits trouvés pour \"" + searchTerm + "\"");
        }
    }

    private void updateCategoryButtonStyles(Button activeButton) {
        // Reset all category buttons to default style
        for (javafx.scene.Node node : ((HBox) activeButton.getParent()).getChildren()) {
            if (node instanceof Button && node.getStyleClass().contains("category-filter-btn")) {
                node.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #94a3b8; -fx-background-radius: 20; -fx-padding: 8 16; -fx-font-size: 13px; -fx-font-weight: 600; -fx-cursor: hand;");
            }
        }
        
        // Highlight active button
        activeButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 16; -fx-font-size: 13px; -fx-font-weight: 600; -fx-cursor: hand;");
    }

    @FXML
    public void addProduct() {
        if (mainController != null) {
            mainController.showProductForm(null);
        }
    }

    @FXML
    public void editProduct(Produit produit) {
        openEditForm(produit);
    }

    @FXML
    public void deleteProduct(Produit produit) {
        if (produit == null) return;
        openDeleteConfirmModal(produit);
    }

    private void openDeleteConfirmModal(Produit produit) {
        try {
            // Load the delete confirmation modal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/produit/ProduitDeleteConfirm.fxml"));
            javafx.scene.Parent root = loader.load();
            
            ProduitDeleteConfirmController controller = loader.getController();
            controller.setProduit(produit);
            controller.setOnDeleted(() -> {
                refreshProducts(); // Use refreshProducts() to work with pagination
            });
            
            Stage stage = new Stage();
            stage.setTitle("Supprimer le produit");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(productsContainer.getScene().getWindow());
            stage.showAndWait();
            
        } catch (Exception e) {
            // Fallback to simple alert if modal fails
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Supprimer produit");
            alert.setHeaderText("Supprimer \"" + produit.getNom() + "\" ?");
            alert.setContentText(
                "Êtes-vous sûr de vouloir supprimer ce produit ?\n\n" +
                "Nom: " + produit.getNom() + "\n" +
                "Catégorie: " + produit.getCategorie() + "\n" +
                "Prix: " + String.format("%.3f DT", produit.getPrix()) + "\n" +
                "Stock: " + produit.getStock() + "\n\n" +
                "Cette action est irréversible."
            );
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    service.supprimer(produit.getId());
                    showSuccess("Produit supprimé avec succès !");
                    refreshProducts(); // Use refreshProducts() to work with pagination
                } catch (Exception ex) {
                    showError("Erreur lors de la suppression: " + ex.getMessage());
                }
            }
        }
    }
    
    private void showDeleteSuccess(Produit produit) {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Succès");
        successAlert.setHeaderText("Produit Supprimé");
        successAlert.setContentText(
            "Le produit \"" + produit.getNom() + "\" a été supprimé avec succès.\n\n" +
            "Catégorie: " + produit.getCategorie() + "\n" +
            "Prix: " + String.format("%.3f DT", produit.getPrix())
        );
        successAlert.showAndWait();
    }
    
    private void showDeleteError(Produit produit) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Erreur");
        errorAlert.setHeaderText("Échec de Suppression");
        errorAlert.setContentText(
            "Impossible de supprimer le produit \"" + produit.getNom() + "\".\n\n" +
            "Veuillez vérifier:\n" +
            "• Que le produit existe toujours\n" +
            "• Que vous avez les permissions nécessaires\n" +
            "• Que la connexion à la base de données est active"
        );
        errorAlert.showAndWait();
    }

    @FXML
    public void goBack() {
        if (onClose != null) {
            onClose.accept(null);
        } else if (mainController != null) {
            mainController.showDashboard();
        }
    }

    private void openEditForm(Produit produit) {
        System.out.println("=== DEBUG: openEditForm() STARTED ===");
        System.out.println("Product to edit: " + produit.getNom());
        
        try {
            // Check FXML resource
            String fxmlPath = "/fxml/product/ProduitEditForm_Compact.fxml";
            URL resource = getClass().getResource(fxmlPath);
            System.out.println("FXML resource check: " + (resource != null ? "FOUND" : "NOT FOUND"));
            System.out.println("FXML path: " + fxmlPath);
            
            if (resource == null) {
                throw new RuntimeException("FXML file not found: " + fxmlPath);
            }
            
            System.out.println("FXML URL: " + resource.toExternalForm());
            
            // Load the edit form FXML
            FXMLLoader loader = new FXMLLoader(resource);
            System.out.println("FXMLLoader created successfully");
            
            VBox editFormRoot = loader.load();
            System.out.println("FXML loaded successfully");
            
            // Get the controller
            ProduitEditFormController editController = loader.getController();
            System.out.println("Controller: " + (editController != null ? "FOUND" : "NULL"));
            
            if (editController == null) {
                throw new RuntimeException("Controller not found in FXML");
            }
            
            editController.setProduit(produit);
            editController.setOnProductUpdated(this::loadProducts);
            System.out.println("Controller initialized successfully");
            
            // Create and configure the dialog stage
            Stage editStage = new Stage();
            editStage.setTitle("Modifier: " + produit.getNom());
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.setResizable(true);
            editStage.setWidth(950);
            editStage.setHeight(700);
            editStage.setMinWidth(800);
            editStage.setMinHeight(600);
            System.out.println("Stage configured successfully");
            
            // Create scene
            Scene scene = new Scene(editFormRoot);
            System.out.println("Scene created successfully");
            
            // Add CSS if available
            URL css = getClass().getResource("/css/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
                System.out.println("CSS loaded successfully");
            } else {
                System.out.println("CSS not found, continuing without styles");
            }
            
            editStage.setScene(scene);
            System.out.println("Scene set on stage");
            
            editStage.showAndWait();
            System.out.println("Form displayed successfully");
            
        } catch (Exception e) {
            System.err.println("ERROR in openEditForm: " + e.getMessage());
            e.printStackTrace();
            
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText("Impossible d'ouvrir le formulaire de modification");
            errorAlert.setContentText("Une erreur est survenue: " + e.getMessage() + "\n\nDétails: " + e.getClass().getSimpleName());
            errorAlert.showAndWait();
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Avertissement");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setOnClose(Consumer<Void> onClose) {
        this.onClose = onClose;
    }
}
