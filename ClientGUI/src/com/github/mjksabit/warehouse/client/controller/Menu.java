package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.model.Car;
import com.github.mjksabit.warehouse.client.network.*;
import com.github.mjksabit.warehouse.client.view.Card;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

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

    // All available Cards & Car in ClientSide
    private final Map<Integer, Card> cardMap = new HashMap<>();

    // Observable List where Cards are
    private ObservableList<Node> cards;
    // The Predicate to filter cards in the Observable List
    private volatile Predicate<Card> cardFilter = item -> true;

    // Initiate the Menu View
    // MUST CALL THIS BEFORE SHOW
    public void init(String username) {

        // Initially Sets the username
        usernameLabel.setText(username);

        // Binding for Removing Search bars completely with Visibility
        regSearchContainer.managedProperty().bind(regSearchContainer.visibleProperty());
        makeSearchContainer.managedProperty().bind(makeSearchContainer.visibleProperty());

        // No Searchbar at start
        hideSearchBar();

        // Sets the ObservableList that shows Cards
        cards = carListFlowPane.getChildren();

        ResponseListener responseListener = ServerConnect.getInstance().getResponseListener();

        // What to do if Server sends Data.ERROR Response ?
        responseListener.setErrorHandler(
                new ErrorListener(this)
        );

        // Listen for new/deleted/updated Car INFO without any REQUEST
        responseListener.addHandler(Data.UPDATE_CAR, response -> {
            // If response body has no car, then the car is deleted
            if (!response.getText().has(Data.CAR))
                removeCar(response.getText().optInt(Data.CAR_ID));
                // Else the car is either ADDED or UPDATED
            else
                updateCar(response.getText().optInt(Data.CAR_ID), Car.fromData(response));
        });

        // After adding Listener, only then request for all car as
        // now we can handle the UPDATE request
        ServerConnect.getInstance().sendRequest(
                new Data(Data.VIEW_ALL, new JSONObject(), null),
                // Nothing to do with the response
                response -> {});

    }

    // Remove Car with particular id
    private void removeCar(int id) {
        // Checking if the client had this Car
        if (cardMap.containsKey(id)) {
            Card card = cardMap.remove(id);

            // Try to remove only if currently on the observable list
            if (cardFilter.test(card))
                Platform.runLater(() -> cards.remove(card));
        }
    }

    // Method to handle adding and updating
    private void updateCar(int id, Car car) {
        if (!cardMap.containsKey(id))
            addCard(id, car);
        else if (car == null)
            System.out.println("THIS SHOULD HAVE BEEN DELETED!");
        else
            updateCard(id, car);
    }

    // Add new Card with car data
    private void addCard(int id, Car car) {
        Card card = new Card(car);

        // Save the card in the client side
        cardMap.put(id, card);

        // If any filter active (eg. Search),
        // show the card only if it is true for the predicate
        if(cardFilter.test(card))
            Platform.runLater(() -> cards.add(card));

        // Add Car Buy Option for Viewer
        card.setOnBuyListener((ignored -> buyCar(id)));

        // Add Car Edit/Remove Option for Manufacturer
        card.setManufacturerListener(
                ignored -> editCarRequest(id),
                ignored -> removeCarRequest(id)
        );
    }

    private void buyCar(int id) {
        JSONObject object = new JSONObject();
        try {
            object.put(Data.CAR_ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerConnect.getInstance().sendRequest(
                new Data(Data.BUY_CAR, object, null),
                // Show Info if Car bought is successful
                response -> this.showInfo("Car bought successfully")
        );
    }

    // Sends request to remove a car providing id
    private void removeCarRequest(int id) {
        JSONObject object = new JSONObject();
        try {
            object.put(Data.CAR_ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Data data = new Data(Data.REMOVE_CAR, object, null);
        ServerConnect.getInstance().sendRequest(
                data,
                // Show info if Car remove request was from this Manufacturer
                response -> this.showInfo("Car removed successfully")
        );
    }

    // Show Edit Car Page to Edit a car attributes
    private void editCarRequest(int id) {
        Edit edit = this.getEdit();

        // Set initial values on the Edit Page
        edit.setCar(cardMap.get(id).getCar());

        // On save send request to server to edit teh car
        edit.setOnSave(ignored -> editCar(id, edit.getCar()));

        // Show the Car Edit Page
        edit.show("Edit Car");
    }

    // Update Card Info
    private void updateCard(int id, Car car) {
        Platform.runLater(() -> {
            Card card = cardMap.get(id);

            // Set the latest Car info on the Card
            card.setCar(car);


            if (cardFilter.test(card)) {
                // Add the latest Card to Observable list only if
                // it passes the latest search criteria and it is not already showing
                // -- To preserve order and prevent multiple Card
                if (!cards.contains(card))
                    cards.add(card);
            } else {
                // Don't Add the car if it does not pass search criteria
                // remove older version already exists there
                cards.remove(card);
            }
        });
    }

    public void asViewer() {
        Card.setAsViewer(true);
        addCarButton.setVisible(false);
        addCarButton.setDisable(true);
    }

    public void asManufacturer() {
        Card.setAsViewer(false);
        addCarButton.setVisible(true);
        addCarButton.setDisable(false);
    }

    public Edit getEdit() {
        try {
            Edit edit = FXUtil.loadFXML("edit");
            edit.setOnSave(actionEvent -> addCar(edit.getCar()));
            return edit;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Sends Car Edit request to server
    private void editCar(int id, Car car) {
        try {
            // Convert Car to Data
            Data data = car.toData(id);
            ServerConnect.getInstance().sendRequest(
                    new Data(Data.EDIT_CAR, data.getText(), data.getBinary()),
                    // Show confirmation that car has been edited from this side
                    response -> this.showInfo("Car Edited!")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addCar(Car car) {
        try {
            // Convert Car to Data
            // I don't know the car ID, so  -1
            Data data = car.toData(-1);

            ServerConnect.getInstance().sendRequest(
                    new Data(Data.ADD_CAR, data.getText(), data.getBinary()),
                    response -> System.out.println("Car Added")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void addCar(ActionEvent event){
        getEdit().show("Add Car");
    }

    @FXML
    void closeSearchByMakeModel(ActionEvent event) {
        hideSearchBar();
        resetCardFilter();
    }

    @FXML
    void closeSearchByReg(ActionEvent event) {
        hideSearchBar();
        resetCardFilter();
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        // Log out from Server
        ServerConnect.getInstance().sendRequest(
                new Data(Data.LOGOUT, new JSONObject(), null),
                response -> System.out.println("Logged Out Successfully!")
        );

        // Show Login Page
        Login loginPage = FXUtil.loadFXML("login");
        loginPage.setStage(getStage());
        loginPage.show("Log in - MJK Warehouse");
    }

    private void hideSearchBar() {
        makeSearchLoading.setVisible(false);
        regSearchLoading.setVisible(false);
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
        setCardFilter(card -> {
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
        setCardFilter(card -> {
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

    // Set filters to show search results
    public void setCardFilter(Predicate<Card> cardFilter) {
        this.cardFilter = cardFilter;
        updateCardFilter();
    }

    // Reset Filter to show all cars
    public void resetCardFilter() {
        // Reset means show all cars
        this.cardFilter = card -> true;
        updateCardFilter();
    }

    // Changes the cards observable list according to the filter applied
    private void updateCardFilter() {
        // First, make the list empty
        cards.clear();

        var carIds = cardMap.keySet();
        for (var carId : carIds)
            // Second add only those cars, that have passed the predicate
            if (cardFilter.test(cardMap.get(carId)))
                cards.add(cardMap.get(carId));
    }

    /**
     * Shows Info as Toast
     * @param info  Information to show
     */
    public void showInfo(String info) {
        Platform.runLater(
            () -> FXUtil.showSuccess(
                (Pane) getStage().getScene().getRoot(),
                info,
                2000
            )
        );
    }
}
