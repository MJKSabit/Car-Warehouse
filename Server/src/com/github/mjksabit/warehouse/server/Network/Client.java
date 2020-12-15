package com.github.mjksabit.warehouse.server.Network;

import com.github.mjksabit.warehouse.server.Model.Car;
import com.github.mjksabit.warehouse.server.data.DB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public final class Client implements Runnable, Closeable {

    // Keeping the list of subscribers, we can publish the updates
    private static final ArrayList<Client> clientList = new ArrayList<>();
    private final int id;

    private static final Logger logger = LogManager.getLogger(Client.class);

    // Socket to communicate with client
    private final Socket socket;

    // Send Response from different Thread to send from other threads serially
    private ResponseSender sender;

    // Input and Output Streams with Client
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final DataInputStream in;
    private final DataOutputStream out;

    // User State Save, initial
    private boolean isManufacturer = false;
    private boolean isAdmin = false;

    // Current logged in username
    private String user = null;

    /**
     * Manages One Socket Network to communicate, automatically creates new thread (non-blocking)
     * @param socket    Socket to connect to (Clients Socket)
     * @throws IOException  If unable to get input/output stream
     */
    public Client(Socket socket) throws IOException {
        this.socket = socket;

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        in = new DataInputStream(inputStream);
        out = new DataOutputStream(outputStream);

        // Keeps all connected client in a static list
        id = clientList.size();
        clientList.add(this);
        logger.info("Client Connected with id="+id);

        new Thread(this).start();
    }

    /**
     * Classifies the request type to send proper Response
     * @param request   the Request Data
     * @return          the Response Data
     * @throws JSONException    Unexpected
     */
    private Data route(Data request) throws JSONException {

        // All my routes for this project
        switch (request.getTYPE()) {

            // For manufacturer
            case Data.LOGIN:
                return login(request);

            case Data.REMOVE_CAR:
                return removeCar(request);
            case Data.ADD_CAR:
                return addCar(request);
            case Data.EDIT_CAR:
                return editCar(request);

            // View all Car Data for Viewer & Manufacturer
            case Data.VIEW_ALL:
                return viewCar(request);

            // For Viewer Only
            case Data.BUY_CAR:
                return buyCar(request);

            // Admin Login Route
            case Data.ADMIN:
                return admin(request);

            case Data.GET_USERS:
                return getUsers(request);
            case Data.ADD_USER:
                return addUser(request);
            case Data.REMOVE_USER:
                return removeUser(request);

            // Logout for all users, including manufacturer or Admin
            case Data.LOGOUT:
                return logout(request);

            // Unknown ROUTE
            default: {
                JSONObject object = new JSONObject();
                object.put(Data.INFO, "Unknown Request!");
                return new Data(Data.ERROR, object, null);
            }
        }
    }

    @Override
    public void run() {

        // Create Separate thread for sending response
        sender = new ResponseSender(this, out);

        Thread.currentThread().setName("Guest-"+id);
        sender.renameThread("Guest-Response-"+id);

        try {
            while (true) {
                logger.info("run: Waiting For Request");

                // Get new Request from InputStream
                var request = new Data(in);

                logger.info("run: New Request: " + request.getTYPE());

                // Route the Request to get proper Response
                var response = route(request);

                // Request key is embedded in Response even if the response if ERROR
                response.getText().put(Data.REQUEST_KEY, request.getTYPE());

                // Server has the control to remove ResponseListener from Client
                // Currently Every Request will be removed from listener
                response.getText().put(Data.REMOVE_REQUESTER, true);

                logger.info("run: Response "+response.getTYPE()+":"+response.getText());

                // Try to send the response from different thread, serially
                sender.addToQueue(response);
            }
        } catch (JSONException | IOException e) {
            logger.debug("Error on run: Probably Disconnected");
            logger.error(e.getMessage());

            // Try closing the client
            try { this.close(); } catch (IOException ignored) {}
        }

    }

    /**
     * Get single car from database with specified id
     * @param id    car id
     * @return      Data.CAR_UPDATE data format with car in it
     * @throws JSONException    Usual, unexpected
     */
    private static Data carUpdate(int id) throws JSONException {
        // Get car from database, null if not found
        Car car = DB.get().getCar(id);

        // not null means, either EDIT or ADD or UPDATE
        if (car != null)
            return car.toData(id);
            // null means DELETE
        else {
            JSONObject object = new JSONObject();
            object.put(Data.CAR_ID, id);
            return new Data(Data.UPDATE_CAR, object, null);
        }
    }

    /**
     * Sends car to all connected Clients
     * @param id    Car id which has been added/edited/deleted/updated
     */
    public static void notifyAllCar(int id) {
        try {
            var carData = carUpdate(id);
            for (var client : clientList)
                // Admin do not gets the car list
                if (!client.isAdmin)
                    client.sender.addToQueue(carData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ERROR 403: Unauthorized Access to any Route Response
    private Data unauthorized() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(Data.INFO, "You are not authorized!");
        return new Data(Data.ERROR, object, null);
    }

    // Login for Manufacturer Only
    private Data login(Data request) throws JSONException {
        JSONObject jsonObject = request.getText();

        String username = jsonObject.optString(Data.LOGIN_USERNAME);
        String password = jsonObject.optString(Data.LOGIN_PASSWORD);

        Data response = DB.get().login(username, password);

        response.getText().put(Data.REQUEST_KEY, request.getTYPE());

        // Application Layer Login Update
        if (response.getTYPE().equals(Data.LOGIN)) {
            this.user = username;
            this.isManufacturer = true;
            Thread.currentThread().setName(user);
            sender.renameThread(user + "-Response");

            logger.info("Logged in <" + user + ">");
        }

        return response;
    }

    private Data addCar(Data request) throws JSONException {
        // Check if Manufacturer, Second Check is to ensure the manufacturer is not removed by admin
        if (!isManufacturer || !DB.get().isManufacturer(user))
            return unauthorized();

        // Add car in Database
        int id = DB.get().addCar(Car.fromData(request));

        // Maybe year was not an integer...
        if (id==-1)
            return new Data(Data.ERROR, new JSONObject().put(Data.INFO, "Data Validation Failed!"), null);

        // Notify everyone about the added Car
        notifyAllCar(id);

        return new Data(Data.ADD_CAR, new JSONObject(), null);
    }

    private Data removeCar(Data request) throws JSONException {
        // Check if Manufacturer, Second Check is to ensure the manufacturer is not removed by admin
        if (!isManufacturer || !DB.get().isManufacturer(user))
            return unauthorized();

        // Remove car, and check if deleted
        boolean isRemoved = DB.get().removeCar(request.getText().optInt(Data.CAR_ID));
        String type;
        JSONObject object = new JSONObject();

        if (isRemoved)
            type = Data.REMOVE_CAR;
        else {
            type = Data.ERROR;
            object.put(Data.INFO, "Car can not be deleted.");
        }

        // Notify Everyone about removed car
        notifyAllCar(request.getText().optInt(Data.CAR_ID));
        return new Data(type, object, null);
    }

    private Data editCar(Data request) throws JSONException {

        // Check if Manufacturer, Second Check is to ensure the manufacturer is not removed by admin
        if (!isManufacturer || !DB.get().isManufacturer(user))
            return unauthorized();

        // Database Request for editing a car
        Data data = DB.get().editCar(request);

        // If not ERROR, Send data to every connected client to update the car
        if (!data.getTYPE().equals(Data.ERROR)) {
            notifyAllCar(request.getText().optInt(Data.CAR_ID));
        }

        return data;
    }

    private Data buyCar(Data request) throws JSONException {
        Data data = DB.get().buyCar(request.getText().optInt(Data.CAR_ID));

        // Manufacturer can not buy
        if (isManufacturer) {
            JSONObject object = new JSONObject();
            object.putOpt(Data.INFO, "Only Viewer can buy!");
            return new Data(Data.ERROR, object, null);
        }

        // Notify every client about this buy
        if (!data.getTYPE().equals(Data.ERROR))
            notifyAllCar(request.getText().optInt(Data.CAR_ID));

        return data;
    }

    // Immediately called after the client navigates to menu page
    // After having lister to handle CAR_UPDATE
    private Data viewCar(Data request) {
        // Send All Cars from Separate Thread so that User listen for
        // Other responses that time
        new Thread(() -> {
            try {
                // Get all cars from database
                var cars = DB.get().allCars();
                for (var car : cars)
                    sender.addToQueue(car);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();

        // Sends a dummy Response
        return new Data(Data.VIEW_ALL, new JSONObject(), null);
    }

    // Admin Login
    private Data admin(Data request) throws JSONException {
        // Password Check from Database
        isAdmin = DB.get().adminLogin(request.getText().optString(Data.LOGIN_PASSWORD));

        // Authenticated Admin
        if(isAdmin) {
            logger.info("ADMIN logged in");
            user = "admin";
            Thread.currentThread().setName("ADMIN");
            sender.renameThread("ADMIN-Response");

            return new Data(Data.ADMIN, new JSONObject(), null);
        }

        logger.info("ADMIN login error");

        JSONObject object = new JSONObject();
        object.put(Data.INFO, "Password mismatch!");
        return new Data(Data.ERROR, object, null);
    }

    // Add user from request
    private Data addUser(Data request) throws JSONException {
        JSONObject object = new JSONObject();

        // Retrieve Username
        String username = request.getText().getString(Data.LOGIN_USERNAME);

        // Only Admin can access this route
        if (!isAdmin)
            return unauthorized();

        // Successful only if new user is added to database
        if(DB.get().addUser(username,
                request.getText().getString(Data.LOGIN_PASSWORD)))
            return new Data(Data.ADD_USER, object, null);

            // Maybe there already exists a user with that username
        else{
            object.put(Data.INFO, "Can not add user, maybe user already exists!");
            return new Data(Data.ERROR, object, null);
        }
    }

    // Get all user (name only) request,
    // returns all the user in JSONArray with key Data.USER
    private Data getUsers(Data request) throws JSONException {
        if (!isAdmin) // Only Admin can get the manufacturer list
            return unauthorized();
        else {
            JSONObject object = new JSONObject();

            // Get all users from database
            ArrayList<String> users = DB.get().getUsers();
            // Put in Response Object
            object.put(Data.USER, new JSONArray(users));
            return new Data(Data.GET_USERS, object, null);
        }
    }

    // remove user from database, only throws exception only if
    // the user is unauthorized, else delete from database if exists
    private Data removeUser(Data request) throws JSONException {
        if (!isAdmin) // Only admin can delete any user
            return unauthorized();

        // Delete from Database
        DB.get().removeUser(request.getText().optString(Data.USER));
        return new Data(Data.REMOVE_USER, new JSONObject(), null);
    }

    // Manufacturer Logout Route
    private Data logout(Data request) {
        logger.info("Logged out <" + user + ">");

        // Reset Thread Name
        Thread.currentThread().setName("Guest-"+id);
        sender.renameThread("Guest-Response-"+id);

        isAdmin = isManufacturer = false;
        user = null;

        return new Data(Data.LOGOUT, new JSONObject(), null);
    }

    @Override
    public void close() throws IOException {
        // Removing this Client from client list
        clientList.remove(this);

        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException ignored) {
        } finally {
            socket.close();
        }

        logger.info("Closed Client");
    }
}
