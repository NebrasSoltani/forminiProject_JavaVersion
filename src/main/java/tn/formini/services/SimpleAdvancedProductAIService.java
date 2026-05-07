package tn.formini.services;

import tn.formini.entities.produits.Produit;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class SimpleAdvancedProductAIService implements AdvancedProductAIService {
    
    public SimpleAdvancedProductAIService() {
        // Constructeur vide
    }
    
    public List<String> getProductSuggestions(String category, String preferences) {
        List<String> suggestions = new ArrayList<>();
        
        // Suggestions basiques selon la catégorie
        switch (category.toLowerCase()) {
            case "programmation":
                suggestions.add("Formation Java Avancé");
                suggestions.add("Développement Web Full Stack");
                suggestions.add("Python pour Data Science");
                suggestions.add("Algorithmes et Structures de Données");
                suggestions.add("Développement Mobile React Native");
                break;
            case "design":
                suggestions.add("UI/UX Design Fundamentals");
                suggestions.add("Adobe Creative Suite");
                suggestions.add("Design Thinking Workshop");
                suggestions.add("Figma Advanced Techniques");
                suggestions.add("Web Design Principles");
                break;
            case "marketing":
                suggestions.add("Digital Marketing Strategy");
                suggestions.add("SEO Optimization");
                suggestions.add("Social Media Marketing");
                suggestions.add("Content Marketing Mastery");
                suggestions.add("Google Analytics Expert");
                break;
            case "business":
                suggestions.add("Business Intelligence");
                suggestions.add("Project Management PMP");
                suggestions.add("Leadership Excellence");
                suggestions.add("Financial Analysis");
                suggestions.add("Strategic Planning");
                break;
            default:
                suggestions.add("Formation Personnalisée 1");
                suggestions.add("Formation Personnalisée 2");
                suggestions.add("Formation Personnalisée 3");
                suggestions.add("Formation Personnalisée 4");
                suggestions.add("Formation Personnalisée 5");
        }
        
        // Mélanger les suggestions
        Collections.shuffle(suggestions);
        
        // Retourner 3 suggestions
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(3, suggestions.size()); i++) {
            result.add(suggestions.get(i));
        }
        
        return result;
    }
    
    public String getProductRecommendation(String productName) {
        return "Recommandation pour: " + productName + 
               "\n\nCe produit est excellent pour vos besoins. " +
               "Il offre une grande flexibilité et s'adapte parfaitement " +
               "à votre niveau actuel. Nous vous recommandons de commencer " +
               "par les modules de base avant de progresser vers les " +
               "concepts avancés.";
    }
    
    @Override
    public CompletableFuture<List<String>> getSearchSuggestions(String searchTerm, String userContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simuler un délai de traitement IA
                Thread.sleep(500);
                
                List<String> suggestions = new ArrayList<>();
                
                // Générer des suggestions basées sur le terme de recherche
                if (searchTerm.toLowerCase().contains("java")) {
                    suggestions.add("Formation Java Complète");
                    suggestions.add("Développement Java EE");
                    suggestions.add("Spring Framework");
                } else if (searchTerm.toLowerCase().contains("web")) {
                    suggestions.add("Développement Web Full Stack");
                    suggestions.add("HTML/CSS/JavaScript");
                    suggestions.add("React.js Avancé");
                } else if (searchTerm.toLowerCase().contains("design")) {
                    suggestions.add("UI/UX Design");
                    suggestions.add("Figma pour débutants");
                    suggestions.add("Design Thinking");
                } else if (searchTerm.toLowerCase().contains("marketing")) {
                    suggestions.add("Marketing Digital");
                    suggestions.add("SEO & SEM");
                    suggestions.add("Social Media Strategy");
                } else {
                    suggestions.add("Formation " + searchTerm + " - Niveau Débutant");
                    suggestions.add("Formation " + searchTerm + " - Niveau Avancé");
                    suggestions.add("Workshop " + searchTerm);
                }
                
                // Ajouter une suggestion personnalisée basée sur le contexte
                if (userContext != null && userContext.contains("Client")) {
                    suggestions.add("Recommandation personnalisée pour: " + searchTerm);
                }
                
                return suggestions;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ArrayList<>();
            }
        });
    }
}
