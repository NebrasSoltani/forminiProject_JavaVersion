package tn.formini.services.UsersService;

import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import tn.formini.entities.Users.User;

/**
 * Service for handling password reset token generation and validation.
 */
public class PasswordResetService {

    private static final int TOKEN_LENGTH = 32; // 32 bytes = 64 hex chars
    private static final long TOKEN_EXPIRY_HOURS = 1; // Token expires in 1 hour

    private final UserService userService;
    private final EmailService emailService;

    public PasswordResetService() {
        this.userService = new UserService();
        this.emailService = new EmailService();
    }

    /**
     * Generate a password reset token for a user and send it via email
     * @param email User's email address
     * @return true if token generated and email sent successfully, false otherwise
     */
    public boolean initiatePasswordReset(String email) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            System.out.println("User not found for email: " + email);
            return false;
        }

        // Generate token
        String token = generateToken();
        Date expiresAt = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(TOKEN_EXPIRY_HOURS));

        // Save token to user
        user.setPassword_reset_token(token);
        user.setPassword_reset_token_expires_at(expiresAt);
        userService.modifier(user);

        // Send email with token
        boolean emailSent = emailService.sendPasswordResetEmail(email, user.getNom(), token);

        if (emailSent) {
            System.out.println("Password reset token sent to: " + email);
        }

        return emailSent;
    }

    /**
     * Validate a password reset token
     * @param token The reset token to validate
     * @return The user associated with the token, or null if invalid
     */
    public User validateResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        User user = userService.getUserByResetToken(token);
        if (user == null) {
            return null;
        }

        // Check if token is expired
        Date expiresAt = user.getPassword_reset_token_expires_at();
        if (expiresAt == null || expiresAt.before(new Date())) {
            System.out.println("Password reset token expired for user: " + user.getEmail());
            return null;
        }

        return user;
    }

    /**
     * Reset user's password
     * @param user The user whose password to reset
     * @param newPassword The new password
     * @return true if password reset successfully, false otherwise
     */
    public boolean resetPassword(User user, String newPassword) {
        try {
            user.setPassword(newPassword);
            user.setPassword_reset_token(null); // Clear the token
            user.setPassword_reset_token_expires_at(null); // Clear the expiry
            userService.modifier(user);
            System.out.println("Password reset successfully for user: " + user.getEmail());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to reset password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate a random hex token
     * @return Random token string
     */
    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : tokenBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
