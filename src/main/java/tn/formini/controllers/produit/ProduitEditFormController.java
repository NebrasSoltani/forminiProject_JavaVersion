package tn.formini.controllers.produit;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;

public class ProduitEditFormController {

    @FXML private Label labelFormTitle;
    @FXML private TextField fieldId;
    @FXML private TextField fieldNom;
    @FXML private ComboBox<String> fieldCategorie;
    @FXML private ComboBox<String> fieldStatut;
    @FXML private TextArea fieldDescription;
    @FXML private TextField fieldPrix;
    @FXML private TextField fieldStock;
    @FXML private DatePicker fieldDateCreation;
    @FXML private TextField fieldImage;
    @FXML private ImageView imagePreview;
    @FXML private Button btnSave;
    @FXML private Label validationSummary;

    // Error labels
    @FXML private Label errNom;
    @FXML private Label errCategorie;
    @FXML private Label errStatut;
    @FXML private Label errDescription;
    @FXML private Label errPrix;
    @FXML private Label errStock;
    @FXML private Label errImage;

    private Produit produit;
    private ProduitService produitService;
    private Consumer<Void> onCloseCallback;
    private Runnable onProductUpdated;

    @FXML
    public void initialize() {
        produitService = new ProduitService();
        
        // Initialize categories
        fieldCategorie.getItems().addAll(
            "Informatique", "Scientifique", "Accessoires", "Outils intelligents"
        );
        
        // Initialize statuts
        fieldStatut.getItems().addAll(
            "disponible", "épuisé", "archive"
        );
        
        // Setup validation listeners
        setupValidationListeners();
        
        // Initialize validation state
        clearAllValidationErrors();
    }
    
