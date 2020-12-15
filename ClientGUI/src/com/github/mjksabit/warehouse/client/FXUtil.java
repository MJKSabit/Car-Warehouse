package com.github.mjksabit.warehouse.client;

import com.jfoenix.controls.JFXSnackbar;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.github.mjksabit.warehouse.client.controller.Controller;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Utility Methods for JavaFX
 */
public class FXUtil {
    /**
     * Loads an fxml file and provides the instance of the controller
     *
     * @param fxmlName  FXML File name excluding ".fxml" part and FXML must be in the view package
     * @param <T>       T is the Controller which <b>extends @link{Controller}</b>
     * @return          instance of the loaded controller, auto type casted
     * @throws IOException  if unable to load the FXML File
     */
    public static <T extends Controller> T loadFXML(String fxmlName) throws IOException {
        String fxmlPath = "view/" + fxmlName + ".fxml";
        FXMLLoader loader = new FXMLLoader(FXMain.class.getResource(fxmlPath));

        Parent rootNode = loader.load();

        // Fade Transition when changing scene
        FadeTransition ft = new FadeTransition(Duration.millis(1000), rootNode);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        Scene scene = new Scene(rootNode);

        T controller = loader.getController();
        controller.setRootNode(rootNode);

        return controller;
    }

    /**
     * Load a FXML file without controller
     *
     * @param fxmlName  FXML File name excluding ".fxml" part and FXML must be in the view package
     * @return          FXMLLoader for the fxml file
     */
    public static FXMLLoader getFXMLLoader(String fxmlName) {
        String fxmlPath = "view/" + fxmlName + ".fxml";
        return new FXMLLoader(FXMain.class.getResource(fxmlPath));
    }

    /**
     * Shows Error Message in provided Pane
     *
     * @param rootPane       Pane to show Error Message
     * @param text           Error Text
     * @param durationInMSec Duration of Message
     */
    public static void showError(Pane rootPane, String text, long durationInMSec) {
        Label toast = new Label(text);
        toast.setPrefWidth(rootPane.getWidth());
        toast.setWrapText(true);
        toast.setStyle("-fx-background-color: #ff0e4d; -fx-text-fill: #f0f8ff; -fx-padding: 10px; -fx-alignment: center; -fx-font-family: Ubuntu;");
        showNotification(rootPane, toast, durationInMSec);
    }

    /**
     * Shows Success Message in provided Pane
     *
     * @param rootPane       Pane to show Success Message
     * @param text           Success Text
     * @param durationInMSec Duration of Message
     */
    public static void showSuccess(Pane rootPane, String text, long durationInMSec) {
        Label toast = new Label(text);
        toast.setPrefWidth(rootPane.getWidth());
        toast.setWrapText(true);
        toast.setStyle("-fx-background-color: #00ba35; -fx-text-fill: #f0f8ff; -fx-padding: 10px; -fx-alignment: center; -fx-font-family: Ubuntu;");
        showNotification(rootPane, toast, durationInMSec);
    }

    /**
     * Shows Custom Notification (node) from rootPane
     *
     * @param rootPane Pane to show Notification
     * @param node     Node to show as Notification
     * @param duration Notification Time
     */
    private static void showNotification(Pane rootPane, Node node, long duration) {
        Platform.runLater(() -> {
            JFXSnackbar snackbar = new JFXSnackbar(rootPane);
            JFXSnackbar.SnackbarEvent eventToast = new JFXSnackbar.SnackbarEvent(node, new Duration(duration), null);
            snackbar.enqueue(eventToast);
        });
    }
}
