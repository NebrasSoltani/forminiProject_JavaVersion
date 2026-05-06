package tn.formini.services.UsersService;

import tn.formini.entities.Users.User;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    private Map<String, Object> sessionData;
    private boolean isLoggedIn;
    
    private SessionManager() {
        sessionData = new HashMap<>();
        isLoggedIn = false;
        currentUser = null;
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Login user and create session
     * @param user User object that successfully authenticated
     */
    public void login(User user) {
        if (user != null) {
            this.currentUser = user;
            this.isLoggedIn = true;
            this.sessionData.clear();
            
            // Store basic user info in session
            sessionData.put("userId", user.getId());
            sessionData.put("userEmail", user.getEmail());
            sessionData.put("userRole", user.getRole_utilisateur());
            sessionData.put("userName", user.getNom() + " " + user.getPrenom());
            sessionData.put("loginTime", System.currentTimeMillis());
            
            System.out.println("Session créée pour l'utilisateur: " + user.getEmail());
        }
    }
    
    /**
     * Logout user and clear session
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("Session terminée pour l'utilisateur: " + currentUser.getEmail());
        }
        
        currentUser = null;
        isLoggedIn = false;
        sessionData.clear();
    }
    
    /**
     * Get current logged in user
     * @return Current user object or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is logged in
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return isLoggedIn && currentUser != null;
    }
    
    /**
     * Get current user role
     * @return User role string or null if not logged in
     */
    public String getCurrentUserRole() {
        return isLoggedIn && currentUser != null ? currentUser.getRole_utilisateur() : null;
    }
    
    /**
     * Get current user ID
     * @return User ID or -1 if not logged in
     */
    public int getCurrentUserId() {
        return isLoggedIn && currentUser != null ? currentUser.getId() : -1;
    }
    
    /**
     * Store data in session
     * @param key Session key
     * @param value Session value
     */
    public void setSessionData(String key, Object value) {
        sessionData.put(key, value);
    }
    
    /**
     * Get data from session
     * @param key Session key
     * @return Session value or null if not found
     */
    public Object getSessionData(String key) {
        return sessionData.get(key);
    }
    
    /**
     * Check if current user has specific role
     * @param role Role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        return isLoggedIn && currentUser != null && 
               role.equalsIgnoreCase(currentUser.getRole_utilisateur());
    }
    
    /**
     * Check if current user is admin
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return hasRole("admin");
    }
    
    /**
     * Check if current user is formateur
     * @return true if user is formateur, false otherwise
     */
    public boolean isFormateur() {
        return hasRole("formateur");
    }
    
    /**
     * Check if current user is apprenant
     * @return true if user is apprenant, false otherwise
     */
    public boolean isApprenant() {
        return hasRole("apprenant");
    }
    
    /**
     * Check if current user is societe
     * @return true if user is societe, false otherwise
     */
    public boolean isSociete() {
        return hasRole("societe");
    }
    
    /**
     * Get session duration in milliseconds
     * @return Session duration or 0 if not logged in
     */
    public long getSessionDuration() {
        if (!isLoggedIn) return 0;
        
        Long loginTime = (Long) sessionData.get("loginTime");
        return loginTime != null ? System.currentTimeMillis() - loginTime : 0;
    }
    
    /**
     * Refresh session (update login time)
     */
    public void refreshSession() {
        if (isLoggedIn) {
            sessionData.put("loginTime", System.currentTimeMillis());
        }
    }
    
    /**
     * Get all session data for debugging
     * @return Map of all session data
     */
    public Map<String, Object> getAllSessionData() {
        return new HashMap<>(sessionData);
    }
    
    /**
     * Validate session is still valid (you can add timeout logic here)
     * @return true if session is valid, false otherwise
     */
    public boolean isSessionValid() {
        if (!isLoggedIn) return false;
        
        // You can add session timeout logic here
        // For example, session expires after 8 hours
        long sessionDuration = getSessionDuration();
        long maxSessionDuration = 8 * 60 * 60 * 1000; // 8 hours in milliseconds
        
        return sessionDuration < maxSessionDuration;
    }
}
