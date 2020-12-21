package com.github.mjksabit.warehouse.server.data;

import com.github.mjksabit.warehouse.server.Model.Car;
import com.github.mjksabit.warehouse.server.Network.Data;
import org.apache.commons.codec.digest.Crypt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public final class DB {

    private static final Logger logger = LogManager.getLogger(DB.class);

    // JDBC Prefix for sqlite
    private static final String SQLITE_PREFIX = "jdbc:sqlite:";
    // Database Location, Can either be absolute or relative
    private static final String DATABASE_FILE = "database.db";

    // Password Hashing SALT
    private static final String SALT = "$6$SALT-FOR-HASHING-PASSWORD";
    
    // Save image in that directory
    private static final String IMAGE_PATH = "images";

    // Admin Log in Password
    private static final String ADMIN_PASSWORD = "password";

    /*
    CREATE TABLE "users" (
	    "username"	TEXT NOT NULL,
	    "password"	TEXT NOT NULL,
	    PRIMARY KEY("username")
    )
    */
    private static final String USER_TABLE = "users";
    
    private static final String USER_USERNAME = "username";
    private static final String USER_PASSWORD = "password";

    /*
    CREATE TABLE "cars" (
	    "carid"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	    "registrationNumber"	TEXT NOT NULL UNIQUE,
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
    private static final String CAR_TABLE = "cars";
    
    private static final String CAR_ID          = "carid";
    private static final String CAR_REG_NO      = "registrationNumber";
    private static final String CAR_MAKE        = "make";
    private static final String CAR_MODEL       = "model";
    private static final String CAR_YEAR        = "year";
    private static final String CAR_PRICE       = "price";
    private static final String CAR_AVAILABLE   = "available";
    private static final String CAR_IMAGE       = "image";
    private static final String CAR_COLOR_1     = "color1";
    private static final String CAR_COLOR_2     = "color2";
    private static final String CAR_COLOR_3     = "color3";

    // Database Connection
    private final Connection dbConnect;

    // Private Constructor for Singleton
    private DB() {
        dbConnect = makeConnection();
    }

    private static DB instance = null;
    // Singleton instance Getter
    public static DB get() {
        if (instance == null)
            instance = new DB();
        return instance;
    }

    // Connect to Database, called once from constructor
    private Connection makeConnection() {
        String DATABASE_LOCATION = SQLITE_PREFIX + new File(DATABASE_FILE).getAbsolutePath();
        logger.info("makeConnection: " + DATABASE_LOCATION);
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

    // Login Validation using PLAIN PASSWORD
    public Data login(String username, String password) throws JSONException {
        username = username.toLowerCase();

        // Hashed Password with SALT
        password = Crypt.crypt(password, SALT);

        String type = Data.ERROR;
        JSONObject jsonObject = new JSONObject();

        String query =
                "SELECT " +
                        USER_PASSWORD +
                " from " +
                        USER_TABLE +
                " where " +
                        USER_USERNAME + " = ?";

        try (PreparedStatement statement = dbConnect.prepareStatement(query)) {
            // USER_USERNAME = username
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next()) {
                jsonObject.put(Data.INFO, "No such user exists!");
                type = Data.ERROR;
            } else if (resultSet.getString(USER_PASSWORD).equals(password)) {
                type = Data.LOGIN;
                jsonObject.put(Data.INFO, "Welcome back " + username + "!");
            } else {
                jsonObject.put(Data.INFO, "Password mismatch!");
                type = Data.ERROR;
            }
        } catch (SQLException t) { t.printStackTrace(); }

        return new Data(type, jsonObject, null);
    }

    // Method is synchronized so that buyer can not overbuy a product
    // However, protection is also given in Database level
    // with Conditional Checking of CAR_AVAILABLE>0
    public synchronized Data buyCar(int id) throws JSONException {
        Data.SimpleBuilder builder = null;

        String update =
                "UPDATE " +
                    CAR_TABLE +
                " SET " +
                    CAR_AVAILABLE + " = " + CAR_AVAILABLE + " - 1" +
                " WHERE " +
                    CAR_AVAILABLE + ">0" +
                    " AND " +
                    CAR_ID + "=?";

        try (PreparedStatement statement = dbConnect.prepareStatement(update)) {
            // CAR_ID = id
            statement.setInt(1, id);

            int execute = statement.executeUpdate();

            if (execute != 1) throw new SQLException();
            builder = new Data.SimpleBuilder(Data.BUY_CAR);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            builder = new Data.SimpleBuilder(Data.ERROR).add(Data.INFO, "Car not available to buy");
        }

        return builder.build();
    }

    public ArrayList<Data> allCars() throws JSONException {
        ArrayList<Data> carData = new ArrayList<>();

        String query = "SELECT " + CAR_ID + " FROM " + CAR_TABLE;

        try (PreparedStatement statement = dbConnect.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                carData.add(getCar(id).toData(id));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return carData;
    }

    // Returns null if no car found
    public Car getCar(int id) {
        Car car = null;

        String query =
                "SELECT * " +
                "FROM " +
                        CAR_TABLE +
                " WHERE " +
                        CAR_ID + "=?";

        try (PreparedStatement statement = dbConnect.prepareStatement(query)) {
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
            car.setImage(IMAGE_PATH + File.separator + resultSet.getString(CAR_IMAGE));

        } catch (SQLException throwables) {
            logger.info("No car [Either removed or not added] with id = " + id);
        }

        return car;
    }

    public boolean removeCar(int id) {

        // Another Query to delete the Picture
        String query =
                "SELECT " +
                        CAR_IMAGE +
                " FROM " +
                        CAR_TABLE +
                " WHERE " +
                        CAR_ID + "=?";

        // Delete Record from database
        String delete =
                "DELETE " +
                "FROM " +
                        CAR_TABLE +
                " WHERE " +
                        CAR_ID + "=?";

        try (PreparedStatement image = dbConnect.prepareStatement(query);
             PreparedStatement statement = dbConnect.prepareStatement(delete)) {
            // CAR_ID = id
            image.setInt(1, id);
            ResultSet set = image.executeQuery();

            set.next();

            // Will throw SQLException if no car found
            String imageName = set.getString(CAR_IMAGE);
            if (!new File(IMAGE_PATH + File.separator + imageName).delete())
                logger.error("Can not delete image file: " + imageName);

            // CAR_ID = id
            statement.setInt(1, id);
            int count = statement.executeUpdate();

            return count == 1;
        } catch (SQLException throwable) {
            logger.error("No car with id " + id + " to delete");
            return false;
        }
    }

    public int addCar(Car car) {

        // Data Validation before adding
        if (car.getPrice()<0 || car.getLeft()<0 || car.getYearMade()<0)
            return -1;

        // UUID to generate unique id for each image
        String uuid = UuidUtil.getTimeBasedUuid().toString();

        try {
            // Write to file from Data byte
            FileOutputStream fos = new FileOutputStream(IMAGE_PATH + File.separator + uuid, false);
            fos.write(car.getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String query = "INSERT " +
                "INTO " +
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
                "VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = dbConnect.prepareStatement(query)) {
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
            logger.info("Added New Car with id " + id);
            return id;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            // Validation Failed
            // Delete Uploaded File
            new File(IMAGE_PATH + File.separator + uuid).delete();
            return -1;
        }
    }

    public Data editCar(Data request) throws JSONException {
        int id = request.getText().optInt(Data.CAR_ID);
        String type;
        JSONObject jsonObject = new JSONObject();

        Car car = Car.fromData(request);

        String query =
                "SELECT " +
                        CAR_IMAGE +
                " FROM " +
                        CAR_TABLE +
                " WHERE " +
                        CAR_ID + "=?";

        String update =
                "UPDATE " +
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
                " WHERE " +
                        CAR_ID + "=?";

        if (car.getPrice()<0 || car.getLeft()<0 || car.getYearMade()<0) {
            type = Data.ERROR;
            jsonObject.put(Data.INFO, "Data Validation Error!");
        }
        else try (PreparedStatement image = dbConnect.prepareStatement(query);
             PreparedStatement statement = dbConnect.prepareStatement(update)) {

            image.setInt(1, id);
            ResultSet set = image.executeQuery();
            set.next();

            // Reuse image name
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
                throw new SQLException();

            FileOutputStream fos = new FileOutputStream(IMAGE_PATH + File.separator + imageName, false);
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

    public boolean isManufacturer(String username) {
        String query = "SELECT "+USER_USERNAME+" FROM "+USER_TABLE+" WHERE "+USER_USERNAME+"=?";

        try(PreparedStatement statement = dbConnect.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet set = statement.executeQuery();

            // Has one record means user exists
            return set.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public boolean adminLogin(String password) {
        return ADMIN_PASSWORD.equals(password);
    }

    public boolean addUser(String username, String password) {
        username = username.toLowerCase();
        // Password Hashing with SALT
        password = Crypt.crypt(password, SALT);

        String query =
                "INSERT INTO " +
                        USER_TABLE +
                            " (" + USER_USERNAME + ", " + USER_PASSWORD + ")" +
                " VALUES " +
                        "(?, ?)";

        try (PreparedStatement statement = dbConnect.prepareStatement(query)){
            statement.setString(1, username);
            statement.setString(2, password);
            statement.execute();
            return true;
        } catch (SQLException throwables) {
            return false;
        }
    }

    public ArrayList<String> getUsers() {
        ArrayList<String> users = new ArrayList<>();

        String query =
                "SELECT " +
                        USER_USERNAME +
                " FROM " +
                        USER_TABLE;

        try (PreparedStatement statement = dbConnect.prepareStatement(query)){
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                users.add(resultSet.getString(USER_USERNAME));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return users;
    }

    public void removeUser(String username) {
        username = username.toLowerCase();

        String query =
                "DELETE " +
                "FROM "+
                        USER_TABLE+
                " WHERE " +
                        USER_USERNAME + "=?";

        logger.info(query+username);
        try (PreparedStatement statement = dbConnect.prepareStatement(query)){
            statement.setString(1, username);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
