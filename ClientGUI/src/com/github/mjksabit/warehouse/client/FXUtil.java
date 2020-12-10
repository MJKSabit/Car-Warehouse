package com.github.mjksabit.warehouse.client;

import com.jfoenix.controls.JFXSnackbar;
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

public class FXUtil {
    public static <T extends Controller> T loadFXML(String fxmlName) throws IOException {
        String fxmlPath = "view/" + fxmlName + ".fxml";
        FXMLLoader loader = new FXMLLoader(FXMain.class.getResource(fxmlPath));

        Parent rootNode = loader.load();
        Scene scene = new Scene(rootNode);

        T controller = loader.getController();
        controller.setRootNode(rootNode);

        return controller;
    }

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
        toast.setStyle("-fx-background-color: #ff0e4d; -fx-text-fill: #f0f8ff; -fx-padding: 20px; -fx-alignment: center; ");
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
        toast.setStyle("-fx-background-color: #00ba35; -fx-text-fill: #f0f8ff; -fx-padding: 20px; -fx-alignment: center; ");
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
