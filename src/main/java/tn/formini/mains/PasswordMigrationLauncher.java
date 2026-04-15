package tn.formini.mains;

import tn.formini.utils.PasswordMigration;

/**
 * Launcher for password migration utility
 * Run this to migrate existing plain text passwords to hashed passwords
 */
public class PasswordMigrationLauncher {
    public static void main(String[] args) {
        System.out.println("=== Password Migration Utility ===");
        System.out.println("This utility will migrate existing plain text passwords to secure hashed passwords.");
        System.out.println("Run with 'verify' argument to check migration status instead.");
        System.out.println();
        
        PasswordMigration.main(args);
    }
}
