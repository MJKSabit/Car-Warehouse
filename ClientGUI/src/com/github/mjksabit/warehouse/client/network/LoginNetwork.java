package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.controller.Login;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginNetwork {

    private final Login loginController;

    public LoginNetwork(Login loginController) {
        this.loginController = loginController;

        ResponseListener responseListener = ServerConnect.getInstance().getResponseListener();
        responseListener.setErrorHandler(response -> FXUtil.showError(
                (Pane)loginController.getStage().getScene().getRoot(),
                response.getText().optString(Data.INFO, "Information not provided"),
                2000));
    }

    public void loginAsManufacturer(String username, String password) {
        var object = new JSONObject();
        try {
            object.put(Data.LOGIN_USERNAME, username);
            object.put(Data.LOGIN_PASSWORD, password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        var request = new Data(Data.LOGIN, object, null);

        ServerConnect.getInstance().sendRequest(request, response -> {
            Platform.runLater(() -> {try { loginController.showHome();} catch (IOException e) {}});
        });
    }

}
