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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.print.PrinterJob;
import javafx.application.Platform;
import javafx.util.Callback;
import tn.formini.services.produitsService.CommandeService;
import tn.formini.services.cart.CartItem;
import tn.formini.services.cart.CartService;
import tn.formini.services.CartAIService;
import tn.formini.services.AdvancedProductAIService;
import tn.formini.entities.produits.Produit;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    @FXML private Button btnGetSuggestions;
    @FXML private VBox suggestionsContainer;
    @FXML private ProgressIndicator suggestionsLoading;
    @FXML private VBox noSuggestionsContainer;
    @FXML private ScrollPane suggestionsScrollPane;

    private final CartService cart = CartService.getInstance();
    private final CartAIService cartAIService = CartAIService.getInstance();
    private final AdvancedProductAIService advancedAIService = AdvancedProductAIService.getInstance();

    private enum SuggestionSort {
        ORDER, NAME, PRICE, STOCK
    }

    private SuggestionSort suggestionSort = SuggestionSort.ORDER;
    private FlowPane suggestionsCardsFlow;
    private final List<Produit> suggestionsSource = new ArrayList<>();
    private final Map<SuggestionSort, Button> suggestionSortButtons = new EnumMap<>(SuggestionSort.class);

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
        
        // Initialiser les composants de suggestions IA
        if (btnGetSuggestions != null) {
            btnGetSuggestions.setOnAction(event -> handleGetSuggestions());
            // Désactiver si le panier est vide
            btnGetSuggestions.disableProperty().bind(Bindings.isEmpty(cart.getItems()));
        }
        
        if (suggestionsLoading != null) {
            suggestionsLoading.setVisible(false);
        }

        if (suggestionsScrollPane != null) {
            suggestionsScrollPane.setVisible(false);
            suggestionsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            suggestionsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }

        if (noSuggestionsContainer != null) {
            noSuggestionsContainer.setVisible(true);
        }
    }

    @FXML
    public void clearCart() {
        cart.clear();
        suggestionsSource.clear();
        suggestionsCardsFlow = null;
        suggestionSortButtons.clear();
        if (suggestionsContainer != null) {
            suggestionsContainer.getChildren().clear();
        }
        if (noSuggestionsContainer != null) {
            noSuggestionsContainer.setVisible(true);
        }
        if (suggestionsScrollPane != null) {
            suggestionsScrollPane.setVisible(false);
        }
    }
    
    @FXML
    public void handleGetSuggestions() {
        if (cart.getItems().isEmpty()) {
            showError("Panier vide", "Ajoutez des produits au panier pour obtenir des suggestions.");
            return;
        }

        showSuggestionsLoading(true);
        if (suggestionsScrollPane != null) {
            suggestionsScrollPane.setVisible(true);
        }

        // Utiliser l'IA avancée pour des suggestions contextuelles
        List<Produit> cartProducts = cart.getItems().stream()
            .map(CartItem::getProduit)
            .toList();

        advancedAIService.getCartBasedSuggestions(cartProducts)
            .thenAccept(suggestions -> {
                System.out.println("Suggestions IA obtenues: " + suggestions.size() + " produits");
                
                // Convertir les suggestions en objets Produit (si possible)
                List<Produit> produitsSuggestionnes = convertSuggestionsToProducts(suggestions);
                
                Platform.runLater(() -> {
                    showSuggestionsLoading(false);
                    displaySuggestions(produitsSuggestionnes);
                });
            })
            .exceptionally(throwable -> {
                System.err.println("Erreur suggestions IA: " + throwable.getMessage());
                
                // Fallback vers l'ancien système
                try {
                    List<Produit> fallbackSuggestions = cartAIService.getProduitsComplementaires();
                    Platform.runLater(() -> {
                        showSuggestionsLoading(false);
                        displaySuggestions(fallbackSuggestions);
                    });
                } catch (Exception fallbackError) {
                    Platform.runLater(() -> {
                        showSuggestionsLoading(false);
                        showError("Erreur de suggestions", "Impossible d'obtenir des suggestions: " + fallbackError.getMessage());
                    });
                }
                return null;
            });
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
            private final Button details = new Button("Détails");
            private final Button remove = new Button("Retirer");
            private final HBox box = new HBox(8);
            {
                details.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-width: 2px; " +
                                "-fx-text-fill: #1976d2; -fx-font-weight: bold; -fx-font-size: 12px; " +
                                "-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 6px 12px; -fx-cursor: hand;");

                remove.setStyle("-fx-background-color: #ffebee; -fx-border-color: #f44336; -fx-border-width: 2px; " +
                               "-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 12px; " +
                               "-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 6px 12px; -fx-cursor: hand;");
                remove.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cart.remove(item);
                });

                box.getStyleClass().add("cart-actions");
                box.getChildren().addAll(details, remove);
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
    
    private void showSuggestionsLoading(boolean show) {
        if (suggestionsLoading != null) {
            suggestionsLoading.setVisible(show);
        }
    }

    /**
     * Convertit les suggestions textuelles de l'IA en objets Produit
     */
    private List<Produit> convertSuggestionsToProducts(List<String> suggestions) {
        List<Produit> produits = new ArrayList<>();
        
        System.out.println("=== CONVERSION SUGGESTIONS IA ===");
        System.out.println("Suggestions reçues: " + suggestions.size());
        for (int i = 0; i < suggestions.size(); i++) {
            String suggestion = suggestions.get(i);
            System.out.println("Suggestion " + (i+1) + ": " + suggestion);
            
            // Essayer de trouver un produit correspondant dans la base
            Produit produit = trouverProduitParNom(suggestion);
            if (produit != null) {
                produits.add(produit);
                System.out.println("✅ Produit trouvé: " + produit.getNom());
            } else {
                // Créer un produit virtuel basé sur la suggestion
                Produit virtuel = createProduitVirtuel(suggestion);
                produits.add(virtuel);
                System.out.println("🤖 Produit virtuel créé: " + virtuel.getNom());
            }
        }
        
        System.out.println("Total produits convertis: " + produits.size());
        return produits;
    }

    /**
     * Cherche un produit par nom dans la base de données
     */
    private Produit trouverProduitParNom(String nom) {
        try {
            // Utiliser une recherche floue pour trouver le produit
            List<Produit> tousProduits = cartAIService.getAllProducts();
            System.out.println("Recherche de: '" + nom + "' parmi " + tousProduits.size() + " produits");
            
            // Nettoyer la recherche
            String searchClean = nom.toLowerCase().trim();
            String[] searchWords = searchClean.split("\\s+");
            
            for (Produit produit : tousProduits) {
                String productClean = produit.getNom().toLowerCase().trim();
                
                // Recherche exacte
                if (productClean.contains(searchClean) || searchClean.contains(productClean)) {
                    System.out.println("✅ Correspondance exacte: " + produit.getNom());
                    return produit;
                }
                
                // Recherche par mots-clés
                int matchCount = 0;
                for (String word : searchWords) {
                    if (productClean.contains(word)) {
                        matchCount++;
                    }
                }
                
                // Si au moins 50% des mots correspondent
                if (matchCount > 0 && matchCount >= Math.ceil(searchWords.length * 0.5)) {
                    System.out.println("✅ Correspondance partielle (" + matchCount + "/" + searchWords.length + "): " + produit.getNom());
                    return produit;
                }
            }
            
            System.out.println("❌ Aucune correspondance trouvée pour: " + nom);
        } catch (Exception e) {
            System.err.println("Erreur recherche produit: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crée un produit virtuel basé sur la suggestion IA
     */
    private Produit createProduitVirtuel(String suggestion) {
        Produit virtuel = new Produit();
        virtuel.setId(-1); // ID négatif pour indiquer un produit virtuel
        virtuel.setNom(suggestion);
        virtuel.setDescription("🤖 Suggestion générée par IA basée sur votre panier");
        virtuel.setCategorie("Suggestion IA");
        virtuel.setPrix(new java.math.BigDecimal("0.00")); // Prix à déterminer
        virtuel.setStock(999); // Stock virtuel illimité
        virtuel.setImage("https://via.placeholder.com/200x150/4f46e5/ffffff?text=IA");
        virtuel.setStatut("Disponible");
        return virtuel;
    }

    /**
     * Affiche les suggestions avec un badge IA pour les produits virtuels
     */
    private void displayAdvancedSuggestions(List<Produit> suggestions) {
        suggestionsContainer.setVisible(true);
        suggestionsContainer.getChildren().clear();
        
        // Titre avec badge IA
        Label title = new Label("🤖 Suggestions IA Intelligentes");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4f46e5;");
        suggestionsContainer.getChildren().add(title);
        
        // Description
        Label desc = new Label("Basées sur l'analyse de votre panier avec GPT-4");
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-padding: 0 0 10px 0;");
        suggestionsContainer.getChildren().add(desc);
        
        // Conteneur pour les cartes
        FlowPane cardsFlow = new FlowPane(15, 15);
        cardsFlow.setPadding(new Insets(10));
        
        for (Produit produit : suggestions) {
            VBox card = createAdvancedSuggestionCard(produit);
            cardsFlow.getChildren().add(card);
        }
        
        ScrollPane scrollPane = new ScrollPane(cardsFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        suggestionsContainer.getChildren().add(scrollPane);
    }

    /**
     * Crée une carte de suggestion avancée
     */
    private VBox createAdvancedSuggestionCard(Produit produit) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; " +
                      "-fx-border-color: #e5e7eb; " +
                      "-fx-border-radius: 12; " +
                      "-fx-background-radius: 12; " +
                      "-fx-padding: 15; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);");

        // Header avec badge IA si virtuel
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        if (produit.getId() == -1) {
            // Badge IA pour produit virtuel
            Label badgeIA = new Label("🤖 IA");
            badgeIA.setStyle("-fx-background-color: linear-gradient(45deg, #4f46e5, #7c3aed); " +
                             "-fx-text-fill: white; " +
                             "-fx-background-radius: 20; " +
                             "-fx-font-size: 10px; " +
                             "-fx-font-weight: bold; " +
                             "-fx-padding: 2 8px;");
            header.getChildren().add(badgeIA);
        }
        
        Label nameLabel = new Label(produit.getNom());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);
        header.getChildren().add(nameLabel);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        
        try {
            if (produit.getImage() != null && !produit.getImage().trim().isEmpty()) {
                imageView.setImage(new Image(produit.getImage(), true));
            }
        } catch (Exception e) {
            // Image par défaut
        }
        
        // Description
        Label descLabel = new Label(produit.getDescription());
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(180);
        
        // Prix et badge spécial
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label priceLabel = new Label(formatMoney(produit.getPrix()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #059669;");
        
        if (produit.getId() == -1) {
            Label virtuelLabel = new Label("Virtuel");
            virtuelLabel.setStyle("-fx-background-color: #f3f4f6; " +
                                 "-fx-text-fill: #6b7280; " +
                                 "-fx-background-radius: 4; " +
                                 "-fx-font-size: 9px; " +
                                 "-fx-padding: 2 6px;");
            priceBox.getChildren().addAll(priceLabel, virtuelLabel);
        } else {
            priceBox.getChildren().add(priceLabel);
        }

        // Boutons d'action
        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button detailsBtn = new Button("👁 Détails");
        detailsBtn.setStyle("-fx-background-color: #f3f4f6; " +
                           "-fx-text-fill: #374151; " +
                           "-fx-background-radius: 6; " +
                           "-fx-font-size: 11px; " +
                           "-fx-cursor: hand;");
        detailsBtn.setOnAction(e -> showProductDetails(produit));
        
        if (produit.getId() == -1) {
            // Pour les produits virtuels, bouton de recherche
            Button searchBtn = new Button("🔍 Rechercher");
            searchBtn.setStyle("-fx-background-color: #3b82f6; " +
                              "-fx-text-fill: white; " +
                              "-fx-background-radius: 6; " +
                              "-fx-font-size: 11px; " +
                              "-fx-cursor: hand;");
            searchBtn.setOnAction(e -> searchForProduct(produit.getNom()));
            actions.getChildren().addAll(detailsBtn, searchBtn);
        } else {
            // Pour les produits réels, bouton d'ajout
            Button addBtn = new Button("🛒 Ajouter");
            addBtn.setStyle("-fx-background-color: #22c55e; " +
                           "-fx-text-fill: white; " +
                           "-fx-background-radius: 6; " +
                           "-fx-font-size: 11px; " +
                           "-fx-cursor: hand;");
            addBtn.setDisable(produit.getStock() <= 0);
            addBtn.setOnAction(e -> {
                cart.add(produit, 1);
                refreshCart();
                showSuccess("Produit ajouté", produit.getNom() + " a été ajouté au panier");
            });
            actions.getChildren().addAll(detailsBtn, addBtn);
        }

        card.getChildren().addAll(header, imageView, descLabel, priceBox, actions);
        return card;
    }

    /**
     * Recherche un produit dans la boutique
     */
    private void searchForProduct(String productName) {
        try {
            // Naviguer vers la boutique avec le terme de recherche
            FrontMainController.getInstance().loadView("/fxml/frontend/Shop.fxml");
            // TODO: Passer le terme de recherche au ShopController
        } catch (Exception e) {
            showError("Erreur navigation", "Impossible d'ouvrir la boutique: " + e.getMessage());
        }
    }

    /**
     * Affiche les détails d'un produit
     */
    private void showProductDetails(Produit produit) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Détails du produit");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        
        Label name = new Label(produit.getNom());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label category = new Label("Catégorie: " + produit.getCategorie());
        Label price = new Label("Prix: " + formatMoney(produit.getPrix()));
        Label stock = new Label("Stock: " + produit.getStock());
        Label desc = new Label(produit.getDescription());
        desc.setWrapText(true);
        
        if (produit.getId() == -1) {
            Label iaInfo = new Label("🤖 Ce produit est une suggestion générée par IA");
            iaInfo.setStyle("-fx-text-fill: #4f46e5; -fx-font-style: italic;");
            content.getChildren().addAll(name, category, price, stock, iaInfo, new Label("Description:"), desc);
        } else {
            content.getChildren().addAll(name, category, price, stock, new Label("Description:"), desc);
        }
        
        details.getDialogPane().setContent(content);
        details.showAndWait();
    }

    /**
     * Affiche un message de succès
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Rafraîchit l'affichage du panier
     */
    private void refreshCart() {
        table.refresh();
        labelGrandTotal.setText(formatMoney(cart.getGrandTotal()));
        refreshCartHint();
    }

    /**
     * Rafraîchit l'indicateur du panier
     */
    private void refreshCartHint() {
        // Cette méthode mettrait à jour un indicateur d'articles dans le panier
        // Pour l'instant, nous la laissons vide car il n'y a pas de labelCartHint dans CartController
        // Si besoin, on peut ajouter un compteur d'articles ici
    }

    private void displaySuggestions(List<Produit> produits) {
        if (suggestionsContainer == null) {
            return;
        }

        if (suggestionsScrollPane != null) {
            suggestionsScrollPane.setVisible(true);
        }
        if (noSuggestionsContainer != null) {
            noSuggestionsContainer.setVisible(false);
        }

        suggestionsSource.clear();
        suggestionSortButtons.clear();
        suggestionsContainer.getChildren().clear();

        if (produits == null || produits.isEmpty()) {
            Label emptyLabel = new Label(
                    "Aucune suggestion pour le moment. Vérifiez que le catalogue contient d'autres produits "
                            + "dans les mêmes catégories que votre panier (hors articles déjà ajoutés).");
            emptyLabel.getStyleClass().add("cart-suggestion-meta");
            emptyLabel.setWrapText(true);
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            suggestionsContainer.getChildren().add(emptyLabel);
            return;
        }

        suggestionSort = SuggestionSort.ORDER;
        suggestionsSource.addAll(produits);

        VBox toolbar = buildSuggestionToolbar(produits.size());
        suggestionsCardsFlow = new FlowPane();
        suggestionsCardsFlow.getStyleClass().add("cart-suggestions-flow");
        suggestionsCardsFlow.setPrefWrapLength(640);
        if (suggestionsScrollPane != null) {
            suggestionsCardsFlow.prefWrapLengthProperty().bind(
                    suggestionsScrollPane.widthProperty().subtract(56));
        }

        fillSuggestionCards();

        Button hideBtn = new Button("Masquer les suggestions");
        hideBtn.setStyle("-fx-background-color: #f3e5f5; -fx-border-color: #9c27b0; -fx-border-width: 2px; " +
                        "-fx-text-fill: #7b1fa2; -fx-font-weight: bold; -fx-font-size: 14px; " +
                        "-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 10px 16px; -fx-cursor: hand;");
        hideBtn.setMaxWidth(Double.MAX_VALUE);
        hideBtn.setOnAction(e -> hideSuggestionsPanel());

        suggestionsContainer.getChildren().addAll(toolbar, suggestionsCardsFlow, hideBtn);
    }

    private VBox buildSuggestionToolbar(int count) {
        VBox wrap = new VBox(8);
        wrap.getStyleClass().add("cart-suggestions-toolbar-wrap");

        List<String> categoriesPanier = cart.getItems().stream()
                .map(i -> i.getProduit() != null ? i.getProduit().getCategorie() : null)
                .filter(c -> c != null && !c.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .toList();
        if (!categoriesPanier.isEmpty()) {
            Label filterHint = new Label("🎯 Uniquement les catégories de votre panier : " + String.join(" · ", categoriesPanier));
            filterHint.setStyle("-fx-background-color: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%); " +
                             "-fx-text-fill: #2d3436; -fx-font-weight: bold; -fx-font-size: 13px; " +
                             "-fx-background-radius: 10px; -fx-padding: 10px 14px; " +
                             "-fx-effect: dropshadow(gaussian, rgba(253,203,110,0.3), 2, 0, 0, 1);");
            filterHint.setWrapText(true);
            filterHint.setMaxWidth(Double.MAX_VALUE);
            wrap.getChildren().add(filterHint);
        }

        HBox bar = new HBox(8);
        bar.getStyleClass().add("cart-suggestions-toolbar");
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String countText = count == 1 ? "1 produit proposé" : count + " produits proposés";
        Label countLabel = new Label(countText);
        countLabel.getStyleClass().add("cart-suggestions-count");
        countLabel.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        suggestionSortButtons.clear();
        Button bOrder = makeSortButton("Ordre", SuggestionSort.ORDER);
        Button bName = makeSortButton("A → Z", SuggestionSort.NAME);
        Button bPrice = makeSortButton("Prix", SuggestionSort.PRICE);
        Button bStock = makeSortButton("Stock", SuggestionSort.STOCK);

        suggestionSortButtons.put(SuggestionSort.ORDER, bOrder);
        suggestionSortButtons.put(SuggestionSort.NAME, bName);
        suggestionSortButtons.put(SuggestionSort.PRICE, bPrice);
        suggestionSortButtons.put(SuggestionSort.STOCK, bStock);

        updateSortButtonStyles();
        bar.getChildren().addAll(countLabel, spacer, bOrder, bName, bPrice, bStock);
        wrap.getChildren().add(bar);
        return wrap;
    }

    private Button makeSortButton(String text, SuggestionSort mode) {
        Button b = new Button(text);
        
        // Couleurs différentes selon le mode
        String colorStyle = switch (mode) {
            case ORDER -> "-fx-background-color: #e8f5e8; -fx-border-color: #4caf50; -fx-border-width: 2px; " +
                       "-fx-text-fill: #2e7d32;";
            case NAME -> "-fx-background-color: #fff3e0; -fx-border-color: #ff9800; -fx-border-width: 2px; " +
                       "-fx-text-fill: #e65100;";
            case PRICE -> "-fx-background-color: #e3f2fd; -fx-border-color: #03a9f4; -fx-border-width: 2px; " +
                       "-fx-text-fill: #0277bd;";
            case STOCK -> "-fx-background-color: #fce4ec; -fx-border-color: #e91e63; -fx-border-width: 2px; " +
                       "-fx-text-fill: #c2185b;";
        };
        
        b.setStyle(colorStyle +
                  "-fx-font-weight: bold; -fx-font-size: 12px; " +
                  "-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 6px 12px; -fx-cursor: hand;");
        b.getStyleClass().addAll("cart-sort-btn", "cart-sort-" + mode.name().toLowerCase());
        b.setMnemonicParsing(false);
        b.setOnAction(e -> {
            suggestionSort = mode;
            updateSortButtonStyles();
            fillSuggestionCards();
        });
        return b;
    }

    private void updateSortButtonStyles() {
        suggestionSortButtons.forEach((sort, btn) -> {
            btn.getStyleClass().removeAll("cart-sort-btn-active");
            if (sort == suggestionSort) {
                btn.getStyleClass().add("cart-sort-btn-active");
            }
        });
    }

    private void fillSuggestionCards() {
        if (suggestionsCardsFlow == null) {
            return;
        }
        suggestionsCardsFlow.getChildren().clear();
        List<Produit> ordered = new ArrayList<>(suggestionsSource);
        if (suggestionSort != SuggestionSort.ORDER) {
            ordered.sort(comparatorFor(suggestionSort));
        }
        int rank = 1;
        for (Produit p : ordered) {
            suggestionsCardsFlow.getChildren().add(createSuggestionCard(p, rank++));
        }
    }

    private Comparator<Produit> comparatorFor(SuggestionSort sort) {
        return switch (sort) {
            case ORDER -> Comparator.comparingInt(suggestionsSource::indexOf);
            case NAME -> Comparator.comparing(
                    (Produit p) -> nullToEmpty(p.getNom()), String.CASE_INSENSITIVE_ORDER);
            case PRICE -> Comparator.comparing(Produit::getPrix, Comparator.nullsLast(BigDecimal::compareTo));
            case STOCK -> Comparator.comparingInt(Produit::getStock).reversed();
        };
    }

    private VBox createSuggestionCard(Produit produit, int rank) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1px; " +
                     "-fx-border-radius: 8px; -fx-background-radius: 8px; " +
                     "-fx-padding: 16px;");
        card.setFillWidth(true);

        // Header simple
        HBox topRow = new HBox(12);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label rankBadge = new Label("#" + rank);
        rankBadge.setStyle("-fx-background-color: #667eea; " +
                          "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; " +
                          "-fx-background-radius: 10px; -fx-padding: 4px 8px;");
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        
        // Catégorie simple
        String cat = produit.getCategorie() == null || produit.getCategorie().isBlank()
                ? "Non catégorisé"
                : produit.getCategorie();
        Label categoryBadge = new Label(cat);
        categoryBadge.setStyle("-fx-background-color: #6c757d; " +
                              "-fx-text-fill: white; -fx-font-size: 11px; " +
                              "-fx-background-radius: 6px; -fx-padding: 3px 6px;");
        
        topRow.getChildren().addAll(rankBadge, grow, categoryBadge);

        // Image simple
        HBox thumbRow = new HBox();
        thumbRow.setAlignment(javafx.geometry.Pos.CENTER);
        thumbRow.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-width: 1px; " +
                         "-fx-border-radius: 6px; -fx-padding: 10px;");
        ImageView img = new ImageView();
        img.setFitWidth(120);
        img.setFitHeight(120);
        img.setPreserveRatio(true);
        img.setSmooth(true);
        if (produit.getImage() != null && !produit.getImage().trim().isEmpty()) {
            try {
                img.setImage(new Image(produit.getImage().trim(), true));
            } catch (Exception ignored) {
            }
        }
        thumbRow.getChildren().add(img);

        // Nom du produit
        Label name = new Label(nullToEmpty(produit.getNom()));
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; " +
                     "-fx-wrap-text: true; -fx-max-width: 260px;");

        // Prix simple
        Label price = new Label(formatMoney(produit.getPrix()));
        price.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        // Stock simple
        String stockTxt = produit.getStock() <= 0
                ? "Rupture de stock"
                : produit.getStock() + " disponibles";
        Label stockLabel = new Label(stockTxt);
        stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + 
                        (produit.getStock() > 0 ? "#28a745" : "#dc3545") + ";");

        // Description si disponible
        String desc = nullToEmpty(produit.getDescription()).trim();
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Bouton d'ajout simple
        Button add = new Button("Ajouter au panier");
        add.setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #4caf50; -fx-border-width: 2px; " +
                    "-fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-font-size: 14px; " +
                    "-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 10px 16px; -fx-cursor: hand;");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setDisable(produit.getStock() <= 0);
        
        add.setOnAction(e -> {
            cart.add(produit);
            showInfo("Ajouté", nullToEmpty(produit.getNom()) + " a été ajouté au panier.");
        });

        // Assemblage de la carte
        card.getChildren().addAll(topRow, thumbRow, name, price, stockLabel);
        if (!desc.isEmpty()) {
            Label descLabel = new Label(desc);
            descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-wrap-text: true; " +
                              "-fx-max-width: 260px; -fx-font-style: italic;");
            card.getChildren().add(descLabel);
        }
        card.getChildren().addAll(spacer, add);
        return card;
    }

    private void hideSuggestionsPanel() {
        if (suggestionsScrollPane != null) {
            suggestionsScrollPane.setVisible(false);
        }
        if (noSuggestionsContainer != null) {
            noSuggestionsContainer.setVisible(true);
        }
    }
}

