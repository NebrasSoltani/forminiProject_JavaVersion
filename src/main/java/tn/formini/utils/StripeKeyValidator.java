package tn.formini.utils;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import java.util.Scanner;

/**
 * Utilitaire pour valider et mettre à jour la clé Stripe
 */
public class StripeKeyValidator {

    public static void main(String[] args) {
        System.out.println("🔍 Validateur de clé Stripe");
        System.out.println("==========================\n");
        
        Scanner scanner = new Scanner(System.in);
        
        // Test avec la clé actuelle
        System.out.println("1. Test avec la clé actuelle...");
        testStripeKey("sk_test_51TUXHqCTsTDYpm1n1ScuYXQjLFIC91lAxHW6iPJoLv7TIvNDd5JRKFLRrMxKfxHeiPFcpROzR7TZp9tSa41vp0PY00WN6AMrT2");
        
        // Demander une nouvelle clé
        System.out.println("\n2. Entrez votre nouvelle clé secrète Stripe:");
        System.out.print("Clé (sk_test_...): ");
        String newKey = scanner.nextLine().trim();
        
        if (newKey.startsWith("sk_test_") || newKey.startsWith("sk_live_")) {
            System.out.println("\n🔑 Test de la nouvelle clé...");
            if (testStripeKey(newKey)) {
                System.out.println("✅ La clé fonctionne !");
                System.out.println("\n📝 Instructions pour mettre à jour:");
                System.out.println("1. Ouvrez: src/main/resources/application.properties");
                System.out.println("2. Remplacez la ligne:");
                System.out.println("   stripe.secret.key=sk_test_VOTRE_NOUVELLE_CLÉ_ICI");
                System.out.println("3. Par:");
                System.out.println("   stripe.secret.key=" + newKey);
                System.out.println("4. Relancez l'application");
            } else {
                System.out.println("❌ La clé ne fonctionne pas !");
            }
        } else {
            System.out.println("❌ Format de clé invalide !");
            System.out.println("💡 La clé doit commencer par 'sk_test_' ou 'sk_live_'");
        }
        
        scanner.close();
    }
    
    public static boolean testStripeKey(String stripeKey) {
        try {
            System.out.println("🔑 Configuration de la clé...");
            System.out.println("   Clé: " + stripeKey.substring(0, Math.min(20, stripeKey.length())) + "...");
            
            Stripe.apiKey = stripeKey;
            
            System.out.println("🌐 Test de connexion...");
            Account account = Account.retrieve();
            
            System.out.println("✅ SUCCÈS - Connexion établie !");
            System.out.println("📧 Email: " + (account.getEmail() != null ? account.getEmail() : "Non disponible"));
            System.out.println("🌍 Pays: " + account.getCountry());
            System.out.println("🏢 ID Compte: " + account.getId());
            System.out.println("💼 Type: " + account.getType());
            
            return true;
            
        } catch (StripeException e) {
            System.err.println("❌ ERREUR:");
            System.err.println("   Message: " + e.getMessage());
            
            if (e.getStripeError() != null) {
                System.err.println("   Code: " + e.getStripeError().getCode());
                System.err.println("   Type: " + e.getStripeError().getType());
                
                // Analyse spécifique
                switch (e.getStripeError().getCode()) {
                    case "api_key_invalid":
                        System.err.println("💡 Solution: La clé est invalide ou révoquée");
                        break;
                    case "api_key_expired":
                        System.err.println("💡 Solution: La clé a expiré, générez-en une nouvelle");
                        break;
                    case "rate_limit":
                        System.err.println("💡 Solution: Attendez 1 minute avant de réessayer");
                        break;
                    default:
                        System.err.println("💡 Solution: Vérifiez la clé et votre connexion");
                        break;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR SYSTÈME:");
            System.err.println("   Message: " + e.getMessage());
            return false;
        }
    }
}
