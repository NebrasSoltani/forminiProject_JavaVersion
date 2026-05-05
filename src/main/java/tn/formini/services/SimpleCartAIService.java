package tn.formini.services;

import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple implementation of Cart AI Service
 * Provides basic product suggestions without external AI dependencies
 */
public class SimpleCartAIService {

    private final ProduitService produitService;

    public SimpleCartAIService() {
        this.produitService = new ProduitService();
    }

    /**
     * Get products similar to cart items by category
     * @param cartProducts List of products currently in cart
     * @return List of similar products
     */
    public List<Produit> getProduitsSimilairesParCategorie(List<Produit> cartProducts) {
        try {
            List<Produit> allProducts = produitService.afficher();
            List<Produit> suggestions = new ArrayList<>();

            // Get categories from cart products
            List<String> cartCategories = cartProducts.stream()
                .map(Produit::getCategorie)
                .filter(cat -> cat != null && !cat.isEmpty())
                .distinct()
                .collect(Collectors.toList());

            // Find products from same categories but not in cart
            for (String category : cartCategories) {
                List<Produit> categoryProducts = allProducts.stream()
                    .filter(p -> category.equals(p.getCategorie()))
                    .filter(p -> p.getStock() > 0)
                    .filter(p -> !isInCart(p, cartProducts))
                    .limit(3)
                    .collect(Collectors.toList());
                suggestions.addAll(categoryProducts);
            }

            return suggestions.stream().limit(8).collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting similar products by category: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get complementary products
     * @return List of complementary products
     */
    public List<Produit> getProduitsComplementaires() {
        try {
            List<Produit> allProducts = produitService.afficher();
            
            // Simple algorithm: return products with good stock
            return allProducts.stream()
                .filter(p -> p.getStock() > 0)
                .limit(6)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("Error getting complementary products: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all products for search/matching
     * @return List of all products
     */
    public List<Produit> getAllProducts() {
        try {
            return produitService.afficher();
        } catch (Exception e) {
            System.err.println("Error getting all products: " + e.getMessage());
            return new ArrayList<>();
        }
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
