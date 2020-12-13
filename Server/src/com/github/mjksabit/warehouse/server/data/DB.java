package com.github.mjksabit.warehouse.server.data;

import com.github.mjksabit.warehouse.server.Model.Car;
import com.github.mjksabit.warehouse.server.Model.User;
import com.github.mjksabit.warehouse.server.Network.Data;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DB {

    private static Logger logger = LogManager.getLogger(DB.class);

    public static final String DATABASE_FILE = "database.db";
    public static final String IMAGE_PATH = "images";

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
	    "carid"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
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
    public static final String CAR_ID = "carid";
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

        String query = "SELECT "+CAR_ID+" FROM "+CAR_TABLE;

        try (PreparedStatement statement = dbConnect.prepareStatement(query)){
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                int id = resultSet.getInt(1);
                carData.add(getCar(id).toData(id));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return carData;
    }

    public Car getCar(int id) {
        Car car = null;

        String query = "SELECT * FROM " + CAR_TABLE +
                " WHERE " + CAR_ID + "=?";

        try(PreparedStatement statement = dbConnect.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            resultSet.next();

            car = new Car(
                    resultSet.getString(CAR_REG_NO),
                    resultSet.getString(CAR_MAKE),
                    resultSet.getString(CAR_MODEL),
                    resultSet.getInt(CAR_YEAR),
                    resultSet.getInt(CAR_PRICE),
                    resultSet.getString(CAR_COLOR_1),
                    resultSet.getString(CAR_COLOR_2),
                    resultSet.getString(CAR_COLOR_3)
            );
            car.setLeft(resultSet.getInt(CAR_AVAILABLE));
            car.setImage(IMAGE_PATH+File.separator+resultSet.getString(CAR_IMAGE));

        } catch (SQLException throwables) {
            logger.info("No car with id = "+id);
        }

        return car;
    }

    public boolean removeCar(int id) {
        String query = "SELECT " + CAR_IMAGE +
                " FROM " + CAR_TABLE +
                " WHERE " + CAR_ID + "=?";
        String delete = "DELETE FROM "+CAR_TABLE+" WHERE "+CAR_ID+"=?";

        try (PreparedStatement image = dbConnect.prepareStatement(query);
             PreparedStatement statement = dbConnect.prepareStatement(delete)){
            image.setInt(1, id);

            ResultSet set = image.executeQuery();

            set.next();
            String imageName = set.getString(CAR_IMAGE);
            new File(IMAGE_PATH+File.separator+imageName).delete();

            statement.setInt(1, id);
            int count = statement.executeUpdate();

            return count==1;
        } catch (SQLException throwables) {
            logger.error("No car with id "+id+" to delete");
            return false;
        }
    }

    public int addCar(Car car) {

        String uuid = UuidUtil.getTimeBasedUuid().toString();
        try {
            FileOutputStream fos = new FileOutputStream(IMAGE_PATH+File.separator+uuid, false);
            fos.write(car.getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String query = "INSERT INTO " +
                CAR_TABLE +
                " (" +
                    CAR_REG_NO + ", " +
                    CAR_MAKE + ", " +
                    CAR_MODEL + ", " +
                    CAR_YEAR + ", " +
                    CAR_PRICE + ", " +
                    CAR_AVAILABLE + ", " +
                    CAR_COLOR_1 + ", " +
                    CAR_COLOR_2 + ", " +
                    CAR_COLOR_3 + ", " +
                    CAR_IMAGE +
                ") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement statement = dbConnect.prepareStatement(query)) {
            statement.setString(1, car.getRegistrationNumber());
            statement.setString(2, car.getMake());
            statement.setString(3, car.getModel());
            statement.setInt(4, car.getYearMade());
            statement.setInt(5, car.getPrice());
            statement.setInt(6, car.getLeft());
            statement.setString(7, car.getColors()[0]);
            statement.setString(8, car.getColors()[1]);
            statement.setString(9, car.getColors()[2]);
            statement.setString(10, uuid);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating car failed, no rows affected.");
            }

            ResultSet resultSet = statement.getGeneratedKeys();
            int id = resultSet.getInt(1);
            logger.info("Added New Car with id "+id);
            return id;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return -1;
        }
    }

    public Data editCar(Data request) throws JSONException {
        int id = request.getText().optInt(Data.CAR_ID);
        String type;
        JSONObject jsonObject = new JSONObject();

        Car car = Car.fromData(request);

        String query = "SELECT " + CAR_IMAGE +
                " FROM " + CAR_TABLE +
                " WHERE " + CAR_ID + "=?";

        String update = "UPDATE " +
                CAR_TABLE +
                " SET " +
                    CAR_REG_NO + "=? , " +
                    CAR_MAKE + "=? , " +
                    CAR_MODEL + "=? , " +
                    CAR_YEAR + "=? , " +
                    CAR_PRICE + "=? , " +
                    CAR_AVAILABLE + "=? , " +
                    CAR_COLOR_1 + "=? , " +
                    CAR_COLOR_2 + "=? , " +
                    CAR_COLOR_3 + "=? " +
                " WHERE " + CAR_ID + "=?";

        try(PreparedStatement image = dbConnect.prepareStatement(query);
            PreparedStatement statement = dbConnect.prepareStatement(update)) {

            image.setInt(1, id);
            ResultSet set = image.executeQuery();

            set.next();
            String imageName = set.getString(CAR_IMAGE);

            statement.setString(1, car.getRegistrationNumber());
            statement.setString(2, car.getMake());
            statement.setString(3, car.getModel());
            statement.setInt(4, car.getYearMade());
            statement.setInt(5, car.getPrice());
            statement.setInt(6, car.getLeft());
            statement.setString(7, car.getColors()[0]);
            statement.setString(8, car.getColors()[1]);
            statement.setString(9, car.getColors()[2]);
            statement.setInt(10, id);

            int updated = statement.executeUpdate();
            if (updated != 1)
                new SQLException("What the hack! updated more than once?");

            FileOutputStream fos = new FileOutputStream(IMAGE_PATH+File.separator+imageName, false);
            fos.write(car.getImage());

            type = Data.UPDATE_CAR;
            jsonObject.put(Data.INFO, "Car Edit successful");
        } catch (SQLException | IOException throwables) {
            throwables.printStackTrace();
            type = Data.ERROR;
            jsonObject.put(Data.INFO, "Can not edit the car!");
        }

        return new Data(type, jsonObject, null);
    }
}
