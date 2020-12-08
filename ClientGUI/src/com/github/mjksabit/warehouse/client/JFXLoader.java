package com.github.mjksabit.warehouse.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.github.mjksabit.warehouse.client.controller.Controller;

import java.io.IOException;

public class JFXLoader {
    public static <T extends Controller> T loadFXML(String fxmlName) throws IOException {
        String fxmlPath = "view/" + fxmlName + ".fxml";
        FXMLLoader loader = new FXMLLoader(FXMain.class.getResource(fxmlPath));

        Parent rootNode = loader.load();
        Scene scene = new Scene(rootNode);

        T controller = loader.getController();
        controller.setRootNode(rootNode);

        return controller;
    }

    public static FXMLLoader getFXMLLoader(String fxmlName) {
        String fxmlPath = "view/" + fxmlName + ".fxml";
        return new FXMLLoader(FXMain.class.getResource(fxmlPath));
    }
}
