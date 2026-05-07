package tn.formini.services;

import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class SimpleCartAIService {
    private ProduitService produitService;
    
    public SimpleCartAIService() {
        this.produitService = new ProduitService();
    }
    
    public List<Produit> getAllProducts() throws SQLException {
        try {
            return produitService.afficher();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des produits: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Produit> getProduitsComplementaires() {
        try {
            List<Produit> allProducts = getAllProducts();
            if (allProducts.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Retourner 4 produits aléatoires
            Collections.shuffle(allProducts);
            List<Produit> result = new ArrayList<>();
            for (int i = 0; i < Math.min(4, allProducts.size()); i++) {
                result.add(allProducts.get(i));
            }
            return result;
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des produits complémentaires: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Produit> getProduitsSimilairesParCategorie(List<Produit> cartProducts) {
        try {
            // Extraire les catégories des produits dans le panier
            Set<String> cartCategories = new HashSet<>();
            for (Produit p : cartProducts) {
                if (p.getCategorie() != null && !p.getCategorie().trim().isEmpty()) {
                    cartCategories.add(p.getCategorie().trim());
                }
            }
            
            if (cartCategories.isEmpty()) {
                return getProduitsComplementaires(); // Fallback vers produits aléatoires
            }
            
            // Récupérer tous les produits
            List<Produit> allProducts = getAllProducts();
            if (allProducts.isEmpty()) {
                return new ArrayList<>();
            }
            
            System.out.println("=== ANALYSE DES CATÉGORIES DU PANIER ===");
            for (String category : cartCategories) {
                System.out.println("Catégorie dans panier: " + category);
            }
            
            // Filtrer les produits par catégories exactes du panier
            List<Produit> suggestions = new ArrayList<>();
            Map<String, List<Produit>> suggestionsByCategory = new HashMap<>();
            
            for (Produit product : allProducts) {
                // Exclure les produits déjà dans le panier
                boolean alreadyInCart = false;
                for (Produit cartItem : cartProducts) {
                    if (cartItem.getId() == product.getId()) {
                        alreadyInCart = true;
                        break;
                    }
                }
                
                if (!alreadyInCart && product.getCategorie() != null && !product.getCategorie().trim().isEmpty()) {
                    String productCategory = product.getCategorie().trim();
                    
                    // Vérifier si la catégorie du produit correspond exactement à une catégorie du panier
                    for (String cartCategory : cartCategories) {
                        if (productCategory.equalsIgnoreCase(cartCategory)) {
                            suggestions.add(product);
                            
                            // Organiser par catégorie pour équilibrage
                            suggestionsByCategory.computeIfAbsent(cartCategory, k -> new ArrayList<>()).add(product);
                            break;
                        }
                    }
                }
            }
            
            System.out.println("=== SUGGESTIONS TROUVÉES PAR CATÉGORIE ===");
            for (Map.Entry<String, List<Produit>> entry : suggestionsByCategory.entrySet()) {
                System.out.println("Catégorie '" + entry.getKey() + "': " + entry.getValue().size() + " produits");
            }
            
            // Équilibrer les suggestions pour inclure toutes les catégories
            List<Produit> balancedSuggestions = new ArrayList<>();
            
            // Si on a plusieurs catégories, essayer d'inclure des produits de chaque catégorie
            if (cartCategories.size() > 1) {
                // Calculer combien de produits par catégorie (répartis équitablement)
                int productsPerCategory = Math.max(1, 4 / cartCategories.size());
                
                for (String category : cartCategories) {
                    List<Produit> categoryProducts = suggestionsByCategory.get(category);
                    if (categoryProducts != null && !categoryProducts.isEmpty()) {
                        // Mélanger et prendre le nombre requis
                        Collections.shuffle(categoryProducts);
                        int toTake = Math.min(productsPerCategory, categoryProducts.size());
                        for (int i = 0; i < toTake; i++) {
                            balancedSuggestions.add(categoryProducts.get(i));
                        }
                    }
                }
                
                // Si on n'a pas encore 4 produits, ajouter plus aléatoirement
                if (balancedSuggestions.size() < 4) {
                    List<Produit> remainingSuggestions = new ArrayList<>(suggestions);
                    // Remplacer removeIf par une boucle for
                    List<Produit> toRemove = new ArrayList<>();
                    for (Produit p : remainingSuggestions) {
                        if (balancedSuggestions.contains(p)) {
                            toRemove.add(p);
                        }
                    }
                    remainingSuggestions.removeAll(toRemove);
                    Collections.shuffle(remainingSuggestions);
                    int needed = 4 - balancedSuggestions.size();
                    for (int i = 0; i < Math.min(needed, remainingSuggestions.size()); i++) {
                        balancedSuggestions.add(remainingSuggestions.get(i));
                    }
                }
            } else {
                // Si une seule catégorie, prendre simplement les 4 premiers
                balancedSuggestions = new ArrayList<>();
                for (int i = 0; i < Math.min(4, suggestions.size()); i++) {
                    balancedSuggestions.add(suggestions.get(i));
                }
            }
            
            // Limiter à 4 suggestions maximum
            if (balancedSuggestions.size() > 4) {
                Collections.shuffle(balancedSuggestions);
                List<Produit> temp = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    temp.add(balancedSuggestions.get(i));
                }
                balancedSuggestions = temp;
            }
            
            // Si pas assez de suggestions, ajouter des produits aléatoires
            if (balancedSuggestions.size() < 4) {
                List<Produit> allAvailableProducts = getAllProducts();
                List<Produit> randomProducts = new ArrayList<>();
                
                // Créer une liste de produits non déjà dans le panier ou suggestions
                for (Produit p : allAvailableProducts) {
                    boolean alreadyAdded = false;
                    for (Produit cartItem : cartProducts) {
                        if (cartItem.getId() == p.getId()) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    for (Produit suggestion : balancedSuggestions) {
                        if (suggestion.getId() == p.getId()) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded) {
                        randomProducts.add(p);
                    }
                }
                
                Collections.shuffle(randomProducts);
                int needed = 4 - balancedSuggestions.size();
                for (int i = 0; i < Math.min(needed, randomProducts.size()); i++) {
                    balancedSuggestions.add(randomProducts.get(i));
                }
            }
            
            return balancedSuggestions;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des produits similaires: " + e.getMessage());
            return getProduitsComplementaires(); // Fallback
        }
    }
}
