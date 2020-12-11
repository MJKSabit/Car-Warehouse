package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.FXMain;
import com.github.mjksabit.warehouse.client.model.Car;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Edit extends Controller{

    @FXML
    private ImageView image;

    @FXML
    private JFXTextField carMake;

    @FXML
    private JFXTextField yearMade;

    @FXML
    private JFXTextField model;

    @FXML
    private JFXTextField regNo;

    @FXML
    private JFXColorPicker color1;

    @FXML
    private JFXColorPicker color2;

    @FXML
    private JFXColorPicker color3;

    @FXML
    private JFXTextField price;

    @FXML
    private JFXTextField stock;

    @FXML
    private JFXButton save;

    private File selectedFile;



    @FXML
    void cancel(ActionEvent event) {
        getStage().close();
    }

    @FXML
    void chooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        selectedFile = fileChooser.showOpenDialog(getStage());
        try {
            image.setImage(new Image(new FileInputStream(selectedFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setCar(Car car) {
        regNo.setText(car.getRegistrationNumber());
        carMake.setText(car.getMake());
        yearMade.setText(""+car.getYearMade());
        model.setText(car.getModel());
        price.setText(""+car.getPrice());

        JFXColorPicker[] colorPicker = {color1, color2, color3};

        for (int i = 0; i < colorPicker.length; i++) {
            if(!car.getColors()[i].equals("null")) colorPicker[i].setValue(Color.valueOf(car.getColors()[i]));
        }

        image.setImage(new Image(new ByteArrayInputStream(car.getImage())));
        stock.setText(""+car.getLeft());
    }

    public Car getCar() {
        var car = new Car(regNo.getText(),
                carMake.getText(),
                model.getText(),
                Integer.parseInt(yearMade.getText()),
                Integer.parseInt(price.getText()),
                color1.valueProperty().get().toString(),
                color2.valueProperty().get().toString(),
                color3.valueProperty().get().toString());

        if (selectedFile == null)
            selectedFile = new File(FXMain.class.getResource("assets/car.jpeg").getFile());

        car.setImage(selectedFile.getPath());
        car.setLeft(Integer.parseInt(stock.getText()));
        return car;
    }

    public void setOnSave(EventHandler<ActionEvent> event) {
        save.setOnAction(actionEvent -> {
            event.handle(actionEvent);
            getStage().close();
        });
    }

}
