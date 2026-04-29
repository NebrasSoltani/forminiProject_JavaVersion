package tn.formini.services.UsersService;

import tn.formini.entities.Users.User;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;
import tn.formini.utils.PasswordUtil;
import tn.formini.utils.TOTPService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements service<User> {

    Connection cnx;
    public UserService(){
        cnx= MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(User u) {


        String req = "INSERT INTO user (email, roles, password, nom, prenom, telephone, gouvernorat, date_naissance, role_utilisateur) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            // Hash the password before storing
            String hashedPassword = PasswordUtil.hashPassword(u.getPassword());
            
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getRoles());
            ps.setString(3, hashedPassword);
            ps.setString(4, u.getNom());
            ps.setString(5, u.getPrenom());
            ps.setString(6, u.getTelephone());
            ps.setString(7, u.getGouvernorat());
            ps.setTimestamp(8, u.getDate_naissance() != null ? new Timestamp(u.getDate_naissance().getTime()) : null);
            ps.setString(9, u.getRole_utilisateur());
            ps.executeUpdate();
            
            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                u.setId(rs.getInt(1));
                System.out.println("Utilisateur ajouté avec succès ! ID: " + u.getId());
            } else {
                System.out.println("Erreur: Aucun ID généré pour l'utilisateur");
            }
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter utilisateur : " + ex.getMessage());
            // En cas d'erreur, s'assurer que l'ID n'est pas défini
            u.setId(0);
        }
    }

    @Override
    public void modifier(User u) {

        String password = u.getPassword();
        boolean isPasswordHashed = password != null && password.startsWith("$2a$");

        // Build SQL based on whether password needs to be updated
        String req;
        if (isPasswordHashed) {
            // Password already hashed, don't update it
            req = "UPDATE user SET email=?, roles=?, nom=?, prenom=?, telephone=?, gouvernorat=?, date_naissance=?, role_utilisateur=?, photo=?, is_email_verified=?, email_verification_token=?, email_verification_token_expires_at=?, email_verified_at=?, password_reset_token=?, password_reset_token_expires_at=?, google_authenticator_secret=?, backup_codes=?, email_auth_enabled=?, google_auth_enabled=?, phone_verified=?, phone_verified_at=?, face_auth_enabled=? WHERE id=?";
        } else {
            // New password, hash and update it
            req = "UPDATE user SET email=?, roles=?, password=?, nom=?, prenom=?, telephone=?, gouvernorat=?, date_naissance=?, role_utilisateur=?, photo=?, is_email_verified=?, email_verification_token=?, email_verification_token_expires_at=?, email_verified_at=?, password_reset_token=?, password_reset_token_expires_at=?, google_authenticator_secret=?, backup_codes=?, email_auth_enabled=?, google_auth_enabled=?, phone_verified=?, phone_verified_at=?, face_auth_enabled=? WHERE id=?";
        }

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getRoles());

            if (!isPasswordHashed) {
                String hashedPassword = PasswordUtil.hashPassword(password);
                ps.setString(3, hashedPassword);
            }

            int offset = isPasswordHashed ? 2 : 3;
            ps.setString(offset + 1, u.getNom());
            ps.setString(offset + 2, u.getPrenom());
            ps.setString(offset + 3, u.getTelephone());
            ps.setString(offset + 4, u.getGouvernorat());
            ps.setTimestamp(offset + 5, u.getDate_naissance() != null ? new Timestamp(u.getDate_naissance().getTime()) : null);
            ps.setString(offset + 6, u.getRole_utilisateur());
            ps.setString(offset + 7, u.getPhoto());
            ps.setBoolean(offset + 8, u.isIs_email_verified());
            ps.setString(offset + 9, u.getEmail_verification_token());
            ps.setTimestamp(offset + 10, u.getEmail_verification_token_expires_at() != null ? new Timestamp(u.getEmail_verification_token_expires_at().getTime()) : null);
            ps.setTimestamp(offset + 11, u.getEmail_verified_at() != null ? new Timestamp(u.getEmail_verified_at().getTime()) : null);
            ps.setString(offset + 12, u.getPassword_reset_token());
            ps.setTimestamp(offset + 13, u.getPassword_reset_token_expires_at() != null ? new Timestamp(u.getPassword_reset_token_expires_at().getTime()) : null);
            ps.setString(offset + 14, u.getGoogle_authenticator_secret());
            ps.setString(offset + 15, u.getBackup_codes());
            ps.setBoolean(offset + 16, u.isEmail_auth_enabled());
            ps.setBoolean(offset + 17, u.isGoogle_auth_enabled());
            ps.setBoolean(offset + 18, u.isPhone_verified());
            ps.setTimestamp(offset + 19, u.getPhone_verified_at() != null ? new Timestamp(u.getPhone_verified_at().getTime()) : null);
            ps.setBoolean(offset + 20, u.isFace_auth_enabled());
            ps.setInt(offset + 21, u.getId());

            ps.executeUpdate();
            System.out.println("Utilisateur modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier utilisateur : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {

        String req = "DELETE FROM user WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Utilisateur supprimé avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer utilisateur : " + ex.getMessage());
        }
    }

    @Override
    public List<User> afficher() {
        List<User> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM user";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setRole_utilisateur(rs.getString("role_utilisateur"));
                list.add(u);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher utilisateur : " + ex.getMessage());
        }
        return list;
    }

    /**
     * Indique si un compte existe déjà pour cet email (comparaison insensible à la casse).
     */
    public boolean emailExists(String email) {
        if (cnx == null || email == null) {
            return false;
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        String req = "SELECT id FROM user WHERE LOWER(email) = LOWER(?) LIMIT 1";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, trimmed);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            System.out.println("Erreur emailExists : " + ex.getMessage());
            return false;
        }
    }

    public User findById(int id) {
        if (cnx == null || id <= 0) {
            return null;
        }
        String req = "SELECT * FROM user WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setRoles(rs.getString("roles"));
                u.setPassword(rs.getString("password"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setTelephone(rs.getString("telephone"));
                u.setGouvernorat(rs.getString("gouvernorat"));
                Timestamp naissance = rs.getTimestamp("date_naissance");
                if (naissance != null) {
                    u.setDate_naissance(new java.util.Date(naissance.getTime()));
                }
                u.setRole_utilisateur(rs.getString("role_utilisateur"));
                u.setPhoto(rs.getString("photo"));
                u.setIs_email_verified(rs.getBoolean("is_email_verified"));
                u.setGoogle_authenticator_secret(rs.getString("google_authenticator_secret"));
                u.setBackup_codes(rs.getString("backup_codes"));
                u.setEmail_auth_enabled(rs.getBoolean("email_auth_enabled"));
                u.setGoogle_auth_enabled(rs.getBoolean("google_auth_enabled"));
                u.setPhone_verified(rs.getBoolean("phone_verified"));
                Timestamp phoneVerifiedAt = rs.getTimestamp("phone_verified_at");
                if (phoneVerifiedAt != null) {
                    u.setPhone_verified_at(new java.util.Date(phoneVerifiedAt.getTime()));
                }
                u.setFace_auth_enabled(rs.getBoolean("face_auth_enabled"));
                return u;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findById user : " + ex.getMessage());
        }
        return null;
    }

    public User getUserByEmail(String email) {
        if (cnx == null || email == null || email.trim().isEmpty()) {
            return null;
        }
        String req = "SELECT * FROM user WHERE LOWER(email) = LOWER(?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setRoles(rs.getString("roles"));
                u.setPassword(rs.getString("password"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setTelephone(rs.getString("telephone"));
                u.setGouvernorat(rs.getString("gouvernorat"));
                Timestamp naissance = rs.getTimestamp("date_naissance");
                if (naissance != null) {
                    u.setDate_naissance(new java.util.Date(naissance.getTime()));
                }
                u.setRole_utilisateur(rs.getString("role_utilisateur"));
                u.setPhoto(rs.getString("photo"));
                u.setIs_email_verified(rs.getBoolean("is_email_verified"));
                u.setEmail_verification_token(rs.getString("email_verification_token"));
                Timestamp emailTokenExpiry = rs.getTimestamp("email_verification_token_expires_at");
                if (emailTokenExpiry != null) {
                    u.setEmail_verification_token_expires_at(new java.util.Date(emailTokenExpiry.getTime()));
                }
                Timestamp emailVerifiedAt = rs.getTimestamp("email_verified_at");
                if (emailVerifiedAt != null) {
                    u.setEmail_verified_at(new java.util.Date(emailVerifiedAt.getTime()));
                }
                u.setPassword_reset_token(rs.getString("password_reset_token"));
                Timestamp passwordTokenExpiry = rs.getTimestamp("password_reset_token_expires_at");
                if (passwordTokenExpiry != null) {
                    u.setPassword_reset_token_expires_at(new java.util.Date(passwordTokenExpiry.getTime()));
                }
                u.setGoogle_authenticator_secret(rs.getString("google_authenticator_secret"));
                u.setBackup_codes(rs.getString("backup_codes"));
                u.setEmail_auth_enabled(rs.getBoolean("email_auth_enabled"));
                u.setGoogle_auth_enabled(rs.getBoolean("google_auth_enabled"));
                u.setPhone_verified(rs.getBoolean("phone_verified"));
                Timestamp phoneVerifiedAt = rs.getTimestamp("phone_verified_at");
                if (phoneVerifiedAt != null) {
                    u.setPhone_verified_at(new java.util.Date(phoneVerifiedAt.getTime()));
                }
                u.setFace_auth_enabled(rs.getBoolean("face_auth_enabled"));
                return u;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur getUserByEmail : " + ex.getMessage());
        }
        return null;
    }

    public User getUserByResetToken(String token) {
        if (cnx == null || token == null || token.trim().isEmpty()) {
            return null;
        }
        String req = "SELECT * FROM user WHERE password_reset_token = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setRoles(rs.getString("roles"));
                u.setPassword(rs.getString("password"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setTelephone(rs.getString("telephone"));
                u.setGouvernorat(rs.getString("gouvernorat"));
                Timestamp naissance = rs.getTimestamp("date_naissance");
                if (naissance != null) {
                    u.setDate_naissance(new java.util.Date(naissance.getTime()));
                }
                u.setRole_utilisateur(rs.getString("role_utilisateur"));
                u.setPhoto(rs.getString("photo"));
                u.setIs_email_verified(rs.getBoolean("is_email_verified"));
                u.setPassword_reset_token(rs.getString("password_reset_token"));
                Timestamp passwordTokenExpiry = rs.getTimestamp("password_reset_token_expires_at");
                if (passwordTokenExpiry != null) {
                    u.setPassword_reset_token_expires_at(new java.util.Date(passwordTokenExpiry.getTime()));
                }
                u.setGoogle_authenticator_secret(rs.getString("google_authenticator_secret"));
                u.setBackup_codes(rs.getString("backup_codes"));
                u.setEmail_auth_enabled(rs.getBoolean("email_auth_enabled"));
                u.setGoogle_auth_enabled(rs.getBoolean("google_auth_enabled"));
                u.setPhone_verified(rs.getBoolean("phone_verified"));
                Timestamp phoneVerifiedAt = rs.getTimestamp("phone_verified_at");
                if (phoneVerifiedAt != null) {
                    u.setPhone_verified_at(new java.util.Date(phoneVerifiedAt.getTime()));
                }
                u.setFace_auth_enabled(rs.getBoolean("face_auth_enabled"));
                return u;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur getUserByResetToken : " + ex.getMessage());
        }
        return null;
    }

    /**
     * Enable Google Authenticator 2FA for a user
     * @param userId the user ID
     * @param secret the TOTP secret
     * @param backupCodes JSON string of backup codes
     * @return true if successful
     */
    public boolean enableGoogleAuth(int userId, String secret, String backupCodes) {
        String req = "UPDATE user SET google_authenticator_secret=?, backup_codes=?, google_auth_enabled=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, secret);
            ps.setString(2, backupCodes);
            ps.setBoolean(3, true);
            ps.setInt(4, userId);
            ps.executeUpdate();
            System.out.println("Google Authenticator 2FA enabled for user ID: " + userId);
            return true;
        } catch (SQLException ex) {
            System.out.println("Erreur enableGoogleAuth : " + ex.getMessage());
            return false;
        }
    }

    /**
     * Disable Google Authenticator 2FA for a user
     * @param userId the user ID
     * @return true if successful
     */
    public boolean disableGoogleAuth(int userId) {
        String req = "UPDATE user SET google_authenticator_secret=?, backup_codes=?, google_auth_enabled=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, null);
            ps.setString(2, null);
            ps.setBoolean(3, false);
            ps.setInt(4, userId);
            ps.executeUpdate();
            System.out.println("Google Authenticator 2FA disabled for user ID: " + userId);
            return true;
        } catch (SQLException ex) {
            System.out.println("Erreur disableGoogleAuth : " + ex.getMessage());
            return false;
        }
    }

    /**
     * Use a backup code and remove it from the list
     * @param userId the user ID
     * @param backupCode the backup code to use
     * @return true if code was valid and used
     */
    public boolean useBackupCode(int userId, String backupCode) {
        User user = findById(userId);
        if (user == null || user.getBackup_codes() == null) {
            return false;
        }

        TOTPService totpService = new TOTPService();
        List<String> codes = totpService.parseBackupCodesFromJson(user.getBackup_codes());
        
        if (codes.contains(backupCode)) {
            codes.remove(backupCode);
            String newJson = totpService.backupCodesToJson(codes);
            
            String req = "UPDATE user SET backup_codes=? WHERE id=?";
            try {
                PreparedStatement ps = cnx.prepareStatement(req);
                ps.setString(1, newJson);
                ps.setInt(2, userId);
                ps.executeUpdate();
                System.out.println("Backup code used and removed for user ID: " + userId);
                return true;
            } catch (SQLException ex) {
                System.out.println("Erreur useBackupCode : " + ex.getMessage());
                return false;
            }
        }
        
        return false;
    }
}
