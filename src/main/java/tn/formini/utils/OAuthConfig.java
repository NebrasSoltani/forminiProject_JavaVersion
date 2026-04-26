package tn.formini.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class OAuthConfig {
    private static Properties properties;
    
    static {
        properties = new Properties();
        try (InputStream input = OAuthConfig.class.getClassLoader().getResourceAsStream("oauth.properties")) {
            if (input == null) {
                System.out.println("Warning: oauth.properties not found, using default values");
                setDefaultValues();
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            System.out.println("Error loading oauth.properties: " + ex.getMessage());
            setDefaultValues();
        }
    }
    
    private static void setDefaultValues() {
        // Default placeholder values - should be replaced with actual credentials
        properties.setProperty("google.client.id", "YOUR_GOOGLE_CLIENT_ID");
        properties.setProperty("google.client.secret", "YOUR_GOOGLE_CLIENT_SECRET");
        properties.setProperty("google.redirect.uri", "http://localhost:8080/callback/google");
        properties.setProperty("github.client.id", "YOUR_GITHUB_CLIENT_ID");
        properties.setProperty("github.client.secret", "YOUR_GITHUB_CLIENT_SECRET");
        properties.setProperty("github.redirect.uri", "http://localhost:8080/callback/github");
        properties.setProperty("cloudflare.client.id", "YOUR_CLOUDFLARE_CLIENT_ID");
        properties.setProperty("cloudflare.client.secret", "YOUR_CLOUDFLARE_CLIENT_SECRET");
        properties.setProperty("cloudflare.redirect.uri", "http://localhost:8080/callback/cloudflare");
    }
    
    public static String getGoogleClientId() {
        return properties.getProperty("google.client.id");
    }
    
    public static String getGoogleClientSecret() {
        return properties.getProperty("google.client.secret");
    }
    
    public static String getGoogleRedirectUri() {
        return properties.getProperty("google.redirect.uri");
    }
    
    public static String getGithubClientId() {
        return properties.getProperty("github.client.id");
    }
    
    public static String getGithubClientSecret() {
        return properties.getProperty("github.client.secret");
    }
    
    public static String getGithubRedirectUri() {
        return properties.getProperty("github.redirect.uri");
    }
    
    public static String getCloudflareClientId() {
        return properties.getProperty("cloudflare.client.id");
    }
    
    public static String getCloudflareClientSecret() {
        return properties.getProperty("cloudflare.client.secret");
    }
    
    public static String getCloudflareRedirectUri() {
        return properties.getProperty("cloudflare.redirect.uri");
    }
}
