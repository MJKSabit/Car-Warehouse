package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.controller.Edit;
import com.github.mjksabit.warehouse.client.controller.Menu;
import com.github.mjksabit.warehouse.client.model.Car;
import com.github.mjksabit.warehouse.client.view.Card;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

final public class MenuNetwork {

    private final Menu menuController;

    // Observable List where Cards are
    private final ObservableList<Node> cards;
    // The Predicate to filter cards in the Observable List
    private volatile Predicate<Card> cardFilter;

    // All available Cards & Car in ClientSide
    private final Map<Integer, Card> cardMap = new HashMap<>();

    /**
     * Handles all network stuffs for Menu Controller
     * As a result, tightly coupled with that controller
     * @param menuController    Instance of Menu
     * @param cards             Where to update/show new/deleted Cards
     */
    public MenuNetwork(Menu menuController, ObservableList<Node> cards) {
        this.menuController = menuController;
        this.cards = cards;
        this.cardFilter = card -> true;

        ResponseListener responseListener = ServerConnect.getInstance().getResponseListener();

        // What to do if Server sends Data.ERROR Response ?
        responseListener.setErrorHandler(
                new ErrorListener(menuController)
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

    public void setViewer(boolean viewer) {
        Card.setAsViewer(viewer);
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
            response -> menuController.showInfo("Car bought successfully")
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
            response -> menuController.showInfo("Car removed successfully")
        );
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

    // Show Edit Car Page to Edit a car attributes
    private void editCarRequest(int id) {
        Edit edit = menuController.getEdit();
        
        // Set initial values on the Edit Page
        edit.setCar(cardMap.get(id).getCar());

        // On save send request to server to edit teh car
        edit.setOnSave(ignored -> editCar(id, edit.getCar()));

        // Show the Car Edit Page
        edit.show("Edit Car");
    }

    // Sends Car Edit request to server
    private void editCar(int id, Car car) {
        try {
            // Convert Car to Data
            Data data = car.toData(id);
            ServerConnect.getInstance().sendRequest(
                    new Data(Data.EDIT_CAR, data.getText(), data.getBinary()),
                    // Show confirmation that car has been edited from this side
                    response -> menuController.showInfo("Car Edited!")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    // Log out from Current Session
    public void logout() {
        ServerConnect.getInstance().sendRequest(
            new Data(Data.LOGOUT, new JSONObject(), null),
            response -> System.out.println("Logged Out Successfully!")
        );
    }
}
