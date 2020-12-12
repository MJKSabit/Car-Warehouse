package com.github.mjksabit.warehouse.client.controller;

import com.github.mjksabit.warehouse.client.FXMain;
import com.github.mjksabit.warehouse.client.model.Car;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
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

    @FXML
    private JFXCheckBox color_1_check;

    @FXML
    private JFXCheckBox color_2_check;

    @FXML
    private JFXCheckBox color_3_check;

    private File selectedFile;


    public void initialize() {
        color1.visibleProperty().bind(color_1_check.selectedProperty());
        color2.visibleProperty().bind(color_2_check.selectedProperty());
        color3.visibleProperty().bind(color_3_check.selectedProperty());
    }


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
        JFXCheckBox[] checkBoxes = {color_1_check, color_2_check, color_3_check};

        for (int i = 0; i < colorPicker.length; i++) {
            if(!car.getColors()[i].equals("null")) {
                checkBoxes[i].selectedProperty().setValue(true);
                colorPicker[i].setValue(Color.valueOf(car.getColors()[i]));
            } else {
                checkBoxes[i].selectedProperty().setValue(false);
            }
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
                color1.isVisible() ? color1.valueProperty().get().toString() : null,
                color2.isVisible() ? color2.valueProperty().get().toString() : null,
                color3.isVisible() ? color3.valueProperty().get().toString() : null);

        if (selectedFile == null)
            selectedFile = new File(FXMain.class.getResource("assets/car.jpeg").getFile());

        car.setImage(selectedFile.getPath());
        car.setLeft(Integer.parseInt(stock.getText()));
        return car;
    }

    private boolean validate() {
        JFXTextField []fields = {yearMade, price, stock};

        int i=0;
        try {
            for (i=0; i<fields.length; i++) {
                if (Integer.parseInt(fields[i].getText())<0) throw new Exception();
            }
        } catch (Exception e) {
            fields[i].setStyle("-fx-fill: #F00");
            fields[i].setFocusColor(Color.RED);
            fields[i].requestFocus();
            return false;
        }

        JFXTextField [] texts = {carMake, model, regNo};

        for (var text : texts)
            if (text.getText().equals("")) {
                text.setStyle("-fx-fill: #F00");
                text.setFocusColor(Color.RED);
                text.requestFocus();
                return false;
            }

        return true;
    }

    public void setOnSave(EventHandler<ActionEvent> event) {
        save.setOnAction(actionEvent -> {
            if (!validate()) return;
            event.handle(actionEvent);
            getStage().close();
        });
    }

}
