package tn.formini.services.UsersService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SMTPEmailService
 */
public class SMTPEmailServiceTest {

    private SMTPEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new SMTPEmailService();
    }

    @Test
    void testEmailServiceCreation() {
        assertNotNull(emailService);
    }

    @Test
    void testBuildVerificationEmailBody() {
        // Use reflection to test private method or create a public test method
        String name = "Test User";
        String token = "123456";
        
        // For now, just test that the public methods don't throw exceptions
        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail("test@example.com", name, token);
        });
    }

    @Test
    void testBuildPasswordResetEmailBody() {
        String name = "Test User";
        String token = "abcdef";
        
        assertDoesNotThrow(() -> {
            emailService.sendPasswordResetEmail("test@example.com", name, token);
        });
    }

    @Test
    void testTestConfiguration() {
        // This test will fail until proper SMTP credentials are configured
        // but it verifies the method exists and can be called
        assertDoesNotThrow(() -> {
            emailService.testConfiguration();
        });
    }
}
