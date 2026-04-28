package tn.formini.services.UsersService;

import tn.formini.entities.Users.User;
import tn.formini.tools.MyDataBase;
import tn.formini.utils.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoginService {
    
    private Connection cnx;
    
    public LoginService() {
        cnx = MyDataBase.getInstance().getCnx();
    }
    
    /**
     * Authenticate user with email and password
     * @param email User email
     * @param password User password (plain text)
     * @return User object if authentication successful, null otherwise
     */
    public User authenticate(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            System.out.println("Email et mot de passe sont obligatoires");
            return null;
        }
        
        String req = "SELECT * FROM user WHERE LOWER(email) = LOWER(?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                
                // Verify password using secure hash verification
                if (PasswordUtil.verifyPassword(password, user.getPassword())) {
                    System.out.println("Authentification réussie pour: " + email);
                    return user;
                } else {
                    System.out.println("Mot de passe incorrect pour: " + email);
                }
            } else {
                System.out.println("Aucun utilisateur trouvé avec l'email: " + email);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de l'authentification: " + ex.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get user by email
     * @param email User email
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        String req = "SELECT * FROM user WHERE LOWER(email) = LOWER(?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la récupération de l'utilisateur: " + ex.getMessage());
        }
        
        return null;
    }
    
    /**
     * Check if user account is verified
     * @param user User object
     * @return true if email is verified, false otherwise
     */
    public boolean isAccountVerified(User user) {
        if (user == null) {
            return false;
        }
        // Check if email is verified
        return user.isIs_email_verified();
    }
    
    /**
     * Get user role for authorization
     * @param user User object
     * @return user role string
     */
    public String getUserRole(User user) {
        return user != null ? user.getRole_utilisateur() : null;
    }
    
    /**
     * Validate user credentials format before database query
     * @param email User email
     * @param password User password
     * @return true if format is valid, false otherwise
     */
    public boolean validateCredentialsFormat(String email, String password) {
        // Basic format validation
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Password validation (basic check)
        if (password == null || password.length() < 8) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Map ResultSet to User object
     * @param rs ResultSet from database query
     * @return User object
     * @throws SQLException if database error occurs
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setRoles(rs.getString("roles"));
        user.setPassword(rs.getString("password"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setTelephone(rs.getString("telephone"));
        user.setGouvernorat(rs.getString("gouvernorat"));
        
        Timestamp dateNaissance = rs.getTimestamp("date_naissance");
        if (dateNaissance != null) {
            user.setDate_naissance(new Date(dateNaissance.getTime()));
        }
        
        user.setRole_utilisateur(rs.getString("role_utilisateur"));
        user.setPhoto(rs.getString("photo"));
        user.setIs_email_verified(rs.getBoolean("is_email_verified"));
        user.setEmail_verification_token(rs.getString("email_verification_token"));
        
        Timestamp tokenExpires = rs.getTimestamp("email_verification_token_expires_at");
        if (tokenExpires != null) {
            user.setEmail_verification_token_expires_at(new Date(tokenExpires.getTime()));
        }
        
        Timestamp emailVerified = rs.getTimestamp("email_verified_at");
        if (emailVerified != null) {
            user.setEmail_verified_at(new Date(emailVerified.getTime()));
        }
        
        user.setGoogle_id(rs.getString("google_id"));
        user.setGithub_id(rs.getString("github_id"));
        user.setOauth_provider(rs.getString("oauth_provider"));
        user.setAvatar_url(rs.getString("avatar_url"));
        user.setGoogle_authenticator_secret(rs.getString("google_authenticator_secret"));
        user.setBackup_codes(rs.getString("backup_codes"));
        user.setEmail_auth_enabled(rs.getBoolean("email_auth_enabled"));
        user.setGoogle_auth_enabled(rs.getBoolean("google_auth_enabled"));
        user.setPhone_verified(rs.getBoolean("phone_verified"));
        
        Timestamp phoneVerified = rs.getTimestamp("phone_verified_at");
        if (phoneVerified != null) {
            user.setPhone_verified_at(new Date(phoneVerified.getTime()));
        }
        
        return user;
    }
    
    /**
     * Update last login timestamp for user
     * @param userId User ID
     * @return true if update successful, false otherwise
     */
    public boolean updateLastLogin(int userId) {
        String req = "UPDATE user SET last_login = NOW() WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la mise à jour du dernier login: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user account is active/blocked
     * @param user User object
     * @return true if account is active, false if blocked
     */
    public boolean isAccountActive(User user) {
        // You can add logic here to check if account is blocked/suspended
        // For now, all accounts are considered active
        return user != null;
    }
}
