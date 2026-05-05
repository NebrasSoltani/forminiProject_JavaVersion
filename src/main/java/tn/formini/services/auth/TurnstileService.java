package tn.formini.services.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import tn.formini.utils.OAuthConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Service for verifying Cloudflare Turnstile tokens.
 * Turnstile is Cloudflare's "Verify you are human" CAPTCHA alternative.
 */
public class TurnstileService {
    private static final String VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    private static final Gson gson = new Gson();

    /**
     * Verify a Turnstile token with Cloudflare's API.
     * 
     * @param token The token from the Turnstile widget
     * @return true if verification successful, false otherwise
     */
    public static boolean verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            System.err.println("Turnstile token is null or empty");
            return false;
        }

        String secretKey = OAuthConfig.getTurnstileSecretKey();
        if (secretKey == null || secretKey.isEmpty() || secretKey.equals("YOUR_TURNSTILE_SECRET_KEY")) {
            System.err.println("Turnstile secret key not configured");
            return false;
        }

        try {
            URL url = new URL(VERIFY_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            String postData = "secret=" + java.net.URLEncoder.encode(secretKey, "UTF-8") +
                             "&response=" + java.net.URLEncoder.encode(token, "UTF-8");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Turnstile verification failed with HTTP code: " + responseCode);
                return false;
            }

            String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

            boolean success = jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean();
            
            if (!success) {
                System.err.println("Turnstile verification failed: " + response);
            }

            return success;

        } catch (IOException e) {
            System.err.println("Error verifying Turnstile token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
