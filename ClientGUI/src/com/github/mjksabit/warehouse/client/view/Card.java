package com.github.mjksabit.warehouse.client.view;

import com.github.mjksabit.warehouse.client.JFXLoader;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class Card extends AnchorPane {

    public Card() {
        FXMLLoader loader = JFXLoader.getFXMLLoader("card");
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
