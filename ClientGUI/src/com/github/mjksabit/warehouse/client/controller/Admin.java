package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.network.Data;
import com.github.mjksabit.warehouse.client.network.ErrorListener;
import com.github.mjksabit.warehouse.client.network.ServerConnect;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Admin extends Controller{

    private Login loginPage = null;

    @FXML
    private ListView<String> userListView;

    @FXML
    private JFXButton deleteButton;

    @FXML
    private JFXTextField newUsername;

    @FXML
    private JFXPasswordField newPassword;

    @FXML
    private JFXPasswordField newConfirmPassword;

    @FXML
    private JFXButton addNewButton;

    public void initialize() {

        // Realtime Validation and Protection
        deleteButton.disableProperty().bind(userListView.getSelectionModel().selectedItemProperty().isNull());
        addNewButton.disableProperty().bind(
                newPassword.textProperty().isNotEqualTo(newConfirmPassword.textProperty())
                .or(newPassword.textProperty().isEqualTo(""))
                .or(newUsername.textProperty().isEqualTo(""))
        );

        // What to do if Server sends Data.ERROR Response ?
        ServerConnect.getInstance().getResponseListener().setErrorHandler(
                new ErrorListener(this)
        );

        refresh(null);
    }

    public void setLoginPage(Login loginPage) {
        this.loginPage = loginPage;
    }

    @FXML
    void addNewUser(ActionEvent event) {
        this.addUser(newUsername.getText(), newPassword.getText());
    }

    @FXML
    void deleteSelected(ActionEvent event) {
        this.removeUser(userListView.getSelectionModel().getSelectedItem(), userListView.getItems());
    }

    @FXML
    void logout(ActionEvent event) {
        this.logout();
    }

    @FXML
    void refresh(ActionEvent event) {
        this.getAllUser(userListView.getItems());
    }

    public void showLogin() {
        loginPage.setStage(getStage());
        loginPage.show("Log in - MJK Warehouse");
    }

    public void addUser(String username) {
        userListView.getItems().add(username);
        newUsername.setText("");
        newPassword.setText("");
        newConfirmPassword.setText("");
    }

    // Sends Logout request to Server
    public void logout() {
        ServerConnect.getInstance().sendRequest(
                // Send Logout Request
                new Data(Data.LOGOUT, new JSONObject(), null),
                // Navigate to Login Page after Logout
                response -> Platform.runLater(this::showLogin)
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
                response -> Platform.runLater(()->this.addUser(username))
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

        ServerConnect.getInstance().sendRequest(
                new Data.SimpleBuilder(Data.REMOVE_USER).add(Data.USER, username).build(),
                // Remove user from the observable list
                response -> Platform.runLater(() -> list.remove(username))
        );
    }
}
