package tn.formini.controllers.frontend;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.print.PrinterJob;
import javafx.util.Callback;
import tn.formini.services.produitsService.CommandeService;
import tn.formini.services.cart.CartItem;
import tn.formini.services.cart.CartService;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CartController implements Initializable {

    @FXML private TableView<CartItem> table;
    @FXML private TableColumn<CartItem, String> colName;
    @FXML private TableColumn<CartItem, Number> colQty;
    @FXML private TableColumn<CartItem, BigDecimal> colUnitPrice;
    @FXML private TableColumn<CartItem, BigDecimal> colLineTotal;
    @FXML private TableColumn<CartItem, Void> colActions;

    @FXML private Label labelGrandTotal;
    @FXML private Label labelItemsCount;
    @FXML private Label labelEmpty;
    @FXML private Button btnClear;
    @FXML private Button btnValidate;
    @FXML private Button btnPrintInvoice;

    private final CartService cart = CartService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        table.setItems(cart.getItems());

        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getProduit().getNom()));
        colQty.setCellValueFactory(cd -> cd.getValue().quantityProperty());
        colUnitPrice.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getProduit().getPrix()));
        colLineTotal.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getLineTotal()));

        colName.setCellFactory(productCell());
        colQty.setCellFactory(qtySpinnerCell());
        colUnitPrice.setCellFactory(moneyCell());
        colLineTotal.setCellFactory(moneyCell());

        colActions.setCellFactory(makeActionsCell());

        labelGrandTotal.textProperty().bind(Bindings.createStringBinding(
                () -> formatMoney(cart.getGrandTotal()),
                cart.getItems()
        ));

        if (labelItemsCount != null) {
            labelItemsCount.textProperty().bind(Bindings.createStringBinding(
                    () -> cart.getItemsCountDistinct() + " produits / " + cart.getItemsCountTotal() + " articles",
                    cart.getItems()
            ));
        }

        labelEmpty.visibleProperty().bind(Bindings.isEmpty(cart.getItems()));
        table.visibleProperty().bind(Bindings.isNotEmpty(cart.getItems()));
        btnClear.disableProperty().bind(Bindings.isEmpty(cart.getItems()));
        if (btnValidate != null) {
            btnValidate.disableProperty().bind(Bindings.isEmpty(cart.getItems()));
        }
        if (btnPrintInvoice != null) {
            btnPrintInvoice.disableProperty().bind(Bindings.isEmpty(cart.getItems()));
        }
    }

    @FXML
    public void clearCart() {
        cart.clear();
    }

    @FXML
    public void validateOrder() {
        if (cart.getItems().isEmpty()) return;

        VBox form = new VBox(10);
        form.setPadding(new Insets(10));

        Label hint = new Label("Veuillez renseigner vos informations de livraison.");
        hint.setStyle("-fx-text-fill: #334155;");

        TextArea address = new TextArea();
        address.setPromptText("Adresse de livraison");
        address.setWrapText(true);
        address.setPrefRowCount(3);

        TextField phone = new TextField();
        phone.setPromptText("Téléphone (ex: 22123456)");

        Label total = new Label("Total à payer: " + formatMoney(cart.getGrandTotal()));
        total.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");

        form.getChildren().addAll(hint, new Label("Adresse:"), address, new Label("Téléphone:"), phone, total);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Valider la commande");
        confirm.setHeaderText("Confirmer la commande");
        confirm.getDialogPane().setContent(form);
        confirm.setResizable(true);

        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK) return;

        String adresseLivraison = address.getText() == null ? "" : address.getText().trim();
        String telephone = phone.getText() == null ? "" : phone.getText().trim();

        if (adresseLivraison.isEmpty()) {
            showError("Adresse obligatoire", "Veuillez saisir une adresse de livraison.");
            return;
        }

        try {
            int commandeId = new CommandeService().createOrderFromCart(
                    java.util.List.copyOf(cart.getItems()),
                    adresseLivraison,
                    telephone.isEmpty() ? null : telephone
            );

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Commande validée");
            ok.setHeaderText("Merci !");
            ok.setContentText("Votre commande a été enregistrée avec succès.\nID commande: " + commandeId);
            ok.showAndWait();

            // Ask if user wants to print invoice
            Alert printConfirm = new Alert(Alert.AlertType.CONFIRMATION);
            printConfirm.setTitle("Imprimer la facture");
            printConfirm.setHeaderText("Voulez-vous imprimer la facture maintenant ?");
            printConfirm.setContentText("Vous pouvez imprimer une facture pour votre commande.");
            
            var printResult = printConfirm.showAndWait();
            if (printResult.isPresent() && printResult.get() == javafx.scene.control.ButtonType.OK) {
                printInvoice();
            }

            cart.clear();
        } catch (IllegalArgumentException ex) {
            showError("Commande refusée", ex.getMessage());
        } catch (SQLException ex) {
            showError("Erreur base de données", ex.getMessage());
        } catch (Exception ex) {
            showError("Erreur", ex.getMessage());
        }
    }

    private Callback<TableColumn<CartItem, BigDecimal>, TableCell<CartItem, BigDecimal>> moneyCell() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(formatMoney(item));
                }
            }
        };
    }

    private Callback<TableColumn<CartItem, Number>, TableCell<CartItem, Number>> qtySpinnerCell() {
        return column -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>();
            private boolean ignoreChanges = false;

            {
                spinner.setEditable(true);
                spinner.setPrefWidth(90);
                spinner.getStyleClass().add("qty-spinner");

                spinner.valueProperty().addListener((obs, oldV, newV) -> {
                    if (ignoreChanges || newV == null) return;
                    CartItem rowItem = (CartItem) getTableRow().getItem();
                    if (rowItem == null) return;
                    rowItem.setQuantity(newV);
                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                CartItem cartItem = getTableView().getItems().get(getIndex());
                int current = cartItem.getQuantity();

                SpinnerValueFactory.IntegerSpinnerValueFactory vf =
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, current);
                ignoreChanges = true;
                spinner.setValueFactory(vf);
                ignoreChanges = false;

                setGraphic(spinner);
            }
        };
    }

    private Callback<TableColumn<CartItem, String>, TableCell<CartItem, String>> productCell() {
        return column -> new TableCell<>() {
            private final HBox root = new HBox(12);
            private final ImageView img = new ImageView();
            private final VBox textBox = new VBox(4);
            private final Label name = new Label();
            private final HBox metaRow = new HBox(8);
            private final Label badgeCat = new Label();
            private final Label badgeStock = new Label();

            {
                root.getStyleClass().add("cart-product-cell");

                img.setFitWidth(72);
                img.setFitHeight(48);
                img.setPreserveRatio(true);
                img.setSmooth(true);
                img.getStyleClass().add("cart-product-thumb");

                name.getStyleClass().add("cart-product-name");

                badgeCat.getStyleClass().addAll("shop-badge", "cart-badge");
                badgeStock.getStyleClass().addAll("shop-badge", "cart-badge");

                metaRow.getChildren().addAll(badgeCat, badgeStock);

                textBox.getChildren().addAll(name, metaRow);
                root.getChildren().addAll(img, textBox);
                HBox.setHgrow(textBox, Priority.ALWAYS);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                CartItem row = (CartItem) getTableRow().getItem();
                if (row == null || row.getProduit() == null) {
                    setGraphic(null);
                    return;
                }

                var p = row.getProduit();
                name.setText(p.getNom());

                badgeCat.setText(p.getCategorie() == null ? "—" : p.getCategorie());
                badgeStock.setText(p.getStock() <= 0 ? "Rupture" : "Disponible");
                badgeStock.getStyleClass().removeAll("shop-badge-success", "shop-badge-danger");
                badgeStock.getStyleClass().add(p.getStock() <= 0 ? "shop-badge-danger" : "shop-badge-success");

                img.setImage(null);
                if (p.getImage() != null && !p.getImage().trim().isEmpty()) {
                    try {
                        img.setImage(new Image(p.getImage().trim(), true));
                    } catch (Exception ignored) {}
                }

                setGraphic(root);
            }
        };
    }

    private Callback<TableColumn<CartItem, Void>, TableCell<CartItem, Void>> makeActionsCell() {
        return column -> new TableCell<>() {
            private final Button btnDetails = new Button("👁");
            private final Button btnRemove = new Button("Retirer");
            private final HBox box = new HBox(8);
            {
                btnDetails.getStyleClass().addAll("btn-secondary", "btn-small", "cart-icon-btn");
                btnDetails.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    showProductDetails(item);
                });

                btnRemove.getStyleClass().addAll("btn-danger", "btn-small", "cart-remove-btn");
                btnRemove.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cart.remove(item);
                });

                box.getStyleClass().add("cart-actions");
                box.getChildren().addAll(btnDetails, btnRemove);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        };
    }

    private void showProductDetails(CartItem item) {
        if (item == null || item.getProduit() == null) return;

        var p = item.getProduit();

        ImageView imageView = new ImageView();
        imageView.setFitWidth(360);
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");

        if (p.getImage() != null && !p.getImage().trim().isEmpty()) {
            try {
                imageView.setImage(new Image(p.getImage().trim(), true));
            } catch (Exception ignored) {}
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label name = new Label(p.getNom());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label cat = new Label("Catégorie: " + nullToEmpty(p.getCategorie()));
        Label price = new Label("Prix: " + formatMoney(p.getPrix()));
        Label stock = new Label("Stock: " + p.getStock());
        Label qty = new Label("Quantité dans votre panier: " + item.getQuantity());
        Label desc = new Label(nullToEmpty(p.getDescription()));
        desc.setWrapText(true);

        HBox top = new HBox(12, imageView, detailsBox(cat, price, stock, qty));
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);

        content.getChildren().addAll(name, top, new Label("Description:"), desc);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du produit");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(content);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private VBox detailsBox(Label... labels) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(0, 0, 0, 0));
        for (Label l : labels) {
            l.setStyle("-fx-text-fill: #334155;");
            box.getChildren().add(l);
        }
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        box.getChildren().add(spacer);
        return box;
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) return "0.000 DT";
        return String.format("%.3f DT", value.doubleValue());
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @FXML
    public void printInvoice() {
        if (cart.getItems().isEmpty()) {
            showError("Panier vide", "Impossible d'imprimer une facture pour un panier vide.");
            return;
        }

        try {
            // Create invoice content for preview
            VBox previewInvoice = createInvoiceContent();

            // Show invoice preview dialog first
            Alert previewDialog = new Alert(Alert.AlertType.INFORMATION);
            previewDialog.setTitle("Aperçu Facture");
            previewDialog.setHeaderText("Aperçu de votre facture");
            previewDialog.getDialogPane().setContent(previewInvoice);
            previewDialog.setResizable(true);
            
            // Add custom buttons
            ButtonType printButtonType = new ButtonType("Imprimer", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
            previewDialog.getButtonTypes().setAll(printButtonType, cancelButtonType);
            
            // Show dialog and wait for response
            var result = previewDialog.showAndWait();
            
            if (result.isPresent() && result.get() == printButtonType) {
                // User wants to print - try to print
                attemptPrint();
            }
            
        } catch (Exception e) {
            System.out.println("Print error: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur d'impression", "Une erreur est survenue: " + e.getMessage());
        }
    }
    
    private void attemptPrint() {
        try {
            // Always create a new VBox for printing to avoid "already inside a parent" error
            VBox printContent = createInvoiceContent();

            // Create a new stage for printing
            javafx.stage.Stage printStage = new javafx.stage.Stage();
            printStage.setTitle("Impression Facture");
            javafx.scene.Scene scene = new javafx.scene.Scene(printContent, 600, 800);
            printStage.setScene(scene);

            // Try to print
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null) {
                System.out.println("Printer job created successfully");
                boolean success = job.showPrintDialog(printStage);
                System.out.println("Print dialog shown: " + success);
                
                if (success) {
                    boolean printed = job.printPage(printContent);
                    System.out.println("Print page result: " + printed);
                    
                    if (printed) {
                        job.endJob();
                        showInfo("Impression réussie", "La facture a été imprimée avec succès.");
                    } else {
                        showError("Erreur d'impression", "L'impression a échoué.");
                    }
                }
            } else {
                System.out.println("No printer available");
                // Fallback: show save dialog message
                Alert fallbackAlert = new Alert(Alert.AlertType.INFORMATION);
                fallbackAlert.setTitle("Impression non disponible");
                fallbackAlert.setHeaderText("Aucune imprimante disponible");
                fallbackAlert.setContentText("Aucune imprimante n'est disponible sur ce système. Vous pouvez prendre une capture d'écran de cette facture comme preuve d'achat.");
                fallbackAlert.showAndWait();
            }
            
            printStage.close();
            
        } catch (Exception e) {
            System.out.println("Print attempt error: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur d'impression", "Erreur lors de l'impression: " + e.getMessage());
        }
    }

    private VBox createInvoiceContent() {
        VBox invoice = new VBox(15);
        invoice.setPadding(new Insets(30));
        invoice.setStyle("-fx-background-color: white; -fx-border: 1px solid #ccc; -fx-border-radius: 5;");

        // Header
        Label header = new Label("FACTURE");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label company = new Label("Formini.tn - L'EXCELLENCE EN FORMATION");
        company.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        // Date and reference
        String date = java.time.LocalDate.now().toString();
        String reference = "FACT-" + System.currentTimeMillis();
        Label dateLabel = new Label("Date: " + date);
        Label refLabel = new Label("Référence: " + reference);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        HBox headerInfo = new HBox(20, dateLabel, refLabel);
        headerInfo.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Customer info placeholder
        Label customerTitle = new Label("Informations client:");
        customerTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label customerInfo = new Label("[À compléter avec les informations du client]");
        customerInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        // Products table
        Label productsTitle = new Label("Détails des produits:");
        productsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        VBox productsList = new VBox(5);
        for (CartItem item : cart.getItems()) {
            HBox productLine = new HBox(10);
            Label productName = new Label(item.getProduit().getNom());
            productName.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
            productName.setPrefWidth(200);
            
            Label qty = new Label("x" + item.getQuantity());
            qty.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            qty.setPrefWidth(50);
            
            Label price = new Label(formatMoney(item.getProduit().getPrix()));
            price.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            price.setPrefWidth(80);
            
            Label lineTotal = new Label(formatMoney(item.getLineTotal()));
            lineTotal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
            lineTotal.setPrefWidth(80);
            
            productLine.getChildren().addAll(productName, qty, price, lineTotal);
            productsList.getChildren().add(productLine);
        }

        // Total
        HBox totalLine = new HBox();
        totalLine.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label totalLabel = new Label("TOTAL: " + formatMoney(cart.getGrandTotal()));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        totalLine.getChildren().add(totalLabel);

        // Footer
        Label footer = new Label("Merci de votre confiance!");
        footer.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");
        footer.setAlignment(javafx.geometry.Pos.CENTER);

        // Add all components
        invoice.getChildren().addAll(
            header, company, headerInfo,
            new javafx.scene.control.Separator(),
            customerTitle, customerInfo,
            new javafx.scene.control.Separator(),
            productsTitle, productsList,
            new javafx.scene.control.Separator(),
            totalLine,
            new javafx.scene.control.Separator(),
            footer
        );

        return invoice;
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

