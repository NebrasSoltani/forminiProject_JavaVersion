package tn.formini.utils;

import tn.formini.services.UsersService.UserService;
import tn.formini.tools.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class to migrate existing plain text passwords to hashed passwords
 * This should be run once to convert all existing passwords in the database
 */
public class PasswordMigration {
    
    private static final UserService userService = new UserService();
    
    /**
     * Migrate all existing plain text passwords to hashed passwords
     * This method should be run once to convert existing passwords
     */
    public static void migrateAllPasswords() {
        System.out.println("Starting password migration...");
        
        Connection cnx = MyDataBase.getInstance().getCnx();
        String selectQuery = "SELECT id, password FROM user WHERE password NOT LIKE '$2a$%'";
        
        try {
            PreparedStatement ps = cnx.prepareStatement(selectQuery);
            ResultSet rs = ps.executeQuery();
            
            int migratedCount = 0;
            int totalCount = 0;
            
            while (rs.next()) {
                totalCount++;
                int userId = rs.getInt("id");
                String plainPassword = rs.getString("password");
                
                try {
                    // Check if password is already hashed (starts with $2a$)
                    if (plainPassword.startsWith("$2a$")) {
                        System.out.println("User " + userId + " already has hashed password, skipping...");
                        continue;
                    }
                    
                    // Hash the plain text password
                    String hashedPassword = PasswordUtil.hashPassword(plainPassword);
                    
                    // Update the database with the hashed password
                    String updateQuery = "UPDATE user SET password = ? WHERE id = ?";
                    PreparedStatement updatePs = cnx.prepareStatement(updateQuery);
                    updatePs.setString(1, hashedPassword);
                    updatePs.setInt(2, userId);
                    updatePs.executeUpdate();
                    updatePs.close();
                    
                    migratedCount++;
                    System.out.println("Migrated password for user ID: " + userId);
                    
                } catch (Exception e) {
                    System.err.println("Error migrating password for user ID " + userId + ": " + e.getMessage());
                }
            }
            
            rs.close();
            ps.close();
            
            System.out.println("Password migration completed!");
            System.out.println("Total users processed: " + totalCount);
            System.out.println("Passwords migrated: " + migratedCount);
            System.out.println("Users skipped (already hashed): " + (totalCount - migratedCount));
            
        } catch (SQLException e) {
            System.err.println("Error during password migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verify that all passwords in the database are hashed
     */
    public static void verifyPasswordMigration() {
        System.out.println("Verifying password migration...");
        
        Connection cnx = MyDataBase.getInstance().getCnx();
        String query = "SELECT id, password FROM user";
        
        try {
            PreparedStatement ps = cnx.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            int hashedCount = 0;
            int totalCount = 0;
            int plainTextCount = 0;
            
            while (rs.next()) {
                totalCount++;
                int userId = rs.getInt("id");
                String password = rs.getString("password");
                
                if (password.startsWith("$2a$")) {
                    hashedCount++;
                } else {
                    plainTextCount++;
                    System.out.println("WARNING: User " + userId + " still has plain text password!");
                }
            }
            
            rs.close();
            ps.close();
            
            System.out.println("Password verification completed!");
            System.out.println("Total users: " + totalCount);
            System.out.println("Hashed passwords: " + hashedCount);
            System.out.println("Plain text passwords: " + plainTextCount);
            
            if (plainTextCount > 0) {
                System.out.println("WARNING: " + plainTextCount + " users still have plain text passwords!");
            } else {
                System.out.println("SUCCESS: All passwords are properly hashed!");
            }
            
        } catch (SQLException e) {
            System.err.println("Error during password verification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main method to run the migration
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("verify")) {
            verifyPasswordMigration();
        } else {
            migrateAllPasswords();
        }
    }
}
