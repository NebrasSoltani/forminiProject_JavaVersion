import tn.formini.services.UsersService.SendGridEmailService;

public class test_sendgrid {
    public static void main(String[] args) {
        SendGridEmailService emailService = new SendGridEmailService();
        
        // Test configuration
        boolean configValid = emailService.testConfiguration();
        System.out.println("SendGrid configuration test: " + (configValid ? "SUCCESS" : "FAILED"));
        
        // Test email sending
        boolean emailSent = emailService.sendVerificationEmail(
            "test@example.com", 
            "Test User", 
            "123456"
        );
        System.out.println("Email sending test: " + (emailSent ? "SUCCESS" : "FAILED"));
    }
}
