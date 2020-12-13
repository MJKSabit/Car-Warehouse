package com.github.mjksabit.warehouse.client.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class Admin extends Controller{

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
        deleteButton.disableProperty().bind(userListView.getSelectionModel().selectedItemProperty().isNull());
        addNewButton.disableProperty().bind(
                newPassword.textProperty().isNotEqualTo(newConfirmPassword.textProperty())
                .or(newPassword.textProperty().isEqualTo(""))
                .or(newUsername.textProperty().isEqualTo(""))
        );
    }

    @FXML
    void addNewUser(ActionEvent event) {

    }

    @FXML
    void deleteSelected(ActionEvent event) {

    }

    @FXML
    void logout(ActionEvent event) {

    }

    @FXML
    void refresh(ActionEvent event) {

    }

}
