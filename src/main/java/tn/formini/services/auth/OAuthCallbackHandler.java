package tn.formini.services.auth;

import com.sun.net.httpserver.HttpServer;
import tn.formini.entities.Users.User;
import tn.formini.tools.SessionManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OAuthCallbackHandler {
    private HttpServer server;
    private CountDownLatch latch;
    private User authenticatedUser;
    private String errorMessage;
    private String oauthCode;
    private String oauthState;
    
    public User authenticateWithGoogle() {
        return authenticate("google");
    }
    
    public User authenticateWithGithub() {
        return authenticate("github");
    }
    
    public User authenticateWithCloudflare() {
        return authenticate("cloudflare");
    }
    
    private User authenticate(String provider) {
        if (!OAuthService.isConfigured(provider)) {
            System.err.println("OAuth not configured for " + provider);
            return null;
        }
        
        try {
            // Start local server
            startServer();
            
            // Get authorization URL
            String authUrl;
            if (provider.equals("google")) {
                authUrl = OAuthService.getGoogleAuthorizationUrl();
            } else if (provider.equals("github")) {
                authUrl = OAuthService.getGithubAuthorizationUrl();
            } else {
                authUrl = OAuthService.getCloudflareAuthorizationUrl();
            }
            
            // Open browser
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(authUrl));
            
            // Wait for callback (timeout after 5 minutes)
            boolean completed = latch.await(5, TimeUnit.MINUTES);
            
            if (!completed) {
                System.err.println("OAuth authentication timed out");
                return null;
            }
            
            if (errorMessage != null) {
                System.err.println("OAuth error: " + errorMessage);
                return null;
            }
            
            return authenticatedUser;
            
        } catch (Exception e) {
            System.err.println("OAuth authentication error: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            stopServer();
        }
    }
    
    private void startServer() throws IOException {
        latch = new CountDownLatch(1);
        authenticatedUser = null;
        errorMessage = null;

        // Try to find an available port starting from 8080
        int port = 8080;
        int maxAttempts = 10;
        IOException lastException = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                break;
            } catch (IOException e) {
                lastException = e;
                port++;
            }
        }

        if (server == null) {
            throw new IOException("Could not find an available port after " + maxAttempts + " attempts", lastException);
        }

        // Google callback endpoint
        server.createContext("/callback/google", exchange -> {
            handleCallback(exchange, "google");
        });

        // GitHub callback endpoint
        server.createContext("/callback/github", exchange -> {
            handleCallback(exchange, "github");
        });

        // Cloudflare callback endpoint
        server.createContext("/callback/cloudflare", exchange -> {
            handleCallback(exchange, "cloudflare");
        });

        server.setExecutor(null);
        server.start();
        System.out.println("OAuth callback server started on port " + port);
    }
    
    private void handleCallback(com.sun.net.httpserver.HttpExchange exchange, String provider) {
        try {
            String query = exchange.getRequestURI().getQuery();
            System.out.println("Received callback: " + query);

            // Check if this is a role selection submission
            if (query != null && query.contains("role=")) {
                handleRoleSelection(exchange, provider, query);
                return;
            }

            if (query == null || query.isEmpty()) {
                sendResponse(exchange, "Error: No callback parameters", 400);
                errorMessage = "No callback parameters received";
                latch.countDown();
                return;
            }

            // Parse query parameters
            String code = null;
            String state = null;
            String error = null;

            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    String key = pair[0];
                    String value = java.net.URLDecoder.decode(pair[1], "UTF-8");

                    if (key.equals("code")) {
                        code = value;
                    } else if (key.equals("state")) {
                        state = value;
                    } else if (key.equals("error")) {
                        error = value;
                    }
                }
            }

            if (error != null) {
                sendResponse(exchange, "Authentication cancelled or failed", 400);
                errorMessage = "OAuth error: " + error;
                latch.countDown();
                return;
            }

            if (code == null) {
                sendResponse(exchange, "Error: No authorization code received", 400);
                errorMessage = "No authorization code received";
                latch.countDown();
                return;
            }

            // Store the code and state for role selection
            this.oauthCode = code;
            this.oauthState = state;

            // For Google and GitHub, check if user exists before showing role selection
            if (provider.equals("google") || provider.equals("github")) {
                try {
                    // Get user info to check if user exists
                    String email = null;
                    if (provider.equals("google")) {
                        com.google.gson.JsonObject userInfo = OAuthService.getGoogleUserInfo(code);
                        email = userInfo.has("email") ? userInfo.get("email").getAsString() : null;
                    } else {
                        com.google.gson.JsonObject userInfo = OAuthService.getGitHubUserInfo(code);
                        email = userInfo.has("email") && !userInfo.get("email").isJsonNull() ? 
                                userInfo.get("email").getAsString() : null;
                    }

                    // If user exists, skip role selection and proceed with existing role
                    if (email != null && OAuthService.userExists(email)) {
                        processAuthentication(provider, code, state, null); // null role = use existing
                    } else {
                        // New user, show role selection page
                        sendRoleSelectionPage(exchange, provider, code, state);
                    }
                } catch (Exception e) {
                    System.err.println("Error checking user existence: " + e.getMessage());
                    // If check fails, show role selection as fallback
                    sendRoleSelectionPage(exchange, provider, code, state);
                }
            } else {
                // Cloudflare doesn't require role selection, proceed directly
                processAuthentication(provider, code, state, "apprenant");
            }

        } catch (Exception e) {
            System.err.println("Error handling callback: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRoleSelection(com.sun.net.httpserver.HttpExchange exchange, String provider, String query) {
        try {
            String role = null;
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && pair[0].equals("role")) {
                    role = java.net.URLDecoder.decode(pair[1], "UTF-8");
                    break;
                }
            }

            if (role == null || (!role.equals("apprenant") && !role.equals("formateur"))) {
                sendResponse(exchange, "Invalid role selection", 400);
                errorMessage = "Invalid role selected";
                latch.countDown();
                return;
            }

            processAuthentication(provider, oauthCode, oauthState, role);

            // Send success response after authentication
            if (authenticatedUser != null) {
                sendSuccessPage(exchange);
            } else {
                sendResponse(exchange, "Authentication failed: " + errorMessage, 500);
            }

            // Count down latch AFTER sending response
            latch.countDown();

        } catch (Exception e) {
            System.err.println("Error handling role selection: " + e.getMessage());
            e.printStackTrace();
            try {
                sendResponse(exchange, "Error: " + e.getMessage(), 500);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            latch.countDown();
        }
    }

    private void processAuthentication(String provider, String code, String state, String role) {
        try {
            if (provider.equals("google")) {
                authenticatedUser = OAuthService.handleGoogleCallback(code, state, role);
            } else if (provider.equals("github")) {
                authenticatedUser = OAuthService.handleGithubCallback(code, state, role);
            } else {
                authenticatedUser = OAuthService.handleCloudflareCallback(code, state);
            }

            if (authenticatedUser != null) {
                System.out.println("OAuth authentication successful for user: " + authenticatedUser.getEmail());
            } else {
                errorMessage = "Failed to create/update user";
            }
        } catch (Exception e) {
            errorMessage = "Error processing authentication: " + e.getMessage();
            e.printStackTrace();
        }
    }

    private void sendRoleSelectionPage(com.sun.net.httpserver.HttpExchange exchange, String provider, String code, String state) throws IOException {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>Choose Your Role - Formini</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); margin: 0; }\n" +
                "        .container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 10px 25px rgba(0,0,0,0.2); text-align: center; max-width: 400px; }\n" +
                "        h1 { color: #333; margin-bottom: 10px; }\n" +
                "        p { color: #666; margin-bottom: 30px; }\n" +
                "        .role-buttons { display: flex; flex-direction: column; gap: 15px; }\n" +
                "        .role-btn { padding: 15px 30px; font-size: 16px; border: none; border-radius: 5px; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; }\n" +
                "        .role-btn:hover { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(0,0,0,0.2); }\n" +
                "        .btn-apprenant { background: #4CAF50; color: white; }\n" +
                "        .btn-formateur { background: #2196F3; color: white; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <h1>Choose Your Role</h1>\n" +
                "        <p>Select how you want to use Formini:</p>\n" +
                "        <div class='role-buttons'>\n" +
                "            <button class='role-btn btn-apprenant' onclick='selectRole(\"apprenant\")'>📚 Apprenant (Learner)</button>\n" +
                "            <button class='role-btn btn-formateur' onclick='selectRole(\"formateur\")'>👨‍🏫 Formateur (Instructor)</button>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        function selectRole(role) {\n" +
                "            const url = window.location.pathname + '?code=" + code + "&state=" + state + "&role=' + role;\n" +
                "            window.location.href = url;\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, html.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(html.getBytes());
        }
    }
    
    private void sendResponse(com.sun.net.httpserver.HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void sendSuccessPage(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>Authentication Successful - Formini</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); margin: 0; }\n" +
                "        .container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 10px 25px rgba(0,0,0,0.2); text-align: center; max-width: 400px; }\n" +
                "        h1 { color: #4CAF50; margin-bottom: 10px; }\n" +
                "        p { color: #666; margin-bottom: 20px; }\n" +
                "        .success-icon { font-size: 60px; margin-bottom: 20px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <div class='success-icon'>✅</div>\n" +
                "        <h1>Authentication Successful!</h1>\n" +
                "        <p>You have been successfully authenticated. You can now close this window and return to the application.</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, html.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(html.getBytes());
        }
    }
    
    private void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("OAuth callback server stopped");
        }
    }
}
