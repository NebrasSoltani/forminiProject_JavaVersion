package tn.formini.services;

import tn.formini.entities.produits.Produit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * AI-powered product search suggestion service
 * Uses Gemini API to provide intelligent product recommendations
 */
public class AdvancedProductAIService {

    private static AdvancedProductAIService instance;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private String apiKey;

    private AdvancedProductAIService() {
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            this.apiKey = envKey.trim();
        } else {
            this.apiKey = "AIzaSyA4-Xmxn3SO-5sbZw0t0315N7qsCKGKjx8";
        }
    }

    public static synchronized AdvancedProductAIService getInstance() {
        if (instance == null) {
            instance = new AdvancedProductAIService();
        }
        return instance;
    }

    /**
     * Get AI-powered search suggestions for products
     * @param searchTerm The user's search term
     * @param userContext Additional context about available categories
     * @return CompletableFuture containing list of suggested product names
     */
    public CompletableFuture<List<String>> getSearchSuggestions(String searchTerm, String userContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = buildSearchPrompt(searchTerm, userContext);
                String jsonResponse = callGeminiApi(prompt);
                return parseSuggestionsResponse(jsonResponse);
            } catch (Exception e) {
                System.err.println("Error getting AI search suggestions: " + e.getMessage());
                return List.of("Erreur lors de la génération de suggestions");
            }
        });
    }

    /**
     * Get AI-powered cart-based product suggestions
     * @param cartProducts List of products currently in the cart
     * @return CompletableFuture containing list of suggested product names
     */
    public CompletableFuture<List<String>> getCartBasedSuggestions(List<Produit> cartProducts) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = buildCartPrompt(cartProducts);
                String jsonResponse = callGeminiApi(prompt);
                return parseSuggestionsResponse(jsonResponse);
            } catch (Exception e) {
                System.err.println("Error getting AI cart suggestions: " + e.getMessage());
                return List.of("Produit complémentaire 1", "Produit complémentaire 2", "Produit complémentaire 3");
            }
        });
    }

    private String buildSearchPrompt(String searchTerm, String userContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tu es un assistant de recherche de produits pour une boutique en ligne.\n\n");
        prompt.append("L'utilisateur recherche: \"").append(searchTerm).append("\"\n");
        if (userContext != null && !userContext.isEmpty()) {
            prompt.append("Contexte disponible: ").append(userContext).append("\n");
        }
        prompt.append("\nGénère 3 à 5 suggestions de produits pertinents qui pourraient correspondre à cette recherche.\n");
        prompt.append("Les suggestions doivent être des noms de produits réalistes et pertinents.\n");
        prompt.append("Réponds uniquement avec une liste JSON de chaînes de caractères, sans autre texte.\n");
        prompt.append("Format: [\"suggestion1\", \"suggestion2\", \"suggestion3\"]");
        return prompt.toString();
    }

    private String buildCartPrompt(List<Produit> cartProducts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tu es un assistant de recommandation de produits pour une boutique en ligne.\n\n");
        prompt.append("L'utilisateur a les produits suivants dans son panier:\n");
        for (Produit p : cartProducts) {
            prompt.append("- ").append(p.getNom());
            if (p.getCategorie() != null) {
                prompt.append(" (Catégorie: ").append(p.getCategorie()).append(")");
            }
            prompt.append("\n");
        }
        prompt.append("\nGénère 3 à 5 suggestions de produits complémentaires qui seraient pertinents pour cet utilisateur.\n");
        prompt.append("Les suggestions doivent être des noms de produits réalistes qui complètent bien les articles du panier.\n");
        prompt.append("Réponds uniquement avec une liste JSON de chaînes de caractères, sans autre texte.\n");
        prompt.append("Format: [\"suggestion1\", \"suggestion2\", \"suggestion3\"]");
        return prompt.toString();
    }

    private String callGeminiApi(String prompt) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("Clé API Gemini non configurée.");
        }

        URL url = new URL(GEMINI_API_URL + "?key=" + apiKey.trim());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        String escapedPrompt = prompt.replace("\\", "\\\\")
                                     .replace("\"", "\\\"")
                                     .replace("\n", "\\n")
                                     .replace("\r", "\\r");

        String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + escapedPrompt + "\"}]}]}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorResponse = "";
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(), 
                    StandardCharsets.UTF_8))) {
                errorResponse = errorReader.lines().collect(Collectors.joining());
            } catch (Exception e) {
                errorResponse = "Erreur HTTP " + responseCode;
            }
            throw new Exception("Détail API : " + errorResponse);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining());
        }
    }

    private List<String> parseSuggestionsResponse(String jsonResponse) {
        try {
            // Try to parse as JSON array first
            String token = "\"text\": \"";
            int start = jsonResponse.indexOf(token);
            if (start == -1) {
                return List.of("Produit similaire 1", "Produit similaire 2", "Produit similaire 3");
            }
            start += token.length();
            
            StringBuilder result = new StringBuilder();
            for (int i = start; i < jsonResponse.length(); i++) {
                char c = jsonResponse.charAt(i);
                if (c == '\"' && jsonResponse.charAt(i-1) != '\\') break;
                result.append(c);
            }
            
            String content = result.toString()
                                 .replace("\\n", "\n")
                                 .replace("\\\"", "\"")
                                 .replace("\\\\", "\\");
            
            // Try to extract JSON array from content
            if (content.startsWith("[") && content.endsWith("]")) {
                // Simple parsing of JSON array
                String[] items = content.substring(1, content.length() - 1).split("\",\"");
                List<String> suggestions = new java.util.ArrayList<>();
                for (String item : items) {
                    String cleaned = item.replace("\"", "").trim();
                    if (!cleaned.isEmpty()) {
                        suggestions.add(cleaned);
                    }
                }
                if (!suggestions.isEmpty()) {
                    return suggestions;
                }
            }
            
            // Fallback: split by newlines if not JSON
            return List.of(content.split("\n"));
            
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + e.getMessage());
            return List.of("Produit similaire 1", "Produit similaire 2", "Produit similaire 3");
        }
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
