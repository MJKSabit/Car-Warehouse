package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.model.Car;
import com.github.mjksabit.warehouse.client.network.MenuNetwork;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
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

    @FXML
    private ProgressIndicator regSearchLoading;

    @FXML
    private ProgressIndicator makeSearchLoading;

    MenuNetwork network;
    ObservableList<Node> cards;

    public void init(String username) {
        usernameLabel.setText(username);

        regSearchContainer.managedProperty().bind(regSearchContainer.visibleProperty());
        makeSearchContainer.managedProperty().bind(makeSearchContainer.visibleProperty());

        hideSearchBar();

        cards = carListFlowPane.getChildren();
        network = new MenuNetwork(this, cards);

        makeSearchLoading.setVisible(false);
        regSearchLoading.setVisible(false);
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
        network.resetCardFilter();
    }

    @FXML
    void closeSearchByReg(ActionEvent event) {
        hideSearchBar();
        network.resetCardFilter();
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
        makeSearchTab.setStyle("-fx-background-color:  #008891;");
        regSearchTab.setStyle("-fx-background-color:  #008891;");
    }

    @FXML
    void showSearchByMake(ActionEvent event) {
        hideSearchBar();
        makeSearchContainer.setVisible(true);
        makeSearchTab.setStyle("-fx-background-color: #00587A");
    }

    @FXML
    void searchByMakeModel(ActionEvent event) {
        makeSearchLoading.setVisible(true);
        network.setCardFilter(card -> {
            Car car = card.getCar();
            String make = carMake.getText().toLowerCase();
            String model = carModel.getText().toLowerCase();

            return car.getMake().toLowerCase().contains(make) &&
                    car.getModel().toLowerCase().contains(model);
        });
        makeSearchLoading.setVisible(false);
    }

    @FXML
    void searchByRegNo(ActionEvent event) {
        regSearchLoading.setVisible(true);
        network.setCardFilter(card -> {
            Car car = card.getCar();
            String regNo = registrationNo.getText().toLowerCase();
            return car.getRegistrationNumber().toLowerCase().contains(regNo);
        });
        regSearchLoading.setVisible(false);
    }

    @FXML
    void showSearchByReg(ActionEvent event) {
        hideSearchBar();
        regSearchContainer.setVisible(true);
        regSearchTab.setStyle("-fx-background-color: #00587A");
    }
}
