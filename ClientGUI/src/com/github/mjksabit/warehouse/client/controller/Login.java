package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.network.*;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Login extends Controller {

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

        // What to do if Server sends Data.ERROR Response ?
        ServerConnect.getInstance().getResponseListener().setErrorHandler(
                new ErrorListener(this)
        );

        // View Property Binding For Automatic Toggle
        manuPane.visibleProperty().bind(manuPane.disableProperty().not());
        adminPane.visibleProperty().bind(adminPane.disableProperty().not());
        adminPane.disableProperty().bind(manuPane.disableProperty().not());

        // Show Manufacturer Login Options Initially
        manuPane.setDisable(false);

        manuUsername.setText("sabit");
        manuPassword.setText("1234");
    }

    @FXML
    void manufacturerLogin(ActionEvent event) {
        this.loginAsManufacturer(manuUsername.getText(), manuPassword.getText());
    }

    @FXML
    void toggleMode(ActionEvent event) {
        manuPane.setDisable(!manuPane.isDisabled());
    }

    @FXML
    void viewerLogin(ActionEvent event) throws IOException {
        Menu menu = FXUtil.loadFXML("menu");

        // Show Viewer Options Only
        menu.init("Viewer");
        menu.asViewer();

        // Show in Current Stage
        menu.setStage(getStage());

        menu.show("MJK Warehouse - Buy Car");
    }

    @FXML
    void adminLogin(ActionEvent event) {
        this.loginAsAdmin(adminPassword.getText());
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
                response -> Platform.runLater(this::showHome)
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
                response -> Platform.runLater(this::showAdmin)
        );
    }
}
