package tn.formini.services.UsersService;

import tn.formini.entities.Users.User;
import tn.formini.tools.MyDataBase;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Date;

public class EmailVerificationService {
    
    private Connection cnx;
    private static final int TOKEN_LENGTH = 32;
    private static final int TOKEN_EXPIRY_HOURS = 24;
    private static final SecureRandom random = new SecureRandom();
    
    public EmailVerificationService() {
        cnx = MyDataBase.getInstance().getCnx();
    }
    
    /**
     * Generate a random verification token
     * @return random token string
     */
    public String generateVerificationToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : tokenBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Generate verification token for a user and save to database
     * @param userId User ID
     * @return verification token
     */
    public String generateAndSaveToken(int userId) {
        String token = generateVerificationToken();
        Date expiresAt = new Date(System.currentTimeMillis() + (TOKEN_EXPIRY_HOURS * 60 * 60 * 1000));
        
        String req = "UPDATE user SET email_verification_token = ?, email_verification_token_expires_at = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, token);
            ps.setTimestamp(2, new Timestamp(expiresAt.getTime()));
            ps.setInt(3, userId);
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Verification token generated for user ID: " + userId);
                return token;
            }
        } catch (SQLException ex) {
            System.out.println("Error generating verification token: " + ex.getMessage());
        }
        
        return null;
    }
    
    /**
     * Verify email using token
     * @param token Verification token
     * @return true if verification successful, false otherwise
     */
    public boolean verifyEmail(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        String req = "SELECT id, email_verification_token_expires_at FROM user WHERE email_verification_token = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("id");
                Timestamp expiresAt = rs.getTimestamp("email_verification_token_expires_at");
                
                // Check if token is expired
                if (expiresAt != null && expiresAt.before(new Date())) {
                    System.out.println("Verification token expired for user ID: " + userId);
                    return false;
                }
                
                // Mark email as verified
                String updateReq = "UPDATE user SET is_email_verified = true, email_verified_at = NOW(), email_verification_token = NULL, email_verification_token_expires_at = NULL WHERE id = ?";
                PreparedStatement updatePs = cnx.prepareStatement(updateReq);
                updatePs.setInt(1, userId);
                int rowsUpdated = updatePs.executeUpdate();
                
                if (rowsUpdated > 0) {
                    System.out.println("Email verified successfully for user ID: " + userId);
                    return true;
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error verifying email: " + ex.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if a user's email is verified
     * @param userId User ID
     * @return true if verified, false otherwise
     */
    public boolean isEmailVerified(int userId) {
        String req = "SELECT is_email_verified FROM user WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("is_email_verified");
            }
        } catch (SQLException ex) {
            System.out.println("Error checking email verification status: " + ex.getMessage());
        }
        
        return false;
    }
    
    /**
     * Resend verification token for a user
     * @param email User email
     * @return new verification token, or null if user not found
     */
    public String resendVerificationToken(String email) {
        String req = "SELECT id FROM user WHERE LOWER(email) = LOWER(?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("id");
                return generateAndSaveToken(userId);
            }
        } catch (SQLException ex) {
            System.out.println("Error resending verification token: " + ex.getMessage());
        }
        
        return null;
    }
}
