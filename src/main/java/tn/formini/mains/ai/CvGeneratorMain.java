package tn.formini.mains.ai;

import tn.formini.entities.ai.CvData;
import tn.formini.entities.ai.CvGenerationRequest;
import tn.formini.entities.ai.CvGenerationResponse;
import tn.formini.services.ai.CvGenerationService;

import java.util.Arrays;

public class CvGeneratorMain {
    public static void main(String[] args) {
        // Remplacer par votre clé API Gemini réelle
        String apiKey = "VOTRE_CLE_API_GEMINI";
        CvGenerationService service = new CvGenerationService(apiKey);

        CvData cv = new CvData();
        cv.setNom("Ben Ali");
        cv.setPrenom("Mohamed");
        cv.setEmail("mohamed.benali@email.com");
        cv.setTelephone("+216 55 123 456");
        cv.setProfession("Développeur Java Full Stack");
        cv.setCompetences(Arrays.asList("Java 17", "Spring Boot", "Angular", "MySQL"));

        CvData.ExperienceProfessionnelle exp = new CvData.ExperienceProfessionnelle();
        exp.setTitre("Stagiaire Développeur");
        exp.setEntreprise("Formini");
        exp.setDateDebut("Janvier 2024");
        exp.setDescription("Développement d'une plateforme de formation.");
        cv.getExperiences().add(exp);

        CvGenerationRequest request = new CvGenerationRequest(cv);
        request.setTargetJob("Développeur Junior");

        System.out.println("Génération du CV en cours...");
        CvGenerationResponse response = service.generateCv(request);

        if (response.isSuccess()) {
            System.out.println("CV GÉNÉRÉ :\n");
            System.out.println(response.getCvContent());
            System.out.println("\nSuggestions :");
            System.out.println(response.getSuggestions());
        } else {
            System.out.println("Erreur : " + response.getErrorMessage());
        }
    }
}
