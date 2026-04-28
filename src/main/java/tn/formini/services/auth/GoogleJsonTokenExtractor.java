package tn.formini.services.auth;

import com.github.scribejava.core.extractors.OAuth2AccessTokenExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom token extractor that handles Google's JSON response format
 */
public class GoogleJsonTokenExtractor extends OAuth2AccessTokenExtractor {
    private static final GoogleJsonTokenExtractor INSTANCE = new GoogleJsonTokenExtractor();
    private static final Gson GSON = new Gson();

    private GoogleJsonTokenExtractor() {
    }

    public static GoogleJsonTokenExtractor instance() {
        return INSTANCE;
    }

    public OAuth2AccessToken extract(String response) {
        // Try to parse as JSON first (Google's format)
        try {
            JsonObject json = GSON.fromJson(response, JsonObject.class);
            if (json.has("access_token")) {
                String accessToken = json.get("access_token").getAsString();
                String tokenType = json.has("token_type") ? json.get("token_type").getAsString() : "Bearer";
                Integer expiresIn = json.has("expires_in") ? json.get("expires_in").getAsInt() : null;
                String refreshToken = json.has("refresh_token") ? json.get("refresh_token").getAsString() : null;
                String scope = json.has("scope") ? json.get("scope").getAsString() : null;

                return new OAuth2AccessToken(
                    accessToken,
                    tokenType,
                    expiresIn,
                    refreshToken,
                    scope,
                    response
                );
            }
        } catch (Exception e) {
            // If JSON parsing fails, fall back to regex extraction
        }

        // Fallback to standard URL-encoded format extraction
        Pattern pattern = Pattern.compile("access_token=([^&]+)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String accessToken = matcher.group(1);
            String tokenType = extractParam(response, "token_type", "Bearer");
            Integer expiresIn = extractIntParam(response, "expires_in");
            String refreshToken = extractParam(response, "refresh_token", null);
            String scope = extractParam(response, "scope", null);

            return new OAuth2AccessToken(
                accessToken,
                tokenType,
                expiresIn,
                refreshToken,
                scope,
                response
            );
        }

        throw new IllegalArgumentException("Cannot extract access token from response: " + response);
    }

    private String extractParam(String response, String paramName, String defaultValue) {
        Pattern pattern = Pattern.compile(paramName + "=([^&]+)");
        Matcher matcher = pattern.matcher(response);
        return matcher.find() ? matcher.group(1) : defaultValue;
    }

    private Integer extractIntParam(String response, String paramName) {
        String value = extractParam(response, paramName, null);
        return value != null ? Integer.parseInt(value) : null;
    }
}
