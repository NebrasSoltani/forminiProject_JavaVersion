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
import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.SocieteService;
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

public class SocieteCrudController {
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    private TableView<Societe> tableView;

    @FXML
    private Pagination pagination;
    
    @FXML
    private TableColumn<Societe, Integer> idColumn;
    
    @FXML
    private TableColumn<Societe, String> nomColumn;
    
    @FXML
    private TableColumn<Societe, String> secteurColumn;
    
    @FXML
    private TableColumn<Societe, String> descriptionColumn;
    
    @FXML
    private TableColumn<Societe, String> adresseColumn;
    
    @FXML
    private TableColumn<Societe, String> siteWebColumn;
    
    @FXML
    private TableColumn<Societe, String> userEmailColumn;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchScopeComboBox;

    @FXML
    private TextField secteurFilterField;

    @FXML
    private CheckBox hasWebsiteCheckBox;

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortDirectionComboBox;

    @FXML
    private Button resetFiltersButton;
    
    @FXML
    private Label countLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label lastUpdateLabel;
    
    private SocieteService societeService;
    private UserService userService;
    private ObservableList<Societe> societeList = FXCollections.observableArrayList();
    private ObservableList<Societe> filteredSocieteList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        societeService = new SocieteService();
        userService = new UserService();
        
