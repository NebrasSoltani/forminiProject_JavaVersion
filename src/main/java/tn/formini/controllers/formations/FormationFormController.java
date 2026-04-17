package tn.formini.controllers.formations;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tn.formini.entities.Users.User;
import tn.formini.entities.formations.Formation;
import tn.formini.services.formations.FormationService;

import java.io.File;
import java.math.BigDecimal;
import java.util.function.UnaryOperator;
import java.util.Date;

public class FormationFormController {

    @FXML private Label titleLabel;
    @FXML private TextField titreField;
    @FXML private TextField categorieField;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private TextField langueField;
    @FXML private TextArea descriptionCourteArea;
    @FXML private TextArea descriptionDetailleeArea;
    @FXML private TextArea objectifsArea;
    @FXML private TextArea prerequisArea;
    @FXML private TextArea programmeArea;
    @FXML private TextField dureeField;
    @FXML private TextField nombreLeconsField;
    @FXML private ComboBox<String> formatCombo;
    @FXML private ComboBox<String> typeAccesCombo;
    @FXML private TextField prixField;
    @FXML private TextField imageField;
    @FXML private ImageView coverPreview;
    @FXML private ComboBox<String> statutCombo;
    @FXML private CheckBox certificatCheck;
    @FXML private CheckBox quizCheck;
    @FXML private CheckBox fichiersCheck;
    @FXML private CheckBox forumCheck;

    private final FormationService formationService = new FormationService();
    private Formation formation;
    private int formateurId;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        niveauCombo.setItems(FXCollections.observableArrayList("debutant", "intermediaire", "avance"));
        formatCombo.setItems(FXCollections.observableArrayList("video", "texte", "mixte", "live"));
        typeAccesCombo.setItems(FXCollections.observableArrayList("gratuit", "payant", "sur_invitation"));
        statutCombo.setItems(FXCollections.observableArrayList("brouillon", "publie", "archive"));

