package tn.formini.services;

import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI-powered cart recommendation service
 * Provides complementary product suggestions based on cart contents
 */
public class CartAIService {

    private static CartAIService instance;
    private final ProduitService produitService;

    private CartAIService() {
        this.produitService = new ProduitService();
    }

    public static synchronized CartAIService getInstance() {
        if (instance == null) {
            instance = new CartAIService();
        }
        return instance;
    }

    /**
     * Get complementary products based on cart contents
     * @return List of products that complement items in the cart
     */
    public List<Produit> getProduitsComplementaires() {
        try {
            List<Produit> allProducts = produitService.afficher();
            
            // Simple algorithm: return products from different categories
            // In a real implementation, this would use AI/ML for recommendations
            List<Produit> suggestions = new ArrayList<>();
            
            // Get unique categories from existing products
            List<String> categories = allProducts.stream()
                .map(Produit::getCategorie)
                .filter(cat -> cat != null && !cat.isEmpty())
                .distinct()
                .collect(Collectors.toList());
            
            // Add 1-2 products from each category
            for (String category : categories) {
                List<Produit> categoryProducts = allProducts.stream()
                    .filter(p -> category.equals(p.getCategorie()))
                    .filter(p -> p.getStock() > 0)
                    .limit(2)
                    .collect(Collectors.toList());
                suggestions.addAll(categoryProducts);
            }
            
            // Limit to 8 suggestions
            return suggestions.stream().limit(8).collect(Collectors.toList());
            
        } catch (Exception e) {
            System.err.println("Error getting complementary products: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all products for search/matching
     * @return List of all products in database
     */
    public List<Produit> getAllProducts() {
        try {
            return produitService.afficher();
        } catch (Exception e) {
            System.err.println("Error getting all products: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
