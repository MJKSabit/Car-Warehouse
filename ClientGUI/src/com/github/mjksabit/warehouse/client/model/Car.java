package com.github.mjksabit.warehouse.client.model;

import com.github.mjksabit.warehouse.client.network.Data;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Car {
    private String    registrationNumber;
    private int       yearMade;
    private final String[]  colors = new String[3];
    private String    make;
    private String    model;
    private int       price;
    private byte[]    image;
    private int       left;

    public Car(String registrationNumber, String make, String model, int yearMade, int price, String... colors) {
        this.registrationNumber = registrationNumber;
        this.yearMade = yearMade;
        this.make = make;
        this.model = model;
        this.price = price;
        for (int i = 0; i < 3 && i<colors.length; i++) {
            this.colors[i] = colors[i];
        }
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public int getYearMade() {
        return yearMade;
    }

    public void setYearMade(int yearMade) {
        this.yearMade = yearMade;
    }

    public String[] getColors() {
        return colors;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setColors(String... colors) {
        for (int i = 0; i < 3; i++) {
            this.colors[i] = i<colors.length ? colors[i] : null;
        }
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setImage(String filePath) {
        File file = new File(filePath);
        try {
            image = new FileInputStream(file).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getImage() {
        return image;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    private static final String    REG_NO = "regNo";
    private static final String    YEAR = "year";
    private static final String    COLORS = "colors";
    private static final String    MAKE = "make";
    private static final String    MODEL = "model";
    private static final String    PRICE = "price";
    private static final String    LEFT = "left";

    public static final String     CAR = "car";
    public static final String     ID = Data.CAR_ID;

    public Data toData(int id) throws JSONException {
        JSONObject object = new JSONObject();

        object.put(REG_NO, registrationNumber);
        object.put(YEAR, yearMade);

        var color = new JSONArray();
        color.put(colors[0]);
        color.put(colors[1]);
        color.put(colors[2]);
        object.put(COLORS, color);

        object.put(PRICE, price);
        object.put(MAKE, make);
        object.put(MODEL, model);
        object.put(LEFT, left);

        JSONObject root = new JSONObject();
        root.put(CAR, object);
        root.put(ID, id);

        return new Data(Data.UPDATE_CAR, root, image);
    }

    public static Car fromData(Data data) {
        JSONObject object = data.getText().optJSONObject(CAR);

        Car car = new Car(
                object.optString(REG_NO),
                object.optString(MAKE),
                object.optString(MODEL),
                object.optInt(YEAR),
                object.optInt(PRICE),
                object.optJSONArray(COLORS).optString(0),
                object.optJSONArray(COLORS).optString(1),
                object.optJSONArray(COLORS).optString(2)
        );

        car.left = object.optInt(LEFT, 0);

        car.setImage(data.getBinary());

        return car;
    }
}
