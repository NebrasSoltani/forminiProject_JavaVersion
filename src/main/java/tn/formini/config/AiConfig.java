package tn.formini.config;

/**
 * Centralized configuration for AI services.
 * IMPORTANT: To avoid leaking your API key, prefer using the GEMINI_API_KEY environment variable.
 */
public class AiConfig {
    
    // Default key used as fallback if environment variable is not set
    // Replace this string with your new valid API key
    private static String API_KEY = "AIzaSyADwvT9_4yN_AHQ1O1oSd6OG_R9sqHVFCg";

    /**
     * Retrieves the API key.
     * Priorities:
     * 1. Environment variable GEMINI_API_KEY
     * 2. Hardcoded fallback key
     */
    public static String getApiKey() {
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        return API_KEY;
    }

    /**
     * Optionally sets the key at runtime.
     */
    public static void setApiKey(String key) {
        API_KEY = key;
    }
}
