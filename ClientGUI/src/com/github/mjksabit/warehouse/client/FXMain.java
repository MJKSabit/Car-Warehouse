package com.github.mjksabit.warehouse.client;

import com.github.mjksabit.warehouse.client.controller.Login;
import javafx.application.Application;
import javafx.stage.Stage;

public class FXMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Login loginPage = FXUtil.loadFXML("login");
        loginPage.setStage(primaryStage);

        loginPage.show("Log in - MJK Warehouse");

        primaryStage.setOnCloseRequest(event -> System.exit(0));
    }


    public static void main(String[] args) {
        launch(args);
    }
}
