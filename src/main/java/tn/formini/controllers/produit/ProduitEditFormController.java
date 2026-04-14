package tn.formini.controllers.produit;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.math.BigDecimal;
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
    @FXML private Button btnSave;
    @FXML private Label validationSummary;

    // Error labels
    @FXML private Label errNom;
    @FXML private Label errCategorie;
    @FXML private Label errStatut;
    @FXML private Label errDescription;
    @FXML private Label errPrix;
    @FXML private Label errStock;

    private Produit produit;
    private ProduitService produitService;
    private Consumer<Void> onCloseCallback;
    private Runnable onProductUpdated;

    @FXML
    public void initialize() {
        produitService = new ProduitService();
        
        // Initialize categories
        fieldCategorie.getItems().addAll(
            "Électronique", "Vêtements", "Alimentation", "Mobilier", 
            "Livres", "Sports", "Jouets", "Beauté", "Automobile", "Autre"
        );
        
        // Initialize statuses
        fieldStatut.getItems().addAll("disponible", "épuisé", "archivé");
        
        // Add listeners for real-time validation
        fieldNom.textProperty().addListener((obs, oldVal, newVal) -> clearError(errNom));
        fieldCategorie.valueProperty().addListener((obs, oldVal, newVal) -> clearError(errCategorie));
        fieldStatut.valueProperty().addListener((obs, oldVal, newVal) -> clearError(errStatut));
        fieldDescription.textProperty().addListener((obs, oldVal, newVal) -> clearError(errDescription));
        fieldPrix.textProperty().addListener((obs, oldVal, newVal) -> clearError(errPrix));
        fieldStock.textProperty().addListener((obs, oldVal, newVal) -> clearError(errStock));
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
        
        // Convert Date to LocalDate for DatePicker
        if (produit.getDate_creation() != null) {
            fieldDateCreation.setValue(produit.getDate_creation().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());
        }
        
        labelFormTitle.setText("Modifier: " + produit.getNom());
    }

    @FXML
    private void updateProduit() {
        if (!validateForm()) {
            return;
        }

        try {
            // Update product with new values
            produit.setNom(fieldNom.getText().trim());
            produit.setCategorie(fieldCategorie.getValue());
            produit.setStatut(fieldStatut.getValue());
            produit.setDescription(fieldDescription.getText().trim());
            produit.setPrix(new BigDecimal(fieldPrix.getText().trim()));
            produit.setStock(Integer.parseInt(fieldStock.getText().trim()));
            produit.setImage(fieldImage.getText().trim());

            // Validate the product
            produit.valider();

            // Update in database
            produitService.modifier(produit);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès!");
            
            if (onProductUpdated != null) {
                onProductUpdated.run();
            }
            
            close();

        } catch (NumberFormatException e) {
            showError("Format invalide pour le prix ou le stock.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
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
    private void duplicateProduct() {
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
    private void deleteProduct() {
        if (produit == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Voulez-vous vraiment supprimer ce produit?");
        confirm.setContentText("Cette action est irréversible.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                produitService.supprimer(produit.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès!");
                
                if (onProductUpdated != null) {
                    onProductUpdated.run();
                }
                
                close();
            } catch (Exception e) {
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void previewImage() {
        String imageUrl = fieldImage.getText().trim();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // You could implement an image preview dialog here
            showAlert(Alert.AlertType.INFORMATION, "Aperçu", "URL de l'image: " + imageUrl);
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
            showError(errNom, "Le nom est obligatoire");
            isValid = false;
        } else if (nom.length() > 255) {
            showError(errNom, "Le nom ne doit pas dépasser 255 caractères");
            isValid = false;
        }

        // Validate category
        if (fieldCategorie.getValue() == null || fieldCategorie.getValue().trim().isEmpty()) {
            showError(errCategorie, "La catégorie est obligatoire");
            isValid = false;
        }

        // Validate status
        if (fieldStatut.getValue() == null || fieldStatut.getValue().trim().isEmpty()) {
            showError(errStatut, "Le statut est obligatoire");
            isValid = false;
        }

        // Validate description
        String description = fieldDescription.getText().trim();
        if (description.isEmpty()) {
            showError(errDescription, "La description est obligatoire");
            isValid = false;
        }

        // Validate price
        try {
            String prixText = fieldPrix.getText().trim();
            if (prixText.isEmpty()) {
                showError(errPrix, "Le prix est obligatoire");
                isValid = false;
            } else {
                BigDecimal prix = new BigDecimal(prixText);
                if (prix.compareTo(BigDecimal.ZERO) < 0) {
                    showError(errPrix, "Le prix ne peut pas être négatif");
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            showError(errPrix, "Format de prix invalide");
            isValid = false;
        }

        // Validate stock
        try {
            String stockText = fieldStock.getText().trim();
            if (stockText.isEmpty()) {
                showError(errStock, "Le stock est obligatoire");
                isValid = false;
            } else {
                int stock = Integer.parseInt(stockText);
                if (stock < 0) {
                    showError(errStock, "Le stock ne peut pas être négatif");
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            showError(errStock, "Format de stock invalide");
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setText("");
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
}
