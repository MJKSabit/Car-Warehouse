package com.github.mjksabit.warehouse.server.Network;

import com.github.mjksabit.warehouse.server.data.DB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable, Closeable {

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

        new Thread(this).start();
    }

    private Data createResponse(Data request) throws JSONException {
        switch (request.getTYPE()) {
            case Data.LOGIN: return login(request);
            default: {
                JSONObject object = new JSONObject();
                object.put(Data.INFO, "Unknown Request!");
                return new Data(Data.ERROR, object, null);
            }
        }
    }

    private Data login(Data request) throws JSONException {
        JSONObject jsonObject = request.getText();
        Data response =  DB.getInstance().login(
                jsonObject.optString(Data.LOGIN_USERNAME),
                jsonObject.optString(Data.LOGIN_PASSWORD));

        if (response.getTYPE().equals(Data.LOGIN_SUCCESS)){
            this.name = jsonObject.optString(Data.LOGIN_USERNAME);
            this.isManufacturer = true;
        }

        return response;
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
                var response = createResponse(request);
                sender.addToQueue(response);
            }
        } catch (JSONException | IOException e) {
            logger.error(e.getMessage());
        }

    }


    @Override
    public void close() throws IOException {
        inputStream.close();
        outputStream.close();

        socket.close();
    }
}
