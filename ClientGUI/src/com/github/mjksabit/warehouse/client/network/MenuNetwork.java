package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.controller.Menu;
import com.github.mjksabit.warehouse.client.model.Car;
import com.github.mjksabit.warehouse.client.view.Card;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class MenuNetwork {
    private final Menu menuController;
    private ObservableList<Node> cards;

    public MenuNetwork(Menu menuController, ObservableList<Node> cards) {
        this.menuController = menuController;
        this.cards = cards;

        ResponseListener responseListener = ServerConnect.getInstance().getResponseListener();
        responseListener.setErrorHandler(response -> FXUtil.showError(
                (Pane) menuController.getStage().getScene().getRoot(),
                response.getText().optString(Data.INFO, "Information not provided"),
                2000));

        ServerConnect.getInstance().getResponseListener().addHandler(Data.UPDATE_CAR, response -> {
            final Car car = Car.fromData(response);
            System.out.println("Adding New Car from Network.");
            Platform.runLater(() -> cards.add(new Card(car)));
        });

        ServerConnect.getInstance().sendRequest(new Data(Data.VIEW_ALL, null, null),
                (response -> System.out.println(response.getTYPE())));
    }
}
