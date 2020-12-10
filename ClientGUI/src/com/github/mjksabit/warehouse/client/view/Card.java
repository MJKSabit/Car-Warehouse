package com.github.mjksabit.warehouse.client.view;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.model.Car;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Card extends AnchorPane {

    @FXML
    private ImageView image;

    @FXML
    private Label carMake;

    @FXML
    private Label carModel;

    @FXML
    private Label yearMade;

    @FXML
    private Circle color1;

    @FXML
    private Circle color2;

    @FXML
    private Circle color3;

    @FXML
    private Label quantity;

    @FXML
    private Label registrationNo;

    @FXML
    private Label price;

    @FXML
    private Pane viewerOptions;

    @FXML
    private Pane manuOptions;

    public Card(Car car) {
        FXMLLoader loader = FXUtil.getFXMLLoader("card");
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setCar(car);
    }

    public void setCar(Car car) {
        Circle[] circles = {color1, color2, color3};

        carMake.setText(car.getMake());
        carModel.setText(car.getModel());
        price.setText("$"+car.getPrice());
        registrationNo.setText(car.getRegistrationNumber());
        yearMade.setText(car.getYearMade()+"");

        for (int i = 0; i < 3; i++) {
            if (car.getColors()[i] != null) {
                circles[i].setFill(Color.valueOf(car.getColors()[i]));
                circles[i].setStroke(Color.valueOf("#000000"));
            } else {
                circles[i].setFill(Paint.valueOf("#00000000"));
                circles[i].setStroke(Color.valueOf("#00000000"));
            }
        }

        if(car.getImage() != null)
            image.setImage(new Image(new ByteArrayInputStream(car.getImage())));
    }
}
