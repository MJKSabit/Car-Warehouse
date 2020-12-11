package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.network.MenuNetwork;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

import java.io.IOException;

public class Menu extends Controller {

    @FXML
    private JFXButton addCarButton;

    @FXML
    private JFXButton regSearchTab;

    @FXML
    private JFXButton makeSearchTab;

    @FXML
    private AnchorPane sidePane;

    @FXML
    private Label usernameLabel;

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
    ObservableList<Node> cards;

    public void init(String username) {
        usernameLabel.setText(username);

        regSearchContainer.managedProperty().bind(regSearchContainer.visibleProperty());
        makeSearchContainer.managedProperty().bind(makeSearchContainer.visibleProperty());

        hideSearchBar();

        cards = carListFlowPane.getChildren();
        network = new MenuNetwork(this, cards);


    }

    public void asViewer() {
        network.setViewer(true);
        addCarButton.setVisible(false);
        addCarButton.setDisable(true);
    }

    public void asManufacturer() {
        network.setViewer(false);
        addCarButton.setVisible(true);
        addCarButton.setDisable(false);
    }

    public Edit getEdit() {
        try {
            Edit edit = FXUtil.loadFXML("edit");
            edit.setOnSave(actionEvent -> {
                network.addCar(edit.getCar());
            });
            return edit;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    void addCar(ActionEvent event){
        getEdit().show("Add Car");
    }

    @FXML
    void closeSearchByMakeModel(ActionEvent event) {
        hideSearchBar();
    }

    @FXML
    void closeSearchByReg(ActionEvent event) {
        hideSearchBar();
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        network.logout();
        Login loginPage = FXUtil.loadFXML("login");
        loginPage.setStage(getStage());
        loginPage.show("Log in - MJK Warehouse");
    }

    private void hideSearchBar() {
        makeSearchContainer.setVisible(false);
        regSearchContainer.setVisible(false);
    }

    @FXML
    void showSearchByMake(ActionEvent event) {
        hideSearchBar();
        makeSearchContainer.setVisible(true);
    }

    @FXML
    void searchByMakeModel(ActionEvent event) {

    }

    @FXML
    void searchByRegNo(ActionEvent event) {

    }

    @FXML
    void showSearchByReg(ActionEvent event) {
        hideSearchBar();
        regSearchContainer.setVisible(true);
    }
}
