package tn.formini.controllers.order;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.formini.entities.order.Order;

import java.text.SimpleDateFormat;

public class OrderDetailsController {

    @FXML private Label orderNumberLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label customerEmailLabel;
    @FXML private Label customerPhoneLabel;
    @FXML private Label shippingAddressLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label statusLabel;
    @FXML private Label orderDateLabel;
    @FXML private Label deliveryDateLabel;
    @FXML private Label itemCountLabel;
    @FXML private Label itemsDescriptionLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label paymentStatusLabel;
    @FXML private Label notesLabel;

    private Order currentOrder;

    public void setOrder(Order order) {
        this.currentOrder = order;
        populateFields();
    }

    private void populateFields() {
        if (currentOrder == null) return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        orderNumberLabel.setText(currentOrder.getOrderNumber());
        customerNameLabel.setText(currentOrder.getCustomerName());
        customerEmailLabel.setText(currentOrder.getCustomerEmail() != null ? currentOrder.getCustomerEmail() : "N/A");
        customerPhoneLabel.setText(currentOrder.getCustomerPhone() != null ? currentOrder.getCustomerPhone() : "N/A");
        shippingAddressLabel.setText(currentOrder.getShippingAddress() != null ? currentOrder.getShippingAddress() : "N/A");
        
        totalAmountLabel.setText(String.format("%.3f DT", currentOrder.getTotalAmount()));
        
        // Status with color
        statusLabel.setText(currentOrder.getStatus());
        statusLabel.setStyle("-fx-text-fill: " + currentOrder.getStatusColor() + "; -fx-font-weight: 600;");
        
        orderDateLabel.setText(dateFormat.format(currentOrder.getOrderDate()));
        deliveryDateLabel.setText(currentOrder.getDeliveryDate() != null ? dateFormat.format(currentOrder.getDeliveryDate()) : "N/A");
        
        itemCountLabel.setText(String.valueOf(currentOrder.getItemCount()));
        itemsDescriptionLabel.setText(currentOrder.getItemsDescription() != null ? currentOrder.getItemsDescription() : "N/A");
        
        paymentMethodLabel.setText(currentOrder.getPaymentMethod() != null ? currentOrder.getPaymentMethod() : "N/A");
        paymentStatusLabel.setText(currentOrder.getPaymentStatus() != null ? currentOrder.getPaymentStatus() : "N/A");
        
        notesLabel.setText(currentOrder.getNotes() != null ? currentOrder.getNotes() : "Aucune note");
    }

    @FXML
    private void closeDetails() {
        // Close the stage
        Stage stage = (Stage) orderNumberLabel.getScene().getWindow();
        stage.close();
    }
}
