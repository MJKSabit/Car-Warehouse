package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.model.Car;
import com.github.mjksabit.warehouse.client.network.MenuNetwork;
import com.github.mjksabit.warehouse.client.view.Card;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Menu extends Controller {

    @FXML
    private Pane toggleButtonGraphics;

    @FXML
    private Label usernameLabel;

    @FXML
    private AnchorPane sidePane;

    @FXML
    private Pane logoutButton;

    @FXML
    private Pane searchbyRegButton;

    @FXML
    private Pane searchByMakeButton;

    @FXML
    private AnchorPane regSearchContainer;

    @FXML
    private JFXTextField registrationNo;

    @FXML
    private AnchorPane makeSearchContainer;

    @FXML
    private JFXTextField carMake;

    @FXML
    private JFXTextField carModel;

    @FXML
    private FlowPane carListFlowPane;

    MenuNetwork network;

    public void init(String username) {
        usernameLabel.setText(username);
        registrationNo.requestFocus();

        regSearchContainer.managedProperty().bind(regSearchContainer.visibleProperty());
        makeSearchContainer.managedProperty().bind(makeSearchContainer.visibleProperty());

        regSearchContainer.setVisible(false);

        network = new MenuNetwork(this);

//        var car = new Car("XYZ-123", "Toyota", "Nova", 2020, 10000, "#2A2A2A");
//        car.setImage("./src/com/github/mjksabit/warehouse/client/assets/car.jpeg");
//        carListFlowPane.getChildren().add(new Card(car));
//        carListFlowPane.getChildren().add(new Card());
//        carListFlowPane.getChildren().add(new Card());
    }

    @FXML
    void closeSearchByMakeModel(ActionEvent event) {

    }

    @FXML
    void closeSearchByReg(ActionEvent event) {

    }

    @FXML
    void searchByMakeModel(ActionEvent event) {

    }

    @FXML
    void searchByRegNo(ActionEvent event) {

    }

    @FXML
    void toggleSideMenu(ActionEvent event) {

    }
}
