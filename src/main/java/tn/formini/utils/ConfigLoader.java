package tn.formini.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utilitaire pour charger la configuration depuis différentes sources
 * Priorité: Variables d'environnement > Fichiers de configuration
 */
public class ConfigLoader {
    private static Properties properties = new Properties();
    private static boolean loaded = false;
    
    static {
        loadConfiguration();
    }
    
    private static void loadConfiguration() {
        if (loaded) return;
        
        try {
            // 1. Charger depuis les variables d'environnement
            loadFromEnvironment();
            
            // 2. Charger depuis le fichier local config.properties.local
            loadFromLocalFile();
            
            // 3. Charger depuis le fichier principal config.properties
            loadFromClasspath();
            
            loaded = true;
            System.out.println("✅ Configuration loaded successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading configuration: " + e.getMessage());
        }
    }
    
    private static void loadFromEnvironment() {
        // Charger les clés depuis les variables d'environnement
        String[] keys = {
            "brevo.api.key",
            "stripe.secret.key", 
            "openai.api.key"
        };
        
        for (String key : keys) {
            String envValue = System.getenv(key.toUpperCase().replace(".", "_"));
            if (envValue != null && !envValue.trim().isEmpty()) {
                properties.setProperty(key, envValue.trim());
                System.out.println("🌍 Loaded from environment: " + key);
            }
        }
    }
    
    private static void loadFromLocalFile() {
        try {
            // Essayer config-local.properties à la racine du projet (prioritaire)
            String projectPath = System.getProperty("user.dir");
            String localConfigPath = projectPath + "/config-local.properties";
            
            try (FileInputStream fis = new FileInputStream(localConfigPath)) {
                Properties localProps = new Properties();
                localProps.load(fis);
                
                // Ajouter seulement les clés non déjà définies
                for (String key : localProps.stringPropertyNames()) {
                    if (!properties.containsKey(key)) {
                        properties.setProperty(key, localProps.getProperty(key));
                        System.out.println("📁 Loaded from local file: " + key);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("📁 Local config file not found: " + e.getMessage());
        }
        
        try {
            // Essayer config.properties.local comme fallback
            String projectPath = System.getProperty("user.dir");
            String localConfigPath = projectPath + "/config.properties.local";
            
            try (FileInputStream fis = new FileInputStream(localConfigPath)) {
                Properties localProps = new Properties();
                localProps.load(fis);
                
                // Ajouter seulement les clés non déjà définies
                for (String key : localProps.stringPropertyNames()) {
                    if (!properties.containsKey(key)) {
                        properties.setProperty(key, localProps.getProperty(key));
                        System.out.println("📁 Loaded from local file: " + key);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("📁 Local config file not found: " + e.getMessage());
        }
    }
    
    private static void loadFromClasspath() {
        try {
            // Essayer config.properties dans le classpath
            try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (is != null) {
                    Properties classpathProps = new Properties();
                    classpathProps.load(is);
                    
                    // Ajouter seulement les clés non déjà définies
                    for (String key : classpathProps.stringPropertyNames()) {
                        if (!properties.containsKey(key)) {
                            String value = classpathProps.getProperty(key);
                            
                            // Gérer les variables d'environnement dans le fichier
                            if (value != null && value.startsWith("${") && value.endsWith("}")) {
                                String envVar = value.substring(2, value.length() - 1);
                                String envValue = System.getenv(envVar);
                                if (envValue != null) {
                                    properties.setProperty(key, envValue);
                                    System.out.println("🌍 Loaded from env var in config: " + key);
                                }
                            } else {
                                properties.setProperty(key, value);
                                System.out.println("📦 Loaded from classpath: " + key);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("📦 Classpath config not found: " + e.getMessage());
        }
    }
    
    // Méthodes publiques pour accéder aux clés
    public static String getBrevoKey() {
        String key = properties.getProperty("brevo.api.key");
        if (key == null || key.trim().isEmpty()) {
            System.out.println("❌ NO BREVO API KEY FOUND IN CONFIGURATION");
            return null;
        }
        System.out.println("✅ Brevo API key found: " + maskKey(key));
        return key;
    }
    
    public static String getStripeKey() {
        String key = properties.getProperty("stripe.secret.key");
        if (key == null || key.trim().isEmpty()) {
            System.out.println("❌ NO STRIPE API KEY FOUND IN CONFIGURATION");
            return null;
        }
        System.out.println("✅ Stripe API key found: " + maskKey(key));
        return key;
    }
    
    public static String getOpenAIKey() {
        String key = properties.getProperty("openai.api.key");
        if (key == null || key.trim().isEmpty()) {
            System.out.println("❌ NO OPENAI API KEY FOUND IN CONFIGURATION");
            return null;
        }
        System.out.println("✅ OpenAI API key found: " + maskKey(key));
        return key;
    }
    
    // Méthode utilitaire pour masquer les clés dans les logs
    private static String maskKey(String key) {
        if (key == null || key.length() < 10) {
            return "***";
        }
        return key.substring(0, 8) + "..." + key.substring(key.length() - 4);
    }
    
    // Méthode pour débogage
    public static void printConfigurationStatus() {
        System.out.println("=== CONFIGURATION STATUS ===");
        System.out.println("Brevo API: " + (properties.containsKey("brevo.api.key") ? "✅ Configured" : "❌ Not configured"));
        System.out.println("Stripe API: " + (properties.containsKey("stripe.secret.key") ? "✅ Configured" : "❌ Not configured"));
        System.out.println("OpenAI API: " + (properties.containsKey("openai.api.key") ? "✅ Configured" : "❌ Not configured"));
        System.out.println("========================");
    }
}
