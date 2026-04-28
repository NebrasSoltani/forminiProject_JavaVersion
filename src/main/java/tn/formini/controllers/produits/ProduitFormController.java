package tn.formini.controllers.produits;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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
    @FXML private VBox validationCard;
    @FXML private VBox imagePreviewContainer;
    @FXML private ImageView imagePreview;

    private MainController mainController;
    private Produit produitToEdit;
    private final ProduitService service = new ProduitService();
    private Consumer<Void> onClose;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        fieldCategorie.setItems(FXCollections.observableArrayList(
                "Informatique", "Scientifique", "Outils intelligents", "Accessoires"
        ));

        fieldStatut.setItems(FXCollections.observableArrayList(
                "disponible", "épuisé", "archive"
        ));

        fieldDateCreation.setValue(LocalDate.now());
        fieldStatut.setValue("disponible");

        fieldImage.textProperty().addListener((obs, oldVal, newVal) -> updateImagePreview(newVal));

        validationCard.setVisible(false);

        setupValidationListeners();
    }

    private void setupValidationListeners() {
        fieldNom.textProperty().addListener((obs, o, n) -> validateNom());
        fieldCategorie.valueProperty().addListener((obs, o, n) -> validateCategorie());
        fieldDescription.textProperty().addListener((obs, o, n) -> validateDescription());
        fieldPrix.textProperty().addListener((obs, o, n) -> validatePrix());
        fieldStock.textProperty().addListener((obs, o, n) -> validateStock());
        fieldDateCreation.valueProperty().addListener((obs, o, n) -> validateDateCreation());
        fieldStatut.valueProperty().addListener((obs, o, n) -> validateStatut());
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void setProduit(Produit produit) {
        this.produitToEdit = produit;
        labelFormTitle.setText("Modifier le Produit");
        btnSave.setText("💾 Mettre à jour");
        populateForm(produit);
    }

    private void populateForm(Produit p) {
        fieldNom.setText(p.getNom());
        fieldCategorie.setValue(p.getCategorie());
        fieldDescription.setText(p.getDescription());
        fieldPrix.setText(p.getPrix().toString());
        fieldStock.setText(String.valueOf(p.getStock()));
        fieldImage.setText(p.getImage());
        fieldStatut.setValue(p.getStatut());

        if (p.getDate_creation() != null) {
            fieldDateCreation.setValue(
                    p.getDate_creation().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
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
            Produit p = (produitToEdit != null) ? produitToEdit : new Produit();

            p.setNom(fieldNom.getText().trim());
            p.setCategorie(fieldCategorie.getValue());
            p.setDescription(fieldDescription.getText().trim());
            p.setPrix(new BigDecimal(fieldPrix.getText().trim()));
            p.setStock(Integer.parseInt(fieldStock.getText().trim()));
            p.setImage(fieldImage.getText().trim());
            p.setStatut(fieldStatut.getValue());
            p.setDate_creation(toDate(fieldDateCreation.getValue()));

            if (produitToEdit == null) {
                service.ajouter(p);
                showSuccess("Produit ajouté !");
            } else {
                service.modifier(p);
                showSuccess("Produit modifié !");
            }

            if (mainController != null) mainController.showProductList();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void browseImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));

        File file = chooser.showOpenDialog(fieldImage.getScene().getWindow());
        if (file != null) {
            fieldImage.setText(file.toURI().toString());
        }
    }

    private void updateImagePreview(String url) {
        try {
            if (url != null && !url.isEmpty()) {
                imagePreview.setImage(new Image(url));
                imagePreviewContainer.setVisible(true);
            } else {
                imagePreviewContainer.setVisible(false);
            }
        } catch (Exception e) {
            imagePreviewContainer.setVisible(false);
        }
    }

    @FXML
    public void goBack() {
        if (mainController != null) mainController.showProductList();
    }

    // ===== VALIDATION =====

    private boolean validateNom() {
        String v = fieldNom.getText().trim();
        if (v.isEmpty()) return showErr(errNom, "Nom obligatoire");
        if (v.length() < 3) return showErr(errNom, "Min 3 caractères");
        return hideErr(errNom);
    }

    private boolean validateCategorie() {
        if (fieldCategorie.getValue() == null)
            return showErr(errCategorie, "Choisir catégorie");
        return hideErr(errCategorie);
    }

    private boolean validateDescription() {
        String v = fieldDescription.getText().trim();
        if (v.length() < 10) return showErr(errDescription, "Min 10 caractères");
        return hideErr(errDescription);
    }

    private boolean validatePrix() {
        try {
            double p = Double.parseDouble(fieldPrix.getText());
            if (p <= 0) return showErr(errPrix, "Prix invalide");
        } catch (Exception e) {
            return showErr(errPrix, "Nombre invalide");
        }
        return hideErr(errPrix);
    }

    private boolean validateStock() {
        try {
            int s = Integer.parseInt(fieldStock.getText());
            if (s < 0) return showErr(errStock, "Stock invalide");
        } catch (Exception e) {
            return showErr(errStock, "Nombre invalide");
        }
        return hideErr(errStock);
    }

    private boolean validateDateCreation() {
        if (fieldDateCreation.getValue() == null)
            return showErr(errDateCreation, "Date obligatoire");
        return hideErr(errDateCreation);
    }

    private boolean validateStatut() {
        if (fieldStatut.getValue() == null)
            return showErr(errStatut, "Statut obligatoire");
        return hideErr(errStatut);
    }

    private boolean validateAll() {
        return validateNom() &
                validateCategorie() &
                validateDescription() &
                validatePrix() &
                validateStock() &
                validateDateCreation() &
                validateStatut();
    }

    private boolean showErr(Label l, String msg) {
        l.setText(msg);
        l.setVisible(true);
        return false;
    }

    private boolean hideErr(Label l) {
        l.setVisible(false);
        return true;
    }

    private void showValidationSummary() {
        validationCard.setVisible(true);
        validationSummary.setText("Veuillez corriger les champs en rouge");
    }

    private Date toDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void showSuccess(String m) {
        new Alert(Alert.AlertType.INFORMATION, m).show();
    }

    private void showError(String m) {
        new Alert(Alert.AlertType.ERROR, m).show();
    }
}