package com.github.mjksabit.warehouse.server.Network;

import com.github.mjksabit.warehouse.server.Model.Car;
import com.github.mjksabit.warehouse.server.Model.User;
import com.github.mjksabit.warehouse.server.data.DB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable, Closeable {

    private static ArrayList<Client> clients = new ArrayList<>();

    private static Logger logger = LogManager.getLogger(Client.class);

    private Socket socket;

    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    private ResponseSender sender;

    private boolean isManufacturer  = false;
    private boolean isAdmin         = false;

    private String name = null;

    public Client(Socket socket) throws IOException {
        this.socket = socket;

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        in = new DataInputStream(inputStream);
        out = new DataOutputStream(outputStream);

        clients.add(this);

        new Thread(this).start();
    }

    @Override
    public void run() {
        sender = new ResponseSender(this, out);

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

//    private static Data carUpdate(int id, Car car) {
//        JSONObject object = new JSONObject();
//        try {
//            object.put(Data.CAR_ID, id);
//            object.put(Data.CAR, Util.jsonFromCar(car));
//        } catch (JSONException e) {}
//        return new Data(Data.UPDATE_CAR, object, null);
//    }
//
//    // null means deleted
//    public static void notifyAllCar(int id, Car car) {
//        Data carData = carUpdate(id, car);
//
//        for (var client : clients)
//            if (!client.isAdmin)
//                client.sender.addToQueue(carData);
//    }

//    // Implement
//    public static void notifyAllUsers(int id, User user) {
//
//    }

    private Data route(Data request) throws JSONException {
        switch (request.getTYPE()) {
            case Data.LOGIN: return login(request);

//            case Data.REMOVE_CAR: return removeCar(request);
//            case Data.ADD_CAR: return addCar(request);
//            case Data.EDIT_CAR: return editCar(request);
//
            case Data.VIEW_ALL: return viewCar(request);
//            case Data.BUY_CAR: return buyCar(request);
//
//            case Data.ADMIN: return admin(request);
//            case Data.ADD_USER: return addUser(request);
//            case Data.REMOVE_USER: return removeUser(request);
//
            case Data.LOGOUT: return logout(request);

            default: {
                JSONObject object = new JSONObject();
                object.put(Data.INFO, "Unknown Request!");
                return new Data(Data.ERROR, object, null);
            }
        }
    }

    private Data viewCar(Data request) {
        new Thread( () -> {
            try {
                var cars = DB.getInstance().allCars();
                for (var car: cars)
                    sender.addToQueue(car);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();

        return new Data(Data.VIEW_ALL, new JSONObject(), null);
    }

    private Data login(Data request) throws JSONException {
        JSONObject jsonObject = request.getText();
        Data response =  DB.getInstance().login(
                jsonObject.optString(Data.LOGIN_USERNAME),
                jsonObject.optString(Data.LOGIN_PASSWORD));

        response.getText().put(Data.REQUEST_KEY, request.getTYPE());

        if (response.getTYPE().equals(Data.LOGIN)){
            this.name = jsonObject.optString(Data.LOGIN_USERNAME);
            this.isManufacturer = true;
            Thread.currentThread().setName(name);
            sender.renameThread(name+"-Request");

            logger.info("Logged in <" + name + ">");
        }

        return response;
    }

    private Data logout(Data request) {
        logger.info("Logged out <" + name + ">");
        isAdmin = isManufacturer = false;
        name = null;

        return new Data(Data.LOGOUT, null, null);
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
