package tn.formini.utils;

import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.UserService;
import tn.formini.tools.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Utility class to automatically create an admin user if it doesn't exist
 * This should be called when the application starts
 */
public class AdminInitializer {
    
    private static final String DEFAULT_ADMIN_EMAIL = "admin@formini.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin#123";
    private static final String DEFAULT_ADMIN_FIRST_NAME = "Admin";
    private static final String DEFAULT_ADMIN_LAST_NAME = "System";
    
    /**
     * Initialize admin user if it doesn't exist
     * This method should be called when the application starts
     */
    public static void initializeAdmin() {
        System.out.println("Checking for admin user...");
        
        try {
            // Check if admin user already exists
            if (adminExists()) {
                System.out.println("Admin user already exists. Skipping creation.");
                return;
            }
            
            // Create admin user
            createAdminUser();
            System.out.println("Admin user created successfully!");
            System.out.println("Email: " + DEFAULT_ADMIN_EMAIL);
            System.out.println("Password: " + DEFAULT_ADMIN_PASSWORD);
            
        } catch (Exception e) {
            System.err.println("Error initializing admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if admin user already exists
     * @return true if admin exists, false otherwise
     */
    private static boolean adminExists() {
        Connection cnx = MyDataBase.getInstance().getCnx();
        
        try {
            String query = "SELECT COUNT(*) FROM user WHERE email = ? AND role_utilisateur = 'admin'";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setString(1, DEFAULT_ADMIN_EMAIL);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                ps.close();
                return count > 0;
            }
            
            rs.close();
            ps.close();
            
        } catch (Exception e) {
            System.err.println("Error checking admin existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Create the default admin user
     */
    private static void createAdminUser() {
        UserService userService = new UserService();
        
        try {
            User admin = new User();
            admin.setEmail(DEFAULT_ADMIN_EMAIL);
            admin.setPassword(DEFAULT_ADMIN_PASSWORD);
            admin.setNom(DEFAULT_ADMIN_LAST_NAME);
            admin.setPrenom(DEFAULT_ADMIN_FIRST_NAME);
            admin.setRole_utilisateur("admin");
            admin.setRoles("[\"ROLE_ADMIN\"]");
            admin.setTelephone("+21600000000");
            admin.setGouvernorat("Tunis");
            admin.setDate_naissance(new java.util.Date(System.currentTimeMillis() - 25L * 365 * 24 * 60 * 60 * 1000)); // 25 years ago
            admin.setProfession("System Administrator");
            admin.setNiveau_etude("Higher Education");
            
            userService.ajouter(admin);
            
            if (admin.getId() > 0) {
                System.out.println("Admin user created with ID: " + admin.getId());
            } else {
                throw new RuntimeException("Failed to create admin user - ID not assigned");
            }
            
        } catch (Exception e) {
            System.err.println("Error creating admin user: " + e.getMessage());
            throw new RuntimeException("Failed to create admin user", e);
        }
    }
    
    /**
     * Reset admin password (utility method)
     * @param newPassword the new password
     * @return true if password reset successfully
     */
    public static boolean resetAdminPassword(String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            System.err.println("Password cannot be null or empty");
            return false;
        }
        
        // Validate password strength
        if (!PasswordUtil.isPasswordStrong(newPassword)) {
            System.err.println("Password does not meet security requirements");
            return false;
        }
        
        Connection cnx = MyDataBase.getInstance().getCnx();
        
        try {
            // Hash the new password
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            
            String query = "UPDATE user SET password = ? WHERE email = ? AND role_utilisateur = 'admin'";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setString(1, hashedPassword);
            ps.setString(2, DEFAULT_ADMIN_EMAIL);
            int rowsUpdated = ps.executeUpdate();
            ps.close();
            
            if (rowsUpdated > 0) {
                System.out.println("Admin password reset successfully");
                return true;
            } else {
                System.err.println("Admin user not found or password not updated");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error resetting admin password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get admin user information
     * @return the admin user or null if not found
     */
    public static User getAdminUser() {
        Connection cnx = MyDataBase.getInstance().getCnx();
        
        try {
            String query = "SELECT * FROM user WHERE email = ? AND role_utilisateur = 'admin'";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setString(1, DEFAULT_ADMIN_EMAIL);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                User admin = new User();
                admin.setId(rs.getInt("id"));
                admin.setEmail(rs.getString("email"));
                admin.setPassword(rs.getString("password"));
                admin.setNom(rs.getString("nom"));
                admin.setPrenom(rs.getString("prenom"));
                admin.setRole_utilisateur(rs.getString("role_utilisateur"));
                admin.setRoles(rs.getString("roles"));
                admin.setTelephone(rs.getString("telephone"));
                admin.setGouvernorat(rs.getString("gouvernorat"));
                admin.setDate_naissance(rs.getTimestamp("date_naissance"));
                admin.setProfession(rs.getString("profession"));
                admin.setNiveau_etude(rs.getString("niveau_etude"));
                
                rs.close();
                ps.close();
                
                return admin;
            }
            
            rs.close();
            ps.close();
            
        } catch (Exception e) {
            System.err.println("Error getting admin user: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Main method for testing admin initialization
     */
    public static void main(String[] args) {
        System.out.println("=== Admin Initialization Test ===");
        initializeAdmin();
        
        User admin = getAdminUser();
        if (admin != null) {
            System.out.println("Admin user found: " + admin.getEmail());
        } else {
            System.out.println("Admin user not found");
        }
    }
}
