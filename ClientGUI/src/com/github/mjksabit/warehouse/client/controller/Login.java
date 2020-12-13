package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.network.LoginNetwork;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class Login extends Controller {

    private LoginNetwork network;

    @FXML
    private JFXButton togglerButton;

    @FXML
    private JFXTextField manuUsername;

    @FXML
    private JFXPasswordField manuPassword;

    @FXML
    private JFXPasswordField adminPassword;

    @FXML
    private AnchorPane adminPane;

    @FXML
    private AnchorPane manuPane;

    public void initialize() {
        network = new LoginNetwork(this);

        manuPane.visibleProperty().bind(manuPane.disableProperty().not());
        adminPane.visibleProperty().bind(adminPane.disableProperty().not());
        adminPane.disableProperty().bind(manuPane.disableProperty().not());

        manuPane.setDisable(false);

        manuUsername.setText("sabit");
        manuPassword.setText("1234");
    }

    @FXML
    void manufacturerLogin(ActionEvent event) {
        network.loginAsManufacturer(manuUsername.getText(), manuPassword.getText());
    }

    @FXML
    void toggleMode(ActionEvent event) {
        manuPane.setDisable(!manuPane.isDisabled());
    }

    @FXML
    void viewerLogin(ActionEvent event) throws IOException {
        Menu menu = FXUtil.loadFXML("menu");
        menu.init("Viewer");
        menu.asViewer();
        menu.setStage(getStage());
        menu.show("MJK Warehouse - Buy Car");
    }

    @FXML
    void adminLogin(ActionEvent event) {
        network.loginAsAdmin(adminPassword.getText());
    }

    public void showHome() {
        try {
            Menu menu = FXUtil.loadFXML("menu");
            menu.init(manuUsername.getText());
            menu.asManufacturer();
            menu.setStage(getStage());
            menu.show("MJK Warehouse - " + manuUsername.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void showAdmin() {
        try {
            Admin admin = FXUtil.loadFXML("admin");
            admin.setStage(getStage());
            admin.setLoginPage(this);
            admin.show("Admin Panel");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
