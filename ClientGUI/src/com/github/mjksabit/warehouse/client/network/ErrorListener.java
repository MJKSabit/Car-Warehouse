package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.controller.Controller;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

public class ErrorListener implements ResponseHandler {

    private final Controller controller;

    /**
     * Specific Response Listener for ERROR
     * Show Error INFO if Available
     * @param controller    In which page to show the error
     */
    public ErrorListener(Controller controller) {
        this.controller = controller;
    }

    /**
     * When A Type of Response is obtained from server, what to do then
     * Set what to do with lambda function or implemented Class
     *
     * @param response response that is obtained from server
     */
    @Override
    public void handle(Data response) {
        Platform.runLater(() ->
            // Show error info as Toast
            FXUtil.showError(
                // Toast in Root Pane
                (Pane) controller.getStage().getScene().getRoot(),

                // If response do not have Data.INFO, tell user that error happened
                response.getText().optString(Data.INFO, "ERROR: Information not provided"),

                // Show Error for 2 Seconds
                2000
            )
        );
    }
}
