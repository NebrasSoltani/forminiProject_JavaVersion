package tn.formini.controllers.stages;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import tn.formini.entities.stages.OffreStage;

public class CardOffreController {

    @FXML private AnchorPane cardRoot;
    @FXML private Label badgeStatut;
    @FXML private Label lblTitre;
    @FXML private Label lblEntreprise;
    @FXML private Label lblLieu;
    @FXML private Label lblDomaine;
    @FXML private Label lblDate;
    @FXML private Label lblDescription;

    private OffreStage offre;
    private Runnable onEdit;
    private Runnable onDelete;
    private Runnable onView;

    public void setData(OffreStage offre, Runnable onEdit, Runnable onDelete, Runnable onView) {
        this.offre = offre;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onView = onView;

        lblTitre.setText(offre.getTitre());
        lblEntreprise.setText(offre.getEntreprise());
        lblLieu.setText(offre.getLieu() != null ? offre.getLieu() : "Tunis");
        lblDomaine.setText(offre.getDomaine() != null ? offre.getDomaine() : "Général");
        lblDescription.setText(offre.getDescription());
        
        String statut = offre.getStatut() != null ? offre.getStatut().toLowerCase() : "ouvert";
        badgeStatut.setText(statut.toUpperCase());
        
        if (statut.equals("ouvert")) {
            badgeStatut.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 10;");
        } else {
            badgeStatut.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 10;");
        }
    }

    @FXML
    private void handleVoirDetails() {
        if (onView != null) onView.run();
    }

    @FXML
    private void handleModifier() {
        if (onEdit != null) onEdit.run();
    }

    @FXML
    private void handleSupprimer() {
        if (onDelete != null) onDelete.run();
    }

    @FXML
    private void handlePostuler() {
        // Logique pour postuler (peut être redirigé vers l'onglet candidatures)
        System.out.println("Postuler à l'offre: " + offre.getTitre());
    }
}
