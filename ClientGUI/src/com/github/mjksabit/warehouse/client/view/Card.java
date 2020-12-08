package com.github.mjksabit.warehouse.client.view;

import com.github.mjksabit.warehouse.client.JFXLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

public class Card extends AnchorPane {

    @FXML
    private ImageView image;

    public Card() {
        FXMLLoader loader = JFXLoader.getFXMLLoader("card");
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Rectangle clip = new Rectangle(250, 250);
//        clip.setArcWidth(20);
//        clip.setArcHeight(20);
//
//        image.setClip(clip);
    }
}
