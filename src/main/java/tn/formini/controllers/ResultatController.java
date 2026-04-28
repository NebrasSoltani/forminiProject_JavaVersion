package tn.formini.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.formini.entities.ResultatQuiz;
import tn.formini.services.ResultatQuizService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ResultatController implements Initializable {

    @FXML private TableView<ResultatQuiz> tableResultat;
    @FXML private TableColumn<ResultatQuiz, Integer> colId;
    @FXML private TableColumn<ResultatQuiz, Object> colNote;
    @FXML private TableColumn<ResultatQuiz, Integer> colBonnes;
    @FXML private TableColumn<ResultatQuiz, Integer> colTotal;
    @FXML private TableColumn<ResultatQuiz, Boolean> colReussi;
    @FXML private TableColumn<ResultatQuiz, Object> colDate;
    @FXML private TextField searchField;

    private final ResultatQuizService service = new ResultatQuizService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colBonnes.setCellValueFactory(new PropertyValueFactory<>("nombre_bonnes_reponses"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("nombre_total_questions"));
        colReussi.setCellValueFactory(new PropertyValueFactory<>("reussi"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_tentative"));
        chargerDonnees();
    }

    private void chargerDonnees() {
        tableResultat.setItems(FXCollections.observableArrayList(service.getAll()));
    }

    @FXML
    public void filtrer() {
        String texte = searchField.getText().toLowerCase();
        List<ResultatQuiz> filtres = service.getAll().stream()
                .filter(r -> r.getNote().toString().contains(texte)
                        || String.valueOf(r.isReussi()).contains(texte))
                .collect(Collectors.toList());
        tableResultat.setItems(FXCollections.observableArrayList(filtres));
    }
}
