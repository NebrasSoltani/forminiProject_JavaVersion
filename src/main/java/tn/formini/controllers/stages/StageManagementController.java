package tn.formini.controllers.stages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.formini.entities.stages.Candidature;
import tn.formini.entities.stages.OffreStage;
import tn.formini.entities.Users.User;
import tn.formini.services.stageService.CandidatureService;
import tn.formini.services.stageService.OffreStageService;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class StageManagementController implements Initializable {

    // Offres
    @FXML private TextField txtTitreOffre;
    @FXML private TextField txtEntrepriseOffre;
    @FXML private TextField txtLieuOffre;
    @FXML private TextField txtDomaineOffre;
    @FXML private ComboBox<String> comboStatutOffre;
    @FXML private TextArea txtDescriptionOffre;
    @FXML private TextField txtRechercheOffre;
    @FXML private TableView<OffreStage> tableOffres;
    @FXML private TableColumn<OffreStage, Integer> colIdOffre;
    @FXML private TableColumn<OffreStage, String> colTitreOffre;
    @FXML private TableColumn<OffreStage, String> colEntrepriseOffre;
    @FXML private TableColumn<OffreStage, String> colDomaineOffre;
    @FXML private TableColumn<OffreStage, String> colStatutOffre;

    // Candidatures
    @FXML private TextField txtOffreIdCand;
    @FXML private TextField txtApprenantIdCand;
    @FXML private ComboBox<String> comboStatutCand;
    @FXML private TextArea txtLettreCand;
    @FXML private TextArea txtCommentaireCand;
    @FXML private TableView<Candidature> tableCandidatures;
    @FXML private TableColumn<Candidature, Integer> colIdCand;
    @FXML private TableColumn<Candidature, String> colOffreCand;
    @FXML private TableColumn<Candidature, String> colApprenantCand;
    @FXML private TableColumn<Candidature, String> colStatutCand;
    @FXML private TabPane tabPane;

    private OffreStageService offreService = new OffreStageService();
    private CandidatureService candService = new CandidatureService();
    private tn.formini.services.ai.CvGenerationService aiService = new tn.formini.services.ai.CvGenerationService();
    private ObservableList<OffreStage> masterOffres = FXCollections.observableArrayList();
    private ObservableList<Candidature> masterCands = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Init Offres
        colIdOffre.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitreOffre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colEntrepriseOffre.setCellValueFactory(new PropertyValueFactory<>("entreprise"));
        colDomaineOffre.setCellValueFactory(new PropertyValueFactory<>("domaine"));
        colStatutOffre.setCellValueFactory(new PropertyValueFactory<>("statut"));
        comboStatutOffre.setItems(FXCollections.observableArrayList("ouvert", "ferme", "en_attente"));
        loadOffres();
        setupRechercheOffre();

        // Init Candidatures
        colIdCand.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOffreCand.setCellValueFactory(new PropertyValueFactory<>("offreStage"));
        colApprenantCand.setCellValueFactory(new PropertyValueFactory<>("apprenant"));
        colStatutCand.setCellValueFactory(new PropertyValueFactory<>("statut"));
        comboStatutCand.setItems(FXCollections.observableArrayList("en_attente", "acceptee", "refusee", "en_cours"));
        loadCands();

        // Selection listeners
        tableOffres.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillOffreFields(newVal);
        });

        tableCandidatures.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillCandFields(newVal);
        });
    }

    private void setupRechercheOffre() {
        txtRechercheOffre.textProperty().addListener((observable, oldValue, newValue) -> {
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

    private void loadOffres() {
        masterOffres.clear();
        masterOffres.addAll(offreService.afficher());
        tableOffres.setItems(masterOffres);
    }

    private void loadCands() {
        masterCands.clear();
        masterCands.addAll(candService.afficher());
        tableCandidatures.setItems(masterCands);
    }

    private void fillOffreFields(OffreStage o) {
        txtTitreOffre.setText(o.getTitre());
        txtEntrepriseOffre.setText(o.getEntreprise());
        txtLieuOffre.setText(o.getLieu());
        txtDomaineOffre.setText(o.getDomaine());
        comboStatutOffre.setValue(o.getStatut());
        txtDescriptionOffre.setText(o.getDescription());
        
        // Remplir automatiquement l'ID pour la candidature si on change d'onglet
        txtOffreIdCand.setText(String.valueOf(o.getId()));
    }

    private void fillCandFields(Candidature c) {
        txtOffreIdCand.setText(c.getOffreStage() != null ? String.valueOf(c.getOffreStage().getId()) : "");
        txtApprenantIdCand.setText(c.getApprenant() != null ? String.valueOf(c.getApprenant().getId()) : "");
        comboStatutCand.setValue(c.getStatut());
        txtLettreCand.setText(c.getLettre_motivation());
        txtCommentaireCand.setText(c.getCommentaire());
    }

    @FXML
    void handleGenererDescriptionIA(ActionEvent event) {
        String titre = txtTitreOffre.getText().trim();
        String domaine = txtDomaineOffre.getText().trim();

        if (titre.isEmpty() || domaine.isEmpty()) {
            showAlert("Attention", "Veuillez saisir un Titre et un Domaine pour que l'IA puisse générer une description.");
            return;
        }

        try {
            // Afficher un indicateur de chargement ou changer le texte
            txtDescriptionOffre.setText("Génération en cours par l'IA...");
            
            // Appel asynchrone pour ne pas bloquer l'UI (optionnel mais recommandé)
            // Ici on fait un appel simple pour la démo
            String generation = aiService.generateOffreDescription(titre, domaine);
            
            if (generation != null && !generation.startsWith("Erreur")) {
                txtDescriptionOffre.setText(generation);
            } else {
                showAlert("Erreur IA", generation);
                txtDescriptionOffre.clear();
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de générer la description: " + e.getMessage());
        }
    }

    @FXML
    void handleAjouterOffre(ActionEvent event) {
        String titre = txtTitreOffre.getText().trim();
        String entreprise = txtEntrepriseOffre.getText().trim();
        String description = txtDescriptionOffre.getText().trim();
        String statut = comboStatutOffre.getValue();

        if (titre.isEmpty()) {
            showAlert("Erreur de saisie", "Le titre de l'offre est obligatoire.");
            return;
        }
        if (entreprise.isEmpty()) {
            showAlert("Erreur de saisie", "Le nom de l'entreprise est obligatoire.");
            return;
        }
        if (description.isEmpty()) {
            showAlert("Erreur de saisie", "La description est obligatoire.");
            return;
        }
        if (statut == null) {
            showAlert("Erreur de saisie", "Veuillez sélectionner un statut pour l'offre.");
            return;
        }

        try {
            OffreStage o = new OffreStage();
            o.setTitre(titre);
            o.setEntreprise(entreprise);
            o.setLieu(txtLieuOffre.getText().trim().isEmpty() ? "Tunis" : txtLieuOffre.getText().trim());
            o.setDomaine(txtDomaineOffre.getText().trim().isEmpty() ? "Général" : txtDomaineOffre.getText().trim());
            o.setStatut(statut);
            o.setDescription(description);
            o.setDate_publication(new Date());
            o.setDuree("3 mois");
            o.setType_stage("Stage");
            
            User s = new User(); s.setId(1); o.setSociete(s); 
            
            offreService.ajouter(o);
            loadOffres();
            showAlert("Succès", "Offre de stage ajoutée avec succès !");
            handleViderOffre(null);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    void handleModifierOffre(ActionEvent event) {
        OffreStage selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            selected.setTitre(txtTitreOffre.getText());
            selected.setEntreprise(txtEntrepriseOffre.getText());
            selected.setStatut(comboStatutOffre.getValue());
            offreService.modifier(selected);
            loadOffres();
        } catch (Exception e) { showAlert("Erreur", e.getMessage()); }
    }

    @FXML
    void handleSupprimerOffre(ActionEvent event) {
        OffreStage selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            offreService.supprimer(selected.getId());
            loadOffres();
        }
    }

    @FXML
    void handleViderOffre(ActionEvent event) {
        txtTitreOffre.clear();
        txtEntrepriseOffre.clear();
        txtLieuOffre.clear();
        txtDomaineOffre.clear();
        txtDescriptionOffre.clear();
    }

    // Candidature Handlers
    @FXML
    void handleAjouterCand(ActionEvent event) {
        if (txtOffreIdCand.getText().isEmpty() || txtApprenantIdCand.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner une offre et saisir un ID Apprenant.");
            return;
        }
        
        try {
            int offreId = Integer.parseInt(txtOffreIdCand.getText());
            int apprenantId = Integer.parseInt(txtApprenantIdCand.getText());

            Candidature c = new Candidature();
            
            // On s'assure que l'offre existe vraiment
            OffreStage o = offreService.findById(offreId);
            if (o == null) {
                showAlert("Erreur", "L'offre de stage avec l'ID " + offreId + " n'existe pas.");
                return;
            }
            
            // On s'assure que l'apprenant existe (on mock l'utilisateur s'il n'est pas trouvé pour le test, mais idéalement on check)
            User a = new User(); 
            a.setId(apprenantId);
            
            c.setOffreStage(o);
            c.setApprenant(a);
            c.setStatut(comboStatutCand.getValue() != null ? comboStatutCand.getValue() : "en_attente");
            c.setLettre_motivation(txtLettreCand.getText());
            c.setCommentaire(txtCommentaireCand.getText());
            c.setDate_candidature(new Date());
            
            candService.ajouter(c);
            
            if (c.getId() > 0) {
                loadCands();
                showAlert("Succès", "Candidature enregistrée avec succès !");
            } else {
                showAlert("Erreur", "L'ajout a échoué (vérifiez vos IDs).");
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Les IDs doivent être des nombres.");
        } catch (Exception e) {
            showAlert("Erreur", "Détail : " + e.getMessage());
        }
    }

    @FXML
    void handleAnalyseIA(ActionEvent event) {
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une candidature pour l'analyse IA.");
            return;
        }

        try {
            // Récupérer les détails complets de l'offre si nécessaire
            OffreStage offre = selected.getOffreStage();
            String descriptionOffre = (offre != null) ? "Titre: " + offre.getTitre() + "\nDescription: " + offre.getDescription() : "Non spécifiée";
            
            // Simuler le contenu du CV à partir de la lettre de motivation si le fichier n'est pas lisible directement
            String cvContent = "Lettre de motivation: " + selected.getLettre_motivation();
            if (selected.getCv() != null && !selected.getCv().isEmpty()) {
                cvContent += "\nChemin du CV: " + selected.getCv();
            }

            // Afficher un message de chargement (optionnel car Gemini est rapide)
            String resultat = aiService.analyzeMatching(descriptionOffre, cvContent);
            
            // Afficher le résultat dans une grande zone de texte
            TextArea textArea = new TextArea(resultat);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefHeight(400);
            textArea.setPrefWidth(500);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Analyse Matching IA");
            alert.setHeaderText("Analyse de compatibilité pour : " + (selected.getApprenant() != null ? selected.getApprenant().getNom() : "Candidat"));
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();

        } catch (Exception e) {
            showAlert("Erreur IA", "Impossible de contacter le service IA: " + e.getMessage());
        }
    }

    @FXML
    void handleAccepterCand(ActionEvent event) {
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une candidature.");
            return;
        }
        selected.setStatut("acceptee");
        candService.modifier(selected);
        loadCands();
        showAlert("Succès", "Candidature acceptée !");
    }

    @FXML
    void handleRefuserCand(ActionEvent event) {
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une candidature.");
            return;
        }
        selected.setStatut("refusee");
        candService.modifier(selected);
        loadCands();
        showAlert("Succès", "Candidature refusée.");
    }

    @FXML
    void handleModifierCand(ActionEvent event) {
        Candidature s = tableCandidatures.getSelectionModel().getSelectedItem();
        if (s == null) return;
        s.setStatut(comboStatutCand.getValue());
        s.setCommentaire(txtCommentaireCand.getText());
        s.setLettre_motivation(txtLettreCand.getText());
        candService.modifier(s);
        loadCands();
        showAlert("Succès", "Candidature mise à jour !");
    }

    @FXML
    void handleSupprimerCand(ActionEvent event) {
        Candidature s = tableCandidatures.getSelectionModel().getSelectedItem();
        if (s != null) { candService.supprimer(s.getId()); loadCands(); }
    }

    @FXML
    void handleViderCand(ActionEvent event) {
        txtOffreIdCand.clear();
        txtApprenantIdCand.clear();
        txtLettreCand.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setSelectedTab(int index) {
        if (tabPane != null && index >= 0 && index < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(index);
        }
    }
}
