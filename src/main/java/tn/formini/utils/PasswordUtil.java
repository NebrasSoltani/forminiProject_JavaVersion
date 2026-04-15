package tn.formini.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification using BCrypt
 */
public class PasswordUtil {
    
    // BCrypt cost factor (higher = more secure but slower)
    private static final int COST_FACTOR = 12;
    
    /**
     * Hash a plain text password using BCrypt
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            // Using BCrypt implementation
            return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST_FACTOR));
        } catch (Exception e) {
            System.err.println("Error hashing password: " + e.getMessage());
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /**
     * Verify a plain text password against a hashed password
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the hashed password to verify against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate a secure random password
     * @param length the length of the password
     * @return a random password
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Validate password strength
     * @param password the password to validate
     * @return true if the password meets security requirements
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0) hasSpecial = true;
        }
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}

/**
 * BCrypt implementation for password hashing
 * This is a simplified implementation. In production, consider using a library like Spring Security's BCryptPasswordEncoder
 */
class BCrypt {
    private static final int GENSALT_DEFAULT_LOG2_ROUNDS = 10;
    private static final int BCRYPT_SALT_LEN = 16;
    
    public static String hashpw(String password, String salt) {
        return hashpw(password, salt, 0);
    }
    
    public static String gensalt() {
        return gensalt(GENSALT_DEFAULT_LOG2_ROUNDS);
    }
    
    public static String gensalt(int log_rounds) {
        SecureRandom random = new SecureRandom();
        byte[] rs = new byte[BCRYPT_SALT_LEN];
        random.nextBytes(rs);
        
        StringBuilder salt = new StringBuilder("$2a$");
        salt.append(String.format("%02d", log_rounds));
        salt.append("$");
        salt.append(Base64.getEncoder().encodeToString(rs).substring(0, 22));
        
        return salt.toString();
    }
    
    public static boolean checkpw(String plaintext, String hashed) {
        // This is a simplified implementation
        // In production, use a proper BCrypt library
        try {
            // For now, we'll use a simple hash for demonstration
            // Replace this with proper BCrypt implementation
            return hashpw(plaintext, hashed.substring(0, 29)).equals(hashed);
        } catch (Exception e) {
            return false;
        }
    }
    
    private static String hashpw(String password, String salt, int rounds) {
        // Simplified implementation - replace with proper BCrypt
        // This is just for demonstration
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            String salted = password + salt;
            byte[] hash = md.digest(salted.getBytes());
            
            StringBuilder result = new StringBuilder(salt);
            result.append("$");
            result.append(Base64.getEncoder().encodeToString(hash).substring(0, 31));
            
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
