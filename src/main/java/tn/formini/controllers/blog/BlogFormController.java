package tn.formini.controllers.blog;
import tn.formini.controllers.MainController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tn.formini.entities.evenements.Blog;
import tn.formini.repositories.BlogRepository;
import tn.formini.services.CloudinaryService;
import tn.formini.services.evenementsService.BlogService;
import tn.formini.services.evenementsService.EvenementService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

@Component
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

    @FXML private ImageView blogPreview;
    @FXML private ProgressIndicator aiLoading;

    private MainController   mainController;
    private Blog             blogToEdit;
    private File             imageFile;

    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private BlogRepository blogRepository;

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
        if (blog.getImage() != null && blog.getImage().startsWith("http")) {
            blogPreview.setImage(new Image(blog.getImage()));
        }
    }

    @FXML
    public void saveBlog() {
        if (!validate()) return;

        try {
            String cloudinaryUrl = fieldImage.getText();

            if (imageFile != null) {
                cloudinaryUrl = cloudinaryService.uploadImage(imageFile);
            }

            Blog blog = (blogToEdit != null) ? blogToEdit : new Blog();
            blog.setTitre(fieldTitre.getText().trim());
            blog.setCategorie(fieldCategorie.getValue());
            blog.setResume(fieldResume.getText().trim());
            blog.setContenu(fieldContenu.getText().trim());
            blog.setImage(cloudinaryUrl);
            String tags = fieldTags.getText().trim();
            blog.setTags(ensureJsonArray(tags));
            blog.setIs_publie(fieldIsPublie.isSelected());
            blog.setDate_publication(
                    Date.from(fieldDatePublication.getValue()
                            .atStartOfDay(ZoneId.systemDefault()).toInstant())
            );
            
            Object selectedEvt = fieldEvenement.getValue();
            if (selectedEvt instanceof tn.formini.entities.evenements.Evenement) {
                blog.setEvenement_id(((tn.formini.entities.evenements.Evenement) selectedEvt).getId());
            } else {
                blog.setEvenement_id(null);
            }

            if (tn.formini.tools.SessionManager.getCurrentUser() != null) {
                blog.setAuteur_id(tn.formini.tools.SessionManager.getCurrentUser().getId());
            } else {
                blog.setAuteur_id(1); 
            }

            // Validation de l'entité
            blog.valider();

            // Utilisation du repository JPA
            blogRepository.save(blog);

            showSuccess(blogToEdit == null ? "Blog ajouté avec succès !" : "Blog mis à jour !");
            mainController.showBlogList();
        } catch (IllegalArgumentException e) {
            showError("Erreur de validation : " + e.getMessage());
        } catch (Exception e) {
            showError("Erreur d'enregistrement : " + e.getMessage());
        }
    }

    @FXML
    public void handleGenerateAIImage() {
        final String titre = fieldTitre.getText().trim();
        String initialContext = fieldContenu.getText().trim();
        if (initialContext.isEmpty()) initialContext = fieldResume.getText().trim();
        final String context = initialContext;

        if (titre.isEmpty()) {
            showError("Saisissez un titre pour l'IA.");
            return;
        }

        aiLoading.setVisible(true);

        new Thread(() -> {
            try {
                String promptText = titre + " " + (context.length() > 60 ? context.substring(0, 60) : context) + " professional blog post hero image";
                String encodedPrompt = java.net.URLEncoder.encode(promptText, "UTF-8");
                String apiUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt + "?seed=" + System.currentTimeMillis();
                
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(60000);
                conn.setReadTimeout(60000);

                if (conn.getResponseCode() == 200) {
                    File tempFile = File.createTempFile("ai_blog_", ".jpg");
                    tempFile.deleteOnExit();
                    
                    try (InputStream in = conn.getInputStream();
                         FileOutputStream out = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    this.imageFile = tempFile;
                    Platform.runLater(() -> blogPreview.setImage(new Image(tempFile.toURI().toString())));
                } else {
                    int responseCode = conn.getResponseCode();
                    Platform.runLater(() -> showError("Erreur IA (Code: " + responseCode + ")"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Erreur IA : " + e.getMessage()));
            } finally {
                Platform.runLater(() -> aiLoading.setVisible(false));
            }
        }).start();
    }

    @FXML
    public void browseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        java.io.File file = chooser.showOpenDialog(fieldImage.getScene().getWindow());
        if (file != null) {
            this.imageFile = file;
            blogPreview.setImage(new Image(file.toURI().toString()));
            fieldImage.setText(file.getName());
        }
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

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
