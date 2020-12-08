package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.JFXLoader;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class Login extends Controller {

    public void initialize() {
        manuUsername.setText("Sabit");
    }

    @FXML
    private JFXButton togglerButton;

    @FXML
    private JFXTextField manuUsername;

    @FXML
    private JFXPasswordField manuPassword;

    @FXML
    void manufacturerLogin(ActionEvent event) throws IOException {
        Menu menu = JFXLoader.loadFXML("menu");
        menu.init(manuUsername.getText());
        menu.setStage(getStage());
        menu.show("MJK Warehouse - " + manuUsername.getText());
    }

    @FXML
    void toggleMode(ActionEvent event) {

    }

    @FXML
    void viewerLogin(ActionEvent event) {

    }
}
