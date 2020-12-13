package com.github.mjksabit.warehouse.server.Network;

import com.github.mjksabit.warehouse.server.Model.Car;
import com.github.mjksabit.warehouse.server.data.DB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable, Closeable {

    private static final ArrayList<Client> clients = new ArrayList<>();

    private static final Logger logger = LogManager.getLogger(Client.class);

    private final Socket socket;

    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    private ResponseSender sender;

    private boolean isManufacturer = false;
    private boolean isAdmin = false;

    private String username = null;

    public Client(Socket socket) throws IOException {
        this.socket = socket;

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        in = new DataInputStream(inputStream);
        out = new DataOutputStream(outputStream);

        clients.add(this);

        new Thread(this).start();
    }

    private static Data carUpdate(int id) throws JSONException {
        Car car = DB.getInstance().getCar(id);

        if (car != null)
            return car.toData(id);
        else {
            JSONObject object = new JSONObject();
            object.put(Data.CAR_ID, id);
            return new Data(Data.UPDATE_CAR, object, null);
        }
    }

    // null means deleted
    public static void notifyAllCar(int id) {
        try {
            var carData = carUpdate(id);
            for (var client : clients)
                if (!client.isAdmin)
                    client.sender.addToQueue(carData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        sender = new ResponseSender(this, out);

        Thread.currentThread().setName("Unknown");
        sender.renameThread("Unknown-Request");

        logger.info("Client Connected");

        try {
            while (true) {
                logger.info("Waiting For Request");
                var request = new Data(in);
                logger.info("REQUEST: " + request.getTYPE());

                var response = route(request);
                response.getText().put(Data.REQUEST_KEY, request.getTYPE());
                response.getText().put(Data.REMOVE_REQUESTER, true);

                sender.addToQueue(response);
            }
        } catch (JSONException | IOException e) {
            logger.error(e.getMessage());
        }

    }

    private Data route(Data request) throws JSONException {
        switch (request.getTYPE()) {
            case Data.LOGIN:
                return login(request);

            case Data.REMOVE_CAR:
                return removeCar(request);
            case Data.ADD_CAR:
                return addCar(request);
            case Data.EDIT_CAR:
                return editCar(request);

            case Data.VIEW_ALL:
                return viewCar(request);
            case Data.BUY_CAR:
                return buyCar(request);

            case Data.ADMIN: return admin(request);
            case Data.ADD_USER: return addUser(request);
//            case Data.REMOVE_USER: return removeUser(request);

            case Data.LOGOUT:
                return logout(request);

            default: {
                JSONObject object = new JSONObject();
                object.put(Data.INFO, "Unknown Request!");
                return new Data(Data.ERROR, object, null);
            }
        }
    }

    private Data addUser(Data request) throws JSONException {
        JSONObject object = new JSONObject();
        String username = request.getText().getString(Data.LOGIN_USERNAME);

        if(DB.getInstance().addUser(username,
                request.getText().getString(Data.LOGIN_PASSWORD)))
            return new Data(Data.ADD_USER, object, null);

        object.put(Data.INFO, "Can not add user: " + username);
        return new Data(Data.ERROR, object, null);
    }

    private Data admin(Data request) throws JSONException {
        isAdmin = DB.getInstance().adminLogin(request.getText().optString(Data.LOGIN_PASSWORD));
        if(isAdmin) {
            logger.info("ADMIN logged in");
            username = "admin";
            Thread.currentThread().setName("ADMIN");
            sender.renameThread("ADMIN-Request");

            return new Data(Data.ADMIN, new JSONObject(), null);
        }

        logger.info("ADMIN login error");

        JSONObject object = new JSONObject();
        object.put(Data.INFO, "Password mismatch!");
        return new Data(Data.ERROR, object, null);
    }

    private Data editCar(Data request) throws JSONException {
        if (!isManufacturer || !DB.getInstance().isManufacturer(username)) {
            JSONObject object = new JSONObject();
            object.putOpt(Data.INFO, "Viewer can't edit a car!");
            return new Data(Data.ERROR, object, null);
        }

        Data data = DB.getInstance().editCar(request);

        if (!data.getTYPE().equals(Data.ERROR)) {
            notifyAllCar(request.getText().optInt(Data.CAR_ID));
        }

        return data;
    }

    private Data addCar(Data request) throws JSONException {
        if (!isManufacturer || !DB.getInstance().isManufacturer(username)) {
            JSONObject object = new JSONObject();
            object.putOpt(Data.INFO, "Viewer can't add a car!");
            return new Data(Data.ERROR, object, null);
        }

        int id = DB.getInstance().addCar(Car.fromData(request));
        notifyAllCar(id);
        return new Data(Data.ADD_CAR, new JSONObject(), null);
    }

    private Data removeCar(Data request) throws JSONException {
        if (!isManufacturer || !DB.getInstance().isManufacturer(username)) {
            JSONObject object = new JSONObject();
            object.putOpt(Data.INFO, "Viewer can't remove a car!");
            return new Data(Data.ERROR, object, null);
        }

        boolean isRemoved = DB.getInstance().removeCar(request.getText().optInt(Data.CAR_ID));
        String type;
        JSONObject object = new JSONObject();

        if (isRemoved)
            type = Data.REMOVE_CAR;
        else {
            type = Data.ERROR;
            object.put(Data.INFO, "Car can not be deleted.");
        }
        notifyAllCar(request.getText().optInt(Data.CAR_ID));
        return new Data(type, object, null);
    }

    private Data buyCar(Data request) throws JSONException {
        Data data = DB.getInstance().buyCar(request.getText().optInt(Data.CAR_ID));

        if (isManufacturer) {
            JSONObject object = new JSONObject();
            object.putOpt(Data.INFO, "Manufacturer can't buy.");
            return new Data(Data.ERROR, object, null);
        }

        if (!data.getTYPE().equals(Data.ERROR)) {
            notifyAllCar(request.getText().optInt(Data.CAR_ID));
        }

        return data;
    }

    private Data viewCar(Data request) {
        new Thread(() -> {
            try {
                var cars = DB.getInstance().allCars();
                for (var car : cars)
                    sender.addToQueue(car);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();

        return new Data(Data.VIEW_ALL, new JSONObject(), null);
    }

    private Data login(Data request) throws JSONException {
        JSONObject jsonObject = request.getText();
        Data response = DB.getInstance().login(
                jsonObject.optString(Data.LOGIN_USERNAME),
                jsonObject.optString(Data.LOGIN_PASSWORD));

        response.getText().put(Data.REQUEST_KEY, request.getTYPE());

        if (response.getTYPE().equals(Data.LOGIN)) {
            this.username = jsonObject.optString(Data.LOGIN_USERNAME);
            this.isManufacturer = true;
            Thread.currentThread().setName(username);
            sender.renameThread(username + "-Request");

            logger.info("Logged in <" + username + ">");
        }

        return response;
    }

    private Data logout(Data request) {
        logger.info("Logged out <" + username + ">");

        Thread.currentThread().setName("Unknown");
        sender.renameThread("Unknown-Request");

        isAdmin = isManufacturer = false;
        username = null;

        return new Data(Data.LOGOUT, new JSONObject(), null);
    }


    @Override
    public void close() throws IOException {
        clients.remove(this);

        inputStream.close();
        outputStream.close();

        socket.close();
        logger.info("Closed Client");
    }
}
