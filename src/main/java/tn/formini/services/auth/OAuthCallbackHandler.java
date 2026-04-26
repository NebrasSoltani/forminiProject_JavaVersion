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
        
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        
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
        System.out.println("OAuth callback server started on port 8080");
    }
    
    private void handleCallback(com.sun.net.httpserver.HttpExchange exchange, String provider) {
        try {
            String query = exchange.getRequestURI().getQuery();
            System.out.println("Received callback: " + query);
            
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
            
            // Process the callback
            try {
                if (provider.equals("google")) {
                    authenticatedUser = OAuthService.handleGoogleCallback(code, state);
                } else if (provider.equals("github")) {
                    authenticatedUser = OAuthService.handleGithubCallback(code, state);
                } else {
                    authenticatedUser = OAuthService.handleCloudflareCallback(code, state);
                }
                
                if (authenticatedUser != null) {
                    sendResponse(exchange, "Authentication successful! You can close this window.", 200);
                    System.out.println("OAuth authentication successful for user: " + authenticatedUser.getEmail());
                } else {
                    sendResponse(exchange, "Authentication failed. Please try again.", 400);
                    errorMessage = "Failed to create/update user";
                }
            } catch (Exception e) {
                sendResponse(exchange, "Error processing authentication: " + e.getMessage(), 500);
                errorMessage = "Error processing authentication: " + e.getMessage();
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.err.println("Error handling callback: " + e.getMessage());
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }
    
    private void sendResponse(com.sun.net.httpserver.HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    private void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("OAuth callback server stopped");
        }
    }
}
