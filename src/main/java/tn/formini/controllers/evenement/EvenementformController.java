package tn.formini.controllers.evenement;
import tn.formini.controllers.MainController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tn.formini.entities.evenements.Evenement;
import tn.formini.services.evenementsService.EvenementService;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class EvenementFormController implements Initializable {

    @FXML private Label            labelFormTitle;
    @FXML private TextField        fieldTitre;
    @FXML private ComboBox<String> fieldType;
    @FXML private TextArea         fieldDescription;
    @FXML private DatePicker       fieldDateDebut;
    @FXML private DatePicker       fieldDateFin;
    @FXML private TextField        fieldLieu;
    @FXML private TextField        fieldNombrePlaces;
    @FXML private TextField        fieldImage;
    @FXML private TextField        fieldImage360;
    @FXML private TextField        fieldTags;
    @FXML private TextField        fieldFilieres;
    @FXML private TextField        fieldStreamUrl;
    @FXML private CheckBox         fieldIsActif;
    @FXML private CheckBox         fieldLive;
    @FXML private Button           btnSave;

    @FXML private Label errTitre;
    @FXML private Label errDescription;
    @FXML private Label errDateDebut;
    @FXML private Label errDateFin;

    private MainController   mainController;
    private Evenement        evenementToEdit;
    private final EvenementService service = new EvenementService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fieldType.setItems(FXCollections.observableArrayList(
                "Conférence", "Atelier", "Webinaire", "Formation", "Autre"
        ));
        fieldDateDebut.setValue(LocalDate.now());
        fieldDateFin.setValue(LocalDate.now().plusDays(1));
        fieldStreamUrl.disableProperty().bind(fieldLive.selectedProperty().not());
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void setEvenement(Evenement evt) {
        this.evenementToEdit = evt;
        labelFormTitle.setText("Modifier l'Événement");
        btnSave.setText("Mettre à jour");
        populateForm(evt);
    }

    private void populateForm(Evenement evt) {
        fieldTitre.setText(evt.getTitre());
        fieldType.setValue(evt.getType());
        fieldDescription.setText(evt.getDescription());
        fieldLieu.setText(evt.getLieu());
        fieldNombrePlaces.setText(evt.getNombre_places() != null ? evt.getNombre_places().toString() : "");
        fieldImage.setText(evt.getImage());
        fieldImage360.setText(evt.getImage360());
        fieldTags.setText(evt.getTags());
        fieldFilieres.setText(evt.getFilieres());
        fieldStreamUrl.setText(evt.getStream_url());
        fieldIsActif.setSelected(evt.isIs_actif());
        fieldLive.setSelected(evt.isLive());

        if (evt.getDate_debut() != null)
            fieldDateDebut.setValue(evt.getDate_debut().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        if (evt.getDate_fin() != null)
            fieldDateFin.setValue(evt.getDate_fin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    @FXML
    public void saveEvenement() {
        if (!validate()) return;

        Evenement evt = (evenementToEdit != null) ? evenementToEdit : new Evenement();
        evt.setTitre(fieldTitre.getText().trim());
        evt.setType(fieldType.getValue());
        evt.setDescription(fieldDescription.getText().trim());
        evt.setLieu(fieldLieu.getText().trim());
        evt.setImage(fieldImage.getText().trim());
        evt.setImage360(fieldImage360.getText().trim());
        String tags = fieldTags.getText().trim();
        evt.setTags(ensureJsonArray(tags));
        String filieres = fieldFilieres.getText().trim();
        evt.setFilieres(ensureJsonArray(filieres));
        evt.setStream_url(fieldStreamUrl.getText().trim());
        evt.setIs_actif(fieldIsActif.isSelected());
        evt.setLive(fieldLive.isSelected());
        evt.setDate_debut(toDate(fieldDateDebut.getValue()));
        evt.setDate_fin(toDate(fieldDateFin.getValue()));

        String placesStr = fieldNombrePlaces.getText().trim();
        if (!placesStr.isEmpty()) {
            try { evt.setNombre_places(Integer.parseInt(placesStr)); }
            catch (NumberFormatException ignored) {}
        }

        if (evenementToEdit == null) {
            service.ajouter(evt);
        } else {
            service.modifier(evt);
        }

        showSuccess(evenementToEdit == null ? "Événement ajouté !" : "Événement mis à jour !");
        mainController.showEventList();
    }

    @FXML
    public void browseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        java.io.File file = chooser.showOpenDialog(fieldImage.getScene().getWindow());
        if (file != null) fieldImage.setText(file.toURI().toString());
    }

    @FXML
    public void goBack() {
        mainController.showEventList();
    }

    private boolean validate() {
        boolean ok = true;
        errTitre.setVisible(false);
        errDescription.setVisible(false);
        errDateDebut.setVisible(false);
        errDateFin.setVisible(false);

        if (fieldTitre.getText().trim().isEmpty()) {
            errTitre.setText("Le titre est obligatoire.");
            errTitre.setVisible(true);
            ok = false;
        }
        if (fieldDescription.getText().trim().isEmpty()) {
            errDescription.setText("La description est obligatoire.");
            errDescription.setVisible(true);
            ok = false;
        }
        if (fieldDateDebut.getValue() == null) {
            errDateDebut.setText("Date de début obligatoire.");
            errDateDebut.setVisible(true);
            ok = false;
        }
        if (fieldDateFin.getValue() == null) {
            errDateFin.setText("Date de fin obligatoire.");
            errDateFin.setVisible(true);
            ok = false;
        }
        if (ok && fieldDateFin.getValue().isBefore(fieldDateDebut.getValue())) {
            errDateFin.setText("La date de fin doit être après la date de début.");
            errDateFin.setVisible(true);
            ok = false;
        }
        return ok;
    }

    private Date toDate(LocalDate ld) {
        return ld == null ? null
                : Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String ensureJsonArray(String input) {
        if (input == null || input.trim().isEmpty()) return "[]";
        input = input.trim();
        if (input.startsWith("[") && input.endsWith("]")) return input;
        String[] parts = input.split(",");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < parts.length; i++) {
            sb.append("\"").append(parts[i].trim().replace("\"", "\\\"")).append("\"");
            if (i < parts.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}