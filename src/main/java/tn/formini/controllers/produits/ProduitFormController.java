package tn.formini.controllers.produits;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tn.formini.controllers.MainController;
import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ProduitFormController implements Initializable {

    @FXML private Label labelFormTitle;
    @FXML private TextField fieldNom;
    @FXML private ComboBox<String> fieldCategorie;
    @FXML private TextArea fieldDescription;
    @FXML private TextField fieldPrix;
    @FXML private TextField fieldStock;
    @FXML private TextField fieldImage;
    @FXML private DatePicker fieldDateCreation;
    @FXML private ComboBox<String> fieldStatut;
    @FXML private Button btnSave;

    @FXML private Label errNom;
    @FXML private Label errCategorie;
    @FXML private Label errDescription;
    @FXML private Label errPrix;
    @FXML private Label errStock;
    @FXML private Label errDateCreation;
    @FXML private Label errStatut;
    @FXML private Label validationSummary;

    private MainController mainController;
    private Produit produitToEdit;
    private final ProduitService service = new ProduitService();
    private Consumer<Void> onClose;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize categories
        fieldCategorie.setItems(FXCollections.observableArrayList(
                "Informatique", "Électronique", "Électroménager", "Vêtements", 
                "Alimentation", "Livres", "Sports", "Maison", "Jouets", "Autre"
        ));

        // Initialize statuts
        fieldStatut.setItems(FXCollections.observableArrayList(
                "disponible", "épuisé", "archive"
        ));

        // Set default values
        fieldDateCreation.setValue(LocalDate.now());
        fieldStatut.setValue("disponible");

        // Add real-time validation listeners
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        // Name validation
        fieldNom.textProperty().addListener((obs, oldVal, newVal) -> {
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

        // Statut validation
        fieldStatut.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateStatut();
        });
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void setProduit(Produit produit) {
        this.produitToEdit = produit;
        labelFormTitle.setText("Modifier le Produit");
        btnSave.setText("💾 Mettre à jour le produit");
        populateForm(produit);
    }

    private void populateForm(Produit produit) {
        fieldNom.setText(produit.getNom());
        fieldCategorie.setValue(produit.getCategorie());
        fieldDescription.setText(produit.getDescription());
        fieldPrix.setText(produit.getPrix().toString());
        fieldStock.setText(String.valueOf(produit.getStock()));
        fieldImage.setText(produit.getImage());
        fieldStatut.setValue(produit.getStatut());
        
        if (produit.getDate_creation() != null) {
            fieldDateCreation.setValue(
                produit.getDate_creation().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate()
            );
        }
    }

    @FXML
    public void saveProduit() {
        if (!validateAll()) {
            showValidationSummary();
            return;
        }

        try {
            Produit produit = (produitToEdit != null) ? produitToEdit : new Produit();
            
            produit.setNom(fieldNom.getText().trim());
            produit.setCategorie(fieldCategorie.getValue());
            produit.setDescription(fieldDescription.getText().trim());
            produit.setPrix(new BigDecimal(fieldPrix.getText().trim()));
            produit.setStock(Integer.parseInt(fieldStock.getText().trim()));
            produit.setImage(fieldImage.getText().trim());
            produit.setStatut(fieldStatut.getValue());
            produit.setDate_creation(toDate(fieldDateCreation.getValue()));

            // Validate the entity
            produit.valider();

            if (produitToEdit == null) {
                service.ajouter(produit);
                showSuccess("Produit ajouté avec succès !");
            } else {
                service.modifier(produit);
                showSuccess("Produit mis à jour avec succès !");
            }

            // Go back to product list
            if (mainController != null) {
                mainController.showProductList();
            }

        } catch (NumberFormatException e) {
            showError("Erreur de format: Vérifiez que le prix et la quantité sont des nombres valides.");
        } catch (IllegalArgumentException e) {
            showError("Erreur de validation: " + e.getMessage());
        } catch (Exception e) {
            showError("Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    @FXML
    public void browseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        File file = chooser.showOpenDialog(fieldImage.getScene().getWindow());
        if (file != null) {
            fieldImage.setText(file.toURI().toString());
        }
    }

    @FXML
    public void goBack() {
        if (onClose != null) {
            onClose.accept(null);
        } else if (mainController != null) {
            mainController.showProductList();
        }
    }

    // Validation methods
    private boolean validateNom() {
        String nom = fieldNom.getText().trim();
        if (nom.isEmpty()) {
            errNom.setText("Le nom du produit est obligatoire.");
            errNom.setVisible(true);
            return false;
        }
        if (nom.length() < 3) {
            errNom.setText("Le nom doit contenir au moins 3 caractères.");
            errNom.setVisible(true);
            return false;
        }
        if (nom.length() > 255) {
            errNom.setText("Le nom ne doit pas dépasser 255 caractères.");
            errNom.setVisible(true);
            return false;
        }
        errNom.setVisible(false);
        return true;
    }

    private boolean validateCategorie() {
        String categorie = fieldCategorie.getValue();
        if (categorie == null || categorie.trim().isEmpty()) {
            errCategorie.setText("La catégorie est obligatoire.");
            errCategorie.setVisible(true);
            return false;
        }
        errCategorie.setVisible(false);
        return true;
    }

    private boolean validateDescription() {
        String description = fieldDescription.getText().trim();
        if (description.isEmpty()) {
            errDescription.setText("La description est obligatoire.");
            errDescription.setVisible(true);
            return false;
        }
        if (description.length() < 10) {
            errDescription.setText("La description doit contenir au moins 10 caractères.");
            errDescription.setVisible(true);
            return false;
        }
        if (description.length() > 2000) {
            errDescription.setText("La description ne doit pas dépasser 2000 caractères.");
            errDescription.setVisible(true);
            return false;
        }
        errDescription.setVisible(false);
        return true;
    }

    private boolean validatePrix() {
        String prixStr = fieldPrix.getText().trim();
        if (prixStr.isEmpty()) {
            errPrix.setText("Le prix est obligatoire.");
            errPrix.setVisible(true);
            return false;
        }
        try {
            BigDecimal prix = new BigDecimal(prixStr);
            if (prix.compareTo(BigDecimal.ZERO) <= 0) {
                errPrix.setText("Le prix doit être strictement positif.");
                errPrix.setVisible(true);
                return false;
            }
            if (prix.compareTo(new BigDecimal("999999.99")) > 0) {
                errPrix.setText("Le prix ne peut pas dépasser 999999.99€.");
                errPrix.setVisible(true);
                return false;
            }
        } catch (NumberFormatException e) {
            errPrix.setText("Le prix doit être un nombre valide (ex: 19.99).");
            errPrix.setVisible(true);
            return false;
        }
        errPrix.setVisible(false);
        return true;
    }

    private boolean validateStock() {
        String stockStr = fieldStock.getText().trim();
        if (stockStr.isEmpty()) {
            errStock.setText("La quantité en stock est obligatoire.");
            errStock.setVisible(true);
            return false;
        }
        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                errStock.setText("La quantité en stock ne peut pas être négative.");
                errStock.setVisible(true);
                return false;
            }
            if (stock > 999999) {
                errStock.setText("La quantité en stock ne peut pas dépasser 999999.");
                errStock.setVisible(true);
                return false;
            }
        } catch (NumberFormatException e) {
            errStock.setText("La quantité doit être un nombre entier.");
            errStock.setVisible(true);
            return false;
        }
        errStock.setVisible(false);
        return true;
    }

    private boolean validateDateCreation() {
        LocalDate date = fieldDateCreation.getValue();
        if (date == null) {
            errDateCreation.setText("La date de création est obligatoire.");
            errDateCreation.setVisible(true);
            return false;
        }
        if (date.isAfter(LocalDate.now())) {
            errDateCreation.setText("La date de création ne peut pas être dans le futur.");
            errDateCreation.setVisible(true);
            return false;
        }
        errDateCreation.setVisible(false);
        return true;
    }

    private boolean validateStatut() {
        String statut = fieldStatut.getValue();
        if (statut == null || statut.trim().isEmpty()) {
            errStatut.setText("Le statut est obligatoire.");
            errStatut.setVisible(true);
            return false;
        }
        errStatut.setVisible(false);
        return true;
    }

    private boolean validateAll() {
        boolean isValid = true;
        isValid &= validateNom();
        isValid &= validateCategorie();
        isValid &= validateDescription();
        isValid &= validatePrix();
        isValid &= validateStock();
        isValid &= validateDateCreation();
        isValid &= validateStatut();
        return isValid;
    }

    private void showValidationSummary() {
        StringBuilder errors = new StringBuilder("Veuillez corriger les erreurs suivantes:\n");
        
        if (errNom.isVisible()) errors.append("• ").append(errNom.getText()).append("\n");
        if (errCategorie.isVisible()) errors.append("• ").append(errCategorie.getText()).append("\n");
        if (errDescription.isVisible()) errors.append("• ").append(errDescription.getText()).append("\n");
        if (errPrix.isVisible()) errors.append("• ").append(errPrix.getText()).append("\n");
        if (errStock.isVisible()) errors.append("• ").append(errStock.getText()).append("\n");
        if (errDateCreation.isVisible()) errors.append("• ").append(errDateCreation.getText()).append("\n");
        if (errStatut.isVisible()) errors.append("• ").append(errStatut.getText()).append("\n");
        
        validationSummary.setText(errors.toString());
        validationSummary.setVisible(true);
    }

    private Date toDate(LocalDate localDate) {
        return localDate == null ? null
                : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public void setOnClose(Consumer<Void> onClose) {
        this.onClose = onClose;
    }
}
