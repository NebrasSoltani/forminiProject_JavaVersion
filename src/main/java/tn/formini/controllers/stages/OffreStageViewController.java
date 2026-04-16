package tn.formini.controllers.stages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.formini.entities.stages.Candidature;
import tn.formini.entities.stages.OffreStage;
import tn.formini.entities.Users.User;
import tn.formini.services.stageService.CandidatureService;
import tn.formini.services.stageService.OffreStageService;

import java.util.Date;
import java.util.List;

public class OffreStageViewController {

    @FXML private TilePane tileOffres;
    @FXML private Label lblTitre;
    @FXML private Label lblEntreprise;
    @FXML private Label lblLieu;
    @FXML private Label lblDomaine;
    @FXML private TextArea txtDescription;
    @FXML private TextArea txtLettreMotivation;
    @FXML private TextArea txtCommentaire;
    @FXML private TextField txtRecherche;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtLieuVie;
    @FXML private Label lblCvPath;
    @FXML private Button btnPostuler;
    @FXML private Label lblError;

    private String selectedCvPath = "";
    private ObservableList<OffreStage> masterOffres = FXCollections.observableArrayList();
    private OffreStage selectedOffre = null;

    @FXML
    private void handleChoisirCV() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir votre CV");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(btnPostuler.getScene().getWindow());
        if (selectedFile != null) {
            selectedCvPath = selectedFile.getAbsolutePath();
            lblCvPath.setText(selectedFile.getName());
        }
    }

    private OffreStageService offreService = new OffreStageService();
    private CandidatureService candService = new CandidatureService();
    private User currentUser;

    @FXML
    public void initialize() {
        loadOffres();
        setupRecherche();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void loadOffres() {
        masterOffres.clear();
        masterOffres.addAll(offreService.afficher());
        displayOffres(masterOffres);
    }

    private void displayOffres(ObservableList<OffreStage> offres) {
        tileOffres.getChildren().clear();
        for (OffreStage o : offres) {
            VBox card = createOffreCard(o);
            tileOffres.getChildren().add(card);
        }
    }

    private VBox createOffreCard(OffreStage o) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefWidth(500);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, #00000020, 3, 0.2, 0, 2);"
        );

        Label titreLabel = new Label(o.getTitre());
        titreLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label entrepriseLabel = new Label(o.getEntreprise());
        entrepriseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #3498db;");

        Label lieuLabel = new Label(o.getLieu());
        lieuLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        Label domaineLabel = new Label(o.getDomaine());
        domaineLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

        Label statutLabel = new Label(o.getStatut());
        String statutColor = "ouvert".equals(o.getStatut()) ? "#27ae60" : "#f39c12";
        statutLabel.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + statutColor + ";"
        );

        Button btnVoir = new Button("Voir details");
        btnVoir.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-cursor: hand; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        btnVoir.setOnAction(e -> selectOffre(o));

        card.getChildren().addAll(titreLabel, entrepriseLabel, lieuLabel, domaineLabel, statutLabel, btnVoir);
        return card;
    }

    private void selectOffre(OffreStage o) {
        selectedOffre = o;
        lblTitre.setText(o.getTitre());
        lblEntreprise.setText(o.getEntreprise());
        lblLieu.setText(o.getLieu());
        lblDomaine.setText(o.getDomaine());
        txtDescription.setText(o.getDescription());
    }

    private void setupRecherche() {
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                displayOffres(masterOffres);
                return;
            }
            String lowerCaseFilter = newValue.toLowerCase();
            ObservableList<OffreStage> filteredList = FXCollections.observableArrayList();
            for (OffreStage o : masterOffres) {
                if (o.getTitre().toLowerCase().contains(lowerCaseFilter) ||
                    o.getEntreprise().toLowerCase().contains(lowerCaseFilter) ||
                    o.getDomaine().toLowerCase().contains(lowerCaseFilter)) {
                    filteredList.add(o);
                }
            }
            displayOffres(filteredList);
        });
    }

    @FXML
    private void handlePostuler() {
        lblError.setVisible(false);
        
        if (selectedOffre == null) {
            showError("Veuillez sélectionner une offre.");
            return;
        }

        // Validation des nouveaux champs
        if (txtNom.getText().trim().isEmpty() || txtPrenom.getText().trim().isEmpty()) {
            showError("Le nom et le prénom sont obligatoires.");
            return;
        }
        if (txtTelephone.getText().trim().isEmpty()) {
            showError("Le numéro de téléphone est obligatoire.");
            return;
        }
        if (txtLieuVie.getText().trim().isEmpty()) {
            showError("Le lieu de vie est obligatoire.");
            return;
        }

        String lettre = txtLettreMotivation.getText();
        if (lettre == null || lettre.trim().isEmpty()) {
            showError("La lettre de motivation est obligatoire.");
            return;
        }
        
        try {
            Candidature c = new Candidature();
            c.setOffreStage(selectedOffre);
            
            // On peut enrichir l'objet User ou Candidature avec ces infos si la DB le permet
            // Pour l'instant on les concatène dans la lettre ou le commentaire pour que la société les voie
            String infosPersos = String.format("--- INFOS CANDIDAT ---\nNom: %s %s\nTel: %s\nVille: %s\n\n", 
                txtNom.getText(), txtPrenom.getText(), txtTelephone.getText(), txtLieuVie.getText());
            
            User apprenant = (currentUser != null) ? currentUser : new User();
            if (currentUser == null) apprenant.setId(1); 
            
            c.setApprenant(apprenant);
            c.setStatut("en_attente");
            c.setLettre_motivation(infosPersos + lettre);
            c.setCommentaire(txtCommentaire != null ? txtCommentaire.getText() : "");
            c.setCv(selectedCvPath);
            c.setDate_candidature(new Date());

            candService.ajouter(c);
            
            if (c.getId() > 0) {
                showAlert("Succès", "Votre candidature a été envoyée avec succès !");
                viderFormulaire();
            } else {
                showAlert("Erreur", "Impossible d'envoyer la candidature.");
            }
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void viderFormulaire() {
        txtNom.clear();
        txtPrenom.clear();
        txtTelephone.clear();
        txtLieuVie.clear();
        txtLettreMotivation.clear();
        if (txtCommentaire != null) txtCommentaire.clear();
        lblCvPath.setText("Aucun fichier choisi");
        selectedCvPath = "";
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
