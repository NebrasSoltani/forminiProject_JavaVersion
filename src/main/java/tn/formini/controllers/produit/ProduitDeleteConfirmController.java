package tn.formini.controllers.produit;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.math.BigDecimal;

public class ProduitDeleteConfirmController {

    @FXML private Label labelTitle;
    @FXML private Label labelName;
    @FXML private Label labelMeta;
    @FXML private ImageView imageView;
    @FXML private TextField fieldConfirm;
    @FXML private Label labelError;
    @FXML private Button btnDelete;

    private Produit produit;
    private final ProduitService service = new ProduitService();
    private Runnable onDeleted;

    @FXML
    public void initialize() {
        fieldConfirm.textProperty().addListener((obs, oldV, newV) -> {
            boolean ok = "SUPPRIMER".equalsIgnoreCase(newV == null ? "" : newV.trim());
            btnDelete.setDisable(!ok);
            labelError.setVisible(false);
        });
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit == null) return;

        labelTitle.setText("Confirmer la suppression du produit");
        labelName.setText(produit.getNom() == null ? "—" : produit.getNom());

        String cat = produit.getCategorie() == null ? "—" : produit.getCategorie();
        String price = formatMoney(produit.getPrix());
        String stock = "Stock: " + produit.getStock();
        labelMeta.setText(cat + " • " + price + " • " + stock);

        imageView.setImage(null);
        if (produit.getImage() != null && !produit.getImage().trim().isEmpty()) {
            try {
                imageView.setImage(new Image(produit.getImage().trim(), true));
            } catch (Exception ignored) {}
        }
    }

    public void setOnDeleted(Runnable onDeleted) {
        this.onDeleted = onDeleted;
    }

    @FXML
    public void confirmDelete() {
        if (produit == null) return;
        String typed = fieldConfirm.getText() == null ? "" : fieldConfirm.getText().trim();
        if (!"SUPPRIMER".equalsIgnoreCase(typed)) {
            labelError.setText("Veuillez taper SUPPRIMER pour confirmer.");
            labelError.setVisible(true);
            return;
        }

        try {
            service.supprimer(produit.getId());
            if (onDeleted != null) onDeleted.run();
            close();
        } catch (Exception e) {
            labelError.setText("Erreur suppression: " + e.getMessage());
            labelError.setVisible(true);
        }
    }

    @FXML
    public void close() {
        Stage stage = (Stage) fieldConfirm.getScene().getWindow();
        stage.close();
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) return "0.000 DT";
        return String.format("%.3f DT", value.doubleValue());
    }
}

