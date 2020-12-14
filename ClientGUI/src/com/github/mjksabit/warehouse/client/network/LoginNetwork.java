package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.controller.Login;
import javafx.application.Platform;
import org.json.JSONException;
import org.json.JSONObject;

final public class LoginNetwork {

    private final Login loginController;

    /**
     * Tightly Coupled with Login Controller,
     * Only handles network related stuff of Login
     * @param loginController   Login Controller
     */
    public LoginNetwork(Login loginController) {
        this.loginController = loginController;

        ResponseListener responseListener = ServerConnect.getInstance().getResponseListener();

        // What to do if Server sends Data.ERROR Response ?
        responseListener.setErrorHandler(
            new ErrorListener(loginController)
        );
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

        ServerConnect.getInstance().sendRequest(
            request,
            // If Successful log in, navigate to home page
            response -> Platform.runLater(loginController::showHome)
        );
    }

    public void loginAsAdmin(String password) {
        var object = new JSONObject();
        try {
            // Default ADMIN USERNAME
            object.put(Data.LOGIN_USERNAME, "admin");
            object.put(Data.LOGIN_PASSWORD, password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        var request = new Data(Data.ADMIN, object, null);

        ServerConnect.getInstance().sendRequest(
            request,
            // If Successful log in, navigate to admin page
            response -> Platform.runLater(loginController::showAdmin)
        );
    }
}
