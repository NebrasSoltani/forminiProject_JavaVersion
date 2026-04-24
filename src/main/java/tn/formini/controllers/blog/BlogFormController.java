package tn.formini.controllers.blog;
import tn.formini.controllers.MainController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tn.formini.entities.evenements.Blog;
import tn.formini.services.evenementsService.BlogService;
import tn.formini.services.evenementsService.EvenementService;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class BlogFormController implements Initializable {

    @FXML private Label                labelFormTitle;
    @FXML private TextField            fieldTitre;
    @FXML private ComboBox<String>     fieldCategorie;
    @FXML private TextArea             fieldResume;
    @FXML private TextArea             fieldContenu;
    @FXML private TextField            fieldImage;
    @FXML private ComboBox<Object>     fieldEvenement;
    @FXML private TextField            fieldTags;
    @FXML private DatePicker           fieldDatePublication;
    @FXML private CheckBox             fieldIsPublie;
    @FXML private Button               btnSave;

    @FXML private Label errTitre;
    @FXML private Label errContenu;
    @FXML private Label errCategorie;

    private MainController   mainController;
    private Blog             blogToEdit;
    private final BlogService      blogService     = new BlogService();
    private final EvenementService evenementService = new EvenementService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fieldCategorie.setItems(FXCollections.observableArrayList(
                "Technologie", "Formation", "Événement", "Actualité", "Autre"
        ));
        fieldDatePublication.setValue(LocalDate.now());
        loadEvenements();
        
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        fieldTitre.textProperty().addListener((obs, old, val) -> clearError(fieldTitre, errTitre));
        fieldContenu.textProperty().addListener((obs, old, val) -> clearError(fieldContenu, errContenu));
        fieldCategorie.valueProperty().addListener((obs, old, val) -> clearError(fieldCategorie, errCategorie));
    }

    private void clearError(Control field, Label errorLabel) {
        field.getStyleClass().remove("form-control-error");
        errorLabel.setVisible(false);
    }

    private void applyError(Control field, Label errorLabel, String message) {
        if (!field.getStyleClass().contains("form-control-error")) {
            field.getStyleClass().add("form-control-error");
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void setBlog(Blog blog) {
        this.blogToEdit = blog;
        labelFormTitle.setText("Modifier le Blog");
        btnSave.setText("Mettre à jour");
        populateForm(blog);
    }

    private void loadEvenements() {
        fieldEvenement.getItems().add("Aucun");
        evenementService.afficher().forEach(e -> fieldEvenement.getItems().add(e));
        fieldEvenement.setValue("Aucun");
    }

    private void populateForm(Blog blog) {
        fieldTitre.setText(blog.getTitre());
        fieldCategorie.setValue(blog.getCategorie());
        fieldResume.setText(blog.getResume());
        fieldContenu.setText(blog.getContenu());
        fieldImage.setText(blog.getImage());
        fieldTags.setText(blog.getTags());
        fieldIsPublie.setSelected(blog.isIs_publie());
        if (blog.getDate_publication() != null) {
            fieldDatePublication.setValue(
                    blog.getDate_publication().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate()
            );
        }
    }

    @FXML
    public void saveBlog() {
        if (!validate()) return;

        Blog blog = (blogToEdit != null) ? blogToEdit : new Blog();
        blog.setTitre(fieldTitre.getText().trim());
        blog.setCategorie(fieldCategorie.getValue());
        blog.setResume(fieldResume.getText().trim());
        blog.setContenu(fieldContenu.getText().trim());
        blog.setImage(fieldImage.getText().trim());
        String tags = fieldTags.getText().trim();
        blog.setTags(ensureJsonArray(tags));
        blog.setIs_publie(fieldIsPublie.isSelected());
        blog.setDate_publication(
                Date.from(fieldDatePublication.getValue()
                        .atStartOfDay(ZoneId.systemDefault()).toInstant())
        );
        // Récupération de l'événement lié
        Object selectedEvt = fieldEvenement.getValue();
        if (selectedEvt instanceof tn.formini.entities.evenements.Evenement) {
            blog.setEvenement_id(((tn.formini.entities.evenements.Evenement) selectedEvt).getId());
        } else {
            blog.setEvenement_id(null);
        }

        // Auteur (Session)
        if (tn.formini.tools.SessionManager.getCurrentUser() != null) {
            blog.setAuteur_id(tn.formini.tools.SessionManager.getCurrentUser().getId());
        } else {
            blog.setAuteur_id(1); // Fallback
        }

        if (blogToEdit == null) {
            blogService.ajouter(blog);
        } else {
            blogService.modifier(blog);
        }

        showSuccess(blogToEdit == null ? "Blog ajouté avec succès !" : "Blog mis à jour !");
        mainController.showBlogList();
    }

    @FXML
    public void browseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        java.io.File file = chooser.showOpenDialog(fieldImage.getScene().getWindow());
        if (file != null) fieldImage.setText(file.toURI().toString());
    }

    @FXML
    public void goBack() {
        mainController.showBlogList();
    }

    private boolean validate() {
        boolean ok = true;

        if (fieldTitre.getText().trim().isEmpty()) {
            applyError(fieldTitre, errTitre, "Le titre est obligatoire.");
            ok = false;
        }
        if (fieldContenu.getText().trim().isEmpty()) {
            applyError(fieldContenu, errContenu, "Le contenu est obligatoire.");
            ok = false;
        }
        if (fieldCategorie.getValue() == null) {
            applyError(fieldCategorie, errCategorie, "Veuillez choisir une catégorie.");
            ok = false;
        }
        return ok;
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
