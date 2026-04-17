package tn.formini.controllers.crud;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.UserService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class FormateurCrudController {
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    private TableView<Formateur> tableView;

    @FXML
    private Pagination pagination;
    
    @FXML
    private TableColumn<Formateur, Integer> idColumn;
    
    @FXML
    private TableColumn<Formateur, String> specialiteColumn;
    
    @FXML
    private TableColumn<Formateur, String> bioColumn;
    
    @FXML
    private TableColumn<Formateur, Integer> experienceColumn;
    
    @FXML
    private TableColumn<Formateur, String> linkedinColumn;
    
    @FXML
    private TableColumn<Formateur, Double> noteColumn;
    
    @FXML
    private TableColumn<Formateur, String> userEmailColumn;
    
    @FXML
    private TableColumn<Formateur, String> userNomColumn;
    
    @FXML
    private TableColumn<Formateur, String> portfolioColumn;
    
        
    @FXML
    private Button addButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;

    @FXML
    private Button backButton;

    @FXML
    private Button searchButton;
    
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchScopeComboBox;

    @FXML
    private TextField specialiteFilterField;

    @FXML
    private ComboBox<String> minNoteComboBox;

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortDirectionComboBox;

    @FXML
    private Button resetFiltersButton;

    @FXML
    private Button filterButton;

    @FXML
    private Button sortButton;
    
    @FXML
    private Label countLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label lastUpdateLabel;
    
    private FormateurService formateurService;
    private UserService userService;
    private ObservableList<Formateur> formateurList;
    private ObservableList<Formateur> filteredFormateurList;

    @FXML
    public void initialize() {
        formateurService = new FormateurService();
        userService = new UserService();
        formateurList = FXCollections.observableArrayList();
        filteredFormateurList = FXCollections.observableArrayList();
        
        setupTableColumns();
        setupAdvancedControls();
        setupPagination();
        loadFormateurs();
        updateUI();
        
        tableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                updateButtonStates();
                updateSelectionStatus(newSelection);
            }
        );
        
        updateButtonStates();
    }

    private void setupPagination() {
        pagination.setPageFactory(this::createPage);
    }

    private void setupAdvancedControls() {
        searchScopeComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "ID", "Nom complet", "Email", "Spécialité", "Bio", "LinkedIn", "Portfolio"
        ));
        minNoteComboBox.setItems(FXCollections.observableArrayList(
                "Toutes", "4+", "3+", "2+", "1+"
        ));
        sortByComboBox.setItems(FXCollections.observableArrayList(
                "ID", "Nom complet", "Email", "Spécialité", "Expérience", "Note"
        ));
        sortDirectionComboBox.setItems(FXCollections.observableArrayList(
                "Ascendant", "Descendant"
        ));

        searchScopeComboBox.setValue("Tous");
        minNoteComboBox.setValue("Toutes");
        sortByComboBox.setValue("ID");
        sortDirectionComboBox.setValue("Ascendant");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        specialiteFilterField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        searchScopeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        minNoteComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        sortByComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        sortDirectionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        specialiteColumn.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        bioColumn.setCellValueFactory(new PropertyValueFactory<>("bio"));
        experienceColumn.setCellValueFactory(new PropertyValueFactory<>("experience_annees"));
        linkedinColumn.setCellValueFactory(new PropertyValueFactory<>("linkedin"));
        portfolioColumn.setCellValueFactory(new PropertyValueFactory<>("portfolio"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note_moyenne"));
        
        userEmailColumn.setCellValueFactory(cellData -> {
            Formateur formateur = cellData.getValue();
            return formateur.getUser() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> formateur.getUser().getEmail()) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
        
        userNomColumn.setCellValueFactory(cellData -> {
            Formateur formateur = cellData.getValue();
            return formateur.getUser() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> 
                    formateur.getUser().getPrenom() + " " + formateur.getUser().getNom()
                ) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
    }

    private void loadFormateurs() {
        try {
            statusLabel.setText("Chargement...");
            List<Formateur> formateurs = formateurService.afficher();
            formateurList = FXCollections.observableArrayList(formateurs);
            applyFiltersAndSorting();
            updateUI();
            statusLabel.setText("Prêt");
        } catch (Exception e) {
            statusLabel.setText("Erreur lors du chargement");
            showAlert("Erreur", "Impossible de charger les formateurs: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateUI() {
        // Update count label
        int count = filteredFormateurList.size();
        countLabel.setText("Total: " + count + " formateur" + (count > 1 ? "s" : ""));
        
        // Update last update time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        lastUpdateLabel.setText("Dernière mise à jour: " + now.format(formatter));
    }

    private TableView<Formateur> createPage(int pageIndex) {
        updateTablePage(pageIndex);
        return tableView;
    }

    private void updateTablePage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        if (fromIndex >= filteredFormateurList.size()) {
            tableView.setItems(FXCollections.observableArrayList());
            return;
        }

        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredFormateurList.size());
        tableView.setItems(FXCollections.observableArrayList(filteredFormateurList.subList(fromIndex, toIndex)));
    }

    private void refreshPagination() {
        int pageCount = Math.max(1, (int) Math.ceil((double) filteredFormateurList.size() / ROWS_PER_PAGE));
        pagination.setPageCount(pageCount);

        int currentPage = pagination.getCurrentPageIndex();
        if (currentPage >= pageCount) {
            currentPage = pageCount - 1;
            pagination.setCurrentPageIndex(currentPage);
        }

        updateTablePage(currentPage);
    }

    private void setFilteredFormateurList(List<Formateur> formateurs) {
        filteredFormateurList = FXCollections.observableArrayList(formateurs != null ? formateurs : List.of());
        tableView.getSelectionModel().clearSelection();
        refreshPagination();
    }

    private void applyFiltersAndSorting() {
        String keyword = normalize(searchField.getText());
        String scope = valueOrDefault(searchScopeComboBox.getValue(), "Tous");
        String specialiteFilter = normalize(specialiteFilterField.getText());
        double minNote = parseMinNote(valueOrDefault(minNoteComboBox.getValue(), "Toutes"));

        List<Formateur> results = formateurList.stream()
                .filter(formateur -> matchesKeyword(formateur, keyword, scope))
                .filter(formateur -> specialiteFilter.isEmpty() || containsIgnoreCase(formateur.getSpecialite(), specialiteFilter))
                .filter(formateur -> getSafeNote(formateur) >= minNote)
                .sorted(buildComparator())
                .toList();

        setFilteredFormateurList(results);
        updateUI();
    }

    private Comparator<Formateur> buildComparator() {
        String sortBy = valueOrDefault(sortByComboBox.getValue(), "ID");
        String sortDirection = valueOrDefault(sortDirectionComboBox.getValue(), "Ascendant");

        Comparator<Formateur> comparator;
        switch (sortBy) {
            case "Nom complet":
                comparator = Comparator.comparing(this::getFullName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Email":
                comparator = Comparator.comparing(this::getEmail, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Spécialité":
                comparator = Comparator.comparing(f -> nullSafe(f.getSpecialite()), String.CASE_INSENSITIVE_ORDER);
                break;
            case "Expérience":
                comparator = Comparator.comparingInt(this::getSafeExperience);
                break;
            case "Note":
                comparator = Comparator.comparingDouble(this::getSafeNote);
                break;
            case "ID":
            default:
                comparator = Comparator.comparingInt(Formateur::getId);
                break;
        }

        return "Descendant".equalsIgnoreCase(sortDirection) ? comparator.reversed() : comparator;
    }

    private boolean matchesKeyword(Formateur formateur, String keyword, String scope) {
        if (keyword.isEmpty()) {
            return true;
        }

        switch (scope) {
            case "ID":
                return String.valueOf(formateur.getId()).contains(keyword);
            case "Nom complet":
                return containsIgnoreCase(getFullName(formateur), keyword);
            case "Email":
                return containsIgnoreCase(getEmail(formateur), keyword);
            case "Spécialité":
                return containsIgnoreCase(formateur.getSpecialite(), keyword);
            case "Bio":
                return containsIgnoreCase(formateur.getBio(), keyword);
            case "LinkedIn":
                return containsIgnoreCase(formateur.getLinkedin(), keyword);
            case "Portfolio":
                return containsIgnoreCase(formateur.getPortfolio(), keyword);
            case "Tous":
            default:
                return String.valueOf(formateur.getId()).contains(keyword)
                        || containsIgnoreCase(getFullName(formateur), keyword)
                        || containsIgnoreCase(getEmail(formateur), keyword)
                        || containsIgnoreCase(formateur.getSpecialite(), keyword)
                        || containsIgnoreCase(formateur.getBio(), keyword)
                        || containsIgnoreCase(formateur.getLinkedin(), keyword)
                        || containsIgnoreCase(formateur.getPortfolio(), keyword);
        }
    }

    private double parseMinNote(String minNoteValue) {
        if (minNoteValue == null || "Toutes".equalsIgnoreCase(minNoteValue)) {
            return 0d;
        }
        try {
            return Double.parseDouble(minNoteValue.replace("+", "").trim());
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

    private int getSafeExperience(Formateur formateur) {
        return formateur.getExperience_annees() != null ? formateur.getExperience_annees() : 0;
    }

    private double getSafeNote(Formateur formateur) {
        return formateur.getNote_moyenne() != null ? formateur.getNote_moyenne() : 0d;
    }

    private String getFullName(Formateur formateur) {
        if (formateur.getUser() == null) {
            return "";
        }
        return (nullSafe(formateur.getUser().getPrenom()) + " " + nullSafe(formateur.getUser().getNom())).trim();
    }

    private String getEmail(Formateur formateur) {
        return formateur.getUser() != null ? nullSafe(formateur.getUser().getEmail()) : "";
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return normalize(source).contains(keyword);
    }

    private String normalize(String value) {
        return nullSafe(value).toLowerCase(Locale.ROOT).trim();
    }

    private String nullSafe(String value) {
        return Objects.toString(value, "");
    }

    private String valueOrDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
    
    private void updateSelectionStatus(Formateur selected) {
        if (selected != null) {
            statusLabel.setText("Sélectionné: " + selected.getUser().getPrenom() + " " + selected.getUser().getNom());
        } else {
            updateUI();
        }
    }
    
    @FXML
    private void handleAddButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/formateur-form.fxml"));
            Parent root = loader.load();
            
            FormateurFormController controller = loader.getController();
            controller.setMode(FormateurFormController.Mode.ADD);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Formateur");
            Scene scene = new Scene(root, 760, 720);
            URL css = getClass().getResource("/css/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
            stage.setMinWidth(640);
            stage.setMinHeight(560);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadFormateurs();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewDetailsButton(ActionEvent event) {
        Formateur selectedFormateur = tableView.getSelectionModel().getSelectedItem();
        if (selectedFormateur == null) {
            showAlert("Avertissement", "Veuillez sélectionner un formateur pour voir les détails", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/formateur-details.fxml"));
            Parent root = loader.load();

            FormateurDetailsController controller = loader.getController();
            controller.setFormateur(selectedFormateur);

            Stage stage = new Stage();
            stage.setTitle("Détails du Formateur");
            Scene scene = new Scene(root, 760, 720);
            URL css = getClass().getResource("/css/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
            stage.setMinWidth(640);
            stage.setMinHeight(560);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditButton(ActionEvent event) {
        Formateur selectedFormateur = tableView.getSelectionModel().getSelectedItem();
        if (selectedFormateur == null) {
            showAlert("Avertissement", "Veuillez sélectionner un formateur à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/formateur-form.fxml"));
            Parent root = loader.load();

            FormateurFormController controller = loader.getController();
            controller.setMode(FormateurFormController.Mode.EDIT);
            controller.setFormateur(selectedFormateur);

            Stage stage = new Stage();
            stage.setTitle("Modifier un Formateur");
            Scene scene = new Scene(root, 760, 720);
            URL css = getClass().getResource("/css/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
            stage.setMinWidth(640);
            stage.setMinHeight(560);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadFormateurs();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        Formateur selectedFormateur = tableView.getSelectionModel().getSelectedItem();
        if (selectedFormateur == null) {
            showAlert("Avertissement", "Veuillez sélectionner un formateur à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer ce formateur ?");
        confirmDialog.setContentText("Formateur ID: " + selectedFormateur.getId());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                formateurService.supprimer(selectedFormateur.getId());
                loadFormateurs();
                showAlert("Succès", "Formateur supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadFormateurs();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSorting();
    }

    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        specialiteFilterField.clear();
        searchScopeComboBox.setValue("Tous");
        minNoteComboBox.setValue("Toutes");
        sortByComboBox.setValue("ID");
        sortDirectionComboBox.setValue("Ascendant");
        applyFiltersAndSorting();
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        applyFiltersAndSorting();
        statusLabel.setText("Filtres appliqués");
    }

    @FXML
    private void handleSort(ActionEvent event) {
        applyFiltersAndSorting();
        statusLabel.setText("Tri appliqué");
    }

    private void updateButtonStates() {
        boolean isSelected = tableView.getSelectionModel().getSelectedItem() != null;
        viewDetailsButton.setDisable(!isSelected);
        editButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}
