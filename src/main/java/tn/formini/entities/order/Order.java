package tn.formini.entities.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

public class Order {
    private int id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private String status;
    private Date orderDate;
    private Date deliveryDate;
    private String paymentMethod;
    private String paymentStatus;
    private String notes;
    private int itemCount;
    private String itemsDescription;

    // Order status constants
    public static final String STATUS_PENDING = "en attente";
    public static final String STATUS_CONFIRMED = "confirmée";
    public static final String STATUS_PROCESSING = "en cours";
    public static final String STATUS_SHIPPED = "expédiée";
    public static final String STATUS_DELIVERED = "livrée";
    public static final String STATUS_CANCELED = "annulée";
    public static final String STATUS_RETURNED = "retournée";

    public Order() {
        this.orderDate = new Date();
        this.status = STATUS_PENDING;
        this.paymentStatus = "en attente";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public String getItemsDescription() {
        return itemsDescription;
    }

    public void setItemsDescription(String itemsDescription) {
        this.itemsDescription = itemsDescription;
    }

    // Utility methods
    public String getStatusColor() {
        switch (status) {
            case STATUS_PENDING:
                return "#f59e0b"; // Orange
            case STATUS_CONFIRMED:
                return "#3b82f6"; // Blue
            case STATUS_PROCESSING:
                return "#8b5cf6"; // Purple
            case STATUS_SHIPPED:
                return "#06b6d4"; // Cyan
            case STATUS_DELIVERED:
                return "#10b981"; // Green
            case STATUS_CANCELED:
                return "#ef4444"; // Red
            case STATUS_RETURNED:
                return "#6b7280"; // Gray
            default:
                return "#6b7280"; // Gray
        }
    }

    public boolean canBeCanceled() {
        return STATUS_PENDING.equals(status) || STATUS_CONFIRMED.equals(status);
    }

    public boolean canBeProcessed() {
        return STATUS_CONFIRMED.equals(status);
    }

    public boolean canBeShipped() {
        return STATUS_PROCESSING.equals(status);
    }

    public boolean canBeDelivered() {
        return STATUS_SHIPPED.equals(status);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", orderDate=" + orderDate +
                '}';
    }
}
