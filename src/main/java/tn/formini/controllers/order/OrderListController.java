package tn.formini.controllers.order;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.controllers.MainController;
import tn.formini.entities.order.Order;
import tn.formini.services.order.OrderService;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Consumer;

public class OrderListController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button refreshButton;
    @FXML private Button addOrderButton;
    @FXML private FlowPane ordersContainer;
    @FXML private Label totalOrdersLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label processingOrdersLabel;
    @FXML private Label deliveredOrdersLabel;
    @FXML private HBox paginationContainer;
    @FXML private Button firstPageBtn;
    @FXML private Button prevPageBtn;
    @FXML private Label currentPageLabel;
    @FXML private Button nextPageBtn;
    @FXML private Button lastPageBtn;
    @FXML private Label totalItemsLabel;

    private OrderService orderService;
    private MainController mainController;
    private Consumer<Void> onClose;
    private List<Order> allOrders;
    private List<Order> filteredOrders;
    
    // Pagination settings
    private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        try {
            orderService = new OrderService();
            setupStatusFilter();
            setupSearchListener();
            initializePagination();
            loadOrders();
            updateStatistics();
        } catch (Exception e) {
            System.err.println("Error initializing order list: " + e.getMessage());
            showError("Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void setupStatusFilter() {
        statusFilter.getItems().addAll(
            "Tous les statuts",
            Order.STATUS_PENDING,
            Order.STATUS_CONFIRMED,
            Order.STATUS_PROCESSING,
            Order.STATUS_SHIPPED,
            Order.STATUS_DELIVERED,
            Order.STATUS_CANCELED,
            Order.STATUS_RETURNED
        );
        statusFilter.setValue("Tous les statuts");
        
        statusFilter.setOnAction(e -> filterOrders());
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterOrders());
    }

    private void loadOrders() {
        try {
            if (orderService != null) {
                allOrders = orderService.getAllOrders();
                filteredOrders = allOrders;
                updatePagination();
                displayPaginatedOrders();
            } else {
                showError("Service de commande non disponible");
            }
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            showError("Erreur lors du chargement des commandes: " + e.getMessage());
        }
    }

    private void displayOrders(List<Order> orders) {
        ordersContainer.getChildren().clear();
        
        for (Order order : orders) {
            VBox orderCard = createOrderCard(order);
            ordersContainer.getChildren().add(orderCard);
        }
        
        updateStatistics();
    }
    
    private void initializePagination() {
        // Set pagination button symbols
        firstPageBtn.setText("«");
        prevPageBtn.setText("«");
        nextPageBtn.setText("»");
        lastPageBtn.setText("»");
        
        // Set button actions
        firstPageBtn.setOnAction(e -> goToFirstPage());
        prevPageBtn.setOnAction(e -> goToPreviousPage());
        nextPageBtn.setOnAction(e -> goToNextPage());
        lastPageBtn.setOnAction(e -> goToLastPage());
    }
    
    private void updatePagination() {
        if (filteredOrders == null) {
            totalPages = 0;
        } else {
            totalPages = (int) Math.ceil((double) filteredOrders.size() / ITEMS_PER_PAGE);
        }
        
        // Update pagination controls
        currentPageLabel.setText("Page " + currentPage + " sur " + totalPages);
        totalItemsLabel.setText(filteredOrders != null ? filteredOrders.size() + " commandes" : "0 commandes");
        
        // Enable/disable buttons
        firstPageBtn.setDisable(currentPage <= 1);
        prevPageBtn.setDisable(currentPage <= 1);
        nextPageBtn.setDisable(currentPage >= totalPages);
        lastPageBtn.setDisable(currentPage >= totalPages);
        
        // Show/hide pagination container
        paginationContainer.setVisible(totalPages > 1);
    }
    
    private void displayPaginatedOrders() {
        if (filteredOrders == null || filteredOrders.isEmpty()) {
            ordersContainer.getChildren().clear();
            return;
        }
        
        // Get items for current page
        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredOrders.size());
        
        List<Order> currentPageOrders = filteredOrders.subList(startIndex, endIndex);
        displayOrders(currentPageOrders);
    }
    
    @FXML
    private void goToFirstPage() {
        currentPage = 1;
        updatePagination();
        displayPaginatedOrders();
    }
    
    @FXML
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
            displayPaginatedOrders();
        }
    }
    
    @FXML
    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
            displayPaginatedOrders();
        }
    }
    
    @FXML
    private void goToLastPage() {
        currentPage = totalPages;
        updatePagination();
        displayPaginatedOrders();
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox();
        card.setSpacing(8);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12; " +
                     "-fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(300);

        // Header with order number and status
        HBox header = new HBox();
        header.setSpacing(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Order number
        Label orderNumberLabel = new Label(order.getOrderNumber());
        orderNumberLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        orderNumberLabel.setStyle("-fx-text-fill: #1f2937;");

        // Status badge
        Label statusBadge = new Label(order.getStatus());
        statusBadge.setStyle("-fx-background-color: " + order.getStatusColor() + "; -fx-text-fill: white; " +
                           "-fx-background-radius: 20; -fx-padding: 5 12; -fx-font-size: 12;");
        statusBadge.setFont(Font.font("System", FontWeight.BOLD, 12));

        header.getChildren().addAll(orderNumberLabel, statusBadge);
        header.setHgrow(orderNumberLabel, javafx.scene.layout.Priority.ALWAYS);

        // Customer info
        Label customerLabel = new Label(order.getCustomerName());
        customerLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        customerLabel.setStyle("-fx-text-fill: #374151;");

        Label emailLabel = new Label(order.getCustomerEmail());
        emailLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");

        // Order details
        VBox detailsBox = new VBox();
        detailsBox.setSpacing(5);

        Label amountLabel = new Label(String.format("Montant: %.3f DT", order.getTotalAmount()));
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        amountLabel.setStyle("-fx-text-fill: #059669;");

        Label dateLabel = new Label("Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(order.getOrderDate()));
        dateLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");

        Label itemsLabel = new Label(order.getItemCount() + " article(s)");
        itemsLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");

        detailsBox.getChildren().addAll(amountLabel, dateLabel, itemsLabel);

        // Action buttons
        HBox actionsBox = new HBox();
        actionsBox.setSpacing(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // View button (eye icon)
        Button viewButton = createViewButton();
        viewButton.setOnAction(e -> viewOrderDetails(order));

        // Edit button
        Button editButton = createEditButton();
        editButton.setOnAction(e -> editOrder(order));

        // Delete button
        Button deleteButton = createDeleteButton();
        deleteButton.setOnAction(e -> deleteOrder(order));

        actionsBox.getChildren().addAll(viewButton, editButton, deleteButton);

        // Add all components to card
        card.getChildren().addAll(header, customerLabel, emailLabel, detailsBox, actionsBox);

        return card;
    }

    private Button createViewButton() {
        Button button = new Button();
        button.setPrefSize(35, 32);
        button.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        button.setText("👁");
        return button;
    }

    private Button createEditButton() {
        Button button = new Button();
        button.setPrefSize(90, 32);
        button.setStyle("-fx-background-color: #10b981; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
        button.setText("modifier");
        return button;
    }

    private Button createDeleteButton() {
        Button button = new Button();
        button.setPrefSize(100, 32);
        button.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
        button.setText("supprimer");
        return button;
    }

    private void filterOrders() {
        String searchTerm = searchField.getText().toLowerCase();
        String selectedStatus = statusFilter.getValue();

        filteredOrders = allOrders.stream()
            .filter(order -> {
                boolean matchesSearch = searchTerm.isEmpty() || 
                    order.getOrderNumber().toLowerCase().contains(searchTerm) ||
                    order.getCustomerName().toLowerCase().contains(searchTerm) ||
                    order.getCustomerEmail().toLowerCase().contains(searchTerm);

                boolean matchesStatus = selectedStatus.equals("Tous les statuts") || 
                    order.getStatus().equals(selectedStatus);

                return matchesSearch && matchesStatus;
            })
            .toList();

        currentPage = 1;
        updatePagination();
        displayPaginatedOrders();
    }

    private void updateStatistics() {
        int total = allOrders.size();
        int pending = (int) allOrders.stream().filter(o -> o.getStatus().equals(Order.STATUS_PENDING)).count();
        int processing = (int) allOrders.stream().filter(o -> o.getStatus().equals(Order.STATUS_PROCESSING)).count();
        int delivered = (int) allOrders.stream().filter(o -> o.getStatus().equals(Order.STATUS_DELIVERED)).count();

        totalOrdersLabel.setText(String.valueOf(total));
        pendingOrdersLabel.setText(String.valueOf(pending));
        processingOrdersLabel.setText(String.valueOf(processing));
        deliveredOrdersLabel.setText(String.valueOf(delivered));
    }

    @FXML
    private void refreshOrders() {
        loadOrders();
        showInfo("Commandes actualisées avec succès!");
    }

    @FXML
    private void addNewOrder() {
        // TODO: Implement add order functionality
        showInfo("Fonctionnalité d'ajout de commande à implémenter!");
    }

    private void viewOrderDetails(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order/OrderDetails.fxml"));
            VBox root = loader.load();
            
            OrderDetailsController controller = loader.getController();
            controller.setOrder(order);
            
            Stage stage = new Stage();
            stage.setTitle("Détails de la commande: " + order.getOrderNumber());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture des détails: " + e.getMessage());
        }
    }

    private void editOrder(Order order) {
        try {
            System.out.println("DEBUG: Edit order clicked for order: " + order.getOrderNumber());
            
            // Check if FXML resource exists
            URL fxmlResource = getClass().getResource("/fxml/order/OrderEditForm.fxml");
            System.out.println("DEBUG: FXML resource: " + (fxmlResource != null ? "FOUND" : "NOT FOUND"));
            
            if (fxmlResource == null) {
                showError("FXML file not found: /fxml/order/OrderEditForm.fxml");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlResource);
            System.out.println("DEBUG: Loading FXML...");
            
            VBox root = loader.load();
            System.out.println("DEBUG: FXML loaded successfully");
            
            OrderEditFormController controller = loader.getController();
            System.out.println("DEBUG: Controller: " + (controller != null ? "FOUND" : "NOT FOUND"));
            
            if (controller == null) {
                showError("Controller not found in FXML");
                return;
            }
            
            controller.setOrder(order);
            controller.setOnOrderUpdated(this::loadOrders);
            System.out.println("DEBUG: Order set and callback configured");
            
            Stage stage = new Stage();
            stage.setTitle("Modifier la commande: " + order.getOrderNumber());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.setWidth(600);
            stage.setHeight(500);
            stage.setScene(new javafx.scene.Scene(root));
            System.out.println("DEBUG: Stage created and configured");
            
            stage.showAndWait();
            System.out.println("DEBUG: Edit form closed");
            
        } catch (IOException e) {
            System.err.println("ERROR: IOException in editOrder: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR: General exception in editOrder: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur inattendue: " + e.getMessage());
        }
    }

    private void deleteOrder(Order order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer la commande");
        alert.setHeaderText("Supprimer \"" + order.getOrderNumber() + "\" ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette commande ? Cette action est irréversible.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            if (orderService.deleteOrder(order.getId())) {
                showInfo("Commande supprimée avec succès!");
                loadOrders();
            } else {
                showError("Erreur lors de la suppression de la commande.");
            }
        }
    }

    @FXML
    public void goBack() {
        if (onClose != null) {
            onClose.accept(null);
        } else if (mainController != null) {
            mainController.showDashboard();
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Setters
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setOnClose(Consumer<Void> onClose) {
        this.onClose = onClose;
    }
}
