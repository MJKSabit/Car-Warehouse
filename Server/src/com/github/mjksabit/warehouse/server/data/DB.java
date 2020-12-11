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

        car = new Car("XYZ-123", "BMW", "Nova", 2020, 10000, "#2A2A2A");
        car.setImage("/media/sabit/Data/@CODE/Java/Car-Warehouse/ClientGUI/src/com/github/mjksabit/warehouse/client/assets/car.jpeg");
        car.setLeft(0);
        cars.put(2, car);
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

    public synchronized Data buyCar(int id) throws JSONException {
        String type;
        JSONObject jsonObject = new JSONObject();

        if (!cars.containsKey(id) || cars.get(id).getLeft()<=0) {
            type = Data.ERROR;
            jsonObject.put(Data.INFO, "Car not available");
        } else {
            type = Data.BUY_CAR;
            cars.get(id).setLeft(cars.get(id).getLeft()-1);
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

    public Car getCar(int id) {
        return cars.getOrDefault(id, null);
    }

    public boolean removeCar(int id) {
        if (cars.containsKey(id)) {
            cars.remove(id);
            return true;
        }
        return false;
    }
}
