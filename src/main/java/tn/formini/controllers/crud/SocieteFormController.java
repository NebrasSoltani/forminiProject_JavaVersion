package tn.formini.controllers.crud;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.formini.controllers.MainController;
import tn.formini.entities.Users.Gouvernorat;
import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.services.FileUploadService;
import tn.formini.services.UsersService.SocieteService;
import tn.formini.services.UsersService.UserService;
import tn.formini.utils.SignupFieldValidation;
import tn.formini.utils.TunisiaGovernorates;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.io.File;

public class SocieteFormController implements Initializable {

    /** Quand défini (ex. depuis MainController), retourne au shell parent au lieu de remplacer la scène. */
    private Runnable onBack;
    private MainController mainController;

    @FXML private Label lblMessage;
    @FXML private Label lblTitle;
    @FXML private Label lblSubtitle;
    @FXML private TextField fieldNomSociete;
    @FXML private TextField fieldSecteur;
    @FXML private TextArea fieldDescription;
    @FXML private TextField fieldAdresse;
    @FXML private TextField fieldSiteWeb;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    // User creation fields
    @FXML private VBox panelNewUser;
    @FXML private TextField fieldUserEmail;
    @FXML private TextField fieldUserTelephone;
    @FXML private PasswordField fieldUserPassword;
    @FXML private PasswordField fieldUserPasswordConfirm;
    @FXML private Button btnToggleUserPassword;
    @FXML private Button btnToggleUserPasswordConfirm;
    @FXML private Label eyeIconUser;
    @FXML private Label eyeSlashIconUser;
    @FXML private Label eyeIconUserConfirm;
    @FXML private Label eyeSlashIconUserConfirm;
    @FXML private TextField fieldUserNom;
    @FXML private TextField fieldUserPrenom;
    @FXML private DatePicker fieldUserDateNaissance;
    @FXML private ComboBox<Gouvernorat> fieldUserGouvernorat;
    
    // Error labels
    @FXML private Label errorNomSociete;
    @FXML private Label errorSecteur;
    @FXML private Label errorDescription;
    @FXML private Label errorAdresse;
    @FXML private Label errorSiteWeb;
    @FXML private Label errorUserEmail;
    @FXML private Label errorUserTelephone;
    @FXML private Label errorUserPassword;
    @FXML private Label errorUserPasswordConfirm;
    @FXML private Label errorUserNom;
    @FXML private Label errorUserPrenom;
    @FXML private Label errorUserDateNaissance;

    private SocieteService societeService;
    private UserService userService;
    @FXML
    private Label heroSubLabel;

    @FXML
    private VBox passwordGroup;

    @FXML
    private VBox passwordConfirmGroup;

    @FXML
    private Label passwordHintLabel;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField passwordConfirmField;

    @FXML
    private Button btnTogglePassword;

    @FXML
    private Button btnTogglePasswordConfirm;

    @FXML
    private Label eyeIcon;

    @FXML
    private Label eyeSlashIcon;

    @FXML
    private Label eyeIconConfirm;

    @FXML
    private Label eyeSlashIconConfirm;

    @FXML
    private Label errorEmail;

    @FXML
    private Label errorTelephone;

    @FXML
    private Label errorNom;

    @FXML
    private Label errorPrenom;

    @FXML
    private Label errorDateNaissance;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField telephoneField;

    @FXML
    private ComboBox<String> gouvernoratField;

    @FXML
    private DatePicker dateNaissanceField;

    @FXML
    private TextField photoField;

    @FXML
    private Label lblPhotoFileName;

    @FXML
    private Button btnUploadPhoto;

    @FXML
    private ImageView imageViewPhoto;


    private FileUploadService fileUploadService;

    private Societe societe;
    private Mode mode;
    private File uploadedPhotoFile;

