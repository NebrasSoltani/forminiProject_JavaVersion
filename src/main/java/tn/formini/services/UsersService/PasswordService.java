package tn.formini.services.UsersService;

import tn.formini.entities.Users.User;
import tn.formini.tools.MyDataBase;
import tn.formini.utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service for password-related operations
 */
public class PasswordService {
    
    private Connection cnx;
    
    public PasswordService() {
        cnx = MyDataBase.getInstance().getCnx();
    }
    
    /**
     * Change user password with verification of current password
     * @param userId the user ID
     * @param currentPassword the current password (plain text)
     * @param newPassword the new password (plain text)
     * @return true if password changed successfully, false otherwise
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        if (currentPassword == null || newPassword == null || 
            currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            return false;
        }
        
        // Validate new password strength
        if (!PasswordUtil.isPasswordStrong(newPassword)) {
            System.out.println("New password does not meet security requirements");
            return false;
        }
        
        try {
            // First, verify current password
            String verifyQuery = "SELECT password FROM user WHERE id = ?";
            PreparedStatement verifyPs = cnx.prepareStatement(verifyQuery);
            verifyPs.setInt(1, userId);
            ResultSet rs = verifyPs.executeQuery();
            
            if (!rs.next()) {
                System.out.println("User not found");
                return false;
            }
            
            String currentHashedPassword = rs.getString("password");
            
            // Verify current password
            if (!PasswordUtil.verifyPassword(currentPassword, currentHashedPassword)) {
                System.out.println("Current password is incorrect");
                return false;
            }
            
            // Hash the new password
            String newHashedPassword = PasswordUtil.hashPassword(newPassword);
            
            // Update password in database
            String updateQuery = "UPDATE user SET password = ? WHERE id = ?";
            PreparedStatement updatePs = cnx.prepareStatement(updateQuery);
            updatePs.setString(1, newHashedPassword);
            updatePs.setInt(2, userId);
            int rowsUpdated = updatePs.executeUpdate();
            
            rs.close();
            verifyPs.close();
            updatePs.close();
            
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reset user password (admin function)
     * @param userId the user ID
     * @param newPassword the new password (plain text)
     * @return true if password reset successfully, false otherwise
     */
    public boolean resetPassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        
        // Validate new password strength
        if (!PasswordUtil.isPasswordStrong(newPassword)) {
            System.out.println("New password does not meet security requirements");
            return false;
        }
        
        try {
            // Hash the new password
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            
            // Update password in database
            String updateQuery = "UPDATE user SET password = ? WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(updateQuery);
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            int rowsUpdated = ps.executeUpdate();
            ps.close();
            
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate a temporary password for user
     * @param userId the user ID
     * @return the generated temporary password, or null if failed
     */
    public String generateTemporaryPassword(int userId) {
        // Generate a random 12-character password
        String tempPassword = PasswordUtil.generateRandomPassword(12);
        
        if (resetPassword(userId, tempPassword)) {
            System.out.println("Temporary password generated for user ID: " + userId);
            return tempPassword;
        } else {
            System.err.println("Failed to generate temporary password for user ID: " + userId);
            return null;
        }
    }
    
    /**
     * Check if user needs to change password (e.g., first login or expired password)
     * @param userId the user ID
     * @return true if user needs to change password, false otherwise
     */
    public boolean needsPasswordChange(int userId) {
        // This is a placeholder for future implementation
        // You could add a 'password_changed_at' column to track when password was last changed
        // and implement password expiration policies
        
        try {
            String query = "SELECT password FROM user WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String password = rs.getString("password");
                // Check if password is still in plain text (not migrated)
                boolean needsChange = !password.startsWith("$2a$");
                rs.close();
                ps.close();
                return needsChange;
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.err.println("Error checking password change requirement: " + e.getMessage());
        }
        
        return false;
    }
}
