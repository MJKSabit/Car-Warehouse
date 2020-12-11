package com.github.mjksabit.warehouse.server.data;

import com.github.mjksabit.warehouse.server.Model.Car;
import com.github.mjksabit.warehouse.server.Model.User;
import com.github.mjksabit.warehouse.server.Network.Data;
import org.apache.logging.log4j.CloseableThreadContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DB {

    private Map<String, String> users = new HashMap<>();
    private Map<Integer, Car> cars = new HashMap<>();

    private DB() {
        users.put("sabit", "1234");
        var car = new Car("XYZ-123", "Toyota", "Nova", 2020, 10000, "#2A2A2A");
        car.setImage("/media/sabit/Data/@CODE/Java/Car-Warehouse/ClientGUI/src/com/github/mjksabit/warehouse/client/assets/car.jpeg");
        car.setLeft(10);
        cars.put(1, car);
//        cars.put(2, new Car("XYZ-1_3", "Toyota", "Nova", 2020, 10000, "#2A2A2A"));
    }

    private static DB instance = null;
    public static DB getInstance() {
        if (instance==null)
            instance = new DB();
        return instance;
    }


    public Data login(String username, String password) throws JSONException {
        username = username.toLowerCase();
        String type;
        JSONObject jsonObject = new JSONObject();

        if (!users.containsKey(username)) {
            jsonObject.put(Data.INFO, "No such user exists!");
            type = Data.ERROR;
        } else if (!users.get(username).equals(password)) {
            jsonObject.put(Data.INFO, "Password mismatch!");
            type = Data.ERROR;
        } else {
            type = Data.LOGIN;
            jsonObject.put(Data.INFO, "Welcome back "+username+"!");
        }

        return new Data(type, jsonObject, null);
    }

    public ArrayList<Data> allCars() throws JSONException {
        ArrayList<Data> carData = new ArrayList<>();
        for ( var key : cars.keySet()) {
            carData.add(cars.get(key).toData(key));
        }
        return carData;
    }
}
