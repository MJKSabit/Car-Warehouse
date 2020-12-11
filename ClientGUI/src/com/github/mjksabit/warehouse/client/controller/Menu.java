package com.github.mjksabit.warehouse.client.controller;

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

//        var car = new Car("XYZ-123", "Toyota", "Nova", 2020, 10000, "#2A2A2A");
//        car.setImage("./src/com/github/mjksabit/warehouse/client/assets/car.jpeg");
//        carListFlowPane.getChildren().add(new Card(car));
//        carListFlowPane.getChildren().add(new Card());
//        carListFlowPane.getChildren().add(new Card());
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

    @FXML
    void addCar(ActionEvent event) {

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
    void logout(ActionEvent event) {

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
