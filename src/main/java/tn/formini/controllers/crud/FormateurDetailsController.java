package tn.formini.controllers.crud;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.FormateurService;

import java.awt.Desktop;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormateurDetailsController {

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
    private Label roleLabel;

    @FXML
    private Label professionLabel;

    @FXML
    private Label niveauEtudeLabel;

    @FXML
    private Label idLabel;

    @FXML
    private Label specialiteLabel;

    @FXML
    private Label experienceLabel;

    @FXML
    private Label noteMoyenneLabel;

    @FXML
    private Label bioLabel;

    @FXML
    private Hyperlink linkedinLink;

    @FXML
    private Hyperlink portfolioLink;

    @FXML
    private Hyperlink cvLink;

    @FXML
    private Button closeButton;

    private Formateur formateur;
    private FormateurService formateurService;

    @FXML
    public void initialize() {
        formateurService = new FormateurService();
    }

    public void setFormateur(Formateur formateur) {
        System.out.println("setFormateur called with formateur: " + (formateur != null ? "id=" + formateur.getId() : "null"));
        if (formateur != null) {
            System.out.println("setFormateur: User from original = " + (formateur.getUser() != null ? formateur.getUser().getEmail() : "null"));

            // Reload from database to ensure User is fully loaded
            if (formateur.getId() > 0) {
                Formateur reloaded = formateurService.findById(formateur.getId());
                if (reloaded != null) {
                    System.out.println("setFormateur: Reloaded formateur, User=" + (reloaded.getUser() != null ? reloaded.getUser().getEmail() : "null"));
                    this.formateur = reloaded;
                } else {
                    System.out.println("setFormateur: Reload failed, using original");
                    this.formateur = formateur;
                }
            } else {
                this.formateur = formateur;
            }
        } else {
            this.formateur = formateur;
        }
        populateFields();
    }

    private void populateFields() {
        if (formateur == null) {
            System.out.println("populateFields: formateur is null");
            return;
        }

        System.out.println("populateFields: formateur id=" + formateur.getId());

        User user = formateur.getUser();
        System.out.println("populateFields: user=" + (user != null ? user.getEmail() : "null"));
        if (user != null) {
            nomCompletLabel.setText(formatString(user.getPrenom()) + " " + formatString(user.getNom()));
            emailLabel.setText(formatString(user.getEmail()));
            telephoneLabel.setText(formatString(user.getTelephone()));
            dateNaissanceLabel.setText(formatDate(user.getDate_naissance()));
            gouvernoratLabel.setText(formatString(user.getGouvernorat()));
            roleLabel.setText(formatString(user.getRole_utilisateur()));
            professionLabel.setText(formatString(user.getProfession()));
            niveauEtudeLabel.setText(formatString(user.getNiveau_etude()));
        } else {
            System.out.println("populateFields: User is null, setting user fields to N/A");
            nomCompletLabel.setText("N/A");
            emailLabel.setText("N/A");
            telephoneLabel.setText("N/A");
            dateNaissanceLabel.setText("N/A");
            gouvernoratLabel.setText("N/A");
            roleLabel.setText("N/A");
            professionLabel.setText("N/A");
            niveauEtudeLabel.setText("N/A");
        }

        idLabel.setText(String.valueOf(formateur.getId()));
        specialiteLabel.setText(formatString(formateur.getSpecialite()));
        experienceLabel.setText(formateur.getExperience_annees() != null ? String.valueOf(formateur.getExperience_annees()) + " ans" : "N/A");
        noteMoyenneLabel.setText(formateur.getNote_moyenne() != null ? String.valueOf(formateur.getNote_moyenne()) + "/5" : "N/A");
        bioLabel.setText(formatString(formateur.getBio()));

        setupHyperlink(linkedinLink, formateur.getLinkedin());
        setupHyperlink(portfolioLink, formateur.getPortfolio());
        setupHyperlink(cvLink, formateur.getCv());
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

    private void setupHyperlink(Hyperlink hyperlink, String url) {
        if (url != null && !url.isEmpty()) {
            hyperlink.setText(url);
            hyperlink.setOnAction(e -> openBrowser(url));
        } else {
            hyperlink.setText("N/A");
            hyperlink.setDisable(true);
        }
    }

    private void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCloseButton() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
