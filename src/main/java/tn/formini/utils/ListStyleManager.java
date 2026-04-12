package tn.formini.utils;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * Utility to standardize TableView styles across the application.
 * Forces constrained resize policy to prevent horizontal scrolling.
 */
public class ListStyleManager {

    /**
     * Applies standard SaaS styling rules to a TableView.
     * @param table The table to style.
     */
    public static void applyStandardStyle(TableView<?> table) {
        if (table == null) return;
        
        // 1. Force columns to fit within the table width (Standardized)
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // 2. Clear old styles and apply modern base
        table.getStyleClass().add("modern-table");
        
        // 3. Prevent focus ring and enable internal sorting
        table.setFocusTraversable(false);
        
        // 4. Default sorting visual (optional, but encouraged)
    }

    /**
     * Installs a tooltip on a specific column to show full text on hover.
     */
    public static <T> void installTooltip(TableColumn<T, String> col) {
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    Tooltip tt = new Tooltip(item);
                    tt.setShowDelay(Duration.millis(500));
                    tt.getStyleClass().add("modern-tooltip");
                    setTooltip(tt);
                }
            }
        });
    }
}
