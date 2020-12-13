package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.network.AdminNetwork;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

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

    AdminNetwork network;

    public void initialize() {
        deleteButton.disableProperty().bind(userListView.getSelectionModel().selectedItemProperty().isNull());
        addNewButton.disableProperty().bind(
                newPassword.textProperty().isNotEqualTo(newConfirmPassword.textProperty())
                .or(newPassword.textProperty().isEqualTo(""))
                .or(newUsername.textProperty().isEqualTo(""))
        );

        network = new AdminNetwork(this);
    }

    public void setLoginPage(Login loginPage) {
        this.loginPage = loginPage;
    }

    @FXML
    void addNewUser(ActionEvent event) {
        network.addUser(newUsername.getText(), newPassword.getText());
    }

    @FXML
    void deleteSelected(ActionEvent event) {

    }

    @FXML
    void logout(ActionEvent event) {
        network.logout();
    }

    @FXML
    void refresh(ActionEvent event) {
        network.getAllUser(userListView.getItems());
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
}
