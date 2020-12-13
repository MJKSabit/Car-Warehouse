package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.controller.Admin;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdminNetwork {

    private Admin admin;

    public AdminNetwork(Admin admin) {
        this.admin = admin;
        ServerConnect.getInstance().getResponseListener().setErrorHandler(response -> Platform.runLater(() ->
                FXUtil.showError((Pane) admin.getStage().getScene().getRoot(),
                        response.getText().optString(Data.INFO, "Information not provided"),
                        2000)));
    }

    public void logout() {
        ServerConnect.getInstance().sendRequest(new Data(Data.LOGOUT, new JSONObject(), null), response -> {
            Platform.runLater(() -> admin.showLogin());
        });
    }

    public void addUser(String username, String password) {
        JSONObject object = new JSONObject();
        try {
            object.put(Data.LOGIN_USERNAME, username);
            object.put(Data.LOGIN_PASSWORD, password);
        } catch (JSONException ignored) {}

        Data request = new Data(Data.ADD_USER, object, null);
        ServerConnect.getInstance().sendRequest(request, response -> Platform.runLater(()->admin.addUser(username)));
    }

    public void getAllUser(ObservableList<String> items) {
        Data request = new Data(Data.GET_USERS, new JSONObject(), null);
        ServerConnect.getInstance().sendRequest(request, response -> {
            Platform.runLater(items::clear);

            JSONArray users = response.getText().optJSONArray(Data.USER);
            for (int i=0; users!=null && i<users.length(); i++) {
                var user = users.optString(i);
                Platform.runLater(() -> items.add(user));
            }
        });
    }
}
