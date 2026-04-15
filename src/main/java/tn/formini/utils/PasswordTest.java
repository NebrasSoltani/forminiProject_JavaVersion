package tn.formini.utils;

/**
 * Simple test class to verify password hashing functionality
 */
public class PasswordTest {
    
    public static void main(String[] args) {
        System.out.println("Testing password hashing functionality...");
        
        // Test password hashing
        String plainPassword = "TestPassword123!";
        System.out.println("Original password: " + plainPassword);
        
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        System.out.println("Hashed password: " + hashedPassword);
        
        // Test password verification
        boolean isCorrect = PasswordUtil.verifyPassword(plainPassword, hashedPassword);
        System.out.println("Password verification (correct): " + isCorrect);
        
        // Test incorrect password
        boolean isIncorrect = PasswordUtil.verifyPassword("WrongPassword", hashedPassword);
        System.out.println("Password verification (incorrect): " + isIncorrect);
        
        // Test password strength validation
        String weakPassword = "123";
        String strongPassword = "StrongPass123!";
        
        System.out.println("Weak password strength: " + PasswordUtil.isPasswordStrong(weakPassword));
        System.out.println("Strong password strength: " + PasswordUtil.isPasswordStrong(strongPassword));
        
        // Test random password generation
        String randomPassword = PasswordUtil.generateRandomPassword(12);
        System.out.println("Generated random password: " + randomPassword);
        System.out.println("Random password strength: " + PasswordUtil.isPasswordStrong(randomPassword));
        
        System.out.println("Password testing completed!");
    }
}
