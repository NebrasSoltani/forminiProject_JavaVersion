package tn.formini.utils;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;

/**
 * Classe de test pour diagnostiquer les problèmes Stripe
 */
public class StripeTest {

    public static void main(String[] args) {
        System.out.println("🔍 Début du test Stripe...");
        
        // Test 1: Vérifier la version de Stripe
        System.out.println("📦 Version Stripe: " + Stripe.VERSION);
        
        // Test 2: Test avec votre clé
        String stripeKey = "sk_test_51TUXHqCTsTDYpm1n1ScuYXQjLFIC91lAxHW6iPJoLv7TIvNDd5JRKFLRrMxKfxHeiPFcpROzR7TZp9tSa41vp0PY00WN6AMrT2";
        
        try {
            System.out.println("🔑 Configuration de la clé...");
            Stripe.apiKey = stripeKey;
            
            System.out.println("🌐 Test de connexion à Stripe...");
            Account account = Account.retrieve();
            
            System.out.println("✅ SUCCÈS - Connexion établie !");
            System.out.println("📧 Email: " + account.getEmail());
            System.out.println("🌍 Pays: " + account.getCountry());
            System.out.println("🏢 ID Compte: " + account.getId());
            System.out.println("💼 Type: " + account.getType());
            
        } catch (StripeException e) {
            System.err.println("❌ ERREUR STRIPE:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Code: " + e.getStripeError().getCode());
            System.err.println("   Type: " + e.getStripeError().getType());
            System.err.println("   HTTP Status: " + e.getStatusCode());
            
            // Analyse de l'erreur
            analyzeError(e);
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR INATTENDUE:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Type: " + e.getClass().getName());
            e.printStackTrace();
        }
        
        System.out.println("🏁 Fin du test Stripe");
    }
    
    private static void analyzeError(StripeException e) {
        String code = e.getStripeError().getCode();
        
        System.out.println("\n🔍 Analyse de l'erreur:");
        
        if ("api_key_invalid".equals(code)) {
            System.out.println("❌ Clé API invalide");
            System.out.println("💡 Solution: Vérifiez que la clé est correcte et non révoquée");
            
        } else if ("api_key_expired".equals(code)) {
            System.out.println("❌ Clé API expirée");
            System.out.println("💡 Solution: Générez une nouvelle clé dans le dashboard Stripe");
            
        } else if ("rate_limit".equals(code)) {
            System.out.println("❌ Limite de dépassée");
            System.out.println("💡 Solution: Attendez quelques minutes avant de réessayer");
            
        } else if ("invalid_request_error".equals(code)) {
            System.out.println("❌ Requête invalide");
            System.out.println("💡 Solution: Vérifiez les paramètres de la requête");
            
        } else if ("authentication_error".equals(code)) {
            System.out.println("❌ Erreur d'authentification");
            System.out.println("💡 Solution: Vérifiez la clé et les permissions");
            
        } else {
            System.out.println("❌ Erreur inconnue: " + code);
            System.out.println("💡 Solution: Contactez le support Stripe");
        }
        
        // Test réseau
        testNetworkConnectivity();
    }
    
    private static void testNetworkConnectivity() {
        System.out.println("\n🌐 Test de connectivité réseau...");
        
        try {
            java.net.URL url = new java.net.URL("https://api.stripe.com/v1/account");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            System.out.println("📡 Code réponse HTTP: " + responseCode);
            
            if (responseCode == 200) {
                System.out.println("✅ Connexion réseau OK");
            } else if (responseCode == 401) {
                System.out.println("❌ Erreur d'authentification (clé invalide)");
            } else if (responseCode == 403) {
                System.out.println("❌ Accès interdit");
            } else {
                System.out.println("⚠️ Réponse inattendue: " + responseCode);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur réseau: " + e.getMessage());
            System.out.println("💡 Solution: Vérifiez votre connexion internet ou proxy");
        }
    }
}
