package tn.formini.controllers.crud;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Domaine;
import tn.formini.services.UsersService.ApprenantService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ApprenantDetailsController {

    @FXML
    private Label heroSubLabel;

    @FXML
    private Label nomCompletLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label telephoneLabel;

    @FXML
    private Label dateNaissanceLabel;

    @FXML
    private Label gouvernoratLabel;

    @FXML
    private Label genreLabel;

    @FXML
    private Label etatCivilLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label objectifLabel;

    @FXML
    private Label domainesInteretLabel;

    @FXML
    private Label domaineLabel;

    @FXML
    private Label idLabel;

    @FXML
    private ImageView imageViewPhoto;

    @FXML
    private Button closeButton;

    private Apprenant apprenant;
    private ApprenantService apprenantService;

    @FXML
    public void initialize() {
        apprenantService = new ApprenantService();
    }

    public void setApprenant(Apprenant apprenant) {
        System.out.println("setApprenant called with apprenant: " + (apprenant != null ? "id=" + apprenant.getId() : "null"));
        if (apprenant != null) {
            System.out.println("setApprenant: User from original = " + (apprenant.getUser() != null ? apprenant.getUser().getEmail() : "null"));
            System.out.println("setApprenant: Domaine from original = " + (apprenant.getDomaine() != null ? apprenant.getDomaine().getNom() : "null"));

            // Reload from database to ensure User and Domaine are fully loaded
            if (apprenant.getId() > 0) {
                Apprenant reloaded = apprenantService.findById(apprenant.getId());
                if (reloaded != null) {
                    System.out.println("setApprenant: Reloaded apprenant, User=" + (reloaded.getUser() != null ? reloaded.getUser().getEmail() : "null"));
                    this.apprenant = reloaded;
                } else {
                    System.out.println("setApprenant: Reload failed, using original");
                    this.apprenant = apprenant;
                }
            } else {
                this.apprenant = apprenant;
            }
        } else {
            this.apprenant = apprenant;
        }
        populateFields();
    }

    private void populateFields() {
        if (apprenant == null) {
            System.out.println("populateFields: apprenant is null");
            return;
        }

        System.out.println("populateFields: apprenant id=" + apprenant.getId());

        User user = apprenant.getUser();
        System.out.println("populateFields: user=" + (user != null ? user.getEmail() : "null"));
        if (user != null) {
            nomCompletLabel.setText(formatString(user.getPrenom()) + " " + formatString(user.getNom()));
            emailLabel.setText(formatString(user.getEmail()));
            telephoneLabel.setText(formatString(user.getTelephone()));
            dateNaissanceLabel.setText(formatDate(user.getDate_naissance()));
            gouvernoratLabel.setText(formatString(user.getGouvernorat()));
            roleLabel.setText(formatString(user.getRole_utilisateur()));

            // Load photo if available
            String photoUrl = user.getPhoto();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                try {
                    Image image = new Image(photoUrl, true);
                    imageViewPhoto.setImage(image);
                } catch (Exception e) {
                    System.out.println("populateFields: Failed to load photo: " + e.getMessage());
                }
            }
        } else {
            System.out.println("populateFields: User is null, setting user fields to N/A");
            nomCompletLabel.setText("N/A");
            emailLabel.setText("N/A");
            telephoneLabel.setText("N/A");
            dateNaissanceLabel.setText("N/A");
            gouvernoratLabel.setText("N/A");
            roleLabel.setText("N/A");
        }

        genreLabel.setText(formatString(apprenant.getGenre()));
        etatCivilLabel.setText(formatString(apprenant.getEtat_civil()));
        objectifLabel.setText(formatString(apprenant.getObjectif()));
        domainesInteretLabel.setText(formatDomainesInteret(apprenant.getDomaines_interet()));
        idLabel.setText(String.valueOf(apprenant.getId()));

        Domaine domaine = apprenant.getDomaine();
        domaineLabel.setText(domaine != null ? formatString(domaine.getNom()) : "N/A");
    }

    private String formatString(String value) {
        return value != null && !value.isEmpty() ? value : "N/A";
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    private String formatDomainesInteret(String domainesInteret) {
        if (domainesInteret == null || domainesInteret.isEmpty() || "[]".equals(domainesInteret)) {
            return "N/A";
        }
        
        String cleaned = domainesInteret
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .trim();
        
        if (cleaned.isEmpty()) {
            return "N/A";
        }
        
        return cleaned.contains(",") ? cleaned.replace(",", ", ") : cleaned;
    }

    @FXML
    private void handleCloseButton() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
