package tn.formini.mains;

import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ProduitCRUDTest {
    public static void main(String[] args) {
        ProduitService produitService = new ProduitService();
        
        System.out.println("=== TEST CRUD PRODUIT ===\n");
        
        // Test 1: Ajouter des produits
        System.out.println("1. Test d'ajout de produits:");
        testAjout(produitService);
        
        // Test 2: Afficher tous les produits
        System.out.println("\n2. Test d'affichage de tous les produits:");
        testAfficher(produitService);
        
        // Test 3: Modifier un produit
        System.out.println("\n3. Test de modification d'un produit:");
        testModifier(produitService);
        
        // Test 4: Rechercher par catégorie
        System.out.println("\n4. Test de recherche par catégorie:");
        testRechercheParCategorie(produitService);
        
        // Test 5: Mettre à jour le stock
        System.out.println("\n5. Test de mise à jour du stock:");
        testMiseAJourStock(produitService);
        
        // Test 6: Supprimer un produit
        System.out.println("\n6. Test de suppression d'un produit:");
        testSupprimer(produitService);
        
        // Affichage final
        System.out.println("\n7. Affichage final après tous les tests:");
        testAfficher(produitService);
        
        System.out.println("\n=== FIN DES TESTS ===");
    }
    
    private static void testAjout(ProduitService service) {
        try {
            // Produit 1
            Produit p1 = new Produit();
            p1.setNom("Laptop Dell XPS 15");
            p1.setCategorie("Informatique");
            p1.setDescription("Laptop haute performance avec écran 4K");
            p1.setPrix(new BigDecimal("1299.99"));
            p1.setStock(15);
            p1.setImage("laptop_dell_xps15.jpg");
            p1.setStatut("disponible");
            p1.setDate_creation(new Date());
            
            service.ajouter(p1);
            
            // Produit 2
            Produit p2 = new Produit();
            p2.setNom("Souris Gaming Logitech");
            p2.setCategorie("Informatique");
            p2.setDescription("Souris gaming avec RGB et capteur haute précision");
            p2.setPrix(new BigDecimal("79.99"));
            p2.setStock(50);
            p2.setImage("souris_logitech_gaming.jpg");
            p2.setStatut("disponible");
            p2.setDate_creation(new Date());
            
            service.ajouter(p2);
            
            // Produit 3
            Produit p3 = new Produit();
            p3.setNom("Clavier Mécanique RGB");
            p3.setCategorie("Informatique");
            p3.setDescription("Clavier mécanique avec rétroéclairage RGB");
            p3.setPrix(new BigDecimal("129.99"));
            p3.setStock(25);
            p3.setImage("clavier_mecanique_rgb.jpg");
            p3.setStatut("disponible");
            p3.setDate_creation(new Date());
            
            service.ajouter(p3);
            
            // Produit 4 (autre catégorie)
            Produit p4 = new Produit();
            p4.setNom("Moniteur 27 pouces 4K");
            p4.setCategorie("Écrans");
            p4.setDescription("Moniteur 4K avec HDR et 144Hz");
            p4.setPrix(new BigDecimal("449.99"));
            p4.setStock(10);
            p4.setImage("moniteur_27_4k.jpg");
            p4.setStatut("disponible");
            p4.setDate_creation(new Date());
            
            service.ajouter(p4);
            
        } catch (Exception e) {
            System.out.println("Erreur lors de l'ajout: " + e.getMessage());
        }
    }
    
    private static void testAfficher(ProduitService service) {
        List<Produit> produits = service.afficher();
        System.out.println("Nombre total de produits: " + produits.size());
        
        for (Produit p : produits) {
            System.out.println("ID: " + p.getId() + 
                             " | Nom: " + p.getNom() + 
                             " | Catégorie: " + p.getCategorie() + 
                             " | Prix: " + p.getPrix() + 
                             " | Stock: " + p.getStock() + 
                             " | Statut: " + p.getStatut());
        }
    }
    
    private static void testModifier(ProduitService service) {
        try {
            // Récupérer le premier produit pour le modifier
            List<Produit> produits = service.afficher();
            if (!produits.isEmpty()) {
                Produit p = produits.get(0);
                System.out.println("Modification du produit: " + p.getNom());
                
                p.setNom(p.getNom() + " (Mis à jour)");
                p.setDescription(p.getDescription() + " - Version améliorée");
                p.setPrix(p.getPrix().add(new BigDecimal("100.00")));
                p.setStatut("disponible");
                
                service.modifier(p);
                System.out.println("Produit modifié avec succès!");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la modification: " + e.getMessage());
        }
    }
    
    private static void testRechercheParCategorie(ProduitService service) {
        List<Produit> produitsInfo = service.getByCategorie("Informatique");
        System.out.println("Produits dans la catégorie 'Informatique': " + produitsInfo.size());
        
        for (Produit p : produitsInfo) {
            System.out.println("  - " + p.getNom() + " | " + p.getPrix());
        }
    }
    
    private static void testMiseAJourStock(ProduitService service) {
        try {
            List<Produit> produits = service.afficher();
            if (!produits.isEmpty()) {
                Produit p = produits.get(1); // Prendre le deuxième produit
                System.out.println("Mise à jour du stock pour: " + p.getNom());
                System.out.println("Stock actuel: " + p.getStock());
                
                int nouveauStock = p.getStock() - 5;
                service.mettreAJourStock(p.getId(), nouveauStock);
                
                // Vérifier la mise à jour
                Produit pUpdated = service.getById(p.getId());
                System.out.println("Nouveau stock: " + pUpdated.getStock());
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la mise à jour du stock: " + e.getMessage());
        }
    }
    
    private static void testSupprimer(ProduitService service) {
        try {
            List<Produit> produits = service.afficher();
            if (produits.size() > 2) {
                Produit p = produits.get(produits.size() - 1); // Supprimer le dernier produit
                System.out.println("Suppression du produit: " + p.getNom() + " (ID: " + p.getId() + ")");
                
                service.supprimer(p.getId());
                System.out.println("Produit supprimé avec succès!");
            } else {
                System.out.println("Pas assez de produits pour supprimer");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }
}
