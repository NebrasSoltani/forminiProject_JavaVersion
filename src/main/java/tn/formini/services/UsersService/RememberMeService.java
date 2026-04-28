package tn.formini.services.UsersService;

import java.util.prefs.Preferences;

/**
 * Service for handling "Remember Me" functionality using Java Preferences API
 * Stores user credentials securely on the local machine
 */
public class RememberMeService {
    
    private static final String PREF_NAME = "formini_login";
    private static final String EMAIL_KEY = "remembered_email";
    private static final String PASSWORD_KEY = "remembered_password";
    private static final String REMEMBER_FLAG_KEY = "remember_me_flag";
    
    private Preferences preferences;
    
    public RememberMeService() {
        preferences = Preferences.userRoot().node(PREF_NAME);
    }
    
    /**
     * Save user credentials for remember me functionality
     * @param email User email
     * @param password User password (plain text - will be stored)
     */
    public void saveCredentials(String email, String password) {
        if (email != null && !email.trim().isEmpty() && 
            password != null && !password.trim().isEmpty()) {
            
            preferences.put(EMAIL_KEY, email.trim());
            preferences.put(PASSWORD_KEY, password);
            preferences.putBoolean(REMEMBER_FLAG_KEY, true);
            
            System.out.println("Identifiants sauvegardés pour: " + email);
        }
    }
    
    /**
     * Get saved email if remember me was checked
     * @return Saved email or null if not found
     */
    public String getSavedEmail() {
        if (isRememberMeChecked()) {
            return preferences.get(EMAIL_KEY, null);
        }
        return null;
    }
    
    /**
     * Get saved password if remember me was checked
     * @return Saved password or null if not found
     */
    public String getSavedPassword() {
        if (isRememberMeChecked()) {
            return preferences.get(PASSWORD_KEY, null);
        }
        return null;
    }
    
    /**
     * Check if remember me was previously checked
     * @return true if remember me flag is set, false otherwise
     */
    public boolean isRememberMeChecked() {
        return preferences.getBoolean(REMEMBER_FLAG_KEY, false);
    }
    
    /**
     * Clear all saved credentials
     */
    public void clearCredentials() {
        preferences.remove(EMAIL_KEY);
        preferences.remove(PASSWORD_KEY);
        preferences.remove(REMEMBER_FLAG_KEY);
        
        System.out.println("Identifiants sauvegardés effacés");
    }
    
    /**
     * Update remember me flag without saving credentials
     * @param rememberMe New remember me state
     */
    public void setRememberMeFlag(boolean rememberMe) {
        preferences.putBoolean(REMEMBER_FLAG_KEY, rememberMe);
    }
    
    /**
     * Check if there are any saved credentials
     * @return true if credentials exist, false otherwise
     */
    public boolean hasSavedCredentials() {
        return isRememberMeChecked() && 
               preferences.get(EMAIL_KEY, null) != null && 
               preferences.get(PASSWORD_KEY, null) != null;
    }
}