        setupTableColumns();
        setupAdvancedControls();
        setupPagination();
        loadSocietes();
        
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
                "Tous", "ID", "Nom société", "Secteur", "Description", "Adresse", "Site web", "Email"
        ));
        sortByComboBox.setItems(FXCollections.observableArrayList(
                "ID", "Nom société", "Secteur", "Email"
        ));
        sortDirectionComboBox.setItems(FXCollections.observableArrayList(
                "Ascendant", "Descendant"
        ));

        searchScopeComboBox.setValue("Tous");
        sortByComboBox.setValue("ID");
        sortDirectionComboBox.setValue("Ascendant");
        hasWebsiteCheckBox.setSelected(false);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        secteurFilterField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        hasWebsiteCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        searchScopeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        sortByComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        sortDirectionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom_societe"));
        secteurColumn.setCellValueFactory(new PropertyValueFactory<>("secteur"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        siteWebColumn.setCellValueFactory(new PropertyValueFactory<>("site_web"));
        
        userEmailColumn.setCellValueFactory(cellData -> {
            Societe societe = cellData.getValue();
            return societe.getUser() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> societe.getUser().getEmail()) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
    }

    private void loadSocietes() {
        try {
            statusLabel.setText("Chargement...");
            List<Societe> societes = societeService.afficher();
            societeList = FXCollections.observableArrayList(societes);
            applyFiltersAndSorting();
            updateUI();
            statusLabel.setText("Prêt");
        } catch (Exception e) {
            statusLabel.setText("Erreur lors du chargement");
            showAlert("Erreur", "Impossible de charger les societes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateUI() {
        if (societeList == null) {
            societeList = FXCollections.observableArrayList();
        }
        // Update count label
        int count = filteredSocieteList.size();
        countLabel.setText("Total: " + count + " societe" + (count > 1 ? "s" : ""));
        
        // Update last update time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        lastUpdateLabel.setText("Dernière mise à jour: " + now.format(formatter));
    }

    private TableView<Societe> createPage(int pageIndex) {
        updateTablePage(pageIndex);
        return tableView;
    }

    private void updateTablePage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        if (fromIndex >= filteredSocieteList.size()) {
            tableView.setItems(FXCollections.observableArrayList());
            return;
        }

        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredSocieteList.size());
        tableView.setItems(FXCollections.observableArrayList(filteredSocieteList.subList(fromIndex, toIndex)));
    }

    private void refreshPagination() {
        int pageCount = Math.max(1, (int) Math.ceil((double) filteredSocieteList.size() / ROWS_PER_PAGE));
        pagination.setPageCount(pageCount);

        int currentPage = pagination.getCurrentPageIndex();
        if (currentPage >= pageCount) {
            currentPage = pageCount - 1;
            pagination.setCurrentPageIndex(currentPage);
        }

        updateTablePage(currentPage);
    }

    private void setFilteredSocieteList(List<Societe> societes) {
        filteredSocieteList = FXCollections.observableArrayList(societes != null ? societes : List.of());
        tableView.getSelectionModel().clearSelection();
        refreshPagination();
    }

    private void applyFiltersAndSorting() {
        String keyword = normalize(searchField.getText());
        String scope = valueOrDefault(searchScopeComboBox.getValue(), "Tous");
        String secteurFilter = normalize(secteurFilterField.getText());
        boolean onlyWithWebsite = hasWebsiteCheckBox.isSelected();

        List<Societe> results = societeList.stream()
                .filter(societe -> matchesKeyword(societe, keyword, scope))
                .filter(societe -> secteurFilter.isEmpty() || containsIgnoreCase(societe.getSecteur(), secteurFilter))
                .filter(societe -> !onlyWithWebsite || !nullSafe(societe.getSite_web()).isBlank())
                .sorted(buildComparator())
                .toList();

        setFilteredSocieteList(results);
        updateUI();
    }

    private Comparator<Societe> buildComparator() {
        String sortBy = valueOrDefault(sortByComboBox.getValue(), "ID");
        String sortDirection = valueOrDefault(sortDirectionComboBox.getValue(), "Ascendant");

        Comparator<Societe> comparator;
        switch (sortBy) {
            case "Nom société":
                comparator = Comparator.comparing(s -> nullSafe(s.getNom_societe()), String.CASE_INSENSITIVE_ORDER);
                break;
            case "Secteur":
                comparator = Comparator.comparing(s -> nullSafe(s.getSecteur()), String.CASE_INSENSITIVE_ORDER);
                break;
            case "Email":
                comparator = Comparator.comparing(this::getEmail, String.CASE_INSENSITIVE_ORDER);
                break;
            case "ID":
            default:
                comparator = Comparator.comparingInt(Societe::getId);
                break;
        }

        return "Descendant".equalsIgnoreCase(sortDirection) ? comparator.reversed() : comparator;
    }

    private boolean matchesKeyword(Societe societe, String keyword, String scope) {
        if (keyword.isEmpty()) {
            return true;
        }

        switch (scope) {
            case "ID":
                return String.valueOf(societe.getId()).contains(keyword);
            case "Nom société":
                return containsIgnoreCase(societe.getNom_societe(), keyword);
            case "Secteur":
                return containsIgnoreCase(societe.getSecteur(), keyword);
            case "Description":
                return containsIgnoreCase(societe.getDescription(), keyword);
            case "Adresse":
                return containsIgnoreCase(societe.getAdresse(), keyword);
            case "Site web":
                return containsIgnoreCase(societe.getSite_web(), keyword);
            case "Email":
                return containsIgnoreCase(getEmail(societe), keyword);
            case "Tous":
            default:
                return String.valueOf(societe.getId()).contains(keyword)
                        || containsIgnoreCase(societe.getNom_societe(), keyword)
                        || containsIgnoreCase(societe.getSecteur(), keyword)
                        || containsIgnoreCase(societe.getDescription(), keyword)
                        || containsIgnoreCase(societe.getAdresse(), keyword)
                        || containsIgnoreCase(societe.getSite_web(), keyword)
                        || containsIgnoreCase(getEmail(societe), keyword);
        }
    }

    private String getEmail(Societe societe) {
        return societe.getUser() != null ? nullSafe(societe.getUser().getEmail()) : "";
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
    
    private void updateSelectionStatus(Societe selected) {
        if (selected != null) {
            statusLabel.setText("Sélectionné: " + selected.getNom_societe());
        } else {
            updateUI();
        }
    }
    
    @FXML
    private void handleAddButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/societe-form.fxml"));
            Parent root = loader.load();
            
            SocieteFormController controller = loader.getController();
            controller.setMode(SocieteFormController.Mode.ADD);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Société");
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
            
            loadSocietes();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditButton(ActionEvent event) {
        Societe selectedSociete = tableView.getSelectionModel().getSelectedItem();
        if (selectedSociete == null) {
            showAlert("Avertissement", "Veuillez sélectionner une société à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/societe-form.fxml"));
            Parent root = loader.load();
            
            SocieteFormController controller = loader.getController();
            controller.setMode(SocieteFormController.Mode.EDIT);
            controller.setSociete(selectedSociete);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier une Société");
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
            
            loadSocietes();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        Societe selectedSociete = tableView.getSelectionModel().getSelectedItem();
        if (selectedSociete == null) {
            showAlert("Avertissement", "Veuillez sélectionner une société à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cette société ?");
        confirmDialog.setContentText("Société: " + selectedSociete.getNom_societe());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                societeService.supprimer(selectedSociete.getId());
                loadSocietes();
                showAlert("Succès", "Société supprimée avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadSocietes();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSorting();
    }

    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        secteurFilterField.clear();
        hasWebsiteCheckBox.setSelected(false);
        searchScopeComboBox.setValue("Tous");
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
