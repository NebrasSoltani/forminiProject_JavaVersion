package tn.formini.services;

import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;
import tn.formini.services.OpenAIService;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Service IA pour filtrer les vrais produits de la boutique selon les préférences du client
 */
public class RealProductAIService {
    private static RealProductAIService instance;
    private ProduitService produitService;
    private OpenAIService openAIService;
    
    private RealProductAIService() {
        this.produitService = new ProduitService();
        this.openAIService = OpenAIService.getInstance();
    }
    
    public static synchronized RealProductAIService getInstance() {
        if (instance == null) {
            instance = new RealProductAIService();
        }
        return instance;
    }
    
    /**
     * Recherche des produits réels selon les préférences du client
     */
    public List<Produit> searchProducts(String preferences, String category, Double minPrice, Double maxPrice) {
        try {
            // 1. Récupérer tous les produits de la boutique
            List<Produit> allProducts = produitService.afficher();
            System.out.println("📦 " + allProducts.size() + " produits trouvés dans la boutique");
            
            // 2. Filtrer par catégorie si spécifiée
            List<Produit> filteredProducts = allProducts;
            if (category != null && !category.equals("Toutes les catégories")) {
                filteredProducts = allProducts.stream()
                    .filter(p -> p.getCategorie() != null && 
                                 p.getCategorie().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
                System.out.println("🏷️ " + filteredProducts.size() + " produits dans la catégorie: " + category);
            }
            
            // 3. Filtrer par prix
            if (minPrice != null && minPrice > 0) {
                filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getPrix() != null && p.getPrix().doubleValue() >= minPrice)
                    .collect(Collectors.toList());
                System.out.println("💰 " + filteredProducts.size() + " produits avec prix >= " + minPrice + " DT");
            }
            
            if (maxPrice != null && maxPrice > 0) {
                filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getPrix() != null && p.getPrix().doubleValue() <= maxPrice)
                    .collect(Collectors.toList());
                System.out.println("💰 " + filteredProducts.size() + " produits avec prix <= " + maxPrice + " DT");
            }
            
            // 4. Utiliser l'IA pour analyser les préférences et filtrer par pertinence
            if (!preferences.trim().isEmpty()) {
                filteredProducts = filterByAIPreferences(filteredProducts, preferences);
            }
            
            // 5. Limiter à 5 résultats maximum
            List<Produit> results = filteredProducts.stream()
                .limit(5)
                .collect(Collectors.toList());
            
            System.out.println("✅ " + results.size() + " produits finaux recommandés par l'IA");
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche IA: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Filtre les produits selon les préférences textuelles (mots-clés simples)
     */
    private List<Produit> filterByAIPreferences(List<Produit> products, String preferences) {
        // Utiliser directement le filtrage par mots-clés pour garantir des résultats de la base
        return filterByKeywords(products, preferences);
    }
    
    /**
     * Filtrage par mots-clés optimisé pour trouver les produits pertinents
     */
    private List<Produit> filterByKeywords(List<Produit> products, String preferences) {
        String preferencesLower = preferences.toLowerCase();
        String[] keywords = preferencesLower.split("\\s+");
        
        System.out.println("🔍 Mots-clés recherchés: " + java.util.Arrays.toString(keywords));
        
        return products.stream()
            .filter(product -> {
                int score = 0;
                String productName = product.getNom() != null ? product.getNom().toLowerCase() : "";
                String productCategory = product.getCategorie() != null ? product.getCategorie().toLowerCase() : "";
                String productDescription = product.getDescription() != null ? product.getDescription().toLowerCase() : "";
                
                // Calculer un score de pertinence
                for (String keyword : keywords) {
                    if (productName.contains(keyword)) score += 10; // Nom = plus important
                    if (productCategory.contains(keyword)) score += 5;  // Catégorie = important
                    if (productDescription.contains(keyword)) score += 2; // Description = moins important
                }
                
                // Accepter si le score est suffisant
                boolean isRelevant = score >= 5; // Au moins un mot-clé important trouvé
                if (isRelevant) {
                    System.out.println("✅ Produit pertinent: " + product.getNom() + " (score: " + score + ")");
                }
                return isRelevant;
            })
            .sorted((p1, p2) -> {
                // Trier par pertinence (nom > catégorie > description)
                int score1 = calculateRelevanceScore(p1, preferencesLower);
                int score2 = calculateRelevanceScore(p2, preferencesLower);
                return Integer.compare(score2, score1); // Ordre décroissant
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Calcule le score de pertinence d'un produit
     */
    private int calculateRelevanceScore(Produit product, String preferences) {
        String[] keywords = preferences.toLowerCase().split("\\s+");
        int score = 0;
        
        String productName = product.getNom() != null ? product.getNom().toLowerCase() : "";
        String productCategory = product.getCategorie() != null ? product.getCategorie().toLowerCase() : "";
        String productDescription = product.getDescription() != null ? product.getDescription().toLowerCase() : "";
        
        for (String keyword : keywords) {
            if (productName.contains(keyword)) score += 10;
            if (productCategory.contains(keyword)) score += 5;
            if (productDescription.contains(keyword)) score += 2;
        }
        
        return score;
    }
}
