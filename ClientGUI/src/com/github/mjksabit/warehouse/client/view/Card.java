package com.github.mjksabit.warehouse.client.view;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.controller.Menu;
import com.github.mjksabit.warehouse.client.model.Car;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import java.util.function.Function;

public class Card extends AnchorPane {

    private static boolean asViewer = true;

    public static void setAsViewer(boolean asViewer) {
        Card.asViewer = asViewer;
    }

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
    private JFXButton buyButton;

    @FXML
    private Pane manuOptions;

    @FXML
    private JFXButton editButton;

    @FXML
    private JFXButton deleteButton;

    public Card(Car car) {
        FXMLLoader loader = FXUtil.getFXMLLoader("card");
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewerOptions.setDisable(!asViewer);
        viewerOptions.setVisible(asViewer);
        manuOptions.setDisable(asViewer);
        manuOptions.setVisible(!asViewer);

        setCar(car);
    }

    public void setCar(Car car) {
        Circle[] circles = {color3, color2, color1};

        carMake.setText(car.getMake());
        carModel.setText(car.getModel());
        price.setText("$"+car.getPrice());
        registrationNo.setText(car.getRegistrationNumber());
        yearMade.setText(car.getYearMade()+"");

        for (int i = 0; i < 3; i++) {
            if (!car.getColors()[i].equals("null")) {
                circles[i].setFill(Color.valueOf(car.getColors()[i]));
                circles[i].setVisible(true);
            } else {
                circles[i].setVisible(false);
            }
        }

        if(car.getImage() != null)
            image.setImage(new Image(new ByteArrayInputStream(car.getImage())));

        setLeft(car.getLeft());
    }

    public void setLeft(int left) {
        if (left <= 0) {
            quantity.setText("Out of Stock");
            quantity.setStyle("-fx-background-color: #F007");
            viewerOptions.setDisable(true);
        }
        else {
            quantity.setText(left+" left");
            quantity.setStyle("-fx-background-color: #0007");
            viewerOptions.setDisable(!asViewer);
        }

    }

    public void setOnBuyListener(EventHandler<ActionEvent> buyListener) {
        buyButton.setOnAction(buyListener);
    }
}
