package tn.formini.mains;

import tn.formini.utils.AdminInitializer;
import tn.formini.entities.Users.User;

/**
 * Launcher for testing admin initialization
 */
public class AdminInitLauncher {
    public static void main(String[] args) {
        System.out.println("=== Admin Initialization Test ===");
        
        // Initialize admin user
        AdminInitializer.initializeAdmin();
        
        // Test if admin exists
        User admin = AdminInitializer.getAdminUser();
        if (admin != null) {
            System.out.println("Admin user found:");
            System.out.println("  ID: " + admin.getId());
            System.out.println("  Email: " + admin.getEmail());
            System.out.println("  Name: " + admin.getPrenom() + " " + admin.getNom());
            System.out.println("  Role: " + admin.getRole_utilisateur());
            System.out.println("  Created: " + admin.getDate_naissance());
        } else {
            System.out.println("Admin user not found!");
        }
        
        System.out.println("\nDefault admin credentials:");
        System.out.println("  Email: admin@formini.com");
        System.out.println("  Password: Admin123!@#");
        System.out.println("\nYou can now login with these credentials.");
    }
}
