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

final public class Card extends AnchorPane {

    // Flag to show whether Buy Option || Delete & Edit Option
    private static boolean asViewer = true;

    // Changing Which options to show in each cards
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

    /**
     * Card to show Car details with appropriate actions
     * @param car   Initial values for the card
     */
    public Card(Car car) {

        // Loads the template from Card FXML
        FXMLLoader loader = FXUtil.getFXMLLoader("card");

        // Inject this instance to bind with FXML
        loader.setController(this);
        // Specify Root of Card (Not specified in FXML)
        loader.setRoot(this);

        try { loader.load(); }
        catch (IOException e) { e.printStackTrace(); }

        // Show Options which are relevant to User
        viewerOptions.setDisable(!asViewer);
        viewerOptions.setVisible(asViewer);
        manuOptions.setDisable(asViewer);
        manuOptions.setVisible(!asViewer);

        // Change default values of the card
        setCar(car);
    }

    // Keeps the car instance for later usage
    private Car car = null;

    // Set the car object to change its values
    public void setCar(Car car) {
        this.car = car;

        // From Right To Left :: Color
        Circle[] circles = {color3, color2, color1};

        carMake.setText(car.getMake());
        carModel.setText(car.getModel());
        price.setText(String.format("$%d", car.getPrice()));
        registrationNo.setText(car.getRegistrationNumber());
        yearMade.setText(String.valueOf(car.getYearMade()));

        for (int i = 0; i < 3; i++) {
            // Do not show color circle if NULL
            if (!car.getColors()[i].equals("null")) {
                circles[i].setFill(Color.valueOf(car.getColors()[i]));
                circles[i].setVisible(true);
            } else {
                circles[i].setVisible(false);
            }
        }

        if(car.getImage() != null) // If there is image, set the image, else default will be shown
            image.setImage(new Image(new ByteArrayInputStream(car.getImage())));

        // How many car left?
        setLeft(car.getLeft());
    }


    public void setLeft(int left) {
        // Show Out of stock if no car LEFT
        // And disable option to BUY
        if (left <= 0) {
            quantity.setText("Out of Stock");
            quantity.setStyle("-fx-background-color: #F007");
            viewerOptions.setDisable(true);
        }
        else {
            quantity.setText(String.format("%d left", left));
            quantity.setStyle("-fx-background-color: #0007");
            viewerOptions.setDisable(!asViewer);
        }

    }

    // Inject OnClickListener From Outside (FOR VIEWER)
    public void setOnBuyListener(EventHandler<ActionEvent> buyListener) {
        if (asViewer)
            buyButton.setOnAction(buyListener);
    }

    // Inject OnClickListener From Outside (FOR MANUFACTURER)
    public void setManufacturerListener(EventHandler<ActionEvent> edit, EventHandler<ActionEvent> remove) {
        if (!asViewer) {
            editButton.setOnAction(edit);
            deleteButton.setOnAction(remove);
        }
    }

    public Car getCar() {
        return car;
    }
}
