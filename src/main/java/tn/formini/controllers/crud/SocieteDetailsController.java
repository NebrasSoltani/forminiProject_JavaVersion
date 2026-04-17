package tn.formini.controllers.crud;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.SocieteService;

import java.awt.Desktop;
import java.net.URI;

public class SocieteDetailsController {

    @FXML
    private Label nomSocieteLabel;

    @FXML
    private Label secteurLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label idLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label adresseLabel;

    @FXML
    private Hyperlink siteWebLink;

    @FXML
    private Button closeButton;

    private Societe societe;
    private SocieteService societeService;

    @FXML
    public void initialize() {
        societeService = new SocieteService();
    }

    public void setSociete(Societe societe) {
        System.out.println("setSociete called with societe: " + (societe != null ? "id=" + societe.getId() : "null"));
        if (societe != null) {
            System.out.println("setSociete: User from original = " + (societe.getUser() != null ? societe.getUser().getEmail() : "null"));

            // Reload from database to ensure User is fully loaded
            if (societe.getId() > 0) {
                Societe reloaded = societeService.findById(societe.getId());
                if (reloaded != null) {
                    System.out.println("setSociete: Reloaded societe, User=" + (reloaded.getUser() != null ? reloaded.getUser().getEmail() : "null"));
                    this.societe = reloaded;
                } else {
                    System.out.println("setSociete: Reload failed, using original");
                    this.societe = societe;
                }
            } else {
                this.societe = societe;
            }
        } else {
            this.societe = societe;
        }
        populateFields();
    }

    private void populateFields() {
        if (societe == null) {
            System.out.println("populateFields: societe is null");
            return;
        }

        System.out.println("populateFields: societe id=" + societe.getId());

        nomSocieteLabel.setText(formatString(societe.getNom_societe()));
        secteurLabel.setText(formatString(societe.getSecteur()));
        idLabel.setText(String.valueOf(societe.getId()));
        descriptionLabel.setText(formatString(societe.getDescription()));
        adresseLabel.setText(formatString(societe.getAdresse()));

        User user = societe.getUser();
        System.out.println("populateFields: user=" + (user != null ? user.getEmail() : "null"));
        if (user != null) {
            emailLabel.setText(formatString(user.getEmail()));
        } else {
            System.out.println("populateFields: User is null, setting email to N/A");
            emailLabel.setText("N/A");
        }

        String siteWeb = societe.getSite_web();
        if (siteWeb != null && !siteWeb.isEmpty()) {
            siteWebLink.setText(siteWeb);
            siteWebLink.setOnAction(e -> openBrowser(siteWeb));
        } else {
            siteWebLink.setText("N/A");
            siteWebLink.setDisable(true);
        }
    }

    private String formatString(String value) {
        return value != null && !value.isEmpty() ? value : "N/A";
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
