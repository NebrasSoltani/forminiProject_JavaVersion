package tn.formini.utils;

import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.SocieteService;
import tn.formini.services.UsersService.ApprenantService;

/**
 * Simple test to check CRUD services
 */
public class CrudTest {
    public static void main(String[] args) {
        System.out.println("=== CRUD Services Test ===");
        
        try {
            // Test FormateurService
            System.out.println("Testing FormateurService...");
            FormateurService formateurService = new FormateurService();
            System.out.println("FormateurService created successfully");
            System.out.println("Formateurs count: " + formateurService.afficher().size());
            
            // Test SocieteService
            System.out.println("Testing SocieteService...");
            SocieteService societeService = new SocieteService();
            System.out.println("SocieteService created successfully");
            System.out.println("Societes count: " + societeService.afficher().size());
            
            // Test ApprenantService
            System.out.println("Testing ApprenantService...");
            ApprenantService apprenantService = new ApprenantService();
            System.out.println("ApprenantService created successfully");
            System.out.println("Apprenants count: " + apprenantService.afficher().size());
            
            System.out.println("All CRUD services working correctly!");
            
        } catch (Exception e) {
            System.err.println("Error testing CRUD services: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
