package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.controller.Login;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginNetwork {

    private final Login loginController;

    public LoginNetwork(Login loginController) {
        this.loginController = loginController;
        setErrorListener();
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

    public void setErrorListener() {
//        loginController.getStage().getScene().getRoot()
        ServerConnect.getInstance().getResponseListener().setErrorHandler( (response) -> {
            Platform.runLater(() -> {
                Dialog dialog = new Dialog();
                dialog.setContentText("Error");
                dialog.setContentText(response.getText().optString(Data.INFO, "Information not provided"));
                dialog.show();
            });
        });
    }

    public void showInfo(String info) {
        //
    }

}
