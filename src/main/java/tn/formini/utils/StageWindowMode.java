package tn.formini.utils;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class StageWindowMode {

    private static final String AUTO_MAXIMIZE_KEY = "formini.autoMaximizeInstalled";
    private static final String SKIP_AUTO_MAXIMIZE_KEY = "formini.skipAutoMaximize";
    private static boolean globalPolicyInstalled = false;

    private StageWindowMode() {
    }

    public static void installGlobalMaximizedPolicy() {
        if (globalPolicyInstalled) {
            return;
        }
        globalPolicyInstalled = true;

        Window.getWindows().addListener((ListChangeListener<Window>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Window window : change.getAddedSubList()) {
                        applyMaximizedPolicy(window);
                    }
                }
            }
        });

        // Apply immediately to windows already created before policy installation.
        for (Window window : Window.getWindows()) {
            applyMaximizedPolicy(window);
        }
    }

    public static void maximize(Stage stage) {
        if (stage == null) {
            return;
        }
        if (shouldSkipAutoMaximize(stage)) {
            return;
        }
        stage.setMaximized(true);
    }

    public static void skipAutoMaximize(Stage stage) {
        if (stage == null) {
            return;
        }
        stage.getProperties().put(SKIP_AUTO_MAXIMIZE_KEY, true);
    }

    private static void applyMaximizedPolicy(Window window) {
        if (!(window instanceof Stage stage)) {
            return;
        }

        if (Boolean.TRUE.equals(stage.getProperties().get(AUTO_MAXIMIZE_KEY))) {
            return;
        }
        stage.getProperties().put(AUTO_MAXIMIZE_KEY, true);

        stage.showingProperty().addListener((obs, oldVal, isShowing) -> {
            if (Boolean.TRUE.equals(isShowing)) {
                Platform.runLater(() -> {
                    if (!shouldSkipAutoMaximize(stage)) {
                        stage.setMaximized(true);
                    }
                });
            }
        });

        if (stage.isShowing()) {
            Platform.runLater(() -> {
                if (!shouldSkipAutoMaximize(stage)) {
                    stage.setMaximized(true);
                }
            });
        }
    }

    private static boolean shouldSkipAutoMaximize(Stage stage) {
        return isDialogStage(stage)
                || Boolean.TRUE.equals(stage.getProperties().get(SKIP_AUTO_MAXIMIZE_KEY));
    }

    private static boolean isDialogStage(Stage stage) {
        return stage.getScene() != null && stage.getScene().getRoot() instanceof DialogPane;
    }
}

