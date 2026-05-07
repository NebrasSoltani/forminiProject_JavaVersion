package tn.formini.controllers.dashboard;

import tn.formini.controllers.MainController;
import tn.formini.entities.Users.User;

/**
 * Interface for role-specific dashboard controllers
 */
public interface DashboardRoleController {
    
    /**
     * Initialize the dashboard with user data
     * @param user The current logged-in user
     */
    void initializeDashboard(User user);

    /**
     * Set the main controller to allow navigation
     * @param mainController The main controller
     */
    default void setMainController(MainController mainController) {
        // Default implementation - can be overridden
    }
    
    /**
     * Refresh dashboard data
     */
    default void refreshDashboard() {
        // Default implementation - can be overridden
    }
    
    /**
     * Handle user logout
     */
    default void handleLogout() {
        // Default implementation - can be overridden
    }
}
