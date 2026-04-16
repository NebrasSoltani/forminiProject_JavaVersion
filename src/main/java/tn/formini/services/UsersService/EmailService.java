package tn.formini.services.UsersService;

/**
 * Simple email service for development.
 * Logs verification tokens to console instead of sending real emails.
 * In production, replace with JavaMail or an email service API.
 */
public class EmailService {
    
    public EmailService() {
        System.out.println("EmailService initialized (development mode - logging only)");
    }
    
    /**
     * Send verification email to user (logs to console in development)
     * @param to recipient email
     * @param name recipient name
     * @param token verification token
     * @return true always (in development)
     */
    public boolean sendVerificationEmail(String to, String name, String token) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("EMAIL VERIFICATION TOKEN (Development Mode)");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("To: " + to);
        System.out.println("Name: " + name);
        System.out.println("Token: " + token);
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("Use this token to verify your account in the verification page.");
        System.out.println("═══════════════════════════════════════════════════════════════");
        return true;
    }
    
    /**
     * Test email configuration (always true in development)
     * @return true
     */
    public boolean testConfiguration() {
        System.out.println("EmailService test: Development mode - no SMTP configuration needed");
        return true;
    }
}