    public enum Mode {
        ADD, EDIT
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        // Configure le callback pour revenir à la liste des sociétés
        setOnBack(() -> {
            if (mainController != null) {
                mainController.showSocieteManagement();
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        societeService = new SocieteService();
        userService = new UserService();
        
        // Initialize user gouvernorat combo box if it exists
        if (fieldUserGouvernorat != null) {
            fieldUserGouvernorat.getItems().addAll(Gouvernorat.values());
        }
        
        // Always show new user panel if it exists
        if (panelNewUser != null) {
            panelNewUser.setVisible(true);
            panelNewUser.setManaged(true);
        }
        
        fileUploadService = new FileUploadService();
        
        // Initialize other fields if they exist
        if (gouvernoratField != null) {
            gouvernoratField.setItems(TunisiaGovernorates.asObservableList());
        }
        
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        // Edit mode validation listeners (only if fields exist)
        if (emailField != null) {
            emailField.textProperty().addListener((obs, o, n) -> {
                if (mode == Mode.EDIT) {
                    validateEditEmail();
                }
            });
        }
        if (telephoneField != null) {
            telephoneField.textProperty().addListener((obs, o, n) -> {
                if (mode == Mode.EDIT) {
                    validateEditPhone();
                }
            });
        }
        if (nomField != null) {
            nomField.textProperty().addListener((obs, o, n) -> {
                if (mode == Mode.EDIT) {
                    validateEditNom();
                }
            });
        }
        if (prenomField != null) {
            prenomField.textProperty().addListener((obs, o, n) -> {
                if (mode == Mode.EDIT) {
                    validateEditPrenom();
                }
            });
        }
        if (dateNaissanceField != null) {
            dateNaissanceField.valueProperty().addListener((obs, o, n) -> {
                if (mode == Mode.EDIT) {
                    validateEditBirthDate();
                }
            });
        }
        
        // Nom société validation
        if (fieldNomSociete != null) {
            fieldNomSociete.textProperty().addListener((obs, oldVal, newVal) -> {
                validateNomSociete();
            });
        }
        
        // Secteur validation
        if (fieldSecteur != null) {
            fieldSecteur.textProperty().addListener((obs, oldVal, newVal) -> {
                validateSecteur();
            });
        }
        
        // Description validation
        if (fieldDescription != null) {
            fieldDescription.textProperty().addListener((obs, oldVal, newVal) -> {
                validateDescription();
            });
        }
        
        // Adresse validation
        if (fieldAdresse != null) {
            fieldAdresse.textProperty().addListener((obs, oldVal, newVal) -> {
                validateAdresse();
            });
        }
        
        // Site web validation
        if (fieldSiteWeb != null) {
            fieldSiteWeb.textProperty().addListener((obs, oldVal, newVal) -> {
                validateSiteWeb();
            });
        }
        
        // New user validation
        if (fieldUserEmail != null) {
            fieldUserEmail.textProperty().addListener((obs, oldVal, newVal) -> {
                validateUserEmail();
            });
        }
        
        if (fieldUserTelephone != null) {
            fieldUserTelephone.textProperty().addListener((obs, oldVal, newVal) -> {
                validateUserTelephone();
            });
        }
        
        if (fieldUserPassword != null && fieldUserPasswordConfirm != null) {
            fieldUserPassword.textProperty().addListener((obs, oldVal, newVal) -> {
                validateUserPassword();
                if (!fieldUserPasswordConfirm.getText().isEmpty()) {
                    validateUserPasswordConfirm();
                }
            });
            
            fieldUserPasswordConfirm.textProperty().addListener((obs, oldVal, newVal) -> {
                validateUserPasswordConfirm();
            });
        }
        
        if (fieldUserNom != null) {
            fieldUserNom.textProperty().addListener((obs, oldVal, newVal) -> {
                validateUserNom();
            });
        }
        
        if (fieldUserPrenom != null) {
            fieldUserPrenom.textProperty().addListener((obs, oldVal, newVal) -> {
                validateUserPrenom();
            });
        }
        
        if (fieldUserDateNaissance != null) {
            fieldUserDateNaissance.valueProperty().addListener((obs, oldVal, newVal) -> {
                validateUserDateNaissance();
            });
        }
    }

    @FXML
    private void onTogglePassword() {
        if (passwordGroup == null || !passwordGroup.isVisible()) {
            return;
        }
        togglePasswordField(passwordField, btnTogglePassword, true);
    }

    @FXML
    private void onTogglePasswordConfirm() {
        if (passwordConfirmGroup == null || !passwordConfirmGroup.isVisible()) {
            return;
        }
        togglePasswordField(passwordConfirmField, btnTogglePasswordConfirm, false);
    }

    private void togglePasswordField(PasswordField targetField, Button toggleButton, boolean primary) {
        HBox parent = (HBox) toggleButton.getParent();
        javafx.scene.control.TextInputControl currentField = null;
        int fieldIndex = -1;

        for (int i = 0; i < parent.getChildren().size(); i++) {
            javafx.scene.Node node = parent.getChildren().get(i);
            if ((node instanceof PasswordField || node instanceof TextField) && !node.equals(toggleButton)) {
                currentField = (javafx.scene.control.TextInputControl) node;
                fieldIndex = i;
                break;
            }
        }

        if (currentField == null) {
            return;
        }

        if (currentField instanceof PasswordField currentPasswordField) {
            TextField visiblePassword = new TextField();
            visiblePassword.setPromptText(currentPasswordField.getPromptText());
            visiblePassword.getStyleClass().addAll(currentPasswordField.getStyleClass());
            visiblePassword.setStyle(currentPasswordField.getStyle());
            visiblePassword.textProperty().bindBidirectional(currentPasswordField.textProperty());

            parent.getChildren().set(fieldIndex, visiblePassword);

            if (primary) {
                eyeIcon.setVisible(false);
                eyeIcon.setManaged(false);
                eyeSlashIcon.setVisible(true);
                eyeSlashIcon.setManaged(true);
            } else {
                eyeIconConfirm.setVisible(false);
                eyeIconConfirm.setManaged(false);
                eyeSlashIconConfirm.setVisible(true);
                eyeSlashIconConfirm.setManaged(true);
            }
        } else if (currentField instanceof TextField visiblePasswordField) {
            PasswordField restore = primary ? passwordField : passwordConfirmField;
            if (restore == null) {
                return;
            }
            visiblePasswordField.textProperty().unbindBidirectional(restore.textProperty());
            restore.setPromptText(visiblePasswordField.getPromptText());
            restore.getStyleClass().setAll(visiblePasswordField.getStyleClass());
            restore.setStyle(visiblePasswordField.getStyle());
            parent.getChildren().set(fieldIndex, restore);

            if (primary) {
                eyeIcon.setVisible(true);
                eyeIcon.setManaged(true);
                eyeSlashIcon.setVisible(false);
                eyeSlashIcon.setManaged(false);
            } else {
                eyeIconConfirm.setVisible(true);
                eyeIconConfirm.setManaged(true);
                eyeSlashIconConfirm.setVisible(false);
                eyeSlashIconConfirm.setManaged(false);
            }
        }
    }

    @FXML
    private void onUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("Tous les fichiers", "*.*");

        fileChooser.getExtensionFilters().addAll(imageFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(imageFilter);

        Stage stage = (Stage) btnUploadPhoto.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            uploadedPhotoFile = selectedFile;
            photoField.setText(selectedFile.getAbsolutePath());
            lblPhotoFileName.setText(selectedFile.getName());

            try {
                Image image = new Image(selectedFile.toURI().toString());
                imageViewPhoto.setImage(image);
            } catch (Exception e) {
                System.err.println("Failed to load image: " + e.getMessage());
            }
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        clearForm();
        
        if (mode == Mode.ADD) {
            lblTitle.setText("Ajouter une société");
            lblSubtitle.setText("Complétez les informations de la société. Un compte utilisateur sera créé automatiquement.");
            btnSave.setText("Ajouter");
            // Hide user panel in ADD mode since user will be created automatically
            if (panelNewUser != null) {
                panelNewUser.setVisible(false);
                panelNewUser.setManaged(false);
            }
        } else {
            lblTitle.setText("Modifier la société");
            lblSubtitle.setText("Modifiez les informations de la société et du contact.");
            btnSave.setText("Mettre à jour");
            // Show user panel in EDIT mode
            if (panelNewUser != null) {
                panelNewUser.setVisible(true);
                panelNewUser.setManaged(true);
            }
        }
    }

    private void setPasswordSectionVisible(boolean visible) {
        if (passwordGroup != null) {
            passwordGroup.setVisible(visible);
            passwordGroup.setManaged(visible);
        }
        if (passwordConfirmGroup != null) {
            passwordConfirmGroup.setVisible(visible);
            passwordConfirmGroup.setManaged(visible);
        }
        if (passwordHintLabel != null) {
            passwordHintLabel.setVisible(visible);
            passwordHintLabel.setManaged(visible);
        }
    }

    public void setSociete(Societe societe) {
        this.societe = societe;
        populateForm();
    }

    private void populateForm() {
        if (societe != null) {
            // Populate company fields
            if (fieldNomSociete != null) fieldNomSociete.setText(societe.getNom_societe() != null ? societe.getNom_societe() : "");
            if (fieldSecteur != null) fieldSecteur.setText(societe.getSecteur() != null ? societe.getSecteur() : "");
            if (fieldDescription != null) fieldDescription.setText(societe.getDescription() != null ? societe.getDescription() : "");
            if (fieldAdresse != null) fieldAdresse.setText(societe.getAdresse() != null ? societe.getAdresse() : "");
            if (fieldSiteWeb != null) fieldSiteWeb.setText(societe.getSite_web() != null ? societe.getSite_web() : "");
            
            // Populate user fields if editing and user exists
            if (mode == Mode.EDIT && societe.getUser() != null) {
                User user = societe.getUser();
                if (fieldUserEmail != null) fieldUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                if (fieldUserTelephone != null) fieldUserTelephone.setText(user.getTelephone() != null ? user.getTelephone() : "");
                if (fieldUserNom != null) fieldUserNom.setText(user.getNom() != null ? user.getNom() : "");
                if (fieldUserPrenom != null) fieldUserPrenom.setText(user.getPrenom() != null ? user.getPrenom() : "");
                
                if (user.getGouvernorat() != null && fieldUserGouvernorat != null) {
                    Gouvernorat gouvernorat = Gouvernorat.fromDisplayName(user.getGouvernorat());
                    fieldUserGouvernorat.setValue(gouvernorat);
                }
                
                if (user.getDate_naissance() != null && fieldUserDateNaissance != null) {
                    fieldUserDateNaissance.setValue(user.getDate_naissance().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate());
                }
                
                // Clear password fields in edit mode (optional update)
                if (fieldUserPassword != null) fieldUserPassword.clear();
                if (fieldUserPasswordConfirm != null) fieldUserPasswordConfirm.clear();
            }
            
            // Populate edit mode fields if they exist
            if (societe.getUser() != null) {
                User user = societe.getUser();
                if (emailField != null) emailField.setText(user.getEmail() != null ? user.getEmail() : "");
                if (nomField != null) nomField.setText(user.getNom() != null ? user.getNom() : "");
                if (prenomField != null) prenomField.setText(user.getPrenom() != null ? user.getPrenom() : "");
                if (telephoneField != null) telephoneField.setText(user.getTelephone() != null ? user.getTelephone() : "");
                if (gouvernoratField != null) {
                    String govString = user.getGouvernorat();
                    if (govString != null) {
                        Gouvernorat govEnum = Gouvernorat.fromDisplayName(govString);
                        gouvernoratField.setValue(String.valueOf(govEnum));
                    }
                }
                if (photoField != null) photoField.setText(user.getPhoto() != null ? user.getPhoto() : "");
                if (lblPhotoFileName != null && user.getPhoto() != null && !user.getPhoto().isEmpty()) {
                    lblPhotoFileName.setText(stripToFileName(user.getPhoto()));
                }

                if (user.getDate_naissance() != null && dateNaissanceField != null) {
                    dateNaissanceField.setValue(user.getDate_naissance().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                }

                if (user.getPhoto() != null && !user.getPhoto().isEmpty() && imageViewPhoto != null) {
                    try {
                        Image image = new Image(user.getPhoto());
                        imageViewPhoto.setImage(image);
                    } catch (Exception e) {
                        System.err.println("Failed to load image: " + e.getMessage());
                    }
                }
            }
        }
    }

    private static String stripToFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "Aucune photo sélectionnée";
        }
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private void clearForm() {
        // Clear company fields
        if (fieldNomSociete != null) fieldNomSociete.clear();
        if (fieldSecteur != null) fieldSecteur.clear();
        if (fieldDescription != null) fieldDescription.clear();
        if (fieldAdresse != null) fieldAdresse.clear();
        if (fieldSiteWeb != null) fieldSiteWeb.clear();
        
        // Clear user fields
        if (fieldUserEmail != null) fieldUserEmail.clear();
        if (fieldUserTelephone != null) fieldUserTelephone.clear();
        if (fieldUserPassword != null) fieldUserPassword.clear();
        if (fieldUserPasswordConfirm != null) fieldUserPasswordConfirm.clear();
        if (fieldUserNom != null) fieldUserNom.clear();
        if (fieldUserPrenom != null) fieldUserPrenom.clear();
        if (fieldUserGouvernorat != null) fieldUserGouvernorat.setValue(null);
        if (fieldUserDateNaissance != null) fieldUserDateNaissance.setValue(null);
        
        clearAllErrors();
        
        // Clear edit mode fields (if they exist)
        if (emailField != null) emailField.clear();
        if (passwordField != null) passwordField.clear();
        if (passwordConfirmField != null) passwordConfirmField.clear();
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        if (telephoneField != null) telephoneField.clear();
        if (gouvernoratField != null) gouvernoratField.setValue(null);
        if (dateNaissanceField != null) dateNaissanceField.setValue(null);
        if (photoField != null) photoField.clear();
        if (lblPhotoFileName != null) lblPhotoFileName.setText("Aucune photo sélectionnée");
        if (imageViewPhoto != null) imageViewPhoto.setImage(null);
        uploadedPhotoFile = null;
    }

    @FXML
    private void onSubmit() {
        hideMessage();

        // Validate all fields first
        if (!validateAllFields()) {
            showMessage("Veuillez corriger les erreurs dans le formulaire.");
            return;
        }

        try {
            // Get company information
            String nomSociete = (fieldNomSociete != null) ? fieldNomSociete.getText().trim() : "";
            String secteur = (fieldSecteur != null) ? fieldSecteur.getText().trim() : "";
            String description = (fieldDescription != null) ? fieldDescription.getText().trim() : "";
            String adresse = (fieldAdresse != null) ? fieldAdresse.getText().trim() : "";
            String siteWeb = (fieldSiteWeb != null) ? fieldSiteWeb.getText().trim() : "";
            
            // Normalize website URL
            if (!siteWeb.isEmpty() && !siteWeb.startsWith("http")) {
                siteWeb = "https://" + siteWeb;
            }
            
            User userToUse;

            if (mode == Mode.EDIT && societe != null && societe.getUser() != null) {
                // EDIT MODE - Use existing user fields
                String email = (emailField != null) ? emailField.getText().trim() : "";
                String password = (passwordField != null) ? passwordField.getText() : "";
                String phoneNorm = (telephoneField != null) ? SignupFieldValidation.normalizePhone(telephoneField.getText()) : "";
                String nom = (nomField != null) ? nomField.getText().trim() : "";
                String prenom = (prenomField != null) ? prenomField.getText().trim() : "";
                String gouvernoratString = (gouvernoratField != null) ? gouvernoratField.getValue() : null;
                LocalDate dateNaissance = (dateNaissanceField != null) ? dateNaissanceField.getValue() : null;
                User dbUser = userService.getUserByEmail(societe.getUser().getEmail());
                if (dbUser == null) {
                    dbUser = userService.findById(societe.getUser().getId());
                }
                if (dbUser == null) {
                    showAlert("Erreur", "Utilisateur introuvable.", Alert.AlertType.ERROR);
                    return;
                }
                if (email.isEmpty()) {
                    showAlert("Erreur de validation", "L'email est obligatoire", Alert.AlertType.ERROR);
                    return;
                }
                if (userService.emailExists(email) && !email.equalsIgnoreCase(dbUser.getEmail())) {
                    showAlert("Erreur de validation", "Cet email existe déjà", Alert.AlertType.ERROR);
                    return;
                }
                dbUser.setEmail(email);
                dbUser.setNom(nom);
                dbUser.setPrenom(prenom);
                dbUser.setTelephone(phoneNorm);
                dbUser.setGouvernorat(gouvernoratString);
                if (dateNaissance != null) {
                    dbUser.setDate_naissance(java.sql.Date.valueOf(dateNaissance));
                }
                String photoPathEdit = (photoField != null) ? photoField.getText().trim() : "";
                if (uploadedPhotoFile != null) {
                    photoPathEdit = fileUploadService.uploadPhoto(uploadedPhotoFile);
                }
                dbUser.setPhoto(photoPathEdit.isEmpty() ? null : photoPathEdit);
                userService.modifier(dbUser);
                userToUse = dbUser;
            } else {
                // ADD MODE - Create user from form input (like signup)
                String email = (fieldUserEmail != null) ? fieldUserEmail.getText().trim() : "";
                String password = (fieldUserPassword != null) ? fieldUserPassword.getText() : "";
                String phoneNorm = (fieldUserTelephone != null) ? SignupFieldValidation.normalizePhone(fieldUserTelephone.getText()) : "";
                String nom = (fieldUserNom != null) ? fieldUserNom.getText().trim() : "";
                String prenom = (fieldUserPrenom != null) ? fieldUserPrenom.getText().trim() : "";
                String gouvernoratString = (fieldUserGouvernorat != null && fieldUserGouvernorat.getValue() != null) ? fieldUserGouvernorat.getValue().toString() : null;
                LocalDate dateNaissance = (fieldUserDateNaissance != null) ? fieldUserDateNaissance.getValue() : null;

                // Validate email uniqueness
                if (userService.emailExists(email)) {
                    showError(errorUserEmail, "Cet email existe déjà");
                    return;
                }

                User newUser = new User();
                newUser.setEmail(email);
                newUser.setPassword(password);
                newUser.setNom(nom);
                newUser.setPrenom(prenom);
                newUser.setTelephone(phoneNorm);
                newUser.setGouvernorat(gouvernoratString);
                newUser.setRole_utilisateur("societe");
                newUser.setRoles("[\"ROLE_SOCIETE\"]");
                newUser.setIs_email_verified(false); // Email not verified by default

                if (dateNaissance != null) {
                    newUser.setDate_naissance(java.sql.Date.valueOf(dateNaissance));
                }

                userService.ajouter(newUser);
                userToUse = newUser;
            }

            boolean isNew = (mode == Mode.ADD);
            if (isNew) {
                societe = new Societe();
            }

            societe.setNom_societe(fieldNomSociete.getText().trim());
            societe.setSecteur(fieldSecteur.getText().trim().isEmpty() ? null : fieldSecteur.getText().trim());
            societe.setDescription(fieldDescription.getText().trim().isEmpty() ? null : fieldDescription.getText().trim());
            societe.setAdresse(fieldAdresse.getText().trim().isEmpty() ? null : fieldAdresse.getText().trim());

            String siteWebText = fieldSiteWeb.getText().trim();
            if (!siteWebText.isEmpty() && !siteWebText.startsWith("http")) {
                siteWebText = "https://" + siteWebText;
            }
            societe.setSite_web(siteWebText.isEmpty() ? null : siteWebText);

            // Set user before validation
            societe.setUser(userToUse);
            
            // Validate entity
            societe.valider();

            if (isNew) {
                societeService.ajouter(societe);
                showAlert("Société ajoutée avec succès", "La société a été créée avec succès.", Alert.AlertType.INFORMATION);
                // Return to list after successful add
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(1000);
                        if (onBack != null) {
                            onBack.run();
                        } else {
                            closeForm();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        if (onBack != null) {
                            onBack.run();
                        } else {
                            closeForm();
                        }
                    }
                });
            } else {
                societeService.modifier(societe);
                showMessage("Société modifiée avec succès.");
                // Return to list after successful edit
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(1500);
                        if (onBack != null) {
                            onBack.run();
                        } else {
                            closeForm();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        if (onBack != null) {
                            onBack.run();
                        } else {
                            closeForm();
                        }
                    }
                });
            }
        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage() != null ? ex.getMessage() : "Données invalides.");
        } catch (IllegalStateException ex) {
            showMessage(ex.getMessage() != null ? ex.getMessage() : "Impossible de finaliser l'opération.");
        } catch (Exception ex) {
            showMessage("Erreur lors de l'enregistrement: " + ex.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        if (onBack != null) {
            onBack.run();
        } else {
            closeForm();
        }
    }

    @FXML
    private void handleSaveButton() {
        onSubmit();
    }

    @FXML
    private void handleCancelButton() {
        onCancel();
    }

    @FXML
    private void goBack() {
        if (onBack != null) {
            onBack.run();
            return;
        }
        closeForm();
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void showMessage(String text) {
        lblMessage.setText(text);
        if (!lblMessage.getStyleClass().contains("signup-alert")) {
            lblMessage.getStyleClass().add("signup-alert");
        }
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
        Platform.runLater(() -> {
            lblMessage.requestLayout();
            scrollToMessageIfNeeded();
        });
    }

    private void hideMessage() {
        lblMessage.setText("");
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }

    private void scrollToMessageIfNeeded() {
        javafx.scene.Parent parent = lblMessage.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane) {
                ((ScrollPane) parent).setVvalue(0);
                return;
            }
            parent = parent.getParent();
        }
    }

    @FXML
    private void onToggleUserPassword() {
        toggleUserPasswordField(fieldUserPassword, btnToggleUserPassword);
    }

    @FXML
    private void onToggleUserPasswordConfirm() {
        toggleUserPasswordField(fieldUserPasswordConfirm, btnToggleUserPasswordConfirm);
    }

    private void toggleUserPasswordField(javafx.scene.control.PasswordField passwordField, Button toggleButton) {
        HBox parent = (HBox) toggleButton.getParent();
        
        // Find current password field (either PasswordField or TextField)
        javafx.scene.control.TextInputControl currentField = null;
        int fieldIndex = -1;
        
        for (int i = 0; i < parent.getChildren().size(); i++) {
            javafx.scene.Node node = parent.getChildren().get(i);
            if ((node instanceof PasswordField || node instanceof TextField) && !node.equals(toggleButton)) {
                currentField = (javafx.scene.control.TextInputControl) node;
                fieldIndex = i;
                break;
            }
        }
        
        if (currentField == null) return;
        
        if (currentField instanceof PasswordField currentPasswordField) {
            // Show plain text while keeping bidirectional sync with injected field.
            TextField visiblePassword = new TextField();
            visiblePassword.setPromptText(currentPasswordField.getPromptText());
            visiblePassword.getStyleClass().addAll(currentPasswordField.getStyleClass());
            visiblePassword.setStyle(currentPasswordField.getStyle());
            visiblePassword.textProperty().bindBidirectional(currentPasswordField.textProperty());

            parent.getChildren().set(fieldIndex, visiblePassword);

            if (toggleButton == btnToggleUserPassword) {
                eyeIconUser.setVisible(false);
                eyeIconUser.setManaged(false);
                eyeSlashIconUser.setVisible(true);
                eyeSlashIconUser.setManaged(true);
            } else if (toggleButton == btnToggleUserPasswordConfirm) {
                eyeIconUserConfirm.setVisible(false);
                eyeIconUserConfirm.setManaged(false);
                eyeSlashIconUserConfirm.setVisible(true);
                eyeSlashIconUserConfirm.setManaged(true);
            }
        } else if (currentField instanceof TextField visiblePasswordField) {
            // Restore the original injected PasswordField to keep listeners/validation stable.
            PasswordField targetField = toggleButton == btnToggleUserPassword ? fieldUserPassword : fieldUserPasswordConfirm;
            if (targetField == null) {
                return;
            }

            visiblePasswordField.textProperty().unbindBidirectional(targetField.textProperty());
            targetField.setPromptText(visiblePasswordField.getPromptText());
            targetField.getStyleClass().setAll(visiblePasswordField.getStyleClass());
            targetField.setStyle(visiblePasswordField.getStyle());
            parent.getChildren().set(fieldIndex, targetField);

            if (toggleButton == btnToggleUserPassword) {
                eyeIconUser.setVisible(true);
                eyeIconUser.setManaged(true);
                eyeSlashIconUser.setVisible(false);
                eyeSlashIconUser.setManaged(false);
            } else if (toggleButton == btnToggleUserPasswordConfirm) {
                eyeIconUserConfirm.setVisible(true);
                eyeIconUserConfirm.setManaged(true);
                eyeSlashIconUserConfirm.setVisible(false);
                eyeSlashIconUserConfirm.setManaged(false);
            }
        }
    }
    
    private boolean validateNomSociete() {
        if (fieldNomSociete == null) {
            return true; // Skip validation if field doesn't exist
        }
        String nom = fieldNomSociete.getText().trim();
        if (nom.isEmpty()) {
            showError(errorNomSociete, "Le nom de la société est obligatoire");
            return false;
        }
        
        if (nom.length() < 2) {
            showError(errorNomSociete, "Minimum 2 caractères");
            return false;
        }
        
        if (nom.length() > 255) {
            showError(errorNomSociete, "Maximum 255 caractères");
            return false;
        }
        
        hideError(errorNomSociete);
        return true;
    }
    
    private boolean validateSecteur() {
        if (fieldSecteur == null) {
            return true; // Skip validation if field doesn't exist
        }
        String secteur = fieldSecteur.getText().trim();
        if (!secteur.isEmpty() && secteur.length() > 100) {
            showError(errorSecteur, "Maximum 100 caractères");
            return false;
        }
        
        hideError(errorSecteur);
        return true;
    }
    
    private boolean validateDescription() {
        if (fieldDescription == null) {
            return true; // Skip validation if field doesn't exist
        }
        String description = fieldDescription.getText().trim();
        if (!description.isEmpty() && description.length() > 2000) {
            showError(errorDescription, "Maximum 2000 caractères");
            return false;
        }
        
        hideError(errorDescription);
        return true;
    }
    
    private boolean validateAdresse() {
        if (fieldAdresse == null) {
            return true; // Skip validation if field doesn't exist
        }
        String adresse = fieldAdresse.getText().trim();
        if (!adresse.isEmpty() && adresse.length() > 500) {
            showError(errorAdresse, "Maximum 500 caractères");
            return false;
        }
        
        hideError(errorAdresse);
        return true;
    }
    
    private boolean validateSiteWeb() {
        if (fieldSiteWeb == null) {
            return true; // Skip validation if field doesn't exist
        }
        String siteWeb = fieldSiteWeb.getText().trim();
        
        // Website is optional, so if empty, it's valid
        if (siteWeb.isEmpty()) {
            hideError(errorSiteWeb);
            return true;
        }
        
        // Validate URL format when website is provided
        if (!siteWeb.startsWith("http://") && !siteWeb.startsWith("https://")) {
            showError(errorSiteWeb, "Doit commencer par http:// ou https://");
            return false;
        }
        
        if (siteWeb.length() > 500) {
            showError(errorSiteWeb, "Maximum 500 caractères");
            return false;
        }
        
        // Basic URL validation
        String urlRegex = "^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$";
        if (!Pattern.matches(urlRegex, siteWeb)) {
            showError(errorSiteWeb, "Format d'URL invalide");
            return false;
        }
        
        hideError(errorSiteWeb);
        return true;
    }
    
        
    private void clearAllErrors() {
        hideError(errorNomSociete);
        hideError(errorSecteur);
        hideError(errorDescription);
        hideError(errorAdresse);
        hideError(errorSiteWeb);
        hideError(errorUserEmail);
        hideError(errorUserTelephone);
        hideError(errorUserPassword);
        hideError(errorUserPasswordConfirm);
        hideError(errorUserNom);
        hideError(errorUserPrenom);
        hideError(errorUserDateNaissance);
    }
    
    private boolean validateAllFields() {
        clearAllErrors();
        
        boolean isValid = true;
        
        // Validate societe fields
        isValid &= validateNomSociete();
        isValid &= validateSecteur();
        isValid &= validateDescription();
        isValid &= validateAdresse();
        isValid &= validateSiteWeb();
        
        // Only validate user fields in EDIT mode
        if (mode == Mode.EDIT) {
            isValid &= validateUserEmail();
            isValid &= validateUserTelephone();
            isValid &= validateUserNom();
            isValid &= validateUserPrenom();
            isValid &= validateUserDateNaissance();
            
            // Password validation - only required if password is provided in EDIT mode
            if (!fieldUserPassword.getText().isEmpty() || !fieldUserPasswordConfirm.getText().isEmpty()) {
                isValid &= validateUserPassword();
                isValid &= validateUserPasswordConfirm();
            }
        }
        
        return isValid;
    }
    
    /** Retire espaces / séparateurs ; conserve un + initial. Doit matcher ^\\+?[0-9]{8,12}$ après nettoyage. */
    private static String normalizePhone(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return null;
        }
        boolean plus = t.startsWith("+");
        String digits = t.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return plus ? "+" : "";
        }
        return plus ? "+" + digits : digits;
    }
    
    private String generateEmailFromCompany(String nomSociete) {
        if (nomSociete == null || nomSociete.trim().isEmpty()) {
            return "contact@formini.com";
        }
        
        // Normalize company name for email
        String normalized = nomSociete.trim()
            .toLowerCase()
            .replaceAll("[^a-zA-Z0-9]", "")
            .replaceAll("\\s+", "");
        
        if (normalized.isEmpty()) {
            return "contact@formini.com";
        }
        
        return normalized + "@formini.com";
    }
    
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        
        // Generate 12 character password
        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
    
    // User validation methods
    private boolean validateUserEmail() {
        if (fieldUserEmail == null) {
            return true; // Skip validation if field doesn't exist
        }
        String email = fieldUserEmail.getText().trim();
        if (email.isEmpty()) {
            showError(errorUserEmail, "L'email est obligatoire");
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        if (!Pattern.matches(emailRegex, email)) {
            showError(errorUserEmail, "Format d'email invalide");
            return false;
        }
        
        hideError(errorUserEmail);
        return true;
    }
    
    private boolean validateUserTelephone() {
        if (fieldUserTelephone == null) {
            return true; // Skip validation if field doesn't exist
        }
        String telephone = fieldUserTelephone.getText().trim();
        if (telephone.isEmpty()) {
            showError(errorUserTelephone, "Le téléphone est obligatoire");
            return false;
        }
        
        String normalized = normalizePhone(telephone);
        if (normalized == null || !normalized.matches("\\+?[0-9]{8,12}$")) {
            showError(errorUserTelephone, "Format invalide: 8-12 chiffres");
            return false;
        }
        
        hideError(errorUserTelephone);
        return true;
    }
    
    private boolean validateUserPassword() {
        if (fieldUserPassword == null) {
            return true; // Skip validation if field doesn't exist
        }
        String password = fieldUserPassword.getText();
        if (password.isEmpty()) {
            // In EDIT mode, password is optional
            if (mode == Mode.EDIT) {
                hideError(errorUserPassword);
                return true;
            } else {
                showError(errorUserPassword, "Le mot de passe est obligatoire");
                return false;
            }
        }
        
        if (password.length() < 8) {
            showError(errorUserPassword, "Minimum 8 caractères");
            return false;
        }
        
        if (!password.matches(".*[A-Z].*")) {
            showError(errorUserPassword, "Une majuscule requise");
            return false;
        }
        
        if (!password.matches(".*[a-z].*")) {
            showError(errorUserPassword, "Une minuscule requise");
            return false;
        }
        
        if (!password.matches(".*\\d.*")) {
            showError(errorUserPassword, "Un chiffre requis");
            return false;
        }
        
        hideError(errorUserPassword);
        return true;
    }
    
    private boolean validateUserPasswordConfirm() {
        if (fieldUserPassword == null || fieldUserPasswordConfirm == null) {
            return true; // Skip validation if fields don't exist
        }
        String password = fieldUserPassword.getText();
        String passwordConfirm = fieldUserPasswordConfirm.getText();
        
        if (passwordConfirm.isEmpty()) {
            // In EDIT mode, password confirmation is optional if password is also empty
            if (mode == Mode.EDIT && password.isEmpty()) {
                hideError(errorUserPasswordConfirm);
                return true;
            } else {
                showError(errorUserPasswordConfirm, "La confirmation est obligatoire");
                return false;
            }
        }
        
        if (!password.equals(passwordConfirm)) {
            showError(errorUserPasswordConfirm, "Les mots de passe ne correspondent pas");
            return false;
        }
        
        hideError(errorUserPasswordConfirm);
        return true;
    }
    
    private boolean validateUserNom() {
        if (fieldUserNom == null) {
            return true; // Skip validation if field doesn't exist
        }
        String nom = fieldUserNom.getText().trim();
        if (nom.isEmpty()) {
            showError(errorUserNom, "Le nom est obligatoire");
            return false;
        }
        
        if (nom.length() < 2) {
            showError(errorUserNom, "Minimum 2 caractères");
            return false;
        }
        
        hideError(errorUserNom);
        return true;
    }
    
    private boolean validateUserPrenom() {
        if (fieldUserPrenom == null) {
            return true; // Skip validation if field doesn't exist
        }
        String prenom = fieldUserPrenom.getText().trim();
        if (prenom.isEmpty()) {
            showError(errorUserPrenom, "Le prénom est obligatoire");
            return false;
        }
        
        if (prenom.length() < 2) {
            showError(errorUserPrenom, "Minimum 2 caractères");
            return false;
        }
        
        hideError(errorUserPrenom);
        return true;
    }
    
    private boolean validateUserDateNaissance() {
        if (fieldUserDateNaissance == null) {
            return true; // Skip validation if field doesn't exist
        }
        LocalDate date = fieldUserDateNaissance.getValue();
        if (date == null) {
            showError(errorUserDateNaissance, "La date de naissance est obligatoire");
            return false;
        }
        
        if (date.isAfter(LocalDate.now())) {
            showError(errorUserDateNaissance, "Date invalide");
            return false;
        }
        
        if (date.isBefore(LocalDate.now().minusYears(120))) {
            showError(errorUserDateNaissance, "Date invalide");
            return false;
        }
        
        hideError(errorUserDateNaissance);
        return true;
    }


    private boolean validateEditEmail() {
        if (!SignupFieldValidation.isValidEmail(emailField.getText().trim())) {
            showError(errorEmail, "Email invalide ou manquant.");
            return false;
        }
        hideError(errorEmail);
        return true;
    }

    private boolean validateEditPhone() {
        String phoneNorm = SignupFieldValidation.normalizePhone(telephoneField.getText());
        if (!SignupFieldValidation.isValidPhoneNormalized(phoneNorm)) {
            showError(errorTelephone, "Téléphone invalide (8-12 chiffres).");
            return false;
        }
        hideError(errorTelephone);
        return true;
    }

    private boolean validateEditNom() {
        if (!SignupFieldValidation.isValidNomPrenom(nomField.getText())) {
            showError(errorNom, "Le nom est obligatoire (min. 2 caractères).");
            return false;
        }
        hideError(errorNom);
        return true;
    }

    private boolean validateEditPrenom() {
        if (!SignupFieldValidation.isValidNomPrenom(prenomField.getText())) {
            showError(errorPrenom, "Le prénom est obligatoire (min. 2 caractères).");
            return false;
        }
        hideError(errorPrenom);
        return true;
    }

    private boolean validateEditBirthDate() {
        if (dateNaissanceField.getValue() == null) {
            showError(errorDateNaissance, "La date de naissance est obligatoire.");
            return false;
        }
        hideError(errorDateNaissance);
        return true;
    }

    private void clearEditErrors() {
        hideError(errorEmail);
        hideError(errorTelephone);
        hideError(errorNom);
        hideError(errorPrenom);
        hideError(errorDateNaissance);
    }

    private void showError(Label label, String msg) {
        if (label == null) {
            return;
        }
        label.setText(msg);
        label.setStyle("-fx-text-fill: #dc2626;");
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        if (label == null) {
            return;
        }
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeForm() {
        if (btnCancel.getScene() != null && btnCancel.getScene().getWindow() != null) {
            btnCancel.getScene().getWindow().hide();
        }
    }

    @FXML
    private void onSignupWithGoogle() {
        if (!tn.formini.services.auth.OAuthService.isConfigured("google")) {
            showMessage("OAuth Google n'est pas configuré. Veuillez contacter l'administrateur.");
            return;
        }

        new Thread(() -> {
            try {
                tn.formini.services.auth.OAuthCallbackHandler handler = new tn.formini.services.auth.OAuthCallbackHandler();
                User user = handler.authenticateWithGoogle();

                Platform.runLater(() -> {
                    if (user != null) {
                        // Set user information from OAuth
                        if (fieldUserEmail != null) fieldUserEmail.setText(user.getEmail());
                        if (fieldUserNom != null && user.getNom() != null) fieldUserNom.setText(user.getNom());
                        if (fieldUserPrenom != null && user.getPrenom() != null) fieldUserPrenom.setText(user.getPrenom());
                        
                        showAlert("Connexion réussie avec Google !", "Veuillez compléter les informations de la société.", Alert.AlertType.INFORMATION);
                    } else {
                        showMessage("L'inscription avec Google a échoué. Veuillez réessayer.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showMessage("Erreur lors de l'inscription avec Google: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void onSignupWithGithub() {
        if (!tn.formini.services.auth.OAuthService.isConfigured("github")) {
            showMessage("OAuth GitHub n'est pas configuré. Veuillez contacter l'administrateur.");
            return;
        }

        new Thread(() -> {
            try {
                tn.formini.services.auth.OAuthCallbackHandler handler = new tn.formini.services.auth.OAuthCallbackHandler();
                User user = handler.authenticateWithGithub();

                Platform.runLater(() -> {
                    if (user != null) {
                        // Set user information from OAuth
                        if (fieldUserEmail != null) fieldUserEmail.setText(user.getEmail());
                        if (fieldUserNom != null && user.getNom() != null) fieldUserNom.setText(user.getNom());
                        if (fieldUserPrenom != null && user.getPrenom() != null) fieldUserPrenom.setText(user.getPrenom());
                        
                        showAlert("Connexion réussie avec GitHub !", "Veuillez compléter les informations de la société.", Alert.AlertType.INFORMATION);
                    } else {
                        showMessage("L'inscription avec GitHub a échoué. Veuillez réessayer.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showMessage("Erreur lors de l'inscription avec GitHub: " + e.getMessage());
                });
            }
        }).start();
    }

    private void redirectToLogin() {
        if (lblMessage.getScene() == null) {
            return;
        }
        try {
            URL resource = getClass().getResource("/fxml/auth/Login.fxml");
            if (resource != null) {
                Parent root = FXMLLoader.load(resource);
                lblMessage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onGoToLogin() {
        redirectToLogin();
    }
}
