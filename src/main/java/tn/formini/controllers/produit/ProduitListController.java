package tn.formini.controllers.produit;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
                "Toutes les catégories", "Informatique", "Accessoires", "Scientifique", "Outils Intelligents"
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

    private void displayProducts(List<Produit> products) {
        productsContainer.getChildren().clear();
        
        if (products.isEmpty()) {
            emptyState.setVisible(true);
            return;
        }
        
        emptyState.setVisible(false);
        
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
        card.getChildren().addAll(header, nameLabel, descLabel, priceStockBox, actionsBox);

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
        // Create custom confirmation dialog
        Dialog<ButtonType> deleteDialog = new Dialog<>();
        deleteDialog.setTitle("Confirmation de Suppression");
        deleteDialog.setHeaderText(null);
        
        // Create dialog content
        VBox dialogContent = new VBox(15);
        dialogContent.setStyle("-fx-padding: 20;");
        
        // Product info
        Label productInfo = new Label("Produit à supprimer:");
        productInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #ef4444;");
        
        Label productName = new Label("\"" + produit.getNom() + "\"");
        productName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2937; -fx-padding: 5 0;");
        
        Label productDetails = new Label(
            "Catégorie: " + produit.getCategorie() + "\n" +
            "Prix: " + String.format("%.3f DT", produit.getPrix()) + "\n" +
            "Stock: " + produit.getStock() + " unités"
        );
        productDetails.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        
        // Warning message
        Label warningMessage = new Label("⚠️ Cette action est irréversible !");
        warningMessage.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        
        dialogContent.getChildren().addAll(productInfo, productName, productDetails, warningMessage);
        
        deleteDialog.getDialogPane().setContent(dialogContent);
        
        // Add buttons
        ButtonType deleteButton = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        deleteDialog.getDialogPane().getButtonTypes().addAll(deleteButton, cancelButton);
        
        // Style buttons
        deleteDialog.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                // Perform deletion
                try {
                    service.supprimer(produit.getId());
                    // Show success feedback
                    showDeleteSuccess(produit);
                    loadProducts(); // Reload the list
                } catch (Exception e) {
                    // Show error feedback
                    showDeleteError(produit);
                }
            }
        });
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

    public void setOnClose(Consumer<Void> onClose) {
        this.onClose = onClose;
    }
}