        niveauCombo.setValue("debutant");
        formatCombo.setValue("video");
        typeAccesCombo.setValue("gratuit");
        statutCombo.setValue("brouillon");
        dureeField.setText("1");
        nombreLeconsField.setText("0");
        loadPreviewImage(null);
        installInputConstraints();
    }

    public void setFormateurId(int formateurId) {
        this.formateurId = formateurId;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
        titleLabel.setText("Modifier la formation");
        fillForm(formation);
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void handleSave() {
        try {
            String validationError = validateForm();
            if (validationError != null) {
                showError(validationError);
                return;
            }

            Formation target = (formation == null) ? new Formation() : formation;

            target.setTitre(titreField.getText().trim());
            target.setCategorie(categorieField.getText().trim());
            target.setNiveau(niveauCombo.getValue());
            target.setLangue(langueField.getText().trim());
            target.setDescription_courte(descriptionCourteArea.getText().trim());
            target.setDescription_detaillee(descriptionDetailleeArea.getText().trim());
            target.setObjectifs_pedagogiques(objectifsArea.getText().trim());
            target.setPrerequis(emptyToNull(prerequisArea.getText()));
            target.setProgramme(programmeArea.getText().trim());
            target.setDuree(Integer.parseInt(dureeField.getText().trim()));
            target.setNombre_lecons(Integer.parseInt(nombreLeconsField.getText().trim()));
            target.setFormat(formatCombo.getValue());
            target.setType_acces(typeAccesCombo.getValue());
            target.setPrix(parsePrix(prixField.getText()));
            target.setImage_couverture(normalizeImagePath(imageField.getText()));
            target.setStatut(statutCombo.getValue());
            target.setCertificat(certificatCheck.isSelected());
            target.setHas_quiz(quizCheck.isSelected());
            target.setFichiers_telechargeables(fichiersCheck.isSelected());
            target.setForum(forumCheck.isSelected());

            if (target.getDate_creation() == null) {
                target.setDate_creation(new Date());
            }

            User formateur = new User();
            formateur.setId(formateurId);
            target.setFormateur(formateur);

            target.valider();

            if (formation == null) {
                formationService.ajouter(target);
                showInfo("Formation creee avec succes.");
            } else {
                formationService.modifier(target);
                showInfo("Formation modifiee avec succes.");
            }

            if (onSaved != null) {
                onSaved.run();
            }
            closeWindow();
        } catch (NumberFormatException ex) {
            showError("La duree, le nombre de lecons et le prix doivent etre numeriques.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erreur lors de l'enregistrement: " + ex.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    @FXML
    private void handleChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image de couverture");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        if (titreField.getScene() == null || titreField.getScene().getWindow() == null) {
            return;
        }

        File selected = chooser.showOpenDialog(titreField.getScene().getWindow());
        if (selected != null) {
            imageField.setText(selected.toURI().toString());
            loadPreviewImage(imageField.getText());
        }
    }

    @FXML
    private void handleImageTyped() {
        loadPreviewImage(imageField.getText());
    }

    private void fillForm(Formation f) {
        titreField.setText(f.getTitre());
        categorieField.setText(f.getCategorie());
        niveauCombo.setValue(f.getNiveau());
        langueField.setText(f.getLangue());
        descriptionCourteArea.setText(f.getDescription_courte());
        descriptionDetailleeArea.setText(f.getDescription_detaillee());
        objectifsArea.setText(f.getObjectifs_pedagogiques());
        prerequisArea.setText(f.getPrerequis());
        programmeArea.setText(f.getProgramme());
        dureeField.setText(String.valueOf(f.getDuree()));
        nombreLeconsField.setText(String.valueOf(f.getNombre_lecons()));
        formatCombo.setValue(f.getFormat());
        typeAccesCombo.setValue(f.getType_acces());
        prixField.setText(f.getPrix() == null ? "" : f.getPrix().toPlainString());
        imageField.setText(f.getImage_couverture() == null ? "" : f.getImage_couverture());
        loadPreviewImage(f.getImage_couverture());
        statutCombo.setValue(f.getStatut());
        certificatCheck.setSelected(f.isCertificat());
        quizCheck.setSelected(f.isHas_quiz());
        fichiersCheck.setSelected(f.isFichiers_telechargeables());
        forumCheck.setSelected(f.isForum());
    }

    private BigDecimal parsePrix(String value) {
        String raw = value == null ? "" : value.trim();
        return raw.isEmpty() ? null : new BigDecimal(raw.replace(',', '.'));
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeImagePath(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String validateForm() {
        if (formateurId <= 0) {
            return "Formateur introuvable. Reouvrez l'ecran et reessayez.";
        }

        if (isBlank(titreField.getText())) {
            return "Le titre est obligatoire.";
        }
        if (isBlank(categorieField.getText())) {
            return "La categorie est obligatoire.";
        }
        if (isBlank(langueField.getText())) {
            return "La langue est obligatoire.";
        }
        if (isBlank(descriptionCourteArea.getText())) {
            return "La description courte est obligatoire.";
        }
        if (isBlank(programmeArea.getText())) {
            return "Le programme est obligatoire.";
        }

        Integer duree = parseIntegerSafe(dureeField.getText());
        if (duree == null || duree <= 0) {
            return "La duree doit etre un entier positif.";
        }

        Integer lecons = parseIntegerSafe(nombreLeconsField.getText());
        if (lecons == null || lecons < 0) {
            return "Le nombre de lecons doit etre un entier >= 0.";
        }

        try {
            BigDecimal prix = parsePrix(prixField.getText());
            if (prix != null && prix.compareTo(BigDecimal.ZERO) < 0) {
                return "Le prix ne peut pas etre negatif.";
            }
            if ("payant".equalsIgnoreCase(typeAccesCombo.getValue())
                    && (prix == null || prix.compareTo(BigDecimal.ZERO) <= 0)) {
                return "Pour un acces payant, le prix doit etre superieur a 0.";
            }
        } catch (NumberFormatException ex) {
            return "Le prix est invalide. Utilisez un nombre (ex: 199.99).";
        }

        String imagePath = normalizeImagePath(imageField.getText());
        if (imagePath != null && resolveImagePath(imagePath) == null) {
            return "Le chemin de l'image est invalide ou inaccessible.";
        }
        return null;
    }

    private void installInputConstraints() {
        UnaryOperator<TextFormatter.Change> integerFilter = change ->
                change.getControlNewText().matches("\\d*") ? change : null;
        UnaryOperator<TextFormatter.Change> decimalFilter = change ->
                change.getControlNewText().matches("\\d{0,9}([.,]\\d{0,2})?") ? change : null;

        dureeField.setTextFormatter(new TextFormatter<>(integerFilter));
        nombreLeconsField.setTextFormatter(new TextFormatter<>(integerFilter));
        prixField.setTextFormatter(new TextFormatter<>(decimalFilter));
    }

    private Integer parseIntegerSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void loadPreviewImage(String imagePath) {
        if (coverPreview == null) {
            return;
        }

        Image image = null;
        String resolvedPath = resolveImagePath(imagePath);
        if (resolvedPath != null) {
            try {
                image = new Image(resolvedPath, true);
            } catch (Exception ignored) {
                image = null;
            }
        }

        if (image == null || image.isError()) {
            image = new Image(getClass().getResourceAsStream("/images/no-image-placeholder.png"));
        }
        coverPreview.setImage(image);
    }

    private String resolveImagePath(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            return null;
        }
        String path = rawPath.trim();
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file:")) {
            return path;
        }
        File file = new File(path);
        if (file.exists()) {
            return file.toURI().toString();
        }
        if (path.startsWith("/") && getClass().getResource(path) != null) {
            return getClass().getResource(path).toExternalForm();
        }
        if (getClass().getResource("/" + path) != null) {
            return getClass().getResource("/" + path).toExternalForm();
        }
        return null;
    }

    private void closeWindow() {
        titreField.getScene().getWindow().hide();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

