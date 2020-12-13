package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.controller.Edit;
import com.github.mjksabit.warehouse.client.controller.Menu;
import com.github.mjksabit.warehouse.client.model.Car;
import com.github.mjksabit.warehouse.client.view.Card;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class MenuNetwork {

    private final Menu menuController;
    private ObservableList<Node> cards;

    private volatile Map<Integer, Card> cardMap = new HashMap<>();

    private volatile Predicate<Card> cardFilter = null;

    public MenuNetwork(Menu menuController, ObservableList<Node> cards) {
        this.menuController = menuController;
        this.cards = cards;
        this.cardFilter = card -> true;

        ResponseListener responseListener = ServerConnect.getInstance().getResponseListener();

        responseListener.setErrorHandler(response -> FXUtil.showError(
                (Pane) menuController.getStage().getScene().getRoot(),
                response.getText().optString(Data.INFO, "Information not provided"),
                2000));

        responseListener.addHandler(Data.UPDATE_CAR, response -> {
            System.out.println("Car from Network: " + response.getText());
            if (!response.getText().has(Data.CAR)) {
                System.out.println("REMOVE CAR!!!");
                removeCar(response.getText().optInt(Data.CAR_ID));
            } else {
                final Car car = Car.fromData(response);
                updateCar(response.getText().optInt(Car.ID), car);
            }
        });

        ServerConnect.getInstance().sendRequest(new Data(Data.VIEW_ALL, null, null),
                response -> {});
    }

    public void setCardFilter(Predicate<Card> cardFilter) {
        this.cardFilter = cardFilter;
        updateCardFilter();
    }

    public void resetCardFilter() {
        this.cardFilter = card -> true;
        updateCardFilter();
    }

    private void updateCardFilter() {
        cards.clear();

        var carIds = cardMap.keySet();
        for (var carId : carIds)
            if (cardFilter.test(cardMap.get(carId)))
                cards.add(cardMap.get(carId));
    }


    private void removeCar(int id) {
        if (cardMap.containsKey(id)) {
            Card card = cardMap.remove(id);
            if (cardFilter.test(card))
                Platform.runLater(() -> cards.remove(card));
        }
    }

    public void setViewer(boolean viewer) {
        Card.setAsViewer(viewer);
    }

    private void buyCar(int id) {
        System.out.println("BUYING CAR with ID "+id);
        JSONObject object = new JSONObject();
        try {
            object.put(Data.CAR_ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Data data = new Data(Data.BUY_CAR, object, null);
        ServerConnect.getInstance().sendRequest(data, response -> {
            System.out.println(response.getTYPE());
        });
    }

    private void removeCarRequest(int id) {
        System.out.println("Removing CAR with ID "+ id);
        JSONObject object = new JSONObject();
        try {
            object.put(Data.CAR_ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Data data = new Data(Data.REMOVE_CAR, object, null);
        ServerConnect.getInstance().sendRequest(data, response -> {
            System.out.println(response.getTYPE());
        });
    }

    public void updateCar(int id, Car car) {
        if (!cardMap.containsKey(id)) {
            Card card = new Card(car);
            cardMap.put(id, card);

            if(cardFilter.test(card))
                Platform.runLater(() -> cards.add(card));

            card.setOnBuyListener((actionEvent -> buyCar(id)));
            card.setManufacturerListener(actionEvent -> {
                Edit edit = menuController.getEdit();
                edit.setCar(cardMap.get(id).getCar());
                edit.setOnSave(actionEvent1 -> editCar(id, edit.getCar()));
                edit.show("Edit Car");
            }, actionEvent -> removeCarRequest(id));
        } else {
            if (car==null) System.out.println("NULL CAR");
            Platform.runLater(() -> {
                Card card = cardMap.get(id);
                card.setCar(car);
                if (cardFilter.test(card)) {
                    if (!cards.contains(card)) cards.add(card);
                } else {
                    cards.remove(card);
                }
            });
        }
    }

    public void addCar(Car car) {
        try {
            Data data = car.toData(-1);

            ServerConnect.getInstance().sendRequest(
                    new Data(Data.ADD_CAR, data.getText(), data.getBinary()),
                    response -> System.out.println("Car Added"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void editCar(int id, Car car) {
        try {
            Data data = car.toData(id);
            ServerConnect.getInstance().sendRequest(
                    new Data(Data.EDIT_CAR, data.getText(), data.getBinary()),
                    response -> System.out.println("Car edited!"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        ServerConnect.getInstance().sendRequest(new Data(Data.LOGOUT, new JSONObject(), null), response -> System.out.println("Logged Out Successfully!"));
    }
}
