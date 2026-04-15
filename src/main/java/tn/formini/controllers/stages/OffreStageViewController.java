package tn.formini.controllers.stages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.formini.entities.stages.Candidature;
import tn.formini.entities.stages.OffreStage;
import tn.formini.entities.Users.User;
import tn.formini.services.stageService.CandidatureService;
import tn.formini.services.stageService.OffreStageService;

import java.util.Date;
import java.util.List;

public class OffreStageViewController {

    @FXML private TableView<OffreStage> tableOffres;
    @FXML private TableColumn<OffreStage, String> colTitre;
    @FXML private TableColumn<OffreStage, String> colEntreprise;
    @FXML private TableColumn<OffreStage, String> colDomaine;
    @FXML private TableColumn<OffreStage, String> colLieu;

    @FXML private Label lblTitre;
    @FXML private Label lblEntreprise;
    @FXML private TextArea txtDescription;
    @FXML private TextArea txtLettreMotivation;
    @FXML private TextArea txtCommentaire;
    @FXML private TextField txtRecherche;
    @FXML private Label lblCvPath;
    @FXML private Button btnPostuler;

    private String selectedCvPath = "";
    private ObservableList<OffreStage> masterOffres = FXCollections.observableArrayList();

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
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colEntreprise.setCellValueFactory(new PropertyValueFactory<>("entreprise"));
        colDomaine.setCellValueFactory(new PropertyValueFactory<>("domaine"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));

        loadOffres();
        setupRecherche();

        tableOffres.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayDetails(newVal);
            }
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void loadOffres() {
        List<OffreStage> offres = offreService.afficher();
        masterOffres.setAll(offres);
        tableOffres.setItems(masterOffres);
    }

    private void setupRecherche() {
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                tableOffres.setItems(masterOffres);
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
            tableOffres.setItems(filteredList);
        });
    }

    private void displayDetails(OffreStage o) {
        lblTitre.setText(o.getTitre());
        lblEntreprise.setText(o.getEntreprise() + " - " + o.getLieu());
        txtDescription.setText(o.getDescription());
    }

    @FXML
    private void handlePostuler() {
        OffreStage selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une offre.");
            return;
        }

        try {
            Candidature c = new Candidature();
            c.setOffreStage(selected);
            
            // Si on n'a pas l'utilisateur connecté, on utilise un ID temporaire (ex: 1)
            User apprenant = (currentUser != null) ? currentUser : new User();
            if (currentUser == null) apprenant.setId(1); 
            
            c.setApprenant(apprenant);
            c.setStatut("en_attente");
            c.setLettre_motivation(txtLettreMotivation.getText());
            c.setCommentaire(txtCommentaire.getText());
            c.setCv(selectedCvPath);
            c.setDate_candidature(new Date());

            candService.ajouter(c);
            
            if (c.getId() > 0) {
                showAlert("Succès", "Votre candidature a été envoyée avec succès !");
                txtLettreMotivation.clear();
                txtCommentaire.clear();
                lblCvPath.setText("Aucun fichier choisi");
                selectedCvPath = "";
            } else {
                showAlert("Erreur", "Impossible d'envoyer la candidature.");
            }
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
