package tn.formini.services.UsersService;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Email service using JavaMail with SMTP for sending verification emails.
 */
public class EmailService {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean authEnabled;
    private final boolean starttlsEnabled;

    public EmailService() {
        // Gmail SMTP configuration
        this.host = "smtp.gmail.com";
        this.port = 587;
        this.username = "soltaninebras304@gmail.com";
        this.password = "gooq dlkh xdxl btda";
        this.authEnabled = true;
        this.starttlsEnabled = true;
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
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", String.valueOf(authEnabled));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnabled));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
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
        }
    }

    /**
     * Test email configuration
     * @return true if configuration is valid, false otherwise
     */
    public boolean testConfiguration() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", String.valueOf(authEnabled));
            props.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnabled));

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Transport transport = session.getTransport("smtp");
            transport.connect(host, port, username, password);
            transport.close();

            System.out.println("Email configuration test successful");
            return true;
        } catch (MessagingException e) {
            System.err.println("Email configuration test failed: " + e.getMessage());
            return false;
        }
    }
}
