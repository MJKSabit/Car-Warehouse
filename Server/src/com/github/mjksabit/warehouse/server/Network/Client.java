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

    private static final ArrayList<Client> clientList = new ArrayList<>();

    private static final Logger logger = LogManager.getLogger(Client.class);

    private final Socket socket;

    private ResponseSender sender;

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final DataInputStream in;
    private final DataOutputStream out;

    private boolean isManufacturer = false;
    private boolean isAdmin = false;

    private String user = null;

    public Client(Socket socket) throws IOException {
        this.socket = socket;

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        in = new DataInputStream(inputStream);
        out = new DataOutputStream(outputStream);

        clientList.add(this);

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
            for (var client : clientList)
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

        logger.info("run: Client Connected");

        try {
            while (true) {
                logger.info("run: Waiting For Request");
                var request = new Data(in);
                logger.info("run: New Request: " + request.getTYPE());

                var response = route(request);
                response.getText().put(Data.REQUEST_KEY, request.getTYPE());
                response.getText().put(Data.REMOVE_REQUESTER, true);
                logger.info("run: Response "+response.getTYPE()+":"+response.getText());

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
            case Data.GET_USERS: return getUsers(request);
            case Data.REMOVE_USER: return removeUser(request);

            case Data.LOGOUT:
                return logout(request);

            default: {
                JSONObject object = new JSONObject();
                object.put(Data.INFO, "Unknown Request!");
                return new Data(Data.ERROR, object, null);
            }
        }
    }

    private Data unauthorized() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(Data.INFO, "You are not authorized!");
        return new Data(Data.ERROR, object, null);
    }

    private Data removeUser(Data request) throws JSONException {
        if (!isAdmin)
            return unauthorized();

        DB.getInstance().removeUser(request.getText().optString(Data.USER));
        return new Data(Data.REMOVE_USER, new JSONObject(), null);
    }

    private Data getUsers(Data request) throws JSONException {
        JSONObject object = new JSONObject();
        String type = null;

        if (!isAdmin)
            return unauthorized();
        else {
            type = Data.GET_USERS;
            ArrayList<String> users = DB.getInstance().getUsers();
            object.put(Data.USER, new JSONArray(users));
        }

        return new Data(type, object, null);
    }

    private Data addUser(Data request) throws JSONException {
        JSONObject object = new JSONObject();
        String username = request.getText().getString(Data.LOGIN_USERNAME);

        if(isAdmin && DB.getInstance().addUser(username,
                request.getText().getString(Data.LOGIN_PASSWORD)))
            return new Data(Data.ADD_USER, object, null);

        return unauthorized();
    }

    private Data admin(Data request) throws JSONException {
        isAdmin = DB.getInstance().adminLogin(request.getText().optString(Data.LOGIN_PASSWORD));
        if(isAdmin) {
            logger.info("ADMIN logged in");
            user = "admin";
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
        if (!isManufacturer || !DB.getInstance().isManufacturer(user))
            return unauthorized();

        Data data = DB.getInstance().editCar(request);

        if (!data.getTYPE().equals(Data.ERROR)) {
            notifyAllCar(request.getText().optInt(Data.CAR_ID));
        }

        return data;
    }

    private Data addCar(Data request) throws JSONException {
        if (!isManufacturer || !DB.getInstance().isManufacturer(user))
            return unauthorized();

        int id = DB.getInstance().addCar(Car.fromData(request));
        notifyAllCar(id);
        return new Data(Data.ADD_CAR, new JSONObject(), null);
    }

    private Data removeCar(Data request) throws JSONException {
        if (!isManufacturer || !DB.getInstance().isManufacturer(user))
            return unauthorized();

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
            object.putOpt(Data.INFO, "Only Viewer can buy!");
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
            this.user = jsonObject.optString(Data.LOGIN_USERNAME);
            this.isManufacturer = true;
            Thread.currentThread().setName(user);
            sender.renameThread(user + "-Request");

            logger.info("Logged in <" + user + ">");
        }

        return response;
    }

    private Data logout(Data request) {
        logger.info("Logged out <" + user + ">");

        Thread.currentThread().setName("Unknown");
        sender.renameThread("Unknown-Request");

        isAdmin = isManufacturer = false;
        user = null;

        return new Data(Data.LOGOUT, new JSONObject(), null);
    }


    @Override
    public void close() throws IOException {
        clientList.remove(this);

        inputStream.close();
        outputStream.close();

        socket.close();
        logger.info("Closed Client");
    }
}
