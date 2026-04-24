package tn.formini.controllers.stages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.formini.entities.stages.Candidature;
import tn.formini.entities.stages.OffreStage;
import tn.formini.services.stageService.CandidatureService;
import tn.formini.services.stageService.OffreStageService;
import tn.formini.services.formations.InscriptionService;
import tn.formini.entities.formations.Formation;
import tn.formini.utils.OffreCardBuilder;
import tn.formini.utils.CandidatureCardBuilder;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.List;

public class StageManagementController implements Initializable {

    @FXML private TextField txtTitreOffre;
    @FXML private TextField txtEntrepriseOffre;
    @FXML private TextField txtLieuOffre;
    @FXML private TextField txtDomaineOffre;
    @FXML private ComboBox<String> comboStatutOffre;
    @FXML private TextArea txtDescriptionOffre;
    @FXML private TextField txtRechercheOffre;
    @FXML private Pagination paginationOffres;
    @FXML private TabPane tabPane;

    @FXML private ComboBox<String> comboStatutCand;
    @FXML private TextArea txtLettreCand;
    @FXML private VBox containerCandidatures;

    private OffreStageService offreService = new OffreStageService();
    private CandidatureService candService = new CandidatureService();
    private InscriptionService inscriptionService = new InscriptionService();
    private tn.formini.services.ai.CvGenerationService aiService = new tn.formini.services.ai.CvGenerationService();
    
    private ObservableList<OffreStage> masterOffres = FXCollections.observableArrayList();
    private ObservableList<Candidature> masterCands = FXCollections.observableArrayList();
    private FilteredList<OffreStage> filteredOffres;
    private final int ITEMS_PER_PAGE = 3;

    // Track selections
    private OffreStage selectedOffre;
    private Candidature selectedCand;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
        filteredOffres = new FilteredList<>(masterOffres, p -> true);
        
        // Setup Pagination Page Factory
        if (paginationOffres != null) {
            paginationOffres.setPageFactory(this::createPage);
        }
        
