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
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Domaine;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.UserService;
import tn.formini.services.UsersService.DomaineService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class ApprenantCrudController {
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    private TableView<Apprenant> tableView;

    @FXML
    private Pagination pagination;
    
    @FXML
    private TableColumn<Apprenant, Integer> idColumn;
    
    @FXML
    private TableColumn<Apprenant, String> genreColumn;
    
    @FXML
    private TableColumn<Apprenant, String> etatCivilColumn;
    
    @FXML
    private TableColumn<Apprenant, String> objectifColumn;
    
    @FXML
    private TableColumn<Apprenant, String> userEmailColumn;
    
    @FXML
    private TableColumn<Apprenant, String> domainesColumn;
    
    @FXML
    private TableColumn<Apprenant, String> userNomColumn;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Label countLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label lastUpdateLabel;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchScopeComboBox;

    @FXML
    private ComboBox<String> genreFilterComboBox;

    @FXML
    private ComboBox<String> etatCivilFilterComboBox;

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortDirectionComboBox;

    @FXML
    private Button resetFiltersButton;
    
    private ApprenantService apprenantService;
    private UserService userService;
    private DomaineService domaineService;
    private ObservableList<Apprenant> apprenantList;
    private ObservableList<Apprenant> filteredApprenantList;

    @FXML
    public void initialize() {
        apprenantService = new ApprenantService();
        userService = new UserService();
        domaineService = new DomaineService();
        
        apprenantList = FXCollections.observableArrayList();
        filteredApprenantList = FXCollections.observableArrayList();
        
        setupTableColumns();
        setupAdvancedControls();
        setupPagination();
        loadApprenants();
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
                "Tous", "ID", "Nom complet", "Email", "Genre", "Etat civil", "Objectif", "Domaines"
        ));
        genreFilterComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "homme", "femme", "autre"
        ));
        etatCivilFilterComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "celibataire", "marie", "divorce", "veuf"
        ));
        sortByComboBox.setItems(FXCollections.observableArrayList(
                "ID", "Nom complet", "Email", "Genre", "Etat civil"
        ));
        sortDirectionComboBox.setItems(FXCollections.observableArrayList(
                "Ascendant", "Descendant"
        ));

        searchScopeComboBox.setValue("Tous");
        genreFilterComboBox.setValue("Tous");
        etatCivilFilterComboBox.setValue("Tous");
        sortByComboBox.setValue("ID");
        sortDirectionComboBox.setValue("Ascendant");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        searchScopeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        genreFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        etatCivilFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        sortByComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        sortDirectionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        etatCivilColumn.setCellValueFactory(new PropertyValueFactory<>("etat_civil"));
        objectifColumn.setCellValueFactory(new PropertyValueFactory<>("objectif"));
        
        userEmailColumn.setCellValueFactory(cellData -> {
            Apprenant apprenant = cellData.getValue();
            return apprenant.getUser() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> apprenant.getUser().getEmail()) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
        
        domainesColumn.setCellValueFactory(cellData -> {
            Apprenant apprenant = cellData.getValue();
            return apprenant.getDomaine() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> {
                    String domaines = apprenant.getDomaines_interet();
                    return domaines != null && !domaines.isEmpty() ? domaines : "N/A";
                }) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
        
        userNomColumn.setCellValueFactory(cellData -> {
            Apprenant apprenant = cellData.getValue();
            return apprenant.getUser() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> 
                    apprenant.getUser().getPrenom() + " " + apprenant.getUser().getNom()
                ) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
    }

    private void loadApprenants() {
        try {
            statusLabel.setText("Chargement...");
            List<Apprenant> apprenants = apprenantService.afficher();
            apprenantList = FXCollections.observableArrayList(apprenants);
            applyFiltersAndSorting();
            updateUI();
            statusLabel.setText("Prêt");
        } catch (Exception e) {
            statusLabel.setText("Erreur lors du chargement");
            showAlert("Erreur", "Impossible de charger les apprenants: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateUI() {
        // Update count label
        int count = filteredApprenantList.size();
        countLabel.setText("Total: " + count + " apprenant" + (count > 1 ? "s" : ""));
        
        // Update last update time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        lastUpdateLabel.setText("Dernière mise à jour: " + now.format(formatter));
    }

    private TableView<Apprenant> createPage(int pageIndex) {
        updateTablePage(pageIndex);
        return tableView;
    }

    private void updateTablePage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        if (fromIndex >= filteredApprenantList.size()) {
            tableView.setItems(FXCollections.observableArrayList());
            return;
        }

        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredApprenantList.size());
        tableView.setItems(FXCollections.observableArrayList(filteredApprenantList.subList(fromIndex, toIndex)));
    }

    private void refreshPagination() {
        int pageCount = Math.max(1, (int) Math.ceil((double) filteredApprenantList.size() / ROWS_PER_PAGE));
        pagination.setPageCount(pageCount);

        int currentPage = pagination.getCurrentPageIndex();
        if (currentPage >= pageCount) {
            currentPage = pageCount - 1;
            pagination.setCurrentPageIndex(currentPage);
        }

        updateTablePage(currentPage);
    }

    private void setFilteredApprenantList(List<Apprenant> apprenants) {
        filteredApprenantList = FXCollections.observableArrayList(apprenants != null ? apprenants : List.of());
        tableView.getSelectionModel().clearSelection();
        refreshPagination();
    }

    private void applyFiltersAndSorting() {
        String keyword = normalize(searchField.getText());
        String scope = valueOrDefault(searchScopeComboBox.getValue(), "Tous");
        String genreFilter = valueOrDefault(genreFilterComboBox.getValue(), "Tous");
        String etatCivilFilter = valueOrDefault(etatCivilFilterComboBox.getValue(), "Tous");

        List<Apprenant> results = apprenantList.stream()
                .filter(apprenant -> matchesKeyword(apprenant, keyword, scope))
                .filter(apprenant -> "Tous".equalsIgnoreCase(genreFilter) || genreFilter.equalsIgnoreCase(nullSafe(apprenant.getGenre())))
                .filter(apprenant -> "Tous".equalsIgnoreCase(etatCivilFilter) || etatCivilFilter.equalsIgnoreCase(nullSafe(apprenant.getEtat_civil())))
                .sorted(buildComparator())
                .toList();

        setFilteredApprenantList(results);
        updateUI();
    }

    private Comparator<Apprenant> buildComparator() {
        String sortBy = valueOrDefault(sortByComboBox.getValue(), "ID");
        String sortDirection = valueOrDefault(sortDirectionComboBox.getValue(), "Ascendant");

        Comparator<Apprenant> comparator;
        switch (sortBy) {
            case "Nom complet":
                comparator = Comparator.comparing(this::getFullName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Email":
                comparator = Comparator.comparing(this::getEmail, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Genre":
                comparator = Comparator.comparing(a -> nullSafe(a.getGenre()), String.CASE_INSENSITIVE_ORDER);
                break;
            case "Etat civil":
                comparator = Comparator.comparing(a -> nullSafe(a.getEtat_civil()), String.CASE_INSENSITIVE_ORDER);
                break;
            case "ID":
            default:
                comparator = Comparator.comparingInt(Apprenant::getId);
                break;
        }

        return "Descendant".equalsIgnoreCase(sortDirection) ? comparator.reversed() : comparator;
    }

    private boolean matchesKeyword(Apprenant apprenant, String keyword, String scope) {
        if (keyword.isEmpty()) {
            return true;
        }

        switch (scope) {
            case "ID":
                return String.valueOf(apprenant.getId()).contains(keyword);
            case "Nom complet":
                return containsIgnoreCase(getFullName(apprenant), keyword);
            case "Email":
                return containsIgnoreCase(getEmail(apprenant), keyword);
            case "Genre":
                return containsIgnoreCase(apprenant.getGenre(), keyword);
            case "Etat civil":
                return containsIgnoreCase(apprenant.getEtat_civil(), keyword);
            case "Objectif":
                return containsIgnoreCase(apprenant.getObjectif(), keyword);
            case "Domaines":
                return containsIgnoreCase(apprenant.getDomaines_interet(), keyword);
            case "Tous":
            default:
                return String.valueOf(apprenant.getId()).contains(keyword)
                        || containsIgnoreCase(getFullName(apprenant), keyword)
                        || containsIgnoreCase(getEmail(apprenant), keyword)
                        || containsIgnoreCase(apprenant.getGenre(), keyword)
                        || containsIgnoreCase(apprenant.getEtat_civil(), keyword)
                        || containsIgnoreCase(apprenant.getObjectif(), keyword)
                        || containsIgnoreCase(apprenant.getDomaines_interet(), keyword);
        }
    }

    private String getFullName(Apprenant apprenant) {
        if (apprenant.getUser() == null) {
            return "";
        }
        return (nullSafe(apprenant.getUser().getPrenom()) + " " + nullSafe(apprenant.getUser().getNom())).trim();
    }

    private String getEmail(Apprenant apprenant) {
        return apprenant.getUser() != null ? nullSafe(apprenant.getUser().getEmail()) : "";
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
    
    private void updateSelectionStatus(Apprenant selected) {
        if (selected != null) {
            statusLabel.setText("Sélectionné: " + selected.getUser().getPrenom() + " " + selected.getUser().getNom());
        } else {
            updateUI();
        }
    }

    @FXML
    private void handleAddButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/apprenant-form.fxml"));
            Parent root = loader.load();
            
            ApprenantFormController controller = loader.getController();
            controller.setMode(ApprenantFormController.Mode.ADD);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Apprenant");
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
            
            loadApprenants();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditButton(ActionEvent event) {
        Apprenant selectedApprenant = tableView.getSelectionModel().getSelectedItem();
        if (selectedApprenant == null) {
            showAlert("Avertissement", "Veuillez sélectionner un apprenant à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/apprenant-form.fxml"));
            Parent root = loader.load();
            
            ApprenantFormController controller = loader.getController();
            controller.setMode(ApprenantFormController.Mode.EDIT);
            controller.setApprenant(selectedApprenant);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier un Apprenant");
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
            
            loadApprenants();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        Apprenant selectedApprenant = tableView.getSelectionModel().getSelectedItem();
        if (selectedApprenant == null) {
            showAlert("Avertissement", "Veuillez sélectionner un apprenant à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cet apprenant ?");
        confirmDialog.setContentText("Apprenant ID: " + selectedApprenant.getId());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                apprenantService.supprimer(selectedApprenant.getId());
                loadApprenants();
                showAlert("Succès", "Apprenant supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadApprenants();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSorting();
    }

    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        searchScopeComboBox.setValue("Tous");
        genreFilterComboBox.setValue("Tous");
        etatCivilFilterComboBox.setValue("Tous");
        sortByComboBox.setValue("ID");
        sortDirectionComboBox.setValue("Ascendant");
        applyFiltersAndSorting();
    }

    private void updateButtonStates() {
        boolean isSelected = tableView.getSelectionModel().getSelectedItem() != null;
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
}
