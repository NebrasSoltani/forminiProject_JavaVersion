package tn.formini.utils;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for TOTP-based Two-Factor Authentication (2FA)
 * Compatible with Google Authenticator and other TOTP apps
 */
public class TOTPService {

    private static final String ISSUER = "Formini";
    private static final int QR_CODE_SIZE = 200;
    private static final int BACKUP_CODES_COUNT = 10;
    private static final int BACKUP_CODE_LENGTH = 8;

    private final GoogleAuthenticator gAuth;

    public TOTPService() {
        this.gAuth = new GoogleAuthenticator();
    }

    /**
     * Generate a new TOTP secret key for a user
     * @return GoogleAuthenticatorKey containing the secret
     */
    public GoogleAuthenticatorKey generateSecret() {
        return gAuth.createCredentials();
    }

    /**
     * Generate the QR code URL for TOTP setup
     * @param secret the TOTP secret
     * @param email user's email (used as account name)
     * @return QR code URL string
     */
    public String getQRCodeUrl(String secret, String email) {
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(secret).build();
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(ISSUER, email, key);
    }

    /**
     * Generate a QR code image file for TOTP setup
     * @param secret the TOTP secret
     * @param email user's email
     * @param outputPath path where to save the QR code image
     * @return true if successful, false otherwise
     */
    public boolean generateQRCodeImage(String secret, String email, String outputPath) {
        try {
            String qrUrl = getQRCodeUrl(secret, email);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
            
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            File outputFile = new File(outputPath);
            ImageIO.write(image, "PNG", outputFile);
            
            return true;
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verify a TOTP code against the secret
     * @param secret the user's TOTP secret
     * @param code the 6-digit code to verify
     * @return true if code is valid, false otherwise
     */
    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }

    /**
     * Verify a TOTP code (string version)
     * @param secret the user's TOTP secret
     * @param code the 6-digit code as string
     * @return true if code is valid, false otherwise
     */
    public boolean verifyCode(String secret, String code) {
        try {
            return verifyCode(secret, Integer.parseInt(code));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Generate backup codes for account recovery
     * @return list of backup codes
     */
    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < BACKUP_CODES_COUNT; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                code.append(random.nextInt(10));
            }
            codes.add(code.toString());
        }
        
        return codes;
    }

    /**
     * Convert backup codes list to JSON string for storage
     * @param codes list of backup codes
     * @return JSON string representation
     */
    public String backupCodesToJson(List<String> codes) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < codes.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(codes.get(i)).append("\"");
        }
        json.append("]");
        return json.toString();
    }

    /**
     * Parse backup codes from JSON string
     * @param json JSON string of backup codes
     * @return list of backup codes
     */
    public List<String> parseBackupCodesFromJson(String json) {
        List<String> codes = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return codes;
        }
        
        // Simple JSON parsing for array of strings
        String content = json.trim();
        if (content.startsWith("[") && content.endsWith("]")) {
            content = content.substring(1, content.length() - 1);
            String[] parts = content.split(",");
            for (String part : parts) {
                String code = part.trim().replaceAll("\"", "");
                if (!code.isEmpty()) {
                    codes.add(code);
                }
            }
        }
        
        return codes;
    }

    /**
     * Validate a backup code
     * @param code the code to validate
     * @return true if it's a valid backup code format (8 digits)
     */
    public boolean isValidBackupCodeFormat(String code) {
        if (code == null || code.length() != BACKUP_CODE_LENGTH) {
            return false;
        }
        try {
            Integer.parseInt(code);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
