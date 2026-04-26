package tn.formini.services.ai;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Service d'appel à l'API Groq (compatible OpenAI).
 * Utilisé pour le chatbot de recommandation de blogs.
 */
public class GroqChatService {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY   = System.getenv("GROQ_API_KEY");
    private static final String MODEL     = "llama-3.1-8b-instant";

    /**
     * Envoie une conversation à Groq et retourne la réponse de l'assistant.
     *
     * @param systemPrompt  Instructions système (rôle du chatbot)
     * @param userMessage   Message de l'utilisateur
     * @return              Réponse textuelle de Groq, ou message d'erreur
     */
    public String chat(String systemPrompt, String userMessage) {
        try {
            // ── Construction du JSON de requête ───────────────────────────
            String jsonBody = buildRequestJson(systemPrompt, userMessage);

            // ── Connexion HTTP ────────────────────────────────────────────
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(60_000);
            conn.setDoOutput(true);

            // ── Envoi ─────────────────────────────────────────────────────
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            // ── Lecture de la réponse ─────────────────────────────────────
            int statusCode = conn.getResponseCode();
            InputStream is = (statusCode >= 200 && statusCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String rawResponse = readStream(is);

            if (statusCode != 200) {
                System.err.println("[GroqChatService] Erreur HTTP " + statusCode + " : " + rawResponse);
                return "❌ Erreur API Groq (code " + statusCode + "). Vérifiez votre connexion.";
            }

            return extractContent(rawResponse);

        } catch (Exception e) {
            System.err.println("[GroqChatService] Exception : " + e.getMessage());
            return "❌ Erreur : " + e.getMessage();
        }
    }

    /**
     * Construit le JSON de la requête OpenAI-compatible.
     * Utilise une échappement manuelle pour éviter toute dépendance JSON externe.
     */
    private String buildRequestJson(String systemPrompt, String userMessage) {
        return "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"max_tokens\":1024,"
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":" + jsonString(systemPrompt) + "},"
                + "{\"role\":\"user\",\"content\":" + jsonString(userMessage) + "}"
                + "]"
                + "}";
    }

    /**
     * Échappe une chaîne pour l'inclure dans un JSON.
     */
    private String jsonString(String s) {
        if (s == null) return "\"\"";
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    /**
     * Lit un InputStream et retourne son contenu sous forme de String.
     */
    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    /**
     * Extrait le texte de la réponse JSON Groq :
     * choices[0].message.content
     */
    private String extractContent(String json) {
        try {
            // Recherche de "content":"..."
            String marker = "\"content\":";
            int idx = json.indexOf(marker);
            if (idx < 0) return "Pas de réponse reçue.";
            idx += marker.length();

            // Saute les espaces et la première guillemet
            while (idx < json.length() && json.charAt(idx) != '"') idx++;
            idx++; // passe la guillemet ouvrante

            // Lit jusqu'à la guillemet fermante (non échappée)
            StringBuilder content = new StringBuilder();
            while (idx < json.length()) {
                char c = json.charAt(idx);
                if (c == '\\' && idx + 1 < json.length()) {
                    char next = json.charAt(idx + 1);
                    switch (next) {
                        case 'n' -> content.append('\n');
                        case 't' -> content.append('\t');
                        case '"' -> content.append('"');
                        case '\\' -> content.append('\\');
                        default  -> content.append(next);
                    }
                    idx += 2;
                } else if (c == '"') {
                    break; // fin du contenu
                } else {
                    content.append(c);
                    idx++;
                }
            }
            return content.toString().trim();
        } catch (Exception e) {
            return json; // retourne brut en cas d'erreur de parsing
        }
    }
}
