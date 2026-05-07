package tn.formini.services.UsersService;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Email service using SMTP for sending verification emails.
 */
public class SMTPEmailService {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromEmail;
    private final String fromName;
    private final Properties properties;

    public SMTPEmailService() {
        // Gmail SMTP configuration
        this.host = "smtp.gmail.com";
        this.port = 587;
        this.username = "forminiapp@gmail.com"; // Your Gmail address
        this.password = "xcsvjqlyqlkbdfya"; // App password for Gmail SMTP
        this.fromEmail = "forminiapp@gmail.com";
        this.fromName = "Formini";
        
        this.properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.ssl.trust", host);
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
    }

    /**
     * Send verification email to user
     * @param to recipient email
     * @param name recipient name
     * @param token verification token
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendVerificationEmail(String to, String name, String token) {
        String subject = "Verify your Formini account";
        String body = buildVerificationEmailBody(name, token);
        return sendEmail(to, subject, body);
    }

    /**
     * Send password reset email to user
     * @param to recipient email
     * @param name recipient name
     * @param token password reset token
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendPasswordResetEmail(String to, String name, String token) {
        String subject = "Reset your Formini password";
        String body = buildPasswordResetEmailBody(name, token);
        return sendEmail(to, subject, body);
    }

    /**
     * Build verification email body
     * @param name recipient name
     * @param token verification token
     * @return email body HTML
     */
    private String buildVerificationEmailBody(String name, String token) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Welcome to Formini, " + name + "!</h2>" +
                "<p>Thank you for registering. Please verify your email address using the token below:</p>" +
                "<p style='background-color: #f0f0f0; padding: 10px; font-family: monospace; font-size: 16px;'>" + token + "</p>" +
                "<p>This token will expire in 24 hours.</p>" +
                "<p>If you did not create an account, please ignore this email.</p>" +
                "<br>" +
                "<p>Best regards,<br>The Formini Team</p>" +
                "</body>" +
                "</html>";
    }

    /**
     * Build password reset email body
     * @param name recipient name
     * @param token password reset token
     * @return email body HTML
     */
    private String buildPasswordResetEmailBody(String name, String token) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Password Reset Request</h2>" +
                "<p>Hello " + name + ",</p>" +
                "<p>We received a request to reset your password. Use the token below to reset your password:</p>" +
                "<p style='background-color: #f0f0f0; padding: 10px; font-family: monospace; font-size: 16px;'>" + token + "</p>" +
                "<p>This token will expire in 1 hour.</p>" +
                "<p>If you did not request a password reset, please ignore this email.</p>" +
                "<br>" +
                "<p>Best regards,<br>The Formini Team</p>" +
                "</body>" +
                "</html>";
    }

    /**
     * Send email using SMTP
     * @param to recipient email
     * @param subject email subject
     * @param body email body (HTML)
     * @return true if sent successfully, false otherwise
     */
    private boolean sendEmail(String to, String subject, String body) {
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body, "text/html");

            Transport.send(message);
            
            System.out.println("Verification email sent to: " + to);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error sending email to " + to + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test SMTP configuration
     * @return true if configuration is valid, false otherwise
     */
    public boolean testConfiguration() {
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            
            System.out.println("SMTP configuration test successful");
            return true;
            
        } catch (MessagingException e) {
            System.err.println("SMTP configuration test failed: " + e.getMessage());
            return false;
        }
    }
}
