package tn.formini.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import tn.formini.services.BlogService;
import tn.formini.services.EvenementService;
import tn.formini.services.UserService; // Assuming there's a UserService for participants

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label labelTotalBlogs;
    @FXML private Label labelTotalEvents;
    @FXML private Label labelTotalParticipants;

    private final BlogService blogService = new BlogService();
    private final EvenementService eventService = new EvenementService();
    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadStats();
    }

    private void loadStats() {
        try {
            int blogs = blogService.afficher().size();
            int events = eventService.afficher().size();
            int participants = userService.afficher().size();

            labelTotalBlogs.setText(String.valueOf(blogs));
            labelTotalEvents.setText(String.valueOf(events));
            labelTotalParticipants.setText(String.valueOf(participants));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
