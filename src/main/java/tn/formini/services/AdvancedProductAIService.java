package tn.formini.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AdvancedProductAIService {
    // Interface pour les services avancés de produits
    // Peut être étendue plus tard avec des méthodes supplémentaires
    
    // Méthode pour obtenir des suggestions de recherche
    CompletableFuture<List<String>> getSearchSuggestions(String searchTerm, String userContext);
    
    // Méthode singleton
    static AdvancedProductAIService getInstance() {
        return new SimpleAdvancedProductAIService();
    }
}
