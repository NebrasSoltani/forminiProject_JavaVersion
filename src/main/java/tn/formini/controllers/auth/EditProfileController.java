package tn.formini.controllers.auth;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Gouvernorat;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.SessionManager;
import tn.formini.services.UsersService.UserService;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class EditProfileController implements Initializable {

    @FXML private Label lblMessage;
    @FXML private Label lblRole;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldNom;
    @FXML private TextField fieldPrenom;
    @FXML private TextField fieldTelephone;
    @FXML private ComboBox<Gouvernorat> fieldGouvernorat;
    @FXML private DatePicker fieldDateNaissance;

    @FXML private VBox apprenantSection;
    @FXML private ComboBox<String> comboGenre;
    @FXML private ComboBox<String> comboEtatCivil;
    @FXML private TextField fieldObjectif;
    @FXML private TextField fieldDomainesInteret;

    @FXML private VBox formateurSection;
    @FXML private TextField fieldSpecialite;
    @FXML private TextArea fieldBio;
    @FXML private Spinner<Integer> spinnerExperience;
    @FXML private TextField fieldLinkedin;
    @FXML private TextField fieldPortfolio;
    @FXML private TextField fieldCv;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final UserService userService = new UserService();
    private final ApprenantService apprenantService = new ApprenantService();
    private final FormateurService formateurService = new FormateurService();

    private Runnable onBack;
    private User currentUser;
    private Apprenant currentApprenant;
    private Formateur currentFormateur;

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboGenre.getItems().addAll("homme", "femme");
        comboEtatCivil.getItems().addAll("celibataire", "marie", "divorce", "veuf");
        fieldGouvernorat.getItems().addAll(Gouvernorat.values());
        spinnerExperience.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 70, 0));
        hideMessage();
        loadConnectedUser();
    }

    private void loadConnectedUser() {
        if (!sessionManager.isLoggedIn() || sessionManager.getCurrentUser() == null) {
            showMessage("Aucun utilisateur connecté.");
            setDisableForm(true);
            return;
        }

        User sessionUser = sessionManager.getCurrentUser();
        currentUser = userService.findById(sessionUser.getId());
        if (currentUser == null) {
            currentUser = sessionUser;
        }

        fillGeneralData(currentUser);
        configureRoleSections(currentUser.getRole_utilisateur());
        loadRoleData(currentUser);
    }

    private void fillGeneralData(User user) {
        fieldEmail.setText(safe(user.getEmail()));
        fieldNom.setText(safe(user.getNom()));
        fieldPrenom.setText(safe(user.getPrenom()));
        fieldTelephone.setText(safe(user.getTelephone()));
        if (user.getGouvernorat() != null) {
                Gouvernorat gouvernorat = Gouvernorat.fromDisplayName(user.getGouvernorat());
                fieldGouvernorat.setValue(gouvernorat);
            }
        if (user.getDate_naissance() != null) {
            fieldDateNaissance.setValue(user.getDate_naissance().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
        }
    }

    private void configureRoleSections(String role) {
        String normalized = role == null ? "" : role.trim().toLowerCase();
        lblRole.setText("Rôle : " + (role == null || role.isBlank() ? "non défini" : role));
        boolean isApprenant = "apprenant".equals(normalized);
        boolean isFormateur = "formateur".equals(normalized);

        apprenantSection.setVisible(isApprenant);
        apprenantSection.setManaged(isApprenant);
        formateurSection.setVisible(isFormateur);
        formateurSection.setManaged(isFormateur);
    }

    private void loadRoleData(User user) {
        String role = user.getRole_utilisateur() == null ? "" : user.getRole_utilisateur().trim().toLowerCase();
        if ("apprenant".equals(role)) {
            currentApprenant = apprenantService.findByUserId(user.getId());
            if (currentApprenant != null) {
                comboGenre.setValue(currentApprenant.getGenre());
                comboEtatCivil.setValue(currentApprenant.getEtat_civil());
                fieldObjectif.setText(safe(currentApprenant.getObjectif()));
                fieldDomainesInteret.setText(safe(currentApprenant.getDomaines_interet()));
            }
        } else if ("formateur".equals(role)) {
            currentFormateur = formateurService.findByUserId(user.getId());
            if (currentFormateur != null) {
                fieldSpecialite.setText(safe(currentFormateur.getSpecialite()));
                fieldBio.setText(safe(currentFormateur.getBio()));
                spinnerExperience.getValueFactory().setValue(
                    currentFormateur.getExperience_annees() == null ? 0 : currentFormateur.getExperience_annees()
                );
                fieldLinkedin.setText(safe(currentFormateur.getLinkedin()));
                fieldPortfolio.setText(safe(currentFormateur.getPortfolio()));
                fieldCv.setText(safe(currentFormateur.getCv()));
            }
        }
    }

    private void setDisableForm(boolean disable) {
        fieldEmail.setDisable(disable);
        fieldNom.setDisable(disable);
        fieldPrenom.setDisable(disable);
        fieldTelephone.setDisable(disable);
        fieldGouvernorat.setDisable(disable);
        fieldDateNaissance.setDisable(disable);
        apprenantSection.setDisable(disable);
        formateurSection.setDisable(disable);
    }

    @FXML
    private void onSave() {
        if (currentUser == null) {
            showMessage("Aucun utilisateur connecté.");
            return;
        }
        try {
            applyGeneralChanges(currentUser);
            userService.modifier(currentUser);

            String role = currentUser.getRole_utilisateur() == null ? "" : currentUser.getRole_utilisateur().trim().toLowerCase();
            if ("apprenant".equals(role)) {
                saveApprenant();
            } else if ("formateur".equals(role)) {
                saveFormateur();
            }

            sessionManager.login(currentUser);
            showMessage("Profil mis à jour avec succès.");
        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage() == null ? "Données invalides." : ex.getMessage());
        } catch (Exception ex) {
            showMessage("Erreur lors de la sauvegarde du profil.");
        }
    }

    private void applyGeneralChanges(User user) {
        user.setEmail(trim(fieldEmail.getText()));
        user.setNom(trim(fieldNom.getText()));
        user.setPrenom(trim(fieldPrenom.getText()));
        user.setTelephone(trim(fieldTelephone.getText()));
        Gouvernorat selectedGouvernorat = fieldGouvernorat.getValue();
        user.setGouvernorat(selectedGouvernorat != null ? selectedGouvernorat.getDisplayName() : null);

        LocalDate birthDate = fieldDateNaissance.getValue();
        if (birthDate != null) {
            Date date = Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            user.setDate_naissance(date);
        } else {
            user.setDate_naissance(null);
        }
    }

    private void saveApprenant() {
        if (currentApprenant == null) {
            currentApprenant = new Apprenant();
            currentApprenant.setUser(currentUser);
        }
        currentApprenant.setGenre(comboGenre.getValue());
        currentApprenant.setEtat_civil(comboEtatCivil.getValue());
        currentApprenant.setObjectif(trim(fieldObjectif.getText()));
        currentApprenant.setDomaines_interet(trim(fieldDomainesInteret.getText()));
        if (currentApprenant.getId() > 0) {
            apprenantService.modifier(currentApprenant);
        } else {
            apprenantService.ajouter(currentApprenant);
        }
    }

    private void saveFormateur() {
        if (currentFormateur == null) {
            currentFormateur = new Formateur();
            currentFormateur.setUser(currentUser);
        }
        currentFormateur.setSpecialite(trim(fieldSpecialite.getText()));
        currentFormateur.setBio(trim(fieldBio.getText()));
        Integer exp = spinnerExperience.getValue() == null ? 0 : spinnerExperience.getValue();
        currentFormateur.setExperience_annees(exp == 0 ? null : exp);
        currentFormateur.setLinkedin(trim(fieldLinkedin.getText()));
        currentFormateur.setPortfolio(trim(fieldPortfolio.getText()));
        currentFormateur.setCv(trim(fieldCv.getText()));
        if (currentFormateur.getId() > 0) {
            formateurService.modifier(currentFormateur);
        } else {
            formateurService.ajouter(currentFormateur);
        }
    }

    @FXML
    private void onBack() {
        if (onBack != null) {
            onBack.run();
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showMessage(String text) {
        lblMessage.setText(text);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void hideMessage() {
        lblMessage.setText("");
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}
