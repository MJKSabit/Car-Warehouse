package com.github.mjksabit.warehouse.server.data;

import com.github.mjksabit.warehouse.server.Model.Car;
import com.github.mjksabit.warehouse.server.Model.User;
import com.github.mjksabit.warehouse.server.Network.Data;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DB {

    private static Logger logger = LogManager.getLogger(DB.class);

    public static final String DATABASE_FILE = "database.db";

    /*
    CREATE TABLE "users" (
	    "username"	TEXT NOT NULL,
	    "password"	TEXT NOT NULL,
	    PRIMARY KEY("username")
    )
     */

    public static final String USER_TABLE = "users";
    public static final String USER_USERNAME = "username";
    public static final String USER_PASSWORD = "password";

    /*
    CREATE TABLE "cars" (
	    "id"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	    "registrationNumber"	TEXT NOT NULL,
	    "make"	TEXT NOT NULL,
    	"model"	TEXT NOT NULL,
    	"year"	INTEGER NOT NULL,
    	"price"	INTEGER NOT NULL,
    	"available"	INTEGER NOT NULL DEFAULT 0,
    	"color1"	TEXT DEFAULT NULL,
	    "color2"	TEXT DEFAULT NULL,
    	"color3"	TEXT DEFAULT NULL,
    	"image"	TEXT NOT NULL
    )
     */

    public static final String CAR_TABLE = "cars";
    public static final String CAR_ID = "id";
    public static final String CAR_REG_NO = "registrationNumber";
    public static final String CAR_MAKE = "make";
    public static final String CAR_MODEL = "model";
    public static final String CAR_YEAR = "year";
    public static final String CAR_PRICE = "price";
    public static final String CAR_AVAILABLE = "available";
    public static final String CAR_IMAGE = "image";
    public static final String CAR_COLOR_1 = "color1";
    public static final String CAR_COLOR_2 = "color2";
    public static final String CAR_COLOR_3 = "color3";

    private Map<String, String> users = new HashMap<>();
    private Map<Integer, Car> cars = new HashMap<>();

    private final Connection dbConnect;

    private Connection makeConnection() {
        String DATABASE_LOCATION = "jdbc:sqlite:" + new File(DATABASE_FILE).getAbsolutePath();
        logger.info("Connecting to Database: "+DATABASE_LOCATION);
        try {
            Connection connection = DriverManager.getConnection(DATABASE_LOCATION);
            logger.info("Connected to database");
            return connection;
        } catch (SQLException e) {
            logger.error("Cannot connect to database");
            e.printStackTrace();
            return null;
        }
    }

    private DB() {

        dbConnect = makeConnection();


        users.put("sabit", "1234");

        var car = new Car("XYZ-123", "Toyota", "Nova", 2020, 10000, "#2A2A2A");
        car.setImage("/media/sabit/Data/@CODE/Java/Car-Warehouse/ClientGUI/src/com/github/mjksabit/warehouse/client/assets/car.jpeg");
        car.setLeft(10);
        cars.put(1, car);

        car = new Car("XYZ-123", "BMW", "Nova", 2020, 10000, "#2A2A2A");
        car.setImage("/media/sabit/Data/@CODE/Java/Car-Warehouse/ClientGUI/src/com/github/mjksabit/warehouse/client/assets/car.jpeg");
        car.setLeft(0);
        cars.put(0, car);
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

        String query = "SELECT " +
                    USER_PASSWORD +
                " from " + USER_TABLE +
                " where " +
                    USER_USERNAME + " = ?";

        try(PreparedStatement statement = dbConnect.prepareStatement(query)){
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next() && resultSet.getString(USER_PASSWORD).equals(password)) {
                type = Data.LOGIN;
                jsonObject.put(Data.INFO, "Welcome back "+username+"!");
            } else {
                jsonObject.put(Data.INFO, "Password mismatch!");
                type = Data.ERROR;
            }
        } catch (SQLException throwables) {
            jsonObject.put(Data.INFO, "No such user exists!");
            type = Data.ERROR;
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

    private static Random random = new Random();
    public int addCar(Car car) {
        int id;
        do {
            id = random.nextInt();
        } while (cars.containsKey(id));
        cars.put(id, car);
        return id;
    }

    public Data editCar(Data request) throws JSONException {
        int id = request.getText().optInt(Data.CAR_ID);
        String type;
        JSONObject jsonObject = new JSONObject();

        if (cars.containsKey(id)) {
            Car car = Car.fromData(request);
            cars.put(id, car);
            type = Data.EDIT_CAR;
        } else {
            type = Data.ERROR;
            jsonObject.put(Data.INFO, "No car found in database");
        }

        return new Data(type, jsonObject, null);
    }
}
