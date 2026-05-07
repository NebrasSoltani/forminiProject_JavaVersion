package tn.formini.utils;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

/**
 * Test rapide Stripe avec votre clé
 */
public class QuickStripeTest {

    public static void main(String[] args) {
        System.out.println("🚀 Test rapide Stripe...");
        
        // METTEZ VOTRE CLÉ ICI
        String stripeKey = "sk_test_51Syx8bGm24wH1K4NlMt4ot1bTPXUC0mEJlsvroYIM32vB5FuD0Vyqk3gY693nTe3HP2BJNjTHwlOiIEau1TcSw2Y00rUE0hRah";
        
        try {
            System.out.println("🔑 Configuration...");
            Stripe.apiKey = stripeKey;
            
            System.out.println("🌐 Test de connexion...");
            Account account = Account.retrieve();
            
            System.out.println("✅ CONNEXION RÉUSSIE !");
            System.out.println("📧 Email: " + account.getEmail());
            System.out.println("🌍 Pays: " + account.getCountry());
            System.out.println("🏢 ID: " + account.getId());
            
            System.out.println("\n💳 Test de paiement...");
            PaymentIntent intent = PaymentIntent.create(
                new PaymentIntentCreateParams.Builder()
                    .setAmount(2000L) // 20.00
                    .setCurrency("eur")
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
                    )
                    .build()
            );
            
            System.out.println("✅ PAIEMENT CRÉÉ !");
            System.out.println("🆔 ID: " + intent.getId());
            System.out.println("💰 Montant: " + (intent.getAmount() / 100.0) + " EUR");
            System.out.println("📊 Statut: " + intent.getStatus());
            
            System.out.println("\n🎉 STRIPE FONCTIONNE PARFAITEMENT !");
            
        } catch (StripeException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            if (e.getStripeError() != null) {
                System.err.println("Code: " + e.getStripeError().getCode());
            }
        }
    }
}
