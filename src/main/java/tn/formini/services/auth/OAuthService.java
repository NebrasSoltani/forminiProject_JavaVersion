package tn.formini.services.auth;

import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.UserService;
import tn.formini.utils.OAuthConfig;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class OAuthService {
    private static final String STATE_SECRET = "formini_oauth_secret";
    private static final Gson gson = new Gson();
    
    // Google OAuth
    public static String getGoogleAuthorizationUrl() {
        String state = generateState();
        OAuth20Service service = new ServiceBuilder(OAuthConfig.getGoogleClientId())
                .apiSecret(OAuthConfig.getGoogleClientSecret())
                .callback(OAuthConfig.getGoogleRedirectUri())
                .build(GoogleApi20.instance());
        
        return service.getAuthorizationUrl();
    }
    
    public static User handleGoogleCallback(String code, String state) throws Exception {
        OAuth20Service service = new ServiceBuilder(OAuthConfig.getGoogleClientId())
                .apiSecret(OAuthConfig.getGoogleClientSecret())
                .callback(OAuthConfig.getGoogleRedirectUri())
                .build(GoogleApi20.instance());
        
        OAuth2AccessToken token = service.getAccessToken(code);
        
        // Get user info from Google
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/oauth2/v2/userinfo");
        service.signRequest(token, request);
        Response response = service.execute(request);
        
        JsonObject userInfo = gson.fromJson(response.getBody(), JsonObject.class);
        
        return createOrUpdateUserFromOAuth(userInfo, "google");
    }
    
    // GitHub OAuth
    public static String getGithubAuthorizationUrl() {
        String state = generateState();
        OAuth20Service service = new ServiceBuilder(OAuthConfig.getGithubClientId())
                .apiSecret(OAuthConfig.getGithubClientSecret())
                .callback(OAuthConfig.getGithubRedirectUri())
                .build(GitHubApi.instance());
        
        return service.getAuthorizationUrl();
    }
    
    public static User handleGithubCallback(String code, String state) throws Exception {
        OAuth20Service service = new ServiceBuilder(OAuthConfig.getGithubClientId())
                .apiSecret(OAuthConfig.getGithubClientSecret())
                .callback(OAuthConfig.getGithubRedirectUri())
                .build(GitHubApi.instance());
        
        OAuth2AccessToken token = service.getAccessToken(code);
        
        // Get user info from GitHub
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
        service.signRequest(token, request);
        Response response = service.execute(request);
        
        JsonObject userInfo = gson.fromJson(response.getBody(), JsonObject.class);
        
        // Get email (might need separate call for private emails)
        OAuthRequest emailRequest = new OAuthRequest(Verb.GET, "https://api.github.com/user/emails");
        service.signRequest(token, emailRequest);
        Response emailResponse = service.execute(emailRequest);
        
        return createOrUpdateUserFromGitHub(userInfo, emailResponse.getBody(), "github");
    }
    
    // Cloudflare OAuth
    public static String getCloudflareAuthorizationUrl() {
        String state = generateState();
        OAuth20Service service = new ServiceBuilder(OAuthConfig.getCloudflareClientId())
                .apiSecret(OAuthConfig.getCloudflareClientSecret())
                .callback(OAuthConfig.getCloudflareRedirectUri())
                .build(CloudflareApi.instance());
        
        return service.getAuthorizationUrl();
    }
    
    public static User handleCloudflareCallback(String code, String state) throws Exception {
        OAuth20Service service = new ServiceBuilder(OAuthConfig.getCloudflareClientId())
                .apiSecret(OAuthConfig.getCloudflareClientSecret())
                .callback(OAuthConfig.getCloudflareRedirectUri())
                .build(CloudflareApi.instance());
        
        OAuth2AccessToken token = service.getAccessToken(code);
        
        // Get user info from Cloudflare
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://dash.cloudflare.com/oauth2/userinfo");
        service.signRequest(token, request);
        Response response = service.execute(request);
        
        JsonObject userInfo = gson.fromJson(response.getBody(), JsonObject.class);
        
        return createOrUpdateUserFromCloudflare(userInfo, "cloudflare");
    }
    
    private static User createOrUpdateUserFromCloudflare(JsonObject userInfo, String provider) {
        UserService userService = new UserService();
        
        String email = userInfo.has("email") ? userInfo.get("email").getAsString() : "";
        String name = userInfo.has("name") ? userInfo.get("name").getAsString() : "";
        String picture = userInfo.has("picture") ? userInfo.get("picture").getAsString() : "";
        String providerId = userInfo.has("id") ? userInfo.get("id").getAsString() : "";
        
        if (email.isEmpty()) {
            throw new RuntimeException("Unable to get email from Cloudflare.");
        }
        
        // Check if user exists by email
        User existingUser = userService.getUserByEmail(email);
        
        if (existingUser != null) {
            // Update existing user with OAuth info
            existingUser.setOauth_provider("cloudflare");
            if (!picture.isEmpty()) {
                existingUser.setAvatar_url(picture);
            }
            existingUser.setIs_email_verified(true);
            userService.modifier(existingUser);
            return existingUser;
        }
        
        // Create new user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setNom(name.contains(" ") ? name.split(" ")[0] : name);
        newUser.setPrenom(name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "");
        newUser.setRole_utilisateur("apprenant"); // Default role
        newUser.setRoles("[\"ROLE_USER\"]");
        newUser.setTelephone("00000000"); // Placeholder
        newUser.setGouvernorat("");
        newUser.setDate_naissance(new java.util.Date());
        newUser.setPhoto(picture);
        newUser.setIs_email_verified(true);
        newUser.setOauth_provider("cloudflare");
        
        userService.ajouter(newUser);
        return newUser;
    }
    
    private static User createOrUpdateUserFromOAuth(JsonObject userInfo, String provider) {
        UserService userService = new UserService();
        
        String email = userInfo.get("email").getAsString();
        String name = userInfo.has("name") ? userInfo.get("name").getAsString() : 
                     userInfo.has("given_name") ? userInfo.get("given_name").getAsString() : "";
        String picture = userInfo.has("picture") ? userInfo.get("picture").getAsString() : "";
        String providerId = userInfo.has("id") ? userInfo.get("id").getAsString() : "";
        
        // Check if user exists by email
        User existingUser = userService.getUserByEmail(email);
        
        if (existingUser != null) {
            // Update existing user with OAuth info
            if (provider.equals("google")) {
                existingUser.setGoogle_id(providerId);
                existingUser.setOauth_provider("google");
            }
            if (!picture.isEmpty()) {
                existingUser.setAvatar_url(picture);
            }
            existingUser.setIs_email_verified(true);
            userService.modifier(existingUser);
            return existingUser;
        }
        
        // Create new user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setNom(name.contains(" ") ? name.split(" ")[0] : name);
        newUser.setPrenom(name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "");
        newUser.setRole_utilisateur("apprenant"); // Default role
        newUser.setRoles("[\"ROLE_USER\"]");
        newUser.setTelephone("00000000"); // Placeholder
        newUser.setGouvernorat("");
        newUser.setDate_naissance(new java.util.Date());
        newUser.setPhoto(picture);
        newUser.setIs_email_verified(true);
        
        if (provider.equals("google")) {
            newUser.setGoogle_id(providerId);
            newUser.setOauth_provider("google");
        }
        
        userService.ajouter(newUser);
        return newUser;
    }
    
    private static User createOrUpdateUserFromGitHub(JsonObject userInfo, String emailsJson, String provider) {
        UserService userService = new UserService();
        
        String login = userInfo.has("login") ? userInfo.get("login").getAsString() : "";
        String name = userInfo.has("name") ? userInfo.get("name").getAsString() : login;
        String avatarUrl = userInfo.has("avatar_url") ? userInfo.get("avatar_url").getAsString() : "";
        String providerId = userInfo.has("id") ? userInfo.get("id").getAsString() : "";
        
        // Parse emails to get primary email
        String email = "";
        try {
            com.google.gson.JsonArray emails = gson.fromJson(emailsJson, com.google.gson.JsonArray.class);
            for (com.google.gson.JsonElement emailObj : emails) {
                JsonObject emailJson = emailObj.getAsJsonObject();
                if (emailJson.has("primary") && emailJson.get("primary").getAsBoolean()) {
                    email = emailJson.get("email").getAsString();
                    break;
                }
            }
            if (email.isEmpty() && emails.size() > 0) {
                email = emails.get(0).getAsJsonObject().get("email").getAsString();
            }
        } catch (Exception e) {
            System.err.println("Error parsing GitHub emails: " + e.getMessage());
        }
        
        if (email.isEmpty()) {
            throw new RuntimeException("Unable to get email from GitHub. Please make your email public.");
        }
        
        // Check if user exists by email
        User existingUser = userService.getUserByEmail(email);
        
        if (existingUser != null) {
            // Update existing user with OAuth info
            existingUser.setGithub_id(providerId);
            existingUser.setOauth_provider("github");
            if (!avatarUrl.isEmpty()) {
                existingUser.setAvatar_url(avatarUrl);
            }
            existingUser.setIs_email_verified(true);
            userService.modifier(existingUser);
            return existingUser;
        }
        
        // Create new user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setNom(name.contains(" ") ? name.split(" ")[0] : name);
        newUser.setPrenom(name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "");
        newUser.setRole_utilisateur("apprenant"); // Default role
        newUser.setRoles("[\"ROLE_USER\"]");
        newUser.setTelephone("00000000"); // Placeholder
        newUser.setGouvernorat("");
        newUser.setDate_naissance(new java.util.Date());
        newUser.setPhoto(avatarUrl);
        newUser.setIs_email_verified(true);
        newUser.setGithub_id(providerId);
        newUser.setOauth_provider("github");
        
        userService.ajouter(newUser);
        return newUser;
    }
    
    private static String generateState() {
        Random random = new Random();
        return String.valueOf(random.nextInt(999999));
    }
    
    public static boolean isConfigured(String provider) {
        if (provider.equals("google")) {
            return !OAuthConfig.getGoogleClientId().equals("YOUR_GOOGLE_CLIENT_ID") &&
                   !OAuthConfig.getGoogleClientSecret().equals("YOUR_GOOGLE_CLIENT_SECRET");
        } else if (provider.equals("github")) {
            return !OAuthConfig.getGithubClientId().equals("YOUR_GITHUB_CLIENT_ID") &&
                   !OAuthConfig.getGithubClientSecret().equals("YOUR_GITHUB_CLIENT_SECRET");
        } else if (provider.equals("cloudflare")) {
            return !OAuthConfig.getCloudflareClientId().equals("YOUR_CLOUDFLARE_CLIENT_ID") &&
                   !OAuthConfig.getCloudflareClientSecret().equals("YOUR_CLOUDFLARE_CLIENT_SECRET");
        }
        return false;
    }
}
