package tn.formini.controllers.formations;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.formini.entities.formations.Formation;
import tn.formini.entities.formations.Lecon;
import tn.formini.services.formations.LeconService;

import java.net.URI;
import java.util.function.UnaryOperator;

public class LeconFormController {

    @FXML private Label titleLabel;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea contenuArea;
    @FXML private TextField ordreField;
    @FXML private TextField dureeField;
    @FXML private TextField videoUrlField;
    @FXML private TextField fichierField;
    @FXML private CheckBox gratuitCheck;

    private final LeconService leconService = new LeconService();
    private Lecon lecon;
    private Formation formation;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        installInputConstraints();
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    public void setLecon(Lecon lecon) {
        this.lecon = lecon;
        titleLabel.setText("Modifier la lecon");
        fillForm(lecon);
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void handleSave() {
        if (formation == null) {
            showError("Formation introuvable.");
            return;
        }

        try {
            String validationError = validateForm();
            if (validationError != null) {
                showError(validationError);
                return;
            }

            Lecon target = (lecon == null) ? new Lecon() : lecon;
            target.setTitre(titreField.getText().trim());
            target.setDescription(emptyToNull(descriptionArea.getText()));
            target.setContenu(emptyToNull(contenuArea.getText()));
            target.setOrdre(Integer.parseInt(ordreField.getText().trim()));
            target.setDuree(parseOptionalInteger(dureeField.getText()));
            target.setVideo_url(emptyToNull(videoUrlField.getText()));
            target.setFichier(emptyToNull(fichierField.getText()));
            target.setGratuit(gratuitCheck.isSelected());
            target.setFormation(formation);

            target.valider();

            if (lecon == null) {
                leconService.ajouter(target);
                showInfo("Lecon ajoutee avec succes.");
            } else {
                leconService.modifier(target);
                showInfo("Lecon modifiee avec succes.");
            }

            if (onSaved != null) {
                onSaved.run();
            }
            closeWindow();
        } catch (NumberFormatException ex) {
            showError("Ordre et duree doivent etre numeriques.");
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

    private void fillForm(Lecon l) {
        titreField.setText(l.getTitre());
        descriptionArea.setText(l.getDescription());
        contenuArea.setText(l.getContenu());
        ordreField.setText(String.valueOf(l.getOrdre()));
        dureeField.setText(l.getDuree() == null ? "" : String.valueOf(l.getDuree()));
        videoUrlField.setText(l.getVideo_url());
        fichierField.setText(l.getFichier());
        gratuitCheck.setSelected(l.isGratuit());
    }

    private Integer parseOptionalInteger(String value) {
        String raw = value == null ? "" : value.trim();
        return raw.isEmpty() ? null : Integer.parseInt(raw);
    }

    private String validateForm() {
        if (isBlank(titreField.getText())) {
            return "Le titre de la lecon est obligatoire.";
        }

        Integer ordre = parseIntegerSafe(ordreField.getText());
        if (ordre == null || ordre < 0) {
            return "L'ordre doit etre un entier >= 0.";
        }

        Integer duree = parseIntegerSafe(dureeField.getText());
        if (duree != null && duree <= 0) {
            return "La duree doit etre superieure a 0.";
        }

        String videoUrl = emptyToNull(videoUrlField.getText());
        if (videoUrl != null && !isValidVideoUrl(videoUrl)) {
            return "Le lien video est invalide (utilisez http:// ou https://).";
        }

        return null;
    }

    private void installInputConstraints() {
        UnaryOperator<TextFormatter.Change> integerFilter = change ->
                change.getControlNewText().matches("\\d*") ? change : null;
        ordreField.setTextFormatter(new TextFormatter<>(integerFilter));
        dureeField.setTextFormatter(new TextFormatter<>(integerFilter));
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

    private boolean isValidVideoUrl(String value) {
        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
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

