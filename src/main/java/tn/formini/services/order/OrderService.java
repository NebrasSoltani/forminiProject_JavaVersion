package tn.formini.services.order;

import tn.formini.entities.order.Order;
import tn.formini.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private Connection connection;

    public OrderService() {
        this.connection = DatabaseConnection.getConnection();
        if (this.connection == null) {
            System.out.println("INFO: Database not available - using mock data mode");
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        
        if (connection == null) {
            System.out.println("INFO: Using mock orders data");
            return createSampleOrdersList();
        }
        
        String query = "SELECT * FROM commande ORDER BY date_commande DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
            System.out.println("INFO: Falling back to mock data");
            return createSampleOrdersList();
        }
        
        return orders;
    }

    public Order getOrderById(int id) {
        String query = "SELECT * FROM commande WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order: " + e.getMessage());
        }
        
        return null;
    }

    public boolean addOrder(Order order) {
        String query = "INSERT INTO commande (reference, date_commande, statut, total, adresse_livraison, telephone, utilisateur_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Generate order number if not provided
            if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
                order.setOrderNumber(generateOrderNumber());
            }
            
            pstmt.setString(1, order.getOrderNumber());
            pstmt.setTimestamp(2, new Timestamp(order.getOrderDate().getTime()));
            pstmt.setString(3, order.getStatus());
            pstmt.setBigDecimal(4, order.getTotalAmount());
            pstmt.setString(5, order.getShippingAddress());
            pstmt.setString(6, order.getCustomerPhone());
            pstmt.setInt(7, 1); // Default user ID
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
        }
        
        return false;
    }

    public boolean updateOrder(Order order) {
        // Only update fields that exist in the actual database table
        String query = "UPDATE commande SET reference = ?, date_commande = ?, statut = ?, total = ?, " +
                       "adresse_livraison = ?, telephone = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, order.getOrderNumber());
            pstmt.setTimestamp(2, new Timestamp(order.getOrderDate().getTime()));
            pstmt.setString(3, order.getStatus());
            pstmt.setBigDecimal(4, order.getTotalAmount());
            pstmt.setString(5, order.getShippingAddress());
            pstmt.setString(6, order.getCustomerPhone());
            pstmt.setInt(7, order.getId());
            
            System.out.println("DEBUG: Executing UPDATE query for order ID: " + order.getId());
            System.out.println("DEBUG: New status: " + order.getStatus());
            System.out.println("DEBUG: New total: " + order.getTotalAmount());
            System.out.println("DEBUG: New address: " + order.getShippingAddress());
            System.out.println("DEBUG: New phone: " + order.getCustomerPhone());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("DEBUG: Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        String query = "UPDATE commande SET statut = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
        
        return false;
    }

    public boolean deleteOrder(int id) {
        String query = "DELETE FROM commande WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
        }
        
        return false;
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders by status: " + e.getMessage());
        }
        
        return orders;
    }

    public List<Order> searchOrders(String searchTerm) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE order_number LIKE ? OR customer_name LIKE ? " +
                       "OR customer_email LIKE ? OR shipping_address LIKE ? ORDER BY order_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching orders: " + e.getMessage());
        }
        
        return orders;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setOrderNumber(rs.getString("reference"));
        order.setOrderDate(rs.getTimestamp("date_commande"));
        order.setStatus(rs.getString("statut"));
        order.setTotalAmount(rs.getBigDecimal("total"));
        order.setShippingAddress(rs.getString("adresse_livraison"));
        order.setCustomerPhone(rs.getString("telephone"));
        
        // Set default values for fields not in your database
        order.setCustomerName("Client #" + rs.getInt("utilisateur_id"));
        order.setCustomerEmail("client" + rs.getInt("utilisateur_id") + "@email.com");
        order.setPaymentMethod("Non spécifié");
        order.setPaymentStatus("Non spécifié");
        order.setItemCount(1);
        order.setItemsDescription("Produits de la commande");
        order.setNotes("Commande de l'utilisateur ID: " + rs.getInt("utilisateur_id"));
        
        return order;
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + uuid;
    }

    // Initialize sample data for testing
    public void initializeSampleData() {
        // Check if orders table exists and has data
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet tables = meta.getTables(null, null, "commande", null);
            
            if (!tables.next()) {
                createOrdersTable();
            }
            
            // Check if there are any orders
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM commande");
            rs.next();
            int count = rs.getInt(1);
            
            if (count == 0) {
                createOrdersTable();
                createSampleOrders();
            }
            
        } catch (SQLException e) {
            System.err.println("Error initializing order data: " + e.getMessage());
        }
    }

    private void createOrdersTable() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS commande (
                id INT AUTO_INCREMENT PRIMARY KEY,
                reference VARCHAR(50) UNIQUE NOT NULL,
                date_commande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                statut VARCHAR(50) NOT NULL DEFAULT 'en attente',
                total DECIMAL(10,2) NOT NULL,
                adresse_livraison TEXT,
                telephone VARCHAR(50),
                utilisateur_id INT
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    private List<Order> createSampleOrdersList() {
        List<Order> sampleOrders = new ArrayList<>();
        
        Order order1 = new Order();
        order1.setId(1);
        order1.setOrderNumber("ORD-20240416110001-ABC123");
        order1.setCustomerName("Ahmed Ben Ali");
        order1.setCustomerEmail("ahmed.benali@email.com");
        order1.setCustomerPhone("+216 22 123 456");
        order1.setShippingAddress("123 rue Habib Bourguiba, Tunis, Tunisie");
        order1.setTotalAmount(new BigDecimal("1250.99"));
        order1.setStatus(Order.STATUS_PROCESSING);
        order1.setPaymentMethod("Carte de crédit");
        order1.setPaymentStatus("payé");
        order1.setItemCount(3);
        order1.setItemsDescription("MacBook Pro, Mouse, Keyboard");
        order1.setOrderDate(new Date(System.currentTimeMillis()));
        
        Order order2 = new Order();
        order2.setId(2);
        order2.setOrderNumber("ORD-20240416110002-DEF456");
        order2.setCustomerName("Sarra Trabelsi");
        order2.setCustomerEmail("sarra.trabelsi@email.com");
        order2.setCustomerPhone("+216 98 765 432");
        order2.setShippingAddress("45 avenue Farhat Hached, Sfax, Tunisie");
        order2.setTotalAmount(new BigDecimal("899.50"));
        order2.setStatus(Order.STATUS_DELIVERED);
        order2.setPaymentMethod("Espèces");
        order2.setPaymentStatus("payé");
        order2.setItemCount(2);
        order2.setItemsDescription("iPhone 14, Chargeur");
        order2.setOrderDate(new Date(System.currentTimeMillis()));
        
        Order order3 = new Order();
        order3.setId(3);
        order3.setOrderNumber("ORD-20240416110003-GHI789");
        order3.setCustomerName("Mohamed Jemni");
        order3.setCustomerEmail("mohamed.jemni@email.com");
        order3.setCustomerPhone("+216 55 111 222");
        order3.setShippingAddress("78 rue de la République, Sousse, Tunisie");
        order3.setTotalAmount(new BigDecimal("450.00"));
        order3.setStatus(Order.STATUS_PENDING);
        order3.setPaymentMethod("PayPal");
        order3.setPaymentStatus("en attente");
        order3.setItemCount(1);
        order3.setItemsDescription("iPad Air");
        order3.setOrderDate(new Date(System.currentTimeMillis()));
        
        sampleOrders.add(order1);
        sampleOrders.add(order2);
        sampleOrders.add(order3);
        
        return sampleOrders;
    }

    private void createSampleOrders() {
        List<Order> sampleOrders = new ArrayList<>();
        
        Order order1 = new Order();
        order1.setCustomerName("Ahmed Ben Ali");
        order1.setCustomerEmail("ahmed.benali@email.com");
        order1.setCustomerPhone("+216 22 123 456");
        order1.setShippingAddress("123 rue Habib Bourguiba, Tunis, Tunisie");
        order1.setTotalAmount(new BigDecimal("1250.99"));
        order1.setStatus(Order.STATUS_PROCESSING);
        order1.setPaymentMethod("Carte de crédit");
        order1.setPaymentStatus("payé");
        order1.setItemCount(3);
        order1.setItemsDescription("MacBook Pro, Mouse, Keyboard");
        
        Order order2 = new Order();
        order2.setCustomerName("Sarra Trabelsi");
        order2.setCustomerEmail("sarra.trabelsi@email.com");
        order2.setCustomerPhone("+216 98 765 432");
        order2.setShippingAddress("45 avenue Farhat Hached, Sfax, Tunisie");
        order2.setTotalAmount(new BigDecimal("899.50"));
        order2.setStatus(Order.STATUS_DELIVERED);
        order2.setPaymentMethod("Espèces");
        order2.setPaymentStatus("payé");
        order2.setItemCount(2);
        order2.setItemsDescription("iPhone 14, Chargeur");
        
        Order order3 = new Order();
        order3.setCustomerName("Mohamed Jemni");
        order3.setCustomerEmail("mohamed.jemni@email.com");
        order3.setCustomerPhone("+216 55 111 222");
        order3.setShippingAddress("78 rue de la République, Sousse, Tunisie");
        order3.setTotalAmount(new BigDecimal("450.00"));
        order3.setStatus(Order.STATUS_PENDING);
        order3.setPaymentMethod("PayPal");
        order3.setPaymentStatus("en attente");
        order3.setItemCount(1);
        order3.setItemsDescription("iPad Air");
        
        sampleOrders.add(order1);
        sampleOrders.add(order2);
        sampleOrders.add(order3);
        
        for (Order order : sampleOrders) {
            addOrder(order);
        }
    }
}
