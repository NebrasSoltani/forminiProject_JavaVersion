package tn.formini.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.formini.services.UsersService.SessionManager;
import tn.formini.utils.AdminInitializer;
import tn.formini.utils.StageWindowMode;

public class DashboardApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        StageWindowMode.installGlobalMaximizedPolicy();

        // Initialize admin user if it doesn't exist
        AdminInitializer.initializeAdmin();
        
        // Check if user is logged in
        SessionManager sessionManager = SessionManager.getInstance();
        
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login if not logged in
            LoginApp loginApp = new LoginApp();
            loginApp.start(primaryStage);
            return;
        }
        
        // Load dashboard
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard/main-dashboard.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        primaryStage.setTitle("Formini - Tableau de Bord");
        primaryStage.setScene(scene);
        StageWindowMode.maximize(primaryStage);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
