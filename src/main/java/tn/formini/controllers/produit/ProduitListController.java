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
    @FXML private FlowPane productsContainer;
    @FXML private StackPane emptyState;

    private MainController mainController;
    private final ProduitService service = new ProduitService();
    private List<Produit> allProducts;
    private Consumer<Void> onClose;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize category filter
        fieldFilterCategorie.setItems(FXCollections.observableArrayList(
                "Toutes les catégories", "Informatique", "Scientifique", "Outils intelligents", "Accessoires"
        ));
        fieldFilterCategorie.setValue("Toutes les catégories");

        // Load products
        loadProducts();
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    private void loadProducts() {
        allProducts = service.afficher();
        displayProducts(allProducts);
    }

    @FXML
    public void deleteProductsWithoutImages() {
        // Define the products to keep
        java.util.Set<String> productsToKeep = java.util.Set.of(
            "school bag", "pen", "cle", "keyboard", "headset", "calculator"
        );
        
        // Find ALL products to delete (except the protected ones)
        List<Produit> productsToDelete = allProducts.stream()
                .filter(p -> !productsToKeep.contains(p.getNom().toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
        
        if (productsToDelete.isEmpty()) {
            showInfo("Aucun produit à supprimer (seuls les produits spécifiés existent).");
            return;
        }
        
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Nettoyer la base de données");
        alert.setHeaderText("Supprimer " + productsToDelete.size() + " produit(s) ?");
        alert.setContentText("Cette action est irréversible. Les produits suivants seront supprimés :\n" + 
                productsToDelete.stream()
                        .map(p -> "- " + p.getNom() + " (ID: " + p.getId() + ")")
                        .collect(java.util.stream.Collectors.joining("\n")) + 
                "\n\nLes produits suivants seront préservés :\n" +
                productsToKeep.stream()
                        .map(name -> "- " + name)
                        .collect(java.util.stream.Collectors.joining("\n")));
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                int deletedCount = 0;
                for (Produit produit : productsToDelete) {
                    service.supprimer(produit.getId());
                    deletedCount++;
                }
                
                showSuccess(deletedCount + " produit(s) ont été supprimés. " +
                          "Seuls les produits spécifiés sont conservés.");
                loadProducts(); // Reload the product list
                
            } catch (Exception e) {
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void displayProducts(List<Produit> products) {
        productsContainer.getChildren().clear();
        
        if (products.isEmpty()) {
            emptyState.setVisible(true);
            return;
        }
        
        emptyState.setVisible(false);
        
        System.out.println("Total products: " + products.size());
        
        // Show all products (no filtering)
        for (Produit produit : products) {
            productsContainer.getChildren().add(createProductCard(produit));
        }
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox();
        card.getStyleClass().addAll("product-card", "card");
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(280);

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(250);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");
        
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
        header.setSpacing(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label categoryLabel = new Label(produit.getCategorie());
        categoryLabel.getStyleClass().add("product-category");
        categoryLabel.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");

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
        nameLabel.setMaxWidth(250);

        // Product Description
        Label descLabel = new Label(truncateText(produit.getDescription(), 80));
        descLabel.getStyleClass().add("product-description");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(250);

        // Product Price and Stock
        HBox priceStockBox = new HBox();
        priceStockBox.setSpacing(15);
        priceStockBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label priceLabel = new Label(String.format("%.3f DT", produit.getPrix()));
        priceLabel.getStyleClass().add("product-price");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #10b981;");

        Label stockLabel = new Label("Stock: " + produit.getStock());
        stockLabel.getStyleClass().add("product-stock");
        stockLabel.setStyle(getStockStyle(produit.getStock()));

        priceStockBox.getChildren().addAll(priceLabel, stockLabel);

        // Action Buttons
        HBox actionsBox = new HBox();
        actionsBox.setSpacing(10);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button editBtn = new Button("✏️ Modifier");
        editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
        editBtn.setOnAction(e -> editProduct(produit));

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.getStyleClass().addAll("btn-danger", "btn-small");
        deleteBtn.setOnAction(e -> deleteProduct(produit));

        actionsBox.getChildren().addAll(editBtn, deleteBtn);

        // Add all elements to card
        card.getChildren().addAll(imageView, header, nameLabel, descLabel, priceStockBox, actionsBox);

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
            case "disponible": return "-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;";
            case "épuisé": return "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;";
            case "archive": return "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;";
            default: return "-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;";
        }
    }

    private String getStockStyle(int stock) {
        if (stock == 0) {
            return "-fx-text-fill: #ef4444; -fx-font-weight: bold;";
        } else if (stock < 5) {
            return "-fx-text-fill: #f59e0b; -fx-font-weight: bold;";
        } else {
            return "-fx-text-fill: #10b981; -fx-font-weight: normal;";
        }
    }

    private void showPlaceholderImage(ImageView imageView) {
        // Create a simple placeholder with a pattern
        String placeholderSvg = "data:image/svg+xml;base64," + 
            java.util.Base64.getEncoder().encodeToString(
                ("<svg width='250' height='150' xmlns='http://www.w3.org/2000/svg'>" +
                "<rect width='250' height='150' fill='#f1f5f9' stroke='#d1d5db' stroke-width='2' rx='8'/>" +
                "<text x='125' y='75' text-anchor='middle' font-family='Arial' font-size='16' fill='#6b7280'>No Image</text>" +
                "<circle cx='125' cy='50' r='20' fill='none' stroke='#9ca3af' stroke-width='2'/>" +
                "<line x1='125' y1='60' x2='125' y2='80' stroke='#9ca3af' stroke-width='2'/>" +
                "<line x1='115' y1='70' x2='135' y2='70' stroke='#9ca3af' stroke-width='2'/>" +
                "</svg>").getBytes()
            );
        
        try {
            javafx.scene.image.Image placeholderImage = new javafx.scene.image.Image(placeholderSvg);
            imageView.setImage(placeholderImage);
            imageView.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        } catch (Exception e) {
            // Fallback: create a simple colored rectangle
            imageView.setImage(null);
            imageView.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #d1d5db; -fx-border-radius: 8;");
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

        List<Produit> filteredProducts = allProducts.stream()
                .filter(p -> searchTerm.isEmpty() || 
                         p.getNom().toLowerCase().startsWith(searchTerm) || 
                         p.getDescription().toLowerCase().contains(searchTerm))
                .filter(p -> selectedCategory.equals("Toutes les catégories") || 
                         p.getCategorie().equals(selectedCategory))
                .toList();

        displayProducts(filteredProducts);
        
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
            displayProducts(allProducts);
            showSearchFeedback("", allProducts.size());
        } else {
            List<Produit> filteredProducts = allProducts.stream()
                    .filter(p -> p.getCategorie().equals(category))
                    .toList();
            displayProducts(filteredProducts);
            showSearchFeedback("", filteredProducts.size());
        }
        
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
                node.getStyleClass().remove("category-filter-active");
            }
        }
        
        // Highlight active button
        activeButton.getStyleClass().add("category-filter-active");
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
            URL resource = getClass().getResource("/fxml/product/ProduitDeleteConfirm.fxml");
            if (resource == null) {
                showError("FXML suppression introuvable.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            VBox root = loader.load();
            ProduitDeleteConfirmController c = loader.getController();
            if (c != null) {
                c.setProduit(produit);
                c.setOnDeleted(this::loadProducts);
            }

            Stage stage = new Stage();
            stage.setTitle("Supprimer produit");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            showError("Erreur ouverture suppression: " + e.getMessage());
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
        try {
            // Load the edit form FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product/ProduitEditForm.fxml"));
            VBox editFormRoot = loader.load();
            
            // Get the controller
            ProduitEditFormController editController = loader.getController();
            editController.setProduit(produit);
            editController.setOnProductUpdated(this::loadProducts);
            
            // Create and configure the dialog stage
            Stage editStage = new Stage();
            editStage.setTitle("Modifier: " + produit.getNom());
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.setResizable(true);
            editStage.setMinWidth(1000);
            editStage.setMinHeight(700);
            
            // Create scene
            Scene scene = new Scene(editFormRoot);
            
            // Add CSS if available
            URL css = getClass().getResource("/css/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            
            editStage.setScene(scene);
            editStage.showAndWait();
            
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText("Impossible d'ouvrir le formulaire de modification");
            errorAlert.setContentText("Une erreur est survenue: " + e.getMessage());
            errorAlert.showAndWait();
            e.printStackTrace();
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

    public void setOnClose(Consumer<Void> onClose) {
        this.onClose = onClose;
    }
}
