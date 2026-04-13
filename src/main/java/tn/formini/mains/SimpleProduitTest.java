package tn.formini.mains;

import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class SimpleProduitTest {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Product Form Validation ===");
        
        // Test validation without UI
        testProductValidation();
        
        // Test database operations
        testDatabaseOperations();
    }
    
    private static void testProductValidation() {
        try {
            // Test valid product
            Produit validProduct = new Produit();
            validProduct.setNom("Laptop Dell XPS");
            validProduct.setCategorie("Informatique");
            validProduct.setDescription("Laptop haute performance");
            validProduct.setPrix(new BigDecimal("1299.99"));
            validProduct.setStock(15);
            validProduct.setStatut("disponible");
            validProduct.setDate_creation(new Date());
            
            validProduct.valider();
            System.out.println("✅ Valid product passed validation");
            
            // Test invalid product (empty name)
            Produit invalidProduct = new Produit();
            invalidProduct.setNom("");
            invalidProduct.setCategorie("Informatique");
            invalidProduct.setDescription("Test");
            invalidProduct.setPrix(new BigDecimal("100"));
            invalidProduct.setStock(5);
            invalidProduct.setStatut("disponible");
            invalidProduct.setDate_creation(new Date());
            
            try {
                invalidProduct.valider();
                System.out.println("❌ Invalid product should have failed validation");
            } catch (IllegalArgumentException e) {
                System.out.println("✅ Validation correctly caught error: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in validation test: " + e.getMessage());
        }
    }
    
    private static void testDatabaseOperations() {
        try {
            ProduitService service = new ProduitService();
            
            // Create test product
            Produit testProduct = new Produit();
            testProduct.setNom("Test Product");
            testProduct.setCategorie("Test Category");
            testProduct.setDescription("Test Description for validation");
            testProduct.setPrix(new BigDecimal("99.99"));
            testProduct.setStock(10);
            testProduct.setStatut("disponible");
            testProduct.setDate_creation(new Date());
            
            // Test add
            service.ajouter(testProduct);
            System.out.println("✅ Product added to database");
            
            // Test list
            List<Produit> products = service.afficher();
            System.out.println("✅ Retrieved " + products.size() + " products from database");
            
            // Display products
            for (Produit p : products) {
                System.out.println("   - " + p.getNom() + " | " + p.getPrix() + "€ | Stock: " + p.getStock());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in database test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
