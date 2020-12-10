package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.controller.Menu;
import com.github.mjksabit.warehouse.client.view.Card;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;

public class MenuNetwork {
    private final Menu menuController;
    private ObservableList<Card> cards;

    public MenuNetwork(Menu menuController) {
        this.menuController = menuController;

        ResponseListener responseListener = ServerConnect.getInstance().getResponseListener();
        responseListener.setErrorHandler(response -> FXUtil.showError(
                (Pane) menuController.getStage().getScene().getRoot(),
                response.getText().optString(Data.INFO, "Information not provided"),
                2000));

        ServerConnect.getInstance().getResponseListener().addHandler(Data.UPDATE_CAR, response -> {
            System.out.println(response.getText().toString());
        });

        ServerConnect.getInstance().sendRequest(new Data(Data.VIEW_ALL, null, null),
                (response -> System.out.println(response.getTYPE())));
    }
}
