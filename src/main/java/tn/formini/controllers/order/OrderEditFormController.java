package tn.formini.controllers.order;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.formini.entities.order.Order;
import tn.formini.services.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderEditFormController {

    @FXML private Label orderNumberLabel;
    @FXML private TextField customerNameField;
    @FXML private TextField customerEmailField;
    @FXML private TextField customerPhoneField;
    @FXML private TextArea shippingAddressArea;
    @FXML private TextField totalAmountField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private ComboBox<String> paymentStatusComboBox;
    @FXML private TextArea notesArea;
    @FXML private TextField itemCountField;
    @FXML private TextArea itemsDescriptionArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label validationLabel;
    
    // Validation error labels
    @FXML private Label customerNameError;
    @FXML private Label customerEmailError;
    @FXML private Label customerPhoneError;
    @FXML private Label totalAmountError;
    @FXML private Label itemCountError;
    @FXML private Label shippingAddressError;
    @FXML private Label statusError;
    @FXML private Label paymentMethodError;
    @FXML private Label paymentStatusError;

    @Autowired
    private OrderService orderService;
    private Order currentOrder;
    private Runnable onOrderUpdated;

    @FXML
    public void initialize() {
        System.out.println("DEBUG: OrderEditFormController initialize() called");
        
        try {
            System.out.println("DEBUG: OrderService injected: " + (orderService != null));
            
            setupComboBoxes();
            System.out.println("DEBUG: ComboBoxes setup completed");
            
            setupValidation();
            System.out.println("DEBUG: Validation setup completed");
            
            // Check if all FXML fields are injected
            System.out.println("DEBUG: customerNameField = " + (customerNameField != null ? "OK" : "NULL"));
            System.out.println("DEBUG: totalAmountField = " + (totalAmountField != null ? "OK" : "NULL"));
            System.out.println("DEBUG: statusComboBox = " + (statusComboBox != null ? "OK" : "NULL"));
            System.out.println("DEBUG: saveButton = " + (saveButton != null ? "OK" : "NULL"));
            System.out.println("DEBUG: validationLabel = " + (validationLabel != null ? "OK" : "NULL"));
            System.out.println("DEBUG: customerNameError = " + (customerNameError != null ? "OK" : "NULL"));
            System.out.println("DEBUG: totalAmountError = " + (totalAmountError != null ? "OK" : "NULL"));
            System.out.println("DEBUG: itemCountError = " + (itemCountError != null ? "OK" : "NULL"));
            
        } catch (Exception e) {
            System.err.println("ERROR: Exception in OrderEditFormController.initialize(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupComboBoxes() {
        // Status options
        statusComboBox.getItems().addAll(
            Order.STATUS_PENDING,
            Order.STATUS_CONFIRMED,
            Order.STATUS_PROCESSING,
            Order.STATUS_SHIPPED,
            Order.STATUS_DELIVERED,
            Order.STATUS_CANCELED,
            Order.STATUS_RETURNED
        );

        // Payment method options
        paymentMethodComboBox.getItems().addAll(
            "Carte de crédit",
            "Espèces",
            "PayPal",
            "Virement bancaire",
            "Chèque",
            "Autre"
        );

        // Payment status options
        paymentStatusComboBox.getItems().addAll(
            "en attente",
            "payé",
            "partiellement payé",
            "remboursé"
        );

        // Add listeners for validation
        statusComboBox.setOnAction(e -> validateStatusTransition());
    }

    private void setupValidation() {
        // Add individual field validation listeners
        totalAmountField.textProperty().addListener((obs, oldVal, newVal) -> validateTotalAmount());
        customerPhoneField.textProperty().addListener((obs, oldVal, newVal) -> validateCustomerPhone());
        itemCountField.textProperty().addListener((obs, oldVal, newVal) -> validateItemCount());
        shippingAddressArea.textProperty().addListener((obs, oldVal, newVal) -> validateShippingAddress());
        
        // Add listeners for status and payment fields
        statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateStatus());
        paymentMethodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validatePaymentMethod());
        paymentStatusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validatePaymentStatus());
    }

    // ===== INDIVIDUAL VALIDATION METHODS =====
    
    private boolean validateTotalAmount() {
        String amountStr = totalAmountField.getText().trim();
        
        clearError(totalAmountError);
        
        if (amountStr.isEmpty()) {
            showError(totalAmountError, "Le montant total est obligatoire.");
            return false;
        }
        
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showError(totalAmountError, "Le montant doit être strictement positif.");
                return false;
            }
            if (amount < 0.01) {
                showError(totalAmountError, "Le montant minimum est de 0.01 DT.");
                return false;
            }
            if (amount > 999999.99) {
                showError(totalAmountError, "Le montant ne peut pas dépasser 999,999.99 DT.");
                return false;
            }
            // Check for reasonable decimal places
            if (amountStr.contains(".") && amountStr.split("\\.")[1].length() > 2) {
                showError(totalAmountError, "Le montant ne peut pas avoir plus de 2 décimales.");
                return false;
            }
            
        } catch (NumberFormatException e) {
            showError(totalAmountError, "Le montant doit être un nombre valide (ex: 25.99).");
            return false;
        }
        
        return true;
    }
    
    private boolean validateCustomerPhone() {
        String phone = customerPhoneField.getText().trim();
        
        clearError(customerPhoneError);
        
        if (phone.isEmpty()) {
            showError(customerPhoneError, "Le téléphone est obligatoire.");
            return false;
        }
        if (phone.length() < 8) {
            showError(customerPhoneError, "Le téléphone doit contenir au moins 8 chiffres.");
            return false;
        }
        if (phone.length() > 20) {
            showError(customerPhoneError, "Le téléphone ne peut pas dépasser 20 caractères.");
            return false;
        }
        if (!phone.matches("^[+]?[0-9\\s\\-()]{8,20}$")) {
            showError(customerPhoneError, "Format de téléphone invalide (ex: +216 12 345 678).");
            return false;
        }
        
        return true;
    }
    
    private boolean validateItemCount() {
        String countStr = itemCountField.getText().trim();
        
        clearError(itemCountError);
        
        if (countStr.isEmpty()) {
            showError(itemCountError, "La quantité est obligatoire.");
            return false;
        }
        
        try {
            int count = Integer.parseInt(countStr);
            if (count <= 0) {
                showError(itemCountError, "La quantité doit être strictement positive.");
                return false;
            }
            if (count > 10000) {
                showError(itemCountError, "La quantité ne peut pas dépasser 10,000 articles.");
                return false;
            }
            
        } catch (NumberFormatException e) {
            showError(itemCountError, "La quantité doit être un nombre entier valide.");
            return false;
        }
        
        return true;
    }
    
    private boolean validateShippingAddress() {
        String address = shippingAddressArea.getText().trim();
        
        clearError(shippingAddressError);
        
        if (address.isEmpty()) {
            showError(shippingAddressError, "L'adresse de livraison est obligatoire.");
            return false;
        }
        if (address.length() < 10) {
            showError(shippingAddressError, "L'adresse doit contenir au moins 10 caractères.");
            return false;
        }
        if (address.length() > 500) {
            showError(shippingAddressError, "L'adresse ne peut pas dépasser 500 caractères.");
            return false;
        }
        // Check for reasonable content
        if (address.matches("^(.)\\1{5,}$")) {
            showError(shippingAddressError, "L'adresse semble invalide (caractères répétés).");
            return false;
        }
        
        return true;
    }
    
    private boolean validateStatus() {
        String status = statusComboBox.getValue();
        
        clearError(statusError);
        
        if (status == null || status.trim().isEmpty()) {
            showError(statusError, "Le statut est obligatoire.");
            return false;
        }
        
        return true;
    }
    
    private boolean validatePaymentMethod() {
        String method = paymentMethodComboBox.getValue();
        
        clearError(paymentMethodError);
        
        if (method == null || method.trim().isEmpty()) {
            showError(paymentMethodError, "La méthode de paiement est obligatoire.");
            return false;
        }
        
        return true;
    }
    
    private boolean validatePaymentStatus() {
        String status = paymentStatusComboBox.getValue();
        
        clearError(paymentStatusError);
        
        if (status == null || status.trim().isEmpty()) {
            showError(paymentStatusError, "Le statut de paiement est obligatoire.");
            return false;
        }
        
        return true;
    }
    
    private boolean validateAllFields() {
        boolean isValid = true;
        isValid &= validateTotalAmount();
        isValid &= validateCustomerPhone();
        isValid &= validateItemCount();
        isValid &= validateShippingAddress();
        isValid &= validateStatus();
        isValid &= validatePaymentMethod();
        isValid &= validatePaymentStatus();
        return isValid;
    }
    
    private void clearError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }
    
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private boolean validateField() {
        return validateAllFields();
    }

    private void clearErrorMessages() {
        customerNameError.setText("");
        customerNameError.setVisible(false);
        customerEmailError.setText("");
        customerEmailError.setVisible(false);
        customerPhoneError.setText("");
        customerPhoneError.setVisible(false);
        totalAmountError.setText("");
        totalAmountError.setVisible(false);
        itemCountError.setText("");
        itemCountError.setVisible(false);
    }

    private boolean validateStatusTransition() {
        if (currentOrder == null || statusComboBox.getValue() == null) {
            return true;
        }

        String currentStatus = currentOrder.getStatus();
        String newStatus = statusComboBox.getValue();

        // Allow any status change for now, but you can add business rules here
        // For example: delivered orders cannot be changed to pending
        if (Order.STATUS_DELIVERED.equals(currentStatus) && Order.STATUS_PENDING.equals(newStatus)) {
            validationLabel.setText("Une commande livrée ne peut pas être remise en attente.");
            return false;
        }

        if (Order.STATUS_CANCELED.equals(currentStatus) && !Order.STATUS_CANCELED.equals(newStatus)) {
            validationLabel.setText("Une commande annulée ne peut pas changer de statut.");
            return false;
        }

        return true;
    }

    public void setOrder(Order order) {
        System.out.println("DEBUG: setOrder() called with order: " + (order != null ? order.getOrderNumber() : "NULL"));
        this.currentOrder = order;
        populateFields();
        System.out.println("DEBUG: populateFields() completed");
    }

    private void populateFields() {
        System.out.println("DEBUG: populateFields() started");
        
        if (currentOrder == null) {
            System.out.println("DEBUG: currentOrder is null, returning");
            return;
        }

        try {
            System.out.println("DEBUG: Setting order number: " + currentOrder.getOrderNumber());
            orderNumberLabel.setText(currentOrder.getOrderNumber());
            
            // Disable customer name and email fields (display-only)
            customerNameField.setText(currentOrder.getCustomerName());
            customerNameField.setDisable(true);
            customerNameField.setStyle("-fx-opacity: 0.7; -fx-background-color: #f3f4f6;");
            
            customerEmailField.setText(currentOrder.getCustomerEmail());
            customerEmailField.setDisable(true);
            customerEmailField.setStyle("-fx-opacity: 0.7; -fx-background-color: #f3f4f6;");
            
            System.out.println("DEBUG: Setting customer phone: " + currentOrder.getCustomerPhone());
            customerPhoneField.setText(currentOrder.getCustomerPhone());
            
            System.out.println("DEBUG: Setting shipping address: " + currentOrder.getShippingAddress());
            shippingAddressArea.setText(currentOrder.getShippingAddress());
            
            System.out.println("DEBUG: Setting total amount: " + currentOrder.getTotalAmount());
            totalAmountField.setText(currentOrder.getTotalAmount().toString());
            
            System.out.println("DEBUG: Setting status: " + currentOrder.getStatus());
            statusComboBox.setValue(currentOrder.getStatus());
            
            System.out.println("DEBUG: Setting payment method: " + currentOrder.getPaymentMethod());
            paymentMethodComboBox.setValue(currentOrder.getPaymentMethod());
            
            System.out.println("DEBUG: Setting payment status: " + currentOrder.getPaymentStatus());
            paymentStatusComboBox.setValue(currentOrder.getPaymentStatus());
            
            System.out.println("DEBUG: Setting notes: " + currentOrder.getNotes());
            notesArea.setText(currentOrder.getNotes());
            
            System.out.println("DEBUG: Setting item count: " + currentOrder.getItemCount());
            itemCountField.setText(String.valueOf(currentOrder.getItemCount()));
            
            System.out.println("DEBUG: Setting items description: " + currentOrder.getItemsDescription());
            itemsDescriptionArea.setText(currentOrder.getItemsDescription());

            validateField();
            System.out.println("DEBUG: populateFields() completed successfully");
            
        } catch (Exception e) {
            System.err.println("ERROR: Exception in populateFields(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void saveOrder() {
        if (!validateField()) {
            return;
        }

        try {
            // Update order with form data (only fields that exist in database)
            String newCustomerPhone = customerPhoneField.getText().trim();
            String newShippingAddress = shippingAddressArea.getText().trim();
            java.math.BigDecimal newTotalAmount = java.math.BigDecimal.valueOf(Double.parseDouble(totalAmountField.getText().trim()));
            String newStatus = statusComboBox.getValue();
            String newPaymentMethod = paymentMethodComboBox.getValue();
            String newPaymentStatus = paymentStatusComboBox.getValue();
            String newNotes = notesArea.getText().trim();
            int newItemCount = Integer.parseInt(itemCountField.getText().trim());
            String newItemsDescription = itemsDescriptionArea.getText().trim();

            // Update order object (for display purposes - only fields that exist in database)
            currentOrder.setCustomerPhone(newCustomerPhone);
            currentOrder.setShippingAddress(newShippingAddress);
            currentOrder.setTotalAmount(newTotalAmount);
            currentOrder.setStatus(newStatus);
            currentOrder.setPaymentMethod(newPaymentMethod);
            currentOrder.setPaymentStatus(newPaymentStatus);
            currentOrder.setNotes(newNotes);
            currentOrder.setItemCount(newItemCount);
            currentOrder.setItemsDescription(newItemsDescription);

            // Debug: Print order details before saving
            System.out.println("DEBUG: Saving order with ID: " + currentOrder.getId());
            System.out.println("DEBUG: New status: " + newStatus);
            System.out.println("DEBUG: New total: " + newTotalAmount);
            System.out.println("DEBUG: New address: " + newShippingAddress);
            System.out.println("DEBUG: New phone: " + newCustomerPhone);

            // Save to database (only updates fields that exist in database)
            boolean updateSuccess = orderService.updateOrder(currentOrder);
            System.out.println("DEBUG: Update success: " + updateSuccess);
            
            if (updateSuccess) {
                StringBuilder successMessage = new StringBuilder("Commande mise à jour avec succès!\n\n");
                successMessage.append("Champs sauvegardés dans la base de données:\n");
                successMessage.append("  - Statut: ").append(newStatus).append("\n");
                successMessage.append("  - Montant total: ").append(newTotalAmount).append(" DT\n");
                successMessage.append("  - Adresse de livraison: ").append(newShippingAddress).append("\n");
                successMessage.append("  - Téléphone: ").append(newCustomerPhone).append("\n\n");
                successMessage.append("Note: Le nom du client et l'email sont affichés pour référence,\n");
                successMessage.append("mais ne sont pas sauvegardés dans la base de données actuelle.");
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", successMessage.toString());
                closeForm();
                
                if (onOrderUpdated != null) {
                    onOrderUpdated.run();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour la commande. Vérifiez la connexion à la base de données.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Format de nombre invalide pour le montant ou la quantité.");
        } catch (Exception e) {
            System.err.println("ERROR: Exception during order update: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    private void cancelEdit() {
        closeForm();
    }

    @FXML
    private void quickStatusUpdate(String newStatus) {
        if (currentOrder != null) {
            statusComboBox.setValue(newStatus);
            saveOrder();
        }
    }

    @FXML
    private void markAsDelivered() {
        quickStatusUpdate(Order.STATUS_DELIVERED);
    }

    @FXML
    private void markAsProcessing() {
        quickStatusUpdate(Order.STATUS_PROCESSING);
    }

    @FXML
    private void markAsShipped() {
        quickStatusUpdate(Order.STATUS_SHIPPED);
    }

    @FXML
    private void markAsCanceled() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Annuler la commande");
        confirmAlert.setHeaderText("Annuler cette commande?");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir annuler cette commande? Cette action peut affecter le suivi des stocks.");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            quickStatusUpdate(Order.STATUS_CANCELED);
        }
    }

    private void closeForm() {
        // Close the stage
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Setters
    public void setOnOrderUpdated(Runnable onOrderUpdated) {
        this.onOrderUpdated = onOrderUpdated;
    }
}
