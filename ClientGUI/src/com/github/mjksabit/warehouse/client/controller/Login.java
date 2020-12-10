package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.JFXLoader;
import com.github.mjksabit.warehouse.client.network.LoginNetwork;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class Login extends Controller {

    private LoginNetwork network = new LoginNetwork(this);

    public void initialize() {
        manuUsername.setText("sabit");
    }


    @FXML
    private JFXButton togglerButton;

    @FXML
    private JFXTextField manuUsername;

    @FXML
    private JFXPasswordField manuPassword;

    @FXML
    void manufacturerLogin(ActionEvent event) {
        network.loginAsManufacturer(manuUsername.getText(), manuPassword.getText());
    }

    @FXML
    void toggleMode(ActionEvent event) {

    }

    @FXML
    void viewerLogin(ActionEvent event) {

    }

    public void showHome() throws IOException {
        Menu menu = JFXLoader.loadFXML("menu");
        menu.init(manuUsername.getText());
        menu.setStage(getStage());
        menu.show("MJK Warehouse - " + manuUsername.getText());
    }
}
