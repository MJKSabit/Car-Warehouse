package com.github.mjksabit.warehouse.client.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

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

    @FXML
    void cancel(ActionEvent event) {
        getStage().close();
    }

    @FXML
    void chooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        File selectedFile = fileChooser.showOpenDialog(getStage());
        try {
            image.setImage(new Image(new FileInputStream(selectedFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
