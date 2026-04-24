package tn.formini.services.ai;

import tn.formini.entities.ai.CvData;
import tn.formini.entities.ai.CvGenerationRequest;
import tn.formini.entities.ai.CvGenerationResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class CvGenerationService {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private String apiKey;

    public CvGenerationService() {
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            this.apiKey = envKey.trim();
        } else {
            // Utilisation de la nouvelle clé fournie
            this.apiKey = "AIzaSyA4-Xmxn3SO-5sbZw0t0315N7qsCKGKjx8";
        }
    }

    public CvGenerationService(String apiKey) {
        this.apiKey = apiKey;
    }

    public CvGenerationResponse generateCv(CvGenerationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            request.valider();

            String prompt = buildPrompt(request);

            String jsonResponse = callGeminiApi(prompt);

            String cvContent = parseGeminiResponse(jsonResponse);

            String suggestions = generateSuggestions(request.getCvData());

            long generationTime = System.currentTimeMillis() - startTime;

            CvGenerationResponse response = CvGenerationResponse.success(
                cvContent,
                request.getFormat(),
                request.getTemplateStyle(),
                generationTime
            );
            response.setSuggestions(suggestions);
            return response;

        } catch (Exception e) {
            return CvGenerationResponse.error("Erreur lors de la génération du CV: " + e.getMessage());
        }
    }

    private String buildPrompt(CvGenerationRequest request) {
        CvData data = request.getCvData();
        StringBuilder prompt = new StringBuilder();

        prompt.append("Tu es un expert en rédaction de CV professionnels. Génère un CV professionnel et attractif en markdown.\n\n");

        prompt.append("## Informations Personnelles\n");
        prompt.append("- Nom: ").append(data.getNom()).append("\n");
        prompt.append("- Prénom: ").append(data.getPrenom()).append("\n");
        prompt.append("- Email: ").append(data.getEmail()).append("\n");
        prompt.append("- Téléphone: ").append(data.getTelephone()).append("\n");
        if (data.getAdresse() != null) prompt.append("- Adresse: ").append(data.getAdresse()).append("\n");
        if (data.getProfession() != null) prompt.append("- Profession: ").append(data.getProfession()).append("\n");

        if (data.getSummary() != null && !data.getSummary().isEmpty()) {
            prompt.append("\n## Résumé Professionnel\n").append(data.getSummary()).append("\n");
        }

        if (data.getCompetences() != null && !data.getCompetences().isEmpty()) {
            prompt.append("\n## Compétences\n");
            for (String competence : data.getCompetences()) prompt.append("- ").append(competence).append("\n");
        }

        if (data.getExperiences() != null && !data.getExperiences().isEmpty()) {
            prompt.append("\n## Expérience Professionnelle\n");
            for (CvData.ExperienceProfessionnelle exp : data.getExperiences()) {
                prompt.append("### ").append(exp.getTitre()).append(" chez ").append(exp.getEntreprise()).append("\n");
                prompt.append("**Période:** ").append(exp.getDateDebut()).append(" - ").append(exp.getDateFin() != null ? exp.getDateFin() : "Présent").append("\n");
                if (exp.getDescription() != null) prompt.append(exp.getDescription()).append("\n");
                prompt.append("\n");
            }
        }

        if (data.getFormations() != null && !data.getFormations().isEmpty()) {
            prompt.append("\n## Formation\n");
            for (CvData.FormationAcademique formation : data.getFormations()) {
                prompt.append("### ").append(formation.getDiplome()).append("\n");
                prompt.append("**Établissement:** ").append(formation.getEtablissement()).append("\n");
                prompt.append("**Période:** ").append(formation.getDateDebut()).append(" - ").append(formation.getDateFin() != null ? formation.getDateFin() : "").append("\n");
                if (formation.getDescription() != null) prompt.append(formation.getDescription()).append("\n");
                prompt.append("\n");
            }
        }

        prompt.append("\n\nInstructions:\n");
        prompt.append("- Génère un CV professionnel, clair et bien structuré en français\n");
        prompt.append("- Utilise le format Markdown\n");
        if (request.getTargetJob() != null) prompt.append("- Adapte le contenu pour le poste de: ").append(request.getTargetJob()).append("\n");

        return prompt.toString();
    }

    public String analyzeMatching(String offreDescription, String cvContent) {
        if (!isApiKeyConfigured()) return "Erreur: Clé API Gemini non configurée.";

        String prompt = "En tant qu'expert en recrutement, analyse la compatibilité entre cette offre de stage et ce CV.\n\n" +
            "### OFFRE DE STAGE:\n" + offreDescription + "\n\n" +
            "### CV DU CANDIDAT:\n" + cvContent + "\n\n" +
            "Fournis une réponse structurée en français contenant:\n" +
            "1. Un score de compatibilité sur 100.\n" +
            "2. Les points forts du candidat pour ce poste.\n" +
            "3. Les compétences manquantes ou à améliorer.\n" +
            "4. Une recommandation finale (Accepter, Entretien, ou Refuser).";

        try {
            String jsonResponse = callGeminiApi(prompt);
            return parseGeminiResponse(jsonResponse);
        } catch (Exception e) {
            return "Erreur lors de l'analyse IA: " + e.getMessage();
        }
    }

    public String generateOffreDescription(String titre, String domaine) {
        if (!isApiKeyConfigured()) return "Clé API non configurée.";
        
        String prompt = "Rédige une description de stage professionnelle pour le poste de '" + titre + 
                        "' dans le domaine '" + domaine + "'. " +
                        "Inclus les missions, le profil recherché et les technos clés. Sois concis et pro.";
        try {
            String jsonResponse = callGeminiApi(prompt);
            return parseGeminiResponse(jsonResponse);
        } catch (Exception e) {
            return "Erreur IA: " + e.getMessage();
        }
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

        // Nettoyage et échappement plus robuste du prompt pour le JSON
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

    private String parseGeminiResponse(String jsonResponse) {
        try {
            // Recherche du champ "text" de manière plus robuste
            String token = "\"text\": \"";
            int start = jsonResponse.indexOf(token);
            if (start == -1) return "Erreur: Contenu non trouvé dans la réponse.";
            start += token.length();
            
            StringBuilder result = new StringBuilder();
            for (int i = start; i < jsonResponse.length(); i++) {
                char c = jsonResponse.charAt(i);
                // On s'arrête au prochain guillemet non échappé
                if (c == '\"' && jsonResponse.charAt(i-1) != '\\') break;
                result.append(c);
            }
            
            return result.toString()
                         .replace("\\n", "\n")
                         .replace("\\\"", "\"")
                         .replace("\\\\", "\\");
        } catch (Exception e) {
            return "Erreur lors de la lecture de la réponse IA.";
        }
    }

    private String generateSuggestions(CvData data) {
        if (data.getExperiences().isEmpty()) return "- Ajoutez des expériences pour renforcer votre profil.";
        return "Votre profil semble solide.";
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
