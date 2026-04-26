package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tn.formini.entities.Produit;
import tn.formini.repositories.ProduitRepository;
import tn.formini.services.CloudinaryService;

import java.io.File;

@Component
public class CloudinaryFXController {

    @FXML private ImageView previewImage;
    @FXML private Label urlLabel;
    @FXML private TextField nomField;
    @FXML private TextArea descField;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ProduitRepository produitRepository;

    private File selectedFile;

    @FXML
    public void initialize() {
        System.out.println("CloudinaryFXController initialisé !");
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Toutes les images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("JPG", "*.jpg")
        );

        selectedFile = fileChooser.showOpenDialog(urlLabel.getScene().getWindow());

        if (selectedFile != null) {
            Image img = new Image(selectedFile.toURI().toString());
            previewImage.setImage(img);
            urlLabel.setText("Image prête : " + selectedFile.getName());
        }
    }

    @FXML
    private void handleSave() {
        if (selectedFile == null) {
            showAlert("Erreur", "Veuillez d'abord choisir une image.");
            return;
        }

        if (nomField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un nom pour le produit.");
            return;
        }

        try {
            urlLabel.setText("Upload en cours vers Cloudinary...");
            
            // 1. Upload Cloudinary
            String cloudinaryUrl = cloudinaryService.uploadImage(selectedFile);
            
            // 2. Sauvegarde MySQL via JPA
            Produit produit = new Produit();
            produit.setNom(nomField.getText());
            produit.setDescription(descField.getText());
            produit.setImageUrl(cloudinaryUrl);
            
            produitRepository.save(produit);

            urlLabel.setText("Succès ! URL : " + cloudinaryUrl);
            showAlert("Terminé", "L'image a été uploadée et l'URL a été enregistrée en base de données.");
            
        } catch (Exception e) {
            urlLabel.setText("Échec de l'opération.");
            showAlert("Erreur fatale", "Détail : " + e.getMessage());
            e.printStackTrace();
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
