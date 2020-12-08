package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.view.Card;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

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

    public void init(String username) {
        usernameLabel.setText(username);
        registrationNo.requestFocus();

        carListFlowPane.getChildren().add(new Card());
        carListFlowPane.getChildren().add(new Card());
        carListFlowPane.getChildren().add(new Card());
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
