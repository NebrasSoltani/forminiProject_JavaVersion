package tn.formini.services.auth;

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
        OAuth20Service service = new ServiceBuilder(OAuthConfig.getGoogleClientId())
                .apiSecret(OAuthConfig.getGoogleClientSecret())
                .callback(OAuthConfig.getGoogleRedirectUri())
                .build(GoogleApi20Custom.instance());

        String authUrl = service.getAuthorizationUrl();
        // Manually add scope parameter since ScribeJava version doesn't support it
        if (!authUrl.contains("scope=")) {
            authUrl += "&scope=openid%20email%20profile";
        }
        return authUrl;
    }

    public static User handleGoogleCallback(String code, String state, String role) throws Exception {
        // Manually exchange code for token since Google returns JSON format
        String tokenResponse = exchangeCodeForToken(code);
        JsonObject tokenJson = gson.fromJson(tokenResponse, JsonObject.class);
        String accessToken = tokenJson.get("access_token").getAsString();

        // Get user info from Google
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
        String userInfoResponse = makeAuthenticatedGetRequest(userInfoUrl, accessToken);
        JsonObject userInfo = gson.fromJson(userInfoResponse, JsonObject.class);

        return createOrUpdateUserFromOAuth(userInfo, "google", role);
    }

    // Check if user exists before showing role selection
    public static boolean userExists(String email) {
        UserService userService = new UserService();
        return userService.getUserByEmail(email) != null;
    }

    // Get user info from Google code without creating user
    public static JsonObject getGoogleUserInfo(String code) throws Exception {
        String tokenResponse = exchangeCodeForToken(code);
        JsonObject tokenJson = gson.fromJson(tokenResponse, JsonObject.class);
        String accessToken = tokenJson.get("access_token").getAsString();

        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
        String userInfoResponse = makeAuthenticatedGetRequest(userInfoUrl, accessToken);
        return gson.fromJson(userInfoResponse, JsonObject.class);
    }

    // Get user info from GitHub code without creating user
    public static JsonObject getGitHubUserInfo(String code) throws Exception {
        OAuth20Service service = new ServiceBuilder(OAuthConfig.getGithubClientId())
                .apiSecret(OAuthConfig.getGithubClientSecret())
                .callback(OAuthConfig.getGithubRedirectUri())
                .build(GitHubApiCustom.instance());

        OAuth2AccessToken token = service.getAccessToken(code);

        // Get user info from GitHub
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
        service.signRequest(token, request);
        Response response = service.execute(request);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    private static String exchangeCodeForToken(String code) throws Exception {
        java.net.URI uri = java.net.URI.create("https://oauth2.googleapis.com/token");
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        
        StringBuilder formBody = new StringBuilder();
        formBody.append("code=").append(java.net.URLEncoder.encode(code, "UTF-8"));
        formBody.append("&client_id=").append(java.net.URLEncoder.encode(OAuthConfig.getGoogleClientId(), "UTF-8"));
        formBody.append("&client_secret=").append(java.net.URLEncoder.encode(OAuthConfig.getGoogleClientSecret(), "UTF-8"));
        formBody.append("&redirect_uri=").append(java.net.URLEncoder.encode(OAuthConfig.getGoogleRedirectUri(), "UTF-8"));
        formBody.append("&grant_type=authorization_code");

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(formBody.toString()))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private static String makeAuthenticatedGetRequest(String url, String accessToken) throws Exception {
        java.net.URI uri = java.net.URI.create(url);
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    // GitHub OAuth
    public static String getGithubAuthorizationUrl() {
        OAuth20Service service = new ServiceBuilder(OAuthConfig.getGithubClientId())
                .apiSecret(OAuthConfig.getGithubClientSecret())
                .callback(OAuthConfig.getGithubRedirectUri())
                .build(GitHubApiCustom.instance());

        return service.getAuthorizationUrl();
    }

    public static User handleGithubCallback(String code, String state, String role) throws Exception {
        OAuth20Service service = new ServiceBuilder(OAuthConfig.getGithubClientId())
                .apiSecret(OAuthConfig.getGithubClientSecret())
                .callback(OAuthConfig.getGithubRedirectUri())
                .build(GitHubApiCustom.instance());
        
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
        
        return createOrUpdateUserFromGitHub(userInfo, emailResponse.getBody(), "github", role);
    }
    
    // Cloudflare OAuth
    public static String getCloudflareAuthorizationUrl() {
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
        newUser.setPassword("OAuthUser123_" + System.currentTimeMillis()); // OAuth users don't have passwords
        newUser.setOauth_provider("cloudflare");

        userService.ajouter(newUser);
        return newUser;
    }
    
    private static User createOrUpdateUserFromOAuth(JsonObject userInfo, String provider, String role) {
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
        newUser.setRole_utilisateur(role != null ? role : "apprenant"); // Use selected role or default
        newUser.setRoles("[\"ROLE_USER\"]");
        newUser.setTelephone("00000000"); // Placeholder
        newUser.setGouvernorat("");
        newUser.setDate_naissance(new java.util.Date());
        newUser.setPhoto(picture);
        newUser.setIs_email_verified(true);
        newUser.setPassword("OAuthUser123_" + System.currentTimeMillis()); // OAuth users don't have passwords

        if (provider.equals("google")) {
            newUser.setGoogle_id(providerId);
            newUser.setOauth_provider("google");
        }

        userService.ajouter(newUser);
        return newUser;
    }

    private static User createOrUpdateUserFromGitHub(JsonObject userInfo, String emailsJson, String provider, String role) {
        UserService userService = new UserService();

        String login = userInfo.has("login") ? userInfo.get("login").getAsString() : "";
        String name = userInfo.has("name") ? userInfo.get("name").getAsString() : login;
        String avatarUrl = userInfo.has("avatar_url") ? userInfo.get("avatar_url").getAsString() : "";
        String providerId = userInfo.has("id") ? userInfo.get("id").getAsString() : "";

        // Parse emails to get primary email
        String email = "";
        try {
            // Try to parse as array first
            com.google.gson.JsonArray emails = gson.fromJson(emailsJson, com.google.gson.JsonArray.class);
            for (com.google.gson.JsonElement emailElement : emails) {
                com.google.gson.JsonObject emailObj = emailElement.getAsJsonObject();
                if (emailObj.has("primary") && emailObj.get("primary").getAsBoolean()) {
                    email = emailObj.get("email").getAsString();
                    break;
                }
            }
            if (email.isEmpty() && emails.size() > 0) {
                email = emails.get(0).getAsJsonObject().get("email").getAsString();
            }
        } catch (Exception e) {
            System.err.println("Error parsing GitHub emails as array: " + e.getMessage());
            // Try to parse as single object
            try {
                com.google.gson.JsonObject emailObj = gson.fromJson(emailsJson, com.google.gson.JsonObject.class);
                if (emailObj.has("email")) {
                    email = emailObj.get("email").getAsString();
                }
            } catch (Exception e2) {
                System.err.println("Error parsing GitHub emails as object: " + e2.getMessage());
            }
        }

        // If still no email, try to get from user info
        if (email.isEmpty() && userInfo.has("email")) {
            email = userInfo.get("email").getAsString();
        }

        // If still no email, use login as fallback
        if (email.isEmpty()) {
            email = login + "@github.local";
            System.err.println("Using fallback email: " + email);
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
        newUser.setRole_utilisateur(role != null ? role : "apprenant"); // Use selected role or default
        newUser.setRoles("[\"ROLE_USER\"]");
        newUser.setTelephone("00000000"); // Placeholder
        newUser.setGouvernorat("");
        newUser.setDate_naissance(new java.util.Date());
        newUser.setPhoto(avatarUrl);
        newUser.setIs_email_verified(true);
        newUser.setPassword("OAuthUser123_" + System.currentTimeMillis()); // OAuth users don't have passwords
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