        loadOffres();
        loadCands();
        setupRechercheOffre();
    }

    private void setupComboBoxes() {
        if (comboStatutOffre != null) {
            comboStatutOffre.setItems(FXCollections.observableArrayList("ouvert", "ferme", "en_attente"));
        }
        if (comboStatutCand != null) {
            comboStatutCand.setItems(FXCollections.observableArrayList("en_attente", "acceptee", "refusee", "en_cours"));
        }
    }

    private void setupRechercheOffre() {
        txtRechercheOffre.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredOffres.setPredicate(offre -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return (offre.getTitre() != null && offre.getTitre().toLowerCase().contains(lower)) || 
                       (offre.getEntreprise() != null && offre.getEntreprise().toLowerCase().contains(lower)) ||
                       (offre.getDomaine() != null && offre.getDomaine().toLowerCase().contains(lower));
            });
            updatePagination();
        });
    }

    private void loadOffres() {
        masterOffres.clear();
        masterOffres.addAll(offreService.afficher());
        updatePagination();
    }

    private void updatePagination() {
        if (paginationOffres == null) return;
        int count = filteredOffres.size();
        int pageCount = (count / ITEMS_PER_PAGE) + (count % ITEMS_PER_PAGE > 0 ? 1 : 0);
        paginationOffres.setPageCount(pageCount > 0 ? pageCount : 1);
        // Force refresh of current page
        paginationOffres.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {
        VBox box = new VBox(12);
        box.setStyle("-fx-padding: 5 0;");
        int start = pageIndex * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, filteredOffres.size());

        for (int i = start; i < end; i++) {
            OffreStage o = filteredOffres.get(i);
            Pane card = OffreCardBuilder.createCard(o, 
                selected -> fillOffreFields(selected), 
                selected -> { 
                    offreService.supprimer(selected.getId()); 
                    loadOffres(); 
                    if(selectedOffre != null && selectedOffre.getId() == selected.getId()) handleViderOffre(null);
                },
                selected -> handleVoirDetails(selected));
            box.getChildren().add(card);
        }
        return box;
    }

    private void loadCands() {
        masterCands.clear();
        masterCands.addAll(candService.afficher());
        displayCands(masterCands);
    }

    private void displayCands(List<Candidature> cands) {
        containerCandidatures.getChildren().clear();
        for (Candidature c : cands) {
            containerCandidatures.getChildren().add(CandidatureCardBuilder.createCard(c, 
                selected -> { selected.setStatut("acceptee"); candService.modifier(selected); loadCands(); },
                selected -> { selected.setStatut("refusee"); candService.modifier(selected); loadCands(); },
                selected -> handleAnalyseIAExplicit(selected),
                selected -> handleVoirFormations(selected),
                selected -> fillCandidatureFields(selected),
                selected -> handleVoirCV(selected)));
        }
    }

    private void handleVoirCV(Candidature c) {
        if (c.getCv() == null || c.getCv().isEmpty()) {
            showAlert("CV", "Aucun CV n'a été déposé par cet apprenant.");
            return;
        }
        // Affichage du contenu du CV (si c'est du texte) ou du lien
        showAlert("Curriculum Vitae", "Contenu/Lien du CV :\n\n" + c.getCv());
    }

    private void fillCandidatureFields(Candidature c) {
        this.selectedCand = c;
        txtLettreCand.setText(c.getLettre_motivation());
        if (comboStatutCand != null) comboStatutCand.setValue(c.getStatut());
        
        // Basculer vers l'onglet Candidat
        tabPane.getSelectionModel().select(1);
        
        // Optionnel : Afficher le CV dans une alerte ou log si pas de champ dédié
        if (c.getCv() != null && !c.getCv().isEmpty()) {
            System.out.println("CV du candidat: " + c.getCv());
        }
    }

    private void handleVoirFormations(Candidature c) {
        if (c.getApprenant() == null) {
            showAlert("Erreur", "Apprenant non trouvé.");
            return;
        }
        
        // 1. Inscriptions sur la plateforme
        List<Formation> formations = inscriptionService.findFormationsByApprenant(c.getApprenant().getId());
        
        StringBuilder sb = new StringBuilder();
        sb.append("👤 PROFIL DE L'APPRENANT : ").append(c.getApprenant().getNom().toUpperCase()).append("\n");
        sb.append("────────────────────────────\n\n");

        // 2. Récupérer les données du profil Apprenant (Objectifs, etc.)
        tn.formini.entities.Users.Apprenant profile = new tn.formini.services.UsersService.ApprenantService().findByUserId(c.getApprenant().getId());
        if (profile != null && profile.getObjectif() != null) {
            sb.append("🎯 OBJECTIF PROFESSIONNEL :\n");
            sb.append(profile.getObjectif()).append("\n\n");
        }

        // 3. Inscriptions Formini
        sb.append("📚 FORMATIONS SUR FORMINI :\n");
        if (formations.isEmpty()) {
            sb.append("• Aucune formation suivie sur la plateforme.\n");
        } else {
            for (Formation f : formations) {
                sb.append("• ").append(f.getTitre()).append(" (").append(f.getNiveau()).append(")\n");
            }
        }
        sb.append("\n");

        // 4. Détails du CV (Simulé ou extrait si disponible)
        if (c.getCv() != null && !c.getCv().isEmpty()) {
            sb.append("📄 DÉTAILS DU CURRICULUM VITAE :\n");
            sb.append(c.getCv().length() > 200 ? c.getCv().substring(0, 200) + "..." : c.getCv());
        }
        
        showAlert("Dossier Candidat Complet", sb.toString());
    }

    private void fillOffreFields(OffreStage o) {
        this.selectedOffre = o;
        txtTitreOffre.setText(o.getTitre());
        txtEntrepriseOffre.setText(o.getEntreprise());
        txtLieuOffre.setText(o.getLieu());
        txtDomaineOffre.setText(o.getDomaine());
        comboStatutOffre.setValue(o.getStatut());
        txtDescriptionOffre.setText(o.getDescription());
    }

    private boolean validerSaisieOffre() {
        StringBuilder sb = new StringBuilder();
        if (txtTitreOffre.getText().trim().isEmpty()) sb.append("- Titre requis\n");
        if (txtEntrepriseOffre.getText().trim().isEmpty()) sb.append("- Entreprise requise\n");
        if (txtLieuOffre.getText().trim().isEmpty()) sb.append("- Lieu requis\n");
        if (txtDomaineOffre.getText().trim().isEmpty()) sb.append("- Domaine requis\n");
        if (comboStatutOffre.getValue() == null) sb.append("- Statut requis\n");
        if (txtDescriptionOffre.getText().trim().length() < 10) sb.append("- Description trop courte (min 10 car.)\n");

        if (sb.length() > 0) {
            showAlert("Erreur de saisie", "Veuillez corriger les points suivants :\n" + sb.toString());
            return false;
        }
        return true;
    }

    private boolean validerSaisieCand() {
        if (selectedCand == null) {
            showAlert("Attention", "Veuillez d'abord sélectionner une candidature dans la liste.");
            return false;
        }
        if (txtLettreCand.getText().trim().isEmpty()) {
            showAlert("Erreur", "La lettre de motivation ne peut pas être vide.");
            return false;
        }
        if (comboStatutCand.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un statut.");
            return false;
        }
        return true;
    }

    @FXML void handleGenererDescriptionIA(ActionEvent event) {
        String titre = txtTitreOffre.getText().trim();
        String domaine = txtDomaineOffre.getText().trim();
        if (titre.isEmpty() || domaine.isEmpty()) {
            showAlert("Attention", "Titre et Domaine requis pour l'IA.");
            return;
        }
        try {
            String gen = aiService.generateOffreDescription(titre, domaine);
            txtDescriptionOffre.setText(gen);
        } catch (Exception e) {
            showAlert("Erreur IA", e.getMessage());
        }
    }

    @FXML void handleAjouterOffre(ActionEvent event) {
        if (!validerSaisieOffre()) return;
        try {
            OffreStage o = new OffreStage();
            o.setTitre(txtTitreOffre.getText());
            o.setEntreprise(txtEntrepriseOffre.getText());
            o.setLieu(txtLieuOffre.getText());
            o.setDomaine(txtDomaineOffre.getText());
            o.setStatut(comboStatutOffre.getValue());
            o.setDescription(txtDescriptionOffre.getText());
            o.setDate_publication(new Date());
            offreService.ajouter(o);
            loadOffres();
            showAlert("Succès", "Offre publiée !");
            handleViderOffre(null);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML void handleModifierOffre(ActionEvent event) {
        if (selectedOffre == null) {
            showAlert("Selection requise", "Veuillez cliquer sur une offre dans la liste pour la modifier.");
            return;
        }
        if (!validerSaisieOffre()) return;
        try {
            selectedOffre.setTitre(txtTitreOffre.getText());
            selectedOffre.setEntreprise(txtEntrepriseOffre.getText());
            selectedOffre.setLieu(txtLieuOffre.getText());
            selectedOffre.setDomaine(txtDomaineOffre.getText());
            selectedOffre.setStatut(comboStatutOffre.getValue());
            selectedOffre.setDescription(txtDescriptionOffre.getText());
            
            offreService.modifier(selectedOffre);
            loadOffres();
            showAlert("Succès", "Offre mise à jour !");
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML void handleSupprimerOffre(ActionEvent event) {
        if (selectedOffre == null) {
            showAlert("Selection requise", "Veuillez cliquer sur une offre pour la supprimer.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'offre : " + selectedOffre.getTitre() + " ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                offreService.supprimer(selectedOffre.getId());
                loadOffres();
                handleViderOffre(null);
            }
        });
    }

    @FXML void handleViderOffre(ActionEvent event) {
        selectedOffre = null;
        txtTitreOffre.clear();
        txtEntrepriseOffre.clear();
        txtLieuOffre.clear();
        txtDomaineOffre.clear();
        txtDescriptionOffre.clear();
        if (comboStatutOffre != null) comboStatutOffre.setValue(null);
    }

    @FXML void handleModifierCand(ActionEvent event) {
        if (!validerSaisieCand()) return;
        try {
            selectedCand.setStatut(comboStatutCand.getValue());
            selectedCand.setLettre_motivation(txtLettreCand.getText());
            candService.modifier(selectedCand);
            loadCands();
            showAlert("Succès", "Candidature mise à jour !");
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML void handleSupprimerCand(ActionEvent event) {
        if (selectedCand == null) {
            showAlert("Sélection requise", "Veuillez sélectionner une candidature à supprimer.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la candidature de " + (selectedCand.getApprenant() != null ? selectedCand.getApprenant().getNom() : "ce candidat") + " ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                candService.supprimer(selectedCand.getId());
                loadCands();
                handleViderCand(null);
            }
        });
    }

    @FXML void handleViderCand(ActionEvent event) {
        selectedCand = null;
        txtLettreCand.clear();
        if (comboStatutCand != null) comboStatutCand.setValue(null);
    }

    @FXML void handleAccepterCand(ActionEvent event) {
        if (selectedCand == null) {
            showAlert("Info", "Veuillez sélectionner une candidature.");
            return;
        }
        selectedCand.setStatut("acceptee");
        candService.modifier(selectedCand);
        loadCands();
        showAlert("Succès", "La candidature a été acceptée.");
    }

    @FXML void handleRefuserCand(ActionEvent event) {
        if (selectedCand == null) {
            showAlert("Info", "Veuillez sélectionner une candidature.");
            return;
        }
        selectedCand.setStatut("refusee");
        candService.modifier(selectedCand);
        loadCands();
        showAlert("Succès", "La candidature a été refusée.");
    }

    @FXML public void handleAnalyseIA(ActionEvent event) {
        if (selectedCand != null) {
            handleAnalyseIAExplicit(selectedCand);
        } else {
            showAlert("Info", "Veuillez sélectionner une candidature pour l'analyse.");
        }
    }
    
    private void handleAnalyseIAExplicit(Candidature c) {
        try {
            String res = aiService.analyzeMatching("Offre: " + (c.getOffreStage() != null ? c.getOffreStage().getTitre() : "Inconnue"), 
                                                 "Lettre: " + c.getLettre_motivation());
            showAlert("Analyse IA", res);
        } catch (Exception e) {
            showAlert("Erreur IA", e.getMessage());
        }
    }

    private void handleVoirDetails(OffreStage o) {
        showAlert("Détails Offre", o.getDescription());
    }

    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(c);
        a.showAndWait();
    }

    @FXML 
    private void handleButtonHover(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Button btn = (javafx.scene.control.Button) event.getSource();
        btn.setScaleX(1.05); btn.setScaleY(1.05);
    }

    @FXML 
    private void handleButtonNormal(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Button btn = (javafx.scene.control.Button) event.getSource();
        btn.setScaleX(1.0); btn.setScaleY(1.0);
    }

    public void setSelectedTab(int index) {
        if (tabPane != null) tabPane.getSelectionModel().select(index);
    }
}
