package com.github.mjksabit.warehouse.client;

import com.github.mjksabit.warehouse.client.controller.Login;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FXMain extends Application {

    /*
    DEPENDENCIES:
    - Java JSON         : For Server Client Communication
    - JavaFX 11         : For GUI
    - JFoenix 9.0.8     : For Material Widgets
     */

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Icon for Application
        primaryStage.getIcons().add(new Image(FXMain.class.getResourceAsStream("assets/icon.png")));

        // Start from Login Page
        Login loginPage = FXUtil.loadFXML("login");
        loginPage.setStage(primaryStage);

        loginPage.show("Log in - MJK Warehouse");

        // Stop all currently running thread
        primaryStage.setOnCloseRequest(event -> System.exit(0));
    }


    public static void main(String[] args) {
        launch(args);
    }
}
