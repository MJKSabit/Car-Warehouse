package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.controller.Admin;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final public class AdminNetwork {

    private Admin admin;

    /**
     * Tightly Coupled with Admin Controller,
     * Only handles network related stuff of that Controller
     * @param admin The controller instance
     */
    public AdminNetwork(Admin admin) {
        this.admin = admin;

        // What to do if Server sends Data.ERROR Response ?
        ServerConnect.getInstance().getResponseListener().setErrorHandler(
            new ErrorListener(admin)
        );
    }

    // Sends Logout request to Server
    public void logout() {
        ServerConnect.getInstance().sendRequest(
                // Send Logout Request
                new Data(Data.LOGOUT, new JSONObject(), null),
                // Navigate to Login Page after Logout
                response -> Platform.runLater(admin::showLogin)
        );
    }

    // Sends Adding New User Request to Server
    public void addUser(String username, String password) {
        JSONObject object = new JSONObject();
        try {
            object.put(Data.LOGIN_USERNAME, username);
            object.put(Data.LOGIN_PASSWORD, password);
        } catch (JSONException ignored) {}

        Data request = new Data(Data.ADD_USER, object, null);

        ServerConnect.getInstance().sendRequest(
            request,
            response -> Platform.runLater(()->admin.addUser(username))
        );
    }

    /**
     * Request for All Users (Manufacturer) from the server
     * @param items After retrieving user, where to add?
     */
    public void getAllUser(final ObservableList<String> items) {

        Data request = new Data(Data.GET_USERS, new JSONObject(), null);

        ServerConnect.getInstance().sendRequest(
            request,
            response -> {
                // First Action on getting back the response,
                // Remove all previous items and add all new
                Platform.runLater(items::clear);

                // Get All Users as JSONArray
                JSONArray users = response.getText().optJSONArray(Data.USER);
                for (int i=0; users!=null && i<users.length(); i++) {
                    var user = users.optString(i);

                    // Add User to the items List
                    Platform.runLater(() -> items.add(user));
                }
            }
        );
    }

    /**
     * Request to server to remove a user with username
     * @param username  user to remove
     * @param list      After removal, update this list
     */
    public void removeUser(String username, final ObservableList<String> list) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(Data.USER, username);
        } catch (JSONException ignored) {}

        ServerConnect.getInstance().sendRequest(
                new Data(Data.REMOVE_USER, jsonObject, null),
                // Remove user from the observable list
                response -> Platform.runLater(() -> list.remove(username))
        );
    }
}