    private void setupValidationListeners() {
        // Name validation
        fieldNom.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG: Name field changed from '" + oldVal + "' to '" + newVal + "'");
            validateNom();
        });
        
        // Category validation
        fieldCategorie.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateCategorie();
        });
        
        // Description validation
        fieldDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            validateDescription();
        });
        
        // Price validation
        fieldPrix.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG: Price field changed from '" + oldVal + "' to '" + newVal + "'");
            validatePrix();
        });
        
        // Stock validation
        fieldStock.textProperty().addListener((obs, oldVal, newVal) -> {
            validateStock();
        });
        
        // Date validation
        fieldDateCreation.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDateCreation();
        });
        
        // Status validation
        fieldStatut.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateStatut();
        });
        
        // Image validation
        fieldImage.textProperty().addListener((obs, oldVal, newVal) -> {
            validateImage();
        });
    }
    
    // ===== VALIDATION METHODS =====
    
    private boolean validateNom() {
        String nom = fieldNom.getText().trim();
        System.out.println("DEBUG: validateNom() called with value: '" + nom + "'");
        
        // Clear previous error
        clearError(errNom);
        
        if (nom.isEmpty()) {
            System.out.println("DEBUG: Name is empty, showing error");
            showValidationError(errNom, "Le nom du produit est obligatoire.");
            return false;
        }
        if (nom.length() < 3) {
            showValidationError(errNom, "Le nom doit contenir au moins 3 caractères.");
            return false;
        }
        if (nom.length() > 255) {
            showValidationError(errNom, "Le nom ne doit pas dépasser 255 caractères.");
            return false;
        }
        // Check for valid characters (letters, numbers, spaces, hyphens, underscores, apostrophes)
        if (!nom.matches("^[a-zA-Z0-9\\s\\-_\\'àâäéèêëïîôöùûüçÀÂÄÉÈÊËÏÎÔÖÙÛÜÇ]+$")) {
            showValidationError(errNom, "Le nom ne peut contenir que des lettres, chiffres, espaces, tirets, underscores et apostrophes.");
            return false;
        }
        // Check for consecutive spaces
        if (nom.contains("  ")) {
            showValidationError(errNom, "Le nom ne peut pas contenir d'espaces consécutifs.");
            return false;
        }
        // Check for repeated characters (spam prevention)
        if (nom.matches("^(.)\\1{4,}$")) {
            showValidationError(errNom, "Le nom semble invalide (caractères répétés).");
            return false;
        }
        
        return true;
    }
    
    private boolean validateCategorie() {
        String categorie = fieldCategorie.getValue();
        
        clearError(errCategorie);
        
        if (categorie == null || categorie.trim().isEmpty()) {
            showValidationError(errCategorie, "La catégorie est obligatoire.");
            return false;
        }
        if (categorie.length() < 2) {
            showValidationError(errCategorie, "La catégorie doit contenir au moins 2 caractères.");
            return false;
        }
        if (categorie.length() > 100) {
            showValidationError(errCategorie, "La catégorie ne doit pas dépasser 100 caractères.");
            return false;
        }
        
        return true;
    }
    
    private boolean validateDescription() {
        String description = fieldDescription.getText().trim();
        
        clearError(errDescription);
        
        if (description.isEmpty()) {
            showValidationError(errDescription, "La description est obligatoire.");
            return false;
        }
        if (description.length() < 10) {
            showValidationError(errDescription, "La description doit contenir au moins 10 caractères.");
            return false;
        }
        if (description.length() > 2000) {
            showValidationError(errDescription, "La description ne doit pas dépasser 2000 caractères.");
            return false;
        }
        // Check for reasonable content
        if (description.matches("^(.)\\1{5,}$")) {
            showValidationError(errDescription, "La description semble invalide (caractères répétés).");
            return false;
        }
        
        return true;
    }
    
    private boolean validatePrix() {
        String prixStr = fieldPrix.getText().trim();
        System.out.println("DEBUG: validatePrix() called with value: '" + prixStr + "'");
        
        clearError(errPrix);
        
        if (prixStr.isEmpty()) {
            System.out.println("DEBUG: Price is empty, showing error");
            showValidationError(errPrix, "Le prix est obligatoire.");
            return false;
        }
        
        // Check for valid number format (prevent multiple dots, commas, etc.)
        if (!prixStr.matches("^\\d+(\\.\\d{1,2})?$")) {
            showValidationError(errPrix, "Format de prix invalide. Utilisez: 25.99");
            return false;
        }
        
        try {
            double prix = Double.parseDouble(prixStr);
            if (prix <= 0) {
                showValidationError(errPrix, "Le prix doit être strictement positif.");
                return false;
            }
            if (prix < 0.01) {
                showValidationError(errPrix, "Le prix minimum est de 0.01 DT.");
                return false;
            }
            if (prix > 999999.99) {
                showValidationError(errPrix, "Le prix ne peut pas dépasser 999,999.99 DT.");
                return false;
            }
            // Check for reasonable decimal places
            if (prixStr.contains(".") && prixStr.split("\\.")[1].length() > 2) {
                showValidationError(errPrix, "Le prix ne peut pas avoir plus de 2 décimales.");
                return false;
            }
            // Check for unrealistic prices (business logic)
            if (prix > 10000 && prixStr.length() < 5) {
                showValidationError(errPrix, "Vérifiez le prix saisi (semble trop élevé).");
                return false;
            }
            
        } catch (NumberFormatException e) {
            showValidationError(errPrix, "Le prix doit être un nombre valide (ex: 25.99).");
            return false;
        }
        
        return true;
    }
    
    private boolean validateStock() {
        String stockStr = fieldStock.getText().trim();
        
        clearError(errStock);
        
        if (stockStr.isEmpty()) {
            showValidationError(errStock, "La quantité en stock est obligatoire.");
            return false;
        }
        
        // Check for valid integer format (prevent decimals, letters, etc.)
        if (!stockStr.matches("^\\d+$")) {
            showValidationError(errStock, "La quantité doit être un nombre entier positif.");
            return false;
        }
        
        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                showValidationError(errStock, "La quantité en stock ne peut pas être négative.");
                return false;
            }
            // Check for reasonable stock limits
            if (stock > 1000000) {
                showValidationError(errStock, "La quantité ne peut pas dépasser 1,000,000 unités.");
                return false;
            }
            // Warning for high stock (business logic)
            if (stock > 50000) {
                showValidationError(errStock, "Attention: très grande quantité en stock.");
                return false;
            }
            // Check for unrealistic single-digit stock with leading zeros
            if (stockStr.length() > 1 && stockStr.startsWith("0")) {
                showValidationError(errStock, "Format invalide: pas de zéros au début.");
                return false;
            }
            
        } catch (NumberFormatException e) {
            showValidationError(errStock, "La quantité doit être un nombre entier valide.");
            return false;
        }
        
        return true;
    }
    
    private boolean validateDateCreation() {
        // Date validation for modification form is optional
        return true;
    }
    
    private boolean validateImage() {
        String imageUrl = fieldImage.getText().trim();
        
        // Image is optional, but if provided, must be valid
        if (imageUrl.isEmpty()) {
            return true; // Image is optional
        }
        
        // Check for valid URL format
        if (!isValidImageUrl(imageUrl)) {
            showValidationError(errImage, "Format d'URL d'image invalide.");
            return false;
        }
        
        // Check for supported image formats
        if (!isSupportedImageFormat(imageUrl)) {
            showValidationError(errImage, "Format d'image non supporté. Utilisez: JPG, PNG, GIF, WebP");
            return false;
        }
        
        // Check for reasonable URL length
        if (imageUrl.length() > 2048) {
            showValidationError(errImage, "URL d'image trop longue (max 2048 caractères).");
            return false;
        }
        
        // Check for suspicious patterns
        if (containsSuspiciousPatterns(imageUrl)) {
            showValidationError(errImage, "URL d'image suspecte ou non sécurisée.");
            return false;
        }
        
        return true;
    }
    
    private boolean isValidImageUrl(String url) {
        // Check for valid URL patterns (HTTP/HTTPS, file, data URIs)
        return url.matches("^(https?://|file:/|data:image/).*") || 
               url.matches("^[a-zA-Z]:\\\\.*") || // Windows file path
               url.matches("^/.*"); // Unix file path
    }
    
    private boolean isSupportedImageFormat(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.matches(".*\\.(jpg|jpeg|png|gif|webp|bmp|svg)(\\?.*)?$") ||
               lowerUrl.matches("data:image/(jpg|jpeg|png|gif|webp|bmp|svg);.*");
    }
    
    private boolean containsSuspiciousPatterns(String url) {
        String lowerUrl = url.toLowerCase();
        // Check for suspicious patterns
        return lowerUrl.contains("javascript:") ||
               lowerUrl.contains("<script") ||
               lowerUrl.contains("data:text/html") ||
               lowerUrl.contains("..") || // Directory traversal
               lowerUrl.contains("eval(") ||
               lowerUrl.contains("document.cookie");
    }
    
    private boolean validateStatut() {
        String statut = fieldStatut.getValue();
        
        clearError(errStatut);
        
        if (statut == null || statut.trim().isEmpty()) {
            showValidationError(errStatut, "Le statut est obligatoire.");
            return false;
        }
        
        return true;
    }
    
    private boolean validateAll() {
        System.out.println("DEBUG: validateAll() called");
        boolean isValid = true;
        isValid &= validateNom();
        System.out.println("DEBUG: validateNom() result: " + isValid);
        isValid &= validateCategorie();
        System.out.println("DEBUG: validateCategorie() result: " + isValid);
        isValid &= validateDescription();
        System.out.println("DEBUG: validateDescription() result: " + isValid);
        isValid &= validatePrix();
        System.out.println("DEBUG: validatePrix() result: " + isValid);
        isValid &= validateStock();
        System.out.println("DEBUG: validateStock() result: " + isValid);
        isValid &= validateImage();
        System.out.println("DEBUG: validateImage() result: " + isValid);
        isValid &= validateStatut();
        System.out.println("DEBUG: validateStatut() result: " + isValid);
        System.out.println("DEBUG: Final validation result: " + isValid);
        return isValid;
    }
    
    private void clearError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }
    
    private void showValidationError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void showValidationSummary() {
        StringBuilder errors = new StringBuilder("Veuillez corriger les erreurs suivantes:\n");
        
        if (errNom.isVisible()) errors.append("• ").append(errNom.getText()).append("\n");
        if (errCategorie.isVisible()) errors.append("• ").append(errCategorie.getText()).append("\n");
        if (errDescription.isVisible()) errors.append("• ").append(errDescription.getText()).append("\n");
        if (errPrix.isVisible()) errors.append("• ").append(errPrix.getText()).append("\n");
        if (errStock.isVisible()) errors.append("• ").append(errStock.getText()).append("\n");
        if (errImage != null && errImage.isVisible()) errors.append("• ").append(errImage.getText()).append("\n");
        if (errStatut.isVisible()) errors.append("• ").append(errStatut.getText()).append("\n");
        
        validationSummary.setText(errors.toString());
        validationSummary.setVisible(true);
    }
    
    private void clearAllValidationErrors() {
        clearError(errNom);
        clearError(errCategorie);
        clearError(errDescription);
        clearError(errPrix);
        clearError(errStock);
        if (errImage != null) clearError(errImage);
        clearError(errStatut);
        validationSummary.setVisible(false);
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit != null) {
            populateFields();
        }
    }

    private void populateFields() {
        fieldId.setText(String.valueOf(produit.getId()));
        fieldNom.setText(produit.getNom());
        fieldCategorie.setValue(produit.getCategorie());
        fieldStatut.setValue(produit.getStatut());
        fieldDescription.setText(produit.getDescription());
        fieldPrix.setText(produit.getPrix().toString());
        fieldStock.setText(String.valueOf(produit.getStock()));
        fieldImage.setText(produit.getImage());
        
        // Initialize image preview
        updateImagePreview(produit.getImage());
        
        // Convert Date to LocalDate for DatePicker
        if (produit.getDate_creation() != null) {
            fieldDateCreation.setValue(produit.getDate_creation().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());
        }
        
        labelFormTitle.setText("Modifier: " + produit.getNom());
    }

    @FXML
    public void updateProduit() {
        System.out.println("DEBUG: updateProduit() called - SAVE BUTTON PRESSED!");
        
        // Simple test to make sure the method is called
        showAlert(Alert.AlertType.INFORMATION, "Test", "Save button was pressed!");
        
        if (!validateAll()) {
            System.out.println("DEBUG: Validation failed, showing summary");
            showValidationSummary();
            return;
        }

        try {
            System.out.println("DEBUG: Validation passed, updating product");
            
            // Update product with new values
            produit.setNom(fieldNom.getText().trim());
            produit.setCategorie(fieldCategorie.getValue());
            produit.setStatut(fieldStatut.getValue());
            produit.setDescription(fieldDescription.getText().trim());
            produit.setPrix(new BigDecimal(fieldPrix.getText().trim()));
            produit.setStock(Integer.parseInt(fieldStock.getText().trim()));
            produit.setImage(fieldImage.getText().trim());
            
            // Set creation date if not null
            if (fieldDateCreation.getValue() != null) {
                produit.setDate_creation(Date.from(fieldDateCreation.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            System.out.println("DEBUG: Product data updated: " + produit.getNom() + ", " + produit.getPrix() + ", " + produit.getStock());

            // Update in database
            produitService.modifier(produit);
            System.out.println("DEBUG: Database update completed");

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès!");
            
            if (onProductUpdated != null) {
                System.out.println("DEBUG: Calling onProductUpdated callback");
                onProductUpdated.run();
            }
            
            close();

        } catch (NumberFormatException e) {
            System.err.println("ERROR: NumberFormatException in updateProduit: " + e.getMessage());
            showError("Format invalide pour le prix ou le stock.");
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: IllegalArgumentException in updateProduit: " + e.getMessage());
            showError(e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR: Exception in updateProduit: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    private void resetForm() {
        if (produit != null) {
            populateFields();
        }
        clearAllErrors();
    }

    @FXML
    public void duplicateProduct() {
        if (!validateForm()) {
            return;
        }

        try {
            Produit newProduit = new Produit();
            newProduit.setNom(fieldNom.getText().trim() + " (Copie)");
            newProduit.setCategorie(fieldCategorie.getValue());
            newProduit.setStatut("disponible");
            newProduit.setDescription(fieldDescription.getText().trim());
            newProduit.setPrix(new BigDecimal(fieldPrix.getText().trim()));
            newProduit.setStock(Integer.parseInt(fieldStock.getText().trim()));
            newProduit.setImage(fieldImage.getText().trim());
            newProduit.setDate_creation(new Date());

            newProduit.valider();
            produitService.ajouter(newProduit);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit dupliqué avec succès!");
            
            if (onProductUpdated != null) {
                onProductUpdated.run();
            }

        } catch (Exception e) {
            showError("Erreur lors de la duplication: " + e.getMessage());
        }
    }

    @FXML
    public void deleteProduct() {
        if (produit == null) return;
        openDeleteConfirmModal();
    }

    @FXML
    private void previewImage() {
        String imageUrl = fieldImage.getText().trim();
        if (imageUrl == null || imageUrl.isEmpty()) return;

        ImageView iv = new ImageView();
        iv.setFitWidth(720);
        iv.setFitHeight(420);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        try {
            iv.setImage(new Image(imageUrl, true));
        } catch (Exception ignored) {}

        VBox content = new VBox(10, new Label("Aperçu image"), iv, new Label(imageUrl));
        content.setStyle("-fx-padding: 12;");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aperçu image");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(content);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private void openDeleteConfirmModal() {
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
                c.setOnDeleted(() -> {
                    if (onProductUpdated != null) onProductUpdated.run();
                });
            }

            Stage stage = new Stage();
            stage.setTitle("Supprimer produit");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            // If deleted, close edit window too (product no longer exists)
            // We detect it by checking if it still has an id and the callback ran; simplest: always refresh list, keep form open.
            // User can close manually; or we can close if delete button was used. We'll keep current window open for safety.
        } catch (Exception e) {
            showError("Erreur ouverture suppression: " + e.getMessage());
        }
    }

    @FXML
    private void close() {
        if (onCloseCallback != null) {
            onCloseCallback.accept(null);
        }
        
        Stage stage = (Stage) fieldNom.getScene().getWindow();
        stage.close();
    }

    private boolean validateForm() {
        boolean isValid = true;
        clearAllErrors();

        // Validate name
        String nom = fieldNom.getText().trim();
        if (nom.isEmpty()) {
            showValidationError(errNom, "Le nom est obligatoire");
            isValid = false;
        } else if (nom.length() > 255) {
            showValidationError(errNom, "Le nom ne doit pas dépasser 255 caractères");
            isValid = false;
        }

        // Validate category
        if (fieldCategorie.getValue() == null || fieldCategorie.getValue().trim().isEmpty()) {
            showValidationError(errCategorie, "La catégorie est obligatoire");
            isValid = false;
        }

        // Validate status
        if (fieldStatut.getValue() == null || fieldStatut.getValue().trim().isEmpty()) {
            showValidationError(errStatut, "Le statut est obligatoire");
            isValid = false;
        }

        // Validate description
        String description = fieldDescription.getText().trim();
        if (description.isEmpty()) {
            showValidationError(errDescription, "La description est obligatoire");
            isValid = false;
        }

        // Validate price
        try {
            String prixText = fieldPrix.getText().trim();
            if (prixText.isEmpty()) {
                showValidationError(errPrix, "Le prix est obligatoire");
                isValid = false;
            } else {
                BigDecimal prix = new BigDecimal(prixText);
                if (prix.compareTo(BigDecimal.ZERO) < 0) {
                    showValidationError(errPrix, "Le prix ne peut pas être négatif");
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            showValidationError(errPrix, "Format de prix invalide");
            isValid = false;
        }

        // Validate stock
        try {
            String stockText = fieldStock.getText().trim();
            if (stockText.isEmpty()) {
                showValidationError(errStock, "Le stock est obligatoire");
                isValid = false;
            } else {
                int stock = Integer.parseInt(stockText);
                if (stock < 0) {
                    showValidationError(errStock, "Le stock ne peut pas être négatif");
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            showValidationError(errStock, "Format de stock invalide");
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearAllErrors() {
        clearError(errNom);
        clearError(errCategorie);
        clearError(errStatut);
        clearError(errDescription);
        clearError(errPrix);
        clearError(errStock);
        validationSummary.setVisible(false);
    }

    private void showError(String message) {
        validationSummary.setText(message);
        validationSummary.setVisible(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Setters for callbacks
    public void setOnCloseCallback(Consumer<Void> onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
    }

    public void setOnProductUpdated(Runnable onProductUpdated) {
        this.onProductUpdated = onProductUpdated;
    }

    // Image Management Methods
    @FXML
    public void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("GIF", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(fieldImage.getScene().getWindow());
        if (selectedFile != null) {
            String imageUrl = selectedFile.toURI().toString();
            fieldImage.setText(imageUrl);
            updateImagePreview(imageUrl);
        }
    }

    @FXML
    public void updateInlinePreview() {
        String imageUrl = fieldImage.getText().trim();
        if (imageUrl.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez entrer une URL d'image ou choisir un fichier.");
            return;
        }
        
        updateImagePreview(imageUrl);
    }

    @FXML
    public void clearImage() {
        fieldImage.setText("");
        imagePreview.setImage(null);
        // Set placeholder image
        try {
            Image placeholder = new Image("/images/no-image-placeholder.png", true);
            imagePreview.setImage(placeholder);
        } catch (Exception e) {
            // If placeholder fails, just clear the image
            imagePreview.setImage(null);
        }
    }

    @FXML
    public void setDefaultImage() {
        String defaultImageUrl = "/images/default-product.png";
        fieldImage.setText(defaultImageUrl);
        updateImagePreview(defaultImageUrl);
    }

    private void updateImagePreview(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            clearImage();
            return;
        }
        
        try {
            Image image = new Image(imageUrl, true);
            imagePreview.setImage(image);
            
            // Handle loading errors
            image.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger l'image: " + imageUrl);
                    clearImage();
                }
            });
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger l'image: " + imageUrl);
            clearImage();
        }
    }
}
