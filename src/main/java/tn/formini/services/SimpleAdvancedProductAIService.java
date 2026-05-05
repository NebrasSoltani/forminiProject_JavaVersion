package tn.formini.services;

import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Simple implementation of Advanced Product AI Service
 * Provides basic product suggestions without external AI dependencies
 */
public class SimpleAdvancedProductAIService {

    private final ProduitService produitService;

    public SimpleAdvancedProductAIService() {
        this.produitService = new ProduitService();
    }

    /**
     * Get AI-powered search suggestions for products (simplified version)
     * @param searchTerm The user's search term
     * @param userContext Additional context about available categories
     * @return CompletableFuture containing list of suggested product names
     */
    public CompletableFuture<List<String>> getSearchSuggestions(String searchTerm, String userContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Produit> allProducts = produitService.afficher();
                
                // Simple text-based search
                List<String> suggestions = allProducts.stream()
                    .filter(p -> p.getNom() != null && p.getCategorie() != null)
                    .filter(p -> p.getStock() > 0)
                    .filter(p -> matchesSearchTerm(p, searchTerm))
                    .map(Produit::getNom)
                    .limit(5)
                    .collect(Collectors.toList());
                
                // If no matches, return some popular products
                if (suggestions.isEmpty()) {
                    suggestions = allProducts.stream()
                        .filter(p -> p.getStock() > 0)
                        .map(Produit::getNom)
                        .limit(3)
                        .collect(Collectors.toList());
                }
                
                return suggestions;
                
            } catch (Exception e) {
                System.err.println("Error getting search suggestions: " + e.getMessage());
                return List.of("Produit 1", "Produit 2", "Produit 3");
            }
        });
    }

    /**
     * Get AI-powered cart-based product suggestions (simplified version)
     * @param cartProducts List of products currently in the cart
     * @return CompletableFuture containing list of suggested product names
     */
    public CompletableFuture<List<String>> getCartBasedSuggestions(List<Produit> cartProducts) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Produit> allProducts = produitService.afficher();
                
                // Get categories from cart
                List<String> cartCategories = cartProducts.stream()
                    .map(Produit::getCategorie)
                    .filter(cat -> cat != null && !cat.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
                
                // Find products from same categories
                List<String> suggestions = allProducts.stream()
                    .filter(p -> p.getStock() > 0)
                    .filter(p -> !isInCart(p, cartProducts))
                    .filter(p -> cartCategories.contains(p.getCategorie()))
                    .map(Produit::getNom)
                    .limit(5)
                    .collect(Collectors.toList());
                
                // If no suggestions, get some popular products
                if (suggestions.isEmpty()) {
                    suggestions = allProducts.stream()
                        .filter(p -> p.getStock() > 0)
                        .filter(p -> !isInCart(p, cartProducts))
                        .map(Produit::getNom)
                        .limit(3)
                        .collect(Collectors.toList());
                }
                
                return suggestions;
                
            } catch (Exception e) {
                System.err.println("Error getting cart-based suggestions: " + e.getMessage());
                return List.of("Produit complémentaire 1", "Produit complémentaire 2", "Produit complémentaire 3");
            }
        });
    }

    /**
     * Check if a product matches the search term
     * @param product Product to check
     * @param searchTerm Search term
     * @return true if product matches
     */
    private boolean matchesSearchTerm(Produit product, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }
        
        String term = searchTerm.toLowerCase().trim();
        String name = product.getNom().toLowerCase();
        String category = product.getCategorie().toLowerCase();
        
        return name.contains(term) || category.contains(term);
    }

    /**
     * Check if a product is already in the cart
     * @param product Product to check
     * @param cartProducts List of products in cart
     * @return true if product is in cart
     */
    private boolean isInCart(Produit product, List<Produit> cartProducts) {
        return cartProducts.stream()
            .anyMatch(cartProduct -> cartProduct.getId() == product.getId());
    }
}
