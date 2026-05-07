package tn.formini.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import tn.formini.services.cart.CartService;
import tn.formini.services.cart.CartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StripePaymentService {
    private static StripePaymentService instance;
    
    private StripePaymentService() {
        initializeStripe();
    }
    
    public static StripePaymentService getInstance() {
        if (instance == null) {
            instance = new StripePaymentService();
        }
        return instance;
    }
    
    private void initializeStripe() {
        try {
            String stripeSecretKey = null;
            
            // 1. Essayer les variables d'environnement d'abord
            stripeSecretKey = System.getenv("STRIPE_SECRET_KEY");
            if (stripeSecretKey != null && !stripeSecretKey.trim().isEmpty()) {
                Stripe.apiKey = stripeSecretKey;
                System.out.println("Stripe API initialized from environment variable");
                return;
            }
            
            // 2. Essayer le fichier local config.properties.local (chemin absolu)
            try {
                String projectPath = System.getProperty("user.dir");
                java.util.Properties props = new java.util.Properties();
                props.load(new java.io.FileInputStream(projectPath + "/config.properties.local"));
                stripeSecretKey = props.getProperty("stripe.secret.key");
                if (stripeSecretKey != null && !stripeSecretKey.trim().isEmpty()) {
                    Stripe.apiKey = stripeSecretKey;
                    System.out.println("Stripe API initialized from config.properties.local");
                    return;
                }
            } catch (Exception e) {
                System.out.println("config.properties.local not found, trying main config...");
            }
            
            // 3. Essayer le fichier principal config.properties
            try {
                java.util.Properties props = new java.util.Properties();
                props.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
                stripeSecretKey = props.getProperty("stripe.secret.key");
                
                // Remplacer les variables d'environnement si nécessaire
                if (stripeSecretKey != null && stripeSecretKey.startsWith("${") && stripeSecretKey.endsWith("}")) {
                    String envVar = stripeSecretKey.substring(2, stripeSecretKey.length() - 1);
                    stripeSecretKey = System.getenv(envVar);
                }
                
                if (stripeSecretKey != null && !stripeSecretKey.trim().isEmpty()) {
                    Stripe.apiKey = stripeSecretKey;
                    System.out.println("Stripe API initialized from config.properties");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error loading config.properties: " + e.getMessage());
            }
            
            // 4. Si aucune clé trouvée, afficher un message d'erreur clair
            System.err.println("=== STRIPE CONFIGURATION ERROR ===");
            System.err.println("Stripe API key not found in any source");
            System.err.println("");
            System.err.println("To configure Stripe, choose one option:");
            System.err.println("1. Set environment variable: STRIPE_SECRET_KEY");
            System.err.println("2. Create config.properties.local with stripe.secret.key");
            System.err.println("3. Run setup script: ./setup-stripe.bat (Windows) or ./setup-stripe.sh (Linux/Mac)");
            System.err.println("");
            System.err.println("For testing, you can get a test key from: https://dashboard.stripe.com/apikeys");
            System.err.println("=====================================");
            
        } catch (Exception e) {
            System.err.println("Error initializing Stripe: " + e.getMessage());
        }
    }
    
    public String createCheckoutSession(BigDecimal amount, String successUrl, String cancelUrl) throws StripeException {
        // Convert amount to cents (Stripe works with cents)
        long amountInCents = amount.multiply(new BigDecimal("100")).longValue();
        
        // Create line items from cart
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        CartService cart = CartService.getInstance();
        
        for (CartItem item : cart.getItems()) {
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity((long) item.getQuantity())
                .setPriceData(
                    SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd") // US Dollar (Stripe test mode)
                        .setUnitAmount((long) (item.getProduit().getPrix().multiply(new BigDecimal("100")).longValue()))
                        .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(item.getProduit().getNom())
                                .setDescription(item.getProduit().getDescription() != null ? item.getProduit().getDescription() : "")
                                .build()
                        )
                        .build()
                )
                .build();
            lineItems.add(lineItem);
        }
        
        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .addAllLineItem(lineItems)
            .setSuccessUrl(successUrl)
            .setCancelUrl(cancelUrl)
            .build();
        
        Session session = Session.create(params);
        return session.getUrl();
    }
    
    public boolean isConfigured() {
        try {
            return Stripe.apiKey != null && !Stripe.apiKey.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getConfigurationStatus() {
        if (isConfigured()) {
            return "Stripe API configured (" + Stripe.apiKey.substring(0, Math.min(10, Stripe.apiKey.length())) + "...)";
        } else {
            return "Stripe API not configured - see console for instructions";
        }
    }
    
    public void testConfiguration() {
        System.out.println("=== STRIPE CONFIGURATION TEST ===");
        System.out.println("Environment variable STRIPE_SECRET_KEY: " + 
            (System.getenv("STRIPE_SECRET_KEY") != null ? "Found" : "Not found"));
        
        try {
            String projectPath = System.getProperty("user.dir");
            java.util.Properties props = new java.util.Properties();
            props.load(new java.io.FileInputStream(projectPath + "/config.properties.local"));
            String localKey = props.getProperty("stripe.secret.key");
            System.out.println("config.properties.local stripe.secret.key: " + 
                (localKey != null ? "Found (" + localKey.substring(0, Math.min(10, localKey.length())) + "...)" : "Not found"));
        } catch (Exception e) {
            System.out.println("config.properties.local: Not accessible");
        }
        
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            String mainKey = props.getProperty("stripe.secret.key");
            System.out.println("config.properties stripe.secret.key: " + 
                (mainKey != null ? "Found (" + mainKey + ")" : "Not found"));
        } catch (Exception e) {
            System.out.println("config.properties: Not accessible");
        }
        
        System.out.println("Final Stripe.apiKey: " + 
            (Stripe.apiKey != null ? "Set (" + Stripe.apiKey.substring(0, Math.min(10, Stripe.apiKey.length())) + "...)" : "Not set"));
        System.out.println("=== END TEST ===");
    }
}
